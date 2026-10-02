package com.drivesense.enums;

public enum BookingStatus {
    PENDING("Pending Approval"),
    CONFIRMED("Confirmed"),
    ACTIVE("Trip in Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    BookingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
