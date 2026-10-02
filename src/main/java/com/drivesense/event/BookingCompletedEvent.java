package com.drivesense.event;

import com.drivesense.entity.Booking;

public class BookingCompletedEvent {

    private final Booking booking;
    private final boolean onTime;
    private final boolean noDamage;

    public BookingCompletedEvent(Booking booking, boolean onTime, boolean noDamage) {
        this.booking = booking;
        this.onTime = onTime;
        this.noDamage = noDamage;
    }

    public Booking getBooking() {
        return booking;
    }

    public boolean isOnTime() {
        return onTime;
    }

    public boolean isNoDamage() {
        return noDamage;
    }
}
