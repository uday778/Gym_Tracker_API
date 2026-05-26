package com.gymtracker.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionTier {
    BASIC("Basic", 29.99),
    PREMIUM("Premium", 59.99),
    ELITE("Elite", 99.99);

    private final String displayName;
    private final double monthlyPrice;
}
