package com.drivesense.event;

import com.drivesense.entity.Booking;

public class LateReturnEvent {

    private final Booking booking;
    private final long hoursLate;

    public LateReturnEvent(Booking booking, long hoursLate) {
        this.booking = booking;
        this.hoursLate = hoursLate;
    }

    public Booking getBooking() {
        return booking;
    }

    public long getHoursLate() {
        return hoursLate;
    }
}
