package me.meetrow.testproject;

import org.bukkit.Material;

import java.util.UUID;

public final class TraderPlayerQuest {
    private final UUID traderId;
    private final Profession profession;
    private final Material requestedMaterial;
    private final String requestedContentId;
    private final int requestedAmount;
    private final double rewardMoney;
    private final int rewardXp;
    private final double rewardReputation;
    private final int difficultyTier;
    private final long acceptedAtMillis;
    private final long deliveryAvailableAtMillis;
    private final long expiresAtMillis;

    public TraderPlayerQuest(
            UUID traderId,
            Profession profession,
            Material requestedMaterial,
            String requestedContentId,
            int requestedAmount,
            double rewardMoney,
            int rewardXp,
            double rewardReputation,
            int difficultyTier,
            long acceptedAtMillis,
            long deliveryAvailableAtMillis,
            long expiresAtMillis
    ) {
        this.traderId = traderId;
        this.profession = profession;
        this.requestedMaterial = requestedMaterial;
        this.requestedContentId = requestedContentId;
        this.requestedAmount = requestedAmount;
        this.rewardMoney = rewardMoney;
        this.rewardXp = rewardXp;
        this.rewardReputation = rewardReputation;
        this.difficultyTier = difficultyTier;
        this.acceptedAtMillis = acceptedAtMillis;
        this.deliveryAvailableAtMillis = deliveryAvailableAtMillis;
        this.expiresAtMillis = expiresAtMillis;
    }

    public UUID getTraderId() {
        return traderId;
    }

    public Profession getProfession() {
        return profession;
    }

    public Material getRequestedMaterial() {
        return requestedMaterial;
    }

    public String getRequestedContentId() {
        return requestedContentId;
    }

    public boolean hasRequestedContent() {
        return requestedContentId != null && !requestedContentId.isBlank();
    }

    public int getRequestedAmount() {
        return requestedAmount;
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

    public int getDifficultyTier() {
        return difficultyTier;
    }

    public long getAcceptedAtMillis() {
        return acceptedAtMillis;
    }

    public long getDeliveryAvailableAtMillis() {
        return deliveryAvailableAtMillis;
    }

    public long getExpiresAtMillis() {
        return expiresAtMillis;
    }

    public TraderQuestOffer asOffer() {
        return new TraderQuestOffer(
                profession,
                requestedMaterial,
                requestedContentId,
                requestedAmount,
                rewardMoney,
                rewardXp,
                rewardReputation,
                difficultyTier
        );
    }
}
