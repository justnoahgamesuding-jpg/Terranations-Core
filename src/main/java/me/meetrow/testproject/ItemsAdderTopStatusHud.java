package me.meetrow.testproject;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ItemsAdderTopStatusHud {
    private static final String CONFIG_ROOT = "itemsadder-top-status";
    private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final String LEGACY_DEFAULT_FORMAT =
            "%panel%%offset%&7LOCATION &f%location% &8| &7JOB &f%job% &7Lv.%level% &8| &7MONEY &f$%money%";
    private static final String DEFAULT_FORMAT =
            "%location_panel%%location_offset%&f⌖ %location%:offset_14:"
                    + "%job_panel%%job_offset%&a⚒ %job% &7Lv.%level%:offset_14:"
                    + "%money_panel%%money_offset%&6☀ &f%money%";

    private final Testproject plugin;
    private final Set<UUID> visiblePlayers = new HashSet<>();
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
        for (UUID playerId : visiblePlayers) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.clearTitle();
            }
        }
        visiblePlayers.clear();
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

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.showTitle(Title.title(
                    Component.empty(),
                    SECTION_SERIALIZER.deserialize(plugin.colorize(buildTitle(player))),
                    Title.Times.times(Duration.ZERO, getStayDuration(), Duration.ZERO)
            ));
            visiblePlayers.add(player.getUniqueId());
        }
    }

    private Duration getStayDuration() {
        long updateTicks = Math.max(5L, plugin.getConfig().getLong(CONFIG_ROOT + ".update-ticks", 20L));
        long stayTicks = Math.max(updateTicks + 10L, plugin.getConfig().getLong(CONFIG_ROOT + ".title-stay-ticks", updateTicks + 10L));
        return Duration.ofMillis(stayTicks * 50L);
    }

    private String buildTitle(Player player) {
        String template = plugin.getConfig().getString(
                CONFIG_ROOT + ".format",
                DEFAULT_FORMAT
        );
        if (LEGACY_DEFAULT_FORMAT.equals(template) || template.contains("%panel%") || template.contains("%offset%")) {
            template = DEFAULT_FORMAT;
        }

        String text = template
                .replace("%panel%", plugin.getConfig().getString(CONFIG_ROOT + ".panel-token", ":top_status_panel:"))
                .replace("%offset%", plugin.getConfig().getString(CONFIG_ROOT + ".content-offset-token", ":offset_-248:"))
                .replace("%location_panel%", plugin.getConfig().getString(CONFIG_ROOT + ".location-panel-token", ":top_status_location_panel:"))
                .replace("%job_panel%", plugin.getConfig().getString(CONFIG_ROOT + ".job-panel-token", ":top_status_job_panel:"))
                .replace("%money_panel%", plugin.getConfig().getString(CONFIG_ROOT + ".money-panel-token", ":top_status_money_panel:"))
                .replace("%location_offset%", plugin.getConfig().getString(CONFIG_ROOT + ".location-offset-token", ":offset_-112:"))
                .replace("%job_offset%", plugin.getConfig().getString(CONFIG_ROOT + ".job-offset-token", ":offset_-118:"))
                .replace("%money_offset%", plugin.getConfig().getString(CONFIG_ROOT + ".money-offset-token", ":offset_-78:"))
                .replace("%location%", getLocationLabel(player))
                .replace("%job%", getJobLabel(player))
                .replace("%level%", String.valueOf(getJobLevel(player)))
                .replace("%money%", plugin.formatMoney(plugin.getBalance(player)));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        return replaceItemsAdderFontImages(player, text);
    }

    private String getLocationLabel(Player player) {
        Country country = plugin.getCountryAt(player.getLocation());
        if (country == null) {
            return plugin.getConfig().getString(CONFIG_ROOT + ".wilderness-label", "Wilderness");
        }
        return trimForHud(country.getName(), plugin.getConfig().getInt(CONFIG_ROOT + ".max-location-chars", 18));
    }

    private String getJobLabel(Player player) {
        Profession profession = plugin.getProfession(player.getUniqueId());
        if (profession == null) {
            return plugin.getConfig().getString(CONFIG_ROOT + ".no-job-label", "No Job");
        }
        return trimForHud(plugin.getProfessionPlainDisplayName(profession), plugin.getConfig().getInt(CONFIG_ROOT + ".max-job-chars", 14));
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

    private String replaceItemsAdderFontImages(Player player, String text) {
        try {
            Class<?> wrapperClass = Class.forName("dev.lone.itemsadder.api.FontImages.FontImageWrapper");
            Method replace = wrapperClass.getMethod("replaceFontImages", String.class);
            Object result = replace.invoke(null, text);
            return result instanceof String resultText ? resultText : text;
        } catch (ReflectiveOperationException | LinkageError exception) {
            return text;
        }
    }
}
