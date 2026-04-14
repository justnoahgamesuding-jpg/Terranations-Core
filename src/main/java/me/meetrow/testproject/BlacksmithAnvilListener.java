package me.meetrow.testproject;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BlacksmithAnvilListener implements Listener {
    private static final int GUI_SIZE = 54;
    private static final int INFO_SLOT = 4;
    private static final int NORMAL_ANVIL_SLOT = 48;
    private static final int CLOSE_SLOT = 50;
    private static final int MERGE_SLOT = 52;
    private static final int CATEGORY_INFO_SLOT = 49;
    private static final int[] MERGE_INPUT_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
    private static final int MERGE_RARE_SLOT = 23;
    private static final int MERGE_ACTION_SLOT = 25;
    private static final int MERGE_BACK_SLOT = 26;
    private static final int[] RECIPE_SLOTS = {
            11, 12, 13, 14, 15, 16, 17,
            20, 21, 22, 23, 24, 25, 26,
            29, 30, 31, 32, 33, 34, 35
    };

    private final Testproject plugin;

    public BlacksmithAnvilListener(Testproject plugin) {
        this.plugin = plugin;
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

        BlacksmithCategory category = BlacksmithCategory.fromSlot(event.getSlot());
        if (category != null) {
            openMenu(player, category);
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
        if (event.getView().getTopInventory().getHolder() instanceof BlacksmithMenuHolder) {
            event.setCancelled(true);
            return;
        }
        if (!(event.getView().getTopInventory().getHolder() instanceof MergeMenuHolder)) {
            return;
        }
        for (int rawSlot : event.getRawSlots()) {
            if (!isMergeInputSlot(rawSlot) && rawSlot != MERGE_RARE_SLOT) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof MergeMenuHolder)) {
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

        fillBaseLayout(inventory);
        for (BlacksmithCategory value : BlacksmithCategory.values()) {
            inventory.setItem(value.slot, createCategoryItem(player, value, value == category));
        }
        inventory.setItem(INFO_SLOT, createInfoItem(player, category));
        inventory.setItem(CATEGORY_INFO_SLOT, createSimpleItem(Material.CHAIN, "&6Forge Controls", List.of(
                "&7Left side: forge tiers",
                "&7Center: recipe board",
                "&7Bottom: utility actions"
        )));
        inventory.setItem(NORMAL_ANVIL_SLOT, createSimpleItem(Material.ANVIL, "&bUse Normal Anvil", List.of(
                "&7Open a normal anvil screen for",
                "&7repairing, renaming, or combining."
        )));
        inventory.setItem(MERGE_SLOT, createSimpleItem(Material.NETHER_STAR, "&6Forge Merge", List.of(
                "&7Blacksmith-only upgrade forge.",
                "&7Consume 10 forged copies and",
                "&71 rare material for one risky",
                "&7tier upgrade attempt."
        )));
        inventory.setItem(CLOSE_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close the forge menu.")));

        List<Testproject.BlacksmithRecipe> recipes = getRecipes(category);
        for (int i = 0; i < RECIPE_SLOTS.length && i < recipes.size(); i++) {
            inventory.setItem(RECIPE_SLOTS[i], createRecipeItem(player, recipes.get(i)));
        }

        player.openInventory(inventory);
    }

    private List<Testproject.BlacksmithRecipe> getRecipes(BlacksmithCategory category) {
        List<Testproject.BlacksmithRecipe> recipes = new ArrayList<>();
        for (Testproject.BlacksmithRecipe recipe : plugin.getBlacksmithAnvilRecipes()) {
            if (recipe.category().equalsIgnoreCase(category.key)) {
                recipes.add(recipe);
            }
        }
        recipes.sort((left, right) -> Integer.compare(left.slot(), right.slot()));
        return recipes;
    }

    private Testproject.BlacksmithRecipe getRecipeForSlot(BlacksmithCategory category, int slot) {
        List<Testproject.BlacksmithRecipe> recipes = getRecipes(category);
        for (int i = 0; i < RECIPE_SLOTS.length && i < recipes.size(); i++) {
            if (RECIPE_SLOTS[i] == slot) {
                return recipes.get(i);
            }
        }
        return null;
    }

    private ItemStack createInfoItem(Player player, BlacksmithCategory category) {
        boolean blacksmith = plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH);
        int level = blacksmith ? plugin.getProfessionLevel(player.getUniqueId(), Profession.BLACKSMITH) : 0;
        return createSimpleItem(Material.BLAST_FURNACE, "&6Blacksmith Forge", List.of(
                "&7Tier board: &f" + category.display,
                "&7Forge access: &f" + (blacksmith ? "Blacksmith Lv." + level : "Public Smithing"),
                "",
                "&7Everyone can forge every listed recipe.",
                "&7Blacksmiths roll stronger tiers,",
                "&7better durability, and better perks.",
                "&7Materials are taken directly from",
                "&7your inventory."
        ));
    }

    private ItemStack createCategoryItem(Player player, BlacksmithCategory category, boolean selected) {
        int level = plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH)
                ? plugin.getProfessionLevel(player.getUniqueId(), Profession.BLACKSMITH)
                : 0;
        List<String> lore = new ArrayList<>();
        lore.add("&7Recipe tier: &f" + category.display);
        lore.add("&7Access: &aOpen to everyone");
        lore.add(level >= category.requiredLevel
                ? "&aBlacksmith bonus tier active"
                : "&7Blacksmith bonus starts at Lv." + category.requiredLevel);
        lore.add("");
        lore.add(selected ? "&eCurrently selected." : "&7Click to view recipes.");
        Material material = selected ? Material.ORANGE_STAINED_GLASS_PANE : category.icon;
        return createSimpleItem(material, category.displayName(level), lore);
    }

    private ItemStack createRecipeItem(Player player, Testproject.BlacksmithRecipe recipe) {
        boolean unlocked = plugin.canCraftBlacksmithRecipe(player.getUniqueId(), recipe);
        boolean blacksmith = plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH);
        int level = blacksmith ? plugin.getProfessionLevel(player.getUniqueId(), Profession.BLACKSMITH) : 0;
        List<String> lore = new ArrayList<>();
        lore.add("&7Required level: &f" + recipe.level());
        lore.add("&7Craft XP: &f" + recipe.xp());
        if (plugin.isForgeManagedEquipment(recipe.result())) {
            lore.add("&7Result: &fRolled tier + rarity");
        }
        lore.add("");
        lore.add("&eIngredients:");
        for (Map.Entry<Material, Integer> entry : recipe.ingredients().entrySet()) {
            lore.add("&7- &f" + entry.getValue() + "x " + plugin.formatMaterialName(entry.getKey()));
        }
        lore.add("");
        if (blacksmith) {
            lore.add(level >= recipe.level()
                    ? "&aBlacksmith bonus quality active."
                    : "&7Better rolls unlock at Blacksmith Lv." + recipe.level() + ".");
        } else {
            lore.add("&7Blacksmiths get better rolls on this recipe.");
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

    private void fillBaseLayout(Inventory inventory) {
        ItemStack filler = createSimpleItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());
        ItemStack border = createSimpleItem(Material.BLACK_STAINED_GLASS_PANE, "&8", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
        for (int slot : new int[]{0, 1, 2, 3, 5, 6, 7, 8, 9, 18, 27, 36, 45, 46, 47, 51, 52, 53}) {
            inventory.setItem(slot, border);
        }
        for (int slot : new int[]{10, 19, 28, 37, 38, 39, 40, 41, 42, 43, 44}) {
            inventory.setItem(slot, createSimpleItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "&7", List.of()));
        }
    }

    private void openVanillaAnvil(Player player) {
        Inventory anvilInventory = Bukkit.createInventory(player, InventoryType.ANVIL, plugin.legacyComponent("&8Anvil"));
        player.openInventory(anvilInventory);
    }

    private void openMergeMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(new MergeMenuHolder(), 27, plugin.legacyComponent("&8Forge Merge"));
        ItemStack filler = createSimpleItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
        for (int slot : MERGE_INPUT_SLOTS) {
            inventory.setItem(slot, null);
        }
        inventory.setItem(MERGE_RARE_SLOT, null);
        inventory.setItem(12, createSimpleItem(Material.ANVIL, "&7Merge Rules", List.of(
                "&710 forged copies of the same item",
                "&71 rare material",
                "&7Put the 10 forged items into the slots",
                "&7Place 1 rare material in the catalyst slot",
                "&7Success upgrades one result by 1 tier",
                "&cFailure destroys all inputs"
        )));
        inventory.setItem(18, createSimpleItem(Material.BOOK, "&7Status", List.of(
                plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH) ? "&aBlacksmith access active" : "&cBlacksmith only",
                "&7Input slots: &f10 forged copies",
                "&7Catalyst slot: &f1 rare material"
        )));
        inventory.setItem(22, createSimpleItem(Material.NETHER_STAR, "&dCatalyst Slot", List.of(
                "&7Place one forge material here:",
                "&7Forge Shard, Tempered Flux,",
                "&7Binding Thread, Runic Prism,",
                "&7or Ancient Core."
        )));
        inventory.setItem(MERGE_ACTION_SLOT, createSimpleItem(Material.ANVIL, "&6Merge", List.of(
                "&7Attempt the forge merge.",
                "&cFailure destroys all inputs."
        )));
        inventory.setItem(MERGE_BACK_SLOT, createSimpleItem(Material.ARROW, "&7Back", List.of("&7Return to the forge board.")));
        player.openInventory(inventory);
    }

    private void handleMergeMenuClick(InventoryClickEvent event, Player player) {
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            handlePlayerInventoryMergeClick(event, player);
            return;
        }

        int slot = event.getSlot();
        if (slot == MERGE_BACK_SLOT) {
            event.setCancelled(true);
            returnMergeInputs(player, event.getView().getTopInventory());
            openMenu(player, BlacksmithCategory.BASICS);
            return;
        }
        if (slot == MERGE_ACTION_SLOT) {
            event.setCancelled(true);
            runMergeAttempt(player, event.getView().getTopInventory());
            return;
        }
        if (slot == 12 || slot == 18 || slot == 22) {
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

    private void handlePlayerInventoryMergeClick(InventoryClickEvent event, Player player) {
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType().isAir()) {
            return;
        }
        if (event.isShiftClick()) {
            event.setCancelled(true);
            Inventory top = event.getView().getTopInventory();
            if (plugin.isForgedItem(current) && findFirstEmptyMergeInput(top) >= 0) {
                moveSingleItem(event.getClickedInventory(), event.getSlot(), top, findFirstEmptyMergeInput(top));
                return;
            }
            if (plugin.getRareContractMaterialKey(current) != null && top.getItem(MERGE_RARE_SLOT) == null) {
                moveSingleItem(event.getClickedInventory(), event.getSlot(), top, MERGE_RARE_SLOT);
            }
        }
    }

    private void handleTopInventoryMergeClick(InventoryClickEvent event, Player player) {
        int slot = event.getSlot();
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();
        if (slot == MERGE_RARE_SLOT) {
            if (cursor != null && !cursor.getType().isAir()) {
                if (plugin.getRareContractMaterialKey(cursor) == null || cursor.getAmount() != 1) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.colorize("&cPlace exactly one rare forge material in the catalyst slot."));
                    return;
                }
            }
            return;
        }
        if (!isMergeInputSlot(slot)) {
            event.setCancelled(true);
            return;
        }
        if (cursor != null && !cursor.getType().isAir()) {
            if (!plugin.isForgedItem(cursor) || cursor.getAmount() != 1) {
                event.setCancelled(true);
                player.sendMessage(plugin.colorize("&cEach merge input slot only accepts one forged item."));
                return;
            }
            ItemStack existingType = getFirstMergeInput(event.getView().getTopInventory());
            if (existingType != null && existingType.getType() != cursor.getType()) {
                event.setCancelled(true);
                player.sendMessage(plugin.colorize("&cAll forge merge inputs must be the same item type."));
            }
            return;
        }
        if (current != null && !current.getType().isAir()) {
            return;
        }
    }

    private void runMergeAttempt(Player player, Inventory inventory) {
        if (!plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH)) {
            player.sendMessage(plugin.colorize("&cOnly blacksmiths can use the merge forge."));
            return;
        }
        ItemStack[] inputs = getMergeInputs(inventory);
        if (inputs.length != 10) {
            player.sendMessage(plugin.colorize("&cYou need to place 10 forged copies into the merge grid."));
            return;
        }
        if (!allSameForgedType(inputs)) {
            player.sendMessage(plugin.colorize("&cAll 10 input items must be the same forged item type."));
            return;
        }
        if (plugin.getForgedDisplayLevel(inputs[0]) >= 5) {
            player.sendMessage(plugin.colorize("&cThat item is already at Tier V."));
            return;
        }
        ItemStack catalyst = inventory.getItem(MERGE_RARE_SLOT);
        String rareKey = plugin.getRareContractMaterialKey(catalyst);
        if (rareKey == null) {
            player.sendMessage(plugin.colorize("&cPlace one rare forge material in the catalyst slot."));
            return;
        }
        if (!plugin.tryConsumeSharedActionCooldown(player, Profession.BLACKSMITH)) {
            return;
        }

        Testproject.ForgeMergeOutcome outcome = plugin.attemptForgeMergeFromInputs(inputs[0], rareKey);
        clearMergeInventory(inventory);
        if (outcome == Testproject.ForgeMergeOutcome.SUCCESS) {
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(inputs[0]);
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
            player.sendMessage(plugin.colorize("&6Merge success. &aOne forged item advanced to a higher tier."));
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 1.1F);
        } else {
            player.sendMessage(plugin.colorize("&cMerge failed. &7All forged items and the catalyst were destroyed."));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.9F, 0.8F);
        }
        openMergeMenu(player);
    }

    private ItemStack[] getMergeInputs(Inventory inventory) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int slot : MERGE_INPUT_SLOTS) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack != null && !itemStack.getType().isAir()) {
                inputs.add(itemStack);
            }
        }
        return inputs.toArray(ItemStack[]::new);
    }

    private boolean allSameForgedType(ItemStack[] inputs) {
        if (inputs.length == 0) {
            return false;
        }
        Material type = inputs[0].getType();
        for (ItemStack itemStack : inputs) {
            if (!plugin.isForgedItem(itemStack) || itemStack.getAmount() != 1 || itemStack.getType() != type) {
                return false;
            }
        }
        return true;
    }

    private void clearMergeInventory(Inventory inventory) {
        for (int slot : MERGE_INPUT_SLOTS) {
            inventory.setItem(slot, null);
        }
        inventory.setItem(MERGE_RARE_SLOT, null);
    }

    private void returnMergeInputs(Player player, Inventory inventory) {
        for (int slot : MERGE_INPUT_SLOTS) {
            returnMergeItem(player, inventory, slot);
        }
        returnMergeItem(player, inventory, MERGE_RARE_SLOT);
    }

    private void returnMergeItem(Player player, Inventory inventory, int slot) {
        ItemStack itemStack = inventory.getItem(slot);
        if (itemStack == null || itemStack.getType().isAir()) {
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
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack == null || itemStack.getType().isAir()) {
                return slot;
            }
        }
        return -1;
    }

    private ItemStack getFirstMergeInput(Inventory inventory) {
        for (int slot : MERGE_INPUT_SLOTS) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack != null && !itemStack.getType().isAir()) {
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
    }
}
