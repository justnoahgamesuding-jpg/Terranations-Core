package me.meetrow.testproject;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class Country {
    private String name;
    private UUID ownerId;
    private boolean open;
    private boolean systemCountry;
    private boolean hiddenFromPlayers;
    private String tag;
    private String territoryWorld;
    private String territoryRegionId;
    private String homeWorld;
    private double homeX;
    private double homeY;
    private double homeZ;
    private float homeYaw;
    private float homePitch;
    private String traderSpawnWorld;
    private double traderSpawnX;
    private double traderSpawnY;
    private double traderSpawnZ;
    private float traderSpawnYaw;
    private float traderSpawnPitch;
    private String lastTraderName;
    private String lastTraderSpecialty;
    private long lastTraderSeenAtMillis;
    private String ownerGuildId;
    private long nextUpkeepAtMillis;
    private double unpaidUpkeepDebt;
    private long reclaimAvailableAtMillis;
    private int level;
    private double treasuryBalance;
    private int resourceStockpile;
    private String activeBoostKey;
    private long activeBoostUntilMillis;
    private final Set<UUID> members;
    private final Set<UUID> coOwners;
    private final Set<UUID> stewards;
    private final Set<UUID> invitedPlayers;
    private final Set<String> allowedTradeCountries;
    private final Set<String> unlockedUpgradeKeys;

    public Country(String name, UUID ownerId, boolean open, boolean systemCountry, boolean hiddenFromPlayers,
                   String tag, String territoryWorld, String territoryRegionId,
                   String homeWorld, double homeX, double homeY, double homeZ, float homeYaw, float homePitch,
                   Set<UUID> members, Set<UUID> coOwners, Set<UUID> stewards, Set<UUID> invitedPlayers,
                   String traderSpawnWorld, double traderSpawnX, double traderSpawnY, double traderSpawnZ,
                   float traderSpawnYaw, float traderSpawnPitch, Set<String> allowedTradeCountries,
                   String lastTraderName, String lastTraderSpecialty, long lastTraderSeenAtMillis,
                   String ownerGuildId, long nextUpkeepAtMillis, double unpaidUpkeepDebt, long reclaimAvailableAtMillis, int level,
                   double treasuryBalance, int resourceStockpile, String activeBoostKey, long activeBoostUntilMillis,
                   Set<String> unlockedUpgradeKeys) {
        this.name = name;
        this.ownerId = ownerId;
        this.open = open;
        this.systemCountry = systemCountry;
        this.hiddenFromPlayers = hiddenFromPlayers;
        this.tag = tag;
        this.territoryWorld = territoryWorld;
        this.territoryRegionId = territoryRegionId;
        this.homeWorld = homeWorld;
        this.homeX = homeX;
        this.homeY = homeY;
        this.homeZ = homeZ;
        this.homeYaw = homeYaw;
        this.homePitch = homePitch;
        this.traderSpawnWorld = traderSpawnWorld;
        this.traderSpawnX = traderSpawnX;
        this.traderSpawnY = traderSpawnY;
        this.traderSpawnZ = traderSpawnZ;
        this.traderSpawnYaw = traderSpawnYaw;
        this.traderSpawnPitch = traderSpawnPitch;
        this.lastTraderName = lastTraderName;
        this.lastTraderSpecialty = lastTraderSpecialty;
        this.lastTraderSeenAtMillis = lastTraderSeenAtMillis;
        this.ownerGuildId = ownerGuildId;
        this.nextUpkeepAtMillis = Math.max(0L, nextUpkeepAtMillis);
        this.unpaidUpkeepDebt = unpaidUpkeepDebt;
        this.reclaimAvailableAtMillis = Math.max(0L, reclaimAvailableAtMillis);
        this.level = Math.max(1, level);
        this.treasuryBalance = treasuryBalance;
        this.resourceStockpile = resourceStockpile;
        this.activeBoostKey = activeBoostKey;
        this.activeBoostUntilMillis = activeBoostUntilMillis;
        this.members = new LinkedHashSet<>(members);
        this.coOwners = new LinkedHashSet<>(coOwners);
        this.stewards = new LinkedHashSet<>(stewards);
        this.invitedPlayers = new LinkedHashSet<>(invitedPlayers);
        this.allowedTradeCountries = new LinkedHashSet<>(allowedTradeCountries);
        this.unlockedUpgradeKeys = new LinkedHashSet<>(unlockedUpgradeKeys);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public boolean hasOwner() {
        return ownerId != null;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isSystemCountry() {
        return systemCountry;
    }

    public void setSystemCountry(boolean systemCountry) {
        this.systemCountry = systemCountry;
    }

    public boolean isHiddenFromPlayers() {
        return hiddenFromPlayers;
    }

    public void setHiddenFromPlayers(boolean hiddenFromPlayers) {
        this.hiddenFromPlayers = hiddenFromPlayers;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean hasTag() {
        return tag != null && !tag.isBlank();
    }

    public String getTerritoryWorld() {
        return territoryWorld;
    }

    public void setTerritoryWorld(String territoryWorld) {
        this.territoryWorld = territoryWorld;
    }

    public String getTerritoryRegionId() {
        return territoryRegionId;
    }

    public void setTerritoryRegionId(String territoryRegionId) {
        this.territoryRegionId = territoryRegionId;
    }

    public boolean hasTerritory() {
        return territoryWorld != null && !territoryWorld.isBlank() && territoryRegionId != null && !territoryRegionId.isBlank();
    }

    public String getHomeWorld() {
        return homeWorld;
    }

    public void setHomeWorld(String homeWorld) {
        this.homeWorld = homeWorld;
    }

    public double getHomeX() {
        return homeX;
    }

    public void setHomeX(double homeX) {
        this.homeX = homeX;
    }

    public double getHomeY() {
        return homeY;
    }

    public void setHomeY(double homeY) {
        this.homeY = homeY;
    }

    public double getHomeZ() {
        return homeZ;
    }

    public void setHomeZ(double homeZ) {
        this.homeZ = homeZ;
    }

    public float getHomeYaw() {
        return homeYaw;
    }

    public void setHomeYaw(float homeYaw) {
        this.homeYaw = homeYaw;
    }

    public float getHomePitch() {
        return homePitch;
    }

    public void setHomePitch(float homePitch) {
        this.homePitch = homePitch;
    }

    public boolean hasHome() {
        return homeWorld != null && !homeWorld.isBlank();
    }

    public String getTraderSpawnWorld() {
        return traderSpawnWorld;
    }

    public void setTraderSpawnWorld(String traderSpawnWorld) {
        this.traderSpawnWorld = traderSpawnWorld;
    }

    public double getTraderSpawnX() {
        return traderSpawnX;
    }

    public void setTraderSpawnX(double traderSpawnX) {
        this.traderSpawnX = traderSpawnX;
    }

    public double getTraderSpawnY() {
        return traderSpawnY;
    }

    public void setTraderSpawnY(double traderSpawnY) {
        this.traderSpawnY = traderSpawnY;
    }

    public double getTraderSpawnZ() {
        return traderSpawnZ;
    }

    public void setTraderSpawnZ(double traderSpawnZ) {
        this.traderSpawnZ = traderSpawnZ;
    }

    public float getTraderSpawnYaw() {
        return traderSpawnYaw;
    }

    public void setTraderSpawnYaw(float traderSpawnYaw) {
        this.traderSpawnYaw = traderSpawnYaw;
    }

    public float getTraderSpawnPitch() {
        return traderSpawnPitch;
    }

    public void setTraderSpawnPitch(float traderSpawnPitch) {
        this.traderSpawnPitch = traderSpawnPitch;
    }

    public boolean hasTraderSpawn() {
        return traderSpawnWorld != null && !traderSpawnWorld.isBlank();
    }

    public String getLastTraderName() {
        return lastTraderName;
    }

    public void setLastTraderName(String lastTraderName) {
        this.lastTraderName = lastTraderName;
    }

    public String getLastTraderSpecialty() {
        return lastTraderSpecialty;
    }

    public void setLastTraderSpecialty(String lastTraderSpecialty) {
        this.lastTraderSpecialty = lastTraderSpecialty;
    }

    public long getLastTraderSeenAtMillis() {
        return lastTraderSeenAtMillis;
    }

    public void setLastTraderSeenAtMillis(long lastTraderSeenAtMillis) {
        this.lastTraderSeenAtMillis = lastTraderSeenAtMillis;
    }

    public String getOwnerGuildId() {
        return ownerGuildId;
    }

    public void setOwnerGuildId(String ownerGuildId) {
        this.ownerGuildId = ownerGuildId;
    }

    public long getNextUpkeepAtMillis() {
        return nextUpkeepAtMillis;
    }

    public void setNextUpkeepAtMillis(long nextUpkeepAtMillis) {
        this.nextUpkeepAtMillis = Math.max(0L, nextUpkeepAtMillis);
    }

    public double getUnpaidUpkeepDebt() {
        return unpaidUpkeepDebt;
    }

    public void setUnpaidUpkeepDebt(double unpaidUpkeepDebt) {
        this.unpaidUpkeepDebt = unpaidUpkeepDebt;
    }

    public long getReclaimAvailableAtMillis() {
        return reclaimAvailableAtMillis;
    }

    public void setReclaimAvailableAtMillis(long reclaimAvailableAtMillis) {
        this.reclaimAvailableAtMillis = Math.max(0L, reclaimAvailableAtMillis);
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Set<UUID> getInvitedPlayers() {
        return invitedPlayers;
    }

    public Set<String> getAllowedTradeCountries() {
        return allowedTradeCountries;
    }

    public Set<UUID> getCoOwners() {
        return coOwners;
    }

    public Set<UUID> getStewards() {
        return stewards;
    }

    public Set<String> getUnlockedUpgradeKeys() {
        return unlockedUpgradeKeys;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public double getTreasuryBalance() {
        return treasuryBalance;
    }

    public void setTreasuryBalance(double treasuryBalance) {
        this.treasuryBalance = treasuryBalance;
    }

    public int getResourceStockpile() {
        return resourceStockpile;
    }

    public void setResourceStockpile(int resourceStockpile) {
        this.resourceStockpile = resourceStockpile;
    }

    public String getActiveBoostKey() {
        return activeBoostKey;
    }

    public void setActiveBoostKey(String activeBoostKey) {
        this.activeBoostKey = activeBoostKey;
    }

    public long getActiveBoostUntilMillis() {
        return activeBoostUntilMillis;
    }

    public void setActiveBoostUntilMillis(long activeBoostUntilMillis) {
        this.activeBoostUntilMillis = activeBoostUntilMillis;
    }
}
