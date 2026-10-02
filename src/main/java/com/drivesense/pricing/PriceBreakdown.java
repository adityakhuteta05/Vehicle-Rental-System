package com.drivesense.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class PriceBreakdown {

    public static class PriceItem {
        private final String name;
        private final String description;
        private final BigDecimal amount;

        public PriceItem(String name, String description, BigDecimal amount) {
            this.name = name;
            this.description = description;
            this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }

    private long rentalDays;
    private long rentalHours;
    private BigDecimal baseAmount = BigDecimal.ZERO;
    private final List<PriceItem> surcharges = new ArrayList<>();
    private final List<PriceItem> discounts = new ArrayList<>();
    private BigDecimal totalSurcharges = BigDecimal.ZERO;
    private BigDecimal totalDiscounts = BigDecimal.ZERO;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal gstPercent = new BigDecimal("18.00");
    private BigDecimal gstAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal depositAmount = BigDecimal.ZERO;
    private BigDecimal extraCharges = BigDecimal.ZERO;
    private Integer estDistanceKm;
    private BigDecimal estCo2Kg = BigDecimal.ZERO;

    public PriceBreakdown() {}

    public long getRentalDays() {
        return rentalDays;
    }

    public void setRentalDays(long rentalDays) {
        this.rentalDays = rentalDays;
    }

    public long getRentalHours() {
        return rentalHours;
    }

    public void setRentalHours(long rentalHours) {
        this.rentalHours = rentalHours;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public List<PriceItem> getSurcharges() {
        return surcharges;
    }

    public List<PriceItem> getDiscounts() {
        return discounts;
    }

    public BigDecimal getTotalSurcharges() {
        return totalSurcharges;
    }

    public void setTotalSurcharges(BigDecimal totalSurcharges) {
        this.totalSurcharges = totalSurcharges;
    }

    public BigDecimal getTotalDiscounts() {
        return totalDiscounts;
    }

    public void setTotalDiscounts(BigDecimal totalDiscounts) {
        this.totalDiscounts = totalDiscounts;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getGstPercent() {
        return gstPercent;
    }

    public void setGstPercent(BigDecimal gstPercent) {
        this.gstPercent = gstPercent;
    }

    public BigDecimal getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(BigDecimal gstAmount) {
        this.gstAmount = gstAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public BigDecimal getExtraCharges() {
        return extraCharges;
    }

    public void setExtraCharges(BigDecimal extraCharges) {
        this.extraCharges = extraCharges;
    }

    public Integer getEstDistanceKm() {
        return estDistanceKm;
    }

    public void setEstDistanceKm(Integer estDistanceKm) {
        this.estDistanceKm = estDistanceKm;
    }

    public BigDecimal getEstCo2Kg() {
        return estCo2Kg;
    }

    public void setEstCo2Kg(BigDecimal estCo2Kg) {
        this.estCo2Kg = estCo2Kg;
    }

    public BigDecimal getPayableNow() {
        return totalAmount.add(depositAmount);
    }

    public BigDecimal getGrandTotal() {
        return totalAmount.add(extraCharges);
    }
}
