package com.drivesense.pricing;

import com.drivesense.entity.Car;
import com.drivesense.entity.User;
import com.drivesense.enums.TrustTier;

import java.time.Duration;
import java.time.LocalDateTime;

public class PricingContext {

    private final Car car;
    private final User user;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final LocalDateTime bookingTime;
    private final boolean driverRequired;
    private final boolean insuranceRequired;
    private final boolean childSeatRequired;

    public PricingContext(Car car, User user, LocalDateTime startTime, LocalDateTime endTime,
                          LocalDateTime bookingTime, boolean driverRequired, boolean insuranceRequired, boolean childSeatRequired) {
        this.car = car;
        this.user = user;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bookingTime = bookingTime != null ? bookingTime : LocalDateTime.now();
        this.driverRequired = driverRequired;
        this.insuranceRequired = insuranceRequired;
        this.childSeatRequired = childSeatRequired;
    }

    public long getTotalHours() {
        Duration duration = Duration.between(startTime, endTime);
        long hours = duration.toHours();
        if (duration.toMinutesPart() > 0) {
            hours += 1;
        }
        return Math.max(1, hours);
    }

    public long getRentalDays() {
        long hours = getTotalHours();
        long days = hours / 24;
        if (hours % 24 > 0) {
            days += 1;
        }
        return Math.max(1, days);
    }

    public long getAdvanceBookingDays() {
        Duration duration = Duration.between(bookingTime, startTime);
        return Math.max(0, duration.toDays());
    }

    public TrustTier getTrustTier() {
        return user != null ? user.getTrustTier() : TrustTier.SILVER;
    }

    public Car getCar() {
        return car;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public boolean isDriverRequired() {
        return driverRequired;
    }

    public boolean isInsuranceRequired() {
        return insuranceRequired;
    }

    public boolean isChildSeatRequired() {
        return childSeatRequired;
    }
}
