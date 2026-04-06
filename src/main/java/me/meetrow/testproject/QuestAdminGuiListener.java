package me.meetrow.testproject;

import io.papermc.paper.event.player.AsyncChatEvent;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class QuestAdminGuiListener implements Listener {
    private static final int GUI_SIZE = 54;
    private static final int INFO_SLOT = 4;
    private static final int[] LIST_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int LIST_PREVIOUS_SLOT = 45;
    private static final int LIST_ADD_SLOT = 49;
    private static final int LIST_NEXT_SLOT = 53;

    private static final int EDIT_TYPE_SLOT = 19;
    private static final int EDIT_PROFESSION_SLOT = 20;
    private static final int EDIT_ENABLED_SLOT = 21;
    private static final int EDIT_TARGET_MINUS_TEN_SLOT = 28;
    private static final int EDIT_TARGET_MINUS_ONE_SLOT = 29;
    private static final int EDIT_TARGET_INFO_SLOT = 31;
    private static final int EDIT_TARGET_PLUS_ONE_SLOT = 33;
    private static final int EDIT_TARGET_PLUS_TEN_SLOT = 34;
    private static final int EDIT_TITLE_SLOT = 23;
    private static final int EDIT_OBJECTIVE_SLOT = 24;
    private static final int EDIT_HINT_SLOT = 25;
    private static final int EDIT_ASSIGN_SLOT = 40;
    private static final int EDIT_DELETE_SLOT = 49;
    private static final int EDIT_BACK_SLOT = 53;

    private static final int ASSIGN_PREVIOUS_SLOT = 45;
    private static final int ASSIGN_BACK_SLOT = 49;
    private static final int ASSIGN_NEXT_SLOT = 53;

    private final Testproject plugin;
    private final Map<UUID, PendingTextEdit> pendingTextEdits = new ConcurrentHashMap<>();

    public QuestAdminGuiListener(Testproject plugin) {
        this.plugin = plugin;
    }

    public void openQuestListMenu(Player player, int page) {
        List<PlayerQuestDefinition> quests = plugin.getGeneralQuestDefinitions();
        int totalPages = Math.max(1, (int) Math.ceil(quests.size() / (double) LIST_SLOTS.length));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inventory = Bukkit.createInventory(new QuestListHolder(player.getUniqueId(), safePage), GUI_SIZE, plugin.legacyComponent("&8Quest Admin"));
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createItem(Material.WRITABLE_BOOK, "&6General Quests", List.of(
                "&7Page: &f" + (safePage + 1) + "&7/&f" + totalPages,
                "&7Click a quest to edit it.",
                "&7Use Add Quest to create a new one."
        )));

        int start = safePage * LIST_SLOTS.length;
        for (int i = 0; i < LIST_SLOTS.length && start + i < quests.size(); i++) {
            PlayerQuestDefinition quest = quests.get(start + i);
            inventory.setItem(LIST_SLOTS[i], createQuestSummaryItem(quest));
        }

        inventory.setItem(LIST_PREVIOUS_SLOT, createItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of("&7Open the previous page.")));
        inventory.setItem(LIST_ADD_SLOT, createItem(Material.LIME_CONCRETE, "&aAdd Quest", List.of("&7Create a new general quest.")));
        inventory.setItem(LIST_NEXT_SLOT, createItem(Material.ARROW, "&eNext Page", List.of("&7Open the next page.")));
        player.openInventory(inventory);
    }

    private void openQuestEditMenu(Player player, String questId) {
        PlayerQuestDefinition quest = plugin.getGeneralQuestDefinition(questId);
        if (quest == null) {
            openQuestListMenu(player, 0);
            return;
        }

        Inventory inventory = Bukkit.createInventory(new QuestEditHolder(player.getUniqueId(), quest.getId()), GUI_SIZE,
                plugin.legacyComponent("&8Edit Quest: " + quest.getId()));
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createItem(Material.NAME_TAG, "&6" + quest.getId(), List.of(
                "&7Order: &f" + quest.getOrder(),
                "&7Assigned players can complete this quest.",
                "&7Title/objective/hint use chat prompts."
        )));
        inventory.setItem(EDIT_TYPE_SLOT, createItem(Material.COMPARATOR, "&bType", List.of(
                "&7Current: &f" + formatType(quest.getType()),
                "&eClick to cycle"
        )));
        inventory.setItem(EDIT_PROFESSION_SLOT, createItem(Material.IRON_PICKAXE, "&bProfession", List.of(
                "&7Current: &f" + formatProfession(quest.getProfession()),
                "&eClick to cycle"
        )));
        inventory.setItem(EDIT_ENABLED_SLOT, createItem(quest.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE, "&bEnabled", List.of(
                "&7Current: &f" + quest.isEnabled(),
                "&eClick to toggle"
        )));
        inventory.setItem(EDIT_TARGET_MINUS_TEN_SLOT, createItem(Material.RED_STAINED_GLASS_PANE, "&cTarget -10", List.of("&7Lower target by 10.")));
        inventory.setItem(EDIT_TARGET_MINUS_ONE_SLOT, createItem(Material.ORANGE_STAINED_GLASS_PANE, "&cTarget -1", List.of("&7Lower target by 1.")));
        inventory.setItem(EDIT_TARGET_INFO_SLOT, createItem(Material.EXPERIENCE_BOTTLE, "&eTarget: " + quest.getTarget(), List.of(
                "&7Used by XP, level, and contribution quests."
        )));
        inventory.setItem(EDIT_TARGET_PLUS_ONE_SLOT, createItem(Material.LIME_STAINED_GLASS_PANE, "&aTarget +1", List.of("&7Raise target by 1.")));
        inventory.setItem(EDIT_TARGET_PLUS_TEN_SLOT, createItem(Material.GREEN_STAINED_GLASS_PANE, "&aTarget +10", List.of("&7Raise target by 10.")));
        inventory.setItem(EDIT_TITLE_SLOT, createItem(Material.OAK_SIGN, "&aEdit Title", List.of(
                "&7Current: &f" + stripColors(quest.getTitle()),
                "&eClick and type in chat"
        )));
        inventory.setItem(EDIT_OBJECTIVE_SLOT, createItem(Material.BOOK, "&aEdit Objective", List.of(
                "&7Current: &f" + stripColors(quest.getObjective()),
                "&eClick and type in chat"
        )));
        inventory.setItem(EDIT_HINT_SLOT, createItem(Material.PAPER, "&aEdit Hint", List.of(
                "&7Current: &f" + stripColors(quest.getHint()),
                "&eClick and type in chat"
        )));
        inventory.setItem(EDIT_ASSIGN_SLOT, createItem(Material.PLAYER_HEAD, "&bAssign To Players", List.of(
                "&7Open the player assignment menu."
        )));
        inventory.setItem(EDIT_DELETE_SLOT, createItem(Material.BARRIER, "&cDelete Quest", List.of(
                "&7Remove this quest from config.",
                "&cThis also removes assignments."
        )));
        inventory.setItem(EDIT_BACK_SLOT, createItem(Material.ARROW, "&eBack", List.of("&7Return to the quest list.")));
        player.openInventory(inventory);
    }

    private void openQuestAssignMenu(Player player, String questId, int page) {
        PlayerQuestDefinition quest = plugin.getGeneralQuestDefinition(questId);
        if (quest == null) {
            openQuestListMenu(player, 0);
            return;
        }

        List<Player> players = plugin.getQuestAssignablePlayers();
        int totalPages = Math.max(1, (int) Math.ceil(players.size() / (double) LIST_SLOTS.length));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inventory = Bukkit.createInventory(new QuestAssignHolder(player.getUniqueId(), quest.getId(), safePage), GUI_SIZE,
                plugin.legacyComponent("&8Assign: " + quest.getId()));
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createItem(Material.PLAYER_HEAD, "&6Assign " + quest.getId(), List.of(
                "&7Click a player to assign or unassign.",
                "&7Assigned quests take priority over starter quests."
        )));

        int start = safePage * LIST_SLOTS.length;
        for (int i = 0; i < LIST_SLOTS.length && start + i < players.size(); i++) {
            Player target = players.get(start + i);
            inventory.setItem(LIST_SLOTS[i], createPlayerAssignmentItem(target, quest.getId()));
        }

        inventory.setItem(ASSIGN_PREVIOUS_SLOT, createItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of("&7Open the previous page.")));
        inventory.setItem(ASSIGN_BACK_SLOT, createItem(Material.ARROW, "&eBack", List.of("&7Return to the quest editor.")));
        inventory.setItem(ASSIGN_NEXT_SLOT, createItem(Material.ARROW, "&eNext Page", List.of("&7Open the next page.")));
        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof QuestListHolder) && !(holder instanceof QuestEditHolder) && !(holder instanceof QuestAssignHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
            return;
        }

        if (holder instanceof QuestListHolder listHolder) {
            handleQuestListClick(player, listHolder, event.getSlot());
            return;
        }
        if (holder instanceof QuestEditHolder editHolder) {
            handleQuestEditClick(player, editHolder, event.getSlot());
            return;
        }
        handleQuestAssignClick(player, (QuestAssignHolder) holder, event.getSlot());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof QuestListHolder
                || event.getView().getTopInventory().getHolder() instanceof QuestEditHolder
                || event.getView().getTopInventory().getHolder() instanceof QuestAssignHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        PendingTextEdit pending = pendingTextEdits.remove(event.getPlayer().getUniqueId());
        if (pending == null) {
            return;
        }

        event.setCancelled(true);
        String message = plugin.plainText(event.message()).trim();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (message.equalsIgnoreCase("cancel")) {
                event.getPlayer().sendMessage(plugin.legacyComponent("&cQuest edit cancelled."));
                openQuestEditMenu(event.getPlayer(), pending.questId());
                return;
            }

            boolean success = switch (pending.field()) {
                case TITLE -> plugin.setGeneralQuestTitle(pending.questId(), message);
                case OBJECTIVE -> plugin.setGeneralQuestObjective(pending.questId(), message);
                case HINT -> plugin.setGeneralQuestHint(pending.questId(), message);
            };
            if (!success) {
                event.getPlayer().sendMessage(plugin.legacyComponent("&cFailed to save quest text."));
            } else {
                event.getPlayer().sendMessage(plugin.legacyComponent("&aUpdated quest " + pending.field().name().toLowerCase(Locale.ROOT) + "."));
            }
            openQuestEditMenu(event.getPlayer(), pending.questId());
        });
    }

    private void handleQuestListClick(Player player, QuestListHolder holder, int slot) {
        if (slot == LIST_PREVIOUS_SLOT) {
            openQuestListMenu(player, holder.page() - 1);
            return;
        }
        if (slot == LIST_NEXT_SLOT) {
            openQuestListMenu(player, holder.page() + 1);
            return;
        }
        if (slot == LIST_ADD_SLOT) {
            String questId = plugin.createGeneralQuestDefinition();
            if (questId == null) {
                player.sendMessage(plugin.legacyComponent("&cFailed to create quest."));
                return;
            }
            player.sendMessage(plugin.legacyComponent("&aCreated quest &f" + questId + "&a."));
            openQuestEditMenu(player, questId);
            return;
        }

        int indexInPage = indexOfSlot(slot);
        if (indexInPage < 0) {
            return;
        }
        List<PlayerQuestDefinition> quests = plugin.getGeneralQuestDefinitions();
        int index = holder.page() * LIST_SLOTS.length + indexInPage;
        if (index >= 0 && index < quests.size()) {
            openQuestEditMenu(player, quests.get(index).getId());
        }
    }

    private void handleQuestEditClick(Player player, QuestEditHolder holder, int slot) {
        PlayerQuestDefinition quest = plugin.getGeneralQuestDefinition(holder.questId());
        if (quest == null) {
            openQuestListMenu(player, 0);
            return;
        }

        switch (slot) {
            case EDIT_TYPE_SLOT -> {
                plugin.setGeneralQuestType(quest.getId(), nextType(quest.getType()));
                openQuestEditMenu(player, quest.getId());
            }
            case EDIT_PROFESSION_SLOT -> {
                plugin.setGeneralQuestProfession(quest.getId(), nextProfession(quest.getProfession()));
                openQuestEditMenu(player, quest.getId());
            }
            case EDIT_ENABLED_SLOT -> {
                plugin.setGeneralQuestEnabled(quest.getId(), !quest.isEnabled());
                openQuestEditMenu(player, quest.getId());
            }
            case EDIT_TARGET_MINUS_TEN_SLOT -> {
                plugin.setGeneralQuestTarget(quest.getId(), quest.getTarget() - 10);
                openQuestEditMenu(player, quest.getId());
            }
            case EDIT_TARGET_MINUS_ONE_SLOT -> {
                plugin.setGeneralQuestTarget(quest.getId(), quest.getTarget() - 1);
                openQuestEditMenu(player, quest.getId());
            }
            case EDIT_TARGET_PLUS_ONE_SLOT -> {
                plugin.setGeneralQuestTarget(quest.getId(), quest.getTarget() + 1);
                openQuestEditMenu(player, quest.getId());
            }
            case EDIT_TARGET_PLUS_TEN_SLOT -> {
                plugin.setGeneralQuestTarget(quest.getId(), quest.getTarget() + 10);
                openQuestEditMenu(player, quest.getId());
            }
            case EDIT_TITLE_SLOT -> beginTextEdit(player, quest.getId(), TextField.TITLE, "Type the new quest title in chat. Type cancel to abort.");
            case EDIT_OBJECTIVE_SLOT -> beginTextEdit(player, quest.getId(), TextField.OBJECTIVE, "Type the new quest objective in chat. Type cancel to abort.");
            case EDIT_HINT_SLOT -> beginTextEdit(player, quest.getId(), TextField.HINT, "Type the new quest hint in chat. Type cancel to abort.");
            case EDIT_ASSIGN_SLOT -> openQuestAssignMenu(player, quest.getId(), 0);
            case EDIT_DELETE_SLOT -> {
                plugin.deleteGeneralQuestDefinition(quest.getId());
                player.sendMessage(plugin.legacyComponent("&cDeleted quest &f" + quest.getId() + "&c."));
                openQuestListMenu(player, 0);
            }
            case EDIT_BACK_SLOT -> openQuestListMenu(player, 0);
            default -> {
            }
        }
    }

    private void handleQuestAssignClick(Player player, QuestAssignHolder holder, int slot) {
        if (slot == ASSIGN_PREVIOUS_SLOT) {
            openQuestAssignMenu(player, holder.questId(), holder.page() - 1);
            return;
        }
        if (slot == ASSIGN_NEXT_SLOT) {
            openQuestAssignMenu(player, holder.questId(), holder.page() + 1);
            return;
        }
        if (slot == ASSIGN_BACK_SLOT) {
            openQuestEditMenu(player, holder.questId());
            return;
        }

        int indexInPage = indexOfSlot(slot);
        if (indexInPage < 0) {
            return;
        }
        List<Player> players = plugin.getQuestAssignablePlayers();
        int index = holder.page() * LIST_SLOTS.length + indexInPage;
        if (index < 0 || index >= players.size()) {
            return;
        }
        Player target = players.get(index);
        boolean assigned = plugin.isQuestAssigned(target.getUniqueId(), holder.questId());
        boolean changed = assigned
                ? plugin.unassignQuestFromPlayer(target.getUniqueId(), holder.questId())
                : plugin.assignQuestToPlayer(target.getUniqueId(), holder.questId());
        if (changed) {
            player.sendMessage(plugin.legacyComponent((assigned ? "&cUnassigned &f" : "&aAssigned &f") + holder.questId() + "&7 " + (assigned ? "from " : "to ") + "&f" + target.getName()));
        }
        openQuestAssignMenu(player, holder.questId(), holder.page());
    }

    private void beginTextEdit(Player player, String questId, TextField field, String prompt) {
        pendingTextEdits.put(player.getUniqueId(), new PendingTextEdit(questId, field));
        player.closeInventory();
        player.sendMessage(plugin.legacyComponent("&e" + prompt));
    }

    private PlayerQuestType nextType(PlayerQuestType current) {
        PlayerQuestType[] values = PlayerQuestType.values();
        int index = current != null ? current.ordinal() : -1;
        return values[(index + 1 + values.length) % values.length];
    }

    private Profession nextProfession(Profession current) {
        List<Profession> professions = new ArrayList<>();
        professions.add(null);
        professions.addAll(plugin.getConfiguredProfessions());
        int index = professions.indexOf(current);
        if (index < 0) {
            index = 0;
        }
        return professions.get((index + 1) % professions.size());
    }

    private ItemStack createQuestSummaryItem(PlayerQuestDefinition quest) {
        Material icon = switch (quest.getType()) {
            case SELECT_PROFESSION -> Material.COMPASS;
            case EARN_PROFESSION_XP -> Material.EXPERIENCE_BOTTLE;
            case REACH_PROFESSION_LEVEL -> Material.EMERALD;
            case JOIN_COUNTRY -> Material.BLUE_BANNER;
            case CONTRIBUTE_COUNTRY -> Material.GOLD_INGOT;
        };
        return createItem(icon, (quest.isEnabled() ? "&a" : "&7") + quest.getId(), List.of(
                "&7Type: &f" + formatType(quest.getType()),
                "&7Profession: &f" + formatProfession(quest.getProfession()),
                "&7Target: &f" + quest.getTarget(),
                "&7Title: &f" + stripColors(quest.getTitle()),
                "&eClick to edit"
        ));
    }

    private ItemStack createPlayerAssignmentItem(OfflinePlayer target, String questId) {
        boolean assigned = plugin.isQuestAssigned(target.getUniqueId(), questId);
        List<String> lore = new ArrayList<>();
        lore.add("&7Assigned: &f" + assigned);
        lore.add("&7Current quests: &f" + plugin.getAssignedQuestIds(target.getUniqueId()).size());
        lore.add("");
        lore.add(assigned ? "&cClick to unassign" : "&aClick to assign");
        return createItem(assigned ? Material.LIME_DYE : Material.GRAY_DYE, (assigned ? "&a" : "&7") + plugin.safeOfflineName(target), lore);
    }

    private int indexOfSlot(int slot) {
        for (int i = 0; i < LIST_SLOTS.length; i++) {
            if (LIST_SLOTS[i] == slot) {
                return i;
            }
        }
        return -1;
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.legacyComponent("&7"));
            filler.setItemMeta(meta);
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
    }

    private ItemStack createItem(Material material, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.legacyComponent(name));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(plugin.legacyComponent(line));
            }
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatType(PlayerQuestType type) {
        if (type == null) {
            return "None";
        }
        return switch (type) {
            case SELECT_PROFESSION -> "Select Profession";
            case EARN_PROFESSION_XP -> "Earn Profession XP";
            case REACH_PROFESSION_LEVEL -> "Reach Profession Level";
            case JOIN_COUNTRY -> "Join Country";
            case CONTRIBUTE_COUNTRY -> "Contribute Country";
        };
    }

    private String formatProfession(Profession profession) {
        return profession != null ? plugin.getProfessionPlainDisplayName(profession) : "Any";
    }

    private String stripColors(String value) {
        return value == null ? "" : org.bukkit.ChatColor.stripColor(plugin.colorize(value));
    }

    private record QuestListHolder(UUID viewerId, int page) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record QuestEditHolder(UUID viewerId, String questId) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record QuestAssignHolder(UUID viewerId, String questId, int page) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private enum TextField {
        TITLE,
        OBJECTIVE,
        HINT
    }

    private record PendingTextEdit(String questId, TextField field) {
    }
}
