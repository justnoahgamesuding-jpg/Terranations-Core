package me.meetrow.testproject;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class TraderBigOrder {
    private final UUID traderId;
    private final String countryKey;
    private final int difficultyTier;
    private final double rewardMoney;
    private final int rewardXp;
    private final double rewardReputation;
    private final long createdAtMillis;
    private final long expiresAtMillis;
    private final Map<Profession, TraderBigOrderEntry> entries;
    private long completedAtMillis;

    public TraderBigOrder(
            UUID traderId,
            String countryKey,
            int difficultyTier,
            double rewardMoney,
            int rewardXp,
            double rewardReputation,
            long createdAtMillis,
            long expiresAtMillis,
            Map<Profession, TraderBigOrderEntry> entries,
            long completedAtMillis
    ) {
        this.traderId = traderId;
        this.countryKey = countryKey;
        this.difficultyTier = difficultyTier;
        this.rewardMoney = rewardMoney;
        this.rewardXp = rewardXp;
        this.rewardReputation = rewardReputation;
        this.createdAtMillis = createdAtMillis;
        this.expiresAtMillis = expiresAtMillis;
        this.entries = new LinkedHashMap<>(entries);
        this.completedAtMillis = completedAtMillis;
    }

    public UUID getTraderId() {
        return traderId;
    }

    public String getCountryKey() {
        return countryKey;
    }

    public int getDifficultyTier() {
        return difficultyTier;
    }

    public double getRewardMoney() {
        return rewardMoney;
    }

    public int getRewardXp() {
        return rewardXp;
    }

    public double getRewardReputation() {
        return rewardReputation;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public long getExpiresAtMillis() {
        return expiresAtMillis;
    }

    public Collection<TraderBigOrderEntry> getEntries() {
        return entries.values();
    }

    public TraderBigOrderEntry getEntry(Profession profession) {
        return entries.get(profession);
    }

    public int getTotalRequestedAmount() {
        int total = 0;
        for (TraderBigOrderEntry entry : entries.values()) {
            total += entry.getRequestedAmount();
        }
        return total;
    }

    public int getTotalDeliveredAmount() {
        int total = 0;
        for (TraderBigOrderEntry entry : entries.values()) {
            total += entry.getDeliveredAmount();
        }
        return total;
    }

    public boolean isComplete() {
        if (entries.isEmpty()) {
            return false;
        }
        for (TraderBigOrderEntry entry : entries.values()) {
            if (!entry.isComplete()) {
                return false;
            }
        }
        return true;
    }

    public long getCompletedAtMillis() {
        return completedAtMillis;
    }

    public void markCompleted(long completedAtMillis) {
        this.completedAtMillis = Math.max(0L, completedAtMillis);
    }
}
