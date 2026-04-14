package me.meetrow.testproject;

public enum ForgedRarity {
    COMMON("&7", "Common", 0, 0, 0),
    UNCOMMON("&a", "Uncommon", 1, 1, 0),
    RARE("&b", "Rare", 2, 2, 1),
    EPIC("&5", "Epic", 3, 3, 2),
    LEGENDARY("&6", "Legendary", 4, 4, 3);

    private final String color;
    private final String displayName;
    private final int durabilityBonus;
    private final int toolPowerBonus;
    private final int armorBonus;

    ForgedRarity(String color, String displayName, int durabilityBonus, int toolPowerBonus, int armorBonus) {
        this.color = color;
        this.displayName = displayName;
        this.durabilityBonus = durabilityBonus;
        this.toolPowerBonus = toolPowerBonus;
        this.armorBonus = armorBonus;
    }

    public String getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDurabilityBonus() {
        return durabilityBonus;
    }

    public int getToolPowerBonus() {
        return toolPowerBonus;
    }

    public int getArmorBonus() {
        return armorBonus;
    }
}
