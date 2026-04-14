package me.meetrow.testproject;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public record JobContract(
        Profession profession,
        Material requestedMaterial,
        int requiredAmount,
        int rewardXp,
        double rewardMoney,
        String rareMaterialKey,
        int rareMaterialAmount,
        long acceptedAtMillis
) {
    public static JobContract fromConfig(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        Profession profession = Profession.fromKey(section.getString("profession"));
        Material requestedMaterial = Material.matchMaterial(section.getString("requested-material", ""));
        if (profession == null || requestedMaterial == null || requestedMaterial.isAir()) {
            return null;
        }
        return new JobContract(
                profession,
                requestedMaterial,
                Math.max(1, section.getInt("required-amount", 1)),
                Math.max(0, section.getInt("reward-xp", 0)),
                Math.max(0.0D, section.getDouble("reward-money", 0.0D)),
                section.getString("rare-material", "forge_shard"),
                Math.max(1, section.getInt("rare-material-amount", 1)),
                Math.max(0L, section.getLong("accepted-at", 0L))
        );
    }

    public void save(ConfigurationSection section) {
        if (section == null) {
            return;
        }
        section.set("profession", profession.getKey());
        section.set("requested-material", requestedMaterial.name());
        section.set("required-amount", requiredAmount);
        section.set("reward-xp", rewardXp);
        section.set("reward-money", rewardMoney);
        section.set("rare-material", rareMaterialKey);
        section.set("rare-material-amount", rareMaterialAmount);
        section.set("accepted-at", acceptedAtMillis);
    }
}
