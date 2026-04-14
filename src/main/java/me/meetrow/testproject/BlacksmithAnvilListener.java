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
    private static final int FORGE_STATUS_SLOT = 7;
    private static final int NORMAL_ANVIL_SLOT = 48;
    private static final int CLOSE_SLOT = 50;
    private static final int MERGE_SLOT = 52;
    private static final int CATEGORY_INFO_SLOT = 49;
    private static final int MERGE_GUI_SIZE = 54;
    private static final int[] MERGE_INPUT_SLOTS = {19, 20, 21, 22, 23, 24, 28, 29, 30, 31};
    private static final int MERGE_RARE_SLOT = 25;
    private static final int MERGE_PREVIEW_SLOT = 33;
    private static final int MERGE_ACTION_SLOT = 49;
    private static final int MERGE_BACK_SLOT = 45;
    private static final int MERGE_CLOSE_SLOT = 53;
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

        fillForgeLayout(inventory);
        for (BlacksmithCategory value : BlacksmithCategory.values()) {
            inventory.setItem(value.slot, createCategoryItem(player, value, value == category));
        }
        inventory.setItem(INFO_SLOT, createInfoItem(player, category));
        inventory.setItem(FORGE_STATUS_SLOT, createForgeStatusItem(player));
        inventory.setItem(CATEGORY_INFO_SLOT, createSimpleItem(Material.CHAIN, "&6Forge Controls", List.of(
                "&7Top row: recipe categories",
                "&7Center: available forge recipes",
                "&7Bottom: merge, anvil, and exit"
        )));
        inventory.setItem(NORMAL_ANVIL_SLOT, createSimpleItem(Material.ANVIL, "&bUse Normal Anvil", List.of(
                "&7Open vanilla repair and rename.",
                "&7This skips the custom forge flow."
        )));
        inventory.setItem(MERGE_SLOT, createSimpleItem(Material.NETHER_STAR, "&6Forge Merge", List.of(
                "&7Open the upgrade workstation.",
                "&7Use forged duplicates to risk",
                "&7a higher tier result.",
                "&7Catalysts are optional and improve odds."
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
                "&7Selected Category: &f" + category.display,
                "&7Forge Access: &f" + (blacksmith ? "Blacksmith Lv." + level : "Public Smithing"),
                "",
                "&7Everyone can craft the recipes shown.",
                "&7Blacksmiths get faster forge actions,",
                "&7better rarity rolls, and better tiers."
        ));
    }

    private ItemStack createForgeStatusItem(Player player) {
        boolean blacksmith = plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH);
        int level = blacksmith ? plugin.getProfessionLevel(player.getUniqueId(), Profession.BLACKSMITH) : 0;
        int cooldown = plugin.getActionCooldownSeconds(player.getUniqueId(), blacksmith ? Profession.BLACKSMITH : null);
        return createSimpleItem(Material.SMITHING_TABLE, "&eForge Status", List.of(
                "&7Role: &f" + (blacksmith ? "Blacksmith" : "Crafter"),
                "&7Level: &f" + (blacksmith ? level : "-"),
                "&7Action Cooldown: &f" + cooldown + "s",
                "",
                "&7Blacksmith bonuses affect:",
                "&7- forge speed",
                "&7- forged tier chance",
                "&7- forged rarity chance"
        ));
    }

    private ItemStack createCategoryItem(Player player, BlacksmithCategory category, boolean selected) {
        int level = plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH)
                ? plugin.getProfessionLevel(player.getUniqueId(), Profession.BLACKSMITH)
                : 0;
        List<String> lore = new ArrayList<>();
        lore.add("&7Recipe Group: &f" + category.display);
        lore.add("&7Access: &aOpen to everyone");
        lore.add(level >= category.requiredLevel
                ? "&aBonus quality active"
                : "&7Bonus quality starts at Lv." + category.requiredLevel);
        lore.add("");
        lore.add(selected ? "&eCurrently selected." : "&7Click to show these recipes.");
        Material material = selected ? Material.ORANGE_STAINED_GLASS_PANE : category.icon;
        return createSimpleItem(material, category.displayName(level), lore);
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
            lore.add("&8• &f" + entry.getValue() + "x " + plugin.formatMaterialName(entry.getKey()));
        }
        lore.add("");
        if (blacksmith) {
            lore.add(level >= recipe.level()
                    ? "&aBlacksmith quality bonus active."
                    : "&7Better rolls unlock at Blacksmith Lv." + recipe.level() + ".");
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
        ItemStack filler = createSimpleItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());
        ItemStack border = createSimpleItem(Material.BLACK_STAINED_GLASS_PANE, "&8", List.of());
        ItemStack panel = createSimpleItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "&7", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
        for (int slot : new int[]{0, 1, 2, 3, 5, 6, 8, 9, 18, 27, 36, 45, 46, 47, 51, 53}) {
            inventory.setItem(slot, border);
        }
        for (int slot : new int[]{10, 19, 28, 37, 38, 39, 40, 41, 42, 43, 44}) {
            inventory.setItem(slot, panel);
        }
    }

    private void openVanillaAnvil(Player player) {
        Inventory anvilInventory = Bukkit.createInventory(player, InventoryType.ANVIL, plugin.legacyComponent("&8Anvil"));
        player.openInventory(anvilInventory);
    }

    private void openMergeMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(new MergeMenuHolder(), MERGE_GUI_SIZE, plugin.legacyComponent("&8Forge Merge"));
        fillMergeLayout(inventory);
        for (int slot : MERGE_INPUT_SLOTS) {
            inventory.setItem(slot, null);
        }
        inventory.setItem(MERGE_RARE_SLOT, null);
        inventory.setItem(4, createSimpleItem(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, "&6Forge Merge Workstation", List.of(
                "&710 forged duplicates",
                "&70-1 catalyst material",
                "&7One result upgrades on success",
                "&cAll inputs destroyed on failure"
        )));
        inventory.setItem(7, createSimpleItem(Material.SMITHING_TABLE, "&eMerge Status", List.of(
                plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH) ? "&aBlacksmith access active" : "&cBlacksmith only",
                "&7Place the forged duplicates in the",
                "&7central grid.",
                "&7Catalyst slot is optional."
        )));
        inventory.setItem(16, createSimpleItem(Material.AMETHYST_SHARD, "&dCatalyst", List.of(
                "&7Optional merge booster.",
                "&7Accepted materials:",
                "&8- &fForge Shard",
                "&8- &fTempered Flux",
                "&8- &fBinding Thread",
                "&8- &fRunic Prism",
                "&8- &fAncient Core"
        )));
        inventory.setItem(MERGE_PREVIEW_SLOT, createSimpleItem(Material.ANVIL, "&7Result Preview", List.of(
                "&7After a successful merge, one item",
                "&7returns at the next tier."
        )));
        inventory.setItem(MERGE_ACTION_SLOT, createSimpleItem(Material.ANVIL, "&6Merge", List.of(
                "&7Attempt the forge merge now.",
                "&cFailure destroys all inputs."
        )));
        inventory.setItem(MERGE_BACK_SLOT, createSimpleItem(Material.ARROW, "&7Back", List.of("&7Return to the forge board.")));
        inventory.setItem(MERGE_CLOSE_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close and return items.")));
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
        if (slot == MERGE_CLOSE_SLOT) {
            event.setCancelled(true);
            player.closeInventory();
            return;
        }
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
        if (slot == 4 || slot == 7 || slot == 16 || slot == MERGE_PREVIEW_SLOT) {
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
                    player.sendMessage(plugin.colorize("&cThe catalyst slot only accepts one optional forge material."));
                    return;
                }
            }
            updateMergePreview(event.getView().getTopInventory());
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
            updateMergePreview(event.getView().getTopInventory());
            return;
        }
        if (current != null && !current.getType().isAir()) {
            updateMergePreview(event.getView().getTopInventory());
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
        if (!plugin.tryConsumeSharedActionCooldown(player, Profession.BLACKSMITH)) {
            return;
        }

        Testproject.ForgeMergeOutcome outcome = plugin.attemptForgeMergeFromInputs(inputs[0], rareKey);
        clearMergeInventory(inventory);
        inventory.setItem(MERGE_PREVIEW_SLOT, createSimpleItem(Material.ANVIL, "&7Result Preview", List.of(
                "&7After a successful merge, one item",
                "&7returns at the next tier."
        )));
        if (outcome == Testproject.ForgeMergeOutcome.SUCCESS) {
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(inputs[0]);
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
            player.sendMessage(plugin.colorize("&6Merge success. &aOne forged item advanced to a higher tier."));
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 1.1F);
        } else {
            player.sendMessage(plugin.colorize("&cMerge failed. &7All forged items" + (rareKey != null ? " and the catalyst" : "") + " were destroyed."));
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
        updateMergePreview(target);
    }

    private void fillMergeLayout(Inventory inventory) {
        ItemStack filler = createSimpleItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());
        ItemStack border = createSimpleItem(Material.BLACK_STAINED_GLASS_PANE, "&8", List.of());
        ItemStack panel = createSimpleItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "&7", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
        for (int slot : new int[]{0, 1, 2, 3, 5, 6, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 41, 42, 43, 44, 46, 47, 48, 50, 51, 52}) {
            inventory.setItem(slot, border);
        }
        for (int slot : new int[]{10, 11, 12, 13, 14, 15, 19, 20, 21, 22, 23, 24, 28, 29, 30, 31, 32, 33, 40}) {
            inventory.setItem(slot, panel);
        }
    }

    private void updateMergePreview(Inventory inventory) {
        if (inventory == null) {
            return;
        }
        ItemStack[] inputs = getMergeInputs(inventory);
        ItemStack catalyst = inventory.getItem(MERGE_RARE_SLOT);
        String rareKey = plugin.getRareContractMaterialKey(catalyst);
        if (inputs.length == 0) {
            inventory.setItem(MERGE_PREVIEW_SLOT, createSimpleItem(Material.ANVIL, "&7Result Preview", List.of(
                    "&7Place forged duplicates into the grid",
                    "&7to preview the merge output."
            )));
            return;
        }
        ItemStack first = inputs[0];
        if (!allSameForgedType(inputs)) {
            inventory.setItem(MERGE_PREVIEW_SLOT, createSimpleItem(Material.BARRIER, "&cInvalid Merge", List.of(
                    "&7All 10 input items must be",
                    "&7the same forged item type."
            )));
            return;
        }
        ItemStack preview = first.clone();
        preview.setAmount(1);
        if (plugin.getForgedDisplayLevel(preview) < 5) {
            preview.editMeta(meta -> meta.lore(List.of(
                    plugin.legacyComponent("&7Current Tier: &f" + plugin.toRomanNumeral(plugin.getForgedDisplayLevel(first))),
                    plugin.legacyComponent("&7Next Tier: &f" + plugin.toRomanNumeral(Math.min(5, plugin.getForgedDisplayLevel(first) + 1))),
                    plugin.legacyComponent("&7Catalyst: &f" + (rareKey != null ? plugin.formatRareContractMaterialName(rareKey) : "None")),
                    plugin.legacyComponent("&7Success: &f" + (int) Math.round(plugin.getForgeMergeSuccessChance(rareKey) * 100.0D) + "%")
            )));
        }
        inventory.setItem(MERGE_PREVIEW_SLOT, preview);
    }
}
