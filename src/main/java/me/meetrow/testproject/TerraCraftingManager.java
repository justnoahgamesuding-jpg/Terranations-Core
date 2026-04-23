package me.meetrow.testproject;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TerraCraftingManager implements Listener {
    private static final int[] CATEGORY_ITEM_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int[] RECIPE_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final Testproject plugin;
    private final NamespacedKey contentIdKey;
    private final NamespacedKey contentKindKey;
    private final NamespacedKey managedDisplayKey;
    private final NamespacedKey managedDisplayBlockKey;
    private final Map<String, TerraContentDefinition> contentDefinitions = new LinkedHashMap<>();
    private final Map<TerraCraftCategory, List<TerraContentDefinition>> categorizedContent = new EnumMap<>(TerraCraftCategory.class);
    private final Map<WorldBlockKey, String> placedBlockIds = new ConcurrentHashMap<>();
    private final Map<WorldBlockKey, UUID> activeLabels = new ConcurrentHashMap<>();
    private final File dataFile;
    private final YamlConfiguration dataConfig;

    public TerraCraftingManager(Testproject plugin) {
        this.plugin = plugin;
        this.contentIdKey = new NamespacedKey(plugin, "terra_crafting_content_id");
        this.contentKindKey = new NamespacedKey(plugin, "terra_crafting_content_kind");
        this.managedDisplayKey = new NamespacedKey(plugin, "terra_crafting_label");
        this.managedDisplayBlockKey = new NamespacedKey(plugin, "terra_crafting_label_block");
        for (TerraCraftCategory category : TerraCraftCategory.values()) {
            categorizedContent.put(category, new ArrayList<>());
        }
        registerDefaults();
        this.dataFile = new File(plugin.getDataFolder(), "terra_crafting_data.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadPlacedBlocks();
        removeAllManagedLabels();
        respawnPlacedWorkbenchLabels();
    }

    public void shutdown() {
        savePlacedBlocks();
        activeLabels.clear();
    }

    public void openCatalogRoot(Player player) {
        Inventory inventory = Bukkit.createInventory(new CatalogRootHolder(), 27, plugin.legacyComponent("&8Terra Catalog"));
        fillInventory(inventory, Material.BLACK_STAINED_GLASS_PANE, "&8");
        int slot = 10;
        for (TerraCraftCategory category : TerraCraftCategory.values()) {
            inventory.setItem(slot++, createSimpleItem(category.icon, category.color + category.displayName, List.of(
                    "&7Open the " + category.displayName.toLowerCase(Locale.ROOT) + " tab.",
                    "&eClick to browse."
            )));
        }
        inventory.setItem(22, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        player.openInventory(inventory);
    }

    public boolean openCatalogCategory(Player player, TerraCraftCategory category, int page) {
        if (player == null || category == null) {
            return false;
        }
        List<TerraContentDefinition> items = categorizedContent.getOrDefault(category, List.of());
        int maxPage = Math.max(1, (int) Math.ceil(items.size() / (double) CATEGORY_ITEM_SLOTS.length));
        int currentPage = Math.max(1, Math.min(page, maxPage));

        Inventory inventory = Bukkit.createInventory(
                new CatalogCategoryHolder(category, currentPage),
                54,
                plugin.legacyComponent("&8" + category.displayName)
        );
        fillInventory(inventory, Material.BLACK_STAINED_GLASS_PANE, "&8");
        for (int slot : CATEGORY_ITEM_SLOTS) {
            inventory.setItem(slot, null);
        }
        inventory.setItem(4, createSimpleItem(category.icon, category.color + category.displayName, List.of(
                "&7Admin playtest catalog.",
                "&7Click any item to receive it."
        )));
        inventory.setItem(45, createSimpleItem(Material.ARROW, "&7Back", List.of("&7Return to categories.")));
        inventory.setItem(49, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        if (currentPage > 1) {
            inventory.setItem(48, createSimpleItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of("&7Go to page " + (currentPage - 1) + ".")));
        }
        if (currentPage < maxPage) {
            inventory.setItem(50, createSimpleItem(Material.TIPPED_ARROW, "&eNext Page", List.of("&7Go to page " + (currentPage + 1) + ".")));
        }

        int startIndex = (currentPage - 1) * CATEGORY_ITEM_SLOTS.length;
        for (int slotIndex = 0; slotIndex < CATEGORY_ITEM_SLOTS.length; slotIndex++) {
            int itemIndex = startIndex + slotIndex;
            if (itemIndex >= items.size()) {
                break;
            }
            TerraContentDefinition definition = items.get(itemIndex);
            inventory.setItem(CATEGORY_ITEM_SLOTS[slotIndex], createCatalogDisplayItem(definition));
        }
        player.openInventory(inventory);
        return true;
    }

    public boolean openWorkbench(Player player, String workbenchId) {
        TerraContentDefinition definition = getDefinition(workbenchId);
        if (player == null || definition == null || definition.kind != TerraContentKind.WORKBENCH) {
            return false;
        }

        Inventory inventory = Bukkit.createInventory(
                new WorkbenchHolder(workbenchId),
                54,
                plugin.legacyComponent("&8" + definition.displayName)
        );
        fillInventory(inventory, Material.BLACK_STAINED_GLASS_PANE, "&8");
        for (int slot : RECIPE_SLOTS) {
            inventory.setItem(slot, null);
        }

        inventory.setItem(4, createSimpleItem(definition.baseMaterial, "&6" + definition.displayName, buildWorkbenchInfoLore(definition)));
        inventory.setItem(45, createSimpleItem(Material.ARROW, "&7Close", List.of("&7Close the workbench.")));

        List<TerraCraftRecipe> recipes = definition.recipes;
        for (int index = 0; index < RECIPE_SLOTS.length && index < recipes.size(); index++) {
            inventory.setItem(RECIPE_SLOTS[index], createRecipeDisplayItem(player, definition, recipes.get(index)));
        }
        player.openInventory(inventory);
        return true;
    }

    public ItemStack createContentItem(String contentId, int amount) {
        TerraContentDefinition definition = getDefinition(contentId);
        if (definition == null) {
            return null;
        }
        ItemStack itemStack = new ItemStack(definition.baseMaterial, Math.max(1, amount));
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }

        meta.displayName(plugin.legacyComponent(definition.color + definition.displayName));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        for (String line : definition.lore) {
            lore.add(plugin.legacyComponent(line));
        }
        if (definition.placeable) {
            lore.add(plugin.legacyComponent("&8"));
            lore.add(plugin.legacyComponent("&7Placeable Terra block."));
        }
        if (definition.specialistProfession != null) {
            lore.add(plugin.legacyComponent("&7Specialist: &f" + plugin.getProfessionPlainDisplayName(definition.specialistProfession)));
        }
        lore.add(plugin.legacyComponent("&8ID: " + definition.id));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(contentIdKey, PersistentDataType.STRING, definition.id);
        container.set(contentKindKey, PersistentDataType.STRING, definition.kind.name());
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public boolean isTerraCustomItem(ItemStack itemStack) {
        return getContentId(itemStack) != null;
    }

    public String getContentId(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return null;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(contentIdKey, PersistentDataType.STRING);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraftingTableInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        Block block = event.getClickedBlock();
        TerraContentDefinition definition = getPlacedDefinition(block);
        if (definition != null && definition.kind == TerraContentKind.WORKBENCH) {
            event.setCancelled(true);
            openWorkbench(event.getPlayer(), definition.id);
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_BARREL_OPEN, 0.8F, 1.1F);
            return;
        }

        if (block.getType() == Material.CRAFTING_TABLE) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.colorize("&cVanilla crafting tables are disabled here. &7Use Terra workbenches instead."));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        TerraContentDefinition definition = getDefinition(getContentId(event.getItemInHand()));
        if (definition == null || !definition.placeable) {
            return;
        }

        WorldBlockKey key = WorldBlockKey.fromBlock(event.getBlockPlaced());
        placedBlockIds.put(key, definition.id);
        savePlacedBlocks();

        if (definition.kind == TerraContentKind.WORKBENCH) {
            spawnWorkbenchLabel(event.getBlockPlaced(), definition);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        TerraContentDefinition definition = getPlacedDefinition(event.getBlock());
        if (definition == null) {
            return;
        }

        WorldBlockKey key = WorldBlockKey.fromBlock(event.getBlock());
        placedBlockIds.remove(key);
        removeWorkbenchLabel(key);
        savePlacedBlocks();

        event.setDropItems(false);
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        ItemStack dropped = createContentItem(definition.id, 1);
        if (dropped != null) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), dropped);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        InventoryHolder holder = top.getHolder();
        if (holder instanceof CatalogRootHolder) {
            handleCatalogRootClick(event);
            return;
        }
        if (holder instanceof CatalogCategoryHolder categoryHolder) {
            handleCatalogCategoryClick(event, categoryHolder);
            return;
        }
        if (holder instanceof WorkbenchHolder workbenchHolder) {
            handleWorkbenchClick(event, workbenchHolder);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof CatalogRootHolder || holder instanceof CatalogCategoryHolder || holder instanceof WorkbenchHolder) {
            event.setCancelled(true);
        }
    }

    private void handleCatalogRootClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        int slot = event.getSlot();
        if (slot == 22) {
            player.closeInventory();
            return;
        }

        int categoryIndex = slot - 10;
        if (categoryIndex < 0 || categoryIndex >= TerraCraftCategory.values().length) {
            return;
        }
        openCatalogCategory(player, TerraCraftCategory.values()[categoryIndex], 1);
    }

    private void handleCatalogCategoryClick(InventoryClickEvent event, CatalogCategoryHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        int slot = event.getSlot();
        if (slot == 45) {
            openCatalogRoot(player);
            return;
        }
        if (slot == 49) {
            player.closeInventory();
            return;
        }
        if (slot == 48) {
            openCatalogCategory(player, holder.category, holder.page - 1);
            return;
        }
        if (slot == 50) {
            openCatalogCategory(player, holder.category, holder.page + 1);
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        String contentId = getContentId(clicked);
        TerraContentDefinition definition = getDefinition(contentId);
        if (definition == null) {
            return;
        }

        ItemStack granted = createContentItem(definition.id, 1);
        if (granted == null) {
            return;
        }
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(granted);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
        player.sendMessage(plugin.colorize("&aGave &f" + definition.displayName + "&a."));
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.7F, 1.2F);
    }

    private void handleWorkbenchClick(InventoryClickEvent event, WorkbenchHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        int slot = event.getSlot();
        if (slot == 45) {
            player.closeInventory();
            return;
        }

        TerraContentDefinition workbench = getDefinition(holder.workbenchId);
        if (workbench == null) {
            player.closeInventory();
            return;
        }

        for (int index = 0; index < RECIPE_SLOTS.length && index < workbench.recipes.size(); index++) {
            if (RECIPE_SLOTS[index] != slot) {
                continue;
            }
            craftRecipe(player, workbench, workbench.recipes.get(index));
            openWorkbench(player, workbench.id);
            return;
        }
    }

    private void craftRecipe(Player player, TerraContentDefinition workbench, TerraCraftRecipe recipe) {
        if (recipe.specialistOnly && !plugin.hasProfession(player.getUniqueId(), workbench.specialistProfession)) {
            player.sendMessage(plugin.colorize("&cOnly " + plugin.getProfessionPlainDisplayName(workbench.specialistProfession) + "s can craft that specialist pattern."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7F, 0.8F);
            return;
        }
        if (!hasIngredients(player, recipe.ingredients)) {
            player.sendMessage(plugin.colorize("&cYou do not have the required materials."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7F, 0.8F);
            return;
        }

        removeIngredients(player, recipe.ingredients);
        int outputAmount = recipe.resultAmount;
        if (recipe.specialistBonusOutput > 0 && plugin.hasProfession(player.getUniqueId(), workbench.specialistProfession)) {
            outputAmount += recipe.specialistBonusOutput;
        }

        ItemStack crafted = createContentItem(recipe.resultContentId, outputAmount);
        if (crafted == null) {
            player.sendMessage(plugin.colorize("&cThat Terra recipe is misconfigured."));
            return;
        }

        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(crafted);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }

        if (workbench.specialistProfession != null && plugin.hasProfession(player.getUniqueId(), workbench.specialistProfession)) {
            plugin.rewardProfessionXp(player, workbench.specialistProfession, recipe.specialistXpReward);
        }

        TerraContentDefinition resultDefinition = getDefinition(recipe.resultContentId);
        player.sendMessage(plugin.colorize("&aCrafted &f" + (resultDefinition != null ? resultDefinition.displayName : recipe.resultContentId) + "&a."));
        player.playSound(player.getLocation(), Sound.BLOCK_SMITHING_TABLE_USE, 0.8F, 1.1F);
    }

    private boolean hasIngredients(Player player, List<TerraIngredient> ingredients) {
        for (TerraIngredient ingredient : ingredients) {
            if (countIngredient(player, ingredient) < ingredient.amount) {
                return false;
            }
        }
        return true;
    }

    private int countIngredient(Player player, TerraIngredient ingredient) {
        int total = 0;
        for (ItemStack itemStack : player.getInventory().getStorageContents()) {
            if (itemStack == null || itemStack.getType().isAir()) {
                continue;
            }
            if (ingredient.contentId != null) {
                if (ingredient.contentId.equalsIgnoreCase(getContentId(itemStack))) {
                    total += itemStack.getAmount();
                }
            } else if (ingredient.material == itemStack.getType() && !isTerraCustomItem(itemStack)) {
                total += itemStack.getAmount();
            }
        }
        return total;
    }

    private void removeIngredients(Player player, List<TerraIngredient> ingredients) {
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (TerraIngredient ingredient : ingredients) {
            int remaining = ingredient.amount;
            for (int slot = 0; slot < contents.length && remaining > 0; slot++) {
                ItemStack itemStack = contents[slot];
                if (itemStack == null || itemStack.getType().isAir()) {
                    continue;
                }

                boolean matches = ingredient.contentId != null
                        ? ingredient.contentId.equalsIgnoreCase(getContentId(itemStack))
                        : ingredient.material == itemStack.getType() && !isTerraCustomItem(itemStack);
                if (!matches) {
                    continue;
                }

                int removed = Math.min(remaining, itemStack.getAmount());
                itemStack.setAmount(itemStack.getAmount() - removed);
                remaining -= removed;
                contents[slot] = itemStack.getAmount() <= 0 ? null : itemStack;
            }
        }
        player.getInventory().setStorageContents(contents);
    }

    private TerraContentDefinition getPlacedDefinition(Block block) {
        return block == null ? null : getDefinition(placedBlockIds.get(WorldBlockKey.fromBlock(block)));
    }

    private TerraContentDefinition getDefinition(String contentId) {
        if (contentId == null || contentId.isBlank()) {
            return null;
        }
        return contentDefinitions.get(contentId.toLowerCase(Locale.ROOT));
    }

    private void spawnWorkbenchLabel(Block block, TerraContentDefinition definition) {
        if (block == null || definition == null || definition.kind != TerraContentKind.WORKBENCH) {
            return;
        }

        WorldBlockKey key = WorldBlockKey.fromBlock(block);
        removeWorkbenchLabel(key);
        Location location = block.getLocation().add(0.5D, 1.2D, 0.5D);
        TextDisplay display = block.getWorld().spawn(location, TextDisplay.class, entity -> {
            entity.setPersistent(true);
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setSeeThrough(true);
            entity.setShadowed(false);
            entity.setAlignment(TextDisplay.TextAlignment.CENTER);
            entity.setText(plugin.colorize("&6" + definition.displayName));
            entity.getPersistentDataContainer().set(managedDisplayKey, PersistentDataType.BYTE, (byte) 1);
            entity.getPersistentDataContainer().set(managedDisplayBlockKey, PersistentDataType.STRING, key.serialize());
        });
        activeLabels.put(key, display.getUniqueId());
    }

    private void removeWorkbenchLabel(WorldBlockKey key) {
        if (key == null) {
            return;
        }

        UUID activeId = activeLabels.remove(key);
        if (activeId != null) {
            Entity entity = Bukkit.getEntity(activeId);
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }

        World world = Bukkit.getWorld(key.worldName);
        if (world == null) {
            return;
        }
        for (TextDisplay display : world.getEntitiesByClass(TextDisplay.class)) {
            String stored = display.getPersistentDataContainer().get(managedDisplayBlockKey, PersistentDataType.STRING);
            if (key.serialize().equalsIgnoreCase(stored)) {
                display.remove();
            }
        }
    }

    private void removeAllManagedLabels() {
        for (World world : Bukkit.getWorlds()) {
            for (TextDisplay display : world.getEntitiesByClass(TextDisplay.class)) {
                if (display.getPersistentDataContainer().has(managedDisplayKey, PersistentDataType.BYTE)) {
                    display.remove();
                }
            }
        }
        activeLabels.clear();
    }

    private void respawnPlacedWorkbenchLabels() {
        for (Map.Entry<WorldBlockKey, String> entry : placedBlockIds.entrySet()) {
            TerraContentDefinition definition = getDefinition(entry.getValue());
            if (definition == null || definition.kind != TerraContentKind.WORKBENCH) {
                continue;
            }
            Block block = entry.getKey().resolveBlock();
            if (block == null || block.getType().isAir()) {
                continue;
            }
            spawnWorkbenchLabel(block, definition);
        }
    }

    private void loadPlacedBlocks() {
        placedBlockIds.clear();
        if (!dataFile.exists()) {
            return;
        }
        if (dataConfig.getConfigurationSection("placed-blocks") == null) {
            return;
        }
        for (String key : dataConfig.getConfigurationSection("placed-blocks").getKeys(false)) {
            String contentId = dataConfig.getString("placed-blocks." + key);
            WorldBlockKey blockKey = WorldBlockKey.deserialize(key);
            if (blockKey == null || contentId == null || getDefinition(contentId) == null) {
                continue;
            }
            placedBlockIds.put(blockKey, contentId.toLowerCase(Locale.ROOT));
        }
    }

    private void savePlacedBlocks() {
        dataConfig.set("placed-blocks", null);
        for (Map.Entry<WorldBlockKey, String> entry : placedBlockIds.entrySet()) {
            dataConfig.set("placed-blocks." + entry.getKey().serialize(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to save terra_crafting_data.yml: " + exception.getMessage());
        }
    }

    private List<String> buildWorkbenchInfoLore(TerraContentDefinition definition) {
        List<String> lore = new ArrayList<>(definition.lore);
        lore.add("&8");
        lore.add("&7General recipes: &f" + definition.recipes.stream().filter(recipe -> !recipe.specialistOnly).count());
        lore.add("&7Specialist recipes: &f" + definition.recipes.stream().filter(recipe -> recipe.specialistOnly).count());
        if (definition.specialistProfession != null) {
            lore.add("&7Job bonus: &fExtra output on eligible recipes.");
        }
        lore.add("&eClick a recipe below to craft.");
        return lore;
    }

    private ItemStack createRecipeDisplayItem(Player player, TerraContentDefinition workbench, TerraCraftRecipe recipe) {
        ItemStack itemStack = createContentItem(recipe.resultContentId, Math.max(1, recipe.resultAmount));
        TerraContentDefinition resultDefinition = getDefinition(recipe.resultContentId);
        if (itemStack == null || resultDefinition == null) {
            return createSimpleItem(Material.BARRIER, "&cBroken Recipe", List.of("&7Missing Terra content definition."));
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }

        boolean specialist = workbench.specialistProfession != null && plugin.hasProfession(player.getUniqueId(), workbench.specialistProfession);
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(plugin.legacyComponent("&7Output: &f" + recipe.resultAmount + "x " + resultDefinition.displayName));
        if (recipe.specialistBonusOutput > 0) {
            lore.add(plugin.legacyComponent("&7Specialist bonus: &f+" + recipe.specialistBonusOutput + " output"));
        }
        lore.add(plugin.legacyComponent("&8"));
        lore.add(plugin.legacyComponent("&eIngredients"));
        for (TerraIngredient ingredient : recipe.ingredients) {
            lore.add(plugin.legacyComponent("&8- &f" + ingredient.amount + "x " + ingredient.displayName(this)));
        }
        lore.add(plugin.legacyComponent("&8"));
        if (recipe.specialistOnly) {
            lore.add(plugin.legacyComponent(specialist
                    ? "&aSpecialist pattern unlocked."
                    : "&cRequires " + plugin.getProfessionPlainDisplayName(workbench.specialistProfession) + "."));
        } else if (specialist && recipe.specialistBonusOutput > 0) {
            lore.add(plugin.legacyComponent("&aYour job bonus applies here."));
        } else if (workbench.specialistProfession != null) {
            lore.add(plugin.legacyComponent("&7" + plugin.getProfessionPlainDisplayName(workbench.specialistProfession) + "s get better yields here."));
        }
        lore.add(plugin.legacyComponent(hasIngredients(player, recipe.ingredients) ? "&eClick to craft." : "&cMissing materials."));
        meta.lore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private ItemStack createCatalogDisplayItem(TerraContentDefinition definition) {
        ItemStack itemStack = createContentItem(definition.id, 1);
        if (itemStack == null) {
            return createSimpleItem(Material.BARRIER, "&cBroken Entry", List.of("&7Missing Terra definition."));
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }
        List<net.kyori.adventure.text.Component> lore = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());
        lore.add(plugin.legacyComponent("&8"));
        lore.add(plugin.legacyComponent("&eClick to receive this item."));
        meta.lore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private void fillInventory(Inventory inventory, Material material, String name) {
        ItemStack filler = createSimpleItem(material, name, List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
    }

    private ItemStack createSimpleItem(Material material, String name, List<String> loreLines) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }
        meta.displayName(plugin.legacyComponent(name));
        if (loreLines != null && !loreLines.isEmpty()) {
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(plugin.legacyComponent(line));
            }
            meta.lore(lore);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private void registerDefaults() {
        register(workbench(
                "prospector_bench", "&b", "Prospector Bench", Material.CARTOGRAPHY_TABLE, Profession.MINER,
                List.of(
                        "&7For ore surveying, assay work,",
                        "&7and rough camp tools."
                ),
                List.of(
                        recipe("tin_ingot", 1, false, 0, 8, ingredient(Material.RAW_COPPER, 2), ingredient(Material.COAL, 1)),
                        recipe("survey_marker_block", 2, false, 1, 8, ingredient(Material.STICK, 2), ingredient("tin_ingot", 1)),
                        recipe("surveyor_helm", 1, true, 0, 12, ingredient("tin_ingot", 4), ingredient(Material.LEATHER, 1))
                )
        ));
        register(workbench(
                "timber_bench", "&a", "Timber Bench", Material.LOOM, Profession.LUMBERJACK,
                List.of(
                        "&7Shapes rough timber into treated",
                        "&7construction parts and travel gear."
                ),
                List.of(
                        recipe("treated_plank", 4, false, 2, 8, ingredient(Material.OAK_PLANKS, 4), ingredient(Material.CHARCOAL, 1)),
                        recipe("timber_frame_block", 2, false, 1, 8, ingredient("treated_plank", 2), ingredient(Material.IRON_NUGGET, 4)),
                        recipe("trail_boots", 1, true, 0, 12, ingredient(Material.LEATHER_BOOTS, 1), ingredient(Material.LEATHER, 2), ingredient("treated_plank", 1))
                )
        ));
        register(workbench(
                "field_kitchen", "&e", "Field Kitchen", Material.BARREL, Profession.FARMER,
                List.of(
                        "&7Turns farm output into durable",
                        "&7travel food and soft goods."
                ),
                List.of(
                        recipe("field_ration", 2, false, 1, 8, ingredient(Material.BREAD, 1), ingredient(Material.DRIED_KELP, 2), ingredient(Material.COOKED_BEEF, 1)),
                        recipe("linen_roll", 2, false, 1, 8, ingredient(Material.WHEAT, 4), ingredient(Material.STRING, 1)),
                        recipe("harvest_apron", 1, true, 0, 12, ingredient("linen_roll", 2), ingredient(Material.LEATHER, 2))
                )
        ));
        register(workbench(
                "mason_bench", "&f", "Mason Bench", Material.STONECUTTER, Profession.BUILDER,
                List.of(
                        "&7Cuts stonework, mixes mortar,",
                        "&7and prepares support pieces."
                ),
                List.of(
                        recipe("mortar_mix", 2, false, 1, 8, ingredient(Material.CLAY_BALL, 2), ingredient(Material.SAND, 2), ingredient(Material.BONE_MEAL, 1)),
                        recipe("reinforced_brick_block", 2, false, 1, 8, ingredient(Material.BRICK, 4), ingredient("mortar_mix", 1)),
                        recipe("mason_trowel", 1, true, 0, 12, ingredient(Material.IRON_NUGGET, 4), ingredient(Material.STICK, 1), ingredient("mortar_mix", 1))
                )
        ));
        register(workbench(
                "forge_bench", "&6", "Forge Bench", Material.SMITHING_TABLE, Profession.BLACKSMITH,
                List.of(
                        "&7Processes mixed metals and",
                        "&7small forged fittings."
                ),
                List.of(
                        recipe("bronze_ingot", 2, false, 1, 8, ingredient(Material.COPPER_INGOT, 2), ingredient("tin_ingot", 1)),
                        recipe("steel_rivet", 4, false, 2, 8, ingredient(Material.IRON_NUGGET, 4), ingredient(Material.COAL, 1)),
                        recipe("forge_hammer", 1, true, 0, 12, ingredient(Material.IRON_INGOT, 2), ingredient(Material.STICK, 1), ingredient("steel_rivet", 2))
                )
        ));
        register(workbench(
                "trade_desk", "&d", "Trade Desk", Material.LECTERN, Profession.TRADER,
                List.of(
                        "&7Bundles stock, papers, and",
                        "&7small export-ready goods."
                ),
                List.of(
                        recipe("trade_crate", 1, false, 0, 8, ingredient(Material.CHEST, 1), ingredient(Material.PAPER, 2), ingredient(Material.LEATHER, 2)),
                        recipe("leather_strap", 2, false, 1, 8, ingredient(Material.LEATHER, 1), ingredient(Material.STRING, 1)),
                        recipe("merchant_coat", 1, true, 0, 12, ingredient(Material.LEATHER_CHESTPLATE, 1), ingredient("linen_roll", 2), ingredient("leather_strap", 2))
                )
        ));
        register(workbench(
                "war_table", "&c", "War Table", Material.GRINDSTONE, Profession.SOLDIER,
                List.of(
                        "&7Builds hardened field gear",
                        "&7and defensive deployment pieces."
                ),
                List.of(
                        recipe("brigandine_vest", 1, false, 0, 8, ingredient(Material.CHAINMAIL_CHESTPLATE, 1), ingredient("steel_rivet", 4), ingredient("leather_strap", 2)),
                        recipe("defensive_barrier_block", 1, false, 1, 8, ingredient(Material.SHIELD, 1), ingredient(Material.OAK_PLANKS, 4), ingredient("steel_rivet", 2)),
                        recipe("guard_boots", 1, true, 0, 12, ingredient(Material.CHAINMAIL_BOOTS, 1), ingredient("leather_strap", 2), ingredient("steel_rivet", 2))
                )
        ));

        register(block("tin_ore_block", TerraCraftCategory.ORES, TerraContentKind.ORE, "&7", "Tin Ore", Material.LIGHT_GRAY_GLAZED_TERRACOTTA, List.of(
                "&7Placeholder custom ore block.",
                "&7Swap this with an ItemsAdder block later."
        )));
        register(block("silver_ore_block", TerraCraftCategory.ORES, TerraContentKind.ORE, "&f", "Silver Ore", Material.WHITE_GLAZED_TERRACOTTA, List.of(
                "&7Placeholder custom ore block.",
                "&7Useful for refined trade goods."
        )));
        register(block("sulfur_ore_block", TerraCraftCategory.ORES, TerraContentKind.ORE, "&e", "Sulfur Ore", Material.YELLOW_GLAZED_TERRACOTTA, List.of(
                "&7Placeholder custom ore block.",
                "&7Useful for later chemistry lines."
        )));
        register(block("saltpeter_ore_block", TerraCraftCategory.ORES, TerraContentKind.ORE, "&6", "Saltpeter Ore", Material.ORANGE_GLAZED_TERRACOTTA, List.of(
                "&7Placeholder custom ore block.",
                "&7Used for preserved field supplies."
        )));

        register(item("tin_ingot", TerraCraftCategory.MATERIALS, TerraContentKind.ITEM, "&7", "Tin Ingot", Material.IRON_NUGGET, List.of(
                "&7Soft utility metal for camp tools",
                "&7and survey hardware."
        )));
        register(item("bronze_ingot", TerraCraftCategory.MATERIALS, TerraContentKind.ITEM, "&6", "Bronze Ingot", Material.COPPER_INGOT, List.of(
                "&7Mixed alloy used in durable",
                "&7frontier-grade fittings."
        )));
        register(item("steel_rivet", TerraCraftCategory.MATERIALS, TerraContentKind.ITEM, "&f", "Steel Rivet", Material.IRON_NUGGET, List.of(
                "&7Small forged fastener for armor",
                "&7and structural pieces."
        )));
        register(item("treated_plank", TerraCraftCategory.MATERIALS, TerraContentKind.ITEM, "&a", "Treated Plank", Material.OAK_PLANKS, List.of(
                "&7Weather-hardened plank for field",
                "&7construction and travel kits."
        )));
        register(item("linen_roll", TerraCraftCategory.MATERIALS, TerraContentKind.ITEM, "&e", "Linen Roll", Material.WHITE_WOOL, List.of(
                "&7Rough cloth roll for aprons,",
                "&7coats, and wraps."
        )));
        register(item("mortar_mix", TerraCraftCategory.MATERIALS, TerraContentKind.ITEM, "&f", "Mortar Mix", Material.CLAY_BALL, List.of(
                "&7Binder mix used by builders to",
                "&7set reinforced masonry."
        )));
        register(item("trade_crate", TerraCraftCategory.MATERIALS, TerraContentKind.ITEM, "&d", "Trade Crate", Material.CHEST, List.of(
                "&7Packed export bundle used in",
                "&7merchant logistics."
        )));
        register(item("field_ration", TerraCraftCategory.MATERIALS, TerraContentKind.ITEM, "&e", "Field Ration", Material.COOKED_BEEF, List.of(
                "&7Longer-lasting food pack for",
                "&7travel and work details."
        )));
        register(item("leather_strap", TerraCraftCategory.MATERIALS, TerraContentKind.ITEM, "&6", "Leather Strap", Material.RABBIT_HIDE, List.of(
                "&7Binding material for armor,",
                "&7packs, and harness work."
        )));

        register(block("survey_marker_block", TerraCraftCategory.BLOCKS, TerraContentKind.BLOCK, "&b", "Survey Marker", Material.LAPIS_BLOCK, List.of(
                "&7Placeholder field marker block.",
                "&7Useful for prospecting camps."
        )));
        register(block("timber_frame_block", TerraCraftCategory.BLOCKS, TerraContentKind.BLOCK, "&a", "Timber Frame", Material.STRIPPED_OAK_WOOD, List.of(
                "&7Placeholder structural frame block.",
                "&7Used in frontier construction."
        )));
        register(block("reinforced_brick_block", TerraCraftCategory.BLOCKS, TerraContentKind.BLOCK, "&f", "Reinforced Brick", Material.BRICKS, List.of(
                "&7Placeholder reinforced masonry block.",
                "&7Built for heavier structures."
        )));
        register(block("defensive_barrier_block", TerraCraftCategory.BLOCKS, TerraContentKind.BLOCK, "&c", "Defensive Barrier", Material.POLISHED_ANDESITE, List.of(
                "&7Placeholder barricade block.",
                "&7Used for field fortifications."
        )));

        register(item("surveyor_helm", TerraCraftCategory.ARMOR, TerraContentKind.ARMOR, "&b", "Surveyor Helm", Material.LEATHER_HELMET, List.of(
                "&7Placeholder work helm for miners.",
                "&7Specialist access item."
        )));
        register(item("harvest_apron", TerraCraftCategory.ARMOR, TerraContentKind.ARMOR, "&e", "Harvest Apron", Material.LEATHER_CHESTPLATE, List.of(
                "&7Placeholder farm apron.",
                "&7Specialist access item."
        )));
        register(item("merchant_coat", TerraCraftCategory.ARMOR, TerraContentKind.ARMOR, "&d", "Merchant Coat", Material.LEATHER_CHESTPLATE, List.of(
                "&7Placeholder trader overcoat.",
                "&7Specialist access item."
        )));
        register(item("brigandine_vest", TerraCraftCategory.ARMOR, TerraContentKind.ARMOR, "&c", "Brigandine Vest", Material.CHAINMAIL_CHESTPLATE, List.of(
                "&7Placeholder soldier armor piece.",
                "&7Crafted for field duty."
        )));
        register(item("trail_boots", TerraCraftCategory.ARMOR, TerraContentKind.ARMOR, "&a", "Trail Boots", Material.LEATHER_BOOTS, List.of(
                "&7Placeholder boots for rough travel.",
                "&7Specialist access item."
        )));
        register(item("guard_boots", TerraCraftCategory.ARMOR, TerraContentKind.ARMOR, "&c", "Guard Boots", Material.CHAINMAIL_BOOTS, List.of(
                "&7Placeholder patrol boots.",
                "&7Specialist access item."
        )));

        register(item("mason_trowel", TerraCraftCategory.TOOLS, TerraContentKind.TOOL, "&f", "Mason Trowel", Material.IRON_SHOVEL, List.of(
                "&7Placeholder builder tool.",
                "&7Specialist access item."
        )));
        register(item("forge_hammer", TerraCraftCategory.TOOLS, TerraContentKind.TOOL, "&6", "Forge Hammer", Material.IRON_PICKAXE, List.of(
                "&7Placeholder blacksmith tool.",
                "&7Specialist access item."
        )));
    }

    private void register(TerraContentDefinition definition) {
        contentDefinitions.put(definition.id.toLowerCase(Locale.ROOT), definition);
        categorizedContent.get(definition.category).add(definition);
    }

    private TerraContentDefinition workbench(String id, String color, String displayName, Material material, Profession profession, List<String> lore, List<TerraCraftRecipe> recipes) {
        List<String> lines = new ArrayList<>(lore);
        lines.add("&8");
        lines.add("&7Specialist bonus: &f" + profession.getDisplayName() + "s get extra output.");
        lines.add("&7Specialist access: &fOne exclusive pattern.");
        return new TerraContentDefinition(id, TerraCraftCategory.WORKBENCHES, TerraContentKind.WORKBENCH, color, displayName, material, true, profession, lines, recipes);
    }

    private TerraContentDefinition block(String id, TerraCraftCategory category, TerraContentKind kind, String color, String displayName, Material material, List<String> lore) {
        return new TerraContentDefinition(id, category, kind, color, displayName, material, true, null, lore, List.of());
    }

    private TerraContentDefinition item(String id, TerraCraftCategory category, TerraContentKind kind, String color, String displayName, Material material, List<String> lore) {
        return new TerraContentDefinition(id, category, kind, color, displayName, material, false, null, lore, List.of());
    }

    private TerraCraftRecipe recipe(String resultContentId, int resultAmount, boolean specialistOnly, int specialistBonusOutput, int specialistXpReward, TerraIngredient... ingredients) {
        return new TerraCraftRecipe(resultContentId, resultAmount, specialistOnly, specialistBonusOutput, specialistXpReward, List.of(ingredients));
    }

    private TerraIngredient ingredient(Material material, int amount) {
        return new TerraIngredient(null, material, amount);
    }

    private TerraIngredient ingredient(String contentId, int amount) {
        return new TerraIngredient(contentId, null, amount);
    }

    public enum TerraCraftCategory {
        WORKBENCHES("Workbenches", Material.CRAFTING_TABLE, "&6"),
        ORES("Ores", Material.GOLD_ORE, "&f"),
        MATERIALS("Materials", Material.COPPER_INGOT, "&e"),
        BLOCKS("Blocks", Material.BRICKS, "&a"),
        ARMOR("Armor", Material.IRON_CHESTPLATE, "&c"),
        TOOLS("Tools", Material.IRON_PICKAXE, "&b");

        private final String displayName;
        private final Material icon;
        private final String color;

        TerraCraftCategory(String displayName, Material icon, String color) {
            this.displayName = displayName;
            this.icon = icon;
            this.color = color;
        }
    }

    private enum TerraContentKind {
        WORKBENCH,
        ORE,
        BLOCK,
        ITEM,
        ARMOR,
        TOOL
    }

    private record TerraContentDefinition(
            String id,
            TerraCraftCategory category,
            TerraContentKind kind,
            String color,
            String displayName,
            Material baseMaterial,
            boolean placeable,
            Profession specialistProfession,
            List<String> lore,
            List<TerraCraftRecipe> recipes
    ) {
    }

    private record TerraCraftRecipe(
            String resultContentId,
            int resultAmount,
            boolean specialistOnly,
            int specialistBonusOutput,
            int specialistXpReward,
            List<TerraIngredient> ingredients
    ) {
    }

    private record TerraIngredient(String contentId, Material material, int amount) {
        private String displayName(TerraCraftingManager manager) {
            if (contentId != null) {
                TerraContentDefinition definition = manager.getDefinition(contentId);
                return definition != null ? definition.displayName : contentId;
            }
            return material != null ? manager.plugin.formatMaterialName(material) : "Unknown";
        }
    }

    private record WorldBlockKey(String worldName, int x, int y, int z) {
        private static WorldBlockKey fromBlock(Block block) {
            return new WorldBlockKey(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        }

        private String serialize() {
            return worldName + ";" + x + ";" + y + ";" + z;
        }

        private static WorldBlockKey deserialize(String serialized) {
            if (serialized == null || serialized.isBlank()) {
                return null;
            }
            String[] parts = serialized.split(";");
            if (parts.length != 4) {
                return null;
            }
            try {
                return new WorldBlockKey(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
            } catch (NumberFormatException exception) {
                return null;
            }
        }

        private Block resolveBlock() {
            World world = Bukkit.getWorld(worldName);
            return world != null ? world.getBlockAt(x, y, z) : null;
        }
    }

    private record CatalogRootHolder() implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record CatalogCategoryHolder(TerraCraftCategory category, int page) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record WorkbenchHolder(String workbenchId) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
