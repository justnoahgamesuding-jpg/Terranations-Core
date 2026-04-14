package me.meetrow.testproject;

import me.clip.placeholderapi.PlaceholderAPI;
import me.meetrow.testproject.scoreboard.CustomScoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.HeightMap;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Levelled;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Testproject extends JavaPlugin {
    private static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER = PlainTextComponentSerializer.plainText();
    private static final String HARD_RESTART_PHASE_PATH = "system.hardrestart.phase";
    private static final long FURNACE_LOCK_DURATION_MILLIS = 48L * 60L * 60L * 1000L;
    private static final int PLAYTEST_START_COUNTDOWN_SECONDS = 30;
    private static final int PLAYTEST_STOP_COUNTDOWN_SECONDS = 10;
    private static final long DEFAULT_PLAYTEST_DURATION_MILLIS = 60L * 60L * 1000L;
    private static final double DEFAULT_PLAYTEST_XP_MULTIPLIER = 1.0D;
    private static final boolean DEFAULT_PLAYTEST_DAYLIGHT_CYCLE = false;
    private static final boolean DEFAULT_PLAYTEST_PVP = false;
    private static final boolean DEFAULT_PLAYTEST_KEEP_COUNTRY_DATA = false;
    private static final boolean DEFAULT_PLAYTEST_SAVE_PLAYER_PROGRESSION = false;
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?i)&?#([0-9a-f]{6})");
    private static final int SCOREBOARD_CONFIG_VERSION = 7;
    private static final long MINER_OVERDRIVE_DURATION_MILLIS = 10_000L;
    private static final long MINER_OVERDRIVE_COOLDOWN_MILLIS = 60_000L;
    private static final long FARMER_GROWTH_BURST_DURATION_MILLIS = 20_000L;
    private static final long FARMER_GROWTH_BURST_COOLDOWN_MILLIS = 75_000L;
    private static final long TRADER_MARKET_SCAN_COOLDOWN_MILLIS = 30_000L;
    private static final DateTimeFormatter PLAYTEST_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    public static final String ADMIN_PERMISSION = "terra.admin";
    public static final String PLUGIN_LIST_PERMISSION = "terra.plugins.view";
    public static final String COUNTRY_USE_PERMISSION = "terra.country.use";
    public static final String COUNTRY_ADMIN_PERMISSION = "terra.country.admin";
    public static final String COUNTRY_CREATE_PERMISSION = "terra.country.create";
    public static final String COUNTRY_JOIN_PERMISSION = "terra.country.join";
    public static final String COUNTRY_INVITE_PERMISSION = "terra.country.invite";
    public static final String COUNTRY_ACCEPT_INVITE_PERMISSION = "terra.country.acceptinvite";
    public static final String COUNTRY_DISBAND_PERMISSION = "terra.country.disband";
    public static final String COUNTRY_JOINSTATUS_PERMISSION = "terra.country.joinstatus";
    public static final String COUNTRY_LEAVE_PERMISSION = "terra.country.leave";
    public static final String COUNTRY_KICK_PERMISSION = "terra.country.kick";
    public static final String COUNTRY_INFO_PERMISSION = "terra.country.info";
    public static final String COUNTRY_LIST_PERMISSION = "terra.country.list";
    public static final String COUNTRY_RENAME_PERMISSION = "terra.country.rename";
    public static final String COUNTRY_FARMLAND_PERMISSION = "terra.country.farmland";
    public static final String COUNTRY_TERRITORY_PERMISSION = "terra.country.territory";
    public static final String COUNTRY_TAG_PERMISSION = "terra.country.tag";
    public static final String COUNTRY_SETOWNER_PERMISSION = "terra.country.setowner";
    public static final String COUNTRY_TRANSFER_PERMISSION = "terra.country.transfer";
    public static final String COUNTRY_ACCEPTTRANSFER_PERMISSION = "terra.country.accepttransfer";
    public static final String COUNTRY_HOME_PERMISSION = "terra.country.home";
    public static final String COUNTRY_SETHOME_PERMISSION = "terra.country.sethome";
    public static final String COUNTRY_WARP_ADMIN_PERMISSION = "terra.country.warpadmin";
    private static final int[] COUNTRY_MILESTONE_LEVELS = {5, 10, 20, 30, 40, 50};
    private static final int[] COUNTRY_LEVEL_SCORE_THRESHOLDS = {0, 24, 45, 70, 95};
    private static final double[] COUNTRY_LEVEL_UP_BALANCE_COSTS = {0.0D, 120.0D, 260.0D, 520.0D, 950.0D};
    private static final int[] COUNTRY_LEVEL_UP_RESOURCE_COSTS = {0, 48, 96, 168, 256};
    private static final int COUNTRY_MAX_LEVEL = 5;
    private static final long COUNTRY_BOOST_DURATION_MILLIS = 30L * 60_000L;
    private static final double COUNTRY_ALL_XP_BOOST_MULTIPLIER = 1.25D;
    private static final double COUNTRY_JOB_XP_BOOST_MULTIPLIER = 1.60D;
    public static final String STAFF_PERMISSION = "terra.staff";

    private final Map<UUID, Long> breakCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> placeCooldowns = new ConcurrentHashMap<>();
    private final Map<Material, BlockReward> blockRewards = new EnumMap<>(Material.class);
    private final Map<UUID, BypassEntry> bypassEntries = new LinkedHashMap<>();
    private final Map<PlacedBlockKey, UUID> placedBlocks = new ConcurrentHashMap<>();
    private final Map<PlacedBlockKey, Material> fixedOreBlocks = new ConcurrentHashMap<>();
    private final Map<FurnaceKey, FurnaceSession> furnaceSessions = new ConcurrentHashMap<>();
    private final Map<String, Country> countriesByKey = new LinkedHashMap<>();
    private final Map<UUID, String> playerCountries = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastTerritoryCountries = new ConcurrentHashMap<>();
    private final Map<UUID, Long> breakCooldownMessageTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> placeCooldownMessageTimes = new ConcurrentHashMap<>();
    private final Map<UUID, BlockActionType> lastBlockActions = new ConcurrentHashMap<>();
    private final Map<UUID, CountryTransferRequest> pendingCountryTransfers = new ConcurrentHashMap<>();
    private final Map<UUID, GameMode> staffModeGamemodes = new ConcurrentHashMap<>();
    private final Map<UUID, StaffInventoryState> staffModeInventories = new ConcurrentHashMap<>();
    private final Set<UUID> vanishedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> frozenPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> countryHomeCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Double> balances = new ConcurrentHashMap<>();
    private final Map<UUID, Profession> playerProfessions = new ConcurrentHashMap<>();
    private final Map<UUID, Profession> secondaryProfessions = new ConcurrentHashMap<>();
    private final Map<UUID, Profession> activeProfessions = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> secondaryProfessionUnlockOverrides = new ConcurrentHashMap<>();
    private final Map<UUID, Profession> developmentModeProfessions = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Profession, ProfessionProgress>> professionProgress = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Profession, Integer>> professionSkillPointBonuses = new ConcurrentHashMap<>();
    private final Map<Profession, Map<String, ProfessionSkillNode>> professionSkillNodes = new EnumMap<>(Profession.class);
    private final Map<UUID, Map<Profession, Set<String>>> unlockedProfessionSkillNodes = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Profession, Double>> pendingFractionalProfessionXp = new ConcurrentHashMap<>();
    private final Map<UUID, Set<Profession>> pendingStarterKitGrants = new ConcurrentHashMap<>();
    private final Map<UUID, TutorialStage> tutorialStages = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> tutorialIntroIndices = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> tutorialIntroAdvanceTasks = new ConcurrentHashMap<>();
    private final Map<UUID, Scoreboard> tutorialStoredScoreboards = new ConcurrentHashMap<>();
    private final Map<UUID, Scoreboard> tutorialChecklistStoredScoreboards = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> tutorialStarterXpProgress = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> completedTutorialQuestIds = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Integer>> tutorialQuestProgress = new ConcurrentHashMap<>();
    private final List<PlayerQuestDefinition> tutorialQuestDefinitions = new ArrayList<>();
    private final Map<UUID, String> tutorialQuestHudIdCache = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> tutorialQuestHudPercentCache = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> tutorialQuestHudStepCache = new ConcurrentHashMap<>();
    private final List<PlayerQuestDefinition> generalQuestDefinitions = new ArrayList<>();
    private final Map<UUID, List<String>> assignedQuestIds = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> completedAssignedQuestIds = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Integer>> assignedQuestProgress = new ConcurrentHashMap<>();
    private final Map<UUID, Double> traderReputations = new ConcurrentHashMap<>();
    private final Map<String, TraderPlayerQuest> traderQuests = new ConcurrentHashMap<>();
    private final Map<String, Long> traderQuestCooldowns = new ConcurrentHashMap<>();
    private final Map<String, TraderBigOrder> traderBigOrders = new ConcurrentHashMap<>();
    private final Map<String, MerchantShopState> activeMerchantStates = new ConcurrentHashMap<>();
    private final Map<String, Integer> merchantSharedStock = new ConcurrentHashMap<>();
    private final Map<String, Integer> merchantDailySoldAmounts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> merchantTradeCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> legendaryPickaxeBoostUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> globalChatCooldowns = new ConcurrentHashMap<>();
    private final Map<String, List<Location>> countryBorderLocationCache = new ConcurrentHashMap<>();
    private final Set<UUID> countryChatEnabledPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> globalChatDisabledPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> countryBorderParticlesDisabledPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> stabilityMeterChatEnabledPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Integer> stabilityMeterProgress = new ConcurrentHashMap<>();
    private final Map<UUID, Long> temporaryStabilityDebugExpiries = new ConcurrentHashMap<>();
    private final Map<String, Boolean> daylightCycleStates = new HashMap<>();
    private final Map<String, Boolean> playtestDaylightCycleStates = new HashMap<>();
    private final Map<String, Boolean> playtestPvpStates = new HashMap<>();

    private boolean blockDelayEnabled;
    private int blockDelaySeconds;
    private LuckPerms luckPerms;
    private TerritoryService territoryService = new NoOpTerritoryService("integration-unavailable");
    private BukkitTask realTimeClockTask;
    private BukkitTask globalXpBoostTask;
    private BukkitTask cooldownDebugTask;
    private BukkitTask oreVisionTask;
    private BukkitTask playtestCountdownTask;
    private BukkitTask playtestTickTask;
    private BukkitTask traderRuntimeTask;
    private BukkitTask tutorialActionBarTask;
    private BukkitTask persistentActionBarTask;
    private BukkitTask climateBossBarTask;
    private BukkitTask climateFreezeTask;
    private BukkitTask climateDebugParticleTask;
    private BukkitTask climateCropEffectTask;
    private BukkitTask terraTipsTask;
    private BukkitTask stabilityDebugTask;
    private BukkitTask merchantRuntimeTask;
    private BukkitTask mobSuppressionTask;
    private BukkitTask npcHeadTrackingTask;
    private BukkitTask countryBorderParticlesTask;
    private BukkitTask groundItemClearTask;
    private BukkitTask mobStackingTask;
    private ItemsAdderTopStatusHud itemsAdderTopStatusHud;
    private CustomScoreboard customScoreboard;
    private ProfessionSelectionListener professionSelectionListener;
    private ProfessionAdminGuiListener professionAdminGuiListener;
    private JobConfigGuiListener jobConfigGuiListener;
    private CountryGuiListener countryGuiListener;
    private CountryWarpGuiListener countryWarpGuiListener;
    private TraderQuestListener traderQuestListener;
    private MerchantShopListener merchantShopListener;
    private StaffMenuListener staffMenuListener;
    private PlaytestGuiListener playtestGuiListener;
    private StabilityGuiListener stabilityGuiListener;
    private QuestAdminGuiListener questAdminGuiListener;
    private BossBar globalXpBoostBossBar;
    private final Set<UUID> cooldownDebugPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, BossBar> breakCooldownDebugBars = new ConcurrentHashMap<>();
    private final Map<UUID, BossBar> placeCooldownDebugBars = new ConcurrentHashMap<>();
    private final Set<UUID> persistentActionBarPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, BossBar> climateBossBars = new ConcurrentHashMap<>();
    private final Set<String> climateFrozenWaterBlocks = ConcurrentHashMap.newKeySet();
    private final Map<UUID, List<ClimateDebugRegion>> climateDebugRegions = new ConcurrentHashMap<>();
    private final Set<UUID> climateLiveDisplayPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> oreVisionPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Map<PlacedBlockKey, UUID>> oreVisionDisplays = new ConcurrentHashMap<>();
    private final Map<UUID, Long> minerOverdriveUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> minerOverdriveCooldownUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> farmerGrowthBurstUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> farmerGrowthBurstCooldownUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> traderMarketScanCooldownUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Map<PlacedBlockKey, StabilityDebugDisplayState>> stabilityDebugDisplays = new ConcurrentHashMap<>();
    private final Set<Material> configuredStructuralSupportMaterials = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> climateRecentRainEndMillis = new ConcurrentHashMap<>();
    private final Map<UUID, StabilityCollapse> pendingStabilityCollapses = new ConcurrentHashMap<>();
    private final Set<PlacedBlockKey> pendingStabilityBlockKeys = ConcurrentHashMap.newKeySet();
    private final Set<UUID> stabilityDebugPlayers = ConcurrentHashMap.newKeySet();
    private final Set<String> queuedStabilityScanOrigins = ConcurrentHashMap.newKeySet();
    private final Map<String, Location> deferredStabilityRescans = new ConcurrentHashMap<>();
    private double globalXpBoostMultiplier = 1.0D;
    private long globalXpBoostEndMillis;
    private long globalXpBoostDurationMillis;
    private String globalXpBoostEnabledBy;
    private boolean playtestPreparing;
    private boolean playtestActive;
    private boolean playtestStopPending;
    private long playtestPrepareEndMillis;
    private long playtestEndMillis;
    private long playtestDurationMillis;
    private String playtestStartedBy;
    private long configuredPlaytestDurationMillis = DEFAULT_PLAYTEST_DURATION_MILLIS;
    private double configuredPlaytestXpBoostMultiplier = DEFAULT_PLAYTEST_XP_MULTIPLIER;
    private boolean configuredPlaytestDaylightCycle = DEFAULT_PLAYTEST_DAYLIGHT_CYCLE;
    private boolean configuredPlaytestPvp = DEFAULT_PLAYTEST_PVP;
    private boolean configuredPlaytestKeepCountryData = DEFAULT_PLAYTEST_KEEP_COUNTRY_DATA;
    private boolean configuredPlaytestSavePlayerProgression = DEFAULT_PLAYTEST_SAVE_PLAYER_PROGRESSION;
    private double playtestXpBoostMultiplier = DEFAULT_PLAYTEST_XP_MULTIPLIER;
    private boolean playtestDaylightCycleEnabled = DEFAULT_PLAYTEST_DAYLIGHT_CYCLE;
    private boolean playtestPvpEnabled = DEFAULT_PLAYTEST_PVP;
    private boolean playtestKeepCountryData = DEFAULT_PLAYTEST_KEEP_COUNTRY_DATA;
    private boolean playtestSavePlayerProgression = DEFAULT_PLAYTEST_SAVE_PLAYER_PROGRESSION;
    private boolean playtestResumeRealTimeClock;
    private final Set<Long> announcedPlaytestReminders = ConcurrentHashMap.newKeySet();
    private final Map<String, DynamicTraderState> activeTraderStates = new ConcurrentHashMap<>();
    private long nextTraderSpawnMillis;
    private int nextTerraTipIndex;
    private long nextMerchantSpawnMillis;
    private long merchantDailySalesResetMillis;
    private long merchantCycleSeed;

    private enum TutorialStage {
        SELECT_PRIMARY,
        EARN_FIRST_XP,
        BUILD_MOMENTUM,
        JOIN_COUNTRY,
        CONTRIBUTE_TO_COUNTRY
    }

    private enum BlockActionType {
        BREAK,
        PLACE
    }

    private File blockValuesFile;
    private File jobsFile;
    private File chatFile;
    private File messagesFile;
    private File dataFile;
    private File countryDataFile;
    private File coreSettingsFile;
    private File climateSettingsFile;
    private File stabilitySettingsFile;
    private File merchantSettingsFile;
    private File territorySettingsFile;
    private File questsSettingsFile;
    private File scoreboardSettingsFile;
    private FileConfiguration blockValuesConfig;
    private FileConfiguration jobsConfig;
    private FileConfiguration chatConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration dataConfig;
    private FileConfiguration countryDataConfig;
    private FileConfiguration coreSettingsConfig;
    private FileConfiguration climateSettingsConfig;
    private FileConfiguration stabilitySettingsConfig;
    private FileConfiguration merchantSettingsConfig;
    private FileConfiguration territorySettingsConfig;
    private FileConfiguration questsSettingsConfig;
    private FileConfiguration scoreboardSettingsConfig;
    private final Map<Profession, File> professionFiles = new EnumMap<>(Profession.class);
    private final Map<Profession, FileConfiguration> professionConfigs = new EnumMap<>(Profession.class);
    private NamespacedKey itemSourceOwnerKey;
    private NamespacedKey itemSourceProfessionKey;
    private NamespacedKey traderNpcKey;
    private NamespacedKey merchantNpcKey;
    private NamespacedKey fixedOreToolKey;
    private NamespacedKey climateCropLoreKey;
    private NamespacedKey soulboundItemKey;
    private NamespacedKey guidanceItemKey;
    private NamespacedKey stackedMobCountKey;
    private NamespacedKey forgedItemKey;
    private NamespacedKey forgedLevelKey;
    private NamespacedKey forgedRarityKey;
    private final Set<UUID> maintenanceAllowedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<Integer> groundItemClearWarningsSent = ConcurrentHashMap.newKeySet();
    private boolean maintenanceModeEnabled;
    private long nextGroundItemClearMillis;

    @Override
    public void onEnable() {
        itemSourceOwnerKey = new NamespacedKey(this, "item_source_owner");
        itemSourceProfessionKey = new NamespacedKey(this, "item_source_profession");
        traderNpcKey = new NamespacedKey(this, "dynamic_trader_npc");
        merchantNpcKey = new NamespacedKey(this, "wandering_merchant_npc");
        fixedOreToolKey = new NamespacedKey(this, "fixed_ore_tool");
        climateCropLoreKey = new NamespacedKey(this, "climate_crop_lore");
        soulboundItemKey = new NamespacedKey(this, "soulbound_item");
        guidanceItemKey = new NamespacedKey(this, "guidance_item");
        stackedMobCountKey = new NamespacedKey(this, "stacked_mob_count");
        forgedItemKey = new NamespacedKey(this, "forged_item");
        forgedLevelKey = new NamespacedKey(this, "forged_level");
        forgedRarityKey = new NamespacedKey(this, "forged_rarity");
        saveDefaultConfigFiles();
        reloadPluginSettings();
        if (!setupLuckPerms()) {
            getLogger().warning("LuckPerms was not found. Bypass list group names will fall back to unknown.");
        }

        territoryService = createTerritoryService();
        territoryService.syncAll(countriesByKey.values());
        refreshAllCountryTags();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TerraPlaceholderExpansion(this).register();
        }

        getServer().getPluginManager().registerEvents(new BlockDelayListener(this), this);
        getServer().getPluginManager().registerEvents(new HostileMobSpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new PhantomSpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new HungerRateListener(this), this);
        getServer().getPluginManager().registerEvents(new StabilityListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new CountryTerritoryListener(this), this);
        getServer().getPluginManager().registerEvents(new CountryFarmlandListener(this), this);
        getServer().getPluginManager().registerEvents(new ProfessionCraftListener(this), this);
        getServer().getPluginManager().registerEvents(new ProfessionSmeltingListener(this), this);
        getServer().getPluginManager().registerEvents(new ProfessionActionListener(this), this);
        getServer().getPluginManager().registerEvents(new ProfessionAbilityListener(this), this);
        getServer().getPluginManager().registerEvents(new BlacksmithAnvilListener(this), this);
        getServer().getPluginManager().registerEvents(new ForgedEquipmentListener(this), this);
        getServer().getPluginManager().registerEvents(new StaffModeListener(this), this);
        getServer().getPluginManager().registerEvents(new StaffUtilityListener(this), this);
        getServer().getPluginManager().registerEvents(new GlobalXpBoostListener(this), this);
        getServer().getPluginManager().registerEvents(new CooldownDebugListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatSoundListener(this), this);
        getServer().getPluginManager().registerEvents(new TutorialIntroListener(this), this);
        getServer().getPluginManager().registerEvents(new ClimateListener(this), this);
        getServer().getPluginManager().registerEvents(new ClimateWeatherListener(this), this);
        getServer().getPluginManager().registerEvents(new ClimateGuiListener(this), this);
        getServer().getPluginManager().registerEvents(new SoulboundListener(this), this);
        getServer().getPluginManager().registerEvents(new GuidanceItemListener(this), this);
        getServer().getPluginManager().registerEvents(new TerraGuideListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerUtilityListener(this), this);
        professionSelectionListener = new ProfessionSelectionListener(this);
        getServer().getPluginManager().registerEvents(professionSelectionListener, this);
        getServer().getPluginManager().registerEvents(new ChatRoutingListener(this), this);
        professionAdminGuiListener = new ProfessionAdminGuiListener(this);
        getServer().getPluginManager().registerEvents(professionAdminGuiListener, this);
        jobConfigGuiListener = new JobConfigGuiListener(this);
        getServer().getPluginManager().registerEvents(jobConfigGuiListener, this);
        countryGuiListener = new CountryGuiListener(this);
        getServer().getPluginManager().registerEvents(countryGuiListener, this);
        playtestGuiListener = new PlaytestGuiListener(this);
        getServer().getPluginManager().registerEvents(playtestGuiListener, this);
        stabilityGuiListener = new StabilityGuiListener(this);
        getServer().getPluginManager().registerEvents(stabilityGuiListener, this);
        questAdminGuiListener = new QuestAdminGuiListener(this);
        getServer().getPluginManager().registerEvents(questAdminGuiListener, this);
        countryWarpGuiListener = new CountryWarpGuiListener(this);
        getServer().getPluginManager().registerEvents(countryWarpGuiListener, this);
        traderQuestListener = new TraderQuestListener(this);
        getServer().getPluginManager().registerEvents(traderQuestListener, this);
        merchantShopListener = new MerchantShopListener(this);
        getServer().getPluginManager().registerEvents(merchantShopListener, this);
        staffMenuListener = new StaffMenuListener(this);
        getServer().getPluginManager().registerEvents(staffMenuListener, this);
        getServer().getPluginManager().registerEvents(new FixedOreToolListener(this), this);
        getServer().getPluginManager().registerEvents(new RestrictedItemListener(this), this);

        TerraCommand terraCommand = new TerraCommand(this);
        PluginCommand terraRootCommand = getCommand("terra");
        if (terraRootCommand != null) {
            terraRootCommand.setExecutor(terraCommand);
            terraRootCommand.setTabCompleter(terraCommand);
        } else {
            getLogger().warning("Command 'terra' is missing from plugin.yml.");
        }

        PluginCommand jobsRootCommand = getCommand("jobs");
        if (jobsRootCommand != null) {
            jobsRootCommand.setExecutor(terraCommand);
            jobsRootCommand.setTabCompleter(terraCommand);
        } else {
            getLogger().warning("Command 'jobs' is missing from plugin.yml.");
        }

        PluginCommand traderCommand = getCommand("trader");
        if (traderCommand != null) {
            traderCommand.setExecutor(terraCommand);
            traderCommand.setTabCompleter(terraCommand);
        } else {
            getLogger().warning("Command 'trader' is missing from plugin.yml.");
        }

        PluginCommand merchantCommand = getCommand("merchant");
        if (merchantCommand != null) {
            merchantCommand.setExecutor(terraCommand);
            merchantCommand.setTabCompleter(terraCommand);
        } else {
            getLogger().warning("Command 'merchant' is missing from plugin.yml.");
        }

        PluginCommand climateCommand = getCommand("climate");
        if (climateCommand != null) {
            climateCommand.setExecutor(terraCommand);
            climateCommand.setTabCompleter(terraCommand);
        } else {
            getLogger().warning("Command 'climate' is missing from plugin.yml.");
        }

        PluginCommand flyspeedCommand = getCommand("flyspeed");
        if (flyspeedCommand != null) {
            flyspeedCommand.setExecutor(terraCommand);
            flyspeedCommand.setTabCompleter(terraCommand);
        } else {
            getLogger().warning("Command 'flyspeed' is missing from plugin.yml.");
        }

        PluginCommand vanishCommand = getCommand("vanish");
        if (vanishCommand != null) {
            vanishCommand.setExecutor(terraCommand);
            vanishCommand.setTabCompleter(terraCommand);
        } else {
            getLogger().warning("Command 'vanish' is missing from plugin.yml.");
        }

        PluginCommand rollbackAreaCommand = getCommand("rollbackarea");
        if (rollbackAreaCommand != null) {
            rollbackAreaCommand.setExecutor(terraCommand);
            rollbackAreaCommand.setTabCompleter(terraCommand);
        } else {
            getLogger().warning("Command 'rollbackarea' is missing from plugin.yml.");
        }

        PluginCommand undoAreaCommand = getCommand("undoarea");
        if (undoAreaCommand != null) {
            undoAreaCommand.setExecutor(terraCommand);
            undoAreaCommand.setTabCompleter(terraCommand);
        } else {
            getLogger().warning("Command 'undoarea' is missing from plugin.yml.");
        }

        CountryCommand countryCommand = new CountryCommand(this);
        PluginCommand countryRootCommand = getCommand("country");
        if (countryRootCommand != null) {
            countryRootCommand.setExecutor(countryCommand);
            countryRootCommand.setTabCompleter(countryCommand);
        } else {
            getLogger().warning("Command 'country' is missing from plugin.yml.");
        }

        ChatChannelCommand chatChannelCommand = new ChatChannelCommand(this);
        PluginCommand countryChatCommand = getCommand("countrychat");
        if (countryChatCommand != null) {
            countryChatCommand.setExecutor(chatChannelCommand);
            countryChatCommand.setTabCompleter(chatChannelCommand);
        } else {
            getLogger().warning("Command 'countrychat' is missing from plugin.yml.");
        }

        PluginCommand globalChatCommand = getCommand("globalchat");
        if (globalChatCommand != null) {
            globalChatCommand.setExecutor(chatChannelCommand);
            globalChatCommand.setTabCompleter(chatChannelCommand);
        } else {
            getLogger().warning("Command 'globalchat' is missing from plugin.yml.");
        }

        PluginCommand staffCommand = getCommand("staff");
        if (staffCommand != null) {
            StaffCommand executor = new StaffCommand(this);
            staffCommand.setExecutor(executor);
            staffCommand.setTabCompleter(executor);
        } else {
            getLogger().warning("Command 'staff' is missing from plugin.yml.");
        }

        PluginCommand spawnCommand = getCommand("spawn");
        if (spawnCommand != null) {
            SpawnCommand executor = new SpawnCommand(this);
            spawnCommand.setExecutor(executor);
            spawnCommand.setTabCompleter(executor);
        } else {
            getLogger().warning("Command 'spawn' is missing from plugin.yml.");
        }

        PluginCommand balanceCommand = getCommand("balance");
        if (balanceCommand != null) {
            BalanceCommand executor = new BalanceCommand(this);
            balanceCommand.setExecutor(executor);
            balanceCommand.setTabCompleter(executor);
        } else {
            getLogger().warning("Command 'balance' is missing from plugin.yml.");
        }

        restartRealTimeClock();
        reloadGlobalXpBoostState();
        startOreVisionRuntime();
        restorePlaytestState();
        restartTraderRuntime();
        restartMerchantRuntime();
        restartMobSuppressionRuntime();
        restartNpcHeadTrackingRuntime();
        restartCountryBorderParticlesRuntime();
        restartTerraTipsRuntime();
        restartStabilityDebugRuntime();
        restartLagReductionRuntime();
        restartItemsAdderTopStatusHud();
        restartCustomScoreboard();
        ensurePersistentActionBarTask();
        restartClimateRuntime();
        processPendingHardRestartPhase();

        for (Player player : getServer().getOnlinePlayers()) {
            ensurePlayerGuidanceItem(player);
            handleTutorialJoin(player);
            if (requiresProfessionSelection(player) && !isTutorialIntroActive(player.getUniqueId())) {
                openProfessionMenu(player);
            }
        }
        refreshDescriptiveItemDetailsAfterReload();
    }

    @Override
    public void onDisable() {
        for (Player player : getServer().getOnlinePlayers()) {
            setVanished(player, false);
            setFrozen(player.getUniqueId(), false);
            disableStaffMode(player);
        }
        saveTraderData();
        saveMerchantData();
        stopPlaytestTasks();
        restorePlaytestWorldSettings();
        shutdownGlobalXpBoostRuntime();
        shutdownCooldownDebugRuntime();
        shutdownOreVisionRuntime();
        shutdownPersistentActionBarRuntime();
        shutdownClimateRuntime();
        stopItemsAdderTopStatusHud();
        stopCustomScoreboard();
        stopTerraTipsRuntime();
        stopRealTimeClock();
        stopTraderRuntime();
        stopMerchantRuntime();
        stopMobSuppressionRuntime();
        stopNpcHeadTrackingRuntime();
        stopCountryBorderParticlesRuntime();
        stopStabilityDebugRuntime();
        stopLagReductionRuntime();
        breakCooldowns.clear();
        placeCooldowns.clear();
        blockRewards.clear();
        bypassEntries.clear();
        placedBlocks.clear();
        fixedOreBlocks.clear();
        furnaceSessions.clear();
        countriesByKey.clear();
        playerCountries.clear();
        lastTerritoryCountries.clear();
        breakCooldownMessageTimes.clear();
        placeCooldownMessageTimes.clear();
        lastBlockActions.clear();
        cooldownDebugPlayers.clear();
        breakCooldownDebugBars.clear();
        placeCooldownDebugBars.clear();
        persistentActionBarPlayers.clear();
        climateBossBars.clear();
        climateFrozenWaterBlocks.clear();
        climateDebugRegions.clear();
        climateLiveDisplayPlayers.clear();
        climateRecentRainEndMillis.clear();
        oreVisionPlayers.clear();
        oreVisionDisplays.clear();
        minerOverdriveUntil.clear();
        minerOverdriveCooldownUntil.clear();
        farmerGrowthBurstUntil.clear();
        farmerGrowthBurstCooldownUntil.clear();
        traderMarketScanCooldownUntil.clear();
        stabilityDebugDisplays.clear();
        clearAllPendingStabilityCollapses();
        pendingStabilityBlockKeys.clear();
        queuedStabilityScanOrigins.clear();
        stabilityDebugPlayers.clear();
        deferredStabilityRescans.clear();
        pendingCountryTransfers.clear();
        staffModeGamemodes.clear();
        staffModeInventories.clear();
        vanishedPlayers.clear();
        frozenPlayers.clear();
        maintenanceAllowedPlayers.clear();
        groundItemClearWarningsSent.clear();
        countryHomeCooldowns.clear();
        playerProfessions.clear();
        secondaryProfessions.clear();
        activeProfessions.clear();
        secondaryProfessionUnlockOverrides.clear();
        developmentModeProfessions.clear();
        professionProgress.clear();
        professionSkillPointBonuses.clear();
        professionSkillNodes.clear();
        unlockedProfessionSkillNodes.clear();
        tutorialStages.clear();
        tutorialIntroIndices.clear();
        for (BukkitTask task : tutorialIntroAdvanceTasks.values()) {
            task.cancel();
        }
        tutorialIntroAdvanceTasks.clear();
        tutorialStoredScoreboards.clear();
        tutorialChecklistStoredScoreboards.clear();
        tutorialStarterXpProgress.clear();
        tutorialQuestHudIdCache.clear();
        tutorialQuestHudPercentCache.clear();
        tutorialQuestHudStepCache.clear();
        generalQuestDefinitions.clear();
        assignedQuestIds.clear();
        completedAssignedQuestIds.clear();
        assignedQuestProgress.clear();
        traderReputations.clear();
        traderQuests.clear();
        traderQuestCooldowns.clear();
        traderBigOrders.clear();
        activeMerchantStates.clear();
        merchantSharedStock.clear();
        merchantDailySoldAmounts.clear();
        merchantTradeCooldowns.clear();
        globalChatCooldowns.clear();
        countryBorderLocationCache.clear();
        countryChatEnabledPlayers.clear();
        globalChatDisabledPlayers.clear();
        countryBorderParticlesDisabledPlayers.clear();
        stabilityMeterChatEnabledPlayers.clear();
        stabilityMeterProgress.clear();
        temporaryStabilityDebugExpiries.clear();
        activeTraderStates.clear();
        nextTraderSpawnMillis = 0L;
    }

    public boolean isBlockDelayEnabled() {
        return blockDelayEnabled;
    }

    public void setBlockDelayEnabled(boolean enabled) {
        this.blockDelayEnabled = enabled;
        setManagedConfigValue("block-delay.enabled", enabled);
    }

    public int getBlockDelaySeconds() {
        return blockDelaySeconds;
    }

    public void setBlockDelaySeconds(int seconds) {
        this.blockDelaySeconds = seconds;
        setManagedConfigValue("block-delay.seconds", seconds);
    }

    public long getBreakCooldownEnd(UUID playerId) {
        return breakCooldowns.getOrDefault(playerId, 0L);
    }

    public void setBreakCooldown(UUID playerId, long cooldownEnd) {
        breakCooldowns.put(playerId, cooldownEnd);
    }

    public long getPlaceCooldownEnd(UUID playerId) {
        return placeCooldowns.getOrDefault(playerId, 0L);
    }

    public void setPlaceCooldown(UUID playerId, long cooldownEnd) {
        placeCooldowns.put(playerId, cooldownEnd);
    }

    public long getLastBreakCooldownMessageTime(UUID playerId) {
        return breakCooldownMessageTimes.getOrDefault(playerId, 0L);
    }

    public void setLastBreakCooldownMessageTime(UUID playerId, long time) {
        breakCooldownMessageTimes.put(playerId, time);
    }

    public long getLastPlaceCooldownMessageTime(UUID playerId) {
        return placeCooldownMessageTimes.getOrDefault(playerId, 0L);
    }

    public void setLastPlaceCooldownMessageTime(UUID playerId, long time) {
        placeCooldownMessageTimes.put(playerId, time);
    }

    public boolean shouldEnforceBlockActionCooldown(UUID playerId, boolean breakAction) {
        return true;
    }

    public void noteSuccessfulBlockAction(UUID playerId, boolean breakAction) {
        if (playerId == null) {
            return;
        }
        lastBlockActions.put(playerId, breakAction ? BlockActionType.BREAK : BlockActionType.PLACE);
    }

    public void clearLastBlockAction(UUID playerId) {
        if (playerId == null) {
            return;
        }
        lastBlockActions.remove(playerId);
    }

    public boolean toggleCooldownDebug(Player player) {
        if (player == null) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        if (!cooldownDebugPlayers.add(playerId)) {
            cooldownDebugPlayers.remove(playerId);
            removeCooldownDebugBars(playerId);
            stopCooldownDebugTaskIfIdle();
            return false;
        }

        refreshCooldownDebugBars(player);
        ensureCooldownDebugTask();
        return true;
    }

    public boolean isCooldownDebugEnabled(UUID playerId) {
        return playerId != null && cooldownDebugPlayers.contains(playerId);
    }

    public void refreshCooldownDebugBars(Player player) {
        if (player == null || !cooldownDebugPlayers.contains(player.getUniqueId())) {
            return;
        }

        BossBar breakBar = breakCooldownDebugBars.computeIfAbsent(
                player.getUniqueId(),
                ignored -> getServer().createBossBar("", BarColor.RED, BarStyle.SEGMENTED_10)
        );
        BossBar placeBar = placeCooldownDebugBars.computeIfAbsent(
                player.getUniqueId(),
                ignored -> getServer().createBossBar("", BarColor.GREEN, BarStyle.SEGMENTED_10)
        );

        breakBar.removeAll();
        placeBar.removeAll();
        breakBar.addPlayer(player);
        placeBar.addPlayer(player);
        breakBar.setVisible(true);
        placeBar.setVisible(true);
        updateCooldownDebugBars(player);
    }

    public void handleCooldownDebugQuit(Player player) {
        if (player == null) {
            return;
        }
        UUID playerId = player.getUniqueId();
        BossBar breakBar = breakCooldownDebugBars.get(playerId);
        BossBar placeBar = placeCooldownDebugBars.get(playerId);
        if (breakBar != null) {
            breakBar.removePlayer(player);
        }
        if (placeBar != null) {
            placeBar.removePlayer(player);
        }
    }

    public boolean toggleOreVision(Player player) {
        if (player == null) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        if (!oreVisionPlayers.add(playerId)) {
            oreVisionPlayers.remove(playerId);
            clearOreVisionDisplays(playerId);
            return false;
        }

        refreshOreVision(player);
        return true;
    }

    public boolean isOreVisionEnabled(UUID playerId) {
        return playerId != null && oreVisionPlayers.contains(playerId);
    }

    private void startOreVisionRuntime() {
        shutdownOreVisionRuntime();
        oreVisionTask = getServer().getScheduler().runTaskTimer(this, this::tickOreVision, 20L, 40L);
    }

    private void shutdownOreVisionRuntime() {
        if (oreVisionTask != null) {
            oreVisionTask.cancel();
            oreVisionTask = null;
        }
        for (UUID playerId : new ArrayList<>(oreVisionDisplays.keySet())) {
            clearOreVisionDisplays(playerId);
        }
    }

    private void tickOreVision() {
        oreVisionPlayers.removeIf(playerId -> {
            Player player = getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                clearOreVisionDisplays(playerId);
                return true;
            }
            refreshOreVision(player);
            return false;
        });
    }

    private void refreshOreVision(Player player) {
        if (player == null || !oreVisionPlayers.contains(player.getUniqueId())) {
            return;
        }

        Map<PlacedBlockKey, UUID> activeDisplays = oreVisionDisplays.computeIfAbsent(
                player.getUniqueId(),
                ignored -> new ConcurrentHashMap<>()
        );
        Set<PlacedBlockKey> desiredKeys = new LinkedHashSet<>();
        World world = player.getWorld();
        int horizontalRadius = Math.max(8, getConfig().getInt("ore-vision.horizontal-radius", 20));
        int verticalRadius = Math.max(4, getConfig().getInt("ore-vision.vertical-radius", 12));
        int centerX = player.getLocation().getBlockX();
        int centerY = player.getLocation().getBlockY();
        int centerZ = player.getLocation().getBlockZ();
        int minY = Math.max(world.getMinHeight(), centerY - verticalRadius);
        int maxY = Math.min(world.getMaxHeight() - 1, centerY + verticalRadius);

        for (int x = centerX - horizontalRadius; x <= centerX + horizontalRadius; x++) {
            for (int z = centerZ - horizontalRadius; z <= centerZ + horizontalRadius; z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!isOreVisionMaterial(block.getType())) {
                        continue;
                    }

                    PlacedBlockKey key = PlacedBlockKey.from(block);
                    desiredKeys.add(key);
                    activeDisplays.computeIfAbsent(key, ignored -> spawnOreVisionDisplay(player, block));
                }
            }
        }

        activeDisplays.entrySet().removeIf(entry -> {
            if (desiredKeys.contains(entry.getKey())) {
                return false;
            }
            removeOreVisionDisplay(entry.getValue());
            return true;
        });
    }

    private UUID spawnOreVisionDisplay(Player viewer, Block block) {
        Location location = block.getLocation();
        BlockDisplay display = (BlockDisplay) location.getWorld().spawn(location, BlockDisplay.class, spawned -> {
            spawned.setBlock(block.getBlockData());
            spawned.setGlowing(true);
            spawned.setGlowColorOverride(getOreVisionColor(block.getType()));
            spawned.setGravity(false);
            spawned.setInvulnerable(true);
            spawned.setPersistent(false);
            spawned.setVisibleByDefault(false);
            spawned.setShadowRadius(0.0F);
            spawned.setShadowStrength(0.0F);
            spawned.setDisplayWidth(1.0F);
            spawned.setDisplayHeight(1.0F);
            spawned.setViewRange(0.8F);
        });
        viewer.showEntity(this, display);
        return display.getUniqueId();
    }

    private void clearOreVisionDisplays(UUID playerId) {
        Map<PlacedBlockKey, UUID> displays = oreVisionDisplays.remove(playerId);
        if (displays == null) {
            return;
        }
        for (UUID entityId : displays.values()) {
            removeOreVisionDisplay(entityId);
        }
    }

    private void removeOreVisionDisplay(UUID entityId) {
        if (entityId == null) {
            return;
        }
        Entity entity = getServer().getEntity(entityId);
        if (entity != null) {
            entity.remove();
        }
    }

    private boolean isOreVisionMaterial(Material material) {
        if (material == null) {
            return false;
        }
        return material.name().endsWith("_ORE")
                || material == Material.ANCIENT_DEBRIS;
    }

    private Color getOreVisionColor(Material material) {
        if (material == null) {
            return Color.WHITE;
        }
        String name = material.name();
        if (name.contains("DIAMOND")) {
            return Color.AQUA;
        }
        if (name.contains("EMERALD")) {
            return Color.LIME;
        }
        if (name.contains("GOLD")) {
            return Color.YELLOW;
        }
        if (name.contains("IRON")) {
            return Color.SILVER;
        }
        if (name.contains("COPPER")) {
            return Color.ORANGE;
        }
        if (name.contains("REDSTONE")) {
            return Color.RED;
        }
        if (name.contains("LAPIS")) {
            return Color.BLUE;
        }
        if (name.contains("COAL")) {
            return Color.BLACK;
        }
        if (name.contains("NETHER_QUARTZ") || name.contains("QUARTZ")) {
            return Color.WHITE;
        }
        if (material == Material.ANCIENT_DEBRIS) {
            return Color.fromRGB(110, 72, 58);
        }
        return Color.TEAL;
    }

    public BlockReward getBlockReward(Material material) {
        return blockRewards.computeIfAbsent(material, this::createScaledDefaultReward);
    }

    public void setBlockReward(Material material, int xp, double money) {
        BlockReward reward = new BlockReward(xp, scaleRewardMoney(money));
        blockRewards.put(material, reward);
        blockValuesConfig.set(material.name() + ".xp", xp);
        blockValuesConfig.set(material.name() + ".money", money);
        saveBlockValuesConfig();
    }

    public boolean hasEconomy() {
        return getConfig().getBoolean("economy.enabled", true);
    }

    public double getEconomyRewardScale() {
        return Math.max(0.01D, getConfig().getDouble("economy.reward-scale", 0.20D));
    }

    public double getEconomyPriceScale() {
        return Math.max(0.01D, getConfig().getDouble("economy.price-scale", 0.20D));
    }

    private double scaleRewardMoney(double amount) {
        return roundMoney(Math.max(0.0D, amount) * getEconomyRewardScale());
    }

    private double scalePriceMoney(double amount) {
        return roundMoney(Math.max(0.0D, amount) * getEconomyPriceScale());
    }

    public double getBalance(OfflinePlayer player) {
        return player != null ? getBalance(player.getUniqueId()) : 0.0D;
    }

    public double getBalance(UUID playerId) {
        if (playerId == null) {
            return 0.0D;
        }
        return balances.getOrDefault(playerId, 0.0D);
    }

    public double setBalance(UUID playerId, double amount) {
        if (playerId == null) {
            return 0.0D;
        }
        double sanitized = Math.max(0.0D, roundMoney(amount));
        if (sanitized <= 0.0D) {
            balances.remove(playerId);
            dataConfig.set("money." + playerId, null);
        } else {
            balances.put(playerId, sanitized);
            dataConfig.set("money." + playerId, sanitized);
        }
        saveDataConfig();
        return sanitized;
    }

    public double depositBalance(UUID playerId, double amount) {
        if (playerId == null || amount <= 0.0D || !hasEconomy()) {
            return getBalance(playerId);
        }
        return setBalance(playerId, getBalance(playerId) + amount);
    }

    public double withdrawBalance(UUID playerId, double amount) {
        if (playerId == null || amount <= 0.0D) {
            return getBalance(playerId);
        }
        return setBalance(playerId, Math.max(0.0D, getBalance(playerId) - amount));
    }

    public String formatMoney(double amount) {
        return String.format(Locale.US, "%.2f", roundMoney(amount));
    }

    private double roundMoney(double amount) {
        return Math.round(Math.max(0.0D, amount) * 100.0D) / 100.0D;
    }

    public boolean areBlockRewardsEnabled() {
        return getConfig().getBoolean("rewards.break.enabled", true);
    }

    public void setBlockRewardsEnabled(boolean enabled) {
        setManagedConfigValue("rewards.break.enabled", enabled);
    }

    public boolean areBlockMoneyRewardsEnabled() {
        return getConfig().getBoolean("rewards.break.money-enabled", true);
    }

    public void setBlockMoneyRewardsEnabled(boolean enabled) {
        setManagedConfigValue("rewards.break.money-enabled", enabled);
    }

    public boolean areHostileMobSpawnsEnabled() {
        return getConfig().getBoolean("hostile-mobs.spawning-enabled", true);
    }

    public void setHostileMobSpawnsEnabled(boolean enabled) {
        setManagedConfigValue("hostile-mobs.spawning-enabled", enabled);
        if (!enabled) {
            purgeBlockedMobs();
        }
    }

    public boolean arePhantomsEnabled() {
        return getConfig().getBoolean("phantoms.enabled", false);
    }

    public void setPhantomsEnabled(boolean enabled) {
        setManagedConfigValue("phantoms.enabled", enabled);
        if (!enabled) {
            purgeBlockedMobs();
        }
    }

    public boolean areGroundItemClearEnabled() {
        return getConfig().getBoolean("lag-reduction.ground-item-clear.enabled", true);
    }

    public void setGroundItemClearEnabled(boolean enabled) {
        setManagedConfigValue("lag-reduction.ground-item-clear.enabled", enabled);
        restartLagReductionRuntime();
    }

    public int getGroundItemClearIntervalMinutes() {
        return Math.max(1, getConfig().getInt("lag-reduction.ground-item-clear.interval-minutes", 5));
    }

    public void setGroundItemClearIntervalMinutes(int minutes) {
        setManagedConfigValue("lag-reduction.ground-item-clear.interval-minutes", Math.max(1, minutes));
        restartLagReductionRuntime();
    }

    public List<Integer> getGroundItemClearWarningSeconds() {
        List<Integer> configured = getConfig().getIntegerList("lag-reduction.ground-item-clear.warning-seconds");
        List<Integer> sanitized = new ArrayList<>();
        for (Integer value : configured) {
            if (value != null && value > 0 && !sanitized.contains(value)) {
                sanitized.add(value);
            }
        }
        sanitized.sort(Comparator.reverseOrder());
        return sanitized;
    }

    public boolean isMobStackingEnabled() {
        return getConfig().getBoolean("lag-reduction.mob-stacking.enabled", true);
    }

    public void setMobStackingEnabled(boolean enabled) {
        setManagedConfigValue("lag-reduction.mob-stacking.enabled", enabled);
        restartLagReductionRuntime();
    }

    public double getMobStackingRadius() {
        return Math.max(1.0D, getConfig().getDouble("lag-reduction.mob-stacking.radius-blocks", 8.0D));
    }

    public void setMobStackingRadius(double radius) {
        setManagedConfigValue("lag-reduction.mob-stacking.radius-blocks", Math.max(1.0D, radius));
    }

    public int getMobStackingMaxStackSize() {
        return Math.max(2, getConfig().getInt("lag-reduction.mob-stacking.max-stack-size", 50));
    }

    public void setMobStackingMaxStackSize(int maxStackSize) {
        setManagedConfigValue("lag-reduction.mob-stacking.max-stack-size", Math.max(2, maxStackSize));
    }

    public boolean isItemMergeEnabled() {
        return getConfig().getBoolean("lag-reduction.item-merge.enabled", true);
    }

    public void setItemMergeEnabled(boolean enabled) {
        setManagedConfigValue("lag-reduction.item-merge.enabled", enabled);
    }

    public double getItemMergeRadius() {
        return Math.max(0.5D, getConfig().getDouble("lag-reduction.item-merge.radius-blocks", 2.5D));
    }

    public void setItemMergeRadius(double radius) {
        setManagedConfigValue("lag-reduction.item-merge.radius-blocks", Math.max(0.5D, radius));
    }

    public boolean isJoinLeaveMessagesEnabled() {
        return getConfig().getBoolean("join-leave-messages.enabled", true);
    }

    public void setJoinLeaveMessagesEnabled(boolean enabled) {
        setManagedConfigValue("join-leave-messages.enabled", enabled);
    }

    public boolean isMaintenanceModeEnabled() {
        return maintenanceModeEnabled;
    }

    public void setMaintenanceModeEnabled(boolean enabled) {
        maintenanceModeEnabled = enabled;
        dataConfig.set("maintenance.enabled", enabled);
        saveDataConfig();
    }

    public boolean canBypassMaintenance(OfflinePlayer player) {
        if (player == null) {
            return false;
        }
        if (player.isOp()) {
            return true;
        }
        return maintenanceAllowedPlayers.contains(player.getUniqueId());
    }

    public boolean addMaintenanceAccess(OfflinePlayer player) {
        if (player == null) {
            return false;
        }
        boolean changed = maintenanceAllowedPlayers.add(player.getUniqueId());
        if (changed) {
            saveMaintenanceAccessList();
        }
        return changed;
    }

    public boolean removeMaintenanceAccess(OfflinePlayer player) {
        if (player == null) {
            return false;
        }
        boolean changed = maintenanceAllowedPlayers.remove(player.getUniqueId());
        if (changed) {
            saveMaintenanceAccessList();
        }
        return changed;
    }

    public List<String> getMaintenanceAccessNames() {
        List<String> names = new ArrayList<>();
        for (UUID playerId : maintenanceAllowedPlayers) {
            names.add(safeOfflineName(playerId));
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public boolean hasLuckPerms() {
        return luckPerms != null;
    }

    public Profession getProfession(UUID playerId) {
        Profession developmentProfession = developmentModeProfessions.get(playerId);
        if (developmentProfession != null) {
            return developmentProfession;
        }
        Profession active = activeProfessions.get(playerId);
        if (active != null) {
            return active;
        }
        return playerProfessions.get(playerId);
    }

    public boolean hasProfession(UUID playerId) {
        return playerProfessions.containsKey(playerId);
    }

    public Profession getPrimaryProfession(UUID playerId) {
        return playerProfessions.get(playerId);
    }

    public Profession getSecondaryProfession(UUID playerId) {
        return secondaryProfessions.get(playerId);
    }

    public Profession getStoredActiveProfession(UUID playerId) {
        Profession active = activeProfessions.get(playerId);
        return active != null ? active : playerProfessions.get(playerId);
    }

    public boolean hasProfession(UUID playerId, Profession profession) {
        return profession != null
                && (profession.equals(playerProfessions.get(playerId)) || profession.equals(secondaryProfessions.get(playerId)));
    }

    public boolean meetsProfessionRequirement(UUID playerId, Profession profession) {
        if (profession == null) {
            return true;
        }
        return playtestActive ? hasProfession(playerId, profession) : getProfession(playerId) == profession;
    }

    public boolean prepareProfessionRequirement(UUID playerId, Profession profession) {
        if (profession == null) {
            return true;
        }
        if (playerId == null) {
            return false;
        }
        if (!isProfessionDevelopmentModeEnabled(playerId) && hasProfession(playerId, profession)) {
            noteProfessionAction(playerId, profession);
        }
        return meetsProfessionRequirement(playerId, profession);
    }

    public Profession resolveProfessionForRequirement(UUID playerId, Profession preferredProfession) {
        if (preferredProfession != null && prepareProfessionRequirement(playerId, preferredProfession)) {
            return preferredProfession;
        }
        return getProfession(playerId);
    }

    public List<Profession> getOwnedProfessions(UUID playerId) {
        List<Profession> professions = new ArrayList<>();
        Profession primary = getPrimaryProfession(playerId);
        Profession secondary = getSecondaryProfession(playerId);
        if (primary != null) {
            professions.add(primary);
        }
        if (secondary != null) {
            professions.add(secondary);
        }
        return professions;
    }

    public boolean requiresProfessionSelection(Player player) {
        return !hasProfession(player.getUniqueId());
    }

    public ProfessionSelectionResult selectProfession(UUID playerId, Profession profession) {
        Profession primary = getPrimaryProfession(playerId);
        Profession secondary = getSecondaryProfession(playerId);
        Profession active = getProfession(playerId);

        if (!canUnlockProfession(playerId, profession)) {
            return ProfessionSelectionResult.PROFESSION_LOCKED;
        }

        if (primary == null) {
            if (!canGrantProfession(playerId, profession)) {
                return ProfessionSelectionResult.JOB_FULL;
            }
            playerProfessions.put(playerId, profession);
            activeProfessions.put(playerId, profession);
            saveProfessionState(playerId);
            grantProfessionStarterKit(playerId, profession);
            Player player = getServer().getPlayer(playerId);
            if (player != null) {
                handleTutorialPrimaryProfessionSelected(player);
            }
            return ProfessionSelectionResult.PRIMARY_SELECTED;
        }

        if (profession.equals(active)) {
            return ProfessionSelectionResult.ALREADY_ACTIVE;
        }

        if (profession.equals(primary) || profession.equals(secondary)) {
            activeProfessions.put(playerId, profession);
            saveProfessionState(playerId);
            return ProfessionSelectionResult.ACTIVE_SWITCHED;
        }

        if (secondary != null) {
            return ProfessionSelectionResult.NO_FREE_SLOT;
        }

        if (!canUnlockSecondProfession(playerId)) {
            return ProfessionSelectionResult.SECOND_SLOT_LOCKED;
        }
        if (!canGrantProfession(playerId, profession)) {
            return ProfessionSelectionResult.JOB_FULL;
        }

        secondaryProfessions.put(playerId, profession);
        activeProfessions.put(playerId, profession);
        saveProfessionState(playerId);
        grantProfessionStarterKit(playerId, profession);
        return ProfessionSelectionResult.SECONDARY_SELECTED;
    }

    public boolean switchActiveProfession(UUID playerId, Profession profession) {
        if (!hasProfession(playerId, profession)) {
            return false;
        }
        activeProfessions.put(playerId, profession);
        saveProfessionState(playerId);
        return true;
    }

    public void noteProfessionAction(UUID playerId, Profession profession) {
        if (playerId == null || profession == null) {
            return;
        }
        if (isProfessionDevelopmentModeEnabled(playerId)) {
            return;
        }
        if (!hasProfession(playerId, profession)) {
            return;
        }
        if (profession == getStoredActiveProfession(playerId)) {
            return;
        }
        activeProfessions.put(playerId, profession);
        saveProfessionState(playerId);
    }

    public boolean adminSetPrimaryProfession(UUID playerId, Profession profession) {
        if (!canGrantProfession(playerId, profession)) {
            return false;
        }
        boolean newlyGranted = !hasProfession(playerId, profession);
        playerProfessions.put(playerId, profession);
        if (profession.equals(secondaryProfessions.get(playerId))) {
            secondaryProfessions.remove(playerId);
        }
        if (activeProfessions.get(playerId) == null || !hasProfession(playerId, activeProfessions.get(playerId))) {
            activeProfessions.put(playerId, profession);
        }
        saveProfessionState(playerId);
        if (newlyGranted) {
            grantProfessionStarterKit(playerId, profession);
        }
        return true;
    }

    public boolean adminSetSecondaryProfession(UUID playerId, Profession profession) {
        if (!canGrantProfession(playerId, profession)) {
            return false;
        }
        boolean newlyGranted = !hasProfession(playerId, profession);
        if (profession.equals(playerProfessions.get(playerId))) {
            return true;
        }
        secondaryProfessions.put(playerId, profession);
        if (playerProfessions.get(playerId) == null) {
            playerProfessions.put(playerId, profession);
        }
        if (activeProfessions.get(playerId) == null) {
            activeProfessions.put(playerId, playerProfessions.get(playerId));
        }
        saveProfessionState(playerId);
        if (newlyGranted) {
            grantProfessionStarterKit(playerId, profession);
        }
        return true;
    }

    public boolean canGrantProfession(UUID playerId, Profession profession) {
        if (profession == null || hasProfession(playerId, profession)) {
            return true;
        }
        int cap = getProfessionPlayerCap(profession);
        return cap <= 0 || getProfessionPlayerCount(profession) < cap;
    }

    public boolean canUnlockProfession(UUID playerId, Profession profession) {
        if (profession == null) {
            return false;
        }
        if (profession != Profession.SOLDIER) {
            return true;
        }
        return getQualifiedProfessionCount(playerId, Math.max(1, jobsConfig.getInt("progression.soldier-unlock-level", 10))) >= 2;
    }

    public String getProfessionUnlockRequirementText(UUID playerId, Profession profession) {
        if (profession == null || canUnlockProfession(playerId, profession)) {
            return "";
        }
        if (profession == Profession.SOLDIER) {
            int requiredLevel = Math.max(1, jobsConfig.getInt("progression.soldier-unlock-level", 10));
            return "Reach level " + requiredLevel + " on two jobs";
        }
        return "Locked";
    }

    public int getQualifiedProfessionCount(UUID playerId, int minimumLevel) {
        int count = 0;
        for (Profession profession : getOwnedProfessions(playerId)) {
            if (getProfessionLevel(playerId, profession) >= minimumLevel) {
                count++;
            }
        }
        return count;
    }

    public int getProfessionPlayerCap(Profession profession) {
        return Math.max(0, getProfessionConfig(profession).getInt("player-cap", 10));
    }

    public int getProfessionPlayerCount(Profession profession) {
        if (profession == null) {
            return 0;
        }

        Set<UUID> uniquePlayers = new LinkedHashSet<>();
        uniquePlayers.addAll(playerProfessions.keySet());
        uniquePlayers.addAll(secondaryProfessions.keySet());

        int count = 0;
        for (UUID playerId : uniquePlayers) {
            if (hasProfession(playerId, profession)) {
                count++;
            }
        }
        return count;
    }

    public void setProfessionPlayerCap(Profession profession, int cap) {
        FileConfiguration config = getProfessionConfig(profession);
        config.set("player-cap", Math.max(0, cap));
        saveProfessionConfig(profession);
    }

    public void adminClearSecondaryProfession(UUID playerId) {
        secondaryProfessions.remove(playerId);
        if (!hasProfession(playerId, activeProfessions.get(playerId))) {
            activeProfessions.put(playerId, playerProfessions.get(playerId));
        }
        saveProfessionState(playerId);
    }

    public void adminClearAllProfessions(UUID playerId) {
        playerProfessions.remove(playerId);
        secondaryProfessions.remove(playerId);
        activeProfessions.remove(playerId);
        secondaryProfessionUnlockOverrides.remove(playerId);
        developmentModeProfessions.remove(playerId);
        professionProgress.remove(playerId);
        professionSkillPointBonuses.remove(playerId);
        unlockedProfessionSkillNodes.remove(playerId);
        pendingStarterKitGrants.remove(playerId);
        dataConfig.set("professions." + playerId, null);
        dataConfig.set("profession-progress." + playerId, null);
        dataConfig.set("profession-skill-points." + playerId, null);
        dataConfig.set("profession-skills." + playerId, null);
        dataConfig.set("starter-kits.pending." + playerId, null);
        saveDataConfig();
    }

    public void resetPlayerData(OfflinePlayer target) {
        clearPlayerPluginData(target, true, true);
    }

    private void clearPlayerPluginData(OfflinePlayer target, boolean resetOnlineState, boolean restoreStaffInventory) {
        if (target == null) {
            return;
        }

        UUID playerId = target.getUniqueId();
        Player onlinePlayer = target.getPlayer();
        if (onlinePlayer != null && isInStaffMode(playerId)) {
            if (restoreStaffInventory) {
                disableStaffMode(onlinePlayer);
            } else {
                clearStaffModeForReset(onlinePlayer);
            }
        }

        Country country = getPlayerCountry(playerId);
        if (country != null) {
            removePlayerFromCountry(country, playerId);
        }

        for (Country currentCountry : new ArrayList<>(countriesByKey.values())) {
            if (currentCountry.getInvitedPlayers().remove(playerId)) {
                saveCountry(currentCountry);
            }
        }

        pendingCountryTransfers.entrySet().removeIf(entry ->
                playerId.equals(entry.getKey())
                        || playerId.equals(entry.getValue().currentOwnerId())
                        || playerId.equals(entry.getValue().targetPlayerId()));

        breakCooldowns.remove(playerId);
        placeCooldowns.remove(playerId);
        breakCooldownMessageTimes.remove(playerId);
        placeCooldownMessageTimes.remove(playerId);
        clearLastBlockAction(playerId);
        countryHomeCooldowns.remove(playerId);
        balances.remove(playerId);
        lastTerritoryCountries.remove(playerId);
        pendingFractionalProfessionXp.remove(playerId);
        pendingStarterKitGrants.remove(playerId);
        tutorialStages.remove(playerId);
        traderReputations.remove(playerId);
        traderQuests.entrySet().removeIf(entry -> entry.getKey().startsWith(playerId + ":"));
        traderQuestCooldowns.entrySet().removeIf(entry -> entry.getKey().startsWith(playerId + ":"));
        placedBlocks.entrySet().removeIf(entry -> playerId.equals(entry.getValue()));
        setBlockDelayBypass(target, false);
        clearPlayerFurnaceData(playerId);
        adminClearAllProfessions(playerId);

        if (resetOnlineState && onlinePlayer != null) {
            resetOnlinePlayerState(onlinePlayer);
        }

        dataConfig.set("starter-kits.pending." + playerId, null);
        dataConfig.set("tutorial." + playerId, null);
        dataConfig.set("money." + playerId, null);
        dataConfig.set("traders.reputation." + playerId, null);
        dataConfig.set("traders.quests." + playerId, null);
        dataConfig.set("personal-guide.skill-points." + playerId, null);
        dataConfig.set("personal-guide.playtime-millis." + playerId, null);
        dataConfig.set("personal-guide.skills." + playerId, null);
        dataConfig.set("personal-guide.work-orders." + playerId, null);
        saveDataConfig();
    }

    private void clearPlayerPlaytestData(OfflinePlayer target, boolean clearProgression, boolean clearCountryData) {
        if (target == null) {
            return;
        }

        UUID playerId = target.getUniqueId();
        Player onlinePlayer = target.getPlayer();
        if (onlinePlayer != null && isInStaffMode(playerId)) {
            clearStaffModeForReset(onlinePlayer);
        }

        if (clearCountryData) {
            Country country = getPlayerCountry(playerId);
            if (country != null) {
                removePlayerFromCountry(country, playerId);
            }

            for (Country currentCountry : new ArrayList<>(countriesByKey.values())) {
                if (currentCountry.getInvitedPlayers().remove(playerId)) {
                    saveCountry(currentCountry);
                }
            }

            pendingCountryTransfers.entrySet().removeIf(entry ->
                    playerId.equals(entry.getKey())
                            || playerId.equals(entry.getValue().currentOwnerId())
                            || playerId.equals(entry.getValue().targetPlayerId()));
        }

        breakCooldowns.remove(playerId);
        placeCooldowns.remove(playerId);
        breakCooldownMessageTimes.remove(playerId);
        placeCooldownMessageTimes.remove(playerId);
        clearLastBlockAction(playerId);
        countryHomeCooldowns.remove(playerId);
        lastTerritoryCountries.remove(playerId);
        pendingFractionalProfessionXp.remove(playerId);
        pendingStarterKitGrants.remove(playerId);
        tutorialStages.remove(playerId);
        tutorialIntroIndices.remove(playerId);
        traderQuests.entrySet().removeIf(entry -> entry.getKey().startsWith(playerId + ":"));
        traderQuestCooldowns.entrySet().removeIf(entry -> entry.getKey().startsWith(playerId + ":"));
        placedBlocks.entrySet().removeIf(entry -> playerId.equals(entry.getValue()));
        clearPlayerFurnaceData(playerId);

        dataConfig.set("starter-kits.pending." + playerId, null);
        dataConfig.set("tutorial." + playerId, null);

        if (clearProgression) {
            balances.remove(playerId);
            traderReputations.remove(playerId);
            adminClearAllProfessions(playerId);
            dataConfig.set("money." + playerId, null);
            dataConfig.set("traders.reputation." + playerId, null);
            dataConfig.set("traders.quests." + playerId, null);
            dataConfig.set("personal-guide.skill-points." + playerId, null);
            dataConfig.set("personal-guide.playtime-millis." + playerId, null);
            dataConfig.set("personal-guide.skills." + playerId, null);
            dataConfig.set("personal-guide.work-orders." + playerId, null);
        }

        saveDataConfig();
    }

    public boolean adminSetProfessionLevel(UUID playerId, Profession profession, int level) {
        if (profession == null || !hasProfession(playerId, profession)) {
            return false;
        }

        int maxLevel = getProfessionMaxLevel(playerId, profession);
        ProfessionProgress progress = getStoredProfessionProgress(playerId, profession);
        progress.setLevel(Math.min(maxLevel, Math.max(1, level)));

        if (progress.getLevel() >= maxLevel) {
            progress.setXp(0);
        } else {
            int maxXpForLevel = Math.max(0, getProfessionXpRequiredForLevel(profession, progress.getLevel()) - 1);
            progress.setXp(Math.min(progress.getXp(), maxXpForLevel));
        }

        saveProfessionProgress(playerId, profession, progress);
        return true;
    }

    public boolean adminSetProfessionXp(UUID playerId, Profession profession, int xp) {
        if (profession == null || !hasProfession(playerId, profession)) {
            return false;
        }

        ProfessionProgress progress = getStoredProfessionProgress(playerId, profession);
        int maxLevel = getProfessionMaxLevel(playerId, profession);
        if (progress.getLevel() >= maxLevel) {
            progress.setLevel(maxLevel);
            progress.setXp(0);
            saveProfessionProgress(playerId, profession, progress);
            return true;
        }

        int maxXpForLevel = Math.max(0, getProfessionXpRequiredForLevel(profession, progress.getLevel()) - 1);
        progress.setXp(Math.min(Math.max(0, xp), maxXpForLevel));
        saveProfessionProgress(playerId, profession, progress);
        return true;
    }

    public boolean canUnlockSecondProfession(UUID playerId) {
        Profession primary = getPrimaryProfession(playerId);
        if (primary == null || getSecondaryProfession(playerId) != null) {
            return false;
        }
        if (playtestActive) {
            return true;
        }
        Boolean override = secondaryProfessionUnlockOverrides.get(playerId);
        if (override != null) {
            return override;
        }
        return getProfessionLevel(playerId, primary) >= Math.max(1, jobsConfig.getInt("progression.secondary-unlock-level", 8));
    }

    public boolean isSecondProfessionNaturallyUnlocked(UUID playerId) {
        Profession primary = getPrimaryProfession(playerId);
        if (primary == null || getSecondaryProfession(playerId) != null) {
            return false;
        }
        if (playtestActive) {
            return true;
        }
        return getProfessionLevel(playerId, primary) >= Math.max(1, jobsConfig.getInt("progression.secondary-unlock-level", 8));
    }

    public Boolean getSecondProfessionUnlockOverride(UUID playerId) {
        return secondaryProfessionUnlockOverrides.get(playerId);
    }

    public void cycleSecondProfessionUnlockOverride(UUID playerId) {
        Boolean current = secondaryProfessionUnlockOverrides.get(playerId);
        if (current == null) {
            secondaryProfessionUnlockOverrides.put(playerId, Boolean.TRUE);
        } else if (current) {
            secondaryProfessionUnlockOverrides.put(playerId, Boolean.FALSE);
        } else {
            secondaryProfessionUnlockOverrides.remove(playerId);
        }
        saveProfessionState(playerId);
    }

    public boolean isProfessionDevelopmentModeEnabled(UUID playerId) {
        return developmentModeProfessions.containsKey(playerId);
    }

    public Profession getDevelopmentModeProfession(UUID playerId) {
        return developmentModeProfessions.get(playerId);
    }

    public void toggleProfessionDevelopmentMode(UUID playerId) {
        if (developmentModeProfessions.containsKey(playerId)) {
            developmentModeProfessions.remove(playerId);
        } else {
            Profession active = activeProfessions.get(playerId);
            Profession fallback = active != null ? active : getPrimaryProfession(playerId);
            if (fallback != null) {
                developmentModeProfessions.put(playerId, fallback);
            }
        }
        saveProfessionState(playerId);
    }

    public boolean setProfessionDevelopmentModeJob(UUID playerId, Profession profession) {
        if (profession == null) {
            return false;
        }
        developmentModeProfessions.put(playerId, profession);
        saveProfessionState(playerId);
        return true;
    }

    public void openProfessionAdminMenu(Player admin, OfflinePlayer target) {
        if (professionAdminGuiListener != null) {
            professionAdminGuiListener.openAdminMenu(admin, target);
        }
    }

    public void openQuestAdminMenu(Player player) {
        if (questAdminGuiListener != null) {
            questAdminGuiListener.openQuestListMenu(player, 0);
        }
    }

    public void openJobConfigEditor(Player player) {
        if (jobConfigGuiListener != null) {
            jobConfigGuiListener.openProfessionSelector(player);
        }
    }

    public void openJobConfigEditor(Player player, Profession profession, int level) {
        if (jobConfigGuiListener != null) {
            jobConfigGuiListener.openEditorMenu(player, profession, level);
        }
    }

    public int getProfessionLevel(UUID playerId, Profession profession) {
        return getProfessionProgress(playerId, profession).getLevel();
    }

    public int getProfessionXp(UUID playerId, Profession profession) {
        return getProfessionProgress(playerId, profession).getXp();
    }

    public int getProfessionXpRequired(UUID playerId, Profession profession) {
        if (profession == null) {
            return 0;
        }
        if (getProfessionLevel(playerId, profession) >= getProfessionMaxLevel(playerId, profession)) {
            return 0;
        }
        return getProfessionXpRequiredForLevel(profession, getProfessionLevel(playerId, profession));
    }

    public int getProfessionXpRequiredForLevel(Profession profession, int level) {
        double base = getProfessionProgressConfig(profession).getDouble("progression.base-xp", 100.0D);
        double multiplier = getProfessionProgressConfig(profession).getDouble("progression.xp-multiplier", 1.75D);
        return Math.max(1, (int) Math.round(base * Math.pow(multiplier, Math.max(0, level - 1))));
    }

    public int getProfessionBaseMaxLevel(Profession profession) {
        int configured = getProfessionProgressConfig(profession).getInt("progression.max-level", 15);
        return Math.max(1, configured);
    }

    public int getProfessionMaxLevel(UUID playerId, Profession profession) {
        int baseMaxLevel = getProfessionBaseMaxLevel(profession);
        if (profession != null && profession.equals(getSecondaryProfession(playerId))) {
            return Math.max(1, jobsConfig.getInt("progression.secondary-max-level", 5));
        }
        return baseMaxLevel;
    }

    public double getProfessionLevelUpMoneyReward(Profession profession, int level) {
        ConfigurationSection levelSection = getProfessionLevelSection(profession, level);
        if (levelSection != null && levelSection.contains("money-reward")) {
            return scaleRewardMoney(Math.max(0.0D, levelSection.getDouble("money-reward", 0.0D)));
        }

        FileConfiguration config = getProfessionProgressConfig(profession);
        String professionProgressPath = getProfessionProgressPath(profession);
        double baseReward = config.getDouble(professionProgressPath + ".level-up-money-base",
                config.getDouble("progression.level-up-money-base", 0.0D));
        double multiplier = config.getDouble(professionProgressPath + ".level-up-money-multiplier",
                config.getDouble("progression.level-up-money-multiplier", 1.0D));
        return scaleRewardMoney(Math.max(0.0D, baseReward * Math.pow(multiplier, Math.max(0, level - 2))));
    }

    public double getProfessionLevelUpMoneyRewardTotal(Profession profession, int fromLevelInclusive, int toLevelInclusive) {
        double total = 0.0D;
        for (int level = Math.max(2, fromLevelInclusive); level <= toLevelInclusive; level++) {
            total += getProfessionLevelUpMoneyReward(profession, level);
        }
        return total;
    }

    public List<ProfessionSkillNode> getProfessionSkillNodes(Profession profession) {
        Map<String, ProfessionSkillNode> nodes = professionSkillNodes.get(profession);
        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }
        return nodes.values().stream()
                .sorted(Comparator.comparingInt(ProfessionSkillNode::getSlot))
                .toList();
    }

    public ProfessionSkillNode getProfessionSkillNode(Profession profession, String nodeKey) {
        if (profession == null || nodeKey == null || nodeKey.isBlank()) {
            return null;
        }
        Map<String, ProfessionSkillNode> nodes = professionSkillNodes.get(profession);
        if (nodes == null) {
            return null;
        }
        return nodes.get(nodeKey.trim().toLowerCase(Locale.ROOT));
    }

    public Set<String> getUnlockedProfessionSkillNodeKeys(UUID playerId, Profession profession) {
        if (playerId == null || profession == null) {
            return Set.of();
        }
        Map<Profession, Set<String>> byProfession = unlockedProfessionSkillNodes.get(playerId);
        if (byProfession == null) {
            return Set.of();
        }
        Set<String> unlocked = byProfession.get(profession);
        return unlocked == null ? Set.of() : Set.copyOf(unlocked);
    }

    public boolean hasProfessionSkillNode(UUID playerId, Profession profession, String nodeKey) {
        return getUnlockedProfessionSkillNodeKeys(playerId, profession).contains(nodeKey == null ? "" : nodeKey.trim().toLowerCase(Locale.ROOT));
    }

    public int getAvailableProfessionSkillPoints(UUID playerId, Profession profession) {
        if (playerId == null || profession == null || !hasProfession(playerId, profession)) {
            return 0;
        }
        int earned = Math.max(0, getProfessionLevel(playerId, profession) - 1);
        int bonus = getProfessionSkillPointBonus(playerId, profession);
        int spent = 0;
        for (String nodeKey : getUnlockedProfessionSkillNodeKeys(playerId, profession)) {
            ProfessionSkillNode unlockedNode = getProfessionSkillNode(profession, nodeKey);
            spent += unlockedNode != null ? unlockedNode.getCost() : 1;
        }
        return Math.max(0, earned + bonus - spent);
    }

    public int getProfessionSkillPointBonus(UUID playerId, Profession profession) {
        if (playerId == null || profession == null) {
            return 0;
        }
        Map<Profession, Integer> byProfession = professionSkillPointBonuses.get(playerId);
        if (byProfession == null) {
            return 0;
        }
        return Math.max(0, byProfession.getOrDefault(profession, 0));
    }

    public boolean adminAddProfessionSkillPoints(UUID playerId, Profession profession, int amount) {
        if (playerId == null || profession == null || amount == 0 || !hasProfession(playerId, profession)) {
            return false;
        }
        Map<Profession, Integer> byProfession =
                professionSkillPointBonuses.computeIfAbsent(playerId, ignored -> new EnumMap<>(Profession.class));
        int updated = Math.max(0, byProfession.getOrDefault(profession, 0) + amount);
        if (updated <= 0) {
            byProfession.remove(profession);
            if (byProfession.isEmpty()) {
                professionSkillPointBonuses.remove(playerId);
            }
        } else {
            byProfession.put(profession, updated);
        }
        saveProfessionSkillPointBonuses(playerId, profession);
        return true;
    }

    public boolean canUnlockProfessionSkillNode(UUID playerId, Profession profession, ProfessionSkillNode node) {
        if (playerId == null || profession == null || node == null || !hasProfession(playerId, profession)) {
            return false;
        }
        if (hasProfessionSkillNode(playerId, profession, node.getKey())) {
            return false;
        }
        if (profession.equals(getSecondaryProfession(playerId)) && !node.isSecondaryAllowed()) {
            return false;
        }
        if (getAvailableProfessionSkillPoints(playerId, profession) < node.getCost()) {
            return false;
        }
        for (String requirement : node.getRequirements()) {
            if (!hasProfessionSkillNode(playerId, profession, requirement)) {
                return false;
            }
        }
        return true;
    }

    public boolean unlockProfessionSkillNode(Player player, Profession profession, String nodeKey) {
        if (player == null || profession == null) {
            return false;
        }
        UUID playerId = player.getUniqueId();
        ProfessionSkillNode node = getProfessionSkillNode(profession, nodeKey);
        if (!canUnlockProfessionSkillNode(playerId, profession, node)) {
            return false;
        }
        Map<Profession, Set<String>> byProfession =
                unlockedProfessionSkillNodes.computeIfAbsent(playerId, ignored -> new EnumMap<>(Profession.class));
        Set<String> unlocked = byProfession.computeIfAbsent(profession, ignored -> new LinkedHashSet<>());
        unlocked.add(node.getKey());
        saveProfessionSkillProgress(playerId, profession);
        return true;
    }

    public List<String> getProfessionUnlockDescriptions(Profession profession, int level) {
        List<String> descriptions = new ArrayList<>();
        ConfigurationSection levelSection = getProfessionLevelSection(profession, level);
        if (levelSection == null) {
            return descriptions;
        }

        descriptions.addAll(levelSection.getStringList("unlocks"));
        if (profession == Profession.MINER && level > 1) {
            int reductionSeconds = Math.max(0, getProfessionConfig(Profession.MINER)
                    .getInt("progression.cooldown-reduction-seconds-per-level", 2));
            if (reductionSeconds > 0) {
                descriptions.add("Reduce break cooldown by " + reductionSeconds + " second" + (reductionSeconds == 1 ? "" : "s"));
            }
        }
        return descriptions;
    }

    public List<Material> getProfessionLevelUnlockedBlocks(Profession profession, int level) {
        List<Material> materials = new ArrayList<>();
        ConfigurationSection blockSection = getProfessionBlocksSection(profession, level);
        if (blockSection == null) {
            return materials;
        }

        for (String materialName : blockSection.getKeys(false)) {
            if (!blockSection.getBoolean(materialName + ".enabled", true)) {
                continue;
            }
            Material material = Material.matchMaterial(materialName);
            if (material != null) {
                materials.add(material);
            }
        }
        materials.sort((left, right) -> left.name().compareToIgnoreCase(right.name()));
        return materials;
    }

    public int getProfessionLevelBlockXp(Profession profession, int level, Material material) {
        if (profession == null || material == null) {
            return 0;
        }
        ConfigurationSection blockSection = getProfessionBlocksSection(profession, level);
        if (blockSection == null) {
            return 0;
        }
        return Math.max(0, blockSection.getInt(material.name() + ".xp", 0));
    }

    public int getProfessionConfigIntValue(Profession profession, String path, int defaultValue) {
        if (profession == null || path == null || path.isBlank()) {
            return defaultValue;
        }
        return getProfessionConfig(profession).getInt(path, defaultValue);
    }

    public double getProfessionConfigDoubleValue(Profession profession, String path, double defaultValue) {
        if (profession == null || path == null || path.isBlank()) {
            return defaultValue;
        }
        return getProfessionConfig(profession).getDouble(path, defaultValue);
    }

    public void setProfessionConfigIntValue(Profession profession, String path, int value) {
        if (profession == null || path == null || path.isBlank()) {
            return;
        }
        FileConfiguration config = getProfessionConfig(profession);
        config.set(path, value);
        saveProfessionConfig(profession);
    }

    public void setProfessionConfigDoubleValue(Profession profession, String path, double value) {
        if (profession == null || path == null || path.isBlank()) {
            return;
        }
        FileConfiguration config = getProfessionConfig(profession);
        config.set(path, value);
        saveProfessionConfig(profession);
    }

    public void setProfessionLevelBlockReward(Profession profession, int level, Material material, int xp) {
        if (profession == null || material == null || level < 1) {
            return;
        }
        FileConfiguration config = getProfessionConfig(profession);
        String basePath = "levels." + level + ".blocks." + material.name();
        config.set(basePath + ".enabled", true);
        config.set(basePath + ".xp", Math.max(0, xp));
        List<String> unlocks = new ArrayList<>(config.getStringList("levels." + level + ".unlocks"));
        String displayName = formatMaterialName(material);
        if (!unlocks.contains(displayName)) {
            unlocks.add(displayName);
            config.set("levels." + level + ".unlocks", unlocks);
        }
        saveProfessionConfig(profession);
    }

    public void removeProfessionLevelBlockReward(Profession profession, int level, Material material) {
        if (profession == null || material == null || level < 1) {
            return;
        }
        FileConfiguration config = getProfessionConfig(profession);
        String basePath = "levels." + level + ".blocks." + material.name();
        config.set(basePath, null);
        saveProfessionConfig(profession);
    }

    public int addProfessionXp(UUID playerId, Profession profession, int amount) {
        if (amount <= 0 || profession == null || !hasProfession(playerId, profession)) {
            return 0;
        }

        ProfessionProgress progress = getStoredProfessionProgress(playerId, profession);
        progress.setXp(progress.getXp() + amount);
        int levelUps = 0;
        int maxLevel = getProfessionMaxLevel(playerId, profession);

        while (progress.getLevel() < maxLevel) {
            int requiredXp = getProfessionXpRequiredForLevel(profession, progress.getLevel());
            if (progress.getXp() < requiredXp) {
                break;
            }
            progress.setXp(progress.getXp() - requiredXp);
            progress.setLevel(progress.getLevel() + 1);
            levelUps++;
        }

        if (progress.getLevel() >= maxLevel) {
            progress.setLevel(maxLevel);
            progress.setXp(0);
        }

        saveProfessionProgress(playerId, profession, progress);
        return levelUps;
    }

    public int rewardProfessionXp(Player player, Profession profession, int amount) {
        if (player == null) {
            return 0;
        }
        noteProfessionAction(player.getUniqueId(), profession);
        return rewardProfessionXp(player.getUniqueId(), profession, amount, player);
    }

    public int rewardProfessionXp(UUID playerId, Profession profession, int amount) {
        noteProfessionAction(playerId, profession);
        Player player = getServer().getPlayer(playerId);
        return rewardProfessionXp(playerId, profession, amount, player);
    }

    private int rewardProfessionXp(UUID playerId, Profession profession, int amount, Player onlinePlayer) {
        if (playerId == null || profession == null || amount <= 0) {
            return 0;
        }

        amount = getEffectiveProfessionXpAward(playerId, profession, amount);
        Country country = getPlayerCountry(playerId);
        double xpMultiplier = getCountryPassiveProfessionXpMultiplier(country, profession) * getCountryProfessionXpMultiplier(country, profession);
        amount = (int) Math.max(1, Math.round(amount * xpMultiplier));
        if (amount <= 0) {
            return 0;
        }

        int previousLevel = getProfessionLevel(playerId, profession);
        int levelUps = addProfessionXp(playerId, profession, amount);
        if (onlinePlayer != null) {
            handleTutorialProfessionXpGain(onlinePlayer, profession, amount);
        }
        if (levelUps <= 0) {
            return amount;
        }

        int newLevel = getProfessionLevel(playerId, profession);
        if (onlinePlayer != null) {
            playProfessionLevelUpEffect(onlinePlayer);
            onlinePlayer.sendMessage(getMessage("profession.level-up", placeholders(
                    "profession", getProfessionPlainDisplayName(profession),
                    "level", String.valueOf(newLevel)
            )));
        }
        double rewardMoney = roundMoney(getProfessionLevelUpMoneyRewardTotal(profession, previousLevel + 1, newLevel)
                * getCountryPassiveMoneyMultiplier(country));
        if (rewardMoney > 0.0D && hasEconomy()) {
            depositBalance(playerId, rewardMoney);
            if (onlinePlayer != null) {
                onlinePlayer.sendMessage(getMessage("profession.level-up-money", placeholders(
                        "money", formatMoney(rewardMoney)
                )));
            }
        }
        return amount;
    }

    public int getEffectiveProfessionXpAward(UUID playerId, Profession profession, int amount) {
        if (playerId == null || profession == null || amount <= 0 || !hasProfession(playerId, profession)) {
            return 0;
        }

        int adjustedAmount = applyGlobalXpBoost(amount);
        adjustedAmount = applyPlaytestXpBoost(adjustedAmount);
        if (adjustedAmount <= 0) {
            return 0;
        }

        if (profession.equals(getSecondaryProfession(playerId))) {
            adjustedAmount = Math.max(1, adjustedAmount / 2);
        }

        return adjustedAmount;
    }

    public int rewardFractionalProfessionXp(Player player, Profession profession, double amount) {
        if (player == null || profession == null || amount <= 0.0D) {
            return 0;
        }
        noteProfessionAction(player.getUniqueId(), profession);

        UUID playerId = player.getUniqueId();
        Map<Profession, Double> pendingByProfession = pendingFractionalProfessionXp.computeIfAbsent(
                playerId,
                ignored -> new ConcurrentHashMap<>()
        );
        double totalAmount = pendingByProfession.getOrDefault(profession, 0.0D) + amount;
        int wholeXp = (int) Math.floor(totalAmount);
        double remainder = totalAmount - wholeXp;

        if (remainder > 0.000001D) {
            pendingByProfession.put(profession, remainder);
        } else {
            pendingByProfession.remove(profession);
        }
        if (pendingByProfession.isEmpty()) {
            pendingFractionalProfessionXp.remove(playerId);
        }

        if (wholeXp <= 0) {
            return 0;
        }
        return rewardProfessionXp(player, profession, wholeXp);
    }

    public List<ItemStack> getProfessionStarterKit(Profession profession) {
        List<ItemStack> items = new ArrayList<>();
        if (profession == null) {
            return items;
        }

        for (Map<?, ?> entry : getProfessionConfig(profession).getMapList("starter-kit")) {
            Object materialValue = entry.get("material");
            if (!(materialValue instanceof String materialName)) {
                continue;
            }

            Material material = Material.matchMaterial(materialName);
            if (material == null || material.isAir()) {
                continue;
            }

            int amount = 1;
            Object amountValue = entry.get("amount");
            if (amountValue instanceof Number number) {
                amount = Math.max(1, number.intValue());
            } else if (amountValue instanceof String amountString) {
                try {
                    amount = Math.max(1, Integer.parseInt(amountString));
                } catch (NumberFormatException ignored) {
                    amount = 1;
                }
            }

            while (amount > 0) {
                int stackAmount = Math.min(amount, material.getMaxStackSize());
                items.add(applySoulboundTag(new ItemStack(material, stackAmount)));
                amount -= stackAmount;
            }
        }

        if (profession == Profession.BLACKSMITH && items.stream().noneMatch(item -> item.getType() == Material.ANVIL)) {
            items.add(applySoulboundTag(new ItemStack(Material.ANVIL, 1)));
        }

        return items;
    }

    public List<StarterKitEntry> getProfessionStarterKitEntries(Profession profession) {
        List<StarterKitEntry> entries = new ArrayList<>();
        if (profession == null) {
            return entries;
        }

        for (Map<?, ?> entry : getProfessionConfig(profession).getMapList("starter-kit")) {
            Object materialValue = entry.get("material");
            if (!(materialValue instanceof String materialName)) {
                continue;
            }

            Material material = Material.matchMaterial(materialName);
            if (material == null || material.isAir()) {
                continue;
            }

            int amount = 1;
            Object amountValue = entry.get("amount");
            if (amountValue instanceof Number number) {
                amount = Math.max(1, number.intValue());
            } else if (amountValue instanceof String amountString) {
                try {
                    amount = Math.max(1, Integer.parseInt(amountString));
                } catch (NumberFormatException ignored) {
                    amount = 1;
                }
            }

            entries.add(new StarterKitEntry(material, amount));
        }

        if (profession == Profession.BLACKSMITH && entries.stream().noneMatch(entry -> entry.material() == Material.ANVIL)) {
            entries.add(new StarterKitEntry(Material.ANVIL, 1));
        }
        return entries;
    }

    public void setProfessionStarterKitEntry(Profession profession, Material material, int amount) {
        if (profession == null || material == null || material.isAir()) {
            return;
        }
        List<Map<String, Object>> stored = new ArrayList<>();
        boolean replaced = false;
        for (StarterKitEntry entry : getProfessionStarterKitEntries(profession)) {
            if (entry.material() == material) {
                if (!replaced) {
                    stored.add(createStarterKitMap(material, amount));
                    replaced = true;
                }
                continue;
            }
            stored.add(createStarterKitMap(entry.material(), entry.amount()));
        }
        if (!replaced) {
            stored.add(createStarterKitMap(material, amount));
        }
        getProfessionConfig(profession).set("starter-kit", stored);
        saveProfessionConfig(profession);
    }

    public void removeProfessionStarterKitEntry(Profession profession, Material material) {
        if (profession == null || material == null) {
            return;
        }
        List<Map<String, Object>> stored = new ArrayList<>();
        for (StarterKitEntry entry : getProfessionStarterKitEntries(profession)) {
            if (entry.material() == material) {
                continue;
            }
            stored.add(createStarterKitMap(entry.material(), entry.amount()));
        }
        getProfessionConfig(profession).set("starter-kit", stored);
        saveProfessionConfig(profession);
    }

    private Map<String, Object> createStarterKitMap(Material material, int amount) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("material", material.name());
        map.put("amount", Math.max(1, amount));
        return map;
    }

    public void grantProfessionStarterKit(UUID playerId, Profession profession) {
        if (playerId == null || profession == null) {
            return;
        }

        List<ItemStack> items = getProfessionStarterKit(profession);
        if (items.isEmpty()) {
            return;
        }

        Player player = getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            giveProfessionStarterKit(player, profession, items);
            return;
        }

        queueProfessionStarterKit(playerId, profession);
    }

    public void deliverPendingStarterKits(Player player) {
        if (player == null) {
            return;
        }

        Set<Profession> pending = pendingStarterKitGrants.remove(player.getUniqueId());
        if (pending == null || pending.isEmpty()) {
            return;
        }

        for (Profession profession : pending) {
            List<ItemStack> items = getProfessionStarterKit(profession);
            if (!items.isEmpty()) {
                giveProfessionStarterKit(player, profession, items);
            }
        }

        dataConfig.set("starter-kits.pending." + player.getUniqueId(), null);
        saveDataConfig();
    }

    public double getProfessionDoubleDropChance(Profession profession) {
        if (profession == null) {
            return 0.0D;
        }
        double chance = getProfessionConfig(profession).getDouble("rewards.double-drop-chance", 0.0D);
        return Math.max(0.0D, Math.min(1.0D, chance));
    }

    public double getProfessionDoubleDropChance(UUID playerId, Profession profession) {
        double chance = getProfessionDoubleDropChance(profession);
        if (playerId == null || profession == null) {
            return chance;
        }
        if (profession == Profession.MINER) {
            chance += countUnlockedProfessionSkillNodes(playerId, profession, "double_drop_i") * 0.10D;
            chance += countUnlockedProfessionSkillNodes(playerId, profession, "double_drop_ii") * 0.10D;
        } else if (profession == Profession.LUMBERJACK) {
            chance += countUnlockedProfessionSkillNodes(playerId, profession, "chain_chop") * 0.08D;
        } else if (profession == Profession.FARMER) {
            chance += countUnlockedProfessionSkillNodes(playerId, profession, "double_harvest") * 0.10D;
            chance += countUnlockedProfessionSkillNodes(playerId, profession, "mega_yield") * 0.10D;
        }
        return Math.max(0.0D, Math.min(1.0D, chance));
    }

    public void notifyDoubleDrop(Player player, Profession profession) {
        if (player == null) {
            return;
        }
        String message = getMessage("rewards.double-drop.triggered", placeholders(
                "profession", profession != null ? getProfessionPlainDisplayName(profession) : "Unknown"
        ));
        player.sendActionBar(legacyComponent(message));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.9F, 1.2F);
    }

    private void giveProfessionStarterKit(Player player, Profession profession, List<ItemStack> items) {
        for (ItemStack item : items) {
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(applyUsageRequirementLore(item.clone()));
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }

        player.sendMessage(getMessage("profession.starter-kit-received", placeholders(
                "profession", getProfessionPlainDisplayName(profession)
        )));
    }

    private void queueProfessionStarterKit(UUID playerId, Profession profession) {
        Set<Profession> queued = pendingStarterKitGrants.computeIfAbsent(playerId, ignored -> new LinkedHashSet<>());
        if (!queued.add(profession)) {
            return;
        }

        List<String> stored = new ArrayList<>();
        for (Profession queuedProfession : queued) {
            stored.add(queuedProfession.name());
        }
        dataConfig.set("starter-kits.pending." + playerId, stored);
        saveDataConfig();
    }

    public ItemStack applySoulboundTag(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return itemStack;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }
        meta.getPersistentDataContainer().set(soulboundItemKey, PersistentDataType.BYTE, (byte) 1);
        if (!meta.hasDisplayName()) {
            meta.displayName(legacyComponent("&a" + formatMaterialName(itemStack.getType())));
        }
        List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        boolean hasSoulboundLine = lore.stream().anyMatch(line ->
                PLAIN_TEXT_SERIALIZER.serialize(line).toLowerCase(Locale.ROOT).contains("soulbound"));
        if (!hasSoulboundLine) {
            if (!lore.isEmpty()) {
                lore.add(legacyComponent("&8"));
            }
            lore.add(legacyComponent("&cSoulbound"));
            lore.add(legacyComponent("&7Cannot be dropped."));
        }
        meta.lore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public ItemStack createGuidanceItem() {
        ItemStack itemStack = applySoulboundTag(new ItemStack(Material.NETHER_STAR));
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }

        meta.displayName(legacyComponent("&6Terra Guide"));
        meta.lore(List.of(
                legacyComponent("&7Your main tool for guidance,"),
                legacyComponent("&7management, and progression."),
                legacyComponent("&8"),
                legacyComponent("&ePlaceholder item for now."),
                legacyComponent("&7More functionality comes next.")
        ));
        meta.getPersistentDataContainer().set(guidanceItemKey, PersistentDataType.BYTE, (byte) 1);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public boolean isGuidanceItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return false;
        }
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(guidanceItemKey, PersistentDataType.BYTE);
    }

    public void ensurePlayerGuidanceItem(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack displaced = inventory.getItem(8);
        boolean displacedIsGuide = isGuidanceItem(displaced);

        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            if (isGuidanceItem(contents[slot])) {
                contents[slot] = null;
            }
        }
        inventory.setContents(contents);

        if (!displacedIsGuide && displaced != null && !displaced.getType().isAir()) {
            Map<Integer, ItemStack> leftovers = inventory.addItem(displaced);
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }

        inventory.setItem(8, createGuidanceItem());
        player.updateInventory();
    }

    public boolean isSoulboundItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return false;
        }
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(soulboundItemKey, PersistentDataType.BYTE);
    }

    public boolean isForgeManagedEquipment(Material material) {
        if (material == null) {
            return false;
        }
        String name = material.name();
        return name.endsWith("_SWORD")
                || name.endsWith("_PICKAXE")
                || name.endsWith("_AXE")
                || name.endsWith("_SHOVEL")
                || name.endsWith("_HOE")
                || name.endsWith("_HELMET")
                || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS")
                || name.endsWith("_BOOTS");
    }

    public boolean canUseAdvancedBlacksmithForge(UUID playerId) {
        return playerId != null && hasProfession(playerId, Profession.BLACKSMITH);
    }

    public boolean isPublicBlacksmithRecipe(BlacksmithRecipe recipe) {
        if (recipe == null) {
            return false;
        }
        return switch (recipe.category().toLowerCase(Locale.ROOT)) {
            case "basics", "stone", "iron" -> true;
            default -> false;
        };
    }

    public boolean canCraftBlacksmithRecipe(UUID playerId, BlacksmithRecipe recipe) {
        if (recipe == null) {
            return false;
        }
        if (recipe.level() <= 0 || isPublicBlacksmithRecipe(recipe)) {
            return true;
        }
        if (!canUseAdvancedBlacksmithForge(playerId)) {
            return false;
        }
        return getProfessionLevel(playerId, Profession.BLACKSMITH) >= recipe.level();
    }

    public ItemStack createForgedEquipment(Player player, BlacksmithRecipe recipe) {
        ItemStack itemStack = applyUsageRequirementLore(new ItemStack(recipe.result(), recipe.amount()));
        if (!isForgeManagedEquipment(recipe.result())) {
            return itemStack;
        }

        UUID playerId = player.getUniqueId();
        int blacksmithLevel = hasProfession(playerId, Profession.BLACKSMITH)
                ? getProfessionLevel(playerId, Profession.BLACKSMITH)
                : 0;
        int itemLevel = rollForgedItemLevel(playerId, recipe.level(), blacksmithLevel);
        ForgedRarity rarity = rollForgedRarity(playerId, blacksmithLevel);

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }

        meta.getPersistentDataContainer().set(forgedItemKey, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(forgedLevelKey, PersistentDataType.INTEGER, itemLevel);
        meta.getPersistentDataContainer().set(forgedRarityKey, PersistentDataType.STRING, rarity.name());
        meta.displayName(legacyComponent(rarity.getColor() + rarity.getDisplayName() + " &f"
                + formatMaterialName(recipe.result()) + " &8[" + toRomanNumeral(Math.max(1, Math.min(5, itemLevel))) + "]"));

        List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        if (!lore.isEmpty()) {
            lore.add(legacyComponent("&8"));
        }
        lore.add(legacyComponent("&6Forged Equipment"));
        lore.add(legacyComponent("&7Rarity: " + rarity.getColor() + rarity.getDisplayName()));
        lore.add(legacyComponent("&7Item Level: &f" + toRomanNumeral(Math.max(1, Math.min(5, itemLevel)))));
        lore.add(legacyComponent("&7Forge Tier: &f" + Math.max(1, recipe.level())));
        lore.add(legacyComponent("&7Durability: &f+" + (getForgedDurabilityProtectionPercent(rarity, itemLevel)) + "%"));
        String perkLine = getForgedPerkLore(recipe.result(), rarity);
        if (perkLine != null) {
            lore.add(legacyComponent("&7Perk: &f" + perkLine));
        }
        if (blacksmithLevel > 0) {
            lore.add(legacyComponent("&7Smith Quality: &fBlacksmith Lv." + blacksmithLevel));
        } else {
            lore.add(legacyComponent("&7Smith Quality: &fPublic Forge"));
        }
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private int rollForgedItemLevel(UUID playerId, int recipeLevel, int blacksmithLevel) {
        int baseLevel = 1;
        int cap = blacksmithLevel > 0 ? 3 : 2;
        if (blacksmithLevel > 0) {
            cap += blacksmithLevel >= 8 ? 1 : 0;
            cap += blacksmithLevel >= 12 ? 1 : 0;
            cap += countUnlockedProfessionSkillNodes(playerId, Profession.BLACKSMITH, "reinforced_tools") > 0 ? 1 : 0;
            cap += countUnlockedProfessionSkillNodes(playerId, Profession.BLACKSMITH, "resource_refinement") > 0 ? 1 : 0;
            cap += countUnlockedProfessionSkillNodes(playerId, Profession.BLACKSMITH, "masterwork_items") > 0 ? 1 : 0;
        }
        if (recipeLevel >= 7) {
            baseLevel++;
        }
        if (recipeLevel >= 10) {
            baseLevel++;
        }
        cap = Math.max(baseLevel, Math.min(5, cap));
        int rolled = baseLevel;
        while (rolled < cap) {
            double chance = blacksmithLevel > 0 ? 0.45D : 0.25D;
            if (blacksmithLevel > 0) {
                chance += Math.min(0.25D, blacksmithLevel * 0.015D);
                chance += countUnlockedProfessionSkillNodes(playerId, Profession.BLACKSMITH, "reinforced_tools") * 0.10D;
                chance += countUnlockedProfessionSkillNodes(playerId, Profession.BLACKSMITH, "resource_refinement") * 0.08D;
            }
            if (ThreadLocalRandom.current().nextDouble() > Math.min(0.85D, chance)) {
                break;
            }
            rolled++;
        }
        return rolled;
    }

    private ForgedRarity rollForgedRarity(UUID playerId, int blacksmithLevel) {
        double roll = ThreadLocalRandom.current().nextDouble();
        double commonWeight = 0.72D;
        double uncommonWeight = 0.22D;
        double rareWeight = 0.0D;
        double epicWeight = 0.0D;
        double legendaryWeight = 0.0D;

        if (blacksmithLevel > 0) {
            rareWeight = 0.05D;
            epicWeight = 0.01D;
            commonWeight = Math.max(0.32D, commonWeight - blacksmithLevel * 0.02D);
            uncommonWeight = Math.min(0.34D, uncommonWeight + blacksmithLevel * 0.01D);
            rareWeight = Math.min(0.20D, rareWeight + blacksmithLevel * 0.007D);
            epicWeight = Math.min(0.10D, epicWeight + blacksmithLevel * 0.003D);
            legendaryWeight = Math.min(0.05D, Math.max(0.0D, blacksmithLevel - 9) * 0.004D);
            rareWeight += countUnlockedProfessionSkillNodes(playerId, Profession.BLACKSMITH, "reinforced_tools") * 0.05D;
            epicWeight += countUnlockedProfessionSkillNodes(playerId, Profession.BLACKSMITH, "masterwork_items") * 0.08D;
            legendaryWeight += countUnlockedProfessionSkillNodes(playerId, Profession.BLACKSMITH, "masterwork_items") * 0.03D;
            uncommonWeight += countUnlockedProfessionSkillNodes(playerId, Profession.BLACKSMITH, "resource_refinement") * 0.04D;
            rareWeight += countUnlockedProfessionSkillNodes(playerId, Profession.BLACKSMITH, "resource_refinement") * 0.05D;
        }

        double total = commonWeight + uncommonWeight + rareWeight + epicWeight + legendaryWeight;
        commonWeight /= total;
        uncommonWeight /= total;
        rareWeight /= total;
        epicWeight /= total;
        legendaryWeight /= total;

        if (roll < commonWeight) {
            return ForgedRarity.COMMON;
        }
        roll -= commonWeight;
        if (roll < uncommonWeight) {
            return ForgedRarity.UNCOMMON;
        }
        roll -= uncommonWeight;
        if (roll < rareWeight) {
            return ForgedRarity.RARE;
        }
        roll -= rareWeight;
        if (roll < epicWeight) {
            return ForgedRarity.EPIC;
        }
        return ForgedRarity.LEGENDARY;
    }

    public boolean isForgedItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return false;
        }
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(forgedItemKey, PersistentDataType.BYTE);
    }

    public int getForgedItemLevel(ItemStack itemStack) {
        if (!isForgedItem(itemStack)) {
            return 0;
        }
        ItemMeta meta = itemStack.getItemMeta();
        Integer stored = meta.getPersistentDataContainer().get(forgedLevelKey, PersistentDataType.INTEGER);
        return stored == null ? 0 : Math.max(1, stored);
    }

    public ForgedRarity getForgedItemRarity(ItemStack itemStack) {
        if (!isForgedItem(itemStack)) {
            return null;
        }
        ItemMeta meta = itemStack.getItemMeta();
        String stored = meta.getPersistentDataContainer().get(forgedRarityKey, PersistentDataType.STRING);
        if (stored == null || stored.isBlank()) {
            return null;
        }
        try {
            return ForgedRarity.valueOf(stored);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public int getForgedDisplayLevel(ItemStack itemStack) {
        return Math.max(1, Math.min(5, getForgedItemLevel(itemStack)));
    }

    public String toRomanNumeral(int value) {
        return switch (Math.max(1, Math.min(5, value))) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            default -> "V";
        };
    }

    public int getForgedDurabilityProtectionPercent(ForgedRarity rarity, int itemLevel) {
        if (rarity == null) {
            return 0;
        }
        return Math.min(70, 8 + (itemLevel * 8) + (rarity.ordinal() * 7));
    }

    public String getForgedPerkLore(Material material, ForgedRarity rarity) {
        if (material == null || rarity == null) {
            return null;
        }
        if (rarity == ForgedRarity.LEGENDARY && material.name().endsWith("_PICKAXE")) {
            return "Ore Surge: Fortune III for 30s";
        }
        if (rarity == ForgedRarity.EPIC && material.name().endsWith("_PICKAXE")) {
            return "Deep Cut: stronger ore yields";
        }
        if (rarity == ForgedRarity.RARE && material.name().endsWith("_PICKAXE")) {
            return "Fine Edge: steadier mining";
        }
        return "Forged quality bonus";
    }

    public void activateLegendaryPickaxeBoost(Player player) {
        if (player == null) {
            return;
        }
        legendaryPickaxeBoostUntil.put(player.getUniqueId(), System.currentTimeMillis() + 30_000L);
        player.sendMessage(colorize("&6Ore Surge activated &7for 30 seconds."));
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.9F, 1.2F);
    }

    public boolean hasLegendaryPickaxeBoost(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        long until = legendaryPickaxeBoostUntil.getOrDefault(playerId, 0L);
        if (until <= System.currentTimeMillis()) {
            legendaryPickaxeBoostUntil.remove(playerId);
            return false;
        }
        return true;
    }

    public boolean isOreMaterial(Material material) {
        if (material == null) {
            return false;
        }
        String name = material.name();
        return name.endsWith("_ORE")
                || material == Material.ANCIENT_DEBRIS
                || material == Material.GILDED_BLACKSTONE;
    }

    public NamespacedKey getClimateCropLoreKey() {
        return climateCropLoreKey;
    }

    public void playProfessionLevelUpEffect(Player player) {
        Location location = player.getLocation().add(0.0D, 3.0D, 0.0D);
        World world = player.getWorld();

        world.spawnParticle(Particle.FIREWORK, location, 10, 0.35D, 0.45D, 0.35D, 0.02D);
        world.spawnParticle(Particle.END_ROD, location, 14, 0.4D, 0.6D, 0.4D, 0.01D);

        Firework firework = world.spawn(location, Firework.class, spawned -> {
            FireworkMeta meta = spawned.getFireworkMeta();
            meta.setPower(0);
            meta.clearEffects();
            meta.addEffect(FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL)
                    .withColor(Color.fromRGB(255, 215, 120))
                    .withFade(Color.fromRGB(255, 255, 255))
                    .trail(false)
                    .flicker(false)
                    .build());
            spawned.setFireworkMeta(meta);
            spawned.setSilent(true);
        });

        getServer().getScheduler().runTaskLater(this, firework::detonate, 2L);
    }

    public boolean hasActiveGlobalXpBoost() {
        return globalXpBoostMultiplier > 1.0D && globalXpBoostEndMillis > System.currentTimeMillis();
    }

    public double getGlobalXpBoostMultiplier() {
        return hasActiveGlobalXpBoost() ? globalXpBoostMultiplier : 1.0D;
    }

    public long getGlobalXpBoostRemainingMillis() {
        return Math.max(0L, globalXpBoostEndMillis - System.currentTimeMillis());
    }

    public int applyGlobalXpBoost(int amount) {
        if (amount <= 0) {
            return 0;
        }
        double multiplier = getGlobalXpBoostMultiplier();
        if (multiplier <= 1.0D) {
            return amount;
        }
        return Math.max(1, (int) Math.round(amount * multiplier));
    }

    public int applyPlaytestXpBoost(int amount) {
        if (amount <= 0) {
            return 0;
        }
        if (!playtestActive || playtestXpBoostMultiplier <= 1.0D) {
            return amount;
        }
        return Math.max(1, (int) Math.round(amount * playtestXpBoostMultiplier));
    }

    public void setGlobalXpBoost(double multiplier, long durationMillis, String enabledBy) {
        long duration = Math.max(1000L, durationMillis);
        globalXpBoostMultiplier = multiplier;
        globalXpBoostDurationMillis = duration;
        globalXpBoostEndMillis = System.currentTimeMillis() + duration;
        globalXpBoostEnabledBy = enabledBy;
        saveGlobalXpBoostState();
        startGlobalXpBoostBossBar();
    }

    public void stopGlobalXpBoost() {
        shutdownGlobalXpBoostRuntime();
        globalXpBoostMultiplier = 1.0D;
        globalXpBoostEndMillis = 0L;
        globalXpBoostDurationMillis = 0L;
        globalXpBoostEnabledBy = null;
        saveGlobalXpBoostState();
    }

    private void shutdownGlobalXpBoostRuntime() {
        if (globalXpBoostTask != null) {
            globalXpBoostTask.cancel();
            globalXpBoostTask = null;
        }
        if (globalXpBoostBossBar != null) {
            globalXpBoostBossBar.removeAll();
            globalXpBoostBossBar.setVisible(false);
            globalXpBoostBossBar = null;
        }
    }

    private void ensureCooldownDebugTask() {
        if (cooldownDebugTask != null) {
            return;
        }
        cooldownDebugTask = getServer().getScheduler().runTaskTimer(this, this::updateAllCooldownDebugBars, 0L, 2L);
    }

    private void stopCooldownDebugTaskIfIdle() {
        if (!cooldownDebugPlayers.isEmpty()) {
            return;
        }
        if (cooldownDebugTask != null) {
            cooldownDebugTask.cancel();
            cooldownDebugTask = null;
        }
    }

    private void shutdownCooldownDebugRuntime() {
        if (cooldownDebugTask != null) {
            cooldownDebugTask.cancel();
            cooldownDebugTask = null;
        }
        for (BossBar bossBar : breakCooldownDebugBars.values()) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }
        for (BossBar bossBar : placeCooldownDebugBars.values()) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }
    }

    private void ensurePersistentActionBarTask() {
        if (persistentActionBarTask != null) {
            return;
        }
        persistentActionBarTask = getServer().getScheduler().runTaskTimer(this, this::updatePersistentActionBars, 0L, 2L);
    }

    private void shutdownPersistentActionBarRuntime() {
        if (persistentActionBarTask != null) {
            persistentActionBarTask.cancel();
            persistentActionBarTask = null;
        }
        for (UUID playerId : persistentActionBarPlayers) {
            Player player = getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendActionBar(Component.empty());
            }
        }
        persistentActionBarPlayers.clear();
    }

    private void restartItemsAdderTopStatusHud() {
        if (itemsAdderTopStatusHud == null) {
            itemsAdderTopStatusHud = new ItemsAdderTopStatusHud(this);
        }
        itemsAdderTopStatusHud.restart();
    }

    private void stopItemsAdderTopStatusHud() {
        if (itemsAdderTopStatusHud != null) {
            itemsAdderTopStatusHud.stop();
        }
    }

    private void restartCustomScoreboard() {
        if (customScoreboard == null) {
            customScoreboard = new CustomScoreboard(this);
        }
        customScoreboard.restart();
    }

    private void stopCustomScoreboard() {
        if (customScoreboard != null) {
            customScoreboard.stop();
        }
    }

    private void updatePersistentActionBars() {
        long now = System.currentTimeMillis();
        for (Player player : getServer().getOnlinePlayers()) {
            String actionBarText = getPersistentActionBarText(player, now);
            UUID playerId = player.getUniqueId();
            if (actionBarText == null || actionBarText.isBlank()) {
                if (persistentActionBarPlayers.remove(playerId)) {
                    player.sendActionBar(Component.empty());
                }
                continue;
            }

            persistentActionBarPlayers.add(playerId);
            player.sendActionBar(legacyComponent(actionBarText));
        }
    }

    private String getPersistentActionBarText(Player player, long now) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        List<String> segments = new ArrayList<>();
        String healthSegment = getPlayerHealthActionBarText(player);
        if (healthSegment != null) {
            segments.add(healthSegment);
        }

        if (playtestActive) {
            segments.add(getPlaytestActionBarText(getPlaytestRemainingMillis()));
        }

        String cooldownSegment = getPlayerCooldownActionBarText(player, now);
        if (cooldownSegment != null) {
            segments.add(cooldownSegment);
        }

        if (segments.isEmpty()) {
            return null;
        }
        return String.join(colorize(" &8| "), segments);
    }

    private String getPlayerHealthActionBarText(Player player) {
        if (!getConfig().getBoolean("health-hotbar.enabled", true)) {
            return null;
        }

        double maxHealth = getPlayerMaxHealth(player);
        double armor = getConfig().getBoolean("health-hotbar.include-armor", true) ? getPlayerArmorPoints(player) : 0.0D;
        double current = Math.max(0.0D, Math.min(player.getHealth(), maxHealth)) + armor;
        double max = maxHealth + armor;
        String template = getConfig().getString("health-hotbar.format", "&c\u2764 &f%current%&7/&f%max%");
        if (template == null || template.isBlank()) {
            template = "&c\u2764 &f%current%&7/&f%max%";
        }

        return colorize(template
                .replace("%current%", formatHealthNumber(current))
                .replace("%max%", formatHealthNumber(max))
                .replace("%health%", formatHealthNumber(Math.max(0.0D, Math.min(player.getHealth(), maxHealth))))
                .replace("%armor%", formatHealthNumber(armor)));
    }

    private double getPlayerMaxHealth(Player player) {
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) == null) {
            return 20.0D;
        }
        return Math.max(1.0D, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
    }

    private double getPlayerArmorPoints(Player player) {
        if (player.getAttribute(Attribute.GENERIC_ARMOR) == null) {
            return 0.0D;
        }
        return Math.max(0.0D, player.getAttribute(Attribute.GENERIC_ARMOR).getValue());
    }

    private String formatHealthNumber(double value) {
        double safeValue = Math.max(0.0D, value);
        return safeValue == Math.rint(safeValue)
                ? String.valueOf((int) safeValue)
                : String.format(Locale.US, "%.1f", safeValue);
    }

    private String getPlayerCooldownActionBarText(Player player, long now) {
        if (player == null
                || hasBlockDelayBypass(player.getUniqueId())
                || !isBlockDelayEnabled()) {
            return null;
        }

        UUID playerId = player.getUniqueId();
        long breakRemaining = Math.max(0L, getBreakCooldownEnd(playerId) - now);
        long placeRemaining = Math.max(0L, getPlaceCooldownEnd(playerId) - now);
        if (breakRemaining <= 0L && placeRemaining <= 0L) {
            return null;
        }

        List<String> segments = new ArrayList<>(2);
        if (breakRemaining > 0L) {
            segments.add("&cBreak: &f" + formatCompactCooldown(breakRemaining));
        }
        if (placeRemaining > 0L) {
            segments.add("&aPlace: &f" + formatCompactCooldown(placeRemaining));
        }
        return colorize(String.join(" &8| ", segments));
    }

    private String formatCompactCooldown(long remainingMillis) {
        if (remainingMillis <= 0L) {
            return "0.0s";
        }
        double seconds = Math.ceil(remainingMillis / 100.0D) / 10.0D;
        return String.format(Locale.US, "%.1fs", seconds);
    }

    private void restartClimateRuntime() {
        shutdownClimateRuntime();
        if (!isClimateEnabled()) {
            return;
        }
        ensureClimateBossBarTask();
        ensureClimateFreezeTask();
        ensureClimateDebugParticleTask();
        ensureClimateCropEffectTask();
    }

    private void ensureClimateBossBarTask() {
        if (climateBossBarTask != null || !getConfig().getBoolean("climate.debug-bossbar.enabled", true)) {
            return;
        }
        long intervalTicks = Math.max(1L, getConfig().getLong("climate.debug-bossbar.interval-ticks", 20L));
        climateBossBarTask = getServer().getScheduler().runTaskTimer(this, this::updateClimateBossBars, 0L, intervalTicks);
    }

    private void ensureClimateFreezeTask() {
        if (climateFreezeTask != null || !getConfig().getBoolean("climate.freeze-water.enabled", true)) {
            return;
        }
        long intervalTicks = Math.max(20L, getConfig().getLong("climate.freeze-water.interval-ticks", 100L));
        climateFreezeTask = getServer().getScheduler().runTaskTimer(this, this::tickClimateWater, 20L, intervalTicks);
    }

    private void ensureClimateDebugParticleTask() {
        if (climateDebugParticleTask != null || !getConfig().getBoolean("climate.debug-particles.enabled", true)) {
            return;
        }
        long intervalTicks = Math.max(5L, getConfig().getLong("climate.debug-particles.interval-ticks", 10L));
        climateDebugParticleTask = getServer().getScheduler().runTaskTimer(this, this::tickClimateDebugParticles, intervalTicks, intervalTicks);
    }

    private void ensureClimateCropEffectTask() {
        if (climateCropEffectTask != null || !getConfig().getBoolean("climate.crop-effect.enabled", true)) {
            return;
        }
        long intervalTicks = Math.max(20L, getConfig().getLong("climate.crop-effect.interval-ticks", 1200L));
        climateCropEffectTask = getServer().getScheduler().runTaskTimer(this, this::tickClimateCropEffects, intervalTicks, intervalTicks);
    }

    private void shutdownClimateRuntime() {
        if (climateBossBarTask != null) {
            climateBossBarTask.cancel();
            climateBossBarTask = null;
        }
        if (climateFreezeTask != null) {
            climateFreezeTask.cancel();
            climateFreezeTask = null;
        }
        if (climateDebugParticleTask != null) {
            climateDebugParticleTask.cancel();
            climateDebugParticleTask = null;
        }
        if (climateCropEffectTask != null) {
            climateCropEffectTask.cancel();
            climateCropEffectTask = null;
        }
        for (BossBar bossBar : climateBossBars.values()) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }
        climateBossBars.clear();
    }

    private void updateClimateBossBars() {
        if (!getConfig().getBoolean("climate.debug-bossbar.enabled", true)) {
            for (BossBar bossBar : climateBossBars.values()) {
                bossBar.removeAll();
                bossBar.setVisible(false);
            }
            climateBossBars.clear();
            return;
        }

        Set<UUID> onlineIds = new HashSet<>();
        for (Player player : getServer().getOnlinePlayers()) {
            onlineIds.add(player.getUniqueId());
            BossBar bossBar = climateBossBars.computeIfAbsent(
                    player.getUniqueId(),
                    ignored -> getServer().createBossBar("", BarColor.WHITE, BarStyle.SOLID)
            );
            if (!bossBar.getPlayers().contains(player)) {
                bossBar.removeAll();
                bossBar.addPlayer(player);
            }

            ClimateSnapshot climate = getClimate(player.getLocation());
            bossBar.setColor(getClimateBarColor(climate));
            bossBar.setProgress(getClimateBossBarProgress(climate.temperatureCelsius()));
            bossBar.setTitle(colorize("&eClimate &8| &f" + climate.season().getDisplayName()
                    + " &8| &f" + climate.climateName()
                    + " &8| &f" + formatTemperature(climate.temperatureCelsius())));
            bossBar.setVisible(true);
        }

        climateBossBars.entrySet().removeIf(entry -> {
            if (onlineIds.contains(entry.getKey())) {
                return false;
            }
            entry.getValue().removeAll();
            entry.getValue().setVisible(false);
            return true;
        });
    }

    private double getClimateBossBarProgress(double temperatureCelsius) {
        double normalized = (temperatureCelsius + 25.0D) / 65.0D;
        return Math.max(0.0D, Math.min(1.0D, normalized));
    }

    private BarColor getClimateBarColor(ClimateSnapshot climate) {
        double temperature = climate.temperatureCelsius();
        if (temperature <= 0.0D) {
            return BarColor.PURPLE;
        }
        if (temperature <= 10.0D) {
            return BarColor.BLUE;
        }
        if (temperature <= 28.0D) {
            return BarColor.GREEN;
        }
        if (temperature <= 36.0D) {
            return BarColor.YELLOW;
        }
        return BarColor.RED;
    }

    private void tickClimateWater() {
        int radius = Math.max(1, getConfig().getInt("climate.freeze-water.radius-blocks", 10));
        int verticalRadius = Math.max(0, getConfig().getInt("climate.freeze-water.vertical-radius-blocks", 4));
        for (Player player : getServer().getOnlinePlayers()) {
            Location origin = player.getLocation();
            World world = origin.getWorld();
            if (world == null) {
                continue;
            }

            int baseX = origin.getBlockX();
            int baseY = origin.getBlockY();
            int baseZ = origin.getBlockZ();
            for (int x = baseX - radius; x <= baseX + radius; x++) {
                for (int y = baseY - verticalRadius; y <= baseY + verticalRadius; y++) {
                    for (int z = baseZ - radius; z <= baseZ + radius; z++) {
                        updateClimateWaterBlock(world.getBlockAt(x, y, z));
                    }
                }
            }
        }
    }

    private void tickClimateDebugParticles() {
        if (climateDebugRegions.isEmpty() && climateLiveDisplayPlayers.isEmpty()) {
            return;
        }
        double debugViewRadius = Math.max(8.0D, getConfig().getDouble("climate.debug-particles.view-radius-blocks", 80.0D));
        double liveViewRadius = Math.max(8.0D, getConfig().getDouble("climate.live-display.view-radius-blocks", 48.0D));
        int spacing = Math.max(1, getConfig().getInt("climate.debug-particles.spacing-blocks", 2));
        float size = (float) Math.max(0.6D, Math.min(1.8D, getConfig().getDouble("climate.debug-particles.size", 1.0D)));

        for (Player player : getServer().getOnlinePlayers()) {
            World world = player.getWorld();
            boolean renderedRegion = false;
            List<ClimateDebugRegion> regions = climateDebugRegions.get(world.getUID());
            if (regions == null || regions.isEmpty()) {
                regions = List.of();
            }

            double playerX = player.getLocation().getX();
            double playerZ = player.getLocation().getZ();
            for (ClimateDebugRegion region : regions) {
                if (!isRegionNearPlayer(region, playerX, playerZ, debugViewRadius)) {
                    continue;
                }
                renderClimateDebugParticles(player, region, spacing, size);
                renderedRegion = true;
            }

            if (!renderedRegion && climateLiveDisplayPlayers.contains(player.getUniqueId())) {
                renderLocalClimateParticles(player, liveViewRadius, spacing, size);
            }
        }
    }

    private boolean isRegionNearPlayer(ClimateDebugRegion region, double x, double z, double viewRadius) {
        double nearestX = Math.max(region.minX(), Math.min(x, region.maxX()));
        double nearestZ = Math.max(region.minZ(), Math.min(z, region.maxZ()));
        double dx = x - nearestX;
        double dz = z - nearestZ;
        return (dx * dx) + (dz * dz) <= (viewRadius * viewRadius);
    }

    private void renderClimateDebugParticles(Player player, ClimateDebugRegion region, int spacing, float size) {
        World world = player.getWorld();
        for (int x = region.minX(); x <= region.maxX(); x += spacing) {
            for (int z = region.minZ(); z <= region.maxZ(); z += spacing) {
                Location location = new Location(world, x + 0.5D, region.topY() + 1.15D, z + 0.5D);
                ClimateSnapshot climate = getClimate(location);
                Color color = getClimateDebugColor(climate.temperatureCelsius(), getClimateTimeFactor(world) < 0.0D);
                Particle.DustOptions dust = new Particle.DustOptions(color, size);
                player.spawnParticle(Particle.DUST, location, 1, 0.0D, 0.0D, 0.0D, 0.0D, dust);
            }
        }
    }

    private void renderLocalClimateParticles(Player player, double radius, int spacing, float size) {
        World world = player.getWorld();
        int minX = (int) Math.floor(player.getLocation().getX() - radius);
        int maxX = (int) Math.ceil(player.getLocation().getX() + radius);
        int minZ = (int) Math.floor(player.getLocation().getZ() - radius);
        int maxZ = (int) Math.ceil(player.getLocation().getZ() + radius);

        for (int x = minX; x <= maxX; x += spacing) {
            for (int z = minZ; z <= maxZ; z += spacing) {
                double dx = player.getLocation().getX() - (x + 0.5D);
                double dz = player.getLocation().getZ() - (z + 0.5D);
                if ((dx * dx) + (dz * dz) > (radius * radius)) {
                    continue;
                }

                int highestY = world.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE);
                Location location = new Location(world, x + 0.5D, highestY + 1.35D, z + 0.5D);
                ClimateSnapshot climate = getClimate(location);
                Color color = getClimateDebugColor(climate.temperatureCelsius(), getClimateTimeFactor(world) < 0.0D);
                Particle.DustOptions dust = new Particle.DustOptions(color, size);
                player.spawnParticle(Particle.DUST, location, 1, 0.0D, 0.0D, 0.0D, 0.0D, dust);
            }
        }
    }

    private void tickClimateCropEffects() {
        int radius = Math.max(4, getConfig().getInt("climate.crop-effect.radius-blocks", 16));
        for (Player player : getServer().getOnlinePlayers()) {
            World world = player.getWorld();
            Location origin = player.getLocation();
            int minX = origin.getBlockX() - radius;
            int maxX = origin.getBlockX() + radius;
            int minZ = origin.getBlockZ() - radius;
            int maxZ = origin.getBlockZ() + radius;

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int y = world.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE);
                    showClimateCropEffect(world.getBlockAt(x, y, z));
                    showClimateCropEffect(world.getBlockAt(x, y - 1, z));
                }
            }
        }
    }

    private void showClimateCropEffect(Block block) {
        if (block == null || !shouldClimateManageCrop(block.getType()) || isFullyGrownCrop(block)) {
            return;
        }
        ClimateSnapshot climate = getClimate(block.getLocation());
        double multiplier = getClimateGrowthMultiplier(block.getType(), block.getLocation(), climate);
        if (multiplier < 2.2D) {
            return;
        }
        Location particleLocation = block.getLocation().add(0.5D, 0.85D, 0.5D);
        block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, particleLocation, 7, 0.2D, 0.25D, 0.2D, 0.01D);
    }

    public boolean isClimateLiveDisplayEnabled(UUID playerId) {
        return playerId != null && climateLiveDisplayPlayers.contains(playerId);
    }

    public void setClimateLiveDisplayEnabled(UUID playerId, boolean enabled) {
        if (playerId == null) {
            return;
        }
        if (enabled) {
            climateLiveDisplayPlayers.add(playerId);
            return;
        }
        climateLiveDisplayPlayers.remove(playerId);
    }

    private void updateClimateWaterBlock(Block block) {
        if (block == null) {
            return;
        }

        String key = climateBlockKey(block);
        ClimateSnapshot climate = getClimate(block.getLocation());
        if (block.getType() == Material.WATER && climate.freezing() && canFreezeWater(block)) {
            block.setType(Material.ICE, false);
            climateFrozenWaterBlocks.add(key);
            return;
        }

        if (block.getType() == Material.ICE && climateFrozenWaterBlocks.contains(key) && !climate.freezing()) {
            block.setType(Material.WATER, false);
            climateFrozenWaterBlocks.remove(key);
            return;
        }

        if (block.getType() != Material.ICE) {
            climateFrozenWaterBlocks.remove(key);
        }
    }

    private boolean canFreezeWater(Block block) {
        if (block.getType() != Material.WATER) {
            return false;
        }
        if (!(block.getBlockData() instanceof Levelled levelled) || levelled.getLevel() != 0) {
            return false;
        }
        return block.getRelative(BlockFace.UP).isEmpty();
    }

    private String climateBlockKey(Block block) {
        return block.getWorld().getUID() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }

    public boolean isClimateEnabled() {
        return getConfig().getBoolean("climate.enabled", true);
    }

    public void setClimateEnabled(boolean enabled) {
        setManagedConfigValue("climate.enabled", enabled);
        restartClimateRuntime();
    }

    public boolean areClimateSeasonsEnabled() {
        return getConfig().getBoolean("climate.seasons.enabled", true);
    }

    public void setClimateSeasonsEnabled(boolean enabled) {
        setManagedConfigValue("climate.seasons.enabled", enabled);
    }

    public ClimateSeason getClimateSeasonOverride() {
        String value = getConfig().getString("climate.season-override");
        if (value == null || value.equalsIgnoreCase("auto")) {
            return null;
        }
        try {
            return ClimateSeason.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public void setClimateSeasonOverride(ClimateSeason season) {
        setManagedConfigValue("climate.season-override", season == null ? "auto" : season.name());
    }

    public String getClimateTemperatureUnit() {
        String unit = getConfig().getString("climate.temperature-unit", "C");
        return unit != null && unit.equalsIgnoreCase("F") ? "F" : "C";
    }

    public void setClimateTemperatureUnit(String unit) {
        setManagedConfigValue("climate.temperature-unit", unit != null && unit.equalsIgnoreCase("F") ? "F" : "C");
    }

    public boolean isClimateBossBarEnabled() {
        return getConfig().getBoolean("climate.debug-bossbar.enabled", true);
    }

    public void setClimateBossBarEnabled(boolean enabled) {
        setManagedConfigValue("climate.debug-bossbar.enabled", enabled);
        restartClimateRuntime();
    }

    public boolean isClimateFreezeWaterEnabled() {
        return getConfig().getBoolean("climate.freeze-water.enabled", true);
    }

    public void setClimateFreezeWaterEnabled(boolean enabled) {
        setManagedConfigValue("climate.freeze-water.enabled", enabled);
        restartClimateRuntime();
    }

    public String getClimateEquatorMode() {
        String mode = getConfig().getString("climate.equator.mode", "spawn");
        return mode != null && mode.equalsIgnoreCase("fixed") ? "fixed" : "spawn";
    }

    public void setClimateEquatorModeToSpawn() {
        setManagedConfigValue("climate.equator.mode", "spawn");
    }

    public void setClimateEquatorCoordinate(double coordinate) {
        setManagedConfigValue("climate.equator.mode", "fixed");
        setManagedConfigValue("climate.equator.coordinate", coordinate);
    }

    public boolean isClimatePlaytestModeEnabled() {
        return getConfig().getBoolean("climate.playtest-mode.enabled", false);
    }

    public void setClimatePlaytestModeEnabled(boolean enabled) {
        setManagedConfigValue("climate.playtest-mode.enabled", enabled);
    }

    public double getClimatePlaytestCenterX() {
        return getConfig().getDouble("climate.playtest-mode.center-x", 0.0D);
    }

    public double getClimatePlaytestCenterZ() {
        return getConfig().getDouble("climate.playtest-mode.center-z", 0.0D);
    }

    public void setClimatePlaytestCenter(double x, double z) {
        setManagedConfigValue("climate.playtest-mode.center-x", x);
        setManagedConfigValue("climate.playtest-mode.center-z", z);
    }

    public double getClimatePlaytestRadiusBlocks() {
        return Math.max(1.0D, getConfig().getDouble("climate.playtest-mode.radius-blocks", 750.0D));
    }

    public void setClimatePlaytestRadiusBlocks(double radiusBlocks) {
        setManagedConfigValue("climate.playtest-mode.radius-blocks", Math.max(1.0D, radiusBlocks));
    }

    public double getClimatePlaytestCenterTemperatureCelsius() {
        return getConfig().getDouble("climate.playtest-mode.center-temperature-c", 30.0D);
    }

    public double getClimatePlaytestEdgeTemperatureCelsius() {
        return getConfig().getDouble("climate.playtest-mode.edge-temperature-c", 18.0D);
    }

    public void setClimatePlaytestTemperatures(double centerTemperatureCelsius, double edgeTemperatureCelsius) {
        setManagedConfigValue("climate.playtest-mode.center-temperature-c", centerTemperatureCelsius);
        setManagedConfigValue("climate.playtest-mode.edge-temperature-c", edgeTemperatureCelsius);
    }

    public boolean isClimatePatternEnabled() {
        return getConfig().getBoolean("climate.pattern.enabled", true);
    }

    public void setClimatePatternEnabled(boolean enabled) {
        setManagedConfigValue("climate.pattern.enabled", enabled);
    }

    public double getClimatePatternScaleBlocks() {
        return Math.max(8.0D, getConfig().getDouble("climate.pattern.scale-blocks", 160.0D));
    }

    public void setClimatePatternScaleBlocks(double scaleBlocks) {
        setManagedConfigValue("climate.pattern.scale-blocks", Math.max(8.0D, scaleBlocks));
    }

    public double getClimatePatternStrengthCelsius() {
        return Math.max(0.0D, getConfig().getDouble("climate.pattern.strength-c", 6.0D));
    }

    public void setClimatePatternStrengthCelsius(double strengthCelsius) {
        setManagedConfigValue("climate.pattern.strength-c", Math.max(0.0D, strengthCelsius));
    }

    public long getClimatePatternSeedOffset() {
        return getConfig().getLong("climate.pattern.seed-offset", 0L);
    }

    public long randomizeClimatePatternSeedOffset() {
        long offset = ThreadLocalRandom.current().nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
        setManagedConfigValue("climate.pattern.seed-offset", offset);
        return offset;
    }

    public double getClimateOptimalAltitudeY() {
        return getConfig().getDouble("climate.altitude.optimal-y", 63.0D);
    }

    public void setClimateOptimalAltitudeY(double altitudeY) {
        setManagedConfigValue("climate.altitude.optimal-y", altitudeY);
    }

    public double getHungerSpeedMultiplier() {
        return Math.max(0.0D, getConfig().getDouble("hunger.speed-multiplier", 1.0D));
    }

    public void setHungerSpeedMultiplier(double multiplier) {
        setManagedConfigValue("hunger.speed-multiplier", Math.max(0.0D, multiplier));
    }

    public double getClimateHungerMultiplier(Location location) {
        if (location == null || location.getWorld() == null || !isClimateEnabled()) {
            return 1.0D;
        }
        ClimateSnapshot climate = getClimate(location);
        double temperature = climate.temperatureCelsius();
        double comfortMin = getConfig().getDouble("hunger.climate.comfort-min-c", 12.0D);
        double comfortMax = getConfig().getDouble("hunger.climate.comfort-max-c", 24.0D);
        double mildStep = Math.max(0.1D, getConfig().getDouble("hunger.climate.mild-step-c", 8.0D));
        double extremeStep = Math.max(0.1D, getConfig().getDouble("hunger.climate.extreme-step-c", 18.0D));
        double mildBonus = Math.max(0.0D, getConfig().getDouble("hunger.climate.mild-bonus", 0.18D));
        double extremeBonus = Math.max(0.0D, getConfig().getDouble("hunger.climate.extreme-bonus", 0.42D));
        double maxMultiplier = Math.max(1.0D, getConfig().getDouble("hunger.climate.max-multiplier", 1.85D));

        double deviation = 0.0D;
        if (temperature < comfortMin) {
            deviation = comfortMin - temperature;
        } else if (temperature > comfortMax) {
            deviation = temperature - comfortMax;
        }

        if (deviation <= 0.0D) {
            return 1.0D;
        }

        double mildProgress = Math.min(1.0D, deviation / mildStep);
        double extremeProgress = Math.max(0.0D, (deviation - mildStep) / extremeStep);
        double multiplier = 1.0D + (mildBonus * mildProgress) + (extremeBonus * Math.min(1.0D, extremeProgress));
        return Math.max(1.0D, Math.min(maxMultiplier, multiplier));
    }

    public boolean rollInstantGrowProc(UUID playerId, Profession profession) {
        if (playerId == null || profession == null) {
            return false;
        }
        int level = Math.max(1, getProfessionLevel(playerId, profession));
        double chance = switch (profession) {
            case FARMER -> getScaledProcChance(
                    "profession-procs.farmer.instant-grow.base-chance",
                    "profession-procs.farmer.instant-grow.per-level",
                    "profession-procs.farmer.instant-grow.max-chance",
                    level
            );
            case LUMBERJACK -> getScaledProcChance(
                    "profession-procs.lumberjack.instant-grow.base-chance",
                    "profession-procs.lumberjack.instant-grow.per-level",
                    "profession-procs.lumberjack.instant-grow.max-chance",
                    level
            );
            default -> 0.0D;
        };
        if (profession == Profession.FARMER) {
            chance += countUnlockedProfessionSkillNodes(playerId, profession, "fast_growth") * 0.04D;
            chance += countUnlockedProfessionSkillNodes(playerId, profession, "regrowth_boost") * 0.04D;
            if (isFarmerGrowthBurstActive(playerId)) {
                chance += 0.20D;
            }
        } else if (profession == Profession.LUMBERJACK) {
            chance += countUnlockedProfessionSkillNodes(playerId, profession, "regrowth_boost") * 0.03D;
        }
        return chance > 0.0D && ThreadLocalRandom.current().nextDouble() < chance;
    }

    private double getScaledProcChance(String basePath, String perLevelPath, String maxPath, int level) {
        double baseChance = Math.max(0.0D, getConfig().getDouble(basePath, 0.02D));
        double perLevelChance = Math.max(0.0D, getConfig().getDouble(perLevelPath, 0.005D));
        double maxChance = Math.max(0.0D, getConfig().getDouble(maxPath, 0.20D));
        return Math.min(maxChance, baseChance + (Math.max(0, level - 1) * perLevelChance));
    }

    public void triggerFarmerInstantGrow(Block block) {
        if (block == null || block.getType().isAir()) {
            return;
        }
        getServer().getScheduler().runTask(this, () -> {
            Block liveBlock = block.getLocation().getBlock();
            if (liveBlock.getBlockData() instanceof Ageable ageable) {
                ageable.setAge(ageable.getMaximumAge());
                liveBlock.setBlockData(ageable, false);
                liveBlock.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, liveBlock.getLocation().add(0.5D, 0.8D, 0.5D), 10, 0.25D, 0.2D, 0.25D, 0.0D);
                liveBlock.getWorld().playSound(liveBlock.getLocation(), Sound.ITEM_BONE_MEAL_USE, 0.8F, 1.1F);
                return;
            }
            for (int i = 0; i < 6; i++) {
                liveBlock.applyBoneMeal(BlockFace.UP);
                if (!(liveBlock.getBlockData() instanceof Ageable)) {
                    break;
                }
            }
            liveBlock.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, liveBlock.getLocation().add(0.5D, 0.8D, 0.5D), 10, 0.25D, 0.2D, 0.25D, 0.0D);
            liveBlock.getWorld().playSound(liveBlock.getLocation(), Sound.ITEM_BONE_MEAL_USE, 0.8F, 1.1F);
        });
    }

    public void triggerLumberjackInstantGrow(Block block) {
        if (block == null || block.getType().isAir()) {
            return;
        }
        getServer().getScheduler().runTask(this, () -> {
            Block liveBlock = block.getLocation().getBlock();
            if (!isClimateSapling(liveBlock.getType())) {
                return;
            }
            for (int i = 0; i < 12 && isClimateSapling(liveBlock.getType()); i++) {
                liveBlock.applyBoneMeal(BlockFace.UP);
            }
            liveBlock.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, liveBlock.getLocation().add(0.5D, 0.8D, 0.5D), 14, 0.3D, 0.25D, 0.3D, 0.0D);
            liveBlock.getWorld().playSound(liveBlock.getLocation(), Sound.ITEM_BONE_MEAL_USE, 0.9F, 0.95F);
        });
    }

    public boolean isStabilityEnabled() {
        return getConfig().getBoolean("stability.enabled", true);
    }

    public void setStabilityEnabled(boolean enabled) {
        setManagedConfigValue("stability.enabled", enabled);
        if (!enabled) {
            clearAllPendingStabilityCollapses();
            for (UUID playerId : new ArrayList<>(stabilityDebugDisplays.keySet())) {
                clearStabilityDebugDisplays(playerId);
            }
        }
    }

    public int getStabilityScanRadius() {
        return Math.max(2, getConfig().getInt("stability.scan-radius", 8));
    }

    public void setStabilityScanRadius(int radius) {
        setManagedConfigValue("stability.scan-radius", Math.max(2, radius));
    }

    public int getStabilityWarningDelayTicks() {
        return Math.max(5, (int) getConfig().getLong("stability.warning-delay-ticks", 40L));
    }

    public void setStabilityWarningDelayTicks(int ticks) {
        setManagedConfigValue("stability.warning-delay-ticks", Math.max(5, ticks));
    }

    public int getStabilityLooseMaxSpan() {
        return Math.max(1, getConfig().getInt("stability.loose-max-span", 2));
    }

    public void setStabilityLooseMaxSpan(int span) {
        setManagedConfigValue("stability.loose-max-span", Math.max(1, span));
    }

    public int getStabilityFragileMaxSpan() {
        return Math.max(2, getConfig().getInt("stability.fragile-max-span", 5));
    }

    public void setStabilityFragileMaxSpan(int span) {
        setManagedConfigValue("stability.fragile-max-span", Math.max(2, span));
    }

    public int getStabilityStrictnessPercent() {
        return Math.max(25, Math.min(300, getConfig().getInt("stability.strictness.percent", 100)));
    }

    public void setStabilityStrictnessPercent(int percent) {
        setManagedConfigValue("stability.strictness.percent", Math.max(25, Math.min(300, percent)));
    }

    public double getStabilityNaturalTerrainToleranceMultiplier() {
        return Math.max(1.0D, getConfig().getDouble("stability.mining.natural-terrain-tolerance-multiplier", 1.85D));
    }

    public int getStabilityNaturalMinCollapseBonus() {
        return Math.max(0, getConfig().getInt("stability.mining.natural-min-collapse-size-bonus", 2));
    }

    public int getStabilityNaturalRoofExtraSpan() {
        return Math.max(0, getConfig().getInt("stability.mining.natural-roof-extra-span", 2));
    }

    public int getStabilityNaturalShaftExtraDepth() {
        return Math.max(0, getConfig().getInt("stability.mining.natural-shaft-extra-depth", 1));
    }

    public int getStabilityNaturalTunnelMinLength() {
        return Math.max(3, getConfig().getInt("stability.mining.natural-tunnel-min-length", 6));
    }

    public int getStabilityNaturalTunnelMinWidth() {
        return Math.max(2, getConfig().getInt("stability.mining.natural-tunnel-min-width", 3));
    }

    public int getStabilityNaturalTunnelMinHeight() {
        return Math.max(2, getConfig().getInt("stability.mining.natural-tunnel-min-height", 3));
    }

    public int getStabilityNaturalRoomMinWidth() {
        return Math.max(3, getConfig().getInt("stability.mining.natural-room-min-width", 5));
    }

    public int getStabilityNaturalRoomMinLength() {
        return Math.max(3, getConfig().getInt("stability.mining.natural-room-min-length", 5));
    }

    public int getStabilityNaturalRoomMinHeight() {
        return Math.max(3, getConfig().getInt("stability.mining.natural-room-min-height", 5));
    }

    public int getStabilitySupportHorizontalRadius() {
        return Math.max(1, getConfig().getInt("stability.support-radius-horizontal", 2));
    }

    public void setStabilitySupportHorizontalRadius(int radius) {
        setManagedConfigValue("stability.support-radius-horizontal", Math.max(1, radius));
    }

    public int getStabilitySupportVerticalRadius() {
        return Math.max(1, getConfig().getInt("stability.support-radius-vertical", 2));
    }

    public void setStabilitySupportVerticalRadius(int radius) {
        setManagedConfigValue("stability.support-radius-vertical", Math.max(1, radius));
    }

    public boolean toggleStabilityDebug(Player player) {
        if (player == null) {
            return false;
        }
        UUID playerId = player.getUniqueId();
        if (!stabilityDebugPlayers.add(playerId)) {
            stabilityDebugPlayers.remove(playerId);
            if (!hasTemporaryStabilityDebug(playerId)) {
                clearStabilityDebugDisplays(playerId);
            } else {
                refreshStabilityDebugDisplays(player);
            }
            return false;
        }
        refreshStabilityDebugDisplays(player);
        return true;
    }

    public boolean isStabilityDebugEnabled(Player player) {
        return player != null && (stabilityDebugPlayers.contains(player.getUniqueId()) || hasTemporaryStabilityDebug(player.getUniqueId()));
    }

    public boolean isStabilityMeterChatEnabled(UUID playerId) {
        return playerId != null && stabilityMeterChatEnabledPlayers.contains(playerId);
    }

    public void setStabilityMeterChatEnabled(UUID playerId, boolean enabled) {
        if (playerId == null) {
            return;
        }
        if (enabled) {
            stabilityMeterChatEnabledPlayers.add(playerId);
            dataConfig.set("stability-meter.chat-visible." + playerId, true);
        } else {
            stabilityMeterChatEnabledPlayers.remove(playerId);
            dataConfig.set("stability-meter.chat-visible." + playerId, null);
        }
        saveDataConfig();
    }

    public int getStabilityMeterProgress(UUID playerId) {
        if (playerId == null) {
            return 0;
        }
        int requiredActions = getStabilityMeterRequiredActions();
        int progress = stabilityMeterProgress.getOrDefault(playerId, 0);
        return Math.max(0, Math.min(requiredActions, progress));
    }

    public int getStabilityMeterPercent(UUID playerId) {
        int requiredActions = getStabilityMeterRequiredActions();
        if (requiredActions <= 0) {
            return 0;
        }
        return (int) Math.round((getStabilityMeterProgress(playerId) * 100.0D) / requiredActions);
    }

    public void awardStabilityMeterProgress(Player player, Block block, boolean breakAction) {
        if (player == null || block == null || !isStructuralMeterActionBlock(block)) {
            return;
        }
        UUID playerId = player.getUniqueId();
        int requiredActions = getStabilityMeterRequiredActions();
        int progress = stabilityMeterProgress.merge(playerId, 1, Integer::sum);
        boolean charged = false;
        while (progress >= requiredActions) {
            progress -= requiredActions;
            charged = true;
        }
        stabilityMeterProgress.put(playerId, progress);
        if (charged) {
            grantTemporaryStabilityDebugVision(player);
        }
        if (isStabilityMeterChatEnabled(playerId)) {
            sendStabilityMeterStatus(player, progress, requiredActions, charged, breakAction);
        }
    }

    public void queueStabilityScan(Location origin) {
        if (!isStabilityEnabled() || origin == null || origin.getWorld() == null) {
            return;
        }
        queueSingleStabilityScan(origin);
    }

    public void queueStabilityScansAround(Location origin, int horizontalRadius, int verticalRadius) {
        if (!isStabilityEnabled() || origin == null || origin.getWorld() == null) {
            return;
        }
        World world = origin.getWorld();
        int x = origin.getBlockX();
        int y = origin.getBlockY();
        int z = origin.getBlockZ();
        Set<String> queued = new LinkedHashSet<>();
        queueSingleStabilityScan(world, x, y, z, queued);

        int horizontal = Math.max(0, horizontalRadius);
        int vertical = Math.max(0, verticalRadius);
        if (horizontal > 0) {
            queueSingleStabilityScan(world, x + horizontal, y, z, queued);
            queueSingleStabilityScan(world, x - horizontal, y, z, queued);
            queueSingleStabilityScan(world, x, y, z + horizontal, queued);
            queueSingleStabilityScan(world, x, y, z - horizontal, queued);
        }
        if (vertical > 0) {
            queueSingleStabilityScan(world, x, y + vertical, z, queued);
            queueSingleStabilityScan(world, x, y - vertical, z, queued);
        }
        if (horizontal > 1) {
            queueSingleStabilityScan(world, x + horizontal, y, z + horizontal, queued);
            queueSingleStabilityScan(world, x + horizontal, y, z - horizontal, queued);
            queueSingleStabilityScan(world, x - horizontal, y, z + horizontal, queued);
            queueSingleStabilityScan(world, x - horizontal, y, z - horizontal, queued);
        }
    }

    private void queueSingleStabilityScan(Location origin) {
        queueSingleStabilityScan(origin.getWorld(), origin.getBlockX(), origin.getBlockY(), origin.getBlockZ(), new LinkedHashSet<>());
    }

    private void queueSingleStabilityScan(World world, int blockX, int blockY, int blockZ, Set<String> localKeys) {
        if (world == null || !world.isChunkLoaded(blockX >> 4, blockZ >> 4)) {
            return;
        }
        String key = world.getUID() + ":" + blockX + ":" + blockY + ":" + blockZ;
        if (!localKeys.add(key) || !queuedStabilityScanOrigins.add(key)) {
            return;
        }
        Location scanOrigin = new Location(world, blockX + 0.5D, blockY + 0.5D, blockZ + 0.5D);
        getServer().getScheduler().runTask(this, () -> {
            try {
                scanStabilityAt(scanOrigin);
            } finally {
                queuedStabilityScanOrigins.remove(key);
            }
        });
    }

    public boolean scanStabilityAt(Location origin) {
        if (!isStabilityEnabled() || origin == null || origin.getWorld() == null) {
            return false;
        }
        if (pendingStabilityCollapses.size() >= getMaxPendingStabilityCollapses()) {
            return false;
        }

        List<StabilityClusterCandidate> candidates = new ArrayList<>();
        Set<PlacedBlockKey> visited = new HashSet<>();
        List<Block> seeds = collectStabilityScanSeeds(origin);

        for (Block block : seeds) {
            PlacedBlockKey key = PlacedBlockKey.from(block);
            if (visited.contains(key)) {
                continue;
            }
            StabilityMaterialClass materialClass = getStabilityMaterialClass(block.getType());
            if (materialClass == StabilityMaterialClass.STABLE) {
                visited.add(key);
                continue;
            }
            List<Block> cluster = collectUnstableCluster(block, materialClass, visited, getMaxStabilityClusterSize());
            if (cluster.size() >= getEffectiveMinCollapseSize(cluster, materialClass)) {
                candidates.add(new StabilityClusterCandidate(cluster, materialClass));
            }
        }

        if (candidates.isEmpty()) {
            return false;
        }

        candidates.sort((left, right) -> Integer.compare(right.blocks.size(), left.blocks.size()));
        int scheduled = 0;
        for (StabilityClusterCandidate candidate : candidates) {
            if (pendingStabilityCollapses.size() >= getMaxPendingStabilityCollapses()) {
                break;
            }
            if (scheduleStabilityCollapse(candidate.blocks, candidate.materialClass)) {
                scheduled++;
            }
            if (scheduled >= getStabilityMaxClustersPerScan()) {
                break;
            }
        }
        return scheduled > 0;
    }

    private List<Block> collectStabilityScanSeeds(Location origin) {
        World world = origin.getWorld();
        if (world == null) {
            return List.of();
        }
        int seedHorizontalRadius = Math.min(2, Math.max(1, getStabilityScanRadius() / 4));
        int seedVerticalRadius = Math.min(3, Math.max(2, getStabilityScanRadius() / 3));
        Map<PlacedBlockKey, Block> seeds = new LinkedHashMap<>();
        int centerX = origin.getBlockX();
        int centerY = origin.getBlockY();
        int centerZ = origin.getBlockZ();

        for (int x = centerX - seedHorizontalRadius; x <= centerX + seedHorizontalRadius; x++) {
            for (int z = centerZ - seedHorizontalRadius; z <= centerZ + seedHorizontalRadius; z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                for (int y = Math.max(world.getMinHeight(), centerY - seedVerticalRadius);
                     y <= Math.min(world.getMaxHeight() - 1, centerY + seedVerticalRadius);
                     y++) {
                    addStabilitySeed(seeds, world.getBlockAt(x, y, z));
                }
            }
        }

        Block originBlock = origin.getBlock();
        addStabilitySeed(seeds, originBlock);
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN}) {
            Block relative = originBlock.getRelative(face);
            addStabilitySeed(seeds, relative);
            addStabilitySeed(seeds, relative.getRelative(face));
        }
        return new ArrayList<>(seeds.values());
    }

    private void addStabilitySeed(Map<PlacedBlockKey, Block> seeds, Block block) {
        if (block == null || block.isEmpty()) {
            return;
        }
        StabilityMaterialClass materialClass = getStabilityMaterialClass(block.getType());
        if (materialClass == StabilityMaterialClass.STABLE || materialClass == StabilityMaterialClass.HARD_ROCK) {
            return;
        }
        seeds.putIfAbsent(PlacedBlockKey.from(block), block);
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN}) {
            Block relative = block.getRelative(face);
            StabilityMaterialClass relativeClass = getStabilityMaterialClass(relative.getType());
            if (relativeClass != StabilityMaterialClass.STABLE && relativeClass != StabilityMaterialClass.HARD_ROCK) {
                seeds.putIfAbsent(PlacedBlockKey.from(relative), relative);
            }
        }
    }

    public int getPendingStabilityCollapseCount() {
        return pendingStabilityCollapses.size();
    }

    private int getMaxPendingStabilityCollapses() {
        return Math.max(1, getConfig().getInt("stability.max-pending-collapses", 24));
    }

    private int getMaxStabilityClusterSize() {
        return Math.max(16, getConfig().getInt("stability.max-cluster-size", 192));
    }

    private int getStabilityCollapseBatchSize() {
        return Math.max(1, getConfig().getInt("stability.collapse-batch-size", 18));
    }

    private long getStabilityBatchIntervalTicks() {
        return Math.max(1L, getConfig().getLong("stability.batch-interval-ticks", 2L));
    }

    private int getMaxFallingBlocksPerCollapse() {
        return Math.max(0, getConfig().getInt("stability.max-falling-blocks-per-collapse", 24));
    }

    private int getStabilityMaxClustersPerScan() {
        return Math.max(1, getConfig().getInt("stability.max-clusters-per-scan", 3));
    }

    private double getStabilityWarningRadiusBlocks() {
        return Math.max(4.0D, getConfig().getDouble("stability.player-warning-radius-blocks", 16.0D));
    }

    private boolean isStabilityDebugRuntimeEnabled() {
        return getConfig().getBoolean("stability.debug.enabled", true);
    }

    private long getStabilityDebugIntervalTicks() {
        return Math.max(5L, getConfig().getLong("stability.debug.interval-ticks", 20L));
    }

    public double getStabilityDebugViewRadiusBlocks() {
        return Math.max(2.0D, getConfig().getDouble("stability.debug.view-radius-blocks", 6.0D));
    }

    public void setStabilityDebugViewRadiusBlocks(double radius) {
        setManagedConfigValue("stability.debug.view-radius-blocks", Math.max(2.0D, radius));
    }

    public List<Material> getConfiguredStructuralSupportMaterials() {
        List<Material> materials = new ArrayList<>(configuredStructuralSupportMaterials);
        materials.sort(Comparator.comparing(Material::name));
        return materials;
    }

    public boolean addStructuralSupportMaterial(Material material) {
        if (material == null || material.isAir() || !material.isBlock()) {
            return false;
        }
        boolean changed = configuredStructuralSupportMaterials.add(material);
        if (changed) {
            saveStructuralSupportMaterials();
        }
        return changed;
    }

    public boolean removeStructuralSupportMaterial(Material material) {
        if (material == null) {
            return false;
        }
        boolean changed = configuredStructuralSupportMaterials.remove(material);
        if (changed) {
            saveStructuralSupportMaterials();
        }
        return changed;
    }

    public void openStabilitySupportGui(Player player) {
        if (player != null && stabilityGuiListener != null) {
            stabilityGuiListener.openSupportMaterialsMenu(player, 0);
        }
    }

    private int getStabilityMinOpenFaces() {
        return Math.max(1, getConfig().getInt("stability.min-open-faces", 2));
    }

    private double getStabilitySidewaysFallSpeed() {
        return Math.max(0.0D, getConfig().getDouble("stability.fall-sideways-speed", 0.16D));
    }

    private int getStabilityLooseGroundedStackHeight() {
        return Math.max(1, getConfig().getInt("stability.loose-grounded-stack-height", 2));
    }

    private int getStabilityShaftOpenDepth() {
        return Math.max(2, getConfig().getInt("stability.shaft-open-depth", 3));
    }

    private int getStabilityNaturalShaftOpenDepth() {
        return getStabilityShaftOpenDepth() + getStabilityNaturalShaftExtraDepth();
    }

    private boolean isNaturalTunnelMaterial(StabilityMaterialClass materialClass) {
        return materialClass == StabilityMaterialClass.SOFT_ROCK
                || materialClass == StabilityMaterialClass.FRAGILE
                || materialClass == StabilityMaterialClass.HARD_ROCK;
    }

    private int getStabilitySupportFrameHorizontalSpan() {
        return Math.max(2, getConfig().getInt("stability.support-frame.horizontal-span", 3));
    }

    private int getStabilitySupportFrameVerticalHeight() {
        return Math.max(2, getConfig().getInt("stability.support-frame.vertical-height", 3));
    }

    private double getStabilityRainStress() {
        return Math.max(0.0D, getConfig().getDouble("stability.wetness.rain-stress", 0.7D));
    }

    private double getStabilityNearWaterStress() {
        return Math.max(0.0D, getConfig().getDouble("stability.wetness.near-water-stress", 0.8D));
    }

    private int getStabilityWaterRadius() {
        return Math.max(1, getConfig().getInt("stability.wetness.water-radius", 2));
    }

    private int getStabilityWeightTraceHeight() {
        return Math.max(4, getConfig().getInt("stability.weight-trace-height", 16));
    }

    private double getStabilityLoadStressMultiplier() {
        return Math.max(0.05D, getConfig().getDouble("stability.load-stress-multiplier", 0.42D));
    }

    private double getStabilityAdjacentSupportFactor() {
        return Math.max(0.0D, getConfig().getDouble("stability.adjacent-support-factor", 0.35D));
    }

    private double getStabilityFoundationSupportFactor() {
        return Math.max(0.0D, getConfig().getDouble("stability.foundation-support-factor", 1.15D));
    }

    private double getStabilitySupportFrameBonus() {
        return Math.max(0.0D, getConfig().getDouble("stability.support-frame-bonus", 2.8D));
    }

    private double getStabilityAnchorSupportBonus() {
        return Math.max(0.0D, getConfig().getDouble("stability.anchor-support-bonus", 1.5D));
    }

    private double getStabilityRoofLoadFactor() {
        return Math.max(0.0D, getConfig().getDouble("stability.roof-load-factor", 0.3D));
    }

    private double getStabilityColumnWeightFactor() {
        return Math.max(0.0D, getConfig().getDouble("stability.column-weight-factor", 0.24D));
    }

    private boolean isStabilityRubbleEnabled() {
        return getConfig().getBoolean("stability.rubble.enabled", true);
    }

    private double getStabilityRubbleChance() {
        return Math.max(0.0D, Math.min(1.0D, getConfig().getDouble("stability.rubble.chance", 0.22D)));
    }

    private int getStabilityMaxRubblePerCollapse() {
        return Math.max(0, getConfig().getInt("stability.rubble.max-per-collapse", 12));
    }

    private int getMinCollapseSize(StabilityMaterialClass materialClass) {
        return switch (materialClass) {
            case LOOSE -> Math.max(1, getConfig().getInt("stability.loose-min-collapse-size", 2));
            case PACKED_SOIL -> Math.max(2, getConfig().getInt("stability.packed-min-collapse-size", 3));
            case SOFT_ROCK -> Math.max(3, getConfig().getInt("stability.soft-rock-min-collapse-size", 4));
            case FRAGILE -> Math.max(2, getConfig().getInt("stability.fragile-min-collapse-size", 4));
            case HARD_ROCK, STABLE -> Integer.MAX_VALUE;
        };
    }

    private int getEffectiveMinCollapseSize(List<Block> cluster, StabilityMaterialClass materialClass) {
        if (cluster == null || cluster.isEmpty()) {
            return getMinCollapseSize(materialClass);
        }
        boolean allNaturalTerrain = cluster.stream().noneMatch(this::isPlayerBuiltStructureBlock);
        if ((materialClass == StabilityMaterialClass.LOOSE || materialClass == StabilityMaterialClass.PACKED_SOIL)
                && cluster.stream().anyMatch(this::isFreestandingSoilBlock)) {
            return 1;
        }
        if (cluster.stream().anyMatch(block -> getStabilityFailureMode(block, materialClass) == StabilityFailureMode.FLOATING)) {
            return 1;
        }
        if (cluster.stream().anyMatch(block -> isUnsupportedFreestandingWall(block, materialClass))) {
            return Math.max(2, getMinCollapseSize(materialClass) - 1);
        }
        if (allNaturalTerrain && cluster.stream().anyMatch(block -> isNaturalUnsupportedTunnelBlock(block, materialClass))) {
            return getMinCollapseSize(materialClass);
        }
        return allNaturalTerrain
                ? getMinCollapseSize(materialClass) + getStabilityNaturalMinCollapseBonus()
                : getMinCollapseSize(materialClass);
    }

    private List<Block> blockKeysToBlocks(World world, List<PlacedBlockKey> keys) {
        if (world == null || keys == null || keys.isEmpty()) {
            return List.of();
        }
        List<Block> blocks = new ArrayList<>(keys.size());
        for (PlacedBlockKey key : keys) {
            blocks.add(world.getBlockAt(key.x, key.y, key.z));
        }
        return blocks;
    }

    private double getStabilityStressThreshold(StabilityMaterialClass materialClass) {
        return switch (materialClass) {
            case LOOSE -> 2.4D;
            case PACKED_SOIL -> 3.0D;
            case SOFT_ROCK -> 3.7D;
            case FRAGILE -> 4.3D;
            case HARD_ROCK, STABLE -> Double.MAX_VALUE;
        };
    }

    private List<Block> collectUnstableCluster(Block start, StabilityMaterialClass materialClass, Set<PlacedBlockKey> visited, int maxClusterSize) {
        if (!shouldCollapseBlock(start, materialClass)) {
            visited.add(PlacedBlockKey.from(start));
            return List.of();
        }

        List<Block> cluster = new ArrayList<>();
        ArrayList<Block> queue = new ArrayList<>();
        queue.add(start);
        int index = 0;
        while (index < queue.size() && cluster.size() < maxClusterSize) {
            Block block = queue.get(index++);
            PlacedBlockKey key = PlacedBlockKey.from(block);
            if (!visited.add(key)) {
                continue;
            }
            if (pendingStabilityBlockKeys.contains(key)) {
                continue;
            }
            if (!shouldCollapseBlock(block, materialClass)) {
                continue;
            }
            cluster.add(block);
            for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN}) {
                Block relative = block.getRelative(face);
                if (getStabilityMaterialClass(relative.getType()) == materialClass && !visited.contains(PlacedBlockKey.from(relative))) {
                    queue.add(relative);
                }
            }
        }
        return cluster;
    }

    private StabilityFailureMode getStabilityFailureMode(Block block, StabilityMaterialClass expectedClass) {
        if (block == null || block.isEmpty()) {
            return null;
        }
        StabilityMaterialClass materialClass = getStabilityMaterialClass(block.getType());
        if (materialClass != expectedClass || materialClass == StabilityMaterialClass.STABLE || materialClass == StabilityMaterialClass.HARD_ROCK) {
            return null;
        }
        boolean playerBuiltStructure = isPlayerBuiltStructureBlock(block);
        if (hasNearbyStructuralSupport(block)) {
            return null;
        }
        if (hasSupportFrame(block)) {
            return null;
        }
        if (hasTimberedMineSupport(block)) {
            return null;
        }
        boolean shaftExposed = playerBuiltStructure
                ? isExposedShaftWallBlock(block)
                : isNaturalShaftCollapseCandidate(block);
        boolean roofExposed = playerBuiltStructure
                ? isExposedRoofBlock(block)
                : isNaturalRoofCollapseCandidate(block, materialClass);
        if (!playerBuiltStructure && !isTerrainCollapseCandidate(block, materialClass)) {
            return null;
        }
        if ((materialClass == StabilityMaterialClass.LOOSE || materialClass == StabilityMaterialClass.PACKED_SOIL)
                && !shaftExposed
                && !roofExposed
                && isFoundationSupportedLooseWall(block)) {
            return null;
        }
        if ((materialClass == StabilityMaterialClass.LOOSE || materialClass == StabilityMaterialClass.PACKED_SOIL)
                && isFreestandingSoilBlock(block)) {
            return StabilityFailureMode.FLOATING;
        }
        if (!playerBuiltStructure
                && isNaturalTunnelMaterial(materialClass)
                && isNaturalUnsupportedRoomRoof(block, materialClass)) {
            return StabilityFailureMode.ROOF;
        }
        if (!playerBuiltStructure
                && isNaturalTunnelMaterial(materialClass)
                && isNaturalUnsupportedTunnelRoof(block, materialClass)) {
            return StabilityFailureMode.ROOF;
        }
        if (playerBuiltStructure && isUnsupportedFloatingPlayerBuiltBlock(block, materialClass)) {
            return StabilityFailureMode.FLOATING;
        }
        if (isUnsupportedFreestandingWall(block, materialClass)) {
            return StabilityFailureMode.ROOF;
        }
        double stress = computeStabilityStress(block, materialClass);
        double threshold = getStabilityStressThreshold(materialClass) * getStabilityToleranceMultiplier(block, playerBuiltStructure);
        if (stress < threshold) {
            return null;
        }
        if (shaftExposed) {
            return StabilityFailureMode.SHAFT;
        }
        if (roofExposed) {
            return StabilityFailureMode.ROOF;
        }
        if ((materialClass == StabilityMaterialClass.LOOSE || materialClass == StabilityMaterialClass.PACKED_SOIL)
                && !isFoundationSupportedLooseWall(block)) {
            return StabilityFailureMode.FLOATING;
        }
        return materialClass == StabilityMaterialClass.LOOSE ? StabilityFailureMode.FLOATING : null;
    }

    private boolean isTerrainCollapseCandidate(Block block, StabilityMaterialClass materialClass) {
        if (block == null || block.isEmpty()) {
            return false;
        }
        if (materialClass == StabilityMaterialClass.LOOSE || materialClass == StabilityMaterialClass.PACKED_SOIL) {
            return isFreestandingSoilBlock(block)
                    || isNaturalShaftCollapseCandidate(block)
                    || isNaturalRoofCollapseCandidate(block, materialClass);
        }
        return isNaturalShaftCollapseCandidate(block) || isNaturalRoofCollapseCandidate(block, materialClass);
    }

    private double getStabilityToleranceMultiplier(Block block, boolean playerBuiltStructure) {
        double strictnessFactor = 100.0D / getStabilityStrictnessPercent();
        if (!playerBuiltStructure) {
            return getStabilityNaturalTerrainToleranceMultiplier() * strictnessFactor;
        }
        UUID ownerId = getPlacedBlockOwner(block);
        if (ownerId == null) {
            return 1.15D * strictnessFactor;
        }
        int cooldownSeconds = Math.max(8, getSharedActionCooldownSeconds(ownerId));
        double normalized = Math.min(1.0D, Math.max(0.0D, (cooldownSeconds - 8.0D) / 12.0D));
        return (1.0D + (normalized * 0.35D)) * strictnessFactor;
    }

    private boolean isPlayerBuiltStructureBlock(Block block) {
        if (block == null || block.isEmpty()) {
            return false;
        }
        if (getPlacedBlockOwner(block) != null) {
            return isStructureMaterial(block.getType());
        }
        return false;
    }

    private boolean isStructureMaterial(Material material) {
        if (material == null || material.isAir() || !material.isSolid()) {
            return false;
        }
        if (isFunctionalMeterMaterial(material)) {
            return false;
        }
        return !isFarmerCrop(material);
    }

    private boolean isStructuralMeterActionBlock(Block block) {
        return block != null && isStructureMaterial(block.getType());
    }

    private boolean isFunctionalMeterMaterial(Material material) {
        if (material == null) {
            return true;
        }
        if (material.isInteractable()) {
            return true;
        }
        String name = material.name();
        return name.endsWith("_CROP")
                || name.endsWith("_SAPLING")
                || name.endsWith("_BUTTON")
                || name.endsWith("_PRESSURE_PLATE")
                || name.endsWith("_TRAPDOOR")
                || name.endsWith("_DOOR")
                || name.endsWith("_BED")
                || name.endsWith("_SIGN")
                || name.endsWith("_BANNER")
                || name.endsWith("_TORCH")
                || name.endsWith("_LANTERN")
                || name.endsWith("_RAIL")
                || name.endsWith("_MINECART")
                || name.contains("CHEST")
                || name.contains("FURNACE")
                || name.contains("BARREL")
                || name.contains("SHULKER_BOX")
                || name.contains("COMMAND_BLOCK")
                || name.contains("SPAWNER")
                || name.contains("CAULDRON")
                || name.contains("ANVIL")
                || name.contains("LECTERN")
                || name.contains("BREWING")
                || name.contains("CAMPFIRE");
    }

    private boolean isExtraRestrictableUtilityMaterial(Material material) {
        if (material == null) {
            return false;
        }
        String name = material.name();
        return material == Material.ENDER_PEARL
                || name.endsWith("_BUCKET")
                || name.endsWith("_BOAT")
                || name.endsWith("_CHEST_BOAT")
                || material == Material.FLINT_AND_STEEL
                || material == Material.FIRE_CHARGE
                || material == Material.SHEARS
                || material == Material.LEAD
                || material == Material.NAME_TAG
                || material == Material.COMPASS
                || material == Material.RECOVERY_COMPASS
                || material == Material.CLOCK
                || material == Material.BRUSH
                || material == Material.SPYGLASS;
    }

    private boolean shouldCollapseBlock(Block block, StabilityMaterialClass expectedClass) {
        return getStabilityFailureMode(block, expectedClass) != null;
    }

    private double computeStabilityStress(Block block, StabilityMaterialClass materialClass) {
        if (!isPlayerBuiltStructureBlock(block)) {
            return computeTerrainStabilityStress(block, materialClass);
        }
        double blockWeight = getStabilityBlockWeight(block.getType());
        double stress = 0.0D;
        stress += Math.max(0, getUnsupportedFaceCount(block) - 1) * 0.55D;
        stress += Math.max(0, getUnsupportedHorizontalFaceCount(block) - 1) * 0.45D;
        int openSpanBelow = getOpenSpanBelow(block);
        stress += Math.max(0, openSpanBelow - getMaxSupportedSpan(materialClass)) * (0.65D + (blockWeight * getStabilityRoofLoadFactor()));
        stress += Math.max(0, getOpenVerticalDepth(block.getRelative(BlockFace.DOWN)) - getStabilityShaftOpenDepth()) * 0.18D;
        double supportedLoad = getColumnLoadAbove(block) + blockWeight;
        double supportCapacity = getBlockSupportCapacity(block, materialClass);
        stress += Math.max(0.0D, supportedLoad - supportCapacity) * getStabilityLoadStressMultiplier();
        stress += Math.max(0.0D, getColumnWeightAbove(block) - getColumnWeightAllowance(materialClass)) * getStabilityColumnWeightFactor();
        stress += getFreestandingWallStress(block, materialClass);
        if (hasClimateRecentlyRained(block.getWorld()) || block.getWorld().hasStorm()) {
            stress += getStabilityRainStress();
        }
        if (isNearWater(block)) {
            stress += getStabilityNearWaterStress();
        }
        if (materialClass == StabilityMaterialClass.PACKED_SOIL) {
            stress -= 0.35D;
        } else if (materialClass == StabilityMaterialClass.SOFT_ROCK) {
            stress -= 0.6D;
        } else if (materialClass == StabilityMaterialClass.FRAGILE) {
            stress -= 0.3D;
        }
        return stress;
    }

    private double computeTerrainStabilityStress(Block block, StabilityMaterialClass materialClass) {
        double stress = 0.0D;
        if (materialClass == StabilityMaterialClass.LOOSE || materialClass == StabilityMaterialClass.PACKED_SOIL) {
            stress += Math.max(0, getUnsupportedHorizontalFaceCount(block) - 2) * 0.24D;
            stress += Math.max(0, getOpenVerticalDepth(block.getRelative(BlockFace.DOWN)) - getStabilityNaturalShaftOpenDepth()) * 0.09D;
            if (isNearWater(block)) {
                stress += getStabilityNearWaterStress() * 0.4D;
            }
            return stress;
        }
        if (!isTerrainCollapseCandidate(block, materialClass)) {
            return 0.0D;
        }
        stress += Math.max(0, getUnsupportedHorizontalFaceCount(block) - 2) * 0.18D;
        stress += Math.max(0, getOpenVerticalDepth(block.getRelative(BlockFace.DOWN)) - (getStabilityNaturalShaftOpenDepth() + 1)) * 0.08D;
        stress += Math.max(0, getOpenSpanBelow(block) - (getMaxSupportedSpan(materialClass) + getStabilityNaturalRoofExtraSpan())) * 0.14D;
        double blockWeight = getStabilityBlockWeight(block.getType());
        double supportedLoad = getColumnLoadAbove(block) + blockWeight;
        double supportCapacity = getBlockSupportCapacity(block, materialClass);
        stress += Math.max(0.0D, supportedLoad - supportCapacity) * (getStabilityLoadStressMultiplier() * 0.55D);
        stress += Math.max(0.0D, getColumnWeightAbove(block) - getColumnWeightAllowance(materialClass)) * (getStabilityColumnWeightFactor() * 0.7D);
        if (isNaturalUnsupportedRoomRoof(block, materialClass)) {
            stress += 2.4D;
        }
        if (isNaturalUnsupportedTunnelRoof(block, materialClass)) {
            stress += Math.max(0.0D, getColumnWeightAbove(block) - (getColumnWeightAllowance(materialClass) * 0.6D)) * 0.12D;
        }
        return stress;
    }

    private double getFreestandingWallStress(Block block, StabilityMaterialClass materialClass) {
        if (!isPlayerBuiltStructureBlock(block)) {
            return 0.0D;
        }
        if (materialClass != StabilityMaterialClass.SOFT_ROCK && materialClass != StabilityMaterialClass.FRAGILE) {
            return 0.0D;
        }
        if (hasNearbyStableAnchor(block) || hasNearbyStructuralSupport(block) || hasSupportFrame(block)) {
            return 0.0D;
        }
        int wallHeight = getFreestandingWallHeight(block, materialClass);
        if (wallHeight < 7) {
            return 0.0D;
        }
        int thickness = getWallThickness(block, materialClass);
        if (thickness > 1) {
            return 0.0D;
        }
        int unsupportedBelow = getOpenVerticalDepth(block.getRelative(BlockFace.DOWN));
        double heightStress = Math.max(0, wallHeight - 6) * 0.45D;
        double faceStress = Math.max(0, getUnsupportedHorizontalFaceCount(block) - 1) * 0.25D;
        double unsupportedBaseStress = unsupportedBelow > 0 ? 0.75D : 0.0D;
        return heightStress + faceStress + unsupportedBaseStress;
    }

    private boolean isUnsupportedFreestandingWall(Block block, StabilityMaterialClass materialClass) {
        if (!isPlayerBuiltStructureBlock(block)) {
            return false;
        }
        if (materialClass != StabilityMaterialClass.SOFT_ROCK && materialClass != StabilityMaterialClass.FRAGILE) {
            return false;
        }
        if (hasNearbyStableAnchor(block) || hasNearbyStructuralSupport(block) || hasSupportFrame(block) || hasTimberedMineSupport(block)) {
            return false;
        }
        return getFreestandingWallHeight(block, materialClass) >= 10
                && getWallThickness(block, materialClass) <= 1
                && getUnsupportedHorizontalFaceCount(block) >= 2;
    }

    private int getFreestandingWallHeight(Block block, StabilityMaterialClass materialClass) {
        return 1 + countConnectedWallBlocks(block, BlockFace.UP, materialClass) + countConnectedWallBlocks(block, BlockFace.DOWN, materialClass);
    }

    private int countConnectedWallBlocks(Block origin, BlockFace direction, StabilityMaterialClass materialClass) {
        int count = 0;
        int limit = Math.max(6, getStabilityWeightTraceHeight());
        Block cursor = origin;
        for (int i = 0; i < limit; i++) {
            cursor = cursor.getRelative(direction);
            if (cursor.isEmpty() || getStabilityMaterialClass(cursor.getType()) != materialClass) {
                break;
            }
            count++;
        }
        return count;
    }

    private int getWallThickness(Block block, StabilityMaterialClass materialClass) {
        int eastWest = 1 + countSameMaterialDirection(block, BlockFace.EAST, materialClass, 2) + countSameMaterialDirection(block, BlockFace.WEST, materialClass, 2);
        int northSouth = 1 + countSameMaterialDirection(block, BlockFace.NORTH, materialClass, 2) + countSameMaterialDirection(block, BlockFace.SOUTH, materialClass, 2);
        return Math.min(eastWest, northSouth);
    }

    private int countSameMaterialDirection(Block origin, BlockFace direction, StabilityMaterialClass materialClass, int limit) {
        int count = 0;
        Block cursor = origin;
        for (int i = 0; i < limit; i++) {
            cursor = cursor.getRelative(direction);
            if (cursor.isEmpty() || getStabilityMaterialClass(cursor.getType()) != materialClass) {
                break;
            }
            count++;
        }
        return count;
    }

    private double getColumnLoadAbove(Block block) {
        if (block == null || block.isEmpty()) {
            return 0.0D;
        }
        double load = 0.0D;
        Block cursor = block.getRelative(BlockFace.UP);
        int limit = getStabilityWeightTraceHeight();
        for (int i = 0; i < limit; i++) {
            if (cursor == null || cursor.isEmpty() || isOpenSpace(cursor)) {
                break;
            }
            Material type = cursor.getType();
            double weight = getStabilityBlockWeight(type);
            if (weight <= 0.0D) {
                break;
            }
            load += weight;
            if (!isSimilarStructuralColumnMaterial(block.getType(), type)) {
                load += weight * 0.2D;
                break;
            }
            cursor = cursor.getRelative(BlockFace.UP);
        }
        return load;
    }

    private double getColumnWeightAbove(Block block) {
        if (block == null || block.isEmpty()) {
            return 0.0D;
        }
        double total = 0.0D;
        Block cursor = block.getRelative(BlockFace.UP);
        int limit = getStabilityWeightTraceHeight();
        for (int i = 0; i < limit; i++) {
            if (cursor == null || cursor.isEmpty() || isOpenSpace(cursor)) {
                break;
            }
            double weight = getStabilityBlockWeight(cursor.getType());
            if (weight <= 0.0D) {
                break;
            }
            total += weight;
            cursor = cursor.getRelative(BlockFace.UP);
        }
        return total;
    }

    private double getBlockSupportCapacity(Block block, StabilityMaterialClass materialClass) {
        if (block == null || block.isEmpty()) {
            return 0.0D;
        }
        boolean playerBuiltStructure = isPlayerBuiltStructureBlock(block);

        double capacity = getIntrinsicSupportCapacity(block.getType());
        Block below = block.getRelative(BlockFace.DOWN);
        if (!isOpenSpace(below)) {
            capacity += getIntrinsicSupportCapacity(below.getType()) * getStabilityFoundationSupportFactor();
        }

        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block adjacent = block.getRelative(face);
            if (isOpenSpace(adjacent)) {
                continue;
            }
            capacity += getIntrinsicSupportCapacity(adjacent.getType()) * getStabilityAdjacentSupportFactor();
        }

        if (hasNearbyStructuralSupport(block)) {
            capacity += 1.4D;
        }
        if (hasSupportFrame(block) || hasTimberedMineSupport(block)) {
            capacity += getStabilitySupportFrameBonus();
        }
        if (playerBuiltStructure && hasNearbyStableAnchor(block)) {
            capacity += getStabilityAnchorSupportBonus();
        }
        capacity += getIntentionalSupportPatternBonus(block);

        return switch (materialClass) {
            case LOOSE -> capacity;
            case PACKED_SOIL -> capacity + 0.4D;
            case SOFT_ROCK -> capacity + 0.75D;
            case FRAGILE -> capacity + 1.0D;
            case HARD_ROCK, STABLE -> capacity + 1.5D;
        };
    }

    private double getColumnWeightAllowance(StabilityMaterialClass materialClass) {
        return switch (materialClass) {
            case LOOSE -> 1.75D;
            case PACKED_SOIL -> 2.6D;
            case SOFT_ROCK -> 4.5D;
            case FRAGILE -> 6.5D;
            case HARD_ROCK -> 10.0D;
            case STABLE -> Double.MAX_VALUE;
        };
    }

    private boolean isExposedRoofBlock(Block block) {
        return isOpenSpace(block.getRelative(BlockFace.DOWN));
    }

    private boolean isNaturalRoofCollapseCandidate(Block block, StabilityMaterialClass materialClass) {
        if (block == null || block.isEmpty() || isPlayerBuiltStructureBlock(block)) {
            return false;
        }
        if (!isOpenSpace(block.getRelative(BlockFace.DOWN))) {
            return false;
        }
        if (hasNaturalMiningSupport(block)) {
            return false;
        }
        int requiredSpan = getMaxSupportedSpan(materialClass) + getStabilityNaturalRoofExtraSpan();
        if (getOpenSpanBelow(block) < requiredSpan) {
            return false;
        }
        if (isNaturalTunnelMaterial(materialClass) && isNaturalUnsupportedRoomRoof(block, materialClass)) {
            return true;
        }
        if (isNaturalTunnelMaterial(materialClass) && isNaturalUnsupportedTunnelRoof(block, materialClass)) {
            return true;
        }
        return getUnsupportedHorizontalFaceCount(block) >= 2
                || getOpenVerticalDepth(block.getRelative(BlockFace.DOWN)) >= getStabilityNaturalShaftOpenDepth();
    }

    private boolean isNaturalUnsupportedRoomRoof(Block block, StabilityMaterialClass materialClass) {
        if (block == null || block.isEmpty() || !isNaturalTunnelMaterial(materialClass)) {
            return false;
        }
        Block below = block.getRelative(BlockFace.DOWN);
        if (!isOpenSpace(below)) {
            return false;
        }
        int width = getOpenLineSpan(below, BlockFace.EAST, BlockFace.WEST);
        int length = getOpenLineSpan(below, BlockFace.NORTH, BlockFace.SOUTH);
        int height = getOpenVerticalDepth(below);
        return width >= getStabilityNaturalRoomMinWidth()
                && length >= getStabilityNaturalRoomMinLength()
                && height >= getStabilityNaturalRoomMinHeight();
    }

    private boolean isNaturalUnsupportedTunnelRoof(Block block, StabilityMaterialClass materialClass) {
        if (block == null || block.isEmpty() || !isNaturalTunnelMaterial(materialClass)) {
            return false;
        }
        Block below = block.getRelative(BlockFace.DOWN);
        if (!isOpenSpace(below)) {
            return false;
        }
        int eastWestSpan = getOpenLineSpan(below, BlockFace.EAST, BlockFace.WEST);
        int northSouthSpan = getOpenLineSpan(below, BlockFace.NORTH, BlockFace.SOUTH);
        int width = Math.min(eastWestSpan, northSouthSpan);
        int length = Math.max(eastWestSpan, northSouthSpan);
        int height = getOpenVerticalDepth(below);
        return width >= getStabilityNaturalTunnelMinWidth()
                && height >= getStabilityNaturalTunnelMinHeight()
                && length >= getStabilityNaturalTunnelMinLength();
    }

    private int getUnsupportedFaceCount(Block block) {
        int count = 0;
        if (isOpenSpace(block.getRelative(BlockFace.DOWN))) {
            count++;
        }
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            if (isOpenSpace(block.getRelative(face)) && isOpenSpace(block.getRelative(face).getRelative(BlockFace.DOWN))) {
                count++;
            }
        }
        return count;
    }

    private int getUnsupportedHorizontalFaceCount(Block block) {
        int count = 0;
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            if (isOpenSpace(block.getRelative(face))) {
                count++;
            }
        }
        return count;
    }

    private boolean isFreestandingSoilBlock(Block block) {
        StabilityMaterialClass materialClass = getStabilityMaterialClass(block.getType());
        if (materialClass != StabilityMaterialClass.LOOSE && materialClass != StabilityMaterialClass.PACKED_SOIL) {
            return false;
        }
        if (isLooseStackGroundSupported(block)) {
            return false;
        }
        Block below = block.getRelative(BlockFace.DOWN);
        StabilityMaterialClass belowClass = getStabilityMaterialClass(below.getType());
        if (!isOpenSpace(below)
                && belowClass != StabilityMaterialClass.LOOSE
                && belowClass != StabilityMaterialClass.PACKED_SOIL) {
            return false;
        }
        if (hasHorizontalBraceSupport(block)) {
            return false;
        }
        if (hasNearbyStableAnchor(block)) {
            return false;
        }
        return getUnsupportedHorizontalFaceCount(block) >= 1 || isOpenSpace(below);
    }

    private boolean isUnsupportedFloatingPlayerBuiltBlock(Block block, StabilityMaterialClass materialClass) {
        if (!isPlayerBuiltStructureBlock(block)) {
            return false;
        }
        if (materialClass == StabilityMaterialClass.STABLE || materialClass == StabilityMaterialClass.HARD_ROCK) {
            return false;
        }
        if (hasNearbyStableAnchor(block) || hasNearbyStructuralSupport(block) || hasSupportFrame(block) || hasTimberedMineSupport(block)) {
            return false;
        }

        Block below = block.getRelative(BlockFace.DOWN);
        int openBelow = getOpenVerticalDepth(below);
        if (openBelow <= 0) {
            return false;
        }

        int unsupportedHorizontalFaces = getUnsupportedHorizontalFaceCount(block);
        if (openBelow >= 1 && unsupportedHorizontalFaces >= 3) {
            return true;
        }
        if (openBelow >= 2 && unsupportedHorizontalFaces >= 2) {
            return true;
        }

        double supportedLoad = getColumnLoadAbove(block) + getStabilityBlockWeight(block.getType());
        double supportCapacity = getBlockSupportCapacity(block, materialClass);
        return supportedLoad > (supportCapacity * 0.8D);
    }

    private boolean hasHorizontalBraceSupport(Block block) {
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block adjacent = block.getRelative(face);
            Material material = adjacent.getType();
            StabilityMaterialClass adjacentClass = getStabilityMaterialClass(material);
            if (isStructuralSupportMaterial(material)
                    || adjacentClass == StabilityMaterialClass.SOFT_ROCK
                    || adjacentClass == StabilityMaterialClass.FRAGILE
                    || adjacentClass == StabilityMaterialClass.HARD_ROCK) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSupportFrame(Block block) {
        int height = getStabilitySupportFrameVerticalHeight();
        int span = getStabilitySupportFrameHorizontalSpan();
        for (BlockFace axis : new BlockFace[]{BlockFace.EAST, BlockFace.NORTH}) {
            BlockFace opposite = axis == BlockFace.EAST ? BlockFace.WEST : BlockFace.SOUTH;
            Block leftBase = block.getRelative(axis, span / 2);
            Block rightBase = block.getRelative(opposite, span / 2);
            if (!isSupportPost(leftBase, height) || !isSupportPost(rightBase, height)) {
                continue;
            }
            int beamY = block.getY() + height - 1;
            boolean beamComplete = true;
            for (int offset = -(span / 2); offset <= span / 2; offset++) {
                Block beamBlock = axis == BlockFace.EAST
                        ? block.getWorld().getBlockAt(block.getX() + offset, beamY, block.getZ())
                        : block.getWorld().getBlockAt(block.getX(), beamY, block.getZ() + offset);
                if (!isStructuralSupportMaterial(beamBlock.getType())) {
                    beamComplete = false;
                    break;
                }
            }
            if (beamComplete) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTimberedMineSupport(Block block) {
        return hasTimberedMineSupport(block, BlockFace.EAST, BlockFace.WEST)
                || hasTimberedMineSupport(block, BlockFace.NORTH, BlockFace.SOUTH);
    }

    private boolean hasTimberedMineSupport(Block block, BlockFace left, BlockFace right) {
        Block leftSide = block.getRelative(left);
        Block rightSide = block.getRelative(right);
        if (!hasTimberPost(leftSide) || !hasTimberPost(rightSide)) {
            return false;
        }
        return hasBeamAcross(block, left, right, 0) || hasBeamAcross(block, left, right, 1);
    }

    private boolean hasTimberPost(Block sideBlock) {
        if (sideBlock == null) {
            return false;
        }
        int minHeight = Math.max(2, getStabilitySupportFrameVerticalHeight() - 1);
        for (int startOffset = 0; startOffset <= 1; startOffset++) {
            Block cursor = sideBlock.getRelative(BlockFace.DOWN, startOffset);
            int height = 0;
            while (height < getStabilitySupportFrameVerticalHeight() + 1 && isStructuralSupportMaterial(cursor.getType())) {
                height++;
                cursor = cursor.getRelative(BlockFace.DOWN);
            }
            if (height >= minHeight && !isOpenSpace(cursor)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBeamAcross(Block block, BlockFace left, BlockFace right, int yOffset) {
        Block center = block.getRelative(BlockFace.UP, yOffset);
        return isStructuralSupportMaterial(center.getType())
                && isStructuralSupportMaterial(center.getRelative(left).getType())
                && isStructuralSupportMaterial(center.getRelative(right).getType());
    }

    private boolean isSupportPost(Block topishBlock, int height) {
        Block cursor = topishBlock;
        for (int i = 0; i < height; i++) {
            if (!isStructuralSupportMaterial(cursor.getType())) {
                return false;
            }
            cursor = cursor.getRelative(BlockFace.DOWN);
        }
        return !isOpenSpace(cursor);
    }

    private boolean isExposedShaftWallBlock(Block block) {
        if (hasHorizontalBraceSupport(block) || hasNearbyStableAnchor(block)) {
            return false;
        }
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            if (!isOpenSpace(block.getRelative(face))) {
                continue;
            }
            if (hasTerracedQuarrySupport(block, face)) {
                continue;
            }
            if (getOpenVerticalDepth(block.getRelative(face)) < getStabilityShaftOpenDepth()) {
                continue;
            }
            if (isOpenSpace(block.getRelative(face.getOppositeFace()))) {
                return true;
            }
            if (getUnsupportedHorizontalFaceCount(block) >= 2) {
                return true;
            }
        }
        return false;
    }

    private boolean isNaturalShaftCollapseCandidate(Block block) {
        if (block == null || block.isEmpty() || isPlayerBuiltStructureBlock(block)) {
            return false;
        }
        if (hasNaturalMiningSupport(block)) {
            return false;
        }
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block side = block.getRelative(face);
            if (!isOpenSpace(side) || hasTerracedQuarrySupport(block, face)) {
                continue;
            }
            if (getOpenVerticalDepth(side) < getStabilityNaturalShaftOpenDepth()) {
                continue;
            }
            if (isNaturalUnsupportedTunnelBlock(block, getStabilityMaterialClass(block.getType()), face)) {
                return true;
            }
            if (isOpenSpace(block.getRelative(face.getOppositeFace())) || getUnsupportedHorizontalFaceCount(block) >= 3) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNaturalMiningSupport(Block block) {
        return hasNearbyStructuralSupport(block)
                || hasSupportFrame(block)
                || hasTimberedMineSupport(block);
    }

    private boolean isNaturalUnsupportedTunnelBlock(Block block, StabilityMaterialClass materialClass) {
        if (block == null || block.isEmpty()) {
            return false;
        }
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            if (isNaturalUnsupportedTunnelBlock(block, materialClass, face)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNaturalUnsupportedTunnelBlock(Block block, StabilityMaterialClass materialClass, BlockFace openFace) {
        if (block == null || block.isEmpty() || isPlayerBuiltStructureBlock(block) || !isNaturalTunnelMaterial(materialClass)) {
            return false;
        }
        Block openSide = block.getRelative(openFace);
        if (!isOpenSpace(openSide) || hasTerracedQuarrySupport(block, openFace)) {
            return false;
        }
        if (getOpenVerticalDepth(openSide) < getStabilityNaturalShaftOpenDepth()) {
            return false;
        }
        int tunnelWidth = getOpenCorridorWidth(openSide, openFace);
        if (tunnelWidth < getStabilityNaturalTunnelMinWidth()) {
            return false;
        }
        int tunnelLength = getOpenCorridorLength(openSide, openFace);
        return tunnelLength >= getStabilityNaturalTunnelMinLength();
    }

    private boolean hasTerracedQuarrySupport(Block block, BlockFace openFace) {
        Block open = block.getRelative(openFace);
        if (!isOpenSpace(open)) {
            return false;
        }
        if (!isOpenSpace(open.getRelative(BlockFace.DOWN))) {
            return true;
        }
        if (!isOpenSpace(open.getRelative(BlockFace.DOWN, 2))) {
            return true;
        }
        Block lateralLeft = open.getRelative(rotateLeft(openFace));
        Block lateralRight = open.getRelative(rotateRight(openFace));
        return !isOpenSpace(lateralLeft.getRelative(BlockFace.DOWN))
                || !isOpenSpace(lateralRight.getRelative(BlockFace.DOWN))
                || isStructuralSupportMaterial(open.getType())
                || isStructuralSupportMaterial(open.getRelative(BlockFace.DOWN).getType());
    }

    private BlockFace rotateLeft(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.WEST;
            case SOUTH -> BlockFace.EAST;
            case EAST -> BlockFace.NORTH;
            case WEST -> BlockFace.SOUTH;
            default -> BlockFace.NORTH;
        };
    }

    private BlockFace rotateRight(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case SOUTH -> BlockFace.WEST;
            case EAST -> BlockFace.SOUTH;
            case WEST -> BlockFace.NORTH;
            default -> BlockFace.SOUTH;
        };
    }

    private int getOpenVerticalDepth(Block start) {
        int depth = 0;
        int limit = Math.max(getStabilityScanRadius(), getStabilityShaftOpenDepth() + 2);
        Block cursor = start;
        while (depth < limit && isOpenSpace(cursor)) {
            depth++;
            cursor = cursor.getRelative(BlockFace.DOWN);
        }
        return depth;
    }

    private boolean isLooseStackGroundSupported(Block block) {
        int maxDepth = getStabilityLooseGroundedStackHeight();
        Block cursor = block;
        for (int depth = 0; depth < maxDepth; depth++) {
            Block below = cursor.getRelative(BlockFace.DOWN);
            StabilityMaterialClass belowClass = getStabilityMaterialClass(below.getType());
            if (!isOpenSpace(below) && belowClass != StabilityMaterialClass.LOOSE) {
                return true;
            }
            if (belowClass != StabilityMaterialClass.LOOSE) {
                return false;
            }
            cursor = below;
        }
        Block below = cursor.getRelative(BlockFace.DOWN);
        StabilityMaterialClass belowClass = getStabilityMaterialClass(below.getType());
        return !isOpenSpace(below) && belowClass != StabilityMaterialClass.LOOSE;
    }

    private boolean isFoundationSupportedLooseWall(Block block) {
        if (block == null || block.isEmpty()) {
            return false;
        }
        if (isOpenSpace(block.getRelative(BlockFace.DOWN))) {
            return false;
        }
        Block cursor = block;
        int limit = Math.max(4, getStabilityScanRadius() * 2);
        for (int depth = 0; depth < limit; depth++) {
            Block below = cursor.getRelative(BlockFace.DOWN);
            if (isOpenSpace(below)) {
                return false;
            }
            StabilityMaterialClass belowClass = getStabilityMaterialClass(below.getType());
            if (belowClass != StabilityMaterialClass.LOOSE && belowClass != StabilityMaterialClass.PACKED_SOIL) {
                return true;
            }
            cursor = below;
        }
        return false;
    }

    private boolean hasNearbyStableAnchor(Block block) {
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            for (int distance = 1; distance <= 2; distance++) {
                Block adjacent = block.getRelative(face, distance);
                StabilityMaterialClass adjacentClass = getStabilityMaterialClass(adjacent.getType());
                if (isStableAnchorMaterial(adjacent.getType())) {
                    if (!isPlayerBuiltStructureBlock(adjacent) || isGroundAnchoredSupport(adjacent, true)) {
                        return true;
                    }
                } else if (isStructuralSupportMaterial(adjacent.getType()) && isGroundAnchoredSupport(adjacent, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getMaxSupportedSpan(StabilityMaterialClass materialClass) {
        return switch (materialClass) {
            case LOOSE -> getStabilityLooseMaxSpan();
            case PACKED_SOIL -> Math.max(getStabilityLooseMaxSpan() + 1, getConfig().getInt("stability.packed-max-span", 3));
            case SOFT_ROCK -> Math.max(2, getConfig().getInt("stability.soft-rock-max-span", 4));
            case FRAGILE -> Math.max(2, getConfig().getInt("stability.fragile-max-span", 4));
            case HARD_ROCK, STABLE -> Integer.MAX_VALUE;
        };
    }

    private int getOpenSpanBelow(Block block) {
        Block below = block.getRelative(BlockFace.DOWN);
        if (!isOpenSpace(below)) {
            return 0;
        }
        return Math.max(
                getOpenLineSpan(below, BlockFace.EAST, BlockFace.WEST),
                getOpenLineSpan(below, BlockFace.NORTH, BlockFace.SOUTH)
        );
    }

    private int getOpenLineSpan(Block center, BlockFace forward, BlockFace backward) {
        int span = 1;
        span += countOpenBlocks(center, forward);
        span += countOpenBlocks(center, backward);
        return span;
    }

    private int getOpenCorridorWidth(Block center, BlockFace openFace) {
        BlockFace left = rotateLeft(openFace);
        BlockFace right = rotateRight(openFace);
        return 1 + countOpenBlocks(center, left) + countOpenBlocks(center, right);
    }

    private int getOpenCorridorLength(Block center, BlockFace openFace) {
        BlockFace forward = rotateLeft(openFace);
        BlockFace backward = rotateRight(openFace);
        int limit = Math.max(getStabilityScanRadius() * 2, getStabilityNaturalTunnelMinLength() + 2);
        int length = 1;
        length += countOpenRunWithExposedWall(center, forward, openFace, limit);
        length += countOpenRunWithExposedWall(center, backward, openFace, limit);
        return length;
    }

    private int countOpenRunWithExposedWall(Block origin, BlockFace direction, BlockFace openFace, int limit) {
        int count = 0;
        Block cursor = origin;
        for (int i = 0; i < limit; i++) {
            cursor = cursor.getRelative(direction);
            if (!isOpenSpace(cursor)) {
                break;
            }
            if (!isOpenSpace(cursor.getRelative(openFace))) {
                break;
            }
            count++;
        }
        return count;
    }

    private int countOpenBlocks(Block origin, BlockFace direction) {
        int count = 0;
        int limit = Math.max(getStabilityFragileMaxSpan() + 4, getStabilityLooseMaxSpan() + 4);
        Block cursor = origin;
        for (int i = 0; i < limit; i++) {
            cursor = cursor.getRelative(direction);
            if (!isOpenSpace(cursor)) {
                break;
            }
            count++;
        }
        return count;
    }

    private boolean hasNearbyStructuralSupport(Block block) {
        int horizontal = getStabilitySupportHorizontalRadius();
        int vertical = getStabilitySupportVerticalRadius();
        World world = block.getWorld();
        for (int x = -horizontal; x <= horizontal; x++) {
            for (int z = -horizontal; z <= horizontal; z++) {
                for (int y = -vertical; y <= vertical; y++) {
                    Block nearby = world.getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);
                    if (isStructuralSupportMaterial(nearby.getType()) && isGroundAnchoredSupport(nearby, false)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private double getIntentionalSupportPatternBonus(Block block) {
        double bonus = 0.0D;
        if (hasVerticalSupportPost(block, 6)) {
            bonus += 1.35D;
        }
        if (hasButtressSupport(block)) {
            bonus += 1.1D;
        }
        int braces = countAdjacentSupportMembers(block);
        if (braces > 0) {
            bonus += braces * 0.3D;
        }
        return bonus;
    }

    private boolean hasVerticalSupportPost(Block block, int depth) {
        Block cursor = block.getRelative(BlockFace.DOWN);
        int supportDepth = 0;
        while (supportDepth < depth && !isOpenSpace(cursor) && isStructuralSupportMaterial(cursor.getType())) {
            supportDepth++;
            cursor = cursor.getRelative(BlockFace.DOWN);
        }
        return supportDepth >= 2 && !isOpenSpace(cursor);
    }

    private boolean hasButtressSupport(Block block) {
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block side = block.getRelative(face);
            Block down = side.getRelative(BlockFace.DOWN);
            if (isStructuralSupportMaterial(side.getType()) && isGroundAnchoredSupport(side, false)) {
                return true;
            }
            if (isStructuralSupportMaterial(down.getType()) && isGroundAnchoredSupport(down, false)) {
                return true;
            }
        }
        return false;
    }

    private int countAdjacentSupportMembers(Block block) {
        int supports = 0;
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block adjacent = block.getRelative(face);
            if (isStructuralSupportMaterial(adjacent.getType()) && isGroundAnchoredSupport(adjacent, false)) {
                supports++;
            }
        }
        return supports;
    }

    private boolean isStableAnchorMaterial(Material material) {
        StabilityMaterialClass materialClass = getStabilityMaterialClass(material);
        return materialClass == StabilityMaterialClass.SOFT_ROCK
                || materialClass == StabilityMaterialClass.FRAGILE
                || materialClass == StabilityMaterialClass.HARD_ROCK;
    }

    private boolean isGroundAnchoredSupport(Block start, boolean allowAnchorMaterials) {
        if (start == null || start.isEmpty()) {
            return false;
        }
        Block cursor = start;
        int limit = Math.max(6, getStabilityWeightTraceHeight());
        for (int depth = 0; depth < limit; depth++) {
            Material type = cursor.getType();
            if (!isStructuralSupportMaterial(type) && !(allowAnchorMaterials && isStableAnchorMaterial(type))) {
                return false;
            }
            Block below = cursor.getRelative(BlockFace.DOWN);
            if (isOpenSpace(below)) {
                return false;
            }
            Material belowType = below.getType();
            if (!isStructuralSupportMaterial(belowType) && !(allowAnchorMaterials && isStableAnchorMaterial(belowType))) {
                return true;
            }
            cursor = below;
        }
        return false;
    }

    private boolean isOpenSpace(Block block) {
        return block == null || !block.getType().isSolid();
    }

    private boolean isNearWater(Block block) {
        int radius = getStabilityWaterRadius();
        World world = block.getWorld();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Material material = world.getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z).getType();
                    if (material == Material.WATER || material == Material.BUBBLE_COLUMN) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isSimilarStructuralColumnMaterial(Material base, Material other) {
        if (base == other) {
            return true;
        }
        StabilityMaterialClass baseClass = getStabilityMaterialClass(base);
        StabilityMaterialClass otherClass = getStabilityMaterialClass(other);
        if (baseClass == otherClass && baseClass != StabilityMaterialClass.STABLE) {
            return true;
        }
        return isStructuralSupportMaterial(base) && isStructuralSupportMaterial(other);
    }

    private double getStabilityBlockWeight(Material material) {
        if (material == null || material.isAir() || !material.isSolid()) {
            return 0.0D;
        }
        if (Tag.LOGS.isTagged(material)) {
            return 0.9D;
        }
        String name = material.name();
        if (name.endsWith("_LEAVES")) {
            return 0.25D;
        }
        if (name.endsWith("_WOOL") || name.endsWith("_CARPET") || name.endsWith("_BANNER")) {
            return 0.2D;
        }
        if (name.endsWith("_GLASS") || name.endsWith("_GLASS_PANE")) {
            return 0.55D;
        }
        if (name.endsWith("_PLANKS") || name.endsWith("_WOOD") || name.endsWith("_HYPHAE")) {
            return 1.0D;
        }
        if (name.endsWith("_SLAB")) {
            return 0.65D;
        }
        if (name.endsWith("_STAIRS")) {
            return 0.85D;
        }
        if (name.endsWith("_FENCE") || name.endsWith("_FENCE_GATE") || name.endsWith("_WALL")) {
            return 0.75D;
        }
        if (name.endsWith("_BRICKS") || name.endsWith("_BRICK") || name.endsWith("_TILES")) {
            return 2.1D;
        }
        if (name.endsWith("_CONCRETE")) {
            return 2.2D;
        }
        if (name.contains("IRON") || name.contains("COPPER") || name.contains("GOLD")) {
            return 2.6D;
        }

        return switch (getStabilityMaterialClass(material)) {
            case LOOSE -> 0.8D;
            case PACKED_SOIL -> 1.15D;
            case SOFT_ROCK -> 1.45D;
            case FRAGILE -> 1.9D;
            case HARD_ROCK -> 2.3D;
            case STABLE -> 1.1D;
        };
    }

    private double getIntrinsicSupportCapacity(Material material) {
        if (material == null || material.isAir() || !material.isSolid()) {
            return 0.0D;
        }
        if (Tag.LOGS.isTagged(material)) {
            return 3.3D;
        }
        String name = material.name();
        if (name.endsWith("_FENCE") || name.endsWith("_FENCE_GATE") || name.endsWith("_WALL")) {
            return 2.6D;
        }
        if (name.endsWith("_PLANKS") || name.endsWith("_WOOD") || name.endsWith("_HYPHAE")) {
            return 2.8D;
        }
        if (name.endsWith("_SLAB")) {
            return 1.4D;
        }
        if (name.endsWith("_STAIRS")) {
            return 1.8D;
        }
        if (name.contains("IRON") || name.contains("COPPER")) {
            return 4.4D;
        }

        return switch (getStabilityMaterialClass(material)) {
            case LOOSE -> 0.75D;
            case PACKED_SOIL -> 1.6D;
            case SOFT_ROCK -> 2.4D;
            case FRAGILE -> 3.3D;
            case HARD_ROCK -> 4.8D;
            case STABLE -> material.isSolid() ? 2.2D : 0.0D;
        };
    }

    private StabilityMaterialClass getStabilityMaterialClass(Material material) {
        if (material == null || material.isAir()) {
            return StabilityMaterialClass.STABLE;
        }
        if (isLooseStabilityMaterial(material)) {
            return StabilityMaterialClass.LOOSE;
        }
        if (isPackedSoilStabilityMaterial(material)) {
            return StabilityMaterialClass.PACKED_SOIL;
        }
        if (isSoftRockStabilityMaterial(material)) {
            return StabilityMaterialClass.SOFT_ROCK;
        }
        if (isFragileStabilityMaterial(material)) {
            return StabilityMaterialClass.FRAGILE;
        }
        if (isHardRockStabilityMaterial(material)) {
            return StabilityMaterialClass.HARD_ROCK;
        }
        return StabilityMaterialClass.STABLE;
    }

    private boolean isLooseStabilityMaterial(Material material) {
        return switch (material) {
            case SAND, RED_SAND, GRAVEL, SOUL_SAND, SOUL_SOIL, CLAY -> true;
            default -> false;
        };
    }

    private boolean isPackedSoilStabilityMaterial(Material material) {
        return switch (material) {
            case DIRT, GRASS_BLOCK, COARSE_DIRT, ROOTED_DIRT, PODZOL, MYCELIUM, MUD, MOSS_BLOCK, MUDDY_MANGROVE_ROOTS -> true;
            default -> false;
        };
    }

    private boolean isSoftRockStabilityMaterial(Material material) {
        return switch (material) {
            case SANDSTONE, RED_SANDSTONE, TERRACOTTA, WHITE_TERRACOTTA, LIGHT_GRAY_TERRACOTTA, GRAY_TERRACOTTA,
                    BLACK_TERRACOTTA, BROWN_TERRACOTTA, RED_TERRACOTTA, ORANGE_TERRACOTTA, YELLOW_TERRACOTTA,
                    LIME_TERRACOTTA, GREEN_TERRACOTTA, CYAN_TERRACOTTA, LIGHT_BLUE_TERRACOTTA,
                    BLUE_TERRACOTTA, PURPLE_TERRACOTTA, MAGENTA_TERRACOTTA, PINK_TERRACOTTA -> true;
            default -> false;
        };
    }

    private boolean isFragileStabilityMaterial(Material material) {
        return switch (material) {
            case STONE, COBBLESTONE, ANDESITE, DIORITE, GRANITE, TUFF, CALCITE,
                    DRIPSTONE_BLOCK, BLACKSTONE -> true;
            default -> false;
        };
    }

    private boolean isHardRockStabilityMaterial(Material material) {
        return switch (material) {
            case DEEPSLATE, COBBLED_DEEPSLATE, TUFF_BRICKS, DEEPSLATE_BRICKS, DEEPSLATE_TILES, OBSIDIAN, BEDROCK -> true;
            default -> false;
        };
    }

    private boolean isStructuralSupportMaterial(Material material) {
        return material != null && configuredStructuralSupportMaterials.contains(material);
    }

    private void reloadStructuralSupportMaterials() {
        configuredStructuralSupportMaterials.clear();
        List<String> configured = getConfig().getStringList("stability.support-materials");
        if (configured.isEmpty()) {
            configured = getDefaultStructuralSupportMaterialNames();
        }
        for (String entry : configured) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            Material material = Material.matchMaterial(entry.trim().toUpperCase(Locale.ROOT));
            if (material != null && material.isBlock() && !material.isAir()) {
                configuredStructuralSupportMaterials.add(material);
            }
        }
        if (configuredStructuralSupportMaterials.isEmpty()) {
            for (String entry : getDefaultStructuralSupportMaterialNames()) {
                Material material = Material.matchMaterial(entry);
                if (material != null) {
                    configuredStructuralSupportMaterials.add(material);
                }
            }
            saveStructuralSupportMaterials();
        }
    }

    private void saveStructuralSupportMaterials() {
        List<String> names = new ArrayList<>();
        for (Material material : getConfiguredStructuralSupportMaterials()) {
            names.add(material.name());
        }
        setManagedConfigValue("stability.support-materials", names);
        reloadStructuralSupportMaterials();
    }

    private List<String> getDefaultStructuralSupportMaterialNames() {
        List<String> defaults = new ArrayList<>();
        for (Material material : Material.values()) {
            if (material == null || material.isAir() || !material.isBlock()) {
                continue;
            }
            if (Tag.LOGS.isTagged(material)) {
                defaults.add(material.name());
                continue;
            }
            String name = material.name();
            if (name.endsWith("_WOOD")
                    || name.endsWith("_PLANKS")
                    || name.endsWith("_SLAB")
                    || name.endsWith("_STAIRS")
                    || name.endsWith("_FENCE")
                    || name.endsWith("_FENCE_GATE")
                    || name.endsWith("_WALL")
                    || name.endsWith("_IRON_BARS")
                    || name.endsWith("_CHAIN")) {
                defaults.add(name);
            }
        }
        defaults.sort(String::compareTo);
        return defaults;
    }

    private boolean scheduleStabilityCollapse(List<Block> cluster, StabilityMaterialClass materialClass) {
        if (cluster.isEmpty()) {
            return false;
        }

        UUID collapseId = UUID.randomUUID();
        List<PlacedBlockKey> blockKeys = new ArrayList<>(cluster.size());
        for (Block block : cluster) {
            PlacedBlockKey key = PlacedBlockKey.from(block);
            if (pendingStabilityBlockKeys.add(key)) {
                blockKeys.add(key);
            }
        }
        if (blockKeys.isEmpty()) {
            return false;
        }

        Location center = getStabilityClusterCenter(cluster);
        StabilityFailureMode failureMode = determineCollapseMode(cluster, materialClass);
        StabilityCollapse collapse = new StabilityCollapse(collapseId, center.getWorld().getUID(), blockKeys, materialClass, failureMode, center);
        pendingStabilityCollapses.put(collapseId, collapse);
        warnPlayersAboutStability(center, materialClass, failureMode);

        collapse.warningTask = getServer().getScheduler().runTaskTimer(this, () -> tickStabilityWarning(collapseId), 0L, 5L);
        collapse.executeTask = getServer().getScheduler().runTaskLater(this, () -> beginStabilityCollapse(collapseId), getStabilityWarningDelayTicks());
        return true;
    }

    private Location getStabilityClusterCenter(List<Block> cluster) {
        Block sample = cluster.get(0);
        double totalX = 0.0D;
        double totalY = 0.0D;
        double totalZ = 0.0D;
        for (Block block : cluster) {
            totalX += block.getX() + 0.5D;
            totalY += block.getY() + 0.5D;
            totalZ += block.getZ() + 0.5D;
        }
        double count = cluster.size();
        return new Location(sample.getWorld(), totalX / count, totalY / count, totalZ / count);
    }

    private StabilityFailureMode determineCollapseMode(List<Block> cluster, StabilityMaterialClass materialClass) {
        Map<StabilityFailureMode, Integer> counts = new EnumMap<>(StabilityFailureMode.class);
        for (Block block : cluster) {
            StabilityFailureMode mode = getStabilityFailureMode(block, materialClass);
            if (mode != null) {
                counts.merge(mode, 1, Integer::sum);
            }
        }
        StabilityFailureMode best = StabilityFailureMode.ROOF;
        int bestCount = -1;
        for (Map.Entry<StabilityFailureMode, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > bestCount) {
                best = entry.getKey();
                bestCount = entry.getValue();
            }
        }
        return best;
    }

    private void warnPlayersAboutStability(Location center, StabilityMaterialClass materialClass, StabilityFailureMode failureMode) {
        double radiusSquared = getStabilityWarningRadiusBlocks() * getStabilityWarningRadiusBlocks();
        for (Player player : center.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(center) > radiusSquared) {
                continue;
            }
            String message = switch (failureMode) {
                case FLOATING -> "&6Loose ground starts to shear away.";
                case SHAFT -> "&6The shaft wall starts to crumble inward.";
                case ROOF -> materialClass == StabilityMaterialClass.LOOSE ? "&6The ground feels unstable." : "&6The roof starts to crack.";
            };
            player.sendActionBar(legacyComponent(message));
            player.playSound(center, failureMode == StabilityFailureMode.SHAFT ? Sound.BLOCK_SAND_HIT : Sound.BLOCK_GRAVEL_HIT, 0.7F, materialClass == StabilityMaterialClass.LOOSE ? 0.8F : 0.6F);
        }
    }

    private void tickStabilityWarning(UUID collapseId) {
        StabilityCollapse collapse = pendingStabilityCollapses.get(collapseId);
        if (collapse == null) {
            return;
        }
        World world = getServer().getWorld(collapse.worldId);
        if (world == null) {
            clearPendingStabilityCollapse(collapseId);
            return;
        }
        int samples = Math.min(8, collapse.blockKeys.size());
        for (int i = 0; i < samples; i++) {
            PlacedBlockKey key = collapse.blockKeys.get(ThreadLocalRandom.current().nextInt(collapse.blockKeys.size()));
            Block block = world.getBlockAt(key.x, key.y, key.z);
            BlockData data = block.getBlockData();
            Location particleLocation = block.getLocation().add(0.5D, 0.85D, 0.5D);
            world.spawnParticle(Particle.BLOCK, particleLocation, 6, 0.18D, 0.08D, 0.18D, data);
        }
        world.spawnParticle(collapse.failureMode == StabilityFailureMode.SHAFT ? Particle.CLOUD : Particle.DUST_PLUME, collapse.center, 3, 0.6D, 0.2D, 0.6D, 0.01D);
        world.playSound(collapse.center, Sound.BLOCK_STONE_HIT, 0.45F, 0.55F);
    }

    private void beginStabilityCollapse(UUID collapseId) {
        StabilityCollapse collapse = pendingStabilityCollapses.get(collapseId);
        if (collapse == null) {
            return;
        }
        if (collapse.warningTask != null) {
            collapse.warningTask.cancel();
            collapse.warningTask = null;
        }

        World world = getServer().getWorld(collapse.worldId);
        if (world == null) {
            clearPendingStabilityCollapse(collapseId);
            return;
        }

        List<PlacedBlockKey> validated = new ArrayList<>();
        for (PlacedBlockKey key : collapse.blockKeys) {
            Block block = world.getBlockAt(key.x, key.y, key.z);
            if (shouldCollapseBlock(block, collapse.materialClass)) {
                validated.add(key);
            } else {
                pendingStabilityBlockKeys.remove(key);
            }
        }

        if (validated.size() < getEffectiveMinCollapseSize(blockKeysToBlocks(world, validated), collapse.materialClass)) {
            clearPendingStabilityCollapse(collapseId);
            return;
        }

        validated.sort((a, b) -> Integer.compare(a.y, b.y));
        collapse.blockKeys = validated;
        collapse.currentIndex = 0;
        collapse.spawnedFallingBlocks = 0;
        collapse.spawnedRubbleBlocks = 0;
        collapse.collapseTask = getServer().getScheduler().runTaskTimer(this, () -> tickStabilityCollapse(collapseId), 0L, getStabilityBatchIntervalTicks());
    }

    private void tickStabilityCollapse(UUID collapseId) {
        StabilityCollapse collapse = pendingStabilityCollapses.get(collapseId);
        if (collapse == null) {
            return;
        }
        World world = getServer().getWorld(collapse.worldId);
        if (world == null) {
            clearPendingStabilityCollapse(collapseId);
            return;
        }

        int batchSize = getStabilityCollapseBatchSize();
        for (int i = 0; i < batchSize && collapse.currentIndex < collapse.blockKeys.size(); i++) {
            PlacedBlockKey key = collapse.blockKeys.get(collapse.currentIndex++);
            pendingStabilityBlockKeys.remove(key);
            Block block = world.getBlockAt(key.x, key.y, key.z);
            if (!shouldCollapseBlock(block, collapse.materialClass)) {
                continue;
            }
            collapseStabilityBlock(block, collapse);
        }

        if (collapse.currentIndex >= collapse.blockKeys.size()) {
            queueStabilityScan(collapse.center);
            clearPendingStabilityCollapse(collapseId);
        }
    }

    private void collapseStabilityBlock(Block block, StabilityCollapse collapse) {
        World world = block.getWorld();
        Location blockOrigin = block.getLocation();
        Location location = blockOrigin.clone().add(0.5D, 0.5D, 0.5D);
        BlockData blockData = block.getBlockData().clone();
        block.setType(Material.AIR, false);
        world.spawnParticle(Particle.BLOCK, location, 10, 0.2D, 0.2D, 0.2D, blockData);
        world.playSound(location, collapse.materialClass == StabilityMaterialClass.LOOSE ? Sound.BLOCK_GRAVEL_BREAK : Sound.BLOCK_STONE_BREAK, 0.7F, 0.85F);
        maybePlaceStabilityRubble(block, collapse);
        queueStabilityScansAround(blockOrigin, 1, 2);
        if (collapse.spawnedFallingBlocks >= getMaxFallingBlocksPerCollapse()) {
            return;
        }
        FallingBlock fallingBlock = world.spawnFallingBlock(location, blockData);
        fallingBlock.setDropItem(false);
        fallingBlock.setHurtEntities(true);
        fallingBlock.setVelocity(getStabilityCollapseVelocity(block, collapse.failureMode));
        collapse.spawnedFallingBlocks++;
    }

    private Vector getStabilityCollapseVelocity(Block block, StabilityFailureMode failureMode) {
        double east = countOpenBlocks(block.getRelative(BlockFace.DOWN), BlockFace.EAST);
        double west = countOpenBlocks(block.getRelative(BlockFace.DOWN), BlockFace.WEST);
        double south = countOpenBlocks(block.getRelative(BlockFace.DOWN), BlockFace.SOUTH);
        double north = countOpenBlocks(block.getRelative(BlockFace.DOWN), BlockFace.NORTH);
        double speed = getStabilitySidewaysFallSpeed();

        double x = 0.0D;
        if (Math.abs(east - west) > 0.0D) {
            x = Math.signum(east - west) * speed;
        }

        double z = 0.0D;
        if (Math.abs(south - north) > 0.0D) {
            z = Math.signum(south - north) * speed;
        }

        if (x == 0.0D && z == 0.0D) {
            BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
            BlockFace chosen = faces[ThreadLocalRandom.current().nextInt(faces.length)];
            x = chosen.getModX() * speed;
            z = chosen.getModZ() * speed;
        }
        if (failureMode == StabilityFailureMode.ROOF) {
            x *= 0.7D;
            z *= 0.7D;
        } else if (failureMode == StabilityFailureMode.SHAFT) {
            x *= 1.2D;
            z *= 1.2D;
        }
        return new Vector(x, -0.04D, z);
    }

    private void maybePlaceStabilityRubble(Block origin, StabilityCollapse collapse) {
        if (!isStabilityRubbleEnabled() || collapse.spawnedRubbleBlocks >= getStabilityMaxRubblePerCollapse()) {
            return;
        }
        if (ThreadLocalRandom.current().nextDouble() > getStabilityRubbleChance()) {
            return;
        }
        Block landing = findRubbleLandingBlock(origin);
        if (landing == null || !landing.isEmpty()) {
            return;
        }
        Material rubbleMaterial = switch (collapse.materialClass) {
            case LOOSE, PACKED_SOIL -> Material.GRAVEL;
            case SOFT_ROCK, FRAGILE -> Material.COBBLESTONE;
            case HARD_ROCK, STABLE -> Material.COBBLED_DEEPSLATE;
        };
        landing.setType(rubbleMaterial, false);
        collapse.spawnedRubbleBlocks++;
    }

    private Block findRubbleLandingBlock(Block origin) {
        World world = origin.getWorld();
        for (int y = origin.getY() - 1; y >= world.getMinHeight(); y--) {
            Block candidate = world.getBlockAt(origin.getX(), y, origin.getZ());
            if (!candidate.isEmpty()) {
                Block above = candidate.getRelative(BlockFace.UP);
                return above.isEmpty() ? above : null;
            }
        }
        return null;
    }

    private void clearPendingStabilityCollapse(UUID collapseId) {
        StabilityCollapse collapse = pendingStabilityCollapses.remove(collapseId);
        if (collapse == null) {
            return;
        }
        if (collapse.warningTask != null) {
            collapse.warningTask.cancel();
        }
        if (collapse.executeTask != null) {
            collapse.executeTask.cancel();
        }
        if (collapse.collapseTask != null) {
            collapse.collapseTask.cancel();
        }
        for (PlacedBlockKey key : collapse.blockKeys) {
            pendingStabilityBlockKeys.remove(key);
        }
    }

    private void clearAllPendingStabilityCollapses() {
        for (UUID collapseId : new ArrayList<>(pendingStabilityCollapses.keySet())) {
            clearPendingStabilityCollapse(collapseId);
        }
    }

    private void restartStabilityDebugRuntime() {
        stopStabilityDebugRuntime();
        if (!isStabilityDebugRuntimeEnabled()) {
            return;
        }
        stabilityDebugTask = getServer().getScheduler().runTaskTimer(this, this::tickStabilityDebugRuntime, getStabilityDebugIntervalTicks(), getStabilityDebugIntervalTicks());
    }

    private void stopStabilityDebugRuntime() {
        if (stabilityDebugTask != null) {
            stabilityDebugTask.cancel();
            stabilityDebugTask = null;
        }
        for (UUID playerId : new ArrayList<>(stabilityDebugDisplays.keySet())) {
            clearStabilityDebugDisplays(playerId);
        }
    }

    private void tickStabilityDebugRuntime() {
        if (!isStabilityEnabled()) {
            return;
        }
        long now = System.currentTimeMillis();
        temporaryStabilityDebugExpiries.entrySet().removeIf(entry -> {
            if (entry.getValue() > now) {
                return false;
            }
            if (!stabilityDebugPlayers.contains(entry.getKey())) {
                clearStabilityDebugDisplays(entry.getKey());
            }
            return true;
        });
        Set<UUID> activePlayers = new LinkedHashSet<>(stabilityDebugPlayers);
        activePlayers.addAll(temporaryStabilityDebugExpiries.keySet());
        if (activePlayers.isEmpty()) {
            return;
        }
        double radius = getStabilityDebugViewRadiusBlocks();
        int scanRadius = Math.max(2, (int) Math.ceil(radius));
        double radiusSquared = radius * radius;
        for (UUID playerId : activePlayers) {
            Player player = getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                stabilityDebugPlayers.remove(playerId);
                temporaryStabilityDebugExpiries.remove(playerId);
                clearStabilityDebugDisplays(playerId);
                continue;
            }
            refreshStabilityDebugDisplays(player, scanRadius, radiusSquared);
        }
    }

    private void refreshStabilityDebugDisplays(Player player) {
        if (player == null) {
            return;
        }
        double radius = getStabilityDebugViewRadiusBlocks();
        int scanRadius = Math.max(2, (int) Math.ceil(radius));
        refreshStabilityDebugDisplays(player, scanRadius, radius * radius);
    }

    private void refreshStabilityDebugDisplays(Player player, int scanRadius, double radiusSquared) {
        if (player == null || !isStabilityDebugEnabled(player)) {
            return;
        }
        Location origin = player.getLocation();
        World world = origin.getWorld();
        if (world == null) {
            clearStabilityDebugDisplays(player.getUniqueId());
            return;
        }

        Map<PlacedBlockKey, StabilityDebugDisplayState> activeDisplays = stabilityDebugDisplays.computeIfAbsent(
                player.getUniqueId(),
                ignored -> new ConcurrentHashMap<>()
        );
        Set<PlacedBlockKey> desiredKeys = new LinkedHashSet<>();

        for (int x = origin.getBlockX() - scanRadius; x <= origin.getBlockX() + scanRadius; x++) {
            for (int z = origin.getBlockZ() - scanRadius; z <= origin.getBlockZ() + scanRadius; z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                for (int y = Math.max(world.getMinHeight(), origin.getBlockY() - scanRadius);
                     y <= Math.min(world.getMaxHeight() - 1, origin.getBlockY() + scanRadius);
                     y++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getLocation().distanceSquared(origin) > radiusSquared) {
                        continue;
                    }
                    StabilityDebugType debugType = getStabilityDebugType(block);
                    if (debugType == null) {
                        continue;
                    }

                    PlacedBlockKey key = PlacedBlockKey.from(block);
                    desiredKeys.add(key);
                    StabilityDebugDisplayState existing = activeDisplays.get(key);
                    if (existing == null || existing.type != debugType) {
                        if (existing != null) {
                            removeOreVisionDisplay(existing.entityId);
                        }
                        activeDisplays.put(key, spawnStabilityDebugDisplay(player, block, debugType));
                    }
                }
            }
        }

        activeDisplays.entrySet().removeIf(entry -> {
            if (desiredKeys.contains(entry.getKey())) {
                return false;
            }
            removeOreVisionDisplay(entry.getValue().entityId);
            return true;
        });
    }

    private StabilityDebugType getStabilityDebugType(Block block) {
        if (block == null || block.isEmpty()) {
            return null;
        }
        StabilityMaterialClass materialClass = getStabilityMaterialClass(block.getType());
        StabilityFailureMode mode = getStabilityFailureMode(block, materialClass);
        if (mode != null) {
            return switch (mode) {
                case FLOATING -> StabilityDebugType.FAIL_FLOATING;
                case SHAFT -> StabilityDebugType.FAIL_SHAFT;
                case ROOF -> StabilityDebugType.FAIL_ROOF;
            };
        }
        if (isStabilityDebugSupportBlock(block)) {
            return StabilityDebugType.SUPPORT;
        }
        if (isStabilityDebugSupportedBlock(block)) {
            return StabilityDebugType.SUPPORTED;
        }
        return null;
    }

    private boolean isStabilityDebugSupportBlock(Block block) {
        return block != null && isStructuralSupportMaterial(block.getType());
    }

    private boolean isStabilityDebugSupportedBlock(Block block) {
        if (block == null || block.isEmpty() || isStabilityDebugSupportBlock(block) || !isStructureMaterial(block.getType())) {
            return false;
        }
        return hasNearbyStructuralSupport(block)
                || hasSupportFrame(block)
                || hasTimberedMineSupport(block)
                || hasNearbyStableAnchor(block);
    }

    private StabilityDebugDisplayState spawnStabilityDebugDisplay(Player viewer, Block block, StabilityDebugType type) {
        Location location = block.getLocation();
        BlockDisplay display = (BlockDisplay) location.getWorld().spawn(location, BlockDisplay.class, spawned -> {
            spawned.setBlock(getStabilityDebugMarkerMaterial(type).createBlockData());
            spawned.setGlowing(true);
            spawned.setGlowColorOverride(getStabilityDebugColor(type));
            spawned.setGravity(false);
            spawned.setInvulnerable(true);
            spawned.setPersistent(false);
            spawned.setVisibleByDefault(false);
            spawned.setShadowRadius(0.0F);
            spawned.setShadowStrength(0.0F);
            spawned.setBrightness(new Display.Brightness(15, 15));
            spawned.setDisplayWidth(0.34F);
            spawned.setDisplayHeight(0.34F);
            spawned.setViewRange(Math.max(24.0F, (float) getStabilityDebugViewRadiusBlocks() + 6.0F));
            spawned.setTransformation(new Transformation(
                    new Vector3f(0.33F, 0.33F, 0.33F),
                    new Quaternionf(),
                    new Vector3f(0.34F, 0.34F, 0.34F),
                    new Quaternionf()
            ));
        });
        viewer.showEntity(this, display);
        return new StabilityDebugDisplayState(display.getUniqueId(), type);
    }

    private void clearStabilityDebugDisplays(UUID playerId) {
        Map<PlacedBlockKey, StabilityDebugDisplayState> displays = stabilityDebugDisplays.remove(playerId);
        if (displays == null) {
            return;
        }
        for (StabilityDebugDisplayState state : displays.values()) {
            removeOreVisionDisplay(state.entityId);
        }
    }

    private Color getStabilityDebugColor(StabilityDebugType type) {
        if (type == null) {
            return Color.fromRGB(255, 70, 70);
        }
        return switch (type) {
            case FAIL_FLOATING -> Color.fromRGB(255, 200, 80);
            case FAIL_SHAFT -> Color.fromRGB(255, 120, 70);
            case FAIL_ROOF -> Color.fromRGB(255, 70, 70);
            case SUPPORT -> Color.fromRGB(80, 170, 255);
            case SUPPORTED -> Color.fromRGB(80, 255, 140);
        };
    }

    private Material getStabilityDebugMarkerMaterial(StabilityDebugType type) {
        if (type == null) {
            return Material.RED_CONCRETE;
        }
        return switch (type) {
            case FAIL_FLOATING -> Material.YELLOW_CONCRETE;
            case FAIL_SHAFT -> Material.ORANGE_CONCRETE;
            case FAIL_ROOF -> Material.RED_CONCRETE;
            case SUPPORT -> Material.BLUE_CONCRETE;
            case SUPPORTED -> Material.LIME_CONCRETE;
        };
    }

    public void handleStabilityChunkUnload(org.bukkit.Chunk chunk) {
        if (chunk == null || pendingStabilityCollapses.isEmpty()) {
            return;
        }
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        String chunkKey = chunk.getWorld().getUID() + ":" + chunkX + ":" + chunkZ;
        for (StabilityCollapse collapse : new ArrayList<>(pendingStabilityCollapses.values())) {
            for (PlacedBlockKey key : collapse.blockKeys) {
                if (!key.worldId.equals(chunk.getWorld().getUID())) {
                    continue;
                }
                if ((key.x >> 4) == chunkX && (key.z >> 4) == chunkZ) {
                    deferredStabilityRescans.put(chunkKey, collapse.center);
                    clearPendingStabilityCollapse(collapse.collapseId);
                    break;
                }
            }
        }
    }

    public void handleStabilityChunkLoad(org.bukkit.Chunk chunk) {
        if (chunk == null) {
            return;
        }
        String chunkKey = chunk.getWorld().getUID() + ":" + chunk.getX() + ":" + chunk.getZ();
        Location rescan = deferredStabilityRescans.remove(chunkKey);
        if (rescan != null) {
            queueStabilityScan(rescan);
        }
    }

    public double getClimateAltitudeTemperaturePerBlock() {
        return Math.max(0.0D, getConfig().getDouble("climate.altitude.temperature-per-block-c", 0.08D));
    }

    public double getClimateAltitudeGrowthPenaltyPerBlock() {
        return Math.max(0.0D, getConfig().getDouble("climate.altitude.growth-penalty-per-block", 0.012D));
    }

    public void recordClimateRainEnded(World world) {
        if (world == null) {
            return;
        }
        climateRecentRainEndMillis.put(world.getUID(), System.currentTimeMillis());
    }

    public boolean hasClimateRecentlyRained(World world) {
        if (world == null) {
            return false;
        }
        Long endedAt = climateRecentRainEndMillis.get(world.getUID());
        if (endedAt == null || endedAt <= 0L) {
            return false;
        }
        long windowMillis = Math.max(0L, getConfig().getLong("climate.rain.recent-duration-seconds", 900L)) * 1000L;
        return System.currentTimeMillis() - endedAt <= windowMillis;
    }

    public double getClimateRainTemperatureOffset(Location location) {
        if (location == null || location.getWorld() == null) {
            return 0.0D;
        }
        return location.getWorld().hasStorm() ? -Math.max(0.0D, getConfig().getDouble("climate.rain.temperature-drop-c", 2.5D)) : 0.0D;
    }

    public double getClimateRainGrowthBonus(Location location) {
        if (location == null || location.getWorld() == null) {
            return 0.0D;
        }
        return hasClimateRecentlyRained(location.getWorld())
                ? Math.max(0.0D, getConfig().getDouble("climate.rain.recent-growth-bonus", 0.20D))
                : 0.0D;
    }

    public ClimateSnapshot getClimate(Location location) {
        if (!isClimateEnabled() || location == null || location.getWorld() == null) {
            return new ClimateSnapshot(20.0D, 1.0D, ClimateSeason.SPRING, "Temperate", false, false, false, 1.0D, 0.0D, 0.0D,
                    0.5D, 0.5D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        ClimateSeason season = getCurrentClimateSeason();
        double polarFactor = getClimateLatitudeFactor(location);
        double heatFactor = Math.max(0.0D, 1.0D - polarFactor);
        double humidity = getClimateHumidity(location);
        double continentality = getClimateContinentality(location);
        double currentInfluence = getClimateCurrentInfluence(location);
        double biomeTemperatureOffset = getClimateBiomeTemperatureOffset(location);
        double biomeHumidity = getClimateBiomeHumidity(location);
        humidity = blendClimateHumidity(humidity, biomeHumidity);
        double seasonalShift = getClimateSeasonShift(season);
        double baseTemperature;
        double dayNightSwing;
        if (isClimatePlaytestModeEnabled()) {
            double hotBeltTemperature = getClimatePlaytestCenterTemperatureCelsius();
            double polarTemperature = getClimatePlaytestEdgeTemperatureCelsius();
            baseTemperature = polarTemperature + ((hotBeltTemperature - polarTemperature) * heatFactor) + (seasonalShift * (0.35D + (polarFactor * 0.60D)));
            dayNightSwing = 2.5D + (polarFactor * 3.0D);
        } else {
            double polarTemperature = -16.0D;
            double hotBeltTemperature = 34.0D;
            baseTemperature = polarTemperature + ((hotBeltTemperature - polarTemperature) * heatFactor) + (seasonalShift * (0.35D + (polarFactor * 0.85D)));
            dayNightSwing = 2.5D + (polarFactor * 4.5D);
        }
        double bandOffset = getClimateBandVariationOffset(location, heatFactor);
        double humidityOffset = getClimateHumidityTemperatureOffset(humidity, heatFactor);
        double continentalOffset = getClimateContinentalTemperatureOffset(continentality, heatFactor);
        double currentOffset = getClimateCurrentTemperatureOffset(currentInfluence, heatFactor, polarFactor);
        double altitudeOffset = getClimateAltitudeTemperatureOffset(location);
        double rainOffset = getClimateRainTemperatureOffset(location);
        double localOffset = getClimateLocalTemperatureOffset(location);
        double submergedOffset = getClimateSubmergedTemperatureOffset(location);
        double temperature = baseTemperature + bandOffset + humidityOffset + continentalOffset + currentOffset
                + altitudeOffset + rainOffset + biomeTemperatureOffset + localOffset + submergedOffset
                + (dayNightSwing * getClimateTimeFactor(location.getWorld()));
        double altitudeGrowthMultiplier = getClimateAltitudeGrowthMultiplier(location);
        boolean raining = location.getWorld().hasStorm();
        boolean recentlyRained = hasClimateRecentlyRained(location.getWorld());
        double rainGrowthBonus = getClimateRainGrowthBonus(location);
        double growthMultiplier = Math.max(0.08D, Math.min(3.0D, (getClimateGrowthMultiplier(temperature) * altitudeGrowthMultiplier) + rainGrowthBonus));
        String climateName = describeClimate(temperature, humidity, continentality);
        boolean freezing = temperature <= -1.0D;
        return new ClimateSnapshot(temperature, growthMultiplier, season, climateName, freezing, raining, recentlyRained,
                altitudeGrowthMultiplier, bandOffset, altitudeOffset,
                humidity, continentality, currentInfluence, rainGrowthBonus, rainOffset,
                humidityOffset, continentalOffset, currentOffset, localOffset, submergedOffset);
    }

    private double getClimateBiomeTemperatureOffset(Location location) {
        if (location == null || location.getWorld() == null || !getConfig().getBoolean("climate.biome-adaptation.enabled", true)) {
            return 0.0D;
        }
        double normalizedBiomeTemperature = getLocalBiomeTemperature(location);
        double baseline = Math.max(0.0D, Math.min(2.0D, getConfig().getDouble("climate.biome-adaptation.temperature-baseline", 0.8D)));
        double strength = Math.max(0.0D, getConfig().getDouble("climate.biome-adaptation.temperature-strength-c", 12.0D));
        return (normalizedBiomeTemperature - baseline) * strength;
    }

    private double getClimateBiomeHumidity(Location location) {
        if (location == null || location.getWorld() == null || !getConfig().getBoolean("climate.biome-adaptation.enabled", true)) {
            return 0.5D;
        }
        return Math.max(0.0D, Math.min(1.0D, getLocalBiomeHumidity(location)));
    }

    private double blendClimateHumidity(double proceduralHumidity, double biomeHumidity) {
        double weight = Math.max(0.0D, Math.min(1.0D, getConfig().getDouble("climate.biome-adaptation.humidity-weight", 0.65D)));
        return Math.max(0.0D, Math.min(1.0D, (proceduralHumidity * (1.0D - weight)) + (biomeHumidity * weight)));
    }

    private double getLocalBiomeTemperature(Location location) {
        try {
            return Math.max(0.0D, Math.min(2.0D, location.getBlock().getTemperature()));
        } catch (Exception ignored) {
            return 0.8D;
        }
    }

    private double getLocalBiomeHumidity(Location location) {
        try {
            return Math.max(0.0D, Math.min(1.0D, location.getBlock().getHumidity()));
        } catch (Exception ignored) {
            return 0.5D;
        }
    }

    public boolean shouldClimateManageCrop(Material material) {
        return material != null && (isFarmerCrop(material)
                || material == Material.NETHER_WART
                || material == Material.SWEET_BERRY_BUSH
                || material == Material.SUGAR_CANE
                || material == Material.CACTUS
                || material == Material.COCOA
                || isClimateSapling(material));
    }

    public CropClimateProfile getClimateCropProfile(Material material) {
        if (material == null) {
            return null;
        }
        for (CropClimateProfile profile : getClimateCropProfiles()) {
            if (profile.matches(material)) {
                return profile;
            }
        }
        return null;
    }

    public CropClimateProfile getClimateCropProfile(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        for (CropClimateProfile profile : getClimateCropProfiles()) {
            if (profile.key().equalsIgnoreCase(key)) {
                return profile;
            }
        }
        return null;
    }

    public List<CropClimateProfile> getClimateCropProfiles() {
        return List.of(
                new CropClimateProfile("wheat", "Wheat", Material.WHEAT, ClimateSeason.SPRING, 16.0D, 24.0D, Set.of(Material.WHEAT, Material.WHEAT_SEEDS)),
                new CropClimateProfile("carrot", "Carrot", Material.CARROT, ClimateSeason.SPRING, 15.0D, 23.0D, Set.of(Material.CARROT, Material.CARROTS)),
                new CropClimateProfile("potato", "Potato", Material.POTATO, ClimateSeason.SPRING, 14.0D, 22.0D, Set.of(Material.POTATO, Material.POTATOES, Material.POISONOUS_POTATO)),
                new CropClimateProfile("beetroot", "Beetroot", Material.BEETROOT, ClimateSeason.AUTUMN, 12.0D, 20.0D, Set.of(Material.BEETROOT, Material.BEETROOT_SEEDS, Material.BEETROOTS)),
                new CropClimateProfile("melon", "Melon", Material.MELON_SLICE, ClimateSeason.SUMMER, 22.0D, 32.0D, Set.of(Material.MELON_SLICE, Material.MELON, Material.MELON_STEM, Material.ATTACHED_MELON_STEM)),
                new CropClimateProfile("pumpkin", "Pumpkin", Material.PUMPKIN, ClimateSeason.AUTUMN, 18.0D, 28.0D, Set.of(Material.PUMPKIN, Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM, Material.ATTACHED_PUMPKIN_STEM, Material.CARVED_PUMPKIN)),
                new CropClimateProfile("sweet_berries", "Sweet Berries", Material.SWEET_BERRIES, ClimateSeason.SUMMER, 18.0D, 26.0D, Set.of(Material.SWEET_BERRIES, Material.SWEET_BERRY_BUSH)),
                new CropClimateProfile("cocoa", "Cocoa", Material.COCOA_BEANS, ClimateSeason.SUMMER, 24.0D, 34.0D, Set.of(Material.COCOA_BEANS, Material.COCOA)),
                new CropClimateProfile("sugar_cane", "Sugar Cane", Material.SUGAR_CANE, ClimateSeason.SUMMER, 21.0D, 31.0D, Set.of(Material.SUGAR_CANE)),
                new CropClimateProfile("cactus", "Cactus", Material.CACTUS, ClimateSeason.SUMMER, 26.0D, 38.0D, Set.of(Material.CACTUS)),
                new CropClimateProfile("nether_wart", "Nether Wart", Material.NETHER_WART, ClimateSeason.AUTUMN, 20.0D, 30.0D, Set.of(Material.NETHER_WART)),
                new CropClimateProfile("oak_sapling", "Oak Sapling", Material.OAK_SAPLING, ClimateSeason.SPRING, 12.0D, 22.0D, Set.of(Material.OAK_SAPLING)),
                new CropClimateProfile("spruce_sapling", "Spruce Sapling", Material.SPRUCE_SAPLING, ClimateSeason.WINTER, 4.0D, 16.0D, Set.of(Material.SPRUCE_SAPLING)),
                new CropClimateProfile("birch_sapling", "Birch Sapling", Material.BIRCH_SAPLING, ClimateSeason.SPRING, 10.0D, 20.0D, Set.of(Material.BIRCH_SAPLING)),
                new CropClimateProfile("jungle_sapling", "Jungle Sapling", Material.JUNGLE_SAPLING, ClimateSeason.SUMMER, 24.0D, 34.0D, Set.of(Material.JUNGLE_SAPLING)),
                new CropClimateProfile("acacia_sapling", "Acacia Sapling", Material.ACACIA_SAPLING, ClimateSeason.SUMMER, 22.0D, 34.0D, Set.of(Material.ACACIA_SAPLING)),
                new CropClimateProfile("dark_oak_sapling", "Dark Oak Sapling", Material.DARK_OAK_SAPLING, ClimateSeason.AUTUMN, 14.0D, 24.0D, Set.of(Material.DARK_OAK_SAPLING)),
                new CropClimateProfile("cherry_sapling", "Cherry Sapling", Material.CHERRY_SAPLING, ClimateSeason.SPRING, 10.0D, 18.0D, Set.of(Material.CHERRY_SAPLING)),
                new CropClimateProfile("mangrove_propagule", "Mangrove Propagule", Material.MANGROVE_PROPAGULE, ClimateSeason.SUMMER, 24.0D, 34.0D, Set.of(Material.MANGROVE_PROPAGULE))
        );
    }

    private boolean isClimateSapling(Material material) {
        return material == Material.OAK_SAPLING
                || material == Material.SPRUCE_SAPLING
                || material == Material.BIRCH_SAPLING
                || material == Material.JUNGLE_SAPLING
                || material == Material.ACACIA_SAPLING
                || material == Material.DARK_OAK_SAPLING
                || material == Material.CHERRY_SAPLING
                || material == Material.MANGROVE_PROPAGULE;
    }

    public double getClimateGrowthMultiplier(Material material, Location location, ClimateSnapshot climate) {
        if (material == null || climate == null) {
            return 1.0D;
        }
        CropClimateProfile profile = getClimateCropProfile(material);
        if (profile == null) {
            return climate.growthMultiplier();
        }

        double temperature = climate.temperatureCelsius();
        double distanceToRange = 0.0D;
        if (temperature < profile.optimalMinCelsius()) {
            distanceToRange = profile.optimalMinCelsius() - temperature;
        } else if (temperature > profile.optimalMaxCelsius()) {
            distanceToRange = temperature - profile.optimalMaxCelsius();
        }

        double multiplier;
        if (distanceToRange <= 0.0D) {
            multiplier = 2.35D;
        } else if (distanceToRange <= 4.0D) {
            multiplier = 1.55D;
        } else if (distanceToRange <= 8.0D) {
            multiplier = 1.0D;
        } else if (distanceToRange <= 14.0D) {
            multiplier = 0.5D;
        } else {
            multiplier = 0.18D;
        }

        if (climate.season() == profile.bestSeason()) {
            multiplier += 0.35D;
        } else if (isOppositeSeason(climate.season(), profile.bestSeason())) {
            multiplier -= 0.20D;
        }
        multiplier *= getClimateAltitudeGrowthMultiplier(location);
        multiplier += getClimateRainGrowthBonus(location);
        return Math.max(0.08D, Math.min(3.0D, multiplier));
    }

    private boolean isOppositeSeason(ClimateSeason left, ClimateSeason right) {
        return (left == ClimateSeason.SPRING && right == ClimateSeason.AUTUMN)
                || (left == ClimateSeason.AUTUMN && right == ClimateSeason.SPRING)
                || (left == ClimateSeason.SUMMER && right == ClimateSeason.WINTER)
                || (left == ClimateSeason.WINTER && right == ClimateSeason.SUMMER);
    }

    public ItemStack applyClimateCropLore(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return itemStack;
        }
        CropClimateProfile profile = getClimateCropProfile(itemStack.getType());
        if (profile == null) {
            return itemStack;
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }
        boolean hasDisplayName = meta.hasDisplayName();
        boolean hasLore = meta.hasLore() && meta.lore() != null && !meta.lore().isEmpty();
        if (meta.getPersistentDataContainer().has(climateCropLoreKey, PersistentDataType.STRING) && hasDisplayName && hasLore) {
            return itemStack;
        }

        List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        if (!lore.isEmpty()) {
            lore.add(legacyComponent("&8"));
        }
        if (!meta.hasDisplayName()) {
            meta.displayName(legacyComponent("&a" + formatMaterialName(itemStack.getType())));
        }
        lore.add(legacyComponent("&6Climate Guide"));
        lore.add(legacyComponent("&7Best Season: &f" + profile.bestSeason().getDisplayName()));
        lore.add(legacyComponent("&7Best Temp: &f" + formatTemperature(profile.optimalMinCelsius()) + " &7to &f" + formatTemperature(profile.optimalMaxCelsius())));
        lore.add(legacyComponent("&7Best Altitude: &fY " + String.format(Locale.US, "%.0f", getClimateOptimalAltitudeY())));
        lore.add(legacyComponent("&7Good climate speeds growth."));
        lore.add(legacyComponent("&7Bad climate slows growth hard."));
        meta.lore(lore);
        meta.getPersistentDataContainer().set(climateCropLoreKey, PersistentDataType.STRING, profile.key());
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public ItemStack createClimateCropGuideItem(CropClimateProfile profile) {
        ItemStack item = new ItemStack(profile.displayMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.displayName(legacyComponent("&a" + profile.displayName()));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return applyClimateCropLore(item);
    }

    public ItemStack createClimateCropGuideItem(CropClimateProfile profile, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.displayName(legacyComponent("&a" + formatMaterialName(material)));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return applyClimateCropLore(item);
    }

    public void openClimateCropGuide(Player player) {
        if (player == null) {
            return;
        }
        Inventory inventory = Bukkit.createInventory(new ClimateGuiListener.ClimateCropGuideHolder(), 54, legacyComponent("&8Climate Crops"));
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(legacyComponent("&7"));
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
        int[] guideSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
        int index = 0;
        for (CropClimateProfile profile : getClimateCropProfiles()) {
            List<Material> relatedMaterials = new ArrayList<>(profile.relatedMaterials());
            relatedMaterials.sort((left, right) -> left.name().compareToIgnoreCase(right.name()));
            for (Material material : relatedMaterials) {
                if (index >= guideSlots.length) {
                    break;
                }
                inventory.setItem(guideSlots[index++], createClimateCropGuideItem(profile, material));
            }
            if (index >= guideSlots.length) {
                break;
            }
        }
        player.openInventory(inventory);
    }

    public ItemStack refreshDescriptiveItemDetails(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return itemStack;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }

        boolean missingName = !meta.hasDisplayName();
        boolean missingLore = !meta.hasLore() || meta.lore() == null || meta.lore().isEmpty();
        if (!missingName && !missingLore) {
            return itemStack;
        }

        if (isSoulboundItem(itemStack)) {
            itemStack = applySoulboundTag(itemStack);
        }
        if (getClimateCropProfile(itemStack.getType()) != null) {
            itemStack = applyClimateCropLore(itemStack);
        }
        return itemStack;
    }

    public void refreshDescriptiveItemDetailsAfterReload() {
        for (Player player : getServer().getOnlinePlayers()) {
            refreshInventoryDescriptiveItems(player.getInventory());
            refreshInventoryDescriptiveItems(player.getEnderChest());
        }
        for (World world : getServer().getWorlds()) {
            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                for (BlockState state : chunk.getTileEntities()) {
                    if (state instanceof Container container) {
                        refreshInventoryDescriptiveItems(container.getInventory());
                    }
                }
            }
        }
    }

    private void refreshInventoryDescriptiveItems(Inventory inventory) {
        if (inventory == null) {
            return;
        }
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemStack = contents[i];
            if (itemStack == null || itemStack.getType().isAir()) {
                continue;
            }
            ItemStack refreshed = refreshDescriptiveItemDetails(itemStack);
            if (refreshed != itemStack) {
                contents[i] = refreshed;
            }
        }
        inventory.setContents(contents);
    }

    public void scheduleClimateExtraGrowth(Block block, int extraSteps) {
        if (block == null) {
            return;
        }
        Location location = block.getLocation();
        int safeSteps = Math.max(1, extraSteps);
        getServer().getScheduler().runTask(this, () -> advanceClimateCrop(location, safeSteps));
    }

    private void advanceClimateCrop(Location location, int steps) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        Block liveBlock = location.getBlock();
        if (!(liveBlock.getBlockData() instanceof Ageable ageable)) {
            return;
        }
        int targetAge = Math.min(ageable.getMaximumAge(), ageable.getAge() + Math.max(1, steps));
        if (targetAge <= ageable.getAge()) {
            return;
        }
        ageable.setAge(targetAge);
        liveBlock.setBlockData(ageable, false);
    }

    private ClimateSeason getCurrentClimateSeason() {
        ClimateSeason override = getClimateSeasonOverride();
        if (override != null) {
            return override;
        }
        LocalDate now = LocalDate.now(getClimateZoneId());
        int month = now.getMonthValue();
        return switch (month) {
            case 12, 1, 2 -> ClimateSeason.WINTER;
            case 3, 4, 5 -> ClimateSeason.SPRING;
            case 6, 7, 8 -> ClimateSeason.SUMMER;
            default -> ClimateSeason.AUTUMN;
        };
    }

    private ZoneId getClimateZoneId() {
        try {
            return ZoneId.of(getConfig().getString("realtime-clock.timezone", ZoneId.systemDefault().getId()));
        } catch (Exception ignored) {
            return ZoneId.systemDefault();
        }
    }

    private double getClimateSeasonShift(ClimateSeason season) {
        if (!getConfig().getBoolean("climate.seasons.enabled", true)) {
            return 0.0D;
        }
        return switch (season) {
            case SPRING -> 3.0D;
            case SUMMER -> 9.0D;
            case AUTUMN -> -1.0D;
            case WINTER -> -10.0D;
        };
    }

    private double getClimateLatitudeFactor(Location location) {
        ClimateDebugRegion debugRegion = getClimateDebugRegion(location);
        if (debugRegion != null) {
            return getDebugRegionPolarFactor(location, debugRegion);
        }
        if (isClimatePlaytestModeEnabled()) {
            return getClimatePlaytestDistanceFactor(location);
        }
        double climateMidpointZ = location.getWorld() != null ? location.getWorld().getSpawnLocation().getZ() : 0.0D;
        double currentCoordinate = location.getZ() + getClimateLatitudeWaveOffset(location, Math.max(1.0D, getConfig().getDouble("climate.latitude-scale-blocks", 750.0D)));
        double scale = Math.max(1.0D, getConfig().getDouble("climate.latitude-scale-blocks", 750.0D));
        return Math.max(0.0D, Math.min(1.0D, Math.abs(currentCoordinate - climateMidpointZ) / scale));
    }

    private double getClimatePlaytestDistanceFactor(Location location) {
        if (location == null) {
            return 0.0D;
        }
        double radius = getClimatePlaytestRadiusBlocks();
        double dz = Math.abs((location.getZ() + getClimateLatitudeWaveOffset(location, radius)) - getClimatePlaytestCenterZ());
        double normalized = dz / radius;
        return Math.max(0.0D, Math.min(1.0D, normalized));
    }

    private double getDebugRegionPolarFactor(Location location, ClimateDebugRegion region) {
        double latitude = region.normalizedLatitude(location);
        double warpedLatitude = latitude + getDebugRegionLatitudeWaveOffset(location, region);
        return Math.max(0.0D, Math.min(1.0D, Math.abs((warpedLatitude - 0.5D) * 2.0D)));
    }

    private double getClimateLatitudeWaveOffset(Location location, double scale) {
        if (location == null || location.getWorld() == null) {
            return 0.0D;
        }
        long seed = location.getWorld().getSeed();
        double longWave = sampleClimateRegionField(seed + 701L, location.getX(), location.getZ(), scale * 2.8D, 1) * scale * 0.22D;
        double regionalWave = sampleClimateRegionField(seed + 733L, location.getX(), location.getZ(), scale * 1.2D, 1) * scale * 0.08D;
        return longWave + regionalWave;
    }

    private double getDebugRegionLatitudeWaveOffset(Location location, ClimateDebugRegion region) {
        if (location == null || region == null) {
            return 0.0D;
        }
        World world = location.getWorld();
        if (world == null) {
            return 0.0D;
        }
        double height = Math.max(1.0D, region.maxZ() - region.minZ());
        double normalizedX = (location.getX() - region.minX()) / Math.max(1.0D, region.maxX() - region.minX());
        double broadWave = Math.sin((normalizedX * Math.PI * 2.4D) + (((world.getSeed() & 31L) / 31.0D) * Math.PI)) * 0.06D;
        double regionalWave = sampleClimateRegionField(world.getSeed() + 911L, location.getX(), location.getZ(), height * 0.22D, 1) * 0.11D;
        return broadWave + regionalWave;
    }

    public double getClimateBandVariationOffset(Location location, double heatFactor) {
        if (location == null || location.getWorld() == null) {
            return 0.0D;
        }
        double scale = Math.max(32.0D, getConfig().getDouble("climate.latitude-scale-blocks", 750.0D));
        long seed = location.getWorld().getSeed();
        double continental = sampleClimateRegionField(seed + 101L, location.getX(), location.getZ(), scale * 1.8D, 2);
        double regional = sampleClimateRegionField(seed + 211L, location.getX(), location.getZ(), scale * 0.75D, 2);
        double coastal = sampleClimateCurrentField(seed + 307L, location.getX(), location.getZ(), scale * 1.15D);
        double strength = 2.5D + (heatFactor * 2.5D);
        double blended = (continental * 0.55D) + (regional * 0.30D) + (coastal * 0.15D);
        return Math.tanh(blended * 1.2D) * strength;
    }

    public double getClimateHumidity(Location location) {
        if (location == null || location.getWorld() == null) {
            return 0.5D;
        }
        double scale = Math.max(32.0D, getConfig().getDouble("climate.latitude-scale-blocks", 750.0D));
        long seed = location.getWorld().getSeed();
        double humidMacro = sampleClimateRegionField(seed + 1201L, location.getX(), location.getZ(), scale * 1.6D, 2);
        double humidRegional = sampleClimateRegionField(seed + 1217L, location.getX(), location.getZ(), scale * 0.65D, 1);
        double humidCurrent = sampleClimateCurrentField(seed + 1231L, location.getX(), location.getZ(), scale * 0.95D);
        double normalized = 0.5D + ((humidMacro * 0.5D) + (humidRegional * 0.3D) + (humidCurrent * 0.2D)) * 0.5D;
        return Math.max(0.0D, Math.min(1.0D, normalized));
    }

    public double getClimateContinentality(Location location) {
        if (location == null || location.getWorld() == null) {
            return 0.5D;
        }
        double scale = Math.max(32.0D, getConfig().getDouble("climate.latitude-scale-blocks", 750.0D));
        long seed = location.getWorld().getSeed();
        double macro = sampleClimateRegionField(seed + 1301L, location.getX(), location.getZ(), scale * 2.3D, 2);
        double regional = sampleClimateRegionField(seed + 1319L, location.getX(), location.getZ(), scale * 0.9D, 1);
        double normalized = 0.5D + ((macro * 0.7D) + (regional * 0.3D)) * 0.5D;
        return Math.max(0.0D, Math.min(1.0D, normalized));
    }

    public double getClimateCurrentInfluence(Location location) {
        if (location == null || location.getWorld() == null) {
            return 0.0D;
        }
        double scale = Math.max(32.0D, getConfig().getDouble("climate.latitude-scale-blocks", 750.0D));
        long seed = location.getWorld().getSeed();
        return Math.max(-1.0D, Math.min(1.0D,
                sampleClimateCurrentField(seed + 1409L, location.getX(), location.getZ(), scale * 1.45D)));
    }

    private double getClimateHumidityTemperatureOffset(double humidity, double heatFactor) {
        double centeredHumidity = humidity - 0.5D;
        return centeredHumidity * (heatFactor >= 0.55D ? -5.0D : 3.5D);
    }

    private double getClimateContinentalTemperatureOffset(double continentality, double heatFactor) {
        double inlandFactor = continentality - 0.5D;
        return inlandFactor * ((heatFactor >= 0.5D) ? 5.5D : -4.0D);
    }

    private double getClimateCurrentTemperatureOffset(double currentInfluence, double heatFactor, double polarFactor) {
        double strength = 2.0D + (heatFactor * 2.5D) + (polarFactor * 1.5D);
        return currentInfluence * strength;
    }

    public double getClimateAltitudeTemperatureOffset(Location location) {
        if (location == null) {
            return 0.0D;
        }
        double optimalY = getClimateOptimalAltitudeY();
        double delta = optimalY - location.getY();
        double softenedDistance = getClimateSoftenedAltitudeDistance(location.getY(), location);
        double offset = Math.signum(delta) * softenedDistance * getClimateAltitudeTemperaturePerBlock();

        double highlandsStartY = getConfig().getDouble("climate.altitude.highlands-start-y", 160.0D);
        if (location.getY() > highlandsStartY) {
            offset -= (location.getY() - highlandsStartY)
                    * Math.max(0.0D, getConfig().getDouble("climate.altitude.highlands-temperature-per-block-c", 0.03D));
        }
        return offset;
    }

    public double getClimateAltitudeGrowthMultiplier(Location location) {
        if (location == null) {
            return 1.0D;
        }
        double distance = getClimateSoftenedAltitudeDistance(location.getY(), location);
        double penalty = distance * getClimateAltitudeGrowthPenaltyPerBlock();
        double highlandsStartY = getConfig().getDouble("climate.altitude.highlands-start-y", 160.0D);
        if (location.getY() > highlandsStartY) {
            penalty += (location.getY() - highlandsStartY)
                    * Math.max(0.0D, getConfig().getDouble("climate.altitude.highlands-growth-penalty-per-block", 0.004D));
        }
        return Math.max(0.18D, Math.min(1.0D, 1.0D - penalty));
    }

    private double getClimateSoftenedAltitudeDistance(double y, Location location) {
        double distance = Math.abs(y - getClimateOptimalAltitudeY());
        double softRange = Math.max(0.0D, getConfig().getDouble("climate.altitude.soft-range-blocks", 36.0D));
        double softenedDistance = Math.max(0.0D, distance - softRange);
        double coldBiomeThreshold = Math.max(0.0D, Math.min(2.0D,
                getConfig().getDouble("climate.altitude.cold-biome-temperature-threshold", 0.2D)));
        double coldBiomeMultiplier = Math.max(1.0D,
                getConfig().getDouble("climate.altitude.cold-biome-distance-multiplier", 1.35D));
        if (location != null && getLocalBiomeTemperature(location) <= coldBiomeThreshold) {
            softenedDistance *= coldBiomeMultiplier;
        }
        return softenedDistance;
    }

    private double getClimateLocalTemperatureOffset(Location location) {
        if (location == null || location.getWorld() == null || !getConfig().getBoolean("climate.local-effects.enabled", true)) {
            return 0.0D;
        }
        int radius = Math.max(1, getConfig().getInt("climate.local-effects.block-radius", 3));
        int verticalRadius = Math.max(0, getConfig().getInt("climate.local-effects.vertical-radius", 2));
        double heat = 0.0D;
        double cold = 0.0D;

        Location center = location.getBlock().getLocation().add(0.5D, 0.5D, 0.5D);
        World world = center.getWorld();
        if (world == null) {
            return 0.0D;
        }

        for (int x = -radius; x <= radius; x++) {
            for (int y = -verticalRadius; y <= verticalRadius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = world.getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);
                    double strength = getClimateBlockTemperatureStrength(block);
                    if (Math.abs(strength) < 0.0001D) {
                        continue;
                    }
                    double dx = x;
                    double dy = y;
                    double dz = z;
                    double distance = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
                    double weight = 1.0D / (1.0D + distance);
                    if (strength > 0.0D) {
                        heat += strength * weight;
                    } else {
                        cold += (-strength) * weight;
                    }
                }
            }
        }

        double maxHeat = Math.max(0.0D, getConfig().getDouble("climate.local-effects.max-heat-offset-c", 4.0D));
        double maxCold = Math.max(0.0D, getConfig().getDouble("climate.local-effects.max-cold-offset-c", 3.5D));
        return Math.min(maxHeat, heat) - Math.min(maxCold, cold);
    }

    private double getClimateSubmergedTemperatureOffset(Location location) {
        if (location == null || location.getWorld() == null || !getConfig().getBoolean("climate.local-effects.enabled", true)) {
            return 0.0D;
        }
        Block feet = location.getBlock();
        Block head = feet.getRelative(BlockFace.UP);
        boolean submerged = feet.isLiquid() || head.isLiquid() || feet.isPassable() && isWaterlogged(feet) || isWaterlogged(head);
        if (!submerged) {
            return 0.0D;
        }
        return Math.min(0.0D, getConfig().getDouble("climate.local-effects.submerged-temperature-offset-c", -2.5D));
    }

    private boolean isWaterlogged(Block block) {
        if (block == null) {
            return false;
        }
        if (!(block.getBlockData() instanceof org.bukkit.block.data.Waterlogged waterlogged)) {
            return false;
        }
        return waterlogged.isWaterlogged();
    }

    private double getClimateBlockTemperatureStrength(Block block) {
        if (block == null) {
            return 0.0D;
        }
        Material type = block.getType();
        return switch (type) {
            case LAVA -> 2.4D;
            case MAGMA_BLOCK -> 1.2D;
            case CAMPFIRE -> 1.0D;
            case SOUL_CAMPFIRE -> 0.75D;
            case FIRE -> 1.35D;
            case SOUL_FIRE -> 1.0D;
            case TORCH, WALL_TORCH -> 0.28D;
            case SOUL_TORCH, SOUL_WALL_TORCH -> 0.18D;
            case ICE -> -0.45D;
            case PACKED_ICE -> -0.8D;
            case BLUE_ICE -> -1.2D;
            case FROSTED_ICE -> -0.5D;
            case SNOW_BLOCK, POWDER_SNOW -> -0.25D;
            default -> getClimateLitBlockTemperatureStrength(block);
        };
    }

    private double getClimateLitBlockTemperatureStrength(Block block) {
        Material type = block.getType();
        if (type != Material.FURNACE && type != Material.BLAST_FURNACE && type != Material.SMOKER) {
            return 0.0D;
        }
        if (block.getBlockData() instanceof org.bukkit.block.data.Lightable lightable && lightable.isLit()) {
            return 0.85D;
        }
        return 0.0D;
    }

    private double sampleClimateRegionField(long seed, double worldX, double worldZ, double cellSize, int searchRadius) {
        double sampleX = worldX / Math.max(1.0D, cellSize);
        double sampleZ = worldZ / Math.max(1.0D, cellSize);
        int centerCellX = fastFloor(sampleX);
        int centerCellZ = fastFloor(sampleZ);
        double weightedValue = 0.0D;
        double totalWeight = 0.0D;

        for (int offsetX = -searchRadius; offsetX <= searchRadius; offsetX++) {
            for (int offsetZ = -searchRadius; offsetZ <= searchRadius; offsetZ++) {
                int cellX = centerCellX + offsetX;
                int cellZ = centerCellZ + offsetZ;
                double nodeX = cellX + (pseudoRandomUnit(seed + 11L, cellX, cellZ) * 0.7D) + 0.15D;
                double nodeZ = cellZ + (pseudoRandomUnit(seed + 29L, cellX, cellZ) * 0.7D) + 0.15D;
                double dx = sampleX - nodeX;
                double dz = sampleZ - nodeZ;
                double distance = Math.sqrt((dx * dx) + (dz * dz));
                if (distance > 2.35D) {
                    continue;
                }

                double weight = Math.pow(Math.max(0.0D, 1.0D - (distance / 2.35D)), 3.0D);
                double value = pseudoRandomSigned(seed + 73L, cellX, cellZ);
                weightedValue += value * weight;
                totalWeight += weight;
            }
        }

        if (totalWeight <= 0.0D) {
            return 0.0D;
        }
        return weightedValue / totalWeight;
    }

    private double sampleClimateCurrentField(long seed, double worldX, double worldZ, double scale) {
        double flowA = sampleClimateRegionField(seed + 7L, worldX, worldZ, scale, 1);
        double flowB = sampleClimateRegionField(seed + 19L, worldX + (flowA * scale * 0.55D), worldZ - (flowA * scale * 0.55D), scale * 0.65D, 1);
        return (flowA * 0.6D) + (flowB * 0.4D);
    }

    private double pseudoRandomUnit(long seed, int x, int z) {
        long hashed = hashClimate(seed, x, z);
        return (hashed & 0xFFFFFFL) / (double) 0x1000000L;
    }

    private double pseudoRandomSigned(long seed, int x, int z) {
        return (pseudoRandomUnit(seed, x, z) * 2.0D) - 1.0D;
    }

    private long hashClimate(long seed, int x, int z) {
        long value = seed;
        value ^= (long) x * 341873128712L;
        value ^= (long) z * 132897987541L;
        value = (value << 13) ^ value;
        return value * (value * value * 15731L + 789221L) + 1376312589L;
    }

    private int fastFloor(double value) {
        int floor = (int) value;
        return value < floor ? floor - 1 : floor;
    }

    private double getClimateEquatorCoordinate(World world) {
        String mode = getConfig().getString("climate.equator.mode", "spawn");
        if (mode != null && mode.equalsIgnoreCase("fixed")) {
            return getConfig().getDouble("climate.equator.coordinate", 0.0D);
        }
        return world != null ? world.getSpawnLocation().getZ() : 0.0D;
    }

    public double getResolvedClimateEquatorCoordinate(World world) {
        return getClimateEquatorCoordinate(world);
    }

    private double getClimateTimeFactor(World world) {
        if (world == null) {
            return 0.0D;
        }
        double angle = ((world.getTime() - 6000L) / 24000.0D) * (Math.PI * 2.0D);
        return Math.cos(angle);
    }

    private double getClimateGrowthMultiplier(double temperatureCelsius) {
        if (temperatureCelsius <= -2.0D) {
            return 0.15D;
        }
        if (temperatureCelsius <= 5.0D) {
            return 0.45D;
        }
        if (temperatureCelsius <= 12.0D) {
            return 0.80D;
        }
        if (temperatureCelsius <= 18.0D) {
            return 1.00D;
        }
        if (temperatureCelsius <= 28.0D) {
            return 1.75D;
        }
        if (temperatureCelsius <= 34.0D) {
            return 1.20D;
        }
        if (temperatureCelsius <= 40.0D) {
            return 0.70D;
        }
        return 0.35D;
    }

    private String describeClimate(double temperatureCelsius, double humidity, double continentality) {
        if (temperatureCelsius <= -16.0D) {
            return humidity >= 0.45D ? "Polar Ice" : "Frozen Wastes";
        }
        if (temperatureCelsius <= -6.0D) {
            return humidity >= 0.55D ? "Snow Belt" : "Cold Steppe";
        }
        if (temperatureCelsius <= 6.0D) {
            return humidity >= 0.55D ? "Boreal" : "Cool Plains";
        }
        if (temperatureCelsius <= 15.0D) {
            if (humidity >= 0.65D) {
                return "Maritime Temperate";
            }
            return continentality >= 0.6D ? "Temperate Interior" : "Temperate";
        }
        if (temperatureCelsius <= 24.0D) {
            if (humidity >= 0.68D) {
                return "Humid Subtropical";
            }
            return continentality >= 0.62D ? "Warm Interior" : "Mild Subtropical";
        }
        if (temperatureCelsius <= 31.0D) {
            if (humidity >= 0.7D) {
                return "Tropical Wet";
            }
            if (humidity >= 0.45D) {
                return "Tropical Seasonal";
            }
            return "Hot Savanna";
        }
        if (humidity <= 0.3D) {
            return continentality >= 0.6D ? "Scorching Desert" : "Hot Desert";
        }
        return humidity >= 0.65D ? "Equatorial" : "Arid Tropical";
    }

    public String formatTemperature(double temperatureCelsius) {
        String unit = getConfig().getString("climate.temperature-unit", "C");
        if (unit != null && unit.equalsIgnoreCase("F")) {
            double fahrenheit = (temperatureCelsius * 9.0D / 5.0D) + 32.0D;
            return String.format(Locale.US, "%.1fF", fahrenheit);
        }
        return String.format(Locale.US, "%.1fC", temperatureCelsius);
    }

    public Color getClimateDebugColor(double temperatureCelsius, boolean night) {
        int[][] gradient = {
                {28, 22, 58},
                {62, 54, 150},
                {82, 64, 186},
                {106, 74, 204},
                {60, 80, 212},
                {44, 116, 255},
                {72, 176, 255},
                {78, 234, 220},
                {82, 214, 134},
                {124, 236, 82},
                {224, 244, 82},
                {255, 204, 72},
                {255, 148, 56},
                {255, 98, 52},
                {222, 32, 48}
        };
        double[] stops = {-26.0D, -20.0D, -15.0D, -10.0D, -4.0D, 2.0D, 8.0D, 14.0D, 20.0D, 25.0D, 30.0D, 35.0D, 40.0D, 45.0D, 50.0D};

        int[] rgb;
        if (temperatureCelsius <= stops[0]) {
            rgb = gradient[0];
        } else if (temperatureCelsius >= stops[stops.length - 1]) {
            rgb = gradient[gradient.length - 1];
        } else {
            rgb = gradient[gradient.length - 1];
            for (int i = 1; i < stops.length; i++) {
                if (temperatureCelsius <= stops[i]) {
                    double amount = (temperatureCelsius - stops[i - 1]) / (stops[i] - stops[i - 1]);
                    rgb = interpolateColor(gradient[i - 1], gradient[i], amount);
                    break;
                }
            }
        }

        double brightness = night ? 0.68D : 1.0D;
        return Color.fromRGB(
                clampColor((int) Math.round(rgb[0] * brightness)),
                clampColor((int) Math.round(rgb[1] * brightness)),
                clampColor((int) Math.round(rgb[2] * brightness))
        );
    }

    private int[] interpolateColor(int[] left, int[] right, double amount) {
        return new int[] {
                (int) Math.round(left[0] + ((right[0] - left[0]) * amount)),
                (int) Math.round(left[1] + ((right[1] - left[1]) * amount)),
                (int) Math.round(left[2] + ((right[2] - left[2]) * amount))
        };
    }

    private int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private double getClimateDebugDayNightSwing(Location location) {
        if (location == null || location.getWorld() == null) {
            return 0.0D;
        }
        double latitudeFactor = getClimateLatitudeFactor(location);
        return isClimatePlaytestModeEnabled()
                ? 3.0D + (latitudeFactor * 2.5D)
                : 5.0D + (latitudeFactor * 8.0D);
    }

    public ClimateDebugRegion registerClimateDebugRegion(World world, int minX, int maxX, int minZ, int maxZ, int baseY) {
        if (world == null) {
            return null;
        }
        ClimateDebugRegion region = new ClimateDebugRegion(
                world.getUID(),
                Math.min(minX, maxX),
                Math.max(minX, maxX),
                Math.min(minZ, maxZ),
                Math.max(minZ, maxZ),
                baseY
        );
        List<ClimateDebugRegion> regions = climateDebugRegions.computeIfAbsent(world.getUID(), ignored -> new ArrayList<>());
        regions.removeIf(existing -> overlaps(existing, region));
        regions.add(region);
        return region;
    }

    public ClimateDebugRegion getClimateDebugRegion(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        List<ClimateDebugRegion> regions = climateDebugRegions.get(location.getWorld().getUID());
        if (regions == null || regions.isEmpty()) {
            return null;
        }
        for (int i = regions.size() - 1; i >= 0; i--) {
            ClimateDebugRegion region = regions.get(i);
            if (region.contains(location)) {
                return region;
            }
        }
        return null;
    }

    public ClimateDebugRegion removeClimateDebugRegion(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        List<ClimateDebugRegion> regions = climateDebugRegions.get(location.getWorld().getUID());
        if (regions == null || regions.isEmpty()) {
            return null;
        }
        for (int i = regions.size() - 1; i >= 0; i--) {
            ClimateDebugRegion region = regions.get(i);
            if (region.contains(location)) {
                regions.remove(i);
                return region;
            }
        }
        return null;
    }

    public int createClimateDebugPlatform(ClimateDebugRegion region) {
        if (region == null) {
            return 0;
        }
        return getClimateDebugParticlePointCount(region);
    }

    public int refreshClimateDebugPlatform(ClimateDebugRegion region) {
        if (region == null) {
            return 0;
        }
        return getClimateDebugParticlePointCount(region);
    }

    public int clearClimateDebugPlatform(ClimateDebugRegion region) {
        if (region == null) {
            return 0;
        }
        return getClimateDebugParticlePointCount(region);
    }

    public int clearAllClimateDebugPlatforms(World world) {
        if (world == null) {
            return 0;
        }
        List<ClimateDebugRegion> regions = climateDebugRegions.remove(world.getUID());
        if (regions == null || regions.isEmpty()) {
            return 0;
        }
        int changed = 0;
        for (ClimateDebugRegion region : regions) {
            changed += clearClimateDebugPlatform(region);
        }
        return changed;
    }

    private int getClimateDebugParticlePointCount(ClimateDebugRegion region) {
        int spacing = Math.max(1, getConfig().getInt("climate.debug-particles.spacing-blocks", 2));
        int width = ((region.maxX() - region.minX()) / spacing) + 1;
        int depth = ((region.maxZ() - region.minZ()) / spacing) + 1;
        return Math.max(0, width * depth);
    }

    private boolean overlaps(ClimateDebugRegion left, ClimateDebugRegion right) {
        return left.maxX() >= right.minX()
                && left.minX() <= right.maxX()
                && left.maxZ() >= right.minZ()
                && left.minZ() <= right.maxZ();
    }

    private void updateAllCooldownDebugBars() {
        cooldownDebugPlayers.removeIf(playerId -> {
            Player player = getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                removeCooldownDebugBars(playerId);
                return true;
            }

            updateCooldownDebugBars(player);
            return false;
        });
        stopCooldownDebugTaskIfIdle();
    }

    private void updateCooldownDebugBars(Player player) {
        UUID playerId = player.getUniqueId();
        BossBar breakBar = breakCooldownDebugBars.get(playerId);
        BossBar placeBar = placeCooldownDebugBars.get(playerId);
        if (breakBar == null || placeBar == null) {
            return;
        }

        long breakRemaining = Math.max(0L, getBreakCooldownEnd(playerId) - System.currentTimeMillis());
        long placeRemaining = Math.max(0L, getPlaceCooldownEnd(playerId) - System.currentTimeMillis());
        int breakDurationSeconds = Math.max(1, getSharedActionCooldownSeconds(playerId));
        int placeDurationSeconds = Math.max(1, getSharedActionCooldownSeconds(playerId));

        breakBar.setProgress(cooldownProgress(breakRemaining, breakDurationSeconds));
        breakBar.setTitle(colorize("&cBreak CD: &f" + formatCooldownDebugValue(breakRemaining)));
        placeBar.setProgress(cooldownProgress(placeRemaining, placeDurationSeconds));
        placeBar.setTitle(colorize("&aPlace CD: &f" + formatCooldownDebugValue(placeRemaining)));
    }

    private double cooldownProgress(long remainingMillis, int durationSeconds) {
        if (remainingMillis <= 0L) {
            return 1.0D;
        }
        long totalMillis = Math.max(1000L, durationSeconds * 1000L);
        double progress = 1.0D - (remainingMillis / (double) totalMillis);
        return Math.max(0.0D, Math.min(1.0D, progress));
    }

    private String formatCooldownDebugValue(long remainingMillis) {
        if (remainingMillis <= 0L) {
            return "Ready";
        }
        return String.format(Locale.US, "%.1fs", remainingMillis / 1000.0D);
    }

    private void removeCooldownDebugBars(UUID playerId) {
        BossBar breakBar = breakCooldownDebugBars.remove(playerId);
        BossBar placeBar = placeCooldownDebugBars.remove(playerId);
        if (breakBar != null) {
            breakBar.removeAll();
            breakBar.setVisible(false);
        }
        if (placeBar != null) {
            placeBar.removeAll();
            placeBar.setVisible(false);
        }
    }

    public void addPlayerToGlobalXpBoostBar(Player player) {
        if (player == null || globalXpBoostBossBar == null || !hasActiveGlobalXpBoost()) {
            return;
        }
        globalXpBoostBossBar.addPlayer(player);
    }

    private void reloadGlobalXpBoostState() {
        double multiplier = dataConfig.getDouble("global-xp-boost.multiplier", 1.0D);
        long expiresAt = dataConfig.getLong("global-xp-boost.expires-at", 0L);
        long duration = dataConfig.getLong("global-xp-boost.duration", 0L);
        String enabledBy = dataConfig.getString("global-xp-boost.enabled-by");
        globalXpBoostMultiplier = multiplier;
        globalXpBoostEndMillis = expiresAt;
        globalXpBoostDurationMillis = duration;
        globalXpBoostEnabledBy = enabledBy;

        if (!hasActiveGlobalXpBoost()) {
            stopGlobalXpBoost();
            return;
        }
        startGlobalXpBoostBossBar();
    }

    private void startGlobalXpBoostBossBar() {
        if (globalXpBoostTask != null) {
            globalXpBoostTask.cancel();
            globalXpBoostTask = null;
        }
        if (globalXpBoostBossBar == null) {
            globalXpBoostBossBar = getServer().createBossBar("", BarColor.BLUE, BarStyle.SEGMENTED_10);
        }

        globalXpBoostBossBar.removeAll();
        globalXpBoostBossBar.setVisible(true);
        for (Player player : getServer().getOnlinePlayers()) {
            globalXpBoostBossBar.addPlayer(player);
        }
        updateGlobalXpBoostBossBar();
        globalXpBoostTask = getServer().getScheduler().runTaskTimer(this, this::updateGlobalXpBoostBossBar, 20L, 20L);
    }

    private void updateGlobalXpBoostBossBar() {
        if (globalXpBoostBossBar == null) {
            return;
        }
        if (!hasActiveGlobalXpBoost()) {
            stopGlobalXpBoost();
            return;
        }

        long remainingMillis = getGlobalXpBoostRemainingMillis();
        long totalMillis = Math.max(1000L, globalXpBoostDurationMillis);
        double progress = Math.max(0.0D, Math.min(1.0D, remainingMillis / (double) Math.max(1000L, totalMillis)));
        globalXpBoostBossBar.setProgress(progress);
        String booster = globalXpBoostEnabledBy == null || globalXpBoostEnabledBy.isBlank() ? "Unknown" : globalXpBoostEnabledBy;
        globalXpBoostBossBar.setTitle(colorize("&bGlobal XP Boost &f" + formatXpBoostMultiplier(globalXpBoostMultiplier)
                + "&b by &f" + booster + "&b for &f" + formatDurationWords(remainingMillis)));
    }

    private void saveGlobalXpBoostState() {
        dataConfig.set("global-xp-boost.multiplier", globalXpBoostMultiplier > 1.0D ? globalXpBoostMultiplier : null);
        dataConfig.set("global-xp-boost.expires-at", hasActiveGlobalXpBoost() ? globalXpBoostEndMillis : null);
        dataConfig.set("global-xp-boost.duration", hasActiveGlobalXpBoost() ? globalXpBoostDurationMillis : null);
        dataConfig.set("global-xp-boost.enabled-by", hasActiveGlobalXpBoost() ? globalXpBoostEnabledBy : null);
        saveDataConfig();
    }

    public String getGlobalXpBoostEnabledBy() {
        return globalXpBoostEnabledBy;
    }

    public String formatXpBoostMultiplier(double multiplier) {
        return String.format(Locale.US, "%.1fx", multiplier);
    }

    public String formatDurationWords(long millis) {
        long totalSeconds = Math.max(1L, (long) Math.ceil(millis / 1000.0D));
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        List<String> parts = new ArrayList<>();
        if (hours > 0) {
            parts.add(hours + "h");
        }
        if (minutes > 0) {
            parts.add(minutes + "m");
        }
        if (seconds > 0 && parts.size() < 2) {
            parts.add(seconds + "s");
        }
        return String.join(" ", parts);
    }

    public String formatPlaytestRemainingDuration(long millis) {
        long totalSeconds = Math.max(0L, (long) Math.ceil(millis / 1000.0D));
        long days = totalSeconds / 86400L;
        long hours = (totalSeconds % 86400L) / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        if (days > 0L) {
            return days + "d " + hours + "h";
        }
        if (hours > 0L) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m " + seconds + "s";
    }

    public int countUnlockedProfessionSkillNodes(UUID playerId, Profession profession, String... nodeKeys) {
        if (playerId == null || profession == null || nodeKeys == null || nodeKeys.length == 0) {
            return 0;
        }
        int count = 0;
        for (String nodeKey : nodeKeys) {
            if (hasProfessionSkillNode(playerId, profession, nodeKey)) {
                count++;
            }
        }
        return count;
    }

    public long getAbilityCooldownRemainingMillis(UUID playerId, String abilityKey) {
        if (playerId == null || abilityKey == null) {
            return 0L;
        }
        long now = System.currentTimeMillis();
        return switch (abilityKey.toLowerCase(Locale.ROOT)) {
            case "miner_overdrive" -> Math.max(0L, minerOverdriveCooldownUntil.getOrDefault(playerId, 0L) - now);
            case "farmer_growth_burst" -> Math.max(0L, farmerGrowthBurstCooldownUntil.getOrDefault(playerId, 0L) - now);
            case "trader_market_scan" -> Math.max(0L, traderMarketScanCooldownUntil.getOrDefault(playerId, 0L) - now);
            default -> 0L;
        };
    }

    public boolean isMinerOverdriveActive(UUID playerId) {
        return playerId != null && minerOverdriveUntil.getOrDefault(playerId, 0L) > System.currentTimeMillis();
    }

    public boolean activateMinerOverdrive(Player player) {
        if (player == null) {
            return false;
        }
        UUID playerId = player.getUniqueId();
        if (!hasProfessionSkillNode(playerId, Profession.MINER, "overdrive")) {
            return false;
        }
        if (getAbilityCooldownRemainingMillis(playerId, "miner_overdrive") > 0L) {
            return false;
        }
        long now = System.currentTimeMillis();
        minerOverdriveUntil.put(playerId, now + MINER_OVERDRIVE_DURATION_MILLIS);
        minerOverdriveCooldownUntil.put(playerId, now + MINER_OVERDRIVE_COOLDOWN_MILLIS);
        player.sendMessage(colorize("&6[Terra] &aMiner Overdrive activated for &f10s&a."));
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.7F, 1.2F);
        return true;
    }

    public boolean isFarmerGrowthBurstActive(UUID playerId) {
        return playerId != null && farmerGrowthBurstUntil.getOrDefault(playerId, 0L) > System.currentTimeMillis();
    }

    public boolean activateFarmerGrowthBurst(Player player) {
        if (player == null) {
            return false;
        }
        UUID playerId = player.getUniqueId();
        if (!hasProfessionSkillNode(playerId, Profession.FARMER, "fast_growth")) {
            return false;
        }
        if (getAbilityCooldownRemainingMillis(playerId, "farmer_growth_burst") > 0L) {
            return false;
        }
        long now = System.currentTimeMillis();
        farmerGrowthBurstUntil.put(playerId, now + FARMER_GROWTH_BURST_DURATION_MILLIS);
        farmerGrowthBurstCooldownUntil.put(playerId, now + FARMER_GROWTH_BURST_COOLDOWN_MILLIS);
        player.sendMessage(colorize("&6[Terra] &aGrowth Burst activated for &f20s&a."));
        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.7F, 1.3F);
        return true;
    }

    public boolean activateTraderMarketScan(Player player) {
        if (player == null) {
            return false;
        }
        UUID playerId = player.getUniqueId();
        if (!hasProfessionSkillNode(playerId, Profession.TRADER, "better_prices_i")) {
            return false;
        }
        long remaining = getAbilityCooldownRemainingMillis(playerId, "trader_market_scan");
        if (remaining > 0L) {
            player.sendMessage(colorize("&cMarket Scan cooldown: &f" + formatLongDurationWords(remaining)));
            return false;
        }

        traderMarketScanCooldownUntil.put(playerId, System.currentTimeMillis() + TRADER_MARKET_SCAN_COOLDOWN_MILLIS);
        DynamicTraderState traderState = getActiveTraderState();
        if (traderState == null) {
            long nextSpawn = Math.max(0L, getTraderNextSpawnMillis() - System.currentTimeMillis());
            player.sendMessage(colorize("&6[Terra] &eNo active trader. Next trader cycle in &f" + formatLongDurationWords(nextSpawn) + "&e."));
            return true;
        }

        Country hostCountry = getTraderHostCountry(traderState);
        long despawnIn = Math.max(0L, traderState.getDespawnAtMillis() - System.currentTimeMillis());
        player.sendMessage(colorize("&6[Terra] &aMarket Scan"));
        player.sendMessage(colorize("&7Trader: &f" + traderState.getTraderName()));
        player.sendMessage(colorize("&7Specialty: &f" + getProfessionPlainDisplayName(traderState.getSpecialtyProfession())));
        player.sendMessage(colorize("&7Host country: &f" + (hostCountry != null ? hostCountry.getName() : "Unknown")));
        player.sendMessage(colorize("&7Despawns in: &f" + formatLongDurationWords(despawnIn)));
        player.sendMessage(colorize("&7Your trader reputation: &f" + formatTraderReputation(getTraderReputation(playerId))));
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8F, 1.2F);
        return true;
    }

    public float getProfessionExhaustionMultiplier(UUID playerId) {
        if (playerId == null) {
            return 1.0F;
        }
        double multiplier = 1.0D;
        multiplier -= countUnlockedProfessionSkillNodes(playerId, Profession.MINER, "efficient_miner") * 0.12D;
        multiplier -= countUnlockedProfessionSkillNodes(playerId, Profession.LUMBERJACK, "energy_saver") * 0.10D;
        multiplier -= countUnlockedProfessionSkillNodes(playerId, Profession.FARMER, "no_hunger_drain") * 0.18D;
        return (float) Math.max(0.45D, multiplier);
    }

    public void applyFarmerMealBuffs(Player player, ItemStack itemStack) {
        if (player == null || itemStack == null) {
            return;
        }
        UUID playerId = player.getUniqueId();
        int basicMeals = countUnlockedProfessionSkillNodes(playerId, Profession.FARMER, "basic_meals");
        int advancedMeals = countUnlockedProfessionSkillNodes(playerId, Profession.FARMER, "advanced_meals");
        if (basicMeals <= 0 && advancedMeals <= 0) {
            return;
        }

        Material material = itemStack.getType();
        if (!(material.isEdible() || material == Material.POTION)) {
            return;
        }

        if (basicMeals > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 8, 0, true, false, true));
        }
        if (advancedMeals > 0) {
            PotionEffectType type = switch (material) {
                case BREAD, BAKED_POTATO, COOKED_BEEF, COOKED_CHICKEN, COOKED_PORKCHOP -> PotionEffectType.HASTE;
                case GOLDEN_CARROT, APPLE, MELON_SLICE, SWEET_BERRIES, GLOW_BERRIES -> PotionEffectType.SPEED;
                default -> PotionEffectType.REGENERATION;
            };
            player.addPotionEffect(new PotionEffect(type, 20 * 20, 0, true, false, true));
        }
    }

    public int getMinerBreakCooldownSeconds(UUID playerId) {
        int baseCooldown = getBlockDelaySeconds();
        int totalProfessionReduction = getTotalProfessionCooldownReductionSeconds(playerId);
        int minerReduction = 0;
        if (hasProfession(playerId, Profession.MINER)) {
            int level = getProfessionLevel(playerId, Profession.MINER);
            int reductionPerLevel = Math.max(0, getProfessionConfig(Profession.MINER).getInt("progression.cooldown-reduction-seconds-per-level", 2));
            minerReduction = Math.max(0, (level - 1) * reductionPerLevel);
            minerReduction += countUnlockedProfessionSkillNodes(playerId, Profession.MINER, "quick_hands_i") * 3;
            minerReduction += countUnlockedProfessionSkillNodes(playerId, Profession.MINER, "quick_hands_ii") * 2;
            if (isMinerOverdriveActive(playerId)) {
                minerReduction += 6;
            }
        }
        Country country = getPlayerCountry(playerId);
        int countryReduction = getCountryMinerCooldownReduction(country);
        return Math.max(3, baseCooldown - totalProfessionReduction - minerReduction - countryReduction);
    }

    public int getBuilderPlaceCooldownSeconds(UUID playerId) {
        int baseCooldown = getMinerBreakCooldownSeconds(playerId);
        int builderReduction = countUnlockedProfessionSkillNodes(playerId, Profession.BUILDER, "faster_placement_i") * 2
                + countUnlockedProfessionSkillNodes(playerId, Profession.BUILDER, "faster_placement_ii") * 2;
        return Math.max(3, baseCooldown - builderReduction);
    }

    public int getSharedActionCooldownSeconds(UUID playerId) {
        return Math.max(0, Math.min(getMinerBreakCooldownSeconds(playerId), getBuilderPlaceCooldownSeconds(playerId)));
    }

    private int getTotalProfessionCooldownReductionSeconds(UUID playerId) {
        int reduction = 0;
        for (Profession profession : getOwnedProfessions(playerId)) {
            reduction += Math.max(0, getProfessionLevel(playerId, profession) - 1);
        }
        return reduction;
    }

    public boolean canMinerBreak(Material material, int level) {
        return canProfessionBreakAtLevel(Profession.MINER, material, level);
    }

    public boolean isConfiguredMinerBlock(Material material) {
        return isConfiguredProfessionBlock(Profession.MINER, material);
    }

    public boolean isFarmerCrop(Material material) {
        return Tag.CROPS.isTagged(material);
    }

    public boolean isFixedOreMaterial(Material material) {
        if (material == null || !material.isBlock()) {
            return false;
        }
        return material.name().endsWith("_ORE")
                || material == Material.ANCIENT_DEBRIS
                || material == Material.DIRT
                || material == Material.GRASS_BLOCK
                || material == Material.STONE
                || material == Material.COBBLESTONE
                || Tag.LOGS.isTagged(material)
                || material.name().endsWith("_STEM")
                || material.name().endsWith("_HYPHAE");
    }

    public List<String> getFixedOreMaterialNames() {
        List<String> oreNames = new ArrayList<>();
        for (Material material : Material.values()) {
            if (isFixedOreMaterial(material)) {
                oreNames.add(material.name().toLowerCase(Locale.ROOT));
            }
        }
        oreNames.sort(String::compareToIgnoreCase);
        return oreNames;
    }

    public Profession getRequiredProfessionForBlock(Material material) {
        if (isLumberjackWoodBlock(material)) {
            return Profession.LUMBERJACK;
        }
        for (Profession profession : Profession.values()) {
            if (!isProfessionEnabled(profession)) {
                continue;
            }
            if (isConfiguredProfessionBlock(profession, material)) {
                return profession;
            }
        }
        if (isFarmerCrop(material)) {
            return Profession.FARMER;
        }
        return null;
    }

    public int getRequiredProfessionLevelForBlock(Profession profession, Material material) {
        if (profession == null || material == null) {
            return 1;
        }
        if (profession == Profession.LUMBERJACK && isLumberjackWoodBlock(material)) {
            return 1;
        }
        if (profession == Profession.FARMER && isFarmerCrop(material)) {
            return 1;
        }

        int maxLevel = getProfessionBaseMaxLevel(profession);
        for (int level = 1; level <= maxLevel; level++) {
            ConfigurationSection blockSection = getProfessionBlocksSection(profession, level);
            if (blockSection != null && blockSection.contains(material.name())
                    && blockSection.getBoolean(material.name() + ".enabled", true)) {
                return level;
            }
        }
        return 1;
    }

    public boolean canProfessionBreak(UUID playerId, Profession profession, Material material) {
        return canProfessionBreakAtLevel(profession, material, getProfessionLevel(playerId, profession));
    }

    public boolean canProfessionBreak(UUID playerId, Profession profession, Material material, int level) {
        return canProfessionBreakAtLevel(profession, material, level);
    }

    private boolean canProfessionBreakAtLevel(Profession profession, Material material, int level) {
        if (profession == null) {
            return false;
        }

        if (profession == Profession.LUMBERJACK && isLumberjackWoodBlock(material)) {
            return true;
        }

        int maxLevel = Math.min(level, getProfessionBaseMaxLevel(profession));
        for (int currentLevel = 1; currentLevel <= maxLevel; currentLevel++) {
            ConfigurationSection blockSection = getProfessionBlocksSection(profession, currentLevel);
            if (blockSection == null) {
                continue;
            }
            if (blockSection.contains(material.name()) && blockSection.getBoolean(material.name() + ".enabled", true)) {
                return true;
            }
        }
        return false;
    }

    public boolean isConfiguredProfessionBlock(Profession profession, Material material) {
        if (profession == null) {
            return false;
        }

        int maxLevel = getProfessionBaseMaxLevel(profession);
        for (int level = 1; level <= maxLevel; level++) {
            ConfigurationSection blockSection = getProfessionBlocksSection(profession, level);
            if (blockSection != null && blockSection.contains(material.name())) {
                return true;
            }
        }
        return false;
    }

    public int getProfessionBlockXp(Profession profession, Material material, int level) {
        if (profession == Profession.LUMBERJACK && isLumberjackDecorativeWoodBlock(material)) {
            return 1;
        }

        for (int currentLevel = level; currentLevel >= 1; currentLevel--) {
            ConfigurationSection blockSection = getProfessionBlocksSection(profession, currentLevel);
            if (blockSection != null && blockSection.contains(material.name() + ".xp")) {
                return Math.max(0, blockSection.getInt(material.name() + ".xp", 0));
            }
        }
        return 0;
    }

    public int getMinerProfessionXp(Material material, int level) {
        return getProfessionBlockXp(Profession.MINER, material, level);
    }

    public boolean isUniversalCraftAllowed(Material material) {
        if (material == null) {
            return false;
        }

        String materialName = material.name();
        return materialName.startsWith("WOODEN_")
                || materialName.startsWith("LEATHER_");
    }

    public int getBlacksmithRequiredLevel(Material material) {
        if (material == null || isUniversalCraftAllowed(material)) {
            return 0;
        }

        String materialName = material.name();
        if (materialName.startsWith("STONE_")) {
            return 1;
        }
        if (materialName.startsWith("IRON_")) {
            return materialName.endsWith("_HELMET")
                    || materialName.endsWith("_CHESTPLATE")
                    || materialName.endsWith("_LEGGINGS")
                    || materialName.endsWith("_BOOTS") ? 5 : 3;
        }
        if (materialName.startsWith("GOLDEN_")) {
            return 4;
        }
        if (materialName.startsWith("DIAMOND_")) {
            return materialName.endsWith("_HELMET")
                    || materialName.endsWith("_CHESTPLATE")
                    || materialName.endsWith("_LEGGINGS")
                    || materialName.endsWith("_BOOTS") ? 8 : 7;
        }
        if (materialName.startsWith("NETHERITE_")) {
            return 10;
        }
        return 0;
    }

    public boolean isBlacksmithCraft(Material material) {
        return getBlacksmithRequiredLevel(material) > 0 || isUniversalCraftAllowed(material);
    }

    public int getBlacksmithCraftXp(Material material) {
        int requiredLevel = getBlacksmithRequiredLevel(material);
        if (isUniversalCraftAllowed(material)) {
            return material != null && material.name().startsWith("LEATHER_") ? 4 : 3;
        }
        return switch (requiredLevel) {
            case 1 -> 5;
            case 3 -> 8;
            case 4 -> 9;
            case 5 -> 12;
            case 7 -> 15;
            case 8 -> 18;
            case 10 -> 24;
            default -> 0;
        };
    }

    public String getBlacksmithAnvilGuiTitle() {
        return colorize(getProfessionConfig(Profession.BLACKSMITH).getString("custom-anvil.title", "&8Blacksmith Forge"));
    }

    public List<BlacksmithRecipe> getBlacksmithAnvilRecipes() {
        List<BlacksmithRecipe> recipes = new ArrayList<>();
        ConfigurationSection section = getProfessionConfig(Profession.BLACKSMITH).getConfigurationSection("custom-anvil.recipes");
        if (section == null) {
            return recipes;
        }

        for (String recipeKey : section.getKeys(false)) {
            ConfigurationSection recipeSection = section.getConfigurationSection(recipeKey);
            if (recipeSection == null) {
                continue;
            }

            Material result = Material.matchMaterial(recipeSection.getString("result", recipeKey));
            if (result == null || result.isAir()) {
                continue;
            }

            int level = Math.max(0, recipeSection.getInt("level", getBlacksmithRequiredLevel(result)));
            int amount = Math.max(1, recipeSection.getInt("amount", 1));
            int xp = Math.max(0, recipeSection.getInt("xp", getBlacksmithCraftXp(result)));
            String category = recipeSection.getString("category", "basics");
            int slot = Math.max(0, recipeSection.getInt("slot", recipes.size()));

            LinkedHashMap<Material, Integer> ingredients = new LinkedHashMap<>();
            ConfigurationSection ingredientSection = recipeSection.getConfigurationSection("ingredients");
            if (ingredientSection == null) {
                continue;
            }

            for (String ingredientKey : ingredientSection.getKeys(false)) {
                Material ingredient = Material.matchMaterial(ingredientKey);
                if (ingredient == null || ingredient.isAir()) {
                    continue;
                }
                int ingredientAmount = Math.max(1, ingredientSection.getInt(ingredientKey, 1));
                ingredients.put(ingredient, ingredientAmount);
            }

            if (!ingredients.isEmpty()) {
                recipes.add(new BlacksmithRecipe(result, amount, level, xp, category, slot, ingredients));
            }
        }

        recipes.sort((left, right) -> Integer.compare(left.slot(), right.slot()));
        return recipes;
    }

    public boolean isBlacksmithForgeRecipe(Material material) {
        if (material == null) {
            return false;
        }
        for (BlacksmithRecipe recipe : getBlacksmithAnvilRecipes()) {
            if (recipe.result() == material) {
                return true;
            }
        }
        return false;
    }

    public int getBlacksmithSmeltXp(Material material) {
        if (material == null) {
            return 0;
        }

        return switch (material) {
            case COPPER_INGOT -> 5;
            case IRON_INGOT -> 7;
            case GOLD_INGOT -> 9;
            case NETHERITE_SCRAP -> 20;
            default -> 0;
        };
    }

    public int getMinerSmeltCollaborationXp(Material material) {
        if (material == null) {
            return 0;
        }

        return switch (material) {
            case COPPER_INGOT -> 1;
            case IRON_INGOT -> 2;
            case GOLD_INGOT -> 3;
            case NETHERITE_SCRAP -> 6;
            default -> 0;
        };
    }

    public int getFarmerCookCollaborationXp(Material material) {
        if (material == null) {
            return 0;
        }

        return switch (material) {
            case BREAD -> 1;
            case BAKED_POTATO, COOKED_CHICKEN, COOKED_MUTTON, COOKED_RABBIT -> 2;
            case COOKED_PORKCHOP, COOKED_COD, COOKED_SALMON -> 3;
            case COOKED_BEEF, PUMPKIN_PIE, CAKE -> 4;
            default -> 1;
        };
    }

    public boolean isSmeltableOre(Material material) {
        if (material == null) {
            return false;
        }

        return switch (material) {
            case RAW_COPPER, RAW_IRON, RAW_GOLD,
                 COPPER_ORE, DEEPSLATE_COPPER_ORE,
                 IRON_ORE, DEEPSLATE_IRON_ORE,
                 GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE,
                 ANCIENT_DEBRIS -> true;
            default -> false;
        };
    }

    public boolean isCookableFood(Material material) {
        if (material == null) {
            return false;
        }

        return switch (material) {
            case POTATO, BEEF, CHICKEN, PORKCHOP, MUTTON, RABBIT, COD, SALMON, KELP -> true;
            default -> false;
        };
    }

    public int getFarmerCookingLevel(Material material) {
        if (material == null) {
            return 0;
        }

        return switch (material) {
            case POTATO, CHICKEN, MUTTON, RABBIT -> 2;
            case PORKCHOP, COD, SALMON, KELP -> 3;
            case BEEF -> 4;
            default -> 1;
        };
    }

    public int getFarmerCookingXp(Material material) {
        if (material == null) {
            return 0;
        }

        return switch (material) {
            case BAKED_POTATO -> 5;
            case COOKED_CHICKEN, COOKED_MUTTON, COOKED_RABBIT -> 6;
            case COOKED_PORKCHOP, COOKED_COD, COOKED_SALMON, DRIED_KELP -> 7;
            case COOKED_BEEF -> 9;
            default -> 4;
        };
    }

    public boolean isFarmerCraftFood(Material material) {
        return material != null && material.isEdible();
    }

    public int getFarmerCraftLevel(Material material) {
        if (material == null) {
            return 0;
        }

        return switch (material) {
            case BREAD -> 1;
            case COOKIE, MUSHROOM_STEW, BEETROOT_SOUP -> 2;
            case PUMPKIN_PIE -> 3;
            case CAKE -> 4;
            case RABBIT_STEW -> 5;
            case SUSPICIOUS_STEW -> 6;
            default -> 1;
        };
    }

    public int getFarmerCraftXp(Material material) {
        if (material == null) {
            return 0;
        }

        return switch (material) {
            case BREAD -> 4;
            case COOKIE, MUSHROOM_STEW, BEETROOT_SOUP -> 5;
            case PUMPKIN_PIE -> 6;
            case CAKE -> 8;
            case RABBIT_STEW, SUSPICIOUS_STEW -> 9;
            default -> 3;
        };
    }

    public int getFarmerFarmlandCreationXp() {
        return Math.max(1, getProfessionConfig(Profession.FARMER).getInt("rewards.farmland-create-xp", 3));
    }

    public int getFarmerFarmlandCreationRequiredLevel() {
        return Math.max(1, getProfessionConfig(Profession.FARMER).getInt("rewards.farmland-create-required-level", 1));
    }

    public int getFarmerCoarseDirtConversionXp() {
        return Math.max(1, getProfessionConfig(Profession.FARMER).getInt("rewards.coarse-dirt-convert-xp", 2));
    }

    public int getFarmerCoarseDirtConversionRequiredLevel() {
        return Math.max(1, getProfessionConfig(Profession.FARMER).getInt("rewards.coarse-dirt-convert-required-level", 1));
    }

    public double getFarmerPlantXp() {
        return Math.max(0.0D, getProfessionConfig(Profession.FARMER).getDouble("rewards.plant-xp", 1.5D));
    }

    public int getFarmerPlantRequiredLevel() {
        return Math.max(1, getProfessionConfig(Profession.FARMER).getInt("rewards.plant-required-level", 1));
    }

    public double getFarmerBonemealXp() {
        return Math.max(0.0D, getProfessionConfig(Profession.FARMER).getDouble("rewards.bonemeal-xp", 1.0D));
    }

    public int getFarmerBonemealRequiredLevel() {
        return Math.max(1, getProfessionConfig(Profession.FARMER).getInt("rewards.bonemeal-required-level", 1));
    }

    public int getBuilderPlaceXp() {
        return Math.max(0, (int) Math.round(getProfessionConfig(Profession.BUILDER).getDouble("rewards.place-xp", 5.0D)));
    }

    public int getBuilderPlaceRequiredLevel() {
        return Math.max(1, getProfessionConfig(Profession.BUILDER).getInt("rewards.place-required-level", 1));
    }

    public int getFarmerHoeRequiredLevel(Material material) {
        if (material == null) {
            return 1;
        }

        return switch (material) {
            case WOODEN_HOE -> 1;
            case STONE_HOE -> 2;
            case IRON_HOE -> 4;
            case GOLDEN_HOE -> 5;
            case DIAMOND_HOE -> 7;
            case NETHERITE_HOE -> 10;
            default -> 1;
        };
    }

    public ItemStack applyUsageRequirementLore(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return itemStack;
        }

        Profession requiredProfession = getUsageRequirementProfession(itemStack.getType());
        int requiredLevel = getUsageRequirementLevel(itemStack.getType(), requiredProfession);
        if (requiredProfession == null || requiredLevel <= 0) {
            return itemStack;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return itemStack;
        }

        List<Component> lore = itemMeta.lore() != null ? new ArrayList<>(itemMeta.lore()) : new ArrayList<>();
        String plainJobPrefix = "Required Job:";
        String plainLevelPrefix = "Required Level:";
        lore.removeIf(line -> {
            String plain = PLAIN_TEXT_SERIALIZER.serialize(line);
            return plain.startsWith(plainJobPrefix) || plain.startsWith(plainLevelPrefix);
        });

        lore.add(legacyComponent("&8"));
        lore.add(legacyComponent("&7Required Job: " + getProfessionDisplayName(requiredProfession)));
        lore.add(legacyComponent("&7Required Level: &f" + requiredLevel));
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private Profession getUsageRequirementProfession(Material material) {
        if (material == null) {
            return null;
        }
        if (material.name().endsWith("_HOE")) {
            return Profession.FARMER;
        }
        if (material.name().endsWith("_AXE")) {
            int blacksmithLevel = getBlacksmithRequiredLevel(material);
            return blacksmithLevel > 0 ? Profession.BLACKSMITH : Profession.LUMBERJACK;
        }
        if (material.name().endsWith("_PICKAXE")
                || material.name().endsWith("_SHOVEL")
                || material.name().endsWith("_SWORD")
                || material.name().endsWith("_HELMET")
                || material.name().endsWith("_CHESTPLATE")
                || material.name().endsWith("_LEGGINGS")
                || material.name().endsWith("_BOOTS")
                || material == Material.SHIELD
                || material == Material.CROSSBOW
                || material == Material.SHEARS
                || material == Material.BUCKET
                || material == Material.FLINT_AND_STEEL
                || material == Material.BELL
                || material == Material.LODESTONE
                || material == Material.ENCHANTING_TABLE
                || material == Material.ANVIL) {
            return getBlacksmithRequiredLevel(material) > 0 ? Profession.BLACKSMITH : Profession.MINER;
        }
        return null;
    }

    private int getUsageRequirementLevel(Material material, Profession profession) {
        if (material == null || profession == null) {
            return 0;
        }
        return switch (profession) {
            case FARMER -> material.name().endsWith("_HOE") ? getFarmerHoeRequiredLevel(material) : Math.max(1, getFarmerCraftLevel(material));
            case BLACKSMITH -> Math.max(1, getBlacksmithRequiredLevel(material));
            case MINER, LUMBERJACK, BUILDER, TRADER, SOLDIER -> 1;
        };
    }

    public int getFarmerWaterBucketRequiredLevel() {
        return Math.max(1, getProfessionConfig(Profession.FARMER).getInt("rewards.water-bucket-required-level", 3));
    }

    public int getFarmerSmokerRequiredLevel() {
        return Math.max(1, getProfessionConfig(Profession.FARMER).getInt("rewards.smoker-required-level", 5));
    }

    public void openProfessionMenu(Player player) {
        if (player == null || isTutorialIntroActive(player.getUniqueId())) {
            return;
        }
        if (requiresProfessionSelection(player)) {
            preparePlayerForProfessionSelection(player);
        }
        if (professionSelectionListener != null) {
            professionSelectionListener.openSelectionMenu(player);
        }
    }

    public void openProfessionSkillTreeMenu(Player player, Profession profession) {
        if (player == null || profession == null) {
            return;
        }
        if (professionSelectionListener != null) {
            professionSelectionListener.openProfessionSkillTreeMenu(player, profession);
        }
    }

    public void preparePlayerForProfessionSelection(Player player) {
        if (player == null) {
            return;
        }

        Location safeLocation = findSafeProfessionSelectionLocation(player);
        if (safeLocation == null) {
            return;
        }

        Location currentLocation = player.getLocation();
        if (!sameBlock(currentLocation, safeLocation) || isUnsafeProfessionSelectionLocation(currentLocation)) {
            player.leaveVehicle();
            player.setFallDistance(0.0F);
            player.setFireTicks(0);
            player.teleport(safeLocation);
        }
    }

    public String getProfessionGuiTitle() {
        return colorize(jobsConfig.getString("gui.title", "&8Choose Your Profession"));
    }

    public String getProfessionDetailGuiTitle(Profession profession) {
        return colorize(jobsConfig.getString(
                "gui.detail-title",
                "&8%profession% Progression"
        ).replace("%profession%", getProfessionPlainDisplayName(profession)));
    }

    public String getProfessionLevelDetailGuiTitle(Profession profession, int level) {
        return colorize(jobsConfig.getString(
                "gui.level-detail-title",
                "&8%profession% Level %level%"
        ).replace("%profession%", getProfessionPlainDisplayName(profession))
                .replace("%level%", String.valueOf(level)));
    }

    public int getProfessionGuiSize() {
        int size = jobsConfig.getInt("gui.size", 27);
        if (size < 9) {
            return 9;
        }
        if (size > 54) {
            return 54;
        }
        return (size / 9) * 9;
    }

    public List<String> getProfessionGuiLore() {
        List<String> lore = jobsConfig.getStringList("gui.lore");
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(colorize(line));
        }
        return coloredLore;
    }

    public List<Profession> getConfiguredProfessions() {
        List<Profession> professions = new ArrayList<>();
        for (Profession profession : Profession.values()) {
            if (isProfessionEnabled(profession)) {
                professions.add(profession);
            }
        }
        professions.sort((left, right) -> Integer.compare(getProfessionSlot(left), getProfessionSlot(right)));
        return professions;
    }

    public boolean isProfessionEnabled(Profession profession) {
        return getProfessionConfig(profession).getBoolean("enabled", true);
    }

    public String getProfessionDisplayName(Profession profession) {
        return colorize(getProfessionConfig(profession).getString(
                "display-name",
                "&a" + profession.getDefaultDisplayName()
        ));
    }

    public String getProfessionPlainDisplayName(Profession profession) {
        return PLAIN_TEXT_SERIALIZER.serialize(AMPERSAND_SERIALIZER.deserialize(getProfessionDisplayName(profession)));
    }

    public Material getProfessionIcon(Profession profession) {
        String iconName = getProfessionConfig(profession).getString("icon");
        Material material = iconName != null ? Material.matchMaterial(iconName) : null;
        return material != null ? material : profession.getDefaultIcon();
    }

    public int getProfessionSlot(Profession profession) {
        return getProfessionConfig(profession).getInt("slot", profession.getDefaultSlot());
    }

    public void reloadTerra() {
        shutdownGlobalXpBoostRuntime();
        stopRealTimeClock();
        shutdownOreVisionRuntime();
        shutdownClimateRuntime();
        stopItemsAdderTopStatusHud();
        stopCustomScoreboard();
        stopTraderRuntime();
        stopMerchantRuntime();
        stopNpcHeadTrackingRuntime();
        stopCountryBorderParticlesRuntime();
        stopLagReductionRuntime();
        reloadConfig();
        reloadCustomConfigs();
        reloadPluginSettings();
        setupLuckPerms();
        territoryService = createTerritoryService();
        territoryService.syncAll(countriesByKey.values());
        refreshAllCountryTags();
        restartRealTimeClock();
        reloadGlobalXpBoostState();
        startOreVisionRuntime();
        restorePlaytestState();
        restartTraderRuntime();
        restartMerchantRuntime();
        restartNpcHeadTrackingRuntime();
        restartCountryBorderParticlesRuntime();
        restartLagReductionRuntime();
        restartItemsAdderTopStatusHud();
        restartCustomScoreboard();
        restartClimateRuntime();

        for (Player player : getServer().getOnlinePlayers()) {
            handleTutorialJoin(player);
            if (requiresProfessionSelection(player) && !isTutorialIntroActive(player.getUniqueId())) {
                openProfessionMenu(player);
            }
        }
    }

    public boolean isWildernessRegenerationEnabled() {
        return getConfig().getBoolean("wilderness-regeneration.enabled", true);
    }

    public long getWildernessRegenerationDelayTicks() {
        long seconds = Math.max(1L, getConfig().getLong("wilderness-regeneration.seconds", 600L));
        return seconds * 20L;
    }

    public long getWildernessRegenerationDelaySeconds() {
        return Math.max(1L, getConfig().getLong("wilderness-regeneration.seconds", 600L));
    }

    public long getWildernessBuildDecayDelayTicks() {
        long seconds = Math.max(1L, getConfig().getLong("wilderness-regeneration.build-decay-seconds", 300L));
        return seconds * 20L;
    }

    public long getWildernessBuildDecayDelaySeconds() {
        return Math.max(1L, getConfig().getLong("wilderness-regeneration.build-decay-seconds", 300L));
    }

    public void setWildernessRegenerationDelaySeconds(long seconds) {
        setManagedConfigValue("wilderness-regeneration.seconds", Math.max(1L, seconds));
    }

    public void setWildernessBuildDecayDelaySeconds(long seconds) {
        setManagedConfigValue("wilderness-regeneration.build-decay-seconds", Math.max(1L, seconds));
    }

    public void scheduleWildernessBlockRestore(Block block, Material originalMaterial) {
        if (!isWildernessRegenerationEnabled()) {
            return;
        }

        World world = block.getWorld();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        Material restoreMaterial = getWildernessRestoreMaterial(originalMaterial);

        getServer().getScheduler().runTaskLater(this, () -> {
            Block currentBlock = world.getBlockAt(x, y, z);
            if (!currentBlock.getType().isAir()) {
                return;
            }
            currentBlock.setType(restoreMaterial, false);
        }, getWildernessRegenerationDelayTicks());
    }

    public void scheduleWildernessPlacedBlockRemoval(Block block, Material placedMaterial) {
        if (!isWildernessRegenerationEnabled()) {
            return;
        }

        World world = block.getWorld();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        getServer().getScheduler().runTaskLater(this, () -> {
            Block currentBlock = world.getBlockAt(x, y, z);
            if (isFixedOreBlock(currentBlock)) {
                return;
            }
            if (currentBlock.getType() != placedMaterial) {
                return;
            }
            clearPlacedBlockOwner(currentBlock);
            currentBlock.setType(Material.AIR, false);
        }, getWildernessBuildDecayDelayTicks());
    }

    public void markPlacedBlock(Block block, UUID playerId) {
        placedBlocks.put(PlacedBlockKey.from(block), playerId);
    }

    public boolean canPlacePlayerStructureBlock(Block block, UUID playerId) {
        if (!isStabilityEnabled() || block == null || playerId == null || !isStructureMaterial(block.getType())) {
            return true;
        }

        PlacedBlockKey key = PlacedBlockKey.from(block);
        UUID previousOwner = placedBlocks.put(key, playerId);
        try {
            if (wouldImmediatelyFailPlacement(block)) {
                return false;
            }

            for (BlockFace face : new BlockFace[]{BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                Block relative = block.getRelative(face);
                if (!isPlayerBuiltStructureBlock(relative)) {
                    continue;
                }
                if (wouldImmediatelyFailPlacement(relative)) {
                    return false;
                }
            }
            return true;
        } finally {
            if (previousOwner != null) {
                placedBlocks.put(key, previousOwner);
            } else {
                placedBlocks.remove(key);
            }
        }
    }

    public void clearPlacedBlockOwner(Block block) {
        placedBlocks.remove(PlacedBlockKey.from(block));
    }

    public UUID getPlacedBlockOwner(Block block) {
        return placedBlocks.get(PlacedBlockKey.from(block));
    }

    private boolean wouldImmediatelyFailPlacement(Block block) {
        if (block == null || block.isEmpty()) {
            return false;
        }
        StabilityMaterialClass materialClass = getStabilityMaterialClass(block.getType());
        if (materialClass == StabilityMaterialClass.STABLE || materialClass == StabilityMaterialClass.HARD_ROCK) {
            return false;
        }
        StabilityFailureMode failureMode = getStabilityFailureMode(block, materialClass);
        if (failureMode == null) {
            return false;
        }
        if (failureMode == StabilityFailureMode.FLOATING) {
            return true;
        }
        return computeStabilityStress(block, materialClass)
                >= getStabilityStressThreshold(materialClass) * 0.95D;
    }

    public boolean shouldAwardBreakRewards(Player player, Block block) {
        UUID ownerId = getPlacedBlockOwner(block);
        if (ownerId == null) {
            return true;
        }

        BlockState blockState = block.getState();
        return blockState.getBlockData() instanceof Ageable;
    }

    public boolean isFullyGrownCrop(Block block) {
        if (block == null || !isFarmerCrop(block.getType())) {
            return false;
        }

        if (!(block.getBlockData() instanceof Ageable ageable)) {
            return false;
        }

        return ageable.getAge() >= ageable.getMaximumAge();
    }

    public void createFixedOre(Block block, Material oreType) {
        createFixedOres(List.of(block), oreType);
    }

    public int createFixedOres(Iterable<Block> blocks, Material oreType) {
        if (blocks == null || oreType == null) {
            return 0;
        }

        int createdCount = 0;
        for (Block block : blocks) {
            if (block == null) {
                continue;
            }

            clearPlacedBlockOwner(block);
            fixedOreBlocks.put(PlacedBlockKey.from(block), oreType);
            block.setType(oreType, false);
            createdCount++;
        }

        if (createdCount > 0) {
            saveFixedOreBlocks();
        }
        return createdCount;
    }

    public boolean deleteFixedOre(Block block) {
        if (block == null) {
            return false;
        }

        PlacedBlockKey key = PlacedBlockKey.from(block);
        Material removed = fixedOreBlocks.remove(key);
        if (removed == null) {
            return false;
        }

        Material placeholder = getFixedOrePlaceholder(removed);
        if (block.getType() == removed || block.getType() == placeholder) {
            block.setType(Material.AIR, false);
        }
        saveFixedOreBlocks();
        return true;
    }

    public boolean isFixedOreBlock(Block block) {
        return getFixedOreType(block) != null;
    }

    public Material getFixedOreType(Block block) {
        if (block == null) {
            return null;
        }
        return fixedOreBlocks.get(PlacedBlockKey.from(block));
    }

    public void handleFixedOreBreak(Block block) {
        Material oreType = getFixedOreType(block);
        if (oreType == null) {
            return;
        }

        Material placeholder = getFixedOrePlaceholder(oreType);

        UUID worldId = block.getWorld().getUID();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        getServer().getScheduler().runTask(this, () -> {
            World world = getServer().getWorld(worldId);
            if (world == null) {
                return;
            }

            Block currentBlock = world.getBlockAt(x, y, z);
            if (!fixedOreBlocks.containsKey(PlacedBlockKey.from(currentBlock))) {
                return;
            }
            currentBlock.setType(placeholder, false);
        });

        getServer().getScheduler().runTaskLater(this, () -> {
            World world = getServer().getWorld(worldId);
            if (world == null) {
                return;
            }

            Block currentBlock = world.getBlockAt(x, y, z);
            Material configuredOre = fixedOreBlocks.get(PlacedBlockKey.from(currentBlock));
            if (configuredOre == null) {
                return;
            }

            currentBlock.setType(configuredOre, false);
        }, 200L);
    }

    private Material getFixedOrePlaceholder(Material fixedOreType) {
        if (fixedOreType == null) {
            return Material.BEDROCK;
        }

        if (Tag.LOGS.isTagged(fixedOreType)
                || fixedOreType.name().endsWith("_WOOD")
                || fixedOreType.name().endsWith("_STEM")
                || fixedOreType.name().endsWith("_HYPHAE")) {
            Material strippedType = Material.matchMaterial("STRIPPED_" + fixedOreType.name());
            if (strippedType != null) {
                return strippedType;
            }
        }

        return Material.BEDROCK;
    }

    public boolean canUseAdminCommands(Player player) {
        return player.isOp() || player.hasPermission(ADMIN_PERMISSION);
    }

    public boolean canViewPlugins(Player player) {
        return player.isOp() || player.hasPermission(PLUGIN_LIST_PERMISSION);
    }

    public boolean canUseStaffMode(Player player) {
        return player != null && (player.isOp()
                || player.hasPermission(ADMIN_PERMISSION)
                || player.hasPermission(STAFF_PERMISSION));
    }

    public boolean canUseCountryWarpAdmin(Player player) {
        return player != null && (player.isOp()
                || player.hasPermission(COUNTRY_ADMIN_PERMISSION)
                || player.hasPermission(COUNTRY_WARP_ADMIN_PERMISSION));
    }

    public boolean canUseAnyCountryCommand(Player player) {
        if (player == null) {
            return false;
        }
        if (player.isOp() || player.hasPermission(COUNTRY_ADMIN_PERMISSION)) {
            return true;
        }
        return player.hasPermission(COUNTRY_JOIN_PERMISSION)
                || player.hasPermission(COUNTRY_HOME_PERMISSION)
                || player.hasPermission(COUNTRY_INFO_PERMISSION)
                || player.hasPermission(COUNTRY_LIST_PERMISSION)
                || player.hasPermission(COUNTRY_INVITE_PERMISSION)
                || player.hasPermission(COUNTRY_ACCEPT_INVITE_PERMISSION)
                || player.hasPermission(COUNTRY_DISBAND_PERMISSION)
                || player.hasPermission(COUNTRY_JOINSTATUS_PERMISSION)
                || player.hasPermission(COUNTRY_LEAVE_PERMISSION)
                || player.hasPermission(COUNTRY_KICK_PERMISSION)
                || player.hasPermission(COUNTRY_FARMLAND_PERMISSION)
                || player.hasPermission(COUNTRY_RENAME_PERMISSION)
                || player.hasPermission(COUNTRY_SETOWNER_PERMISSION)
                || player.hasPermission(COUNTRY_TRANSFER_PERMISSION)
                || player.hasPermission(COUNTRY_ACCEPTTRANSFER_PERMISSION)
                || player.hasPermission(COUNTRY_SETHOME_PERMISSION)
                || player.hasPermission(COUNTRY_TERRITORY_PERMISSION)
                || player.hasPermission(COUNTRY_TAG_PERMISSION)
                || player.hasPermission(COUNTRY_USE_PERMISSION);
    }

    public void openCountryMenu(Player player) {
        if (countryGuiListener != null) {
            countryGuiListener.openCountryMenu(player);
        }
    }

    public void openCountryListMenu(Player player) {
        if (countryGuiListener != null) {
            countryGuiListener.openCountryListMenu(player);
        }
    }

    public void openCountryProgressionMenu(Player player) {
        if (countryGuiListener != null) {
            countryGuiListener.openCountryProgressionMenu(player);
        }
    }

    public void openCountryAdminMenu(Player player, Country country) {
        if (countryGuiListener != null) {
            countryGuiListener.openCountryAdminMenu(player, country);
        }
    }

    public void openCountryLeaveConfirmMenu(Player player) {
        if (countryGuiListener != null) {
            countryGuiListener.openLeaveConfirmMenu(player);
        }
    }

    public void openCountryAcceptTransferConfirmMenu(Player player) {
        if (countryGuiListener != null) {
            countryGuiListener.openAcceptTransferConfirmMenu(player);
        }
    }

    public void openPlaytestMenu(Player player) {
        if (playtestGuiListener != null) {
            playtestGuiListener.openPlaytestMenu(player);
        }
    }

    public void openStaffMenu(Player player) {
        if (staffMenuListener != null) {
            staffMenuListener.openMainMenu(player);
        }
    }

    public void openStaffPlayerMenu(Player viewer, UUID targetId) {
        if (staffMenuListener != null) {
            staffMenuListener.openPlayerMenu(viewer, targetId);
        }
    }

    public void openCountryWarpMenu(Player player, int page) {
        if (countryWarpGuiListener != null) {
            countryWarpGuiListener.openWarpMenu(player, page);
        }
    }

    public void initiateHardRestart() {
        setHardRestartPhase(HardRestartPhase.RUN_SERVER_RELOAD);
        getServer().spigot().restart();
    }

    public boolean isInStaffMode(UUID playerId) {
        return staffModeGamemodes.containsKey(playerId);
    }

    public boolean isVanished(UUID playerId) {
        return vanishedPlayers.contains(playerId);
    }

    public boolean toggleVanish(Player player) {
        boolean vanished = !isVanished(player.getUniqueId());
        setVanished(player, vanished);
        return vanished;
    }

    public void setVanished(Player player, boolean vanished) {
        if (player == null) {
            return;
        }
        UUID playerId = player.getUniqueId();
        if (vanished) {
            vanishedPlayers.add(playerId);
        } else {
            vanishedPlayers.remove(playerId);
        }
        for (Player viewer : getServer().getOnlinePlayers()) {
            if (viewer.getUniqueId().equals(playerId)) {
                continue;
            }
            if (canSeeVanishedPlayers(viewer)) {
                viewer.showPlayer(this, player);
            } else if (vanished) {
                viewer.hidePlayer(this, player);
            } else {
                viewer.showPlayer(this, player);
            }
        }
    }

    public void refreshVanishedViewFor(Player viewer) {
        if (viewer == null) {
            return;
        }
        boolean canSeeVanished = canSeeVanishedPlayers(viewer);
        for (Player online : getServer().getOnlinePlayers()) {
            if (online.getUniqueId().equals(viewer.getUniqueId())) {
                continue;
            }
            if (isVanished(online.getUniqueId()) && !canSeeVanished) {
                viewer.hidePlayer(this, online);
            } else {
                viewer.showPlayer(this, online);
            }
        }
    }

    public boolean isFrozen(UUID playerId) {
        return frozenPlayers.contains(playerId);
    }

    public boolean toggleFrozen(UUID playerId) {
        boolean frozen = !isFrozen(playerId);
        setFrozen(playerId, frozen);
        return frozen;
    }

    public void setFrozen(UUID playerId, boolean frozen) {
        if (playerId == null) {
            return;
        }
        if (frozen) {
            frozenPlayers.add(playerId);
        } else {
            frozenPlayers.remove(playerId);
        }
    }

    public void clearStaffStatesOnQuit(Player player) {
        if (player == null) {
            return;
        }
        setVanished(player, false);
        setFrozen(player.getUniqueId(), false);
        disableStaffMode(player);
    }

    public boolean toggleStaffMode(Player player) {
        if (isInStaffMode(player.getUniqueId())) {
            disableStaffMode(player);
            return false;
        }

        enableStaffMode(player);
        return true;
    }

    public void enableStaffMode(Player player) {
        staffModeGamemodes.putIfAbsent(player.getUniqueId(), player.getGameMode());
        staffModeInventories.putIfAbsent(player.getUniqueId(), StaffInventoryState.capture(player.getInventory()));
        clearPlayerInventory(player.getInventory());
        ensurePlayerGuidanceItem(player);
        player.setGameMode(GameMode.CREATIVE);
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.NIGHT_VISION,
                Integer.MAX_VALUE,
                0,
                false,
                false,
                false
        ));
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    public void disableStaffMode(Player player) {
        GameMode previousGameMode = staffModeGamemodes.remove(player.getUniqueId());
        StaffInventoryState inventoryState = staffModeInventories.remove(player.getUniqueId());
        if (previousGameMode == null) {
            return;
        }

        if (inventoryState != null) {
            inventoryState.restore(player.getInventory());
            player.updateInventory();
        }
        player.setGameMode(previousGameMode);
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
        if (previousGameMode != GameMode.CREATIVE && previousGameMode != GameMode.SPECTATOR) {
            player.setFlying(false);
            player.setAllowFlight(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR);
        }
    }

    private void clearStaffModeForReset(Player player) {
        GameMode previousGameMode = staffModeGamemodes.remove(player.getUniqueId());
        staffModeInventories.remove(player.getUniqueId());
        player.setGameMode(previousGameMode != null ? previousGameMode : GameMode.SURVIVAL);
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

    public void healAndFeedPlayer(Player player) {
        if (player == null) {
            return;
        }
        player.setFoodLevel(20);
        player.setSaturation(20.0F);
        player.setExhaustion(0.0F);
        player.setFireTicks(0);
        player.setFallDistance(0.0F);
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        }
    }

    public void clearOnlinePlayerInventory(Player player) {
        if (player == null) {
            return;
        }
        clearPlayerInventory(player.getInventory());
        player.updateInventory();
    }

    public boolean teleportPlayerToPlayer(Player actor, Player target) {
        if (actor == null || target == null) {
            return false;
        }
        return actor.teleport(target.getLocation());
    }

    public boolean summonPlayer(Player target, Player destination) {
        if (target == null || destination == null) {
            return false;
        }
        return target.teleport(destination.getLocation());
    }

    private void clearPlayerInventory(PlayerInventory inventory) {
        inventory.clear();
        inventory.setArmorContents(null);
        inventory.setExtraContents(null);
        inventory.setItemInOffHand(null);
        inventory.setHeldItemSlot(0);
    }

    public int countGroundItems() {
        int count = 0;
        for (World world : getServer().getWorlds()) {
            count += world.getEntitiesByClass(Item.class).size();
        }
        return count;
    }

    public int clearGroundItems() {
        int removed = 0;
        for (World world : getServer().getWorlds()) {
            for (Item item : new ArrayList<>(world.getEntitiesByClass(Item.class))) {
                if (!item.isValid()) {
                    continue;
                }
                item.remove();
                removed++;
            }
        }
        return removed;
    }

    public void tryMergeDroppedItem(Item droppedItem) {
        if (!isItemMergeEnabled() || droppedItem == null || !droppedItem.isValid() || droppedItem.getItemStack() == null) {
            return;
        }

        ItemStack sourceStack = droppedItem.getItemStack();
        double radius = getItemMergeRadius();
        for (Entity nearby : droppedItem.getNearbyEntities(radius, radius, radius)) {
            if (!(nearby instanceof Item nearbyItem) || !nearbyItem.isValid() || nearbyItem.equals(droppedItem)) {
                continue;
            }
            if (!canMergeDroppedItems(droppedItem, nearbyItem)) {
                continue;
            }

            ItemStack targetStack = nearbyItem.getItemStack();
            int maxSize = Math.min(sourceStack.getMaxStackSize(), targetStack.getMaxStackSize());
            int movable = Math.min(sourceStack.getAmount(), maxSize - targetStack.getAmount());
            if (movable <= 0) {
                continue;
            }

            targetStack.setAmount(targetStack.getAmount() + movable);
            nearbyItem.setItemStack(targetStack);
            sourceStack.setAmount(sourceStack.getAmount() - movable);
            if (sourceStack.getAmount() <= 0) {
                droppedItem.remove();
                return;
            }
            droppedItem.setItemStack(sourceStack);
        }
    }

    private boolean canMergeDroppedItems(Item source, Item target) {
        if (source == null || target == null || source.getItemStack() == null || target.getItemStack() == null) {
            return false;
        }
        return source.getItemStack().isSimilar(target.getItemStack());
    }

    public int runMobStackingSweep() {
        if (!isMobStackingEnabled()) {
            return 0;
        }

        int merged = 0;
        double radius = getMobStackingRadius();
        for (World world : getServer().getWorlds()) {
            for (LivingEntity entity : new ArrayList<>(world.getLivingEntities())) {
                if (!canParticipateInMobStacking(entity)) {
                    continue;
                }
                merged += mergeNearbyMobStacks(entity, radius);
            }
        }
        return merged;
    }

    public int stackMobIfPossible(LivingEntity entity) {
        if (!isMobStackingEnabled() || !canParticipateInMobStacking(entity)) {
            return 0;
        }
        return mergeNearbyMobStacks(entity, getMobStackingRadius());
    }

    private int mergeNearbyMobStacks(LivingEntity base, double radius) {
        int merged = 0;
        int currentCount = getMobStackCount(base);
        int maxStackSize = getMobStackingMaxStackSize();
        for (Entity nearby : base.getNearbyEntities(radius, radius, radius)) {
            if (!(nearby instanceof LivingEntity other) || !canParticipateInMobStacking(other) || other.equals(base)) {
                continue;
            }
            if (!canMergeMobStacks(base, other)) {
                continue;
            }

            int otherCount = getMobStackCount(other);
            int freeSpace = maxStackSize - currentCount;
            if (freeSpace <= 0) {
                break;
            }

            int moved = Math.min(freeSpace, otherCount);
            currentCount += moved;
            if (moved >= otherCount) {
                other.remove();
            } else {
                setMobStackCount(other, otherCount - moved);
            }
            merged += moved;
        }
        setMobStackCount(base, currentCount);
        return merged;
    }

    public boolean canParticipateInMobStacking(LivingEntity entity) {
        if (entity == null || !entity.isValid() || entity.isDead()) {
            return false;
        }
        if (entity instanceof Player || entity instanceof Villager || entity instanceof WanderingTrader) {
            return false;
        }
        if (isTraderNpc(entity) || isMarkedMerchantNpc(entity) || isMerchantNpc(entity)) {
            return false;
        }
        if (!(entity instanceof Monster || entity instanceof org.bukkit.entity.Animals || entity instanceof Slime || entity instanceof MagmaCube)) {
            return false;
        }
        if (entity instanceof org.bukkit.entity.Tameable tameable && tameable.isTamed()) {
            return false;
        }
        if (entity.customName() != null && !entity.getPersistentDataContainer().has(stackedMobCountKey, PersistentDataType.INTEGER)) {
            return false;
        }
        return switch (entity.getType()) {
            case ENDER_DRAGON, WITHER, WARDEN, IRON_GOLEM, SNOW_GOLEM, ALLAY -> false;
            default -> true;
        };
    }

    private boolean canMergeMobStacks(LivingEntity first, LivingEntity second) {
        if (first.getType() != second.getType()) {
            return false;
        }
        if (first instanceof org.bukkit.entity.Ageable firstAgeable && second instanceof org.bukkit.entity.Ageable secondAgeable) {
            if (firstAgeable.isAdult() != secondAgeable.isAdult()) {
                return false;
            }
        }
        if (first instanceof Slime firstSlime && second instanceof Slime secondSlime) {
            return firstSlime.getSize() == secondSlime.getSize();
        }
        if (first instanceof MagmaCube firstCube && second instanceof MagmaCube secondCube) {
            return firstCube.getSize() == secondCube.getSize();
        }
        return true;
    }

    public int getMobStackCount(LivingEntity entity) {
        if (entity == null || stackedMobCountKey == null) {
            return 1;
        }
        Integer stored = entity.getPersistentDataContainer().get(stackedMobCountKey, PersistentDataType.INTEGER);
        return stored != null && stored > 1 ? stored : 1;
    }

    public void setMobStackCount(LivingEntity entity, int count) {
        if (entity == null || stackedMobCountKey == null) {
            return;
        }
        int sanitized = Math.max(1, count);
        if (sanitized <= 1) {
            entity.getPersistentDataContainer().remove(stackedMobCountKey);
            entity.customName(null);
            entity.setCustomNameVisible(false);
            return;
        }
        entity.getPersistentDataContainer().set(stackedMobCountKey, PersistentDataType.INTEGER, sanitized);
        entity.customName(legacyComponent(getConfig().getString("lag-reduction.mob-stacking.name-format", "&e%type% &7x&f%amount%")
                .replace("%type%", formatEntityTypeName(entity.getType()))
                .replace("%amount%", String.valueOf(sanitized))));
        entity.setCustomNameVisible(true);
    }

    public void handleStackedMobDeath(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        int remaining = getMobStackCount(entity) - 1;
        if (remaining < 1) {
            return;
        }
        getServer().getScheduler().runTask(this, () -> {
            LivingEntity replacement = (LivingEntity) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
            copyStackedMobState(entity, replacement);
            if (remaining > 1) {
                setMobStackCount(replacement, remaining);
            }
        });
    }

    private void copyStackedMobState(LivingEntity source, LivingEntity target) {
        if (source == null || target == null) {
            return;
        }
        if (source instanceof org.bukkit.entity.Ageable sourceAgeable && target instanceof org.bukkit.entity.Ageable targetAgeable) {
            if (sourceAgeable.isAdult()) {
                targetAgeable.setAdult();
            } else {
                targetAgeable.setBaby();
            }
        }
        if (source instanceof Slime sourceSlime && target instanceof Slime targetSlime) {
            targetSlime.setSize(sourceSlime.getSize());
        }
        if (source instanceof MagmaCube sourceCube && target instanceof MagmaCube targetCube) {
            targetCube.setSize(sourceCube.getSize());
        }
        if (target.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        }
    }

    public String formatEntityTypeName(EntityType type) {
        if (type == null) {
            return "Mob";
        }
        String[] parts = type.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    public int countStackedMobEntities() {
        int count = 0;
        for (World world : getServer().getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (getMobStackCount(entity) > 1) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean canSeeVanishedPlayers(Player viewer) {
        return canUseStaffMode(viewer) || canUseAdminCommands(viewer);
    }

    public boolean hasBlockDelayBypass(UUID playerId) {
        return isInStaffMode(playerId) || bypassEntries.containsKey(playerId);
    }

    public boolean bypassesProfessionRestrictions(UUID playerId) {
        return isInStaffMode(playerId);
    }

    public NamespacedKey getItemSourceOwnerKey() {
        return itemSourceOwnerKey;
    }

    public NamespacedKey getItemSourceProfessionKey() {
        return itemSourceProfessionKey;
    }

    public NamespacedKey getFixedOreToolKey() {
        return fixedOreToolKey;
    }

    public Map<UUID, BypassEntry> getBypassEntries() {
        return bypassEntries;
    }

    public void setBlockDelayBypass(OfflinePlayer player, boolean enabled) {
        if (enabled) {
            String name = player.getName() != null ? player.getName() : player.getUniqueId().toString();
            Instant enabledAt = Instant.now();
            bypassEntries.put(player.getUniqueId(), new BypassEntry(player.getUniqueId(), name, enabledAt));
            dataConfig.set("bypass." + player.getUniqueId() + ".name", name);
            dataConfig.set("bypass." + player.getUniqueId() + ".enabledAt", enabledAt.toString());
        } else {
            bypassEntries.remove(player.getUniqueId());
            dataConfig.set("bypass." + player.getUniqueId(), null);
        }
        saveDataConfig();
    }

    public String getHighestPrefix(OfflinePlayer player) {
        if (!hasLuckPerms()) {
            return "unknown";
        }

        net.luckperms.api.model.user.User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            user = luckPerms.getUserManager().loadUser(player.getUniqueId()).join();
        }

        if (user == null) {
            return "none";
        }

        String prefix = user.getCachedData().getMetaData().getPrefix();
        if (prefix == null || prefix.isBlank()) {
            return "none";
        }

        return prefix;
    }

    public Country getCountry(String countryName) {
        if (countryName == null) {
            return null;
        }
        return countriesByKey.get(normalizeCountryName(countryName));
    }

    public Country getPlayerCountry(UUID playerId) {
        String countryKey = playerCountries.get(playerId);
        if (countryKey == null) {
            return null;
        }
        return countriesByKey.get(countryKey);
    }

    public List<Country> getCountries() {
        return new ArrayList<>(countriesByKey.values());
    }

    public void createCountry(String countryName, OfflinePlayer owner) {
        UUID ownerId = owner != null ? owner.getUniqueId() : null;
        Set<UUID> members = ownerId != null ? Set.of(ownerId) : Set.of();
        Country country = new Country(countryName, ownerId, true, null, null, null, null, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F,
                members, Set.of(), Set.of(), Set.of(), null, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, Set.of(), null, null, 0L,
                1, 0.0D, 0, null, 0L, Set.of());
        countriesByKey.put(normalizeCountryName(countryName), country);
        if (ownerId != null) {
            playerCountries.put(ownerId, normalizeCountryName(countryName));
        }
        saveCountry(country);
        refreshCountryTags(country);
    }

    public void addPlayerToCountry(Country country, OfflinePlayer player) {
        country.getMembers().add(player.getUniqueId());
        country.getInvitedPlayers().remove(player.getUniqueId());
        playerCountries.put(player.getUniqueId(), normalizeCountryName(country.getName()));
        saveCountry(country);
        syncCountryTerritory(country);
        refreshCountryTags(country);
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null) {
            handleTutorialCountryJoined(onlinePlayer);
        }
    }

    public void inviteToCountry(Country country, OfflinePlayer player) {
        country.getInvitedPlayers().add(player.getUniqueId());
        saveCountry(country);
    }

    public void acceptCountryInvite(Country country, OfflinePlayer player) {
        addPlayerToCountry(country, player);
    }

    public void removePlayerFromCountry(Country country, UUID playerId) {
        country.getMembers().remove(playerId);
        country.getCoOwners().remove(playerId);
        country.getStewards().remove(playerId);
        country.getInvitedPlayers().remove(playerId);
        playerCountries.remove(playerId);

        if (country.hasOwner() && country.getOwnerId().equals(playerId)) {
            if (country.getMembers().isEmpty()) {
                country.setOwnerId(null);
                saveCountry(country);
                syncCountryTerritory(country);
                return;
            }

            UUID newOwnerId = country.getCoOwners().isEmpty()
                    ? country.getMembers().iterator().next()
                    : country.getCoOwners().iterator().next();
            country.setOwnerId(newOwnerId);
            country.getCoOwners().remove(newOwnerId);
            country.getStewards().remove(newOwnerId);
        }

        saveCountry(country);
        syncCountryTerritory(country);
        refreshCountryTags(country);
    }

    public void disbandCountry(Country country) {
        territoryService.clearCountry(country);
        String key = normalizeCountryName(country.getName());
        countriesByKey.remove(key);
        for (UUID memberId : country.getMembers()) {
            playerCountries.remove(memberId);
            Player player = getServer().getPlayer(memberId);
            if (player != null) {
                resetPlayerCountryTag(player);
            }
        }
        countryDataConfig.set("countries." + key, null);
        saveCountryDataConfig();
    }

    public void setCountryOpen(Country country, boolean open) {
        country.setOpen(open);
        saveCountry(country);
        syncCountryTerritory(country);
        refreshCountryTags(country);
    }

    public boolean renameCountry(Country country, String newName) {
        String oldKey = normalizeCountryName(country.getName());
        String newKey = normalizeCountryName(newName);
        if (countriesByKey.containsKey(newKey)) {
            return false;
        }

        countriesByKey.remove(oldKey);
        countryDataConfig.set("countries." + oldKey, null);
        country.setName(newName);
        countriesByKey.put(newKey, country);
        for (UUID memberId : country.getMembers()) {
            playerCountries.put(memberId, newKey);
        }
        saveCountry(country);
        syncCountryTerritory(country);
        refreshCountryTags(country);
        return true;
    }

    public void setCountryTag(Country country, String tag) {
        country.setTag(tag);
        saveCountry(country);
        refreshCountryTags(country);
    }

    public void setCountryHome(Country country, Location location) {
        country.setHomeWorld(location.getWorld() != null ? location.getWorld().getName() : null);
        country.setHomeX(location.getX());
        country.setHomeY(location.getY());
        country.setHomeZ(location.getZ());
        country.setHomeYaw(location.getYaw());
        country.setHomePitch(location.getPitch());
        saveCountry(country);
    }

    public Location getCountryHome(Country country) {
        if (!country.hasHome()) {
            return null;
        }
        World world = getServer().getWorld(country.getHomeWorld());
        if (world == null) {
            return null;
        }
        return new Location(world, country.getHomeX(), country.getHomeY(), country.getHomeZ(), country.getHomeYaw(), country.getHomePitch());
    }

    public boolean teleportToCountryHome(Player player, Country country, boolean applyCooldown, String successMessagePath) {
        if (country == null) {
            player.sendMessage(getMessage("country.not-in-country"));
            return false;
        }
        if (!country.hasHome()) {
            player.sendMessage(getMessage("country.home.not-set"));
            return false;
        }
        if (applyCooldown) {
            long cooldownRemaining = getCountryHomeCooldownRemaining(player.getUniqueId());
            if (cooldownRemaining > 0L) {
                long seconds = (cooldownRemaining + 999L) / 1000L;
                player.sendMessage(getMessage("country.home.cooldown", placeholders("seconds", String.valueOf(seconds))));
                return false;
            }
        }

        Location home = getCountryHome(country);
        if (home == null) {
            player.sendMessage(getMessage("country.home.world-missing"));
            return false;
        }

        player.teleport(home);
        if (applyCooldown) {
            setCountryHomeCooldown(player.getUniqueId(),
                    System.currentTimeMillis() + getCountryHomeCooldownMillis(country));
        }
        player.sendMessage(getMessage(successMessagePath, placeholders("country", country.getName())));
        return true;
    }

    public void setWorldSpawn(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        dataConfig.set("world-spawn.world", location.getWorld().getName());
        dataConfig.set("world-spawn.x", location.getX());
        dataConfig.set("world-spawn.y", location.getY());
        dataConfig.set("world-spawn.z", location.getZ());
        dataConfig.set("world-spawn.yaw", location.getYaw());
        dataConfig.set("world-spawn.pitch", location.getPitch());
        saveDataConfig();
        location.getWorld().setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Location getWorldSpawnLocation() {
        String worldName = dataConfig.getString("world-spawn.world");
        if (worldName == null || worldName.isBlank()) {
            return null;
        }

        World world = getServer().getWorld(worldName);
        if (world == null) {
            return null;
        }

        return new Location(
                world,
                dataConfig.getDouble("world-spawn.x", world.getSpawnLocation().getX()),
                dataConfig.getDouble("world-spawn.y", world.getSpawnLocation().getY()),
                dataConfig.getDouble("world-spawn.z", world.getSpawnLocation().getZ()),
                (float) dataConfig.getDouble("world-spawn.yaw", 0.0D),
                (float) dataConfig.getDouble("world-spawn.pitch", 0.0D)
        );
    }

    public Location getPrimarySpawnLocation(Player player) {
        Location configuredSpawn = getWorldSpawnLocation();
        if (configuredSpawn != null) {
            return configuredSpawn;
        }
        if (!getServer().getWorlds().isEmpty()) {
            World defaultWorld = getServer().getWorlds().get(0);
            if (defaultWorld != null) {
                return defaultWorld.getSpawnLocation();
            }
        }
        return player.getWorld().getSpawnLocation();
    }

    public Location getDefaultSpawnLocation() {
        Location configuredSpawn = getWorldSpawnLocation();
        if (configuredSpawn != null) {
            return configuredSpawn;
        }
        if (!getServer().getWorlds().isEmpty()) {
            World defaultWorld = getServer().getWorlds().get(0);
            if (defaultWorld != null) {
                return defaultWorld.getSpawnLocation();
            }
        }
        return null;
    }

    public DynamicTraderState getActiveTraderState() {
        for (DynamicTraderState traderState : activeTraderStates.values()) {
            if (traderState.getDespawnAtMillis() > 0L && traderState.getDespawnAtMillis() <= System.currentTimeMillis()) {
                despawnTrader(traderState.getHostCountryKey(), true);
                continue;
            }
            return traderState;
        }
        return null;
    }

    public boolean hasActiveTrader() {
        return !activeTraderStates.isEmpty();
    }

    public NamespacedKey getTraderNpcKey() {
        return traderNpcKey;
    }

    public long getTraderNextSpawnMillis() {
        return nextTraderSpawnMillis;
    }

    public void setTraderNextSpawnDelayMinutes(long minutes) {
        nextTraderSpawnMillis = System.currentTimeMillis() + (Math.max(0L, minutes) * 60_000L);
        saveTraderData();
    }

    public double getTraderReputation(UUID playerId) {
        if (playerId == null) {
            return 0.0D;
        }
        return Math.max(0.0D, traderReputations.getOrDefault(playerId, 0.0D));
    }

    public boolean setCountryTotalTraderReputation(Country country, double totalReputation) {
        if (country == null) {
            return false;
        }

        List<UUID> members = new ArrayList<>(country.getMembers());
        if (members.isEmpty()) {
            return false;
        }

        double sanitizedTarget = roundTraderReputation(Math.max(0.0D, totalReputation));
        double currentTotal = getCountryTotalTraderReputation(country);

        if (sanitizedTarget <= 0.0D) {
            for (UUID memberId : members) {
                traderReputations.remove(memberId);
            }
            saveTraderData();
            return true;
        }

        double assigned = 0.0D;
        for (int i = 0; i < members.size(); i++) {
            UUID memberId = members.get(i);
            double memberValue;
            if (i == members.size() - 1) {
                memberValue = roundTraderReputation(Math.max(0.0D, sanitizedTarget - assigned));
            } else if (currentTotal > 0.0D) {
                memberValue = roundTraderReputation(sanitizedTarget * (getTraderReputation(memberId) / currentTotal));
            } else {
                memberValue = roundTraderReputation(sanitizedTarget / members.size());
            }

            assigned += memberValue;
            if (memberValue > 0.0D) {
                traderReputations.put(memberId, memberValue);
            } else {
                traderReputations.remove(memberId);
            }
        }

        saveTraderData();
        return true;
    }

    public TraderPlayerQuest getTraderQuest(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        for (String key : new ArrayList<>(traderQuests.keySet())) {
            if (!key.startsWith(playerId + ":")) {
                continue;
            }
            TraderPlayerQuest quest = traderQuests.get(key);
            if (quest == null) {
                continue;
            }
            if (quest.getExpiresAtMillis() > 0L && quest.getExpiresAtMillis() <= System.currentTimeMillis()) {
                traderQuests.remove(key);
                continue;
            }
            return quest;
        }
        return null;
    }

    public TraderPlayerQuest getTraderQuest(UUID playerId, DynamicTraderState traderState) {
        if (playerId == null || traderState == null) {
            return null;
        }
        String questKey = getTraderQuestKey(playerId, traderState.getTraderId());
        TraderPlayerQuest quest = traderQuests.get(questKey);
        if (quest == null) {
            return null;
        }
        if (quest.getExpiresAtMillis() > 0L && quest.getExpiresAtMillis() <= System.currentTimeMillis()) {
            traderQuests.remove(questKey);
            saveTraderData();
            return null;
        }
        return quest;
    }

    public boolean clearTraderQuest(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        boolean removed = traderQuests.entrySet().removeIf(entry -> entry.getKey().startsWith(playerId + ":"));
        if (!removed) {
            return false;
        }
        saveTraderData();
        return true;
    }

    public boolean canPlayerAcceptTraderQuest(UUID playerId, Profession profession) {
        return canPlayerAcceptTraderQuest(playerId, profession, getActiveTraderState());
    }

    public boolean canPlayerAcceptTraderQuest(UUID playerId, Profession profession, DynamicTraderState traderState) {
        if (playerId == null || profession == null || traderState == null || !hasProfession(playerId, profession)) {
            return false;
        }
        if (!canUseTrader(playerId, traderState)) {
            return false;
        }
        if (getTraderQuest(playerId, traderState) != null) {
            return false;
        }
        return getTraderQuestCooldownRemainingMillis(playerId, traderState) <= 0L;
    }

    public TraderQuestOffer previewTraderQuest(UUID playerId, Profession profession) {
        return previewTraderQuest(playerId, profession, getActiveTraderState());
    }

    public TraderQuestOffer previewTraderQuest(UUID playerId, Profession profession, DynamicTraderState traderState) {
        if (playerId == null || profession == null || traderState == null) {
            return null;
        }

        int tier = getTraderDifficultyTier(playerId, profession);
        int professionLevel = Math.max(1, getProfessionLevel(playerId, profession));
        List<Material> pool = getTraderQuestMaterialsForLevel(profession, tier, professionLevel);
        if (pool.isEmpty()) {
            return null;
        }

        long seed = traderState.getTraderId().getMostSignificantBits()
                ^ traderState.getTraderId().getLeastSignificantBits()
                ^ playerId.getMostSignificantBits()
                ^ playerId.getLeastSignificantBits()
                ^ ((long) profession.ordinal() << 32);
        Random random = new Random(seed);
        Material material = pool.get(random.nextInt(pool.size()));

        int baseAmount = getTraderQuestBaseAmount(profession);
        int amountStep = getTraderQuestAmountStep(profession);
        int amount = Math.max(1, baseAmount + ((tier - 1) * amountStep) + random.nextInt(Math.max(2, amountStep + 1)));
        double rewardMoney = scaleRewardMoney((amount * getTraderUnitMoney(material)) * (1.0D + ((tier - 1) * 0.18D)));
        int rewardXp = Math.max(4, amount * Math.max(1, tier));
        double rewardReputation = 0.10D + ((tier - 1) * 0.02D);
        if (profession == traderState.getSpecialtyProfession()) {
            rewardMoney = scaleRewardMoney(rewardMoney * 1.2D);
            rewardXp = Math.max(1, (int) Math.round(rewardXp * 1.2D));
            rewardReputation += 0.10D;
        }
        int marketNodes = countUnlockedProfessionSkillNodes(playerId, Profession.TRADER, "better_prices_i", "better_prices_ii");
        if (marketNodes > 0) {
            double multiplier = 1.0D + (marketNodes * 0.08D);
            rewardMoney = scaleRewardMoney(rewardMoney * multiplier);
            rewardXp = Math.max(1, (int) Math.round(rewardXp * multiplier));
            rewardReputation = sanitizeTraderReputationReward(rewardReputation * multiplier);
        }
        return new TraderQuestOffer(profession, material, amount, rewardMoney, rewardXp, sanitizeTraderReputationReward(rewardReputation), tier);
    }

    public TraderPlayerQuest acceptTraderQuest(UUID playerId, Profession profession) {
        return acceptTraderQuest(playerId, profession, getActiveTraderState());
    }

    public TraderPlayerQuest acceptTraderQuest(UUID playerId, Profession profession, DynamicTraderState traderState) {
        if (!canPlayerAcceptTraderQuest(playerId, profession, traderState)) {
            return null;
        }

        TraderQuestOffer offer = previewTraderQuest(playerId, profession, traderState);
        if (offer == null || traderState == null) {
            return null;
        }

        long now = System.currentTimeMillis();
        long expiresAt = now + getTraderAcceptedQuestDurationMillis();
        TraderPlayerQuest quest = new TraderPlayerQuest(
                traderState.getTraderId(),
                offer.getProfession(),
                offer.getRequestedMaterial(),
                offer.getRequestedAmount(),
                offer.getRewardMoney(),
                offer.getRewardXp(),
                offer.getRewardReputation(),
                offer.getDifficultyTier(),
                now,
                now,
                expiresAt
        );
        traderQuests.put(getTraderQuestKey(playerId, traderState.getTraderId()), quest);
        saveTraderData();
        return quest;
    }

    public boolean canTraderQuestBeDelivered(UUID playerId) {
        return canTraderQuestBeDelivered(playerId, getActiveTraderState());
    }

    public boolean canTraderQuestBeDelivered(UUID playerId, DynamicTraderState traderState) {
        TraderPlayerQuest quest = getTraderQuest(playerId, traderState);
        return quest != null;
    }

    public long getTraderQuestDeliveryRemainingMillis(UUID playerId) {
        TraderPlayerQuest quest = getTraderQuest(playerId);
        if (quest == null) {
            return 0L;
        }
        return Math.max(0L, quest.getDeliveryAvailableAtMillis() - System.currentTimeMillis());
    }

    public long getTraderQuestCooldownRemainingMillis(UUID playerId, DynamicTraderState traderState) {
        if (playerId == null || traderState == null) {
            return 0L;
        }
        long cooldownUntil = traderQuestCooldowns.getOrDefault(getTraderQuestKey(playerId, traderState.getTraderId()), 0L);
        return Math.max(0L, cooldownUntil - System.currentTimeMillis());
    }

    public boolean completeTraderQuest(Player player) {
        return completeTraderQuest(player, getActiveTraderState());
    }

    public boolean completeTraderQuest(Player player, DynamicTraderState traderState) {
        if (player == null) {
            return false;
        }

        TraderPlayerQuest quest = getTraderQuest(player.getUniqueId(), traderState);
        if (quest == null) {
            return false;
        }

        if (!removeItems(player, quest.getRequestedMaterial(), quest.getRequestedAmount())) {
            return false;
        }

        Country country = getPlayerCountry(player.getUniqueId());
        double rewardMoney = roundMoney(quest.getRewardMoney() * getCountryTraderRewardMultiplier(country));
        double rewardReputation = sanitizeTraderReputationReward(quest.getRewardReputation() * getCountryTraderRewardMultiplier(country));
        depositBalance(player.getUniqueId(), rewardMoney);
        rewardProfessionXp(player, quest.getProfession(), quest.getRewardXp());
        traderReputations.put(player.getUniqueId(), roundTraderReputation(getTraderReputation(player.getUniqueId()) + rewardReputation));
        traderQuests.remove(getTraderQuestKey(player.getUniqueId(), traderState.getTraderId()));
        traderQuestCooldowns.put(getTraderQuestKey(player.getUniqueId(), traderState.getTraderId()), System.currentTimeMillis() + getTraderDeliveryCooldownMillis());
        playTraderQuestCompleteEffect(player);
        saveTraderData();
        return true;
    }

    public TraderBigOrder getTraderBigOrder(UUID playerId) {
        return getTraderBigOrder(playerId, getActiveTraderState());
    }

    public TraderBigOrder getTraderBigOrder(UUID playerId, DynamicTraderState traderState) {
        if (playerId == null || traderState == null) {
            return null;
        }
        Country country = getPlayerCountry(playerId);
        if (country == null || !canUseTrader(playerId, traderState)) {
            return null;
        }
        return getOrCreateTraderBigOrder(traderState, country);
    }

    public boolean contributeToTraderBigOrder(Player player) {
        return contributeToTraderBigOrder(player, getActiveTraderState());
    }

    public boolean contributeToTraderBigOrder(Player player, DynamicTraderState traderState) {
        if (player == null || traderState == null) {
            return false;
        }
        Country country = getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(getMessage("country.not-in-country"));
            return false;
        }
        if (!canUseTrader(player.getUniqueId(), traderState)) {
            player.sendMessage(getMessage("terra.trader.country-not-allowed"));
            return false;
        }

        TraderBigOrder bigOrder = getOrCreateTraderBigOrder(traderState, country);
        if (bigOrder == null) {
            player.sendMessage(getMessage("terra.trader.big-order-unavailable"));
            return false;
        }
        if (bigOrder.isComplete()) {
            player.sendMessage(getMessage("terra.trader.big-order-already-complete"));
            return false;
        }

        int contributed = 0;
        for (TraderBigOrderEntry entry : bigOrder.getEntries()) {
            int removed = removeItemsUpTo(player, entry.getRequestedMaterial(), entry.getRemainingAmount());
            if (removed <= 0) {
                continue;
            }
            int applied = entry.addDeliveredAmount(removed);
            contributed += applied;
            broadcastBigOrderContribution(country, player, entry, applied);
        }

        if (contributed <= 0) {
            player.sendMessage(getMessage("terra.trader.big-order-no-items"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8F, 0.9F);
            return false;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 0.9F, 1.05F);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0.0D, 1.0D, 0.0D), 16, 0.45D, 0.55D, 0.45D, 0.02D);

        if (bigOrder.isComplete()) {
            completeTraderBigOrder(country, bigOrder, player);
        } else {
            saveTraderData();
            player.sendMessage(getMessage("terra.trader.big-order-progress", placeholders(
                    "current", String.valueOf(bigOrder.getTotalDeliveredAmount()),
                    "required", String.valueOf(bigOrder.getTotalRequestedAmount())
            )));
        }
        return true;
    }

    public void playTraderQuestAcceptEffect(Player player) {
        if (player == null) {
            return;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 0.9F, 1.15F);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6F, 1.4F);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0.0D, 1.0D, 0.0D), 12, 0.35D, 0.45D, 0.35D, 0.02D);
    }

    public void playTraderQuestCompleteEffect(Player player) {
        if (player == null) {
            return;
        }
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.85F, 1.0F);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.9F, 1.25F);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0.0D, 1.0D, 0.0D), 20, 0.45D, 0.55D, 0.45D, 0.02D);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0.0D, 1.0D, 0.0D), 16, 0.35D, 0.45D, 0.35D, 0.02D);
    }

    private TraderBigOrder getOrCreateTraderBigOrder(DynamicTraderState traderState, Country country) {
        if (country == null || traderState == null) {
            return null;
        }
        String countryKey = normalizeCountryKey(country.getName());
        String orderKey = getTraderBigOrderKey(traderState.getTraderId(), countryKey);
        TraderBigOrder existing = traderBigOrders.get(orderKey);
        if (existing != null && existing.getExpiresAtMillis() > System.currentTimeMillis()) {
            return existing;
        }

        TraderBigOrder generated = createTraderBigOrder(traderState, country);
        traderBigOrders.put(orderKey, generated);
        saveTraderData();
        return generated;
    }

    private TraderBigOrder createTraderBigOrder(DynamicTraderState traderState, Country country) {
        double averageLevel = getCountryAverageProfessionLevel(country);
        int tier = Math.max(1, Math.min(6, 1 + ((int) Math.floor((averageLevel - 1.0D) / 10.0D))));
        int effectiveLevel = Math.max(1, (int) Math.round(averageLevel));
        long seed = traderState.getTraderId().getMostSignificantBits()
                ^ traderState.getTraderId().getLeastSignificantBits()
                ^ normalizeCountryKey(country.getName()).hashCode();
        Random random = new Random(seed);
        Map<Profession, TraderBigOrderEntry> entries = new LinkedHashMap<>();
        double totalValue = 0.0D;
        for (Profession profession : Profession.values()) {
            List<Material> pool = getTraderQuestMaterialsForLevel(profession, tier, effectiveLevel);
            if (pool.isEmpty()) {
                continue;
            }
            Material material = pool.get(random.nextInt(pool.size()));
            int baseAmount = getTraderQuestBaseAmount(profession) + getTraderQuestAmountStep(profession);
            int amount = Math.max(1, baseAmount + ((tier - 1) * getTraderQuestAmountStep(profession)) + random.nextInt(Math.max(2, getTraderQuestAmountStep(profession) + 1)));
            TraderBigOrderEntry entry = new TraderBigOrderEntry(profession, material, amount, 0);
            entries.put(profession, entry);
            totalValue += amount * getTraderUnitMoney(material);
        }

        long now = System.currentTimeMillis();
        double rewardMoney = scaleRewardMoney(totalValue * (1.35D + (tier * 0.08D)));
        int rewardXp = Math.max(20, 25 * tier);
        double rewardReputation = roundTraderReputation(0.2D + (tier * 0.1D));
        return new TraderBigOrder(
                traderState.getTraderId(),
                normalizeCountryKey(country.getName()),
                tier,
                rewardMoney,
                rewardXp,
                rewardReputation,
                now,
                traderState.getDespawnAtMillis(),
                entries,
                0L
        );
    }

    private void broadcastBigOrderContribution(Country country, Player contributor, TraderBigOrderEntry entry, int amount) {
        if (country == null || contributor == null || entry == null || amount <= 0) {
            return;
        }
        String message = getMessage("terra.trader.big-order-member-update", placeholders(
                "player", contributor.getName(),
                "country", country.getName(),
                "profession", getProfessionPlainDisplayName(entry.getProfession()),
                "amount", String.valueOf(amount),
                "item", formatMaterialName(entry.getRequestedMaterial()),
                "current", String.valueOf(entry.getDeliveredAmount()),
                "required", String.valueOf(entry.getRequestedAmount())
        ));
        for (UUID memberId : country.getMembers()) {
            Player member = getServer().getPlayer(memberId);
            if (member != null) {
                member.sendMessage(message);
            }
        }
    }

    private void completeTraderBigOrder(Country country, TraderBigOrder bigOrder, Player finisher) {
        double rewardMultiplier = getCountryTraderRewardMultiplier(country);
        double totalRewardMoney = roundMoney(bigOrder.getRewardMoney() * rewardMultiplier);
        double rewardReputation = sanitizeTraderReputationReward(bigOrder.getRewardReputation() * rewardMultiplier);
        bigOrder.markCompleted(System.currentTimeMillis());
        List<Player> onlineMembers = getOnlineCountryMembers(country);
        double share = onlineMembers.isEmpty() ? 0.0D : roundMoney(totalRewardMoney / onlineMembers.size());
        for (Player member : onlineMembers) {
            depositBalance(member.getUniqueId(), share);
            traderReputations.put(member.getUniqueId(), roundTraderReputation(getTraderReputation(member.getUniqueId()) + rewardReputation));
            member.sendMessage(getMessage("terra.trader.big-order-complete-member", placeholders(
                    "country", country.getName(),
                    "money", formatMoney(share),
                    "reputation", formatTraderReputation(rewardReputation)
            )));
            member.playSound(member.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8F, 1.0F);
            member.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, member.getLocation().add(0.0D, 1.0D, 0.0D), 12, 0.45D, 0.55D, 0.45D, 0.02D);
        }
        if (finisher != null) {
            finisher.sendMessage(getMessage("terra.trader.big-order-complete", placeholders(
                    "country", country.getName(),
                    "money", formatMoney(totalRewardMoney),
                    "reputation", formatTraderReputation(rewardReputation)
            )));
        }
        saveTraderData();
    }

    private List<Player> getOnlineCountryMembers(Country country) {
        List<Player> players = new ArrayList<>();
        if (country == null) {
            return players;
        }
        for (UUID memberId : country.getMembers()) {
            Player player = getServer().getPlayer(memberId);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    public int getOnlineCountryMemberCount(Country country) {
        return getOnlineCountryMembers(country).size();
    }

    public double getCountryAverageProfessionLevel(Country country) {
        if (country == null) {
            return 1.0D;
        }
        int totalLevels = 0;
        int countedLevels = 0;
        for (UUID memberId : country.getMembers()) {
            for (Profession profession : Profession.values()) {
                if (!hasProfession(memberId, profession)) {
                    continue;
                }
                totalLevels += Math.max(1, getProfessionLevel(memberId, profession));
                countedLevels++;
            }
        }
        if (countedLevels <= 0) {
            return 1.0D;
        }
        return totalLevels / (double) countedLevels;
    }

    public int getCountryLevel(Country country) {
        if (country == null) {
            return 1;
        }
        return Math.max(1, Math.min(COUNTRY_MAX_LEVEL, country.getLevel()));
    }

    public int getCountryProgressScore(Country country) {
        if (country == null) {
            return 0;
        }
        double score = getCountryUnlockScore(country)
                + getCountryMemberCountScore(country)
                + getCountryMemberLevelScore(country)
                + getCountryTradeReputationScore(country)
                + getCountryTreasuryScore(country)
                + getCountryResourceScore(country);
        return (int) Math.round(score);
    }

    public int getCountryUnlockScore(Country country) {
        if (country == null) {
            return 0;
        }
        return Math.min(40, country.getUnlockedUpgradeKeys().size() * 8);
    }

    public int getCountryMemberCountScore(Country country) {
        if (country == null) {
            return 0;
        }
        return (int) Math.round(Math.min(8.0D, country.getMembers().size() * 1.6D));
    }

    public int getCountryMemberLevelScore(Country country) {
        if (country == null) {
            return 0;
        }
        return (int) Math.round(Math.min(26.0D, getCountryAverageProfessionLevel(country) * 2.2D));
    }

    public int getCountryTradeReputationScore(Country country) {
        if (country == null) {
            return 0;
        }
        return (int) Math.round(Math.min(16.0D, Math.sqrt(getCountryTotalTraderReputation(country)) * 2.5D));
    }

    public int getCountryTreasuryScore(Country country) {
        if (country == null) {
            return 0;
        }
        return (int) Math.round(Math.min(15.0D, Math.log10(country.getTreasuryBalance() + 1.0D) * 6.0D));
    }

    public int getCountryResourceScore(Country country) {
        if (country == null) {
            return 0;
        }
        return (int) Math.round(Math.min(15.0D, Math.sqrt(country.getResourceStockpile()) * 0.9D));
    }

    public double getCountryTotalTraderReputation(Country country) {
        if (country == null) {
            return 0.0D;
        }
        double total = 0.0D;
        for (UUID memberId : country.getMembers()) {
            total += getTraderReputation(memberId);
        }
        return total;
    }

    public int getCountryMilestonePoints(Country country) {
        if (country == null) {
            return 0;
        }
        int total = 0;
        for (UUID memberId : country.getMembers()) {
            total += getPlayerMilestonePoints(memberId);
        }
        return total;
    }

    public int getPlayerMilestonePoints(UUID playerId) {
        int total = 0;
        for (Profession profession : getOwnedProfessions(playerId)) {
            total += getProfessionMilestoneCount(playerId, profession);
        }
        return total;
    }

    public int getProfessionMilestoneCount(UUID playerId, Profession profession) {
        if (playerId == null || profession == null) {
            return 0;
        }
        int level = getProfessionLevel(playerId, profession);
        int total = 0;
        for (int milestoneLevel : COUNTRY_MILESTONE_LEVELS) {
            if (level >= milestoneLevel) {
                total++;
            }
        }
        return total;
    }

    public CountryRole getCountryRole(Country country, UUID playerId) {
        if (country == null || playerId == null || !country.getMembers().contains(playerId)) {
            return null;
        }
        if (country.hasOwner() && playerId.equals(country.getOwnerId())) {
            return CountryRole.OWNER;
        }
        if (country.getCoOwners().contains(playerId)) {
            return CountryRole.CO_OWNER;
        }
        if (country.getStewards().contains(playerId)) {
            return CountryRole.STEWARD;
        }
        return CountryRole.MEMBER;
    }

    public boolean isCountryOwner(Country country, UUID playerId) {
        return country != null && playerId != null && country.hasOwner() && playerId.equals(country.getOwnerId());
    }

    public boolean isCountryCoOwner(Country country, UUID playerId) {
        return country != null && playerId != null && country.getCoOwners().contains(playerId);
    }

    public boolean isCountrySteward(Country country, UUID playerId) {
        return country != null && playerId != null && country.getStewards().contains(playerId);
    }

    public boolean canManageCountry(Country country, UUID playerId) {
        return isCountryOwner(country, playerId) || isCountryCoOwner(country, playerId);
    }

    public boolean canInviteToCountry(Country country, UUID playerId) {
        return canManageCountry(country, playerId) || isCountrySteward(country, playerId);
    }

    public boolean canUnlockCountryProgress(Country country, UUID playerId) {
        return canManageCountry(country, playerId);
    }

    public int getCountryTargetScoreForLevel(int level) {
        if (level <= 1) {
            return 0;
        }
        int index = Math.max(1, Math.min(COUNTRY_LEVEL_SCORE_THRESHOLDS.length - 1, level - 1));
        return COUNTRY_LEVEL_SCORE_THRESHOLDS[index];
    }

    public double getCountryLevelUpBalanceCost(int targetLevel) {
        int index = Math.max(1, Math.min(COUNTRY_LEVEL_UP_BALANCE_COSTS.length - 1, targetLevel - 1));
        return COUNTRY_LEVEL_UP_BALANCE_COSTS[index];
    }

    public int getCountryLevelUpResourceCost(int targetLevel) {
        int index = Math.max(1, Math.min(COUNTRY_LEVEL_UP_RESOURCE_COSTS.length - 1, targetLevel - 1));
        return COUNTRY_LEVEL_UP_RESOURCE_COSTS[index];
    }

    public boolean isCountryLevelPageComplete(Country country, int pageLevel) {
        if (country == null) {
            return false;
        }
        for (String key : getCountryProgressKeysForPage(pageLevel)) {
            if (!isCountryProgressKeyUnlocked(country, key)) {
                return false;
            }
        }
        return true;
    }

    public List<String> getCountryProgressKeysForPage(int pageLevel) {
        List<String> keys = new ArrayList<>();
        if (pageLevel < 1 || pageLevel > COUNTRY_MAX_LEVEL) {
            return keys;
        }
        int bufferIndex = 1;
        keys.add("path_level_" + pageLevel + "_" + bufferIndex++);
        for (CountryUpgrade upgrade : CountryUpgrade.values()) {
            if (upgrade.getRequiredCountryLevel() != pageLevel) {
                continue;
            }
            keys.add(upgrade.getKey());
            if (keys.size() < 9) {
                keys.add("path_level_" + pageLevel + "_" + bufferIndex++);
            }
        }
        while (keys.size() < 9) {
            keys.add("path_level_" + pageLevel + "_" + bufferIndex++);
        }
        return keys;
    }

    public boolean canLevelUpCountry(Country country) {
        if (country == null) {
            return false;
        }
        int currentLevel = getCountryLevel(country);
        if (currentLevel >= COUNTRY_MAX_LEVEL) {
            return false;
        }
        int targetLevel = currentLevel + 1;
        if (!isCountryLevelPageComplete(country, currentLevel)) {
            return false;
        }
        if (getCountryProgressScore(country) < getCountryTargetScoreForLevel(targetLevel)) {
            return false;
        }
        if (country.getTreasuryBalance() + 0.0001D < getCountryLevelUpBalanceCost(targetLevel)) {
            return false;
        }
        return country.getResourceStockpile() >= getCountryLevelUpResourceCost(targetLevel);
    }

    public boolean levelUpCountry(Country country) {
        if (!canLevelUpCountry(country)) {
            return false;
        }
        int targetLevel = getCountryLevel(country) + 1;
        country.setTreasuryBalance(roundMoney(Math.max(0.0D, country.getTreasuryBalance() - getCountryLevelUpBalanceCost(targetLevel))));
        country.setResourceStockpile(Math.max(0, country.getResourceStockpile() - getCountryLevelUpResourceCost(targetLevel)));
        country.setLevel(targetLevel);
        saveCountry(country);
        refreshCountryProgressionState(country);
        return true;
    }

    public boolean setCountryRole(Country country, UUID targetId, CountryRole role) {
        if (country == null || targetId == null || role == null || !country.getMembers().contains(targetId)) {
            return false;
        }
        if (isCountryOwner(country, targetId) && role != CountryRole.OWNER) {
            return false;
        }
        country.getCoOwners().remove(targetId);
        country.getStewards().remove(targetId);
        if (role == CountryRole.CO_OWNER) {
            country.getCoOwners().add(targetId);
        } else if (role == CountryRole.STEWARD) {
            country.getStewards().add(targetId);
        }
        saveCountry(country);
        return true;
    }

    public boolean isCountryUpgradeUnlocked(Country country, CountryUpgrade upgrade) {
        return country != null && upgrade != null && country.getUnlockedUpgradeKeys().contains(upgrade.getKey());
    }

    public boolean isCountryProgressKeyUnlocked(Country country, String key) {
        if (country == null || key == null || key.isBlank()) {
            return false;
        }
        return country.getUnlockedUpgradeKeys().contains(key);
    }

    public boolean canUnlockCountryProgressKey(Country country, String key, String prerequisiteKey, int requiredCountryLevel,
                                               double balanceCost, int resourceCost, Map<Profession, Integer> professionRequirements) {
        if (country == null || key == null || key.isBlank() || isCountryProgressKeyUnlocked(country, key)) {
            return false;
        }
        if (prerequisiteKey != null && !prerequisiteKey.isBlank() && !isCountryProgressKeyUnlocked(country, prerequisiteKey)) {
            return false;
        }
        if (getCountryLevel(country) < requiredCountryLevel) {
            return false;
        }
        if (country.getTreasuryBalance() + 0.0001D < balanceCost) {
            return false;
        }
        if (professionRequirements != null) {
            for (Map.Entry<Profession, Integer> requirement : professionRequirements.entrySet()) {
                if (!hasCountryMemberAtProfessionLevel(country, requirement.getKey(), requirement.getValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean canUnlockCountryUpgrade(Country country, CountryUpgrade upgrade) {
        if (country == null || upgrade == null || isCountryUpgradeUnlocked(country, upgrade)) {
            return false;
        }
        CountryUpgrade prerequisite = upgrade.getPrerequisite();
        return canUnlockCountryProgressKey(
                country,
                upgrade.getKey(),
                prerequisite != null ? prerequisite.getKey() : null,
                upgrade.getRequiredCountryLevel(),
                upgrade.getBalanceCost(),
                upgrade.getResourceCost(),
                upgrade.getProfessionRequirements()
        );
    }

    public boolean unlockCountryProgressKey(Country country, String key, String prerequisiteKey, int requiredCountryLevel,
                                            double balanceCost, int resourceCost, Map<Profession, Integer> professionRequirements) {
        if (!canUnlockCountryProgressKey(country, key, prerequisiteKey, requiredCountryLevel, balanceCost, resourceCost, professionRequirements)) {
            return false;
        }
        country.setTreasuryBalance(roundMoney(Math.max(0.0D, country.getTreasuryBalance() - balanceCost)));
        boolean added = country.getUnlockedUpgradeKeys().add(key);
        if (added) {
            saveCountry(country);
        }
        return added;
    }

    public boolean unlockCountryUpgrade(Country country, CountryUpgrade upgrade) {
        if (!canUnlockCountryUpgrade(country, upgrade)) {
            return false;
        }
        CountryUpgrade prerequisite = upgrade.getPrerequisite();
        return unlockCountryProgressKey(
                country,
                upgrade.getKey(),
                prerequisite != null ? prerequisite.getKey() : null,
                upgrade.getRequiredCountryLevel(),
                upgrade.getBalanceCost(),
                upgrade.getResourceCost(),
                upgrade.getProfessionRequirements()
        );
    }

    public boolean hasCountryMemberAtProfessionLevel(Country country, Profession profession, int level) {
        if (country == null || profession == null) {
            return false;
        }
        for (UUID memberId : country.getMembers()) {
            if (hasProfession(memberId, profession) && getProfessionLevel(memberId, profession) >= level) {
                return true;
            }
        }
        return false;
    }

    public int getCountryFarmlandBonus(Country country) {
        int bonus = 0;
        if (isCountryUpgradeUnlocked(country, CountryUpgrade.GREEN_FIELDS_I)) {
            bonus += 8;
        }
        if (isCountryUpgradeUnlocked(country, CountryUpgrade.GREEN_FIELDS_II)) {
            bonus += 16;
        }
        return bonus;
    }

    public long getCountryHomeCooldownMillis(Country country) {
        long base = Math.max(0L, getConfig().getLong("country-home.cooldown-seconds", 3600L) * 1000L);
        if (isCountryUpgradeUnlocked(country, CountryUpgrade.HOME_NETWORK)) {
            base = Math.max(0L, base - (10L * 60_000L));
        }
        return base;
    }

    public double getCountryTraderRewardMultiplier(Country country) {
        double multiplier = 1.0D;
        if (isCountryUpgradeUnlocked(country, CountryUpgrade.TRADE_LEDGER)) {
            multiplier += 0.05D;
        }
        if (isCountryUpgradeUnlocked(country, CountryUpgrade.GRAND_EXCHANGE)) {
            multiplier += 0.10D;
        }
        return multiplier;
    }

    public double getCountryPassiveMoneyMultiplier(Country country) {
        if (country == null) {
            return 1.0D;
        }
        double multiplier = 1.0D;
        if (isCountryUpgradeUnlocked(country, CountryUpgrade.COIN_VAULT_I)) {
            multiplier += 0.05D;
        }
        if (isCountryUpgradeUnlocked(country, CountryUpgrade.COIN_VAULT_II)) {
            multiplier += 0.10D;
        }
        return multiplier;
    }

    public long getMerchantTradeCooldownMillis(Country country) {
        long base = getMerchantTradeCooldownMillis();
        if (isCountryUpgradeUnlocked(country, CountryUpgrade.MARKET_PERMIT)) {
            base = Math.max(1_000L, base - 5_000L);
        }
        return base;
    }

    public long getMerchantTradeCooldownMillis(UUID playerId, Country country) {
        long base = getMerchantTradeCooldownMillis(country);
        int logisticsNodes = countUnlockedProfessionSkillNodes(playerId, Profession.TRADER, "logistics_i", "logistics_ii");
        return Math.max(1_000L, base - (logisticsNodes * 2_500L));
    }

    public int getCountryMinerCooldownReduction(Country country) {
        int reduction = 0;
        if (isCountryUpgradeUnlocked(country, CountryUpgrade.MINERS_TUNNELS_I)) {
            reduction += 1;
        }
        if (isCountryUpgradeUnlocked(country, CountryUpgrade.MINERS_TUNNELS_II)) {
            reduction += 1;
        }
        return reduction;
    }

    public int getCountryBuilderCooldownReduction(Country country) {
        int reduction = 0;
        if (isCountryUpgradeUnlocked(country, CountryUpgrade.BUILDERS_GUILD)) {
            reduction += 1;
        }
        if (isCountryUpgradeUnlocked(country, CountryUpgrade.HOME_NETWORK)) {
            reduction += 1;
        }
        return reduction;
    }

    public double getCountryPassiveProfessionXpMultiplier(Country country, Profession profession) {
        if (country == null || profession == null) {
            return 1.0D;
        }
        double multiplier = 1.0D;
        if (profession == Profession.BUILDER && isCountryUpgradeUnlocked(country, CountryUpgrade.BUILDERS_GUILD)) {
            multiplier += 0.05D;
        }
        if (profession == Profession.FARMER && isCountryUpgradeUnlocked(country, CountryUpgrade.HARVEST_CIRCLE)) {
            multiplier += 0.05D;
        }
        if (profession == Profession.LUMBERJACK && isCountryUpgradeUnlocked(country, CountryUpgrade.TIMBER_YARD)) {
            multiplier += 0.05D;
        }
        if (profession == Profession.BLACKSMITH && isCountryUpgradeUnlocked(country, CountryUpgrade.FORGE_QUARTERS)) {
            multiplier += 0.05D;
        }
        if (isCountryUpgradeUnlocked(country, CountryUpgrade.LEARNING_HALL_I)) {
            multiplier += 0.05D;
        }
        if (isCountryUpgradeUnlocked(country, CountryUpgrade.GRAND_EXCHANGE)) {
            multiplier += 0.05D;
        }
        return multiplier;
    }

    public int getCountryMaxLevel() {
        return COUNTRY_MAX_LEVEL;
    }

    public boolean contributeCountryResourcesFromHand(Player player, Country country, int amount) {
        if (player == null || country == null) {
            return false;
        }
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || mainHand.getType().isAir()) {
            return false;
        }
        int requested = amount > 0 ? amount : mainHand.getAmount();
        int removed = removeItemsUpTo(player, mainHand.getType(), Math.min(requested, mainHand.getAmount()));
        if (removed <= 0) {
            return false;
        }
        country.setResourceStockpile(Math.max(0, country.getResourceStockpile() + removed));
        saveCountry(country);
        return true;
    }

    private String normalizeCountryBoostKey(String boostKey) {
        if (boostKey == null || boostKey.isBlank()) {
            return null;
        }
        String normalized = boostKey.toLowerCase(Locale.ROOT);
        if (normalized.equals("all")) {
            return normalized;
        }
        Profession profession = Profession.fromKey(normalized);
        return profession != null ? profession.getKey() : null;
    }

    private double getCountryBoostBalanceCost(String boostKey) {
        return "all".equalsIgnoreCase(boostKey) ? scalePriceMoney(600.0D) : scalePriceMoney(300.0D);
    }

    private int getCountryBoostResourceCost(String boostKey) {
        return "all".equalsIgnoreCase(boostKey) ? 90 : 45;
    }

    private void expireCountryBoostIfNeeded(Country country) {
        if (country == null || country.getActiveBoostKey() == null) {
            return;
        }
        if (country.getActiveBoostUntilMillis() > System.currentTimeMillis()) {
            return;
        }
        country.setActiveBoostKey(null);
        country.setActiveBoostUntilMillis(0L);
        saveCountry(country);
    }

    private void refreshCountryProgressionState(Country country) {
        if (country == null) {
            return;
        }
        if (getCountryLevel(country) >= 2) {
            removeCountryFixedOres(country);
        }
    }

    private void removeCountryFixedOres(Country country) {
        if (country == null || fixedOreBlocks.isEmpty()) {
            return;
        }
        boolean changed = false;
        Iterator<Map.Entry<PlacedBlockKey, Material>> iterator = fixedOreBlocks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<PlacedBlockKey, Material> entry = iterator.next();
            PlacedBlockKey blockKey = entry.getKey();
            World world = getServer().getWorld(blockKey.worldId);
            if (world == null) {
                continue;
            }
            Block block = world.getBlockAt(blockKey.x, blockKey.y, blockKey.z);
            Country blockCountry = getCountryAt(block.getLocation());
            if (blockCountry == null || !blockCountry.getName().equalsIgnoreCase(country.getName())) {
                continue;
            }
            iterator.remove();
            Material oreType = entry.getValue();
            Material placeholder = getFixedOrePlaceholder(oreType);
            if (block.getType() == oreType || block.getType() == placeholder) {
                block.setType(Material.AIR, false);
            }
            changed = true;
        }
        if (changed) {
            saveFixedOreBlocks();
        }
    }

    public double getCountryBalance(Country country) {
        return country != null ? country.getTreasuryBalance() : 0.0D;
    }

    public int getCountryResources(Country country) {
        return country != null ? Math.max(0, country.getResourceStockpile()) : 0;
    }

    public boolean depositCountryBalance(Country country, double amount) {
        if (country == null || amount <= 0.0D) {
            return false;
        }
        country.setTreasuryBalance(roundMoney(country.getTreasuryBalance() + amount));
        saveCountry(country);
        return true;
    }

    public boolean withdrawCountryBalance(Country country, double amount) {
        if (country == null || amount <= 0.0D || country.getTreasuryBalance() + 0.0001D < amount) {
            return false;
        }
        country.setTreasuryBalance(roundMoney(Math.max(0.0D, country.getTreasuryBalance() - amount)));
        saveCountry(country);
        return true;
    }

    public boolean addCountryResources(Country country, int amount) {
        if (country == null || amount <= 0) {
            return false;
        }
        country.setResourceStockpile(Math.max(0, country.getResourceStockpile() + amount));
        saveCountry(country);
        return true;
    }

    public boolean spendCountryResources(Country country, int amount) {
        if (country == null || amount <= 0 || country.getResourceStockpile() < amount) {
            return false;
        }
        country.setResourceStockpile(Math.max(0, country.getResourceStockpile() - amount));
        saveCountry(country);
        return true;
    }

    public boolean activateCountryBoost(Country country, String boostKey) {
        if (country == null) {
            return false;
        }
        String normalizedKey = normalizeCountryBoostKey(boostKey);
        if (normalizedKey == null) {
            return false;
        }
        double balanceCost = getCountryBoostBalanceCost(normalizedKey);
        int resourceCost = getCountryBoostResourceCost(normalizedKey);
        if (country.getTreasuryBalance() + 0.0001D < balanceCost || country.getResourceStockpile() < resourceCost) {
            return false;
        }

        country.setTreasuryBalance(roundMoney(Math.max(0.0D, country.getTreasuryBalance() - balanceCost)));
        country.setResourceStockpile(Math.max(0, country.getResourceStockpile() - resourceCost));
        country.setActiveBoostKey(normalizedKey);
        country.setActiveBoostUntilMillis(System.currentTimeMillis() + COUNTRY_BOOST_DURATION_MILLIS);
        saveCountry(country);
        return true;
    }

    public String getCountryActiveBoostDisplay(Country country) {
        expireCountryBoostIfNeeded(country);
        if (country == null || country.getActiveBoostKey() == null || country.getActiveBoostUntilMillis() <= System.currentTimeMillis()) {
            return "None";
        }
        return switch (country.getActiveBoostKey()) {
            case "all" -> "All Jobs XP";
            case "miner", "lumberjack", "farmer", "builder", "blacksmith" ->
                    getProfessionPlainDisplayName(Profession.fromKey(country.getActiveBoostKey())) + " XP";
            default -> "None";
        };
    }

    public long getCountryActiveBoostRemainingMillis(Country country) {
        expireCountryBoostIfNeeded(country);
        if (country == null) {
            return 0L;
        }
        return Math.max(0L, country.getActiveBoostUntilMillis() - System.currentTimeMillis());
    }

    public double getCountryProfessionXpMultiplier(Country country, Profession profession) {
        expireCountryBoostIfNeeded(country);
        if (country == null || profession == null) {
            return 1.0D;
        }
        String boostKey = country.getActiveBoostKey();
        if (boostKey == null || country.getActiveBoostUntilMillis() <= System.currentTimeMillis()) {
            return 1.0D;
        }
        if (boostKey.equals("all")) {
            return COUNTRY_ALL_XP_BOOST_MULTIPLIER;
        }
        if (boostKey.equalsIgnoreCase(profession.getKey())) {
            return COUNTRY_JOB_XP_BOOST_MULTIPLIER;
        }
        return 1.0D;
    }

    public boolean isCountryChatEnabled(UUID playerId) {
        return playerId != null && countryChatEnabledPlayers.contains(playerId);
    }

    public void setCountryChatEnabled(UUID playerId, boolean enabled) {
        if (playerId == null) {
            return;
        }
        if (enabled) {
            countryChatEnabledPlayers.add(playerId);
        } else {
            countryChatEnabledPlayers.remove(playerId);
        }
    }

    public boolean isGlobalChatEnabled(UUID playerId) {
        return playerId != null && !globalChatDisabledPlayers.contains(playerId);
    }

    public void setGlobalChatEnabled(UUID playerId, boolean enabled) {
        if (playerId == null) {
            return;
        }
        if (enabled) {
            globalChatDisabledPlayers.remove(playerId);
            return;
        }
        globalChatDisabledPlayers.add(playerId);
    }

    public long getGlobalChatCooldownMillis() {
        return Math.max(0L, chatConfig.getLong("global.cooldown-minutes", 5L)) * 60_000L;
    }

    public double getChatLocalRadiusBlocks() {
        return Math.max(1.0D, chatConfig.getDouble("local.radius-blocks", 50.0D));
    }

    public long getGlobalChatCooldownRemaining(UUID playerId) {
        if (playerId == null) {
            return 0L;
        }
        long lastSentAt = globalChatCooldowns.getOrDefault(playerId, 0L);
        long remaining = (lastSentAt + getGlobalChatCooldownMillis()) - System.currentTimeMillis();
        return Math.max(0L, remaining);
    }

    public void sendCountryChat(Player sender, String message) {
        if (sender == null || message == null || message.isBlank()) {
            return;
        }

        Country country = getPlayerCountry(sender.getUniqueId());
        if (country == null) {
            return;
        }

        Component formatted = buildPlayerChatComponent("formats.country", sender, placeholders(
                "country", country.getName(),
                "country_tag", getPlayerCountryTagValue(sender.getUniqueId()),
                "player", sender.getName(),
                "message", message
        ));
        List<Player> recipients = getOnlineCountryMembers(country);
        for (Player recipient : recipients) {
            recipient.sendMessage(formatted);
        }
        playChatMessageSoundToRecipients(sender, recipients);
    }

    public void sendGlobalChat(Player sender, String message) {
        if (sender == null || message == null || message.isBlank()) {
            return;
        }

        globalChatCooldowns.put(sender.getUniqueId(), System.currentTimeMillis());
        Country country = getPlayerCountry(sender.getUniqueId());
        Component formatted = buildPlayerChatComponent("formats.global", sender, placeholders(
                "country", country != null ? country.getName() : "No Country",
                "country_tag", getPlayerCountryTagValue(sender.getUniqueId()),
                "player", sender.getName(),
                "message", message
        ));
        List<Player> recipients = new ArrayList<>(getServer().getOnlinePlayers());
        for (Player recipient : recipients) {
            recipient.sendMessage(formatted);
        }
        playChatMessageSoundToRecipients(sender, recipients);
    }

    public void sendLocalChat(Player sender, String message, double radiusBlocks) {
        if (sender == null || message == null || message.isBlank()) {
            return;
        }

        Country country = getPlayerCountry(sender.getUniqueId());
        Component formatted = buildPlayerChatComponent("formats.local", sender, placeholders(
                "country", country != null ? country.getName() : "No Country",
                "country_tag", getPlayerCountryTagValue(sender.getUniqueId()),
                "player", sender.getName(),
                "message", message
        ));
        List<Player> recipients = new ArrayList<>();
        double maxDistanceSquared = radiusBlocks * radiusBlocks;
        for (Player recipient : getServer().getOnlinePlayers()) {
            if (!recipient.getWorld().equals(sender.getWorld())) {
                continue;
            }
            if (recipient.getLocation().distanceSquared(sender.getLocation()) > maxDistanceSquared) {
                continue;
            }
            recipients.add(recipient);
            recipient.sendMessage(formatted);
        }
        playChatMessageSoundToRecipients(sender, recipients);
    }

    private String getTraderBigOrderKey(UUID traderId, String countryKey) {
        return traderId + ":" + normalizeCountryKey(countryKey);
    }

    private String getTraderQuestKey(UUID playerId, UUID traderId) {
        return playerId + ":" + traderId;
    }

    private void resetTraderCycleState() {
        traderQuests.clear();
        traderQuestCooldowns.clear();
        traderBigOrders.clear();
    }

    public boolean spawnDynamicTraderNow() {
        return spawnDynamicTraderNow(null);
    }

    public boolean spawnDynamicTraderNow(Profession forcedSpecialty) {
        if (!isTraderSystemEnabled()) {
            return false;
        }
        removeOrphanTraderNpcs();
        resetTraderCycleState();
        if (!activeTraderStates.isEmpty()) {
            despawnActiveTrader(false);
        }
        Country hostCountry = selectNextTraderHostCountry();
        if (hostCountry == null) {
            saveTraderData();
            return false;
        }
        long spawnedAtMillis = System.currentTimeMillis();
        long despawnAtMillis = spawnedAtMillis + getTraderActiveDurationMillis();
        boolean spawnedAny = spawnTraderForCountry(hostCountry, forcedSpecialty, true, spawnedAtMillis, despawnAtMillis, true);
        if (spawnedAny || !activeTraderStates.isEmpty()) {
            nextTraderSpawnMillis = 0L;
        }
        saveTraderData();
        return spawnedAny || !activeTraderStates.isEmpty();
    }

    public boolean despawnActiveTrader(boolean announce) {
        if (activeTraderStates.isEmpty()) {
            removeOrphanTraderNpcs();
            return false;
        }
        List<String> countryKeys = new ArrayList<>(activeTraderStates.keySet());
        for (String countryKey : countryKeys) {
            despawnTrader(countryKey, announce);
        }
        scheduleNextTraderSpawn(false);
        removeOrphanTraderNpcs();
        saveTraderData();
        return true;
    }

    public boolean setActiveTraderRemainingMinutes(long minutes) {
        if (activeTraderStates.isEmpty()) {
            return false;
        }
        long clampedMinutes = Math.max(0L, minutes);
        if (clampedMinutes == 0L) {
            return despawnActiveTrader(false);
        }
        for (Map.Entry<String, DynamicTraderState> entry : new ArrayList<>(activeTraderStates.entrySet())) {
            DynamicTraderState traderState = entry.getValue();
            activeTraderStates.put(entry.getKey(), new DynamicTraderState(
                    traderState.getTraderId(),
                    traderState.getEntityId(),
                    traderState.getHostCountryKey(),
                    traderState.getTraderName(),
                    traderState.getSpecialtyProfession(),
                    traderState.getWorldName(),
                    traderState.getX(),
                    traderState.getY(),
                    traderState.getZ(),
                    traderState.getYaw(),
                    traderState.getPitch(),
                    traderState.getSpawnedAtMillis(),
                    System.currentTimeMillis() + (clampedMinutes * 60_000L)
            ));
        }
        saveTraderData();
        return true;
    }

    private DynamicTraderState getSharedTraderCycleState() {
        DynamicTraderState earliest = null;
        for (DynamicTraderState traderState : activeTraderStates.values()) {
            if (traderState == null) {
                continue;
            }
            if (earliest == null || traderState.getSpawnedAtMillis() < earliest.getSpawnedAtMillis()) {
                earliest = traderState;
            }
        }
        return earliest;
    }

    public boolean isTraderNpc(Entity entity) {
        if (entity == null || traderNpcKey == null) {
            return false;
        }
        if (!entity.getPersistentDataContainer().has(traderNpcKey, PersistentDataType.BYTE)) {
            return false;
        }
        return getTraderState(entity) != null;
    }

    public DynamicTraderState getTraderState(Entity entity) {
        if (entity == null) {
            return null;
        }
        return getTraderState(entity.getUniqueId());
    }

    public DynamicTraderState getTraderState(UUID entityId) {
        if (entityId == null) {
            return null;
        }
        for (DynamicTraderState traderState : activeTraderStates.values()) {
            if (entityId.equals(traderState.getEntityId())) {
                return traderState;
            }
        }
        return null;
    }

    public void openTraderMenu(Player player) {
        if (traderQuestListener != null) {
            traderQuestListener.openTraderMenu(player);
        }
    }

    public void openTraderMenu(Player player, DynamicTraderState traderState) {
        if (traderQuestListener != null) {
            traderQuestListener.openTraderMenu(player, traderState);
        }
    }

    public boolean canUseActiveTrader(UUID playerId) {
        return canUseTrader(playerId, getActiveTraderState());
    }

    public boolean canUseTrader(UUID playerId, DynamicTraderState traderState) {
        if (playerId == null || traderState == null) {
            return false;
        }
        Country playerCountry = getPlayerCountry(playerId);
        if (playerCountry == null) {
            return false;
        }
        String playerCountryKey = normalizeCountryKey(playerCountry.getName());
        if (playerCountryKey.equalsIgnoreCase(traderState.getHostCountryKey())) {
            return true;
        }
        Country hostCountry = getCountryByKey(traderState.getHostCountryKey());
        return hostCountry != null && hostCountry.getAllowedTradeCountries().contains(playerCountryKey);
    }

    public Country getActiveTraderHostCountry() {
        return getTraderHostCountry(getActiveTraderState());
    }

    public Country getTraderHostCountry(DynamicTraderState traderState) {
        return traderState != null ? getCountryByKey(traderState.getHostCountryKey()) : null;
    }

    public DynamicTraderState getTraderStateByTraderId(UUID traderId) {
        if (traderId == null) {
            return null;
        }
        for (DynamicTraderState traderState : activeTraderStates.values()) {
            if (traderState != null && traderId.equals(traderState.getTraderId())) {
                return traderState;
            }
        }
        return null;
    }

    public int getActiveTraderCount() {
        return activeTraderStates.size();
    }

    private boolean spawnTraderForCountry(Country hostCountry, Profession forcedSpecialty, boolean replaceExisting) {
        long now = System.currentTimeMillis();
        return spawnTraderForCountry(
                hostCountry,
                forcedSpecialty,
                replaceExisting,
                now,
                now + getTraderActiveDurationMillis(),
                true
        );
    }

    private boolean spawnTraderForCountry(
            Country hostCountry,
            Profession forcedSpecialty,
            boolean replaceExisting,
            long spawnedAtMillis,
            long despawnAtMillis,
            boolean announce
    ) {
        if (hostCountry == null) {
            return false;
        }
        String countryKey = normalizeCountryKey(hostCountry.getName());
        DynamicTraderState existing = activeTraderStates.get(countryKey);
        if (existing != null && !replaceExisting) {
            Entity existingEntity = getServer().getEntity(existing.getEntityId());
            if (existingEntity instanceof Villager villager && villager.isValid()) {
                configureTraderNpc(villager, existing);
                return false;
            }
        }
        if (existing != null) {
            removeTraderEntity(existing);
            activeTraderStates.remove(countryKey);
            clearTraderCycleState(existing.getTraderId(), countryKey);
        }

        Location spawnLocation = getCountryTraderSpawnLocation(hostCountry);
        if (spawnLocation == null || spawnLocation.getWorld() == null) {
            return false;
        }

        Profession specialtyProfession = forcedSpecialty != null ? forcedSpecialty : selectTraderSpecialty(hostCountry);
        String traderName = generateTraderName(specialtyProfession);
        Location npcLocation = spawnLocation.clone();
        npcLocation.setY(Math.max(npcLocation.getY(), npcLocation.getWorld().getHighestBlockYAt(
                npcLocation.getBlockX(),
                npcLocation.getBlockZ(),
                HeightMap.MOTION_BLOCKING_NO_LEAVES
        ) + 1.0D));
        npcLocation.setPitch(0.0F);

        Villager villager = (Villager) npcLocation.getWorld().spawnEntity(npcLocation, EntityType.VILLAGER);
        DynamicTraderState traderState = new DynamicTraderState(
                UUID.randomUUID(),
                villager.getUniqueId(),
                countryKey,
                traderName,
                specialtyProfession,
                npcLocation.getWorld().getName(),
                npcLocation.getX(),
                npcLocation.getY(),
                npcLocation.getZ(),
                npcLocation.getYaw(),
                0.0F,
                spawnedAtMillis,
                despawnAtMillis
        );
        activeTraderStates.put(countryKey, traderState);
        configureTraderNpc(villager, traderState);
        hostCountry.setLastTraderName(traderName);
        hostCountry.setLastTraderSpecialty(specialtyProfession.getKey());
        hostCountry.setLastTraderSeenAtMillis(spawnedAtMillis);
        saveCountry(hostCountry);
        if (announce) {
            broadcastTraderRouteMessage(hostCountry, getMessage("terra.trader.arrived", placeholders(
                    "country", hostCountry.getName(),
                    "name", traderName,
                    "profession", getProfessionPlainDisplayName(specialtyProfession),
                    "time", formatLongDurationWords(Math.max(1000L, despawnAtMillis - System.currentTimeMillis()))
            )));
        }
        return true;
    }

    private boolean despawnTrader(String countryKey, boolean announce) {
        DynamicTraderState traderState = activeTraderStates.remove(countryKey);
        if (traderState == null) {
            return false;
        }
        removeTraderEntity(traderState);
        clearTraderCycleState(traderState.getTraderId(), countryKey);
        if (announce) {
            Country hostCountry = getCountryByKey(countryKey);
            broadcastTraderRouteMessage(hostCountry, getMessage("terra.trader.departed", placeholders(
                    "time", "soon"
            )));
        }
        return true;
    }

    private void clearTraderCycleState(UUID traderId, String countryKey) {
        if (traderId == null) {
            return;
        }
        traderQuests.entrySet().removeIf(entry -> entry.getValue() != null && traderId.equals(entry.getValue().getTraderId()));
        traderQuestCooldowns.entrySet().removeIf(entry -> entry.getKey().endsWith(":" + traderId));
        String normalizedCountryKey = countryKey != null ? countryKey.toLowerCase(Locale.ROOT) : null;
        traderBigOrders.entrySet().removeIf(entry -> {
            TraderBigOrder order = entry.getValue();
            if (order == null || !traderId.equals(order.getTraderId())) {
                return false;
            }
            return normalizedCountryKey == null || normalizedCountryKey.equals(order.getCountryKey());
        });
    }

    private void broadcastTraderRouteMessage(Country hostCountry, String message) {
        if (hostCountry == null || message == null || message.isBlank()) {
            return;
        }
        Set<UUID> recipientIds = new LinkedHashSet<>(hostCountry.getMembers());
        for (String allowedKey : hostCountry.getAllowedTradeCountries()) {
            Country allowedCountry = getCountryByKey(allowedKey);
            if (allowedCountry != null) {
                recipientIds.addAll(allowedCountry.getMembers());
            }
        }
        for (UUID memberId : recipientIds) {
            Player player = getServer().getPlayer(memberId);
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    public boolean setCountryTraderSpawn(Country country, Location location) {
        if (country == null || location == null || location.getWorld() == null) {
            return false;
        }
        country.setTraderSpawnWorld(location.getWorld().getName());
        country.setTraderSpawnX(location.getX());
        country.setTraderSpawnY(location.getY());
        country.setTraderSpawnZ(location.getZ());
        country.setTraderSpawnYaw(location.getYaw());
        country.setTraderSpawnPitch(0.0F);
        saveCountry(country);
        return true;
    }

    public boolean addAllowedTradeCountry(Country ownerCountry, Country allowedCountry) {
        if (ownerCountry == null || allowedCountry == null || ownerCountry == allowedCountry) {
            return false;
        }
        boolean added = ownerCountry.getAllowedTradeCountries().add(normalizeCountryKey(allowedCountry.getName()));
        if (added) {
            saveCountry(ownerCountry);
        }
        return added;
    }

    public boolean removeAllowedTradeCountry(Country ownerCountry, Country allowedCountry) {
        if (ownerCountry == null || allowedCountry == null) {
            return false;
        }
        boolean removed = ownerCountry.getAllowedTradeCountries().remove(normalizeCountryKey(allowedCountry.getName()));
        if (removed) {
            saveCountry(ownerCountry);
        }
        return removed;
    }

    public boolean isMerchantSystemEnabled() {
        return getConfig().getBoolean("merchant-shop.enabled", true);
    }

    public boolean isMerchantRandomBuyRotationEnabled() {
        return getConfig().getBoolean("merchant-shop.random-buy-rotation-enabled", true);
    }

    public int getMerchantRandomBuyOffersPerRotation() {
        return Math.max(3, getConfig().getInt("merchant-shop.random-buy-offers-per-rotation", 5));
    }

    public boolean areEnderPearlsEnabled() {
        return isFunctionalMaterialEnabled(Material.ENDER_PEARL)
                && getConfig().getBoolean("items.ender-pearls-enabled", true);
    }

    public void setEnderPearlsEnabled(boolean enabled) {
        setManagedConfigValue("items.ender-pearls-enabled", enabled);
        setFunctionalMaterialEnabled(Material.ENDER_PEARL, enabled);
    }

    public boolean areShulkerBoxesEnabled() {
        if (!getConfig().getBoolean("items.shulker-boxes-enabled", true)) {
            return false;
        }
        for (Material material : Tag.SHULKER_BOXES.getValues()) {
            if (!isFunctionalMaterialEnabled(material)) {
                return false;
            }
        }
        return true;
    }

    public void setShulkerBoxesEnabled(boolean enabled) {
        setManagedConfigValue("items.shulker-boxes-enabled", enabled);
        for (Material material : Tag.SHULKER_BOXES.getValues()) {
            setFunctionalMaterialEnabled(material, enabled);
        }
    }

    public boolean isFunctionalMaterialEnabled(Material material) {
        if (!isRestrictableFunctionalMaterial(material)) {
            return true;
        }
        return getConfig().getBoolean("items.materials." + material.name(), true);
    }

    public void setFunctionalMaterialEnabled(Material material, boolean enabled) {
        if (isRestrictableFunctionalMaterial(material)) {
            setManagedConfigValue("items.materials." + material.name(), enabled);
        }
    }

    public boolean isRestrictableFunctionalMaterial(Material material) {
        if (material == null || material.isAir() || !material.isItem()) {
            return false;
        }
        return isFunctionalMeterMaterial(material) || isExtraRestrictableUtilityMaterial(material);
    }

    public List<String> getRestrictableFunctionalMaterialNames() {
        List<String> names = new ArrayList<>();
        for (Material material : Material.values()) {
            if (isRestrictableFunctionalMaterial(material)) {
                names.add(material.name().toLowerCase(Locale.ROOT));
            }
        }
        Collections.sort(names);
        return names;
    }

    public Material matchRestrictableFunctionalMaterial(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String normalized = input.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        Material material = Material.matchMaterial(normalized);
        return isRestrictableFunctionalMaterial(material) ? material : null;
    }

    public int getActiveMerchantCount() {
        return activeMerchantStates.size();
    }

    public long getMerchantNextSpawnMillis() {
        return nextMerchantSpawnMillis;
    }

    public MerchantShopState getAnyActiveMerchantState() {
        for (MerchantShopState state : activeMerchantStates.values()) {
            if (state != null) {
                return state;
            }
        }
        return null;
    }

    public boolean spawnMerchantNow() {
        despawnMerchantWave(true);
        return spawnMerchantWave();
    }

    public boolean despawnMerchantNow() {
        if (activeMerchantStates.isEmpty()) {
            return false;
        }
        despawnMerchantWave(true);
        scheduleNextMerchantSpawn();
        return true;
    }

    public void setMerchantNextSpawnDelayMinutes(long minutes) {
        nextMerchantSpawnMillis = System.currentTimeMillis() + (Math.max(0L, minutes) * 60_000L);
        saveMerchantData();
    }

    public boolean setActiveMerchantRemainingMinutes(long minutes) {
        if (activeMerchantStates.isEmpty()) {
            return false;
        }
        long clampedMinutes = Math.max(0L, minutes);
        if (clampedMinutes == 0L) {
            return despawnMerchantNow();
        }

        long despawnAt = System.currentTimeMillis() + (clampedMinutes * 60_000L);
        for (Map.Entry<String, MerchantShopState> entry : new ArrayList<>(activeMerchantStates.entrySet())) {
            MerchantShopState state = entry.getValue();
            if (state == null) {
                continue;
            }
            activeMerchantStates.put(entry.getKey(), new MerchantShopState(
                    state.getMerchantId(),
                    state.getEntityId(),
                    state.getHostCountryKey(),
                    state.getWorldName(),
                    state.getX(),
                    state.getY(),
                    state.getZ(),
                    state.getYaw(),
                    state.getPitch(),
                    state.getSpawnedAtMillis(),
                    despawnAt
            ));
        }
        saveMerchantData();
        return true;
    }

    public void openMerchantMenu(Player player) {
        if (player == null || merchantShopListener == null) {
            return;
        }

        Country playerCountry = getPlayerCountry(player.getUniqueId());
        MerchantShopState state = playerCountry != null
                ? activeMerchantStates.get(normalizeCountryKey(playerCountry.getName()))
                : null;
        if (state == null) {
            state = getAnyActiveMerchantState();
        }
        if (state == null) {
            player.sendMessage(getMessage("merchant.no-active"));
            return;
        }
        merchantShopListener.openMerchantMenu(player, state);
    }

    public void openMerchantAdminMenu(Player player) {
        if (player != null && merchantShopListener != null) {
            merchantShopListener.openMerchantAdminMenu(player);
        }
    }

    public long getMerchantActiveDurationMillis() {
        return Math.max(2L, getConfig().getLong("merchant-shop.active-minutes", 10L)) * 60_000L;
    }

    public long getMerchantRotationDurationMillis() {
        return Math.max(1L, getConfig().getLong("merchant-shop.rotation-minutes", 2L)) * 60_000L;
    }

    public long getMerchantTradeCooldownMillis() {
        return Math.max(1L, getConfig().getLong("merchant-shop.trade-cooldown-seconds", 30L)) * 1000L;
    }

    private void restartMerchantRuntime() {
        cancelMerchantRuntimeTask();
        if (!isMerchantSystemEnabled()) {
            return;
        }
        ensureMerchantPresenceAfterLoad();
        if (activeMerchantStates.isEmpty() && nextMerchantSpawnMillis <= 0L) {
            scheduleNextMerchantSpawn();
        }
        merchantRuntimeTask = getServer().getScheduler().runTaskTimer(this, this::tickMerchantRuntime, 20L, 20L * 10L);
    }

    private void stopMerchantRuntime() {
        cancelMerchantRuntimeTask();
        despawnMerchantWave(false);
    }

    private void cancelMerchantRuntimeTask() {
        if (merchantRuntimeTask != null) {
            merchantRuntimeTask.cancel();
            merchantRuntimeTask = null;
        }
    }

    private void restartNpcHeadTrackingRuntime() {
        stopNpcHeadTrackingRuntime();
        if (!getConfig().getBoolean("npc-head-tracking.enabled", true)) {
            return;
        }
        long intervalTicks = Math.max(1L, getConfig().getLong("npc-head-tracking.interval-ticks", 4L));
        npcHeadTrackingTask = getServer().getScheduler().runTaskTimer(this, this::tickNpcHeadTracking, intervalTicks, intervalTicks);
    }

    private void stopNpcHeadTrackingRuntime() {
        if (npcHeadTrackingTask != null) {
            npcHeadTrackingTask.cancel();
            npcHeadTrackingTask = null;
        }
    }

    private void restartMobSuppressionRuntime() {
        stopMobSuppressionRuntime();
        mobSuppressionTask = getServer().getScheduler().runTaskTimer(this, this::purgeBlockedMobs, 40L, 200L);
    }

    private void stopMobSuppressionRuntime() {
        if (mobSuppressionTask != null) {
            mobSuppressionTask.cancel();
            mobSuppressionTask = null;
        }
    }

    public boolean shouldBlockHostileMob(Entity entity) {
        if (areHostileMobSpawnsEnabled() || entity == null) {
            return false;
        }
        if (entity instanceof Phantom) {
            return false;
        }
        return entity instanceof Monster || entity instanceof Slime || entity instanceof MagmaCube;
    }

    public boolean shouldBlockPhantom(Entity entity) {
        return entity instanceof Phantom && !arePhantomsEnabled();
    }

    private void purgeBlockedMobs() {
        for (World world : getServer().getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (shouldBlockPhantom(entity) || shouldBlockHostileMob(entity)) {
                    entity.remove();
                }
            }
        }
    }

    private void restartCountryBorderParticlesRuntime() {
        stopCountryBorderParticlesRuntime();
        if (!getConfig().getBoolean("country-border-particles.enabled", true)) {
            return;
        }
        long intervalTicks = Math.max(5L, getConfig().getLong("country-border-particles.interval-ticks", 20L));
        countryBorderParticlesTask = getServer().getScheduler().runTaskTimer(this, this::tickCountryBorderParticles, intervalTicks, intervalTicks);
    }

    private void stopCountryBorderParticlesRuntime() {
        if (countryBorderParticlesTask != null) {
            countryBorderParticlesTask.cancel();
            countryBorderParticlesTask = null;
        }
    }

    private void tickCountryBorderParticles() {
        double viewRadius = Math.max(8.0D, getConfig().getDouble("country-border-particles.view-radius-blocks", 48.0D));
        double viewRadiusSquared = viewRadius * viewRadius;
        for (Player player : getServer().getOnlinePlayers()) {
            if (!isCountryBorderParticlesEnabled(player.getUniqueId())) {
                continue;
            }
            showNearbyCountryBorders(player, viewRadiusSquared);
        }
    }

    private void showNearbyCountryBorders(Player player, double viewRadiusSquared) {
        World world = player.getWorld();
        Location playerLocation = player.getLocation();
        for (Country country : countriesByKey.values()) {
            if (country == null || !country.hasTerritory() || !world.getName().equalsIgnoreCase(country.getTerritoryWorld())) {
                continue;
            }
            List<Location> borderLocations = getCountryBorderLocations(country);
            if (borderLocations.isEmpty()) {
                continue;
            }
            Color color = country.isOpen() ? Color.fromRGB(46, 204, 64) : Color.fromRGB(255, 65, 54);
            Particle.DustOptions dust = new Particle.DustOptions(color, 1.2F);
            for (Location location : borderLocations) {
                if (location.getWorld() == null || !location.getWorld().equals(world)) {
                    continue;
                }
                if (location.distanceSquared(playerLocation) > viewRadiusSquared) {
                    continue;
                }
                player.spawnParticle(Particle.DUST, location, 1, 0.0D, 0.0D, 0.0D, 0.0D, dust);
                player.spawnParticle(Particle.END_ROD, location.clone().add(0.0D, 0.35D, 0.0D), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private void tickNpcHeadTracking() {
        double range = Math.max(1.5D, getConfig().getDouble("npc-head-tracking.range-blocks", 5.0D));
        for (DynamicTraderState traderState : activeTraderStates.values()) {
            if (traderState == null) {
                continue;
            }
            Entity entity = getServer().getEntity(traderState.getEntityId());
            if (entity instanceof Villager villager && villager.isValid()) {
                updateNpcHeadTracking(villager, traderState.getYaw(), traderState.getPitch(), range);
            }
        }
        for (MerchantShopState merchantState : activeMerchantStates.values()) {
            if (merchantState == null) {
                continue;
            }
            Entity entity = getServer().getEntity(merchantState.getEntityId());
            if (entity instanceof WanderingTrader trader && trader.isValid()) {
                updateNpcHeadTracking(trader, merchantState.getYaw(), merchantState.getPitch(), range);
            }
        }
    }

    private void updateNpcHeadTracking(LivingEntity entity, float idleYaw, float idlePitch, double range) {
        if (entity == null || !entity.isValid() || entity.getWorld() == null) {
            return;
        }
        Location currentLocation = entity.getLocation();
        Player target = findNearestPlayerForNpcLook(currentLocation, range);
        Location rotated = currentLocation.clone();
        if (target == null) {
            rotated.setYaw(idleYaw);
            rotated.setPitch(idlePitch);
            entity.teleport(rotated);
            return;
        }

        Location eyeLocation = target.getEyeLocation();
        double dx = eyeLocation.getX() - currentLocation.getX();
        double dy = eyeLocation.getY() - (currentLocation.getY() + entity.getHeight() * 0.5D);
        double dz = eyeLocation.getZ() - currentLocation.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.max(0.001D, horizontal)));
        rotated.setYaw(yaw);
        rotated.setPitch(Math.max(-45.0F, Math.min(30.0F, pitch)));
        entity.teleport(rotated);
    }

    private Player findNearestPlayerForNpcLook(Location origin, double range) {
        if (origin == null || origin.getWorld() == null) {
            return null;
        }
        double rangeSquared = range * range;
        Player nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        for (Player player : origin.getWorld().getPlayers()) {
            if (!player.isOnline() || player.isDead() || player.isInvisible()) {
                continue;
            }
            double distanceSquared = player.getLocation().distanceSquared(origin);
            if (distanceSquared > rangeSquared || distanceSquared >= nearestDistanceSquared) {
                continue;
            }
            nearest = player;
            nearestDistanceSquared = distanceSquared;
        }
        return nearest;
    }

    private void tickMerchantRuntime() {
        if (!isMerchantSystemEnabled()) {
            return;
        }

        long now = System.currentTimeMillis();
        resetMerchantDailySalesIfNeeded(now);
        if (!activeMerchantStates.isEmpty()) {
            MerchantShopState state = activeMerchantStates.values().iterator().next();
            if (state.getDespawnAtMillis() <= now) {
                despawnMerchantWave(true);
                scheduleNextMerchantSpawn();
            }
            return;
        }

        if (nextMerchantSpawnMillis > 0L && now >= nextMerchantSpawnMillis) {
            if (!spawnMerchantWave()) {
                scheduleNextMerchantSpawn();
            }
        }
    }

    private void scheduleNextMerchantSpawn() {
        long minDelay = Math.max(10L, getConfig().getLong("merchant-shop.spawn-minutes-min", 60L)) * 60_000L;
        long maxDelay = Math.max(minDelay, getConfig().getLong("merchant-shop.spawn-minutes-max", 120L) * 60_000L);
        long delay = minDelay;
        if (maxDelay > minDelay) {
            delay += ThreadLocalRandom.current().nextLong((maxDelay - minDelay) + 1L);
        }
        nextMerchantSpawnMillis = System.currentTimeMillis() + delay;
        saveMerchantData();
    }

    private void ensureMerchantPresenceAfterLoad() {
        boolean changed = false;
        for (Map.Entry<String, MerchantShopState> entry : new ArrayList<>(activeMerchantStates.entrySet())) {
            MerchantShopState state = entry.getValue();
            if (state == null) {
                activeMerchantStates.remove(entry.getKey());
                changed = true;
                continue;
            }

            if (state.getDespawnAtMillis() <= System.currentTimeMillis()) {
                activeMerchantStates.remove(entry.getKey());
                changed = true;
                continue;
            }

            Entity entity = getServer().getEntity(state.getEntityId());
            if (entity instanceof WanderingTrader trader && trader.isValid()) {
                configureMerchantNpc(trader, state);
                continue;
            }

            World world = getServer().getWorld(state.getWorldName());
            if (world == null) {
                activeMerchantStates.remove(entry.getKey());
                changed = true;
                continue;
            }

            Location location = new Location(
                    world,
                    state.getX(),
                    state.getY(),
                    state.getZ(),
                    state.getYaw(),
                    state.getPitch()
            );
            WanderingTrader trader = (WanderingTrader) world.spawnEntity(location, EntityType.WANDERING_TRADER);
            configureMerchantNpc(trader, state);
            activeMerchantStates.put(entry.getKey(), new MerchantShopState(
                    state.getMerchantId(),
                    trader.getUniqueId(),
                    state.getHostCountryKey(),
                    state.getWorldName(),
                    state.getX(),
                    state.getY(),
                    state.getZ(),
                    state.getYaw(),
                    state.getPitch(),
                    state.getSpawnedAtMillis(),
                    state.getDespawnAtMillis()
            ));
            changed = true;
        }
        removeOrphanMerchantNpcs();
        if (changed) {
            saveMerchantData();
        }
    }

    private boolean spawnMerchantWave() {
        if (!activeMerchantStates.isEmpty()) {
            return true;
        }
        Country hostCountry = selectNextMerchantHostCountry();
        if (hostCountry == null) {
            merchantSharedStock.clear();
            saveMerchantData();
            return false;
        }
        long now = System.currentTimeMillis();
        long despawnAt = now + getMerchantActiveDurationMillis();
        merchantCycleSeed = now;
        merchantSharedStock.clear();
        initializeMerchantSharedStock();
        boolean spawnedAny = spawnMerchantForCountry(hostCountry, now, despawnAt);
        if (!spawnedAny) {
            merchantSharedStock.clear();
            saveMerchantData();
            return false;
        }
        nextMerchantSpawnMillis = 0L;
        saveMerchantData();
        return true;
    }

    private boolean spawnMerchantForCountry(Country country, long spawnedAt, long despawnAt) {
        Location base = getCountryHome(country);
        if (country == null || base == null || base.getWorld() == null) {
            return false;
        }
        Location spawn = findMerchantSpawnLocation(base);
        if (spawn == null || spawn.getWorld() == null) {
            return false;
        }
        WanderingTrader trader = (WanderingTrader) spawn.getWorld().spawnEntity(spawn, EntityType.WANDERING_TRADER);
        trader.setDespawnDelay(Integer.MAX_VALUE);
        trader.setInvulnerable(true);
        trader.setRemoveWhenFarAway(false);
        trader.setPersistent(true);
        trader.setCanDrinkPotion(false);
        trader.setCanPickupItems(false);
        trader.setBreed(false);
        trader.setSilent(false);
        trader.customName(legacyComponent(getMessage("merchant.npc-name")));
        trader.setCustomNameVisible(true);
        trader.getPersistentDataContainer().set(merchantNpcKey, PersistentDataType.BYTE, (byte) 1);
        activeMerchantStates.put(normalizeCountryKey(country.getName()), new MerchantShopState(
                UUID.randomUUID(),
                trader.getUniqueId(),
                normalizeCountryKey(country.getName()),
                spawn.getWorld().getName(),
                spawn.getX(),
                spawn.getY(),
                spawn.getZ(),
                spawn.getYaw(),
                spawn.getPitch(),
                spawnedAt,
                despawnAt
        ));
        announceMerchantSpawnToCountryMembers(country);
        return true;
    }

    private void configureMerchantNpc(WanderingTrader trader, MerchantShopState state) {
        if (trader == null || state == null) {
            return;
        }
        trader.setDespawnDelay(Integer.MAX_VALUE);
        trader.setInvulnerable(true);
        trader.setRemoveWhenFarAway(false);
        trader.setPersistent(true);
        trader.setCanDrinkPotion(false);
        trader.setCanPickupItems(false);
        trader.setBreed(false);
        trader.setSilent(false);
        trader.customName(legacyComponent(getMessage("merchant.npc-name")));
        trader.setCustomNameVisible(true);
        trader.getPersistentDataContainer().set(merchantNpcKey, PersistentDataType.BYTE, (byte) 1);
    }

    private void removeOrphanMerchantNpcs() {
        Set<UUID> activeIds = new HashSet<>();
        for (MerchantShopState state : activeMerchantStates.values()) {
            if (state != null) {
                activeIds.add(state.getEntityId());
            }
        }
        for (World world : getServer().getWorlds()) {
            for (WanderingTrader trader : world.getEntitiesByClass(WanderingTrader.class)) {
                if (!trader.getPersistentDataContainer().has(merchantNpcKey, PersistentDataType.BYTE)) {
                    continue;
                }
                if (activeIds.contains(trader.getUniqueId())) {
                    continue;
                }
                trader.remove();
            }
        }
    }

    private void announceMerchantSpawnToCountryMembers(Country country) {
        if (country == null || !hasMessage("merchant.spawned-country")) {
            return;
        }

        String message = getMessage("merchant.spawned-country", placeholders(
                "country", country.getName()
        ));
        for (Player member : getOnlineCountryMembers(country)) {
            member.sendMessage(message);
        }
    }

    private Location findMerchantSpawnLocation(Location base) {
        World world = base.getWorld();
        if (world == null) {
            return null;
        }
        int radius = Math.max(2, getConfig().getInt("merchant-shop.spawn-radius", 8));
        for (int i = 0; i < 12; i++) {
            int xOffset = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            int zOffset = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            int x = base.getBlockX() + xOffset;
            int z = base.getBlockZ() + zOffset;
            int y = world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES) + 1;
            Location candidate = new Location(world, x + 0.5D, y, z + 0.5D, ThreadLocalRandom.current().nextFloat() * 360.0F, 0.0F);
            if (isSafeProfessionSelectionLocation(candidate)) {
                return candidate;
            }
        }
        return base.clone();
    }

    private void despawnMerchantWave(boolean persist) {
        for (MerchantShopState state : new ArrayList<>(activeMerchantStates.values())) {
            Entity entity = getServer().getEntity(state.getEntityId());
            if (entity != null) {
                entity.remove();
            }
        }
        activeMerchantStates.clear();
        merchantSharedStock.clear();
        if (persist) {
            saveMerchantData();
        }
    }

    private void initializeMerchantSharedStock() {
        for (int rotation = 0; rotation < getMerchantRotationCount(); rotation++) {
            for (MerchantShopOffer offer : createMerchantBuyOffers(rotation)) {
                merchantSharedStock.put(offer.getKey(), offer.getStock());
            }
        }
    }

    private int getMerchantRotationCount() {
        return Math.max(1, (int) (getMerchantActiveDurationMillis() / getMerchantRotationDurationMillis()));
    }

    private int getCurrentMerchantRotationIndex() {
        if (activeMerchantStates.isEmpty()) {
            return 0;
        }
        MerchantShopState state = activeMerchantStates.values().iterator().next();
        long elapsed = Math.max(0L, System.currentTimeMillis() - state.getSpawnedAtMillis());
        return Math.min(getMerchantRotationCount() - 1, (int) (elapsed / getMerchantRotationDurationMillis()));
    }

    public List<MerchantShopOffer> getMerchantBuyOffers() {
        return createMerchantBuyOffers(getCurrentMerchantRotationIndex());
    }

    public List<MerchantShopOffer> getMerchantBuyOffersForRotation(int rotationIndex) {
        return createMerchantBuyOffers(Math.max(0, rotationIndex));
    }

    public List<MerchantShopOffer> getMerchantSellOffers() {
        ConfigurationSection section = getConfig().getConfigurationSection("merchant-shop.sell-offers");
        if (section == null) {
            return getDefaultMerchantSellOffers();
        }
        List<MerchantShopOffer> offers = new ArrayList<>();
        for (String key : section.getKeys(false).stream().sorted().toList()) {
            ConfigurationSection offerSection = section.getConfigurationSection(key);
            MerchantShopOffer offer = readMerchantOfferConfig(offerSection, MerchantShopOffer.Type.SELL, "sell" + key);
            if (offer != null) {
                offers.add(offer);
            }
        }
        return offers.isEmpty() ? getDefaultMerchantSellOffers() : offers;
    }

    public MerchantShopOffer getMerchantBuyOffer(int rotationIndex, int offerIndex) {
        List<MerchantShopOffer> offers = getMerchantBuyOffersForRotation(rotationIndex);
        return offerIndex >= 0 && offerIndex < offers.size() ? offers.get(offerIndex) : null;
    }

    public MerchantShopOffer getMerchantSellOffer(int offerIndex) {
        List<MerchantShopOffer> offers = getMerchantSellOffers();
        return offerIndex >= 0 && offerIndex < offers.size() ? offers.get(offerIndex) : null;
    }

    private List<MerchantShopOffer> createMerchantBuyOffers(int rotationIndex) {
        if (isMerchantRandomBuyRotationEnabled()) {
            return createRandomMerchantBuyOffers(Math.max(0, rotationIndex));
        }

        ConfigurationSection rotationsSection = getConfig().getConfigurationSection("merchant-shop.buy-rotations");
        if (rotationsSection != null && !rotationsSection.getKeys(false).isEmpty()) {
            String rotationKey = String.valueOf(Math.floorMod(rotationIndex, rotationsSection.getKeys(false).size()));
            ConfigurationSection rotationSection = rotationsSection.getConfigurationSection(rotationKey);
            if (rotationSection != null) {
                List<MerchantShopOffer> configured = new ArrayList<>();
                for (String key : rotationSection.getKeys(false).stream().sorted().toList()) {
                    ConfigurationSection offerSection = rotationSection.getConfigurationSection(key);
                    MerchantShopOffer offer = readMerchantOfferConfig(offerSection, MerchantShopOffer.Type.BUY, "buy" + rotationKey + ":" + key);
                    if (offer != null) {
                        configured.add(offer);
                    }
                }
                if (!configured.isEmpty()) {
                    return configured;
                }
            }
        }

        List<MerchantShopOffer> pool = switch (Math.floorMod(rotationIndex, 5)) {
            case 0 -> List.of(
                    merchantBuyOffer("buy0:bone_meal", Material.BONE_MEAL, 8, scalePriceMoney(34.0D), 5),
                    merchantBuyOffer("buy0:string", Material.STRING, 6, scalePriceMoney(46.0D), 4),
                    merchantBuyOffer("buy0:leather", Material.LEATHER, 4, scalePriceMoney(42.0D), 4),
                    merchantBuyOffer("buy0:honeycomb", Material.HONEYCOMB, 4, scalePriceMoney(36.0D), 4),
                    merchantBuyOffer("buy0:slime_ball", Material.SLIME_BALL, 3, scalePriceMoney(58.0D), 3),
                    merchantBuyOffer("buy0:saddle", Material.SADDLE, 1, scalePriceMoney(78.0D), 2),
                    merchantBuyOffer("buy0:name_tag", Material.NAME_TAG, 1, scalePriceMoney(85.0D), 2)
            );
            case 1 -> List.of(
                    merchantBuyOffer("buy1:quartz", Material.QUARTZ, 8, scalePriceMoney(52.0D), 3),
                    merchantBuyOffer("buy1:blaze_rod", Material.BLAZE_ROD, 2, scalePriceMoney(88.0D), 2),
                    merchantBuyOffer("buy1:magma_cream", Material.MAGMA_CREAM, 3, scalePriceMoney(68.0D), 3),
                    merchantBuyOffer("buy1:glowstone_dust", Material.GLOWSTONE_DUST, 8, scalePriceMoney(42.0D), 4),
                    merchantBuyOffer("buy1:ender_pearl", Material.ENDER_PEARL, 2, scalePriceMoney(92.0D), 2),
                    merchantBuyOffer("buy1:ghast_tear", Material.GHAST_TEAR, 1, scalePriceMoney(130.0D), 1),
                    merchantBuyOffer("buy1:nether_wart", Material.NETHER_WART, 6, scalePriceMoney(56.0D), 3)
            );
            case 2 -> List.of(
                    merchantBuyOffer("buy2:prismarine_shard", Material.PRISMARINE_SHARD, 8, scalePriceMoney(46.0D), 3),
                    merchantBuyOffer("buy2:prismarine_crystals", Material.PRISMARINE_CRYSTALS, 6, scalePriceMoney(58.0D), 3),
                    merchantBuyOffer("buy2:phantom_membrane", Material.PHANTOM_MEMBRANE, 2, scalePriceMoney(74.0D), 2),
                    merchantBuyOffer("buy2:nautilus_shell", Material.NAUTILUS_SHELL, 1, scalePriceMoney(90.0D), 1),
                    merchantBuyOffer("buy2:turtle_scute", Material.TURTLE_SCUTE, 1, scalePriceMoney(96.0D), 1),
                    merchantBuyOffer("buy2:sponge", Material.SPONGE, 2, scalePriceMoney(120.0D), 1),
                    merchantBuyOffer("buy2:heart_of_the_sea", Material.HEART_OF_THE_SEA, 1, scalePriceMoney(220.0D), 1)
            );
            case 3 -> List.of(
                    merchantBuyOffer("buy3:chorus_fruit", Material.CHORUS_FRUIT, 6, scalePriceMoney(70.0D), 2),
                    merchantBuyOffer("buy3:popped_chorus_fruit", Material.POPPED_CHORUS_FRUIT, 4, scalePriceMoney(88.0D), 2),
                    merchantBuyOffer("buy3:end_stone", Material.END_STONE, 12, scalePriceMoney(54.0D), 3),
                    merchantBuyOffer("buy3:purpur_block", Material.PURPUR_BLOCK, 8, scalePriceMoney(74.0D), 2),
                    merchantBuyOffer("buy3:shulker_shell", Material.SHULKER_SHELL, 1, scalePriceMoney(190.0D), 1),
                    merchantBuyOffer("buy3:dragon_breath", Material.DRAGON_BREATH, 1, scalePriceMoney(220.0D), 1),
                    merchantBuyOffer("buy3:ender_chest", Material.ENDER_CHEST, 1, scalePriceMoney(110.0D), 1)
            );
            default -> List.of(
                    merchantBuyOffer("buy4:obsidian", Material.OBSIDIAN, 8, scalePriceMoney(48.0D), 3),
                    merchantBuyOffer("buy4:crying_obsidian", Material.CRYING_OBSIDIAN, 2, scalePriceMoney(120.0D), 1),
                    merchantBuyOffer("buy4:soul_sand", Material.SOUL_SAND, 8, scalePriceMoney(44.0D), 3),
                    merchantBuyOffer("buy4:blackstone", Material.BLACKSTONE, 16, scalePriceMoney(40.0D), 3),
                    merchantBuyOffer("buy4:basalt", Material.BASALT, 16, scalePriceMoney(38.0D), 3),
                    merchantBuyOffer("buy4:amethyst", Material.AMETHYST_SHARD, 8, scalePriceMoney(42.0D), 4),
                    merchantBuyOffer("buy4:gunpowder", Material.GUNPOWDER, 4, scalePriceMoney(72.0D), 3)
            );
        };
        return pool;
    }

    private List<MerchantShopOffer> createRandomMerchantBuyOffers(int rotationIndex) {
        List<MerchantShopOffer> pool = new ArrayList<>(getMerchantRandomBuyOfferPool());
        if (pool.isEmpty()) {
            return List.of();
        }

        long seed = merchantCycleSeed > 0L
                ? merchantCycleSeed + (rotationIndex * 31L)
                : System.currentTimeMillis() / Math.max(1L, getMerchantRotationDurationMillis());
        Collections.shuffle(pool, new Random(seed));

        int offerCount = Math.min(getMerchantRandomBuyOffersPerRotation(), pool.size());
        List<MerchantShopOffer> offers = new ArrayList<>(offerCount);
        for (int index = 0; index < offerCount; index++) {
            MerchantShopOffer template = pool.get(index);
            String key = "buy" + rotationIndex + ":" + template.getMaterial().name().toLowerCase(Locale.ROOT);
            offers.add(new MerchantShopOffer(
                    key,
                    MerchantShopOffer.Type.BUY,
                    template.getMaterial(),
                    template.getAmount(),
                    template.getPrice(),
                    template.getStock()
            ));
        }
        return offers;
    }

    private List<MerchantShopOffer> getMerchantRandomBuyOfferPool() {
        return List.of(
                merchantBuyOffer("pool:cobblestone", Material.COBBLESTONE, 32, scalePriceMoney(0.80D), 12),
                merchantBuyOffer("pool:stone", Material.STONE, 32, scalePriceMoney(0.90D), 12),
                merchantBuyOffer("pool:oak_log", Material.OAK_LOG, 24, scalePriceMoney(0.95D), 10),
                merchantBuyOffer("pool:spruce_log", Material.SPRUCE_LOG, 24, scalePriceMoney(1.00D), 10),
                merchantBuyOffer("pool:birch_log", Material.BIRCH_LOG, 24, scalePriceMoney(0.95D), 10),
                merchantBuyOffer("pool:wheat", Material.WHEAT, 32, scalePriceMoney(0.70D), 12),
                merchantBuyOffer("pool:potato", Material.POTATO, 32, scalePriceMoney(0.65D), 12),
                merchantBuyOffer("pool:carrot", Material.CARROT, 32, scalePriceMoney(0.70D), 12),
                merchantBuyOffer("pool:pumpkin", Material.PUMPKIN, 12, scalePriceMoney(1.40D), 8),
                merchantBuyOffer("pool:hay_block", Material.HAY_BLOCK, 12, scalePriceMoney(1.10D), 8),
                merchantBuyOffer("pool:bread", Material.BREAD, 10, scalePriceMoney(1.20D), 10),
                merchantBuyOffer("pool:sand", Material.SAND, 32, scalePriceMoney(0.65D), 10),
                merchantBuyOffer("pool:clay_ball", Material.CLAY_BALL, 24, scalePriceMoney(0.85D), 10),
                merchantBuyOffer("pool:bricks", Material.BRICKS, 16, scalePriceMoney(1.80D), 8),
                merchantBuyOffer("pool:coal", Material.COAL, 20, scalePriceMoney(1.10D), 10),
                merchantBuyOffer("pool:copper_ingot", Material.COPPER_INGOT, 16, scalePriceMoney(1.45D), 8),
                merchantBuyOffer("pool:iron_ingot", Material.IRON_INGOT, 12, scalePriceMoney(2.20D), 8),
                merchantBuyOffer("pool:bone_meal", Material.BONE_MEAL, 8, scalePriceMoney(0.95D), 10),
                merchantBuyOffer("pool:string", Material.STRING, 12, scalePriceMoney(1.15D), 8),
                merchantBuyOffer("pool:leather", Material.LEATHER, 8, scalePriceMoney(1.40D), 8),
                merchantBuyOffer("pool:honeycomb", Material.HONEYCOMB, 6, scalePriceMoney(1.25D), 6),
                merchantBuyOffer("pool:slime_ball", Material.SLIME_BALL, 4, scalePriceMoney(2.00D), 5),
                merchantBuyOffer("pool:gunpowder", Material.GUNPOWDER, 6, scalePriceMoney(1.90D), 6),
                merchantBuyOffer("pool:quartz", Material.QUARTZ, 8, scalePriceMoney(2.30D), 5),
                merchantBuyOffer("pool:glowstone_dust", Material.GLOWSTONE_DUST, 8, scalePriceMoney(1.85D), 5),
                merchantBuyOffer("pool:magma_cream", Material.MAGMA_CREAM, 3, scalePriceMoney(3.20D), 4),
                merchantBuyOffer("pool:blaze_rod", Material.BLAZE_ROD, 2, scalePriceMoney(4.40D), 3),
                merchantBuyOffer("pool:nether_wart", Material.NETHER_WART, 8, scalePriceMoney(1.75D), 5),
                merchantBuyOffer("pool:ghast_tear", Material.GHAST_TEAR, 1, scalePriceMoney(6.50D), 2),
                merchantBuyOffer("pool:obsidian", Material.OBSIDIAN, 8, scalePriceMoney(2.40D), 4),
                merchantBuyOffer("pool:crying_obsidian", Material.CRYING_OBSIDIAN, 2, scalePriceMoney(6.00D), 2),
                merchantBuyOffer("pool:soul_sand", Material.SOUL_SAND, 8, scalePriceMoney(2.20D), 4),
                merchantBuyOffer("pool:blackstone", Material.BLACKSTONE, 16, scalePriceMoney(2.00D), 4),
                merchantBuyOffer("pool:basalt", Material.BASALT, 16, scalePriceMoney(1.90D), 4),
                merchantBuyOffer("pool:amethyst_shard", Material.AMETHYST_SHARD, 8, scalePriceMoney(2.10D), 5),
                merchantBuyOffer("pool:prismarine_shard", Material.PRISMARINE_SHARD, 8, scalePriceMoney(2.30D), 4),
                merchantBuyOffer("pool:prismarine_crystals", Material.PRISMARINE_CRYSTALS, 6, scalePriceMoney(2.90D), 4),
                merchantBuyOffer("pool:phantom_membrane", Material.PHANTOM_MEMBRANE, 2, scalePriceMoney(3.70D), 3),
                merchantBuyOffer("pool:nautilus_shell", Material.NAUTILUS_SHELL, 1, scalePriceMoney(4.50D), 2),
                merchantBuyOffer("pool:turtle_scute", Material.TURTLE_SCUTE, 1, scalePriceMoney(4.80D), 2),
                merchantBuyOffer("pool:sponge", Material.SPONGE, 2, scalePriceMoney(6.00D), 2),
                merchantBuyOffer("pool:heart_of_the_sea", Material.HEART_OF_THE_SEA, 1, scalePriceMoney(11.00D), 1),
                merchantBuyOffer("pool:saddle", Material.SADDLE, 1, scalePriceMoney(3.90D), 2),
                merchantBuyOffer("pool:name_tag", Material.NAME_TAG, 1, scalePriceMoney(4.25D), 2)
        );
    }

    private MerchantShopOffer merchantBuyOffer(String key, Material material, int amount, double price, int stock) {
        return new MerchantShopOffer(key, MerchantShopOffer.Type.BUY, material, amount, roundMoney(Math.max(0.0D, price)), stock);
    }

    private MerchantShopOffer readMerchantOfferConfig(ConfigurationSection section, MerchantShopOffer.Type type, String fallbackKey) {
        if (section == null || type == null) {
            return null;
        }
        Material material = Material.matchMaterial(section.getString("material", ""));
        if (material == null || material.isAir() || !material.isItem()) {
            return null;
        }
        int amount = Math.max(1, section.getInt("amount", 1));
        double price = roundMoney(Math.max(0.0D, section.getDouble("price", 0.0D)));
        int stock = type == MerchantShopOffer.Type.BUY ? Math.max(1, section.getInt("stock", 1)) : 0;
        String key = type == MerchantShopOffer.Type.BUY
                ? "buy:" + material.name().toLowerCase(Locale.ROOT) + ":" + fallbackKey
                : "sell:" + material.name().toLowerCase(Locale.ROOT);
        return new MerchantShopOffer(key, type, material, amount, price, stock);
    }

    private List<List<MerchantShopOffer>> getDefaultMerchantBuyOfferLayouts() {
        return List.of(
                createMerchantBuyOffersFallback(0),
                createMerchantBuyOffersFallback(1),
                createMerchantBuyOffersFallback(2),
                createMerchantBuyOffersFallback(3),
                createMerchantBuyOffersFallback(4)
        );
    }

    private List<MerchantShopOffer> createMerchantBuyOffersFallback(int rotationIndex) {
        return switch (Math.floorMod(rotationIndex, 5)) {
            case 0 -> List.of(
                    merchantBuyOffer("buy0:bone_meal", Material.BONE_MEAL, 8, scalePriceMoney(34.0D), 5),
                    merchantBuyOffer("buy0:string", Material.STRING, 6, scalePriceMoney(46.0D), 4),
                    merchantBuyOffer("buy0:leather", Material.LEATHER, 4, scalePriceMoney(42.0D), 4),
                    merchantBuyOffer("buy0:honeycomb", Material.HONEYCOMB, 4, scalePriceMoney(36.0D), 4),
                    merchantBuyOffer("buy0:slime_ball", Material.SLIME_BALL, 3, scalePriceMoney(58.0D), 3),
                    merchantBuyOffer("buy0:saddle", Material.SADDLE, 1, scalePriceMoney(78.0D), 2),
                    merchantBuyOffer("buy0:name_tag", Material.NAME_TAG, 1, scalePriceMoney(85.0D), 2)
            );
            case 1 -> List.of(
                    merchantBuyOffer("buy1:quartz", Material.QUARTZ, 8, scalePriceMoney(52.0D), 3),
                    merchantBuyOffer("buy1:blaze_rod", Material.BLAZE_ROD, 2, scalePriceMoney(88.0D), 2),
                    merchantBuyOffer("buy1:magma_cream", Material.MAGMA_CREAM, 3, scalePriceMoney(68.0D), 3),
                    merchantBuyOffer("buy1:glowstone_dust", Material.GLOWSTONE_DUST, 8, scalePriceMoney(42.0D), 4),
                    merchantBuyOffer("buy1:ender_pearl", Material.ENDER_PEARL, 2, scalePriceMoney(92.0D), 2),
                    merchantBuyOffer("buy1:ghast_tear", Material.GHAST_TEAR, 1, scalePriceMoney(130.0D), 1),
                    merchantBuyOffer("buy1:nether_wart", Material.NETHER_WART, 6, scalePriceMoney(56.0D), 3)
            );
            case 2 -> List.of(
                    merchantBuyOffer("buy2:prismarine_shard", Material.PRISMARINE_SHARD, 8, scalePriceMoney(46.0D), 3),
                    merchantBuyOffer("buy2:prismarine_crystals", Material.PRISMARINE_CRYSTALS, 6, scalePriceMoney(58.0D), 3),
                    merchantBuyOffer("buy2:phantom_membrane", Material.PHANTOM_MEMBRANE, 2, scalePriceMoney(74.0D), 2),
                    merchantBuyOffer("buy2:nautilus_shell", Material.NAUTILUS_SHELL, 1, scalePriceMoney(90.0D), 1),
                    merchantBuyOffer("buy2:turtle_scute", Material.TURTLE_SCUTE, 1, scalePriceMoney(96.0D), 1),
                    merchantBuyOffer("buy2:sponge", Material.SPONGE, 2, scalePriceMoney(120.0D), 1),
                    merchantBuyOffer("buy2:heart_of_the_sea", Material.HEART_OF_THE_SEA, 1, scalePriceMoney(220.0D), 1)
            );
            case 3 -> List.of(
                    merchantBuyOffer("buy3:chorus_fruit", Material.CHORUS_FRUIT, 6, scalePriceMoney(70.0D), 2),
                    merchantBuyOffer("buy3:popped_chorus_fruit", Material.POPPED_CHORUS_FRUIT, 4, scalePriceMoney(88.0D), 2),
                    merchantBuyOffer("buy3:end_stone", Material.END_STONE, 12, scalePriceMoney(54.0D), 3),
                    merchantBuyOffer("buy3:purpur_block", Material.PURPUR_BLOCK, 8, scalePriceMoney(74.0D), 2),
                    merchantBuyOffer("buy3:shulker_shell", Material.SHULKER_SHELL, 1, scalePriceMoney(190.0D), 1),
                    merchantBuyOffer("buy3:dragon_breath", Material.DRAGON_BREATH, 1, scalePriceMoney(220.0D), 1),
                    merchantBuyOffer("buy3:ender_chest", Material.ENDER_CHEST, 1, scalePriceMoney(110.0D), 1)
            );
            default -> List.of(
                    merchantBuyOffer("buy4:obsidian", Material.OBSIDIAN, 8, scalePriceMoney(48.0D), 3),
                    merchantBuyOffer("buy4:crying_obsidian", Material.CRYING_OBSIDIAN, 2, scalePriceMoney(120.0D), 1),
                    merchantBuyOffer("buy4:soul_sand", Material.SOUL_SAND, 8, scalePriceMoney(44.0D), 3),
                    merchantBuyOffer("buy4:blackstone", Material.BLACKSTONE, 16, scalePriceMoney(40.0D), 3),
                    merchantBuyOffer("buy4:basalt", Material.BASALT, 16, scalePriceMoney(38.0D), 3),
                    merchantBuyOffer("buy4:amethyst", Material.AMETHYST_SHARD, 8, scalePriceMoney(42.0D), 4),
                    merchantBuyOffer("buy4:gunpowder", Material.GUNPOWDER, 4, scalePriceMoney(72.0D), 3)
            );
        };
    }

    private List<MerchantShopOffer> getDefaultMerchantSellOffers() {
        return List.of(
                new MerchantShopOffer("sell:wheat", MerchantShopOffer.Type.SELL, Material.WHEAT, 32, scaleRewardMoney(0.6D), 0),
                new MerchantShopOffer("sell:oak_log", MerchantShopOffer.Type.SELL, Material.OAK_LOG, 16, scaleRewardMoney(0.8D), 0),
                new MerchantShopOffer("sell:cobblestone", MerchantShopOffer.Type.SELL, Material.COBBLESTONE, 32, scaleRewardMoney(0.4D), 0),
                new MerchantShopOffer("sell:iron_ingot", MerchantShopOffer.Type.SELL, Material.IRON_INGOT, 8, scaleRewardMoney(2.2D), 0),
                new MerchantShopOffer("sell:bread", MerchantShopOffer.Type.SELL, Material.BREAD, 8, scaleRewardMoney(1.2D), 0)
        );
    }

    public int getMerchantRemainingStock(MerchantShopOffer offer) {
        if (offer == null || offer.getType() != MerchantShopOffer.Type.BUY) {
            return 0;
        }
        return merchantSharedStock.getOrDefault(offer.getKey(), offer.getStock());
    }

    public int getMerchantConfiguredSpawnMinutesMin() {
        return Math.max(10, getConfig().getInt("merchant-shop.spawn-minutes-min", 60));
    }

    public int getMerchantConfiguredSpawnMinutesMax() {
        return Math.max(getMerchantConfiguredSpawnMinutesMin(), getConfig().getInt("merchant-shop.spawn-minutes-max", 120));
    }

    public int getMerchantConfiguredActiveMinutes() {
        return Math.max(2, getConfig().getInt("merchant-shop.active-minutes", 10));
    }

    public int getMerchantConfiguredRotationMinutes() {
        return Math.max(1, getConfig().getInt("merchant-shop.rotation-minutes", 2));
    }

    public int getMerchantConfiguredTradeCooldownSeconds() {
        return Math.max(1, getConfig().getInt("merchant-shop.trade-cooldown-seconds", 30));
    }

    public void setMerchantConfiguredSpawnMinutesMin(int minutes) {
        int sanitized = Math.max(10, minutes);
        setManagedConfigValue("merchant-shop.spawn-minutes-min", sanitized);
        if (getMerchantConfiguredSpawnMinutesMax() < sanitized) {
            setManagedConfigValue("merchant-shop.spawn-minutes-max", sanitized);
        }
        if (activeMerchantStates.isEmpty()) {
            scheduleNextMerchantSpawn();
        }
    }

    public void setMerchantConfiguredSpawnMinutesMax(int minutes) {
        int sanitized = Math.max(getMerchantConfiguredSpawnMinutesMin(), minutes);
        setManagedConfigValue("merchant-shop.spawn-minutes-max", sanitized);
        if (activeMerchantStates.isEmpty()) {
            scheduleNextMerchantSpawn();
        }
    }

    public void setMerchantConfiguredActiveMinutes(int minutes) {
        setManagedConfigValue("merchant-shop.active-minutes", Math.max(2, minutes));
    }

    public void setMerchantConfiguredRotationMinutes(int minutes) {
        int sanitized = Math.max(1, minutes);
        int activeMinutes = Math.max(getMerchantConfiguredActiveMinutes(), sanitized);
        setManagedConfigValue("merchant-shop.active-minutes", activeMinutes);
        setManagedConfigValue("merchant-shop.rotation-minutes", sanitized);
    }

    public void setMerchantConfiguredTradeCooldownSeconds(int seconds) {
        setManagedConfigValue("merchant-shop.trade-cooldown-seconds", Math.max(1, seconds));
    }

    public int getMerchantBuyRotationEditorCount() {
        if (isMerchantRandomBuyRotationEnabled()) {
            return Math.max(1, getMerchantRotationCount());
        }
        ConfigurationSection section = getConfig().getConfigurationSection("merchant-shop.buy-rotations");
        return Math.max(1, section != null ? section.getKeys(false).size() : 5);
    }

    public boolean updateMerchantBuyOffer(int rotationIndex, int offerIndex, Material material, Integer amount, Double price, Integer stock) {
        String path = "merchant-shop.buy-rotations." + Math.max(0, rotationIndex) + "." + Math.max(0, offerIndex);
        return updateMerchantOfferConfig(path, MerchantShopOffer.Type.BUY, material, amount, price, stock);
    }

    public boolean updateMerchantSellOffer(int offerIndex, Material material, Integer amount, Double price) {
        String path = "merchant-shop.sell-offers." + Math.max(0, offerIndex);
        return updateMerchantOfferConfig(path, MerchantShopOffer.Type.SELL, material, amount, price, null);
    }

    private boolean updateMerchantOfferConfig(String path, MerchantShopOffer.Type type, Material material, Integer amount, Double price, Integer stock) {
        if (path == null || type == null) {
            return false;
        }
        if (material != null) {
            if (material.isAir() || !material.isItem()) {
                return false;
            }
            setManagedConfigValue(path + ".material", material.name());
        }
        if (amount != null) {
            setManagedConfigValue(path + ".amount", Math.max(1, amount));
        }
        if (price != null) {
            setManagedConfigValue(path + ".price", roundMoney(Math.max(0.0D, price)));
        }
        if (type == MerchantShopOffer.Type.BUY && stock != null) {
            setManagedConfigValue(path + ".stock", Math.max(1, stock));
        }
        return true;
    }

    public double getMerchantSellPrice(MerchantShopOffer offer) {
        if (offer == null) {
            return 0.0D;
        }
        resetMerchantDailySalesIfNeeded(System.currentTimeMillis());
        int soldAmount = merchantDailySoldAmounts.getOrDefault(offer.getMaterial().name(), 0);
        double modifier = Math.max(0.25D, 1.0D - (soldAmount / 512.0D));
        return roundMoney(offer.getPrice() * modifier);
    }

    public MerchantShopState getMerchantState(Entity entity) {
        return entity != null ? getMerchantState(entity.getUniqueId()) : null;
    }

    public MerchantShopState getMerchantState(UUID entityId) {
        if (entityId == null) {
            return null;
        }
        for (MerchantShopState state : activeMerchantStates.values()) {
            if (entityId.equals(state.getEntityId())) {
                return state;
            }
        }
        return null;
    }

    public MerchantShopState getMerchantStateByMerchantId(UUID merchantId) {
        if (merchantId == null) {
            return null;
        }
        for (MerchantShopState state : activeMerchantStates.values()) {
            if (merchantId.equals(state.getMerchantId())) {
                return state;
            }
        }
        return null;
    }

    public boolean isMarkedMerchantNpc(Entity entity) {
        return entity != null
                && merchantNpcKey != null
                && entity.getPersistentDataContainer().has(merchantNpcKey, PersistentDataType.BYTE);
    }

    public boolean cleanupStaleMerchantNpc(Entity entity) {
        if (!isMarkedMerchantNpc(entity)) {
            return false;
        }

        MerchantShopState state = getMerchantState(entity);
        if (state != null && state.getDespawnAtMillis() > System.currentTimeMillis()) {
            return false;
        }

        boolean changed = false;
        UUID entityId = entity.getUniqueId();
        for (Map.Entry<String, MerchantShopState> entry : new ArrayList<>(activeMerchantStates.entrySet())) {
            MerchantShopState merchantState = entry.getValue();
            if (merchantState == null || entityId.equals(merchantState.getEntityId())) {
                activeMerchantStates.remove(entry.getKey());
                changed = true;
            }
        }

        entity.remove();
        if (changed) {
            saveMerchantData();
        }
        return true;
    }

    public boolean isMerchantNpc(Entity entity) {
        return isMarkedMerchantNpc(entity) && getMerchantState(entity) != null;
    }

    public void handleMerchantBuy(Player player, int offerIndex) {
        if (player == null) {
            return;
        }
        if (isMerchantTradeCoolingDown(player.getUniqueId())) {
            player.sendMessage(getMessage("merchant.cooldown", placeholders(
                    "time", formatLongDurationWords(getMerchantTradeCooldownRemainingMillis(player.getUniqueId()))
            )));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8F, 0.9F);
            return;
        }
        List<MerchantShopOffer> offers = getMerchantBuyOffers();
        if (offerIndex < 0 || offerIndex >= offers.size()) {
            return;
        }
        MerchantShopOffer offer = offers.get(offerIndex);
        double price = offer.getPrice();
        int remaining = merchantSharedStock.getOrDefault(offer.getKey(), offer.getStock());
        if (remaining <= 0) {
            player.sendMessage(getMessage("merchant.sold-out"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8F, 0.9F);
            return;
        }
        if (getBalance(player.getUniqueId()) < price) {
            player.sendMessage(getMessage("merchant.not-enough-money", placeholders("money", formatMoney(price))));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8F, 0.9F);
            return;
        }
        withdrawBalance(player.getUniqueId(), price);
        player.getInventory().addItem(new ItemStack(offer.getMaterial(), offer.getAmount()));
        merchantSharedStock.put(offer.getKey(), remaining - 1);
        merchantTradeCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + getMerchantTradeCooldownMillis(player.getUniqueId(), getPlayerCountry(player.getUniqueId())));
        saveMerchantData();
        player.sendMessage(getMessage("merchant.bought", placeholders(
                "amount", String.valueOf(offer.getAmount()),
                "item", formatMaterialName(offer.getMaterial()),
                "money", formatMoney(price)
        )));
        player.playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 0.9F, 1.05F);
    }

    public void handleMerchantSell(Player player, int offerIndex) {
        if (player == null) {
            return;
        }
        if (isMerchantTradeCoolingDown(player.getUniqueId())) {
            player.sendMessage(getMessage("merchant.cooldown", placeholders(
                    "time", formatLongDurationWords(getMerchantTradeCooldownRemainingMillis(player.getUniqueId()))
            )));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8F, 0.9F);
            return;
        }
        List<MerchantShopOffer> offers = getMerchantSellOffers();
        if (offerIndex < 0 || offerIndex >= offers.size()) {
            return;
        }
        MerchantShopOffer offer = offers.get(offerIndex);
        int removed = removeItemsUpTo(player, offer.getMaterial(), offer.getAmount());
        if (removed <= 0) {
            player.sendMessage(getMessage("merchant.no-sell-items", placeholders("item", formatMaterialName(offer.getMaterial()))));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8F, 0.9F);
            return;
        }
        double payoutPerItem = getMerchantSellPrice(offer);
        double total = roundMoney(payoutPerItem * removed);
        depositBalance(player.getUniqueId(), total);
        merchantDailySoldAmounts.merge(offer.getMaterial().name(), removed, Integer::sum);
        merchantTradeCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + getMerchantTradeCooldownMillis(player.getUniqueId(), getPlayerCountry(player.getUniqueId())));
        saveMerchantData();
        player.sendMessage(getMessage("merchant.sold", placeholders(
                "amount", String.valueOf(removed),
                "item", formatMaterialName(offer.getMaterial()),
                "money", formatMoney(total)
        )));
        player.playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 0.9F, 1.05F);
    }

    private boolean isMerchantTradeCoolingDown(UUID playerId) {
        return getMerchantTradeCooldownRemainingMillis(playerId) > 0L;
    }

    private long getMerchantTradeCooldownRemainingMillis(UUID playerId) {
        return Math.max(0L, merchantTradeCooldowns.getOrDefault(playerId, 0L) - System.currentTimeMillis());
    }

    public long getMerchantTradeCooldownRemainingMillisPublic(UUID playerId) {
        return getMerchantTradeCooldownRemainingMillis(playerId);
    }

    private void resetMerchantDailySalesIfNeeded(long now) {
        if (merchantDailySalesResetMillis <= 0L) {
            merchantDailySalesResetMillis = now + (24L * 60L * 60L * 1000L);
            return;
        }
        if (now >= merchantDailySalesResetMillis) {
            merchantDailySoldAmounts.clear();
            merchantDailySalesResetMillis = now + (24L * 60L * 60L * 1000L);
            saveMerchantData();
        }
    }

    public List<String> getAllowedTradeCountryNames(Country ownerCountry) {
        List<String> names = new ArrayList<>();
        if (ownerCountry == null) {
            return names;
        }
        for (String key : ownerCountry.getAllowedTradeCountries()) {
            Country country = getCountryByKey(key);
            names.add(country != null ? country.getName() : key);
        }
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    public long getCountryHomeCooldownRemaining(UUID playerId) {
        long nextUse = countryHomeCooldowns.getOrDefault(playerId, 0L);
        return Math.max(0L, nextUse - System.currentTimeMillis());
    }

    public void setCountryHomeCooldown(UUID playerId, long nextUseMillis) {
        countryHomeCooldowns.put(playerId, nextUseMillis);
    }

    public FurnaceAccessResult getFurnaceAccess(Location location, UUID playerId) {
        FurnaceKey key = FurnaceKey.from(location);
        FurnaceSession session = furnaceSessions.get(key);
        if (session == null) {
            return FurnaceAccessResult.UNLOCKED;
        }

        if (session.isExpired()) {
            clearFurnaceSession(location);
            return FurnaceAccessResult.EXPIRED;
        }

        UUID assignedOwnerId = session.getAssignedOwnerId();
        if (assignedOwnerId == null) {
            return FurnaceAccessResult.UNLOCKED;
        }

        if (assignedOwnerId.equals(playerId)) {
            return FurnaceAccessResult.OWNER;
        }

        return FurnaceAccessResult.LOCKED_OTHER;
    }

    public void noteFurnaceProcessor(Location location, Player player) {
        FurnaceKey key = FurnaceKey.from(location);
        FurnaceSession session = furnaceSessions.computeIfAbsent(key, ignored -> new FurnaceSession());
        session.ownerId = player.getUniqueId();
        session.ownerName = player.getName();
        session.lockedAt = System.currentTimeMillis();
        session.pendingOwnerId = player.getUniqueId();
        session.pendingOwnerName = player.getName();
        saveFurnaceSession(key, session);
    }

    public void activateFurnaceOutputLock(Location location) {
        FurnaceKey key = FurnaceKey.from(location);
        FurnaceSession session = furnaceSessions.get(key);
        if (session == null) {
            return;
        }
        if (session.isExpired()) {
            clearFurnaceSession(location);
            return;
        }
        if (session.pendingOwnerId == null) {
            return;
        }

        session.ownerId = session.pendingOwnerId;
        session.ownerName = session.pendingOwnerName;
        session.lockedAt = System.currentTimeMillis();
        saveFurnaceSession(key, session);
    }

    public String getFurnaceLockOwnerName(Location location) {
        FurnaceSession session = furnaceSessions.get(FurnaceKey.from(location));
        if (session == null || session.isExpired()) {
            return null;
        }
        String assignedName = session.getAssignedOwnerName();
        if (assignedName != null && !assignedName.isBlank()) {
            return assignedName;
        }

        UUID assignedOwnerId = session.getAssignedOwnerId();
        if (assignedOwnerId == null) {
            return null;
        }

        Player onlinePlayer = getServer().getPlayer(assignedOwnerId);
        if (onlinePlayer != null) {
            return onlinePlayer.getName();
        }

        OfflinePlayer offlinePlayer = getServer().getOfflinePlayer(assignedOwnerId);
        String offlineName = offlinePlayer.getName();
        return offlineName != null && !offlineName.isBlank() ? offlineName : null;
    }

    public UUID getFurnaceAssignedOwnerId(Location location) {
        FurnaceSession session = furnaceSessions.get(FurnaceKey.from(location));
        if (session == null || session.isExpired()) {
            return null;
        }
        return session.getAssignedOwnerId();
    }

    public void clearFurnaceOutputLock(Location location) {
        FurnaceKey key = FurnaceKey.from(location);
        FurnaceSession session = furnaceSessions.get(key);
        if (session == null) {
            return;
        }

        session.ownerId = null;
        session.ownerName = null;
        session.lockedAt = 0L;
        session.pendingOwnerId = null;
        session.pendingOwnerName = null;

        if (session.isEmpty()) {
            furnaceSessions.remove(key);
            dataConfig.set("furnaces." + key.asPath(), null);
            saveDataConfig();
            return;
        }

        saveFurnaceSession(key, session);
    }

    public void clearFurnaceSession(Location location) {
        FurnaceKey key = FurnaceKey.from(location);
        furnaceSessions.remove(key);
        dataConfig.set("furnaces." + key.asPath(), null);
        saveDataConfig();
    }

    public void addFurnaceSmeltContribution(Location location, Profession profession, UUID playerId, int amount) {
        if (playerId == null || profession == null || amount <= 0) {
            return;
        }

        FurnaceKey key = FurnaceKey.from(location);
        FurnaceSession session = furnaceSessions.computeIfAbsent(key, ignored -> new FurnaceSession());
        session.getPendingMap(profession).merge(playerId, amount, Integer::sum);
        saveFurnaceSession(key, session);
    }

    public Map<UUID, Integer> consumeFurnaceSmeltContributions(Location location, Profession profession, int amount) {
        Map<UUID, Integer> consumed = new LinkedHashMap<>();
        if (profession == null || amount <= 0) {
            return consumed;
        }

        FurnaceKey key = FurnaceKey.from(location);
        FurnaceSession session = furnaceSessions.get(key);
        if (session == null) {
            return consumed;
        }

        Map<UUID, Integer> pending = session.getPendingMap(profession);
        if (pending.isEmpty()) {
            return consumed;
        }

        int remaining = amount;
        for (Map.Entry<UUID, Integer> entry : new ArrayList<>(pending.entrySet())) {
            if (remaining <= 0) {
                break;
            }

            int take = Math.min(remaining, entry.getValue());
            if (take <= 0) {
                continue;
            }

            consumed.put(entry.getKey(), take);
            int left = entry.getValue() - take;
            if (left <= 0) {
                pending.remove(entry.getKey());
            } else {
                pending.put(entry.getKey(), left);
            }
            remaining -= take;
        }

        saveFurnaceSession(key, session);
        return consumed;
    }

    public CountryTransferRequest getPendingCountryTransfer(UUID targetPlayerId) {
        return pendingCountryTransfers.get(targetPlayerId);
    }

    public void setPendingCountryTransfer(Country country, UUID currentOwnerId, UUID targetPlayerId) {
        pendingCountryTransfers.put(targetPlayerId, new CountryTransferRequest(normalizeCountryName(country.getName()), currentOwnerId, targetPlayerId));
    }

    public void clearPendingCountryTransfer(UUID targetPlayerId) {
        pendingCountryTransfers.remove(targetPlayerId);
    }

    public boolean canSetCountryOwner(Country country, UUID newOwnerId) {
        Country playerCountry = getPlayerCountry(newOwnerId);
        return playerCountry == null || playerCountry == country;
    }

    public void setCountryOwner(Country country, OfflinePlayer newOwner) {
        UUID newOwnerId = newOwner.getUniqueId();
        country.getMembers().add(newOwnerId);
        country.getCoOwners().remove(newOwnerId);
        country.getStewards().remove(newOwnerId);
        playerCountries.put(newOwnerId, normalizeCountryName(country.getName()));
        country.setOwnerId(newOwnerId);
        saveCountry(country);
        syncCountryTerritory(country);
        refreshCountryTags(country);
    }

    public TerritoryOperationResult bindCountryTerritory(Country country, String worldName, String regionId) {
        TerritoryOperationResult result = territoryService.bindCountry(country, countriesByKey.values(), worldName, regionId);
        if (result.success()) {
            saveCountry(country);
        }
        return result;
    }

    public TerritoryOperationResult clearCountryTerritory(Country country) {
        TerritoryOperationResult result = territoryService.clearCountry(country);
        if (result.success()) {
            saveCountry(country);
        }
        return result;
    }

    public TerritoryOperationResult syncCountryTerritory(Country country) {
        return territoryService.syncCountry(country);
    }

    public boolean hasTerritoryIntegration() {
        return territoryService.isAvailable();
    }

    public String describeCountryTerritory(Country country) {
        return territoryService.describeTerritory(country);
    }

    public List<Location> getCountryBorderLocations(Country country) {
        if (country == null) {
            return List.of();
        }
        String countryKey = normalizeCountryKey(country.getName());
        return countryBorderLocationCache.computeIfAbsent(countryKey, ignored -> List.copyOf(territoryService.getBorderLocations(country)));
    }

    public List<String> getTerritoryRegionIds(String worldName) {
        return territoryService.getRegionIds(worldName);
    }

    public Country getCountryAt(org.bukkit.Location location) {
        return territoryService.getCountryAt(location, countriesByKey.values());
    }

    public boolean showCountryBorders(Player player, Country country) {
        if (player == null || country == null || !country.hasTerritory()) {
            return false;
        }
        List<Location> borderLocations = getCountryBorderLocations(country);
        if (borderLocations.isEmpty()) {
            return false;
        }

        Color color = country.isOpen() ? Color.fromRGB(46, 204, 64) : Color.fromRGB(255, 65, 54);
        Particle.DustOptions dust = new Particle.DustOptions(color, 1.2F);
        for (Location location : borderLocations) {
            if (location.getWorld() == null || !location.getWorld().equals(player.getWorld())) {
                continue;
            }
            player.spawnParticle(Particle.DUST, location, 1, 0.0D, 0.0D, 0.0D, 0.0D, dust);
            player.spawnParticle(Particle.END_ROD, location.clone().add(0.0D, 0.4D, 0.0D), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        return true;
    }

    public boolean isCountryBorderParticlesEnabled(UUID playerId) {
        return playerId != null && !countryBorderParticlesDisabledPlayers.contains(playerId);
    }

    public void setCountryBorderParticlesEnabled(UUID playerId, boolean enabled) {
        if (playerId == null) {
            return;
        }
        if (enabled) {
            countryBorderParticlesDisabledPlayers.remove(playerId);
            dataConfig.set("country-border-particles.disabled." + playerId, null);
        } else {
            countryBorderParticlesDisabledPlayers.add(playerId);
            dataConfig.set("country-border-particles.disabled." + playerId, true);
        }
        saveDataConfig();
    }

    private int getStabilityMeterRequiredActions() {
        return Math.max(5, getConfig().getInt("stability.meter.actions-per-charge", 20));
    }

    private long getStabilityMeterDebugDurationTicks() {
        return Math.max(20L, getConfig().getLong("stability.meter.debug-duration-ticks", 100L));
    }

    private boolean hasTemporaryStabilityDebug(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        long expiresAt = temporaryStabilityDebugExpiries.getOrDefault(playerId, 0L);
        if (expiresAt <= System.currentTimeMillis()) {
            temporaryStabilityDebugExpiries.remove(playerId);
            return false;
        }
        return true;
    }

    private void grantTemporaryStabilityDebugVision(Player player) {
        if (player == null) {
            return;
        }
        long durationMillis = getStabilityMeterDebugDurationTicks() * 50L;
        temporaryStabilityDebugExpiries.put(player.getUniqueId(), System.currentTimeMillis() + durationMillis);
        refreshStabilityDebugDisplays(player);
        double durationSeconds = getStabilityMeterDebugDurationTicks() / 20.0D;
        player.sendMessage(Component.text("Terra stability vision charged for "
                + String.format(Locale.US, "%.1f", durationSeconds) + " seconds.", NamedTextColor.GOLD));
    }

    private void sendStabilityMeterStatus(Player player, int progress, int requiredActions, boolean charged, boolean breakAction) {
        if (player == null) {
            return;
        }
        int percent = (int) Math.round((progress * 100.0D) / requiredActions);
        int filledBars = Math.max(0, Math.min(10, (int) Math.round(percent / 10.0D)));
        String meterBar = "|".repeat(filledBars) + ".".repeat(Math.max(0, 10 - filledBars));
        NamedTextColor accent = charged ? NamedTextColor.GOLD : NamedTextColor.YELLOW;
        String action = breakAction ? "Break" : "Place";
        player.sendMessage(Component.text()
                .append(Component.text("Terra Stability ", NamedTextColor.GRAY))
                .append(Component.text("[" + meterBar + "] ", accent))
                .append(Component.text(percent + "% ", accent))
                .append(Component.text("(" + action + ")", NamedTextColor.DARK_GRAY))
                .build());
    }

    public TerritoryDebugInfo getTerritoryDebugInfo(Country country) {
        return territoryService.getDebugInfo(country);
    }

    public int countCountryFarmlandBlocks(Country country) {
        return territoryService.countFarmlandBlocks(country);
    }

    public int getCountryFarmlandLimit(Country country) {
        return Math.max(0, (country.getMembers().size() * 16) + getCountryFarmlandBonus(country));
    }

    public Country getLastTerritoryCountry(UUID playerId) {
        String countryKey = lastTerritoryCountries.get(playerId);
        if (countryKey == null) {
            return null;
        }
        return countriesByKey.get(countryKey);
    }

    public void setLastTerritoryCountry(UUID playerId, Country country) {
        if (country == null) {
            lastTerritoryCountries.remove(playerId);
            return;
        }
        lastTerritoryCountries.put(playerId, normalizeCountryName(country.getName()));
    }

    public void clearLastTerritoryCountry(UUID playerId) {
        lastTerritoryCountries.remove(playerId);
    }

    public boolean areTerritoryNotificationsEnabled() {
        return getConfig().getBoolean("territories.chat-notifications.enabled", true);
    }

    public boolean areTerritoryTitlesEnabled() {
        return getConfig().getBoolean("territories.title-notifications.enabled", true);
    }

    public void sendTerritoryEnterTitle(Player player, Country country) {
        if (!areTerritoryTitlesEnabled()) {
            return;
        }

        int fadeIn = getConfig().getInt("territories.title-notifications.fade-in", 10);
        int stay = getConfig().getInt("territories.title-notifications.stay", 50);
        int fadeOut = getConfig().getInt("territories.title-notifications.fade-out", 20);
        String title = getMessage("country.territory.title.enter", placeholders("country", country.getName()));
        String subtitle = getMessage("country.territory.subtitle.enter", placeholders("country", country.getName()));
        player.showTitle(Title.title(
                legacyComponent(title),
                legacyComponent(subtitle),
                Title.Times.times(
                        Duration.ofMillis(fadeIn * 50L),
                        Duration.ofMillis(stay * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        ));
        playConfiguredSound(player, "territories.title-notifications.enter-sound", Sound.BLOCK_NOTE_BLOCK_CHIME);
    }

    public void sendTerritoryLeaveTitle(Player player, Country country) {
        if (!areTerritoryTitlesEnabled()) {
            return;
        }

        int fadeIn = getConfig().getInt("territories.title-notifications.fade-in", 10);
        int stay = getConfig().getInt("territories.title-notifications.stay", 40);
        int fadeOut = getConfig().getInt("territories.title-notifications.fade-out", 20);
        String title = getMessage("country.territory.title.leave", placeholders("country", country.getName()));
        String subtitle = getMessage("country.territory.subtitle.leave", placeholders("country", country.getName()));
        player.showTitle(Title.title(
                legacyComponent(title),
                legacyComponent(subtitle),
                Title.Times.times(
                        Duration.ofMillis(fadeIn * 50L),
                        Duration.ofMillis(stay * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                )
        ));
        playConfiguredSound(player, "territories.title-notifications.leave-sound", Sound.BLOCK_NOTE_BLOCK_BASS);
    }

    public boolean arePresenceSoundsEnabled() {
        return getConfig().getBoolean("player-presence-sounds.enabled", true);
    }

    public void playJoinPresenceSound(Player heardBy) {
        if (!arePresenceSoundsEnabled()) {
            return;
        }
        playConfiguredSound(
                heardBy,
                "player-presence-sounds.join-sound",
                "player-presence-sounds.sound-volume",
                "player-presence-sounds.sound-pitch",
                Sound.BLOCK_AMETHYST_BLOCK_CHIME
        );
    }

    public void playLeavePresenceSound(Player heardBy) {
        if (!arePresenceSoundsEnabled()) {
            return;
        }
        playConfiguredSound(
                heardBy,
                "player-presence-sounds.leave-sound",
                "player-presence-sounds.sound-volume",
                "player-presence-sounds.sound-pitch",
                Sound.BLOCK_AMETHYST_CLUSTER_BREAK
        );
    }

    public boolean areCountrySoundsEnabled() {
        return getConfig().getBoolean("country-sounds.enabled", true);
    }

    public void playCountryInviteSentSound(Player player) {
        if (!areCountrySoundsEnabled()) {
            return;
        }
        playConfiguredSound(
                player,
                "country-sounds.invite-sent-sound",
                "country-sounds.sound-volume",
                "country-sounds.sound-pitch",
                Sound.BLOCK_NOTE_BLOCK_PLING
        );
    }

    public void playCountryInviteReceivedSound(Player player) {
        if (!areCountrySoundsEnabled()) {
            return;
        }
        playConfiguredSound(
                player,
                "country-sounds.invite-received-sound",
                "country-sounds.sound-volume",
                "country-sounds.sound-pitch",
                Sound.ENTITY_EXPERIENCE_ORB_PICKUP
        );
    }

    public void playCountryTransferRequestSound(Player player) {
        if (!areCountrySoundsEnabled()) {
            return;
        }
        playConfiguredSound(
                player,
                "country-sounds.transfer-request-sound",
                "country-sounds.sound-volume",
                "country-sounds.sound-pitch",
                Sound.BLOCK_AMETHYST_BLOCK_RESONATE
        );
    }

    public void playCountryTransferAcceptedSound(Player player) {
        if (!areCountrySoundsEnabled()) {
            return;
        }
        playConfiguredSound(
                player,
                "country-sounds.transfer-accepted-sound",
                "country-sounds.sound-volume",
                "country-sounds.sound-pitch",
                Sound.UI_TOAST_CHALLENGE_COMPLETE
        );
    }

    public boolean areChatSoundsEnabled() {
        return getConfig().getBoolean("chat-sounds.enabled", true);
    }

    public void playChatMessageSound(Player player) {
        if (player == null || !areChatSoundsEnabled()) {
            return;
        }
        playConfiguredSound(
                player,
                "chat-sounds.message-sound",
                "chat-sounds.sound-volume",
                "chat-sounds.sound-pitch",
                Sound.BLOCK_NOTE_BLOCK_HAT
        );
    }

    public void playChatMessageSoundToRecipients(Player sender, List<Player> recipients) {
        if (sender == null || recipients == null || !areChatSoundsEnabled()) {
            return;
        }
        for (Player recipient : recipients) {
            if (recipient == null || recipient.equals(sender) || isTutorialIntroActive(recipient.getUniqueId())) {
                continue;
            }
            playChatMessageSound(recipient);
        }
    }

    public List<String> getWorldNames() {
        List<String> worldNames = new ArrayList<>();
        getServer().getWorlds().forEach(world -> worldNames.add(world.getName()));
        worldNames.sort(String.CASE_INSENSITIVE_ORDER);
        return worldNames;
    }

    public List<String> getCountryNames() {
        List<String> countryNames = new ArrayList<>();
        for (Country country : countriesByKey.values()) {
            countryNames.add(country.getName());
        }
        countryNames.sort(String.CASE_INSENSITIVE_ORDER);
        return countryNames;
    }

    public String getMessage(String path) {
        return colorize(messagesConfig.getString(path, "&cMissing message: " + path));
    }

    public boolean hasMessage(String path) {
        return messagesConfig.contains(path);
    }

    public String getMessage(String path, Map<String, String> replacements) {
        String message = getMessage(path);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }

    public String getPlayerMessage(String path, Player player, Map<String, String> replacements) {
        String message = messagesConfig.getString(path, "&cMissing message: " + path);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        if (player != null) {
            message = message.replace("{prefix}", resolveChatPrefix(player));
        }
        if (player != null && getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        return colorize(message);
    }

    public String getChatMessage(String path) {
        return colorize(chatConfig.getString(path, "&cMissing chat message: " + path));
    }

    public String getChatMessage(String path, Map<String, String> replacements) {
        String message = getChatMessage(path);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }

    public String getPlayerChatMessage(String path, Player player, Map<String, String> replacements) {
        String message = chatConfig.getString(path, "&cMissing chat message: " + path);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        if (player != null) {
            message = message.replace("{prefix}", resolveChatPrefix(player));
        }
        if (player != null && getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        return colorize(message);
    }

    public Component buildPlayerChatComponent(String path, Player player, Map<String, String> replacements) {
        final String playerToken = "__PLAYER_COMPONENT__";
        String message = chatConfig.getString(path, "&cMissing chat message: " + path);
        message = message.replace("%player%", playerToken);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            if ("player".equalsIgnoreCase(entry.getKey())) {
                continue;
            }
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        if (player != null) {
            message = message.replace("{prefix}", resolveChatPrefix(player));
        }
        if (player != null && getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        String[] parts = message.split(playerToken, -1);
        Component built = Component.empty();
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                built = built.append(legacyComponent(parts[i]));
            }
            if (i + 1 < parts.length) {
                String styleCodes = extractTrailingLegacyCodes(parts[i]);
                built = built.append(createChatPlayerComponent(player, styleCodes));
            }
        }
        return built;
    }

    private String resolveChatPrefix(Player player) {
        if (player == null || getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return "";
        }
        String resolved = PlaceholderAPI.setPlaceholders(player, "%luckperms_prefix%");
        if (resolved == null || resolved.isBlank() || resolved.equals("%luckperms_prefix%")) {
            return "";
        }
        return resolved.endsWith(" ") ? resolved : resolved + " ";
    }

    private Component createChatPlayerComponent(Player player, String styleCodes) {
        if (player == null) {
            return legacyComponent(styleCodes + "Unknown");
        }
        return legacyComponent(styleCodes + player.getName())
                .hoverEvent(HoverEvent.showText(buildChatPlayerHover(player)));
    }

    private Component buildChatPlayerHover(Player player) {
        UUID playerId = player.getUniqueId();
        Country country = getPlayerCountry(playerId);
        List<Component> lines = new ArrayList<>();
        lines.add(legacyComponent("&6" + player.getName()));
        lines.add(legacyComponent("&7Country: &f" + (country != null ? country.getName() : "None")));
        lines.add(legacyComponent("&7Country Tag: &f" + (country != null && country.hasTag() ? country.getTag() : "None")));
        lines.add(legacyComponent("&7Jobs:"));

        List<Profession> professions = getOwnedProfessions(playerId);
        if (professions.isEmpty()) {
            lines.add(legacyComponent("&f- None"));
        } else {
            for (Profession profession : professions) {
                String marker = profession == getProfession(playerId) ? " &a(active)" : "";
                lines.add(legacyComponent("&f- " + getProfessionPlainDisplayName(profession)
                        + " &7Lv &f" + getProfessionLevel(playerId, profession) + marker));
            }
        }

        Component hover = Component.empty();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                hover = hover.append(Component.newline());
            }
            hover = hover.append(lines.get(i));
        }
        return hover;
    }

    private String getPlayerCountryTagValue(UUID playerId) {
        Country country = getPlayerCountry(playerId);
        if (country == null || !country.hasTag()) {
            return "";
        }
        return country.getTag();
    }

    private String extractTrailingLegacyCodes(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String color = "";
        StringBuilder formats = new StringBuilder();
        for (int i = 0; i < text.length() - 1; i++) {
            char marker = text.charAt(i);
            if ((marker != '&' && marker != '§')) {
                continue;
            }
            char code = Character.toLowerCase(text.charAt(i + 1));
            if (!isLegacyCode(code)) {
                continue;
            }
            if (code == 'r') {
                color = "&r";
                formats.setLength(0);
                continue;
            }
            if (isColorCode(code)) {
                color = "&" + code;
                formats.setLength(0);
                continue;
            }
            if (formats.indexOf("&" + code) < 0) {
                formats.append('&').append(code);
            }
        }
        return color + formats;
    }

    private boolean isLegacyCode(char code) {
        return isColorCode(code) || "klmnor".indexOf(code) >= 0;
    }

    private boolean isColorCode(char code) {
        return (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f');
    }

    public List<String> getMessageList(String path) {
        List<String> lines = messagesConfig.getStringList(path);
        List<String> colored = new ArrayList<>();
        for (String line : lines) {
            colored.add(colorize(line));
        }
        return colored;
    }

    public Map<String, String> placeholders(String... values) {
        Map<String, String> replacements = new HashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            replacements.put(values[i], values[i + 1]);
        }
        return replacements;
    }

    private void reloadPluginSettings() {
        getConfig().addDefault("block-delay.enabled", false);
        getConfig().addDefault("block-delay.seconds", 30);
        getConfig().addDefault("economy.enabled", true);
        getConfig().addDefault("rewards.break.enabled", true);
        getConfig().addDefault("rewards.break.money-enabled", true);
        getConfig().addDefault("hostile-mobs.spawning-enabled", true);
        getConfig().addDefault("phantoms.enabled", false);
        getConfig().addDefault("wilderness-regeneration.enabled", true);
        getConfig().addDefault("wilderness-regeneration.seconds", 600L);
        getConfig().addDefault("wilderness-regeneration.build-decay-seconds", 300L);
        getConfig().addDefault("ore-vision.horizontal-radius", 20);
        getConfig().addDefault("ore-vision.vertical-radius", 12);
        getConfig().addDefault("traders.enabled", true);
        getConfig().addDefault("traders.spawn-minutes-min", 180L);
        getConfig().addDefault("traders.spawn-minutes-max", 360L);
        getConfig().addDefault("traders.active-minutes", 90L);
        getConfig().addDefault("traders.delivery-cooldown-minutes", 30L);
        getConfig().addDefault("traders.accepted-quest-minutes", 1440L);
        getConfig().addDefault("merchant-shop.enabled", true);
        getConfig().addDefault("merchant-shop.spawn-minutes-min", 60L);
        getConfig().addDefault("merchant-shop.spawn-minutes-max", 120L);
        getConfig().addDefault("merchant-shop.active-minutes", 10L);
        getConfig().addDefault("merchant-shop.rotation-minutes", 2L);
        getConfig().addDefault("merchant-shop.trade-cooldown-seconds", 30L);
        getConfig().addDefault("merchant-shop.spawn-radius", 8);
        getConfig().addDefault("merchant-shop.random-buy-rotation-enabled", true);
        getConfig().addDefault("merchant-shop.random-buy-offers-per-rotation", 5);
        ensureMerchantShopConfigDefaults();
        getConfig().addDefault("npc-head-tracking.enabled", true);
        getConfig().addDefault("npc-head-tracking.range-blocks", 5.0D);
        getConfig().addDefault("npc-head-tracking.interval-ticks", 4L);
        getConfig().addDefault("country-border-particles.enabled", true);
        getConfig().addDefault("country-border-particles.interval-ticks", 20L);
        getConfig().addDefault("country-border-particles.view-radius-blocks", 48.0D);
        getConfig().addDefault("realtime-clock.enabled", false);
        getConfig().addDefault("realtime-clock.timezone", "Europe/Amsterdam");
        getConfig().addDefault("realtime-clock.update-interval-ticks", 2400L);
        getConfig().addDefault("realtime-clock.freeze-daylight-cycle", true);
        getConfig().addDefault("realtime-clock.worlds", new ArrayList<>());
        getConfig().addDefault("player-presence-sounds.enabled", true);
        getConfig().addDefault("player-presence-sounds.join-sound", "BLOCK_AMETHYST_BLOCK_CHIME");
        getConfig().addDefault("player-presence-sounds.leave-sound", "BLOCK_AMETHYST_CLUSTER_BREAK");
        getConfig().addDefault("player-presence-sounds.sound-volume", 0.6D);
        getConfig().addDefault("player-presence-sounds.sound-pitch", 1.2D);
        getConfig().addDefault("server-list-motd.enabled", true);
        getConfig().addDefault("server-list-motd.change-interval", 25);
        getConfig().addDefault("server-list-motd.frames", getDefaultServerMotdFrames());
        getConfig().addDefault("health-hotbar.enabled", true);
        getConfig().addDefault("health-hotbar.include-armor", true);
        getConfig().addDefault("health-hotbar.format", "&c\u2764 &f%current%&7/&f%max%");
        getConfig().addDefault("custom-scoreboard.enabled", true);
        getConfig().addDefault("custom-scoreboard.update-ticks", 20L);
        getConfig().addDefault("custom-scoreboard.respect-other-scoreboards", true);
        getConfig().addDefault("custom-scoreboard.hide-numbers", true);
        getConfig().addDefault("custom-scoreboard.title", "&2&lᴛᴇʀʀᴀɴᴀᴛɪᴏɴѕ");
        getConfig().addDefault("custom-scoreboard.title-animation", "scoreboard_logo");
        getConfig().addDefault("custom-scoreboard.shadow.enabled", false);
        getConfig().addDefault("custom-scoreboard.shadow.color", "&8");
        getConfig().addDefault("custom-scoreboard.shadow.prefix", "");
        getConfig().addDefault("custom-scoreboard.lines", List.of(
                "&7%date% &8| &7%server_time%",
                "&f%player%",
                "",
                "&c⚐ &f%country% &7Lv.%country_level%",
                "&a⛏ &f%job% &7Lv.%job_level%",
                "&b✦ &f%job_xp%&7/&f%job_xp_required% XP",
                "&6⛃ &f$%money%",
                "",
                "&9⌚ &f%playtime%",
                "",
                "&7ᴘʟᴀʏ.ᴛᴇʀʀᴀɴᴀᴛɪᴏɴѕ.ɴᴇᴛ"
        ));
        getConfig().addDefault("itemsadder-top-status.enabled", true);
        getConfig().addDefault("itemsadder-top-status.require-itemsadder", true);
        getConfig().addDefault("itemsadder-top-status.update-ticks", 20L);
        getConfig().addDefault("itemsadder-top-status.tokens.panel", ":top_status_panel:");
        getConfig().addDefault("itemsadder-top-status.tokens.content-offset", ":offset_-248:");
        getConfig().addDefault("itemsadder-top-status.tokens.location-panel", ":top_status_location_panel:");
        getConfig().addDefault("itemsadder-top-status.tokens.location-offset", ":offset_-112:");
        getConfig().addDefault("itemsadder-top-status.layout.panel-width-pixels", 64);
        getConfig().addDefault("itemsadder-top-status.layout.location-panel-width-pixels", 96);
        getConfig().addDefault("itemsadder-top-status.layout.panel-gap-pixels", 8);
        getConfig().addDefault("itemsadder-top-status.format", "auto");
        getConfig().addDefault("itemsadder-top-status.wilderness-label", "Wilderness");
        getConfig().addDefault("itemsadder-top-status.max-location-chars", 7);
        getConfig().addDefault("join-leave-messages.enabled", true);
        getConfig().addDefault("lag-reduction.ground-item-clear.enabled", true);
        getConfig().addDefault("lag-reduction.ground-item-clear.interval-minutes", 5);
        getConfig().addDefault("lag-reduction.ground-item-clear.warning-seconds", List.of(60, 30, 10));
        getConfig().addDefault("lag-reduction.mob-stacking.enabled", true);
        getConfig().addDefault("lag-reduction.mob-stacking.radius-blocks", 8.0D);
        getConfig().addDefault("lag-reduction.mob-stacking.max-stack-size", 50);
        getConfig().addDefault("lag-reduction.mob-stacking.name-format", "&e%type% &7x&f%amount%");
        getConfig().addDefault("lag-reduction.item-merge.enabled", true);
        getConfig().addDefault("lag-reduction.item-merge.radius-blocks", 2.5D);
        getConfig().addDefault("country-sounds.enabled", true);
        getConfig().addDefault("country-sounds.invite-sent-sound", "BLOCK_NOTE_BLOCK_PLING");
        getConfig().addDefault("country-sounds.invite-received-sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        getConfig().addDefault("country-sounds.transfer-request-sound", "BLOCK_AMETHYST_BLOCK_RESONATE");
        getConfig().addDefault("country-sounds.transfer-accepted-sound", "UI_TOAST_CHALLENGE_COMPLETE");
        getConfig().addDefault("country-sounds.sound-volume", 0.8D);
        getConfig().addDefault("country-sounds.sound-pitch", 1.0D);
        getConfig().addDefault("chat-sounds.enabled", true);
        getConfig().addDefault("chat-sounds.message-sound", "BLOCK_NOTE_BLOCK_HAT");
        getConfig().addDefault("chat-sounds.sound-volume", 0.25D);
        getConfig().addDefault("chat-sounds.sound-pitch", 1.6D);
        getConfig().addDefault("chat-channels.global-cooldown-minutes", 5L);
        getConfig().addDefault("terra-tips.enabled", true);
        getConfig().addDefault("terra-tips.interval-minutes", 8L);
        getConfig().addDefault("climate.enabled", true);
        getConfig().addDefault("climate.temperature-unit", "C");
        getConfig().addDefault("climate.equator.mode", "spawn");
        getConfig().addDefault("climate.equator.coordinate", 0.0D);
        getConfig().addDefault("climate.playtest-mode.enabled", false);
        getConfig().addDefault("climate.playtest-mode.center-x", 0.0D);
        getConfig().addDefault("climate.playtest-mode.center-z", 0.0D);
        getConfig().addDefault("climate.playtest-mode.radius-blocks", 256.0D);
        getConfig().addDefault("climate.playtest-mode.center-temperature-c", 30.0D);
        getConfig().addDefault("climate.playtest-mode.edge-temperature-c", 18.0D);
        getConfig().addDefault("climate.pattern.enabled", true);
        getConfig().addDefault("climate.pattern.scale-blocks", 160.0D);
        getConfig().addDefault("climate.pattern.strength-c", 6.0D);
        getConfig().addDefault("climate.pattern.seed-offset", 0L);
        getConfig().addDefault("climate.biome-adaptation.enabled", true);
        getConfig().addDefault("climate.biome-adaptation.temperature-strength-c", 12.0D);
        getConfig().addDefault("climate.biome-adaptation.temperature-baseline", 0.8D);
        getConfig().addDefault("climate.biome-adaptation.humidity-weight", 0.65D);
        getConfig().addDefault("climate.altitude.optimal-y", 63.0D);
        getConfig().addDefault("climate.altitude.temperature-per-block-c", 0.035D);
        getConfig().addDefault("climate.altitude.growth-penalty-per-block", 0.004D);
        getConfig().addDefault("climate.altitude.soft-range-blocks", 36.0D);
        getConfig().addDefault("climate.altitude.highlands-start-y", 160.0D);
        getConfig().addDefault("climate.altitude.highlands-temperature-per-block-c", 0.03D);
        getConfig().addDefault("climate.altitude.highlands-growth-penalty-per-block", 0.004D);
        getConfig().addDefault("climate.altitude.cold-biome-temperature-threshold", 0.2D);
        getConfig().addDefault("climate.altitude.cold-biome-distance-multiplier", 1.35D);
        getConfig().addDefault("climate.latitude-scale-blocks", 750.0D);
        getConfig().addDefault("climate.seasons.enabled", true);
        getConfig().addDefault("climate.season-override", "auto");
        getConfig().addDefault("climate.debug-bossbar.enabled", true);
        getConfig().addDefault("climate.debug-bossbar.interval-ticks", 20L);
        getConfig().addDefault("climate.debug-particles.enabled", true);
        getConfig().addDefault("climate.debug-particles.interval-ticks", 10L);
        getConfig().addDefault("climate.debug-particles.view-radius-blocks", 80.0D);
        getConfig().addDefault("climate.debug-particles.spacing-blocks", 2);
        getConfig().addDefault("climate.debug-particles.size", 1.0D);
        getConfig().addDefault("climate.live-display.view-radius-blocks", 48.0D);
        getConfig().addDefault("climate.crop-effect.enabled", true);
        getConfig().addDefault("climate.crop-effect.interval-ticks", 1200L);
        getConfig().addDefault("climate.crop-effect.radius-blocks", 16);
        getConfig().addDefault("climate.rain.temperature-drop-c", 2.5D);
        getConfig().addDefault("climate.rain.recent-growth-bonus", 0.20D);
        getConfig().addDefault("climate.rain.recent-duration-seconds", 900L);
        getConfig().addDefault("climate.local-effects.enabled", true);
        getConfig().addDefault("climate.local-effects.block-radius", 3);
        getConfig().addDefault("climate.local-effects.vertical-radius", 2);
        getConfig().addDefault("climate.local-effects.max-heat-offset-c", 4.0D);
        getConfig().addDefault("climate.local-effects.max-cold-offset-c", 3.5D);
        getConfig().addDefault("climate.local-effects.submerged-temperature-offset-c", -2.5D);
        getConfig().addDefault("climate.freeze-water.enabled", true);
        getConfig().addDefault("climate.freeze-water.interval-ticks", 100L);
        getConfig().addDefault("climate.freeze-water.radius-blocks", 10);
        getConfig().addDefault("climate.freeze-water.vertical-radius-blocks", 4);
        getConfig().addDefault("hunger.speed-multiplier", 1.0D);
        getConfig().addDefault("hunger.climate.comfort-min-c", 12.0D);
        getConfig().addDefault("hunger.climate.comfort-max-c", 24.0D);
        getConfig().addDefault("hunger.climate.mild-step-c", 8.0D);
        getConfig().addDefault("hunger.climate.extreme-step-c", 18.0D);
        getConfig().addDefault("hunger.climate.mild-bonus", 0.18D);
        getConfig().addDefault("hunger.climate.extreme-bonus", 0.42D);
        getConfig().addDefault("hunger.climate.max-multiplier", 1.85D);
        getConfig().addDefault("profession-procs.farmer.instant-grow.base-chance", 0.02D);
        getConfig().addDefault("profession-procs.farmer.instant-grow.per-level", 0.006D);
        getConfig().addDefault("profession-procs.farmer.instant-grow.max-chance", 0.25D);
        getConfig().addDefault("profession-procs.lumberjack.instant-grow.base-chance", 0.02D);
        getConfig().addDefault("profession-procs.lumberjack.instant-grow.per-level", 0.005D);
        getConfig().addDefault("profession-procs.lumberjack.instant-grow.max-chance", 0.20D);
        getConfig().addDefault("stability.enabled", true);
        getConfig().addDefault("stability.scan-radius", 8);
        getConfig().addDefault("stability.warning-delay-ticks", 40L);
        getConfig().addDefault("stability.batch-interval-ticks", 2L);
        getConfig().addDefault("stability.collapse-batch-size", 18);
        getConfig().addDefault("stability.max-cluster-size", 192);
        getConfig().addDefault("stability.max-clusters-per-scan", 3);
        getConfig().addDefault("stability.max-pending-collapses", 24);
        getConfig().addDefault("stability.max-falling-blocks-per-collapse", 24);
        getConfig().addDefault("stability.player-warning-radius-blocks", 16.0D);
        getConfig().addDefault("stability.debug.enabled", true);
        getConfig().addDefault("stability.debug.interval-ticks", 20L);
        getConfig().addDefault("stability.debug.view-radius-blocks", 6.0D);
        getConfig().addDefault("stability.support-materials", getDefaultStructuralSupportMaterialNames());
        getConfig().addDefault("stability.support-radius-horizontal", 2);
        getConfig().addDefault("stability.support-radius-vertical", 2);
        getConfig().addDefault("stability.support-frame.horizontal-span", 3);
        getConfig().addDefault("stability.support-frame.vertical-height", 3);
        getConfig().addDefault("stability.loose-max-span", 2);
        getConfig().addDefault("stability.packed-max-span", 3);
        getConfig().addDefault("stability.soft-rock-max-span", 4);
        getConfig().addDefault("stability.fragile-max-span", 4);
        getConfig().addDefault("stability.min-open-faces", 2);
        getConfig().addDefault("stability.fall-sideways-speed", 0.16D);
        getConfig().addDefault("stability.loose-grounded-stack-height", 2);
        getConfig().addDefault("stability.shaft-open-depth", 3);
        getConfig().addDefault("stability.wetness.rain-stress", 0.7D);
        getConfig().addDefault("stability.wetness.near-water-stress", 0.8D);
        getConfig().addDefault("stability.wetness.water-radius", 2);
        getConfig().addDefault("stability.weight-trace-height", 16);
        getConfig().addDefault("stability.load-stress-multiplier", 0.42D);
        getConfig().addDefault("stability.adjacent-support-factor", 0.35D);
        getConfig().addDefault("stability.foundation-support-factor", 1.15D);
        getConfig().addDefault("stability.support-frame-bonus", 2.8D);
        getConfig().addDefault("stability.anchor-support-bonus", 1.5D);
        getConfig().addDefault("stability.roof-load-factor", 0.3D);
        getConfig().addDefault("stability.column-weight-factor", 0.24D);
        getConfig().addDefault("stability.strictness.percent", 100);
        getConfig().addDefault("stability.mining.natural-terrain-tolerance-multiplier", 1.85D);
        getConfig().addDefault("stability.mining.natural-min-collapse-size-bonus", 2);
        getConfig().addDefault("stability.mining.natural-roof-extra-span", 2);
        getConfig().addDefault("stability.mining.natural-shaft-extra-depth", 1);
        getConfig().addDefault("stability.mining.natural-tunnel-min-length", 6);
        getConfig().addDefault("stability.mining.natural-tunnel-min-width", 3);
        getConfig().addDefault("stability.mining.natural-tunnel-min-height", 3);
        getConfig().addDefault("stability.mining.natural-room-min-width", 5);
        getConfig().addDefault("stability.mining.natural-room-min-length", 5);
        getConfig().addDefault("stability.mining.natural-room-min-height", 5);
        getConfig().addDefault("stability.rubble.enabled", true);
        getConfig().addDefault("stability.rubble.chance", 0.22D);
        getConfig().addDefault("stability.rubble.max-per-collapse", 12);
        getConfig().addDefault("stability.loose-min-collapse-size", 2);
        getConfig().addDefault("stability.packed-min-collapse-size", 3);
        getConfig().addDefault("stability.soft-rock-min-collapse-size", 4);
        getConfig().addDefault("stability.fragile-min-collapse-size", 4);
        getConfig().addDefault("country-home.cooldown-seconds", 3600);
        getConfig().addDefault("items.ender-pearls-enabled", true);
        getConfig().addDefault("items.shulker-boxes-enabled", true);
        getConfig().addDefault("territories.enabled", true);
        getConfig().addDefault("territories.chat-notifications.enabled", true);
        getConfig().addDefault("territories.dynmap.enabled", true);
        getConfig().addDefault("territories.dynmap.marker-set-id", "terra_countries");
        getConfig().addDefault("territories.dynmap.marker-set-label", "Terra Countries");
        getConfig().addDefault("territories.dynmap.use-status-colors", true);
        getConfig().addDefault("territories.dynmap.line-color", "#4caf50");
        getConfig().addDefault("territories.dynmap.fill-color", "#4caf50");
        getConfig().addDefault("territories.dynmap.open-line-color", "#2ecc40");
        getConfig().addDefault("territories.dynmap.open-fill-color", "#2ecc40");
        getConfig().addDefault("territories.dynmap.closed-line-color", "#ff4136");
        getConfig().addDefault("territories.dynmap.closed-fill-color", "#ff4136");
        getConfig().addDefault("territories.dynmap.ownerless-line-color", "#7fdbff");
        getConfig().addDefault("territories.dynmap.ownerless-fill-color", "#7fdbff");
        getConfig().addDefault("territories.dynmap.show-center-marker", true);
        getConfig().addDefault("territories.dynmap.marker-icon", "world");
        getConfig().addDefault("territories.dynmap.marker-y-offset", 1.0D);
        getConfig().addDefault("territories.dynmap.line-weight", 2);
        getConfig().addDefault("territories.dynmap.line-opacity", 0.9D);
        getConfig().addDefault("territories.dynmap.fill-opacity", 0.4D);
        getConfig().options().copyDefaults(true);
        mergeManagedSettingsIntoRuntimeConfig();

        loadBlockRewardDefaults();
        reloadStructuralSupportMaterials();

        this.blockDelayEnabled = getConfig().getBoolean("block-delay.enabled", false);
        this.blockDelaySeconds = getConfig().getInt("block-delay.seconds", 30);
        reloadBalances();
        reloadBlockRewards();
        reloadBypassEntries();
        reloadCountries();
        reloadProfessions();
        reloadProfessionProgress();
        reloadProfessionSkillDefinitions();
        reloadProfessionSkillPointBonuses();
        reloadProfessionSkillProgress();
        reloadCountryBorderParticlePreferences();
        reloadStabilityMeterPreferences();
        reloadPendingStarterKitGrants();
        reloadTutorialQuestDefinitions();
        reloadTutorialStages();
        reloadAssignedQuestStates();
        reloadFixedOreBlocks();
        reloadFurnaceSessions();
        reloadTraderData();
        reloadMerchantData();
    }

    private void ensureMerchantShopConfigDefaults() {
        if (!isMerchantRandomBuyRotationEnabled() && merchantSettingsConfig.getConfigurationSection("merchant-shop.buy-rotations") == null) {
            for (int rotation = 0; rotation < getDefaultMerchantBuyOfferLayouts().size(); rotation++) {
                List<MerchantShopOffer> offers = getDefaultMerchantBuyOfferLayouts().get(rotation);
                for (int index = 0; index < offers.size(); index++) {
                    writeMerchantOfferConfig("merchant-shop.buy-rotations." + rotation + "." + index, offers.get(index));
                }
            }
        }
        if (merchantSettingsConfig.getConfigurationSection("merchant-shop.sell-offers") == null) {
            List<MerchantShopOffer> offers = getDefaultMerchantSellOffers();
            for (int index = 0; index < offers.size(); index++) {
                writeMerchantOfferConfig("merchant-shop.sell-offers." + index, offers.get(index));
            }
        }
    }

    private void writeMerchantOfferConfig(String path, MerchantShopOffer offer) {
        if (path == null || offer == null) {
            return;
        }
        setManagedConfigValue(path + ".material", offer.getMaterial().name());
        setManagedConfigValue(path + ".amount", offer.getAmount());
        setManagedConfigValue(path + ".price", offer.getPrice());
        if (offer.getType() == MerchantShopOffer.Type.BUY) {
            setManagedConfigValue(path + ".stock", offer.getStock());
        }
    }

    private void loadBlockRewardDefaults() {
        for (Material material : Material.values()) {
            if (!material.isBlock() || material.isAir()) {
                continue;
            }

            BlockReward reward = createDefaultReward(material);
            if (!blockValuesConfig.contains(material.name() + ".xp")) {
                blockValuesConfig.set(material.name() + ".xp", reward.xp());
            }
            if (!blockValuesConfig.contains(material.name() + ".money")) {
                blockValuesConfig.set(material.name() + ".money", reward.money());
            }
        }
        saveBlockValuesConfig();
    }

    private void reloadBlockRewards() {
        blockRewards.clear();
        ConfigurationSection section = blockValuesConfig.getConfigurationSection("");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            Material material = Material.matchMaterial(key);
            if (material == null || !material.isBlock() || material.isAir()) {
                continue;
            }

            int xp = blockValuesConfig.getInt(key + ".xp", 0);
            double money = blockValuesConfig.getDouble(key + ".money", 0.0D);
            blockRewards.put(material, new BlockReward(xp, scaleRewardMoney(money)));
        }
    }

    private BlockReward createScaledDefaultReward(Material material) {
        BlockReward reward = createDefaultReward(material);
        return new BlockReward(reward.xp(), scaleRewardMoney(reward.money()));
    }

    private BlockReward createDefaultReward(Material material) {
        String name = material.name();
        float hardness = material.getHardness();
        int xp = Math.max(1, Math.round(Math.max(hardness, 0.0F) * 3.0F));
        double money = Math.max(0.25D, Math.round(Math.max(hardness, 0.0F) * 1.5D * 100.0D) / 100.0D);

        if (name.contains("ANCIENT_DEBRIS")) {
            return new BlockReward(90, 45.0D);
        }
        if (name.contains("DIAMOND_ORE") || name.contains("DEEPSLATE_DIAMOND_ORE")) {
            return new BlockReward(60, 30.0D);
        }
        if (name.contains("EMERALD_ORE") || name.contains("DEEPSLATE_EMERALD_ORE")) {
            return new BlockReward(55, 27.5D);
        }
        if (name.contains("GOLD_ORE") || name.contains("DEEPSLATE_GOLD_ORE") || name.contains("NETHER_GOLD_ORE")) {
            return new BlockReward(35, 17.5D);
        }
        if (name.contains("IRON_ORE") || name.contains("DEEPSLATE_IRON_ORE")) {
            return new BlockReward(24, 12.0D);
        }
        if (name.contains("REDSTONE_ORE") || name.contains("DEEPSLATE_REDSTONE_ORE")) {
            return new BlockReward(22, 11.0D);
        }
        if (name.contains("LAPIS_ORE") || name.contains("DEEPSLATE_LAPIS_ORE")) {
            return new BlockReward(20, 10.0D);
        }
        if (name.contains("COPPER_ORE") || name.contains("DEEPSLATE_COPPER_ORE")) {
            return new BlockReward(16, 8.0D);
        }
        if (name.contains("COAL_ORE") || name.contains("DEEPSLATE_COAL_ORE")) {
            return new BlockReward(12, 6.0D);
        }
        if (name.contains("OBSIDIAN")) {
            return new BlockReward(45, 22.5D);
        }
        if (name.contains("SPAWNER")) {
            return new BlockReward(80, 40.0D);
        }
        if (name.contains("DEEPSLATE")) {
            return new BlockReward(Math.max(xp, 8), Math.max(money, 4.0D));
        }
        if (name.contains("STONE") || name.contains("COBBLESTONE")) {
            return new BlockReward(Math.max(xp, 4), Math.max(money, 2.0D));
        }
        if (name.contains("LOG") || name.contains("WOOD") || name.endsWith("STEM") || name.endsWith("HYPHAE")) {
            return new BlockReward(Math.max(xp, 5), Math.max(money, 2.5D));
        }

        return new BlockReward(xp, money);
    }

    private Material getWildernessRestoreMaterial(Material originalMaterial) {
        String name = originalMaterial.name();
        if (name.contains("ORE") || name.contains("ANCIENT_DEBRIS")) {
            return Material.STONE;
        }
        return originalMaterial;
    }

    private boolean setupLuckPerms() {
        if (getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            return false;
        }

        try {
            luckPerms = LuckPermsProvider.get();
            return luckPerms != null;
        } catch (IllegalStateException exception) {
            return false;
        }
    }

    private void reloadBypassEntries() {
        bypassEntries.clear();
        ConfigurationSection section = dataConfig.getConfigurationSection("bypass");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            UUID playerId;
            try {
                playerId = UUID.fromString(key);
            } catch (IllegalArgumentException exception) {
                continue;
            }

            String name = dataConfig.getString("bypass." + key + ".name");
            String enabledAtValue = dataConfig.getString("bypass." + key + ".enabledAt");
            if (name == null || enabledAtValue == null) {
                continue;
            }

            Instant enabledAt;
            try {
                enabledAt = Instant.parse(enabledAtValue);
            } catch (DateTimeParseException exception) {
                continue;
            }

            bypassEntries.put(playerId, new BypassEntry(playerId, name, enabledAt));
        }
    }

    private void reloadBalances() {
        balances.clear();
        ConfigurationSection section = dataConfig.getConfigurationSection("money");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(key);
                double amount = Math.max(0.0D, section.getDouble(key, 0.0D));
                if (amount > 0.0D) {
                    balances.put(playerId, roundMoney(amount));
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void reloadCountries() {
        countriesByKey.clear();
        playerCountries.clear();
        countryBorderLocationCache.clear();

        ConfigurationSection section = countryDataConfig.getConfigurationSection("countries");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            String path = "countries." + key;
            String name = countryDataConfig.getString(path + ".name");
            String ownerValue = countryDataConfig.getString(path + ".owner");
            if (name == null) {
                continue;
            }

            UUID ownerId = null;
            if (ownerValue != null && !ownerValue.isBlank()) {
                try {
                    ownerId = UUID.fromString(ownerValue);
                } catch (IllegalArgumentException exception) {
                    continue;
                }
            }

            Set<UUID> members = new LinkedHashSet<>();
            for (String member : countryDataConfig.getStringList(path + ".members")) {
                try {
                    UUID memberId = UUID.fromString(member);
                    members.add(memberId);
                    playerCountries.put(memberId, key);
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (ownerId != null) {
                members.add(ownerId);
                playerCountries.put(ownerId, key);
            }

            Set<UUID> invitedPlayers = new LinkedHashSet<>();
            for (String invited : countryDataConfig.getStringList(path + ".invites")) {
                try {
                    invitedPlayers.add(UUID.fromString(invited));
                } catch (IllegalArgumentException ignored) {
                }
            }

            Set<UUID> coOwners = new LinkedHashSet<>();
            for (String coOwner : countryDataConfig.getStringList(path + ".roles.coowners")) {
                try {
                    UUID playerId = UUID.fromString(coOwner);
                    if (members.contains(playerId)) {
                        coOwners.add(playerId);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }

            Set<UUID> stewards = new LinkedHashSet<>();
            for (String steward : countryDataConfig.getStringList(path + ".roles.stewards")) {
                try {
                    UUID playerId = UUID.fromString(steward);
                    if (members.contains(playerId)) {
                        stewards.add(playerId);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }

            boolean open = countryDataConfig.getBoolean(path + ".open", true);
            String tag = countryDataConfig.getString(path + ".tag");
            String territoryWorld = countryDataConfig.getString(path + ".territory.world");
            String territoryRegionId = countryDataConfig.getString(path + ".territory.region");
            String homeWorld = countryDataConfig.getString(path + ".home.world");
            double homeX = countryDataConfig.getDouble(path + ".home.x", 0.0D);
            double homeY = countryDataConfig.getDouble(path + ".home.y", 0.0D);
            double homeZ = countryDataConfig.getDouble(path + ".home.z", 0.0D);
            float homeYaw = (float) countryDataConfig.getDouble(path + ".home.yaw", 0.0D);
            float homePitch = (float) countryDataConfig.getDouble(path + ".home.pitch", 0.0D);
            String traderSpawnWorld = countryDataConfig.getString(path + ".trader.spawn.world");
            double traderSpawnX = countryDataConfig.getDouble(path + ".trader.spawn.x", 0.0D);
            double traderSpawnY = countryDataConfig.getDouble(path + ".trader.spawn.y", 0.0D);
            double traderSpawnZ = countryDataConfig.getDouble(path + ".trader.spawn.z", 0.0D);
            float traderSpawnYaw = (float) countryDataConfig.getDouble(path + ".trader.spawn.yaw", 0.0D);
            float traderSpawnPitch = (float) countryDataConfig.getDouble(path + ".trader.spawn.pitch", 0.0D);
            Set<String> allowedTradeCountries = new LinkedHashSet<>();
            for (String allowedKey : countryDataConfig.getStringList(path + ".trader.allowed-countries")) {
                if (allowedKey != null && !allowedKey.isBlank()) {
                    allowedTradeCountries.add(allowedKey.toLowerCase(Locale.ROOT));
                }
            }
            String lastTraderName = countryDataConfig.getString(path + ".trader.last.name");
            String lastTraderSpecialty = countryDataConfig.getString(path + ".trader.last.specialty");
            long lastTraderSeenAtMillis = Math.max(0L, countryDataConfig.getLong(path + ".trader.last.seen-at", 0L));
            int countryLevel = Math.max(1, Math.min(COUNTRY_MAX_LEVEL, countryDataConfig.getInt(path + ".progression.level", 1)));
            double treasuryBalance = roundMoney(Math.max(0.0D, countryDataConfig.getDouble(path + ".economy.balance", 0.0D)));
            int resourceStockpile = Math.max(0, countryDataConfig.getInt(path + ".economy.resources", 0));
            String activeBoostKey = normalizeCountryBoostKey(countryDataConfig.getString(path + ".boost.key"));
            long activeBoostUntilMillis = Math.max(0L, countryDataConfig.getLong(path + ".boost.until", 0L));
            Set<String> unlockedUpgradeKeys = new LinkedHashSet<>();
            for (String upgradeKey : countryDataConfig.getStringList(path + ".progression.unlocked")) {
                if (upgradeKey == null || upgradeKey.isBlank()) {
                    continue;
                }
                CountryUpgrade upgrade = CountryUpgrade.fromKey(upgradeKey);
                unlockedUpgradeKeys.add(upgrade != null ? upgrade.getKey() : upgradeKey);
            }
            Country country = new Country(name, ownerId, open, tag, territoryWorld, territoryRegionId, homeWorld, homeX, homeY, homeZ, homeYaw, homePitch,
                    members, coOwners, stewards, invitedPlayers, traderSpawnWorld, traderSpawnX, traderSpawnY, traderSpawnZ, traderSpawnYaw, traderSpawnPitch,
                    allowedTradeCountries, lastTraderName, lastTraderSpecialty, lastTraderSeenAtMillis, countryLevel, treasuryBalance, resourceStockpile,
                    activeBoostKey, activeBoostUntilMillis, unlockedUpgradeKeys);
            countriesByKey.put(key, country);
            refreshCountryProgressionState(country);
        }
    }

    private void reloadProfessions() {
        playerProfessions.clear();
        secondaryProfessions.clear();
        activeProfessions.clear();
        secondaryProfessionUnlockOverrides.clear();
        developmentModeProfessions.clear();

        ConfigurationSection section = dataConfig.getConfigurationSection("professions");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            UUID playerId;
            try {
                playerId = UUID.fromString(key);
            } catch (IllegalArgumentException exception) {
                continue;
            }

            String path = "professions." + key;
            if (dataConfig.isString(path)) {
                Profession profession = Profession.fromKey(dataConfig.getString(path));
                if (profession != null) {
                    playerProfessions.put(playerId, profession);
                    activeProfessions.put(playerId, profession);
                }
                continue;
            }

            Profession primary = Profession.fromKey(dataConfig.getString(path + ".primary"));
            Profession secondary = Profession.fromKey(dataConfig.getString(path + ".secondary"));
            Profession active = Profession.fromKey(dataConfig.getString(path + ".active"));

            if (primary != null) {
                playerProfessions.put(playerId, primary);
            }
            if (secondary != null && secondary != primary) {
                secondaryProfessions.put(playerId, secondary);
            }
            if (active != null && (active.equals(primary) || active.equals(secondary))) {
                activeProfessions.put(playerId, active);
            } else if (primary != null) {
                activeProfessions.put(playerId, primary);
            }

            if (dataConfig.contains(path + ".second-slot-override")) {
                secondaryProfessionUnlockOverrides.put(playerId, dataConfig.getBoolean(path + ".second-slot-override"));
            }

            Profession developmentProfession = Profession.fromKey(dataConfig.getString(path + ".development-mode"));
            if (developmentProfession != null) {
                developmentModeProfessions.put(playerId, developmentProfession);
            }
        }
    }

    private void reloadProfessionProgress() {
        professionProgress.clear();

        ConfigurationSection section = dataConfig.getConfigurationSection("profession-progress");
        if (section == null) {
            return;
        }

        for (String playerKey : section.getKeys(false)) {
            UUID playerId;
            try {
                playerId = UUID.fromString(playerKey);
            } catch (IllegalArgumentException exception) {
                continue;
            }

            ConfigurationSection playerSection = section.getConfigurationSection(playerKey);
            if (playerSection == null) {
                continue;
            }

            Map<Profession, ProfessionProgress> progressByProfession = new EnumMap<>(Profession.class);
            for (String professionKey : playerSection.getKeys(false)) {
                Profession profession = Profession.fromKey(professionKey);
                if (profession == null) {
                    continue;
                }

                int level = playerSection.getInt(professionKey + ".level", 1);
                int xp = playerSection.getInt(professionKey + ".xp", 0);
                progressByProfession.put(profession, new ProfessionProgress(level, xp));
            }

            if (!progressByProfession.isEmpty()) {
                professionProgress.put(playerId, progressByProfession);
            }
        }
    }

    private void reloadProfessionSkillDefinitions() {
        professionSkillNodes.clear();
        for (Profession profession : Profession.values()) {
            ConfigurationSection nodesSection = getProfessionConfig(profession).getConfigurationSection("skill-tree.nodes");
            Map<String, ProfessionSkillNode> nodesByKey = new LinkedHashMap<>();
            if (nodesSection != null) {
                for (String key : nodesSection.getKeys(false)) {
                    ProfessionSkillNode node = ProfessionSkillNode.fromConfig(key, nodesSection.getConfigurationSection(key));
                    if (node != null) {
                        nodesByKey.put(node.getKey(), node);
                    }
                }
            }
            professionSkillNodes.put(profession, nodesByKey);
        }
    }

    private void reloadProfessionSkillPointBonuses() {
        professionSkillPointBonuses.clear();

        ConfigurationSection section = dataConfig.getConfigurationSection("profession-skill-points");
        if (section == null) {
            return;
        }

        for (String playerKey : section.getKeys(false)) {
            UUID playerId;
            try {
                playerId = UUID.fromString(playerKey);
            } catch (IllegalArgumentException exception) {
                continue;
            }

            ConfigurationSection playerSection = section.getConfigurationSection(playerKey);
            if (playerSection == null) {
                continue;
            }

            Map<Profession, Integer> bonusesByProfession = new EnumMap<>(Profession.class);
            for (String professionKey : playerSection.getKeys(false)) {
                Profession profession = Profession.fromKey(professionKey);
                if (profession == null) {
                    continue;
                }
                int amount = Math.max(0, playerSection.getInt(professionKey, 0));
                if (amount > 0) {
                    bonusesByProfession.put(profession, amount);
                }
            }

            if (!bonusesByProfession.isEmpty()) {
                professionSkillPointBonuses.put(playerId, bonusesByProfession);
            }
        }
    }

    private void reloadProfessionSkillProgress() {
        unlockedProfessionSkillNodes.clear();

        ConfigurationSection section = dataConfig.getConfigurationSection("profession-skills");
        if (section == null) {
            return;
        }

        for (String playerKey : section.getKeys(false)) {
            UUID playerId;
            try {
                playerId = UUID.fromString(playerKey);
            } catch (IllegalArgumentException exception) {
                continue;
            }

            ConfigurationSection playerSection = section.getConfigurationSection(playerKey);
            if (playerSection == null) {
                continue;
            }

            Map<Profession, Set<String>> unlockedByProfession = new EnumMap<>(Profession.class);
            for (String professionKey : playerSection.getKeys(false)) {
                Profession profession = Profession.fromKey(professionKey);
                if (profession == null) {
                    continue;
                }
                Set<String> unlocked = new LinkedHashSet<>();
                for (String nodeKey : playerSection.getStringList(professionKey)) {
                    if (nodeKey != null && !nodeKey.isBlank()) {
                        unlocked.add(nodeKey.trim().toLowerCase(Locale.ROOT));
                    }
                }
                if (!unlocked.isEmpty()) {
                    unlockedByProfession.put(profession, unlocked);
                }
            }

            if (!unlockedByProfession.isEmpty()) {
                unlockedProfessionSkillNodes.put(playerId, unlockedByProfession);
            }
        }
    }

    private void reloadPendingStarterKitGrants() {
        pendingStarterKitGrants.clear();

        ConfigurationSection section = dataConfig.getConfigurationSection("starter-kits.pending");
        if (section == null) {
            return;
        }

        for (String playerKey : section.getKeys(false)) {
            UUID playerId;
            try {
                playerId = UUID.fromString(playerKey);
            } catch (IllegalArgumentException exception) {
                continue;
            }

            Set<Profession> queued = new LinkedHashSet<>();
            for (String professionKey : section.getStringList(playerKey)) {
                Profession profession = Profession.fromKey(professionKey);
                if (profession != null) {
                    queued.add(profession);
                }
            }

            if (!queued.isEmpty()) {
                pendingStarterKitGrants.put(playerId, queued);
            }
        }
    }

    private void reloadCountryBorderParticlePreferences() {
        countryBorderParticlesDisabledPlayers.clear();
        ConfigurationSection section = dataConfig.getConfigurationSection("country-border-particles.disabled");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            if (!section.getBoolean(key, false)) {
                continue;
            }
            try {
                countryBorderParticlesDisabledPlayers.add(UUID.fromString(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void reloadStabilityMeterPreferences() {
        stabilityMeterChatEnabledPlayers.clear();
        ConfigurationSection section = dataConfig.getConfigurationSection("stability-meter.chat-visible");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            if (!section.getBoolean(key, false)) {
                continue;
            }
            try {
                stabilityMeterChatEnabledPlayers.add(UUID.fromString(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
    private void reloadTutorialQuestDefinitions() {
        tutorialQuestDefinitions.clear();
        generalQuestDefinitions.clear();

        if (questsSettingsConfig == null) {
            return;
        }

        loadQuestDefinitionsInto(tutorialQuestDefinitions, questsSettingsConfig.getConfigurationSection("quests.starter.list"), "starter");
        loadQuestDefinitionsInto(generalQuestDefinitions, questsSettingsConfig.getConfigurationSection("quests.general.list"), "general");
    }

    private void loadQuestDefinitionsInto(List<PlayerQuestDefinition> target, ConfigurationSection section, String sectionName) {
        if (target == null || section == null) {
            return;
        }

        for (String questId : section.getKeys(false)) {
            ConfigurationSection questSection = section.getConfigurationSection(questId);
            if (questSection == null) {
                continue;
            }

            PlayerQuestType type = PlayerQuestType.fromKey(questSection.getString("type"));
            if (type == null) {
                getLogger().warning("Skipping " + sectionName + " quest '" + questId + "' because its type is invalid.");
                continue;
            }

            Set<String> requiresCompleted = new LinkedHashSet<>();
            for (String dependency : questSection.getStringList("requires-completed")) {
                if (dependency != null && !dependency.isBlank()) {
                    requiresCompleted.add(dependency.trim().toLowerCase(Locale.ROOT));
                }
            }

            target.add(new PlayerQuestDefinition(
                    questId.toLowerCase(Locale.ROOT),
                    questSection.getInt("order", 0),
                    questSection.getBoolean("enabled", true),
                    questSection.getString("title", "&6Quest"),
                    questSection.getString("objective", "Complete this quest"),
                    questSection.getString("hint", ""),
                    Math.max(1, questSection.getInt("target", 1)),
                    Profession.fromKey(questSection.getString("profession")),
                    type,
                    requiresCompleted
            ));
        }

        target.sort(Comparator
                .comparingInt(PlayerQuestDefinition::getOrder)
                .thenComparing(PlayerQuestDefinition::getId));
    }

    private void reloadTutorialStages() {
        tutorialStages.clear();
        tutorialIntroIndices.clear();
        tutorialStarterXpProgress.clear();
        completedTutorialQuestIds.clear();
        tutorialQuestProgress.clear();

        ConfigurationSection section = dataConfig.getConfigurationSection("tutorial");
        if (section == null) {
            return;
        }

        for (String playerKey : section.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(playerKey);
                String stageName = section.getString(playerKey + ".stage");
                if (stageName != null && !stageName.isBlank()) {
                    tutorialStages.put(playerId, TutorialStage.valueOf(stageName.toUpperCase(Locale.ROOT)));
                }
                int introIndex = section.getInt(playerKey + ".intro-index", -1);
                if (introIndex >= 0) {
                    tutorialIntroIndices.put(playerId, introIndex);
                }
                int starterProgress = Math.max(0, section.getInt(playerKey + ".starter-xp-progress", 0));
                if (starterProgress > 0) {
                    tutorialStarterXpProgress.put(playerId, starterProgress);
                }

                Set<String> completed = new LinkedHashSet<>();
                for (String questId : section.getStringList(playerKey + ".completed-quests")) {
                    if (questId != null && !questId.isBlank()) {
                        completed.add(questId.trim().toLowerCase(Locale.ROOT));
                    }
                }
                if (!completed.isEmpty()) {
                    completedTutorialQuestIds.put(playerId, completed);
                }

                ConfigurationSection progressSection = section.getConfigurationSection(playerKey + ".quest-progress");
                if (progressSection != null) {
                    Map<String, Integer> progressByQuest = new LinkedHashMap<>();
                    for (String questId : progressSection.getKeys(false)) {
                        int progress = Math.max(0, progressSection.getInt(questId, 0));
                        if (progress > 0) {
                            progressByQuest.put(questId.toLowerCase(Locale.ROOT), progress);
                        }
                    }
                    if (!progressByQuest.isEmpty()) {
                        tutorialQuestProgress.put(playerId, progressByQuest);
                    }
                }

                migrateLegacyTutorialQuestState(playerId, stageName, starterProgress);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void migrateLegacyTutorialQuestState(UUID playerId, String stageName, int starterProgress) {
        if (playerId == null || stageName == null || stageName.isBlank()) {
            return;
        }
        if (completedTutorialQuestIds.containsKey(playerId) || tutorialQuestProgress.containsKey(playerId)) {
            return;
        }

        TutorialStage stage;
        try {
            stage = TutorialStage.valueOf(stageName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return;
        }

        Set<String> completed = completedTutorialQuestIds.computeIfAbsent(playerId, ignored -> new LinkedHashSet<>());
        Profession primary = getPrimaryProfession(playerId);
        String momentumQuestId = getDefaultMomentumQuestId(primary);

        if (primary != null) {
            completed.add("choose_profession");
        }
        if (stage == TutorialStage.BUILD_MOMENTUM || stage == TutorialStage.JOIN_COUNTRY || stage == TutorialStage.CONTRIBUTE_TO_COUNTRY) {
            completed.add("first_profession_xp");
        }
        if ((stage == TutorialStage.BUILD_MOMENTUM || stage == TutorialStage.JOIN_COUNTRY || stage == TutorialStage.CONTRIBUTE_TO_COUNTRY)
                && starterProgress > 0
                && momentumQuestId != null) {
            setTutorialQuestProgress(playerId, momentumQuestId, starterProgress, false);
        }
        if ((stage == TutorialStage.JOIN_COUNTRY || stage == TutorialStage.CONTRIBUTE_TO_COUNTRY) && momentumQuestId != null) {
            completed.add(momentumQuestId);
        }
        if (stage == TutorialStage.CONTRIBUTE_TO_COUNTRY) {
            completed.add("join_country");
        }
        if (completed.isEmpty()) {
            completedTutorialQuestIds.remove(playerId);
        }
    }

    private void saveTutorialStage(UUID playerId) {
        if (playerId == null) {
            return;
        }

        TutorialStage stage = tutorialStages.get(playerId);
        if (stage == null) {
            dataConfig.set("tutorial." + playerId + ".stage", null);
        } else {
            dataConfig.set("tutorial." + playerId + ".stage", stage.name().toLowerCase(Locale.ROOT));
        }
        Integer introIndex = tutorialIntroIndices.get(playerId);
        if (introIndex == null) {
            dataConfig.set("tutorial." + playerId + ".intro-index", null);
        } else {
            dataConfig.set("tutorial." + playerId + ".intro-index", introIndex);
        }
        int starterProgress = Math.max(0, tutorialStarterXpProgress.getOrDefault(playerId, 0));
        if (starterProgress <= 0) {
            dataConfig.set("tutorial." + playerId + ".starter-xp-progress", null);
        } else {
            dataConfig.set("tutorial." + playerId + ".starter-xp-progress", starterProgress);
        }

        Set<String> completed = completedTutorialQuestIds.get(playerId);
        if (completed == null || completed.isEmpty()) {
            dataConfig.set("tutorial." + playerId + ".completed-quests", null);
        } else {
            dataConfig.set("tutorial." + playerId + ".completed-quests", new ArrayList<>(completed));
        }

        Map<String, Integer> progressByQuest = tutorialQuestProgress.get(playerId);
        dataConfig.set("tutorial." + playerId + ".quest-progress", null);
        if (progressByQuest != null && !progressByQuest.isEmpty()) {
            for (Map.Entry<String, Integer> entry : progressByQuest.entrySet()) {
                if (entry.getValue() != null && entry.getValue() > 0) {
                    dataConfig.set("tutorial." + playerId + ".quest-progress." + entry.getKey(), entry.getValue());
                }
            }
        }

        if (tutorialStages.get(playerId) == null
                && introIndex == null
                && starterProgress <= 0
                && (completed == null || completed.isEmpty())
                && (progressByQuest == null || progressByQuest.isEmpty())) {
            dataConfig.set("tutorial." + playerId, null);
        }
        saveDataConfig();
    }

    private void reloadAssignedQuestStates() {
        assignedQuestIds.clear();
        completedAssignedQuestIds.clear();
        assignedQuestProgress.clear();

        ConfigurationSection section = dataConfig.getConfigurationSection("quests");
        if (section == null) {
            return;
        }

        for (String playerKey : section.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(playerKey);

                List<String> assigned = new ArrayList<>();
                for (String questId : section.getStringList(playerKey + ".assigned")) {
                    if (questId != null && !questId.isBlank()) {
                        assigned.add(questId.trim().toLowerCase(Locale.ROOT));
                    }
                }
                if (!assigned.isEmpty()) {
                    assignedQuestIds.put(playerId, assigned);
                }

                Set<String> completed = new LinkedHashSet<>();
                for (String questId : section.getStringList(playerKey + ".completed")) {
                    if (questId != null && !questId.isBlank()) {
                        completed.add(questId.trim().toLowerCase(Locale.ROOT));
                    }
                }
                if (!completed.isEmpty()) {
                    completedAssignedQuestIds.put(playerId, completed);
                }

                ConfigurationSection progressSection = section.getConfigurationSection(playerKey + ".progress");
                if (progressSection != null) {
                    Map<String, Integer> progressByQuest = new LinkedHashMap<>();
                    for (String questId : progressSection.getKeys(false)) {
                        int progress = Math.max(0, progressSection.getInt(questId, 0));
                        if (progress > 0) {
                            progressByQuest.put(questId.toLowerCase(Locale.ROOT), progress);
                        }
                    }
                    if (!progressByQuest.isEmpty()) {
                        assignedQuestProgress.put(playerId, progressByQuest);
                    }
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void saveAssignedQuestState(UUID playerId) {
        if (playerId == null) {
            return;
        }

        List<String> assigned = assignedQuestIds.get(playerId);
        if (assigned == null || assigned.isEmpty()) {
            dataConfig.set("quests." + playerId + ".assigned", null);
        } else {
            dataConfig.set("quests." + playerId + ".assigned", new ArrayList<>(assigned));
        }

        Set<String> completed = completedAssignedQuestIds.get(playerId);
        if (completed == null || completed.isEmpty()) {
            dataConfig.set("quests." + playerId + ".completed", null);
        } else {
            dataConfig.set("quests." + playerId + ".completed", new ArrayList<>(completed));
        }

        Map<String, Integer> progressByQuest = assignedQuestProgress.get(playerId);
        dataConfig.set("quests." + playerId + ".progress", null);
        if (progressByQuest != null && !progressByQuest.isEmpty()) {
            for (Map.Entry<String, Integer> entry : progressByQuest.entrySet()) {
                if (entry.getValue() != null && entry.getValue() > 0) {
                    dataConfig.set("quests." + playerId + ".progress." + entry.getKey(), entry.getValue());
                }
            }
        }

        if ((assigned == null || assigned.isEmpty())
                && (completed == null || completed.isEmpty())
                && (progressByQuest == null || progressByQuest.isEmpty())) {
            dataConfig.set("quests." + playerId, null);
        }

        saveDataConfig();
    }

    private void restartTerraTipsRuntime() {
        stopTerraTipsRuntime();
        if (!getConfig().getBoolean("terra-tips.enabled", true)) {
            return;
        }
        long intervalMinutes = Math.max(1L, getConfig().getLong("terra-tips.interval-minutes", 8L));
        terraTipsTask = getServer().getScheduler().runTaskTimer(
                this,
                this::broadcastNextTerraTip,
                intervalMinutes * 60L * 20L,
                intervalMinutes * 60L * 20L
        );
    }

    private void stopTerraTipsRuntime() {
        if (terraTipsTask != null) {
            terraTipsTask.cancel();
            terraTipsTask = null;
        }
    }

    private void broadcastNextTerraTip() {
        if (getServer().getOnlinePlayers().isEmpty()) {
            return;
        }
        List<String> tips = getMessageList("tips.lines");
        if (tips.isEmpty()) {
            return;
        }
        String tip = tips.get(Math.floorMod(nextTerraTipIndex, tips.size()));
        nextTerraTipIndex++;
        String border = getMessage("tips.border");
        String title = getMessage("tips.title");
        String prefix = getMessage("tips.prefix");
        for (Player player : getServer().getOnlinePlayers()) {
            player.sendMessage(border);
            player.sendMessage(title);
            player.sendMessage(prefix + tip);
            player.sendMessage(getMessage("tips.footer"));
            player.sendMessage(border);
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.45F, 1.15F);
        }
    }

    public void handleTutorialJoin(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        if (requiresProfessionSelection(player)) {
            if (!tutorialIntroIndices.containsKey(playerId)) {
                startTutorialIntro(player, 0, true);
            } else {
                resumeTutorialIntro(player);
            }
            updateTutorialChecklist(player);
            syncTutorialQuestHud(player, true);
            return;
        }
        refreshAssignedQuestFlow(player, false);
        refreshTutorialQuestFlow(player, false);
        updateTutorialChecklist(player);
        syncTutorialQuestHud(player, true);
    }

    public void startTutorial(UUID playerId, boolean sendIntro) {
        if (playerId == null) {
            return;
        }

        tutorialStages.remove(playerId);
        tutorialStarterXpProgress.remove(playerId);
        completedTutorialQuestIds.remove(playerId);
        tutorialQuestProgress.remove(playerId);
        saveTutorialStage(playerId);
        Player player = getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            refreshAssignedQuestFlow(player, sendIntro);
            refreshTutorialQuestFlow(player, sendIntro);
            updateTutorialChecklist(player);
        }
    }

    public void clearTutorial(UUID playerId) {
        if (playerId == null) {
            return;
        }
        tutorialStages.remove(playerId);
        tutorialStarterXpProgress.remove(playerId);
        completedTutorialQuestIds.remove(playerId);
        tutorialQuestProgress.remove(playerId);
        tutorialQuestHudIdCache.remove(playerId);
        tutorialQuestHudPercentCache.remove(playerId);
        tutorialQuestHudStepCache.remove(playerId);
        saveTutorialStage(playerId);
        Player player = getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            clearTutorialChecklist(player);
            syncTutorialQuestHud(player, true);
        }
    }

    private void sendTutorialIntro(Player player) {
        player.sendMessage(getMessage("tutorial.welcome-header"));
        for (String line : getMessageList("tutorial.welcome-lines")) {
            player.sendMessage(line);
        }
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.7F, 1.2F);
    }

    public boolean isTutorialIntroActive(UUID playerId) {
        return playerId != null && tutorialIntroIndices.containsKey(playerId);
    }

    private void startTutorialIntro(Player player, int introIndex, boolean resetPrompt) {
        if (player == null) {
            return;
        }
        UUID playerId = player.getUniqueId();
        tutorialStages.remove(playerId);
        tutorialIntroIndices.put(playerId, Math.max(0, introIndex));
        applyTutorialIntroLock(player);
        saveTutorialStage(playerId);
        if (resetPrompt) {
            showTutorialIntroPrompt(player);
        }
    }

    private void resumeTutorialIntro(Player player) {
        if (player == null) {
            return;
        }
        applyTutorialIntroLock(player);
        showTutorialIntroPrompt(player);
    }

    public void advanceTutorialIntro(Player player) {
        if (player == null) {
            return;
        }
        UUID playerId = player.getUniqueId();
        Integer currentIndex = tutorialIntroIndices.get(playerId);
        if (currentIndex == null) {
            return;
        }
        int nextIndex = currentIndex + 1;
        List<String> prompts = getMessageList("tutorial.intro.prompts");
        if (nextIndex >= prompts.size()) {
            finishTutorialIntro(player);
            return;
        }
        tutorialIntroIndices.put(playerId, nextIndex);
        saveTutorialStage(playerId);
        showTutorialIntroPrompt(player);
    }

    private void showTutorialIntroPrompt(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        UUID playerId = player.getUniqueId();
        Integer index = tutorialIntroIndices.get(playerId);
        List<String> prompts = getMessageList("tutorial.intro.prompts");
        if (index == null || index < 0 || index >= prompts.size()) {
            finishTutorialIntro(player);
            return;
        }

        applyTutorialIntroLock(player);
        String prompt = prompts.get(index);
        int promptNumber = index + 1;
        int total = prompts.size();
        String prefix = getMessage("tutorial.intro.prefix");
        player.sendMessage(getMessage("tutorial.intro.border"));
        player.sendMessage(prefix + getMessage("tutorial.intro.title", placeholders(
                "current", String.valueOf(promptNumber),
                "total", String.valueOf(total)
        )));
        player.sendMessage(prefix + prompt);
        player.sendMessage(prefix + getMessage("tutorial.intro.hint"));
        player.sendMessage(createTutorialContinueComponent());
        player.sendMessage(getMessage("tutorial.intro.border"));
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.55F, 1.05F);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.45F, 1.55F);
        scheduleTutorialIntroAdvance(playerId);
    }

    private Component createTutorialContinueComponent() {
        return legacyComponent(getMessage("tutorial.intro.button"))
                .clickEvent(ClickEvent.runCommand("/terra tutorial next"))
                .hoverEvent(HoverEvent.showText(legacyComponent(getMessage("tutorial.intro.button-hover"))));
    }

    private void scheduleTutorialIntroAdvance(UUID playerId) {
        BukkitTask existing = tutorialIntroAdvanceTasks.remove(playerId);
        if (existing != null) {
            existing.cancel();
        }
        BukkitTask task = getServer().getScheduler().runTaskLater(this, () -> {
            tutorialIntroAdvanceTasks.remove(playerId);
            Player player = getServer().getPlayer(playerId);
            if (player != null && player.isOnline() && isTutorialIntroActive(playerId)) {
                advanceTutorialIntro(player);
            }
        }, 30L * 20L);
        tutorialIntroAdvanceTasks.put(playerId, task);
    }

    private void finishTutorialIntro(Player player) {
        if (player == null) {
            return;
        }
        UUID playerId = player.getUniqueId();
        BukkitTask existing = tutorialIntroAdvanceTasks.remove(playerId);
        if (existing != null) {
            existing.cancel();
        }
        tutorialIntroIndices.remove(playerId);
        clearTutorialIntroLock(player);
        refreshTutorialQuestFlow(player, false);
        updateTutorialChecklist(player);
        saveTutorialStage(playerId);
        player.sendMessage(getMessage("tutorial.intro.finished"));
        player.playSound(player.getLocation(), Sound.UI_TOAST_IN, 0.75F, 1.0F);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.55F, 1.45F);
        getServer().getScheduler().runTaskLater(this, () -> {
            if (player.isOnline() && requiresProfessionSelection(player)) {
                openProfessionMenu(player);
            }
        }, 2L);
    }

    private void applyTutorialIntroLock(Player player) {
        player.closeInventory();
        ScoreboardManager manager = getServer().getScoreboardManager();
        if (manager != null && !tutorialStoredScoreboards.containsKey(player.getUniqueId())) {
            tutorialStoredScoreboards.put(player.getUniqueId(), player.getScoreboard());
            player.setScoreboard(manager.getNewScoreboard());
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 20 * 35, 0, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 35, 0, false, false, false));
    }

    private void clearTutorialIntroLock(Player player) {
        ScoreboardManager manager = getServer().getScoreboardManager();
        Scoreboard previous = tutorialStoredScoreboards.remove(player.getUniqueId());
        if (previous != null) {
            player.setScoreboard(previous);
        } else if (manager != null) {
            player.setScoreboard(manager.getMainScoreboard());
        }
        player.removePotionEffect(PotionEffectType.DARKNESS);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
    }

    private void completeTutorial(Player player) {
        if (player == null) {
            return;
        }

        UUID playerId = player.getUniqueId();
        tutorialStages.remove(playerId);
        tutorialStarterXpProgress.remove(playerId);
        Set<String> completed = getCompletedTutorialQuestIds(playerId);
        for (PlayerQuestDefinition quest : tutorialQuestDefinitions) {
            if (quest.isEnabled()) {
                completed.add(quest.getId());
            }
        }
        saveTutorialStage(playerId);
        clearTutorialChecklist(player);
        syncTutorialQuestHud(player, true);
        player.sendMessage(getMessage("tutorial.card-border"));
        player.sendMessage(getMessage("tutorial.complete-header"));
        for (String line : getMessageList("tutorial.complete-lines")) {
            player.sendMessage(line);
        }
        player.sendMessage(getMessage("tutorial.card-border"));
        playTutorialCompleteEffect(player);
    }

    public void handleTutorialPrimaryProfessionSelected(Player player) {
        if (player != null) {
            UUID playerId = player.getUniqueId();
            tutorialStages.remove(playerId);
            tutorialStarterXpProgress.remove(playerId);
            saveTutorialStage(playerId);
            updateTutorialChecklist(player);
            refreshAssignedQuestFlow(player, true);
            refreshTutorialQuestFlow(player, true);
        }
    }

    public void handleTutorialProfessionXpGain(Player player, Profession profession, int amount) {
        if (player == null || profession == null || amount <= 0) {
            return;
        }
        UUID playerId = player.getUniqueId();
        Profession primary = getPrimaryProfession(playerId);
        if (primary == null || primary != profession) {
            return;
        }

        tutorialStarterXpProgress.put(playerId, Math.max(
                tutorialStarterXpProgress.getOrDefault(playerId, 0),
                getProfessionTotalProgressXp(playerId, profession)
        ));
        saveTutorialStage(playerId);
        refreshAssignedQuestFlow(player, true);
        refreshTutorialQuestFlow(player, true);
    }

    public void handleTutorialCountryJoined(Player player) {
        if (player != null) {
            refreshAssignedQuestFlow(player, true);
            refreshTutorialQuestFlow(player, true);
        }
    }

    public void handleTutorialCountryContribution(Player player) {
        if (player != null) {
            PlayerQuestDefinition assignedQuest = getActiveAssignedQuest(player.getUniqueId());
            if (assignedQuest != null && assignedQuest.getType() == PlayerQuestType.CONTRIBUTE_COUNTRY) {
                addAssignedQuestProgress(player.getUniqueId(), assignedQuest.getId(), 1, false);
            }
            PlayerQuestDefinition activeQuest = getActiveTutorialQuest(player.getUniqueId());
            if (activeQuest != null && activeQuest.getType() == PlayerQuestType.CONTRIBUTE_COUNTRY) {
                addTutorialQuestProgress(player.getUniqueId(), activeQuest.getId(), 1, false);
            }
            refreshAssignedQuestFlow(player, true);
            refreshTutorialQuestFlow(player, true);
        }
    }

    private boolean shouldShowTutorialChecklist(Player player) {
        if (player == null || !player.isOnline() || isTutorialIntroActive(player.getUniqueId())) {
            return false;
        }
        if (!isTutorialSidebarEnabled()) {
            return false;
        }
        return requiresProfessionSelection(player) || getActiveDisplayedQuest(player.getUniqueId()) != null;
    }

    private void updateTutorialChecklist(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        if (!shouldShowTutorialChecklist(player)) {
            clearTutorialChecklist(player);
            return;
        }

        ScoreboardManager manager = getServer().getScoreboardManager();
        if (manager == null) {
            return;
        }

        tutorialChecklistStoredScoreboards.computeIfAbsent(player.getUniqueId(), ignored -> player.getScoreboard());
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("terraguide", "dummy", colorize(getTutorialSidebarTitle()));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = getTutorialChecklistLines(player);
        int score = lines.size();
        int uniqueIndex = 0;
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                line = " ";
            }
            String entry = makeUniqueSidebarLine(line, uniqueIndex++);
            objective.getScore(entry).setScore(score--);
        }
        player.setScoreboard(scoreboard);
    }

    private List<String> getTutorialChecklistLines(Player player) {
        List<String> lines = new ArrayList<>();
        PlayerQuestDefinition activeQuest = getActiveDisplayedQuest(player.getUniqueId());
        if (activeQuest == null) {
            lines.add(colorize("&7No active quest"));
            return lines;
        }

        String progress = getTutorialQuestProgressText(player.getUniqueId());
        lines.add(colorize("&f "));
        lines.add(colorize(resolveQuestText(activeQuest.getTitle(), player.getUniqueId(), activeQuest, activeQuest == getActiveAssignedQuest(player.getUniqueId()))));
        lines.add(colorize("&7" + resolveQuestText(activeQuest.getObjective(), player.getUniqueId(), activeQuest, activeQuest == getActiveAssignedQuest(player.getUniqueId()))));
        if (!progress.isBlank()) {
            lines.add(colorize("&e" + progress));
        }
        String hint = getTutorialQuestHint(player.getUniqueId());
        if (!hint.isBlank()) {
            lines.add(colorize("&8 "));
            lines.add(colorize("&e" + hint));
        }
        return lines;
    }

    private String formatChecklistLine(boolean done, boolean active, String label) {
        if (done) {
            return "&a✔ " + label;
        }
        if (active) {
            return "&e➜ " + label;
        }
        return "&7• " + label;
    }

    private String getTutorialChecklistHint(Player player) {
        if (player == null) {
            return "";
        }
        if (requiresProfessionSelection(player)) {
            return "Use /jobs";
        }
        TutorialStage stage = tutorialStages.get(player.getUniqueId());
        if (stage == TutorialStage.EARN_FIRST_XP) {
            Profession profession = getProfession(player.getUniqueId());
            return profession != null ? profession.getDisplayName() + " XP" : "Do your job";
        }
        if (stage == TutorialStage.BUILD_MOMENTUM) {
            return getTutorialMomentumHint(player.getUniqueId());
        }
        if (stage == TutorialStage.JOIN_COUNTRY) {
            return "Use /country list";
        }
        if (stage == TutorialStage.CONTRIBUTE_TO_COUNTRY) {
            return "Deposit money or resources";
        }
        return "";
    }

    private int getTutorialStarterXpTarget(UUID playerId) {
        Profession profession = getPrimaryProfession(playerId);
        if (profession == null) {
            return 100;
        }
        return switch (profession) {
            case MINER, LUMBERJACK, FARMER -> 120;
            case BUILDER, BLACKSMITH -> 90;
            case TRADER -> 100;
            case SOLDIER -> 110;
        };
    }

    private String getTutorialMomentumLabel(UUID playerId, int progress, int target) {
        Profession profession = getPrimaryProfession(playerId);
        String name = profession != null ? profession.getDisplayName() : "Starter";
        return "Build momentum as " + name + " " + Math.min(progress, target) + "/" + target + " XP";
    }

    private String getTutorialMomentumHint(UUID playerId) {
        Profession profession = getPrimaryProfession(playerId);
        if (profession == null) {
            return "Earn profession XP";
        }
        return switch (profession) {
            case MINER -> "Mine and gather ore";
            case LUMBERJACK -> "Chop wood and replant";
            case FARMER -> "Farm, till, plant, or craft food";
            case BUILDER -> "Place builder blocks";
            case BLACKSMITH -> "Smelt or forge materials";
            case TRADER -> "Complete trader deals and economy loops";
            case SOLDIER -> "Train through late-game combat systems";
        };
    }

    private String makeUniqueSidebarLine(String line, int index) {
        ChatColor[] colors = ChatColor.values();
        return line + colors[index % colors.length];
    }

    private void clearTutorialChecklist(Player player) {
        if (player == null) {
            return;
        }
        ScoreboardManager manager = getServer().getScoreboardManager();
        Scoreboard previous = tutorialChecklistStoredScoreboards.remove(player.getUniqueId());
        if (previous != null) {
            player.setScoreboard(previous);
        } else if (manager != null && !isTutorialIntroActive(player.getUniqueId())) {
            player.setScoreboard(manager.getMainScoreboard());
        }
    }

    private List<String> getTutorialPrimarySelectedLines(UUID playerId) {
        Profession profession = getPrimaryProfession(playerId);
        if (profession == null) {
            return getMessageList("tutorial.primary-selected-lines");
        }
        String basePath = "tutorial.primary-selected-" + profession.getKey() + "-lines";
        if (hasMessage(basePath)) {
            return getMessageList(basePath);
        }
        return getMessageList("tutorial.primary-selected-lines");
    }

    private void playTutorialStageAdvanceEffect(Player player, boolean primaryJobStep) {
        if (player == null) {
            return;
        }
        if (primaryJobStep) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_IN, 0.85F, 1.0F);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.7F, 1.3F);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.45F, 1.55F);
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0.0D, 1.0D, 0.0D), 10, 0.35D, 0.45D, 0.35D, 0.02D);
            return;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.95F, 1.15F);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6F, 1.15F);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.45F, 1.45F);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0.0D, 1.0D, 0.0D), 10, 0.35D, 0.45D, 0.35D, 0.01D);
    }

    private void playTutorialCompleteEffect(Player player) {
        if (player == null) {
            return;
        }
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.85F, 1.0F);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.55F, 1.15F);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5F, 1.35F);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0.0D, 1.0D, 0.0D), 14, 0.4D, 0.55D, 0.4D, 0.02D);
    }

    private boolean isTutorialSidebarEnabled() {
        return questsSettingsConfig != null && questsSettingsConfig.getBoolean("quests.sidebar.enabled", false);
    }

    private String getTutorialSidebarTitle() {
        if (questsSettingsConfig == null) {
            return "&6Terra Guide";
        }
        return questsSettingsConfig.getString("quests.sidebar.title", "&6Terra Guide");
    }

    private String getDefaultMomentumQuestId(Profession profession) {
        return profession != null ? profession.getKey() + "_momentum" : null;
    }

    private void setTutorialQuestProgress(UUID playerId, String questId, int progress, boolean save) {
        if (playerId == null || questId == null || questId.isBlank()) {
            return;
        }
        String normalizedQuestId = questId.toLowerCase(Locale.ROOT);
        if (progress <= 0) {
            Map<String, Integer> progressByQuest = tutorialQuestProgress.get(playerId);
            if (progressByQuest != null) {
                progressByQuest.remove(normalizedQuestId);
                if (progressByQuest.isEmpty()) {
                    tutorialQuestProgress.remove(playerId);
                }
            }
        } else {
            tutorialQuestProgress.computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>()).put(normalizedQuestId, progress);
        }
        if (save) {
            saveTutorialStage(playerId);
        }
    }

    private void addTutorialQuestProgress(UUID playerId, String questId, int amount, boolean save) {
        if (playerId == null || questId == null || questId.isBlank() || amount <= 0) {
            return;
        }
        setTutorialQuestProgress(playerId, questId, getStoredTutorialQuestProgress(playerId, questId) + amount, save);
    }

    private int getStoredTutorialQuestProgress(UUID playerId, String questId) {
        if (playerId == null || questId == null || questId.isBlank()) {
            return 0;
        }
        Map<String, Integer> progressByQuest = tutorialQuestProgress.get(playerId);
        if (progressByQuest == null) {
            return 0;
        }
        return Math.max(0, progressByQuest.getOrDefault(questId.toLowerCase(Locale.ROOT), 0));
    }

    private Set<String> getCompletedTutorialQuestIds(UUID playerId) {
        return completedTutorialQuestIds.computeIfAbsent(playerId, ignored -> new LinkedHashSet<>());
    }

    private boolean isTutorialQuestCompleted(UUID playerId, String questId) {
        if (playerId == null || questId == null || questId.isBlank()) {
            return false;
        }
        Set<String> completed = completedTutorialQuestIds.get(playerId);
        return completed != null && completed.contains(questId.toLowerCase(Locale.ROOT));
    }

    private Profession resolveTutorialQuestProfession(UUID playerId, PlayerQuestDefinition quest) {
        if (quest == null) {
            return null;
        }
        if (quest.getProfession() != null) {
            return hasProfession(playerId, quest.getProfession()) ? quest.getProfession() : null;
        }
        return getPrimaryProfession(playerId);
    }

    private boolean isTutorialQuestEligible(UUID playerId, PlayerQuestDefinition quest) {
        if (playerId == null || quest == null || !quest.isEnabled()) {
            return false;
        }
        for (String dependency : quest.getRequiresCompleted()) {
            if (!isTutorialQuestCompleted(playerId, dependency)) {
                return false;
            }
        }
        return switch (quest.getType()) {
            case SELECT_PROFESSION -> quest.getProfession() == null || hasProfession(playerId, quest.getProfession()) || !hasProfession(playerId);
            case EARN_PROFESSION_XP, REACH_PROFESSION_LEVEL -> resolveTutorialQuestProfession(playerId, quest) != null;
            case JOIN_COUNTRY, CONTRIBUTE_COUNTRY -> true;
        };
    }

    private int getProfessionTotalProgressXp(UUID playerId, Profession profession) {
        if (playerId == null || profession == null || !hasProfession(playerId, profession)) {
            return 0;
        }
        int level = Math.max(1, getProfessionLevel(playerId, profession));
        int total = 0;
        for (int currentLevel = 1; currentLevel < level; currentLevel++) {
            total += Math.max(0, getProfessionXpRequiredForLevel(profession, currentLevel));
        }
        return total + Math.max(0, getProfessionXp(playerId, profession));
    }

    private int getTutorialQuestCurrentProgress(UUID playerId, PlayerQuestDefinition quest) {
        return getQuestCurrentProgress(playerId, quest, false);
    }

    private boolean isTutorialQuestSatisfied(UUID playerId, PlayerQuestDefinition quest) {
        return getTutorialQuestCurrentProgress(playerId, quest) >= Math.max(1, quest.getTarget());
    }

    private PlayerQuestDefinition getActiveTutorialQuest(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        for (PlayerQuestDefinition quest : tutorialQuestDefinitions) {
            if (!quest.isEnabled() || isTutorialQuestCompleted(playerId, quest.getId())) {
                continue;
            }
            if (isTutorialQuestEligible(playerId, quest)) {
                return quest;
            }
        }
        return null;
    }

    private String getActiveTutorialQuestId(UUID playerId) {
        PlayerQuestDefinition activeQuest = getActiveTutorialQuest(playerId);
        return activeQuest != null ? activeQuest.getId() : null;
    }

    public List<PlayerQuestDefinition> getGeneralQuestDefinitions() {
        return Collections.unmodifiableList(generalQuestDefinitions);
    }

    public PlayerQuestDefinition getGeneralQuestDefinition(String questId) {
        if (questId == null || questId.isBlank()) {
            return null;
        }
        String normalizedQuestId = questId.toLowerCase(Locale.ROOT);
        for (PlayerQuestDefinition definition : generalQuestDefinitions) {
            if (definition.getId().equalsIgnoreCase(normalizedQuestId)) {
                return definition;
            }
        }
        return null;
    }

    public String createGeneralQuestDefinition() {
        if (questsSettingsConfig == null || questsSettingsFile == null) {
            return null;
        }

        ConfigurationSection section = questsSettingsConfig.getConfigurationSection("quests.general.list");
        if (section == null) {
            section = questsSettingsConfig.createSection("quests.general.list");
        }

        String baseId = "quest";
        int index = 1;
        while (section.contains(baseId + "_" + index)) {
            index++;
        }

        String questId = baseId + "_" + index;
        int order = generalQuestDefinitions.stream()
                .mapToInt(PlayerQuestDefinition::getOrder)
                .max()
                .orElse(0) + 10;

        section.set(questId + ".order", order);
        section.set(questId + ".enabled", true);
        section.set(questId + ".type", PlayerQuestType.EARN_PROFESSION_XP.name().toLowerCase(Locale.ROOT));
        section.set(questId + ".target", 10);
        section.set(questId + ".title", "&6New Quest");
        section.set(questId + ".objective", "Reach %target% %profession% XP");
        section.set(questId + ".hint", "Work toward the goal");
        section.set(questId + ".profession", "miner");
        section.set(questId + ".requires-completed", List.of());
        saveQuestsSettings();
        return questId;
    }

    public boolean deleteGeneralQuestDefinition(String questId) {
        if (questsSettingsConfig == null || questsSettingsFile == null || questId == null || questId.isBlank()) {
            return false;
        }
        String normalizedQuestId = questId.toLowerCase(Locale.ROOT);
        if (!questsSettingsConfig.contains("quests.general.list." + normalizedQuestId)) {
            return false;
        }
        questsSettingsConfig.set("quests.general.list." + normalizedQuestId, null);
        for (UUID playerId : new ArrayList<>(assignedQuestIds.keySet())) {
            unassignQuestFromPlayer(playerId, normalizedQuestId);
            Set<String> completed = completedAssignedQuestIds.get(playerId);
            if (completed != null) {
                completed.remove(normalizedQuestId);
                if (completed.isEmpty()) {
                    completedAssignedQuestIds.remove(playerId);
                }
            }
            Map<String, Integer> progress = assignedQuestProgress.get(playerId);
            if (progress != null) {
                progress.remove(normalizedQuestId);
                if (progress.isEmpty()) {
                    assignedQuestProgress.remove(playerId);
                }
            }
            saveAssignedQuestState(playerId);
        }
        saveQuestsSettings();
        return true;
    }

    public boolean setGeneralQuestEnabled(String questId, boolean enabled) {
        return setGeneralQuestConfigValue(questId, "enabled", enabled);
    }

    public boolean setGeneralQuestType(String questId, PlayerQuestType type) {
        return type != null && setGeneralQuestConfigValue(questId, "type", type.name().toLowerCase(Locale.ROOT));
    }

    public boolean setGeneralQuestProfession(String questId, Profession profession) {
        return setGeneralQuestConfigValue(questId, "profession", profession != null ? profession.getKey() : null);
    }

    public boolean setGeneralQuestTarget(String questId, int target) {
        return setGeneralQuestConfigValue(questId, "target", Math.max(1, target));
    }

    public boolean setGeneralQuestTitle(String questId, String title) {
        return setGeneralQuestConfigValue(questId, "title", title);
    }

    public boolean setGeneralQuestObjective(String questId, String objective) {
        return setGeneralQuestConfigValue(questId, "objective", objective);
    }

    public boolean setGeneralQuestHint(String questId, String hint) {
        return setGeneralQuestConfigValue(questId, "hint", hint);
    }

    private boolean setGeneralQuestConfigValue(String questId, String suffix, Object value) {
        if (questsSettingsConfig == null || questsSettingsFile == null || questId == null || questId.isBlank()) {
            return false;
        }
        String normalizedQuestId = questId.toLowerCase(Locale.ROOT);
        String path = "quests.general.list." + normalizedQuestId;
        if (!questsSettingsConfig.contains(path)) {
            return false;
        }
        questsSettingsConfig.set(path + "." + suffix, value);
        saveQuestsSettings();
        return true;
    }

    private void saveQuestsSettings() {
        saveYaml(questsSettingsConfig, questsSettingsFile);
        questsSettingsConfig = loadCustomConfig(questsSettingsFile, "settings/quests.yml");
        reloadTutorialQuestDefinitions();
        for (Player onlinePlayer : getServer().getOnlinePlayers()) {
            refreshAssignedQuestFlow(onlinePlayer, false);
            refreshTutorialQuestFlow(onlinePlayer, false);
            updateTutorialChecklist(onlinePlayer);
            syncTutorialQuestHud(onlinePlayer, true);
        }
    }

    public List<String> getAssignedQuestIds(UUID playerId) {
        List<String> assigned = assignedQuestIds.get(playerId);
        return assigned == null ? List.of() : List.copyOf(assigned);
    }

    public boolean isQuestAssigned(UUID playerId, String questId) {
        if (playerId == null || questId == null || questId.isBlank()) {
            return false;
        }
        List<String> assigned = assignedQuestIds.get(playerId);
        return assigned != null && assigned.stream().anyMatch(questId::equalsIgnoreCase);
    }

    public boolean assignQuestToPlayer(UUID playerId, String questId) {
        if (playerId == null) {
            return false;
        }
        PlayerQuestDefinition definition = getGeneralQuestDefinition(questId);
        if (definition == null) {
            return false;
        }

        List<String> assigned = assignedQuestIds.computeIfAbsent(playerId, ignored -> new ArrayList<>());
        if (assigned.stream().anyMatch(definition.getId()::equalsIgnoreCase)) {
            return false;
        }
        assigned.add(definition.getId());
        saveAssignedQuestState(playerId);

        Player player = getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            refreshAssignedQuestFlow(player, true);
            syncTutorialQuestHud(player, true);
        }
        return true;
    }

    public boolean unassignQuestFromPlayer(UUID playerId, String questId) {
        if (playerId == null || questId == null || questId.isBlank()) {
            return false;
        }
        List<String> assigned = assignedQuestIds.get(playerId);
        if (assigned == null) {
            return false;
        }
        boolean removed = assigned.removeIf(questId::equalsIgnoreCase);
        if (!removed) {
            return false;
        }
        if (assigned.isEmpty()) {
            assignedQuestIds.remove(playerId);
        }
        Map<String, Integer> progressByQuest = assignedQuestProgress.get(playerId);
        if (progressByQuest != null) {
            progressByQuest.remove(questId.toLowerCase(Locale.ROOT));
            if (progressByQuest.isEmpty()) {
                assignedQuestProgress.remove(playerId);
            }
        }
        saveAssignedQuestState(playerId);

        Player player = getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            refreshAssignedQuestFlow(player, false);
            syncTutorialQuestHud(player, true);
        }
        return true;
    }

    public List<Player> getQuestAssignablePlayers() {
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
        players.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
        return players;
    }

    private Set<String> getCompletedAssignedQuestIds(UUID playerId) {
        return completedAssignedQuestIds.computeIfAbsent(playerId, ignored -> new LinkedHashSet<>());
    }

    private int getAssignedQuestStoredProgress(UUID playerId, String questId) {
        if (playerId == null || questId == null || questId.isBlank()) {
            return 0;
        }
        Map<String, Integer> progressByQuest = assignedQuestProgress.get(playerId);
        if (progressByQuest == null) {
            return 0;
        }
        return Math.max(0, progressByQuest.getOrDefault(questId.toLowerCase(Locale.ROOT), 0));
    }

    private void setAssignedQuestProgress(UUID playerId, String questId, int progress, boolean save) {
        if (playerId == null || questId == null || questId.isBlank()) {
            return;
        }
        String normalizedQuestId = questId.toLowerCase(Locale.ROOT);
        if (progress <= 0) {
            Map<String, Integer> progressByQuest = assignedQuestProgress.get(playerId);
            if (progressByQuest != null) {
                progressByQuest.remove(normalizedQuestId);
                if (progressByQuest.isEmpty()) {
                    assignedQuestProgress.remove(playerId);
                }
            }
        } else {
            assignedQuestProgress.computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>()).put(normalizedQuestId, progress);
        }
        if (save) {
            saveAssignedQuestState(playerId);
        }
    }

    private void addAssignedQuestProgress(UUID playerId, String questId, int amount, boolean save) {
        if (playerId == null || questId == null || questId.isBlank() || amount <= 0) {
            return;
        }
        setAssignedQuestProgress(playerId, questId, getAssignedQuestStoredProgress(playerId, questId) + amount, save);
    }

    private int getQuestCurrentProgress(UUID playerId, PlayerQuestDefinition quest, boolean assignedQuest) {
        if (quest == null) {
            return 0;
        }
        return switch (quest.getType()) {
            case SELECT_PROFESSION -> quest.getProfession() != null
                    ? (hasProfession(playerId, quest.getProfession()) ? 1 : 0)
                    : (hasProfession(playerId) ? 1 : 0);
            case EARN_PROFESSION_XP -> getProfessionTotalProgressXp(playerId, resolveTutorialQuestProfession(playerId, quest));
            case REACH_PROFESSION_LEVEL -> {
                Profession profession = resolveTutorialQuestProfession(playerId, quest);
                yield profession != null ? getProfessionLevel(playerId, profession) : 0;
            }
            case JOIN_COUNTRY -> getPlayerCountry(playerId) != null ? 1 : 0;
            case CONTRIBUTE_COUNTRY -> assignedQuest
                    ? getAssignedQuestStoredProgress(playerId, quest.getId())
                    : getStoredTutorialQuestProgress(playerId, quest.getId());
        };
    }

    private PlayerQuestDefinition getActiveAssignedQuest(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        List<String> assigned = assignedQuestIds.get(playerId);
        if (assigned == null || assigned.isEmpty()) {
            return null;
        }
        Set<String> completed = completedAssignedQuestIds.get(playerId);
        for (String questId : assigned) {
            if (completed != null && completed.contains(questId.toLowerCase(Locale.ROOT))) {
                continue;
            }
            PlayerQuestDefinition definition = getGeneralQuestDefinition(questId);
            if (definition == null || !definition.isEnabled()) {
                continue;
            }
            return definition;
        }
        return null;
    }

    private void refreshTutorialQuestFlow(Player player, boolean sendMessages) {
        if (player == null || !player.isOnline()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        String previousQuestId = getActiveTutorialQuestId(playerId);
        boolean completedAny = false;

        while (true) {
            PlayerQuestDefinition activeQuest = getActiveTutorialQuest(playerId);
            if (activeQuest == null || !isTutorialQuestSatisfied(playerId, activeQuest)) {
                break;
            }

            getCompletedTutorialQuestIds(playerId).add(activeQuest.getId());
            if (activeQuest.getType() == PlayerQuestType.CONTRIBUTE_COUNTRY) {
                setTutorialQuestProgress(playerId, activeQuest.getId(), 0, false);
            }
            completedAny = true;
            saveTutorialStage(playerId);

            if (sendMessages) {
                player.sendMessage(getMessage("tutorial.card-border"));
                player.sendMessage(colorize("&aQuest Complete: &f" + ChatColor.stripColor(resolveTutorialQuestText(activeQuest.getTitle(), playerId, activeQuest))));
                player.sendMessage(colorize("&7" + ChatColor.stripColor(resolveTutorialQuestText(activeQuest.getObjective(), playerId, activeQuest))));
                player.sendMessage(getMessage("tutorial.card-border"));
                playTutorialStageAdvanceEffect(player, false);
            }
        }

        String currentQuestId = getActiveTutorialQuestId(playerId);
        if (currentQuestId == null && completedAny) {
            completeTutorial(player);
            return;
        }

        if (sendMessages && currentQuestId != null && (previousQuestId == null || !currentQuestId.equalsIgnoreCase(previousQuestId))) {
            PlayerQuestDefinition activeQuest = getActiveTutorialQuest(playerId);
            if (activeQuest != null) {
                player.sendMessage(getMessage("tutorial.card-border"));
                player.sendMessage(colorize("&6Active Quest: &f" + ChatColor.stripColor(resolveTutorialQuestText(activeQuest.getTitle(), playerId, activeQuest))));
                player.sendMessage(colorize("&7" + ChatColor.stripColor(resolveTutorialQuestText(activeQuest.getObjective(), playerId, activeQuest))));
                String hint = getTutorialQuestHint(playerId);
                if (!hint.isBlank()) {
                    player.sendMessage(colorize("&eHint: &7" + ChatColor.stripColor(hint)));
                }
                player.sendMessage(getMessage("tutorial.card-border"));
                playTutorialStageAdvanceEffect(player, activeQuest.getType() == PlayerQuestType.SELECT_PROFESSION);
            }
        }

        syncTutorialQuestHud(player, false);
    }

    private void refreshAssignedQuestFlow(Player player, boolean sendMessages) {
        if (player == null || !player.isOnline()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        String previousQuestId = getActiveAssignedQuestId(playerId);

        while (true) {
            PlayerQuestDefinition activeQuest = getActiveAssignedQuest(playerId);
            if (activeQuest == null || getQuestCurrentProgress(playerId, activeQuest, true) < Math.max(1, activeQuest.getTarget())) {
                break;
            }

            getCompletedAssignedQuestIds(playerId).add(activeQuest.getId());
            if (activeQuest.getType() == PlayerQuestType.CONTRIBUTE_COUNTRY) {
                setAssignedQuestProgress(playerId, activeQuest.getId(), 0, false);
            }
            saveAssignedQuestState(playerId);

            if (sendMessages) {
                player.sendMessage(getMessage("tutorial.card-border"));
                player.sendMessage(colorize("&aAssigned Quest Complete: &f" + ChatColor.stripColor(resolveQuestText(activeQuest.getTitle(), playerId, activeQuest, true))));
                player.sendMessage(colorize("&7" + ChatColor.stripColor(resolveQuestText(activeQuest.getObjective(), playerId, activeQuest, true))));
                player.sendMessage(getMessage("tutorial.card-border"));
                playTutorialStageAdvanceEffect(player, false);
            }
        }

        String currentQuestId = getActiveAssignedQuestId(playerId);
        if (sendMessages && currentQuestId != null && (previousQuestId == null || !currentQuestId.equalsIgnoreCase(previousQuestId))) {
            PlayerQuestDefinition activeQuest = getActiveAssignedQuest(playerId);
            if (activeQuest != null) {
                player.sendMessage(getMessage("tutorial.card-border"));
                player.sendMessage(colorize("&6Assigned Quest: &f" + ChatColor.stripColor(resolveQuestText(activeQuest.getTitle(), playerId, activeQuest, true))));
                player.sendMessage(colorize("&7" + ChatColor.stripColor(resolveQuestText(activeQuest.getObjective(), playerId, activeQuest, true))));
                String hint = getQuestHintText(playerId, activeQuest, true);
                if (!hint.isBlank()) {
                    player.sendMessage(colorize("&eHint: &7" + ChatColor.stripColor(hint)));
                }
                player.sendMessage(getMessage("tutorial.card-border"));
                playTutorialStageAdvanceEffect(player, activeQuest.getType() == PlayerQuestType.SELECT_PROFESSION);
            }
        }
    }

    private String getActiveAssignedQuestId(UUID playerId) {
        PlayerQuestDefinition activeQuest = getActiveAssignedQuest(playerId);
        return activeQuest != null ? activeQuest.getId() : null;
    }

    private PlayerQuestDefinition getActiveDisplayedQuest(UUID playerId) {
        PlayerQuestDefinition assignedQuest = getActiveAssignedQuest(playerId);
        return assignedQuest != null ? assignedQuest : getActiveTutorialQuest(playerId);
    }

    private String resolveTutorialQuestText(String text, UUID playerId, PlayerQuestDefinition quest) {
        return resolveQuestText(text, playerId, quest, false);
    }

    private String resolveQuestText(String text, UUID playerId, PlayerQuestDefinition quest, boolean assignedQuest) {
        if (text == null || text.isBlank()) {
            return "";
        }
        Profession profession = resolveTutorialQuestProfession(playerId, quest);
        int current = getQuestCurrentProgress(playerId, quest, assignedQuest);
        int target = Math.max(1, quest != null ? quest.getTarget() : 1);
        String playerName = getServer().getOfflinePlayer(playerId).getName();
        return text
                .replace("%player%", playerName != null ? playerName : "player")
                .replace("%profession%", profession != null ? profession.getDisplayName() : "profession")
                .replace("%profession_key%", profession != null ? profession.getKey() : "none")
                .replace("%progress%", String.valueOf(Math.min(current, target)))
                .replace("%current%", String.valueOf(Math.min(current, target)))
                .replace("%target%", String.valueOf(target))
                .replace("%remaining%", String.valueOf(Math.max(0, target - current)));
    }

    private String getQuestHintText(UUID playerId, PlayerQuestDefinition quest, boolean assignedQuest) {
        if (quest == null) {
            return colorize(questsSettingsConfig != null ? questsSettingsConfig.getString("quests.no-active.hint", "") : "");
        }
        return colorize(resolveQuestText(quest.getHint(), playerId, quest, assignedQuest));
    }

    public boolean hasActiveTutorialQuest(UUID playerId) {
        return playerId != null && !isTutorialIntroActive(playerId) && getActiveDisplayedQuest(playerId) != null;
    }

    public String getTutorialQuestId(UUID playerId) {
        PlayerQuestDefinition activeQuest = playerId != null && !isTutorialIntroActive(playerId) ? getActiveDisplayedQuest(playerId) : null;
        return activeQuest != null ? activeQuest.getId() : "";
    }

    public String getTutorialQuestTitle(UUID playerId) {
        PlayerQuestDefinition activeQuest = playerId != null && !isTutorialIntroActive(playerId) ? getActiveDisplayedQuest(playerId) : null;
        if (activeQuest == null) {
            return colorize(questsSettingsConfig != null ? questsSettingsConfig.getString("quests.no-active.title", "&7No active quest") : "&7No active quest");
        }
        return colorize(resolveQuestText(activeQuest.getTitle(), playerId, activeQuest, activeQuest == getActiveAssignedQuest(playerId)));
    }

    public String getTutorialQuestObjective(UUID playerId) {
        PlayerQuestDefinition activeQuest = playerId != null && !isTutorialIntroActive(playerId) ? getActiveDisplayedQuest(playerId) : null;
        if (activeQuest == null) {
            return colorize(questsSettingsConfig != null ? questsSettingsConfig.getString("quests.no-active.objective", "&7Explore Terra") : "&7Explore Terra");
        }
        return colorize(resolveQuestText(activeQuest.getObjective(), playerId, activeQuest, activeQuest == getActiveAssignedQuest(playerId)));
    }

    public String getTutorialQuestHint(UUID playerId) {
        PlayerQuestDefinition activeQuest = playerId != null && !isTutorialIntroActive(playerId) ? getActiveDisplayedQuest(playerId) : null;
        return getQuestHintText(playerId, activeQuest, activeQuest == getActiveAssignedQuest(playerId));
    }

    public String getTutorialQuestProgressText(UUID playerId) {
        PlayerQuestDefinition activeQuest = playerId != null && !isTutorialIntroActive(playerId) ? getActiveDisplayedQuest(playerId) : null;
        if (activeQuest == null) {
            return "";
        }
        int current = getQuestCurrentProgress(playerId, activeQuest, activeQuest == getActiveAssignedQuest(playerId));
        int target = Math.max(1, activeQuest.getTarget());
        return Math.min(current, target) + "/" + target;
    }

    public String getTutorialQuestStatusText(UUID playerId) {
        if (!hasActiveTutorialQuest(playerId)) {
            return "No active objective";
        }
        int current = getTutorialQuestCurrentValue(playerId);
        int target = Math.max(1, getTutorialQuestTargetValue(playerId));
        int percent = getTutorialQuestPercent(playerId);
        return current + "/" + target + " complete  " + percent + "%";
    }

    public String getTutorialQuestProgressBarText(UUID playerId) {
        int filled = Math.max(0, Math.min(12, (int) Math.round((getTutorialQuestPercent(playerId) / 100.0D) * 12.0D)));
        StringBuilder builder = new StringBuilder(14);
        builder.append('[');
        for (int index = 0; index < 12; index++) {
            builder.append(index < filled ? '#' : '-');
        }
        builder.append(']');
        return builder.toString();
    }

    public String getTutorialQuestAccentColor(UUID playerId) {
        if (!hasActiveTutorialQuest(playerId)) {
            return "#7C8794";
        }
        String profession = getTutorialQuestProfessionKey(playerId);
        return switch (profession.toLowerCase(Locale.ROOT)) {
            case "miner" -> "#D1A85A";
            case "lumberjack" -> "#74B87F";
            case "farmer" -> "#B5C96A";
            case "builder" -> "#D48F68";
            case "blacksmith" -> "#B98BD6";
            default -> "#DAB06A";
        };
    }

    public int getTutorialQuestCurrentValue(UUID playerId) {
        PlayerQuestDefinition activeQuest = playerId != null && !isTutorialIntroActive(playerId) ? getActiveDisplayedQuest(playerId) : null;
        if (activeQuest == null) {
            return 0;
        }
        return Math.min(
                getQuestCurrentProgress(playerId, activeQuest, activeQuest == getActiveAssignedQuest(playerId)),
                Math.max(1, activeQuest.getTarget())
        );
    }

    public int getTutorialQuestTargetValue(UUID playerId) {
        PlayerQuestDefinition activeQuest = playerId != null && !isTutorialIntroActive(playerId) ? getActiveDisplayedQuest(playerId) : null;
        return activeQuest != null ? Math.max(1, activeQuest.getTarget()) : 0;
    }

    public int getTutorialQuestPercent(UUID playerId) {
        PlayerQuestDefinition activeQuest = playerId != null && !isTutorialIntroActive(playerId) ? getActiveDisplayedQuest(playerId) : null;
        if (activeQuest == null) {
            return 0;
        }
        int current = getQuestCurrentProgress(playerId, activeQuest, activeQuest == getActiveAssignedQuest(playerId));
        int target = Math.max(1, activeQuest.getTarget());
        return Math.max(0, Math.min(100, (int) Math.round((Math.min(current, target) * 100.0D) / target)));
    }

    public int getTutorialQuestSteps(UUID playerId) {
        int percent = getTutorialQuestPercent(playerId);
        int maxSteps = getItemsAdderQuestHudMaxSteps();
        return Math.max(0, Math.min(maxSteps, (int) Math.round((percent / 100.0D) * maxSteps)));
    }

    public int getTutorialQuestMaxSteps() {
        return getItemsAdderQuestHudMaxSteps();
    }

    public String getTutorialQuestTitlePlain(UUID playerId) {
        return ChatColor.stripColor(getTutorialQuestTitle(playerId));
    }

    public String getTutorialQuestObjectivePlain(UUID playerId) {
        return ChatColor.stripColor(getTutorialQuestObjective(playerId));
    }

    public String getTutorialQuestHintPlain(UUID playerId) {
        return ChatColor.stripColor(getTutorialQuestHint(playerId));
    }

    public String getTutorialQuestProfessionKey(UUID playerId) {
        PlayerQuestDefinition activeQuest = playerId != null && !isTutorialIntroActive(playerId) ? getActiveDisplayedQuest(playerId) : null;
        Profession profession = resolveTutorialQuestProfession(playerId, activeQuest);
        return profession != null ? profession.getKey() : "none";
    }

    private boolean isItemsAdderQuestHudSyncEnabled() {
        return false;
    }

    private int getItemsAdderQuestHudMaxSteps() {
        if (questsSettingsConfig == null) {
            return 10;
        }
        return Math.max(1, questsSettingsConfig.getInt("quests.itemsadder-sync.max-steps", 10));
    }

    private String getItemsAdderQuestHudStepStatName() {
        if (questsSettingsConfig == null) {
            return "terra_quest_steps";
        }
        return questsSettingsConfig.getString("quests.itemsadder-sync.step-stat", "terra_quest_steps");
    }

    private void syncTutorialQuestHud(Player player, boolean force) {
        if (player == null || !player.isOnline()) {
            return;
        }

        // ItemsAdder quest HUD stat sync is no longer used.
        tutorialQuestHudIdCache.remove(player.getUniqueId());
        tutorialQuestHudPercentCache.remove(player.getUniqueId());
        tutorialQuestHudStepCache.remove(player.getUniqueId());
    }

    private void reloadFurnaceSessions() {
        furnaceSessions.clear();

        ConfigurationSection section = dataConfig.getConfigurationSection("furnaces");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            FurnaceKey furnaceKey = FurnaceKey.fromPath(key);
            if (furnaceKey == null) {
                continue;
            }

            UUID ownerId = null;
            UUID pendingOwnerId = null;
            String ownerValue = dataConfig.getString("furnaces." + key + ".owner");
            String ownerName = dataConfig.getString("furnaces." + key + ".ownerName");
            String pendingOwnerValue = dataConfig.getString("furnaces." + key + ".pendingOwner");
            String pendingOwnerName = dataConfig.getString("furnaces." + key + ".pendingOwnerName");
            if (ownerValue != null && !ownerValue.isBlank()) {
                try {
                    ownerId = UUID.fromString(ownerValue);
                } catch (IllegalArgumentException ignored) {
                    ownerId = null;
                }
            }
            if (pendingOwnerValue != null && !pendingOwnerValue.isBlank()) {
                try {
                    pendingOwnerId = UUID.fromString(pendingOwnerValue);
                } catch (IllegalArgumentException ignored) {
                    pendingOwnerId = null;
                }
            }

            long lockedAt = dataConfig.getLong("furnaces." + key + ".lockedAt", System.currentTimeMillis());
            FurnaceSession session = new FurnaceSession();
            session.ownerId = ownerId;
            session.ownerName = ownerName;
            session.lockedAt = ownerId != null ? lockedAt : 0L;
            session.pendingOwnerId = pendingOwnerId;
            session.pendingOwnerName = pendingOwnerName;
            loadFurnacePendingMap(session.pendingMinerItems, "furnaces." + key + ".pending-miner");
            loadFurnacePendingMap(session.pendingFarmerItems, "furnaces." + key + ".pending-farmer");

            if (!session.isExpired() || !session.isEmpty()) {
                furnaceSessions.put(furnaceKey, session);
            }
        }
    }

    private void reloadFixedOreBlocks() {
        fixedOreBlocks.clear();

        ConfigurationSection section = dataConfig.getConfigurationSection("fixed-ores");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            PlacedBlockKey blockKey = PlacedBlockKey.fromPath(key);
            if (blockKey == null) {
                continue;
            }

            Material oreType = Material.matchMaterial(section.getString(key, ""));
            if (!isFixedOreMaterial(oreType)) {
                continue;
            }

            fixedOreBlocks.put(blockKey, oreType);
            World world = getServer().getWorld(blockKey.worldId);
            if (world != null) {
                world.getBlockAt(blockKey.x, blockKey.y, blockKey.z).setType(oreType, false);
            }
        }
    }

    private void reloadTraderData() {
        traderReputations.clear();
        resetTraderCycleState();
        activeTraderStates.clear();
        nextTraderSpawnMillis = 0L;

        long now = System.currentTimeMillis();
        nextTraderSpawnMillis = restorePausedDeadline("traders.next-spawn", "traders.next-spawn-remaining", now);

        ConfigurationSection reputationSection = dataConfig.getConfigurationSection("traders.reputation");
        if (reputationSection != null) {
            for (String key : reputationSection.getKeys(false)) {
                try {
                    traderReputations.put(UUID.fromString(key), roundTraderReputation(Math.max(0.0D, reputationSection.getDouble(key, 0.0D))));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        dataConfig.set("traders.quests", null);
        dataConfig.set("traders.big-orders", null);

        ConfigurationSection activeSection = dataConfig.getConfigurationSection("traders.active");
        if (activeSection == null) {
            return;
        }

        for (String countryKey : activeSection.getKeys(false)) {
            ConfigurationSection entrySection = activeSection.getConfigurationSection(countryKey);
            if (entrySection == null) {
                continue;
            }
            try {
                UUID traderId = UUID.fromString(entrySection.getString("id", ""));
                UUID entityId = UUID.fromString(entrySection.getString("entity-id", ""));
                long despawnAt = restorePausedDeadlineForSection(entrySection, "despawn-at", "remaining-millis", now);
                String worldName = entrySection.getString("world");
                if (worldName == null || despawnAt <= now) {
                    continue;
                }
                long elapsedOnlineMillis = Math.max(0L, entrySection.getLong("elapsed-online-millis", 0L));
                long spawnedAt = Math.max(0L, now - elapsedOnlineMillis);

                DynamicTraderState traderState = new DynamicTraderState(
                        traderId,
                        entityId,
                        entrySection.getString("host-country"),
                        entrySection.getString("name", "Trader"),
                        Profession.fromKey(entrySection.getString("specialty")),
                        worldName,
                        entrySection.getDouble("x", 0.0D),
                        entrySection.getDouble("y", 0.0D),
                        entrySection.getDouble("z", 0.0D),
                        (float) entrySection.getDouble("yaw", 0.0D),
                        (float) entrySection.getDouble("pitch", 0.0D),
                        spawnedAt,
                        despawnAt
                );
                activeTraderStates.put(countryKey, traderState);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private long restorePausedDeadline(String absolutePath, String remainingPath, long now) {
        long remainingMillis = Math.max(-1L, dataConfig.getLong(remainingPath, -1L));
        if (remainingMillis >= 0L) {
            return remainingMillis > 0L ? now + remainingMillis : 0L;
        }
        long absoluteMillis = Math.max(0L, dataConfig.getLong(absolutePath, 0L));
        return absoluteMillis > now ? absoluteMillis : 0L;
    }

    private long restorePausedDeadlineForSection(ConfigurationSection section, String absolutePath, String remainingPath, long now) {
        if (section == null) {
            return 0L;
        }
        long remainingMillis = Math.max(-1L, section.getLong(remainingPath, -1L));
        if (remainingMillis >= 0L) {
            return remainingMillis > 0L ? now + remainingMillis : 0L;
        }
        long absoluteMillis = Math.max(0L, section.getLong(absolutePath, 0L));
        return absoluteMillis > now ? absoluteMillis : 0L;
    }

    private long getRemainingDurationMillis(long deadlineMillis, long now) {
        if (deadlineMillis <= 0L) {
            return 0L;
        }
        return Math.max(0L, deadlineMillis - now);
    }

    private long getElapsedOnlineMillis(long startedAtMillis, long now) {
        if (startedAtMillis <= 0L) {
            return 0L;
        }
        return Math.max(0L, now - startedAtMillis);
    }

    private void reloadMerchantData() {
        activeMerchantStates.clear();
        merchantSharedStock.clear();
        merchantDailySoldAmounts.clear();
        merchantTradeCooldowns.clear();
        long now = System.currentTimeMillis();
        nextMerchantSpawnMillis = restorePausedDeadline("merchant.next-spawn", "merchant.next-spawn-remaining", now);
        merchantDailySalesResetMillis = Math.max(0L, dataConfig.getLong("merchant.daily-sales-reset", 0L));
        merchantCycleSeed = Math.max(0L, dataConfig.getLong("merchant.cycle-seed", 0L));

        ConfigurationSection activeSection = dataConfig.getConfigurationSection("merchant.active");
        if (activeSection != null) {
            for (String countryKey : activeSection.getKeys(false)) {
                ConfigurationSection entrySection = activeSection.getConfigurationSection(countryKey);
                if (entrySection == null) {
                    continue;
                }
                try {
                    UUID merchantId = UUID.fromString(entrySection.getString("id", ""));
                    UUID entityId = UUID.fromString(entrySection.getString("entity-id", ""));
                    long despawnAt = restorePausedDeadlineForSection(entrySection, "despawn-at", "remaining-millis", now);
                    String worldName = entrySection.getString("world");
                    if (worldName == null || despawnAt <= now) {
                        continue;
                    }
                    long elapsedOnlineMillis = Math.max(0L, entrySection.getLong("elapsed-online-millis", 0L));
                    long spawnedAt = Math.max(0L, now - elapsedOnlineMillis);
                    MerchantShopState state = new MerchantShopState(
                            merchantId,
                            entityId,
                            entrySection.getString("host-country"),
                            worldName,
                            entrySection.getDouble("x", 0.0D),
                            entrySection.getDouble("y", 0.0D),
                            entrySection.getDouble("z", 0.0D),
                            (float) entrySection.getDouble("yaw", 0.0D),
                            (float) entrySection.getDouble("pitch", 0.0D),
                            spawnedAt,
                            despawnAt
                    );
                    activeMerchantStates.put(countryKey, state);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        ConfigurationSection stockSection = dataConfig.getConfigurationSection("merchant.shared-stock");
        if (stockSection != null) {
            for (String key : stockSection.getKeys(false)) {
                merchantSharedStock.put(key, Math.max(0, stockSection.getInt(key, 0)));
            }
        }

        ConfigurationSection soldSection = dataConfig.getConfigurationSection("merchant.daily-sold");
        if (soldSection != null) {
            for (String key : soldSection.getKeys(false)) {
                merchantDailySoldAmounts.put(key, Math.max(0, soldSection.getInt(key, 0)));
            }
        }

        ConfigurationSection cooldownSection = dataConfig.getConfigurationSection("merchant.trade-cooldowns");
        if (cooldownSection != null) {
            for (String key : cooldownSection.getKeys(false)) {
                try {
                    long expiresAt = restorePausedDeadlineForSection(cooldownSection, key, key + "-remaining", now);
                    if (expiresAt > now) {
                        merchantTradeCooldowns.put(UUID.fromString(key), expiresAt);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private void saveTraderData() {
        long now = System.currentTimeMillis();
        long nextSpawnRemaining = getRemainingDurationMillis(nextTraderSpawnMillis, now);
        dataConfig.set("traders.next-spawn", null);
        dataConfig.set("traders.next-spawn-remaining", nextSpawnRemaining > 0L ? nextSpawnRemaining : null);

        dataConfig.set("traders.reputation", null);
        for (Map.Entry<UUID, Double> entry : traderReputations.entrySet()) {
            if (entry.getValue() > 0.0D) {
                dataConfig.set("traders.reputation." + entry.getKey(), entry.getValue());
            }
        }

        dataConfig.set("traders.quests", null);
        dataConfig.set("traders.big-orders", null);
        dataConfig.set("traders.active", null);
        for (Map.Entry<String, DynamicTraderState> entry : activeTraderStates.entrySet()) {
            DynamicTraderState traderState = entry.getValue();
            if (traderState == null) {
                continue;
            }
            String path = "traders.active." + entry.getKey();
            dataConfig.set(path + ".id", traderState.getTraderId().toString());
            dataConfig.set(path + ".entity-id", traderState.getEntityId().toString());
            dataConfig.set(path + ".host-country", traderState.getHostCountryKey());
            dataConfig.set(path + ".name", traderState.getTraderName());
            dataConfig.set(path + ".specialty", traderState.getSpecialtyProfession() != null ? traderState.getSpecialtyProfession().getKey() : null);
            dataConfig.set(path + ".world", traderState.getWorldName());
            dataConfig.set(path + ".x", traderState.getX());
            dataConfig.set(path + ".y", traderState.getY());
            dataConfig.set(path + ".z", traderState.getZ());
            dataConfig.set(path + ".yaw", traderState.getYaw());
            dataConfig.set(path + ".pitch", traderState.getPitch());
            dataConfig.set(path + ".spawned-at", null);
            dataConfig.set(path + ".despawn-at", null);
            dataConfig.set(path + ".elapsed-online-millis", getElapsedOnlineMillis(traderState.getSpawnedAtMillis(), now));
            long remainingMillis = getRemainingDurationMillis(traderState.getDespawnAtMillis(), now);
            dataConfig.set(path + ".remaining-millis", remainingMillis > 0L ? remainingMillis : null);
        }
        saveDataConfig();
    }

    private void saveMerchantData() {
        long now = System.currentTimeMillis();
        long nextSpawnRemaining = getRemainingDurationMillis(nextMerchantSpawnMillis, now);
        dataConfig.set("merchant.next-spawn", null);
        dataConfig.set("merchant.next-spawn-remaining", nextSpawnRemaining > 0L ? nextSpawnRemaining : null);
        dataConfig.set("merchant.daily-sales-reset", merchantDailySalesResetMillis > 0L ? merchantDailySalesResetMillis : null);
        dataConfig.set("merchant.cycle-seed", merchantCycleSeed > 0L ? merchantCycleSeed : null);

        dataConfig.set("merchant.active", null);
        for (Map.Entry<String, MerchantShopState> entry : activeMerchantStates.entrySet()) {
            MerchantShopState state = entry.getValue();
            if (state == null) {
                continue;
            }
            String path = "merchant.active." + entry.getKey();
            dataConfig.set(path + ".id", state.getMerchantId().toString());
            dataConfig.set(path + ".entity-id", state.getEntityId().toString());
            dataConfig.set(path + ".host-country", state.getHostCountryKey());
            dataConfig.set(path + ".world", state.getWorldName());
            dataConfig.set(path + ".x", state.getX());
            dataConfig.set(path + ".y", state.getY());
            dataConfig.set(path + ".z", state.getZ());
            dataConfig.set(path + ".yaw", state.getYaw());
            dataConfig.set(path + ".pitch", state.getPitch());
            dataConfig.set(path + ".spawned-at", null);
            dataConfig.set(path + ".despawn-at", null);
            dataConfig.set(path + ".elapsed-online-millis", getElapsedOnlineMillis(state.getSpawnedAtMillis(), now));
            long remainingMillis = getRemainingDurationMillis(state.getDespawnAtMillis(), now);
            dataConfig.set(path + ".remaining-millis", remainingMillis > 0L ? remainingMillis : null);
        }

        dataConfig.set("merchant.shared-stock", null);
        for (Map.Entry<String, Integer> entry : merchantSharedStock.entrySet()) {
            if (entry.getValue() != null && entry.getValue() > 0) {
                dataConfig.set("merchant.shared-stock." + entry.getKey(), entry.getValue());
            }
        }

        dataConfig.set("merchant.daily-sold", null);
        for (Map.Entry<String, Integer> entry : merchantDailySoldAmounts.entrySet()) {
            if (entry.getValue() != null && entry.getValue() > 0) {
                dataConfig.set("merchant.daily-sold." + entry.getKey(), entry.getValue());
            }
        }

        dataConfig.set("merchant.trade-cooldowns", null);
        for (Map.Entry<UUID, Long> entry : merchantTradeCooldowns.entrySet()) {
            long remainingMillis = getRemainingDurationMillis(entry.getValue(), now);
            if (remainingMillis > 0L) {
                dataConfig.set("merchant.trade-cooldowns." + entry.getKey(), null);
                dataConfig.set("merchant.trade-cooldowns." + entry.getKey() + "-remaining", remainingMillis);
            }
        }

        saveDataConfig();
    }

    private void restartTraderRuntime() {
        stopTraderRuntime();
        if (!isTraderSystemEnabled()) {
            return;
        }

        ensureTraderPresenceAfterLoad();
        if (activeTraderStates.isEmpty() && nextTraderSpawnMillis <= 0L) {
            scheduleNextTraderSpawn(true);
        }
        traderRuntimeTask = getServer().getScheduler().runTaskTimer(this, this::tickTraderRuntime, 20L, 20L * 30L);
    }

    private void stopTraderRuntime() {
        if (traderRuntimeTask != null) {
            traderRuntimeTask.cancel();
            traderRuntimeTask = null;
        }
    }

    private void tickTraderRuntime() {
        if (!isTraderSystemEnabled()) {
            return;
        }

        long now = System.currentTimeMillis();
        ensureTraderPresenceAfterLoad();

        DynamicTraderState cycleState = getSharedTraderCycleState();
        if (cycleState != null && cycleState.getDespawnAtMillis() <= now) {
            despawnActiveTrader(true);
            return;
        }

        for (Map.Entry<String, DynamicTraderState> entry : new ArrayList<>(activeTraderStates.entrySet())) {
            DynamicTraderState traderState = entry.getValue();
            Country hostCountry = getCountryByKey(entry.getKey());
            if (hostCountry == null || getCountryTraderSpawnLocation(hostCountry) == null) {
                despawnTrader(entry.getKey(), false);
                continue;
            }
        }

        cycleState = getSharedTraderCycleState();
        if (cycleState != null) {
            String hostCountryKey = normalizeCountryKey(cycleState.getHostCountryKey());
            for (Map.Entry<String, DynamicTraderState> entry : new ArrayList<>(activeTraderStates.entrySet())) {
                if (!entry.getKey().equals(hostCountryKey)) {
                    despawnTrader(entry.getKey(), false);
                }
            }
            Country hostCountry = getCountryByKey(hostCountryKey);
            if (hostCountry == null || getCountryTraderSpawnLocation(hostCountry) == null) {
                despawnActiveTrader(false);
                return;
            }
            spawnTraderForCountry(hostCountry, cycleState.getSpecialtyProfession(), false, cycleState.getSpawnedAtMillis(), cycleState.getDespawnAtMillis(), false);
            removeOrphanTraderNpcs();
            saveTraderData();
            return;
        }

        if (nextTraderSpawnMillis <= 0L) {
            scheduleNextTraderSpawn(true);
            return;
        }

        if (now >= nextTraderSpawnMillis && !spawnDynamicTraderNow()) {
            scheduleNextTraderSpawn(true);
        }
    }

    private void ensureTraderPresenceAfterLoad() {
        boolean changed = false;
        for (Map.Entry<String, DynamicTraderState> entry : new ArrayList<>(activeTraderStates.entrySet())) {
            DynamicTraderState traderState = entry.getValue();
            if (traderState == null) {
                activeTraderStates.remove(entry.getKey());
                changed = true;
                continue;
            }

            Entity entity = getServer().getEntity(traderState.getEntityId());
            if (entity instanceof Villager villager && villager.isValid()) {
                configureTraderNpc(villager, traderState);
                continue;
            }

            World world = getServer().getWorld(traderState.getWorldName());
            if (world == null) {
                activeTraderStates.remove(entry.getKey());
                changed = true;
                continue;
            }

            Location location = new Location(
                    world,
                    traderState.getX(),
                    traderState.getY(),
                    traderState.getZ(),
                    traderState.getYaw(),
                    0.0F
            );
            Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
            configureTraderNpc(villager, traderState);
            activeTraderStates.put(entry.getKey(), new DynamicTraderState(
                    traderState.getTraderId(),
                    villager.getUniqueId(),
                    traderState.getHostCountryKey(),
                    traderState.getTraderName(),
                    traderState.getSpecialtyProfession(),
                    traderState.getWorldName(),
                    traderState.getX(),
                    traderState.getY(),
                    traderState.getZ(),
                    traderState.getYaw(),
                    0.0F,
                    traderState.getSpawnedAtMillis(),
                    traderState.getDespawnAtMillis()
            ));
            changed = true;
        }
        removeOrphanTraderNpcs();
        if (changed) {
            saveTraderData();
        }
    }

    private void scheduleNextTraderSpawn(boolean save) {
        long minDelay = Math.max(300_000L, getConfig().getLong("traders.spawn-minutes-min", 180L) * 60_000L);
        long maxDelay = Math.max(minDelay, getConfig().getLong("traders.spawn-minutes-max", 360L) * 60_000L);
        long delay = minDelay;
        if (maxDelay > minDelay) {
            delay += Math.abs(new Random().nextLong()) % ((maxDelay - minDelay) + 1L);
        }
        nextTraderSpawnMillis = System.currentTimeMillis() + delay;
        if (save) {
            saveTraderData();
        }
    }

    private boolean isTraderSystemEnabled() {
        return getConfig().getBoolean("traders.enabled", true);
    }

    public long getTraderActiveDurationMillis() {
        return Math.max(15L, getConfig().getLong("traders.active-minutes", 90L)) * 60_000L;
    }

    public long getTraderAcceptedQuestDurationMillis() {
        return Math.max(60L, getConfig().getLong("traders.accepted-quest-minutes", 1440L)) * 60_000L;
    }

    public long getTraderDeliveryCooldownMillis() {
        return Math.max(1L, getConfig().getLong("traders.delivery-cooldown-minutes", 30L)) * 60_000L;
    }

    private int getTraderDifficultyTier(UUID playerId, Profession profession) {
        int level = Math.max(1, getProfessionLevel(playerId, profession));
        double reputation = getTraderReputation(playerId);
        int tier = 1 + ((level - 1) / 14) + (int) Math.floor(reputation / 3.0D);
        return Math.max(1, Math.min(6, tier));
    }

    public String formatTraderReputation(double value) {
        return String.format(Locale.US, "%.1f", roundTraderReputation(value));
    }

    private double sanitizeTraderReputationReward(double value) {
        double rounded = roundTraderReputation(value);
        if (rounded <= 0.0D) {
            return 0.1D;
        }
        if (rounded > 0.8D) {
            return 0.8D;
        }
        return rounded;
    }

    public String formatTraderLastSeen(long millis) {
        if (millis <= 0L) {
            return "never";
        }
        long elapsed = System.currentTimeMillis() - millis;
        if (elapsed <= 0L) {
            return "just now";
        }
        return formatLongDurationWords(elapsed) + " ago";
    }

    private double roundTraderReputation(double value) {
        return Math.round(Math.max(0.0D, value) * 10.0D) / 10.0D;
    }

    private int getTraderQuestBaseAmount(Profession profession) {
        return switch (profession) {
            case MINER -> 10;
            case LUMBERJACK -> 14;
            case FARMER -> 12;
            case BUILDER -> 14;
            case BLACKSMITH -> 2;
            case TRADER -> 8;
            case SOLDIER -> 6;
        };
    }

    private int getTraderQuestAmountStep(Profession profession) {
        return switch (profession) {
            case MINER, BLACKSMITH -> 2;
            case LUMBERJACK, FARMER, BUILDER, TRADER -> 4;
            case SOLDIER -> 2;
        };
    }

    private double getTraderUnitMoney(Material material) {
        return switch (material) {
            case DIAMOND, EMERALD, GOLDEN_CARROT, CAKE, TARGET -> 18.0D;
            case NETHERITE_INGOT -> 40.0D;
            case ANCIENT_DEBRIS, DIAMOND_PICKAXE, DIAMOND_AXE, DIAMOND_SWORD, ENCHANTING_TABLE -> 24.0D;
            case IRON_PICKAXE, IRON_AXE, IRON_SHOVEL, IRON_HOE, SHEARS, BUCKET, SHIELD, CROSSBOW -> 15.0D;
            case GLASS, BRICKS, STONE_BRICKS, OAK_PLANKS, SPRUCE_PLANKS, BIRCH_PLANKS, COBBLESTONE, TUFF_BRICKS, MUD_BRICKS -> 3.0D;
            case PUMPKIN_PIE, BREAD, COOKED_BEEF, COOKED_CHICKEN, COOKED_PORKCHOP, HONEY_BOTTLE -> 8.0D;
            default -> 5.0D;
        };
    }

    private List<Material> getTraderQuestMaterials(Profession profession, int tier) {
        return switch (profession) {
            case MINER -> switch (tier) {
                case 1 -> List.of(Material.COAL, Material.COBBLESTONE, Material.COPPER_INGOT, Material.CALCITE);
                case 2 -> List.of(Material.IRON_INGOT, Material.REDSTONE, Material.LAPIS_LAZULI, Material.TUFF);
                case 3 -> List.of(Material.GOLD_INGOT, Material.DEEPSLATE, Material.OBSIDIAN, Material.RAW_IRON);
                case 4 -> List.of(Material.DIAMOND, Material.EMERALD, Material.AMETHYST_SHARD, Material.RAW_GOLD);
                case 5 -> List.of(Material.DIAMOND, Material.EMERALD_BLOCK, Material.OBSIDIAN, Material.AMETHYST_BLOCK);
                default -> List.of(Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK, Material.OBSIDIAN, Material.GOLD_BLOCK);
            };
            case LUMBERJACK -> switch (tier) {
                case 1 -> List.of(Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.OAK_WOOD);
                case 2 -> List.of(Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.SPRUCE_WOOD);
                case 3 -> List.of(Material.MANGROVE_LOG, Material.CHERRY_LOG, Material.BIRCH_WOOD, Material.DARK_OAK_WOOD);
                case 4 -> List.of(Material.MANGROVE_WOOD, Material.CHERRY_WOOD, Material.BAMBOO_BLOCK, Material.STRIPPED_OAK_WOOD);
                case 5 -> List.of(Material.STRIPPED_MANGROVE_WOOD, Material.STRIPPED_CHERRY_WOOD, Material.STRIPPED_BAMBOO_BLOCK, Material.STRIPPED_DARK_OAK_WOOD);
                default -> List.of(Material.STRIPPED_CHERRY_WOOD, Material.STRIPPED_MANGROVE_WOOD, Material.STRIPPED_BAMBOO_BLOCK, Material.STRIPPED_ACACIA_WOOD);
            };
            case FARMER -> switch (tier) {
                case 1 -> List.of(Material.WHEAT, Material.CARROT, Material.POTATO, Material.BEETROOT);
                case 2 -> List.of(Material.MELON_SLICE, Material.PUMPKIN, Material.SWEET_BERRIES, Material.SUGAR_CANE);
                case 3 -> List.of(Material.BREAD, Material.COOKIE, Material.APPLE, Material.HONEY_BOTTLE);
                case 4 -> List.of(Material.PUMPKIN_PIE, Material.CAKE, Material.GOLDEN_CARROT, Material.COOKED_CHICKEN);
                case 5 -> List.of(Material.COOKED_BEEF, Material.COOKED_PORKCHOP, Material.RABBIT_STEW, Material.GLOW_BERRIES);
                default -> List.of(Material.GOLDEN_CARROT, Material.CAKE, Material.GOLDEN_APPLE, Material.SUSPICIOUS_STEW);
            };
            case BUILDER -> switch (tier) {
                case 1 -> List.of(Material.COBBLESTONE, Material.OAK_PLANKS, Material.GLASS, Material.OAK_STAIRS);
                case 2 -> List.of(Material.STONE_BRICKS, Material.BRICKS, Material.SMOOTH_STONE, Material.SPRUCE_PLANKS);
                case 3 -> List.of(Material.POLISHED_ANDESITE, Material.DEEPSLATE_BRICKS, Material.TERRACOTTA, Material.BIRCH_PLANKS);
                case 4 -> List.of(Material.MUD_BRICKS, Material.TUFF_BRICKS, Material.BOOKSHELF, Material.CHISELED_BOOKSHELF);
                case 5 -> List.of(Material.DARK_PRISMARINE, Material.PRISMARINE_BRICKS, Material.LANTERN, Material.TARGET);
                default -> List.of(Material.QUARTZ_PILLAR, Material.POLISHED_DIORITE, Material.POLISHED_GRANITE, Material.TARGET);
            };
            case BLACKSMITH -> switch (tier) {
                case 1 -> List.of(Material.BUCKET, Material.SHEARS, Material.IRON_SHOVEL, Material.FLINT_AND_STEEL);
                case 2 -> List.of(Material.IRON_AXE, Material.IRON_PICKAXE, Material.IRON_HOE, Material.IRON_HELMET);
                case 3 -> List.of(Material.SHIELD, Material.IRON_SWORD, Material.CROSSBOW, Material.IRON_CHESTPLATE);
                case 4 -> List.of(Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SWORD, Material.DIAMOND_HELMET);
                case 5 -> List.of(Material.DIAMOND_CHESTPLATE, Material.DIAMOND_BOOTS, Material.ANVIL, Material.BLAST_FURNACE);
                default -> List.of(Material.ENCHANTING_TABLE, Material.DIAMOND_CHESTPLATE, Material.BELL, Material.BLAST_FURNACE);
            };
            case TRADER -> switch (tier) {
                case 1 -> List.of(Material.PAPER, Material.BOOK, Material.CHEST, Material.EMERALD);
                case 2 -> List.of(Material.BARREL, Material.ITEM_FRAME, Material.MAP, Material.GOLD_INGOT);
                case 3 -> List.of(Material.COMPASS, Material.CLOCK, Material.EMERALD_BLOCK, Material.ENDER_CHEST);
                default -> List.of(Material.EMERALD_BLOCK, Material.ENDER_CHEST, Material.COMPASS, Material.CLOCK);
            };
            case SOLDIER -> switch (tier) {
                case 1 -> List.of(Material.SHIELD, Material.ARROW, Material.IRON_SWORD, Material.COOKED_BEEF);
                case 2 -> List.of(Material.CROSSBOW, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.GOLDEN_APPLE);
                default -> List.of(Material.DIAMOND_SWORD, Material.SHIELD, Material.GOLDEN_APPLE, Material.CROSSBOW);
            };
        };
    }

    private List<Material> getTraderQuestMaterialsForLevel(Profession profession, int tier, int professionLevel) {
        List<Material> pool = new ArrayList<>(getTraderQuestMaterials(profession, tier));
        if (pool.isEmpty()) {
            return pool;
        }

        int safeLevel = Math.max(1, professionLevel);
        List<Material> unlocked = new ArrayList<>();
        List<Material> stretch = new ArrayList<>();
        for (Material material : pool) {
            int requiredLevel = getTraderMaterialSuggestedLevel(profession, material);
            if (requiredLevel <= safeLevel) {
                unlocked.add(material);
            } else if (requiredLevel <= safeLevel + 1) {
                stretch.add(material);
            }
        }

        if (!unlocked.isEmpty()) {
            if (!stretch.isEmpty() && safeLevel >= 3 && ThreadLocalRandom.current().nextDouble() < 0.15D) {
                List<Material> combined = new ArrayList<>(unlocked);
                combined.addAll(stretch);
                return combined;
            }
            return unlocked;
        }

        if (!stretch.isEmpty()) {
            return stretch;
        }

        return pool;
    }

    private int getTraderMaterialSuggestedLevel(Profession profession, Material material) {
        if (profession == null || material == null) {
            return 1;
        }
        return switch (profession) {
            case MINER -> switch (material) {
                case COBBLESTONE, COAL, CALCITE -> 1;
                case COPPER_INGOT, REDSTONE, TUFF -> 2;
                case IRON_INGOT, RAW_IRON, LAPIS_LAZULI -> 3;
                case GOLD_INGOT, DEEPSLATE, OBSIDIAN, RAW_GOLD -> 5;
                case DIAMOND, EMERALD, AMETHYST_SHARD -> 7;
                case EMERALD_BLOCK, AMETHYST_BLOCK, GOLD_BLOCK -> 8;
                case DIAMOND_BLOCK -> 10;
                default -> 1;
            };
            case LUMBERJACK -> switch (material) {
                case OAK_LOG, SPRUCE_LOG, BIRCH_LOG, OAK_WOOD -> 1;
                case JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG, SPRUCE_WOOD -> 2;
                case MANGROVE_LOG, CHERRY_LOG, BIRCH_WOOD, DARK_OAK_WOOD -> 4;
                case MANGROVE_WOOD, CHERRY_WOOD, BAMBOO_BLOCK, STRIPPED_OAK_WOOD -> 5;
                case STRIPPED_MANGROVE_WOOD, STRIPPED_CHERRY_WOOD, STRIPPED_BAMBOO_BLOCK, STRIPPED_DARK_OAK_WOOD -> 7;
                case STRIPPED_ACACIA_WOOD -> 8;
                default -> 1;
            };
            case FARMER -> switch (material) {
                case WHEAT, CARROT, POTATO, BEETROOT -> 1;
                case MELON_SLICE, PUMPKIN, SWEET_BERRIES, SUGAR_CANE -> 2;
                case BREAD, COOKIE, APPLE, HONEY_BOTTLE -> 3;
                case PUMPKIN_PIE, CAKE, COOKED_CHICKEN -> 4;
                case GOLDEN_CARROT -> 5;
                case COOKED_BEEF, COOKED_PORKCHOP, GLOW_BERRIES -> 6;
                case RABBIT_STEW -> 7;
                case SUSPICIOUS_STEW -> 8;
                case GOLDEN_APPLE -> 10;
                default -> 1;
            };
            case BUILDER -> switch (material) {
                case COBBLESTONE, OAK_PLANKS, GLASS, OAK_STAIRS -> 1;
                case STONE_BRICKS, BRICKS, SMOOTH_STONE, SPRUCE_PLANKS -> 2;
                case POLISHED_ANDESITE, DEEPSLATE_BRICKS, TERRACOTTA, BIRCH_PLANKS -> 3;
                case MUD_BRICKS, TUFF_BRICKS, BOOKSHELF, CHISELED_BOOKSHELF -> 5;
                case PRISMARINE_BRICKS, DARK_PRISMARINE, LANTERN -> 7;
                case TARGET, QUARTZ_PILLAR, POLISHED_DIORITE, POLISHED_GRANITE -> 8;
                default -> 1;
            };
            case BLACKSMITH -> switch (material) {
                case BUCKET, SHEARS, IRON_SHOVEL, FLINT_AND_STEEL -> 1;
                case IRON_AXE, IRON_PICKAXE, IRON_HOE, IRON_HELMET -> 2;
                case SHIELD, IRON_SWORD, CROSSBOW, IRON_CHESTPLATE -> 3;
                case DIAMOND_PICKAXE, DIAMOND_AXE, DIAMOND_SWORD, DIAMOND_HELMET -> 5;
                case ANVIL, BLAST_FURNACE, DIAMOND_BOOTS, DIAMOND_CHESTPLATE -> 7;
                case BELL -> 8;
                case ENCHANTING_TABLE -> 9;
                default -> 1;
            };
            case TRADER -> switch (material) {
                case PAPER, BOOK, CHEST, EMERALD -> 1;
                case BARREL, ITEM_FRAME, MAP, GOLD_INGOT -> 3;
                case COMPASS, CLOCK, EMERALD_BLOCK -> 5;
                case ENDER_CHEST -> 7;
                default -> 1;
            };
            case SOLDIER -> switch (material) {
                case SHIELD, ARROW, IRON_SWORD, COOKED_BEEF -> 1;
                case CROSSBOW, IRON_HELMET, IRON_CHESTPLATE -> 3;
                case GOLDEN_APPLE -> 5;
                case DIAMOND_SWORD -> 7;
                default -> 1;
            };
        };
    }

    private void configureTraderNpc(Villager villager, DynamicTraderState traderState) {
        if (villager == null || traderState == null) {
            return;
        }
        String displayName = getMessage("terra.trader.npc-name");
        if (!displayName.contains("%name%") || !displayName.contains("%profession%")) {
            displayName = "&6%name% &8(&f%profession%&8)";
        }
        Villager.Profession villagerProfession = getVillagerProfessionForTrader(traderState.getSpecialtyProfession());
        displayName = displayName
                .replace("%name%", traderState.getTraderName())
                .replace("%profession%", getProfessionPlainDisplayName(traderState.getSpecialtyProfession()));
        villager.customName(legacyComponent(displayName));
        villager.setCustomNameVisible(true);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(false);
        villager.setCollidable(false);
        villager.setCanPickupItems(false);
        villager.setPersistent(true);
        villager.setRemoveWhenFarAway(false);
        villager.setProfession(villagerProfession);
        villager.getPersistentDataContainer().set(traderNpcKey, PersistentDataType.BYTE, (byte) 1);
    }

    private Villager.Profession getVillagerProfessionForTrader(Profession specialtyProfession) {
        if (specialtyProfession == null) {
            return Villager.Profession.NONE;
        }
        return switch (specialtyProfession) {
            case FARMER -> Villager.Profession.FARMER;
            case BLACKSMITH -> Villager.Profession.TOOLSMITH;
            case BUILDER -> Villager.Profession.MASON;
            case LUMBERJACK -> Villager.Profession.FLETCHER;
            case MINER -> Villager.Profession.ARMORER;
            case TRADER -> Villager.Profession.CARTOGRAPHER;
            case SOLDIER -> Villager.Profession.WEAPONSMITH;
        };
    }

    private Country selectNextTraderHostCountry() {
        List<Country> eligible = new ArrayList<>();
        for (Country country : countriesByKey.values()) {
            if (getCountryTraderSpawnLocation(country) != null) {
                eligible.add(country);
            }
        }
        if (eligible.isEmpty()) {
            return null;
        }
        eligible.sort((a, b) -> Long.compare(a.getLastTraderSeenAtMillis(), b.getLastTraderSeenAtMillis()));
        return eligible.get(0);
    }

    private Country selectNextMerchantHostCountry() {
        List<Country> eligible = new ArrayList<>();
        for (Country country : countriesByKey.values()) {
            if (getCountryHome(country) != null) {
                eligible.add(country);
            }
        }
        if (eligible.isEmpty()) {
            return null;
        }
        return eligible.get(ThreadLocalRandom.current().nextInt(eligible.size()));
    }

    private Profession selectTraderSpecialty(Country hostCountry) {
        List<Profession> configuredProfessions = getConfiguredProfessions().stream()
                .filter(profession -> profession != Profession.SOLDIER)
                .toList();
        if (!configuredProfessions.isEmpty()) {
            return configuredProfessions.get(ThreadLocalRandom.current().nextInt(configuredProfessions.size()));
        }
        Profession[] professions = Profession.values();
        return professions[ThreadLocalRandom.current().nextInt(professions.length)];
    }

    private String generateTraderName(Profession specialtyProfession) {
        return switch (specialtyProfession) {
            case MINER -> List.of("Borin", "Keld", "Rurik", "Marn").get(ThreadLocalRandom.current().nextInt(4));
            case LUMBERJACK -> List.of("Alden", "Rowan", "Bran", "Hale").get(ThreadLocalRandom.current().nextInt(4));
            case FARMER -> List.of("Mira", "Elsie", "Tomas", "Greta").get(ThreadLocalRandom.current().nextInt(4));
            case BUILDER -> List.of("Doran", "Petra", "Milo", "Sera").get(ThreadLocalRandom.current().nextInt(4));
            case BLACKSMITH -> List.of("Varric", "Helga", "Toren", "Iris").get(ThreadLocalRandom.current().nextInt(4));
            case TRADER -> List.of("Soren", "Marta", "Jules", "Corin").get(ThreadLocalRandom.current().nextInt(4));
            case SOLDIER -> List.of("Rhea", "Cass", "Vigo", "Brant").get(ThreadLocalRandom.current().nextInt(4));
        };
    }

    public Country getCountryByKey(String countryKey) {
        if (countryKey == null || countryKey.isBlank()) {
            return null;
        }
        return countriesByKey.get(countryKey.toLowerCase(Locale.ROOT));
    }

    public Location getCountryTraderSpawnLocation(Country country) {
        if (country == null) {
            return null;
        }
        if (country.hasTraderSpawn()) {
            World world = getServer().getWorld(country.getTraderSpawnWorld());
            if (world != null) {
                return new Location(world, country.getTraderSpawnX(), country.getTraderSpawnY(), country.getTraderSpawnZ(), country.getTraderSpawnYaw(), 0.0F);
            }
        }
        Location home = getCountryHome(country);
        if (home == null) {
            return null;
        }
        return home.clone();
    }

    private void removeTraderEntity(DynamicTraderState traderState) {
        if (traderState == null) {
            return;
        }
        Entity entity = getServer().getEntity(traderState.getEntityId());
        if (entity != null) {
            entity.remove();
        }
    }

    private void removeOrphanTraderNpcs() {
        if (traderNpcKey == null) {
            return;
        }
        Set<UUID> activeEntityIds = new HashSet<>();
        for (DynamicTraderState traderState : activeTraderStates.values()) {
            if (traderState != null) {
                activeEntityIds.add(traderState.getEntityId());
            }
        }
        for (World world : getServer().getWorlds()) {
            for (Villager villager : world.getEntitiesByClass(Villager.class)) {
                if (!villager.getPersistentDataContainer().has(traderNpcKey, PersistentDataType.BYTE)) {
                    continue;
                }
                if (activeEntityIds.contains(villager.getUniqueId())) {
                    continue;
                }
                villager.remove();
            }
        }
    }

    private boolean removeItems(Player player, Material material, int amount) {
        if (player == null || material == null || amount <= 0) {
            return false;
        }
        if (!player.getInventory().containsAtLeast(new ItemStack(material), amount)) {
            return false;
        }

        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != material) {
                continue;
            }

            int removed = Math.min(remaining, item.getAmount());
            item.setAmount(item.getAmount() - removed);
            if (item.getAmount() <= 0) {
                contents[i] = null;
            }
            remaining -= removed;
        }

        player.getInventory().setContents(contents);
        player.updateInventory();
        return remaining <= 0;
    }

    private int removeItemsUpTo(Player player, Material material, int maxAmount) {
        if (player == null || material == null || maxAmount <= 0) {
            return 0;
        }

        int remaining = maxAmount;
        int removedTotal = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != material) {
                continue;
            }

            int removed = Math.min(remaining, item.getAmount());
            item.setAmount(item.getAmount() - removed);
            if (item.getAmount() <= 0) {
                contents[i] = null;
            }
            remaining -= removed;
            removedTotal += removed;
        }

        if (removedTotal > 0) {
            player.getInventory().setContents(contents);
            player.updateInventory();
        }
        return removedTotal;
    }

    public ItemStack createFixedOreToolItem() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(legacyComponent("&cFixed Ore Remover"));
            meta.lore(List.of(
                    legacyComponent("&7Admin tool for removing fixed ore nodes."),
                    legacyComponent("&7Right-click a fixed ore block to delete it.")
            ));
            meta.getPersistentDataContainer().set(fixedOreToolKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isFixedOreTool(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(fixedOreToolKey, PersistentDataType.BYTE);
    }

    private void saveCountry(Country country) {
        refreshCountryProgressionState(country);
        String key = normalizeCountryName(country.getName());
        countryBorderLocationCache.remove(normalizeCountryKey(country.getName()));
        String path = "countries." + key;
        countryDataConfig.set(path + ".name", country.getName());
        countryDataConfig.set(path + ".owner", country.getOwnerId() != null ? country.getOwnerId().toString() : null);
        countryDataConfig.set(path + ".open", country.isOpen());

        List<String> memberIds = new ArrayList<>();
        for (UUID memberId : country.getMembers()) {
            memberIds.add(memberId.toString());
            playerCountries.put(memberId, key);
        }
        countryDataConfig.set(path + ".members", memberIds);

        List<String> inviteIds = new ArrayList<>();
        for (UUID inviteId : country.getInvitedPlayers()) {
            inviteIds.add(inviteId.toString());
        }
        countryDataConfig.set(path + ".invites", inviteIds);

        List<String> coOwnerIds = new ArrayList<>();
        for (UUID coOwnerId : country.getCoOwners()) {
            if (country.getMembers().contains(coOwnerId) && !coOwnerId.equals(country.getOwnerId())) {
                coOwnerIds.add(coOwnerId.toString());
            }
        }
        countryDataConfig.set(path + ".roles.coowners", coOwnerIds);

        List<String> stewardIds = new ArrayList<>();
        for (UUID stewardId : country.getStewards()) {
            if (country.getMembers().contains(stewardId) && !stewardId.equals(country.getOwnerId())) {
                stewardIds.add(stewardId.toString());
            }
        }
        countryDataConfig.set(path + ".roles.stewards", stewardIds);

        countryDataConfig.set(path + ".tag", country.getTag());
        countryDataConfig.set(path + ".territory.world", country.getTerritoryWorld());
        countryDataConfig.set(path + ".territory.region", country.getTerritoryRegionId());
        countryDataConfig.set(path + ".home.world", country.getHomeWorld());
        countryDataConfig.set(path + ".home.x", country.getHomeX());
        countryDataConfig.set(path + ".home.y", country.getHomeY());
        countryDataConfig.set(path + ".home.z", country.getHomeZ());
        countryDataConfig.set(path + ".home.yaw", country.getHomeYaw());
        countryDataConfig.set(path + ".home.pitch", country.getHomePitch());
        countryDataConfig.set(path + ".trader.spawn.world", country.getTraderSpawnWorld());
        countryDataConfig.set(path + ".trader.spawn.x", country.getTraderSpawnX());
        countryDataConfig.set(path + ".trader.spawn.y", country.getTraderSpawnY());
        countryDataConfig.set(path + ".trader.spawn.z", country.getTraderSpawnZ());
        countryDataConfig.set(path + ".trader.spawn.yaw", country.getTraderSpawnYaw());
        countryDataConfig.set(path + ".trader.spawn.pitch", country.getTraderSpawnPitch());
        countryDataConfig.set(path + ".trader.allowed-countries", new ArrayList<>(country.getAllowedTradeCountries()));
        countryDataConfig.set(path + ".trader.last.name", country.getLastTraderName());
        countryDataConfig.set(path + ".trader.last.specialty", country.getLastTraderSpecialty());
        countryDataConfig.set(path + ".trader.last.seen-at", country.getLastTraderSeenAtMillis() > 0L ? country.getLastTraderSeenAtMillis() : null);
        countryDataConfig.set(path + ".economy.balance", roundMoney(Math.max(0.0D, country.getTreasuryBalance())));
        countryDataConfig.set(path + ".economy.resources", Math.max(0, country.getResourceStockpile()));
        countryDataConfig.set(path + ".boost.key", country.getActiveBoostKey());
        countryDataConfig.set(path + ".boost.until", country.getActiveBoostUntilMillis() > 0L ? country.getActiveBoostUntilMillis() : null);
        countryDataConfig.set(path + ".progression.level", Math.max(1, Math.min(COUNTRY_MAX_LEVEL, country.getLevel())));
        countryDataConfig.set(path + ".progression.unlocked", new ArrayList<>(country.getUnlockedUpgradeKeys()));
        saveCountryDataConfig();
    }

    private ProfessionProgress getStoredProfessionProgress(UUID playerId, Profession profession) {
        Map<Profession, ProfessionProgress> progressByProfession =
                professionProgress.computeIfAbsent(playerId, ignored -> new EnumMap<>(Profession.class));
        return progressByProfession.computeIfAbsent(profession, ignored -> new ProfessionProgress(1, 0));
    }

    private ProfessionProgress getProfessionProgress(UUID playerId, Profession profession) {
        Map<Profession, ProfessionProgress> progressByProfession = professionProgress.get(playerId);
        if (progressByProfession == null) {
            return new ProfessionProgress(1, 0);
        }

        ProfessionProgress progress = progressByProfession.get(profession);
        if (progress == null) {
            return new ProfessionProgress(1, 0);
        }

        return progress;
    }

    private void saveProfessionProgress(UUID playerId, Profession profession, ProfessionProgress progress) {
        Map<Profession, ProfessionProgress> progressByProfession =
                professionProgress.computeIfAbsent(playerId, ignored -> new EnumMap<>(Profession.class));
        progressByProfession.put(profession, progress);

        String path = "profession-progress." + playerId + "." + profession.getKey();
        dataConfig.set(path + ".level", progress.getLevel());
        dataConfig.set(path + ".xp", progress.getXp());
        saveDataConfig();
    }

    private void saveProfessionSkillProgress(UUID playerId, Profession profession) {
        if (playerId == null || profession == null) {
            return;
        }

        Map<Profession, Set<String>> byProfession = unlockedProfessionSkillNodes.get(playerId);
        Set<String> unlocked = byProfession != null ? byProfession.get(profession) : null;
        String path = "profession-skills." + playerId + "." + profession.getKey();
        if (unlocked == null || unlocked.isEmpty()) {
            dataConfig.set(path, null);
        } else {
            dataConfig.set(path, new ArrayList<>(unlocked));
        }
        saveDataConfig();
    }

    private void saveProfessionSkillPointBonuses(UUID playerId, Profession profession) {
        if (playerId == null || profession == null) {
            return;
        }

        Map<Profession, Integer> byProfession = professionSkillPointBonuses.get(playerId);
        int amount = byProfession != null ? Math.max(0, byProfession.getOrDefault(profession, 0)) : 0;
        String path = "profession-skill-points." + playerId + "." + profession.getKey();
        dataConfig.set(path, amount > 0 ? amount : null);
        saveDataConfig();
    }

    private void saveProfessionState(UUID playerId) {
        String path = "professions." + playerId;
        Profession primary = getPrimaryProfession(playerId);
        Profession secondary = getSecondaryProfession(playerId);
        Profession active = activeProfessions.get(playerId);
        Boolean secondSlotOverride = secondaryProfessionUnlockOverrides.get(playerId);
        Profession developmentProfession = developmentModeProfessions.get(playerId);

        if (primary == null && secondSlotOverride == null && developmentProfession == null) {
            dataConfig.set(path, null);
        } else {
            dataConfig.set(path + ".primary", primary != null ? primary.name() : null);
            dataConfig.set(path + ".secondary", secondary != null ? secondary.name() : null);
            dataConfig.set(path + ".active", active != null ? active.name() : (primary != null ? primary.name() : null));
            dataConfig.set(path + ".second-slot-override", secondSlotOverride);
            dataConfig.set(path + ".development-mode", developmentProfession != null ? developmentProfession.name() : null);
        }
        saveDataConfig();
    }

    private void saveProfessionConfig(Profession profession) {
        File file = professionFiles.get(profession);
        FileConfiguration config = professionConfigs.get(profession);
        if (file == null || config == null) {
            return;
        }

        try {
            config.save(file);
        } catch (IOException exception) {
            getLogger().warning("Failed to save profession config for " + profession.name() + ": " + exception.getMessage());
        }
    }

    private FileConfiguration getProfessionProgressConfig(Profession profession) {
        return getProfessionConfig(profession);
    }

    private String getProfessionProgressPath(Profession profession) {
        return "progression";
    }

    private ConfigurationSection getProfessionLevelSection(Profession profession, int level) {
        FileConfiguration config = getProfessionProgressConfig(profession);
        return config.getConfigurationSection("levels." + level);
    }

    private ConfigurationSection getProfessionBlocksSection(Profession profession, int level) {
        ConfigurationSection levelSection = getProfessionLevelSection(profession, level);
        return levelSection != null ? levelSection.getConfigurationSection("blocks") : null;
    }

    private FileConfiguration getProfessionConfig(Profession profession) {
        FileConfiguration config = professionConfigs.get(profession);
        return config != null ? config : jobsConfig;
    }

    public String normalizeCountryKey(String countryName) {
        return countryName.toLowerCase(Locale.ROOT);
    }

    private String normalizeCountryName(String countryName) {
        return normalizeCountryKey(countryName);
    }

    private void saveDefaultConfigFiles() {
        ensureRuntimeConfigFileExists();
        saveResourceIfMissing("economy/block-values.yml");
        saveResourceIfMissing("jobs/config.yml");
        saveResourceIfMissing("settings/core.yml");
        saveResourceIfMissing("settings/climate.yml");
        saveResourceIfMissing("settings/stability.yml");
        saveResourceIfMissing("settings/merchant.yml");
        saveResourceIfMissing("settings/territories.yml");
        saveResourceIfMissing("settings/quests.yml");
        saveResourceIfMissing("scoreboard/config.yml");
        for (Profession profession : Profession.values()) {
            saveResourceIfMissing("jobs/" + profession.getKey() + ".yml");
        }
        saveResourceIfMissing("chat/config.yml");
        saveResourceIfMissing("messages/messages.yml");
        saveResourceIfMissing("data.yml");
        saveResourceIfMissing("countries/data.yml");
        reloadCustomConfigs();
    }

    private void reloadCustomConfigs() {
        blockValuesFile = new File(getDataFolder(), "economy/block-values.yml");
        jobsFile = new File(getDataFolder(), "jobs/config.yml");
        chatFile = new File(getDataFolder(), "chat/config.yml");
        messagesFile = new File(getDataFolder(), "messages/messages.yml");
        dataFile = new File(getDataFolder(), "data.yml");
        countryDataFile = new File(getDataFolder(), "countries/data.yml");
        coreSettingsFile = new File(getDataFolder(), "settings/core.yml");
        climateSettingsFile = new File(getDataFolder(), "settings/climate.yml");
        stabilitySettingsFile = new File(getDataFolder(), "settings/stability.yml");
        merchantSettingsFile = new File(getDataFolder(), "settings/merchant.yml");
        territorySettingsFile = new File(getDataFolder(), "settings/territories.yml");
        questsSettingsFile = new File(getDataFolder(), "settings/quests.yml");
        scoreboardSettingsFile = new File(getDataFolder(), "scoreboard/config.yml");
        blockValuesConfig = loadCustomConfig(blockValuesFile, "economy/block-values.yml");
        jobsConfig = loadCustomConfig(jobsFile, "jobs/config.yml");
        coreSettingsConfig = loadCustomConfig(coreSettingsFile, "settings/core.yml");
        climateSettingsConfig = loadCustomConfig(climateSettingsFile, "settings/climate.yml");
        stabilitySettingsConfig = loadCustomConfig(stabilitySettingsFile, "settings/stability.yml");
        merchantSettingsConfig = loadCustomConfig(merchantSettingsFile, "settings/merchant.yml");
        territorySettingsConfig = loadCustomConfig(territorySettingsFile, "settings/territories.yml");
        questsSettingsConfig = loadCustomConfig(questsSettingsFile, "settings/quests.yml");
        scoreboardSettingsConfig = loadCustomConfig(scoreboardSettingsFile, "scoreboard/config.yml");
        migrateScoreboardConfig();
        professionFiles.clear();
        professionConfigs.clear();
        for (Profession profession : Profession.values()) {
            String resourcePath = "jobs/" + profession.getKey() + ".yml";
            File professionFile = new File(getDataFolder(), resourcePath);
            professionFiles.put(profession, professionFile);
            professionConfigs.put(profession, loadCustomConfig(professionFile, resourcePath));
        }
        chatConfig = loadCustomConfig(chatFile, "chat/config.yml");
        messagesConfig = loadCustomConfig(messagesFile, "messages/messages.yml");
        dataConfig = loadCustomConfig(dataFile, "data.yml");
        countryDataConfig = loadCustomConfig(countryDataFile, "countries/data.yml");
        loadMaintenanceSettings();
        migrateLegacyCountryData();
        migrateLegacyMainConfigSections();
    }

    private void ensureRuntimeConfigFileExists() {
        File file = new File(getDataFolder(), "config.yml");
        if (file.exists()) {
            return;
        }
        try {
            if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
                getLogger().warning("Failed to create plugin data folder for runtime config.");
                return;
            }
            YamlConfiguration configuration = new YamlConfiguration();
            configuration.options().header("""
                    Runtime merge file.
                    Terra now stores real settings in the split files under /settings, /jobs, /messages, /chat, and /economy.
                    This file can stay empty.
                    """);
            configuration.save(file);
        } catch (IOException exception) {
            getLogger().severe("Failed to create runtime config.yml: " + exception.getMessage());
        }
    }

    private void saveResourceIfMissing(String resourcePath) {
        File file = new File(getDataFolder(), resourcePath);
        if (!file.exists()) {
            try {
                saveResource(resourcePath, false);
            } catch (IllegalArgumentException exception) {
                getLogger().severe("Missing embedded resource " + resourcePath + ": " + exception.getMessage());
                File parent = file.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs()) {
                    getLogger().severe("Failed to create parent folder for " + resourcePath + ".");
                    return;
                }
                try {
                    if (!file.createNewFile()) {
                        getLogger().severe("Failed to create empty fallback file for " + resourcePath + ".");
                    }
                } catch (IOException ioException) {
                    getLogger().severe("Failed to create empty fallback file for " + resourcePath + ": " + ioException.getMessage());
                }
            }
        }
    }

    private FileConfiguration loadCustomConfig(File file, String resourcePath) {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        try (InputStream inputStream = getResource(resourcePath)) {
            if (inputStream != null) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                configuration.setDefaults(defaults);
                configuration.options().copyDefaults(true);
                saveYaml(configuration, file);
            }
        } catch (IOException exception) {
            getLogger().severe("Failed to load defaults for " + resourcePath + ": " + exception.getMessage());
        }

        return configuration;
    }

    private void migrateLegacyMainConfigSections() {
        migrateLegacyMainConfigSection("block-delay");
        migrateLegacyMainConfigSection("economy");
        migrateLegacyMainConfigSection("rewards");
        migrateLegacyMainConfigSection("hostile-mobs");
        migrateLegacyMainConfigSection("phantoms");
        migrateLegacyMainConfigSection("wilderness-regeneration");
        migrateLegacyMainConfigSection("ore-vision");
        migrateLegacyMainConfigSection("traders");
        migrateLegacyMainConfigSection("npc-head-tracking");
        migrateLegacyMainConfigSection("realtime-clock");
        migrateLegacyMainConfigSection("player-presence-sounds");
        migrateLegacyMainConfigSection("server-list-motd");
        migrateLegacyMainConfigSection("health-hotbar");
        migrateLegacyMainConfigSection("custom-scoreboard");
        migrateLegacyMainConfigSection("itemsadder-top-status");
        migrateLegacyMainConfigSection("join-leave-messages");
        migrateLegacyMainConfigSection("lag-reduction");
        migrateLegacyMainConfigSection("country-sounds");
        migrateLegacyMainConfigSection("chat-sounds");
        migrateLegacyMainConfigSection("terra-tips");
        migrateLegacyMainConfigSection("hunger");
        migrateLegacyMainConfigSection("profession-procs");
        migrateLegacyMainConfigSection("country-home");
        migrateLegacyMainConfigSection("climate");
        migrateLegacyMainConfigSection("stability");
        migrateLegacyMainConfigSection("merchant-shop");
        migrateLegacyMainConfigSection("territories");
        migrateLegacyMainConfigSection("country-border-particles");
    }

    private void migrateLegacyMainConfigSection(String rootPath) {
        if (rootPath == null || rootPath.isBlank() || !getConfig().contains(rootPath)) {
            return;
        }
        FileConfiguration targetConfig = getManagedConfigForPath(rootPath);
        File targetFile = getManagedConfigFileForPath(rootPath);
        if (targetConfig == null || targetFile == null || targetConfig.contains(rootPath)) {
            return;
        }
        ConfigurationSection legacySection = getConfig().getConfigurationSection(rootPath);
        if (legacySection != null) {
            copySectionValues(legacySection, targetConfig, rootPath);
            saveYaml(targetConfig, targetFile);
            return;
        }
        targetConfig.set(rootPath, getConfig().get(rootPath));
        saveYaml(targetConfig, targetFile);
    }

    private void copySectionValues(ConfigurationSection from, FileConfiguration target, String rootPath) {
        if (from == null || target == null || rootPath == null || rootPath.isBlank()) {
            return;
        }
        for (Map.Entry<String, Object> entry : from.getValues(true).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection) {
                continue;
            }
            String path = entry.getKey().isBlank() ? rootPath : rootPath + "." + entry.getKey();
            target.set(path, entry.getValue());
        }
    }

    private void mergeManagedSettingsIntoRuntimeConfig() {
        mergeManagedConfigIntoRuntime(coreSettingsConfig);
        mergeManagedConfigIntoRuntime(climateSettingsConfig);
        mergeManagedConfigIntoRuntime(stabilitySettingsConfig);
        mergeManagedConfigIntoRuntime(merchantSettingsConfig);
        mergeManagedConfigIntoRuntime(territorySettingsConfig);
        mergeManagedConfigIntoRuntime(scoreboardSettingsConfig);
    }

    private void mergeManagedConfigIntoRuntime(FileConfiguration managedConfig) {
        if (managedConfig == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : managedConfig.getValues(true).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection) {
                continue;
            }
            getConfig().set(entry.getKey(), entry.getValue());
        }
    }

    private FileConfiguration getManagedConfigForPath(String path) {
        if (path == null) {
            return null;
        }
        if (path.startsWith("climate.")) {
            return climateSettingsConfig;
        }
        if (path.startsWith("stability.")) {
            return stabilitySettingsConfig;
        }
        if (path.startsWith("merchant-shop.")) {
            return merchantSettingsConfig;
        }
        if (path.startsWith("territories.") || path.startsWith("country-border-particles.")) {
            return territorySettingsConfig;
        }
        if (path.equals("custom-scoreboard") || path.startsWith("custom-scoreboard.")) {
            return scoreboardSettingsConfig;
        }
        if (path.startsWith("block-delay.")
                || path.startsWith("economy.")
                || path.startsWith("rewards.")
                || path.startsWith("hostile-mobs.")
                || path.startsWith("phantoms.")
                || path.startsWith("wilderness-regeneration.")
                || path.startsWith("ore-vision.")
                || path.startsWith("traders.")
                || path.startsWith("npc-head-tracking.")
                || path.startsWith("realtime-clock.")
                || path.startsWith("player-presence-sounds.")
                || path.equals("server-list-motd")
                || path.startsWith("server-list-motd.")
                || path.equals("health-hotbar")
                || path.startsWith("health-hotbar.")
                || path.equals("itemsadder-top-status")
                || path.startsWith("itemsadder-top-status.")
                || path.startsWith("join-leave-messages.")
                || path.startsWith("lag-reduction.")
                || path.startsWith("country-sounds.")
                || path.startsWith("chat-sounds.")
                || path.startsWith("terra-tips.")
                || path.startsWith("hunger.")
                || path.startsWith("profession-procs.")
                || path.startsWith("country-home.")) {
            return coreSettingsConfig;
        }
        return null;
    }

    private File getManagedConfigFileForPath(String path) {
        if (path == null) {
            return null;
        }
        if (path.startsWith("climate.")) {
            return climateSettingsFile;
        }
        if (path.startsWith("stability.")) {
            return stabilitySettingsFile;
        }
        if (path.startsWith("merchant-shop.")) {
            return merchantSettingsFile;
        }
        if (path.startsWith("territories.") || path.startsWith("country-border-particles.")) {
            return territorySettingsFile;
        }
        if (path.equals("custom-scoreboard") || path.startsWith("custom-scoreboard.")) {
            return scoreboardSettingsFile;
        }
        if (getManagedConfigForPath(path) == coreSettingsConfig) {
            return coreSettingsFile;
        }
        return null;
    }

    private void setManagedConfigValue(String path, Object value) {
        FileConfiguration managedConfig = getManagedConfigForPath(path);
        File managedFile = getManagedConfigFileForPath(path);
        getConfig().set(path, value);
        if (managedConfig != null && managedFile != null) {
            managedConfig.set(path, value);
            saveYaml(managedConfig, managedFile);
            return;
        }
        saveConfig();
    }

    private void saveBlockValuesConfig() {
        saveYaml(blockValuesConfig, blockValuesFile);
    }

    private void saveDataConfig() {
        saveYaml(dataConfig, dataFile);
    }

    private void migrateScoreboardConfig() {
        if (scoreboardSettingsConfig == null || scoreboardSettingsFile == null) {
            return;
        }
        int version = scoreboardSettingsConfig.getInt("custom-scoreboard.config-version", 0);
        if (version >= SCOREBOARD_CONFIG_VERSION) {
            return;
        }

        scoreboardSettingsConfig.set("custom-scoreboard.config-version", SCOREBOARD_CONFIG_VERSION);
        scoreboardSettingsConfig.set("custom-scoreboard.enabled", true);
        scoreboardSettingsConfig.set("custom-scoreboard.update-ticks", 20);
        scoreboardSettingsConfig.set("custom-scoreboard.respect-other-scoreboards", true);
        scoreboardSettingsConfig.set("custom-scoreboard.hide-numbers", true);
        scoreboardSettingsConfig.set("custom-scoreboard.title", "&2&lᴛᴇʀʀᴀɴᴀᴛɪᴏɴѕ");
        scoreboardSettingsConfig.set("custom-scoreboard.title-animation", "scoreboard_logo");
        scoreboardSettingsConfig.set("custom-scoreboard.shadow.enabled", false);
        scoreboardSettingsConfig.set("custom-scoreboard.shadow.color", "&8");
        scoreboardSettingsConfig.set("custom-scoreboard.shadow.prefix", "");
        scoreboardSettingsConfig.set("custom-scoreboard.lines", getDefaultScoreboardLines());
        if (!scoreboardSettingsConfig.contains("scoreboard_logo.texts")) {
            scoreboardSettingsConfig.set("scoreboard_logo.change-interval", 25);
            scoreboardSettingsConfig.set("scoreboard_logo.texts", getDefaultScoreboardLogoFrames());
        }
        saveYaml(scoreboardSettingsConfig, scoreboardSettingsFile);
    }

    private List<String> getDefaultScoreboardLines() {
        return List.of(
                "&7%date% &8| &7%server_time%",
                "&f%player%",
                "",
                "&c⚐ &f%country% &7Lv.%country_level%",
                "&a⛏ &f%job% &7Lv.%job_level%",
                "&b✦ &f%job_xp%&7/&f%job_xp_required% XP",
                "&6⛃ &f$%money%",
                "",
                "&9⌚ &f%playtime%",
                "",
                "&7ᴘʟᴀʏ.ᴛᴇʀʀᴀɴᴀᴛɪᴏɴѕ.ɴᴇᴛ"
        );
    }

    private List<String> getDefaultScoreboardLogoFrames() {
        return List.of(
                "&#E9FFE2&lᴛ&#E4FCDB&lᴇ&#DEF8D4&lʀ&#D9F5CD&lʀ&#D3F1C6&lᴀ&#CEEEBF&lɴ&#C9EAB8&lᴀ&#C3E7B1&lᴛ&#BEE3AA&lɪ&#B8E0A2&lᴏ&#B3DC9B&lɴ&#AED994&lѕ",
                "&#E4FCDB&lᴛ&#DEF8D4&lᴇ&#D9F5CD&lʀ&#D3F1C6&lʀ&#CEEEBF&lᴀ&#C9EAB8&lɴ&#C3E7B1&lᴀ&#BEE3AA&lᴛ&#B8E0A2&lɪ&#B3DC9B&lᴏ&#AED994&lɴ&#A8D58D&lѕ",
                "&#DEF8D4&lᴛ&#D9F5CD&lᴇ&#D3F1C6&lʀ&#CEEEBF&lʀ&#C9EAB8&lᴀ&#C3E7B1&lɴ&#BEE3AA&lᴀ&#B8E0A2&lᴛ&#B3DC9B&lɪ&#AED994&lᴏ&#A8D58D&lɴ&#A3D286&lѕ",
                "&#D9F5CD&lᴛ&#D3F1C6&lᴇ&#CEEEBF&lʀ&#C9EAB8&lʀ&#C3E7B1&lᴀ&#BEE3AA&lɴ&#B8E0A2&lᴀ&#B3DC9B&lᴛ&#AED994&lɪ&#A8D58D&lᴏ&#A3D286&lɴ&#9DCE7F&lѕ",
                "&#D3F1C6&lᴛ&#CEEEBF&lᴇ&#C9EAB8&lʀ&#C3E7B1&lʀ&#BEE3AA&lᴀ&#B8E0A2&lɴ&#B3DC9B&lᴀ&#AED994&lᴛ&#A8D58D&lɪ&#A3D286&lᴏ&#9DCE7F&lɴ&#98CB78&lѕ",
                "&#CEEEBF&lᴛ&#C9EAB8&lᴇ&#C3E7B1&lʀ&#BEE3AA&lʀ&#B8E0A2&lᴀ&#B3DC9B&lɴ&#AED994&lᴀ&#A8D58D&lᴛ&#A3D286&lɪ&#9DCE7F&lᴏ&#98CB78&lɴ&#93C771&lѕ",
                "&#C9EAB8&lᴛ&#C3E7B1&lᴇ&#BEE3AA&lʀ&#B8E0A2&lʀ&#B3DC9B&lᴀ&#AED994&lɴ&#A8D58D&lᴀ&#A3D286&lᴛ&#9DCE7F&lɪ&#98CB78&lᴏ&#93C771&lɴ&#8DC46A&lѕ",
                "&#C3E7B1&lᴛ&#BEE3AA&lᴇ&#B8E0A2&lʀ&#B3DC9B&lʀ&#AED994&lᴀ&#A8D58D&lɴ&#A3D286&lᴀ&#9DCE7F&lᴛ&#98CB78&lɪ&#93C771&lᴏ&#8DC46A&lɴ&#88C063&lѕ"
        );
    }

    private List<Map<String, Object>> getDefaultServerMotdFrames() {
        List<Map<String, Object>> frames = new ArrayList<>();
        frames.add(Map.of("lines", List.of(
                "&#E9FFE2&lᴛᴇʀʀᴀɴᴀᴛɪᴏɴѕ &8| &7%online%&8/&7%max_players%",
                "&7Countries &8• &7Jobs &8• &7Survival"
        )));
        frames.add(Map.of("lines", List.of(
                "&#BEE3AA&lᴛᴇʀʀᴀɴᴀᴛɪᴏɴѕ &8| &f%server_time%",
                "&aBuild your country. Grow your land."
        )));
        frames.add(Map.of("lines", List.of(
                "&#7DB955&lᴛᴇʀʀᴀɴᴀᴛɪᴏɴѕ &8| &7%date%",
                "&2Play &8• &fplay.terranations.net"
        )));
        return frames;
    }

    private void saveMaintenanceAccessList() {
        List<String> encoded = new ArrayList<>();
        for (UUID playerId : maintenanceAllowedPlayers) {
            encoded.add(playerId.toString());
        }
        encoded.sort(String.CASE_INSENSITIVE_ORDER);
        dataConfig.set("maintenance.allowed-players", encoded);
        saveDataConfig();
    }

    private void loadMaintenanceSettings() {
        maintenanceModeEnabled = dataConfig.getBoolean("maintenance.enabled", false);
        maintenanceAllowedPlayers.clear();
        for (String raw : dataConfig.getStringList("maintenance.allowed-players")) {
            try {
                maintenanceAllowedPlayers.add(UUID.fromString(raw));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void restartLagReductionRuntime() {
        stopLagReductionRuntime();

        if (areGroundItemClearEnabled()) {
            nextGroundItemClearMillis = System.currentTimeMillis() + (getGroundItemClearIntervalMinutes() * 60_000L);
            groundItemClearWarningsSent.clear();
            groundItemClearTask = getServer().getScheduler().runTaskTimer(this, this::tickGroundItemClear, 20L, 20L);
        }

        if (isMobStackingEnabled()) {
            mobStackingTask = getServer().getScheduler().runTaskTimer(this, this::runMobStackingSweep, 100L, 200L);
        }
    }

    private void stopLagReductionRuntime() {
        if (groundItemClearTask != null) {
            groundItemClearTask.cancel();
            groundItemClearTask = null;
        }
        if (mobStackingTask != null) {
            mobStackingTask.cancel();
            mobStackingTask = null;
        }
        nextGroundItemClearMillis = 0L;
        groundItemClearWarningsSent.clear();
    }

    private void tickGroundItemClear() {
        if (!areGroundItemClearEnabled()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (nextGroundItemClearMillis <= 0L) {
            nextGroundItemClearMillis = now + (getGroundItemClearIntervalMinutes() * 60_000L);
            groundItemClearWarningsSent.clear();
            return;
        }

        int secondsLeft = (int) Math.ceil((nextGroundItemClearMillis - now) / 1000.0D);
        for (int warningSecond : getGroundItemClearWarningSeconds()) {
            if (secondsLeft == warningSecond && groundItemClearWarningsSent.add(warningSecond)) {
                Bukkit.broadcastMessage(getMessage("lag.item-clear-warning", placeholders(
                        "seconds", String.valueOf(warningSecond)
                )));
            }
        }

        if (now < nextGroundItemClearMillis) {
            return;
        }

        int removed = clearGroundItems();
        Bukkit.broadcastMessage(getMessage("lag.item-clear-complete", placeholders(
                "count", String.valueOf(removed)
        )));
        nextGroundItemClearMillis = now + (getGroundItemClearIntervalMinutes() * 60_000L);
        groundItemClearWarningsSent.clear();
    }

    private void saveCountryDataConfig() {
        saveYaml(countryDataConfig, countryDataFile);
    }

    private void migrateLegacyCountryData() {
        if (countryDataConfig == null || dataConfig == null) {
            return;
        }
        ConfigurationSection legacySection = dataConfig.getConfigurationSection("countries");
        if (legacySection == null || countryDataConfig.getConfigurationSection("countries") != null) {
            return;
        }

        copySectionValues(legacySection, countryDataConfig, "countries");
        dataConfig.set("countries", null);
        saveCountryDataConfig();
        saveDataConfig();
    }

    private void saveFurnaceSession(FurnaceKey key, FurnaceSession session) {
        String path = "furnaces." + key.asPath();
        if (session == null || session.isExpired() || session.isEmpty()) {
            dataConfig.set(path, null);
            saveDataConfig();
            return;
        }

        dataConfig.set(path + ".owner", session.ownerId != null ? session.ownerId.toString() : null);
        dataConfig.set(path + ".ownerName", session.ownerName);
        dataConfig.set(path + ".lockedAt", session.ownerId != null ? session.lockedAt : null);
        dataConfig.set(path + ".pendingOwner", session.pendingOwnerId != null ? session.pendingOwnerId.toString() : null);
        dataConfig.set(path + ".pendingOwnerName", session.pendingOwnerName);
        dataConfig.set(path + ".pending-miner", null);
        dataConfig.set(path + ".pending-farmer", null);
        saveFurnacePendingMap(path + ".pending-miner", session.pendingMinerItems);
        saveFurnacePendingMap(path + ".pending-farmer", session.pendingFarmerItems);
        saveDataConfig();
    }

    private void saveFixedOreBlocks() {
        dataConfig.set("fixed-ores", null);
        for (Map.Entry<PlacedBlockKey, Material> entry : fixedOreBlocks.entrySet()) {
            dataConfig.set("fixed-ores." + entry.getKey().asPath(), entry.getValue().name());
        }
        saveDataConfig();
    }

    private void loadFurnacePendingMap(Map<UUID, Integer> target, String path) {
        ConfigurationSection pendingSection = dataConfig.getConfigurationSection(path);
        if (pendingSection == null) {
            return;
        }
        for (String pendingKey : pendingSection.getKeys(false)) {
            try {
                UUID contributorId = UUID.fromString(pendingKey);
                int amount = pendingSection.getInt(pendingKey, 0);
                if (amount > 0) {
                    target.put(contributorId, amount);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void saveFurnacePendingMap(String path, Map<UUID, Integer> values) {
        for (Map.Entry<UUID, Integer> entry : values.entrySet()) {
            dataConfig.set(path + "." + entry.getKey(), entry.getValue());
        }
    }

    private void processPendingHardRestartPhase() {
        HardRestartPhase phase = getHardRestartPhase();
        if (phase == null) {
            return;
        }

        switch (phase) {
            case RUN_SERVER_RELOAD -> getServer().getScheduler().runTaskLater(this, () -> {
                setHardRestartPhase(HardRestartPhase.RUN_TERRA_RELOAD);
                dispatchConsoleCommand("reload");
                dispatchConsoleCommand("reload confirm");
            }, 100L);
            case RUN_TERRA_RELOAD -> getServer().getScheduler().runTaskLater(this, () -> {
                clearHardRestartPhase();
                dispatchConsoleCommand("terra reload");
            }, 100L);
        }
    }

    private void dispatchConsoleCommand(String command) {
        ConsoleCommandSender console = getServer().getConsoleSender();
        getLogger().info("Executing post-hardrestart command: /" + command);
        getServer().dispatchCommand(console, command);
    }

    private HardRestartPhase getHardRestartPhase() {
        return HardRestartPhase.fromKey(dataConfig.getString(HARD_RESTART_PHASE_PATH));
    }

    private void setHardRestartPhase(HardRestartPhase phase) {
        dataConfig.set(HARD_RESTART_PHASE_PATH, phase.key);
        saveDataConfig();
    }

    private void clearHardRestartPhase() {
        dataConfig.set(HARD_RESTART_PHASE_PATH, null);
        saveDataConfig();
    }

    private void saveYaml(FileConfiguration configuration, File file) {
        try {
            configuration.save(file);
        } catch (IOException exception) {
            getLogger().severe("Failed to save " + file.getName() + ": " + exception.getMessage());
        }
    }

    public String colorize(String text) {
        return SECTION_SERIALIZER.serialize(legacyComponent(text));
    }

    private String expandHexColors(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        Matcher matcher = HEX_COLOR_PATTERN.matcher(text);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (int index = 0; index < hex.length(); index++) {
                replacement.append('&').append(hex.charAt(index));
            }
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    public String formatMaterialName(Material material) {
        return formatMaterialName(material, material.name());
    }

    private String formatMaterialName(Material material, String fallbackName) {
        String lower = (material != null ? material.name() : fallbackName).toLowerCase(Locale.ROOT).replace('_', ' ');
        String[] words = lower.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.toString();
    }

    private boolean isLumberjackWoodBlock(Material material) {
        return isLumberjackDecorativeWoodBlock(material)
                || Tag.LOGS.isTagged(material)
                || material.name().endsWith("_WOOD")
                || material.name().endsWith("_STEM")
                || material.name().endsWith("_HYPHAE");
    }

    private boolean isLumberjackDecorativeWoodBlock(Material material) {
        if (material == null) {
            return false;
        }

        String materialName = material.name();
        if (isNamedWoodVariant(materialName)) {
            return true;
        }

        return switch (material) {
            case CRAFTING_TABLE, CHEST, TRAPPED_CHEST, BARREL, BOOKSHELF, CHISELED_BOOKSHELF,
                 LECTERN, LOOM, CARTOGRAPHY_TABLE, FLETCHING_TABLE, SMITHING_TABLE,
                 COMPOSTER, NOTE_BLOCK, JUKEBOX, BEE_NEST, BEEHIVE, CAMPFIRE, SOUL_CAMPFIRE,
                 LADDER, OAK_SAPLING, SPRUCE_SAPLING, BIRCH_SAPLING, JUNGLE_SAPLING,
                 ACACIA_SAPLING, DARK_OAK_SAPLING, CHERRY_SAPLING, MANGROVE_PROPAGULE,
                 OAK_LEAVES, SPRUCE_LEAVES, BIRCH_LEAVES, JUNGLE_LEAVES, ACACIA_LEAVES,
                 DARK_OAK_LEAVES, CHERRY_LEAVES, MANGROVE_LEAVES, AZALEA_LEAVES,
                 FLOWERING_AZALEA_LEAVES -> true;
            default -> false;
        };
    }

    private boolean isNamedWoodVariant(String materialName) {
        if (materialName == null) {
            return false;
        }
        if (!(materialName.endsWith("_PLANKS")
                || materialName.endsWith("_STAIRS")
                || materialName.endsWith("_SLAB")
                || materialName.endsWith("_FENCE")
                || materialName.endsWith("_FENCE_GATE")
                || materialName.endsWith("_DOOR")
                || materialName.endsWith("_TRAPDOOR")
                || materialName.endsWith("_PRESSURE_PLATE")
                || materialName.endsWith("_BUTTON")
                || materialName.endsWith("_SIGN")
                || materialName.endsWith("_WALL_SIGN")
                || materialName.endsWith("_HANGING_SIGN")
                || materialName.endsWith("_WALL_HANGING_SIGN"))) {
            return false;
        }

        return materialName.startsWith("OAK_")
                || materialName.startsWith("SPRUCE_")
                || materialName.startsWith("BIRCH_")
                || materialName.startsWith("JUNGLE_")
                || materialName.startsWith("ACACIA_")
                || materialName.startsWith("DARK_OAK_")
                || materialName.startsWith("CHERRY_")
                || materialName.startsWith("MANGROVE_")
                || materialName.startsWith("BAMBOO_")
                || materialName.startsWith("CRIMSON_")
                || materialName.startsWith("WARPED_");
    }

    public Component legacyComponent(String text) {
        return AMPERSAND_SERIALIZER.deserialize(expandHexColors(text == null ? "" : text));
    }

    public String plainText(Component component) {
        return PLAIN_TEXT_SERIALIZER.serialize(component == null ? Component.empty() : component);
    }

    public String safeOfflineName(UUID playerId) {
        if (playerId == null) {
            return "none";
        }
        return safeOfflineName(getServer().getOfflinePlayer(playerId));
    }

    public String safeOfflineName(OfflinePlayer player) {
        return player.getName() != null ? player.getName() : player.getUniqueId().toString();
    }

    public void refreshAllCountryTags() {
        for (Player player : getServer().getOnlinePlayers()) {
            updatePlayerCountryTag(player);
        }
    }

    public void refreshCountryTags(Country country) {
        for (UUID memberId : country.getMembers()) {
            Player player = getServer().getPlayer(memberId);
            if (player != null) {
                updatePlayerCountryTag(player);
            }
        }
    }

    public void updatePlayerCountryTag(Player player) {
        Country country = getPlayerCountry(player.getUniqueId());
        if (country == null || !country.hasTag()) {
            resetPlayerCountryTag(player);
            return;
        }

        String nameWithTag = player.getName() + " " + country.getTag();
        Component decoratedName = legacyComponent(nameWithTag);
        player.displayName(decoratedName);
        player.playerListName(decoratedName);
    }

    public void resetPlayerCountryTag(Player player) {
        Component playerName = Component.text(player.getName());
        player.displayName(playerName);
        player.playerListName(playerName);
    }

    private void resetOnlinePlayerState(Player player) {
        player.closeInventory();
        clearPlayerInventory(player.getInventory());
        player.setLevel(0);
        player.setExp(0.0F);
        player.setTotalExperience(0);
        player.setFoodLevel(20);
        player.setSaturation(20.0F);
        player.setExhaustion(0.0F);
        player.setFireTicks(0);
        player.setFallDistance(0.0F);
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        }
        player.teleport(getDefaultSpawnLocation(player));
        resetPlayerCountryTag(player);
        getServer().getScheduler().runTaskLater(this, () -> {
            if (player.isOnline() && requiresProfessionSelection(player)) {
                startTutorialIntro(player, 0, true);
            }
        }, 1L);
    }

    private Location getDefaultSpawnLocation(Player player) {
        return getPrimarySpawnLocation(player);
    }

    private Location findSafeProfessionSelectionLocation(Player player) {
        Location baseLocation = getPrimarySpawnLocation(player);
        if (baseLocation == null || baseLocation.getWorld() == null) {
            return player != null ? player.getLocation() : null;
        }

        World world = baseLocation.getWorld();
        int baseX = baseLocation.getBlockX();
        int baseZ = baseLocation.getBlockZ();
        float yaw = baseLocation.getYaw();
        float pitch = baseLocation.getPitch();

        int[] offsets = {0, 1, -1, 2, -2, 3, -3};
        for (int xOffset : offsets) {
            for (int zOffset : offsets) {
                int x = baseX + xOffset;
                int z = baseZ + zOffset;
                int y = world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES) + 1;
                Location candidate = new Location(world, x + 0.5D, y, z + 0.5D, yaw, pitch);
                if (isSafeProfessionSelectionLocation(candidate)) {
                    return candidate;
                }
            }
        }

        return isSafeProfessionSelectionLocation(baseLocation) ? baseLocation : player.getLocation();
    }

    private boolean isSafeProfessionSelectionLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        Block feet = location.getBlock();
        Block head = feet.getRelative(BlockFace.UP);
        Block below = feet.getRelative(BlockFace.DOWN);
        if (feet.isLiquid() || head.isLiquid() || below.isLiquid()) {
            return false;
        }
        if (!feet.isPassable() || !head.isPassable()) {
            return false;
        }
        return below.getType().isSolid();
    }

    private boolean isUnsafeProfessionSelectionLocation(Location location) {
        return !isSafeProfessionSelectionLocation(location);
    }

    private boolean sameBlock(Location left, Location right) {
        return left != null
                && right != null
                && left.getWorld() != null
                && left.getWorld().equals(right.getWorld())
                && left.getBlockX() == right.getBlockX()
                && left.getBlockY() == right.getBlockY()
                && left.getBlockZ() == right.getBlockZ();
    }

    public boolean isPlaytestActive() {
        return playtestActive;
    }

    public boolean isPlaytestPreparing() {
        return playtestPreparing;
    }

    public boolean isPlaytestStopPending() {
        return playtestStopPending;
    }

    public boolean isPlaytestRestoring() {
        return false;
    }

    public long getConfiguredPlaytestDurationMillis() {
        return configuredPlaytestDurationMillis;
    }

    public void setConfiguredPlaytestDurationMillis(long durationMillis) {
        configuredPlaytestDurationMillis = Math.max(15L * 60L * 1000L, durationMillis);
        saveConfiguredPlaytestSettings();
    }

    public double getConfiguredPlaytestXpBoostMultiplier() {
        return configuredPlaytestXpBoostMultiplier;
    }

    public void setConfiguredPlaytestXpBoostMultiplier(double multiplier) {
        configuredPlaytestXpBoostMultiplier = sanitizePlaytestXpBoostMultiplier(multiplier);
        saveConfiguredPlaytestSettings();
    }

    public boolean isConfiguredPlaytestDaylightCycleEnabled() {
        return configuredPlaytestDaylightCycle;
    }

    public void setConfiguredPlaytestDaylightCycleEnabled(boolean enabled) {
        configuredPlaytestDaylightCycle = enabled;
        saveConfiguredPlaytestSettings();
    }

    public boolean isConfiguredPlaytestPvpEnabled() {
        return configuredPlaytestPvp;
    }

    public void setConfiguredPlaytestPvpEnabled(boolean enabled) {
        configuredPlaytestPvp = enabled;
        saveConfiguredPlaytestSettings();
    }

    public boolean isConfiguredPlaytestKeepCountryData() {
        return configuredPlaytestKeepCountryData;
    }

    public void setConfiguredPlaytestKeepCountryData(boolean enabled) {
        configuredPlaytestKeepCountryData = enabled;
        saveConfiguredPlaytestSettings();
    }

    public boolean isConfiguredPlaytestSavePlayerProgression() {
        return configuredPlaytestSavePlayerProgression;
    }

    public void setConfiguredPlaytestSavePlayerProgression(boolean enabled) {
        configuredPlaytestSavePlayerProgression = enabled;
        saveConfiguredPlaytestSettings();
    }

    public double getPlaytestXpBoostMultiplier() {
        return playtestActive || playtestPreparing ? playtestXpBoostMultiplier : configuredPlaytestXpBoostMultiplier;
    }

    public boolean isPlaytestDaylightCycleEnabled() {
        return playtestActive || playtestPreparing ? playtestDaylightCycleEnabled : configuredPlaytestDaylightCycle;
    }

    public boolean isPlaytestPvpEnabled() {
        return playtestActive || playtestPreparing ? playtestPvpEnabled : configuredPlaytestPvp;
    }

    public boolean isPlaytestKeepCountryData() {
        return playtestActive || playtestPreparing ? playtestKeepCountryData : configuredPlaytestKeepCountryData;
    }

    public boolean isPlaytestSavePlayerProgression() {
        return playtestActive || playtestPreparing ? playtestSavePlayerProgression : configuredPlaytestSavePlayerProgression;
    }

    public long getPlaytestRemainingMillis() {
        return playtestActive ? Math.max(0L, playtestEndMillis - System.currentTimeMillis()) : 0L;
    }

    public String getPlaytestStartedBy() {
        return playtestStartedBy;
    }

    public boolean startPlaytest(long durationMillis, String startedBy) {
        if (playtestPreparing || playtestActive || durationMillis <= 0L) {
            return false;
        }

        playtestPreparing = true;
        playtestStartedBy = startedBy;
        playtestDurationMillis = durationMillis;
        captureConfiguredPlaytestSessionSettings();
        playtestPrepareEndMillis = System.currentTimeMillis() + (PLAYTEST_START_COUNTDOWN_SECONDS * 1000L);
        announcedPlaytestReminders.clear();
        savePlaytestState();
        beginPlaytestCountdown(PLAYTEST_START_COUNTDOWN_SECONDS);
        return true;
    }

    public boolean stopPlaytestNow() {
        if (!playtestPreparing && !playtestActive) {
            return false;
        }

        if (playtestPreparing) {
            stopPlaytestTasks();
            playtestPreparing = false;
            playtestStopPending = false;
            playtestPrepareEndMillis = 0L;
            playtestStartedBy = null;
            playtestDurationMillis = 0L;
            resetPlaytestSessionSettings();
            announcedPlaytestReminders.clear();
            clearPlaytestState();
            return true;
        }

        if (!playtestStopPending) {
            beginPlaytestStopCountdown(PLAYTEST_STOP_COUNTDOWN_SECONDS);
        }
        return true;
    }

    public boolean extendPlaytest(long additionalMillis) {
        if (additionalMillis <= 0L || (!playtestPreparing && !playtestActive)) {
            return false;
        }

        playtestDurationMillis = saturatingAdd(playtestDurationMillis, additionalMillis);
        if (playtestActive) {
            playtestEndMillis = saturatingAdd(playtestEndMillis, additionalMillis);
        }
        savePlaytestState();
        return true;
    }

    public String getPlaytestActionBarText(long remainingMillis) {
        return colorize("&6PLAYTEST &8- &f" + formatPlaytestRemainingDuration(Math.max(0L, remainingMillis)) + " left");
    }

    private void beginPlaytestCountdown(long initialRemainingSeconds) {
        stopPlaytestTasks();
        playtestCountdownTask = getServer().getScheduler().runTaskTimer(this, new Runnable() {
            private long remaining = Math.max(0L, initialRemainingSeconds);

            @Override
            public void run() {
                if (remaining == 30 || remaining == 20 || remaining <= 10) {
                    Bukkit.broadcastMessage(getMessage("terra.playtest.countdown", placeholders(
                            "seconds", String.valueOf(remaining)
                    )));
                }
                if (remaining <= 0) {
                    if (playtestCountdownTask != null) {
                        playtestCountdownTask.cancel();
                        playtestCountdownTask = null;
                    }
                    activatePlaytest();
                    return;
                }
                remaining--;
            }
        }, 0L, 20L);
    }

    private void activatePlaytest() {
        playtestPreparing = false;
        playtestStopPending = false;
        playtestPrepareEndMillis = 0L;
        playtestActive = true;
        playtestEndMillis = System.currentTimeMillis() + playtestDurationMillis;
        announcedPlaytestReminders.clear();
        if (!playtestSavePlayerProgression) {
            resetAllProfessionDataForPlaytest();
        }
        applyPlaytestWorldSettings();
        savePlaytestState();

        for (Player player : getServer().getOnlinePlayers()) {
            resetOnlinePlayerState(player);
        }

        Bukkit.broadcastMessage(getMessage("terra.playtest.started", placeholders(
                "time", formatLongDurationWords(playtestDurationMillis),
                "player", playtestStartedBy != null ? playtestStartedBy : "Console"
        )));
        startPlaytestTicker();
    }

    private void finishPlaytest() {
        if (!playtestActive) {
            return;
        }

        playtestActive = false;
        playtestStopPending = false;
        stopPlaytestTasks();
        playtestPrepareEndMillis = 0L;
        playtestDurationMillis = 0L;
        playtestEndMillis = 0L;
        playtestStartedBy = null;
        announcedPlaytestReminders.clear();
        finalizePlaytestPlayers();
        restorePlaytestWorldSettings();
        resetPlaytestSessionSettings();
        clearPlaytestState();
        Bukkit.broadcastMessage(getMessage("terra.playtest.ended"));
    }

    private void beginPlaytestStopCountdown(long initialRemainingSeconds) {
        playtestStopPending = true;
        if (playtestTickTask != null) {
            playtestTickTask.cancel();
            playtestTickTask = null;
        }
        if (playtestCountdownTask != null) {
            playtestCountdownTask.cancel();
        }
        playtestCountdownTask = getServer().getScheduler().runTaskTimer(this, new Runnable() {
            private long remaining = Math.max(0L, initialRemainingSeconds);

            @Override
            public void run() {
                Bukkit.broadcastMessage(getMessage("terra.playtest.final-countdown", placeholders(
                        "seconds", String.valueOf(remaining),
                        "time", formatLongDurationWords(remaining * 1000L)
                )));
                if (remaining <= 0L) {
                    if (playtestCountdownTask != null) {
                        playtestCountdownTask.cancel();
                        playtestCountdownTask = null;
                    }
                    finishPlaytest();
                    return;
                }
                remaining--;
            }
        }, 0L, 20L);
    }

    private void startPlaytestTicker() {
        stopPlaytestTasks();
        playtestTickTask = getServer().getScheduler().runTaskTimer(this, () -> {
            if (!playtestActive) {
                return;
            }

            long remainingMillis = getPlaytestRemainingMillis();
            long remainingSeconds = Math.max(0L, (long) Math.ceil(remainingMillis / 1000.0D));

            if (remainingSeconds <= 0L) {
                finishPlaytest();
                return;
            }

            maybeBroadcastPlaytestReminder(remainingSeconds);
        }, 0L, 20L);
    }

    private void maybeBroadcastPlaytestReminder(long remainingSeconds) {
        List<Long> reminders = List.of(
                31536000L, 604800L, 86400L, 43200L, 21600L, 3600L, 1800L, 600L, 300L, 60L, 30L,
                10L, 9L, 8L, 7L, 6L, 5L, 4L, 3L, 2L, 1L
        );
        if (!reminders.contains(remainingSeconds) || !announcedPlaytestReminders.add(remainingSeconds)) {
            return;
        }

        String messageKey = remainingSeconds <= 10L
                ? "terra.playtest.final-countdown"
                : "terra.playtest.remaining";
        Bukkit.broadcastMessage(getMessage(messageKey, placeholders(
                "seconds", String.valueOf(remainingSeconds),
                "time", formatPlaytestRemainingDuration(remainingSeconds * 1000L)
        )));
    }

    private void stopPlaytestTasks() {
        if (playtestCountdownTask != null) {
            playtestCountdownTask.cancel();
            playtestCountdownTask = null;
        }
        if (playtestTickTask != null) {
            playtestTickTask.cancel();
            playtestTickTask = null;
        }
    }

    private void restorePlaytestState() {
        stopPlaytestTasks();
        announcedPlaytestReminders.clear();
        loadConfiguredPlaytestSettings();

        String phase = dataConfig.getString("playtest.phase", "inactive");
        playtestPreparing = false;
        playtestActive = false;
        playtestStopPending = false;
        playtestPrepareEndMillis = 0L;
        playtestEndMillis = 0L;
        playtestDurationMillis = 0L;
        playtestStartedBy = null;
        resetPlaytestSessionSettings();

        if (phase == null || phase.equalsIgnoreCase("inactive")) {
            clearPlaytestState();
            return;
        }

        playtestStartedBy = dataConfig.getString("playtest.started-by");
        playtestDurationMillis = Math.max(0L, dataConfig.getLong("playtest.duration", 0L));
        loadActivePlaytestSessionSettings();
        long now = System.currentTimeMillis();

        if (phase.equalsIgnoreCase("preparing")) {
            playtestPrepareEndMillis = dataConfig.getLong("playtest.prepare-end", 0L);
            long remainingSeconds = Math.max(0L, (long) Math.ceil((playtestPrepareEndMillis - now) / 1000.0D));
            if (playtestDurationMillis <= 0L) {
                clearPlaytestState();
                return;
            }
            if (remainingSeconds <= 0L) {
                activatePlaytest();
                return;
            }

            playtestPreparing = true;
            savePlaytestState();
            beginPlaytestCountdown(remainingSeconds);
            return;
        }

        if (phase.equalsIgnoreCase("active")) {
            playtestEndMillis = dataConfig.getLong("playtest.end", 0L);
            if (playtestDurationMillis <= 0L || playtestEndMillis <= now) {
                clearPlaytestState();
                return;
            }

            playtestActive = true;
            applyPlaytestWorldSettings();
            maybeRestorePlaytestPlayers();
            savePlaytestState();
            startPlaytestTicker();
            return;
        }

        clearPlaytestState();
    }

    private void maybeRestorePlaytestPlayers() {
        for (Player player : getServer().getOnlinePlayers()) {
            handleTutorialJoin(player);
            if (requiresProfessionSelection(player) && !isTutorialIntroActive(player.getUniqueId())) {
                openProfessionMenu(player);
            }
        }
    }

    private void savePlaytestState() {
        writeConfiguredPlaytestSettings();
        if (playtestPreparing) {
            dataConfig.set("playtest.phase", "preparing");
            dataConfig.set("playtest.started-by", playtestStartedBy);
            dataConfig.set("playtest.duration", playtestDurationMillis);
            dataConfig.set("playtest.prepare-end", playtestPrepareEndMillis);
            dataConfig.set("playtest.end", null);
            writeActivePlaytestSessionSettings();
            saveDataConfig();
            return;
        }

        if (playtestActive) {
            dataConfig.set("playtest.phase", "active");
            dataConfig.set("playtest.started-by", playtestStartedBy);
            dataConfig.set("playtest.duration", playtestDurationMillis);
            dataConfig.set("playtest.prepare-end", null);
            dataConfig.set("playtest.end", playtestEndMillis);
            writeActivePlaytestSessionSettings();
            saveDataConfig();
            return;
        }

        clearPlaytestState();
    }

    private void clearPlaytestState() {
        writeConfiguredPlaytestSettings();
        dataConfig.set("playtest.phase", null);
        dataConfig.set("playtest.started-by", null);
        dataConfig.set("playtest.duration", null);
        dataConfig.set("playtest.prepare-end", null);
        dataConfig.set("playtest.end", null);
        dataConfig.set("playtest.session", null);
        saveDataConfig();
    }

    private void captureConfiguredPlaytestSessionSettings() {
        playtestXpBoostMultiplier = sanitizePlaytestXpBoostMultiplier(configuredPlaytestXpBoostMultiplier);
        playtestDaylightCycleEnabled = configuredPlaytestDaylightCycle;
        playtestPvpEnabled = configuredPlaytestPvp;
        playtestKeepCountryData = configuredPlaytestKeepCountryData;
        playtestSavePlayerProgression = configuredPlaytestSavePlayerProgression;
    }

    private void resetPlaytestSessionSettings() {
        playtestXpBoostMultiplier = DEFAULT_PLAYTEST_XP_MULTIPLIER;
        playtestDaylightCycleEnabled = DEFAULT_PLAYTEST_DAYLIGHT_CYCLE;
        playtestPvpEnabled = DEFAULT_PLAYTEST_PVP;
        playtestKeepCountryData = DEFAULT_PLAYTEST_KEEP_COUNTRY_DATA;
        playtestSavePlayerProgression = DEFAULT_PLAYTEST_SAVE_PLAYER_PROGRESSION;
        playtestResumeRealTimeClock = false;
    }

    private void loadConfiguredPlaytestSettings() {
        configuredPlaytestDurationMillis = Math.max(15L * 60L * 1000L,
                dataConfig.getLong("playtest.settings.duration", DEFAULT_PLAYTEST_DURATION_MILLIS));
        configuredPlaytestXpBoostMultiplier = sanitizePlaytestXpBoostMultiplier(
                dataConfig.getDouble("playtest.settings.xp-multiplier", DEFAULT_PLAYTEST_XP_MULTIPLIER));
        configuredPlaytestDaylightCycle = dataConfig.getBoolean("playtest.settings.daylight-cycle", DEFAULT_PLAYTEST_DAYLIGHT_CYCLE);
        configuredPlaytestPvp = dataConfig.getBoolean("playtest.settings.pvp", DEFAULT_PLAYTEST_PVP);
        configuredPlaytestKeepCountryData = dataConfig.getBoolean("playtest.settings.keep-country-data", DEFAULT_PLAYTEST_KEEP_COUNTRY_DATA);
        configuredPlaytestSavePlayerProgression = dataConfig.getBoolean("playtest.settings.save-player-progression", DEFAULT_PLAYTEST_SAVE_PLAYER_PROGRESSION);
    }

    private void saveConfiguredPlaytestSettings() {
        writeConfiguredPlaytestSettings();
        saveDataConfig();
    }

    private void writeConfiguredPlaytestSettings() {
        dataConfig.set("playtest.settings.duration", configuredPlaytestDurationMillis);
        dataConfig.set("playtest.settings.xp-multiplier", configuredPlaytestXpBoostMultiplier);
        dataConfig.set("playtest.settings.daylight-cycle", configuredPlaytestDaylightCycle);
        dataConfig.set("playtest.settings.pvp", configuredPlaytestPvp);
        dataConfig.set("playtest.settings.keep-country-data", configuredPlaytestKeepCountryData);
        dataConfig.set("playtest.settings.save-player-progression", configuredPlaytestSavePlayerProgression);
    }

    private void loadActivePlaytestSessionSettings() {
        playtestXpBoostMultiplier = sanitizePlaytestXpBoostMultiplier(
                dataConfig.getDouble("playtest.session.xp-multiplier", configuredPlaytestXpBoostMultiplier));
        playtestDaylightCycleEnabled = dataConfig.getBoolean("playtest.session.daylight-cycle", configuredPlaytestDaylightCycle);
        playtestPvpEnabled = dataConfig.getBoolean("playtest.session.pvp", configuredPlaytestPvp);
        playtestKeepCountryData = dataConfig.getBoolean("playtest.session.keep-country-data", configuredPlaytestKeepCountryData);
        playtestSavePlayerProgression = dataConfig.getBoolean("playtest.session.save-player-progression", configuredPlaytestSavePlayerProgression);
    }

    private void writeActivePlaytestSessionSettings() {
        dataConfig.set("playtest.session.xp-multiplier", playtestXpBoostMultiplier);
        dataConfig.set("playtest.session.daylight-cycle", playtestDaylightCycleEnabled);
        dataConfig.set("playtest.session.pvp", playtestPvpEnabled);
        dataConfig.set("playtest.session.keep-country-data", playtestKeepCountryData);
        dataConfig.set("playtest.session.save-player-progression", playtestSavePlayerProgression);
    }

    private double sanitizePlaytestXpBoostMultiplier(double multiplier) {
        if (Double.isNaN(multiplier) || Double.isInfinite(multiplier)) {
            return DEFAULT_PLAYTEST_XP_MULTIPLIER;
        }
        return Math.max(1.0D, Math.min(5.0D, multiplier));
    }

    private void applyPlaytestWorldSettings() {
        restorePlaytestWorldSettings();
        playtestResumeRealTimeClock = getConfig().getBoolean("realtime-clock.enabled", false);
        if (playtestResumeRealTimeClock) {
            stopRealTimeClock();
        }

        playtestDaylightCycleStates.clear();
        playtestPvpStates.clear();
        for (World world : getServer().getWorlds()) {
            Boolean current = world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE);
            playtestDaylightCycleStates.put(world.getName(), current == null || current);
            playtestPvpStates.put(world.getName(), world.getPVP());
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, playtestDaylightCycleEnabled);
            world.setPVP(playtestPvpEnabled);
        }
    }

    private void restorePlaytestWorldSettings() {
        for (Map.Entry<String, Boolean> entry : playtestDaylightCycleStates.entrySet()) {
            World world = getServer().getWorld(entry.getKey());
            if (world != null) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, entry.getValue());
            }
        }
        playtestDaylightCycleStates.clear();
        for (Map.Entry<String, Boolean> entry : playtestPvpStates.entrySet()) {
            World world = getServer().getWorld(entry.getKey());
            if (world != null) {
                world.setPVP(entry.getValue());
            }
        }
        playtestPvpStates.clear();

        if (playtestResumeRealTimeClock) {
            restartRealTimeClock();
        }
        playtestResumeRealTimeClock = false;
    }

    private void resetAllProfessionDataForPlaytest() {
        playerProfessions.clear();
        secondaryProfessions.clear();
        activeProfessions.clear();
        secondaryProfessionUnlockOverrides.clear();
        developmentModeProfessions.clear();
        professionProgress.clear();
        pendingStarterKitGrants.clear();
        pendingFractionalProfessionXp.clear();
        dataConfig.set("professions", null);
        dataConfig.set("profession-progress", null);
        dataConfig.set("starter-kits.pending", null);
        saveDataConfig();
    }

    private void finalizePlaytestPlayers() {
        List<Player> onlinePlayers = new ArrayList<>(getServer().getOnlinePlayers());
        for (Player player : onlinePlayers) {
            teleportPlayerForPlaytestFinalization(player);
        }

        for (Player player : onlinePlayers) {
            clearOnlinePlayerPlaytestItemsAndExperience(player);
        }

        boolean clearProgression = !playtestSavePlayerProgression;
        boolean clearCountryData = !playtestKeepCountryData;
        for (UUID playerId : collectTrackedPlayerIdsForPlaytestFinalization(onlinePlayers)) {
            clearPlayerPlaytestData(getServer().getOfflinePlayer(playerId), clearProgression, clearCountryData);
        }

        if (clearCountryData) {
            for (Country country : new ArrayList<>(countriesByKey.values())) {
                disbandCountry(country);
            }
            pendingCountryTransfers.clear();
            saveDataConfig();
        }

        for (Player player : onlinePlayers) {
            completeOnlinePlayerPlaytestFinalization(player);
        }
    }

    private Set<UUID> collectTrackedPlayerIdsForPlaytestFinalization(List<Player> onlinePlayers) {
        Set<UUID> playerIds = new LinkedHashSet<>();
        for (Player player : onlinePlayers) {
            playerIds.add(player.getUniqueId());
        }
        playerIds.addAll(breakCooldowns.keySet());
        playerIds.addAll(placeCooldowns.keySet());
        playerIds.addAll(breakCooldownMessageTimes.keySet());
        playerIds.addAll(placeCooldownMessageTimes.keySet());
        playerIds.addAll(bypassEntries.keySet());
        playerIds.addAll(playerCountries.keySet());
        playerIds.addAll(lastTerritoryCountries.keySet());
        playerIds.addAll(countryHomeCooldowns.keySet());
        playerIds.addAll(playerProfessions.keySet());
        playerIds.addAll(secondaryProfessions.keySet());
        playerIds.addAll(activeProfessions.keySet());
        playerIds.addAll(secondaryProfessionUnlockOverrides.keySet());
        playerIds.addAll(developmentModeProfessions.keySet());
        playerIds.addAll(professionProgress.keySet());
        playerIds.addAll(pendingFractionalProfessionXp.keySet());
        playerIds.addAll(pendingStarterKitGrants.keySet());
        playerIds.addAll(staffModeGamemodes.keySet());
        playerIds.addAll(staffModeInventories.keySet());
        for (UUID ownerId : placedBlocks.values()) {
            if (ownerId != null) {
                playerIds.add(ownerId);
            }
        }
        for (Country country : countriesByKey.values()) {
            if (country.getOwnerId() != null) {
                playerIds.add(country.getOwnerId());
            }
            playerIds.addAll(country.getMembers());
            playerIds.addAll(country.getInvitedPlayers());
        }
        for (Map.Entry<UUID, CountryTransferRequest> entry : pendingCountryTransfers.entrySet()) {
            playerIds.add(entry.getKey());
            CountryTransferRequest request = entry.getValue();
            if (request != null) {
                if (request.currentOwnerId() != null) {
                    playerIds.add(request.currentOwnerId());
                }
                if (request.targetPlayerId() != null) {
                    playerIds.add(request.targetPlayerId());
                }
            }
        }
        return playerIds;
    }

    private void teleportPlayerForPlaytestFinalization(Player player) {
        if (player == null) {
            return;
        }
        player.closeInventory();
        if (isInStaffMode(player.getUniqueId())) {
            clearStaffModeForReset(player);
        }
        player.teleport(getDefaultSpawnLocation(player));
    }

    private void clearOnlinePlayerPlaytestItemsAndExperience(Player player) {
        if (player == null) {
            return;
        }
        clearPlayerInventory(player.getInventory());
        player.setLevel(0);
        player.setExp(0.0F);
        player.setTotalExperience(0);
        player.updateInventory();
    }

    private void completeOnlinePlayerPlaytestFinalization(Player player) {
        if (player == null) {
            return;
        }
        player.setFoodLevel(20);
        player.setSaturation(20.0F);
        player.setExhaustion(0.0F);
        player.setFireTicks(0);
        player.setFallDistance(0.0F);
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        }
        resetPlayerCountryTag(player);
        getServer().getScheduler().runTaskLater(this, () -> {
            if (player.isOnline()
                    && requiresProfessionSelection(player)
                    && !isTutorialIntroActive(player.getUniqueId())) {
                openProfessionMenu(player);
            }
        }, 1L);
    }

    public String formatLongDurationWords(long millis) {
        long totalSeconds = Math.max(1L, (long) Math.ceil(millis / 1000.0D));
        long years = totalSeconds / 31536000L;
        totalSeconds %= 31536000L;
        long weeks = totalSeconds / 604800L;
        totalSeconds %= 604800L;
        long days = totalSeconds / 86400L;
        totalSeconds %= 86400L;
        long hours = totalSeconds / 3600L;
        totalSeconds %= 3600L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;

        List<String> parts = new ArrayList<>();
        if (years > 0) {
            parts.add(years + "y");
        }
        if (weeks > 0) {
            parts.add(weeks + "w");
        }
        if (days > 0) {
            parts.add(days + "d");
        }
        if (hours > 0) {
            parts.add(hours + "h");
        }
        if (minutes > 0) {
            parts.add(minutes + "m");
        }
        if (seconds > 0 && parts.size() < 3) {
            parts.add(seconds + "s");
        }
        return parts.isEmpty() ? "0s" : String.join(" ", parts);
    }

    private long saturatingAdd(long left, long right) {
        if (right > 0L && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        if (right < 0L && left < Long.MIN_VALUE - right) {
            return Long.MIN_VALUE;
        }
        return left + right;
    }

    private void clearPlayerFurnaceData(UUID playerId) {
        for (Map.Entry<FurnaceKey, FurnaceSession> entry : new ArrayList<>(furnaceSessions.entrySet())) {
            FurnaceKey key = entry.getKey();
            FurnaceSession session = entry.getValue();

            if (playerId.equals(session.ownerId)) {
                session.ownerId = null;
                session.ownerName = null;
                session.lockedAt = 0L;
            }
            if (playerId.equals(session.pendingOwnerId)) {
                session.pendingOwnerId = null;
                session.pendingOwnerName = null;
            }

            session.pendingMinerItems.remove(playerId);
            session.pendingFarmerItems.remove(playerId);

            if (session.isEmpty()) {
                furnaceSessions.remove(key);
                dataConfig.set("furnaces." + key.asPath(), null);
            } else {
                saveFurnaceSession(key, session);
            }
        }
    }

    private static final class StaffInventoryState {
        private final ItemStack[] contents;
        private final ItemStack[] armorContents;
        private final ItemStack[] extraContents;
        private final ItemStack offHand;
        private final int heldItemSlot;

        private StaffInventoryState(
                ItemStack[] contents,
                ItemStack[] armorContents,
                ItemStack[] extraContents,
                ItemStack offHand,
                int heldItemSlot
        ) {
            this.contents = contents;
            this.armorContents = armorContents;
            this.extraContents = extraContents;
            this.offHand = offHand;
            this.heldItemSlot = heldItemSlot;
        }

        private static StaffInventoryState capture(PlayerInventory inventory) {
            return new StaffInventoryState(
                    cloneContents(inventory.getContents()),
                    cloneContents(inventory.getArmorContents()),
                    cloneContents(inventory.getExtraContents()),
                    cloneItem(inventory.getItemInOffHand()),
                    inventory.getHeldItemSlot()
            );
        }

        private void restore(PlayerInventory inventory) {
            inventory.setContents(cloneContents(contents));
            inventory.setArmorContents(cloneContents(armorContents));
            inventory.setExtraContents(cloneContents(extraContents));
            inventory.setItemInOffHand(cloneItem(offHand));
            inventory.setHeldItemSlot(heldItemSlot);
        }

        private static ItemStack[] cloneContents(ItemStack[] items) {
            if (items == null) {
                return null;
            }

            ItemStack[] clone = new ItemStack[items.length];
            for (int i = 0; i < items.length; i++) {
                clone[i] = cloneItem(items[i]);
            }
            return clone;
        }

        private static ItemStack cloneItem(ItemStack itemStack) {
            return itemStack == null ? null : itemStack.clone();
        }
    }

    public record BlacksmithRecipe(
            Material result,
            int amount,
            int level,
            int xp,
            String category,
            int slot,
            LinkedHashMap<Material, Integer> ingredients
    ) {
    }

    private enum HardRestartPhase {
        RUN_SERVER_RELOAD("run_server_reload"),
        RUN_TERRA_RELOAD("run_terra_reload");

        private final String key;

        HardRestartPhase(String key) {
            this.key = key;
        }

        private static HardRestartPhase fromKey(String key) {
            if (key == null || key.isBlank()) {
                return null;
            }

            for (HardRestartPhase phase : values()) {
                if (phase.key.equalsIgnoreCase(key)) {
                    return phase;
                }
            }
            return null;
        }
    }

    public enum FurnaceAccessResult {
        UNLOCKED,
        ACQUIRED,
        OWNER,
        LOCKED_OTHER,
        EXPIRED
    }

    private enum StabilityMaterialClass {
        LOOSE,
        PACKED_SOIL,
        SOFT_ROCK,
        FRAGILE,
        HARD_ROCK,
        STABLE
    }

    private enum StabilityFailureMode {
        ROOF,
        SHAFT,
        FLOATING
    }

    private enum StabilityDebugType {
        FAIL_FLOATING,
        FAIL_SHAFT,
        FAIL_ROOF,
        SUPPORT,
        SUPPORTED
    }

    private record StabilityClusterCandidate(List<Block> blocks, StabilityMaterialClass materialClass) {
    }

    private record StabilityDebugDisplayState(UUID entityId, StabilityDebugType type) {
    }

    private static final class StabilityCollapse {
        private final UUID collapseId;
        private final UUID worldId;
        private List<PlacedBlockKey> blockKeys;
        private final StabilityMaterialClass materialClass;
        private final StabilityFailureMode failureMode;
        private final Location center;
        private BukkitTask warningTask;
        private BukkitTask executeTask;
        private BukkitTask collapseTask;
        private int currentIndex;
        private int spawnedFallingBlocks;
        private int spawnedRubbleBlocks;

        private StabilityCollapse(UUID collapseId, UUID worldId, List<PlacedBlockKey> blockKeys, StabilityMaterialClass materialClass, StabilityFailureMode failureMode, Location center) {
            this.collapseId = collapseId;
            this.worldId = worldId;
            this.blockKeys = blockKeys;
            this.materialClass = materialClass;
            this.failureMode = failureMode;
            this.center = center;
        }
    }

    private static final class PlacedBlockKey {
        private final UUID worldId;
        private final int x;
        private final int y;
        private final int z;

        private PlacedBlockKey(UUID worldId, int x, int y, int z) {
            this.worldId = worldId;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private static PlacedBlockKey from(Block block) {
            return new PlacedBlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }

        private String asPath() {
            return worldId + "_" + x + "_" + y + "_" + z;
        }

        private static PlacedBlockKey fromPath(String path) {
            String[] parts = path.split("_");
            if (parts.length < 4) {
                return null;
            }
            try {
                UUID worldId = UUID.fromString(parts[0]);
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);
                return new PlacedBlockKey(worldId, x, y, z);
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof PlacedBlockKey other)) {
                return false;
            }
            return x == other.x && y == other.y && z == other.z && worldId.equals(other.worldId);
        }

        @Override
        public int hashCode() {
            int result = worldId.hashCode();
            result = 31 * result + x;
            result = 31 * result + y;
            result = 31 * result + z;
            return result;
        }
    }

    private static final class FurnaceKey {
        private final UUID worldId;
        private final int x;
        private final int y;
        private final int z;

        private FurnaceKey(UUID worldId, int x, int y, int z) {
            this.worldId = worldId;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private static FurnaceKey from(Location location) {
            return new FurnaceKey(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }

        private String asPath() {
            return worldId + "_" + x + "_" + y + "_" + z;
        }

        private static FurnaceKey fromPath(String path) {
            String[] parts = path.split("_");
            if (parts.length < 4) {
                return null;
            }
            try {
                UUID worldId = UUID.fromString(parts[0]);
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);
                return new FurnaceKey(worldId, x, y, z);
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof FurnaceKey other)) {
                return false;
            }
            return x == other.x && y == other.y && z == other.z && worldId.equals(other.worldId);
        }

        @Override
        public int hashCode() {
            int result = worldId.hashCode();
            result = 31 * result + x;
            result = 31 * result + y;
            result = 31 * result + z;
            return result;
        }
    }

    private static final class FurnaceSession {
        private UUID ownerId;
        private String ownerName;
        private long lockedAt;
        private UUID pendingOwnerId;
        private String pendingOwnerName;
        private final LinkedHashMap<UUID, Integer> pendingMinerItems = new LinkedHashMap<>();
        private final LinkedHashMap<UUID, Integer> pendingFarmerItems = new LinkedHashMap<>();

        private FurnaceSession() {
        }

        private boolean isExpired() {
            return ownerId != null && System.currentTimeMillis() - lockedAt >= FURNACE_LOCK_DURATION_MILLIS;
        }

        private boolean isEmpty() {
            return ownerId == null
                    && pendingOwnerId == null
                    && pendingMinerItems.isEmpty()
                    && pendingFarmerItems.isEmpty();
        }

        private UUID getAssignedOwnerId() {
            return ownerId != null ? ownerId : pendingOwnerId;
        }

        private String getAssignedOwnerName() {
            return ownerName != null && !ownerName.isBlank() ? ownerName : pendingOwnerName;
        }

        private LinkedHashMap<UUID, Integer> getPendingMap(Profession profession) {
            return switch (profession) {
                case MINER -> pendingMinerItems;
                case FARMER -> pendingFarmerItems;
                default -> pendingMinerItems;
            };
        }
    }

    private TerritoryService createTerritoryService() {
        if (!getConfig().getBoolean("territories.enabled", true)) {
            return new NoOpTerritoryService("integration-disabled");
        }
        if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            getLogger().warning("WorldGuard was not found. Country territory integration is disabled.");
            return new NoOpTerritoryService("worldguard-missing");
        }

        try {
            return new WorldGuardTerritoryService(this);
        } catch (NoClassDefFoundError error) {
            getLogger().warning("WorldGuard classes could not be loaded. Country territory integration is disabled.");
            return new NoOpTerritoryService("worldguard-missing");
        }
    }

    private void playConfiguredSound(Player player, String path, Sound fallback) {
        playConfiguredSound(
                player,
                path,
                "territories.title-notifications.sound-volume",
                "territories.title-notifications.sound-pitch",
                fallback
        );
    }

    private void playConfiguredSound(Player player, String soundPath, String volumePath, String pitchPath, Sound fallback) {
        String configured = getConfig().getString(soundPath, fallback.name());
        Sound sound;
        try {
            sound = Sound.valueOf(configured.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            sound = fallback;
        }

        float volume = (float) getConfig().getDouble(volumePath, 1.0D);
        float pitch = (float) getConfig().getDouble(pitchPath, 1.0D);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private void restartRealTimeClock() {
        stopRealTimeClock();
        if (!getConfig().getBoolean("realtime-clock.enabled", false)) {
            return;
        }

        long interval = Math.max(1L, getConfig().getLong("realtime-clock.update-interval-ticks", 2400L));
        applyRealTimeToConfiguredWorlds();
        realTimeClockTask = getServer().getScheduler().runTaskTimer(this, this::applyRealTimeToConfiguredWorlds, interval, interval);
    }

    private void stopRealTimeClock() {
        if (realTimeClockTask != null) {
            realTimeClockTask.cancel();
            realTimeClockTask = null;
        }

        for (Map.Entry<String, Boolean> entry : daylightCycleStates.entrySet()) {
            World world = getServer().getWorld(entry.getKey());
            if (world != null) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, entry.getValue());
            }
        }
        daylightCycleStates.clear();
    }

    public boolean isRealTimeClockEnabled() {
        return getConfig().getBoolean("realtime-clock.enabled", false);
    }

    public void setRealTimeClockEnabled(boolean enabled) {
        setManagedConfigValue("realtime-clock.enabled", enabled);
        if (enabled) {
            restartRealTimeClock();
        } else {
            stopRealTimeClock();
        }
    }

    public void syncRealTimeClockNow() {
        applyRealTimeToConfiguredWorlds();
    }

    public String getRealTimeClockTimezoneId() {
        return getConfig().getString("realtime-clock.timezone", "Europe/Amsterdam");
    }

    public List<String> getRealTimeClockWorldNames() {
        return getTargetClockWorlds().stream()
                .map(World::getName)
                .toList();
    }

    public long getCurrentRealTimeClockTicks() {
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(getRealTimeClockTimezoneId());
        } catch (Exception exception) {
            zoneId = ZoneId.of("Europe/Amsterdam");
        }
        return toMinecraftTime(ZonedDateTime.now(zoneId));
    }

    private void applyRealTimeToConfiguredWorlds() {
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(getConfig().getString("realtime-clock.timezone", "Europe/Amsterdam"));
        } catch (Exception exception) {
            zoneId = ZoneId.of("Europe/Amsterdam");
        }

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        long fullMinecraftTime = toMinecraftFullTime(now);
        long minecraftTime = Math.floorMod(fullMinecraftTime, 24000L);
        boolean freezeDaylightCycle = getConfig().getBoolean("realtime-clock.freeze-daylight-cycle", true);

        for (World world : getTargetClockWorlds()) {
            if (freezeDaylightCycle) {
                daylightCycleStates.computeIfAbsent(world.getName(), ignored -> {
                    Boolean current = world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE);
                    return current == null || current;
                });
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            } else if (daylightCycleStates.containsKey(world.getName())) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, daylightCycleStates.remove(world.getName()));
            }
            world.setFullTime(fullMinecraftTime);
            world.setTime(minecraftTime);
        }
    }

    private List<World> getTargetClockWorlds() {
        List<String> configuredWorlds = getConfig().getStringList("realtime-clock.worlds");
        if (configuredWorlds.isEmpty()) {
            return new ArrayList<>(getServer().getWorlds());
        }

        List<World> worlds = new ArrayList<>();
        for (String worldName : configuredWorlds) {
            World world = getServer().getWorld(worldName);
            if (world != null) {
                worlds.add(world);
            }
        }
        return worlds;
    }

    private long toMinecraftTime(ZonedDateTime dateTime) {
        long millisOfDay = (dateTime.getHour() * 3_600_000L)
                + (dateTime.getMinute() * 60_000L)
                + (dateTime.getSecond() * 1_000L)
                + (dateTime.getNano() / 1_000_000L);
        double dayProgress = millisOfDay / 86_400_000.0D;
        long ticks = Math.round(dayProgress * 24000.0D);
        return Math.floorMod(ticks + 18000L, 24000L);
    }

    private long toMinecraftFullTime(ZonedDateTime dateTime) {
        long epochDay = dateTime.toLocalDate().toEpochDay();
        return (epochDay * 24000L) + toMinecraftTime(dateTime);
    }

    public record StarterKitEntry(Material material, int amount) {
    }
}
