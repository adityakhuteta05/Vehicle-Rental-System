package com.drivesense.dto;

import com.drivesense.enums.BootSize;
import com.drivesense.enums.TripType;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VibeRequest {

    private TripType tripType = TripType.FAMILY_ROAD_TRIP;
    private int passengers = 4;
    private BootSize luggage = BootSize.M;
    private BigDecimal maxDailyBudget = new BigDecimal("4500");

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    public VibeRequest() {
        this.startTime = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        this.endTime = LocalDateTime.now().plusDays(3).withHour(18).withMinute(0);
    }

    public TripType getTripType() {
        return tripType;
    }

    public void setTripType(TripType tripType) {
        this.tripType = tripType;
    }

    public int getPassengers() {
        return passengers;
    }

    public void setPassengers(int passengers) {
        this.passengers = passengers;
    }

    public BootSize getLuggage() {
        return luggage;
    }

    public void setLuggage(BootSize luggage) {
        this.luggage = luggage;
    }

    public BigDecimal getMaxDailyBudget() {
        return maxDailyBudget;
    }

    public void setMaxDailyBudget(BigDecimal maxDailyBudget) {
        this.maxDailyBudget = maxDailyBudget;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
