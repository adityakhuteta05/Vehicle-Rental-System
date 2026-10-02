package com.drivesense.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EarlyBirdRule implements PricingRule {

    private final int minAdvanceDays;
    private final double percent;

    public EarlyBirdRule(int minAdvanceDays, double percent) {
        this.minAdvanceDays = minAdvanceDays;
        this.percent = percent;
    }

    @Override
    public void apply(PricingContext context, PriceBreakdownBuilder builder) {
        long advanceDays = context.getAdvanceBookingDays();
        if (advanceDays >= minAdvanceDays) {
            BigDecimal base = context.getCar().getPricePerDay().multiply(BigDecimal.valueOf(context.getRentalDays()));
            BigDecimal discount = base.multiply(BigDecimal.valueOf(percent))
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            builder.addDiscount("Early Bird Discount (" + (int) percent + "%)",
                    "Booked " + advanceDays + " days ahead of time",
                    discount);
        }
    }
}
