package com.drivesense.enums;

public enum ReportStage {
    PICKUP("Pickup Inspection"),
    RETURN("Return Inspection");

    private final String displayName;

    ReportStage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
