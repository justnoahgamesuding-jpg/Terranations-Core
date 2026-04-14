package me.meetrow.testproject;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ProfessionSelectionListener implements Listener {
    private static final int DETAIL_SIZE = 54;
    private static final int LEVEL_DETAIL_SIZE = 54;
    private static final int DETAIL_INFO_SLOT = 13;
    private static final int DETAIL_JOIN_SLOT = 49;
    private static final int DETAIL_BACK_SLOT = 45;
    private static final int[] DETAIL_LEVEL_SLOTS = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
    private static final int LEVEL_DETAIL_INFO_SLOT = 4;
    private static final int LEVEL_DETAIL_BACK_SLOT = 45;
    private static final int[] LEVEL_DETAIL_ACTION_SLOTS = {19, 20, 21, 22, 23, 24, 25};
    private static final int[] LEVEL_DETAIL_BLOCK_SLOTS = {28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    private final Testproject plugin;

    public ProfessionSelectionListener(Testproject plugin) {
        this.plugin = plugin;
    }

    public void openSelectionMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(
                new SelectionMenuHolder(),
                plugin.getProfessionGuiSize(),
                plugin.legacyComponent(plugin.getProfessionGuiTitle())
        );
        fillEmptySlots(inventory);
        for (Profession profession : plugin.getConfiguredProfessions()) {
            int slot = plugin.getProfessionSlot(profession);
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, createProfessionSelectionItem(player, profession));
            }
        }
        player.openInventory(inventory);
    }

    public void openProfessionDetailMenu(Player player, Profession profession) {
        Inventory inventory = Bukkit.createInventory(
                new ProfessionDetailMenuHolder(profession),
                DETAIL_SIZE,
                plugin.legacyComponent(plugin.getProfessionDetailGuiTitle(profession))
        );

        fillEmptySlots(inventory);
        inventory.setItem(DETAIL_INFO_SLOT, createProfessionDetailInfoItem(player, profession));

        int maxLevel = plugin.getProfessionBaseMaxLevel(profession);
        for (int i = 0; i < DETAIL_LEVEL_SLOTS.length; i++) {
            int level = i + 1;
            if (level > maxLevel) {
                break;
            }
            inventory.setItem(DETAIL_LEVEL_SLOTS[i], createLevelProgressItem(player, profession, level));
        }

        inventory.setItem(DETAIL_BACK_SLOT, createSimpleItem(
                Material.ARROW,
                "&eBack",
                List.of("&7Return to the jobs list.")
        ));
        inventory.setItem(DETAIL_JOIN_SLOT, createJoinItem(player, profession));
        player.openInventory(inventory);
    }

    public void openProfessionLevelDetailMenu(Player player, Profession profession, int level) {
        Inventory inventory = Bukkit.createInventory(
                new ProfessionLevelDetailMenuHolder(profession, level),
                LEVEL_DETAIL_SIZE,
                plugin.legacyComponent(plugin.getProfessionLevelDetailGuiTitle(profession, level))
        );

        fillEmptySlots(inventory);
        inventory.setItem(LEVEL_DETAIL_INFO_SLOT, createLevelDetailInfoItem(player, profession, level));

        List<String> unlocks = plugin.getProfessionUnlockDescriptions(profession, level);
        for (int i = 0; i < LEVEL_DETAIL_ACTION_SLOTS.length && i < unlocks.size(); i++) {
            inventory.setItem(LEVEL_DETAIL_ACTION_SLOTS[i], createSimpleItem(
                    Material.PAPER,
                    "&eAction Unlock",
                    List.of("&f" + unlocks.get(i))
            ));
        }

        List<Material> blocks = plugin.getProfessionLevelUnlockedBlocks(profession, level);
        for (int i = 0; i < LEVEL_DETAIL_BLOCK_SLOTS.length && i < blocks.size(); i++) {
            Material material = blocks.get(i);
            inventory.setItem(LEVEL_DETAIL_BLOCK_SLOTS[i], createSimpleItem(
                    material,
                    "&a" + plugin.formatMaterialName(material),
                    List.of("&7Level unlock block", "&7XP: &f" + plugin.getProfessionLevelBlockXp(profession, level, material))
            ));
        }

        inventory.setItem(LEVEL_DETAIL_BACK_SLOT, createSimpleItem(
                Material.ARROW,
                "&eBack",
                List.of("&7Return to the profession overview.")
        ));
        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.deliverPendingStarterKits(event.getPlayer());
        plugin.handleTutorialJoin(event.getPlayer());
        if (!plugin.requiresProfessionSelection(event.getPlayer()) || plugin.isTutorialIntroActive(event.getPlayer().getUniqueId())) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline()
                    && plugin.requiresProfessionSelection(event.getPlayer())
                    && !plugin.isTutorialIntroActive(event.getPlayer().getUniqueId())) {
                plugin.openProfessionMenu(event.getPlayer());
            }
        }, 2L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            plugin.ensurePlayerGuidanceItem(player);
            plugin.applyPersonalSkillEffects(player);
            Profession primaryProfession = plugin.getPrimaryProfession(player.getUniqueId());
            if (primaryProfession != null) {
                plugin.grantProfessionStarterKit(player.getUniqueId(), primaryProfession);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof ProfessionInventoryHolder professionHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
            return;
        }

        if (professionHolder instanceof SelectionMenuHolder) {
            Profession selected = getProfessionBySlot(event.getSlot());
            if (selected != null) {
                openProfessionDetailMenu(player, selected);
            }
            return;
        }

        if (!(professionHolder instanceof ProfessionDetailMenuHolder detailHolder)) {
            if (professionHolder instanceof ProfessionLevelDetailMenuHolder levelDetailHolder) {
                if (event.getSlot() == LEVEL_DETAIL_BACK_SLOT) {
                    openProfessionDetailMenu(player, levelDetailHolder.profession());
                }
                return;
            }
            return;
        }

        if (event.getSlot() == DETAIL_BACK_SLOT) {
            openSelectionMenu(player);
            return;
        }

        int clickedLevel = getLevelBySlot(event.getSlot(), plugin.getProfessionBaseMaxLevel(detailHolder.profession()));
        if (clickedLevel > 0) {
            openProfessionLevelDetailMenu(player, detailHolder.profession(), clickedLevel);
            return;
        }

        if (event.getSlot() != DETAIL_JOIN_SLOT) {
            return;
        }

        ProfessionSelectionResult result = plugin.selectProfession(player.getUniqueId(), detailHolder.profession());
        if (result == ProfessionSelectionResult.JOB_FULL
                || result == ProfessionSelectionResult.SECOND_SLOT_LOCKED
                || result == ProfessionSelectionResult.NO_FREE_SLOT) {
            player.sendMessage(plugin.getMessage(
                    result == ProfessionSelectionResult.JOB_FULL
                            ? "profession.job-full"
                            : result == ProfessionSelectionResult.SECOND_SLOT_LOCKED
                            ? "profession.second-slot-locked"
                            : "profession.no-free-slot",
                    plugin.placeholders(
                            "profession", plugin.getProfessionPlainDisplayName(detailHolder.profession()),
                            "cap", String.valueOf(plugin.getProfessionPlayerCap(detailHolder.profession()))
                    )
            ));
            return;
        }

        player.closeInventory();
        if (result == ProfessionSelectionResult.PRIMARY_SELECTED) {
            player.sendMessage(plugin.getMessage("profession.selected-primary", plugin.placeholders(
                    "profession", plugin.getProfessionPlainDisplayName(detailHolder.profession())
            )));
            return;
        }
        if (result == ProfessionSelectionResult.SECONDARY_SELECTED) {
            player.sendMessage(plugin.getMessage("profession.selected-secondary", plugin.placeholders(
                    "profession", plugin.getProfessionPlainDisplayName(detailHolder.profession())
            )));
            return;
        }
        if (result == ProfessionSelectionResult.ACTIVE_SWITCHED) {
            player.sendMessage(plugin.getMessage("profession.switched", plugin.placeholders(
                    "profession", plugin.getProfessionPlainDisplayName(detailHolder.profession())
            )));
            return;
        }
        player.sendMessage(plugin.getMessage("profession.already-active", plugin.placeholders(
                "profession", plugin.getProfessionPlainDisplayName(detailHolder.profession())
        )));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!plugin.requiresProfessionSelection(player)) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline() || !plugin.requiresProfessionSelection(player)) {
                return;
            }
            if (plugin.isTutorialIntroActive(player.getUniqueId())) {
                return;
            }

            InventoryHolder currentHolder = player.getOpenInventory().getTopInventory().getHolder();
            if (currentHolder instanceof ProfessionInventoryHolder) {
                return;
            }
            plugin.openProfessionMenu(player);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.requiresProfessionSelection(event.getPlayer()) || event.getTo() == null) {
            return;
        }
        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getY() == event.getTo().getY()
                && event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }

        event.setTo(event.getFrom());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        if (!plugin.requiresProfessionSelection(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
        if (!plugin.isTutorialIntroActive(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(plugin.getMessage("profession.locked-chat"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && plugin.requiresProfessionSelection(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (plugin.requiresProfessionSelection(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    private ItemStack createProfessionSelectionItem(Player player, Profession profession) {
        List<String> lore = new ArrayList<>();
        lore.addAll(getProfessionPreviewLore(profession));
        lore.add("");
        lore.add("&7Click to view progression, unlocks,");
        lore.add("&7and join options for this job.");
        lore.add("");

        if (plugin.hasProfession(player.getUniqueId(), profession)) {
            int level = plugin.getProfessionLevel(player.getUniqueId(), profession);
            lore.add("&aOwned");
            lore.add("&7Level: &f" + level);
        } else {
            lore.add("&7Not unlocked yet.");
            lore.add("&7Players: &f" + plugin.getProfessionPlayerCount(profession) + "&7/&f" + formatCap(plugin.getProfessionPlayerCap(profession)));
        }

        return createSimpleItem(plugin.getProfessionIcon(profession), plugin.getProfessionDisplayName(profession), lore);
    }

    private ItemStack createProfessionDetailInfoItem(Player player, Profession profession) {
        int level = plugin.hasProfession(player.getUniqueId(), profession)
                ? plugin.getProfessionLevel(player.getUniqueId(), profession)
                : 1;
        int xp = plugin.hasProfession(player.getUniqueId(), profession)
                ? plugin.getProfessionXp(player.getUniqueId(), profession)
                : 0;
        int requiredXp = plugin.hasProfession(player.getUniqueId(), profession)
                ? plugin.getProfessionXpRequired(player.getUniqueId(), profession)
                : plugin.getProfessionXpRequiredForLevel(profession, 1);

        List<String> lore = new ArrayList<>();
        lore.add("&7Current level: &f" + level);
        lore.add("&7Progress: &f" + xp + "&7/&f" + requiredXp + " xp");
        lore.add("&7Max level: &f" + plugin.getProfessionBaseMaxLevel(profession));
        lore.add("");
        lore.addAll(getProfessionPreviewLore(profession));
        lore.add("");
        if (plugin.hasProfession(player.getUniqueId(), profession)) {
            lore.add(plugin.getProfession(player.getUniqueId()) == profession ? "&aCurrently active." : "&eOwned but inactive.");
        } else {
            lore.add("&7Preview this job before joining.");
        }
        return createSimpleItem(plugin.getProfessionIcon(profession), plugin.getProfessionDisplayName(profession), lore);
    }

    private List<String> getProfessionPreviewLore(Profession profession) {
        return switch (profession) {
            case MINER -> List.of(
                    "&7Role: gather stone, minerals, and ores.",
                    "&7Use pickaxes or shovels and push into",
                    "&7better mining routes as your level rises."
            );
            case LUMBERJACK -> List.of(
                    "&7Role: gather wood and timber.",
                    "&7Use axes to chop logs and build up",
                    "&7a steady supply chain for builders and blacksmiths."
            );
            case FARMER -> List.of(
                    "&7Role: grow food and farm goods.",
                    "&7Plant, harvest, and manage crop routes",
                    "&7to feed players and support country growth."
            );
            case BUILDER -> List.of(
                    "&7Role: shape structures and settlements.",
                    "&7Earn progression through building work",
                    "&7and unlock more advanced blocks over time."
            );
            case BLACKSMITH -> List.of(
                    "&7Role: forge tools and equipment.",
                    "&7Use the forge systems to craft higher-tier",
                    "&7gear that other jobs cannot make normally."
            );
        };
    }

    private ItemStack createLevelProgressItem(Player player, Profession profession, int level) {
        boolean owned = plugin.hasProfession(player.getUniqueId(), profession);
        int currentLevel = owned ? plugin.getProfessionLevel(player.getUniqueId(), profession) : 0;
        boolean unlocked = owned && currentLevel >= level;
        boolean current = owned && currentLevel == level;

        Material material = current ? Material.GLOWSTONE_DUST : unlocked ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        double moneyReward = plugin.getProfessionLevelUpMoneyReward(profession, level);
        List<String> lore = new ArrayList<>();

        if (current) {
            lore.add("&aCurrent level.");
        } else if (unlocked) {
            lore.add("&aUnlocked.");
        } else {
            lore.add("&7Unlocks at this level.");
        }
        lore.add("&7Click to inspect this level.");

        if (level > 1 && moneyReward > 0.0D) {
            lore.add("&7Level reward: &f$" + String.format(Locale.US, "%.2f", moneyReward));
        }

        List<String> unlocks = plugin.getProfessionUnlockDescriptions(profession, level);
        if (!unlocks.isEmpty()) {
            lore.add("");
            lore.add("&eUnlocks:");
            for (String unlock : unlocks) {
                lore.add("&7- &f" + unlock);
            }
        } else {
            lore.add("");
            lore.add("&7No unlock entries configured.");
        }

        return createSimpleItem(material, "&6Level " + level, lore);
    }

    private ItemStack createLevelDetailInfoItem(Player player, Profession profession, int level) {
        boolean owned = plugin.hasProfession(player.getUniqueId(), profession);
        int currentLevel = owned ? plugin.getProfessionLevel(player.getUniqueId(), profession) : 0;
        double moneyReward = plugin.getProfessionLevelUpMoneyReward(profession, level);
        List<String> lore = new ArrayList<>();
        lore.add("&7Profession: &f" + plugin.getProfessionPlainDisplayName(profession));
        lore.add("&7Level: &f" + level);
        lore.add("&7Status: &f" + (currentLevel >= level ? "Unlocked" : "Locked"));
        if (moneyReward > 0.0D && level > 1) {
            lore.add("&7Money reward: &f$" + String.format(Locale.US, "%.2f", moneyReward));
        }
        lore.add("");
        lore.add("&7Top row: action unlocks");
        lore.add("&7Bottom row: blocks and XP");
        return createSimpleItem(plugin.getProfessionIcon(profession), plugin.getProfessionDisplayName(profession), lore);
    }

    private ItemStack createJoinItem(Player player, Profession profession) {
        UUID playerId = player.getUniqueId();
        boolean owned = plugin.hasProfession(playerId, profession);
        boolean active = plugin.getProfession(playerId) == profession;

        if (active) {
            return createSimpleItem(Material.GREEN_CONCRETE, "&aAlready Active", List.of(
                    "&7This job is already your active job."
            ));
        }

        if (owned) {
            return createSimpleItem(Material.LIME_CONCRETE, "&aSwitch To Job", List.of(
                    "&7Set this as your current active job."
            ));
        }

        return createSimpleItem(Material.EMERALD, "&aJoin Job", List.of(
                "&7Unlock this job in your next free slot.",
                "&7If this is your first job, it becomes your primary.",
                "",
                "&7Players: &f" + plugin.getProfessionPlayerCount(profession) + "&7/&f" + formatCap(plugin.getProfessionPlayerCap(profession))
        ));
    }

    private String formatCap(int cap) {
        return cap <= 0 ? "Unlimited" : String.valueOf(cap);
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
        for (Profession profession : plugin.getConfiguredProfessions()) {
            if (plugin.getProfessionSlot(profession) == slot) {
                return profession;
            }
        }
        return null;
    }

    private int getLevelBySlot(int slot, int maxLevel) {
        for (int i = 0; i < DETAIL_LEVEL_SLOTS.length; i++) {
            if (DETAIL_LEVEL_SLOTS[i] == slot) {
                int level = i + 1;
                return level <= maxLevel ? level : -1;
            }
        }
        return -1;
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

    private interface ProfessionInventoryHolder extends InventoryHolder {
    }

    private static final class SelectionMenuHolder implements ProfessionInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record ProfessionDetailMenuHolder(Profession profession) implements ProfessionInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record ProfessionLevelDetailMenuHolder(Profession profession, int level) implements ProfessionInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
