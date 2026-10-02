package com.drivesense.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceBreakdownBuilder {

    private final PriceBreakdown breakdown = new PriceBreakdown();

    public PriceBreakdownBuilder rentalTime(long days, long hours) {
        breakdown.setRentalDays(days);
        breakdown.setRentalHours(hours);
        return this;
    }

    public PriceBreakdownBuilder baseAmount(BigDecimal base) {
        breakdown.setBaseAmount(base.setScale(2, RoundingMode.HALF_UP));
        return this;
    }

    public PriceBreakdownBuilder addSurcharge(String name, String description, BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            breakdown.getSurcharges().add(new PriceBreakdown.PriceItem(name, description, amount));
        }
        return this;
    }

    public PriceBreakdownBuilder addDiscount(String name, String description, BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            breakdown.getDiscounts().add(new PriceBreakdown.PriceItem(name, description, amount));
        }
        return this;
    }

    public PriceBreakdownBuilder gstPercent(BigDecimal gstPercent) {
        breakdown.setGstPercent(gstPercent);
        return this;
    }

    public PriceBreakdownBuilder deposit(BigDecimal deposit) {
        breakdown.setDepositAmount(deposit != null ? deposit.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        return this;
    }

    public PriceBreakdownBuilder carbonEstimation(Integer distanceKm, BigDecimal co2Kg) {
        breakdown.setEstDistanceKm(distanceKm);
        breakdown.setEstCo2Kg(co2Kg != null ? co2Kg.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        return this;
    }

    public PriceBreakdown build() {
        BigDecimal totalSurcharges = breakdown.getSurcharges().stream()
                .map(PriceBreakdown.PriceItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        breakdown.setTotalSurcharges(totalSurcharges);

        BigDecimal totalDiscounts = breakdown.getDiscounts().stream()
                .map(PriceBreakdown.PriceItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        breakdown.setTotalDiscounts(totalDiscounts);

        BigDecimal base = breakdown.getBaseAmount() != null ? breakdown.getBaseAmount() : BigDecimal.ZERO;
        BigDecimal subtotal = base.add(totalSurcharges).subtract(totalDiscounts);
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            subtotal = BigDecimal.ZERO;
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        breakdown.setSubtotal(subtotal);

        BigDecimal gst = subtotal.multiply(breakdown.getGstPercent())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        breakdown.setGstAmount(gst);

        BigDecimal total = subtotal.add(gst).setScale(2, RoundingMode.HALF_UP);
        breakdown.setTotalAmount(total);

        return breakdown;
    }
}
