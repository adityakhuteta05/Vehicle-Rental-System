package com.drivesense.service;

import com.drivesense.entity.Booking;
import com.drivesense.entity.Car;
import com.drivesense.entity.OwnerProfile;
import com.drivesense.entity.User;
import com.drivesense.enums.BookingStatus;
import com.drivesense.enums.CarStatus;
import com.drivesense.exception.ResourceNotFoundException;
import com.drivesense.repository.BookingRepository;
import com.drivesense.repository.CarRepository;
import com.drivesense.repository.OwnerProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
public class OwnerService {

    private static final Logger log = LoggerFactory.getLogger(OwnerService.class);

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final OwnerProfileRepository ownerProfileRepository;

    public OwnerService(CarRepository carRepository,
                        BookingRepository bookingRepository,
                        OwnerProfileRepository ownerProfileRepository) {
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
        this.ownerProfileRepository = ownerProfileRepository;
    }

    public Map<String, Object> getDashboardStats(User owner) {
        Map<String, Object> stats = new HashMap<>();

        long totalCars = carRepository.countByOwnerId(owner.getId());
        long rentedCars = bookingRepository.countByCarOwnerIdAndStatus(owner.getId(), BookingStatus.ACTIVE);
        long availableCars = carRepository.countByOwnerIdAndStatus(owner.getId(), CarStatus.AVAILABLE);

        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        BigDecimal thisMonthEarnings = bookingRepository.sumOwnerEarningsSince(owner.getId(), startOfMonth);
        BigDecimal totalEarnings = bookingRepository.sumOwnerEarnings(owner.getId());

        // Utilisation rate
        double utilisationRate = totalCars > 0 ? ((double) rentedCars / totalCars) * 100 : 0.0;

        stats.put("totalCars", totalCars);
        stats.put("rentedCars", rentedCars);
        stats.put("availableCars", availableCars);
        stats.put("thisMonthEarnings", thisMonthEarnings);
        stats.put("totalEarnings", totalEarnings);
        stats.put("utilisationRate", Math.round(utilisationRate * 10.0) / 10.0);
        stats.put("recentBookings", bookingRepository.findByCarOwnerIdOrderByCreatedAtDesc(owner.getId()));

        return stats;
    }

    public List<Car> getOwnerVehicles(User owner) {
        return carRepository.findByOwnerId(owner.getId());
    }

    public Car getVehicleForOwner(Long carId, User owner) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + carId));

        if (car.getOwner() == null || !car.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this vehicle.");
        }
        return car;
    }

    @Transactional
    public Car saveVehicle(Car car, User owner) {
        car.setOwner(owner);
        if (car.getStatus() == null) {
            car.setStatus(CarStatus.AVAILABLE);
        }
        if (car.getAvgRating() == null) {
            car.setAvgRating(new BigDecimal("5.0"));
        }
        log.info("Owner {} saved vehicle {} ({})", owner.getEmail(), car.getDisplayName(), car.getRegNumber());
        return carRepository.save(car);
    }

    @Transactional
    public void deleteVehicle(Long carId, User owner) {
        Car car = getVehicleForOwner(carId, owner);
        car.setStatus(CarStatus.RETIRED);
        carRepository.save(car);
        log.info("Owner {} retired vehicle {}", owner.getEmail(), car.getDisplayName());
    }

    public List<Booking> getOwnerBookings(User owner, BookingStatus status) {
        if (status != null) {
            return bookingRepository.findByCarOwnerIdAndStatusOrderByCreatedAtDesc(owner.getId(), status);
        }
        return bookingRepository.findByCarOwnerIdOrderByCreatedAtDesc(owner.getId());
    }

    public Booking getBookingForOwner(Long bookingId, User owner) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (booking.getCar() == null || booking.getCar().getOwner() == null ||
                !booking.getCar().getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("Access denied: You do not manage this booking.");
        }
        return booking;
    }

    @Transactional
    public Booking updateBookingStatus(Long bookingId, User owner, BookingStatus newStatus) {
        Booking booking = getBookingForOwner(bookingId, owner);
        booking.setStatus(newStatus);

        if (newStatus == BookingStatus.ACTIVE) {
            booking.getCar().setStatus(CarStatus.MAINTENANCE); // or reserved
        } else if (newStatus == BookingStatus.COMPLETED) {
            booking.getCar().setStatus(CarStatus.AVAILABLE);
            booking.setActualReturnTime(LocalDateTime.now());

            // Credit owner profile earnings
            OwnerProfile profile = ownerProfileRepository.findByUserId(owner.getId()).orElse(null);
            if (profile != null && booking.getOwnerEarnings() != null) {
                profile.setTotalEarnings(profile.getTotalEarnings().add(booking.getOwnerEarnings()));
                ownerProfileRepository.save(profile);
            }
        } else if (newStatus == BookingStatus.CANCELLED) {
            booking.getCar().setStatus(CarStatus.AVAILABLE);
        }

        carRepository.save(booking.getCar());
        return bookingRepository.save(booking);
    }

    public OwnerProfile getOrCreateProfile(User owner) {
        return ownerProfileRepository.findByUserId(owner.getId())
                .orElseGet(() -> {
                    OwnerProfile profile = new OwnerProfile(owner, owner.getFullName() + " Fleet", "GOV-" + owner.getId(), owner.getAddress(), owner.getCity());
                    return ownerProfileRepository.save(profile);
                });
    }
}
