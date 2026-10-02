package com.drivesense.enums;

public enum CarType {
    HATCHBACK("Hatchback"),
    SEDAN("Sedan"),
    SUV("SUV"),
    LUXURY("Luxury"),
    EV("Electric Vehicle");

    private final String displayName;

    CarType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
