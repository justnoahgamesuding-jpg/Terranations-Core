package me.meetrow.testproject;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class ItemsAdderVitalBars {
    private static final String CONFIG_ROOT = "itemsadder-vital-bars";

    private final Testproject plugin;
    private BukkitTask task;

    public ItemsAdderVitalBars(Testproject plugin) {
        this.plugin = plugin;
    }

    public void restart() {
        stop();
        if (!plugin.getConfig().getBoolean(CONFIG_ROOT + ".enabled", true)) {
            return;
        }
        if (plugin.getConfig().getBoolean(CONFIG_ROOT + ".require-itemsadder", true)
                && Bukkit.getPluginManager().getPlugin("ItemsAdder") == null) {
            plugin.getLogger().warning("itemsadder-vital-bars is enabled, but ItemsAdder is not installed.");
            return;
        }

        long intervalTicks = Math.max(5L, plugin.getConfig().getLong(CONFIG_ROOT + ".update-ticks", 10L));
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateAll, 20L, intervalTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void updateAll() {
        int healthDisplayMax = Math.max(1, configInt("display.health-max", "health-display-max", 10));
        int hungerDisplayMax = Math.max(1, configInt("display.hunger-max", "hunger-display-max", 10));
        String healthHudId = configString("huds.health-id", "health-hud-id", "terrahud:terra_health_bar");
        String hungerHudId = configString("huds.hunger-id", "hunger-hud-id", "terrahud:terra_hunger_bar");
        int barWidthPixels = Math.max(1, configInt("display.bar-width-pixels", "bar-width-pixels", 96));
        int barGapPixels = Math.max(0, configInt("display.bar-gap-pixels", "bar-gap-pixels", 12));
        int healthOffsetX = configInt(
                "display.health-x-position-pixels",
                "health-x-position-pixels",
                -((barWidthPixels / 2) + (barGapPixels / 2)));
        int hungerOffsetX = configInt(
                "display.hunger-x-position-pixels",
                "hunger-x-position-pixels",
                healthOffsetX + barWidthPixels + barGapPixels);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!shouldShow(player)) {
                setItemsAdderHudVisible(player, healthHudId, healthOffsetX, false);
                setItemsAdderHudVisible(player, hungerHudId, hungerOffsetX, false);
                continue;
            }

            float healthValue = Math.min(healthDisplayMax, Math.max(0.0F, (float) player.getHealth() / 2.0F));
            float hungerValue = Math.min(hungerDisplayMax, Math.max(0.0F, (float) player.getFoodLevel() / 2.0F));
            setItemsAdderHudValue(player, healthHudId, healthOffsetX, healthValue, true);
            setItemsAdderHudValue(player, hungerHudId, hungerOffsetX, hungerValue, true);
        }
    }

    private boolean shouldShow(Player player) {
        GameMode gameMode = player.getGameMode();
        return gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE;
    }

    private void setItemsAdderHudVisible(Player player, String hudId, int offsetX, boolean visible) {
        setItemsAdderHudValue(player, hudId, offsetX, 0.0F, visible);
    }

    private void setItemsAdderHudValue(Player player, String hudId, int offsetX, float value, boolean visible) {
        if (player == null || hudId == null || hudId.isBlank()) {
            return;
        }

        try {
            Class<?> holderClass = Class.forName("dev.lone.itemsadder.api.FontImages.PlayerHudsHolderWrapper");
            Class<?> quantityHudClass = Class.forName("dev.lone.itemsadder.api.FontImages.PlayerQuantityHudWrapper");
            Constructor<?> holderConstructor = holderClass.getConstructor(Player.class);
            Object holder = holderConstructor.newInstance(player);
            Method holderExists = holderClass.getMethod("exists");
            if (!(boolean) holderExists.invoke(holder)) {
                return;
            }

            Constructor<?> hudConstructor = quantityHudClass.getConstructor(holderClass, String.class);
            Object hud = hudConstructor.newInstance(holder, hudId);
            Method hudExists = quantityHudClass.getMethod("exists");
            if (!(boolean) hudExists.invoke(hud)) {
                return;
            }

            quantityHudClass.getMethod("setVisible", boolean.class).invoke(hud, visible);
            quantityHudClass.getMethod("setOffsetX", int.class).invoke(hud, offsetX);
            if (visible) {
                quantityHudClass.getMethod("setFloatValue", float.class).invoke(hud, value);
            }
            holderClass.getMethod("sendUpdate").invoke(holder);
        } catch (ReflectiveOperationException | LinkageError exception) {
            // ItemsAdder is optional; a missing or changed API should not break Terra.
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
