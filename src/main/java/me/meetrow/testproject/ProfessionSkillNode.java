package me.meetrow.testproject;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProfessionSkillNode {
    private final String key;
    private final String displayName;
    private final String branch;
    private final Material icon;
    private final int slot;
    private final int cost;
    private final boolean secondaryAllowed;
    private final List<String> descriptionLines;
    private final List<String> requirements;

    public ProfessionSkillNode(String key,
                               String displayName,
                               String branch,
                               Material icon,
                               int slot,
                               int cost,
                               boolean secondaryAllowed,
                               List<String> descriptionLines,
                               List<String> requirements) {
        this.key = key;
        this.displayName = displayName;
        this.branch = branch;
        this.icon = icon;
        this.slot = slot;
        this.cost = cost;
        this.secondaryAllowed = secondaryAllowed;
        this.descriptionLines = List.copyOf(descriptionLines);
        this.requirements = List.copyOf(requirements);
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBranch() {
        return branch;
    }

    public Material getIcon() {
        return icon;
    }

    public int getSlot() {
        return slot;
    }

    public int getCost() {
        return cost;
    }

    public boolean isSecondaryAllowed() {
        return secondaryAllowed;
    }

    public List<String> getDescriptionLines() {
        return descriptionLines;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public static ProfessionSkillNode fromConfig(String key, ConfigurationSection section) {
        if (key == null || key.isBlank() || section == null) {
            return null;
        }

        String displayName = section.getString("name", key);
        String branch = section.getString("branch", "Core");
        Material icon = Material.matchMaterial(section.getString("icon", "PAPER"));
        if (icon == null) {
            icon = Material.PAPER;
        }
        int slot = Math.max(0, section.getInt("slot", 0));
        int cost = Math.max(1, section.getInt("cost", 2));
        boolean secondaryAllowed = section.getBoolean("secondary-allowed", false);
        List<String> description = new ArrayList<>(section.getStringList("description"));
        List<String> requirements = new ArrayList<>();
        for (String requirement : section.getStringList("requires")) {
            if (requirement != null && !requirement.isBlank()) {
                requirements.add(requirement.trim().toLowerCase(Locale.ROOT));
            }
        }
        return new ProfessionSkillNode(
                key.trim().toLowerCase(Locale.ROOT),
                displayName,
                branch,
                icon,
                slot,
                cost,
                secondaryAllowed,
                description,
                requirements
        );
    }
}
