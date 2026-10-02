package com.drivesense.event;

import com.drivesense.entity.Booking;

import java.util.List;

public class DamageReportedEvent {

    private final Booking booking;
    private final List<String> newDamageZones;

    public DamageReportedEvent(Booking booking, List<String> newDamageZones) {
        this.booking = booking;
        this.newDamageZones = newDamageZones;
    }

    public Booking getBooking() {
        return booking;
    }

    public List<String> getNewDamageZones() {
        return newDamageZones;
    }
}
