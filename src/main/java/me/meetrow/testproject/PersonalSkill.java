package me.meetrow.testproject;

import org.bukkit.Material;

import java.util.List;

public enum PersonalSkill {
    HEARTY("hearty", "Hearty", Material.GOLDEN_APPLE, 5, List.of(
            "&7Increase your max health.",
            "&7Each level adds &f1 heart&7."
    )),
    XP_MASTERY("xp_mastery", "XP Mastery", Material.EXPERIENCE_BOTTLE, 5, List.of(
            "&7Gain more profession XP.",
            "&7Each level adds &f5% XP gain&7."
    )),
    COOLDOWN_MASTERY("cooldown_mastery", "Cooldown Mastery", Material.CLOCK, 5, List.of(
            "&7Reduce shared action cooldowns.",
            "&7Each level removes &f1 second&7."
    )),
    TRADER_INSTINCT("trader_instinct", "Trader Instinct", Material.EMERALD, 5, List.of(
            "&7Improve trader contract rewards.",
            "&7Boosts money, XP, and reputation."
    )),
    MERCHANT_HAGGLER("merchant_haggler", "Merchant Haggler", Material.CHEST, 5, List.of(
            "&7Reduce your merchant trade cooldown.",
            "&7Each level removes &f2 seconds&7."
    )),
    HARVEST_MASTERY("harvest_mastery", "Harvest Mastery", Material.DIAMOND_PICKAXE, 5, List.of(
            "&7Increase profession double-drop chance.",
            "&7Applies to miner, farmer, and lumberjack drops."
    )),
    GREEN_THUMB("green_thumb", "Green Thumb", Material.WHEAT, 5, List.of(
            "&7Increase instant growth proc chance.",
            "&7Applies to farmer and lumberjack growth procs."
    )),
    ORE_SENSE("ore_sense", "Ore Sense", Material.SPYGLASS, 1, List.of(
            "&7Unlock a personal ore vision toggle.",
            "&7Use it from the Terra Guide."
    ));

    private final String key;
    private final String displayName;
    private final Material icon;
    private final int maxLevel;
    private final List<String> descriptionLines;

    PersonalSkill(String key, String displayName, Material icon, int maxLevel, List<String> descriptionLines) {
        this.key = key;
        this.displayName = displayName;
        this.icon = icon;
        this.maxLevel = maxLevel;
        this.descriptionLines = descriptionLines;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public List<String> getDescriptionLines() {
        return descriptionLines;
    }

    public static PersonalSkill fromKey(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        for (PersonalSkill skill : values()) {
            if (skill.key.equalsIgnoreCase(input) || skill.name().equalsIgnoreCase(input)) {
                return skill;
            }
        }
        return null;
    }
}
