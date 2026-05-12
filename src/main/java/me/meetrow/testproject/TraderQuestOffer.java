package me.meetrow.testproject;

import org.bukkit.Material;

public final class TraderQuestOffer {
    private final Profession profession;
    private final Material requestedMaterial;
    private final String requestedContentId;
    private final int requestedAmount;
    private final double rewardMoney;
    private final int rewardXp;
    private final double rewardReputation;
    private final int difficultyTier;

    public TraderQuestOffer(
            Profession profession,
            Material requestedMaterial,
            String requestedContentId,
            int requestedAmount,
            double rewardMoney,
            int rewardXp,
            double rewardReputation,
            int difficultyTier
    ) {
        this.profession = profession;
        this.requestedMaterial = requestedMaterial;
        this.requestedContentId = requestedContentId;
        this.requestedAmount = requestedAmount;
        this.rewardMoney = rewardMoney;
        this.rewardXp = rewardXp;
        this.rewardReputation = rewardReputation;
        this.difficultyTier = difficultyTier;
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
}
