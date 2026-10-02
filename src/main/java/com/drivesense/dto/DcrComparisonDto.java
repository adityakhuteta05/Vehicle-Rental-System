package com.drivesense.dto;

import com.drivesense.entity.ConditionReport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DcrComparisonDto {

    private final ConditionReport pickupReport;
    private final ConditionReport returnReport;
    private final int distanceTravelledKm;
    private final int allowedDistanceKm;
    private final int extraKm;
    private final BigDecimal extraKmCharge;
    private final int fuelShortfallPercent;
    private final BigDecimal fuelShortfallCharge;
    private final List<String> newDamageZones;
    private final BigDecimal damageCharge;
    private final BigDecimal totalExtraCharge;

    public DcrComparisonDto(ConditionReport pickupReport, ConditionReport returnReport,
                            int distanceTravelledKm, int allowedDistanceKm, int extraKm, BigDecimal extraKmCharge,
                            int fuelShortfallPercent, BigDecimal fuelShortfallCharge,
                            List<String> newDamageZones, BigDecimal damageCharge, BigDecimal totalExtraCharge) {
        this.pickupReport = pickupReport;
        this.returnReport = returnReport;
        this.distanceTravelledKm = distanceTravelledKm;
        this.allowedDistanceKm = allowedDistanceKm;
        this.extraKm = extraKm;
        this.extraKmCharge = extraKmCharge;
        this.fuelShortfallPercent = fuelShortfallPercent;
        this.fuelShortfallCharge = fuelShortfallCharge;
        this.newDamageZones = newDamageZones != null ? newDamageZones : new ArrayList<>();
        this.damageCharge = damageCharge;
        this.totalExtraCharge = totalExtraCharge;
    }

    public ConditionReport getPickupReport() {
        return pickupReport;
    }

    public ConditionReport getReturnReport() {
        return returnReport;
    }

    public int getDistanceTravelledKm() {
        return distanceTravelledKm;
    }

    public int getAllowedDistanceKm() {
        return allowedDistanceKm;
    }

    public int getExtraKm() {
        return extraKm;
    }

    public BigDecimal getExtraKmCharge() {
        return extraKmCharge;
    }

    public int getFuelShortfallPercent() {
        return fuelShortfallPercent;
    }

    public BigDecimal getFuelShortfallCharge() {
        return fuelShortfallCharge;
    }

    public List<String> getNewDamageZones() {
        return newDamageZones;
    }

    public BigDecimal getDamageCharge() {
        return damageCharge;
    }

    public BigDecimal getTotalExtraCharge() {
        return totalExtraCharge;
    }

    public boolean hasNewDamage() {
        return !newDamageZones.isEmpty();
    }
}
