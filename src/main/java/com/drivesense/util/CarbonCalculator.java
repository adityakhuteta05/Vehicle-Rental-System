package com.drivesense.util;

import com.drivesense.entity.Car;
import com.drivesense.enums.FuelType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CarbonCalculator {

    /**
     * Estimates carbon footprint in Kilograms (kg CO2)
     * co2GPerKm * distanceKm / 1000
     */
    public static BigDecimal estimateCo2Kg(Car car, int distanceKm) {
        if (car == null || distanceKm <= 0) {
            return BigDecimal.ZERO;
        }

        if (car.getFuelType() == FuelType.EV) {
            return BigDecimal.ZERO;
        }

        int gPerKm = car.getCo2GPerKm() > 0 ? car.getCo2GPerKm() : car.getFuelType().getDefaultCo2PerKm();
        double totalGrams = (double) gPerKm * distanceKm;
        double kg = totalGrams / 1000.0;

        return BigDecimal.valueOf(kg).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates benchmark savings compared to standard petrol car (142 g/km)
     */
    public static BigDecimal estimateCo2SavedKg(Car car, int distanceKm) {
        if (car == null || distanceKm <= 0) {
            return BigDecimal.ZERO;
        }

        double benchmarkGrams = 142.0 * distanceKm;
        int currentGPerKm = car.getFuelType() == FuelType.EV ? 0 : car.getCo2GPerKm();
        double currentGrams = (double) currentGPerKm * distanceKm;

        double savedKg = Math.max(0, (benchmarkGrams - currentGrams) / 1000.0);
        return BigDecimal.valueOf(savedKg).setScale(2, RoundingMode.HALF_UP);
    }
}
