package com.drivesense.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class FestivalRule implements PricingRule {

    public static class DateRange {
        private final LocalDate from;
        private final LocalDate to;

        public DateRange(LocalDate from, LocalDate to) {
            this.from = from;
            this.to = to;
        }

        public boolean overlaps(LocalDate start, LocalDate end) {
            return !start.isAfter(to) && !end.isBefore(from);
        }
    }

    private final double percent;
    private final List<DateRange> ranges;

    public FestivalRule(double percent, List<DateRange> ranges) {
        this.percent = percent;
        this.ranges = ranges;
    }

    @Override
    public void apply(PricingContext context, PriceBreakdownBuilder builder) {
        LocalDate start = context.getStartTime().toLocalDate();
        LocalDate end = context.getEndTime().toLocalDate();

        boolean inFestival = ranges.stream().anyMatch(range -> range.overlaps(start, end));

        if (inFestival) {
            BigDecimal base = context.getCar().getPricePerDay().multiply(BigDecimal.valueOf(context.getRentalDays()));
            BigDecimal festivalCharge = base.multiply(BigDecimal.valueOf(percent))
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            builder.addSurcharge("Festival Season Surge (+" + (int) percent + "%)",
                    "Special holiday / festive rush pricing",
                    festivalCharge);
        }
    }
}
