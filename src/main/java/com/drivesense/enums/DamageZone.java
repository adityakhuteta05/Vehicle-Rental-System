package com.drivesense.enums;

public enum DamageZone {
    FRONT("Front Bumper & Hood"),
    REAR("Rear Bumper & Trunk"),
    LEFT("Left Panels & Doors"),
    RIGHT("Right Panels & Doors"),
    ROOF("Roof & Windshield"),
    INTERIOR("Interior & Upholstery");

    private final String label;

    DamageZone(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
