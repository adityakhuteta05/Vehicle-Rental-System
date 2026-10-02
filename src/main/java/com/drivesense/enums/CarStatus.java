package com.drivesense.enums;

public enum CarStatus {
    AVAILABLE("Available"),
    MAINTENANCE("In Maintenance"),
    RETIRED("Retired");

    private final String displayName;

    CarStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
