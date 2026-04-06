package me.meetrow.testproject;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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

public class ProfessionAdminGuiListener implements Listener {
    private static final int GUI_SIZE = 54;
    private static final int TARGET_INFO_SLOT = 4;
    private static final int PRIMARY_SLOT = 10;
    private static final int SECONDARY_SLOT = 12;
    private static final int ACTIVE_SLOT = 14;
    private static final int SETTINGS_SLOT = 16;
    private static final int SET_PRIMARY_SLOT = 21;
    private static final int SET_SECONDARY_SLOT = 22;
    private static final int SET_ACTIVE_SLOT = 23;
    private static final int TOGGLE_SECONDARY_UNLOCK_SLOT = 24;
    private static final int TOGGLE_DEVELOPMENT_MODE_SLOT = 25;
    private static final int CLEAR_SECONDARY_SLOT = 26;
    private static final int[] PROFESSION_SLOTS = {28, 29, 30, 31, 32, 33, 34};
    private static final int LEVEL_MINUS_TEN_SLOT = 45;
    private static final int LEVEL_MINUS_ONE_SLOT = 46;
    private static final int LEVEL_PLUS_ONE_SLOT = 47;
    private static final int LEVEL_PLUS_TEN_SLOT = 48;
    private static final int SELECTED_INFO_SLOT = 49;
    private static final int XP_MINUS_HUNDRED_SLOT = 50;
    private static final int XP_MINUS_TEN_SLOT = 51;
    private static final int XP_PLUS_TEN_SLOT = 52;
    private static final int XP_PLUS_HUNDRED_SLOT = 53;

    private final Testproject plugin;

    public ProfessionAdminGuiListener(Testproject plugin) {
        this.plugin = plugin;
    }

    public void openAdminMenu(Player admin, OfflinePlayer target) {
        openAdminMenu(admin, target, plugin.getPrimaryProfession(target.getUniqueId()));
    }

    private void openAdminMenu(Player admin, OfflinePlayer target, Profession selectedProfession) {
        Inventory inventory = Bukkit.createInventory(
                new AdminMenuHolder(target.getUniqueId(), selectedProfession),
                GUI_SIZE,
                plugin.legacyComponent("&8Job Admin: " + plugin.safeOfflineName(target))
        );

        fillEmptySlots(inventory);
        inventory.setItem(TARGET_INFO_SLOT, createTargetInfoItem(target));
        inventory.setItem(PRIMARY_SLOT, createOwnedProfessionItem("&6Primary Job", target.getUniqueId(), plugin.getPrimaryProfession(target.getUniqueId())));
        inventory.setItem(SECONDARY_SLOT, createOwnedProfessionItem("&6Second Job", target.getUniqueId(), plugin.getSecondaryProfession(target.getUniqueId())));
        inventory.setItem(ACTIVE_SLOT, createActiveJobItem(target.getUniqueId()));
        inventory.setItem(SETTINGS_SLOT, createSettingsItem(target.getUniqueId()));
        inventory.setItem(SET_PRIMARY_SLOT, createActionItem(Material.GOLD_INGOT, "&aSet Selected As Primary",
                List.of("&7Assign the selected profession as the", "&7player's primary job.", "", "&eRequires a selected job.")));
        inventory.setItem(SET_SECONDARY_SLOT, createActionItem(Material.IRON_INGOT, "&aSet Selected As Secondary",
                List.of("&7Assign the selected profession as the", "&7player's second job.", "", "&eRequires a selected job.")));
        inventory.setItem(SET_ACTIVE_SLOT, createActionItem(Material.LIME_DYE, "&aApply Selected As Active",
                List.of("&7Without development mode:", "&7Sets the stored active owned job.", "", "&7With development mode:", "&7Sets the temporary debug job.")));
        inventory.setItem(TOGGLE_SECONDARY_UNLOCK_SLOT, createSecondaryUnlockItem(target.getUniqueId()));
        inventory.setItem(TOGGLE_DEVELOPMENT_MODE_SLOT, createDevelopmentModeItem(target.getUniqueId(), selectedProfession));
        inventory.setItem(CLEAR_SECONDARY_SLOT, createActionItem(Material.BARRIER, "&cClear Secondary Job",
                List.of("&7Remove the player's second job slot.")));

        List<Profession> professions = plugin.getConfiguredProfessions();
        for (int i = 0; i < PROFESSION_SLOTS.length && i < professions.size(); i++) {
            Profession profession = professions.get(i);
            inventory.setItem(PROFESSION_SLOTS[i], createProfessionSelectionItem(target.getUniqueId(), profession, profession == selectedProfession));
        }

        inventory.setItem(LEVEL_MINUS_TEN_SLOT, createDeltaItem(Material.RED_STAINED_GLASS_PANE, "&cLevel -10", -10, "level"));
        inventory.setItem(LEVEL_MINUS_ONE_SLOT, createDeltaItem(Material.ORANGE_STAINED_GLASS_PANE, "&cLevel -1", -1, "level"));
        inventory.setItem(LEVEL_PLUS_ONE_SLOT, createDeltaItem(Material.LIME_STAINED_GLASS_PANE, "&aLevel +1", 1, "level"));
        inventory.setItem(LEVEL_PLUS_TEN_SLOT, createDeltaItem(Material.GREEN_STAINED_GLASS_PANE, "&aLevel +10", 10, "level"));
        inventory.setItem(SELECTED_INFO_SLOT, createSelectedInfoItem(target.getUniqueId(), selectedProfession));
        inventory.setItem(XP_MINUS_HUNDRED_SLOT, createDeltaItem(Material.REDSTONE, "&cXP -100", -100, "xp"));
        inventory.setItem(XP_MINUS_TEN_SLOT, createDeltaItem(Material.RED_DYE, "&cXP -10", -10, "xp"));
        inventory.setItem(XP_PLUS_TEN_SLOT, createDeltaItem(Material.LIME_DYE, "&aXP +10", 10, "xp"));
        inventory.setItem(XP_PLUS_HUNDRED_SLOT, createDeltaItem(Material.EXPERIENCE_BOTTLE, "&aXP +100", 100, "xp"));

        admin.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player admin)) {
            return;
        }

        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof AdminMenuHolder adminMenuHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(adminMenuHolder.targetId());
        Profession selectedProfession = adminMenuHolder.selectedProfession();
        UUID targetId = target.getUniqueId();

        Profession clickedProfession = getProfessionBySlot(event.getSlot());
        if (clickedProfession != null) {
            openAdminMenu(admin, target, clickedProfession);
            return;
        }

        switch (event.getSlot()) {
            case SET_PRIMARY_SLOT -> {
                if (selectedProfession == null) {
                    admin.sendMessage(plugin.getMessage("profession.unknown"));
                    return;
                }
                if (!plugin.adminSetPrimaryProfession(targetId, selectedProfession)) {
                    admin.sendMessage(plugin.getMessage("profession.job-full", plugin.placeholders(
                            "profession", plugin.getProfessionPlainDisplayName(selectedProfession),
                            "cap", String.valueOf(plugin.getProfessionPlayerCap(selectedProfession))
                    )));
                    return;
                }
                plugin.switchActiveProfession(targetId, selectedProfession);
                admin.sendMessage(plugin.getMessage("profession.admin.set-primary", plugin.placeholders(
                        "player", plugin.safeOfflineName(target),
                        "profession", plugin.getProfessionPlainDisplayName(selectedProfession)
                )));
            }
            case SET_SECONDARY_SLOT -> {
                if (selectedProfession == null) {
                    admin.sendMessage(plugin.getMessage("profession.unknown"));
                    return;
                }
                if (plugin.getPrimaryProfession(targetId) == null) {
                    admin.sendMessage(plugin.getMessage("profession.admin.primary-required"));
                    return;
                }
                if (!plugin.adminSetSecondaryProfession(targetId, selectedProfession)) {
                    admin.sendMessage(plugin.getMessage("profession.job-full", plugin.placeholders(
                            "profession", plugin.getProfessionPlainDisplayName(selectedProfession),
                            "cap", String.valueOf(plugin.getProfessionPlayerCap(selectedProfession))
                    )));
                    return;
                }
                admin.sendMessage(plugin.getMessage("profession.admin.set-secondary", plugin.placeholders(
                        "player", plugin.safeOfflineName(target),
                        "profession", plugin.getProfessionPlainDisplayName(selectedProfession)
                )));
            }
            case SET_ACTIVE_SLOT -> {
                if (selectedProfession == null) {
                    admin.sendMessage(plugin.getMessage("profession.unknown"));
                    return;
                }
                if (plugin.isProfessionDevelopmentModeEnabled(targetId)) {
                    plugin.setProfessionDevelopmentModeJob(targetId, selectedProfession);
                    admin.sendMessage(plugin.getMessage("profession.admin.set-development-job", plugin.placeholders(
                            "player", plugin.safeOfflineName(target),
                            "profession", plugin.getProfessionPlainDisplayName(selectedProfession)
                    )));
                } else if (plugin.hasProfession(targetId, selectedProfession)) {
                    plugin.switchActiveProfession(targetId, selectedProfession);
                    admin.sendMessage(plugin.getMessage("profession.admin.set-active", plugin.placeholders(
                            "player", plugin.safeOfflineName(target),
                            "profession", plugin.getProfessionPlainDisplayName(selectedProfession)
                    )));
                } else {
                    admin.sendMessage(plugin.getMessage("profession.not-owned"));
                    return;
                }
            }
            case TOGGLE_SECONDARY_UNLOCK_SLOT -> {
                plugin.cycleSecondProfessionUnlockOverride(targetId);
            }
            case TOGGLE_DEVELOPMENT_MODE_SLOT -> {
                if (!plugin.isProfessionDevelopmentModeEnabled(targetId)) {
                    Profession developmentProfession = selectedProfession != null
                            ? selectedProfession
                            : plugin.getStoredActiveProfession(targetId);
                    if (developmentProfession == null) {
                        admin.sendMessage(plugin.getMessage("profession.admin.development-job-required"));
                        return;
                    }
                    plugin.setProfessionDevelopmentModeJob(targetId, developmentProfession);
                    admin.sendMessage(plugin.getMessage("profession.admin.development-enabled", plugin.placeholders(
                            "player", plugin.safeOfflineName(target),
                            "profession", plugin.getProfessionPlainDisplayName(developmentProfession)
                    )));
                } else {
                    plugin.toggleProfessionDevelopmentMode(targetId);
                    admin.sendMessage(plugin.getMessage("profession.admin.development-disabled", plugin.placeholders(
                            "player", plugin.safeOfflineName(target)
                    )));
                }
            }
            case CLEAR_SECONDARY_SLOT -> {
                plugin.adminClearSecondaryProfession(targetId);
                admin.sendMessage(plugin.getMessage("profession.admin.cleared-secondary", plugin.placeholders(
                        "player", plugin.safeOfflineName(target)
                )));
            }
            case LEVEL_MINUS_TEN_SLOT -> applyLevelDelta(admin, target, selectedProfession, -10);
            case LEVEL_MINUS_ONE_SLOT -> applyLevelDelta(admin, target, selectedProfession, -1);
            case LEVEL_PLUS_ONE_SLOT -> applyLevelDelta(admin, target, selectedProfession, 1);
            case LEVEL_PLUS_TEN_SLOT -> applyLevelDelta(admin, target, selectedProfession, 10);
            case XP_MINUS_HUNDRED_SLOT -> applyXpDelta(admin, target, selectedProfession, -100);
            case XP_MINUS_TEN_SLOT -> applyXpDelta(admin, target, selectedProfession, -10);
            case XP_PLUS_TEN_SLOT -> applyXpDelta(admin, target, selectedProfession, 10);
            case XP_PLUS_HUNDRED_SLOT -> applyXpDelta(admin, target, selectedProfession, 100);
            default -> {
                return;
            }
        }

        openAdminMenu(admin, target, selectedProfession);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof AdminMenuHolder) {
            event.setCancelled(true);
        }
    }

    private void applyLevelDelta(Player admin, OfflinePlayer target, Profession selectedProfession, int delta) {
        if (selectedProfession == null || !plugin.hasProfession(target.getUniqueId(), selectedProfession)) {
            admin.sendMessage(plugin.getMessage("profession.not-owned"));
            return;
        }

        int currentLevel = plugin.getProfessionLevel(target.getUniqueId(), selectedProfession);
        int nextLevel = Math.max(1, currentLevel + delta);
        plugin.adminSetProfessionLevel(target.getUniqueId(), selectedProfession, nextLevel);
        admin.sendMessage(plugin.getMessage("profession.admin.set-level", plugin.placeholders(
                "player", plugin.safeOfflineName(target),
                "profession", plugin.getProfessionPlainDisplayName(selectedProfession),
                "level", String.valueOf(plugin.getProfessionLevel(target.getUniqueId(), selectedProfession))
        )));
    }

    private void applyXpDelta(Player admin, OfflinePlayer target, Profession selectedProfession, int delta) {
        if (selectedProfession == null || !plugin.hasProfession(target.getUniqueId(), selectedProfession)) {
            admin.sendMessage(plugin.getMessage("profession.not-owned"));
            return;
        }

        int currentXp = plugin.getProfessionXp(target.getUniqueId(), selectedProfession);
        int nextXp = Math.max(0, currentXp + delta);
        plugin.adminSetProfessionXp(target.getUniqueId(), selectedProfession, nextXp);
        admin.sendMessage(plugin.getMessage("profession.admin.set-xp", plugin.placeholders(
                "player", plugin.safeOfflineName(target),
                "profession", plugin.getProfessionPlainDisplayName(selectedProfession),
                "xp", String.valueOf(plugin.getProfessionXp(target.getUniqueId(), selectedProfession))
        )));
    }

    private ItemStack createTargetInfoItem(OfflinePlayer target) {
        UUID targetId = target.getUniqueId();
        Profession primary = plugin.getPrimaryProfession(targetId);
        Profession secondary = plugin.getSecondaryProfession(targetId);
        Profession active = plugin.getStoredActiveProfession(targetId);
        Profession development = plugin.getDevelopmentModeProfession(targetId);

        List<String> lore = new ArrayList<>();
        lore.add("&7Primary: &f" + formatProfession(primary));
        lore.add("&7Secondary: &f" + formatProfession(secondary));
        lore.add("&7Stored active: &f" + formatProfession(active));
        lore.add("&7Development job: &f" + formatProfession(development));
        lore.add("");
        lore.add("&7Selected profession controls are below.");
        return createSimpleItem(Material.NAME_TAG, "&e" + plugin.safeOfflineName(target), lore);
    }

    private ItemStack createOwnedProfessionItem(String title, UUID targetId, Profession profession) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Job: &f" + formatProfession(profession));
        if (profession != null) {
            lore.add("&7Level: &f" + plugin.getProfessionLevel(targetId, profession));
            lore.add("&7XP: &f" + plugin.getProfessionXp(targetId, profession));
        }
        return createSimpleItem(profession != null ? plugin.getProfessionIcon(profession) : Material.GRAY_DYE, title, lore);
    }

    private ItemStack createActiveJobItem(UUID targetId) {
        Profession currentVisible = plugin.getProfession(targetId);
        Profession storedActive = plugin.getStoredActiveProfession(targetId);
        Profession development = plugin.getDevelopmentModeProfession(targetId);

        List<String> lore = new ArrayList<>();
        lore.add("&7Current visible job: &f" + formatProfession(currentVisible));
        lore.add("&7Stored active job: &f" + formatProfession(storedActive));
        lore.add("&7Development mode: &f" + (development != null ? "Enabled" : "Disabled"));
        if (development != null) {
            lore.add("&7Debug job: &f" + plugin.getProfessionPlainDisplayName(development));
        }
        return createSimpleItem(Material.COMPASS, "&bActive State", lore);
    }

    private ItemStack createSettingsItem(UUID targetId) {
        Boolean secondSlotOverride = plugin.getSecondProfessionUnlockOverride(targetId);
        String secondSlotState = secondSlotOverride == null
                ? "Default (" + (plugin.isSecondProfessionNaturallyUnlocked(targetId) ? "Unlocked" : "Locked") + ")"
                : secondSlotOverride ? "Forced Unlocked" : "Forced Locked";
        List<String> lore = new ArrayList<>();
        lore.add("&7Second slot state: &f" + secondSlotState);
        lore.add("&7Development mode: &f" + (plugin.isProfessionDevelopmentModeEnabled(targetId) ? "Enabled" : "Disabled"));
        lore.add("");
        lore.add("&7Use the control buttons to change these.");
        return createSimpleItem(Material.COMPARATOR, "&eAdmin Settings", lore);
    }

    private ItemStack createSecondaryUnlockItem(UUID targetId) {
        Boolean override = plugin.getSecondProfessionUnlockOverride(targetId);
        Material material = override == null ? Material.CLOCK : override ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        String state = override == null
                ? "Default progression rule"
                : override ? "Forced unlocked" : "Forced locked";
        return createSimpleItem(material, "&eSecond Job Unlock Rule", List.of(
                "&7Current: &f" + state,
                "&7Natural state: &f" + (plugin.isSecondProfessionNaturallyUnlocked(targetId) ? "Unlocked" : "Locked"),
                "",
                "&eClick to cycle:",
                "&7Default -> Forced unlocked -> Forced locked"
        ));
    }

    private ItemStack createDevelopmentModeItem(UUID targetId, Profession selectedProfession) {
        boolean enabled = plugin.isProfessionDevelopmentModeEnabled(targetId);
        Profession developmentProfession = plugin.getDevelopmentModeProfession(targetId);
        List<String> lore = new ArrayList<>();
        lore.add("&7Current state: &f" + (enabled ? "Enabled" : "Disabled"));
        lore.add("&7Current debug job: &f" + formatProfession(developmentProfession));
        if (selectedProfession != null) {
            lore.add("&7Selected job: &f" + plugin.getProfessionPlainDisplayName(selectedProfession));
        }
        lore.add("");
        lore.add("&7When enabled, the selected job can be");
        lore.add("&7applied as a temporary debug profession.");
        return createSimpleItem(enabled ? Material.LIME_CANDLE : Material.GRAY_CANDLE, "&eDevelopment Mode", lore);
    }

    private ItemStack createProfessionSelectionItem(UUID targetId, Profession profession, boolean selected) {
        boolean owned = plugin.hasProfession(targetId, profession);
        boolean primary = profession == plugin.getPrimaryProfession(targetId);
        boolean secondary = profession == plugin.getSecondaryProfession(targetId);
        boolean active = profession == plugin.getStoredActiveProfession(targetId);
        boolean development = profession == plugin.getDevelopmentModeProfession(targetId);

        List<String> lore = new ArrayList<>();
        lore.add("&7Owned: &f" + (owned ? "Yes" : "No"));
        lore.add("&7Role: &f" + (primary ? "Primary" : secondary ? "Secondary" : "Unassigned"));
        if (owned) {
            lore.add("&7Level: &f" + plugin.getProfessionLevel(targetId, profession));
            lore.add("&7XP: &f" + plugin.getProfessionXp(targetId, profession));
        }
        lore.add("&7Players: &f" + plugin.getProfessionPlayerCount(profession) + "&7/&f" + formatCap(plugin.getProfessionPlayerCap(profession)));
        lore.add("&7Stored active: &f" + (active ? "Yes" : "No"));
        lore.add("&7Development job: &f" + (development ? "Yes" : "No"));
        lore.add("");
        lore.add(selected ? "&aCurrently selected." : "&eClick to select this job.");
        return createSimpleItem(plugin.getProfessionIcon(profession), (selected ? "&a" : "&f") + plugin.getProfessionPlainDisplayName(profession), lore);
    }

    private ItemStack createSelectedInfoItem(UUID targetId, Profession profession) {
        if (profession == null) {
            return createSimpleItem(Material.BOOK, "&eNo Job Selected", List.of(
                    "&7Select one of the jobs above first."
            ));
        }

        boolean owned = plugin.hasProfession(targetId, profession);
        int level = owned ? plugin.getProfessionLevel(targetId, profession) : 1;
        int xp = owned ? plugin.getProfessionXp(targetId, profession) : 0;
        int maxLevel = plugin.getProfessionMaxLevel(targetId, profession);
        int xpRequired = owned ? plugin.getProfessionXpRequired(targetId, profession) : plugin.getProfessionXpRequiredForLevel(profession, 1);

        return createSimpleItem(plugin.getProfessionIcon(profession), "&eSelected: " + plugin.getProfessionPlainDisplayName(profession), List.of(
                "&7Owned: &f" + (owned ? "Yes" : "No"),
                "&7Level: &f" + level + "&7/&f" + maxLevel,
                "&7XP: &f" + xp + "&7/&f" + xpRequired,
                "",
                "&7Use the controls around this item",
                "&7to modify the selected profession."
        ));
    }

    private ItemStack createActionItem(Material material, String name, List<String> lore) {
        return createSimpleItem(material, name, lore);
    }

    private ItemStack createDeltaItem(Material material, String name, int delta, String kind) {
        return createSimpleItem(material, name, List.of(
                "&7Adjust selected " + kind + " by &f" + (delta > 0 ? "+" : "") + delta + "&7.",
                "&7Only works on owned jobs."
        ));
    }

    private ItemStack createSimpleItem(Material material, String displayName, List<String> loreLines) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(plugin.legacyComponent(displayName));
        itemMeta.lore(loreLines.stream().map(plugin::legacyComponent).toList());
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private Profession getProfessionBySlot(int slot) {
        List<Profession> professions = plugin.getConfiguredProfessions();
        for (int i = 0; i < PROFESSION_SLOTS.length && i < professions.size(); i++) {
            if (PROFESSION_SLOTS[i] == slot) {
                return professions.get(i);
            }
        }
        return null;
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack filler = createFillerItem();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
    }

    private ItemStack createFillerItem() {
        ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(plugin.legacyComponent("&7"));
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private String formatProfession(Profession profession) {
        if (profession == null) {
            return "None";
        }
        return plugin.getProfessionPlainDisplayName(profession);
    }

    private String formatCap(int cap) {
        return cap <= 0 ? "Unlimited" : String.valueOf(cap);
    }

    private record AdminMenuHolder(UUID targetId, Profession selectedProfession) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
