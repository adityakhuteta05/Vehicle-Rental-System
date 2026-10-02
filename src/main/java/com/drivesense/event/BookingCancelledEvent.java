package com.drivesense.event;

import com.drivesense.entity.Booking;

public class BookingCancelledEvent {

    private final Booking booking;
    private final boolean within24Hours;

    public BookingCancelledEvent(Booking booking, boolean within24Hours) {
        this.booking = booking;
        this.within24Hours = within24Hours;
    }

    public Booking getBooking() {
        return booking;
    }

    public boolean isWithin24Hours() {
        return within24Hours;
    }
}
