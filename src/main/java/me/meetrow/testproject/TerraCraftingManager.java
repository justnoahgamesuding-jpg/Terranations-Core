package me.meetrow.testproject;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public final class TerraCraftingManager implements Listener {
    private static final int[] GRID_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int WORKBENCH_BASIC_TOOLS_TAB_SLOT = 3;
    private static final int WORKBENCH_STATIONS_TAB_SLOT = 4;
    private static final int WORKBENCH_BUILDING_TAB_SLOT = 5;
    private static final int[] WORKBENCH_VARIANT_SLOTS = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42};
    private static final int CAMPFIRE_ROAST_TAB_SLOT = 3;
    private static final int CAMPFIRE_INFO_SLOT = 4;
    private static final int CAMPFIRE_MEALS_TAB_SLOT = 5;
    private static final int[] CAMPFIRE_RECIPE_SLOTS = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42};
    private static final int BUILDER_MASONRY_TAB_SLOT = 2;
    private static final int BUILDER_BOUNDARIES_TAB_SLOT = 3;
    private static final int BUILDER_INFO_SLOT = 4;
    private static final int BUILDER_OPENINGS_TAB_SLOT = 5;
    private static final int BUILDER_FIXTURES_TAB_SLOT = 6;
    private static final int[] BUILDER_RECIPE_SLOTS = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42};
    private static final int FARMER_MEALS_TAB_SLOT = 3;
    private static final int FARMER_INFO_SLOT = 4;
    private static final int FARMER_PRESERVES_TAB_SLOT = 5;
    private static final int[] FARMER_RECIPE_SLOTS = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42};
    private static final int[] FURNACE_INPUT_SLOTS = {19, 28, 37};
    private static final int[] FURNACE_PROGRESS_SLOTS = {20, 29, 38};
    private static final int[] FURNACE_OUTPUT_SLOTS = {21, 30, 39};
    private static final int FURNACE_FUEL_SLOT = 24;
    private static final int FURNACE_FUEL_STATUS_SLOT = 25;
    private static final int[] REFINER_INPUT_SLOTS = {11, 12, 13, 14, 15};
    private static final int[] REFINER_PROGRESS_SLOTS = {20, 21, 22, 23, 24};
    private static final int[] REFINER_OUTPUT_PRIMARY_SLOTS = {29, 30, 31, 32, 33};
    private static final int[] REFINER_OUTPUT_SECONDARY_SLOTS = {38, 39, 40, 41, 42};
    private static final int FURNACE_BASE_COOK_TIME = 300;
    private static final int FURNACE_TICK_INTERVAL = 10;
    private static final List<String> EDITABLE_SCREEN_KEYS = List.of(
            "catalog_root",
            "workbench_basic_tools",
            "workbench_stations",
            "workbench_blocks",
            "builder_masonry",
            "builder_boundaries",
            "builder_openings",
            "builder_fixtures",
            "campfire_rough",
            "campfire_hearty",
            "farmer_meals",
            "farmer_preserves",
            "refiner_processing"
    );

    private final Testproject plugin;
    private final NamespacedKey contentIdKey;
    private final NamespacedKey qualityTierKey;
    private final NamespacedKey managedDisplayKey;
    private final NamespacedKey managedDisplayBlockKey;
    private final NamespacedKey campDishKey;
    private final Map<String, ItemStack[]> guiLayoutOverrides = new ConcurrentHashMap<>();
    private final Map<UUID, GuiEditorSession> guiEditorSessions = new ConcurrentHashMap<>();
    private final Map<String, TerraContentDefinition> contentDefinitions = new LinkedHashMap<>();
    private final Map<TerraCatalogCategory, List<TerraContentDefinition>> catalogContent = new EnumMap<>(TerraCatalogCategory.class);
    private final Map<String, BenchDefinition> benches = new LinkedHashMap<>();
    private final Map<String, List<RecipeDefinition>> recipesByBench = new LinkedHashMap<>();
    private final Map<WorldBlockKey, String> placedBlockIds = new ConcurrentHashMap<>();
    private final Map<WorldBlockKey, UUID> activeLabels = new ConcurrentHashMap<>();
    private final Map<WorldBlockKey, FurnaceBenchState> furnaceStates = new ConcurrentHashMap<>();
    private final Map<WorldBlockKey, RefinerBenchState> refinerStates = new ConcurrentHashMap<>();
    private final Map<WorldBlockKey, RefinerUpgradeState> refinerUpgradeStates = new ConcurrentHashMap<>();
    private final Map<StarterHubBenchKey, FurnaceBenchState> starterHubFurnaceStates = new ConcurrentHashMap<>();
    private final Map<StarterHubBenchKey, RefinerBenchState> starterHubRefinerStates = new ConcurrentHashMap<>();
    private final List<SmeltingRecipeDefinition> smeltingRecipes = new ArrayList<>();
    private final Set<Material> overriddenVanillaResults = new LinkedHashSet<>();
    private final Set<String> overriddenContentResults = new LinkedHashSet<>();
    private final File dataFile;
    private final YamlConfiguration dataConfig;
    private final File recipeOverridesFile;
    private final YamlConfiguration recipeOverridesConfig;
    private BukkitTask furnaceTickTask;

    public TerraCraftingManager(Testproject plugin) {
        this.plugin = plugin;
        this.contentIdKey = new NamespacedKey(plugin, "terra_crafting_content_id");
        this.qualityTierKey = new NamespacedKey(plugin, "terra_crafting_quality_tier");
        this.managedDisplayKey = new NamespacedKey(plugin, "terra_crafting_label");
        this.managedDisplayBlockKey = new NamespacedKey(plugin, "terra_crafting_label_block");
        this.campDishKey = new NamespacedKey(plugin, "terra_camp_dish");
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.recipeOverridesFile = new File(plugin.getDataFolder(), "terra_recipe_overrides.yml");
        this.recipeOverridesConfig = YamlConfiguration.loadConfiguration(recipeOverridesFile);
        for (TerraCatalogCategory category : TerraCatalogCategory.values()) {
            catalogContent.put(category, new ArrayList<>());
        }
        registerBenches();
        registerCustomContent();
        registerCustomRecipes();
        loadRecipeOverrides();
        registerCustomSmeltingRecipes();
        importVanillaRecipes();
        syncBlacksmithForgeRecipes();
        importVanillaSmeltingRecipes();
        sortBenchRecipes();
        this.dataFile = new File(plugin.getDataFolder(), "terra_crafting_data.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadPlacedBlocks();
        loadRefinerUpgrades();
        loadGuiLayouts();
        removeAllManagedLabels();
        respawnWorkbenchLabels();
        startFurnaceRuntime();
    }

    public void shutdown() {
        saveGuiLayouts();
        saveRefinerUpgrades();
        savePlacedBlocks();
        activeLabels.clear();
        if (furnaceTickTask != null) {
            furnaceTickTask.cancel();
            furnaceTickTask = null;
        }
    }

    public List<String> editableScreenKeys() {
        return EDITABLE_SCREEN_KEYS;
    }

    public boolean openGuiEditor(Player player, String screenKey) {
        if (player == null) {
            return false;
        }
        String normalized = normalize(screenKey);
        if (!EDITABLE_SCREEN_KEYS.contains(normalized)) {
            return false;
        }
        Inventory source = buildEditableScreen(player, normalized, true);
        if (source == null) {
            return false;
        }
        Inventory editor = Bukkit.createInventory(new GuiEditorHolder(normalized), source.getSize(), plugin.legacyComponent("&8GUI Editor: " + prettyScreenName(normalized)));
        editor.setContents(cloneContents(source.getContents()));
        guiEditorSessions.put(player.getUniqueId(), new GuiEditorSession(normalized, cloneContents(source.getContents()), null));
        player.openInventory(editor);
        player.sendMessage(plugin.colorize("&eEdit the screen, then close it to confirm or discard the changes."));
        return true;
    }

    public boolean resetGuiLayout(String screenKey) {
        String normalized = normalize(screenKey);
        if (!EDITABLE_SCREEN_KEYS.contains(normalized)) {
            return false;
        }
        guiLayoutOverrides.remove(normalized);
        saveGuiLayouts();
        return true;
    }

    public void openCatalogRoot(Player player) {
        player.openInventory(buildCatalogRootInventory(true));
    }

    public void openCatalogCategory(Player player, TerraCatalogCategory category, int page) {
        List<TerraContentDefinition> entries = catalogContent.getOrDefault(category, List.of());
        int maxPage = Math.max(1, (int) Math.ceil(entries.size() / (double) GRID_SLOTS.length));
        int currentPage = Math.max(1, Math.min(page, maxPage));
        Inventory inventory = Bukkit.createInventory(new CatalogCategoryHolder(category, currentPage), 54, plugin.legacyComponent("&8" + category.displayName));
        fill(inventory, Material.BLACK_STAINED_GLASS_PANE, "&8");
        clearGrid(inventory);
        inventory.setItem(4, simpleItem(category.icon, category.color + category.displayName, List.of(
                "&7Admin spawn catalog.",
                "&7Click an entry to receive it."
        )));
        inventory.setItem(45, simpleItem(Material.ARROW, "&7Back", List.of("&7Return to categories.")));
        inventory.setItem(49, simpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        setPager(inventory, currentPage, maxPage);
        placeEntries(inventory, entries, currentPage, this::catalogDisplayItem);
        player.openInventory(inventory);
    }

    public void openWorkbench(Player player, String benchId) {
        BenchDefinition bench = benches.get(benchId);
        if (player == null || bench == null) {
            return;
        }
        if ("workbench".equalsIgnoreCase(benchId)) {
            openWorkbenchBasicTools(player);
            return;
        }
        if ("refiner".equalsIgnoreCase(benchId)) {
            player.sendMessage(plugin.colorize("&7Place the refiner and use it directly to process materials."));
            return;
        }
        if ("farmer_workbench".equalsIgnoreCase(benchId)) {
            openFarmerMenu(player, "meals", 1);
            return;
        }
        if ("builder_workbench".equalsIgnoreCase(benchId)) {
            openBuilderMenu(player, BuilderMenuTab.MASONRY, 1);
            return;
        }
        if ("campfire_bench".equalsIgnoreCase(benchId)) {
            openCampfireMenu(player, "rough", 1);
            return;
        }
        List<RecipeFamily> families = familiesForBench(benchId);
        int maxPage = Math.max(1, (int) Math.ceil(families.size() / (double) GRID_SLOTS.length));
        int currentPage = 1;
        Inventory inventory = Bukkit.createInventory(new BenchRootHolder(benchId, currentPage), 54, plugin.legacyComponent("&8" + bench.displayName));
        fill(inventory, Material.BLACK_STAINED_GLASS_PANE, "&8");
        inventory.setItem(4, simpleItem(bench.blockMaterial, bench.color + bench.displayName, bench.description));
        inventory.setItem(49, simpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        setPager(inventory, currentPage, maxPage);
        placeEntries(inventory, families, currentPage, this::familyDisplayItem);
        player.openInventory(inventory);
    }

    public ItemStack createContentItem(String contentId, int amount) {
        return createContentItem(contentId, amount, TerraItemQuality.STANDARD);
    }

    public ItemStack createContentItem(String contentId, int amount, TerraItemQuality quality) {
        TerraContentDefinition definition = contentDefinitions.get(normalize(contentId));
        if (definition == null) {
            return null;
        }
        ItemStack itemStack = definition.itemsAdderItemId != null
                ? plugin.createItemsAdderCustomItem(definition.itemsAdderItemId)
                : null;
        if (itemStack == null) {
            itemStack = new ItemStack(definition.material, Math.max(1, amount));
        } else {
            itemStack.setAmount(Math.max(1, amount));
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }
        TerraItemQuality resolvedQuality = quality == null ? TerraItemQuality.STANDARD : quality;
        String displayName = resolvedQuality == TerraItemQuality.STANDARD
                ? definition.color + definition.displayName
                : resolvedQuality.color + resolvedQuality.label + " " + definition.displayName;
        meta.displayName(plugin.legacyComponent(displayName));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(plugin.legacyComponent("&7Quality: " + resolvedQuality.color + resolvedQuality.label));
        for (String line : definition.lore) {
            lore.add(plugin.legacyComponent(line));
        }
        lore.add(plugin.legacyComponent("&8"));
        lore.add(plugin.legacyComponent(definition.useLine));
        if (definition.placeable) {
            lore.add(plugin.legacyComponent("&7Placeable Terra block."));
        }
        if (definition.customModelData != null) {
            meta.setCustomModelData(definition.customModelData);
        }
        applyUniqueItemTuning(meta, definition.id);
        CampDishEffect campDish = campDishEffect(definition.id);
        if (campDish != null) {
            lore.add(plugin.legacyComponent("&7Hunger: &f+" + campDish.displayedFoodPoints()));
            if (campDish.foodAdjustment() < 0 || campDish.hungerTicks() > 0) {
                lore.add(plugin.legacyComponent("&7Nutrition: &cPoor"));
                lore.add(plugin.legacyComponent("&7Drains faster than normal food."));
            } else {
                lore.add(plugin.legacyComponent("&7Nutrition: &aSteady"));
                lore.add(plugin.legacyComponent("&7Holds hunger better than rough camp food."));
            }
            meta.getPersistentDataContainer().set(campDishKey, PersistentDataType.STRING, definition.id);
        }
        if (definition.specialistProfession != null) {
            lore.add(plugin.legacyComponent("&7Specialist: &f" + plugin.getProfessionPlainDisplayName(definition.specialistProfession)));
        }
        lore.add(plugin.legacyComponent("&8ID: " + definition.id));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(contentIdKey, PersistentDataType.STRING, definition.id);
        meta.getPersistentDataContainer().set(qualityTierKey, PersistentDataType.STRING, resolvedQuality.key);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private void applyUniqueItemTuning(ItemMeta meta, String contentId) {
        if (meta == null || contentId == null || contentId.isBlank()) {
            return;
        }
        String normalized = normalize(contentId);
        if ("settlers_hatchet".equalsIgnoreCase(normalized)) {
            if (meta instanceof Damageable damageable) {
                damageable.setMaxDamage(38);
            }
            meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
            meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                    new AttributeModifier(
                            new NamespacedKey(plugin, "settlers_hatchet_attack_damage"),
                            2.0D,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.HAND
                    ));
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                    new AttributeModifier(
                            new NamespacedKey(plugin, "settlers_hatchet_attack_speed"),
                            -3.4D,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.HAND
                    ));
            return;
        }
        if ("settlers_pickaxe".equalsIgnoreCase(normalized)) {
            if (meta instanceof Damageable damageable) {
                damageable.setMaxDamage(38);
            }
            meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
            meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                    new AttributeModifier(
                            new NamespacedKey(plugin, "settlers_pickaxe_attack_damage"),
                            2.0D,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.HAND
                    ));
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                    new AttributeModifier(
                            new NamespacedKey(plugin, "settlers_pickaxe_attack_speed"),
                            -3.6D,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.HAND
                    ));
        }
    }

    public String getContentId(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return null;
        }
        ItemMeta meta = itemStack.getItemMeta();
        return meta == null ? null : meta.getPersistentDataContainer().get(contentIdKey, PersistentDataType.STRING);
    }

    private boolean matchesContentItem(ItemStack itemStack, String contentId) {
        if (itemStack == null || itemStack.getType().isAir() || contentId == null || contentId.isBlank()) {
            return false;
        }
        String normalized = normalize(contentId);
        String storedContentId = getContentId(itemStack);
        if (normalized.equalsIgnoreCase(storedContentId)) {
            return true;
        }
        TerraContentDefinition definition = contentDefinitions.get(normalized);
        if (definition == null || itemStack.getType() != definition.material) {
            return false;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return false;
        }
        if (definition.customModelData != null && meta.hasCustomModelData() && definition.customModelData.equals(meta.getCustomModelData())) {
            return true;
        }
        net.kyori.adventure.text.Component displayName = meta.displayName();
        if (displayName == null) {
            return false;
        }
        String plainName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(displayName).trim();
        if (plainName.isBlank()) {
            return false;
        }
        return plainName.equalsIgnoreCase(definition.displayName)
                || plainName.endsWith(" " + definition.displayName)
                || plainName.endsWith(definition.displayName);
    }

    public TerraItemQuality getItemQuality(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return TerraItemQuality.STANDARD;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return TerraItemQuality.STANDARD;
        }
        return TerraItemQuality.fromKey(meta.getPersistentDataContainer().get(qualityTierKey, PersistentDataType.STRING));
    }

    public boolean isTerraCustomItem(ItemStack itemStack) {
        return getContentId(itemStack) != null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }
        Block block = event.getClickedBlock();
        String placedId = placedBlockIds.get(WorldBlockKey.fromBlock(block));
        if (placedId != null && benches.containsKey(placedId)) {
            event.setCancelled(true);
            BenchDefinition bench = benches.get(placedId);
            plugin.handleTerraWorkbenchInteraction(
                    event.getPlayer(),
                    placedId,
                    bench != null ? bench.displayName : placedId,
                    block.getLocation(),
                    () -> {
                        if ("furnace_bench".equalsIgnoreCase(placedId)) {
                            if (isStarterHubBench(block)) {
                                openStarterHubFurnaceBench(event.getPlayer(), WorldBlockKey.fromBlock(block));
                            } else {
                                openFurnaceBench(event.getPlayer(), WorldBlockKey.fromBlock(block));
                            }
                        } else if ("refiner".equalsIgnoreCase(placedId)) {
                            if (isStarterHubBench(block)) {
                                openStarterHubRefinerBench(event.getPlayer(), WorldBlockKey.fromBlock(block));
                            } else {
                                openRefinerBench(event.getPlayer(), WorldBlockKey.fromBlock(block));
                            }
                        } else {
                            openWorkbench(event.getPlayer(), placedId);
                        }
                        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_BARREL_OPEN, 0.8F, 1.1F);
                    }
            );
            return;
        }
        if (block.getType() == Material.CRAFTING_TABLE) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.colorize("&cVanilla crafting tables are disabled. &7Use Terra benches instead."));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        String contentId = getContentId(event.getItem());
        CampDishEffect effect = campDishEffect(contentId);
        if (effect == null) {
            return;
        }
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            player.setFoodLevel(Math.max(0, Math.min(20, player.getFoodLevel() + effect.foodAdjustment())));
            if (effect.saturationMinimum()) {
                player.setSaturation(Math.max(player.getSaturation(), effect.saturationValue()));
            } else {
                player.setSaturation(Math.min(player.getSaturation(), effect.saturationValue()));
            }
            if (effect.hungerTicks() > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, effect.hungerTicks(), 0, true, false, true));
            }
            if (effect.buffType() != null && effect.buffTicks() > 0) {
                player.addPotionEffect(new PotionEffect(effect.buffType(), effect.buffTicks(), effect.buffAmplifier(), true, false, true));
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        String contentId = getContentId(event.getItemInHand());
        if (contentId == null) {
            return;
        }
        TerraContentDefinition definition = contentDefinitions.get(normalize(contentId));
        if (definition == null || !definition.placeable) {
            return;
        }
        WorldBlockKey key = WorldBlockKey.fromBlock(event.getBlockPlaced());
        placedBlockIds.put(key, definition.id);
        savePlacedBlocks();
        if (benches.containsKey(definition.id)) {
            spawnWorkbenchLabel(event.getBlockPlaced(), benches.get(definition.id));
        }
        if ("furnace_bench".equalsIgnoreCase(definition.id)) {
            furnaceStates.put(key, createFurnaceState(key));
        } else if ("refiner".equalsIgnoreCase(definition.id)) {
            refinerUpgradeStates.putIfAbsent(key, new RefinerUpgradeState());
            refinerStates.put(key, createRefinerState(key));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        WorldBlockKey key = WorldBlockKey.fromBlock(event.getBlock());
        String contentId = placedBlockIds.remove(key);
        if (contentId == null) {
            return;
        }
        removeWorkbenchLabel(key);
        savePlacedBlocks();
        event.setDropItems(false);
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            furnaceStates.remove(key);
            refinerStates.remove(key);
            refinerUpgradeStates.remove(key);
            saveRefinerUpgrades();
            return;
        }
        if ("furnace_bench".equalsIgnoreCase(contentId)) {
            dropFurnaceContents(event.getBlock(), key);
        } else if ("refiner".equalsIgnoreCase(contentId)) {
            dropRefinerContents(event.getBlock(), key);
            refinerUpgradeStates.remove(key);
            saveRefinerUpgrades();
        }
        for (ItemStack drop : getBlockBreakDrops(contentId)) {
            if (drop != null && !drop.getType().isAir()) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVanillaOreBreak(BlockBreakEvent event) {
        RefinerOreType oreType = RefinerOreType.fromBlock(event.getBlock().getType());
        if (oreType == null || event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setDropItems(false);
        ItemStack rawOre = createContentItem(oreType.rawContentId, 1);
        if (rawOre != null) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), rawOre);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSettlersHatchetBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!"settlers_hatchet".equalsIgnoreCase(getContentId(heldItem))) {
            return;
        }
        Material material = event.getBlock().getType();
        boolean crudeLogTool = Tag.LOGS.isTagged(material) || Tag.LEAVES.isTagged(material);
        if (!crudeLogTool) {
            event.setCancelled(true);
            player.sendActionBar(plugin.legacyComponent("&cSettler's Hatchet is too crude for that block."));
            player.playSound(player.getLocation(), Sound.ITEM_AXE_STRIP, 0.5F, 0.6F);
            return;
        }
        player.damageItemStack(EquipmentSlot.HAND, Tag.LOGS.isTagged(material) ? 2 : 3);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSettlersPickaxeBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!"settlers_pickaxe".equalsIgnoreCase(getContentId(heldItem))) {
            return;
        }
        Material material = event.getBlock().getType();
        boolean crudePickTool = material == Material.STONE
                || material == Material.COBBLESTONE
                || material == Material.COBBLED_DEEPSLATE
                || material == Material.COAL_ORE
                || material == Material.DEEPSLATE_COAL_ORE;
        if (!crudePickTool) {
            event.setCancelled(true);
            player.sendActionBar(plugin.legacyComponent("&cSettler's Pickaxe is too crude for that block."));
            player.playSound(player.getLocation(), Sound.BLOCK_STONE_HIT, 0.55F, 0.7F);
            return;
        }
        player.damageItemStack(EquipmentSlot.HAND, (material == Material.COAL_ORE || material == Material.DEEPSLATE_COAL_ORE) ? 3 : 2);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSettlersHatchetDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!"settlers_hatchet".equalsIgnoreCase(getContentId(heldItem))) {
            return;
        }
        event.setDamage(Math.min(event.getDamage(), 1.0D));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSettlersPickaxeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!"settlers_pickaxe".equalsIgnoreCase(getContentId(heldItem))) {
            return;
        }
        event.setDamage(Math.min(event.getDamage(), 1.0D));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof GuiEditorHolder editorHolder) {
            handleGuiEditorClick(event, editorHolder);
            return;
        }
        if (holder instanceof GuiEditorConfirmHolder confirmHolder) {
            handleGuiEditorConfirmClick(event, confirmHolder);
            return;
        }
        if (holder instanceof CatalogRootHolder) {
            handleCatalogRoot(event);
            return;
        }
        if (holder instanceof CatalogCategoryHolder catalogHolder) {
            handleCatalogCategory(event, catalogHolder);
            return;
        }
        if (holder instanceof BenchRootHolder benchRootHolder) {
            handleBenchRoot(event, benchRootHolder);
            return;
        }
        if (holder instanceof WorkbenchMenuHolder workbenchHolder) {
            if ("stations".equalsIgnoreCase(workbenchHolder.tabKey)) {
                handleWorkbenchStationsClick(event, workbenchHolder);
            } else if ("blocks".equalsIgnoreCase(workbenchHolder.tabKey)) {
                handleWorkbenchBlocksClick(event, workbenchHolder);
            } else {
                handleWorkbenchMenuClick(event, workbenchHolder);
            }
            return;
        }
        if (holder instanceof WorkbenchVariantHolder variantHolder) {
            handleWorkbenchVariantClick(event, variantHolder);
            return;
        }
        if (holder instanceof BuilderMenuHolder builderHolder) {
            handleBuilderMenuClick(event, builderHolder);
            return;
        }
        if (holder instanceof BuilderVariantHolder builderVariantHolder) {
            handleBuilderVariantClick(event, builderVariantHolder);
            return;
        }
        if (holder instanceof CampfireMenuHolder campfireHolder) {
            handleCampfireMenuClick(event, campfireHolder);
            return;
        }
        if (holder instanceof FarmerMenuHolder farmerHolder) {
            handleFarmerMenuClick(event, farmerHolder);
            return;
        }
        if (holder instanceof BenchFamilyHolder familyHolder) {
            handleBenchFamily(event, familyHolder);
            return;
        }
        if (holder instanceof FurnaceBenchHolder furnaceHolder) {
            handleFurnaceClick(event, furnaceHolder);
            return;
        }
        if (holder instanceof StarterHubFurnaceBenchHolder furnaceHolder) {
            handleStarterHubFurnaceClick(event, furnaceHolder);
            return;
        }
        if (holder instanceof RefinerBenchHolder refinerHolder) {
            handleRefinerClick(event, refinerHolder);
            return;
        }
        if (holder instanceof StarterHubRefinerBenchHolder refinerHolder) {
            handleStarterHubRefinerClick(event, refinerHolder);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof GuiEditorHolder editorHolder) {
            if (event.getRawSlots().stream().anyMatch(slot -> slot < event.getView().getTopInventory().getSize()
                    && isLockedEditorSlot(editorHolder.screenKey, slot))) {
                event.setCancelled(true);
            }
            return;
        }
        if (holder instanceof GuiEditorConfirmHolder) {
            return;
        }
        if (holder instanceof CatalogRootHolder || holder instanceof CatalogCategoryHolder || holder instanceof BenchRootHolder
                || holder instanceof WorkbenchMenuHolder
                || holder instanceof WorkbenchVariantHolder
                || holder instanceof BuilderMenuHolder
                || holder instanceof BuilderVariantHolder
                || holder instanceof CampfireMenuHolder
                || holder instanceof FarmerMenuHolder
                || holder instanceof BenchFamilyHolder) {
            event.setCancelled(true);
            return;
        }
        if (holder instanceof FurnaceBenchHolder && event.getRawSlots().stream().anyMatch(slot -> slot < event.getView().getTopInventory().getSize())) {
            event.setCancelled(true);
            return;
        }
        if (holder instanceof StarterHubFurnaceBenchHolder && event.getRawSlots().stream().anyMatch(slot -> slot < event.getView().getTopInventory().getSize())) {
            event.setCancelled(true);
            return;
        }
        if (holder instanceof RefinerBenchHolder && event.getRawSlots().stream().anyMatch(slot -> slot < event.getView().getTopInventory().getSize())) {
            event.setCancelled(true);
            return;
        }
        if (holder instanceof StarterHubRefinerBenchHolder && event.getRawSlots().stream().anyMatch(slot -> slot < event.getView().getTopInventory().getSize())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof GuiEditorHolder editorHolder && event.getPlayer() instanceof Player player) {
            GuiEditorSession session = guiEditorSessions.get(player.getUniqueId());
            if (session != null && session.confirmContents == null) {
                ItemStack[] editedContents = cloneContents(event.getInventory().getContents());
                if (contentsEqual(session.originalContents, editedContents)) {
                    guiEditorSessions.remove(player.getUniqueId());
                    return;
                }
                guiEditorSessions.put(player.getUniqueId(), new GuiEditorSession(editorHolder.screenKey, session.originalContents, editedContents));
                Bukkit.getScheduler().runTask(plugin, () -> openGuiEditorConfirm(player, editorHolder.screenKey));
                return;
            }
        }
        if (event.getInventory().getHolder() instanceof FurnaceBenchHolder furnaceHolder) {
            FurnaceBenchState state = furnaceStates.get(furnaceHolder.blockKey);
            if (state != null) {
                refreshFurnaceGui(state);
            }
            return;
        }
        if (event.getInventory().getHolder() instanceof StarterHubFurnaceBenchHolder furnaceHolder) {
            FurnaceBenchState state = starterHubFurnaceStates.get(new StarterHubBenchKey(furnaceHolder.playerId, furnaceHolder.blockKey));
            if (state != null) {
                refreshFurnaceGui(state);
            }
            return;
        }
        if (event.getInventory().getHolder() instanceof RefinerBenchHolder refinerHolder) {
            RefinerBenchState state = refinerStates.get(refinerHolder.blockKey);
            if (state != null) {
                refreshRefinerGui(state);
            }
            return;
        }
        if (event.getInventory().getHolder() instanceof StarterHubRefinerBenchHolder refinerHolder) {
            RefinerBenchState state = starterHubRefinerStates.get(new StarterHubBenchKey(refinerHolder.playerId, refinerHolder.blockKey));
            if (state != null) {
                refreshRefinerGui(state);
            }
        }
    }

    private void handleGuiEditorClick(InventoryClickEvent event, GuiEditorHolder holder) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }
        if (event.getClickedInventory() == event.getView().getTopInventory() && isLockedEditorSlot(holder.screenKey, event.getSlot())) {
            event.setCancelled(true);
            player.sendMessage(plugin.colorize("&cThat slot is locked in the editor because it is used by the live machine."));
            return;
        }
        if (event.getClickedInventory() == event.getView().getTopInventory() && event.getSlotType() == org.bukkit.event.inventory.InventoryType.SlotType.OUTSIDE) {
            event.setCancelled(true);
            return;
        }
        if (event.isShiftClick() && event.getClickedInventory() == event.getView().getBottomInventory()) {
            if (hasAnyLockedEditorSlots(holder.screenKey)) {
                event.setCancelled(true);
                player.sendMessage(plugin.colorize("&cShift-moving is disabled for this editor screen because it has locked machine slots."));
                return;
            }
            return;
        }
        guiEditorSessions.computeIfPresent(player.getUniqueId(), (ignored, session) ->
                new GuiEditorSession(holder.screenKey, session.originalContents, session.confirmContents));
    }

    private void handleGuiEditorConfirmClick(InventoryClickEvent event, GuiEditorConfirmHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        if (event.getSlot() == 11) {
            GuiEditorSession session = guiEditorSessions.remove(player.getUniqueId());
            if (session == null || session.confirmContents == null) {
                player.closeInventory();
                return;
            }
            guiLayoutOverrides.put(holder.screenKey, cloneContents(session.confirmContents));
            saveGuiLayouts();
            player.sendMessage(plugin.colorize("&aSaved GUI layout for &f" + prettyScreenName(holder.screenKey) + "&a."));
            player.closeInventory();
            return;
        }
        if (event.getSlot() == 15) {
            guiEditorSessions.remove(player.getUniqueId());
            player.sendMessage(plugin.colorize("&7Discarded GUI changes for &f" + prettyScreenName(holder.screenKey) + "&7."));
            player.closeInventory();
        }
    }

    private void openGuiEditorConfirm(Player player, String screenKey) {
        Inventory inventory = Bukkit.createInventory(new GuiEditorConfirmHolder(screenKey), 27, plugin.legacyComponent("&8Confirm GUI Changes"));
        fill(inventory, Material.BLACK_STAINED_GLASS_PANE, "&8");
        inventory.setItem(11, simpleItem(Material.LIME_CONCRETE, "&aSave Layout", List.of(
                "&7Apply these slot changes immediately.",
                "&7This updates the live Terra screen."
        )));
        inventory.setItem(13, simpleItem(Material.BOOK, "&f" + prettyScreenName(screenKey), List.of(
                "&7Confirm to save this edited screen,",
                "&7or cancel to keep the previous layout."
        )));
        inventory.setItem(15, simpleItem(Material.RED_CONCRETE, "&cDiscard Changes", List.of(
                "&7Throw away the staged layout edits."
        )));
        player.openInventory(inventory);
    }

    private void handleCatalogRoot(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) {
            return;
        }
        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }
        for (TerraCatalogCategory category : TerraCatalogCategory.values()) {
            if (clicked.getType() == category.icon) {
                openCatalogCategory(player, category, 1);
                return;
            }
        }
    }

    private void handleCatalogCategory(InventoryClickEvent event, CatalogCategoryHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) {
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
        String contentId = getContentId(event.getCurrentItem());
        if (contentId == null) {
            return;
        }
        ItemStack granted = createContentItem(contentId, 1);
        if (granted == null) {
            return;
        }
        giveOrDrop(player, granted);
        player.sendMessage(plugin.colorize("&aGave &f" + contentDefinitions.get(normalize(contentId)).displayName + "&a."));
    }

    private void handleBenchRoot(InventoryClickEvent event, BenchRootHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        int slot = event.getSlot();
        if (slot == 49) {
            player.closeInventory();
            return;
        }
        if (slot == 48) {
            openWorkbench(player, holder.benchId, holder.page - 1);
            return;
        }
        if (slot == 50) {
            openWorkbench(player, holder.benchId, holder.page + 1);
            return;
        }
        List<RecipeFamily> families = familiesForBench(holder.benchId);
        int index = slotIndex(slot, holder.page);
        if (index < 0 || index >= families.size()) {
            return;
        }
        RecipeFamily family = families.get(index);
        if (family.recipes.size() == 1) {
            craftRecipe(player, benches.get(holder.benchId), family.recipes.get(0));
            openWorkbench(player, holder.benchId, holder.page);
            return;
        }
        openBenchFamily(player, holder.benchId, family.key, 1);
    }

    private void handleWorkbenchMenuClick(InventoryClickEvent event, WorkbenchMenuHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (slot == 53) {
            player.closeInventory();
            return;
        }
        if (slot == WORKBENCH_BASIC_TOOLS_TAB_SLOT) {
            openWorkbenchBasicTools(player);
            return;
        }
        if (slot == WORKBENCH_STATIONS_TAB_SLOT) {
            openWorkbenchStations(player);
            return;
        }
        if (slot == WORKBENCH_BUILDING_TAB_SLOT) {
            openWorkbenchBlocks(player);
            return;
        }
        String contentId = getContentId(clicked);
        RecipeDefinition recipe = contentId != null ? findCustomRecipeByContentId(contentId) : clicked == null ? null : switch (clicked.getType()) {
            case WOODEN_SWORD -> findRecipeByResult("workbench", Material.WOODEN_SWORD);
            case WOODEN_PICKAXE -> findRecipeByResult("workbench", Material.WOODEN_PICKAXE);
            case WOODEN_SHOVEL -> findRecipeByResult("workbench", Material.WOODEN_SHOVEL);
            case WOODEN_AXE -> slot == WORKBENCH_BASIC_TOOLS_TAB_SLOT ? null : findRecipeByResult("workbench", Material.WOODEN_AXE);
            case WOODEN_HOE -> findRecipeByResult("workbench", Material.WOODEN_HOE);
            case LEATHER_HELMET -> findRecipeByResultAnywhere(Material.LEATHER_HELMET);
            case LEATHER_CHESTPLATE -> findRecipeByResultAnywhere(Material.LEATHER_CHESTPLATE);
            case LEATHER_LEGGINGS -> findRecipeByResultAnywhere(Material.LEATHER_LEGGINGS);
            case LEATHER_BOOTS -> findRecipeByResultAnywhere(Material.LEATHER_BOOTS);
            case STICK -> findRecipeByResult("workbench", Material.STICK);
            case TORCH -> findRecipeByResult("workbench", Material.TORCH);
            default -> null;
        };
        if (recipe == null) {
            return;
        }
        craftRecipe(player, benches.get("workbench"), recipe);
        openWorkbenchBasicTools(player);
    }

    private void handleWorkbenchStationsClick(InventoryClickEvent event, WorkbenchMenuHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (slot == 53) {
            player.closeInventory();
            return;
        }
        if (slot == WORKBENCH_BASIC_TOOLS_TAB_SLOT) {
            openWorkbenchBasicTools(player);
            return;
        }
        if (slot == WORKBENCH_STATIONS_TAB_SLOT) {
            openWorkbenchStations(player);
            return;
        }
        if (slot == WORKBENCH_BUILDING_TAB_SLOT) {
            openWorkbenchBlocks(player);
            return;
        }
        String contentId = getContentId(clicked);
        RecipeDefinition recipe = contentId != null ? findCustomRecipeByContentId(contentId) : clicked == null ? null : switch (clicked.getType()) {
            case SMOKER -> findRecipeByResultAnywhere(Material.SMOKER);
            case BLAST_FURNACE -> findRecipeByResultAnywhere(Material.BLAST_FURNACE);
            default -> null;
        };
        if (recipe == null) {
            return;
        }
        craftRecipe(player, benches.get("workbench"), recipe);
        openWorkbenchStations(player);
    }

    private void handleWorkbenchBlocksClick(InventoryClickEvent event, WorkbenchMenuHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (slot == 53) {
            player.closeInventory();
            return;
        }
        if (slot == WORKBENCH_BASIC_TOOLS_TAB_SLOT) {
            openWorkbenchBasicTools(player);
            return;
        }
        if (slot == WORKBENCH_STATIONS_TAB_SLOT) {
            openWorkbenchStations(player);
            return;
        }
        if (slot == WORKBENCH_BUILDING_TAB_SLOT) {
            openWorkbenchBlocks(player);
            return;
        }
        RecipeDefinition directRecipe = clicked == null ? null : switch (clicked.getType()) {
            case CHEST -> findRecipeByResultAnywhere(Material.CHEST);
            case BARREL -> findRecipeByResultAnywhere(Material.BARREL);
            case LADDER -> findRecipeByResultAnywhere(Material.LADDER);
            default -> null;
        };
        if (directRecipe != null) {
            craftRecipe(player, benches.get("workbench"), directRecipe);
            openWorkbenchBlocks(player);
            return;
        }
        WorkbenchVariantGroup group = clicked == null ? null : switch (clicked.getType()) {
            case OAK_PLANKS -> WorkbenchVariantGroup.PLANKS;
            case OAK_STAIRS -> WorkbenchVariantGroup.STAIRS;
            case OAK_SLAB -> WorkbenchVariantGroup.SLABS;
            case OAK_FENCE -> WorkbenchVariantGroup.FENCES;
            case OAK_FENCE_GATE -> WorkbenchVariantGroup.FENCE_GATES;
            case OAK_DOOR -> WorkbenchVariantGroup.DOORS;
            case OAK_TRAPDOOR -> WorkbenchVariantGroup.TRAPDOORS;
            case RED_BED -> WorkbenchVariantGroup.BEDS;
            case OAK_SIGN -> WorkbenchVariantGroup.SIGNS;
            case OAK_BOAT -> WorkbenchVariantGroup.BOATS;
            default -> null;
        };
        if (group == null) {
            return;
        }
        openWorkbenchVariants(player, group, 1);
    }

    private void handleWorkbenchVariantClick(InventoryClickEvent event, WorkbenchVariantHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        int slot = event.getSlot();
        if (slot == 45) {
            openWorkbenchBlocks(player);
            return;
        }
        if (slot == 48) {
            openWorkbenchVariants(player, holder.group, holder.page - 1);
            return;
        }
        if (slot == 50) {
            openWorkbenchVariants(player, holder.group, holder.page + 1);
            return;
        }
        if (slot == 53) {
            player.closeInventory();
            return;
        }
        WorkbenchVariantGroup group = holder.group;
        List<RecipeDefinition> recipes = workbenchVariantRecipes(group);
        int index = slotIndexInArray(slot, WORKBENCH_VARIANT_SLOTS, holder.page);
        if (index < 0 || index >= recipes.size()) {
            return;
        }
        craftRecipe(player, benches.get("workbench"), recipes.get(index));
        openWorkbenchVariants(player, group, holder.page);
    }

    private void handleBuilderMenuClick(InventoryClickEvent event, BuilderMenuHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        int slot = event.getSlot();
        if (slot == 53) {
            player.closeInventory();
            return;
        }
        BuilderMenuTab clickedTab = switch (slot) {
            case BUILDER_MASONRY_TAB_SLOT -> BuilderMenuTab.MASONRY;
            case BUILDER_BOUNDARIES_TAB_SLOT -> BuilderMenuTab.BOUNDARIES;
            case BUILDER_OPENINGS_TAB_SLOT -> BuilderMenuTab.OPENINGS;
            case BUILDER_FIXTURES_TAB_SLOT -> BuilderMenuTab.FIXTURES;
            default -> null;
        };
        if (clickedTab != null) {
            openBuilderMenu(player, clickedTab, 1);
            return;
        }
        if (slot == 48) {
            openBuilderMenu(player, holder.tab, holder.page - 1);
            return;
        }
        if (slot == 50) {
            openBuilderMenu(player, holder.tab, holder.page + 1);
            return;
        }
        int index = slotIndexInArray(slot, BUILDER_RECIPE_SLOTS, holder.page);
        List<BuilderMenuEntry> entries = builderEntries(holder.tab);
        if (index < 0 || index >= entries.size()) {
            return;
        }
        BuilderMenuEntry entry = entries.get(index);
        if (entry.isVariantGroup()) {
            openBuilderVariants(player, holder.tab, entry.group(), 1);
            return;
        }
        RecipeDefinition recipe = entry.directRecipe();
        if (recipe == null) {
            return;
        }
        craftRecipe(player, benches.get("builder_workbench"), recipe);
        openBuilderMenu(player, holder.tab, holder.page);
    }

    private void handleBuilderVariantClick(InventoryClickEvent event, BuilderVariantHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        int slot = event.getSlot();
        if (slot == 45) {
            openBuilderMenu(player, holder.tab, 1);
            return;
        }
        if (slot == 48) {
            openBuilderVariants(player, holder.tab, holder.group, holder.page - 1);
            return;
        }
        if (slot == 50) {
            openBuilderVariants(player, holder.tab, holder.group, holder.page + 1);
            return;
        }
        if (slot == 53) {
            player.closeInventory();
            return;
        }
        List<RecipeDefinition> recipes = builderVariantRecipes(holder.group);
        int index = slotIndexInArray(slot, BUILDER_RECIPE_SLOTS, holder.page);
        if (index < 0 || index >= recipes.size()) {
            return;
        }
        craftRecipe(player, benches.get("builder_workbench"), recipes.get(index));
        openBuilderVariants(player, holder.tab, holder.group, holder.page);
    }

    private void handleCampfireMenuClick(InventoryClickEvent event, CampfireMenuHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (slot == 53) {
            player.closeInventory();
            return;
        }
        if (clicked != null && "Rough Meals".equalsIgnoreCase(plainDisplayName(clicked))) {
            openCampfireMenu(player, "rough", 1);
            return;
        }
        if (clicked != null && "Field Meals".equalsIgnoreCase(plainDisplayName(clicked))) {
            openCampfireMenu(player, "hearty", 1);
            return;
        }
        if (slot == 48) {
            openCampfireMenu(player, holder.tabKey, holder.page - 1);
            return;
        }
        if (slot == 50) {
            openCampfireMenu(player, holder.tabKey, holder.page + 1);
            return;
        }
        RecipeDefinition recipe = findRecipeByDisplayItem(campfireRecipes(holder.tabKey), event.getCurrentItem());
        if (recipe == null) {
            return;
        }
        craftRecipe(player, benches.get("campfire_bench"), recipe);
        openCampfireMenu(player, holder.tabKey, holder.page);
    }

    private void handleFarmerMenuClick(InventoryClickEvent event, FarmerMenuHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (slot == 53) {
            player.closeInventory();
            return;
        }
        if (clicked != null && "Prepared Meals".equalsIgnoreCase(plainDisplayName(clicked))) {
            openFarmerMenu(player, "meals", 1);
            return;
        }
        if (clicked != null && "Preserves".equalsIgnoreCase(plainDisplayName(clicked))) {
            openFarmerMenu(player, "preserves", 1);
            return;
        }
        if (slot == 48) {
            openFarmerMenu(player, holder.tabKey, holder.page - 1);
            return;
        }
        if (slot == 50) {
            openFarmerMenu(player, holder.tabKey, holder.page + 1);
            return;
        }
        RecipeDefinition recipe = findRecipeByDisplayItem(farmerRecipes(holder.tabKey), event.getCurrentItem());
        if (recipe == null) {
            return;
        }
        craftRecipe(player, benches.get("farmer_workbench"), recipe);
        openFarmerMenu(player, holder.tabKey, holder.page);
    }

    private void handleBenchFamily(InventoryClickEvent event, BenchFamilyHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        int slot = event.getSlot();
        if (slot == 45) {
            openWorkbench(player, holder.benchId);
            return;
        }
        if (slot == 49) {
            player.closeInventory();
            return;
        }
        if (slot == 48) {
            openBenchFamily(player, holder.benchId, holder.familyKey, holder.page - 1);
            return;
        }
        if (slot == 50) {
            openBenchFamily(player, holder.benchId, holder.familyKey, holder.page + 1);
            return;
        }
        List<RecipeDefinition> recipes = recipesForFamily(holder.benchId, holder.familyKey);
        int index = slotIndex(slot, holder.page);
        if (index < 0 || index >= recipes.size()) {
            return;
        }
        craftRecipe(player, benches.get(holder.benchId), recipes.get(index));
        openBenchFamily(player, holder.benchId, holder.familyKey, holder.page);
    }

    private void handleFurnaceClick(InventoryClickEvent event, FurnaceBenchHolder holder) {
        FurnaceBenchState state = furnaceStates.computeIfAbsent(holder.blockKey, this::createFurnaceState);
        handleFurnaceClick(event, state, "furnace_bench");
    }

    private void handleStarterHubFurnaceClick(InventoryClickEvent event, StarterHubFurnaceBenchHolder holder) {
        FurnaceBenchState state = starterHubFurnaceStates.computeIfAbsent(
                new StarterHubBenchKey(holder.playerId, holder.blockKey),
                this::createStarterHubFurnaceState);
        handleFurnaceClick(event, state, "furnace_bench");
    }

    private void handleFurnaceClick(InventoryClickEvent event, FurnaceBenchState state, String benchId) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }
        int rawSlot = event.getRawSlot();
        int topSize = event.getView().getTopInventory().getSize();
        if (rawSlot >= topSize) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
                shiftMoveIntoFurnace(event, state);
            }
            return;
        }
        int slot = event.getSlot();
        if (slot == 49) {
            event.setCancelled(true);
            player.closeInventory();
            return;
        }
        if (isFurnaceInputSlot(slot) || slot == FURNACE_FUEL_SLOT) {
            return;
        }
        if (isFurnaceOutputSlot(slot)) {
            boolean collectingOutput = event.getCurrentItem() != null
                    && !event.getCurrentItem().getType().isAir()
                    && (event.isShiftClick() || event.getCursor() == null || event.getCursor().getType().isAir());
            if (event.getCursor() != null && !event.getCursor().getType().isAir()) {
                event.setCancelled(true);
            }
            if (collectingOutput) {
                plugin.recordTerraWorkbenchCollection(player, benchId);
            }
            return;
        }
        event.setCancelled(true);
    }

    private void handleRefinerClick(InventoryClickEvent event, RefinerBenchHolder holder) {
        RefinerBenchState state = refinerStates.computeIfAbsent(holder.blockKey, this::createRefinerState);
        handleRefinerClick(event, state, "refiner");
    }

    private void handleStarterHubRefinerClick(InventoryClickEvent event, StarterHubRefinerBenchHolder holder) {
        RefinerBenchState state = starterHubRefinerStates.computeIfAbsent(
                new StarterHubBenchKey(holder.playerId, holder.blockKey),
                this::createStarterHubRefinerState);
        handleRefinerClick(event, state, "refiner");
    }

    private void handleRefinerClick(InventoryClickEvent event, RefinerBenchState state, String benchId) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }
        int rawSlot = event.getRawSlot();
        int topSize = event.getView().getTopInventory().getSize();
        if (rawSlot >= topSize) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
                shiftMoveIntoRefiner(event, player, state);
            }
            return;
        }
        int slot = event.getSlot();
        if (slot == 49) {
            event.setCancelled(true);
            player.closeInventory();
            return;
        }
        if (slot == 46 || slot == 47) {
            event.setCancelled(true);
            handleRefinerUpgradeClick(player, state, slot);
            return;
        }
        if (isRefinerInputSlot(slot)) {
            if (!canPlaceIntoRefiner(player, event.getCursor())) {
                event.setCancelled(true);
            }
            return;
        }
        if (isRefinerOutputSlot(slot)) {
            boolean collectingOutput = event.getCurrentItem() != null
                    && !event.getCurrentItem().getType().isAir()
                    && (event.isShiftClick() || event.getCursor() == null || event.getCursor().getType().isAir());
            if (event.getCursor() != null && !event.getCursor().getType().isAir()) {
                event.setCancelled(true);
            }
            if (collectingOutput) {
                plugin.recordTerraWorkbenchCollection(player, benchId);
            }
            return;
        }
        event.setCancelled(true);
    }

    private void shiftMoveIntoFurnace(InventoryClickEvent event, FurnaceBenchState state) {
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType().isAir()) {
            return;
        }
        boolean smeltable = findSmeltingRecipe(current) != null;
        boolean fuel = fuelBurnTicks(current.getType()) > 0;
        if (!smeltable && !fuel) {
            return;
        }
        ItemStack remaining = current.clone();
        if (fuel) {
            ItemStack existingFuel = state.inventory.getItem(FURNACE_FUEL_SLOT);
            if (existingFuel == null || existingFuel.getType().isAir()) {
                state.inventory.setItem(FURNACE_FUEL_SLOT, remaining.clone());
                remaining.setAmount(0);
            } else if (canStackTogether(existingFuel, remaining)) {
                int moved = Math.min(existingFuel.getMaxStackSize() - existingFuel.getAmount(), remaining.getAmount());
                if (moved > 0) {
                    existingFuel.setAmount(existingFuel.getAmount() + moved);
                    state.inventory.setItem(FURNACE_FUEL_SLOT, existingFuel);
                    remaining.setAmount(remaining.getAmount() - moved);
                }
            }
        }
        for (int inputSlot : FURNACE_INPUT_SLOTS) {
            if (!smeltable || remaining.getAmount() <= 0) {
                break;
            }
            ItemStack existing = state.inventory.getItem(inputSlot);
            if (existing == null || existing.getType().isAir()) {
                state.inventory.setItem(inputSlot, remaining.clone());
                remaining.setAmount(0);
                break;
            }
            if (!canStackTogether(existing, remaining)) {
                continue;
            }
            int freeSpace = existing.getMaxStackSize() - existing.getAmount();
            if (freeSpace <= 0) {
                continue;
            }
            int moved = Math.min(freeSpace, remaining.getAmount());
            existing.setAmount(existing.getAmount() + moved);
            state.inventory.setItem(inputSlot, existing);
            remaining.setAmount(remaining.getAmount() - moved);
        }
        event.getClickedInventory().setItem(event.getSlot(), remaining.getAmount() <= 0 ? null : remaining);
        refreshFurnaceGui(state);
    }

    private void shiftMoveIntoRefiner(InventoryClickEvent event, Player player, RefinerBenchState state) {
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType().isAir()) {
            return;
        }
        if (!canPlaceIntoRefiner(player, current)) {
            return;
        }
        RefinerRecipeDefinition recipe = findRefinerRecipe(current);
        if (recipe == null) {
            return;
        }
        ItemStack remaining = current.clone();
        for (int inputSlot : REFINER_INPUT_SLOTS) {
            if (remaining.getAmount() <= 0) {
                break;
            }
            ItemStack existing = state.inventory.getItem(inputSlot);
            if (existing == null || existing.getType().isAir()) {
                state.inventory.setItem(inputSlot, remaining.clone());
                remaining.setAmount(0);
                break;
            }
            if (!canStackTogether(existing, remaining)) {
                continue;
            }
            int freeSpace = existing.getMaxStackSize() - existing.getAmount();
            if (freeSpace <= 0) {
                continue;
            }
            int moved = Math.min(freeSpace, remaining.getAmount());
            existing.setAmount(existing.getAmount() + moved);
            state.inventory.setItem(inputSlot, existing);
            remaining.setAmount(remaining.getAmount() - moved);
        }
        event.getClickedInventory().setItem(event.getSlot(), remaining.getAmount() <= 0 ? null : remaining);
        refreshRefinerGui(state);
    }

    private void openFurnaceBench(Player player, WorldBlockKey blockKey) {
        FurnaceBenchState state = furnaceStates.computeIfAbsent(blockKey, this::createFurnaceState);
        refreshFurnaceGui(state);
        player.openInventory(state.inventory);
    }

    private void openStarterHubFurnaceBench(Player player, WorldBlockKey blockKey) {
        StarterHubBenchKey key = new StarterHubBenchKey(player.getUniqueId(), blockKey);
        FurnaceBenchState state = starterHubFurnaceStates.computeIfAbsent(key, this::createStarterHubFurnaceState);
        refreshFurnaceGui(state);
        player.openInventory(state.inventory);
    }

    private void openRefinerBench(Player player, WorldBlockKey blockKey) {
        RefinerBenchState state = refinerStates.computeIfAbsent(blockKey, this::createRefinerState);
        refreshRefinerGui(state);
        player.openInventory(state.inventory);
    }

    private void openStarterHubRefinerBench(Player player, WorldBlockKey blockKey) {
        StarterHubBenchKey key = new StarterHubBenchKey(player.getUniqueId(), blockKey);
        RefinerBenchState state = starterHubRefinerStates.computeIfAbsent(key, this::createStarterHubRefinerState);
        refreshRefinerGui(state);
        player.openInventory(state.inventory);
    }

    private void openWorkbench(Player player, String benchId, int page) {
        BenchDefinition bench = benches.get(benchId);
        if (bench == null) {
            return;
        }
        List<RecipeFamily> families = familiesForBench(benchId);
        int maxPage = Math.max(1, (int) Math.ceil(families.size() / (double) GRID_SLOTS.length));
        int currentPage = Math.max(1, Math.min(page, maxPage));
        Inventory inventory = Bukkit.createInventory(new BenchRootHolder(benchId, currentPage), 54, plugin.legacyComponent("&8" + bench.displayName));
        fillBenchLayout(inventory);
        inventory.setItem(4, simpleItem(bench.blockMaterial, bench.color + bench.displayName, List.of(
                "&7Bench notes",
                "&7Recipes are grouped by job use."
        )));
        inventory.setItem(45, simpleItem(Material.BOOK, "&fBench Notes", List.of(
                "&7Click once to craft when a group",
                "&7only has one recipe.",
                "&7Open a second page only for",
                "&7real variant choices."
        )));
        inventory.setItem(46, simpleItem(bench.blockMaterial, bench.color + "Bench Role", bench.description));
        inventory.setItem(49, simpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        setPager(inventory, currentPage, maxPage);
        placeEntries(inventory, families, currentPage, this::familyDisplayItem);
        player.openInventory(inventory);
    }

    private void openBenchFamily(Player player, String benchId, String familyKey, int page) {
        BenchDefinition bench = benches.get(benchId);
        List<RecipeDefinition> recipes = recipesForFamily(benchId, familyKey);
        if (bench == null || recipes.isEmpty()) {
            return;
        }
        int maxPage = Math.max(1, (int) Math.ceil(recipes.size() / (double) GRID_SLOTS.length));
        int currentPage = Math.max(1, Math.min(page, maxPage));
        Inventory inventory = Bukkit.createInventory(new BenchFamilyHolder(benchId, familyKey, currentPage), 54, plugin.legacyComponent("&8" + recipes.get(0).familyName));
        fillBenchLayout(inventory);
        inventory.setItem(4, simpleItem(recipes.get(0).familyIcon, bench.color + recipes.get(0).familyName, List.of(
                "&7Variant choice",
                "&7Pick the exact version you want."
        )));
        inventory.setItem(46, simpleItem(Material.BOOK, "&fBench Notes", List.of(
                "&7This page only exists because",
                "&7this family has multiple variants."
        )));
        inventory.setItem(45, simpleItem(Material.ARROW, "&7Back", List.of("&7Return to the bench recipes.")));
        inventory.setItem(49, simpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        setPager(inventory, currentPage, maxPage);
        placeEntries(inventory, recipes, currentPage, recipe -> recipeDisplayItem(player, bench, recipe));
        player.openInventory(inventory);
    }

    private void openWorkbenchBasicTools(Player player) {
        player.openInventory(buildWorkbenchBasicToolsInventory(true));
    }

    private void openWorkbenchStations(Player player) {
        player.openInventory(buildWorkbenchStationsInventory(true));
    }

    private void openWorkbenchBlocks(Player player) {
        player.openInventory(buildWorkbenchBlocksInventory(true));
    }

    private void openWorkbenchVariants(Player player, WorkbenchVariantGroup group, int page) {
        List<RecipeDefinition> recipes = workbenchVariantRecipes(group);
        int maxPage = Math.max(1, (int) Math.ceil(recipes.size() / (double) WORKBENCH_VARIANT_SLOTS.length));
        int currentPage = Math.max(1, Math.min(page, maxPage));
        Inventory inventory = Bukkit.createInventory(new WorkbenchVariantHolder(group, currentPage), 54, plugin.legacyComponent("&8" + group.displayName));
        applyWorkbenchFrame(inventory);
        fillSlots(inventory, new int[]{10, 11, 12, 13, 14, 15, 16}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        fillSlots(inventory, new int[]{19, 25, 28, 34, 37, 43, 46, 47, 48, 49, 50, 51, 52}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        inventory.setItem(4, simpleItem(group.icon, "&6" + group.displayName, List.of(
                "&7Choose the specific variant",
                "&7you want to craft."
        )));
        inventory.setItem(45, simpleItem(Material.ARROW, "&7Back", List.of("&7Return to the blocks tab.")));
        if (currentPage > 1) {
            inventory.setItem(48, simpleItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of("&7Go to page " + (currentPage - 1) + ".")));
        }
        if (currentPage < maxPage) {
            inventory.setItem(50, simpleItem(Material.TIPPED_ARROW, "&eNext Page", List.of("&7Go to page " + (currentPage + 1) + ".")));
        }
        inventory.setItem(53, simpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        int startIndex = (currentPage - 1) * WORKBENCH_VARIANT_SLOTS.length;
        for (int i = 0; i < WORKBENCH_VARIANT_SLOTS.length; i++) {
            int recipeIndex = startIndex + i;
            if (recipeIndex >= recipes.size()) {
                break;
            }
            RecipeDefinition recipe = recipes.get(recipeIndex);
            inventory.setItem(WORKBENCH_VARIANT_SLOTS[i], recipeDisplayOverride(recipe.iconItem(this), "&f" + recipe.resultDisplayName, recipe));
        }
        player.openInventory(inventory);
    }

    private void openBuilderMenu(Player player, BuilderMenuTab tab, int page) {
        player.openInventory(buildBuilderMenuInventory(player, tab, page, true));
    }

    private void openBuilderVariants(Player player, BuilderMenuTab tab, BuilderVariantGroup group, int page) {
        List<RecipeDefinition> recipes = builderVariantRecipes(group);
        int maxPage = Math.max(1, (int) Math.ceil(recipes.size() / (double) BUILDER_RECIPE_SLOTS.length));
        int currentPage = Math.max(1, Math.min(page, maxPage));
        Inventory inventory = Bukkit.createInventory(new BuilderVariantHolder(tab, group, currentPage), 54, plugin.legacyComponent("&8" + group.displayName));
        applyWorkbenchFrame(inventory);
        fillSlots(inventory, new int[]{10, 11, 12, 13, 14, 15, 16}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        fillSlots(inventory, new int[]{19, 25, 28, 34, 37, 43, 46, 47, 48, 49, 50, 51, 52}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        inventory.setItem(4, simpleItem(group.icon, "&6" + group.displayName, List.of(
                "&7Choose the exact building piece",
                "&7you want to craft."
        )));
        inventory.setItem(45, simpleItem(Material.ARROW, "&7Back", List.of("&7Return to the builder board.")));
        if (currentPage > 1) {
            inventory.setItem(48, simpleItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of("&7Go to page " + (currentPage - 1) + ".")));
        }
        inventory.setItem(49, simpleItem(group.icon, "&f" + group.displayName, List.of(
                "&7Grouped by building family to keep",
                "&7the builder bench easy to scan."
        )));
        if (currentPage < maxPage) {
            inventory.setItem(50, simpleItem(Material.TIPPED_ARROW, "&eNext Page", List.of("&7Go to page " + (currentPage + 1) + ".")));
        }
        inventory.setItem(53, simpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        placeEntries(inventory, recipes, currentPage, recipe -> recipeDisplayItem(player, benches.get("builder_workbench"), recipe), BUILDER_RECIPE_SLOTS);
        player.openInventory(inventory);
    }

    private void openCampfireMenu(Player player, String tabKey, int page) {
        player.openInventory(buildCampfireMenuInventory(player, tabKey, page, true));
    }

    private void openFarmerMenu(Player player, String tabKey, int page) {
        player.openInventory(buildFarmerMenuInventory(player, tabKey, page, true));
    }

    private Inventory buildCatalogRootInventory(boolean applyLayout) {
        Inventory inventory = Bukkit.createInventory(new CatalogRootHolder(), 27, plugin.legacyComponent("&8Terra Catalog"));
        fill(inventory, Material.BLACK_STAINED_GLASS_PANE, "&8");
        int slot = 10;
        for (TerraCatalogCategory category : TerraCatalogCategory.values()) {
            inventory.setItem(slot++, simpleItem(category.icon, category.color + category.displayName, List.of(
                    "&7Browse " + category.displayName.toLowerCase(Locale.ROOT) + ".",
                    "&eClick to open."
            )));
        }
        inventory.setItem(22, simpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        return applyLayout ? applySavedLayout("catalog_root", inventory) : inventory;
    }

    private Inventory buildWorkbenchBasicToolsInventory(boolean applyLayout) {
        Inventory inventory = Bukkit.createInventory(new WorkbenchMenuHolder("basic_tools"), 54, plugin.legacyComponent("&8Workbench"));
        applyWorkbenchFrame(inventory);
        setSlot(inventory, 3, simpleItem(Material.WOODEN_AXE, "&6Basic Tools", List.of(
                "&7Starter ore gear, clothes, and first-phase kit.",
                "&eCurrent tab."
        )));
        setSlot(inventory, 4, simpleItem(Material.ANVIL, "&7Stations", List.of(
                "&7Workbench station recipes.",
                "&eClick to open when set up."
        )));
        setSlot(inventory, 5, simpleItem(Material.SCAFFOLDING, "&7Building", List.of(
                "&7Starter building recipes.",
                "&eClick to open when set up."
        )));
        fillSlots(inventory, new int[]{10, 11, 12, 13, 14, 15, 16}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        fillSlots(inventory, new int[]{19, 25, 28, 34, 37, 43, 46, 47, 48, 49, 50, 51, 52}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        clearSlots(inventory, new int[]{20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42});
        inventory.setItem(20, displayWorkbenchContentRecipe("settlers_hatchet", Material.WOODEN_AXE, "&fSettler's Hatchet"));
        inventory.setItem(21, displayWorkbenchContentRecipe("settlers_pickaxe", Material.WOODEN_PICKAXE, "&fSettler's Pickaxe"));
        inventory.setItem(22, displayWorkbenchRecipe(Material.WOODEN_SHOVEL, "&fWooden Shovel"));
        inventory.setItem(23, displayWorkbenchContentRecipe("copper_pioneer_axe", Material.IRON_AXE, "&fCopper Axe"));
        inventory.setItem(24, displayWorkbenchRecipe(Material.WOODEN_HOE, "&fWooden Hoe"));
        inventory.setItem(29, displayWorkbenchRecipe(Material.LEATHER_HELMET, "&fLeather Helmet"));
        inventory.setItem(30, displayWorkbenchRecipe(Material.LEATHER_CHESTPLATE, "&fLeather Chestplate"));
        inventory.setItem(31, displayWorkbenchRecipe(Material.LEATHER_LEGGINGS, "&fLeather Leggings"));
        inventory.setItem(32, displayWorkbenchRecipe(Material.LEATHER_BOOTS, "&fLeather Boots"));
        inventory.setItem(33, displayWorkbenchRecipe(Material.STICK, "&fStick &7(4)"));
        inventory.setItem(38, displayWorkbenchRecipe(Material.TORCH, "&fTorch"));
        inventory.setItem(53, simpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        return applyLayout ? applySavedLayout("workbench_basic_tools", inventory) : inventory;
    }

    private Inventory buildWorkbenchStationsInventory(boolean applyLayout) {
        Inventory inventory = Bukkit.createInventory(new WorkbenchMenuHolder("stations"), 54, plugin.legacyComponent("&8Workbench"));
        applyWorkbenchFrame(inventory);
        setSlot(inventory, 3, simpleItem(Material.WOODEN_AXE, "&7Basic Tools", List.of(
                "&7Starter tools, clothes, and camp basics.",
                "&eClick to open."
        )));
        setSlot(inventory, 4, simpleItem(Material.ANVIL, "&6Workbenches", List.of(
                "&7Craft stations from the main workbench.",
                "&eCurrent tab."
        )));
        setSlot(inventory, 5, simpleItem(Material.SCAFFOLDING, "&7Building", List.of(
                "&7Starter building recipes.",
                "&eClick to open when set up."
        )));
        fillSlots(inventory, new int[]{10, 11, 12, 13, 14, 15, 16}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        fillSlots(inventory, new int[]{19, 25, 28, 34, 37, 43, 46, 47, 48, 49, 50, 51, 52}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        clearSlots(inventory, new int[]{32, 33, 38, 39, 40, 41, 42});
        inventory.setItem(20, displayWorkbenchContentRecipe("workbench", Material.CRAFTING_TABLE, "&fWorkbench"));
        inventory.setItem(21, displayWorkbenchContentRecipe("campfire_bench", Material.CAMPFIRE, "&fCampfire"));
        inventory.setItem(22, displayWorkbenchContentRecipe("farmer_workbench", Material.BARREL, "&fFarmer's Workbench"));
        inventory.setItem(23, displayWorkbenchContentRecipe("furnace_bench", Material.FURNACE, "&fFurnace"));
        inventory.setItem(24, displayWorkbenchRecipe(Material.SMOKER, "&fSmoker"));
        inventory.setItem(29, displayWorkbenchRecipe(Material.BLAST_FURNACE, "&fBlast Furnace"));
        inventory.setItem(30, displayWorkbenchContentRecipe("builder_workbench", Material.STONECUTTER, "&fBuilder's Workbench"));
        inventory.setItem(31, displayWorkbenchContentRecipe("refiner", Material.GRINDSTONE, "&fRefiner"));
        inventory.setItem(53, simpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        return applyLayout ? applySavedLayout("workbench_stations", inventory) : inventory;
    }

    private Inventory buildWorkbenchBlocksInventory(boolean applyLayout) {
        Inventory inventory = Bukkit.createInventory(new WorkbenchMenuHolder("blocks"), 54, plugin.legacyComponent("&8Workbench"));
        applyWorkbenchFrame(inventory);
        setSlot(inventory, 3, simpleItem(Material.WOODEN_AXE, "&7Basic Tools", List.of(
                "&7Starter tools, clothes, and camp basics.",
                "&eClick to open."
        )));
        setSlot(inventory, 4, simpleItem(Material.ANVIL, "&7Workbenches", List.of(
                "&7Craft stations from the main workbench.",
                "&eClick to open."
        )));
        setSlot(inventory, 5, simpleItem(Material.SCAFFOLDING, "&6Blocks", List.of(
                "&7Pick a building family, then choose",
                "&7the wood variant you want.",
                "&eCurrent tab."
        )));
        fillSlots(inventory, new int[]{10, 11, 12, 13, 14, 15, 16}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        fillSlots(inventory, new int[]{19, 25, 28, 34, 37, 43, 46, 47, 48, 49, 50, 51, 52}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        clearSlots(inventory, new int[]{41, 42});
        inventory.setItem(20, simpleItem(Material.OAK_PLANKS, "&fPlanks", List.of("&7Choose a wood type.")));
        inventory.setItem(21, simpleItem(Material.OAK_STAIRS, "&fStairs", List.of("&7Choose a wood type.")));
        inventory.setItem(22, simpleItem(Material.OAK_SLAB, "&fSlabs", List.of("&7Choose a wood type.")));
        inventory.setItem(23, simpleItem(Material.OAK_FENCE, "&fFences", List.of("&7Choose a wood type.")));
        inventory.setItem(24, simpleItem(Material.OAK_FENCE_GATE, "&fFence Gates", List.of("&7Choose a wood type.")));
        inventory.setItem(29, simpleItem(Material.OAK_DOOR, "&fDoors", List.of("&7Choose a wood type.")));
        inventory.setItem(30, simpleItem(Material.OAK_TRAPDOOR, "&fTrapdoors", List.of("&7Choose a wood type.")));
        inventory.setItem(31, simpleItem(Material.CHEST, "&fChests", List.of("&7Choose a chest variant.")));
        inventory.setItem(32, simpleItem(Material.BARREL, "&fBarrels", List.of("&7Choose a barrel recipe.")));
        inventory.setItem(33, simpleItem(Material.RED_BED, "&fBeds", List.of("&7Choose a bed color.")));
        inventory.setItem(38, simpleItem(Material.OAK_SIGN, "&fSigns", List.of("&7Choose a wood type.")));
        inventory.setItem(39, simpleItem(Material.LADDER, "&fLadders", List.of("&7Simple climbing pieces.")));
        inventory.setItem(40, simpleItem(Material.OAK_BOAT, "&fBoats", List.of("&7Choose a wood type.")));
        inventory.setItem(53, simpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        return applyLayout ? applySavedLayout("workbench_blocks", inventory) : inventory;
    }

    private Inventory buildBuilderMenuInventory(Player player, BuilderMenuTab tab, int page, boolean applyLayout) {
        List<BuilderMenuEntry> entries = builderEntries(tab);
        int maxPage = Math.max(1, (int) Math.ceil(entries.size() / (double) BUILDER_RECIPE_SLOTS.length));
        int currentPage = Math.max(1, Math.min(page, maxPage));
        Inventory inventory = Bukkit.createInventory(new BuilderMenuHolder(tab, currentPage), 54, plugin.legacyComponent("&8Builder's Workbench"));
        applyWorkbenchFrame(inventory);
        fillSlots(inventory, new int[]{9, 10, 11, 12, 13, 14, 15, 16, 17}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        fillSlots(inventory, new int[]{19, 25, 28, 34, 37, 43, 45, 46, 47, 48, 49, 50, 51, 52}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        inventory.setItem(BUILDER_MASONRY_TAB_SLOT, builderTabItem(BuilderMenuTab.MASONRY, tab == BuilderMenuTab.MASONRY));
        inventory.setItem(BUILDER_BOUNDARIES_TAB_SLOT, builderTabItem(BuilderMenuTab.BOUNDARIES, tab == BuilderMenuTab.BOUNDARIES));
        inventory.setItem(BUILDER_INFO_SLOT, simpleItem(Material.STONECUTTER, "&6Builder's Workbench", List.of(
                "&7Settlement construction station for",
                "&7stonework, borders, openings, and fittings.",
                "&7Builder XP is earned here."
        )));
        inventory.setItem(BUILDER_OPENINGS_TAB_SLOT, builderTabItem(BuilderMenuTab.OPENINGS, tab == BuilderMenuTab.OPENINGS));
        inventory.setItem(BUILDER_FIXTURES_TAB_SLOT, builderTabItem(BuilderMenuTab.FIXTURES, tab == BuilderMenuTab.FIXTURES));
        inventory.setItem(45, simpleItem(Material.BOOK, "&fBuilder Notes", builderNotes(tab)));
        if (currentPage > 1) {
            inventory.setItem(48, simpleItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of("&7Go to page " + (currentPage - 1) + ".")));
        }
        inventory.setItem(49, simpleItem(tab.icon, "&6" + tab.displayName, List.of(
                "&7" + entries.size() + " grouped builder options.",
                "&7Single pieces craft directly.",
                "&7Variant families open one extra page."
        )));
        if (currentPage < maxPage) {
            inventory.setItem(50, simpleItem(Material.TIPPED_ARROW, "&eNext Page", List.of("&7Go to page " + (currentPage + 1) + ".")));
        }
        inventory.setItem(53, simpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        int startIndex = (currentPage - 1) * BUILDER_RECIPE_SLOTS.length;
        for (int i = 0; i < BUILDER_RECIPE_SLOTS.length; i++) {
            int entryIndex = startIndex + i;
            if (entryIndex >= entries.size()) {
                break;
            }
            BuilderMenuEntry entry = entries.get(entryIndex);
            inventory.setItem(BUILDER_RECIPE_SLOTS[i], builderEntryDisplayItem(player, entry));
        }
        return applyLayout ? applySavedLayout(tab.screenKey, inventory) : inventory;
    }

    private Inventory buildCampfireMenuInventory(Player player, String tabKey, int page, boolean applyLayout) {
        List<RecipeDefinition> recipes = campfireRecipes(tabKey);
        int maxPage = Math.max(1, (int) Math.ceil(recipes.size() / (double) CAMPFIRE_RECIPE_SLOTS.length));
        int currentPage = Math.max(1, Math.min(page, maxPage));
        boolean heartyTab = "hearty".equalsIgnoreCase(tabKey);
        Inventory inventory = Bukkit.createInventory(new CampfireMenuHolder(heartyTab ? "hearty" : "rough", currentPage), 54, plugin.legacyComponent("&8Campfire"));
        applyWorkbenchFrame(inventory);
        fillSlots(inventory, new int[]{10, 11, 12, 13, 14, 15, 16}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        fillSlots(inventory, new int[]{19, 25, 28, 34, 37, 43, 45, 46, 47, 48, 49, 50, 51, 52}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        inventory.setItem(CAMPFIRE_ROAST_TAB_SLOT, simpleItem(Material.BREAD, heartyTab ? "&7Rough Meals" : "&6Rough Meals", List.of(
                "&7Cheap camp food made from scraps,",
                "&7roots, grain, and rough fire prep.",
                heartyTab ? "&eClick to open." : "&eCurrent tab."
        )));
        inventory.setItem(CAMPFIRE_INFO_SLOT, simpleItem(Material.CAMPFIRE, "&6Campfire", List.of(
                "&7Low-tech food station for travel,",
                "&7camp life, and simple field cooking.",
                "&7Only custom dishes are made here."
        )));
        inventory.setItem(CAMPFIRE_MEALS_TAB_SLOT, simpleItem(Material.RABBIT_STEW, heartyTab ? "&6Field Meals" : "&7Field Meals", List.of(
                "&7Better mixed dishes using more crops",
                "&7and ingredients for longer travel food.",
                heartyTab ? "&eCurrent tab." : "&eClick to open."
        )));
        inventory.setItem(45, simpleItem(Material.BOOK, "&fCamp Notes", List.of(
                "&7Rough meals are cheap and weak.",
                "&7Field meals cost more ingredients,",
                "&7but hold hunger longer."
        )));
        if (currentPage > 1) {
            inventory.setItem(48, simpleItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of("&7Go to page " + (currentPage - 1) + ".")));
        }
        inventory.setItem(49, simpleItem(heartyTab ? Material.RABBIT_STEW : Material.BREAD, heartyTab ? "&6Field Board" : "&6Rough Board", List.of(
                heartyTab ? "&7Stronger travel meals." : "&7Poor but cheap camp food."
        )));
        if (currentPage < maxPage) {
            inventory.setItem(50, simpleItem(Material.TIPPED_ARROW, "&eNext Page", List.of("&7Go to page " + (currentPage + 1) + ".")));
        }
        inventory.setItem(53, simpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        placeEntries(inventory, recipes, currentPage, recipe -> recipeDisplayItem(player, benches.get("campfire_bench"), recipe), CAMPFIRE_RECIPE_SLOTS);
        return applyLayout ? applySavedLayout(heartyTab ? "campfire_hearty" : "campfire_rough", inventory) : inventory;
    }

    private Inventory buildFarmerMenuInventory(Player player, String tabKey, int page, boolean applyLayout) {
        List<RecipeDefinition> recipes = farmerRecipes(tabKey);
        int maxPage = Math.max(1, (int) Math.ceil(recipes.size() / (double) FARMER_RECIPE_SLOTS.length));
        int currentPage = Math.max(1, Math.min(page, maxPage));
        boolean preservesTab = "preserves".equalsIgnoreCase(tabKey);
        Inventory inventory = Bukkit.createInventory(new FarmerMenuHolder(preservesTab ? "preserves" : "meals", currentPage), 54, plugin.legacyComponent("&8Farmer's Workbench"));
        applyWorkbenchFrame(inventory);
        fillSlots(inventory, new int[]{10, 11, 12, 13, 14, 15, 16}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        fillSlots(inventory, new int[]{19, 25, 28, 34, 37, 43, 45, 46, 47, 48, 49, 50, 51, 52}, Material.GRAY_STAINED_GLASS_PANE, "&7");
        inventory.setItem(FARMER_MEALS_TAB_SLOT, simpleItem(Material.BREAD, preservesTab ? "&7Prepared Meals" : "&6Prepared Meals", List.of(
                "&7Better than campfire food and made",
                "&7from fuller crop combinations.",
                preservesTab ? "&eClick to open." : "&eCurrent tab."
        )));
        inventory.setItem(FARMER_INFO_SLOT, simpleItem(Material.BARREL, "&6Farmer's Workbench", List.of(
                "&7Mid-tier food station for field kitchens,",
                "&7ration prep, and preserved supplies.",
                "&7Vanilla food crafting is replaced here."
        )));
        inventory.setItem(FARMER_PRESERVES_TAB_SLOT, simpleItem(Material.HONEY_BOTTLE, preservesTab ? "&6Preserves" : "&7Preserves", List.of(
                "&7Packed goods and preserved staples",
                "&7for longer trips and work days.",
                preservesTab ? "&eCurrent tab." : "&eClick to open."
        )));
        inventory.setItem(45, simpleItem(Material.BOOK, "&fKitchen Notes", List.of(
                "&7Prepared meals cost more crops but",
                "&7feed longer than campfire food.",
                "&7Preserves are stable travel support."
        )));
        if (currentPage > 1) {
            inventory.setItem(48, simpleItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of("&7Go to page " + (currentPage - 1) + ".")));
        }
        inventory.setItem(49, simpleItem(preservesTab ? Material.HONEY_BOTTLE : Material.BREAD, preservesTab ? "&6Pantry Board" : "&6Kitchen Board", List.of(
                preservesTab ? "&7Field-ready preserved goods." : "&7Prepared crop meals and work food."
        )));
        if (currentPage < maxPage) {
            inventory.setItem(50, simpleItem(Material.TIPPED_ARROW, "&eNext Page", List.of("&7Go to page " + (currentPage + 1) + ".")));
        }
        inventory.setItem(53, simpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        placeEntries(inventory, recipes, currentPage, recipe -> recipeDisplayItem(player, benches.get("farmer_workbench"), recipe), FARMER_RECIPE_SLOTS);
        return applyLayout ? applySavedLayout(preservesTab ? "farmer_preserves" : "farmer_meals", inventory) : inventory;
    }

    private ItemStack displayWorkbenchRecipe(Material material, String name) {
        RecipeDefinition recipe = findRecipeByResult("workbench", material);
        if (recipe == null) {
            recipe = findRecipeByResultAnywhere(material);
        }
        ItemStack icon = new ItemStack(material);
        if (recipe == null) {
            return simpleItem(material, name, List.of("&7Recipe not available right now."));
        }
        return recipeDisplayOverride(icon, name, recipe);
    }

    private ItemStack displayWorkbenchContentRecipe(String contentId, Material fallbackMaterial, String name) {
        RecipeDefinition recipe = findCustomRecipeByContentId(contentId);
        if (recipe == null) {
            return simpleItem(fallbackMaterial, name, List.of("&7Recipe not available right now."));
        }
        ItemStack icon = createContentItem(contentId, 1);
        if (icon == null) {
            icon = new ItemStack(fallbackMaterial);
        }
        return recipeDisplayOverride(icon, name, recipe);
    }

    private Inventory buildEditableScreen(Player player, String screenKey, boolean applyLayout) {
        return switch (normalize(screenKey)) {
            case "catalog_root" -> buildCatalogRootInventory(applyLayout);
            case "workbench_basic_tools" -> buildWorkbenchBasicToolsInventory(applyLayout);
            case "workbench_stations" -> buildWorkbenchStationsInventory(applyLayout);
            case "workbench_blocks" -> buildWorkbenchBlocksInventory(applyLayout);
            case "builder_masonry" -> buildBuilderMenuInventory(player, BuilderMenuTab.MASONRY, 1, applyLayout);
            case "builder_boundaries" -> buildBuilderMenuInventory(player, BuilderMenuTab.BOUNDARIES, 1, applyLayout);
            case "builder_openings" -> buildBuilderMenuInventory(player, BuilderMenuTab.OPENINGS, 1, applyLayout);
            case "builder_fixtures" -> buildBuilderMenuInventory(player, BuilderMenuTab.FIXTURES, 1, applyLayout);
            case "campfire_rough" -> buildCampfireMenuInventory(player, "rough", 1, applyLayout);
            case "campfire_hearty" -> buildCampfireMenuInventory(player, "hearty", 1, applyLayout);
            case "farmer_meals" -> buildFarmerMenuInventory(player, "meals", 1, applyLayout);
            case "farmer_preserves" -> buildFarmerMenuInventory(player, "preserves", 1, applyLayout);
            case "refiner_processing" -> buildRefinerEditorPreview(applyLayout);
            default -> null;
        };
    }

    private Inventory applySavedLayout(String screenKey, Inventory inventory) {
        ItemStack[] saved = guiLayoutOverrides.get(normalize(screenKey));
        if (saved == null || saved.length == 0) {
            return inventory;
        }
        List<LayoutEntry> baseEntries = new ArrayList<>();
        ItemStack[] baseContents = cloneContents(inventory.getContents());
        for (int slot = 0; slot < baseContents.length; slot++) {
            ItemStack itemStack = baseContents[slot];
            if (itemStack == null || itemStack.getType().isAir()) {
                continue;
            }
            baseEntries.add(new LayoutEntry(slot, layoutItemKey(itemStack), itemStack));
        }
        ItemStack[] rebuilt = new ItemStack[inventory.getSize()];
        boolean[] usedBaseEntries = new boolean[baseEntries.size()];
        for (int slot = 0; slot < Math.min(saved.length, rebuilt.length); slot++) {
            ItemStack savedItem = saved[slot];
            if (savedItem == null || savedItem.getType().isAir()) {
                continue;
            }
            String savedKey = layoutItemKey(savedItem);
            int matchedIndex = -1;
            for (int index = 0; index < baseEntries.size(); index++) {
                if (!usedBaseEntries[index] && Objects.equals(baseEntries.get(index).key, savedKey)) {
                    matchedIndex = index;
                    break;
                }
            }
            if (matchedIndex >= 0) {
                usedBaseEntries[matchedIndex] = true;
                rebuilt[slot] = baseEntries.get(matchedIndex).itemStack.clone();
            } else {
                rebuilt[slot] = savedItem.clone();
            }
        }
        inventory.setContents(rebuilt);
        return inventory;
    }

    private String layoutItemKey(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return "";
        }
        String contentId = getContentId(itemStack);
        if (contentId != null && !contentId.isBlank()) {
            return "content:" + normalize(contentId);
        }
        ItemMeta meta = itemStack.getItemMeta();
        String name = meta != null && meta.hasDisplayName() && meta.displayName() != null
                ? net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(meta.displayName())
                : plugin.formatMaterialName(itemStack.getType());
        return "item:" + itemStack.getType().name() + ":" + normalize(name);
    }

    private boolean contentsEqual(ItemStack[] first, ItemStack[] second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null || first.length != second.length) {
            return false;
        }
        for (int index = 0; index < first.length; index++) {
            if (!Objects.equals(first[index], second[index])) {
                return false;
            }
        }
        return true;
    }

    private ItemStack[] cloneContents(ItemStack[] contents) {
        if (contents == null) {
            return new ItemStack[0];
        }
        ItemStack[] clone = new ItemStack[contents.length];
        for (int index = 0; index < contents.length; index++) {
            clone[index] = contents[index] == null ? null : contents[index].clone();
        }
        return clone;
    }

    private String prettyScreenName(String screenKey) {
        return switch (normalize(screenKey)) {
            case "catalog_root" -> "Terra Catalog";
            case "workbench_basic_tools" -> "Workbench Basic Tools";
            case "workbench_stations" -> "Workbench Stations";
            case "workbench_blocks" -> "Workbench Blocks";
            case "builder_masonry" -> "Builder Masonry";
            case "builder_boundaries" -> "Builder Boundaries";
            case "builder_openings" -> "Builder Openings";
            case "builder_fixtures" -> "Builder Fixtures";
            case "campfire_rough" -> "Campfire Rough Meals";
            case "campfire_hearty" -> "Campfire Field Meals";
            case "farmer_meals" -> "Farmer Prepared Meals";
            case "farmer_preserves" -> "Farmer Preserves";
            case "refiner_processing" -> "Refiner Processing";
            default -> "Unknown Screen";
        };
    }

    private boolean hasAnyLockedEditorSlots(String screenKey) {
        return "refiner_processing".equalsIgnoreCase(normalize(screenKey));
    }

    private boolean isLockedEditorSlot(String screenKey, int slot) {
        if ("refiner_processing".equalsIgnoreCase(normalize(screenKey))) {
            return containsSlot(REFINER_INPUT_SLOTS, slot)
                    || containsSlot(REFINER_PROGRESS_SLOTS, slot)
                    || containsSlot(REFINER_OUTPUT_PRIMARY_SLOTS, slot)
                    || containsSlot(REFINER_OUTPUT_SECONDARY_SLOTS, slot);
        }
        return false;
    }

    private boolean containsSlot(int[] slots, int slot) {
        return IntStream.of(slots).anyMatch(candidate -> candidate == slot);
    }

    private ItemStack recipeDisplayOverride(ItemStack icon, String name, RecipeDefinition recipe) {
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) {
            return icon;
        }
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(plugin.legacyComponent("&7Output: &f" + recipe.resultAmount + "x"));
        lore.add(plugin.legacyComponent("&8"));
        for (RecipeIngredient ingredient : recipe.ingredients) {
            lore.add(plugin.legacyComponent("&8- &f" + ingredient.amount + "x " + ingredient.displayName(this)));
        }
        lore.add(plugin.legacyComponent("&8"));
        lore.add(plugin.legacyComponent("&eClick to craft."));
        meta.displayName(plugin.legacyComponent(name));
        meta.lore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    private FurnaceBenchState createFurnaceState(WorldBlockKey blockKey) {
        Inventory inventory = Bukkit.createInventory(new FurnaceBenchHolder(blockKey), 54, plugin.legacyComponent("&8Terra Furnace"));
        FurnaceBenchState state = new FurnaceBenchState(blockKey, inventory, new int[FURNACE_INPUT_SLOTS.length], new SmeltingRecipeDefinition[FURNACE_INPUT_SLOTS.length], new int[1]);
        initializeFurnaceLayout(state);
        return state;
    }

    private FurnaceBenchState createStarterHubFurnaceState(StarterHubBenchKey key) {
        Inventory inventory = Bukkit.createInventory(new StarterHubFurnaceBenchHolder(key.playerId, key.blockKey), 54, plugin.legacyComponent("&8Terra Furnace"));
        FurnaceBenchState state = new FurnaceBenchState(key.blockKey, inventory, new int[FURNACE_INPUT_SLOTS.length], new SmeltingRecipeDefinition[FURNACE_INPUT_SLOTS.length], new int[1]);
        initializeFurnaceLayout(state);
        return state;
    }

    private RefinerBenchState createRefinerState(WorldBlockKey blockKey) {
        Inventory inventory = Bukkit.createInventory(new RefinerBenchHolder(blockKey), 54, plugin.legacyComponent("&8Refiner"));
        RefinerBenchState state = new RefinerBenchState(
                blockKey,
                inventory,
                new int[REFINER_INPUT_SLOTS.length],
                new RefinerRecipeDefinition[REFINER_INPUT_SLOTS.length],
                refinerUpgradeStates.computeIfAbsent(blockKey, ignored -> new RefinerUpgradeState())
        );
        initializeRefinerLayout(state);
        return state;
    }

    private RefinerBenchState createStarterHubRefinerState(StarterHubBenchKey key) {
        Inventory inventory = Bukkit.createInventory(new StarterHubRefinerBenchHolder(key.playerId, key.blockKey), 54, plugin.legacyComponent("&8Refiner"));
        RefinerBenchState state = new RefinerBenchState(
                key.blockKey,
                inventory,
                new int[REFINER_INPUT_SLOTS.length],
                new RefinerRecipeDefinition[REFINER_INPUT_SLOTS.length],
                new RefinerUpgradeState()
        );
        initializeRefinerLayout(state);
        return state;
    }

    private void initializeFurnaceLayout(FurnaceBenchState state) {
        fill(state.inventory, Material.BLACK_STAINED_GLASS_PANE, "&8");
        clearSlots(state.inventory, FURNACE_INPUT_SLOTS);
        clearSlots(state.inventory, FURNACE_PROGRESS_SLOTS);
        clearSlots(state.inventory, FURNACE_OUTPUT_SLOTS);
        state.inventory.setItem(FURNACE_FUEL_SLOT, null);
        state.inventory.setItem(4, simpleItem(Material.FURNACE, "&cTerra Furnace", List.of(
                "&7Three smelting bays process loads in parallel.",
                "&7Each bay works slower than a normal furnace.",
                "&7Place smeltables in the left slots, fuel in the middle, and collect output on the right."
        )));
        state.inventory.setItem(45, simpleItem(Material.RAW_IRON, "&fOre Bays", List.of(
                "&7Supports Terra ores and vanilla smeltables.",
                "&7Cook time is tuned for heavier industrial batches."
        )));
        state.inventory.setItem(49, simpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        state.inventory.setItem(53, simpleItem(Material.CLOCK, "&eProcessing Rate", List.of(
                "&7300 ticks base per load.",
                "&7Runs 50% slower than a normal furnace."
        )));
        refreshFurnaceGui(state);
    }

    private void initializeRefinerLayout(RefinerBenchState state) {
        populateRefinerShell(state.inventory, true, false);
        refreshRefinerGui(state);
    }

    private Inventory buildRefinerEditorPreview(boolean applyLayout) {
        Inventory inventory = Bukkit.createInventory(null, 54, plugin.legacyComponent("&8Refiner"));
        populateRefinerShell(inventory, applyLayout, true);
        return inventory;
    }

    private void populateRefinerShell(Inventory inventory, boolean applyLayout, boolean editorPreview) {
        fill(inventory, Material.BLACK_STAINED_GLASS_PANE, "&8");
        clearSlots(inventory, REFINER_INPUT_SLOTS);
        clearSlots(inventory, REFINER_PROGRESS_SLOTS);
        clearSlots(inventory, REFINER_OUTPUT_PRIMARY_SLOTS);
        clearSlots(inventory, REFINER_OUTPUT_SECONDARY_SLOTS);
        inventory.setItem(4, simpleItem(Material.GRINDSTONE, "&8Refiner", List.of(
                "&7Load ore, timber, stone, grain, or",
                "&7earth into the upper rack to process it.",
                "&7Upgrade placed refiners to improve rate",
                "&7and output quality."
        )));
        inventory.setItem(45, simpleItem(Material.RAW_IRON, "&fInput Rack", List.of(
                "&7Row 2 slots 3 to 7 are input lanes.",
                "&7Raw ore and processing stock go here."
        )));
        inventory.setItem(46, simpleItem(Material.LIGHTNING_ROD, "&bThroughput Upgrade", List.of(
                "&7Install a throughput kit on a placed",
                "&7refiner to shorten lane process time."
        )));
        inventory.setItem(47, simpleItem(Material.BRUSH, "&dQuality Upgrade", List.of(
                "&7Install a quality kit on a placed",
                "&7refiner to roll cleaner outputs."
        )));
        inventory.setItem(49, simpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        inventory.setItem(53, simpleItem(Material.CLOCK, "&eRefiner Status", List.of(
                "&7Placed refiners show their station",
                "&7upgrade state here."
        )));
        if (editorPreview) {
            for (int slot : REFINER_INPUT_SLOTS) {
                inventory.setItem(slot, simpleItem(Material.RAW_IRON, "&7Input Lane", List.of("&7Processing input appears here.")));
            }
            for (int slot : REFINER_PROGRESS_SLOTS) {
                inventory.setItem(slot, simpleItem(Material.CLOCK, "&eCountdown", List.of("&7Live timer appears here.")));
            }
            for (int slot : REFINER_OUTPUT_PRIMARY_SLOTS) {
                inventory.setItem(slot, simpleItem(Material.IRON_NUGGET, "&fOutput Lane", List.of("&7Primary output buffer.")));
            }
            for (int slot : REFINER_OUTPUT_SECONDARY_SLOTS) {
                inventory.setItem(slot, simpleItem(Material.IRON_NUGGET, "&fOutput Lane", List.of("&7Refined output appears here.")));
            }
        }
        if (applyLayout) {
            applySavedLayout("refiner_processing", inventory);
        }
        clearSlots(inventory, REFINER_INPUT_SLOTS);
        clearSlots(inventory, REFINER_PROGRESS_SLOTS);
        clearSlots(inventory, REFINER_OUTPUT_PRIMARY_SLOTS);
        clearSlots(inventory, REFINER_OUTPUT_SECONDARY_SLOTS);
        if (editorPreview) {
            for (int slot : REFINER_INPUT_SLOTS) {
                inventory.setItem(slot, simpleItem(Material.RAW_IRON, "&7Input Lane", List.of("&7Processing input appears here.")));
            }
            for (int slot : REFINER_PROGRESS_SLOTS) {
                inventory.setItem(slot, simpleItem(Material.CLOCK, "&eCountdown", List.of("&7Live timer appears here.")));
            }
            for (int slot : REFINER_OUTPUT_PRIMARY_SLOTS) {
                inventory.setItem(slot, simpleItem(Material.IRON_NUGGET, "&fOutput Lane", List.of("&7Primary output buffer.")));
            }
            for (int slot : REFINER_OUTPUT_SECONDARY_SLOTS) {
                inventory.setItem(slot, simpleItem(Material.IRON_NUGGET, "&fOutput Lane", List.of("&7Refined output appears here.")));
            }
        }
    }

    private void refreshFurnaceGui(FurnaceBenchState state) {
        updateFurnaceFuelStatusItem(state);
        for (int lane = 0; lane < FURNACE_INPUT_SLOTS.length; lane++) {
            updateFurnaceProgressItem(state, lane);
        }
    }

    private void refreshRefinerGui(RefinerBenchState state) {
        updateRefinerUpgradeItems(state);
        for (int lane = 0; lane < REFINER_INPUT_SLOTS.length; lane++) {
            updateRefinerProgressItem(state, lane);
        }
    }

    private void updateFurnaceFuelStatusItem(FurnaceBenchState state) {
        ItemStack fuel = state.inventory.getItem(FURNACE_FUEL_SLOT);
        int burnTicks = state.fuelTicksRemaining[0];
        List<String> lore = new ArrayList<>();
        if (fuel == null || fuel.getType().isAir()) {
            lore.add("&7Place coal, charcoal, logs,");
            lore.add("&7or other furnace fuel here.");
        } else {
            lore.add("&7Fuel: &f" + plugin.formatMaterialName(fuel.getType()));
            lore.add("&7Burn time left: &f" + burnTicks + " ticks");
            lore.add(progressBarLine(Math.max(0, Math.min(burnTicks, fuelBurnTicks(fuel.getType()))), Math.max(1, fuelBurnTicks(fuel.getType()))));
        }
        state.inventory.setItem(FURNACE_FUEL_STATUS_SLOT, simpleItem(burnTicks > 0 ? Material.BLAZE_POWDER : Material.COAL, burnTicks > 0 ? "&6Fuel Burning" : "&7Fuel Slot", lore));
    }

    private void updateFurnaceProgressItem(FurnaceBenchState state, int lane) {
        ItemStack input = state.inventory.getItem(FURNACE_INPUT_SLOTS[lane]);
        SmeltingRecipeDefinition recipe = findSmeltingRecipe(input);
        int progress = state.progressTicks[lane];
        Material icon = recipe == null ? Material.GRAY_STAINED_GLASS_PANE : Material.ORANGE_STAINED_GLASS_PANE;
        String title = recipe == null ? "&7Idle Bay" : "&6Smelting Bay";
        List<String> lore = new ArrayList<>();
        if (recipe == null) {
            lore.add("&7Place ore, raw metal, food, or");
            lore.add("&7other smeltables in the input slot.");
        } else {
            int totalTicks = recipe.cookTicks;
            int percent = Math.min(100, (int) Math.round((progress / (double) totalTicks) * 100.0D));
            lore.add("&7Input: &f" + recipe.inputDisplayName(this));
            lore.add("&7Output: &f" + recipe.outputDisplayName(this));
            lore.add("&7Progress: &f" + percent + "%");
            lore.add(progressBarLine(progress, totalTicks));
        }
        state.inventory.setItem(FURNACE_PROGRESS_SLOTS[lane], simpleItem(icon, title, lore));
    }

    private void updateRefinerProgressItem(RefinerBenchState state, int lane) {
        ItemStack input = state.inventory.getItem(REFINER_INPUT_SLOTS[lane]);
        RefinerRecipeDefinition recipe = findRefinerRecipe(input);
        int progress = state.progressTicks[lane];
        Material icon = recipe == null ? Material.GRAY_STAINED_GLASS_PANE : Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        String title = recipe == null ? "&7Idle Lane" : "&bRefining";
        List<String> lore = new ArrayList<>();
        if (recipe == null) {
            lore.add("&7Insert ore, timber, stone,");
            lore.add("&7sand, clay, or grain above.");
        } else {
            int totalTicks = applyRefinerThroughputTicks(recipe.processTicks(), state.upgradeState);
            int secondsRemaining = Math.max(0, (int) Math.ceil((totalTicks - progress) / 20.0D));
            lore.add("&7Input: &f" + recipe.inputDisplayName(this));
            lore.add("&7Output: &f" + recipe.outputDisplayName(this));
            lore.add("&7Ready in: &f" + secondsRemaining + "s");
            lore.add("&7Quality floor: &f" + state.upgradeState.qualityFloor().label);
            lore.add(progressBarLine(progress, totalTicks));
        }
        state.inventory.setItem(REFINER_PROGRESS_SLOTS[lane], simpleItem(icon, title, lore));
    }

    private void updateRefinerUpgradeItems(RefinerBenchState state) {
        RefinerUpgradeState upgrades = state.upgradeState;
        state.inventory.setItem(46, simpleItem(
                Material.LIGHTNING_ROD,
                upgrades.throughputLevel >= RefinerUpgradeState.MAX_LEVEL ? "&bThroughput Upgrade Maxed" : "&bThroughput Upgrade",
                buildRefinerUpgradeLore(
                        "refiner_throughput_kit",
                        upgrades.throughputLevel,
                        "&712% faster lane time per level.",
                        "&7Current speed: &f" + refinerRateText(upgrades)
                )
        ));
        state.inventory.setItem(47, simpleItem(
                Material.BRUSH,
                upgrades.qualityLevel >= RefinerUpgradeState.MAX_LEVEL ? "&dQuality Upgrade Maxed" : "&dQuality Upgrade",
                buildRefinerUpgradeLore(
                        "refiner_quality_kit",
                        upgrades.qualityLevel,
                        "&7Raises the minimum output tier.",
                        "&7Current floor: &f" + upgrades.qualityFloor().label
                )
        ));
        state.inventory.setItem(53, simpleItem(Material.CLOCK, "&eRefiner Status", List.of(
                "&7Throughput: &f" + upgrades.throughputLevel + "/" + RefinerUpgradeState.MAX_LEVEL,
                "&7Quality: &f" + upgrades.qualityLevel + "/" + RefinerUpgradeState.MAX_LEVEL,
                "&7Rate: &f" + refinerRateText(upgrades),
                "&7Floor: &f" + upgrades.qualityFloor().label
        )));
    }

    private List<String> buildRefinerUpgradeLore(String contentId, int level, String effectLine, String currentLine) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Level: &f" + level + "/" + RefinerUpgradeState.MAX_LEVEL);
        lore.add(effectLine);
        lore.add(currentLine);
        if (level >= RefinerUpgradeState.MAX_LEVEL) {
            lore.add("&aThis track is fully upgraded.");
        } else {
            lore.add("&8");
            lore.add("&7Consume: &f1x " + readableContentName(contentId));
            lore.add("&eClick to install the upgrade kit.");
        }
        return lore;
    }

    private String refinerRateText(RefinerUpgradeState upgrades) {
        double modifier = 1.0D - (upgrades.throughputLevel * RefinerUpgradeState.THROUGHPUT_REDUCTION_PER_LEVEL);
        return Math.max(1, (int) Math.round(modifier * 100.0D)) + "% of base time";
    }

    private int applyRefinerThroughputTicks(int baseTicks, RefinerUpgradeState upgrades) {
        if (upgrades == null) {
            return Math.max(FURNACE_TICK_INTERVAL, baseTicks);
        }
        double modifier = 1.0D - (upgrades.throughputLevel * RefinerUpgradeState.THROUGHPUT_REDUCTION_PER_LEVEL);
        return Math.max(FURNACE_TICK_INTERVAL, (int) Math.round(baseTicks * Math.max(0.45D, modifier)));
    }

    private void handleRefinerUpgradeClick(Player player, RefinerBenchState state, int slot) {
        if (player == null || state == null) {
            return;
        }
        if (!refinerUpgradeStates.containsKey(state.blockKey)) {
            player.sendMessage(plugin.colorize("&cStarter hub refiners cannot be upgraded."));
            return;
        }
        RefinerUpgradeState upgrades = state.upgradeState;
        if (slot == 46) {
            if (upgrades.throughputLevel >= RefinerUpgradeState.MAX_LEVEL) {
                player.sendMessage(plugin.colorize("&cThis refiner already has maximum throughput."));
                return;
            }
            if (removeOneContentItem(player, "refiner_throughput_kit")) {
                upgrades.throughputLevel++;
                persistRefinerUpgradeState(state.blockKey, upgrades);
                player.sendMessage(plugin.colorize("&aInstalled a throughput kit. New level: &f" + upgrades.throughputLevel + "&a."));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.9F, 1.15F);
                refreshRefinerGui(state);
            } else {
                player.sendMessage(plugin.colorize("&cYou need a &fRefiner Throughput Kit&c to install that upgrade."));
            }
            return;
        }
        if (upgrades.qualityLevel >= RefinerUpgradeState.MAX_LEVEL) {
            player.sendMessage(plugin.colorize("&cThis refiner already has maximum quality tooling."));
            return;
        }
        if (removeOneContentItem(player, "refiner_quality_kit")) {
            upgrades.qualityLevel++;
            persistRefinerUpgradeState(state.blockKey, upgrades);
            player.sendMessage(plugin.colorize("&aInstalled a quality kit. New level: &f" + upgrades.qualityLevel + "&a."));
            player.playSound(player.getLocation(), Sound.ITEM_BRUSH_BRUSHING_GENERIC, 0.9F, 1.1F);
            refreshRefinerGui(state);
        } else {
            player.sendMessage(plugin.colorize("&cYou need a &fRefiner Quality Kit&c to install that upgrade."));
        }
    }

    private String progressBarLine(int progress, int totalTicks) {
        int filled = totalTicks <= 0 ? 0 : Math.max(0, Math.min(10, (int) Math.round((progress / (double) totalTicks) * 10.0D)));
        StringBuilder builder = new StringBuilder("&8[");
        for (int i = 0; i < 10; i++) {
            builder.append(i < filled ? "&6|" : "&7|");
        }
        return builder + "&8]";
    }

    private void clearSlots(Inventory inventory, int[] slots) {
        for (int slot : slots) {
            inventory.setItem(slot, null);
        }
    }

    private void applyWorkbenchFrame(Inventory inventory) {
        clearSlots(inventory, IntStream.range(0, inventory.getSize()).toArray());
        fillSlots(inventory, new int[]{0, 1, 2, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45}, Material.BLACK_STAINED_GLASS_PANE, "&8");
    }

    private void fillBenchLayout(Inventory inventory) {
        ItemStack black = simpleItem(Material.BLACK_STAINED_GLASS_PANE, "&8", List.of());
        ItemStack gray = simpleItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, black);
        }
        for (int slot : new int[]{10, 11, 12, 13, 14, 15, 16, 19, 25, 28, 34}) {
            inventory.setItem(slot, gray);
        }
        clearGrid(inventory);
    }

    private <T> void placeEntries(Inventory inventory, List<T> entries, int page, java.util.function.Function<T, ItemStack> mapper) {
        int startIndex = (page - 1) * GRID_SLOTS.length;
        for (int i = 0; i < GRID_SLOTS.length; i++) {
            int entryIndex = startIndex + i;
            if (entryIndex >= entries.size()) {
                break;
            }
            inventory.setItem(GRID_SLOTS[i], mapper.apply(entries.get(entryIndex)));
        }
    }

    private <T> void placeEntries(Inventory inventory, List<T> entries, int page, java.util.function.Function<T, ItemStack> mapper, int[] slots) {
        int startIndex = (page - 1) * slots.length;
        for (int i = 0; i < slots.length; i++) {
            int entryIndex = startIndex + i;
            if (entryIndex >= entries.size()) {
                break;
            }
            inventory.setItem(slots[i], mapper.apply(entries.get(entryIndex)));
        }
    }

    private int slotIndex(int slot, int page) {
        for (int i = 0; i < GRID_SLOTS.length; i++) {
            if (GRID_SLOTS[i] == slot) {
                return ((page - 1) * GRID_SLOTS.length) + i;
            }
        }
        return -1;
    }

    private int slotIndexInArray(int slot, int[] slots, int page) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == slot) {
                return ((page - 1) * slots.length) + i;
            }
        }
        return -1;
    }

    private void startFurnaceRuntime() {
        if (furnaceTickTask != null) {
            furnaceTickTask.cancel();
        }
        furnaceTickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickFurnaces, FURNACE_TICK_INTERVAL, FURNACE_TICK_INTERVAL);
    }

    private void tickFurnaces() {
        for (FurnaceBenchState state : furnaceStates.values()) {
            tickFurnaceState(state);
        }
        for (FurnaceBenchState state : starterHubFurnaceStates.values()) {
            tickFurnaceState(state);
        }
        for (RefinerBenchState state : refinerStates.values()) {
            tickRefinerState(state);
        }
        for (RefinerBenchState state : starterHubRefinerStates.values()) {
            tickRefinerState(state);
        }
    }

    public boolean isInteractiveTerraBench(Block block) {
        if (block == null) {
            return false;
        }
        String placedId = placedBlockIds.get(WorldBlockKey.fromBlock(block));
        return placedId != null && benches.containsKey(placedId);
    }

    public List<String> getWorkbenchQuestKeys() {
        List<String> keys = new ArrayList<>();
        keys.add("terra_workbench_any");
        for (String benchId : benches.keySet()) {
            keys.add("terra_workbench_" + normalize(benchId));
        }
        return keys;
    }

    public List<String> getWorkbenchCollectionQuestKeys() {
        List<String> keys = new ArrayList<>();
        keys.add("terra_workbench_collect_any");
        for (String benchId : benches.keySet()) {
            keys.add("terra_workbench_collect_" + normalize(benchId));
        }
        return keys;
    }

    private boolean isStarterHubBench(Block block) {
        return block != null && plugin.isStarterHubBuildProtected(block.getLocation());
    }

    private void tickFurnaceState(FurnaceBenchState state) {
        boolean consumedFuelThisTick = false;
        for (int lane = 0; lane < FURNACE_INPUT_SLOTS.length; lane++) {
            ItemStack input = state.inventory.getItem(FURNACE_INPUT_SLOTS[lane]);
            ItemStack output = state.inventory.getItem(FURNACE_OUTPUT_SLOTS[lane]);
            SmeltingRecipeDefinition recipe = findSmeltingRecipe(input);
            if (recipe == null || !canAcceptSmeltingOutput(output, recipe)) {
                state.progressTicks[lane] = 0;
                state.activeRecipes[lane] = recipe;
                updateFurnaceProgressItem(state, lane);
                continue;
            }
            if (!Objects.equals(state.activeRecipes[lane], recipe)) {
                state.progressTicks[lane] = 0;
                state.activeRecipes[lane] = recipe;
            }
            if (state.fuelTicksRemaining[0] <= 0 && !consumeFurnaceFuel(state)) {
                updateFurnaceProgressItem(state, lane);
                continue;
            }
            state.progressTicks[lane] += FURNACE_TICK_INTERVAL;
            consumedFuelThisTick = true;
            if (state.progressTicks[lane] >= recipe.cookTicks) {
                finishSmeltingCycle(state, lane, recipe);
                state.progressTicks[lane] = 0;
            }
            updateFurnaceProgressItem(state, lane);
        }
        if (consumedFuelThisTick) {
            state.fuelTicksRemaining[0] = Math.max(0, state.fuelTicksRemaining[0] - FURNACE_TICK_INTERVAL);
        }
        updateFurnaceFuelStatusItem(state);
    }

    private void tickRefinerState(RefinerBenchState state) {
        for (int lane = 0; lane < REFINER_INPUT_SLOTS.length; lane++) {
            ItemStack input = state.inventory.getItem(REFINER_INPUT_SLOTS[lane]);
            RefinerRecipeDefinition recipe = findRefinerRecipe(input);
            if (recipe == null || !canAcceptRefinerOutput(state, lane, recipe)) {
                state.progressTicks[lane] = 0;
                state.activeRecipes[lane] = recipe;
                updateRefinerProgressItem(state, lane);
                continue;
            }
            if (!Objects.equals(state.activeRecipes[lane], recipe)) {
                state.progressTicks[lane] = 0;
                state.activeRecipes[lane] = recipe;
            }
            state.progressTicks[lane] += FURNACE_TICK_INTERVAL;
            if (state.progressTicks[lane] >= applyRefinerThroughputTicks(recipe.processTicks(), state.upgradeState)) {
                finishRefiningCycle(state, lane, recipe);
                state.progressTicks[lane] = 0;
            }
            updateRefinerProgressItem(state, lane);
        }
    }

    private void finishSmeltingCycle(FurnaceBenchState state, int lane, SmeltingRecipeDefinition recipe) {
        int inputSlot = FURNACE_INPUT_SLOTS[lane];
        int outputSlot = FURNACE_OUTPUT_SLOTS[lane];
        ItemStack input = state.inventory.getItem(inputSlot);
        if (input == null || input.getType().isAir()) {
            return;
        }
        input.setAmount(input.getAmount() - 1);
        state.inventory.setItem(inputSlot, input.getAmount() <= 0 ? null : input);
        ItemStack result = recipe.resultItem(this, 1);
        if (result == null || result.getType().isAir()) {
            return;
        }
        ItemStack output = state.inventory.getItem(outputSlot);
        if (output == null || output.getType().isAir()) {
            state.inventory.setItem(outputSlot, result);
            return;
        }
        output.setAmount(output.getAmount() + result.getAmount());
        state.inventory.setItem(outputSlot, output);
    }

    private boolean canAcceptSmeltingOutput(ItemStack output, SmeltingRecipeDefinition recipe) {
        ItemStack result = recipe.resultItem(this, 1);
        if (result == null || result.getType().isAir()) {
            return false;
        }
        if (output == null || output.getType().isAir()) {
            return true;
        }
        if (!canStackTogether(output, result)) {
            return false;
        }
        return output.getAmount() + result.getAmount() <= output.getMaxStackSize();
    }

    private boolean canAcceptRefinerOutput(RefinerBenchState state, int lane, RefinerRecipeDefinition recipe) {
        ItemStack result = recipe.previewResult(this);
        if (result == null || result.getType().isAir()) {
            return false;
        }
        for (int outputSlot : refinerOutputSlotsForLane(lane)) {
            ItemStack output = state.inventory.getItem(outputSlot);
            if (canStackTogether(output, result) && output.getAmount() + result.getAmount() <= output.getMaxStackSize()) {
                return true;
            }
        }
        for (int outputSlot : refinerOutputSlotsForLane(lane)) {
            ItemStack output = state.inventory.getItem(outputSlot);
            if (output == null || output.getType().isAir()) {
                return true;
            }
        }
        return false;
    }

    private boolean consumeFurnaceFuel(FurnaceBenchState state) {
        ItemStack fuel = state.inventory.getItem(FURNACE_FUEL_SLOT);
        if (fuel == null || fuel.getType().isAir()) {
            return false;
        }
        int burnTicks = fuelBurnTicks(fuel.getType());
        if (burnTicks <= 0) {
            return false;
        }
        fuel.setAmount(fuel.getAmount() - 1);
        state.inventory.setItem(FURNACE_FUEL_SLOT, fuel.getAmount() <= 0 ? null : fuel);
        state.fuelTicksRemaining[0] = burnTicks;
        return true;
    }

    private SmeltingRecipeDefinition findSmeltingRecipe(ItemStack input) {
        if (input == null || input.getType().isAir()) {
            return null;
        }
        String contentId = getContentId(input);
        for (SmeltingRecipeDefinition recipe : smeltingRecipes) {
            if (recipe.matches(input.getType(), contentId)) {
                return recipe;
            }
        }
        return null;
    }

    private RefinerRecipeDefinition findRefinerRecipe(ItemStack input) {
        if (input == null || input.getType().isAir()) {
            return null;
        }
        String contentId = getContentId(input);
        if (contentId != null) {
            for (RefinerOreType oreType : RefinerOreType.values()) {
                if (oreType.rawContentId.equalsIgnoreCase(contentId)) {
                    return RefinerRecipeDefinition.forOre(oreType);
                }
            }
            return null;
        }
        if (input.getType() == Material.GRAVEL) {
            return RefinerRecipeDefinition.gravel();
        }
        if (input.getType() == Material.SAND || input.getType() == Material.RED_SAND) {
            return RefinerRecipeDefinition.washedSand(input.getType());
        }
        if (input.getType() == Material.CLAY_BALL) {
            return RefinerRecipeDefinition.washedClay();
        }
        if (input.getType() == Material.WHEAT) {
            return RefinerRecipeDefinition.flourSack();
        }
        if (isRefinerTimberInput(input.getType())) {
            return RefinerRecipeDefinition.timberBundle(input.getType());
        }
        if (isRefinerBeamInput(input.getType())) {
            return RefinerRecipeDefinition.supportBeam(input.getType());
        }
        if (isRefinerStoneInput(input.getType())) {
            return RefinerRecipeDefinition.stoneAggregate(input.getType());
        }
        return null;
    }

    private boolean isRefinerTimberInput(Material material) {
        return switch (material) {
            case OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG, MANGROVE_LOG, CHERRY_LOG -> true;
            default -> false;
        };
    }

    private boolean isRefinerBeamInput(Material material) {
        return switch (material) {
            case STRIPPED_OAK_LOG, STRIPPED_SPRUCE_LOG, STRIPPED_BIRCH_LOG, STRIPPED_JUNGLE_LOG,
                    STRIPPED_ACACIA_LOG, STRIPPED_DARK_OAK_LOG, STRIPPED_MANGROVE_LOG, STRIPPED_CHERRY_LOG -> true;
            default -> false;
        };
    }

    private boolean isRefinerStoneInput(Material material) {
        return switch (material) {
            case COBBLESTONE, STONE, ANDESITE, DIORITE, GRANITE, TUFF -> true;
            default -> false;
        };
    }

    private boolean canStackTogether(ItemStack first, ItemStack second) {
        if (first == null || second == null || first.getType() != second.getType()) {
            return false;
        }
        String firstContentId = getContentId(first);
        String secondContentId = getContentId(second);
        if (!Objects.equals(firstContentId, secondContentId)) {
            return false;
        }
        return getItemQuality(first) == getItemQuality(second);
    }

    private boolean removeOneContentItem(Player player, String contentId) {
        if (player == null || contentId == null || contentId.isBlank()) {
            return false;
        }
        String normalized = normalize(contentId);
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack itemStack = contents[slot];
            if (itemStack == null || itemStack.getType().isAir()) {
                continue;
            }
            if (!matchesContentItem(itemStack, normalized)) {
                continue;
            }
            itemStack.setAmount(itemStack.getAmount() - 1);
            player.getInventory().setItem(slot, itemStack.getAmount() <= 0 ? null : itemStack);
            player.updateInventory();
            return true;
        }
        return false;
    }

    private String readableContentName(String contentId) {
        TerraContentDefinition definition = contentDefinitions.get(normalize(contentId));
        return definition != null ? definition.displayName : contentId;
    }

    private void persistRefinerUpgradeState(WorldBlockKey blockKey, RefinerUpgradeState upgradeState) {
        if (blockKey == null || upgradeState == null) {
            return;
        }
        refinerUpgradeStates.put(blockKey, upgradeState);
        saveRefinerUpgrades();
    }

    private TerraItemQuality rollRefinerOutputQuality(RefinerUpgradeState upgrades) {
        double roll = Math.random();
        if (upgrades == null) {
            return roll < 0.12D ? TerraItemQuality.FINE : TerraItemQuality.STANDARD;
        }
        return switch (upgrades.qualityLevel) {
            case 0 -> roll < 0.12D ? TerraItemQuality.FINE : TerraItemQuality.STANDARD;
            case 1 -> roll < 0.10D ? TerraItemQuality.EXCEPTIONAL : (roll < 0.55D ? TerraItemQuality.FINE : TerraItemQuality.STANDARD);
            case 2 -> roll < 0.18D ? TerraItemQuality.EXCEPTIONAL : (roll < 0.78D ? TerraItemQuality.FINE : TerraItemQuality.STANDARD);
            default -> roll < 0.28D ? TerraItemQuality.EXCEPTIONAL : TerraItemQuality.FINE;
        };
    }

    private double adjustRareChance(double baseChance, RefinerUpgradeState upgrades) {
        if (upgrades == null) {
            return baseChance;
        }
        return Math.min(0.65D, baseChance + (upgrades.qualityLevel * 0.05D));
    }

    private boolean isRefinerInputSlot(int slot) {
        for (int inputSlot : REFINER_INPUT_SLOTS) {
            if (inputSlot == slot) {
                return true;
            }
        }
        return false;
    }

    private boolean isRefinerOutputSlot(int slot) {
        for (int outputSlot : REFINER_OUTPUT_PRIMARY_SLOTS) {
            if (outputSlot == slot) {
                return true;
            }
        }
        for (int outputSlot : REFINER_OUTPUT_SECONDARY_SLOTS) {
            if (outputSlot == slot) {
                return true;
            }
        }
        return false;
    }

    private int[] refinerOutputSlotsForLane(int lane) {
        return new int[]{REFINER_OUTPUT_PRIMARY_SLOTS[lane], REFINER_OUTPUT_SECONDARY_SLOTS[lane]};
    }

    private boolean canPlaceIntoRefiner(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return true;
        }
        RefinerRecipeDefinition recipe = findRefinerRecipe(itemStack);
        if (recipe == null) {
            return false;
        }
        if (!recipe.requiresSpecialist()) {
            return true;
        }
        UUID playerId = player.getUniqueId();
        return plugin.hasProfession(playerId, Profession.MINER) || plugin.hasProfession(playerId, Profession.BLACKSMITH) || plugin.hasCraftingBypass(playerId);
    }

    private boolean isFurnaceInputSlot(int slot) {
        for (int inputSlot : FURNACE_INPUT_SLOTS) {
            if (inputSlot == slot) {
                return true;
            }
        }
        return false;
    }

    private boolean isFurnaceOutputSlot(int slot) {
        for (int outputSlot : FURNACE_OUTPUT_SLOTS) {
            if (outputSlot == slot) {
                return true;
            }
        }
        return false;
    }

    private int fuelBurnTicks(Material material) {
        if (material == null) {
            return 0;
        }
        return switch (material) {
            case COAL, CHARCOAL -> 1600;
            case COAL_BLOCK -> 16000;
            case BLAZE_ROD -> 2400;
            case LAVA_BUCKET -> 20000;
            case OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG, MANGROVE_LOG, CHERRY_LOG,
                    CRIMSON_STEM, WARPED_STEM, OAK_WOOD, SPRUCE_WOOD, BIRCH_WOOD, JUNGLE_WOOD, ACACIA_WOOD,
                    DARK_OAK_WOOD, MANGROVE_WOOD, CHERRY_WOOD, STRIPPED_OAK_LOG, STRIPPED_SPRUCE_LOG,
                    STRIPPED_BIRCH_LOG, STRIPPED_JUNGLE_LOG, STRIPPED_ACACIA_LOG, STRIPPED_DARK_OAK_LOG,
                    STRIPPED_MANGROVE_LOG, STRIPPED_CHERRY_LOG, STRIPPED_CRIMSON_STEM, STRIPPED_WARPED_STEM -> 300;
            case OAK_PLANKS, SPRUCE_PLANKS, BIRCH_PLANKS, JUNGLE_PLANKS, ACACIA_PLANKS, DARK_OAK_PLANKS,
                    MANGROVE_PLANKS, CHERRY_PLANKS, BAMBOO_PLANKS -> 300;
            case STICK -> 100;
            default -> 0;
        };
    }

    private void craftRecipe(Player player, BenchDefinition bench, RecipeDefinition recipe) {
        boolean bypass = plugin.hasCraftingBypass(player.getUniqueId());
        Profession cooldownProfession = bench != null && plugin.hasProfession(player.getUniqueId(), bench.specialistProfession)
                ? bench.specialistProfession
                : null;
        if (!bypass && !plugin.tryConsumeSharedActionCooldown(player, cooldownProfession)) {
            return;
        }
        if (!bypass && "refiner".equalsIgnoreCase(bench.id) && !canUseRefinerRecipe(player, recipe)) {
            player.sendMessage(plugin.colorize("&cYou cannot refine that material with your current job."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7F, 0.8F);
            return;
        }
        if (!bypass && recipe.specialistOnly && bench.specialistProfession != null && !plugin.hasProfession(player.getUniqueId(), bench.specialistProfession)) {
            player.sendMessage(plugin.colorize("&cOnly " + plugin.getProfessionPlainDisplayName(bench.specialistProfession) + "s can craft that pattern."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7F, 0.8F);
            return;
        }
        if (!bypass && !hasIngredients(player, recipe.ingredients)) {
            player.sendMessage(plugin.colorize("&cYou do not have the required materials."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7F, 0.8F);
            return;
        }
        if (!bypass) {
            removeIngredients(player, recipe.ingredients);
        }
        int amount = recipe.resultAmount;
        ItemStack result = "refiner".equalsIgnoreCase(bench.id)
                ? createRefinerResult(player, recipe, amount)
                : recipe.contentId != null ? createContentItem(recipe.contentId, amount) : createVanillaCraftResult(player, recipe, amount);
        if (result == null) {
            player.sendMessage(plugin.colorize("&cThat recipe is not available right now."));
            return;
        }
        giveOrDrop(player, result);
        if (bench != null) {
            plugin.recordTerraWorkbenchCollection(player, bench.id);
        }
        if (bench.specialistProfession != null
                && !bypass
                && plugin.hasProfession(player.getUniqueId(), bench.specialistProfession)
                && recipe.specialistXpReward > 0
                && canAwardBenchXp(bench, recipe)) {
            plugin.rewardProfessionXp(player, bench.specialistProfession, recipe.specialistXpReward);
        }
        player.sendMessage(plugin.colorize("&aCrafted &f" + recipe.resultDisplayName + "&a."));
        player.playSound(player.getLocation(), Sound.BLOCK_SMITHING_TABLE_USE, 0.8F, 1.1F);
    }

    private boolean canUseRefinerRecipe(Player player, RecipeDefinition recipe) {
        if (player == null || recipe == null) {
            return false;
        }
        if ("sifting".equalsIgnoreCase(recipe.categoryKey)) {
            return true;
        }
        UUID playerId = player.getUniqueId();
        return plugin.hasProfession(playerId, Profession.MINER) || plugin.hasProfession(playerId, Profession.BLACKSMITH);
    }

    private boolean canAwardBenchXp(BenchDefinition bench, RecipeDefinition recipe) {
        if (bench == null || recipe == null) {
            return false;
        }
        if ("refiner".equalsIgnoreCase(bench.id)) {
            return "ore_refining".equalsIgnoreCase(recipe.categoryKey) || "sifting".equalsIgnoreCase(recipe.categoryKey);
        }
        return true;
    }

    private ItemStack createRefinerResult(Player player, RecipeDefinition recipe, int amount) {
        if (recipe == null) {
            return null;
        }
        if ("sifting".equalsIgnoreCase(recipe.categoryKey)) {
            return rollSiftingResult(recipe.id, amount);
        }
        if (recipe.contentId != null) {
            ItemStack base = createContentItem(recipe.contentId, amount);
            if (base == null) {
                return null;
            }
            int totalAmount = amount;
            if (Math.random() < 0.30D) {
                totalAmount++;
            }
            base.setAmount(totalAmount);
            if (Math.random() < 0.12D) {
                ItemStack rare = rollRareRefiningMaterial();
                if (rare != null) {
                    giveOrDrop(player, rare);
                    player.sendMessage(plugin.colorize("&6The refiner uncovered &f" + plainDisplayName(rare) + "&6."));
                }
            }
            return base;
        }
        return createVanillaCraftResult(player, recipe, amount);
    }

    private void finishRefiningCycle(RefinerBenchState state, int lane, RefinerRecipeDefinition recipe) {
        int inputSlot = REFINER_INPUT_SLOTS[lane];
        ItemStack input = state.inventory.getItem(inputSlot);
        if (input == null || input.getType().isAir()) {
            return;
        }
        input.setAmount(input.getAmount() - 1);
        state.inventory.setItem(inputSlot, input.getAmount() <= 0 ? null : input);
        RefiningRoll roll = recipe.rollOutput(this, state.upgradeState);
        if (roll == null || roll.primaryResult == null || roll.primaryResult.getType().isAir()) {
            return;
        }
        addToRefinerOutputBuffer(state, lane, roll.primaryResult);
        if (roll.rareBonus != null && !roll.rareBonus.getType().isAir()) {
            Block block = state.blockKey.resolveBlock();
            if (block != null) {
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5D, 1.0D, 0.5D), roll.rareBonus);
            }
        }
    }

    private void addToRefinerOutputBuffer(RefinerBenchState state, int lane, ItemStack result) {
        if (result == null || result.getType().isAir()) {
            return;
        }
        for (int outputSlot : refinerOutputSlotsForLane(lane)) {
            ItemStack output = state.inventory.getItem(outputSlot);
            if (canStackTogether(output, result) && output.getAmount() + result.getAmount() <= output.getMaxStackSize()) {
                output.setAmount(output.getAmount() + result.getAmount());
                state.inventory.setItem(outputSlot, output);
                return;
            }
        }
        for (int outputSlot : refinerOutputSlotsForLane(lane)) {
            ItemStack output = state.inventory.getItem(outputSlot);
            if (output == null || output.getType().isAir()) {
                state.inventory.setItem(outputSlot, result);
                return;
            }
        }
        Block block = state.blockKey.resolveBlock();
        if (block != null) {
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5D, 1.0D, 0.5D), result);
        }
    }

    private ItemStack rollSiftingResult(String recipeId, int amount) {
        String normalized = normalize(recipeId);
        if (normalized.contains("gravel")) {
            return weightedResult(amount,
                    weighted(new ItemStack(Material.FLINT), 55),
                    weighted(new ItemStack(Material.CLAY_BALL, 2), 18),
                    weighted(new ItemStack(Material.IRON_NUGGET, 2), 10),
                    weighted(new ItemStack(Material.GOLD_NUGGET, 2), 7),
                    weighted(createContentItem("ancient_fossil", 1), 5),
                    weighted(createContentItem("vein_shard", 1), 5));
        }
        return weightedResult(amount,
                weighted(new ItemStack(Material.CLAY_BALL, 2), 30),
                weighted(new ItemStack(Material.FLINT), 18),
                weighted(new ItemStack(Material.QUARTZ), 15),
                weighted(new ItemStack(Material.BONE_MEAL, 2), 12),
                weighted(new ItemStack(Material.GOLD_NUGGET, 2), 8),
                weighted(createContentItem("glimmer_dust", 1), 9),
                weighted(createContentItem("mineral_resin", 1), 8));
    }

    @SafeVarargs
    private final ItemStack weightedResult(int amount, WeightedItem... entries) {
        int totalWeight = 0;
        for (WeightedItem entry : entries) {
            if (entry != null && entry.itemStack != null) {
                totalWeight += Math.max(0, entry.weight);
            }
        }
        if (totalWeight <= 0) {
            return null;
        }
        int roll = (int) (Math.random() * totalWeight);
        int cursor = 0;
        for (WeightedItem entry : entries) {
            if (entry == null || entry.itemStack == null) {
                continue;
            }
            cursor += Math.max(0, entry.weight);
            if (roll < cursor) {
                ItemStack result = entry.itemStack.clone();
                result.setAmount(Math.max(1, result.getAmount() * Math.max(1, amount)));
                return result;
            }
        }
        return entries[0] != null && entries[0].itemStack != null ? entries[0].itemStack.clone() : null;
    }

    private WeightedItem weighted(ItemStack itemStack, int weight) {
        return new WeightedItem(itemStack, weight);
    }

    private ItemStack rollRareRefiningMaterial() {
        return weightedResult(1,
                weighted(createContentItem("vein_shard", 1), 35),
                weighted(createContentItem("mineral_resin", 1), 30),
                weighted(createContentItem("ancient_fossil", 1), 20),
                weighted(createContentItem("glimmer_dust", 1), 15));
    }

    private String plainDisplayName(ItemStack itemStack) {
        if (itemStack == null) {
            return "Unknown";
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(meta.displayName());
        }
        return plugin.formatMaterialName(itemStack.getType());
    }

    private ItemStack cloneVanillaResult(RecipeDefinition recipe, int amount) {
        if (recipe.vanillaResult == null) {
            return null;
        }
        ItemStack clone = recipe.vanillaResult.clone();
        clone.setAmount(Math.max(1, amount));
        return plugin.applyUsageRequirementLore(clone);
    }

    private ItemStack createVanillaCraftResult(Player player, RecipeDefinition recipe, int amount) {
        if (recipe == null || recipe.vanillaResult == null) {
            return null;
        }
        Material resultMaterial = recipe.vanillaResult.getType();
        if (plugin.isForgeManagedEquipment(resultMaterial)) {
            ItemStack forged = plugin.createForgedEquipment(player, resultMaterial, amount);
            if (forged != null) {
                return forged;
            }
        }
        return cloneVanillaResult(recipe, amount);
    }

    private boolean hasIngredients(Player player, List<RecipeIngredient> ingredients) {
        for (RecipeIngredient ingredient : ingredients) {
            if (countIngredient(player, ingredient) < ingredient.amount) {
                return false;
            }
        }
        return true;
    }

    private int countIngredient(Player player, RecipeIngredient ingredient) {
        int total = 0;
        for (ItemStack itemStack : player.getInventory().getStorageContents()) {
            if (itemStack == null || itemStack.getType().isAir()) {
                continue;
            }
            if (ingredient.contentId != null) {
                if (matchesContentItem(itemStack, ingredient.contentId)) {
                    total += itemStack.getAmount();
                }
            } else if (!isTerraCustomItem(itemStack) && ingredient.materialOptions.contains(itemStack.getType())) {
                total += itemStack.getAmount();
            }
        }
        return total;
    }

    private void removeIngredients(Player player, List<RecipeIngredient> ingredients) {
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (RecipeIngredient ingredient : ingredients) {
            int remaining = ingredient.amount;
            for (int slot = 0; slot < contents.length && remaining > 0; slot++) {
                ItemStack itemStack = contents[slot];
                if (itemStack == null || itemStack.getType().isAir()) {
                    continue;
                }
                boolean matches = ingredient.contentId != null
                        ? matchesContentItem(itemStack, ingredient.contentId)
                        : !isTerraCustomItem(itemStack) && ingredient.materialOptions.contains(itemStack.getType());
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

    private ItemStack familyDisplayItem(RecipeFamily family) {
        RecipeDefinition sample = family.recipes.get(0);
        ItemStack icon = sample.iconItem(this);
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) {
            return icon;
        }
        meta.displayName(plugin.legacyComponent("&f" + family.displayName));
        meta.lore(List.of(
                plugin.legacyComponent("&7Variants: &f" + family.recipes.size()),
                plugin.legacyComponent("&7Group: &f" + family.categoryDisplayName),
                plugin.legacyComponent(family.recipes.size() > 1 ? "&eClick to choose a variant." : "&eClick to craft this item.")
        ));
        icon.setItemMeta(meta);
        return icon;
    }

    private ItemStack recipeDisplayItem(Player player, BenchDefinition bench, RecipeDefinition recipe) {
        ItemStack icon = recipe.iconItem(this);
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) {
            return icon;
        }
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(plugin.legacyComponent("&7Source: &f" + recipe.sourceLabel));
        lore.add(plugin.legacyComponent("&7Output: &f" + recipe.resultAmount + "x"));
        lore.add(plugin.legacyComponent("&8"));
        lore.add(plugin.legacyComponent("&eIngredients"));
        for (RecipeIngredient ingredient : recipe.ingredients) {
            lore.add(plugin.legacyComponent("&8- &f" + ingredient.amount + "x " + ingredient.displayName(this)));
        }
        lore.add(plugin.legacyComponent("&8"));
        if (recipe.specialistOnly && bench.specialistProfession != null) {
            lore.add(plugin.legacyComponent(plugin.hasCraftingBypass(player.getUniqueId()) || plugin.hasProfession(player.getUniqueId(), bench.specialistProfession)
                    ? "&aSpecialist pattern available."
                    : "&cRequires " + plugin.getProfessionPlainDisplayName(bench.specialistProfession) + "."));
        }
        lore.add(plugin.legacyComponent(plugin.hasCraftingBypass(player.getUniqueId()) ? "&6Craft bypass active." : hasIngredients(player, recipe.ingredients) ? "&eClick to craft." : "&cMissing materials."));
        meta.displayName(plugin.legacyComponent("&f" + recipe.resultDisplayName));
        meta.lore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    private RecipeDefinition findRecipeByDisplayItem(List<RecipeDefinition> recipes, ItemStack clickedItem) {
        if (clickedItem == null || clickedItem.getType().isAir()) {
            return null;
        }
        String clickedKey = layoutItemKey(clickedItem);
        for (RecipeDefinition recipe : recipes) {
            if (Objects.equals(clickedKey, layoutItemKey(recipe.iconItem(this)))) {
                return recipe;
            }
        }
        return null;
    }

    private ItemStack catalogDisplayItem(TerraContentDefinition definition) {
        ItemStack itemStack = createContentItem(definition.id, 1);
        if (itemStack == null) {
            return simpleItem(Material.BARRIER, "&cBroken Entry", List.of("&7Missing Terra definition."));
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<net.kyori.adventure.text.Component> lore = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());
            lore.add(plugin.legacyComponent("&8"));
            lore.add(plugin.legacyComponent("&eClick to receive this item."));
            meta.lore(lore);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    private ItemStack builderTabItem(BuilderMenuTab tab, boolean active) {
        return simpleItem(tab.icon, (active ? "&6" : "&7") + tab.displayName, List.of(
                tab.descriptionLine,
                active ? "&eCurrent tab." : "&eClick to open."
        ));
    }

    private List<String> builderNotes(BuilderMenuTab tab) {
        return switch (tab) {
            case MASONRY -> List.of(
                    "&7Stone and brick structure pieces.",
                    "&7Use grouped pages for stairs,",
                    "&7slabs, and walls."
            );
            case BOUNDARIES -> List.of(
                    "&7Fence lines and perimeter pieces.",
                    "&7Use this tab for settlement",
                    "&7edges and yard separation."
            );
            case OPENINGS -> List.of(
                    "&7Doors, trapdoors, and windows.",
                    "&7Keep entry points and light",
                    "&7materials together here."
            );
            case FIXTURES -> List.of(
                    "&7Site fittings and small decor.",
                    "&7Fast access to practical",
                    "&7builder utility pieces."
            );
        };
    }

    private ItemStack builderEntryDisplayItem(Player player, BuilderMenuEntry entry) {
        if (!entry.isVariantGroup()) {
            return recipeDisplayItem(player, benches.get("builder_workbench"), entry.directRecipe());
        }
        ItemStack icon = new ItemStack(entry.group().icon);
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) {
            return icon;
        }
        meta.displayName(plugin.legacyComponent("&f" + entry.displayName()));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(plugin.legacyComponent("&7Grouped variants"));
        lore.add(plugin.legacyComponent("&7Choose the exact material or"));
        lore.add(plugin.legacyComponent("&7shape you want on the next page."));
        lore.add(plugin.legacyComponent("&8"));
        lore.add(plugin.legacyComponent("&7Options: &f" + builderVariantRecipes(entry.group()).size()));
        lore.add(plugin.legacyComponent("&eClick to browse."));
        meta.lore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    private void giveOrDrop(Player player, ItemStack itemStack) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(itemStack);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }

    private void fill(Inventory inventory, Material material, String name) {
        ItemStack filler = simpleItem(material, name, List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
    }

    private void clearGrid(Inventory inventory) {
        for (int slot : GRID_SLOTS) {
            inventory.setItem(slot, null);
        }
    }

    private void setPager(Inventory inventory, int page, int maxPage) {
        if (page > 1) {
            inventory.setItem(48, simpleItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of("&7Go to page " + (page - 1) + ".")));
        }
        if (page < maxPage) {
            inventory.setItem(50, simpleItem(Material.TIPPED_ARROW, "&eNext Page", List.of("&7Go to page " + (page + 1) + ".")));
        }
    }

    private ItemStack simpleItem(Material material, String name, List<String> loreLines) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }
        meta.displayName(plugin.legacyComponent(name));
        if (!loreLines.isEmpty()) {
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

    private void registerBenches() {
        addBench(new BenchDefinition("workbench", "&6", "Workbench", Material.CRAFTING_TABLE, null, List.of(
                "&7General crafting bench for everyday",
                "&7wood, utility, travel, and misc crafting."
        ), List.of(
                new BenchCategory("wood", "Wood Basics", Material.OAK_PLANKS),
                new BenchCategory("utility", "Utility", Material.CHEST),
                new BenchCategory("transport", "Transport", Material.OAK_BOAT),
                new BenchCategory("misc", "Misc", Material.ITEM_FRAME)
        )));
        addBench(new BenchDefinition("builder_workbench", "&f", "Builder's Workbench", Material.STONECUTTER, Profession.BUILDER, List.of(
                "&7Structural crafting station for",
                "&7blocks, stairs, slabs, walls, and fittings."
        ), List.of(
                new BenchCategory("blocks", "Blocks", Material.BRICKS),
                new BenchCategory("stairs", "Stairs", Material.OAK_STAIRS),
                new BenchCategory("slabs", "Slabs", Material.STONE_SLAB),
                new BenchCategory("walls", "Walls", Material.COBBLESTONE_WALL),
                new BenchCategory("fences", "Fences", Material.OAK_FENCE),
                new BenchCategory("doors", "Doors", Material.OAK_DOOR),
                new BenchCategory("windows", "Windows", Material.GLASS_PANE),
                new BenchCategory("decor", "Decor", Material.LANTERN)
        )));
        addBench(new BenchDefinition("farmer_workbench", "&e", "Farmer's Workbench", Material.BARREL, Profession.FARMER, List.of(
                "&7Food preparation bench for meals,",
                "&7rations, and preserved field supplies."
        ), List.of(
                new BenchCategory("prepared_meals", "Prepared Meals", Material.BREAD),
                new BenchCategory("preserves", "Preserves", Material.HONEY_BOTTLE)
        )));
        addBench(new BenchDefinition("furnace_bench", "&c", "Furnace", Material.FURNACE, Profession.BLACKSMITH, List.of(
                "&7Heating station for smelting ores,",
                "&7food, glass, and other processed outputs."
        ), List.of(
                new BenchCategory("smelt_ores", "Ores", Material.IRON_INGOT),
                new BenchCategory("smelt_food", "Food", Material.COOKED_BEEF),
                new BenchCategory("smelt_blocks", "Blocks", Material.GLASS),
                new BenchCategory("smelt_misc", "Misc", Material.CHARCOAL)
        )));
        addBench(new BenchDefinition("refiner", "&8", "Refiner", Material.GRINDSTONE, Profession.MINER, List.of(
                "&7Processes ore, timber, stone, and grain",
                "&7into settlement-grade refined goods."
        ), List.of(
                new BenchCategory("ore_refining", "Ore Refining", Material.RAW_IRON),
                new BenchCategory("timber_processing", "Timber Processing", Material.OAK_LOG),
                new BenchCategory("masonry_processing", "Masonry Processing", Material.COBBLESTONE),
                new BenchCategory("food_processing", "Food Processing", Material.WHEAT),
                new BenchCategory("sifting", "Sifting", Material.FLINT)
        )));
        addBench(new BenchDefinition("campfire_bench", "&6", "Campfire", Material.CAMPFIRE, Profession.FARMER, List.of(
                "&7Open-fire station for rough food",
                "&7and simple field meals."
        ), List.of(
                new BenchCategory("campfire_rough", "Rough Meals", Material.BREAD),
                new BenchCategory("campfire_hearty", "Field Meals", Material.RABBIT_STEW)
        )));
    }

    private void registerCustomContent() {
        registerContent(new TerraContentDefinition("workbench", TerraCatalogCategory.WORKBENCHES, Material.CRAFTING_TABLE, "&6", "Workbench", true, null, "&ePlace and right-click to open this bench.", List.of(
                "&7General crafting bench for everyday",
                "&7utility and common crafted goods."
        )));
        registerContent(new TerraContentDefinition("builder_workbench", TerraCatalogCategory.WORKBENCHES, Material.STONECUTTER, "&f", "Builder's Workbench", true, Profession.BUILDER, "&ePlace and right-click to open this bench.", List.of(
                "&7Organized builder station for",
                "&7blocks, stairs, slabs, walls, and fittings."
        )));
        registerContent(new TerraContentDefinition("farmer_workbench", TerraCatalogCategory.WORKBENCHES, Material.BARREL, "&e", "Farmer's Workbench", true, Profession.FARMER, "&ePlace and right-click to open this bench.", List.of(
                "&7Food and supply station for",
                "&7farm-crafted meals and utility goods."
        )));
        registerContent(new TerraContentDefinition("furnace_bench", TerraCatalogCategory.WORKBENCHES, Material.FURNACE, "&c", "Furnace", true, Profession.BLACKSMITH, "&ePlace and right-click to open this station.", List.of(
                "&7Heating station for smelting and",
                "&7other processed recipe outputs."
        )));
        registerContent(new TerraContentDefinition("refiner", TerraCatalogCategory.WORKBENCHES, Material.GRINDSTONE, "&8", "Refiner", true, Profession.MINER, "&ePlace and right-click to open this station.", List.of(
                "&7Material processing station for ore,",
                "&7timber, masonry mix, and grain."
        )));
        registerContent(new TerraContentDefinition("campfire_bench", TerraCatalogCategory.WORKBENCHES, Material.CAMPFIRE, "&6", "Campfire", true, Profession.FARMER, "&ePlace and right-click to open this station.", List.of(
                "&7Open-fire station for roasted",
                "&7food and camp cooking."
        )));
        registerContent(new TerraContentDefinition("tin_ore_block", TerraCatalogCategory.ORES, Material.LIGHT_GRAY_GLAZED_TERRACOTTA, "&7", "Tin Ore", true, null, "&eMine or place this as a Terra resource node placeholder.", List.of(
                "&7A soft frontier metal deposit",
                "&7used for survey tools and early alloys."
        )));
        int oreModelData = 91001;
        for (RefinerOreType oreType : RefinerOreType.values()) {
            registerContent(new TerraContentDefinition(oreType.rawContentId, TerraCatalogCategory.RAW_REFINED_ORE, oreType.rawMaterial, oreType.color, "Raw " + oreType.displayName, false, null, "&eUnrefined ore chunk. Smelt it or refine it first.", List.of(
                    "&7Crude ore taken straight from the vein.",
                    "&7Can be smelted 1 to 1 or cleaned in a refiner."
            ), oreModelData++));
            registerContent(new TerraContentDefinition(oreType.refinedContentId, TerraCatalogCategory.RAW_REFINED_ORE, oreType.refinedMaterial, oreType.color, "Refined " + oreType.displayName, false, null, "&eCleaned ore ready for better smelting yield chances.", List.of(
                    "&7Sorted and cleaned ore from the refiner.",
                    "&7Has a chance to come from double-refining."
            ), oreModelData++));
        }
        registerContent(new TerraContentDefinition("vein_shard", TerraCatalogCategory.MATERIALS, Material.AMETHYST_SHARD, "&d", "Vein Shard", false, null, "&eA rare refining byproduct used for trade and late work.", List.of(
                "&7Dense crystal growth pulled from rich ore.",
                "&7Useful for advanced trade and crafting later."
        )));
        registerContent(new TerraContentDefinition("mineral_resin", TerraCatalogCategory.MATERIALS, Material.PRISMARINE_SHARD, "&b", "Mineral Resin", false, null, "&eA sticky mineral concentrate from refined stone.", List.of(
                "&7An uncommon concentrate left after",
                "&7heavy refining and material washing."
        )));
        registerContent(new TerraContentDefinition("ancient_fossil", TerraCatalogCategory.MATERIALS, Material.BONE, "&f", "Ancient Fossil", false, null, "&eA trade good recovered while sifting earth and ore.", List.of(
                "&7Old buried remains cracked free during",
                "&7careful washing and sorting."
        )));
        registerContent(new TerraContentDefinition("glimmer_dust", TerraCatalogCategory.MATERIALS, Material.GLOWSTONE_DUST, "&6", "Glimmer Dust", false, null, "&eFine shining dust used in later trade and industry.", List.of(
                "&7A bright residue found in high-value",
                "&7refining work and rich sediment."
        )));
        registerContent(new TerraContentDefinition("timber_bundle", TerraCatalogCategory.MATERIALS, Material.OAK_PLANKS, "&6", "Timber Bundle", false, null, "&eCut structural timber ready for settlement work.", List.of(
                "&7Trimmed wood stock sized for",
                "&7serious building instead of rough placement."
        )));
        registerContent(new TerraContentDefinition("support_beam", TerraCatalogCategory.MATERIALS, Material.OAK_LOG, "&e", "Support Beam", false, null, "&eA heavy beam cut for frames, roofs, and roadwork.", List.of(
                "&7Refined lumber intended for",
                "&7structural projects and builder contracts."
        )));
        registerContent(new TerraContentDefinition("joinery_kit", TerraCatalogCategory.MATERIALS, Material.ITEM_FRAME, "&6", "Joinery Kit", false, null, "&ePrepared fittings, pegs, and braces for advanced construction.", List.of(
                "&7Packed hardware and carpentry parts",
                "&7used in better settlement builds."
        )));
        registerContent(new TerraContentDefinition("bark_fiber", TerraCatalogCategory.MATERIALS, Material.STRING, "&a", "Bark Fiber", false, null, "&eUsable plant fiber stripped during timber processing.", List.of(
                "&7A byproduct of wood refining",
                "&7useful in trade and light craft."
        )));
        registerContent(new TerraContentDefinition("stone_aggregate", TerraCatalogCategory.MATERIALS, Material.GRAVEL, "&7", "Stone Aggregate", false, null, "&eBroken and sorted stone ready for mixed construction work.", List.of(
                "&7A dense filler used in roads,",
                "&7masonry cores, and settlement expansion."
        )));
        registerContent(new TerraContentDefinition("washed_sand", TerraCatalogCategory.MATERIALS, Material.SAND, "&e", "Washed Sand", false, null, "&eCleaned sand prepared for mortar, glass, and fine work.", List.of(
                "&7Rinsed and screened sand from",
                "&7the refiner's settling trays."
        )));
        registerContent(new TerraContentDefinition("washed_clay", TerraCatalogCategory.MATERIALS, Material.CLAY_BALL, "&f", "Washed Clay", false, null, "&ePrepared clay for brick and finishing mixes.", List.of(
                "&7Cleaner clay stock separated",
                "&7from common earth and grit."
        )));
        registerContent(new TerraContentDefinition("mortar_mix", TerraCatalogCategory.MATERIALS, Material.BRICK, "&f", "Mortar Mix", false, null, "&eA builder-ready binder for brick, stone, and heavy settlement work.", List.of(
                "&7A refined blend of washed sand,",
                "&7clay, and ground stone."
        )));
        registerContent(new TerraContentDefinition("flour_sack", TerraCatalogCategory.MATERIALS, Material.WHEAT, "&e", "Flour Sack", false, null, "&eMilled grain ready for kitchen and ration work.", List.of(
                "&7Clean grain crushed into a",
                "&7stable ingredient for better food chains."
        )));
        registerContent(new TerraContentDefinition("refiner_throughput_kit", TerraCatalogCategory.MATERIALS, Material.LIGHTNING_ROD, "&b", "Refiner Throughput Kit", false, null, "&eUpgrade hardware that speeds up one placed refiner.", List.of(
                "&7Bolted rails, bearings, and braces",
                "&7used to push more material through each lane."
        )));
        registerContent(new TerraContentDefinition("refiner_quality_kit", TerraCatalogCategory.MATERIALS, Material.BRUSH, "&d", "Refiner Quality Kit", false, null, "&eUpgrade tooling that improves refined output quality.", List.of(
                "&7Screens, brushes, and sorting tools",
                "&7tuned for cleaner high-grade output."
        )));
        registerContent(new TerraContentDefinition("settlers_hatchet", TerraCatalogCategory.TOOLS, Material.WOODEN_AXE, "&e", "Settler's Hatchet", false, null, "&eA rough frontier hatchet for the first phase of the server.", List.of(
                "&7A cheap starter chopping tool made from",
                "&7simple wood stock, wraps, and rough fittings."
        ), 41001, "terra_starter_gear:settlers_hatchet"));
        registerContent(new TerraContentDefinition("settlers_pickaxe", TerraCatalogCategory.TOOLS, Material.WOODEN_PICKAXE, "&e", "Settler's Pickaxe", false, null, "&eA rough frontier pick for the first phase of the server.", List.of(
                "&7A cheap starter mining tool made from",
                "&7simple wood stock, wraps, and rough fittings."
        ), 41003, "terra_starter_gear:settlers_pickaxe"));
        registerContent(new TerraContentDefinition("copper_pioneer_axe", TerraCatalogCategory.TOOLS, Material.IRON_AXE, "&6", "Copper Pioneer Axe", false, null, "&eA sturdy copper axe for woodcutting and defense.", List.of(
                "&7Stronger than the tin set and suited",
                "&7for first real settlement expansion."
        ), 41002, "terra_starter_gear:copper_pioneer_axe"));
        registerContent(new TerraContentDefinition("charred_skewer", TerraCatalogCategory.FOOD, Material.COOKED_CHICKEN, "&6", "Charred Skewer", false, null, "&eRough campfire food. Fills little and fades fast.", List.of(
                "&7A stick of scorched meat pulled",
                "&7straight off the fire."
        )));
        registerContent(new TerraContentDefinition("ash_bread", TerraCatalogCategory.FOOD, Material.BREAD, "&6", "Ash Bread", false, null, "&eCheap trail bread. Barely filling.", List.of(
                "&7Flat camp bread baked in the ash,",
                "&7dry and not very nourishing."
        )));
        registerContent(new TerraContentDefinition("trail_mash", TerraCatalogCategory.FOOD, Material.BAKED_POTATO, "&e", "Trail Mash", false, null, "&eWarm but weak camp food.", List.of(
                "&7Mashed roots and potato pressed",
                "&7into a quick roadside meal."
        )));
        registerContent(new TerraContentDefinition("forager_bowl", TerraCatalogCategory.FOOD, Material.MUSHROOM_STEW, "&a", "Forager Bowl", false, null, "&eA thin woodland stew. Spoils from your gut quickly.", List.of(
                "&7Made from gathered mushrooms and",
                "&7whatever else fit in the bowl."
        )));
        registerContent(new TerraContentDefinition("camp_jerky", TerraCatalogCategory.FOOD, Material.DRIED_KELP, "&f", "Camp Jerky", false, null, "&ePortable, tough, and not very filling.", List.of(
                "&7Dry strips of camp-cured meat,",
                "&7good for the road, not much else."
        )));
        registerContent(new TerraContentDefinition("beet_scraps", TerraCatalogCategory.FOOD, Material.BEETROOT_SOUP, "&c", "Beet Scraps", false, null, "&eA weak beet-and-root bowl. Better than nothing.", List.of(
                "&7A thin bowl of chopped beetroot",
                "&7and camp-cut root scraps."
        )));
        registerContent(new TerraContentDefinition("hearth_hash", TerraCatalogCategory.FOOD, Material.RABBIT_STEW, "&e", "Hearth Hash", false, null, "&eA solid field meal. Lasts longer than rough food.", List.of(
                "&7Pan-mashed roots and vegetables",
                "&7cooked down over a hard fire."
        )));
        registerContent(new TerraContentDefinition("farmers_pottage", TerraCatalogCategory.FOOD, Material.RABBIT_STEW, "&6", "Farmer's Pottage", false, null, "&eA better camp pot. Filling and steady.", List.of(
                "&7A thicker grain-and-vegetable pot",
                "&7for longer work in the field."
        )));
        registerContent(new TerraContentDefinition("hunters_stew", TerraCatalogCategory.FOOD, Material.RABBIT_STEW, "&6", "Hunter's Stew", false, null, "&eA strong trail meal with a little kick.", List.of(
                "&7Meat, roots, and broth cooked into",
                "&7a proper traveling stew."
        )));
        registerContent(new TerraContentDefinition("field_chowder", TerraCatalogCategory.FOOD, Material.MUSHROOM_STEW, "&a", "Field Chowder", false, null, "&eA mixed crop chowder that holds longer.", List.of(
                "&7A thicker bowl made from root crops,",
                "&7greens, and whatever the field gave."
        )));
        registerContent(new TerraContentDefinition("grain_loaf", TerraCatalogCategory.FOOD, Material.BREAD, "&e", "Grain Loaf", false, null, "&eA proper kitchen loaf. Better than ash bread.", List.of(
                "&7A denser work loaf baked from",
                "&7milled grain and root mash."
        )));
        registerContent(new TerraContentDefinition("vegetable_stew", TerraCatalogCategory.FOOD, Material.BEETROOT_SOUP, "&a", "Vegetable Stew", false, null, "&eA steady crop stew for work crews.", List.of(
                "&7A thicker pot of carrots, beetroot,",
                "&7potato, and field herbs."
        )));
        registerContent(new TerraContentDefinition("stuffed_flatbread", TerraCatalogCategory.FOOD, Material.BREAD, "&6", "Stuffed Flatbread", false, null, "&eA portable prepared meal with a little staying power.", List.of(
                "&7Folded bread packed with roots and",
                "&7simple field filling."
        )));
        registerContent(new TerraContentDefinition("workers_hotpot", TerraCatalogCategory.FOOD, Material.RABBIT_STEW, "&6", "Worker's Hotpot", false, null, "&eA filling farm pot that keeps you going.", List.of(
                "&7A heavier prepared pot for long",
                "&7field work and road labor."
        )));
        registerContent(new TerraContentDefinition("orchard_mix", TerraCatalogCategory.FOOD, Material.MUSHROOM_STEW, "&b", "Orchard Mix", false, null, "&eA fresh crop bowl with a light lift.", List.of(
                "&7A cleaner prepared mix of produce",
                "&7for travelers and light work."
        )));
        registerContent(new TerraContentDefinition("root_bundle", TerraCatalogCategory.FOOD, Material.PAPER, "&e", "Root Bundle", false, null, "&ePacked travel roots. Stable and simple.", List.of(
                "&7A wrapped bundle of dried and",
                "&7prepared root vegetables."
        )));
        registerContent(new TerraContentDefinition("grain_ration", TerraCatalogCategory.FOOD, Material.PAPER, "&f", "Grain Ration", false, null, "&eA dry preserved ration for the road.", List.of(
                "&7Compressed grain cakes wrapped for",
                "&7transport and storage."
        )));
        registerContent(new TerraContentDefinition("pickled_roots", TerraCatalogCategory.FOOD, Material.HONEY_BOTTLE, "&a", "Pickled Roots", false, null, "&ePreserved roots that keep well.", List.of(
                "&7Jarred root cuts preserved for",
                "&7later travel or camp stock."
        )));
        registerContent(new TerraContentDefinition("farmers_pack", TerraCatalogCategory.FOOD, Material.BUNDLE, "&6", "Farmer's Pack", false, null, "&eA packed bundle of prepared field food.", List.of(
                "&7A bundled reserve of kitchen-made",
                "&7travel food for long trips."
        )));
    }

    private void registerCustomRecipes() {
        addRecipe(customRecipe("workbench_recipe", "Workbench", "workbench", "utility", "Stations", Material.CRAFTING_TABLE, "workbench", 1, false, 0, 4,
                ingredient(Material.OAK_PLANKS, 4), ingredient(Material.STICK, 2)));
        addRecipe(customRecipe("builders_workbench_recipe", "Builder's Workbench", "workbench", "utility", "Stations", Material.STONECUTTER, "builder_workbench", 1, false, 0, 4,
                ingredient(Material.COBBLESTONE, 4), ingredient(Material.OAK_PLANKS, 2)));
        addRecipe(customRecipe("farmers_workbench_recipe", "Farmer's Workbench", "workbench", "utility", "Stations", Material.BARREL, "farmer_workbench", 1, false, 0, 4,
                ingredient(Material.BARREL, 1), ingredient(Material.OAK_PLANKS, 2), ingredient(Material.HAY_BLOCK, 1)));
        addRecipe(customRecipe("furnace_bench_recipe", "Furnace", "workbench", "utility", "Stations", Material.FURNACE, "furnace_bench", 1, false, 0, 4,
                ingredient(Material.COBBLESTONE, 8), ingredient(Material.IRON_INGOT, 1)));
        addRecipe(customRecipe("refiner_recipe", "Refiner", "workbench", "utility", "Stations", Material.GRINDSTONE, "refiner", 1, false, 0, 4,
                ingredient(Material.STICK, 2), ingredient(Material.STONE_SLAB, 1), ingredient(Material.OAK_PLANKS, 2)));
        addRecipe(customRecipe("campfire_bench_recipe", "Campfire", "workbench", "utility", "Stations", Material.CAMPFIRE, "campfire_bench", 1, false, 0, 4,
                ingredient(Material.STICK, 3), ingredient(Material.COAL, 1), ingredient(Material.OAK_LOG, 3)));
        addRecipe(customRecipe("charred_skewer_recipe", "Charred Skewer", "campfire_bench", "campfire_rough", "Rough Meals", Material.COOKED_CHICKEN, "charred_skewer", 1, false, 0, 3,
                ingredient(Material.COOKED_CHICKEN, 1), ingredient(Material.CARROT, 1), ingredient(Material.STICK, 1)));
        addRecipe(customRecipe("ash_bread_recipe", "Ash Bread", "campfire_bench", "campfire_rough", "Rough Meals", Material.BREAD, "ash_bread", 1, false, 0, 3,
                ingredient(Material.WHEAT, 3)));
        addRecipe(customRecipe("trail_mash_recipe", "Trail Mash", "campfire_bench", "campfire_rough", "Rough Meals", Material.BAKED_POTATO, "trail_mash", 1, false, 0, 3,
                ingredient(Material.POTATO, 2), ingredient(Material.BOWL, 1)));
        addRecipe(customRecipe("forager_bowl_recipe", "Forager Bowl", "campfire_bench", "campfire_rough", "Rough Meals", Material.MUSHROOM_STEW, "forager_bowl", 1, false, 0, 4,
                ingredient(Material.BOWL, 1), ingredient(Material.RED_MUSHROOM, 1), ingredient(Material.BROWN_MUSHROOM, 1), ingredient(Material.CARROT, 1)));
        addRecipe(customRecipe("beet_scraps_recipe", "Beet Scraps", "campfire_bench", "campfire_rough", "Rough Meals", Material.BEETROOT_SOUP, "beet_scraps", 1, false, 0, 4,
                ingredient(Material.BOWL, 1), ingredient(Material.BEETROOT, 3), ingredient(Material.CARROT, 1)));
        addRecipe(customRecipe("camp_jerky_recipe", "Camp Jerky", "campfire_bench", "campfire_rough", "Rough Meals", Material.DRIED_KELP, "camp_jerky", 1, false, 0, 3,
                ingredient(Material.COOKED_BEEF, 1), ingredient(Material.CARROT, 1), ingredient(Material.COAL, 1)));
        addRecipe(customRecipe("hearth_hash_recipe", "Hearth Hash", "campfire_bench", "campfire_hearty", "Field Meals", Material.RABBIT_STEW, "hearth_hash", 1, false, 0, 5,
                ingredient(Material.BOWL, 1), ingredient(Material.POTATO, 3), ingredient(Material.CARROT, 2)));
        addRecipe(customRecipe("farmers_pottage_recipe", "Farmer's Pottage", "campfire_bench", "campfire_hearty", "Field Meals", Material.RABBIT_STEW, "farmers_pottage", 1, false, 0, 6,
                ingredient(Material.BOWL, 1), ingredient(Material.WHEAT, 4), ingredient(Material.CARROT, 2), ingredient(Material.BEETROOT, 2)));
        addRecipe(customRecipe("hunters_stew_recipe", "Hunter's Stew", "campfire_bench", "campfire_hearty", "Field Meals", Material.RABBIT_STEW, "hunters_stew", 1, false, 0, 6,
                ingredient(Material.BOWL, 1), ingredient(Material.COOKED_CHICKEN, 2), ingredient(Material.POTATO, 2), ingredient(Material.CARROT, 1)));
        addRecipe(customRecipe("field_chowder_recipe", "Field Chowder", "campfire_bench", "campfire_hearty", "Field Meals", Material.MUSHROOM_STEW, "field_chowder", 1, false, 0, 6,
                ingredient(Material.BOWL, 1), ingredient(Material.POTATO, 2), ingredient(Material.CARROT, 2), ingredient(Material.BEETROOT, 2), ingredient(Material.BROWN_MUSHROOM, 1)));
        addRecipe(customRecipe("grain_loaf_recipe", "Grain Loaf", "farmer_workbench", "prepared_meals", "Prepared Meals", Material.BREAD, "grain_loaf", 1, false, 1, 6,
                ingredient("flour_sack", 2), ingredient(Material.POTATO, 1)));
        addRecipe(customRecipe("vegetable_stew_recipe", "Vegetable Stew", "farmer_workbench", "prepared_meals", "Prepared Meals", Material.BEETROOT_SOUP, "vegetable_stew", 1, false, 1, 7,
                ingredient(Material.BOWL, 1), ingredient(Material.POTATO, 2), ingredient(Material.CARROT, 2), ingredient(Material.BEETROOT, 2)));
        addRecipe(customRecipe("stuffed_flatbread_recipe", "Stuffed Flatbread", "farmer_workbench", "prepared_meals", "Prepared Meals", Material.BREAD, "stuffed_flatbread", 1, false, 1, 7,
                ingredient("flour_sack", 2), ingredient(Material.CARROT, 2), ingredient(Material.BEETROOT, 1)));
        addRecipe(customRecipe("workers_hotpot_recipe", "Worker's Hotpot", "farmer_workbench", "prepared_meals", "Prepared Meals", Material.RABBIT_STEW, "workers_hotpot", 1, false, 1, 8,
                ingredient(Material.BOWL, 1), ingredient(Material.POTATO, 3), ingredient(Material.CARROT, 2), ingredient(Material.BEETROOT, 2), ingredient(Material.COOKED_CHICKEN, 1)));
        addRecipe(customRecipe("orchard_mix_recipe", "Orchard Mix", "farmer_workbench", "prepared_meals", "Prepared Meals", Material.MUSHROOM_STEW, "orchard_mix", 1, false, 1, 7,
                ingredient(Material.BOWL, 1), ingredient(Material.CARROT, 2), ingredient(Material.BEETROOT, 2), ingredient(Material.RED_MUSHROOM, 1)));
        addRecipe(customRecipe("root_bundle_recipe", "Root Bundle", "farmer_workbench", "preserves", "Preserves", Material.PAPER, "root_bundle", 1, false, 1, 6,
                ingredient(Material.POTATO, 3), ingredient(Material.CARROT, 3), ingredient(Material.PAPER, 1)));
        addRecipe(customRecipe("grain_ration_recipe", "Grain Ration", "farmer_workbench", "preserves", "Preserves", Material.PAPER, "grain_ration", 1, false, 1, 6,
                ingredient("flour_sack", 2), ingredient(Material.PAPER, 1)));
        addRecipe(customRecipe("pickled_roots_recipe", "Pickled Roots", "farmer_workbench", "preserves", "Preserves", Material.HONEY_BOTTLE, "pickled_roots", 1, false, 1, 7,
                ingredient(Material.CARROT, 2), ingredient(Material.BEETROOT, 2), ingredient(Material.HONEY_BOTTLE, 1)));
        addRecipe(customRecipe("farmers_pack_recipe", "Farmer's Pack", "farmer_workbench", "preserves", "Preserves", Material.BUNDLE, "farmers_pack", 1, false, 1, 8,
                ingredient("grain_ration", 1), ingredient("root_bundle", 1), ingredient(Material.BUNDLE, 1)));

        for (RefinerOreType oreType : RefinerOreType.values()) {
            addRecipe(customRecipe("refine_" + oreType.key, "Refined " + oreType.displayName, "refiner", "ore_refining", "Ore Refining", oreType.rawMaterial, oreType.refinedContentId, 1, false, 0, 5,
                    ingredient(oreType.rawContentId, 1)));
        }
        addRecipe(customRecipe("refine_timber_bundle", "Timber Bundle", "refiner", "timber_processing", "Timber Processing", Material.OAK_LOG, "timber_bundle", 1, false, 0, 4,
                ingredient(Material.OAK_LOG, 1)));
        addRecipe(customRecipe("refine_support_beam", "Support Beam", "refiner", "timber_processing", "Timber Processing", Material.STRIPPED_OAK_LOG, "support_beam", 1, false, 0, 5,
                ingredient(Material.STRIPPED_OAK_LOG, 1)));
        addRecipe(customRecipe("refine_stone_aggregate", "Stone Aggregate", "refiner", "masonry_processing", "Masonry Processing", Material.COBBLESTONE, "stone_aggregate", 1, false, 0, 4,
                ingredient(Material.COBBLESTONE, 2)));
        addRecipe(customRecipe("refine_washed_sand", "Washed Sand", "refiner", "masonry_processing", "Masonry Processing", Material.SAND, "washed_sand", 1, false, 0, 4,
                ingredient(Material.SAND, 2)));
        addRecipe(customRecipe("refine_washed_clay", "Washed Clay", "refiner", "masonry_processing", "Masonry Processing", Material.CLAY_BALL, "washed_clay", 1, false, 0, 4,
                ingredient(Material.CLAY_BALL, 2)));
        addRecipe(customRecipe("refine_flour_sack", "Flour Sack", "refiner", "food_processing", "Food Processing", Material.WHEAT, "flour_sack", 1, false, 0, 4,
                ingredient(Material.WHEAT, 3)));
        addRecipe(terraVanillaRecipe("refine_gravel", "refiner", "sifting", "Sifting", Material.FLINT, "Sifted Gravel", 1,
                new ItemStack(Material.FLINT), "Refiner Sift", false, 0, 2, ingredient(Material.GRAVEL, 4)));
        addRecipe(terraVanillaRecipe("refine_sand", "refiner", "sifting", "Sifting", Material.SAND, "Washed Sand", 1,
                new ItemStack(Material.CLAY_BALL), "Refiner Sift", false, 0, 2, ingredient(Material.SAND, 4)));
        addRecipe(customRecipe("builder_stone_bricks_refined", "Stone Brick Batch", "builder_workbench", "masonry", "Masonry", Material.STONE_BRICKS, null, 8, false, 0, 6,
                ingredient("stone_aggregate", 4), ingredient("mortar_mix", 2)));
        addRecipe(customRecipe("builder_mud_bricks_refined", "Mud Brick Batch", "builder_workbench", "masonry", "Masonry", Material.MUD_BRICKS, null, 6, false, 0, 6,
                ingredient("washed_sand", 2), ingredient("mortar_mix", 2), ingredient(Material.MUD, 2)));
        addRecipe(customRecipe("builder_tuff_bricks_refined", "Tuff Brick Batch", "builder_workbench", "masonry", "Masonry", Material.TUFF_BRICKS, null, 6, false, 0, 6,
                ingredient("stone_aggregate", 3), ingredient("mortar_mix", 2), ingredient(Material.TUFF, 2)));
        addRecipe(customRecipe("builder_joinery_kit", "Joinery Kit", "builder_workbench", "fixtures", "Fixtures", Material.ITEM_FRAME, "joinery_kit", 1, false, 0, 5,
                ingredient("support_beam", 1), ingredient("bark_fiber", 2), ingredient(Material.IRON_NUGGET, 2)));
        addRecipe(customRecipe("builder_mortar_mix", "Mortar Mix", "builder_workbench", "masonry", "Masonry", Material.BRICK, "mortar_mix", 1, false, 0, 5,
                ingredient("washed_sand", 2), ingredient("washed_clay", 1), ingredient("stone_aggregate", 1)));
        addRecipe(customRecipe("refiner_throughput_kit_recipe", "Refiner Throughput Kit", "builder_workbench", "fixtures", "Refiner Upgrades", Material.LIGHTNING_ROD, "refiner_throughput_kit", 1, false, 0, 8,
                ingredient("refined_iron", 2), ingredient("support_beam", 1), ingredient("joinery_kit", 1)));
        addRecipe(customRecipe("refiner_quality_kit_recipe", "Refiner Quality Kit", "builder_workbench", "fixtures", "Refiner Upgrades", Material.BRUSH, "refiner_quality_kit", 1, false, 0, 8,
                ingredient("refined_copper", 2), ingredient("washed_sand", 2), ingredient("bark_fiber", 1)));
        addRecipe(customRecipe("settlers_hatchet_recipe", "Settler's Hatchet", "workbench", "starter_tools", "Starter Tools", Material.WOODEN_AXE, "settlers_hatchet", 1, false, 0, 4,
                ingredient("timber_bundle", 1), ingredient("bark_fiber", 1), ingredient(Material.STICK, 1)));
        addRecipe(customRecipe("settlers_pickaxe_recipe", "Settler's Pickaxe", "workbench", "starter_tools", "Starter Tools", Material.WOODEN_PICKAXE, "settlers_pickaxe", 1, false, 0, 4,
                ingredient("timber_bundle", 1), ingredient("bark_fiber", 1), ingredient(Material.STICK, 1)));
        addRecipe(customRecipe("copper_pioneer_axe_recipe", "Copper Pioneer Axe", "workbench", "starter_tools", "Starter Tools", Material.IRON_AXE, "copper_pioneer_axe", 1, false, 0, 9,
                ingredient("refined_copper", 3), ingredient("support_beam", 1), ingredient(Material.STICK, 2)));
    }

    private void registerCustomSmeltingRecipes() {
        for (RefinerOreType oreType : RefinerOreType.values()) {
            addSmeltingRecipe(new SmeltingRecipeDefinition(oreType.rawContentId, List.of(), null, new ItemStack(oreType.smeltResult), FURNACE_BASE_COOK_TIME));
            addSmeltingRecipe(new SmeltingRecipeDefinition(oreType.refinedContentId, List.of(), null, new ItemStack(oreType.smeltResult), FURNACE_BASE_COOK_TIME));
        }
    }

    private void syncBlacksmithForgeRecipes() {
        for (Testproject.BlacksmithRecipe forgeRecipe : plugin.getBlacksmithAnvilRecipes()) {
            if (forgeRecipe == null || forgeRecipe.result() == null || !plugin.isForgeManagedEquipment(forgeRecipe.result())) {
                continue;
            }
            RecipePlacement placement = placementForCraftingResult(forgeRecipe.result());
            if (placement == null) {
                continue;
            }
            RecipeDefinition linkedRecipe = createForgeLinkedRecipe(forgeRecipe, placement);
            if (linkedRecipe == null) {
                continue;
            }
            removeRecipesForOverride(linkedRecipe);
            addRecipe(linkedRecipe);
        }
    }

    private RecipeDefinition createForgeLinkedRecipe(Testproject.BlacksmithRecipe forgeRecipe, RecipePlacement placement) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        for (Map.Entry<Material, Integer> entry : forgeRecipe.ingredients().entrySet()) {
            if (entry.getKey() == null || entry.getValue() <= 0) {
                continue;
            }
            ingredients.add(ingredient(entry.getKey(), entry.getValue()));
        }
        if (ingredients.isEmpty()) {
            return null;
        }
        Material result = forgeRecipe.result();
        return terraVanillaRecipe(
                "forge_link_" + result.name().toLowerCase(Locale.ROOT),
                placement.benchId,
                placement.categoryKey,
                placement.familyName,
                placement.familyIcon,
                plugin.formatMaterialName(result),
                Math.max(1, forgeRecipe.amount()),
                new ItemStack(result, Math.max(1, forgeRecipe.amount())),
                "Blacksmith Forge",
                false,
                0,
                Math.max(0, forgeRecipe.xp()),
                mergeIngredients(ingredients).toArray(RecipeIngredient[]::new)
        );
    }

    private void loadRecipeOverrides() {
        ensureRecipeOverrideDefaults();
        ConfigurationSection recipesSection = recipeOverridesConfig.getConfigurationSection("recipes");
        if (recipesSection == null) {
            return;
        }
        for (String key : recipesSection.getKeys(false)) {
            ConfigurationSection section = recipesSection.getConfigurationSection(key);
            if (section == null || !section.getBoolean("enabled", false)) {
                continue;
            }
            RecipeDefinition recipe = parseRecipeOverride(key, section);
            if (recipe == null) {
                continue;
            }
            registerRecipeOverrideResult(recipe);
            removeRecipesForOverride(recipe);
            addRecipe(recipe);
        }
    }

    private void ensureRecipeOverrideDefaults() {
        if (recipeOverridesFile.exists()) {
            return;
        }
        recipeOverridesConfig.set("recipes.oak_stairs.enabled", false);
        recipeOverridesConfig.set("recipes.oak_stairs.bench", "workbench");
        recipeOverridesConfig.set("recipes.oak_stairs.category", "wood");
        recipeOverridesConfig.set("recipes.oak_stairs.family", "Stairs");
        recipeOverridesConfig.set("recipes.oak_stairs.family_icon", "OAK_STAIRS");
        recipeOverridesConfig.set("recipes.oak_stairs.source", "Recipe Override");
        recipeOverridesConfig.set("recipes.oak_stairs.result.material", "OAK_STAIRS");
        recipeOverridesConfig.set("recipes.oak_stairs.result.amount", 4);
        recipeOverridesConfig.set("recipes.oak_stairs.ingredients", List.of(
                Map.of("material", "OAK_PLANKS", "amount", 6)
        ));

        recipeOverridesConfig.set("recipes.red_bed.enabled", false);
        recipeOverridesConfig.set("recipes.red_bed.bench", "workbench");
        recipeOverridesConfig.set("recipes.red_bed.category", "wood");
        recipeOverridesConfig.set("recipes.red_bed.family", "Beds");
        recipeOverridesConfig.set("recipes.red_bed.family_icon", "RED_BED");
        recipeOverridesConfig.set("recipes.red_bed.source", "Recipe Override");
        recipeOverridesConfig.set("recipes.red_bed.result.material", "RED_BED");
        recipeOverridesConfig.set("recipes.red_bed.result.amount", 1);
        recipeOverridesConfig.set("recipes.red_bed.ingredients", List.of(
                Map.of("material", "RED_WOOL", "amount", 3),
                Map.of("material", "OAK_PLANKS", "amount", 3)
        ));
        try {
            recipeOverridesConfig.save(recipeOverridesFile);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to write terra_recipe_overrides.yml: " + exception.getMessage());
        }
    }

    private RecipeDefinition parseRecipeOverride(String key, ConfigurationSection section) {
        String benchId = normalize(section.getString("bench", "workbench"));
        if (!benches.containsKey(benchId)) {
            plugin.getLogger().warning("Skipped Terra recipe override '" + key + "': unknown bench '" + benchId + "'.");
            return null;
        }
        ConfigurationSection resultSection = section.getConfigurationSection("result");
        if (resultSection == null) {
            plugin.getLogger().warning("Skipped Terra recipe override '" + key + "': missing result section.");
            return null;
        }
        String resultContentId = normalize(resultSection.getString("content_id"));
        Material resultMaterial = parseMaterial(resultSection.getString("material"));
        int resultAmount = Math.max(1, resultSection.getInt("amount", 1));
        ItemStack vanillaResult = resultMaterial == null ? null : new ItemStack(resultMaterial, resultAmount);
        if ((resultContentId == null || resultContentId.isBlank()) && resultMaterial == null) {
            plugin.getLogger().warning("Skipped Terra recipe override '" + key + "': result needs material or content_id.");
            return null;
        }
        List<RecipeIngredient> ingredients = parseOverrideIngredients(key, section);
        if (ingredients.isEmpty()) {
            plugin.getLogger().warning("Skipped Terra recipe override '" + key + "': no valid ingredients.");
            return null;
        }
        String familyName = section.getString("family",
                resultMaterial != null ? plugin.formatMaterialName(resultMaterial)
                        : contentDefinitions.containsKey(resultContentId) ? contentDefinitions.get(resultContentId).displayName : key);
        Material familyIcon = parseMaterial(section.getString("family_icon"));
        if (familyIcon == null) {
            familyIcon = resultMaterial != null ? resultMaterial : Material.CRAFTING_TABLE;
        }
        String resultDisplayName = section.getString("result_display_name",
                resultMaterial != null ? plugin.formatMaterialName(resultMaterial)
                        : contentDefinitions.containsKey(resultContentId) ? contentDefinitions.get(resultContentId).displayName : key);
        String sourceLabel = section.getString("source", "Recipe Override");
        boolean specialistOnly = section.getBoolean("specialist_only", false);
        int specialistBonusOutput = Math.max(0, section.getInt("specialist_bonus_output", 0));
        int specialistXpReward = Math.max(0, section.getInt("specialist_xp_reward", 4));
        return new RecipeDefinition("override_" + normalize(key), benchId, normalize(section.getString("category", "misc")),
                normalize(familyName), familyName, familyIcon, resultContentId == null || resultContentId.isBlank() ? null : resultContentId,
                vanillaResult, resultDisplayName, resultAmount, sourceLabel, specialistOnly, specialistBonusOutput, specialistXpReward, ingredients);
    }

    private List<RecipeIngredient> parseOverrideIngredients(String key, ConfigurationSection section) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        for (Map<?, ?> map : section.getMapList("ingredients")) {
            if (map == null || map.isEmpty()) {
                continue;
            }
            int amount = Math.max(1, parseInteger(map.get("amount"), 1));
            Object contentIdValue = map.get("content_id");
            if (contentIdValue instanceof String contentId && !contentId.isBlank()) {
                ingredients.add(ingredient(contentId, amount));
                continue;
            }
            Object materialValue = map.get("material");
            if (materialValue instanceof String materialName) {
                Material material = parseMaterial(materialName);
                if (material != null) {
                    ingredients.add(ingredient(material, amount));
                    continue;
                }
            }
            Object materialsValue = map.get("materials");
            if (materialsValue instanceof List<?> materialList) {
                List<Material> materials = new ArrayList<>();
                for (Object entry : materialList) {
                    if (entry instanceof String materialName) {
                        Material material = parseMaterial(materialName);
                        if (material != null) {
                            materials.add(material);
                        }
                    }
                }
                if (!materials.isEmpty()) {
                    ingredients.add(new RecipeIngredient(null, materials, amount));
                    continue;
                }
            }
            plugin.getLogger().warning("Skipped invalid ingredient in Terra recipe override '" + key + "'.");
        }
        return mergeIngredients(ingredients);
    }

    private int parseInteger(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private Material parseMaterial(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Material.matchMaterial(value.trim().toUpperCase(Locale.ROOT));
    }

    private void registerRecipeOverrideResult(RecipeDefinition recipe) {
        if (recipe.vanillaResult != null) {
            overriddenVanillaResults.add(recipe.vanillaResult.getType());
        }
        if (recipe.contentId != null && !recipe.contentId.isBlank()) {
            overriddenContentResults.add(recipe.contentId);
        }
    }

    private void removeRecipesForOverride(RecipeDefinition recipe) {
        for (List<RecipeDefinition> recipeList : recipesByBench.values()) {
            recipeList.removeIf(existing -> sameRecipeResult(existing, recipe));
        }
    }

    private boolean sameRecipeResult(RecipeDefinition first, RecipeDefinition second) {
        if (first == null || second == null) {
            return false;
        }
        if (first.contentId != null && second.contentId != null) {
            return first.contentId.equalsIgnoreCase(second.contentId);
        }
        return first.vanillaResult != null && second.vanillaResult != null
                && first.vanillaResult.getType() == second.vanillaResult.getType();
    }

    private void importVanillaRecipes() {
        Set<String> seen = new LinkedHashSet<>();
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if (recipe instanceof CraftingRecipe craftingRecipe) {
                ItemStack result = craftingRecipe.getResult();
                if (result == null || result.getType().isAir()) {
                    continue;
                }
                if (overriddenVanillaResults.contains(result.getType())) {
                    continue;
                }
                List<RecipeIngredient> ingredients = extractCraftingIngredients(craftingRecipe);
                if (ingredients.isEmpty()) {
                    continue;
                }
                RecipePlacement placement = placementForCraftingResult(result.getType());
                if (placement == null) {
                    continue;
                }
                if (!shouldImportCraftingRecipe(result.getType(), placement)) {
                    continue;
                }
                String key = placement.benchId + "|" + result.getType().name() + "|" + ingredients.stream().map(RecipeIngredient::signature).sorted().reduce("", String::concat);
                if (!seen.add(key)) {
                    continue;
                }
                RecipeDefinition tunedRecipe = createImportedCraftingRecipe(key, placement, result.clone(), ingredients);
                if (tunedRecipe == null) {
                    continue;
                }
                addRecipe(tunedRecipe);
                registerVanillaCatalogContent(result.getType());
                continue;
            }
            if (recipe instanceof CookingRecipe<?> cookingRecipe) {
                ItemStack result = cookingRecipe.getResult();
                if (result == null || result.getType().isAir()) {
                    continue;
                }
                RecipePlacement placement = placementForCookingRecipe(recipe, result.getType());
                if (placement == null) {
                    continue;
                }
                if (!shouldImportCookingRecipe(result.getType(), placement)) {
                    continue;
                }
                RecipeIngredient ingredient = ingredientFromChoice(cookingRecipe.getInputChoice(), 1);
                if (ingredient == null) {
                    continue;
                }
                String key = placement.benchId + "|" + result.getType().name() + "|" + ingredient.signature();
                if (!seen.add(key)) {
                    continue;
                }
                RecipeDefinition tunedRecipe = createImportedCookingRecipe(key, placement, recipe, result.clone(), ingredient);
                if (tunedRecipe != null) {
                    addRecipe(tunedRecipe);
                }
            }
        }
    }

    private void importVanillaSmeltingRecipes() {
        Set<String> seen = new LinkedHashSet<>();
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if (!(recipe instanceof CookingRecipe<?> cookingRecipe) || recipe instanceof CampfireRecipe) {
                continue;
            }
            ItemStack result = cookingRecipe.getResult();
            if (result == null || result.getType().isAir()) {
                continue;
            }
            if (result.getType().isEdible()) {
                continue;
            }
            if (overriddenVanillaResults.contains(result.getType())) {
                continue;
            }
            RecipeIngredient ingredient = ingredientFromChoice(cookingRecipe.getInputChoice(), 1);
            if (ingredient == null || ingredient.contentId != null || ingredient.materialOptions.isEmpty()) {
                continue;
            }
            String signature = result.getType().name() + "|" + ingredient.signature();
            if (!seen.add(signature)) {
                continue;
            }
            addSmeltingRecipe(new SmeltingRecipeDefinition(null, ingredient.materialOptions, null, result.clone(),
                    Math.max(FURNACE_BASE_COOK_TIME, Math.round(cookingRecipe.getCookingTime() * 1.5F))));
        }
    }

    private List<RecipeIngredient> extractCraftingIngredients(CraftingRecipe recipe) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        if (recipe instanceof ShapedRecipe shaped) {
            Map<Character, Integer> counts = new LinkedHashMap<>();
            for (String row : shaped.getShape()) {
                for (char character : row.toCharArray()) {
                    if (character != ' ') {
                        counts.merge(character, 1, Integer::sum);
                    }
                }
            }
            for (Map.Entry<Character, Integer> entry : counts.entrySet()) {
                RecipeIngredient ingredient = ingredientFromChoice(shaped.getChoiceMap().get(entry.getKey()), entry.getValue());
                if (ingredient != null) {
                    ingredients.add(ingredient);
                }
            }
            return ingredients;
        }
        if (recipe instanceof ShapelessRecipe shapeless) {
            Map<String, RecipeIngredient> merged = new LinkedHashMap<>();
            for (RecipeChoice choice : shapeless.getChoiceList()) {
                RecipeIngredient ingredient = ingredientFromChoice(choice, 1);
                if (ingredient == null) {
                    continue;
                }
                merged.compute(ingredient.signature(), (ignored, existing) -> existing == null ? ingredient : existing.withAmount(existing.amount + ingredient.amount));
            }
            ingredients.addAll(merged.values());
        }
        return ingredients;
    }

    private RecipeIngredient ingredientFromChoice(RecipeChoice choice, int amount) {
        if (choice == null || amount <= 0) {
            return null;
        }
        if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
            List<Material> options = materialChoice.getChoices().stream().filter(Objects::nonNull).filter(material -> !material.isAir()).distinct().toList();
            return options.isEmpty() ? null : new RecipeIngredient(null, options, amount);
        }
        if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
            List<Material> options = exactChoice.getChoices().stream().filter(Objects::nonNull).map(ItemStack::getType).filter(material -> !material.isAir()).distinct().toList();
            return options.isEmpty() ? null : new RecipeIngredient(null, options, amount);
        }
        return null;
    }

    private RecipePlacement placementForCraftingResult(Material material) {
        if (isDisabledTerraCraftMaterial(material)) {
            return null;
        }
        String name = material.name();
        if (name.endsWith("_FENCE") || name.endsWith("_FENCE_GATE")) {
            return new RecipePlacement("builder_workbench", "fences", name.endsWith("_FENCE_GATE") ? "Fence Gates" : "Fences", name.endsWith("_FENCE_GATE") ? Material.OAK_FENCE_GATE : Material.OAK_FENCE);
        }
        if (name.endsWith("_DOOR") || name.endsWith("_TRAPDOOR")) {
            return new RecipePlacement("builder_workbench", "doors", name.endsWith("_TRAPDOOR") ? "Trapdoors" : "Doors", name.endsWith("_TRAPDOOR") ? Material.OAK_TRAPDOOR : Material.OAK_DOOR);
        }
        if (name.endsWith("_STAIRS")) {
            return new RecipePlacement("builder_workbench", "stairs", "Stairs", Material.OAK_STAIRS);
        }
        if (name.endsWith("_SLAB")) {
            return new RecipePlacement("builder_workbench", "slabs", "Slabs", Material.STONE_SLAB);
        }
        if (name.endsWith("_WALL")) {
            return new RecipePlacement("builder_workbench", "walls", "Walls", Material.COBBLESTONE_WALL);
        }
        if (name.contains("GLASS") || name.contains("PANE")) {
            return new RecipePlacement("builder_workbench", "windows", "Glass", Material.GLASS_PANE);
        }
        if (name.endsWith("_BED")) {
            return new RecipePlacement("workbench", "wood", "Beds", Material.RED_BED);
        }
        if (isSimpleWorkbenchTool(material)) {
            return new RecipePlacement("workbench", "misc", "Hand Tools", Material.STONE_AXE);
        }
        if (name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE") || material == Material.SHEARS || material == Material.FLINT_AND_STEEL) {
            return new RecipePlacement("blacksmith_table", "tools", "Tools", Material.IRON_PICKAXE);
        }
        if (name.endsWith("_SWORD") || material == Material.BOW || material == Material.CROSSBOW || material == Material.SHIELD || name.endsWith("_ARROW")) {
            return new RecipePlacement("blacksmith_table", "weapons", "Weapons", Material.IRON_SWORD);
        }
        if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")) {
            return new RecipePlacement("blacksmith_table", "armor", "Armor", Material.IRON_CHESTPLATE);
        }
        if (name.contains("INGOT") || name.contains("NUGGET") || name.contains("CHAIN") || name.contains("BUCKET") || material == Material.CAULDRON || material == Material.HOPPER || material == Material.RAIL || material == Material.POWERED_RAIL || material == Material.DETECTOR_RAIL || material == Material.ACTIVATOR_RAIL) {
            return new RecipePlacement("blacksmith_table", "metalwork", "Metalwork", Material.CHAIN);
        }
        if (material.isEdible() || material == Material.CAKE || material == Material.COOKIE || material == Material.PUMPKIN_PIE || material == Material.SUSPICIOUS_STEW) {
            return new RecipePlacement("farmer_workbench", "meals", "Meals", Material.BREAD);
        }
        if (material == Material.HAY_BLOCK || material == Material.COMPOSTER || name.contains("HONEY") || name.contains("BUNDLE")) {
            return new RecipePlacement("farmer_workbench", material == Material.BUNDLE ? "storage" : "farm", material == Material.BUNDLE ? "Storage" : "Farm Utility", material == Material.BUNDLE ? Material.BUNDLE : Material.COMPOSTER);
        }
        if (name.contains("PLANK") || name.contains("WOOD") || name.contains("STICK") || name.contains("LADDER") || name.contains("SIGN")) {
            return new RecipePlacement("workbench", "wood", "Wood Basics", Material.OAK_PLANKS);
        }
        if (material == Material.CHEST || material == Material.BARREL || material == Material.BOOK || material == Material.BOOKSHELF || material == Material.ITEM_FRAME || material == Material.LANTERN || material == Material.CAMPFIRE) {
            return new RecipePlacement("workbench", "utility", "Utility", Material.CHEST);
        }
        if (name.contains("BOAT")) {
            return new RecipePlacement("workbench", "transport", "Transport", Material.OAK_BOAT);
        }
        if (name.contains("BRICK") || name.contains("STONE") || name.contains("TERRACOTTA") || name.contains("CONCRETE") || name.contains("COPPER_BLOCK") || material.isBlock()) {
            return new RecipePlacement("builder_workbench", "blocks", "Blocks", Material.BRICKS);
        }
        return new RecipePlacement("workbench", "misc", "Misc", Material.ITEM_FRAME);
    }

    private RecipePlacement placementForCookingRecipe(Recipe recipe, Material result) {
        if (isDisabledTerraCraftMaterial(result)) {
            return null;
        }
        if (recipe instanceof CampfireRecipe) {
            return null;
        }
        if (result.isEdible()) {
            return null;
        }
        String name = result.name();
        if (name.contains("INGOT") || name.contains("NUGGET")) {
            return new RecipePlacement("furnace_bench", "smelt_ores", "Smelted Ores", Material.IRON_INGOT);
        }
        if (name.contains("GLASS") || name.contains("STONE") || name.contains("TERRACOTTA")) {
            return new RecipePlacement("furnace_bench", "smelt_blocks", "Processed Blocks", Material.GLASS);
        }
        return new RecipePlacement("furnace_bench", "smelt_misc", "Processed Misc", Material.CHARCOAL);
    }

    private RecipeDefinition createImportedCraftingRecipe(String id, RecipePlacement placement, ItemStack result, List<RecipeIngredient> ingredients) {
        Material material = result.getType();
        List<RecipeIngredient> tunedIngredients = material == Material.STICK
                ? normalizedStickIngredients()
                : new ArrayList<>(ingredients);
        String sourceLabel = switch (placement.benchId) {
            case "builder_workbench" -> "Builder Pattern";
            case "farmer_workbench" -> "Field Pattern";
            case "blacksmith_table" -> "Forged Pattern";
            default -> "Workbench Pattern";
        };
        int specialistXpReward = switch (placement.benchId) {
            case "builder_workbench" -> 6;
            case "farmer_workbench" -> 6;
            case "blacksmith_table" -> 8;
            default -> 4;
        };
        return terraVanillaRecipe(id, placement.benchId, placement.categoryKey, placement.familyName, placement.familyIcon,
                plugin.formatMaterialName(material), Math.max(1, result.getAmount()), result, sourceLabel, false, 0, specialistXpReward,
                mergeIngredients(tunedIngredients).toArray(RecipeIngredient[]::new));
    }

    private List<RecipeIngredient> normalizedStickIngredients() {
        List<Material> plankOptions = java.util.Arrays.stream(Material.values())
                .filter(Objects::nonNull)
                .filter(material -> material.name().endsWith("_PLANKS"))
                .filter(material -> material != Material.BAMBOO_PLANKS)
                .toList();
        return plankOptions.isEmpty()
                ? List.of(ingredient(Material.OAK_PLANKS, 2))
                : List.of(new RecipeIngredient(null, plankOptions, 2));
    }

    private RecipeDefinition createImportedCookingRecipe(String id, RecipePlacement placement, Recipe recipe, ItemStack result, RecipeIngredient ingredient) {
        String sourceLabel = recipe instanceof CampfireRecipe ? "Campfire Cook"
                : recipe instanceof SmokingRecipe ? "Smoking Rack"
                : recipe instanceof BlastingRecipe ? "Blast Furnace Load"
                : "Furnace Load";
        int specialistXpReward = result.getType().isEdible() ? 6 : 5;
        return terraVanillaRecipe(id, placement.benchId, placement.categoryKey, placement.familyName, placement.familyIcon,
                plugin.formatMaterialName(result.getType()), Math.max(1, result.getAmount()), result, sourceLabel, false, 0, specialistXpReward, ingredient);
    }

    private List<RecipeIngredient> mergeIngredients(List<RecipeIngredient> ingredients) {
        Map<String, RecipeIngredient> merged = new LinkedHashMap<>();
        for (RecipeIngredient ingredient : ingredients) {
            if (ingredient == null) {
                continue;
            }
            merged.compute(ingredient.signature(), (ignored, existing) -> existing == null
                    ? ingredient
                    : existing.withAmount(existing.amount + ingredient.amount));
        }
        return new ArrayList<>(merged.values());
    }

    private boolean isSimpleWorkbenchTool(Material material) {
        String name = material.name();
        return (name.startsWith("WOODEN_") || name.startsWith("STONE_"))
                && (name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE") || name.endsWith("_SWORD"));
    }

    private boolean isDisabledTerraCraftMaterial(Material material) {
        if (material == null) {
            return false;
        }
        return isRedstoneMaterial(material) || material.name().contains("MINECART") || isNetheriteMaterial(material);
    }

    private boolean isRedstoneMaterial(Material material) {
        String name = material.name();
        return name.contains("REDSTONE")
                || material == Material.REPEATER
                || material == Material.COMPARATOR
                || material == Material.OBSERVER
                || material == Material.PISTON
                || material == Material.STICKY_PISTON
                || material == Material.DISPENSER
                || material == Material.DROPPER
                || material == Material.LEVER
                || material == Material.DAYLIGHT_DETECTOR
                || material == Material.TRIPWIRE_HOOK
                || material == Material.TARGET
                || material == Material.SCULK_SENSOR
                || material == Material.CALIBRATED_SCULK_SENSOR
                || material == Material.REDSTONE_LAMP
                || material == Material.NOTE_BLOCK
                || material == Material.LIGHTNING_ROD;
    }

    private boolean isNetheriteMaterial(Material material) {
        String name = material.name();
        return name.startsWith("NETHERITE_")
                || material == Material.ANCIENT_DEBRIS;
    }

    private boolean isBulkConstructionOutput(Material material, int amount) {
        String name = material.name();
        return amount > 1 || name.endsWith("_STAIRS") || name.endsWith("_SLAB") || name.endsWith("_WALL")
                || name.endsWith("_FENCE") || name.endsWith("_FENCE_GATE") || name.endsWith("_DOOR") || name.endsWith("_TRAPDOOR");
    }

    private boolean shouldImportCraftingRecipe(Material material, RecipePlacement placement) {
        String name = material.name();
        return switch (placement.benchId) {
            case "workbench" -> shouldImportWorkbenchRecipe(material, name, placement.categoryKey);
            case "builder_workbench" -> shouldImportBuilderRecipe(material, name, placement.categoryKey);
            case "farmer_workbench" -> shouldImportFarmerRecipe(material, name, placement.categoryKey);
            case "blacksmith_table" -> shouldImportBlacksmithRecipe(material, name, placement.categoryKey);
            case "refiner", "salvager" -> false;
            default -> true;
        };
    }

    private boolean shouldImportCookingRecipe(Material material, RecipePlacement placement) {
        if ("furnace_bench".equals(placement.benchId)) {
            String name = material.name();
            return name.contains("INGOT")
                    || name.contains("NUGGET")
                    || name.contains("GLASS")
                    || name.contains("STONE")
                    || name.contains("TERRACOTTA")
                    || material == Material.CHARCOAL;
        }
        return false;
    }

    private boolean shouldImportWorkbenchRecipe(Material material, String name, String categoryKey) {
        return switch (categoryKey) {
            case "wood" -> name.contains("PLANK") || name.contains("WOOD") || name.contains("STICK")
                    || name.contains("LADDER") || name.contains("SIGN") || material == Material.BOWL
                    || material == Material.CHEST || material == Material.BARREL || name.endsWith("_BED");
            case "utility" -> material == Material.CHEST || material == Material.BARREL || material == Material.BOOKSHELF
                    || material == Material.BOOK || material == Material.ITEM_FRAME || material == Material.CAMPFIRE
                    || material == Material.TORCH || material == Material.SOUL_TORCH || material == Material.LANTERN;
            case "transport" -> name.contains("BOAT");
            case "misc" -> material == Material.STRING || material == Material.PAPER || material == Material.BUNDLE
                    || material == Material.BOWL || material == Material.FISHING_ROD;
            default -> false;
        };
    }

    private boolean shouldImportBuilderRecipe(Material material, String name, String categoryKey) {
        return switch (categoryKey) {
            case "blocks" -> material.isBlock() && (name.contains("BRICK") || name.contains("STONE")
                    || name.contains("COBBLE") || name.contains("TERRACOTTA") || name.contains("SANDSTONE")
                    || name.contains("CONCRETE") || name.contains("MUD") || name.contains("POLISHED"));
            case "stairs" -> name.endsWith("_STAIRS");
            case "slabs" -> name.endsWith("_SLAB");
            case "walls" -> name.endsWith("_WALL");
            case "fences" -> name.endsWith("_FENCE") || name.endsWith("_FENCE_GATE");
            case "doors" -> name.endsWith("_DOOR") || name.endsWith("_TRAPDOOR");
            case "windows" -> name.contains("GLASS") || name.contains("PANE");
            case "decor" -> material == Material.LANTERN || material == Material.FLOWER_POT || material == Material.CHAIN;
            default -> false;
        };
    }

    private boolean shouldImportFarmerRecipe(Material material, String name, String categoryKey) {
        return false;
    }

    private boolean shouldImportBlacksmithRecipe(Material material, String name, String categoryKey) {
        if (material == Material.TURTLE_HELMET) {
            return false;
        }
        return switch (categoryKey) {
            case "tools" -> (name.startsWith("IRON_") || name.startsWith("GOLDEN_") || name.startsWith("DIAMOND_") || name.startsWith("NETHERITE_"))
                    && (name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE"))
                    || material == Material.SHEARS || material == Material.FLINT_AND_STEEL;
            case "weapons" -> name.endsWith("_SWORD") || material == Material.BOW || material == Material.CROSSBOW
                    || material == Material.SHIELD || name.endsWith("_ARROW");
            case "armor" -> name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
            case "metalwork" -> name.contains("INGOT") || name.contains("NUGGET") || material == Material.CHAIN
                    || material == Material.BUCKET || material == Material.CAULDRON || material == Material.HOPPER || material == Material.LANTERN;
            default -> false;
        };
    }

    private void sortBenchRecipes() {
        for (List<RecipeDefinition> recipes : recipesByBench.values()) {
            recipes.sort(Comparator.comparing((RecipeDefinition recipe) -> recipe.categoryKey)
                    .thenComparing(recipe -> recipe.familyName)
                    .thenComparing(recipe -> recipe.resultDisplayName));
        }
    }

    private List<RecipeFamily> familiesForBench(String benchId) {
        Map<String, RecipeFamily> grouped = new LinkedHashMap<>();
        for (RecipeDefinition recipe : recipesByBench.getOrDefault(benchId, List.of())) {
            String categoryDisplayName = displayNameForCategory(benchId, recipe.categoryKey);
            grouped.computeIfAbsent(recipe.familyKey, ignored -> new RecipeFamily(recipe.familyKey, recipe.familyName, recipe.familyIcon, categoryDisplayName, new ArrayList<>()))
                    .recipes.add(recipe);
        }
        return new ArrayList<>(grouped.values());
    }

    private List<RecipeDefinition> recipesForFamily(String benchId, String familyKey) {
        List<RecipeDefinition> matches = new ArrayList<>();
        for (RecipeDefinition recipe : recipesByBench.getOrDefault(benchId, List.of())) {
            if (recipe.familyKey.equalsIgnoreCase(familyKey)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    private String displayNameForCategory(String benchId, String categoryKey) {
        BenchDefinition bench = benches.get(benchId);
        BenchCategory category = bench != null ? bench.category(categoryKey) : null;
        return category != null ? category.displayName : "Recipes";
    }

    private RecipeDefinition findRecipeByResult(String benchId, Material material) {
        for (RecipeDefinition recipe : recipesByBench.getOrDefault(benchId, List.of())) {
            if (recipe.vanillaResult != null && recipe.vanillaResult.getType() == material) {
                return recipe;
            }
        }
        return null;
    }

    private RecipeDefinition findRecipeByResultAnywhere(Material material) {
        for (List<RecipeDefinition> recipes : recipesByBench.values()) {
            for (RecipeDefinition recipe : recipes) {
                if (recipe.vanillaResult != null && recipe.vanillaResult.getType() == material) {
                    return recipe;
                }
            }
        }
        return null;
    }

    private RecipeDefinition findCustomRecipeByContentId(String contentId) {
        String normalized = normalize(contentId);
        for (List<RecipeDefinition> recipes : recipesByBench.values()) {
            for (RecipeDefinition recipe : recipes) {
                if (normalized.equalsIgnoreCase(recipe.contentId)) {
                    return recipe;
                }
            }
        }
        return null;
    }

    private List<RecipeDefinition> workbenchVariantRecipes(WorkbenchVariantGroup group) {
        List<RecipeDefinition> matches = new ArrayList<>();
        for (List<RecipeDefinition> recipeList : recipesByBench.values()) {
            for (RecipeDefinition recipe : recipeList) {
                if (!group.matches(recipe)) {
                    continue;
                }
                matches.add(recipe);
            }
        }
        matches.sort(Comparator
                .comparingInt(this::workbenchVariantSortOrder)
                .thenComparing(recipe -> recipe.resultDisplayName));
        return matches;
    }

    private List<BuilderMenuEntry> builderEntries(BuilderMenuTab tab) {
        List<BuilderMenuEntry> entries = new ArrayList<>();
        switch (tab) {
            case MASONRY -> {
                entries.add(BuilderMenuEntry.group(BuilderVariantGroup.STAIRS));
                entries.add(BuilderMenuEntry.group(BuilderVariantGroup.SLABS));
                entries.add(BuilderMenuEntry.group(BuilderVariantGroup.WALLS));
                for (RecipeDefinition recipe : builderDirectRecipes("blocks")) {
                    entries.add(BuilderMenuEntry.recipe(recipe));
                }
            }
            case BOUNDARIES -> {
                entries.add(BuilderMenuEntry.group(BuilderVariantGroup.FENCES));
                entries.add(BuilderMenuEntry.group(BuilderVariantGroup.FENCE_GATES));
            }
            case OPENINGS -> {
                entries.add(BuilderMenuEntry.group(BuilderVariantGroup.DOORS));
                entries.add(BuilderMenuEntry.group(BuilderVariantGroup.TRAPDOORS));
                entries.add(BuilderMenuEntry.group(BuilderVariantGroup.WINDOWS));
            }
            case FIXTURES -> {
                for (RecipeDefinition recipe : builderDirectRecipes("decor")) {
                    entries.add(BuilderMenuEntry.recipe(recipe));
                }
            }
        }
        return entries;
    }

    private List<RecipeDefinition> builderDirectRecipes(String categoryKey) {
        List<RecipeDefinition> matches = new ArrayList<>();
        for (RecipeDefinition recipe : recipesByBench.getOrDefault("builder_workbench", List.of())) {
            if (!categoryKey.equalsIgnoreCase(recipe.categoryKey)) {
                continue;
            }
            matches.add(recipe);
        }
        matches.sort(Comparator
                .comparingInt(this::builderRecipeSortOrder)
                .thenComparing(recipe -> recipe.resultDisplayName));
        return matches;
    }

    private List<RecipeDefinition> builderVariantRecipes(BuilderVariantGroup group) {
        List<RecipeDefinition> matches = new ArrayList<>();
        for (RecipeDefinition recipe : recipesByBench.getOrDefault("builder_workbench", List.of())) {
            if (group.matches(recipe)) {
                matches.add(recipe);
            }
        }
        matches.sort(Comparator
                .comparingInt(this::builderRecipeSortOrder)
                .thenComparing(recipe -> recipe.resultDisplayName));
        return matches;
    }

    private int builderRecipeSortOrder(RecipeDefinition recipe) {
        Material material = recipe.vanillaResult != null ? recipe.vanillaResult.getType() : recipe.iconItem(this).getType();
        String name = material.name();
        if (name.contains("COBBLE")) {
            return 10;
        }
        if (name.contains("STONE_BRICK")) {
            return 20;
        }
        if (name.contains("STONE")) {
            return 30;
        }
        if (name.contains("BRICK")) {
            return 40;
        }
        if (name.contains("SANDSTONE")) {
            return 50;
        }
        if (name.contains("MUD")) {
            return 60;
        }
        if (name.contains("TERRACOTTA")) {
            return 70;
        }
        if (name.contains("CONCRETE")) {
            return 80;
        }
        if (name.contains("GLASS") || name.contains("PANE")) {
            return 90;
        }
        if (name.contains("FENCE")) {
            return 100;
        }
        if (name.contains("DOOR") || name.contains("TRAPDOOR")) {
            return 110;
        }
        if (name.contains("LANTERN") || name.contains("CHAIN") || name.contains("FLOWER_POT")) {
            return 120;
        }
        return 200;
    }

    private List<RecipeDefinition> campfireRecipes(String tabKey) {
        String categoryKey = "hearty".equalsIgnoreCase(tabKey) ? "campfire_hearty" : "campfire_rough";
        List<RecipeDefinition> matches = new ArrayList<>();
        for (RecipeDefinition recipe : recipesByBench.getOrDefault("campfire_bench", List.of())) {
            if (categoryKey.equalsIgnoreCase(recipe.categoryKey)) {
                matches.add(recipe);
            }
        }
        matches.sort(Comparator
                .comparingInt(this::campfireRecipeSortOrder)
                .thenComparing(recipe -> recipe.resultDisplayName));
        return matches;
    }

    private List<RecipeDefinition> farmerRecipes(String tabKey) {
        String categoryKey = "preserves".equalsIgnoreCase(tabKey) ? "preserves" : "prepared_meals";
        List<RecipeDefinition> matches = new ArrayList<>();
        for (RecipeDefinition recipe : recipesByBench.getOrDefault("farmer_workbench", List.of())) {
            if (categoryKey.equalsIgnoreCase(recipe.categoryKey)) {
                matches.add(recipe);
            }
        }
        matches.sort(Comparator
                .comparingInt(this::farmerRecipeSortOrder)
                .thenComparing(recipe -> recipe.resultDisplayName));
        return matches;
    }

    private int farmerRecipeSortOrder(RecipeDefinition recipe) {
        if (recipe == null || recipe.contentId == null) {
            return 100;
        }
        return switch (normalize(recipe.contentId)) {
            case "grain_loaf" -> 0;
            case "vegetable_stew" -> 1;
            case "stuffed_flatbread" -> 2;
            case "workers_hotpot" -> 3;
            case "orchard_mix" -> 4;
            case "root_bundle" -> 20;
            case "grain_ration" -> 21;
            case "pickled_roots" -> 22;
            case "farmers_pack" -> 23;
            default -> 100;
        };
    }

    private int campfireRecipeSortOrder(RecipeDefinition recipe) {
        if (recipe == null || recipe.vanillaResult == null) {
            return Integer.MAX_VALUE;
        }
        return switch (recipe.vanillaResult.getType()) {
            case COOKED_CHICKEN -> 0;
            case BREAD -> 1;
            case BAKED_POTATO -> 2;
            case MUSHROOM_STEW -> 3;
            case BEETROOT_SOUP -> 4;
            case DRIED_KELP -> 5;
            case RABBIT_STEW -> 20;
            default -> 100;
        };
    }

    private CampDishEffect campDishEffect(String contentId) {
        if (contentId == null || contentId.isBlank()) {
            return null;
        }
        return switch (normalize(contentId)) {
            case "charred_skewer" -> new CampDishEffect(4, -2, 0.0F, false, 20 * 30, null, 0, 0);
            case "ash_bread" -> new CampDishEffect(3, -2, 0.0F, false, 20 * 35, null, 0, 0);
            case "trail_mash" -> new CampDishEffect(4, -1, 0.5F, false, 20 * 25, null, 0, 0);
            case "forager_bowl" -> new CampDishEffect(4, -2, 0.5F, false, 20 * 30, null, 0, 0);
            case "beet_scraps" -> new CampDishEffect(4, -1, 0.5F, false, 20 * 28, null, 0, 0);
            case "camp_jerky" -> new CampDishEffect(3, -2, 0.0F, false, 20 * 40, null, 0, 0);
            case "hearth_hash" -> new CampDishEffect(7, 1, 5.0F, true, 0, null, 0, 0);
            case "farmers_pottage" -> new CampDishEffect(8, 2, 6.0F, true, 0, PotionEffectType.REGENERATION, 20 * 6, 0);
            case "hunters_stew" -> new CampDishEffect(8, 2, 6.5F, true, 0, PotionEffectType.SPEED, 20 * 20, 0);
            case "field_chowder" -> new CampDishEffect(7, 1, 6.0F, true, 0, PotionEffectType.REGENERATION, 20 * 4, 0);
            case "grain_loaf" -> new CampDishEffect(7, 1, 6.0F, true, 0, null, 0, 0);
            case "vegetable_stew" -> new CampDishEffect(8, 2, 7.0F, true, 0, PotionEffectType.SATURATION, 20 * 2, 0);
            case "stuffed_flatbread" -> new CampDishEffect(8, 2, 7.0F, true, 0, PotionEffectType.SPEED, 20 * 12, 0);
            case "workers_hotpot" -> new CampDishEffect(9, 3, 8.0F, true, 0, PotionEffectType.REGENERATION, 20 * 8, 0);
            case "orchard_mix" -> new CampDishEffect(7, 2, 7.5F, true, 0, PotionEffectType.SATURATION, 20 * 3, 0);
            case "root_bundle" -> new CampDishEffect(6, 1, 5.5F, true, 0, null, 0, 0);
            case "grain_ration" -> new CampDishEffect(6, 1, 6.0F, true, 0, null, 0, 0);
            case "pickled_roots" -> new CampDishEffect(6, 1, 6.0F, true, 0, PotionEffectType.SATURATION, 20 * 2, 0);
            case "farmers_pack" -> new CampDishEffect(9, 3, 8.0F, true, 0, PotionEffectType.SATURATION, 20 * 4, 0);
            default -> null;
        };
    }

    private int workbenchVariantSortOrder(RecipeDefinition recipe) {
        if (recipe == null || recipe.vanillaResult == null) {
            return Integer.MAX_VALUE;
        }
        String name = recipe.vanillaResult.getType().name();
        if (name.endsWith("_BED")) {
            return bedColorSortOrder(name);
        }
        if (name.startsWith("OAK_")) {
            return 0;
        }
        if (name.startsWith("SPRUCE_")) {
            return 1;
        }
        if (name.startsWith("BIRCH_")) {
            return 2;
        }
        if (name.startsWith("JUNGLE_")) {
            return 3;
        }
        if (name.startsWith("ACACIA_")) {
            return 4;
        }
        if (name.startsWith("DARK_OAK_")) {
            return 5;
        }
        if (name.startsWith("MANGROVE_")) {
            return 6;
        }
        if (name.startsWith("CHERRY_")) {
            return 7;
        }
        if (name.startsWith("BAMBOO_")) {
            return 8;
        }
        if (name.startsWith("CRIMSON_")) {
            return 9;
        }
        if (name.startsWith("WARPED_")) {
            return 10;
        }
        return 100;
    }

    private int bedColorSortOrder(String name) {
        if (name.startsWith("WHITE_")) {
            return 0;
        }
        if (name.startsWith("LIGHT_GRAY_")) {
            return 1;
        }
        if (name.startsWith("GRAY_")) {
            return 2;
        }
        if (name.startsWith("BLACK_")) {
            return 3;
        }
        if (name.startsWith("BROWN_")) {
            return 4;
        }
        if (name.startsWith("RED_")) {
            return 5;
        }
        if (name.startsWith("ORANGE_")) {
            return 6;
        }
        if (name.startsWith("YELLOW_")) {
            return 7;
        }
        if (name.startsWith("LIME_")) {
            return 8;
        }
        if (name.startsWith("GREEN_")) {
            return 9;
        }
        if (name.startsWith("CYAN_")) {
            return 10;
        }
        if (name.startsWith("LIGHT_BLUE_")) {
            return 11;
        }
        if (name.startsWith("BLUE_")) {
            return 12;
        }
        if (name.startsWith("PURPLE_")) {
            return 13;
        }
        if (name.startsWith("MAGENTA_")) {
            return 14;
        }
        if (name.startsWith("PINK_")) {
            return 15;
        }
        return 100;
    }

    private void registerVanillaCatalogContent(Material material) {
        if (material == null || material.isAir()) {
            return;
        }
        TerraCatalogCategory category = material.isEdible() ? TerraCatalogCategory.FOOD : TerraCatalogCategory.MATERIALS;
        String key = "vanilla_catalog_" + material.name().toLowerCase(Locale.ROOT);
        if (contentDefinitions.containsKey(key)) {
            return;
        }
        registerContent(new TerraContentDefinition(key, category, material, "&f", plugin.formatMaterialName(material), false, null, "&eAdmin spawn entry for a vanilla item.", List.of(
                "&7Vanilla material exposed in the",
                "&7Terra admin catalog."
        )));
    }

    private void addBench(BenchDefinition bench) {
        benches.put(bench.id, bench);
        recipesByBench.put(bench.id, new ArrayList<>());
    }

    private void addRecipe(RecipeDefinition recipe) {
        recipesByBench.computeIfAbsent(recipe.benchId, ignored -> new ArrayList<>()).add(recipe);
    }

    private void addSmeltingRecipe(SmeltingRecipeDefinition recipe) {
        smeltingRecipes.add(recipe);
    }

    private void setSlot(Inventory inventory, int slot, ItemStack itemStack) {
        inventory.setItem(slot, itemStack);
    }

    private void fillSlots(Inventory inventory, int[] slots, Material material, String name) {
        for (int slot : slots) {
            inventory.setItem(slot, simpleItem(material, name, List.of()));
        }
    }

    private void registerContent(TerraContentDefinition definition) {
        contentDefinitions.put(definition.id, definition);
        catalogContent.get(definition.catalogCategory).add(definition);
    }

    private RecipeDefinition customRecipe(String id, String resultDisplayName, String benchId, String categoryKey, String familyName, Material familyIcon, String contentId, int resultAmount, boolean specialistOnly, int specialistBonusOutput, int specialistXpReward, RecipeIngredient... ingredients) {
        return new RecipeDefinition(id, benchId, categoryKey, normalize(familyName), familyName, familyIcon, contentId, null, resultDisplayName, resultAmount, "Terra Custom", specialistOnly, specialistBonusOutput, specialistXpReward, List.of(ingredients));
    }

    private RecipeDefinition vanillaRecipe(String id, String benchId, String categoryKey, String familyName, Material familyIcon, String resultDisplayName, int resultAmount, ItemStack result, String sourceLabel, RecipeIngredient... ingredients) {
        return new RecipeDefinition(id, benchId, categoryKey, normalize(familyName), familyName, familyIcon, null, result.clone(), resultDisplayName, resultAmount, sourceLabel, false, 0, 0, List.of(ingredients));
    }

    private RecipeDefinition terraVanillaRecipe(String id, String benchId, String categoryKey, String familyName, Material familyIcon, String resultDisplayName, int resultAmount, ItemStack result, String sourceLabel, boolean specialistOnly, int specialistBonusOutput, int specialistXpReward, RecipeIngredient... ingredients) {
        return new RecipeDefinition(id, benchId, categoryKey, normalize(familyName), familyName, familyIcon, null, result.clone(), resultDisplayName, resultAmount, sourceLabel, specialistOnly, specialistBonusOutput, specialistXpReward, List.of(ingredients));
    }

    private RecipeIngredient ingredient(Material material, int amount) {
        return new RecipeIngredient(null, List.of(material), amount);
    }

    private RecipeIngredient ingredient(String contentId, int amount) {
        return new RecipeIngredient(normalize(contentId), List.of(), amount);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    private List<ItemStack> getBlockBreakDrops(String contentId) {
        List<ItemStack> drops = new ArrayList<>();
        if ("tin_ore_block".equalsIgnoreCase(contentId)) {
            ItemStack rawTin = createContentItem("raw_tin", 1);
            if (rawTin != null) {
                drops.add(rawTin);
            }
            return drops;
        }
        if ("salvager".equalsIgnoreCase(contentId)) {
            contentId = "refiner";
        }
        ItemStack placeable = createContentItem(contentId, 1);
        if (placeable != null) {
            drops.add(placeable);
        }
        return drops;
    }

    private void dropFurnaceContents(Block block, WorldBlockKey blockKey) {
        FurnaceBenchState state = furnaceStates.remove(blockKey);
        if (state == null) {
            return;
        }
        dropInventoryItem(block, state.inventory.getItem(FURNACE_FUEL_SLOT));
        for (int slot : FURNACE_INPUT_SLOTS) {
            dropInventoryItem(block, state.inventory.getItem(slot));
        }
        for (int slot : FURNACE_OUTPUT_SLOTS) {
            dropInventoryItem(block, state.inventory.getItem(slot));
        }
    }

    private void dropRefinerContents(Block block, WorldBlockKey blockKey) {
        RefinerBenchState state = refinerStates.remove(blockKey);
        if (state == null) {
            return;
        }
        for (int slot : REFINER_INPUT_SLOTS) {
            dropInventoryItem(block, state.inventory.getItem(slot));
        }
        for (int slot : REFINER_OUTPUT_PRIMARY_SLOTS) {
            dropInventoryItem(block, state.inventory.getItem(slot));
        }
        for (int slot : REFINER_OUTPUT_SECONDARY_SLOTS) {
            dropInventoryItem(block, state.inventory.getItem(slot));
        }
    }

    private void dropInventoryItem(Block block, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return;
        }
        block.getWorld().dropItemNaturally(block.getLocation(), itemStack.clone());
    }

    private void spawnWorkbenchLabel(Block block, BenchDefinition bench) {
        WorldBlockKey key = WorldBlockKey.fromBlock(block);
        removeWorkbenchLabel(key);
        TextDisplay display = block.getWorld().spawn(block.getLocation().add(0.5D, 1.2D, 0.5D), TextDisplay.class, entity -> {
            entity.setPersistent(true);
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setSeeThrough(true);
            entity.setShadowed(false);
            entity.setAlignment(TextDisplay.TextAlignment.CENTER);
            entity.setText(plugin.colorize(bench.color + bench.displayName));
            entity.getPersistentDataContainer().set(managedDisplayKey, PersistentDataType.BYTE, (byte) 1);
            entity.getPersistentDataContainer().set(managedDisplayBlockKey, PersistentDataType.STRING, key.serialize());
        });
        activeLabels.put(key, display.getUniqueId());
    }

    private void removeWorkbenchLabel(WorldBlockKey key) {
        UUID displayId = activeLabels.remove(key);
        if (displayId != null) {
            Entity entity = Bukkit.getEntity(displayId);
            if (entity != null) {
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

    private void respawnWorkbenchLabels() {
        for (Map.Entry<WorldBlockKey, String> entry : placedBlockIds.entrySet()) {
            BenchDefinition bench = benches.get(entry.getValue());
            Block block = entry.getKey().resolveBlock();
            if (bench != null && block != null && !block.getType().isAir()) {
                spawnWorkbenchLabel(block, bench);
            }
        }
    }

    private void loadPlacedBlocks() {
        placedBlockIds.clear();
        if (!dataFile.exists() || dataConfig.getConfigurationSection("placed-blocks") == null) {
            return;
        }
        for (String key : dataConfig.getConfigurationSection("placed-blocks").getKeys(false)) {
            String contentId = dataConfig.getString("placed-blocks." + key);
            WorldBlockKey blockKey = WorldBlockKey.deserialize(key);
            if (blockKey != null && benches.containsKey(contentId)) {
                placedBlockIds.put(blockKey, contentId);
            }
        }
    }

    private void loadRefinerUpgrades() {
        refinerUpgradeStates.clear();
        ConfigurationSection section = dataConfig.getConfigurationSection("refiner-upgrades");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            WorldBlockKey blockKey = WorldBlockKey.deserialize(key);
            if (blockKey == null) {
                continue;
            }
            int throughput = Math.max(0, Math.min(RefinerUpgradeState.MAX_LEVEL, section.getInt(key + ".throughput", 0)));
            int quality = Math.max(0, Math.min(RefinerUpgradeState.MAX_LEVEL, section.getInt(key + ".quality", 0)));
            refinerUpgradeStates.put(blockKey, new RefinerUpgradeState(throughput, quality));
        }
    }

    private void loadGuiLayouts() {
        guiLayoutOverrides.clear();
        ConfigurationSection section = dataConfig.getConfigurationSection("gui-layouts");
        if (section == null) {
            return;
        }
        for (String screenKey : section.getKeys(false)) {
            if (!EDITABLE_SCREEN_KEYS.contains(normalize(screenKey))) {
                continue;
            }
            ItemStack[] contents = new ItemStack[54];
            boolean hasAny = false;
            for (int slot = 0; slot < contents.length; slot++) {
                ItemStack itemStack = dataConfig.getItemStack("gui-layouts." + screenKey + "." + slot);
                contents[slot] = itemStack == null ? null : itemStack.clone();
                hasAny |= itemStack != null && !itemStack.getType().isAir();
            }
            if (hasAny) {
                guiLayoutOverrides.put(normalize(screenKey), contents);
            }
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

    private void saveRefinerUpgrades() {
        dataConfig.set("refiner-upgrades", null);
        for (Map.Entry<WorldBlockKey, RefinerUpgradeState> entry : refinerUpgradeStates.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBaseState()) {
                continue;
            }
            String path = "refiner-upgrades." + entry.getKey().serialize();
            dataConfig.set(path + ".throughput", entry.getValue().throughputLevel);
            dataConfig.set(path + ".quality", entry.getValue().qualityLevel);
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to save terra_crafting_data.yml: " + exception.getMessage());
        }
    }

    private void saveGuiLayouts() {
        dataConfig.set("gui-layouts", null);
        for (Map.Entry<String, ItemStack[]> entry : guiLayoutOverrides.entrySet()) {
            ItemStack[] contents = entry.getValue();
            for (int slot = 0; slot < contents.length; slot++) {
                ItemStack itemStack = contents[slot];
                if (itemStack != null && !itemStack.getType().isAir()) {
                    dataConfig.set("gui-layouts." + entry.getKey() + "." + slot, itemStack);
                }
            }
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to save terra_crafting_data.yml: " + exception.getMessage());
        }
    }

    private enum TerraCatalogCategory {
        WORKBENCHES("Workbenches", Material.CRAFTING_TABLE, "&6"),
        ORES("Ores", Material.IRON_ORE, "&f"),
        RAW_REFINED_ORE("Raw & Refined Ore", Material.RAW_IRON, "&7"),
        MATERIALS("Materials", Material.COPPER_INGOT, "&e"),
        FOOD("Food", Material.BREAD, "&6"),
        ARMOR("Armor", Material.IRON_CHESTPLATE, "&c"),
        TOOLS("Tools", Material.IRON_PICKAXE, "&b");

        private final String displayName;
        private final Material icon;
        private final String color;

        TerraCatalogCategory(String displayName, Material icon, String color) {
            this.displayName = displayName;
            this.icon = icon;
            this.color = color;
        }
    }

    public enum TerraItemQuality {
        CRUDE("crude", "Crude", "&8"),
        STANDARD("standard", "Standard", "&f"),
        FINE("fine", "Fine", "&b"),
        EXCEPTIONAL("exceptional", "Exceptional", "&d");

        private final String key;
        private final String label;
        private final String color;

        TerraItemQuality(String key, String label, String color) {
            this.key = key;
            this.label = label;
            this.color = color;
        }

        private static TerraItemQuality fromKey(String key) {
            if (key == null || key.isBlank()) {
                return STANDARD;
            }
            for (TerraItemQuality quality : values()) {
                if (quality.key.equalsIgnoreCase(key)) {
                    return quality;
                }
            }
            return STANDARD;
        }
    }

    private enum RefinerOreType {
        COAL("coal", "Coal", "&8", "raw_coal", "refined_coal", Material.COAL, Material.CHARCOAL, Material.COAL, 120,
                List.of(Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE)),
        IRON("iron", "Iron", "&f", "raw_iron", "refined_iron", Material.RAW_IRON, Material.IRON_NUGGET, Material.IRON_INGOT, 180,
                List.of(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE)),
        COPPER("copper", "Copper", "&6", "raw_copper", "refined_copper", Material.RAW_COPPER, Material.COPPER_INGOT, Material.COPPER_INGOT, 140,
                List.of(Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE)),
        GOLD("gold", "Gold", "&6", "raw_gold", "refined_gold", Material.RAW_GOLD, Material.GOLD_NUGGET, Material.GOLD_INGOT, 220,
                List.of(Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE)),
        REDSTONE("redstone", "Redstone", "&c", "raw_redstone", "refined_redstone", Material.REDSTONE, Material.GLOWSTONE_DUST, Material.REDSTONE, 240,
                List.of(Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE)),
        LAPIS("lapis", "Lapis", "&9", "raw_lapis", "refined_lapis", Material.LAPIS_LAZULI, Material.BLUE_DYE, Material.LAPIS_LAZULI, 240,
                List.of(Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE)),
        DIAMOND("diamond", "Diamond", "&b", "raw_diamond", "refined_diamond", Material.DIAMOND, Material.PRISMARINE_CRYSTALS, Material.DIAMOND, 320,
                List.of(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE)),
        EMERALD("emerald", "Emerald", "&a", "raw_emerald", "refined_emerald", Material.EMERALD, Material.SLIME_BALL, Material.EMERALD, 340,
                List.of(Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE)),
        QUARTZ("quartz", "Quartz", "&f", "raw_quartz", "refined_quartz", Material.QUARTZ, Material.QUARTZ_BLOCK, Material.QUARTZ, 260,
                List.of(Material.NETHER_QUARTZ_ORE)),
        TIN("tin", "Tin", "&7", "raw_tin", "refined_tin", Material.RAW_COPPER, Material.IRON_NUGGET, Material.IRON_INGOT, 150,
                List.of(Material.LIGHT_GRAY_GLAZED_TERRACOTTA));

        private final String key;
        private final String displayName;
        private final String color;
        private final String rawContentId;
        private final String refinedContentId;
        private final Material rawMaterial;
        private final Material refinedMaterial;
        private final Material smeltResult;
        private final int refineTicks;
        private final List<Material> blockTypes;

        RefinerOreType(String key, String displayName, String color, String rawContentId, String refinedContentId,
                       Material rawMaterial, Material refinedMaterial, Material smeltResult, int refineTicks, List<Material> blockTypes) {
            this.key = key;
            this.displayName = displayName;
            this.color = color;
            this.rawContentId = rawContentId;
            this.refinedContentId = refinedContentId;
            this.rawMaterial = rawMaterial;
            this.refinedMaterial = refinedMaterial;
            this.smeltResult = smeltResult;
            this.refineTicks = refineTicks;
            this.blockTypes = List.copyOf(blockTypes);
        }

        private static RefinerOreType fromBlock(Material material) {
            if (material == null) {
                return null;
            }
            for (RefinerOreType oreType : values()) {
                if (oreType.blockTypes.contains(material)) {
                    return oreType;
                }
            }
            return null;
        }
    }

    private record WeightedItem(ItemStack itemStack, int weight) {
    }

    private record RefiningRoll(ItemStack primaryResult, ItemStack rareBonus) {
    }

    private record RefinerRecipeDefinition(String key, String inputContentId, Material inputMaterial, String outputContentId, Material outputMaterial, int processTicks, boolean specialistRequired) {
        private static RefinerRecipeDefinition forOre(RefinerOreType oreType) {
            return new RefinerRecipeDefinition(oreType.key, oreType.rawContentId, null, oreType.refinedContentId, null, oreType.refineTicks, true);
        }

        private static RefinerRecipeDefinition timberBundle(Material inputMaterial) {
            return new RefinerRecipeDefinition("timber_" + inputMaterial.name().toLowerCase(Locale.ROOT), null, inputMaterial, "timber_bundle", null, 140, false);
        }

        private static RefinerRecipeDefinition supportBeam(Material inputMaterial) {
            return new RefinerRecipeDefinition("beam_" + inputMaterial.name().toLowerCase(Locale.ROOT), null, inputMaterial, "support_beam", null, 180, false);
        }

        private static RefinerRecipeDefinition stoneAggregate(Material inputMaterial) {
            return new RefinerRecipeDefinition("aggregate_" + inputMaterial.name().toLowerCase(Locale.ROOT), null, inputMaterial, "stone_aggregate", null, 160, false);
        }

        private static RefinerRecipeDefinition washedSand(Material inputMaterial) {
            return new RefinerRecipeDefinition("washed_sand_" + inputMaterial.name().toLowerCase(Locale.ROOT), null, inputMaterial, "washed_sand", null, 140, false);
        }

        private static RefinerRecipeDefinition washedClay() {
            return new RefinerRecipeDefinition("washed_clay", null, Material.CLAY_BALL, "washed_clay", null, 150, false);
        }

        private static RefinerRecipeDefinition flourSack() {
            return new RefinerRecipeDefinition("flour_sack", null, Material.WHEAT, "flour_sack", null, 120, false);
        }

        private static RefinerRecipeDefinition gravel() {
            return new RefinerRecipeDefinition("gravel", null, Material.GRAVEL, null, Material.FLINT, 160, false);
        }

        private static RefinerRecipeDefinition sand() {
            return new RefinerRecipeDefinition("sand", null, Material.SAND, null, Material.CLAY_BALL, 140, false);
        }

        private boolean requiresSpecialist() {
            return specialistRequired;
        }

        private ItemStack previewResult(TerraCraftingManager manager) {
            if (outputContentId != null) {
                return manager.createContentItem(outputContentId, 1);
            }
            return outputMaterial == null ? null : new ItemStack(outputMaterial, 1);
        }

        private String inputDisplayName(TerraCraftingManager manager) {
            if (inputContentId != null) {
                TerraContentDefinition definition = manager.contentDefinitions.get(inputContentId);
                return definition != null ? definition.displayName : inputContentId;
            }
            return inputMaterial != null ? manager.plugin.formatMaterialName(inputMaterial) : "Unknown";
        }

        private String outputDisplayName(TerraCraftingManager manager) {
            if (outputContentId != null) {
                TerraContentDefinition definition = manager.contentDefinitions.get(outputContentId);
                return definition != null ? definition.displayName : outputContentId;
            }
            return outputMaterial != null ? manager.plugin.formatMaterialName(outputMaterial) : "Unknown";
        }

        private RefiningRoll rollOutput(TerraCraftingManager manager, RefinerUpgradeState upgrades) {
            if ("gravel".equalsIgnoreCase(key)) {
                return new RefiningRoll(manager.rollSiftingResult("refine_gravel", 1), null);
            }
            if ("sand".equalsIgnoreCase(key)) {
                return new RefiningRoll(manager.rollSiftingResult("refine_sand", 1), null);
            }
            TerraItemQuality quality = manager.rollRefinerOutputQuality(upgrades);
            int amount = Math.random() < 0.30D ? 2 : 1;
            if (quality == TerraItemQuality.EXCEPTIONAL && Math.random() < 0.25D) {
                amount++;
            }
            ItemStack result = manager.createContentItem(outputContentId, amount, quality);
            ItemStack rare = null;
            if (key.startsWith("timber_") || key.startsWith("beam_")) {
                rare = Math.random() < manager.adjustRareChance(0.18D, upgrades) ? manager.createContentItem("bark_fiber", 1, quality) : null;
            } else if (key.startsWith("aggregate_")) {
                rare = Math.random() < manager.adjustRareChance(0.14D, upgrades) ? manager.createContentItem("mineral_resin", 1, quality) : null;
            } else if (key.startsWith("washed_sand_")) {
                rare = Math.random() < manager.adjustRareChance(0.18D, upgrades) ? manager.createContentItem("washed_clay", 1, quality) : null;
            } else if ("flour_sack".equalsIgnoreCase(key)) {
                rare = Math.random() < manager.adjustRareChance(0.10D, upgrades) ? new ItemStack(Material.WHEAT_SEEDS) : null;
            } else if (Math.random() < manager.adjustRareChance(0.12D, upgrades)) {
                rare = manager.rollRareRefiningMaterial();
            }
            return new RefiningRoll(result, rare);
        }
    }

    private record BenchDefinition(String id, String color, String displayName, Material blockMaterial, Profession specialistProfession, List<String> description, List<BenchCategory> categories) {
        private BenchCategory category(String key) {
            return categories.stream().filter(category -> category.key.equalsIgnoreCase(key)).findFirst().orElse(null);
        }
    }

    private record BenchCategory(String key, String displayName, Material icon) {}

    private record TerraContentDefinition(
            String id,
            TerraCatalogCategory catalogCategory,
            Material material,
            String color,
            String displayName,
            boolean placeable,
            Profession specialistProfession,
            String useLine,
            List<String> lore,
            Integer customModelData,
            String itemsAdderItemId
    ) {
        private TerraContentDefinition(String id, TerraCatalogCategory catalogCategory, Material material, String color, String displayName, boolean placeable, Profession specialistProfession, String useLine, List<String> lore) {
            this(id, catalogCategory, material, color, displayName, placeable, specialistProfession, useLine, lore, null, null);
        }

        private TerraContentDefinition(String id, TerraCatalogCategory catalogCategory, Material material, String color, String displayName, boolean placeable, Profession specialistProfession, String useLine, List<String> lore, Integer customModelData) {
            this(id, catalogCategory, material, color, displayName, placeable, specialistProfession, useLine, lore, customModelData, null);
        }
    }

    private record RecipeDefinition(String id, String benchId, String categoryKey, String familyKey, String familyName, Material familyIcon, String contentId, ItemStack vanillaResult, String resultDisplayName, int resultAmount, String sourceLabel, boolean specialistOnly, int specialistBonusOutput, int specialistXpReward, List<RecipeIngredient> ingredients) {
        private ItemStack iconItem(TerraCraftingManager manager) {
            if (contentId != null && !contentId.isBlank()) {
                ItemStack contentItem = manager.createContentItem(contentId, 1);
                if (contentItem != null) {
                    return contentItem;
                }
            }
            if (vanillaResult != null) {
                return vanillaResult.clone();
            }
            return new ItemStack(Material.BARRIER);
        }
    }

    private record RecipeIngredient(String contentId, List<Material> materialOptions, int amount) {
        private String displayName(TerraCraftingManager manager) {
            if (contentId != null && !contentId.isBlank()) {
                TerraContentDefinition definition = manager.contentDefinitions.get(contentId);
                return definition != null ? definition.displayName : contentId;
            }
            if (materialOptions.size() == 1) {
                return manager.plugin.formatMaterialName(materialOptions.get(0));
            }
            return manager.plugin.formatMaterialName(materialOptions.get(0)) + " or similar";
        }

        private RecipeIngredient withAmount(int amount) {
            return new RecipeIngredient(contentId, materialOptions, amount);
        }

        private String signature() {
            return contentId != null && !contentId.isBlank()
                    ? "custom:" + contentId + ":" + amount
                    : "mat:" + materialOptions.stream().map(Material::name).sorted().reduce("", String::concat) + ":" + amount;
        }
    }

    private record RecipeFamily(String key, String displayName, Material icon, String categoryDisplayName, List<RecipeDefinition> recipes) {}
    private record RecipePlacement(String benchId, String categoryKey, String familyName, Material familyIcon) {}
    private record CampDishEffect(int displayedFoodPoints, int foodAdjustment, float saturationValue, boolean saturationMinimum, int hungerTicks, PotionEffectType buffType, int buffTicks, int buffAmplifier) {}
    private enum BuilderMenuTab {
        MASONRY("Masonry", Material.BRICKS, "builder_masonry", "&7Stone, brick, stairs, slabs, and walls."),
        BOUNDARIES("Boundaries", Material.OAK_FENCE, "builder_boundaries", "&7Fences and edge pieces for plots."),
        OPENINGS("Openings", Material.OAK_DOOR, "builder_openings", "&7Doors, trapdoors, and windows."),
        FIXTURES("Fixtures", Material.LANTERN, "builder_fixtures", "&7Builder utility fittings and decor.");

        private final String displayName;
        private final Material icon;
        private final String screenKey;
        private final String descriptionLine;

        BuilderMenuTab(String displayName, Material icon, String screenKey, String descriptionLine) {
            this.displayName = displayName;
            this.icon = icon;
            this.screenKey = screenKey;
            this.descriptionLine = descriptionLine;
        }
    }

    private enum BuilderVariantGroup {
        STAIRS("Stairs", Material.OAK_STAIRS) {
            @Override boolean matches(RecipeDefinition recipe) { return "stairs".equalsIgnoreCase(recipe.categoryKey); }
        },
        SLABS("Slabs", Material.STONE_SLAB) {
            @Override boolean matches(RecipeDefinition recipe) { return "slabs".equalsIgnoreCase(recipe.categoryKey); }
        },
        WALLS("Walls", Material.COBBLESTONE_WALL) {
            @Override boolean matches(RecipeDefinition recipe) { return "walls".equalsIgnoreCase(recipe.categoryKey); }
        },
        FENCES("Fences", Material.OAK_FENCE) {
            @Override boolean matches(RecipeDefinition recipe) {
                return "fences".equalsIgnoreCase(recipe.categoryKey)
                        && recipe.vanillaResult != null
                        && recipe.vanillaResult.getType().name().endsWith("_FENCE");
            }
        },
        FENCE_GATES("Fence Gates", Material.OAK_FENCE_GATE) {
            @Override boolean matches(RecipeDefinition recipe) {
                return "fences".equalsIgnoreCase(recipe.categoryKey)
                        && recipe.vanillaResult != null
                        && recipe.vanillaResult.getType().name().endsWith("_FENCE_GATE");
            }
        },
        DOORS("Doors", Material.OAK_DOOR) {
            @Override boolean matches(RecipeDefinition recipe) {
                return "doors".equalsIgnoreCase(recipe.categoryKey)
                        && recipe.vanillaResult != null
                        && recipe.vanillaResult.getType().name().endsWith("_DOOR")
                        && !recipe.vanillaResult.getType().name().endsWith("_TRAPDOOR");
            }
        },
        TRAPDOORS("Trapdoors", Material.OAK_TRAPDOOR) {
            @Override boolean matches(RecipeDefinition recipe) {
                return "doors".equalsIgnoreCase(recipe.categoryKey)
                        && recipe.vanillaResult != null
                        && recipe.vanillaResult.getType().name().endsWith("_TRAPDOOR");
            }
        },
        WINDOWS("Windows", Material.GLASS_PANE) {
            @Override boolean matches(RecipeDefinition recipe) {
                return "windows".equalsIgnoreCase(recipe.categoryKey);
            }
        };

        private final String displayName;
        private final Material icon;

        BuilderVariantGroup(String displayName, Material icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        abstract boolean matches(RecipeDefinition recipe);
    }

    private enum WorkbenchVariantGroup {
        PLANKS("Planks", Material.OAK_PLANKS) {
            @Override boolean matches(RecipeDefinition recipe) { return isWoodFamilyRecipe(recipe, "_PLANKS"); }
        },
        STAIRS("Stairs", Material.OAK_STAIRS) {
            @Override boolean matches(RecipeDefinition recipe) { return isWoodFamilyRecipe(recipe, "_STAIRS"); }
        },
        SLABS("Slabs", Material.OAK_SLAB) {
            @Override boolean matches(RecipeDefinition recipe) { return isWoodFamilyRecipe(recipe, "_SLAB"); }
        },
        FENCES("Fences", Material.OAK_FENCE) {
            @Override boolean matches(RecipeDefinition recipe) { return isWoodFamilyRecipe(recipe, "_FENCE"); }
        },
        FENCE_GATES("Fence Gates", Material.OAK_FENCE_GATE) {
            @Override boolean matches(RecipeDefinition recipe) { return isWoodFamilyRecipe(recipe, "_FENCE_GATE"); }
        },
        DOORS("Doors", Material.OAK_DOOR) {
            @Override boolean matches(RecipeDefinition recipe) { return isWoodFamilyRecipe(recipe, "_DOOR"); }
        },
        TRAPDOORS("Trapdoors", Material.OAK_TRAPDOOR) {
            @Override boolean matches(RecipeDefinition recipe) { return isWoodFamilyRecipe(recipe, "_TRAPDOOR"); }
        },
        CHESTS("Chests", Material.CHEST) {
            @Override boolean matches(RecipeDefinition recipe) { return recipe.vanillaResult != null && recipe.vanillaResult.getType() == Material.CHEST; }
        },
        BARRELS("Barrels", Material.BARREL) {
            @Override boolean matches(RecipeDefinition recipe) { return recipe.vanillaResult != null && recipe.vanillaResult.getType() == Material.BARREL; }
        },
        BEDS("Beds", Material.RED_BED) {
            @Override boolean matches(RecipeDefinition recipe) { return recipe.vanillaResult != null && recipe.vanillaResult.getType().name().endsWith("_BED"); }
        },
        SIGNS("Signs", Material.OAK_SIGN) {
            @Override boolean matches(RecipeDefinition recipe) { return isWoodFamilyRecipe(recipe, "_SIGN"); }
        },
        LADDERS("Ladders", Material.LADDER) {
            @Override boolean matches(RecipeDefinition recipe) { return recipe.vanillaResult != null && recipe.vanillaResult.getType() == Material.LADDER; }
        },
        BOATS("Boats", Material.OAK_BOAT) {
            @Override boolean matches(RecipeDefinition recipe) { return isWoodFamilyRecipe(recipe, "_BOAT"); }
        };

        private final String displayName;
        private final Material icon;

        WorkbenchVariantGroup(String displayName, Material icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        abstract boolean matches(RecipeDefinition recipe);

        private static boolean isWoodFamilyRecipe(RecipeDefinition recipe, String suffix) {
            if (recipe.vanillaResult == null) {
                return false;
            }
            Material material = recipe.vanillaResult.getType();
            String name = material.name();
            if (!name.endsWith(suffix)) {
                return false;
            }
            return name.startsWith("OAK_")
                    || name.startsWith("SPRUCE_")
                    || name.startsWith("BIRCH_")
                    || name.startsWith("JUNGLE_")
                    || name.startsWith("ACACIA_")
                    || name.startsWith("DARK_OAK_")
                    || name.startsWith("MANGROVE_")
                    || name.startsWith("CHERRY_")
                    || name.startsWith("BAMBOO_")
                    || name.startsWith("CRIMSON_")
                    || name.startsWith("WARPED_");
        }
    }
    private record SmeltingRecipeDefinition(String inputContentId, List<Material> materialOptions, String resultContentId, ItemStack vanillaResult, int cookTicks) {
        private boolean matches(Material material, String contentId) {
            if (inputContentId != null && !inputContentId.isBlank()) {
                return inputContentId.equalsIgnoreCase(contentId);
            }
            return materialOptions.contains(material);
        }

        private ItemStack resultItem(TerraCraftingManager manager, int amount) {
            if (resultContentId != null && !resultContentId.isBlank()) {
                return manager.createContentItem(resultContentId, amount);
            }
            if (vanillaResult == null) {
                return null;
            }
            ItemStack clone = vanillaResult.clone();
            clone.setAmount(Math.max(1, vanillaResult.getAmount() * amount));
            return clone;
        }

        private String inputDisplayName(TerraCraftingManager manager) {
            if (inputContentId != null && !inputContentId.isBlank()) {
                TerraContentDefinition definition = manager.contentDefinitions.get(inputContentId);
                return definition != null ? definition.displayName : inputContentId;
            }
            return materialOptions.isEmpty() ? "Unknown" : manager.plugin.formatMaterialName(materialOptions.get(0));
        }

        private String outputDisplayName(TerraCraftingManager manager) {
            if (resultContentId != null && !resultContentId.isBlank()) {
                TerraContentDefinition definition = manager.contentDefinitions.get(resultContentId);
                return definition != null ? definition.displayName : resultContentId;
            }
            return vanillaResult == null ? "Unknown" : manager.plugin.formatMaterialName(vanillaResult.getType());
        }
    }

    private record WorldBlockKey(String worldName, int x, int y, int z) {
        private static WorldBlockKey fromBlock(Block block) {
            return new WorldBlockKey(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        }

        private String serialize() {
            return worldName + ";" + x + ";" + y + ";" + z;
        }

        private static WorldBlockKey deserialize(String text) {
            if (text == null || text.isBlank()) {
                return null;
            }
            String[] parts = text.split(";");
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

    private record StarterHubBenchKey(UUID playerId, WorldBlockKey blockKey) {}

    private record CatalogRootHolder() implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record CatalogCategoryHolder(TerraCatalogCategory category, int page) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record BenchRootHolder(String benchId, int page) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record GuiEditorHolder(String screenKey) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record GuiEditorConfirmHolder(String screenKey) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record WorkbenchMenuHolder(String tabKey) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record WorkbenchVariantHolder(WorkbenchVariantGroup group, int page) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record BuilderMenuHolder(BuilderMenuTab tab, int page) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record BuilderVariantHolder(BuilderMenuTab tab, BuilderVariantGroup group, int page) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record CampfireMenuHolder(String tabKey, int page) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record FarmerMenuHolder(String tabKey, int page) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record BenchFamilyHolder(String benchId, String familyKey, int page) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record FurnaceBenchHolder(WorldBlockKey blockKey) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record RefinerBenchHolder(WorldBlockKey blockKey) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record StarterHubFurnaceBenchHolder(UUID playerId, WorldBlockKey blockKey) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record StarterHubRefinerBenchHolder(UUID playerId, WorldBlockKey blockKey) implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    private record GuiEditorSession(String screenKey, ItemStack[] originalContents, ItemStack[] confirmContents) {}
    private record LayoutEntry(int slot, String key, ItemStack itemStack) {}
    private record FurnaceBenchState(WorldBlockKey blockKey, Inventory inventory, int[] progressTicks, SmeltingRecipeDefinition[] activeRecipes, int[] fuelTicksRemaining) {}
    private static final class RefinerUpgradeState {
        private static final int MAX_LEVEL = 3;
        private static final double THROUGHPUT_REDUCTION_PER_LEVEL = 0.12D;

        private int throughputLevel;
        private int qualityLevel;

        private RefinerUpgradeState() {
            this(0, 0);
        }

        private RefinerUpgradeState(int throughputLevel, int qualityLevel) {
            this.throughputLevel = throughputLevel;
            this.qualityLevel = qualityLevel;
        }

        private TerraItemQuality qualityFloor() {
            return switch (qualityLevel) {
                case 1, 2 -> TerraItemQuality.STANDARD;
                case 3 -> TerraItemQuality.FINE;
                default -> TerraItemQuality.STANDARD;
            };
        }

        private boolean isBaseState() {
            return throughputLevel <= 0 && qualityLevel <= 0;
        }
    }

    private record RefinerBenchState(WorldBlockKey blockKey, Inventory inventory, int[] progressTicks, RefinerRecipeDefinition[] activeRecipes, RefinerUpgradeState upgradeState) {}
    private record BuilderMenuEntry(BuilderVariantGroup group, RecipeDefinition directRecipe) {
        private static BuilderMenuEntry group(BuilderVariantGroup group) {
            return new BuilderMenuEntry(group, null);
        }

        private static BuilderMenuEntry recipe(RecipeDefinition recipe) {
            return new BuilderMenuEntry(null, recipe);
        }

        private boolean isVariantGroup() {
            return group != null;
        }

        private String displayName() {
            return isVariantGroup() ? group.displayName : directRecipe.resultDisplayName;
        }
    }
}
