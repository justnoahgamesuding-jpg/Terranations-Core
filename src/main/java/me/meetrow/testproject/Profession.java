package me.meetrow.testproject;

import org.bukkit.Material;

import java.util.Locale;

public enum Profession {
    MINER("Miner", Material.IRON_PICKAXE, 10),
    LUMBERJACK("Lumberjack", Material.IRON_AXE, 11),
    FARMER("Farmer", Material.GOLDEN_HOE, 12),
    BUILDER("Builder", Material.BRICKS, 13),
    BLACKSMITH("Blacksmith", Material.ANVIL, 16);

    private final String displayName;
    private final Material icon;
    private final int slot;

    Profession(String displayName, Material icon, int slot) {
        this.displayName = displayName;
        this.icon = icon;
        this.slot = slot;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public int getSlot() {
        return slot;
    }

    public String getDefaultDisplayName() {
        return displayName;
    }

    public Material getDefaultIcon() {
        return icon;
    }

    public int getDefaultSlot() {
        return slot;
    }

    public String getKey() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Profession fromKey(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String normalized = input.trim().replace(' ', '_').replace('-', '_').toUpperCase(Locale.ROOT);
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
