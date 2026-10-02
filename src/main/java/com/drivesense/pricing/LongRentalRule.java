package com.drivesense.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LongRentalRule implements PricingRule {

    private final int minDays;
    private final double percent;

    public LongRentalRule(int minDays, double percent) {
        this.minDays = minDays;
        this.percent = percent;
    }

    @Override
    public void apply(PricingContext context, PriceBreakdownBuilder builder) {
        long days = context.getRentalDays();
        if (days >= minDays) {
            BigDecimal base = context.getCar().getPricePerDay().multiply(BigDecimal.valueOf(days));
            BigDecimal discount = base.multiply(BigDecimal.valueOf(percent))
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            builder.addDiscount("Long Rental Saver (" + minDays + "+ Days)",
                    (int) percent + "% discount on base fare for extended trip",
                    discount);
        }
    }
}
