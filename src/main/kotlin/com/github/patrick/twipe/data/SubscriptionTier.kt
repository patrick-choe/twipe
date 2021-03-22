package com.github.patrick.twipe.data

enum class SubscriptionTier {
    /**
     * Tier 1 ($4.99 or Prime Gaming) Subscription
     */
    TIER1,

    /**
     * Tier 2 ($9.99) Subscription
     */
    TIER2,

    /**
     * Tier 3 ($24.99) Subscription
     */
    TIER3,

    /**
     * Tier Unknown. This shouldn't be called in normal cases!
     */
    UNKNOWN
}