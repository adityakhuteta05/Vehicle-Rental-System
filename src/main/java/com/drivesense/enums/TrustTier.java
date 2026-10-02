package com.drivesense.enums;

public enum TrustTier {
    SILVER("Silver", 0, 59, 0, 1.0, "Standard perks, standard refundable security deposit"),
    GOLD("Gold", 60, 84, 3, 0.5, "3% rental discount, 50% security deposit reduction"),
    PLATINUM("Platinum", 85, 100, 6, 0.0, "6% rental discount, 100% zero deposit waiver & priority check-in");

    private final String name;
    private final int minScore;
    private final int maxScore;
    private final int discountPercent;
    private final double depositMultiplier;
    private final String benefits;

    TrustTier(String name, int minScore, int maxScore, int discountPercent, double depositMultiplier, String benefits) {
        this.name = name;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.discountPercent = discountPercent;
        this.depositMultiplier = depositMultiplier;
        this.benefits = benefits;
    }

    public static TrustTier fromScore(int score) {
        if (score >= 85) return PLATINUM;
        if (score >= 60) return GOLD;
        return SILVER;
    }

    public String getName() {
        return name;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public double getDepositMultiplier() {
        return depositMultiplier;
    }

    public String getBenefits() {
        return benefits;
    }
}
