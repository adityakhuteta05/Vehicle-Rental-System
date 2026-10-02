package com.drivesense.pricing;

public interface PricingRule {
    void apply(PricingContext context, PriceBreakdownBuilder builder);
}
