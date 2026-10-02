package com.drivesense.dto;

import com.drivesense.entity.Car;

public class VibeMatchResult {

    private final Car car;
    private final int matchPercent;
    private final String reason;
    private final double seatScore;
    private final double luggageScore;
    private final double budgetScore;
    private final double tripTypeScore;

    public VibeMatchResult(Car car, int matchPercent, String reason,
                           double seatScore, double luggageScore, double budgetScore, double tripTypeScore) {
        this.car = car;
        this.matchPercent = matchPercent;
        this.reason = reason;
        this.seatScore = seatScore;
        this.luggageScore = luggageScore;
        this.budgetScore = budgetScore;
        this.tripTypeScore = tripTypeScore;
    }

    public Car getCar() {
        return car;
    }

    public int getMatchPercent() {
        return matchPercent;
    }

    public String getReason() {
        return reason;
    }

    public double getSeatScore() {
        return seatScore;
    }

    public double getLuggageScore() {
        return luggageScore;
    }

    public double getBudgetScore() {
        return budgetScore;
    }

    public double getTripTypeScore() {
        return tripTypeScore;
    }
}
