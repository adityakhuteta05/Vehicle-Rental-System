package com.drivesense.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class WeekendRule implements PricingRule {

    private final double percent;

    public WeekendRule(double percent) {
        this.percent = percent;
    }

    @Override
    public void apply(PricingContext context, PriceBreakdownBuilder builder) {
        LocalDateTime start = context.getStartTime();
        LocalDateTime end = context.getEndTime();
        
        // Count how many days of the rental fall on Saturday or Sunday
        long weekendDays = 0;
        LocalDateTime curr = start;
        while (!curr.isAfter(end)) {
            if (curr.getDayOfWeek() == DayOfWeek.SATURDAY || curr.getDayOfWeek() == DayOfWeek.SUNDAY) {
                weekendDays++;
            }
            curr = curr.plusDays(1);
        }

        if (weekendDays > 0) {
            BigDecimal dailyRate = context.getCar().getPricePerDay();
            BigDecimal uplift = dailyRate.multiply(BigDecimal.valueOf(weekendDays))
                    .multiply(BigDecimal.valueOf(percent))
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            builder.addSurcharge("Weekend Uplift (+" + (int) percent + "%)",
                    weekendDays + " weekend day(s) at " + (int) percent + "% surcharge",
                    uplift);
        }
    }
}
