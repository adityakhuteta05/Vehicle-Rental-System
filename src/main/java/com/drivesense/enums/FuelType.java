package com.drivesense.enums;

public enum FuelType {
    PETROL("Petrol", 142),     // g CO2 / km benchmark
    DIESEL("Diesel", 168),     // g CO2 / km benchmark
    CNG("CNG", 110),          // g CO2 / km benchmark
    EV("Electric", 0);        // 0 direct tailpipe emission

    private final String displayName;
    private final int defaultCo2PerKm;

    FuelType(String displayName, int defaultCo2PerKm) {
        this.displayName = displayName;
        this.defaultCo2PerKm = defaultCo2PerKm;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultCo2PerKm() {
        return defaultCo2PerKm;
    }
}
