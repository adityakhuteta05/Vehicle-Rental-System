package com.drivesense.service;

import com.drivesense.entity.Car;
import com.drivesense.entity.User;
import com.drivesense.pricing.PriceBreakdown;
import com.drivesense.pricing.PriceBreakdownBuilder;
import com.drivesense.pricing.PricingContext;
import com.drivesense.pricing.PricingRule;
import com.drivesense.pricing.PricingRuleLoader;
import com.drivesense.util.CarbonCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PricingService {

    private final PricingRuleLoader ruleLoader;

    public PricingService(PricingRuleLoader ruleLoader) {
        this.ruleLoader = ruleLoader;
    }

    public PriceBreakdown calculatePrice(PricingContext context) {
        long hours = context.getTotalHours();
        long days = context.getRentalDays();

        // Formula from PRD: Base = pricePerDay x numberOfDays (hourly rate for under 24 h)
        BigDecimal baseAmount;
        if (hours < 24) {
            BigDecimal hourlyRate = context.getCar().getPricePerHour();
            baseAmount = hourlyRate.multiply(BigDecimal.valueOf(hours));
        } else {
            BigDecimal dailyRate = context.getCar().getPricePerDay();
            baseAmount = dailyRate.multiply(BigDecimal.valueOf(days));
        }
        baseAmount = baseAmount.setScale(2, RoundingMode.HALF_UP);

        // Estimate typical trip distance: days * 120km
        int estimatedDistanceKm = (int) Math.max(80, days * 150);
        BigDecimal estCo2Kg = CarbonCalculator.estimateCo2Kg(context.getCar(), estimatedDistanceKm);

        PriceBreakdownBuilder builder = new PriceBreakdownBuilder()
                .rentalTime(days, hours)
                .baseAmount(baseAmount)
                .gstPercent(ruleLoader.getGstPercent())
                .carbonEstimation(estimatedDistanceKm, estCo2Kg);

        // Apply all loaded Strategy rules
        List<PricingRule> rules = ruleLoader.getActiveRules();
        for (PricingRule rule : rules) {
            rule.apply(context, builder);
        }

        return builder.build();
    }

    public PriceBreakdown calculatePrice(Car car, User user, LocalDateTime start, LocalDateTime end,
                                         boolean driver, boolean insurance, boolean childSeat) {
        PricingContext context = new PricingContext(car, user, start, end, LocalDateTime.now(), driver, insurance, childSeat);
        return calculatePrice(context);
    }
}
