package me.meetrow.testproject;

import io.papermc.paper.event.player.AsyncChatEvent;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FancyNpcDialogueGuiListener implements Listener {
    private static final int GUI_SIZE = 54;
    private static final int INFO_SLOT = 4;
    private static final int[] LIST_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int PREVIOUS_SLOT = 45;
    private static final int CLEAR_SLOT = 49;
    private static final int NEXT_SLOT = 53;
    private static final int FIRST_STAGE_SLOT = 46;
    private static final int REWARD_ITEM_SLOT = 47;
    private static final int REWARD_AMOUNT_SLOT = 48;
    private static final int REPEAT_STAGE_SLOT = 52;
    private static final int BACK_SLOT = 53;
    private static final int[] LINE_SLOTS = {19, 20, 21, 22, 23, 24, 25, 31};
    private static final int PREVIEW_SLOT = 40;

    private final Testproject plugin;
    private final Map<UUID, PendingDialogueEdit> pendingDialogueEdits = new ConcurrentHashMap<>();
    private final Map<UUID, PendingRewardEdit> pendingRewardEdits = new ConcurrentHashMap<>();

    public FancyNpcDialogueGuiListener(Testproject plugin) {
        this.plugin = plugin;
    }

    public void openNpcSelector(Player player, int page) {
        List<String> npcIds = plugin.getAvailableFancyNpcIds();
        int totalPages = Math.max(1, (int) Math.ceil(npcIds.size() / (double) LIST_SLOTS.length));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inventory = Bukkit.createInventory(new SelectorHolder(player.getUniqueId(), safePage), GUI_SIZE,
                plugin.legacyComponent("&8FancyNpc Dialogue"));
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createItem(Material.BOOK, "&6FancyNpc Dialogue", List.of(
                "&7Pick a FancyNpcs NPC to edit its",
                "&7onboarding dialogue lines.",
                "&7Page: &f" + (safePage + 1) + "&7/&f" + totalPages
        )));
        inventory.setItem(PREVIOUS_SLOT, createItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of("&7Open the previous page.")));
        inventory.setItem(NEXT_SLOT, createItem(Material.ARROW, "&eNext Page", List.of("&7Open the next page.")));

        int start = safePage * LIST_SLOTS.length;
        for (int i = 0; i < LIST_SLOTS.length && start + i < npcIds.size(); i++) {
            String npcId = npcIds.get(start + i);
            inventory.setItem(LIST_SLOTS[i], createNpcSelectorItem(npcId));
        }
        player.openInventory(inventory);
    }

    public void openNpcEditor(Player player, String fancyNpcId) {
        String resolvedFancyNpcId = plugin.resolveExistingFancyNpcId(fancyNpcId);
        if (resolvedFancyNpcId == null) {
            openNpcSelector(player, 0);
            return;
        }

        openNpcEditor(player, resolvedFancyNpcId, false);
    }

    public void openNpcEditor(Player player, String fancyNpcId, boolean repeatStage) {
        String resolvedFancyNpcId = plugin.resolveExistingFancyNpcId(fancyNpcId);
        if (resolvedFancyNpcId == null) {
            openNpcSelector(player, 0);
            return;
        }

        List<String> lines = plugin.getOnboardingFancyNpcDialogueEditorLines(resolvedFancyNpcId, repeatStage);
        String displayName = plugin.getOnboardingFancyNpcDisplayName(resolvedFancyNpcId);
        String questKey = plugin.getOnboardingFancyNpcQuestKey(resolvedFancyNpcId);
        String dialogueKey = repeatStage
                ? plugin.getOnboardingFancyNpcRepeatDialogueStorageKey(resolvedFancyNpcId)
                : plugin.getOnboardingFancyNpcDialogueStorageKey(resolvedFancyNpcId);
        List<ItemStack> rewardItems = plugin.getOnboardingFancyNpcFirstStageRewardItems(resolvedFancyNpcId);

        Inventory inventory = Bukkit.createInventory(new EditorHolder(player.getUniqueId(), resolvedFancyNpcId, repeatStage), GUI_SIZE,
                plugin.legacyComponent("&8Edit Dialogue: " + resolvedFancyNpcId));
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createItem(Material.NAME_TAG, "&6" + displayName, List.of(
                "&7Npc ID: &f" + resolvedFancyNpcId,
                "&7Quest Key: &f" + (questKey != null ? questKey : "None"),
                "&7Dialogue Key: &f" + (dialogueKey != null ? dialogueKey : resolvedFancyNpcId),
                "&7Stage: &f" + (repeatStage ? "Repeat" : "First Interaction"),
                "&7Click a line to edit it."
        )));

        for (int i = 0; i < LINE_SLOTS.length; i++) {
            String currentLine = i < lines.size() ? lines.get(i) : null;
            inventory.setItem(LINE_SLOTS[i], createDialogueLineItem(i, currentLine));
        }

        inventory.setItem(PREVIEW_SLOT, createPreviewItem(lines));
        inventory.setItem(FIRST_STAGE_SLOT, createItem(repeatStage ? Material.OAK_SIGN : Material.LIME_DYE, "&eFirst Interaction", List.of(
                "&7The first time a player clicks this NPC,",
                "&7these lines are shown before stage 2 unlocks."
        )));
        inventory.setItem(REWARD_ITEM_SLOT, createRewardItemPreview(rewardItems));
        inventory.setItem(REWARD_AMOUNT_SLOT, createRewardListControlItem(rewardItems));
        inventory.setItem(REPEAT_STAGE_SLOT, createItem(repeatStage ? Material.LIME_DYE : Material.WRITABLE_BOOK, "&eRepeat Interaction", List.of(
                "&7After the first interaction, the NPC",
                "&7can use different dialogue or open its menu."
        )));
        inventory.setItem(CLEAR_SLOT, createItem(Material.BARRIER, "&cClear All Dialogue", List.of(
                "&7Remove every stored line for this stage."
        )));
        inventory.setItem(BACK_SLOT, createItem(Material.ARROW, "&eBack", List.of("&7Return to the NPC list.")));
        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof SelectorHolder) && !(holder instanceof EditorHolder)) {
            return;
        }

        event.setCancelled(true);
        if (holder instanceof EditorHolder editorHolder) {
            if (handleRewardStackClick(player, editorHolder, event)) {
                return;
            }
        }
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
            return;
        }

        if (holder instanceof SelectorHolder selectorHolder) {
            handleSelectorClick(player, selectorHolder, event.getSlot());
            return;
        }
        handleEditorClick(player, (EditorHolder) holder, event.getSlot());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof SelectorHolder) {
            event.setCancelled(true);
            return;
        }
        if (holder instanceof EditorHolder editorHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }
            if (event.getOldCursor() == null || event.getOldCursor().getType().isAir()) {
                return;
            }
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot == REWARD_ITEM_SLOT || rawSlot == REWARD_AMOUNT_SLOT) {
                    applyRewardItemStack(player, editorHolder, event.getOldCursor());
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onAsyncChat(AsyncChatEvent event) {
        PendingDialogueEdit pending = pendingDialogueEdits.remove(event.getPlayer().getUniqueId());
        if (pending != null) {
            event.setCancelled(true);
            String message = plugin.plainText(event.message()).trim();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (message.equalsIgnoreCase("cancel")) {
                    event.getPlayer().sendMessage(plugin.legacyComponent("&cDialogue edit cancelled."));
                    openNpcEditor(event.getPlayer(), pending.fancyNpcId(), pending.repeatStage());
                    return;
                }

                List<String> lines = new ArrayList<>(plugin.getOnboardingFancyNpcDialogueEditorLines(pending.fancyNpcId(), pending.repeatStage()));
                while (lines.size() <= pending.lineIndex()) {
                    lines.add("");
                }
                if (message.equalsIgnoreCase("clear")) {
                    lines.set(pending.lineIndex(), "");
                } else {
                    lines.set(pending.lineIndex(), message);
                }
                boolean success = plugin.setOnboardingFancyNpcDialogue(pending.fancyNpcId(), lines, pending.repeatStage());
                if (!success) {
                    event.getPlayer().sendMessage(plugin.legacyComponent("&cCould not save that dialogue line."));
                } else {
                    event.getPlayer().sendMessage(plugin.legacyComponent("&aUpdated dialogue line " + (pending.lineIndex() + 1) + "."));
                }
                openNpcEditor(event.getPlayer(), pending.fancyNpcId(), pending.repeatStage());
            });
            return;
        }

        PendingRewardEdit rewardEdit = pendingRewardEdits.remove(event.getPlayer().getUniqueId());
        if (rewardEdit == null) {
            return;
        }

        event.setCancelled(true);
        String message = plugin.plainText(event.message()).trim();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (message.equalsIgnoreCase("cancel")) {
                event.getPlayer().sendMessage(plugin.legacyComponent("&cReward edit cancelled."));
                openNpcEditor(event.getPlayer(), rewardEdit.fancyNpcId(), false);
                return;
            }

            boolean success;
            if (message.equalsIgnoreCase("clear") || message.equalsIgnoreCase("none")) {
                success = plugin.clearOnboardingFancyNpcFirstStageRewards(rewardEdit.fancyNpcId());
            } else {
                String[] split = message.trim().split("\\s+");
                String materialToken = split.length > 0 ? split[0] : "";
                int amount = 1;
                if (split.length > 1) {
                    try {
                        amount = Integer.parseInt(split[1]);
                    } catch (NumberFormatException exception) {
                        amount = -1;
                    }
                }
                Material material = Material.matchMaterial(materialToken.trim().toUpperCase().replace(' ', '_').replace('-', '_'));
                success = material != null && material.isItem() && amount > 0
                        && plugin.addOnboardingFancyNpcFirstStageReward(rewardEdit.fancyNpcId(), new ItemStack(material, amount));
            }

            event.getPlayer().sendMessage(plugin.legacyComponent(success ? "&aUpdated first-time reward." : "&cCould not save that reward setting."));
            openNpcEditor(event.getPlayer(), rewardEdit.fancyNpcId(), false);
        });
    }

    private void handleSelectorClick(Player player, SelectorHolder holder, int slot) {
        if (slot == PREVIOUS_SLOT) {
            openNpcSelector(player, holder.page() - 1);
            return;
        }
        if (slot == NEXT_SLOT) {
            openNpcSelector(player, holder.page() + 1);
            return;
        }

        int indexOnPage = indexOfSlot(slot, LIST_SLOTS);
        if (indexOnPage < 0) {
            return;
        }
        List<String> npcIds = plugin.getAvailableFancyNpcIds();
        int index = holder.page() * LIST_SLOTS.length + indexOnPage;
        if (index >= 0 && index < npcIds.size()) {
            openNpcEditor(player, npcIds.get(index));
        }
    }

    private void handleEditorClick(Player player, EditorHolder holder, int slot) {
        if (slot == FIRST_STAGE_SLOT) {
            openNpcEditor(player, holder.fancyNpcId(), false);
            return;
        }
        if (slot == REWARD_ITEM_SLOT) {
            pendingRewardEdits.put(player.getUniqueId(), new PendingRewardEdit(holder.fancyNpcId(), RewardField.ITEM));
            player.closeInventory();
            player.sendMessage(plugin.legacyComponent("&eType a reward as &fMATERIAL AMOUNT&e to add it. Type &fclear&e to wipe rewards or &fcancel&e to abort."));
            return;
        }
        if (slot == REWARD_AMOUNT_SLOT) {
            boolean cleared = plugin.clearOnboardingFancyNpcFirstStageRewards(holder.fancyNpcId());
            player.sendMessage(plugin.legacyComponent(cleared
                    ? "&aCleared all first-time rewards."
                    : "&cCould not clear the first-time rewards."));
            openNpcEditor(player, holder.fancyNpcId(), false);
            return;
        }
        if (slot == REPEAT_STAGE_SLOT) {
            openNpcEditor(player, holder.fancyNpcId(), true);
            return;
        }
        if (slot == CLEAR_SLOT) {
            boolean cleared = plugin.clearOnboardingFancyNpcDialogue(holder.fancyNpcId(), holder.repeatStage());
            player.sendMessage(plugin.legacyComponent(cleared
                    ? "&aCleared dialogue for &f" + holder.fancyNpcId() + "&a."
                    : "&cNo stored dialogue existed for &f" + holder.fancyNpcId() + "&c."));
            openNpcEditor(player, holder.fancyNpcId(), holder.repeatStage());
            return;
        }
        if (slot == BACK_SLOT) {
            openNpcSelector(player, 0);
            return;
        }

        int lineIndex = indexOfSlot(slot, LINE_SLOTS);
        if (lineIndex < 0) {
            return;
        }
        pendingDialogueEdits.put(player.getUniqueId(), new PendingDialogueEdit(holder.fancyNpcId(), holder.repeatStage(), lineIndex));
        player.closeInventory();
        player.sendMessage(plugin.legacyComponent("&eType dialogue line " + (lineIndex + 1) + " in chat. Type &fclear&e to empty it or &fcancel&e to abort."));
    }

    private boolean handleRewardStackClick(Player player, EditorHolder holder, InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        if (cursor != null && !cursor.getType().isAir()) {
            int slot = event.getSlot();
            if (slot == REWARD_ITEM_SLOT || slot == REWARD_AMOUNT_SLOT) {
                applyRewardItemStack(player, holder, cursor);
                return true;
            }
        }
        if (event.getClickedInventory() != null
                && event.getClickedInventory().getType() == InventoryType.PLAYER
                && event.getCurrentItem() != null
                && !event.getCurrentItem().getType().isAir()
                && event.isShiftClick()) {
            applyRewardItemStack(player, holder, event.getCurrentItem());
            return true;
        }
        return false;
    }

    private void applyRewardItemStack(Player player, EditorHolder holder, ItemStack stack) {
        if (holder.repeatStage()) {
            player.sendMessage(plugin.legacyComponent("&eFirst-time rewards are configured from the first interaction stage."));
            return;
        }
        if (stack == null || stack.getType().isAir() || !stack.getType().isItem()) {
            player.sendMessage(plugin.legacyComponent("&cThat item cannot be used as a reward."));
            return;
        }
        boolean success = plugin.addOnboardingFancyNpcFirstStageReward(holder.fancyNpcId(), stack);
        player.sendMessage(plugin.legacyComponent(success
                ? "&aAdded first-time reward &f" + stack.getAmount() + "x " + plugin.formatItemDisplayName(stack) + "&a."
                : "&cCould not update that first-time reward."));
        openNpcEditor(player, holder.fancyNpcId(), false);
    }

    private ItemStack createNpcSelectorItem(String fancyNpcId) {
        String displayName = plugin.getOnboardingFancyNpcDisplayName(fancyNpcId);
        String questKey = plugin.getOnboardingFancyNpcQuestKey(fancyNpcId);
        String dialogueKey = plugin.getOnboardingFancyNpcDialogueStorageKey(fancyNpcId);
        List<String> lines = plugin.getOnboardingFancyNpcDialogueEditorLines(fancyNpcId);
        return createItem(Material.PLAYER_HEAD, "&6" + displayName, List.of(
                "&7Npc ID: &f" + fancyNpcId,
                "&7Quest Key: &f" + (questKey != null ? questKey : "None"),
                "&7Dialogue Key: &f" + (dialogueKey != null ? dialogueKey : fancyNpcId),
                "&7Stored Lines: &f" + lines.size(),
                "&eClick to edit"
        ));
    }

    private ItemStack createDialogueLineItem(int lineIndex, String line) {
        List<String> lore = new ArrayList<>();
        if (line == null || line.isBlank()) {
            lore.add("&7Current: &8<empty>");
        } else {
            lore.add("&7Current:");
            lore.add("&f" + line);
        }
        lore.add("&8");
        lore.add("&eClick to edit in chat");
        lore.add("&7Type &fclear &7to empty this line.");
        return createItem(line == null || line.isBlank() ? Material.GRAY_DYE : Material.PAPER,
                "&aLine " + (lineIndex + 1), lore);
    }

    private ItemStack createPreviewItem(List<String> lines) {
        List<String> lore = new ArrayList<>();
        if (lines.isEmpty()) {
            lore.add("&7No custom dialogue stored yet.");
        } else {
            for (String line : lines) {
                lore.add("&f" + line);
            }
        }
        return createItem(Material.WRITABLE_BOOK, "&bDialogue Preview", lore);
    }

    private ItemStack createRewardItemPreview(List<ItemStack> rewardItems) {
        ItemStack firstReward = rewardItems.isEmpty() ? null : rewardItems.getFirst();
        ItemStack item = firstReward != null ? firstReward.clone() : new ItemStack(Material.CHEST);
        item.setAmount(firstReward != null ? Math.max(1, Math.min(64, firstReward.getAmount())) : 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.displayName(plugin.legacyComponent("&6Add First-Time Reward"));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(plugin.legacyComponent("&7Current reward count: &f" + rewardItems.size()));
        lore.add(plugin.legacyComponent("&eDrag a stack here to add it."));
        lore.add(plugin.legacyComponent("&eShift-click a stack to add it."));
        lore.add(plugin.legacyComponent("&eClick to type &fMATERIAL AMOUNT"));
        lore.add(plugin.legacyComponent("&7Each stack is added to the reward list."));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createRewardListControlItem(List<ItemStack> rewardItems) {
        List<String> lore = new ArrayList<>();
        if (rewardItems.isEmpty()) {
            lore.add("&7No first-time rewards configured.");
        } else {
            lore.add("&7Current rewards:");
            for (ItemStack rewardItem : rewardItems) {
                lore.add("&f- " + rewardItem.getAmount() + "x " + plugin.formatItemDisplayName(rewardItem));
            }
        }
        lore.add("&8");
        lore.add("&cClick to clear all rewards.");
        return createItem(Material.HOPPER, "&6First-Time Reward List", lore);
    }

    private ItemStack createItem(Material material, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.displayName(plugin.legacyComponent(name));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        for (String loreLine : loreLines) {
            lore.add(plugin.legacyComponent(loreLine));
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
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    private int indexOfSlot(int slot, int[] slots) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == slot) {
                return i;
            }
        }
        return -1;
    }

    private record SelectorHolder(UUID viewerId, int page) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record EditorHolder(UUID viewerId, String fancyNpcId, boolean repeatStage) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record PendingDialogueEdit(String fancyNpcId, boolean repeatStage, int lineIndex) {
    }

    private record PendingRewardEdit(String fancyNpcId, RewardField field) {
    }

    private enum RewardField {
        ITEM
    }
}
