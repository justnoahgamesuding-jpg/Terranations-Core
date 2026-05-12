package me.meetrow.testproject;

import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.Map;

public enum CountryUpgrade {
    MINERS_TUNNELS_I(
            "miners_tunnels_i",
            "Miner Tunnels I",
            Material.IRON_PICKAXE,
            null,
            1,
            15.0D,
            16,
            requirements(Profession.MINER, 4),
            "-1 second miner break cooldown."
    ),
    GREEN_FIELDS_I(
            "green_fields_i",
            "Green Fields I",
            Material.WHEAT,
            "miners_tunnels_i",
            1,
            18.0D,
            18,
            requirements(Profession.FARMER, 4),
            "+8 farmland capacity for the country."
    ),
    BUILDERS_GUILD(
            "builders_guild",
            "Trade Hall",
            Material.BRICKS,
            "green_fields_i",
            1,
            22.0D,
            20,
            requirements(Profession.TRADER, 4),
            "+5% trader XP in the country."
    ),
    COIN_VAULT_I(
            "coin_vault_i",
            "Coin Vault I",
            Material.GOLD_INGOT,
            "builders_guild",
            1,
            26.0D,
            24,
            requirements(Profession.MINER, 4, Profession.TRADER, 4),
            "+5% block and level-up money rewards."
    ),
    HARVEST_CIRCLE(
            "harvest_circle",
            "Harvest Circle",
            Material.GOLDEN_HOE,
            "coin_vault_i",
            2,
            30.0D,
            28,
            requirements(Profession.FARMER, 5),
            "+5% farmer XP in the country."
    ),
    TIMBER_YARD(
            "timber_yard",
            "Timber Yard",
            Material.IRON_AXE,
            "harvest_circle",
            2,
            36.0D,
            32,
            requirements(Profession.LUMBERJACK, 5),
            "+5% lumberjack XP in the country."
    ),
    HOME_NETWORK(
            "home_network",
            "Home Network",
            Material.ENDER_PEARL,
            "timber_yard",
            2,
            42.0D,
            36,
            requirements(Profession.TRADER, 5, Profession.LUMBERJACK, 5),
            "-10 minutes from /country home cooldown."
    ),
    FORGE_QUARTERS(
            "forge_quarters",
            "Forge Quarters",
            Material.ANVIL,
            "home_network",
            3,
            50.0D,
            42,
            requirements(Profession.BLACKSMITH, 6),
            "+5% blacksmith XP in the country."
    ),
    TRADE_LEDGER(
            "trade_ledger",
            "Trade Ledger",
            Material.CHEST_MINECART,
            "forge_quarters",
            3,
            58.0D,
            48,
            requirements(Profession.MINER, 6, Profession.BLACKSMITH, 6),
            "+5% trader money and reputation rewards."
    ),
    LEARNING_HALL_I(
            "learning_hall_i",
            "Learning Hall I",
            Material.EXPERIENCE_BOTTLE,
            "trade_ledger",
            3,
            66.0D,
            54,
            requirements(Profession.FARMER, 6, Profession.TRADER, 6),
            "+5% all job XP in the country."
    ),
    GREEN_FIELDS_II(
            "green_fields_ii",
            "Green Fields II",
            Material.HAY_BLOCK,
            "learning_hall_i",
            4,
            76.0D,
            60,
            requirements(Profession.FARMER, 7),
            "+16 more farmland capacity for the country."
    ),
    MARKET_PERMIT(
            "market_permit",
            "Market Permit",
            Material.EMERALD,
            "green_fields_ii",
            4,
            88.0D,
            68,
            requirements(Profession.TRADER, 7, Profession.BLACKSMITH, 7),
            "-5 seconds from merchant trade cooldown."
    ),
    COIN_VAULT_II(
            "coin_vault_ii",
            "Coin Vault II",
            Material.RAW_GOLD,
            "market_permit",
            4,
            102.0D,
            76,
            requirements(Profession.MINER, 7, Profession.FARMER, 7),
            "+10% more block and level-up money rewards."
    ),
    MINERS_TUNNELS_II(
            "miners_tunnels_ii",
            "Miner Tunnels II",
            Material.DIAMOND_PICKAXE,
            "coin_vault_ii",
            5,
            118.0D,
            86,
            requirements(Profession.MINER, 8),
            "Another -1 second miner break cooldown."
    ),
    GRAND_EXCHANGE(
            "grand_exchange",
            "Grand Exchange",
            Material.NETHER_STAR,
            "miners_tunnels_ii",
            5,
            138.0D,
            96,
            requirements(Profession.MINER, 8, Profession.FARMER, 8, Profession.TRADER, 8, Profession.BLACKSMITH, 8),
            "+5% all job XP and +10% trader rewards."
    );

    private final String key;
    private final String displayName;
    private final Material icon;
    private final String prerequisiteKey;
    private final int requiredCountryLevel;
    private final double balanceCost;
    private final int resourceCost;
    private final Map<Profession, Integer> professionRequirements;
    private final String effectDescription;

    CountryUpgrade(String key, String displayName, Material icon, String prerequisiteKey,
                   int requiredCountryLevel, double balanceCost, int resourceCost,
                   Map<Profession, Integer> professionRequirements, String effectDescription) {
        this.key = key;
        this.displayName = displayName;
        this.icon = icon;
        this.prerequisiteKey = prerequisiteKey;
        this.requiredCountryLevel = requiredCountryLevel;
        this.balanceCost = balanceCost;
        this.resourceCost = resourceCost;
        this.professionRequirements = professionRequirements;
        this.effectDescription = effectDescription;
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

    public String getPrerequisiteKey() {
        return prerequisiteKey;
    }

    public int getRequiredCountryLevel() {
        return requiredCountryLevel;
    }

    public double getBalanceCost() {
        return balanceCost;
    }

    public int getResourceCost() {
        return resourceCost;
    }

    public Map<Profession, Integer> getProfessionRequirements() {
        return professionRequirements;
    }

    public String getEffectDescription() {
        return effectDescription;
    }

    public CountryUpgrade getPrerequisite() {
        return fromKey(prerequisiteKey);
    }

    public static CountryUpgrade fromKey(String input) {
        if (input == null) {
            return null;
        }
        for (CountryUpgrade upgrade : values()) {
            if (upgrade.key.equalsIgnoreCase(input)) {
                return upgrade;
            }
        }
        return null;
    }

    private static Map<Profession, Integer> requirements(Object... values) {
        Map<Profession, Integer> requirements = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            Profession profession = (Profession) values[i];
            Integer level = (Integer) values[i + 1];
            requirements.put(profession, level);
        }
        return requirements;
    }
}
