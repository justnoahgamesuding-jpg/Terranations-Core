package me.meetrow.testproject;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.Locale;

public class TerraPlaceholderExpansion extends PlaceholderExpansion {
    private final Testproject plugin;

    public TerraPlaceholderExpansion(Testproject plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "terra";
    }

    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return "";
        }

        String normalized = params.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "balance" -> getBalancePlaceholder(player);
            case "xp" -> String.valueOf(player.getPlayer() != null ? player.getPlayer().getTotalExperience() : 0);
            case "player_country" -> getPlayerCountryPlaceholder(player);
            case "player_countrytag" -> getPlayerCountryTagPlaceholder(player);
            case "current_country" -> getCurrentCountryPlaceholder(player);
            case "profession" -> getProfessionPlaceholder(player);
            case "current_job" -> getProfessionPlaceholder(player);
            case "profession_display" -> getProfessionDisplayPlaceholder(player);
            case "current_job_display" -> getProfessionDisplayPlaceholder(player);
            case "has_profession" -> String.valueOf(plugin.hasProfession(player.getUniqueId()));
            case "profession_locked" -> String.valueOf(!plugin.hasProfession(player.getUniqueId()));
            case "current_job_level" -> getCurrentJobLevelPlaceholder(player);
            case "current_job_xp" -> getCurrentJobXpPlaceholder(player);
            case "current_job_xp_required" -> getCurrentJobXpRequiredPlaceholder(player);
            case "playtest_active" -> String.valueOf(plugin.isPlaytestActive() || plugin.isPlaytestPreparing());
            case "playtest_remaining" -> getPlaytestRemainingPlaceholder();
            case "playtest_remaining_short" -> getPlaytestRemainingShortPlaceholder();
            default -> null;
        };
    }

    private String getBalancePlaceholder(OfflinePlayer player) {
        if (!plugin.hasEconomy()) {
            return "0.00";
        }

        return plugin.formatMoney(plugin.getBalance(player));
    }

    private String getPlayerCountryPlaceholder(OfflinePlayer player) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            return "None";
        }

        return country.getName();
    }

    private String getCurrentCountryPlaceholder(OfflinePlayer player) {
        if (player.getPlayer() == null) {
            return "wilderness";
        }

        Country country = plugin.getCountryAt(player.getPlayer().getLocation());
        if (country == null) {
            return "wilderness";
        }

        return country.getName();
    }

    private String getPlayerCountryTagPlaceholder(OfflinePlayer player) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null || !country.hasTag()) {
            return "none";
        }

        return country.getTag();
    }

    private String getProfessionPlaceholder(OfflinePlayer player) {
        Profession profession = plugin.getProfession(player.getUniqueId());
        if (profession == null) {
            return "none";
        }

        return profession.getKey();
    }

    private String getProfessionDisplayPlaceholder(OfflinePlayer player) {
        Profession profession = plugin.getProfession(player.getUniqueId());
        if (profession == null) {
            return "None";
        }

        return plugin.getProfessionPlainDisplayName(profession);
    }

    private String getCurrentJobLevelPlaceholder(OfflinePlayer player) {
        Profession profession = plugin.getProfession(player.getUniqueId());
        if (profession == null) {
            return "0";
        }

        return String.valueOf(plugin.getProfessionLevel(player.getUniqueId(), profession));
    }

    private String getCurrentJobXpPlaceholder(OfflinePlayer player) {
        Profession profession = plugin.getProfession(player.getUniqueId());
        if (profession == null) {
            return "0";
        }

        int required = plugin.getProfessionXpRequired(player.getUniqueId(), profession);
        int current = plugin.getProfessionXp(player.getUniqueId(), profession);
        return String.valueOf(Math.max(0, required - current));
    }

    private String getCurrentJobXpRequiredPlaceholder(OfflinePlayer player) {
        Profession profession = plugin.getProfession(player.getUniqueId());
        if (profession == null) {
            return "0";
        }

        return String.valueOf(plugin.getProfessionXpRequired(player.getUniqueId(), profession));
    }

    private String getPlaytestRemainingPlaceholder() {
        if (plugin.isPlaytestPreparing()) {
            return "Starting soon";
        }
        if (!plugin.isPlaytestActive()) {
            return "Inactive";
        }
        return plugin.formatLongDurationWords(plugin.getPlaytestRemainingMillis());
    }

    private String getPlaytestRemainingShortPlaceholder() {
        if (plugin.isPlaytestPreparing()) {
            return "soon";
        }
        if (!plugin.isPlaytestActive()) {
            return "0s";
        }
        return plugin.formatDurationWords(plugin.getPlaytestRemainingMillis());
    }
}
