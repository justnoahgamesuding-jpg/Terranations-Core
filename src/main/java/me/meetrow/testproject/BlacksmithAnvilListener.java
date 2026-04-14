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
        }
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
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        boolean validTarget = plugin.isForgedItem(heldItem) && plugin.getForgedDisplayLevel(heldItem) < 5;
        inventory.setItem(10, validTarget ? heldItem.clone() : createSimpleItem(Material.BARRIER, "&cHold a forged item", List.of(
                "&7Hold the forged tool or armor piece",
                "&7you want to upgrade in your main hand.",
                "&7It must be below Tier V."
        )));
        inventory.setItem(12, createSimpleItem(Material.ANVIL, "&7Merge Rules", List.of(
                "&710 forged copies of the same item",
                "&71 rare material",
                "&7Success upgrades the held item by 1 tier",
                "&cFailure destroys all inputs"
        )));
        inventory.setItem(14, createSimpleItem(Material.BOOK, "&7Status", List.of(
                plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH) ? "&aBlacksmith access active" : "&cBlacksmith only",
                validTarget ? "&aHeld item ready" : "&cHeld item invalid",
                "&7Matching copies: &f" + countHeldItemCopies(player)
        )));
        inventory.setItem(18, createMergeMaterialButton("forge_shard", "&5Forge Shard"));
        inventory.setItem(19, createMergeMaterialButton("tempered_flux", "&cTempered Flux"));
        inventory.setItem(20, createMergeMaterialButton("binding_thread", "&dBinding Thread"));
        inventory.setItem(21, createMergeMaterialButton("runic_prism", "&bRunic Prism"));
        inventory.setItem(22, createMergeMaterialButton("ancient_core", "&6Ancient Core"));
        inventory.setItem(26, createSimpleItem(Material.ARROW, "&7Back", List.of("&7Return to the forge board.")));
        player.openInventory(inventory);
    }

    private void handleMergeMenuClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }
        if (event.getSlot() == 26) {
            openMenu(player, BlacksmithCategory.BASICS);
            return;
        }

        String materialKey = switch (event.getSlot()) {
            case 18 -> "forge_shard";
            case 19 -> "tempered_flux";
            case 20 -> "binding_thread";
            case 21 -> "runic_prism";
            case 22 -> "ancient_core";
            default -> null;
        };
        if (materialKey == null) {
            return;
        }
        if (!plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH)) {
            player.sendMessage(plugin.colorize("&cOnly blacksmiths can use the merge forge."));
            return;
        }
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!plugin.isForgedItem(heldItem)) {
            player.sendMessage(plugin.colorize("&cHold a forged item in your main hand first."));
            return;
        }
        if (plugin.getForgedDisplayLevel(heldItem) >= 5) {
            player.sendMessage(plugin.colorize("&cThat item is already at Tier V."));
            return;
        }
        if (countHeldItemCopies(player) < 10) {
            player.sendMessage(plugin.colorize("&cYou need 10 forged copies of that item to attempt a merge."));
            return;
        }
        if (!plugin.hasRareContractMaterial(player, materialKey, 1)) {
            player.sendMessage(plugin.colorize("&cYou need " + plugin.formatRareContractMaterialName(materialKey) + " to attempt that merge."));
            return;
        }
        if (!plugin.tryConsumeSharedActionCooldown(player, Profession.BLACKSMITH)) {
            return;
        }

        Testproject.ForgeMergeOutcome outcome = plugin.attemptForgeMerge(player, materialKey);
        if (outcome == Testproject.ForgeMergeOutcome.INVALID) {
            player.sendMessage(plugin.colorize("&cThe merge could not be completed."));
            return;
        }

        if (outcome == Testproject.ForgeMergeOutcome.SUCCESS) {
            player.sendMessage(plugin.colorize("&6Merge success. &aYour forged item advanced to a higher tier."));
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 1.1F);
        } else {
            player.sendMessage(plugin.colorize("&cMerge failed. &7The forged copies and rare material were destroyed."));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.9F, 0.8F);
        }
        openMergeMenu(player);
    }

    private int countHeldItemCopies(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!plugin.isForgedItem(heldItem)) {
            return 0;
        }
        int total = 0;
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack != null && itemStack.getType() == heldItem.getType() && plugin.isForgedItem(itemStack)) {
                total += itemStack.getAmount();
            }
        }
        return total;
    }

    private ItemStack createMergeMaterialButton(String materialKey, String displayName) {
        return createSimpleItem(Material.NETHER_STAR, displayName, List.of(
                "&7Success chance: &f" + (int) Math.round(plugin.getForgeMergeSuccessChance(materialKey) * 100.0D) + "%",
                "&7Cost: &f10 forged copies",
                "&7      &f1x " + plugin.formatRareContractMaterialName(materialKey),
                "&cFailure destroys all inputs."
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
}
