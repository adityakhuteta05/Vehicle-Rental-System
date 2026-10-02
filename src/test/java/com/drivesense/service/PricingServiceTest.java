package com.drivesense.service;

import com.drivesense.entity.Car;
import com.drivesense.entity.User;
import com.drivesense.enums.*;
import com.drivesense.pricing.PriceBreakdown;
import com.drivesense.pricing.PricingRuleLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PricingServiceTest {

    private PricingService pricingService;
    private Car testCar;
    private User regularUser;
    private User platinumUser;

    @BeforeEach
    public void setup() {
        PricingRuleLoader loader = new PricingRuleLoader();
        loader.init();
        this.pricingService = new PricingService(loader);

        testCar = new Car(
                "Hyundai", "Creta", "DL04-CC-1024",
                CarType.SUV, FuelType.PETROL, Transmission.AUTOMATIC, 5,
                BootSize.M, new BigDecimal("3000.00"), new BigDecimal("180.00"),
                300, new BigDecimal("12.00"), 140, "img.jpg", CarStatus.AVAILABLE, new BigDecimal("4.8")
        );

        regularUser = new User("Regular User", "reg@test.com", "123", "hash", "DL1", LocalDate.of(1995, 1, 1), "ROLE_CUSTOMER");
        regularUser.setTrustScore(50); // Silver

        platinumUser = new User("VIP User", "vip@test.com", "123", "hash", "DL2", LocalDate.of(1990, 1, 1), "ROLE_CUSTOMER");
        platinumUser.setTrustScore(95); // Platinum
    }

    @Test
    @DisplayName("Hourly rate applies when rental duration is under 24 hours")
    public void testHourlyRateUnder24Hours() {
        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(5);

        PriceBreakdown breakdown = pricingService.calculatePrice(testCar, regularUser, start, end, false, false, false);

        assertNotNull(breakdown);
        assertEquals(5, breakdown.getRentalHours());
        // 5 hours * 180 = 900 base
        assertEquals(new BigDecimal("900.00"), breakdown.getBaseAmount());
    }

    @Test
    @DisplayName("Long rental discount applies for 7+ days (12% off)")
    public void testLongRentalDiscount() {
        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        LocalDateTime end = start.plusDays(7);

        PriceBreakdown breakdown = pricingService.calculatePrice(testCar, regularUser, start, end, false, false, false);

        assertNotNull(breakdown);
        assertTrue(breakdown.getTotalDiscounts().compareTo(BigDecimal.ZERO) > 0);
        boolean hasLongRentalDiscount = breakdown.getDiscounts().stream()
                .anyMatch(d -> d.getName().contains("Long Rental"));
        assertTrue(hasLongRentalDiscount, "Breakdown should include Long Rental discount");
    }

    @Test
    @DisplayName("Platinum loyalty tier receives 6% discount and 100% security deposit waiver")
    public void testPlatinumLoyaltyTierWaiver() {
        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        LocalDateTime end = start.plusDays(2);

        PriceBreakdown breakdown = pricingService.calculatePrice(testCar, platinumUser, start, end, false, false, false);

        assertNotNull(breakdown);
        assertEquals(BigDecimal.ZERO.setScale(2), breakdown.getDepositAmount(), "Platinum members must have zero deposit");
        boolean hasLoyaltyDiscount = breakdown.getDiscounts().stream()
                .anyMatch(d -> d.getName().contains("Platinum Loyalty"));
        assertTrue(hasLoyaltyDiscount, "Platinum members must receive loyalty discount");
    }

    @Test
    @DisplayName("GST 18% is correctly applied to subtotal")
    public void testGstCalculation() {
        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        LocalDateTime end = start.plusDays(1);

        PriceBreakdown breakdown = pricingService.calculatePrice(testCar, regularUser, start, end, false, false, false);

        assertNotNull(breakdown);
        assertNotNull(breakdown.getGstAmount());
        assertTrue(breakdown.getTotalAmount().compareTo(breakdown.getSubtotal()) > 0);
    }
}
