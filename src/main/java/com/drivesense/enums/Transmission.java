package com.drivesense.enums;

public enum Transmission {
    MANUAL("Manual"),
    AUTOMATIC("Automatic");

    private final String displayName;

    Transmission(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
