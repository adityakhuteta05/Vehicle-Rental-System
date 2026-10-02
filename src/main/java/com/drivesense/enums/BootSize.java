package com.drivesense.enums;

public enum BootSize {
    S("Small (1-2 bags)"),
    M("Medium (2-3 bags)"),
    L("Large (4+ bags)");

    private final String description;

    BootSize(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayName() {
        return description;
    }
}
