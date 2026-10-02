package com.drivesense.pricing;

import com.drivesense.enums.TrustTier;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DepositRule implements PricingRule {

    private static final BigDecimal BASE_SECURITY_DEPOSIT = new BigDecimal("3000.00");

    @Override
    public void apply(PricingContext context, PriceBreakdownBuilder builder) {
        TrustTier tier = context.getTrustTier();
        double multiplier = tier.getDepositMultiplier();

        BigDecimal finalDeposit = BASE_SECURITY_DEPOSIT
                .multiply(BigDecimal.valueOf(multiplier))
                .setScale(2, RoundingMode.HALF_UP);

        builder.deposit(finalDeposit);
    }
}
