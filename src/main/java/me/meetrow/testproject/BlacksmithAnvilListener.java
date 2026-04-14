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
        player.sendMessage(plugin.getMessage("profession.blacksmith.anvil-crafted", plugin.placeholders(
                "item", plugin.formatMaterialName(recipe.result()),
                "xp", String.valueOf(awardedXp)
        )));
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
                "&7Everyone can forge basic gear here.",
                "&7Blacksmiths unlock gold, diamond,",
                "&7netherite, and stronger roll quality.",
                "&7Materials are taken directly from",
                "&7your inventory."
        ));
    }

    private ItemStack createCategoryItem(Player player, BlacksmithCategory category, boolean selected) {
        int level = plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH)
                ? plugin.getProfessionLevel(player.getUniqueId(), Profession.BLACKSMITH)
                : 0;
        List<String> lore = new ArrayList<>();
        lore.add("&7Required level: &f" + category.requiredLevel);
        lore.add((category == BlacksmithCategory.BASICS || category == BlacksmithCategory.STONE || category == BlacksmithCategory.IRON)
                ? "&aPublic forge access"
                : level >= category.requiredLevel ? "&aUnlocked" : "&cBlacksmith only");
        lore.add("");
        lore.add(selected ? "&eCurrently selected." : "&7Click to view recipes.");
        Material material = selected ? Material.ORANGE_STAINED_GLASS_PANE : category.icon;
        return createSimpleItem(material, category.displayName(level), lore);
    }

    private ItemStack createRecipeItem(Player player, Testproject.BlacksmithRecipe recipe) {
        boolean unlocked = plugin.canCraftBlacksmithRecipe(player.getUniqueId(), recipe);
        boolean blacksmith = plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH);
        List<String> lore = new ArrayList<>();
        lore.add("&7Required level: &f" + recipe.level());
        lore.add("&7Craft XP: &f" + recipe.xp());
        if (plugin.isForgeManagedEquipment(recipe.result())) {
            lore.add("&7Result: &fRolled rarity + item level");
        }
        lore.add("");
        lore.add("&eIngredients:");
        for (Map.Entry<Material, Integer> entry : recipe.ingredients().entrySet()) {
            lore.add("&7- &f" + entry.getValue() + "x " + plugin.formatMaterialName(entry.getKey()));
        }
        lore.add("");
        if (unlocked) {
            lore.add("&aClick to craft.");
        } else if (!blacksmith && !plugin.isPublicBlacksmithRecipe(recipe)) {
            lore.add("&cRequires the Blacksmith job.");
        } else {
            lore.add("&cRequires Blacksmith Lv." + recipe.level() + ".");
        }
        Material icon = unlocked ? recipe.result() : Material.RED_STAINED_GLASS_PANE;
        String name = unlocked
                ? "&f" + plugin.formatMaterialName(recipe.result())
                : "&c" + plugin.formatMaterialName(recipe.result());
        return createSimpleItem(icon, name, lore);
    }

    private String getRecipeLockMessage(Player player, Testproject.BlacksmithRecipe recipe) {
        if (!plugin.hasProfession(player.getUniqueId(), Profession.BLACKSMITH) && !plugin.isPublicBlacksmithRecipe(recipe)) {
            return plugin.colorize("&cOnly blacksmiths can forge " + plugin.formatMaterialName(recipe.result()) + " above iron tier.");
        }
        return plugin.getMessage("profession.blacksmith.level-locked", plugin.placeholders(
                "item", plugin.formatMaterialName(recipe.result()),
                "level", String.valueOf(recipe.level()),
                "profession", plugin.getProfessionPlainDisplayName(Profession.BLACKSMITH)
        ));
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
