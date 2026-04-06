package me.meetrow.testproject;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PlaytestGuiListener implements Listener {
    private static final int GUI_SIZE = 45;
    private static final int STATUS_SLOT = 4;
    private static final int DURATION_SLOT = 19;
    private static final int XP_BOOST_SLOT = 20;
    private static final int PVP_SLOT = 21;
    private static final int DAYLIGHT_SLOT = 22;
    private static final int COUNTRY_DATA_SLOT = 24;
    private static final int PLAYER_PROGRESS_SLOT = 25;
    private static final int START_SLOT = 39;
    private static final int STOP_SLOT = 40;
    private static final int CLOSE_SLOT = 44;
    private static final long[] DURATION_OPTIONS = {
            15L * 60L * 1000L,
            30L * 60L * 1000L,
            45L * 60L * 1000L,
            60L * 60L * 1000L,
            2L * 60L * 60L * 1000L,
            3L * 60L * 60L * 1000L,
            6L * 60L * 60L * 1000L,
            12L * 60L * 60L * 1000L,
            24L * 60L * 60L * 1000L
    };
    private static final double[] XP_OPTIONS = {1.0D, 1.25D, 1.5D, 2.0D, 3.0D, 5.0D};

    private final Testproject plugin;

    public PlaytestGuiListener(Testproject plugin) {
        this.plugin = plugin;
    }

    public void openPlaytestMenu(Player player) {
        if (player == null) {
            return;
        }

        Inventory inventory = Bukkit.createInventory(
                new PlaytestMenuHolder(player.getUniqueId()),
                GUI_SIZE,
                plugin.legacyComponent("&8Playtest Manager")
        );

        fillEmptySlots(inventory);
        inventory.setItem(STATUS_SLOT, createStatusItem());
        inventory.setItem(DURATION_SLOT, createSettingItem(
                Material.CLOCK,
                "&6Duration",
                List.of(
                        "&7Configured session length:",
                        "&f" + plugin.formatLongDurationWords(plugin.getConfiguredPlaytestDurationMillis()),
                        "",
                        "&eLeft click: next preset",
                        "&eRight click: previous preset"
                )));
        inventory.setItem(XP_BOOST_SLOT, createSettingItem(
                Material.EXPERIENCE_BOTTLE,
                "&bXP Boost",
                List.of(
                        "&7Configured playtest XP multiplier:",
                        "&f" + plugin.formatXpBoostMultiplier(plugin.getConfiguredPlaytestXpBoostMultiplier()),
                        "",
                        "&eLeft click: next preset",
                        "&eRight click: previous preset"
                )));
        inventory.setItem(PVP_SLOT, createToggleItem(
                Material.IRON_SWORD,
                "&cPvP",
                plugin.isConfiguredPlaytestPvpEnabled(),
                "Allow players to damage each other during the playtest."
        ));
        inventory.setItem(DAYLIGHT_SLOT, createToggleItem(
                Material.SUNFLOWER,
                "&eDay/Night Cycle",
                plugin.isConfiguredPlaytestDaylightCycleEnabled(),
                "Allow the normal day and night cycle during the playtest."
        ));
        inventory.setItem(COUNTRY_DATA_SLOT, createToggleItem(
                Material.BEACON,
                "&6Keep Country Data",
                plugin.isConfiguredPlaytestKeepCountryData(),
                "Keep countries and memberships when the playtest ends."
        ));
        inventory.setItem(PLAYER_PROGRESS_SLOT, createToggleItem(
                Material.KNOWLEDGE_BOOK,
                "&aSave Player Progression",
                plugin.isConfiguredPlaytestSavePlayerProgression(),
                "Keep player Terra progression after the playtest ends."
        ));
        inventory.setItem(START_SLOT, createActionItem(
                Material.LIME_CONCRETE,
                "&aStart Playtest",
                List.of(
                        "&7Start a playtest with the configured settings.",
                        "",
                        "&fDuration: &e" + plugin.formatLongDurationWords(plugin.getConfiguredPlaytestDurationMillis()),
                        "&fXP boost: &e" + plugin.formatXpBoostMultiplier(plugin.getConfiguredPlaytestXpBoostMultiplier())
                )));
        inventory.setItem(STOP_SLOT, createActionItem(
                Material.RED_CONCRETE,
                "&cStop Playtest",
                List.of(
                        "&7Start the shutdown countdown for the active playtest.",
                        "&7If the playtest is still preparing, it will be cancelled."
                )));
        inventory.setItem(CLOSE_SLOT, createActionItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof PlaytestMenuHolder menuHolder)) {
            return;
        }

        event.setCancelled(true);
        if (!player.getUniqueId().equals(menuHolder.viewerId())) {
            player.closeInventory();
            return;
        }
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }
        if (!canManagePlaytest(player)) {
            player.closeInventory();
            return;
        }

        int slot = event.getRawSlot();
        if (slot == DURATION_SLOT) {
            long current = plugin.getConfiguredPlaytestDurationMillis();
            plugin.setConfiguredPlaytestDurationMillis(cycleLongOption(current, DURATION_OPTIONS, event.isLeftClick()));
            openPlaytestMenu(player);
            return;
        }
        if (slot == XP_BOOST_SLOT) {
            double current = plugin.getConfiguredPlaytestXpBoostMultiplier();
            plugin.setConfiguredPlaytestXpBoostMultiplier(cycleDoubleOption(current, XP_OPTIONS, event.isLeftClick()));
            openPlaytestMenu(player);
            return;
        }
        if (slot == DAYLIGHT_SLOT) {
            plugin.setConfiguredPlaytestDaylightCycleEnabled(!plugin.isConfiguredPlaytestDaylightCycleEnabled());
            openPlaytestMenu(player);
            return;
        }
        if (slot == PVP_SLOT) {
            plugin.setConfiguredPlaytestPvpEnabled(!plugin.isConfiguredPlaytestPvpEnabled());
            openPlaytestMenu(player);
            return;
        }
        if (slot == COUNTRY_DATA_SLOT) {
            plugin.setConfiguredPlaytestKeepCountryData(!plugin.isConfiguredPlaytestKeepCountryData());
            openPlaytestMenu(player);
            return;
        }
        if (slot == PLAYER_PROGRESS_SLOT) {
            plugin.setConfiguredPlaytestSavePlayerProgression(!plugin.isConfiguredPlaytestSavePlayerProgression());
            openPlaytestMenu(player);
            return;
        }
        if (slot == START_SLOT) {
            if (!plugin.startPlaytest(plugin.getConfiguredPlaytestDurationMillis(), player.getName())) {
                player.sendMessage(plugin.getMessage("terra.playtest.already-running"));
            } else {
                player.sendMessage(plugin.getMessage("terra.playtest.starting", plugin.placeholders(
                        "time", plugin.formatLongDurationWords(plugin.getConfiguredPlaytestDurationMillis())
                )));
                player.closeInventory();
            }
            return;
        }
        if (slot == STOP_SLOT) {
            if (!plugin.stopPlaytestNow()) {
                player.sendMessage(plugin.getMessage("terra.playtest.status-inactive"));
            } else {
                player.sendMessage(plugin.getMessage("terra.playtest.stop-requested"));
                player.closeInventory();
            }
            return;
        }
        if (slot == CLOSE_SLOT) {
            player.closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof PlaytestMenuHolder) {
            event.setCancelled(true);
        }
    }

    private ItemStack createStatusItem() {
        List<String> lore = new ArrayList<>();
        if (plugin.isPlaytestPreparing()) {
            lore.add("&7Status: &ePreparing");
            lore.add("&7Starts with the settings shown below.");
        } else if (plugin.isPlaytestActive()) {
            lore.add("&7Status: &aActive");
            lore.add("&7Time left: &f" + plugin.formatPlaytestRemainingDuration(plugin.getPlaytestRemainingMillis()));
            lore.add("&7XP boost: &f" + plugin.formatXpBoostMultiplier(plugin.getPlaytestXpBoostMultiplier()));
            lore.add("&7PvP: &f" + yesNo(plugin.isPlaytestPvpEnabled()));
            lore.add("&7Day/night cycle: &f" + (plugin.isPlaytestDaylightCycleEnabled() ? "Enabled" : "Disabled"));
            lore.add("&7Keep country data: &f" + yesNo(plugin.isPlaytestKeepCountryData()));
            lore.add("&7Save progression: &f" + yesNo(plugin.isPlaytestSavePlayerProgression()));
        } else {
            lore.add("&7Status: &cInactive");
            lore.add("&7Configure the next playtest below.");
        }
        lore.add("");
        lore.add("&7Configured next session:");
        lore.add("&fDuration: &e" + plugin.formatLongDurationWords(plugin.getConfiguredPlaytestDurationMillis()));
        lore.add("&fXP boost: &e" + plugin.formatXpBoostMultiplier(plugin.getConfiguredPlaytestXpBoostMultiplier()));
        lore.add("&fPvP: &e" + yesNo(plugin.isConfiguredPlaytestPvpEnabled()));
        lore.add("&fDay/night cycle: &e" + yesNo(plugin.isConfiguredPlaytestDaylightCycleEnabled()));
        lore.add("&fKeep country data: &e" + yesNo(plugin.isConfiguredPlaytestKeepCountryData()));
        lore.add("&fSave progression: &e" + yesNo(plugin.isConfiguredPlaytestSavePlayerProgression()));
        return createItem(Material.COMPASS, "&6Playtest Status", lore);
    }

    private ItemStack createToggleItem(Material material, String title, boolean enabled, String description) {
        return createItem(material, title,
                List.of(
                        "&7" + description,
                        "",
                        "&7Current: " + (enabled ? "&aEnabled" : "&cDisabled"),
                        "&eClick to toggle"
                ));
    }

    private ItemStack createSettingItem(Material material, String title, List<String> lore) {
        return createItem(material, title, lore);
    }

    private ItemStack createActionItem(Material material, String title, List<String> lore) {
        return createItem(material, title, lore);
    }

    private ItemStack createItem(Material material, String displayName, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.displayName(plugin.legacyComponent(displayName));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        for (String line : loreLines) {
            lore.add(plugin.legacyComponent(line));
        }
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.legacyComponent("&7"));
            filler.setItemMeta(meta);
        }

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
    }

    private boolean canManagePlaytest(Player player) {
        return player.isOp() || player.hasPermission(Testproject.ADMIN_PERMISSION);
    }

    private long cycleLongOption(long current, long[] options, boolean forward) {
        int index = 0;
        for (int i = 0; i < options.length; i++) {
            if (options[i] == current) {
                index = i;
                break;
            }
        }
        index = forward ? (index + 1) % options.length : (index - 1 + options.length) % options.length;
        return options[index];
    }

    private double cycleDoubleOption(double current, double[] options, boolean forward) {
        int index = 0;
        for (int i = 0; i < options.length; i++) {
            if (Math.abs(options[i] - current) < 0.0001D) {
                index = i;
                break;
            }
        }
        index = forward ? (index + 1) % options.length : (index - 1 + options.length) % options.length;
        return options[index];
    }

    private String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }

    private record PlaytestMenuHolder(UUID viewerId) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
