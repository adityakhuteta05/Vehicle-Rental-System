package com.drivesense.service;

import com.drivesense.dto.BookingRequest;
import com.drivesense.entity.Booking;
import com.drivesense.entity.BookingExtra;
import com.drivesense.entity.Car;
import com.drivesense.entity.Location;
import com.drivesense.entity.Payment;
import com.drivesense.entity.User;
import com.drivesense.enums.BookingStatus;
import com.drivesense.enums.CarStatus;
import com.drivesense.event.BookingCancelledEvent;
import com.drivesense.exception.CarNotAvailableException;
import com.drivesense.exception.ResourceNotFoundException;
import com.drivesense.pricing.PriceBreakdown;
import com.drivesense.repository.BookingRepository;
import com.drivesense.repository.CarRepository;
import com.drivesense.repository.LocationRepository;
import com.drivesense.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final LocationRepository locationRepository;
    private final PaymentRepository paymentRepository;
    private final PricingService pricingService;
    private final ApplicationEventPublisher eventPublisher;

    public BookingService(BookingRepository bookingRepository,
                          CarRepository carRepository,
                          LocationRepository locationRepository,
                          PaymentRepository paymentRepository,
                          PricingService pricingService,
                          ApplicationEventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.carRepository = carRepository;
        this.locationRepository = locationRepository;
        this.paymentRepository = paymentRepository;
        this.pricingService = pricingService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Concurrency-safe booking creation using PESSIMISTIC_WRITE lock
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Booking createBooking(User user, BookingRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Pickup and return date-time are required.");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("Return time must be after pickup time.");
        }
        if (request.getStartTime().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new IllegalArgumentException("Pickup time cannot be in the past.");
        }

        // Section 12: Concurrency safe - Lock car row with PESSIMISTIC_WRITE while checking and inserting
        Car car = carRepository.findByIdWithLock(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car not found: " + request.getCarId()));

        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new CarNotAvailableException("Car is currently " + car.getStatus().getDisplayName() + " and cannot be booked.");
        }

        long conflicts = bookingRepository.countOverlaps(car.getId(), request.getStartTime(), request.getEndTime());
        if (conflicts > 0) {
            throw new CarNotAvailableException("This car is already booked for the selected dates. Please choose different dates or another car.");
        }

        // Calculate transparent price breakdown
        PriceBreakdown breakdown = pricingService.calculatePrice(
                car, user, request.getStartTime(), request.getEndTime(),
                request.isDriverRequired(), request.isInsuranceRequired(), request.isChildSeatRequired()
        );

        Location location = null;
        if (request.getPickupLocationId() != null) {
            location = locationRepository.findById(request.getPickupLocationId()).orElse(null);
        }

        Booking booking = new Booking();
        booking.setBookingCode(generateBookingCode());
        booking.setUser(user);
        booking.setCar(car);
        booking.setPickupLocation(location);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTripType(request.getTripType());

        booking.setBaseAmount(breakdown.getBaseAmount());
        booking.setSurchargeAmount(breakdown.getTotalSurcharges());
        booking.setDiscountAmount(breakdown.getTotalDiscounts());
        booking.setTaxAmount(breakdown.getGstAmount());
        booking.setTotalAmount(breakdown.getTotalAmount());
        booking.setDepositAmount(breakdown.getDepositAmount());
        booking.setExtraCharges(BigDecimal.ZERO);
        booking.setEstDistanceKm(breakdown.getEstDistanceKm());
        booking.setEstCo2Kg(breakdown.getEstCo2Kg());
        booking.setCreatedAt(LocalDateTime.now());

        // Extras
        if (request.isDriverRequired()) {
            booking.getExtras().add(new BookingExtra(booking, "Professional Chauffeur", new BigDecimal("800.00").multiply(BigDecimal.valueOf(breakdown.getRentalDays()))));
        }
        if (request.isInsuranceRequired()) {
            booking.getExtras().add(new BookingExtra(booking, "Comprehensive Insurance", new BigDecimal("350.00").multiply(BigDecimal.valueOf(breakdown.getRentalDays()))));
        }
        if (request.isChildSeatRequired()) {
            booking.getExtras().add(new BookingExtra(booking, "Child Safety Seat", new BigDecimal("200.00").multiply(BigDecimal.valueOf(breakdown.getRentalDays()))));
        }

        Booking savedBooking = bookingRepository.save(booking);

        // Record simulated payment
        String txnRef = "TXN-" + System.currentTimeMillis() + "-" + (1000 + ThreadLocalRandom.current().nextInt(9000));
        Payment payment = new Payment(savedBooking, request.getPaymentMethod(), breakdown.getPayableNow(), "SUCCESS", txnRef);
        paymentRepository.save(payment);

        log.info("Booking created successfully: code={}, user={}, car={}, total={}",
                savedBooking.getBookingCode(), user.getEmail(), car.getDisplayName(), savedBooking.getTotalAmount());

        return savedBooking;
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, User user, boolean isAdmin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!isAdmin && !booking.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied: You do not own this booking.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Booking cannot be cancelled in status: " + booking.getStatus());
        }

        // PRD BK-7: Cancellation rules: free if more than 24 h before pickup, 20% fee otherwise
        Duration durationUntilPickup = Duration.between(LocalDateTime.now(), booking.getStartTime());
        boolean within24Hours = durationUntilPickup.toHours() < 24;

        if (within24Hours) {
            BigDecimal cancellationFee = booking.getTotalAmount().multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
            booking.setExtraCharges(cancellationFee);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updated = bookingRepository.save(booking);

        eventPublisher.publishEvent(new BookingCancelledEvent(updated, within24Hours));
        return updated;
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
    }

    public Booking getBookingByCode(String code) {
        return bookingRepository.findByBookingCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + code));
    }

    private String generateBookingCode() {
        int currentYear = Year.now().getValue();
        int randomSeq = ThreadLocalRandom.current().nextInt(100000, 999999);
        return String.format("DS-%d-%06d", currentYear, randomSeq);
    }
}
