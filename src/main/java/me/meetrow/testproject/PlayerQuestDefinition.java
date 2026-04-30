package me.meetrow.testproject;

import org.bukkit.Material;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class PlayerQuestDefinition {
    private final String id;
    private final int order;
    private final boolean enabled;
    private final String title;
    private final String objective;
    private final String hint;
    private final int target;
    private final String key;
    private final Profession profession;
    private final PlayerQuestType type;
    private final Set<String> requiresCompleted;
    private final double rewardMoney;
    private final int rewardProfessionXp;
    private final Profession rewardProfession;
    private final Material rewardItemMaterial;
    private final int rewardItemAmount;
    private final Material requiredItemMaterial;

    public PlayerQuestDefinition(
            String id,
            int order,
            boolean enabled,
            String title,
            String objective,
            String hint,
            int target,
            String key,
            Profession profession,
            PlayerQuestType type,
            Set<String> requiresCompleted,
            double rewardMoney,
            int rewardProfessionXp,
            Profession rewardProfession,
            Material rewardItemMaterial,
            int rewardItemAmount,
            Material requiredItemMaterial
    ) {
        this.id = id;
        this.order = order;
        this.enabled = enabled;
        this.title = title;
        this.objective = objective;
        this.hint = hint;
        this.target = target;
        this.key = key;
        this.profession = profession;
        this.type = type;
        this.requiresCompleted = Collections.unmodifiableSet(new LinkedHashSet<>(requiresCompleted));
        this.rewardMoney = rewardMoney;
        this.rewardProfessionXp = rewardProfessionXp;
        this.rewardProfession = rewardProfession;
        this.rewardItemMaterial = rewardItemMaterial;
        this.rewardItemAmount = rewardItemAmount;
        this.requiredItemMaterial = requiredItemMaterial;
    }

    public String getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getTitle() {
        return title;
    }

    public String getObjective() {
        return objective;
    }

    public String getHint() {
        return hint;
    }

    public int getTarget() {
        return target;
    }

    public String getKey() {
        return key;
    }

    public Profession getProfession() {
        return profession;
    }

    public PlayerQuestType getType() {
        return type;
    }

    public Set<String> getRequiresCompleted() {
        return requiresCompleted;
    }

    public double getRewardMoney() {
        return rewardMoney;
    }

    public int getRewardProfessionXp() {
        return rewardProfessionXp;
    }

    public Profession getRewardProfession() {
        return rewardProfession;
    }

    public Material getRewardItemMaterial() {
        return rewardItemMaterial;
    }

    public int getRewardItemAmount() {
        return rewardItemAmount;
    }

    public Material getRequiredItemMaterial() {
        return requiredItemMaterial;
    }
}
