package com.drivesense.enums;

public enum TripType {
    FAMILY_ROAD_TRIP("Family Road Trip", "Spacious comfort with ample seating and boot space"),
    WEDDING("Wedding / Premium", "Luxury and executive style to make a grand entrance"),
    SOLO_WEEKEND("Solo / Couple Getaway", "Nimble, fun drive with great handling and efficiency"),
    OFFICE_COMMUTE("City / Daily Commute", "Compact, economical, easy to park, and low emissions"),
    HILL_DRIVE("Hills / Adventure", "High ground clearance, torque, and solid traction"),
    AIRPORT_TRANSFER("Airport Transfer", "Reliable, comfortable, with priority boot capacity");

    private final String title;
    private final String description;

    TripType(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
