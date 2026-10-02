package com.drivesense.pricing;

import java.math.BigDecimal;

public class DriverAndExtrasRule implements PricingRule {

    private static final BigDecimal DRIVER_PER_DAY = new BigDecimal("800.00");
    private static final BigDecimal INSURANCE_PER_DAY = new BigDecimal("350.00");
    private static final BigDecimal CHILD_SEAT_PER_DAY = new BigDecimal("200.00");

    @Override
    public void apply(PricingContext context, PriceBreakdownBuilder builder) {
        long days = context.getRentalDays();

        if (context.isDriverRequired()) {
            BigDecimal totalDriver = DRIVER_PER_DAY.multiply(BigDecimal.valueOf(days));
            builder.addSurcharge("Chauffeur / Professional Driver",
                    "Professional chauffeur service for " + days + " day(s)",
                    totalDriver);
        }

        if (context.isInsuranceRequired()) {
            BigDecimal totalInsurance = INSURANCE_PER_DAY.multiply(BigDecimal.valueOf(days));
            builder.addSurcharge("Comprehensive Zero-Deductible Insurance",
                    "Full accidental & zero-liability coverage for " + days + " day(s)",
                    totalInsurance);
        }

        if (context.isChildSeatRequired()) {
            BigDecimal totalChildSeat = CHILD_SEAT_PER_DAY.multiply(BigDecimal.valueOf(days));
            builder.addSurcharge("Child Safety Seat",
                    "Certified ISOFIX child seat for " + days + " day(s)",
                    totalChildSeat);
        }
    }
}
