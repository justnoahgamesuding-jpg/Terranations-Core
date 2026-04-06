package me.meetrow.testproject;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JobConfigGuiListener implements Listener {
    private static final int GUI_SIZE = 54;
    private static final int INFO_SLOT = 4;
    private static final int PREV_LEVEL_SLOT = 19;
    private static final int LEVEL_SLOT = 22;
    private static final int NEXT_LEVEL_SLOT = 25;
    private static final int BLOCKS_SLOT = 30;
    private static final int ACTIONS_SLOT = 32;
    private static final int STARTER_KIT_SLOT = 34;
    private static final int BACK_SLOT = 49;
    private static final int ADD_BLOCK_SLOT = 49;
    private static final int BLOCK_BACK_SLOT = 45;
    private static final int BLOCK_PAGE_PREV_SLOT = 48;
    private static final int BLOCK_PAGE_NEXT_SLOT = 50;
    private static final int STARTER_BACK_SLOT = 45;
    private static final int STARTER_PAGE_PREV_SLOT = 48;
    private static final int STARTER_PAGE_NEXT_SLOT = 50;
    private static final int ADD_STARTER_ITEM_SLOT = 49;
    private static final int ACTION_BACK_SLOT = 49;
    private static final int EDIT_BACK_SLOT = 49;
    private static final int REMOVE_BLOCK_SLOT = 31;
    private static final int REMOVE_STARTER_ITEM_SLOT = 31;
    private static final int XP_MINUS_TEN_SLOT = 20;
    private static final int XP_MINUS_ONE_SLOT = 21;
    private static final int XP_PLUS_ONE_SLOT = 23;
    private static final int XP_PLUS_TEN_SLOT = 24;
    private static final int AMOUNT_MINUS_SIXTEEN_SLOT = 20;
    private static final int AMOUNT_MINUS_ONE_SLOT = 21;
    private static final int AMOUNT_PLUS_ONE_SLOT = 23;
    private static final int AMOUNT_PLUS_SIXTEEN_SLOT = 24;
    private static final int ACTION_LEVEL_MINUS_SLOT = 20;
    private static final int ACTION_LEVEL_PLUS_SLOT = 24;
    private static final int ACTION_XP_MINUS_ONE_SLOT = 29;
    private static final int ACTION_XP_PLUS_ONE_SLOT = 33;
    private static final int[] PROFESSION_SLOTS = {19, 20, 21, 22, 23, 24, 25};
    private static final int[] BLOCK_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int[] ACTION_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };
    private static final int[] STARTER_KIT_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final Testproject plugin;
    private final Map<UUID, PendingBlockEntry> pendingBlockEntries = new ConcurrentHashMap<>();
    private final Map<UUID, PendingStarterKitEntry> pendingStarterKitEntries = new ConcurrentHashMap<>();

    public JobConfigGuiListener(Testproject plugin) {
        this.plugin = plugin;
    }

    public void openProfessionSelector(Player player) {
        Inventory inventory = Bukkit.createInventory(new ProfessionSelectorHolder(player.getUniqueId()), GUI_SIZE, plugin.legacyComponent("&8Job Config Editor"));
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createSimpleItem(Material.WRITABLE_BOOK, "&6Job Config Editor", List.of(
                "&7Select a job to edit its block unlocks",
                "&7and configurable action rewards."
        )));

        List<Profession> professions = plugin.getConfiguredProfessions();
        for (int i = 0; i < PROFESSION_SLOTS.length && i < professions.size(); i++) {
            Profession profession = professions.get(i);
            inventory.setItem(PROFESSION_SLOTS[i], createSimpleItem(
                    plugin.getProfessionIcon(profession),
                    "&e" + plugin.getProfessionPlainDisplayName(profession),
                    List.of(
                            "&7Max level: &f" + plugin.getProfessionBaseMaxLevel(profession),
                            "&7Click to open the editor."
                    )
            ));
        }
        inventory.setItem(BACK_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        player.openInventory(inventory);
    }

    public void openEditorMenu(Player player, Profession profession, int level) {
        if (profession == null) {
            openProfessionSelector(player);
            return;
        }
        int safeLevel = Math.max(1, Math.min(level, plugin.getProfessionBaseMaxLevel(profession)));
        Inventory inventory = Bukkit.createInventory(new EditorHolder(player.getUniqueId(), profession, safeLevel), GUI_SIZE,
                plugin.legacyComponent("&8Edit " + plugin.getProfessionPlainDisplayName(profession)));
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createSimpleItem(plugin.getProfessionIcon(profession),
                "&6" + plugin.getProfessionPlainDisplayName(profession),
                List.of(
                        "&7Level: &f" + safeLevel + "&7/&f" + plugin.getProfessionBaseMaxLevel(profession),
                        "&7Blocks on this level: &f" + plugin.getProfessionLevelUnlockedBlocks(profession, safeLevel).size(),
                        "&7Actions available: &f" + getActionsForProfession(profession).size(),
                        "&7Starter kit items: &f" + plugin.getProfessionStarterKitEntries(profession).size()
                )));
        inventory.setItem(PREV_LEVEL_SLOT, createSimpleItem(Material.ARROW, "&ePrevious Level", List.of("&7Go down one level.")));
        inventory.setItem(LEVEL_SLOT, createSimpleItem(Material.EXPERIENCE_BOTTLE, "&bLevel " + safeLevel, List.of(
                "&7Use the arrows to change level.",
                "&7Then open blocks or actions."
        )));
        inventory.setItem(NEXT_LEVEL_SLOT, createSimpleItem(Material.ARROW, "&eNext Level", List.of("&7Go up one level.")));
        inventory.setItem(BLOCKS_SLOT, createSimpleItem(Material.GRASS_BLOCK, "&aEdit Blocks", List.of(
                "&7Add or remove block unlocks",
                "&7for this profession level.",
                "&eClick to open"
        )));
        inventory.setItem(ACTIONS_SLOT, createSimpleItem(Material.BLAZE_POWDER, "&6Edit Actions", List.of(
                "&7Change supported action rewards",
                "&7and required levels for this job.",
                "&eClick to open"
        )));
        inventory.setItem(STARTER_KIT_SLOT, createSimpleItem(Material.CHEST, "&bEdit Starter Kit", List.of(
                "&7Add or remove starter kit items",
                "&7for this profession.",
                "&eClick to open"
        )));
        inventory.setItem(BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to profession selection.")));
        player.openInventory(inventory);
    }

    private void openBlocksMenu(Player player, Profession profession, int level, int page) {
        List<Material> blocks = plugin.getProfessionLevelUnlockedBlocks(profession, level);
        int totalPages = Math.max(1, (int) Math.ceil(blocks.size() / (double) BLOCK_SLOTS.length));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));
        Inventory inventory = Bukkit.createInventory(new BlocksHolder(player.getUniqueId(), profession, level, safePage), GUI_SIZE,
                plugin.legacyComponent("&8" + plugin.getProfessionPlainDisplayName(profession) + " Blocks"));
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createSimpleItem(Material.GRASS_BLOCK, "&aLevel " + level + " Blocks", List.of(
                "&7Current page: &f" + (safePage + 1) + "&7/&f" + totalPages,
                "&7Click a block to edit its XP.",
                "&7Use Add Block, then right-click with a block",
                "&7in your hand and type only the XP in chat."
        )));
        int start = safePage * BLOCK_SLOTS.length;
        for (int i = 0; i < BLOCK_SLOTS.length && start + i < blocks.size(); i++) {
            Material material = blocks.get(start + i);
            inventory.setItem(BLOCK_SLOTS[i], createSimpleItem(material, "&e" + plugin.formatMaterialName(material), List.of(
                    "&7XP: &f" + plugin.getProfessionLevelBlockXp(profession, level, material),
                    "&eClick to edit"
            )));
        }
        inventory.setItem(ADD_BLOCK_SLOT, createSimpleItem(Material.LIME_CONCRETE, "&aAdd Block", List.of(
                "&7Click, then right-click with the block",
                "&7you want to add in your main hand.",
                "&7After that, type the XP in chat.",
                "&8Fallback: MATERIAL XP"
        )));
        inventory.setItem(BLOCK_BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the editor.")));
        inventory.setItem(BLOCK_PAGE_PREV_SLOT, createSimpleItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of("&7Go to the previous page.")));
        inventory.setItem(BLOCK_PAGE_NEXT_SLOT, createSimpleItem(Material.ARROW, "&eNext Page", List.of("&7Go to the next page.")));
        player.openInventory(inventory);
    }

    private void openBlockEditMenu(Player player, Profession profession, int level, Material material) {
        Inventory inventory = Bukkit.createInventory(new BlockEditHolder(player.getUniqueId(), profession, level, material), GUI_SIZE,
                plugin.legacyComponent("&8Edit Block XP"));
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createSimpleItem(material, "&e" + plugin.formatMaterialName(material), List.of(
                "&7Job: &f" + plugin.getProfessionPlainDisplayName(profession),
                "&7Level: &f" + level,
                "&7XP: &f" + plugin.getProfessionLevelBlockXp(profession, level, material)
        )));
        inventory.setItem(XP_MINUS_TEN_SLOT, createSimpleItem(Material.RED_STAINED_GLASS_PANE, "&cXP -10", List.of("&7Lower the XP by 10.")));
        inventory.setItem(XP_MINUS_ONE_SLOT, createSimpleItem(Material.ORANGE_STAINED_GLASS_PANE, "&cXP -1", List.of("&7Lower the XP by 1.")));
        inventory.setItem(XP_PLUS_ONE_SLOT, createSimpleItem(Material.LIME_STAINED_GLASS_PANE, "&aXP +1", List.of("&7Raise the XP by 1.")));
        inventory.setItem(XP_PLUS_TEN_SLOT, createSimpleItem(Material.GREEN_STAINED_GLASS_PANE, "&aXP +10", List.of("&7Raise the XP by 10.")));
        inventory.setItem(REMOVE_BLOCK_SLOT, createSimpleItem(Material.BARRIER, "&cRemove Block", List.of("&7Remove this block from the level unlock list.")));
        inventory.setItem(EDIT_BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the blocks list.")));
        player.openInventory(inventory);
    }

    private void openActionsMenu(Player player, Profession profession, int level) {
        Inventory inventory = Bukkit.createInventory(new ActionsHolder(player.getUniqueId(), profession, level), GUI_SIZE,
                plugin.legacyComponent("&8" + plugin.getProfessionPlainDisplayName(profession) + " Actions"));
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createSimpleItem(Material.BLAZE_POWDER, "&6Special Actions", List.of(
                "&7These are the action hooks already used",
                "&7by the live gameplay systems.",
                "&7Click one to edit its level or XP."
        )));
        List<ActionDefinition> actions = getActionsForProfession(profession);
        for (int i = 0; i < ACTION_SLOTS.length && i < actions.size(); i++) {
            ActionDefinition action = actions.get(i);
            inventory.setItem(ACTION_SLOTS[i], createSimpleItem(action.icon(), "&e" + action.displayName(), buildActionLore(profession, action)));
        }
        inventory.setItem(ACTION_BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the editor.")));
        player.openInventory(inventory);
    }

    private void openStarterKitMenu(Player player, Profession profession, int page) {
        List<Testproject.StarterKitEntry> entries = plugin.getProfessionStarterKitEntries(profession);
        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) STARTER_KIT_SLOTS.length));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));
        Inventory inventory = Bukkit.createInventory(new StarterKitHolder(player.getUniqueId(), profession, safePage), GUI_SIZE,
                plugin.legacyComponent("&8" + plugin.getProfessionPlainDisplayName(profession) + " Starter Kit"));
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createSimpleItem(Material.CHEST, "&bStarter Kit", List.of(
                "&7Current page: &f" + (safePage + 1) + "&7/&f" + totalPages,
                "&7Click an item to edit its amount.",
                "&7Use Add Item, then right-click with an item",
                "&7and type the amount in chat."
        )));
        int start = safePage * STARTER_KIT_SLOTS.length;
        for (int i = 0; i < STARTER_KIT_SLOTS.length && start + i < entries.size(); i++) {
            Testproject.StarterKitEntry entry = entries.get(start + i);
            inventory.setItem(STARTER_KIT_SLOTS[i], createSimpleItem(entry.material(), "&e" + plugin.formatMaterialName(entry.material()), List.of(
                    "&7Amount: &f" + entry.amount(),
                    "&eClick to edit"
            )));
        }
        inventory.setItem(ADD_STARTER_ITEM_SLOT, createSimpleItem(Material.LIME_CONCRETE, "&aAdd Item", List.of(
                "&7Click, then right-click with the item",
                "&7you want to add in your main hand.",
                "&7After that, type the amount in chat.",
                "&8Fallback: MATERIAL AMOUNT"
        )));
        inventory.setItem(STARTER_BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the editor.")));
        inventory.setItem(STARTER_PAGE_PREV_SLOT, createSimpleItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of("&7Go to the previous page.")));
        inventory.setItem(STARTER_PAGE_NEXT_SLOT, createSimpleItem(Material.ARROW, "&eNext Page", List.of("&7Go to the next page.")));
        player.openInventory(inventory);
    }

    private void openStarterKitEditMenu(Player player, Profession profession, int page, Material material) {
        int amount = plugin.getProfessionStarterKitEntries(profession).stream()
                .filter(entry -> entry.material() == material)
                .map(Testproject.StarterKitEntry::amount)
                .findFirst()
                .orElse(1);
        Inventory inventory = Bukkit.createInventory(new StarterKitEditHolder(player.getUniqueId(), profession, page, material), GUI_SIZE,
                plugin.legacyComponent("&8Edit Starter Item"));
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createSimpleItem(material, "&e" + plugin.formatMaterialName(material), List.of(
                "&7Job: &f" + plugin.getProfessionPlainDisplayName(profession),
                "&7Amount: &f" + amount
        )));
        inventory.setItem(AMOUNT_MINUS_SIXTEEN_SLOT, createSimpleItem(Material.RED_STAINED_GLASS_PANE, "&cAmount -16", List.of("&7Lower the amount by 16.")));
        inventory.setItem(AMOUNT_MINUS_ONE_SLOT, createSimpleItem(Material.ORANGE_STAINED_GLASS_PANE, "&cAmount -1", List.of("&7Lower the amount by 1.")));
        inventory.setItem(AMOUNT_PLUS_ONE_SLOT, createSimpleItem(Material.LIME_STAINED_GLASS_PANE, "&aAmount +1", List.of("&7Raise the amount by 1.")));
        inventory.setItem(AMOUNT_PLUS_SIXTEEN_SLOT, createSimpleItem(Material.GREEN_STAINED_GLASS_PANE, "&aAmount +16", List.of("&7Raise the amount by 16.")));
        inventory.setItem(REMOVE_STARTER_ITEM_SLOT, createSimpleItem(Material.BARRIER, "&cRemove Item", List.of("&7Remove this item from the starter kit.")));
        inventory.setItem(EDIT_BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the starter kit list.")));
        player.openInventory(inventory);
    }

    private void openActionEditMenu(Player player, Profession profession, int level, String actionKey) {
        ActionDefinition action = getActionDefinition(profession, actionKey);
        if (action == null) {
            openActionsMenu(player, profession, level);
            return;
        }
        Inventory inventory = Bukkit.createInventory(new ActionEditHolder(player.getUniqueId(), profession, level, action.key()), GUI_SIZE,
                plugin.legacyComponent("&8Edit Action"));
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createSimpleItem(action.icon(), "&6" + action.displayName(), buildActionLore(profession, action)));
        if (action.levelPath() != null) {
            inventory.setItem(ACTION_LEVEL_MINUS_SLOT, createSimpleItem(Material.RED_STAINED_GLASS_PANE, "&cLevel -1", List.of("&7Lower required level by 1.")));
            inventory.setItem(ACTION_LEVEL_PLUS_SLOT, createSimpleItem(Material.GREEN_STAINED_GLASS_PANE, "&aLevel +1", List.of("&7Raise required level by 1.")));
        }
        if (action.xpPath() != null) {
            inventory.setItem(ACTION_XP_MINUS_ONE_SLOT, createSimpleItem(Material.ORANGE_STAINED_GLASS_PANE, "&cXP -1", List.of("&7Lower XP by 1.")));
            inventory.setItem(ACTION_XP_PLUS_ONE_SLOT, createSimpleItem(Material.LIME_STAINED_GLASS_PANE, "&aXP +1", List.of("&7Raise XP by 1.")));
        }
        inventory.setItem(EDIT_BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the actions list.")));
        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof JobConfigHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        if (holder instanceof ProfessionSelectorHolder) {
            if (event.getSlot() == BACK_SLOT) {
                player.closeInventory();
                return;
            }
            Profession profession = getProfessionBySlot(event.getSlot());
            if (profession != null) {
                openEditorMenu(player, profession, 1);
            }
            return;
        }

        if (holder instanceof EditorHolder editorHolder) {
            switch (event.getSlot()) {
                case PREV_LEVEL_SLOT -> openEditorMenu(player, editorHolder.profession(), editorHolder.level() - 1);
                case NEXT_LEVEL_SLOT -> openEditorMenu(player, editorHolder.profession(), editorHolder.level() + 1);
                case BLOCKS_SLOT -> openBlocksMenu(player, editorHolder.profession(), editorHolder.level(), 0);
                case ACTIONS_SLOT -> openActionsMenu(player, editorHolder.profession(), editorHolder.level());
                case STARTER_KIT_SLOT -> openStarterKitMenu(player, editorHolder.profession(), 0);
                case BACK_SLOT -> openProfessionSelector(player);
                default -> {
                }
            }
            return;
        }

        if (holder instanceof BlocksHolder blocksHolder) {
            if (event.getSlot() == BLOCK_BACK_SLOT) {
                openEditorMenu(player, blocksHolder.profession(), blocksHolder.level());
                return;
            }
            if (event.getSlot() == ADD_BLOCK_SLOT) {
                pendingBlockEntries.put(player.getUniqueId(), new PendingBlockEntry(blocksHolder.profession(), blocksHolder.level(), blocksHolder.page(), null));
                player.closeInventory();
                player.sendMessage(plugin.legacyComponent("&eRight-click with the block in your main hand to select it, then type the XP in chat. &8Fallback: &fMATERIAL XP&8. Type &fcancel &eto stop."));
                return;
            }
            if (event.getSlot() == BLOCK_PAGE_PREV_SLOT) {
                openBlocksMenu(player, blocksHolder.profession(), blocksHolder.level(), blocksHolder.page() - 1);
                return;
            }
            if (event.getSlot() == BLOCK_PAGE_NEXT_SLOT) {
                openBlocksMenu(player, blocksHolder.profession(), blocksHolder.level(), blocksHolder.page() + 1);
                return;
            }
            Material material = getBlockBySlot(blocksHolder.profession(), blocksHolder.level(), blocksHolder.page(), event.getSlot());
            if (material != null) {
                openBlockEditMenu(player, blocksHolder.profession(), blocksHolder.level(), material);
            }
            return;
        }

        if (holder instanceof BlockEditHolder blockEditHolder) {
            int currentXp = plugin.getProfessionLevelBlockXp(blockEditHolder.profession(), blockEditHolder.level(), blockEditHolder.material());
            switch (event.getSlot()) {
                case XP_MINUS_TEN_SLOT -> plugin.setProfessionLevelBlockReward(blockEditHolder.profession(), blockEditHolder.level(), blockEditHolder.material(), Math.max(0, currentXp - 10));
                case XP_MINUS_ONE_SLOT -> plugin.setProfessionLevelBlockReward(blockEditHolder.profession(), blockEditHolder.level(), blockEditHolder.material(), Math.max(0, currentXp - 1));
                case XP_PLUS_ONE_SLOT -> plugin.setProfessionLevelBlockReward(blockEditHolder.profession(), blockEditHolder.level(), blockEditHolder.material(), currentXp + 1);
                case XP_PLUS_TEN_SLOT -> plugin.setProfessionLevelBlockReward(blockEditHolder.profession(), blockEditHolder.level(), blockEditHolder.material(), currentXp + 10);
                case REMOVE_BLOCK_SLOT -> {
                    plugin.removeProfessionLevelBlockReward(blockEditHolder.profession(), blockEditHolder.level(), blockEditHolder.material());
                    openBlocksMenu(player, blockEditHolder.profession(), blockEditHolder.level(), 0);
                    return;
                }
                case EDIT_BACK_SLOT -> {
                    openBlocksMenu(player, blockEditHolder.profession(), blockEditHolder.level(), 0);
                    return;
                }
                default -> {
                    return;
                }
            }
            openBlockEditMenu(player, blockEditHolder.profession(), blockEditHolder.level(), blockEditHolder.material());
            return;
        }

        if (holder instanceof StarterKitHolder starterKitHolder) {
            if (event.getSlot() == STARTER_BACK_SLOT) {
                openEditorMenu(player, starterKitHolder.profession(), 1);
                return;
            }
            if (event.getSlot() == ADD_STARTER_ITEM_SLOT) {
                pendingStarterKitEntries.put(player.getUniqueId(), new PendingStarterKitEntry(starterKitHolder.profession(), starterKitHolder.page(), null));
                player.closeInventory();
                player.sendMessage(plugin.legacyComponent("&eRight-click with the item in your main hand to select it, then type the amount in chat. &8Fallback: &fMATERIAL AMOUNT&8. Type &fcancel &eto stop."));
                return;
            }
            if (event.getSlot() == STARTER_PAGE_PREV_SLOT) {
                openStarterKitMenu(player, starterKitHolder.profession(), starterKitHolder.page() - 1);
                return;
            }
            if (event.getSlot() == STARTER_PAGE_NEXT_SLOT) {
                openStarterKitMenu(player, starterKitHolder.profession(), starterKitHolder.page() + 1);
                return;
            }
            Material material = getStarterItemBySlot(starterKitHolder.profession(), starterKitHolder.page(), event.getSlot());
            if (material != null) {
                openStarterKitEditMenu(player, starterKitHolder.profession(), starterKitHolder.page(), material);
            }
            return;
        }

        if (holder instanceof StarterKitEditHolder starterKitEditHolder) {
            int currentAmount = plugin.getProfessionStarterKitEntries(starterKitEditHolder.profession()).stream()
                    .filter(entry -> entry.material() == starterKitEditHolder.material())
                    .map(Testproject.StarterKitEntry::amount)
                    .findFirst()
                    .orElse(1);
            switch (event.getSlot()) {
                case AMOUNT_MINUS_SIXTEEN_SLOT -> plugin.setProfessionStarterKitEntry(starterKitEditHolder.profession(), starterKitEditHolder.material(), Math.max(1, currentAmount - 16));
                case AMOUNT_MINUS_ONE_SLOT -> plugin.setProfessionStarterKitEntry(starterKitEditHolder.profession(), starterKitEditHolder.material(), Math.max(1, currentAmount - 1));
                case AMOUNT_PLUS_ONE_SLOT -> plugin.setProfessionStarterKitEntry(starterKitEditHolder.profession(), starterKitEditHolder.material(), currentAmount + 1);
                case AMOUNT_PLUS_SIXTEEN_SLOT -> plugin.setProfessionStarterKitEntry(starterKitEditHolder.profession(), starterKitEditHolder.material(), currentAmount + 16);
                case REMOVE_STARTER_ITEM_SLOT -> {
                    plugin.removeProfessionStarterKitEntry(starterKitEditHolder.profession(), starterKitEditHolder.material());
                    openStarterKitMenu(player, starterKitEditHolder.profession(), starterKitEditHolder.page());
                    return;
                }
                case EDIT_BACK_SLOT -> {
                    openStarterKitMenu(player, starterKitEditHolder.profession(), starterKitEditHolder.page());
                    return;
                }
                default -> {
                    return;
                }
            }
            openStarterKitEditMenu(player, starterKitEditHolder.profession(), starterKitEditHolder.page(), starterKitEditHolder.material());
            return;
        }

        if (holder instanceof ActionsHolder actionsHolder) {
            if (event.getSlot() == ACTION_BACK_SLOT) {
                openEditorMenu(player, actionsHolder.profession(), actionsHolder.level());
                return;
            }
            ActionDefinition action = getActionBySlot(actionsHolder.profession(), event.getSlot());
            if (action != null) {
                openActionEditMenu(player, actionsHolder.profession(), actionsHolder.level(), action.key());
            }
            return;
        }

        if (holder instanceof ActionEditHolder actionEditHolder) {
            ActionDefinition action = getActionDefinition(actionEditHolder.profession(), actionEditHolder.actionKey());
            if (action == null) {
                openActionsMenu(player, actionEditHolder.profession(), actionEditHolder.level());
                return;
            }
            if (event.getSlot() == EDIT_BACK_SLOT) {
                openActionsMenu(player, actionEditHolder.profession(), actionEditHolder.level());
                return;
            }
            if (event.getSlot() == ACTION_LEVEL_MINUS_SLOT && action.levelPath() != null) {
                int current = plugin.getProfessionConfigIntValue(actionEditHolder.profession(), action.levelPath(), action.defaultLevel());
                plugin.setProfessionConfigIntValue(actionEditHolder.profession(), action.levelPath(), Math.max(1, current - 1));
            } else if (event.getSlot() == ACTION_LEVEL_PLUS_SLOT && action.levelPath() != null) {
                int current = plugin.getProfessionConfigIntValue(actionEditHolder.profession(), action.levelPath(), action.defaultLevel());
                plugin.setProfessionConfigIntValue(actionEditHolder.profession(), action.levelPath(), current + 1);
            } else if (event.getSlot() == ACTION_XP_MINUS_ONE_SLOT && action.xpPath() != null) {
                double current = plugin.getProfessionConfigDoubleValue(actionEditHolder.profession(), action.xpPath(), action.defaultXp());
                plugin.setProfessionConfigDoubleValue(actionEditHolder.profession(), action.xpPath(), Math.max(0.0D, current - 1.0D));
            } else if (event.getSlot() == ACTION_XP_PLUS_ONE_SLOT && action.xpPath() != null) {
                double current = plugin.getProfessionConfigDoubleValue(actionEditHolder.profession(), action.xpPath(), action.defaultXp());
                plugin.setProfessionConfigDoubleValue(actionEditHolder.profession(), action.xpPath(), current + 1.0D);
            } else {
                return;
            }
            openActionEditMenu(player, actionEditHolder.profession(), actionEditHolder.level(), action.key());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof JobConfigHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        PendingBlockEntry pending = pendingBlockEntries.get(event.getPlayer().getUniqueId());
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack heldItem = event.getPlayer().getInventory().getItemInMainHand();
        Material material = heldItem != null ? heldItem.getType() : null;
        if (pending != null) {
            if (material == null || material.isAir() || !material.isBlock()) {
                event.getPlayer().sendMessage(plugin.legacyComponent("&cHold the block you want to add in your main hand, then right-click."));
                return;
            }

            pendingBlockEntries.put(event.getPlayer().getUniqueId(), pending.withMaterial(material));
            event.getPlayer().sendMessage(plugin.legacyComponent("&aSelected &f" + plugin.formatMaterialName(material) + "&a. Now type the XP amount in chat, or &fcancel&a to stop."));
            return;
        }

        PendingStarterKitEntry starterPending = pendingStarterKitEntries.get(event.getPlayer().getUniqueId());
        if (starterPending == null) {
            return;
        }
        if (material == null || material.isAir()) {
            event.getPlayer().sendMessage(plugin.legacyComponent("&cHold the item you want to add in your main hand, then right-click."));
            return;
        }
        pendingStarterKitEntries.put(event.getPlayer().getUniqueId(), starterPending.withMaterial(material));
        event.getPlayer().sendMessage(plugin.legacyComponent("&aSelected &f" + plugin.formatMaterialName(material) + "&a. Now type the amount in chat, or &fcancel&a to stop."));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncChat(AsyncChatEvent event) {
        PendingBlockEntry pending = pendingBlockEntries.remove(event.getPlayer().getUniqueId());
        if (pending != null) {
            event.setCancelled(true);
            String input = plugin.plainText(event.originalMessage()).trim();
            if (input.equalsIgnoreCase("cancel")) {
                Bukkit.getScheduler().runTask(plugin, () -> openBlocksMenu(event.getPlayer(), pending.profession(), pending.level(), pending.page()));
                return;
            }

            String[] parts = input.split("\\s+");
            Material material = pending.material();
            int xp;
            if (material != null) {
                try {
                    xp = Integer.parseInt(parts[0]);
                } catch (NumberFormatException ignored) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        event.getPlayer().sendMessage(plugin.legacyComponent("&cType only a number for XP after selecting a block."));
                        pendingBlockEntries.put(event.getPlayer().getUniqueId(), pending);
                    });
                    return;
                }
            } else {
                material = Material.matchMaterial(parts[0]);
                xp = 1;
                if (parts.length >= 2) {
                    try {
                        xp = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException ignored) {
                        xp = 1;
                    }
                }
            }

            if (material == null || !material.isBlock() || material.isAir()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    event.getPlayer().sendMessage(plugin.legacyComponent("&cThat is not a valid block material."));
                    openBlocksMenu(event.getPlayer(), pending.profession(), pending.level(), pending.page());
                });
                return;
            }

            int finalXp = Math.max(0, xp);
            Material finalMaterial = material;
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.setProfessionLevelBlockReward(pending.profession(), pending.level(), finalMaterial, finalXp);
                event.getPlayer().sendMessage(plugin.legacyComponent("&aAdded &f" + plugin.formatMaterialName(finalMaterial) + "&a to &f" + plugin.getProfessionPlainDisplayName(pending.profession()) + " level " + pending.level() + "&a with &f" + finalXp + " xp&a."));
                openBlocksMenu(event.getPlayer(), pending.profession(), pending.level(), pending.page());
            });
            return;
        }

        PendingStarterKitEntry starterPending = pendingStarterKitEntries.remove(event.getPlayer().getUniqueId());
        if (starterPending == null) {
            return;
        }
        event.setCancelled(true);
        String input = plugin.plainText(event.originalMessage()).trim();
        if (input.equalsIgnoreCase("cancel")) {
            Bukkit.getScheduler().runTask(plugin, () -> openStarterKitMenu(event.getPlayer(), starterPending.profession(), starterPending.page()));
            return;
        }

        String[] parts = input.split("\\s+");
        Material material = starterPending.material();
        int amount;
        if (material != null) {
            try {
                amount = Integer.parseInt(parts[0]);
            } catch (NumberFormatException ignored) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    event.getPlayer().sendMessage(plugin.legacyComponent("&cType only a number for amount after selecting an item."));
                    pendingStarterKitEntries.put(event.getPlayer().getUniqueId(), starterPending);
                });
                return;
            }
        } else {
            material = Material.matchMaterial(parts[0]);
            amount = 1;
            if (parts.length >= 2) {
                try {
                    amount = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                    amount = 1;
                }
            }
        }

        if (material == null || material.isAir()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                event.getPlayer().sendMessage(plugin.legacyComponent("&cThat is not a valid item material."));
                openStarterKitMenu(event.getPlayer(), starterPending.profession(), starterPending.page());
            });
            return;
        }

        int finalAmount = Math.max(1, amount);
        Material finalMaterial = material;
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.setProfessionStarterKitEntry(starterPending.profession(), finalMaterial, finalAmount);
            event.getPlayer().sendMessage(plugin.legacyComponent("&aAdded &f" + plugin.formatMaterialName(finalMaterial) + "&a to the &f" + plugin.getProfessionPlainDisplayName(starterPending.profession()) + "&a starter kit with &f" + finalAmount + "&a."));
            openStarterKitMenu(event.getPlayer(), starterPending.profession(), starterPending.page());
        });
    }

    private List<String> buildActionLore(Profession profession, ActionDefinition action) {
        List<String> lore = new ArrayList<>();
        lore.add("&7" + action.description());
        if (action.levelPath() != null) {
            lore.add("&7Required level: &f" + plugin.getProfessionConfigIntValue(profession, action.levelPath(), action.defaultLevel()));
        }
        if (action.xpPath() != null) {
            lore.add("&7XP: &f" + formatNumber(plugin.getProfessionConfigDoubleValue(profession, action.xpPath(), action.defaultXp())));
        }
        lore.add("");
        lore.add("&eClick to edit");
        return lore;
    }

    private String formatNumber(double value) {
        return value == Math.rint(value) ? String.valueOf((int) value) : String.format(Locale.US, "%.1f", value);
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

    private Material getBlockBySlot(Profession profession, int level, int page, int slot) {
        List<Material> blocks = plugin.getProfessionLevelUnlockedBlocks(profession, level);
        int start = page * BLOCK_SLOTS.length;
        for (int i = 0; i < BLOCK_SLOTS.length && start + i < blocks.size(); i++) {
            if (BLOCK_SLOTS[i] == slot) {
                return blocks.get(start + i);
            }
        }
        return null;
    }

    private Material getStarterItemBySlot(Profession profession, int page, int slot) {
        List<Testproject.StarterKitEntry> entries = plugin.getProfessionStarterKitEntries(profession);
        int start = page * STARTER_KIT_SLOTS.length;
        for (int i = 0; i < STARTER_KIT_SLOTS.length && start + i < entries.size(); i++) {
            if (STARTER_KIT_SLOTS[i] == slot) {
                return entries.get(start + i).material();
            }
        }
        return null;
    }

    private ActionDefinition getActionBySlot(Profession profession, int slot) {
        List<ActionDefinition> actions = getActionsForProfession(profession);
        for (int i = 0; i < ACTION_SLOTS.length && i < actions.size(); i++) {
            if (ACTION_SLOTS[i] == slot) {
                return actions.get(i);
            }
        }
        return null;
    }

    private ActionDefinition getActionDefinition(Profession profession, String key) {
        for (ActionDefinition action : getActionsForProfession(profession)) {
            if (action.key().equalsIgnoreCase(key)) {
                return action;
            }
        }
        return null;
    }

    private List<ActionDefinition> getActionsForProfession(Profession profession) {
        List<ActionDefinition> actions = new ArrayList<>();
        if (profession == Profession.FARMER) {
            actions.add(new ActionDefinition("farmland_create", "Create Farmland", Material.FARMLAND, "rewards.farmland-create-required-level", 1, "rewards.farmland-create-xp", 3.0D, "XP for turning dirt into farmland."));
            actions.add(new ActionDefinition("coarse_convert", "Convert Coarse Dirt", Material.COARSE_DIRT, "rewards.coarse-dirt-convert-required-level", 1, "rewards.coarse-dirt-convert-xp", 2.0D, "XP for converting coarse dirt back into normal dirt."));
            actions.add(new ActionDefinition("plant", "Plant Crops", Material.WHEAT_SEEDS, "rewards.plant-required-level", 1, "rewards.plant-xp", 1.5D, "XP for planting farmer crops."));
            actions.add(new ActionDefinition("bonemeal", "Use Bonemeal", Material.BONE_MEAL, "rewards.bonemeal-required-level", 1, "rewards.bonemeal-xp", 1.0D, "XP for bonemealing valid farmer crops."));
            actions.add(new ActionDefinition("water_bucket", "Use Water Bucket", Material.WATER_BUCKET, "rewards.water-bucket-required-level", 3, null, 0.0D, "Required level to place water with the farmer job."));
            actions.add(new ActionDefinition("smoker", "Use Smoker", Material.SMOKER, "rewards.smoker-required-level", 5, null, 0.0D, "Required level to use smokers as a farmer."));
        }
        if (profession == Profession.BUILDER) {
            actions.add(new ActionDefinition("place_block", "Place Builder Blocks", Material.BRICKS, "rewards.place-required-level", 1, "rewards.place-xp", 5.0D, "Fallback XP for placing builder blocks without a specific block XP value."));
        }
        actions.sort(Comparator.comparing(ActionDefinition::displayName, String.CASE_INSENSITIVE_ORDER));
        return actions;
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack filler = createSimpleItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
    }

    private ItemStack createSimpleItem(Material material, String displayName, List<String> loreLines) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return itemStack;
        }
        itemMeta.displayName(plugin.legacyComponent(displayName));
        itemMeta.lore(loreLines.stream().map(plugin::legacyComponent).toList());
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private interface JobConfigHolder extends InventoryHolder {
    }

    private record ProfessionSelectorHolder(UUID viewerId) implements JobConfigHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record EditorHolder(UUID viewerId, Profession profession, int level) implements JobConfigHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record BlocksHolder(UUID viewerId, Profession profession, int level, int page) implements JobConfigHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record BlockEditHolder(UUID viewerId, Profession profession, int level, Material material) implements JobConfigHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record ActionsHolder(UUID viewerId, Profession profession, int level) implements JobConfigHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record StarterKitHolder(UUID viewerId, Profession profession, int page) implements JobConfigHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record ActionEditHolder(UUID viewerId, Profession profession, int level, String actionKey) implements JobConfigHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record StarterKitEditHolder(UUID viewerId, Profession profession, int page, Material material) implements JobConfigHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record PendingBlockEntry(Profession profession, int level, int page, Material material) {
        private PendingBlockEntry withMaterial(Material nextMaterial) {
            return new PendingBlockEntry(profession, level, page, nextMaterial);
        }
    }

    private record PendingStarterKitEntry(Profession profession, int page, Material material) {
        private PendingStarterKitEntry withMaterial(Material nextMaterial) {
            return new PendingStarterKitEntry(profession, page, nextMaterial);
        }
    }

    private record ActionDefinition(String key, String displayName, Material icon, String levelPath, int defaultLevel,
                                    String xpPath, double defaultXp, String description) {
    }
}
