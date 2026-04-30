package me.meetrow.testproject;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class QuestAdminGuiListener implements Listener {
    private static final int SCOPE_GUI_SIZE = 27;
    private static final int GUI_SIZE = 54;
    private static final int INFO_SLOT = 4;
    private static final int[] LIST_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int SCOPE_ONBOARDING_SLOT = 11;
    private static final int SCOPE_GENERAL_SLOT = 15;
    private static final int LIST_PREVIOUS_SLOT = 45;
    private static final int LIST_ADD_SLOT = 49;
    private static final int LIST_NEXT_SLOT = 53;
    private static final int LIST_SWITCH_SCOPE_SLOT = 4;

    private static final int EDIT_TYPE_SLOT = 19;
    private static final int EDIT_PROFESSION_SLOT = 20;
    private static final int EDIT_ENABLED_SLOT = 21;
    private static final int EDIT_LINK_PREVIOUS_SLOT = 22;
    private static final int EDIT_KEY_SLOT = 23;
    private static final int EDIT_TITLE_SLOT = 24;
    private static final int EDIT_OBJECTIVE_SLOT = 25;
    private static final int EDIT_ORDER_MINUS_TEN_SLOT = 28;
    private static final int EDIT_ORDER_MINUS_ONE_SLOT = 29;
    private static final int EDIT_ORDER_INFO_SLOT = 31;
    private static final int EDIT_ORDER_PLUS_ONE_SLOT = 33;
    private static final int EDIT_ORDER_PLUS_TEN_SLOT = 34;
    private static final int EDIT_TARGET_MINUS_TEN_SLOT = 37;
    private static final int EDIT_TARGET_MINUS_ONE_SLOT = 38;
    private static final int EDIT_HINT_SLOT = 39;
    private static final int EDIT_TARGET_INFO_SLOT = 40;
    private static final int EDIT_ASSIGN_SLOT = 41;
    private static final int EDIT_TARGET_PLUS_ONE_SLOT = 42;
    private static final int EDIT_TARGET_PLUS_TEN_SLOT = 43;
    private static final int EDIT_REWARD_MONEY_SLOT = 47;
    private static final int EDIT_REWARD_XP_SLOT = 48;
    private static final int EDIT_DELETE_SLOT = 49;
    private static final int EDIT_REWARD_PROFESSION_SLOT = 50;
    private static final int EDIT_REWARD_ITEM_MATERIAL_SLOT = 51;
    private static final int EDIT_REWARD_ITEM_AMOUNT_SLOT = 52;
    private static final int EDIT_BACK_SLOT = 53;
    private static final int EDIT_REQUIRED_ITEM_SLOT = 46;

    private static final int ASSIGN_PREVIOUS_SLOT = 45;
    private static final int ASSIGN_BACK_SLOT = 49;
    private static final int ASSIGN_NEXT_SLOT = 53;

    private static final int KEY_SELECTOR_PREVIOUS_SLOT = 45;
    private static final int KEY_SELECTOR_BACK_SLOT = 49;
    private static final int KEY_SELECTOR_NEXT_SLOT = 53;

    private static final String CLEAR_KEY_OPTION = "__clear__";

    private final Testproject plugin;
    private final Map<UUID, PendingTextEdit> pendingTextEdits = new ConcurrentHashMap<>();

    public QuestAdminGuiListener(Testproject plugin) {
        this.plugin = plugin;
    }

    public void openScopeMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(new ScopeHolder(player.getUniqueId()), SCOPE_GUI_SIZE, plugin.legacyComponent("&8Quest Admin"));
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createItem(Material.WRITABLE_BOOK, "&6Quest Admin", List.of(
                "&7Choose what you want to edit.",
                "&7Onboarding controls the first-hour",
                "&7tutorial sequence and step order."
        )));
        inventory.setItem(SCOPE_ONBOARDING_SLOT, createItem(Material.COMPASS, "&eOnboarding Sequence", List.of(
                "&7Edit the starter tutorial flow,",
                "&7markers, NPC steps, task keys,",
                "&7rewards, and sequence order."
        )));
        inventory.setItem(SCOPE_GENERAL_SLOT, createItem(Material.BOOK, "&bGeneral Quests", List.of(
                "&7Edit assignable quests for staff",
                "&7and later player objectives."
        )));
        player.openInventory(inventory);
    }

    public void openQuestEditor(Player player, QuestScope scope, String questId) {
        openQuestEditMenu(player, scope, questId);
    }

    public void openQuestListMenu(Player player, QuestScope scope, int page) {
        List<PlayerQuestDefinition> quests = getDefinitions(scope);
        int totalPages = Math.max(1, (int) Math.ceil(quests.size() / (double) LIST_SLOTS.length));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inventory = Bukkit.createInventory(new QuestListHolder(player.getUniqueId(), scope, safePage), GUI_SIZE,
                plugin.legacyComponent(scope == QuestScope.ONBOARDING ? "&8Onboarding Editor" : "&8General Quests"));
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createItem(scope.icon(), scope.title(), List.of(
                "&7Page: &f" + (safePage + 1) + "&7/&f" + totalPages,
                scope == QuestScope.ONBOARDING
                        ? "&7Edit flow order, keys, markers, and rewards."
                        : "&7Click a quest to edit it.",
                scope == QuestScope.ONBOARDING
                        ? "&7Use Add Step to create a new tutorial entry."
                        : "&7Use Add Quest to create a new one."
        )));

        int start = safePage * LIST_SLOTS.length;
        for (int i = 0; i < LIST_SLOTS.length && start + i < quests.size(); i++) {
            inventory.setItem(LIST_SLOTS[i], createQuestSummaryItem(scope, quests.get(start + i)));
        }

        inventory.setItem(LIST_SWITCH_SCOPE_SLOT, createItem(Material.RECOVERY_COMPASS, "&eSwitch Scope", List.of(
                "&7Return to scope selection."
        )));
        inventory.setItem(LIST_PREVIOUS_SLOT, createItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of("&7Open the previous page.")));
        inventory.setItem(LIST_ADD_SLOT, createItem(Material.LIME_CONCRETE, "&aAdd " + (scope == QuestScope.ONBOARDING ? "Step" : "Quest"), List.of(
                scope == QuestScope.ONBOARDING ? "&7Create a new onboarding step." : "&7Create a new general quest."
        )));
        inventory.setItem(LIST_NEXT_SLOT, createItem(Material.ARROW, "&eNext Page", List.of("&7Open the next page.")));
        player.openInventory(inventory);
    }

    private void openQuestEditMenu(Player player, QuestScope scope, String questId) {
        PlayerQuestDefinition quest = getDefinition(scope, questId);
        if (quest == null) {
            openQuestListMenu(player, scope, 0);
            return;
        }

        Inventory inventory = Bukkit.createInventory(new QuestEditHolder(player.getUniqueId(), scope, quest.getId()), GUI_SIZE,
                plugin.legacyComponent("&8Edit " + (scope == QuestScope.ONBOARDING ? "Step: " : "Quest: ") + quest.getId()));
        fillEmptySlots(inventory);

        List<String> dependencyLore = new ArrayList<>();
        PlayerQuestDefinition previous = getPreviousQuest(scope, quest.getId());
        dependencyLore.add("&7Current: &f" + formatDependencySummary(quest));
        if (previous != null) {
            boolean linkedToPrevious = quest.getRequiresCompleted().contains(previous.getId());
            dependencyLore.add(linkedToPrevious ? "&aLinked to previous step." : "&7Not linked to previous step.");
            dependencyLore.add("&7Previous: &f" + previous.getId());
            dependencyLore.add("&eClick to toggle previous-step requirement.");
        } else {
            dependencyLore.add("&7This is the first step in order.");
            dependencyLore.add("&7No previous-step dependency available.");
        }

        inventory.setItem(INFO_SLOT, createInfoItem(quest));
        inventory.setItem(EDIT_TYPE_SLOT, createItem(Material.COMPARATOR, "&bType", List.of(
                "&7Current: &f" + formatType(quest.getType()),
                "&eLeft click: next",
                "&eRight click: previous"
        )));
        inventory.setItem(EDIT_PROFESSION_SLOT, createItem(Material.IRON_PICKAXE, "&bProfession", List.of(
                "&7Current: &f" + formatProfession(quest.getProfession()),
                "&eLeft click: next",
                "&eRight click: previous"
        )));
        inventory.setItem(EDIT_ENABLED_SLOT, createItem(quest.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE, "&bEnabled", List.of(
                "&7Current: &f" + quest.isEnabled(),
                "&eClick to toggle"
        )));
        inventory.setItem(EDIT_LINK_PREVIOUS_SLOT, createItem(Material.CHAIN, "&bSequence Link", dependencyLore));
        inventory.setItem(EDIT_KEY_SLOT, createQuestKeyItem(quest));
        inventory.setItem(EDIT_TITLE_SLOT, createItem(Material.OAK_SIGN, "&aEdit Title", List.of(
                "&7Current: &f" + stripColors(quest.getTitle()),
                "&eClick and type in chat"
        )));
        inventory.setItem(EDIT_OBJECTIVE_SLOT, createItem(Material.BOOK, "&aEdit Objective", List.of(
                "&7Current: &f" + stripColors(quest.getObjective()),
                "&eClick and type in chat"
        )));
        inventory.setItem(EDIT_ORDER_MINUS_TEN_SLOT, createItem(Material.RED_STAINED_GLASS_PANE, "&cOrder -10", List.of("&7Move earlier by 10.")));
        inventory.setItem(EDIT_ORDER_MINUS_ONE_SLOT, createItem(Material.ORANGE_STAINED_GLASS_PANE, "&cOrder -1", List.of("&7Move earlier by 1.")));
        inventory.setItem(EDIT_ORDER_INFO_SLOT, createItem(Material.REPEATER, "&eOrder: " + quest.getOrder(), List.of(
                "&7Controls list order and onboarding flow order."
        )));
        inventory.setItem(EDIT_ORDER_PLUS_ONE_SLOT, createItem(Material.LIME_STAINED_GLASS_PANE, "&aOrder +1", List.of("&7Move later by 1.")));
        inventory.setItem(EDIT_ORDER_PLUS_TEN_SLOT, createItem(Material.GREEN_STAINED_GLASS_PANE, "&aOrder +10", List.of("&7Move later by 10.")));
        inventory.setItem(EDIT_TARGET_MINUS_TEN_SLOT, createItem(Material.RED_STAINED_GLASS_PANE, "&cTarget -10", List.of("&7Lower target by 10.")));
        inventory.setItem(EDIT_TARGET_MINUS_ONE_SLOT, createItem(Material.ORANGE_STAINED_GLASS_PANE, "&cTarget -1", List.of("&7Lower target by 1.")));
        inventory.setItem(EDIT_TARGET_INFO_SLOT, createItem(Material.EXPERIENCE_BOTTLE, "&eTarget: " + quest.getTarget(), List.of(
                "&7Progress needed to complete this step."
        )));
        inventory.setItem(EDIT_ASSIGN_SLOT, createQuestActionItem(scope, quest));
        inventory.setItem(EDIT_TARGET_PLUS_ONE_SLOT, createItem(Material.LIME_STAINED_GLASS_PANE, "&aTarget +1", List.of("&7Raise target by 1.")));
        inventory.setItem(EDIT_TARGET_PLUS_TEN_SLOT, createItem(Material.GREEN_STAINED_GLASS_PANE, "&aTarget +10", List.of("&7Raise target by 10.")));
        inventory.setItem(EDIT_HINT_SLOT, createItem(Material.PAPER, "&aEdit Hint", List.of(
                "&7Current: &f" + stripColors(quest.getHint()),
                "&eClick and type in chat"
        )));
        inventory.setItem(EDIT_REWARD_MONEY_SLOT, createItem(Material.GOLD_INGOT, "&6Reward Money", List.of(
                "&7Current: &f" + plugin.formatMoney(quest.getRewardMoney()),
                "&eLeft/right click: +/-1",
                "&eShift left/right: +/-10"
        )));
        inventory.setItem(EDIT_REQUIRED_ITEM_SLOT, createRequiredItemMaterialItem(quest));
        inventory.setItem(EDIT_REWARD_XP_SLOT, createItem(Material.EXPERIENCE_BOTTLE, "&6Reward Profession XP", List.of(
                "&7Current: &f" + quest.getRewardProfessionXp(),
                "&eLeft/right click: +/-1",
                "&eShift left/right: +/-10"
        )));
        inventory.setItem(EDIT_REWARD_PROFESSION_SLOT, createItem(Material.ENCHANTED_BOOK, "&6Reward Profession", List.of(
                "&7Current: &f" + formatProfession(quest.getRewardProfession()),
                "&7If empty, reward XP uses the quest or player profession.",
                "&eLeft click: next",
                "&eRight click: previous"
        )));
        inventory.setItem(EDIT_REWARD_ITEM_MATERIAL_SLOT, createRewardItemMaterialItem(quest));
        inventory.setItem(EDIT_REWARD_ITEM_AMOUNT_SLOT, createItem(Material.HOPPER, "&6Reward Item Amount", List.of(
                "&7Current: &f" + quest.getRewardItemAmount(),
                "&eLeft/right click: +/-1",
                "&eShift left/right: +/-16"
        )));
        inventory.setItem(EDIT_DELETE_SLOT, createItem(Material.BARRIER, "&cDelete", List.of(
                "&7Remove this " + (scope == QuestScope.ONBOARDING ? "step" : "quest") + " from config."
        )));
        inventory.setItem(EDIT_BACK_SLOT, createItem(Material.ARROW, "&eBack", List.of("&7Return to the list.")));
        player.openInventory(inventory);
    }

    private void openQuestAssignMenu(Player player, String questId, int page) {
        PlayerQuestDefinition quest = plugin.getGeneralQuestDefinition(questId);
        if (quest == null) {
            openQuestListMenu(player, QuestScope.GENERAL, 0);
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
                "&7Assigned quests take priority over onboarding."
        )));

        int start = safePage * LIST_SLOTS.length;
        for (int i = 0; i < LIST_SLOTS.length && start + i < players.size(); i++) {
            inventory.setItem(LIST_SLOTS[i], createPlayerAssignmentItem(players.get(start + i), quest.getId()));
        }

        inventory.setItem(ASSIGN_PREVIOUS_SLOT, createItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of("&7Open the previous page.")));
        inventory.setItem(ASSIGN_BACK_SLOT, createItem(Material.ARROW, "&eBack", List.of("&7Return to the quest editor.")));
        inventory.setItem(ASSIGN_NEXT_SLOT, createItem(Material.ARROW, "&eNext Page", List.of("&7Open the next page.")));
        player.openInventory(inventory);
    }

    private void openQuestKeySelector(Player player, QuestScope scope, String questId, int page) {
        PlayerQuestDefinition quest = getDefinition(scope, questId);
        if (quest == null) {
            openQuestListMenu(player, scope, 0);
            return;
        }
        List<String> options = buildQuestKeyOptions(quest);
        int totalPages = Math.max(1, (int) Math.ceil(options.size() / (double) LIST_SLOTS.length));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));
        Inventory inventory = Bukkit.createInventory(
                new QuestKeySelectorHolder(player.getUniqueId(), scope, questId, safePage),
                GUI_SIZE,
                plugin.legacyComponent("&8Select Quest Key")
        );
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createItem(Material.TRIPWIRE_HOOK, "&6Quest Key", List.of(
                "&7Quest: &f" + quest.getId(),
                "&7Type: &f" + formatType(quest.getType()),
                "&7Current: &f" + formatQuestKey(quest.getKey()),
                "&eClick an option to apply it."
        )));
        int start = safePage * LIST_SLOTS.length;
        for (int i = 0; i < LIST_SLOTS.length && start + i < options.size(); i++) {
            inventory.setItem(LIST_SLOTS[i], createQuestKeyOptionItem(quest, options.get(start + i)));
        }
        inventory.setItem(KEY_SELECTOR_PREVIOUS_SLOT, createItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of("&7Open the previous page.")));
        inventory.setItem(KEY_SELECTOR_BACK_SLOT, createItem(Material.ARROW, "&eBack", List.of("&7Return to the quest editor.")));
        inventory.setItem(KEY_SELECTOR_NEXT_SLOT, createItem(Material.ARROW, "&eNext Page", List.of("&7Open the next page.")));
        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof ScopeHolder)
                && !(holder instanceof QuestListHolder)
                && !(holder instanceof QuestEditHolder)
                && !(holder instanceof QuestAssignHolder)
                && !(holder instanceof QuestKeySelectorHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
            return;
        }

        if (holder instanceof ScopeHolder) {
            handleScopeClick(player, event.getSlot());
            return;
        }
        if (holder instanceof QuestListHolder listHolder) {
            handleQuestListClick(player, listHolder, event.getSlot());
            return;
        }
        if (holder instanceof QuestEditHolder editHolder) {
            handleQuestEditClick(player, editHolder, event);
            return;
        }
        if (holder instanceof QuestAssignHolder assignHolder) {
            handleQuestAssignClick(player, assignHolder, event.getSlot());
            return;
        }
        handleQuestKeySelectorClick(player, (QuestKeySelectorHolder) holder, event.getSlot());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ScopeHolder
                || event.getView().getTopInventory().getHolder() instanceof QuestListHolder
                || event.getView().getTopInventory().getHolder() instanceof QuestEditHolder
                || event.getView().getTopInventory().getHolder() instanceof QuestAssignHolder
                || event.getView().getTopInventory().getHolder() instanceof QuestKeySelectorHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
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
                openQuestEditMenu(event.getPlayer(), pending.scope(), pending.questId());
                return;
            }

            boolean success = switch (pending.field()) {
                case TITLE -> setQuestTitle(pending.scope(), pending.questId(), message);
                case OBJECTIVE -> setQuestObjective(pending.scope(), pending.questId(), message);
                case HINT -> setQuestHint(pending.scope(), pending.questId(), message);
                case KEY -> setQuestKey(pending.scope(), pending.questId(), message);
            };
            if (!success) {
                event.getPlayer().sendMessage(plugin.legacyComponent("&cFailed to save quest text."));
            } else {
                event.getPlayer().sendMessage(plugin.legacyComponent("&aUpdated " + pending.field().name().toLowerCase(Locale.ROOT) + "."));
            }
            openQuestEditMenu(event.getPlayer(), pending.scope(), pending.questId());
        });
    }

    private void handleScopeClick(Player player, int slot) {
        if (slot == SCOPE_ONBOARDING_SLOT) {
            openQuestListMenu(player, QuestScope.ONBOARDING, 0);
        } else if (slot == SCOPE_GENERAL_SLOT) {
            openQuestListMenu(player, QuestScope.GENERAL, 0);
        }
    }

    private void handleQuestListClick(Player player, QuestListHolder holder, int slot) {
        if (slot == LIST_SWITCH_SCOPE_SLOT) {
            openScopeMenu(player);
            return;
        }
        if (slot == LIST_PREVIOUS_SLOT) {
            openQuestListMenu(player, holder.scope(), holder.page() - 1);
            return;
        }
        if (slot == LIST_NEXT_SLOT) {
            openQuestListMenu(player, holder.scope(), holder.page() + 1);
            return;
        }
        if (slot == LIST_ADD_SLOT) {
            String questId = holder.scope() == QuestScope.ONBOARDING
                    ? plugin.createOnboardingQuestDefinition()
                    : plugin.createGeneralQuestDefinition();
            if (questId == null) {
                player.sendMessage(plugin.legacyComponent("&cFailed to create entry."));
                return;
            }
            player.sendMessage(plugin.legacyComponent("&aCreated &f" + questId + "&a."));
            openQuestEditMenu(player, holder.scope(), questId);
            return;
        }

        int indexInPage = indexOfSlot(slot);
        if (indexInPage < 0) {
            return;
        }
        List<PlayerQuestDefinition> quests = getDefinitions(holder.scope());
        int index = holder.page() * LIST_SLOTS.length + indexInPage;
        if (index >= 0 && index < quests.size()) {
            openQuestEditMenu(player, holder.scope(), quests.get(index).getId());
        }
    }

    private void handleQuestEditClick(Player player, QuestEditHolder holder, InventoryClickEvent event) {
        PlayerQuestDefinition quest = getDefinition(holder.scope(), holder.questId());
        if (quest == null) {
            openQuestListMenu(player, holder.scope(), 0);
            return;
        }

        int slot = event.getSlot();
        boolean refresh = true;
        switch (slot) {
            case EDIT_TYPE_SLOT -> setQuestType(holder.scope(), quest.getId(), cycleType(quest.getType(), event.isRightClick() ? -1 : 1));
            case EDIT_PROFESSION_SLOT -> setQuestProfession(holder.scope(), quest.getId(), cycleProfession(quest.getProfession(), event.isRightClick() ? -1 : 1));
            case EDIT_ENABLED_SLOT -> setQuestEnabled(holder.scope(), quest.getId(), !quest.isEnabled());
            case EDIT_LINK_PREVIOUS_SLOT -> togglePreviousQuestDependency(holder.scope(), quest);
            case EDIT_KEY_SLOT -> {
                if (event.isShiftClick() || !supportsKeySelector(quest.getType())) {
                    beginTextEdit(player, holder.scope(), quest.getId(), TextField.KEY, "Type the new quest key in chat. Type cancel to abort.");
                    return;
                }
                openQuestKeySelector(player, holder.scope(), quest.getId(), 0);
                return;
            }
            case EDIT_TITLE_SLOT -> {
                beginTextEdit(player, holder.scope(), quest.getId(), TextField.TITLE, "Type the new quest title in chat. Type cancel to abort.");
                return;
            }
            case EDIT_OBJECTIVE_SLOT -> {
                beginTextEdit(player, holder.scope(), quest.getId(), TextField.OBJECTIVE, "Type the new quest objective in chat. Type cancel to abort.");
                return;
            }
            case EDIT_HINT_SLOT -> {
                beginTextEdit(player, holder.scope(), quest.getId(), TextField.HINT, "Type the new quest hint in chat. Type cancel to abort.");
                return;
            }
            case EDIT_REQUIRED_ITEM_SLOT -> handleRequiredItemMaterialClick(player, holder.scope(), quest, event);
            case EDIT_REWARD_MONEY_SLOT -> setQuestRewardMoney(holder.scope(), quest.getId(), adjustMoney(quest.getRewardMoney(), event));
            case EDIT_REWARD_XP_SLOT -> setQuestRewardProfessionXp(holder.scope(), quest.getId(), adjustWholeNumber(quest.getRewardProfessionXp(), event, 1, 10));
            case EDIT_ORDER_MINUS_TEN_SLOT -> setQuestOrder(holder.scope(), quest.getId(), quest.getOrder() - 10);
            case EDIT_ORDER_MINUS_ONE_SLOT -> setQuestOrder(holder.scope(), quest.getId(), quest.getOrder() - 1);
            case EDIT_ORDER_PLUS_ONE_SLOT -> setQuestOrder(holder.scope(), quest.getId(), quest.getOrder() + 1);
            case EDIT_ORDER_PLUS_TEN_SLOT -> setQuestOrder(holder.scope(), quest.getId(), quest.getOrder() + 10);
            case EDIT_TARGET_MINUS_TEN_SLOT -> setQuestTarget(holder.scope(), quest.getId(), quest.getTarget() - 10);
            case EDIT_TARGET_MINUS_ONE_SLOT -> setQuestTarget(holder.scope(), quest.getId(), quest.getTarget() - 1);
            case EDIT_TARGET_PLUS_ONE_SLOT -> setQuestTarget(holder.scope(), quest.getId(), quest.getTarget() + 1);
            case EDIT_TARGET_PLUS_TEN_SLOT -> setQuestTarget(holder.scope(), quest.getId(), quest.getTarget() + 10);
            case EDIT_REWARD_PROFESSION_SLOT -> setQuestRewardProfession(holder.scope(), quest.getId(), cycleProfession(quest.getRewardProfession(), event.isRightClick() ? -1 : 1));
            case EDIT_REWARD_ITEM_MATERIAL_SLOT -> handleRewardItemMaterialClick(player, holder.scope(), quest, event);
            case EDIT_REWARD_ITEM_AMOUNT_SLOT -> setQuestRewardItemAmount(holder.scope(), quest.getId(), adjustWholeNumber(quest.getRewardItemAmount(), event, 1, 16));
            case EDIT_ASSIGN_SLOT -> {
                handleQuestActionClick(player, holder.scope(), quest, event);
                return;
            }
            case EDIT_DELETE_SLOT -> {
                boolean deleted = holder.scope() == QuestScope.ONBOARDING
                        ? plugin.deleteOnboardingQuestDefinition(quest.getId())
                        : plugin.deleteGeneralQuestDefinition(quest.getId());
                if (deleted) {
                    player.sendMessage(plugin.legacyComponent("&cDeleted &f" + quest.getId() + "&c."));
                }
                openQuestListMenu(player, holder.scope(), 0);
                return;
            }
            case EDIT_BACK_SLOT -> {
                openQuestListMenu(player, holder.scope(), 0);
                return;
            }
            default -> refresh = false;
        }
        if (refresh) {
            openQuestEditMenu(player, holder.scope(), quest.getId());
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
            openQuestEditMenu(player, QuestScope.GENERAL, holder.questId());
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

    private void handleQuestKeySelectorClick(Player player, QuestKeySelectorHolder holder, int slot) {
        if (slot == KEY_SELECTOR_PREVIOUS_SLOT) {
            openQuestKeySelector(player, holder.scope(), holder.questId(), holder.page() - 1);
            return;
        }
        if (slot == KEY_SELECTOR_NEXT_SLOT) {
            openQuestKeySelector(player, holder.scope(), holder.questId(), holder.page() + 1);
            return;
        }
        if (slot == KEY_SELECTOR_BACK_SLOT) {
            openQuestEditMenu(player, holder.scope(), holder.questId());
            return;
        }
        int indexInPage = indexOfSlot(slot);
        if (indexInPage < 0) {
            return;
        }
        PlayerQuestDefinition quest = getDefinition(holder.scope(), holder.questId());
        if (quest == null) {
            openQuestListMenu(player, holder.scope(), 0);
            return;
        }
        List<String> options = buildQuestKeyOptions(quest);
        int index = holder.page() * LIST_SLOTS.length + indexInPage;
        if (index < 0 || index >= options.size()) {
            return;
        }
        String value = options.get(index);
        boolean success = CLEAR_KEY_OPTION.equals(value)
                ? setQuestKey(holder.scope(), holder.questId(), null)
                : setQuestKey(holder.scope(), holder.questId(), value);
        if (!success) {
            player.sendMessage(plugin.legacyComponent("&cCould not update that quest key."));
        }
        openQuestEditMenu(player, holder.scope(), holder.questId());
    }

    private void beginTextEdit(Player player, QuestScope scope, String questId, TextField field, String prompt) {
        pendingTextEdits.put(player.getUniqueId(), new PendingTextEdit(scope, questId, field));
        player.closeInventory();
        player.sendMessage(plugin.legacyComponent("&e" + prompt));
    }

    private void togglePreviousQuestDependency(QuestScope scope, PlayerQuestDefinition quest) {
        PlayerQuestDefinition previous = getPreviousQuest(scope, quest.getId());
        if (previous == null) {
            return;
        }
        List<String> updated = new ArrayList<>(quest.getRequiresCompleted());
        if (updated.removeIf(previous.getId()::equalsIgnoreCase)) {
            setQuestRequiresCompleted(scope, quest.getId(), updated);
            return;
        }
        updated.add(previous.getId());
        setQuestRequiresCompleted(scope, quest.getId(), updated);
    }

    private void handleQuestActionClick(Player player, QuestScope scope, PlayerQuestDefinition quest, InventoryClickEvent event) {
        if (scope == QuestScope.GENERAL) {
            openQuestAssignMenu(player, quest.getId(), 0);
            return;
        }
        if (quest.getType() == PlayerQuestType.INTERACT_NPC || quest.getType() == PlayerQuestType.DELIVER_ITEM) {
            plugin.armPendingQuestNpcSelection(player, scope, quest.getId());
            player.closeInventory();
            player.sendMessage(plugin.legacyComponent("&eRight-click the NPC you want to bind to this quest step."));
            return;
        }
        if (quest.getType() == PlayerQuestType.VISIT_LOCATION) {
            if (event.isLeftClick()) {
                saveLocationMarkerFromSelection(player, scope, quest);
            } else if (event.isRightClick()) {
                saveLocationMarkerFromCurrentPosition(player, scope, quest);
            }
            openQuestEditMenu(player, scope, quest.getId());
        }
    }

    private void saveLocationMarkerFromSelection(Player player, QuestScope scope, PlayerQuestDefinition quest) {
        String key = ensureQuestKeyForLocation(scope, quest);
        if (key == null) {
            player.sendMessage(plugin.legacyComponent("&cCould not set a location key for that step."));
            return;
        }
        Region region = getWorldEditSelection(player);
        if (region == null) {
            return;
        }
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        boolean saved = plugin.setOnboardingLocationMarkerRegion(
                key,
                player.getWorld(),
                min.x(),
                min.y(),
                min.z(),
                max.x(),
                max.y(),
                max.z(),
                deriveLocationMarkerDisplayName(quest, key)
        );
        player.sendMessage(plugin.legacyComponent(saved
                ? "&aSaved location marker &f" + key + "&a from your WorldEdit selection."
                : "&cCould not save that location marker."));
    }

    private void saveLocationMarkerFromCurrentPosition(Player player, QuestScope scope, PlayerQuestDefinition quest) {
        String key = ensureQuestKeyForLocation(scope, quest);
        if (key == null) {
            player.sendMessage(plugin.legacyComponent("&cCould not set a location key for that step."));
            return;
        }
        boolean saved = plugin.setOnboardingLocationMarker(
                key,
                player.getLocation(),
                8.0D,
                deriveLocationMarkerDisplayName(quest, key)
        );
        player.sendMessage(plugin.legacyComponent(saved
                ? "&aSaved point marker &f" + key + "&a at your current location."
                : "&cCould not save that point marker."));
    }

    private String ensureQuestKeyForLocation(QuestScope scope, PlayerQuestDefinition quest) {
        String key = quest.getKey();
        if (key != null && !key.isBlank()) {
            return key;
        }
        String generated = quest.getId();
        return setQuestKey(scope, quest.getId(), generated) ? generated : null;
    }

    private void handleRewardItemMaterialClick(Player player, QuestScope scope, PlayerQuestDefinition quest, InventoryClickEvent event) {
        if (event.isRightClick()) {
            setQuestRewardItemMaterial(scope, quest.getId(), null);
            return;
        }
        ItemStack reference = getReferenceItem(player, event);
        if (reference != null && !reference.getType().isAir() && reference.getType().isItem()) {
            setQuestRewardItemMaterial(scope, quest.getId(), reference.getType());
            if (quest.getRewardItemAmount() <= 0) {
                setQuestRewardItemAmount(scope, quest.getId(), Math.max(1, reference.getAmount()));
            }
            return;
        }
        player.sendMessage(plugin.legacyComponent("&eHold an item on your cursor or in your main hand, then click again. Right click clears the reward item."));
    }

    private void handleRequiredItemMaterialClick(Player player, QuestScope scope, PlayerQuestDefinition quest, InventoryClickEvent event) {
        if (event.isRightClick()) {
            setQuestRequiredItemMaterial(scope, quest.getId(), null);
            return;
        }
        ItemStack reference = getReferenceItem(player, event);
        if (reference != null && !reference.getType().isAir() && reference.getType().isItem()) {
            setQuestRequiredItemMaterial(scope, quest.getId(), reference.getType());
            if (quest.getTarget() <= 0) {
                setQuestTarget(scope, quest.getId(), Math.max(1, reference.getAmount()));
            }
            return;
        }
        player.sendMessage(plugin.legacyComponent("&eHold the required delivery item on your cursor or in your main hand, then click again. Right click clears it."));
    }

    private ItemStack getReferenceItem(Player player, InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        if (cursor != null && !cursor.getType().isAir()) {
            return cursor;
        }
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        return mainHand != null && !mainHand.getType().isAir() ? mainHand : null;
    }

    private PlayerQuestType cycleType(PlayerQuestType current, int delta) {
        PlayerQuestType[] values = PlayerQuestType.values();
        int index = current != null ? current.ordinal() : 0;
        return values[Math.floorMod(index + delta, values.length)];
    }

    private Profession cycleProfession(Profession current, int delta) {
        List<Profession> professions = new ArrayList<>();
        professions.add(null);
        professions.addAll(plugin.getConfiguredProfessions());
        int index = professions.indexOf(current);
        if (index < 0) {
            index = 0;
        }
        return professions.get(Math.floorMod(index + delta, professions.size()));
    }

    private boolean supportsKeySelector(PlayerQuestType type) {
        return switch (type) {
            case OPEN_GUIDE, VISIT_LOCATION, BREAK_BLOCK, PLACE_BLOCK, INTERACT_BLOCK, INTERACT_NPC, DELIVER_ITEM, COMPLETE_TRIAL -> true;
            default -> false;
        };
    }

    private List<String> buildQuestKeyOptions(PlayerQuestDefinition quest) {
        LinkedHashSet<String> options = new LinkedHashSet<>();
        options.add(CLEAR_KEY_OPTION);
        if (quest == null || quest.getType() == null) {
            return new ArrayList<>(options);
        }
        switch (quest.getType()) {
            case OPEN_GUIDE -> options.add("guide_opened");
            case VISIT_LOCATION -> options.addAll(plugin.getOnboardingLocationMarkerKeys());
            case BREAK_BLOCK -> options.add("any_block_break");
            case PLACE_BLOCK -> options.add("any_block_place");
            case INTERACT_BLOCK -> options.add("any_block_interact");
            case INTERACT_NPC, DELIVER_ITEM -> options.addAll(plugin.getOnboardingNpcQuestKeys());
            case COMPLETE_TRIAL -> {
                options.add("any");
                for (Profession profession : plugin.getConfiguredProfessions()) {
                    if (profession != Profession.SOLDIER) {
                        options.add(profession.getKey());
                    }
                }
            }
            default -> {
            }
        }
        if (quest.getKey() != null && !quest.getKey().isBlank()) {
            options.add(quest.getKey());
        }
        return new ArrayList<>(options);
    }

    private ItemStack createInfoItem(PlayerQuestDefinition quest) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Order: &f" + quest.getOrder());
        lore.add("&7Type: &f" + formatType(quest.getType()));
        lore.add("&7Key: &f" + formatQuestKey(quest.getKey()));
        lore.add("&7Target: &f" + quest.getTarget());
        lore.add("&8");
        lore.add("&eMost fields now use click actions.");
        lore.add("&eShift-click key to type manually.");
        return createItem(Material.NAME_TAG, "&6" + quest.getId(), lore);
    }

    private ItemStack createQuestKeyItem(PlayerQuestDefinition quest) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Current: &f" + formatQuestKey(quest.getKey()));
        if (quest.getType() == PlayerQuestType.VISIT_LOCATION && quest.getKey() != null && !quest.getKey().isBlank()) {
            lore.add("&7Marker: &f" + plugin.getOnboardingLocationMarkerSummary(quest.getKey()));
        }
        if (quest.getType() == PlayerQuestType.DELIVER_ITEM) {
            lore.add("&7Required item: &f" + (quest.getRequiredItemMaterial() != null ? plugin.formatMaterialName(quest.getRequiredItemMaterial()) : "None"));
        }
        if (supportsKeySelector(quest.getType())) {
            lore.add("&eClick to choose from a list.");
        }
        lore.add("&eShift-click to type manually.");
        return createItem(Material.TRIPWIRE_HOOK, "&aQuest Key", lore);
    }

    private ItemStack createQuestActionItem(QuestScope scope, PlayerQuestDefinition quest) {
        if (scope == QuestScope.GENERAL) {
            return createItem(Material.PLAYER_HEAD, "&bAssign To Players", List.of(
                    "&7Open the player assignment menu."
            ));
        }
        if (quest.getType() == PlayerQuestType.INTERACT_NPC) {
            return createItem(Material.VILLAGER_SPAWN_EGG, "&eSelect NPC", List.of(
                    "&7Link this step to a FancyNpcs guide",
                    "&7by right-clicking it.",
                    "&7Current key: &f" + formatQuestKey(quest.getKey()),
                    "&eClick to arm NPC selection"
            ));
        }
        if (quest.getType() == PlayerQuestType.DELIVER_ITEM) {
            return createItem(Material.CHEST_MINECART, "&eDelivery NPC", List.of(
                    "&7Bind the NPC that should accept",
                    "&7this item delivery quest.",
                    "&7Current key: &f" + formatQuestKey(quest.getKey()),
                    "&eClick to arm NPC selection"
            ));
        }
        if (quest.getType() == PlayerQuestType.VISIT_LOCATION) {
            return createItem(Material.LODESTONE, "&eLocation Marker Tools", List.of(
                    "&7Current key: &f" + formatQuestKey(quest.getKey()),
                    quest.getKey() != null && !quest.getKey().isBlank()
                            ? "&7Marker: &f" + plugin.getOnboardingLocationMarkerSummary(quest.getKey())
                            : "&7Marker: &fNot set",
                    "&eLeft click: save from WorldEdit selection",
                    "&eRight click: save point at your location"
            ));
        }
        return createItem(Material.COMPASS, "&eOnboarding Step", List.of(
                "&7This step is part of the first-hour",
                "&7tutorial sequence."
        ));
    }

    private ItemStack createRewardItemMaterialItem(PlayerQuestDefinition quest) {
        Material material = quest.getRewardItemMaterial() != null ? quest.getRewardItemMaterial() : Material.CHEST;
        List<String> lore = new ArrayList<>();
        lore.add("&7Current: &f" + (quest.getRewardItemMaterial() != null ? plugin.formatMaterialName(quest.getRewardItemMaterial()) : "None"));
        lore.add("&eLeft click: use cursor or main hand item");
        lore.add("&eRight click: clear");
        return createItem(material, "&6Reward Item", lore);
    }

    private ItemStack createRequiredItemMaterialItem(PlayerQuestDefinition quest) {
        Material material = quest.getRequiredItemMaterial() != null ? quest.getRequiredItemMaterial() : Material.HOPPER_MINECART;
        List<String> lore = new ArrayList<>();
        lore.add("&7Current: &f" + (quest.getRequiredItemMaterial() != null ? plugin.formatMaterialName(quest.getRequiredItemMaterial()) : "None"));
        lore.add("&7Used by delivery quests.");
        lore.add("&eLeft click: use cursor or main hand item");
        lore.add("&eRight click: clear");
        return createItem(material, "&6Required Delivery Item", lore);
    }

    private ItemStack createQuestKeyOptionItem(PlayerQuestDefinition quest, String option) {
        boolean clear = CLEAR_KEY_OPTION.equals(option);
        boolean selected = !clear && option.equalsIgnoreCase(quest.getKey());
        Material material;
        List<String> lore = new ArrayList<>();
        String name;
        if (clear) {
            material = Material.BARRIER;
            name = "&cClear Quest Key";
            lore.add("&7Remove the stored key.");
        } else {
            material = switch (quest.getType()) {
                case VISIT_LOCATION -> Material.LODESTONE;
                case INTERACT_NPC, DELIVER_ITEM -> Material.VILLAGER_SPAWN_EGG;
                case COMPLETE_TRIAL -> Material.TARGET;
                case OPEN_GUIDE -> Material.BOOK;
                case BREAK_BLOCK -> Material.IRON_PICKAXE;
                case PLACE_BLOCK -> Material.BRICKS;
                case INTERACT_BLOCK -> Material.LEVER;
                default -> Material.TRIPWIRE_HOOK;
            };
            name = (selected ? "&a" : "&e") + option;
            if (quest.getType() == PlayerQuestType.VISIT_LOCATION) {
                lore.add("&7" + plugin.getOnboardingLocationMarkerSummary(option));
            }
            lore.add(selected ? "&aCurrently selected." : "&eClick to select.");
        }
        return createItem(material, name, lore);
    }

    private List<PlayerQuestDefinition> getDefinitions(QuestScope scope) {
        return scope == QuestScope.ONBOARDING ? plugin.getOnboardingQuestDefinitions() : plugin.getGeneralQuestDefinitions();
    }

    private PlayerQuestDefinition getDefinition(QuestScope scope, String questId) {
        return scope == QuestScope.ONBOARDING ? plugin.getOnboardingQuestDefinition(questId) : plugin.getGeneralQuestDefinition(questId);
    }

    private PlayerQuestDefinition getPreviousQuest(QuestScope scope, String questId) {
        List<PlayerQuestDefinition> definitions = new ArrayList<>(getDefinitions(scope));
        definitions.sort(Comparator.comparingInt(PlayerQuestDefinition::getOrder).thenComparing(PlayerQuestDefinition::getId));
        PlayerQuestDefinition previous = null;
        for (PlayerQuestDefinition definition : definitions) {
            if (definition.getId().equalsIgnoreCase(questId)) {
                return previous;
            }
            previous = definition;
        }
        return null;
    }

    private boolean setQuestEnabled(QuestScope scope, String questId, boolean enabled) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestEnabled(questId, enabled)
                : plugin.setGeneralQuestEnabled(questId, enabled);
    }

    private boolean setQuestType(QuestScope scope, String questId, PlayerQuestType type) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestType(questId, type)
                : plugin.setGeneralQuestType(questId, type);
    }

    private boolean setQuestProfession(QuestScope scope, String questId, Profession profession) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestProfession(questId, profession)
                : plugin.setGeneralQuestProfession(questId, profession);
    }

    private boolean setQuestTarget(QuestScope scope, String questId, int target) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestTarget(questId, target)
                : plugin.setGeneralQuestTarget(questId, target);
    }

    private boolean setQuestTitle(QuestScope scope, String questId, String title) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestTitle(questId, title)
                : plugin.setGeneralQuestTitle(questId, title);
    }

    private boolean setQuestObjective(QuestScope scope, String questId, String objective) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestObjective(questId, objective)
                : plugin.setGeneralQuestObjective(questId, objective);
    }

    private boolean setQuestHint(QuestScope scope, String questId, String hint) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestHint(questId, hint)
                : plugin.setGeneralQuestHint(questId, hint);
    }

    private boolean setQuestKey(QuestScope scope, String questId, String key) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestKey(questId, key)
                : plugin.setGeneralQuestKey(questId, key);
    }

    private boolean setQuestOrder(QuestScope scope, String questId, int order) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestOrder(questId, order)
                : plugin.setGeneralQuestOrder(questId, order);
    }

    private boolean setQuestRequiresCompleted(QuestScope scope, String questId, List<String> requiresCompleted) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestRequiresCompleted(questId, requiresCompleted)
                : plugin.setGeneralQuestRequiresCompleted(questId, requiresCompleted);
    }

    private boolean setQuestRewardMoney(QuestScope scope, String questId, double rewardMoney) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestRewardMoney(questId, rewardMoney)
                : plugin.setGeneralQuestRewardMoney(questId, rewardMoney);
    }

    private boolean setQuestRewardProfessionXp(QuestScope scope, String questId, int rewardProfessionXp) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestRewardProfessionXp(questId, rewardProfessionXp)
                : plugin.setGeneralQuestRewardProfessionXp(questId, rewardProfessionXp);
    }

    private boolean setQuestRewardProfession(QuestScope scope, String questId, Profession rewardProfession) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestRewardProfession(questId, rewardProfession)
                : plugin.setGeneralQuestRewardProfession(questId, rewardProfession);
    }

    private boolean setQuestRewardItemMaterial(QuestScope scope, String questId, Material material) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestRewardItemMaterial(questId, material)
                : plugin.setGeneralQuestRewardItemMaterial(questId, material);
    }

    private boolean setQuestRewardItemAmount(QuestScope scope, String questId, int amount) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestRewardItemAmount(questId, amount)
                : plugin.setGeneralQuestRewardItemAmount(questId, amount);
    }

    private boolean setQuestRequiredItemMaterial(QuestScope scope, String questId, Material material) {
        return scope == QuestScope.ONBOARDING
                ? plugin.setOnboardingQuestRequiredItemMaterial(questId, material)
                : plugin.setGeneralQuestRequiredItemMaterial(questId, material);
    }

    private ItemStack createQuestSummaryItem(QuestScope scope, PlayerQuestDefinition quest) {
        Material icon = switch (quest.getType()) {
            case SELECT_PROFESSION -> Material.COMPASS;
            case EARN_PROFESSION_XP -> Material.EXPERIENCE_BOTTLE;
            case REACH_PROFESSION_LEVEL -> Material.EMERALD;
            case OPEN_GUIDE -> Material.BOOK;
            case VISIT_LOCATION -> Material.LODESTONE;
            case BREAK_BLOCK -> Material.IRON_PICKAXE;
            case PLACE_BLOCK -> Material.BRICKS;
            case INTERACT_BLOCK -> Material.LEVER;
            case INTERACT_NPC -> Material.VILLAGER_SPAWN_EGG;
            case DELIVER_ITEM -> Material.CHEST_MINECART;
            case COMPLETE_TRIAL -> Material.TARGET;
            case PLAYTIME -> Material.CLOCK;
            case JOIN_COUNTRY -> Material.BLUE_BANNER;
            case CONTRIBUTE_COUNTRY -> Material.GOLD_INGOT;
        };
        return createItem(icon, (quest.isEnabled() ? "&a" : "&7") + quest.getId(), List.of(
                "&7Scope: &f" + scope.label(),
                "&7Type: &f" + formatType(quest.getType()),
                "&7Order: &f" + quest.getOrder(),
                "&7Key: &f" + formatQuestKey(quest.getKey()),
                "&7Requires: &f" + formatDependencySummary(quest),
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
            case OPEN_GUIDE -> "Open Guide";
            case VISIT_LOCATION -> "Visit Location";
            case BREAK_BLOCK -> "Break Block";
            case PLACE_BLOCK -> "Place Block";
            case INTERACT_BLOCK -> "Interact Block";
            case INTERACT_NPC -> "Interact NPC";
            case DELIVER_ITEM -> "Deliver Item";
            case COMPLETE_TRIAL -> "Complete Trial";
            case PLAYTIME -> "Playtime";
            case JOIN_COUNTRY -> "Join Country";
            case CONTRIBUTE_COUNTRY -> "Contribute Country";
        };
    }

    private String formatProfession(Profession profession) {
        return profession != null ? plugin.getProfessionPlainDisplayName(profession) : "Any";
    }

    private String formatDependencySummary(PlayerQuestDefinition quest) {
        if (quest == null || quest.getRequiresCompleted().isEmpty()) {
            return "None";
        }
        return String.join(", ", quest.getRequiresCompleted());
    }

    private String formatQuestKey(String key) {
        return key != null && !key.isBlank() ? key : "None";
    }

    private String stripColors(String value) {
        return value == null ? "" : org.bukkit.ChatColor.stripColor(plugin.colorize(value));
    }

    private double adjustMoney(double current, InventoryClickEvent event) {
        double delta = event.isShiftClick() ? 10.0D : 1.0D;
        double updated = event.isRightClick() ? current - delta : current + delta;
        return Math.max(0.0D, updated);
    }

    private int adjustWholeNumber(int current, InventoryClickEvent event, int normalStep, int shiftStep) {
        int step = event.isShiftClick() ? shiftStep : normalStep;
        int updated = event.isRightClick() ? current - step : current + step;
        return Math.max(0, updated);
    }

    private String deriveLocationMarkerDisplayName(PlayerQuestDefinition quest, String fallbackKey) {
        String title = stripColors(quest != null ? quest.getTitle() : null).trim();
        return title.isBlank() ? fallbackKey : title;
    }

    private Region getWorldEditSelection(Player player) {
        WorldEditPlugin worldEdit = getWorldEditPlugin();
        if (worldEdit == null) {
            player.sendMessage(plugin.legacyComponent("&cWorldEdit is required for that action."));
            return null;
        }
        try {
            LocalSession session = worldEdit.getSession(player);
            return session.getSelection(BukkitAdapter.adapt(player.getWorld()));
        } catch (IncompleteRegionException exception) {
            player.sendMessage(plugin.legacyComponent("&cMake a WorldEdit selection first."));
            return null;
        }
    }

    private WorldEditPlugin getWorldEditPlugin() {
        return Bukkit.getPluginManager().getPlugin("WorldEdit") instanceof WorldEditPlugin worldEditPlugin
                ? worldEditPlugin
                : null;
    }

    public enum QuestScope {
        ONBOARDING("&6Onboarding Sequence", "&6Onboarding", Material.COMPASS),
        GENERAL("&bGeneral Quests", "&bGeneral", Material.BOOK);

        private final String title;
        private final String label;
        private final Material icon;

        QuestScope(String title, String label, Material icon) {
            this.title = title;
            this.label = label;
            this.icon = icon;
        }

        public String title() {
            return title;
        }

        public String label() {
            return label;
        }

        public Material icon() {
            return icon;
        }
    }

    private record ScopeHolder(UUID viewerId) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record QuestListHolder(UUID viewerId, QuestScope scope, int page) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record QuestEditHolder(UUID viewerId, QuestScope scope, String questId) implements InventoryHolder {
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

    private record QuestKeySelectorHolder(UUID viewerId, QuestScope scope, String questId, int page) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private enum TextField {
        TITLE,
        OBJECTIVE,
        HINT,
        KEY
    }

    private record PendingTextEdit(QuestScope scope, String questId, TextField field) {
    }
}
