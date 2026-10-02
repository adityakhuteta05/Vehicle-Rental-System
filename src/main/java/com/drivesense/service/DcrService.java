package com.drivesense.service;

import com.drivesense.dto.DcrComparisonDto;
import com.drivesense.entity.Booking;
import com.drivesense.entity.Car;
import com.drivesense.entity.ConditionReport;
import com.drivesense.enums.BookingStatus;
import com.drivesense.enums.ReportStage;
import com.drivesense.event.DamageReportedEvent;
import com.drivesense.repository.BookingRepository;
import com.drivesense.repository.ConditionReportRepository;
import com.drivesense.util.DateUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DcrService {

    private static final BigDecimal FUEL_PER_PERCENT_PENALTY = new BigDecimal("25.00"); // Rs. 25 per 1% fuel missing
    private static final BigDecimal DAMAGE_ZONE_PENALTY = new BigDecimal("2500.00");    // Rs. 2500 per new damaged zone

    private final ConditionReportRepository conditionReportRepository;
    private final BookingRepository bookingRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DcrService(ConditionReportRepository conditionReportRepository,
                      BookingRepository bookingRepository,
                      ApplicationEventPublisher eventPublisher) {
        this.conditionReportRepository = conditionReportRepository;
        this.bookingRepository = bookingRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ConditionReport submitPickupReport(Long bookingId, int fuelPercent, int odometerKm,
                                              List<String> damageZones, String notes) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        Optional<ConditionReport> existing = conditionReportRepository.findByBookingIdAndStage(bookingId, ReportStage.PICKUP);
        ConditionReport report = existing.orElseGet(ConditionReport::new);

        String zonesJoined = damageZones != null ? String.join(",", damageZones) : "";
        report.setBooking(booking);
        report.setStage(ReportStage.PICKUP);
        report.setFuelPercent(fuelPercent);
        report.setOdometerKm(odometerKm);
        report.setDamageZones(zonesJoined);
        report.setNotes(notes);
        report.setCreatedAt(LocalDateTime.now());

        ConditionReport saved = conditionReportRepository.save(report);

        // Transition booking to ACTIVE (Trip in progress)
        booking.setStatus(BookingStatus.ACTIVE);
        bookingRepository.save(booking);

        return saved;
    }

    @Transactional
    public DcrComparisonDto submitReturnReport(Long bookingId, int fuelPercent, int odometerKm,
                                               List<String> damageZones, String notes) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        Optional<ConditionReport> existing = conditionReportRepository.findByBookingIdAndStage(bookingId, ReportStage.RETURN);
        ConditionReport report = existing.orElseGet(ConditionReport::new);

        String zonesJoined = damageZones != null ? String.join(",", damageZones) : "";
        report.setBooking(booking);
        report.setStage(ReportStage.RETURN);
        report.setFuelPercent(fuelPercent);
        report.setOdometerKm(odometerKm);
        report.setDamageZones(zonesJoined);
        report.setNotes(notes);
        report.setCreatedAt(LocalDateTime.now());

        conditionReportRepository.save(report);

        booking.setActualReturnTime(LocalDateTime.now());
        booking.setStatus(BookingStatus.COMPLETED);

        // Run side-by-side comparison and calculate extra charges
        DcrComparisonDto comparison = compareReports(bookingId);
        booking.setExtraCharges(comparison.getTotalExtraCharge());
        bookingRepository.save(booking);

        if (comparison.hasNewDamage()) {
            eventPublisher.publishEvent(new DamageReportedEvent(booking, comparison.getNewDamageZones()));
        }

        return comparison;
    }

    public DcrComparisonDto compareReports(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        ConditionReport pickup = conditionReportRepository.findByBookingIdAndStage(bookingId, ReportStage.PICKUP)
                .orElse(null);
        ConditionReport returnReport = conditionReportRepository.findByBookingIdAndStage(bookingId, ReportStage.RETURN)
                .orElse(null);

        if (pickup == null || returnReport == null) {
            return new DcrComparisonDto(pickup, returnReport, 0, 0, 0, BigDecimal.ZERO,
                    0, BigDecimal.ZERO, new ArrayList<>(), BigDecimal.ZERO, BigDecimal.ZERO);
        }

        Car car = booking.getCar();
        long rentalDays = DateUtil.calculateRentalDays(booking.getStartTime(), booking.getEndTime());
        int allowedKm = (int) (rentalDays * car.getKmPerDayLimit());

        int travelledKm = Math.max(0, returnReport.getOdometerKm() - pickup.getOdometerKm());
        int extraKm = Math.max(0, travelledKm - allowedKm);
        BigDecimal extraKmCharge = car.getExtraKmRate().multiply(BigDecimal.valueOf(extraKm))
                .setScale(2, RoundingMode.HALF_UP);

        int fuelShortfallPercent = Math.max(0, pickup.getFuelPercent() - returnReport.getFuelPercent());
        BigDecimal fuelCharge = FUEL_PER_PERCENT_PENALTY.multiply(BigDecimal.valueOf(fuelShortfallPercent))
                .setScale(2, RoundingMode.HALF_UP);

        List<String> pickupZones = pickup.getDamageZoneList();
        List<String> returnZones = returnReport.getDamageZoneList();

        List<String> newDamageZones = returnZones.stream()
                .filter(zone -> !pickupZones.contains(zone))
                .collect(Collectors.toList());

        BigDecimal damageCharge = DAMAGE_ZONE_PENALTY.multiply(BigDecimal.valueOf(newDamageZones.size()))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalExtra = extraKmCharge.add(fuelCharge).add(damageCharge).setScale(2, RoundingMode.HALF_UP);

        return new DcrComparisonDto(pickup, returnReport, travelledKm, allowedKm, extraKm, extraKmCharge,
                fuelShortfallPercent, fuelCharge, newDamageZones, damageCharge, totalExtra);
    }

    public Optional<ConditionReport> getReport(Long bookingId, ReportStage stage) {
        return conditionReportRepository.findByBookingIdAndStage(bookingId, stage);
    }
}
