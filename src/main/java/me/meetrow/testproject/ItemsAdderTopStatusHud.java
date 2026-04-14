package me.meetrow.testproject;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ItemsAdderTopStatusHud {
    private static final String CONFIG_ROOT = "itemsadder-top-status";
    private static final String LEGACY_DEFAULT_FORMAT =
            "%panel%%offset%&7LOCATION &f%location% &8| &7JOB &f%job% &7Lv.%level% &8| &7MONEY &f$%money%";
    private static final String DEFAULT_FORMAT =
            "auto";

    private final Testproject plugin;
    private final Map<UUID, BossBar> bars = new ConcurrentHashMap<>();
    private BukkitTask task;

    public ItemsAdderTopStatusHud(Testproject plugin) {
        this.plugin = plugin;
    }

    public void restart() {
        stop();
        if (!isEnabled()) {
            return;
        }
        if (requiresItemsAdder() && Bukkit.getPluginManager().getPlugin("ItemsAdder") == null) {
            plugin.getLogger().warning("itemsadder-top-status is enabled, but ItemsAdder is not installed.");
            return;
        }

        long intervalTicks = Math.max(5L, plugin.getConfig().getLong(CONFIG_ROOT + ".update-ticks", 20L));
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateAll, 20L, intervalTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (BossBar bar : bars.values()) {
            bar.removeAll();
            bar.setVisible(false);
        }
        bars.clear();
    }

    private boolean isEnabled() {
        return plugin.getConfig().getBoolean(CONFIG_ROOT + ".enabled", true);
    }

    private boolean requiresItemsAdder() {
        return plugin.getConfig().getBoolean(CONFIG_ROOT + ".require-itemsadder", true);
    }

    private void updateAll() {
        if (!isEnabled()) {
            stop();
            return;
        }

        Set<UUID> onlineIds = new HashSet<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            onlineIds.add(player.getUniqueId());
            BossBar bar = bars.computeIfAbsent(player.getUniqueId(), ignored -> createBar());
            if (!bar.getPlayers().contains(player)) {
                bar.removeAll();
                bar.addPlayer(player);
            }
            bar.setTitle(plugin.colorize(buildTitle(player)));
            bar.setProgress(1.0D);
            bar.setVisible(true);
        }

        bars.entrySet().removeIf(entry -> {
            if (onlineIds.contains(entry.getKey())) {
                return false;
            }
            BossBar bar = entry.getValue();
            bar.removeAll();
            bar.setVisible(false);
            return true;
        });
    }

    private BossBar createBar() {
        return plugin.getServer().createBossBar("", BarColor.WHITE, BarStyle.SOLID);
    }

    private String buildTitle(Player player) {
        String template = plugin.getConfig().getString(CONFIG_ROOT + ".format", DEFAULT_FORMAT);
        if (template == null
                || template.isBlank()
                || DEFAULT_FORMAT.equalsIgnoreCase(template)
                || LEGACY_DEFAULT_FORMAT.equals(template)
                || template.contains("%panel%")
                || template.contains("%offset%")) {
            template = buildFixedPanelTemplate(player);
        }

        String text = template
                .replace("%panel%", configString("tokens.panel", "panel-token", ":top_status_panel:"))
                .replace("%offset%", configString("tokens.content-offset", "content-offset-token", ":offset_-248:"))
                .replace("%location_panel%", configString("tokens.location-panel", "location-panel-token", ":top_status_location_panel:"))
                .replace("%job_panel%", configString("tokens.job-panel", "job-panel-token", ":top_status_job_panel:"))
                .replace("%money_panel%", configString("tokens.money-panel", "money-panel-token", ":top_status_money_panel:"))
                .replace("%location_offset%", configString("tokens.location-offset", "location-offset-token", ":offset_-112:"))
                .replace("%job_offset%", configString("tokens.job-offset", "job-offset-token", ":offset_-118:"))
                .replace("%money_offset%", configString("tokens.money-offset", "money-offset-token", ":offset_-78:"))
                .replace("%location%", getLocationLabel(player))
                .replace("%job%", getJobLabel(player))
                .replace("%level%", String.valueOf(getJobLevel(player)))
                .replace("%money%", plugin.formatMoney(plugin.getBalance(player)));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        return replaceItemsAdderFontImages(text);
    }

    private String buildFixedPanelTemplate(Player player) {
        int fallbackPanelWidth = Math.max(48, configInt("layout.panel-width-pixels", "panel-width-pixels", 64));
        int locationPanelWidth = Math.max(48, configInt("layout.location-panel-width-pixels", "location-panel-width-pixels", fallbackPanelWidth));
        int jobPanelWidth = Math.max(48, configInt("layout.job-panel-width-pixels", "job-panel-width-pixels", fallbackPanelWidth));
        int moneyPanelWidth = Math.max(48, configInt("layout.money-panel-width-pixels", "money-panel-width-pixels", fallbackPanelWidth));
        int panelGapWidth = configInt("layout.panel-gap-pixels", "panel-gap-pixels", 8);
        int locationJobGapWidth = Math.max(0, plugin.getConfig().getInt(
                CONFIG_ROOT + ".layout.location-job-gap-pixels",
                configInt("location-job-gap-pixels", "panel-gap-pixels", panelGapWidth)
        ));
        int jobMoneyGapWidth = Math.max(0, plugin.getConfig().getInt(
                CONFIG_ROOT + ".layout.job-money-gap-pixels",
                configInt("job-money-gap-pixels", "panel-gap-pixels", panelGapWidth)
        ));
        int locationTextWidth = estimateHudTextWidth("\u27a3 " + getLocationLabel(player));
        int jobTextWidth = estimateHudTextWidth("\u2692 " + getJobLabel(player) + " Lv." + getJobLevel(player));
        int moneyTextWidth = estimateHudTextWidth("\u26c1 " + plugin.formatMoney(plugin.getBalance(player)));
        int jobTextInset = configInt("layout.job-text-inset-pixels", "job-text-inset-pixels", 6);

        return "%location_panel%" + offset(-locationPanelWidth + centeredTextOffset(locationPanelWidth, locationTextWidth))
                + "&c\u27a3 &f%location%" + offset(remainingPanelAdvance(locationPanelWidth, locationTextWidth) + locationJobGapWidth)
                + "%job_panel%" + offset(-jobPanelWidth + centeredTextOffset(jobPanelWidth, jobTextWidth) + jobTextInset)
                + "&a\u2692 %job% &7Lv.%level%" + offset(remainingPanelAdvance(jobPanelWidth, jobTextWidth) + jobMoneyGapWidth)
                + "%money_panel%" + offset(-moneyPanelWidth + centeredTextOffset(moneyPanelWidth, moneyTextWidth))
                + "&6\u26c1 &f%money%";
    }

    private int centeredTextOffset(int panelWidth, int textWidth) {
        return Math.max(4, (panelWidth - textWidth) / 2);
    }

    private int remainingPanelAdvance(int panelWidth, int textWidth) {
        return Math.max(4, panelWidth - centeredTextOffset(panelWidth, textWidth) - textWidth);
    }

    private int estimateHudTextWidth(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        int width = 0;
        boolean colorCode = false;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (colorCode) {
                colorCode = false;
                continue;
            }
            if (character == '&') {
                colorCode = true;
                continue;
            }
            width += character == ' ' ? 4 : 6;
        }
        return width;
    }

    private String offset(int pixels) {
        return ":offset_" + pixels + ":";
    }

    private String getLocationLabel(Player player) {
        Country country = plugin.getCountryAt(player.getLocation());
        if (country == null) {
            return plugin.getConfig().getString(CONFIG_ROOT + ".wilderness-label", "Wilderness");
        }
        return trimForHud(country.getName(), plugin.getConfig().getInt(CONFIG_ROOT + ".max-location-chars", 12));
    }

    private String getJobLabel(Player player) {
        Profession profession = plugin.getProfession(player.getUniqueId());
        if (profession == null) {
            return plugin.getConfig().getString(CONFIG_ROOT + ".no-job-label", "No Job");
        }
        return trimForHud(plugin.getProfessionPlainDisplayName(profession), plugin.getConfig().getInt(CONFIG_ROOT + ".max-job-chars", 10));
    }

    private int getJobLevel(Player player) {
        Profession profession = plugin.getProfession(player.getUniqueId());
        return profession != null ? plugin.getProfessionLevel(player.getUniqueId(), profession) : 0;
    }

    private String trimForHud(String value, int maxChars) {
        if (value == null) {
            return "";
        }
        int safeMax = Math.max(4, maxChars);
        if (value.length() <= safeMax) {
            return value;
        }
        return value.substring(0, safeMax - 3).trim() + "...";
    }

    private String replaceItemsAdderFontImages(String text) {
        try {
            Class<?> wrapperClass = Class.forName("dev.lone.itemsadder.api.FontImages.FontImageWrapper");
            Method replace = wrapperClass.getMethod("replaceFontImages", String.class);
            Object result = replace.invoke(null, text);
            return result instanceof String resultText ? resultText : text;
        } catch (ReflectiveOperationException | LinkageError exception) {
            return text;
        }
    }

    private String configString(String path, String legacyPath, String defaultValue) {
        String value = plugin.getConfig().getString(CONFIG_ROOT + "." + path);
        if (value == null && legacyPath != null) {
            value = plugin.getConfig().getString(CONFIG_ROOT + "." + legacyPath);
        }
        return value != null ? value : defaultValue;
    }

    private int configInt(String path, String legacyPath, int defaultValue) {
        String fullPath = CONFIG_ROOT + "." + path;
        if (plugin.getConfig().contains(fullPath)) {
            return plugin.getConfig().getInt(fullPath, defaultValue);
        }
        if (legacyPath != null) {
            return plugin.getConfig().getInt(CONFIG_ROOT + "." + legacyPath, defaultValue);
        }
        return defaultValue;
    }
}
