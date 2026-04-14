package me.meetrow.testproject;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BlacksmithAnvilListener implements Listener {
    private static final String RENAME_ANVIL_TITLE = "Rename Anvil";
    private static final int GUI_SIZE = 54;
    private static final int INFO_SLOT = 4;
    private static final int FORGE_STATUS_SLOT = 7;
    private static final int NORMAL_ANVIL_SLOT = 48;
    private static final int CLOSE_SLOT = 50;
    private static final int MERGE_SLOT = 52;
    private static final int CATEGORY_INFO_SLOT = 49;

    private static final int MERGE_GUI_SIZE = 54;
    private static final int[] MERGE_INPUT_SLOTS = {2, 3, 4, 5, 6, 20, 21, 22, 23, 24};
    private static final int MERGE_RARE_SLOT = 38;
    private static final int MERGE_PREVIEW_SLOT = 31;
    private static final int MERGE_ACTION_SLOT = 42;
    private static final int MERGE_CLOSE_SLOT = 53;

    private static final int[] RECIPE_SLOTS = {
            11, 12, 13, 14, 15, 16, 17,
            20, 21, 22, 23, 24, 25, 26,
            29, 30, 31, 32, 33, 34, 35
    };

    private final Testproject plugin;
    private final NamespacedKey mergeUiKey;
    private final Map<UUID, BukkitTask> activeMergeAnimations = new ConcurrentHashMap<>();

    public BlacksmithAnvilListener(Testproject plugin) {
        this.plugin = plugin;
        this.mergeUiKey = new NamespacedKey(plugin, "merge_ui_marker");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (!isAnvil(event.getClickedBlock().getType())) {
            return;
        }

        event.setCancelled(true);
        openMenu(event.getPlayer(), BlacksmithCategory.BASICS);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getType() == InventoryType.ANVIL
                && PlainTextComponentSerializer.plainText().serialize(event.getView().title()).equals(RENAME_ANVIL_TITLE)) {
            handleRenameAnvilClick(event);
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getView().getTopInventory().getHolder() instanceof MergeMenuHolder) {
            handleMergeMenuClick(event, player);
            return;
        }
        if (!(event.getView().getTopInventory().getHolder() instanceof BlacksmithMenuHolder holder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        if (event.getSlot() == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }
        if (event.getSlot() == NORMAL_ANVIL_SLOT) {
            openVanillaAnvil(player);
            return;
        }
        if (event.getSlot() == MERGE_SLOT) {
            openMergeMenu(player);
            return;
        }

        Testproject.BlacksmithRecipe recipe = getRecipeForSlot(holder.category(), event.getSlot());
        if (recipe == null) {
            return;
        }

        if (!plugin.canCraftBlacksmithRecipe(player.getUniqueId(), recipe)) {
            player.sendMessage(getRecipeLockMessage(player, recipe));
            return;
        }
        if (!hasIngredients(player, recipe.ingredients())) {
            player.sendMessage(plugin.getMessage("profession.blacksmith.anvil-no-materials", plugin.placeholders(
                    "item", plugin.formatMaterialName(recipe.result())
            )));
            return;
        }
        if (!plugin.tryConsumeSharedActionCooldown(player, plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH) ? Profession.BLACKSMITH : null)) {
            return;
        }

        removeIngredients(player, recipe.ingredients());
        ItemStack result = plugin.createForgedEquipment(player, recipe);
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(result);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }

        int awardedXp = plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH)
                ? plugin.rewardProfessionXp(player, Profession.BLACKSMITH, recipe.xp())
                : 0;
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0F, 1.1F);
        if (awardedXp > 0) {
            player.sendMessage(plugin.getMessage("profession.blacksmith.anvil-crafted", plugin.placeholders(
                    "item", plugin.formatMaterialName(recipe.result()),
                    "xp", String.valueOf(awardedXp)
            )));
        } else {
            player.sendMessage(plugin.colorize("&aForged &f" + plugin.formatMaterialName(recipe.result()) + "&a."));
        }
        openMenu(player, holder.category());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getType() == InventoryType.ANVIL
                && PlainTextComponentSerializer.plainText().serialize(event.getView().title()).equals(RENAME_ANVIL_TITLE)) {
            if (event.getRawSlots().contains(1) || event.getRawSlots().contains(2)) {
                event.setCancelled(true);
            }
            return;
        }
        if (event.getView().getTopInventory().getHolder() instanceof BlacksmithMenuHolder
                || event.getView().getTopInventory().getHolder() instanceof MergeMenuHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!PlainTextComponentSerializer.plainText().serialize(event.getView().title()).equals(RENAME_ANVIL_TITLE)) {
            return;
        }
        ItemStack base = event.getInventory().getFirstItem();
        ItemStack extra = event.getInventory().getSecondItem();
        if (base == null || base.getType().isAir() || (extra != null && !extra.getType().isAir())) {
            event.setResult(null);
            return;
        }
        String renameText = event.getView().getRenameText();
        if (renameText == null || renameText.isBlank()) {
            event.setResult(null);
            return;
        }
        ItemStack result = base.clone();
        result.editMeta(meta -> meta.displayName(plugin.legacyComponent("&f" + renameText)));
        event.setResult(result);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof MergeMenuHolder)) {
            return;
        }
        if (activeMergeAnimations.containsKey(player.getUniqueId())) {
            return;
        }
        returnMergeInputs(player, event.getInventory());
    }

    private void openMenu(Player player, BlacksmithCategory category) {
        Inventory inventory = Bukkit.createInventory(
                new BlacksmithMenuHolder(category),
                GUI_SIZE,
                plugin.legacyComponent(plugin.getBlacksmithAnvilGuiTitle())
        );

        fillForgeLayout(inventory);
        inventory.setItem(NORMAL_ANVIL_SLOT, createSimpleItem(Material.ANVIL, "&fRename Anvil", List.of(
                "&7Open a rename-only anvil.",
                "&7No repairing or combining."
        )));
        inventory.setItem(MERGE_SLOT, createSimpleItem(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, "&6Merge Forge", List.of(
                "&7Open the merging forge.",
                "&7Use 10 matching forged items",
                "&7to risk a higher tier."
        )));
        inventory.setItem(CLOSE_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close the forge menu.")));

        List<Testproject.BlacksmithRecipe> recipes = getVisibleForgeRecipes();
        for (int i = 0; i < RECIPE_SLOTS.length && i < recipes.size(); i++) {
            inventory.setItem(RECIPE_SLOTS[i], createRecipeItem(player, recipes.get(i)));
        }

        player.openInventory(inventory);
    }

    private List<Testproject.BlacksmithRecipe> getVisibleForgeRecipes() {
        List<Testproject.BlacksmithRecipe> recipes = new ArrayList<>();
        for (Testproject.BlacksmithRecipe recipe : plugin.getBlacksmithAnvilRecipes()) {
            if (recipe.result().name().startsWith("NETHERITE_") || recipe.category().equalsIgnoreCase("netherite")) {
                continue;
            }
            recipes.add(recipe);
        }
        recipes.sort((left, right) -> {
            int levelCompare = Integer.compare(left.level(), right.level());
            if (levelCompare != 0) {
                return levelCompare;
            }
            return left.result().name().compareTo(right.result().name());
        });
        return recipes;
    }

    private Testproject.BlacksmithRecipe getRecipeForSlot(BlacksmithCategory category, int slot) {
        List<Testproject.BlacksmithRecipe> recipes = getVisibleForgeRecipes();
        for (int i = 0; i < RECIPE_SLOTS.length && i < recipes.size(); i++) {
            if (RECIPE_SLOTS[i] == slot) {
                return recipes.get(i);
            }
        }
        return null;
    }

    private ItemStack createRecipeItem(Player player, Testproject.BlacksmithRecipe recipe) {
        boolean unlocked = plugin.canCraftBlacksmithRecipe(player.getUniqueId(), recipe);
        boolean blacksmith = plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH);
        int level = blacksmith ? plugin.getProfessionLevel(player.getUniqueId(), Profession.BLACKSMITH) : 0;
        List<String> lore = new ArrayList<>();
        lore.add("&7Recipe Level: &f" + recipe.level());
        lore.add("&7Craft XP: &f" + recipe.xp());
        if (plugin.isForgeManagedEquipment(recipe.result())) {
            lore.add("&7Output: &fRolled tier + rarity");
        }
        lore.add("");
        lore.add("&eMaterials");
        for (Map.Entry<Material, Integer> entry : recipe.ingredients().entrySet()) {
            lore.add("&8- &f" + entry.getValue() + "x " + plugin.formatMaterialName(entry.getKey()));
        }
        lore.add("");
        if (blacksmith) {
            lore.add(level >= recipe.level()
                    ? "&aBlacksmith quality bonus active."
                    : "&7Better rolls start at Blacksmith Lv." + recipe.level() + ".");
        } else {
            lore.add("&7Blacksmiths get better rolls here.");
        }
        Material icon = unlocked ? recipe.result() : Material.RED_STAINED_GLASS_PANE;
        String name = "&f" + plugin.formatMaterialName(recipe.result());
        return createSimpleItem(icon, name, lore);
    }

    private String getRecipeLockMessage(Player player, Testproject.BlacksmithRecipe recipe) {
        return plugin.colorize("&cYou cannot forge " + plugin.formatMaterialName(recipe.result()) + " right now.");
    }

    private boolean hasIngredients(Player player, LinkedHashMap<Material, Integer> ingredients) {
        for (Map.Entry<Material, Integer> entry : ingredients.entrySet()) {
            if (!player.getInventory().containsAtLeast(new ItemStack(entry.getKey()), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private void removeIngredients(Player player, LinkedHashMap<Material, Integer> ingredients) {
        for (Map.Entry<Material, Integer> entry : ingredients.entrySet()) {
            int remaining = entry.getValue();
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length && remaining > 0; i++) {
                ItemStack item = contents[i];
                if (item == null || item.getType() != entry.getKey()) {
                    continue;
                }
                int taken = Math.min(remaining, item.getAmount());
                item.setAmount(item.getAmount() - taken);
                remaining -= taken;
                if (item.getAmount() <= 0) {
                    contents[i] = null;
                }
            }
            player.getInventory().setContents(contents);
        }
    }

    private boolean isAnvil(Material material) {
        return material == Material.ANVIL
                || material == Material.CHIPPED_ANVIL
                || material == Material.DAMAGED_ANVIL;
    }

    private void fillForgeLayout(Inventory inventory) {
        ItemStack filler = createUiItem(Material.GRAY_STAINED_GLASS_PANE, "&7", "filler", List.of());
        ItemStack border = createUiItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "&7", "filler", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
        for (int slot : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 18, 27, 36, 45, 46, 47, 51, 53}) {
            inventory.setItem(slot, border);
        }
        for (int slot : new int[]{37, 38, 39, 40, 41, 42, 43, 44, 48, 49, 50, 52}) {
            inventory.setItem(slot, border);
        }
    }

    private void openVanillaAnvil(Player player) {
        Inventory anvilInventory = Bukkit.createInventory(player, InventoryType.ANVIL, plugin.legacyComponent("&8" + RENAME_ANVIL_TITLE));
        player.openInventory(anvilInventory);
    }

    private void handleRenameAnvilClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        int rawSlot = event.getRawSlot();
        if (rawSlot == 1) {
            event.setCancelled(true);
            return;
        }
        if (rawSlot == 2) {
            ItemStack extra = event.getView().getTopInventory().getItem(1);
            if (extra != null && !extra.getType().isAir()) {
                event.setCancelled(true);
            }
        }
    }

    private void openMergeMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(new MergeMenuHolder(), MERGE_GUI_SIZE, plugin.legacyComponent("&8Forge Merge"));
        fillMergeLayout(inventory);
        refreshMergeDisplay(inventory);
        inventory.setItem(MERGE_CLOSE_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close and return items.")));
        player.openInventory(inventory);
    }

    private void handleMergeMenuClick(InventoryClickEvent event, Player player) {
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            handlePlayerInventoryMergeClick(event);
            return;
        }

        int slot = event.getSlot();
        if (activeMergeAnimations.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (slot == MERGE_CLOSE_SLOT) {
            event.setCancelled(true);
            player.closeInventory();
            return;
        }
        if (slot == MERGE_ACTION_SLOT) {
            event.setCancelled(true);
            runMergeAttempt(player, event.getView().getTopInventory());
            return;
        }
        if (slot == MERGE_PREVIEW_SLOT) {
            event.setCancelled(true);
            return;
        }
        if (isMergeInputSlot(slot) || slot == MERGE_RARE_SLOT) {
            handleTopInventoryMergeClick(event, player);
            return;
        }
        event.setCancelled(true);
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

    private ItemStack createUiItem(Material material, String displayName, String marker, List<String> loreLines) {
        ItemStack itemStack = createSimpleItem(material, displayName, loreLines);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(mergeUiKey, PersistentDataType.STRING, marker.toLowerCase(Locale.ROOT));
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    private boolean isUiItem(ItemStack itemStack, String marker) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return false;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return false;
        }
        String stored = meta.getPersistentDataContainer().get(mergeUiKey, PersistentDataType.STRING);
        return stored != null && stored.equalsIgnoreCase(marker);
    }

    private interface BlacksmithInventoryHolder extends InventoryHolder {
    }

    private record BlacksmithMenuHolder(BlacksmithCategory category) implements BlacksmithInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record MergeMenuHolder() implements BlacksmithInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private enum BlacksmithCategory {
        BASICS("basics", 10, 1, Material.LEATHER_CHESTPLATE, "Basics"),
        STONE("stone", 19, 1, Material.STONE_PICKAXE, "Stone"),
        IRON("iron", 28, 3, Material.IRON_PICKAXE, "Iron"),
        GOLD("gold", 37, 4, Material.GOLDEN_CHESTPLATE, "Gold"),
        DIAMOND("diamond", 46, 7, Material.DIAMOND_PICKAXE, "Diamond"),
        NETHERITE("netherite", 47, 10, Material.NETHERITE_CHESTPLATE, "Netherite");

        private final String key;
        private final int slot;
        private final int requiredLevel;
        private final Material icon;
        private final String display;

        BlacksmithCategory(String key, int slot, int requiredLevel, Material icon, String display) {
            this.key = key;
            this.slot = slot;
            this.requiredLevel = requiredLevel;
            this.icon = icon;
            this.display = display;
        }

        private static BlacksmithCategory fromSlot(int slot) {
            for (BlacksmithCategory category : values()) {
                if (category.slot == slot) {
                    return category;
                }
            }
            return null;
        }

        private String displayName(int playerLevel) {
            return (playerLevel >= requiredLevel ? "&a" : "&c") + display;
        }
    }

    private void handlePlayerInventoryMergeClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType().isAir()) {
            return;
        }
        if (!event.isShiftClick()) {
            return;
        }
        event.setCancelled(true);
        Inventory top = event.getView().getTopInventory();
        if (plugin.isForgedItem(current) && findFirstEmptyMergeInput(top) >= 0) {
            moveSingleItem(event.getClickedInventory(), event.getSlot(), top, findFirstEmptyMergeInput(top));
            return;
        }
        if (plugin.getRareContractMaterialKey(current) != null && isEmptyMergeSlot(top.getItem(MERGE_RARE_SLOT))) {
            moveSingleItem(event.getClickedInventory(), event.getSlot(), top, MERGE_RARE_SLOT);
        }
    }

    private void handleTopInventoryMergeClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getSlot();
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (slot == MERGE_RARE_SLOT) {
            if (cursor == null || cursor.getType().isAir()) {
                takeTopInventoryItem(event, slot);
                refreshMergeDisplay(event.getView().getTopInventory());
                return;
            }
            if (plugin.getRareContractMaterialKey(cursor) == null || cursor.getAmount() != 1) {
                player.sendMessage(plugin.colorize("&cThe catalyst slot only accepts one optional forge material."));
                return;
            }
            if (!isEmptyMergeSlot(current)) {
                player.sendMessage(plugin.colorize("&cRemove the current catalyst first."));
                return;
            }
            placeSingleCursorItem(event, slot);
            refreshMergeDisplay(event.getView().getTopInventory());
            return;
        }
        if (!isMergeInputSlot(slot)) {
            return;
        }
        if (cursor == null || cursor.getType().isAir()) {
            takeTopInventoryItem(event, slot);
            refreshMergeDisplay(event.getView().getTopInventory());
            return;
        }
        if (!plugin.isForgedItem(cursor) || cursor.getAmount() != 1) {
            player.sendMessage(plugin.colorize("&cEach merge slot only accepts one forged item."));
            return;
        }
        if (!isEmptyMergeSlot(current)) {
            player.sendMessage(plugin.colorize("&cRemove the current merge input first."));
            return;
        }
        ItemStack existingType = getFirstMergeInput(event.getView().getTopInventory());
        if (existingType != null && !isSameMergeSignature(existingType, cursor)) {
            player.sendMessage(plugin.colorize("&cAll merge inputs must match item, tier, and rarity."));
            return;
        }
        placeSingleCursorItem(event, slot);
        refreshMergeDisplay(event.getView().getTopInventory());
    }

    private void runMergeAttempt(Player player, Inventory inventory) {
        if (!plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH)) {
            player.sendMessage(plugin.colorize("&cOnly blacksmiths can use the merge forge."));
            return;
        }
        if (activeMergeAnimations.containsKey(player.getUniqueId())) {
            return;
        }
        ItemStack[] inputs = getMergeInputs(inventory);
        if (inputs.length != 10) {
            player.sendMessage(plugin.colorize("&cYou need to place 10 forged copies into the merge grid."));
            return;
        }
        if (!allSameForgedType(inputs)) {
            player.sendMessage(plugin.colorize("&cAll 10 input items must match item, tier, and rarity."));
            return;
        }
        if (plugin.getForgedDisplayLevel(inputs[0]) >= 5) {
            player.sendMessage(plugin.colorize("&cThat item is already at Tier V."));
            return;
        }
        ItemStack catalyst = inventory.getItem(MERGE_RARE_SLOT);
        String rareKey = plugin.getRareContractMaterialKey(catalyst);
        if (!plugin.tryConsumeSharedActionCooldown(player, Profession.BLACKSMITH)) {
            return;
        }

        ItemStack baseItem = inputs[0].clone();
        double successChance = plugin.getForgeMergeSuccessChance(baseItem, rareKey);
        clearMergeInventory(inventory);
        setMergeActionBusy(inventory);
        playMergeAnimation(player, inventory, baseItem, rareKey, successChance);
    }

    private ItemStack[] getMergeInputs(Inventory inventory) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int slot : MERGE_INPUT_SLOTS) {
            ItemStack itemStack = inventory.getItem(slot);
            if (plugin.isForgedItem(itemStack)) {
                inputs.add(itemStack);
            }
        }
        return inputs.toArray(ItemStack[]::new);
    }

    private boolean allSameForgedType(ItemStack[] inputs) {
        if (inputs.length == 0) {
            return false;
        }
        ItemStack first = inputs[0];
        for (ItemStack itemStack : inputs) {
            if (!plugin.isForgedItem(itemStack) || itemStack.getAmount() != 1 || !isSameMergeSignature(first, itemStack)) {
                return false;
            }
        }
        return true;
    }

    private void clearMergeInventory(Inventory inventory) {
        for (int slot : MERGE_INPUT_SLOTS) {
            inventory.setItem(slot, createMergeInputPlaceholder());
        }
        inventory.setItem(MERGE_RARE_SLOT, createCatalystPlaceholder());
    }

    private void returnMergeInputs(Player player, Inventory inventory) {
        for (int slot : MERGE_INPUT_SLOTS) {
            returnMergeItem(player, inventory, slot);
        }
        returnMergeItem(player, inventory, MERGE_RARE_SLOT);
    }

    private void returnMergeItem(Player player, Inventory inventory, int slot) {
        ItemStack itemStack = inventory.getItem(slot);
        if (itemStack == null || itemStack.getType().isAir() || isEmptyMergeSlot(itemStack)) {
            return;
        }
        inventory.setItem(slot, null);
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(itemStack);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }

    private int findFirstEmptyMergeInput(Inventory inventory) {
        for (int slot : MERGE_INPUT_SLOTS) {
            if (isEmptyMergeSlot(inventory.getItem(slot))) {
                return slot;
            }
        }
        return -1;
    }

    private ItemStack getFirstMergeInput(Inventory inventory) {
        for (int slot : MERGE_INPUT_SLOTS) {
            ItemStack itemStack = inventory.getItem(slot);
            if (plugin.isForgedItem(itemStack)) {
                return itemStack;
            }
        }
        return null;
    }

    private boolean isMergeInputSlot(int slot) {
        for (int mergeSlot : MERGE_INPUT_SLOTS) {
            if (mergeSlot == slot) {
                return true;
            }
        }
        return false;
    }

    private void moveSingleItem(Inventory source, int sourceSlot, Inventory target, int targetSlot) {
        ItemStack sourceItem = source.getItem(sourceSlot);
        if (sourceItem == null || sourceItem.getType().isAir()) {
            return;
        }
        ItemStack moved = sourceItem.clone();
        moved.setAmount(1);
        target.setItem(targetSlot, moved);
        sourceItem.setAmount(sourceItem.getAmount() - 1);
        if (sourceItem.getAmount() <= 0) {
            source.setItem(sourceSlot, null);
        } else {
            source.setItem(sourceSlot, sourceItem);
        }
        refreshMergeDisplay(target);
    }

    private void fillMergeLayout(Inventory inventory) {
        ItemStack filler = createUiItem(Material.GRAY_STAINED_GLASS_PANE, "&7", "filler", List.of());
        ItemStack border = createUiItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "&7", "filler", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
        for (int slot : new int[]{0, 1, 7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 39, 40, 41, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52}) {
            inventory.setItem(slot, border);
        }
        inventory.setItem(2, createUiItem(Material.WOODEN_PICKAXE, "&8Merge Slot", "input_placeholder", List.of("&7Place a forged item here.")));
        inventory.setItem(3, createUiItem(Material.STONE_PICKAXE, "&8Merge Slot", "input_placeholder", List.of("&7Place a forged item here.")));
        inventory.setItem(4, createUiItem(Material.IRON_PICKAXE, "&8Merge Slot", "input_placeholder", List.of("&7Place a forged item here.")));
        inventory.setItem(5, createUiItem(Material.GOLDEN_PICKAXE, "&8Merge Slot", "input_placeholder", List.of("&7Place a forged item here.")));
        inventory.setItem(6, createUiItem(Material.DIAMOND_PICKAXE, "&8Merge Slot", "input_placeholder", List.of("&7Place a forged item here.")));
        inventory.setItem(20, createUiItem(Material.WOODEN_SWORD, "&8Merge Slot", "input_placeholder", List.of("&7Place a forged item here.")));
        inventory.setItem(21, createUiItem(Material.WOODEN_PICKAXE, "&8Merge Slot", "input_placeholder", List.of("&7Place a forged item here.")));
        inventory.setItem(22, createUiItem(Material.WOODEN_SHOVEL, "&8Merge Slot", "input_placeholder", List.of("&7Place a forged item here.")));
        inventory.setItem(23, createUiItem(Material.WOODEN_AXE, "&8Merge Slot", "input_placeholder", List.of("&7Place a forged item here.")));
        inventory.setItem(24, createUiItem(Material.WOODEN_HOE, "&8Merge Slot", "input_placeholder", List.of("&7Place a forged item here.")));
        inventory.setItem(29, createUiItem(Material.LEATHER_BOOTS, "&8Tier Shell", "filler", List.of()));
        inventory.setItem(30, createUiItem(Material.LEATHER_LEGGINGS, "&8Tier Shell", "filler", List.of()));
        inventory.setItem(31, createUiItem(Material.LEATHER_CHESTPLATE, "&6Merge Preview", "preview_placeholder", List.of("&7Load the rack to preview the result.")));
        inventory.setItem(32, createUiItem(Material.LEATHER_HELMET, "&8Tier Shell", "filler", List.of()));
    }

    private void refreshMergeDisplay(Inventory inventory) {
        if (inventory == null) {
            return;
        }
        for (int slot : MERGE_INPUT_SLOTS) {
            if (isEmptyMergeSlot(inventory.getItem(slot))) {
                inventory.setItem(slot, createMergeInputPlaceholder());
            }
        }
        if (isEmptyMergeSlot(inventory.getItem(MERGE_RARE_SLOT))) {
            inventory.setItem(MERGE_RARE_SLOT, createCatalystPlaceholder());
        }
        ItemStack[] inputs = getMergeInputs(inventory);
        ItemStack catalyst = inventory.getItem(MERGE_RARE_SLOT);
        String rareKey = plugin.getRareContractMaterialKey(catalyst);
        if (inputs.length == 0) {
            inventory.setItem(MERGE_PREVIEW_SLOT, createUiItem(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, "&6Merge Preview", "preview_placeholder", List.of(
                    "&7Top slot shows the forged result",
                    "&7once the rack is loaded."
            )));
            inventory.setItem(MERGE_PREVIEW_SLOT, createUiItem(Material.LEATHER_CHESTPLATE, "&6Merge Preview", "preview_placeholder", List.of(
                    "&7Load the rack to preview",
                    "&7the merged result."
            )));
            inventory.setItem(MERGE_ACTION_SLOT, createSimpleItem(Material.ANVIL, "&7Merge", List.of(
                    "&7Chance: &f--",
                    "&7Catalyst: &fNone",
                    "&7Load 10 matching items."
            )));
            return;
        }

        ItemStack first = inputs[0];
        if (!allSameForgedType(inputs)) {
            inventory.setItem(MERGE_PREVIEW_SLOT, createSimpleItem(Material.BARRIER, "&cInvalid Merge", List.of(
                    "&7All 10 inputs must match",
                    "&7item, rarity, and tier."
            )));
            inventory.setItem(MERGE_ACTION_SLOT, createSimpleItem(Material.ANVIL, "&7Merge", List.of(
                    "&7Chance: &f0%",
                    "&7Fix the merge inputs first."
            )));
            return;
        }

        double chance = plugin.getForgeMergeSuccessChance(first, rareKey);
        ItemStack preview = first.clone();
        preview.setAmount(1);
        preview.editMeta(meta -> meta.lore(List.of(
                plugin.legacyComponent("&7Current Tier: &f" + plugin.toRomanNumeral(plugin.getForgedDisplayLevel(first))),
                plugin.legacyComponent("&7Next Tier: &f" + plugin.toRomanNumeral(Math.min(5, plugin.getForgedDisplayLevel(first) + 1))),
                plugin.legacyComponent("&7Rarity: &f" + plugin.getForgedItemRarity(first).getDisplayName()),
                plugin.legacyComponent("&7Success: &f" + formatPercent(chance))
        )));
        inventory.setItem(MERGE_PREVIEW_SLOT, preview);
        inventory.setItem(MERGE_ACTION_SLOT, createSimpleItem(Material.ANVIL, "&6Merge", List.of(
                "&7Roll the station now.",
                "&7Current chance: &f" + formatPercent(chance),
                "&7Catalyst: &f" + (rareKey != null ? plugin.formatRareContractMaterialName(rareKey) : "None"),
                "&cFailure destroys all inputs."
        )));
    }

    private boolean isEmptyMergeSlot(ItemStack itemStack) {
        return itemStack == null
                || itemStack.getType().isAir()
                || isUiItem(itemStack, "input_placeholder")
                || isUiItem(itemStack, "catalyst_placeholder");
    }

    private boolean isSameMergeSignature(ItemStack first, ItemStack second) {
        return first != null
                && second != null
                && plugin.isForgedItem(first)
                && plugin.isForgedItem(second)
                && first.getType() == second.getType()
                && plugin.getForgedDisplayLevel(first) == plugin.getForgedDisplayLevel(second)
                && plugin.getForgedItemRarity(first) == plugin.getForgedItemRarity(second);
    }

    private void takeTopInventoryItem(InventoryClickEvent event, int slot) {
        ItemStack current = event.getView().getTopInventory().getItem(slot);
        if (current == null || current.getType().isAir() || isEmptyMergeSlot(current)) {
            return;
        }
        if (event.getCursor() != null && !event.getCursor().getType().isAir()) {
            return;
        }
        event.getView().setCursor(current.clone());
        if (slot == MERGE_RARE_SLOT) {
            event.getView().getTopInventory().setItem(slot, createCatalystPlaceholder());
        } else {
            event.getView().getTopInventory().setItem(slot, createMergeInputPlaceholder());
        }
    }

    private void placeSingleCursorItem(InventoryClickEvent event, int slot) {
        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType().isAir()) {
            return;
        }
        ItemStack placed = cursor.clone();
        placed.setAmount(1);
        event.getView().getTopInventory().setItem(slot, placed);
        cursor.setAmount(cursor.getAmount() - 1);
        if (cursor.getAmount() <= 0) {
            event.getView().setCursor(null);
        } else {
            event.getView().setCursor(cursor);
        }
    }

    private ItemStack createMergeInputPlaceholder() {
        return createUiItem(Material.WOODEN_PICKAXE, "&8Merge Slot", "input_placeholder", List.of(
                "&7Place one forged item here."
        ));
    }

    private ItemStack createCatalystPlaceholder() {
        return createUiItem(Material.AMETHYST_SHARD, "&dCatalyst Slot", "catalyst_placeholder", List.of(
                "&7Optional merge booster.",
                "&7Place catalysts here."
        ));
    }

    private void setMergeActionBusy(Inventory inventory) {
        inventory.setItem(MERGE_ACTION_SLOT, createSimpleItem(Material.ANVIL, "&6Merging...", List.of(
                "&7The forge is rolling the merge.",
                "&7Wait for the result."
        )));
    }

    private void playMergeAnimation(Player player, Inventory inventory, ItemStack baseItem, String rareKey, double successChance) {
        UUID playerId = player.getUniqueId();
        final int[] step = {0};
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (step[0] >= 12) {
                BukkitTask active = activeMergeAnimations.remove(playerId);
                if (active != null) {
                    active.cancel();
                }
                finishMergeAnimation(player, inventory, baseItem, rareKey);
                return;
            }

            Material frameMaterial = switch (step[0] % 4) {
                case 0 -> Material.WOODEN_PICKAXE;
                case 1 -> Material.STONE_PICKAXE;
                case 2 -> Material.IRON_PICKAXE;
                default -> Material.DIAMOND_PICKAXE;
            };
            inventory.setItem(MERGE_PREVIEW_SLOT, createSimpleItem(baseItem.getType(), "&6Rolling Merge", List.of(
                    "&7Chance: &f" + formatPercent(successChance),
                    "&7Tier target: &f" + plugin.toRomanNumeral(Math.min(5, plugin.getForgedDisplayLevel(baseItem) + 1)),
                    "&7Hold steady..."
            )));
            inventory.setItem(MERGE_ACTION_SLOT, createSimpleItem(Material.ANVIL, "&eForge Roll", List.of(
                    "&7Chance Locked: &f" + formatPercent(successChance),
                    "&7Catalyst: &f" + (rareKey != null ? plugin.formatRareContractMaterialName(rareKey) : "None"),
                    "&7The station is rolling..."
            )));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.45F, 0.8F + (step[0] * 0.04F));
            step[0]++;
        }, 0L, 2L);
        activeMergeAnimations.put(playerId, task);
    }

    private void finishMergeAnimation(Player player, Inventory inventory, ItemStack baseItem, String rareKey) {
        Testproject.ForgeMergeOutcome outcome = plugin.attemptForgeMergeFromInputs(baseItem, rareKey);
        clearMergeInventory(inventory);
        refreshMergeDisplay(inventory);
        if (outcome == Testproject.ForgeMergeOutcome.SUCCESS) {
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(baseItem);
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
            inventory.setItem(MERGE_PREVIEW_SLOT, baseItem);
            inventory.setItem(MERGE_ACTION_SLOT, createSimpleItem(Material.ANVIL, "&aMerge Success", List.of(
                    "&7Your forged item advanced",
                    "&7to the next tier."
            )));
            player.sendMessage(plugin.colorize("&6Merge success. &aOne forged item advanced to a higher tier."));
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 1.1F);
        } else {
            inventory.setItem(MERGE_PREVIEW_SLOT, createSimpleItem(Material.BARRIER, "&cMerge Failed", List.of(
                    "&7The forge rejected the merge."
            )));
            inventory.setItem(MERGE_ACTION_SLOT, createSimpleItem(Material.ANVIL, "&cMerge Failed", List.of(
                    "&7All merge inputs were destroyed.",
                    "&7Try again with stronger odds."
            )));
            player.sendMessage(plugin.colorize("&cMerge failed. &7All forged items" + (rareKey != null ? " and the catalyst" : "") + " were destroyed."));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.9F, 0.8F);
        }
    }

    private String formatPercent(double value) {
        return (int) Math.round(value * 100.0D) + "%";
    }
}
