package com.drivesense.pricing;

import com.drivesense.enums.TrustTier;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LoyaltyRule implements PricingRule {

    @Override
    public void apply(PricingContext context, PriceBreakdownBuilder builder) {
        TrustTier tier = context.getTrustTier();
        if (tier.getDiscountPercent() > 0) {
            BigDecimal base = context.getCar().getPricePerDay().multiply(BigDecimal.valueOf(context.getRentalDays()));
            BigDecimal discount = base.multiply(BigDecimal.valueOf(tier.getDiscountPercent()))
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            builder.addDiscount(tier.getName() + " Loyalty Tier (" + tier.getDiscountPercent() + "%)",
                    "Exclusive member benefit for Trust Tier " + tier.getName(),
                    discount);
        }
    }
}
