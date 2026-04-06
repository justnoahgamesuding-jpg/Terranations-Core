package me.meetrow.testproject;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class WorldGuardTerritoryService implements TerritoryService {
    private static final String DYNMAP_MARKER_API_CLASS = "org.dynmap.markers.MarkerAPI";
    private static final String DYNMAP_MARKER_SET_CLASS = "org.dynmap.markers.MarkerSet";
    private static final String DYNMAP_AREA_MARKER_CLASS = "org.dynmap.markers.AreaMarker";
    private static final String DYNMAP_MARKER_CLASS = "org.dynmap.markers.Marker";
    private static final String DYNMAP_MARKER_ICON_CLASS = "org.dynmap.markers.MarkerIcon";

    private final Testproject plugin;

    public WorldGuardTerritoryService(Testproject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public TerritoryOperationResult bindCountry(Country country, Collection<Country> allCountries, String worldName, String regionId) {
        org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) {
            return TerritoryOperationResult.failure("world-not-found");
        }

        String normalizedRegionId = regionId.toLowerCase(Locale.ROOT);
        for (Country other : allCountries) {
            if (other == country || !other.hasTerritory()) {
                continue;
            }
            if (worldName.equalsIgnoreCase(other.getTerritoryWorld()) && normalizedRegionId.equalsIgnoreCase(other.getTerritoryRegionId())) {
                return TerritoryOperationResult.failure("region-already-linked");
            }
        }

        ProtectedRegion region = getRegion(bukkitWorld, normalizedRegionId);
        if (region == null) {
            return TerritoryOperationResult.failure("region-not-found");
        }

        if (country.hasTerritory() && (!worldName.equalsIgnoreCase(country.getTerritoryWorld()) || !normalizedRegionId.equalsIgnoreCase(country.getTerritoryRegionId()))) {
            clearLinkedRegion(country);
        }

        country.setTerritoryWorld(bukkitWorld.getName());
        country.setTerritoryRegionId(region.getId());
        TerritoryOperationResult result = syncCountry(country);
        if (!result.success()) {
            return result;
        }

        return TerritoryOperationResult.success(country.getTerritoryWorld(), country.getTerritoryRegionId());
    }

    @Override
    public TerritoryOperationResult clearCountry(Country country) {
        if (!country.hasTerritory()) {
            return TerritoryOperationResult.failure("not-bound");
        }

        org.bukkit.World bukkitWorld = Bukkit.getWorld(country.getTerritoryWorld());
        if (bukkitWorld != null) {
            TerritoryOperationResult clearResult = clearLinkedRegion(country);
            if (!clearResult.success()) {
                return clearResult;
            }
        }

        removeDynmapMarker(country);
        country.setTerritoryWorld(null);
        country.setTerritoryRegionId(null);
        return TerritoryOperationResult.success(null, null);
    }

    @Override
    public TerritoryOperationResult syncCountry(Country country) {
        if (!country.hasTerritory()) {
            removeDynmapMarker(country);
            return TerritoryOperationResult.failure("not-bound");
        }

        org.bukkit.World bukkitWorld = Bukkit.getWorld(country.getTerritoryWorld());
        if (bukkitWorld == null) {
            return TerritoryOperationResult.failure("world-not-found");
        }

        ProtectedRegion region = getRegion(bukkitWorld, country.getTerritoryRegionId());
        if (region == null) {
            return TerritoryOperationResult.failure("region-not-found");
        }

        region.setOwners(createOwnersDomain(country.getOwnerId()));
        region.setMembers(createMembersDomain(country.getMembers(), country.getOwnerId()));

        TerritoryOperationResult saveResult = saveRegionManager(bukkitWorld);
        if (!saveResult.success()) {
            return saveResult;
        }

        updateDynmapMarker(country, region);
        return TerritoryOperationResult.success(country.getTerritoryWorld(), country.getTerritoryRegionId());
    }

    @Override
    public void syncAll(Collection<Country> countries) {
        for (Country country : countries) {
            if (country.hasTerritory()) {
                TerritoryOperationResult result = syncCountry(country);
                if (!result.success()) {
                    plugin.getLogger().warning("Failed to sync territory for " + country.getName() + ": " + result.reason());
                }
            }
        }
    }

    @Override
    public String describeTerritory(Country country) {
        return country.hasTerritory()
                ? country.getTerritoryWorld() + " / " + country.getTerritoryRegionId()
                : "none";
    }

    @Override
    public List<String> getRegionIds(String worldName) {
        org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) {
            return Collections.emptyList();
        }

        RegionManager manager = getRegionManager(bukkitWorld);
        if (manager == null) {
            return Collections.emptyList();
        }

        List<String> ids = new ArrayList<>(manager.getRegions().keySet());
        ids.sort(String.CASE_INSENSITIVE_ORDER);
        return ids;
    }

    @Override
    public Country getCountryAt(Location location, Collection<Country> countries) {
        if (location.getWorld() == null) {
            return null;
        }

        RegionManager manager = getRegionManager(location.getWorld());
        if (manager == null) {
            return null;
        }

        BlockVector3 point = BukkitAdapter.asBlockVector(location);
        for (ProtectedRegion region : manager.getApplicableRegions(point)) {
            for (Country country : countries) {
                if (!country.hasTerritory()) {
                    continue;
                }
                if (location.getWorld().getName().equalsIgnoreCase(country.getTerritoryWorld())
                        && region.getId().equalsIgnoreCase(country.getTerritoryRegionId())) {
                    return country;
                }
            }
        }

        return null;
    }

    @Override
    public List<Location> getBorderLocations(Country country) {
        if (country == null || !country.hasTerritory()) {
            return List.of();
        }

        org.bukkit.World bukkitWorld = Bukkit.getWorld(country.getTerritoryWorld());
        if (bukkitWorld == null) {
            return List.of();
        }

        ProtectedRegion region = getRegion(bukkitWorld, country.getTerritoryRegionId());
        if (region == null) {
            return List.of();
        }

        Set<String> deduplicated = new LinkedHashSet<>();
        List<Location> points = new ArrayList<>();
        if (region instanceof ProtectedPolygonalRegion polygonalRegion) {
            List<BlockVector2> vertices = polygonalRegion.getPoints();
            if (vertices.size() < 2) {
                return List.of();
            }
            for (int i = 0; i < vertices.size(); i++) {
                BlockVector2 from = vertices.get(i);
                BlockVector2 to = vertices.get((i + 1) % vertices.size());
                addEdgePoints(points, deduplicated, bukkitWorld, from.x(), from.z(), to.x(), to.z());
            }
            return points;
        }

        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        addEdgePoints(points, deduplicated, bukkitWorld, min.x(), min.z(), max.x(), min.z());
        addEdgePoints(points, deduplicated, bukkitWorld, max.x(), min.z(), max.x(), max.z());
        addEdgePoints(points, deduplicated, bukkitWorld, max.x(), max.z(), min.x(), max.z());
        addEdgePoints(points, deduplicated, bukkitWorld, min.x(), max.z(), min.x(), min.z());
        return points;
    }

    @Override
    public TerritoryDebugInfo getDebugInfo(Country country) {
        boolean dynmapInstalled = Bukkit.getPluginManager().getPlugin("dynmap") != null;
        boolean dynmapEnabledInConfig = plugin.getConfig().getBoolean("territories.dynmap.enabled", true);
        boolean territoryLinked = country.hasTerritory();
        String worldName = country.getTerritoryWorld();
        String regionId = country.getTerritoryRegionId();
        boolean worldFound = false;
        boolean regionFound = false;
        String regionType = null;
        int pointCount = 0;

        if (territoryLinked && worldName != null) {
            org.bukkit.World world = Bukkit.getWorld(worldName);
            worldFound = world != null;
            if (world != null && regionId != null) {
                ProtectedRegion region = getRegion(world, regionId);
                regionFound = region != null;
                if (region != null) {
                    regionType = region.getType().name();
                    pointCount = region.getPoints().size();
                }
            }
        }

        Object markerSet = getMarkerSet();
        return new TerritoryDebugInfo(
                true,
                dynmapInstalled,
                dynmapEnabledInConfig,
                territoryLinked,
                worldFound,
                regionFound,
                worldName,
                regionId,
                regionType,
                pointCount,
                plugin.getConfig().getString("territories.dynmap.marker-set-id", "terra_countries"),
                markerSet != null
        );
    }

    @Override
    public int countFarmlandBlocks(Country country) {
        if (!country.hasTerritory()) {
            return 0;
        }

        org.bukkit.World bukkitWorld = Bukkit.getWorld(country.getTerritoryWorld());
        if (bukkitWorld == null) {
            return 0;
        }

        ProtectedRegion region = getRegion(bukkitWorld, country.getTerritoryRegionId());
        if (region == null) {
            return 0;
        }

        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        int farmlandCount = 0;

        for (int x = min.x(); x <= max.x(); x++) {
            for (int y = min.y(); y <= max.y(); y++) {
                for (int z = min.z(); z <= max.z(); z++) {
                    if (!containsBlock(region, x, y, z)) {
                        continue;
                    }
                    if (bukkitWorld.getBlockAt(x, y, z).getType() == Material.FARMLAND) {
                        farmlandCount++;
                    }
                }
            }
        }

        return farmlandCount;
    }

    private boolean containsBlock(ProtectedRegion region, int x, int y, int z) {
        if (region instanceof ProtectedPolygonalRegion polygonalRegion) {
            if (y < polygonalRegion.getMinimumPoint().y() || y > polygonalRegion.getMaximumPoint().y()) {
                return false;
            }
            return polygonalRegion.contains(BlockVector2.at(x, z));
        }
        return region.contains(x, y, z);
    }

    private void addEdgePoints(List<Location> points, Set<String> deduplicated, org.bukkit.World world, int fromX, int fromZ, int toX, int toZ) {
        int deltaX = toX - fromX;
        int deltaZ = toZ - fromZ;
        int steps = Math.max(Math.abs(deltaX), Math.abs(deltaZ));
        if (steps <= 0) {
            addBorderPoint(points, deduplicated, world, fromX, fromZ);
            return;
        }

        for (int step = 0; step <= steps; step++) {
            double progress = step / (double) steps;
            int x = (int) Math.round(fromX + (deltaX * progress));
            int z = (int) Math.round(fromZ + (deltaZ * progress));
            addBorderPoint(points, deduplicated, world, x, z);
        }
    }

    private void addBorderPoint(List<Location> points, Set<String> deduplicated, org.bukkit.World world, int x, int z) {
        String key = x + ":" + z;
        if (!deduplicated.add(key)) {
            return;
        }
        int y = world.getHighestBlockYAt(x, z) + 1;
        points.add(new Location(world, x + 0.5D, y + 0.2D, z + 0.5D));
    }

    private DefaultDomain createOwnersDomain(UUID ownerId) {
        DefaultDomain owners = new DefaultDomain();
        if (ownerId != null) {
            owners.addPlayer(ownerId);
        }
        return owners;
    }

    private DefaultDomain createMembersDomain(Set<UUID> members, UUID ownerId) {
        DefaultDomain memberDomain = new DefaultDomain();
        for (UUID memberId : members) {
            if (ownerId == null || !memberId.equals(ownerId)) {
                memberDomain.addPlayer(memberId);
            }
        }
        return memberDomain;
    }

    private ProtectedRegion getRegion(org.bukkit.World bukkitWorld, String regionId) {
        RegionManager manager = getRegionManager(bukkitWorld);
        if (manager == null) {
            return null;
        }
        return manager.getRegion(regionId);
    }

    private RegionManager getRegionManager(org.bukkit.World bukkitWorld) {
        World world = BukkitAdapter.adapt(bukkitWorld);
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
    }

    private TerritoryOperationResult saveRegionManager(org.bukkit.World bukkitWorld) {
        RegionManager manager = getRegionManager(bukkitWorld);
        if (manager == null) {
            return TerritoryOperationResult.failure("region-manager-unavailable");
        }

        try {
            manager.saveChanges();
            return TerritoryOperationResult.success(bukkitWorld.getName(), null);
        } catch (StorageException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save WorldGuard regions for " + bukkitWorld.getName(), exception);
            return TerritoryOperationResult.failure("save-failed");
        }
    }

    private TerritoryOperationResult clearLinkedRegion(Country country) {
        org.bukkit.World linkedWorld = Bukkit.getWorld(country.getTerritoryWorld());
        if (linkedWorld == null) {
            removeDynmapMarker(country);
            return TerritoryOperationResult.success(null, null);
        }

        ProtectedRegion linkedRegion = getRegion(linkedWorld, country.getTerritoryRegionId());
        if (linkedRegion != null) {
            linkedRegion.setOwners(new DefaultDomain());
            linkedRegion.setMembers(new DefaultDomain());
            TerritoryOperationResult saveResult = saveRegionManager(linkedWorld);
            if (!saveResult.success()) {
                return saveResult;
            }
        }

        removeDynmapMarker(country);
        return TerritoryOperationResult.success(null, null);
    }

    private void updateDynmapMarker(Country country, ProtectedRegion region) {
        if (!plugin.getConfig().getBoolean("territories.dynmap.enabled", true)) {
            return;
        }

        Object markerSet = getOrCreateMarkerSet();
        if (markerSet == null) {
            return;
        }

        try {
            Method findAreaMarker = getDynmapMethod(DYNMAP_MARKER_SET_CLASS, "findAreaMarker", String.class);
            Object areaMarker = invoke(findAreaMarker, markerSet, markerId(country));

            double[] x = region.getPoints().stream().mapToDouble(BlockVector2::x).toArray();
            double[] z = region.getPoints().stream().mapToDouble(BlockVector2::z).toArray();
            if (x.length < 3 || z.length < 3) {
                if (areaMarker != null) {
                    invoke(getDynmapMethod(DYNMAP_AREA_MARKER_CLASS, "deleteMarker"), areaMarker);
                }
                removeDynmapCenterMarker(markerSet, country);
                return;
            }

            if (areaMarker != null) {
                invoke(getDynmapMethod(DYNMAP_AREA_MARKER_CLASS, "deleteMarker"), areaMarker);
            }

            Method createAreaMarker = getDynmapMethod(DYNMAP_MARKER_SET_CLASS, "createAreaMarker", String.class, String.class, boolean.class, String.class, double[].class, double[].class, boolean.class);
            areaMarker = invoke(createAreaMarker, markerSet, markerId(country), country.getName(), false, country.getTerritoryWorld(), x, z, false);

            int lineColor = getConfiguredLineColor(country);
            int fillColor = getConfiguredFillColor(country);
            int lineWeight = plugin.getConfig().getInt("territories.dynmap.line-weight", 2);
            double lineOpacity = plugin.getConfig().getDouble("territories.dynmap.line-opacity", 0.9D);
            double fillOpacity = Math.max(plugin.getConfig().getDouble("territories.dynmap.fill-opacity", 0.4D), 0.35D);

            invoke(getDynmapMethod(DYNMAP_AREA_MARKER_CLASS, "setLineStyle", int.class, double.class, int.class), areaMarker, lineWeight, lineOpacity, lineColor);
            invoke(getDynmapMethod(DYNMAP_AREA_MARKER_CLASS, "setFillStyle", double.class, int.class), areaMarker, fillOpacity, fillColor);
            if (plugin.getConfig().getBoolean("territories.dynmap.use-3d-regions", false)) {
                double topY = region.getMaximumPoint().y() + 1.0D;
                double bottomY = region.getMinimumPoint().y();
                invoke(getDynmapMethod(DYNMAP_AREA_MARKER_CLASS, "setRangeY", double.class, double.class), areaMarker, topY, bottomY);
            }
            invoke(getDynmapMethod(DYNMAP_AREA_MARKER_CLASS, "setBoostFlag", boolean.class), areaMarker, plugin.getConfig().getBoolean("territories.dynmap.boost", true));
            invoke(getDynmapMethod(DYNMAP_AREA_MARKER_CLASS, "setDescription", String.class), areaMarker, buildMarkerDescription(country));
            updateDynmapCenterMarker(markerSet, country, region);
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to update Dynmap marker for " + country.getName(), exception);
        }
    }

    private void removeDynmapMarker(Country country) {
        Object markerSet = getMarkerSet();
        if (markerSet == null) {
            return;
        }

        try {
            Method findAreaMarker = getDynmapMethod(DYNMAP_MARKER_SET_CLASS, "findAreaMarker", String.class);
            Object areaMarker = invoke(findAreaMarker, markerSet, markerId(country));
            if (areaMarker != null) {
                invoke(getDynmapMethod(DYNMAP_AREA_MARKER_CLASS, "deleteMarker"), areaMarker);
            }
            removeDynmapCenterMarker(markerSet, country);
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to remove Dynmap marker for " + country.getName(), exception);
        }
    }

    private Object getOrCreateMarkerSet() {
        Object markerSet = getMarkerSet();
        if (markerSet != null) {
            return markerSet;
        }

        Object markerApi = getMarkerApi();
        if (markerApi == null) {
            return null;
        }

        try {
            Method createMarkerSet = getDynmapMethod(DYNMAP_MARKER_API_CLASS, "createMarkerSet", String.class, String.class, Set.class, boolean.class);
            Object created = invoke(
                    createMarkerSet,
                    markerApi,
                    plugin.getConfig().getString("territories.dynmap.marker-set-id", "terra_countries"),
                    plugin.getConfig().getString("territories.dynmap.marker-set-label", "Terra Countries"),
                    null,
                    false
            );
            if (created != null) {
                invoke(getDynmapMethod(DYNMAP_MARKER_SET_CLASS, "setHideByDefault", boolean.class), created, false);
            }
            return created;
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to create Dynmap marker set", exception);
            return null;
        }
    }

    private Object getMarkerSet() {
        Object markerApi = getMarkerApi();
        if (markerApi == null) {
            return null;
        }

        try {
            Method getMarkerSet = getDynmapMethod(DYNMAP_MARKER_API_CLASS, "getMarkerSet", String.class);
            return invoke(getMarkerSet, markerApi, plugin.getConfig().getString("territories.dynmap.marker-set-id", "terra_countries"));
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to fetch Dynmap marker set", exception);
            return null;
        }
    }

    private Object getMarkerApi() {
        if (Bukkit.getPluginManager().getPlugin("dynmap") == null) {
            return null;
        }

        try {
            Object dynmapPlugin = Bukkit.getPluginManager().getPlugin("dynmap");
            Method getMarkerAPI = dynmapPlugin.getClass().getMethod("getMarkerAPI");
            return invoke(getMarkerAPI, dynmapPlugin);
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to access Dynmap Marker API", exception);
            return null;
        }
    }

    private String buildMarkerDescription(Country country) {
        List<String> memberNames = new ArrayList<>();
        for (UUID memberId : country.getMembers()) {
            memberNames.add(plugin.safeOfflineName(memberId));
        }
        memberNames.sort(String.CASE_INSENSITIVE_ORDER);

        return "<div>"
                + "<strong>" + escapeHtml(country.getName()) + "</strong>"
                + "<br/>Owner: " + escapeHtml(plugin.safeOfflineName(country.getOwnerId()))
                + "<br/>Join: " + escapeHtml(country.isOpen() ? "open" : "closed")
                + "<br/>Members (" + memberNames.size() + "): " + escapeHtml(String.join(", ", memberNames))
                + "</div>";
    }

    private String markerId(Country country) {
        String world = country.getTerritoryWorld() != null ? country.getTerritoryWorld().toLowerCase(Locale.ROOT) : "world";
        String region = country.getTerritoryRegionId() != null ? country.getTerritoryRegionId().toLowerCase(Locale.ROOT) : plugin.normalizeCountryKey(country.getName());
        return "terra_country_" + world.replaceAll("[^a-z0-9_]", "_") + "_" + region.replaceAll("[^a-z0-9_]", "_");
    }

    private String centerMarkerId(Country country) {
        return markerId(country) + "_center";
    }

    private int parseColor(String value, int fallback) {
        String normalized = value == null ? "" : value.trim().replace("#", "");
        try {
            return Integer.parseInt(normalized, 16);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private int getConfiguredLineColor(Country country) {
        if (!plugin.getConfig().getBoolean("territories.dynmap.use-status-colors", true)) {
            return parseColor(plugin.getConfig().getString("territories.dynmap.line-color", "#4caf50"), 0x4CAF50);
        }
        return getCountryLineColor(country);
    }

    private int getConfiguredFillColor(Country country) {
        if (!plugin.getConfig().getBoolean("territories.dynmap.use-status-colors", true)) {
            return parseColor(plugin.getConfig().getString("territories.dynmap.fill-color", "#4caf50"), 0x4CAF50);
        }
        return getCountryFillColor(country);
    }

    private int getCountryLineColor(Country country) {
        if (country.getOwnerId() == null) {
            return parseColor(plugin.getConfig().getString("territories.dynmap.ownerless-line-color", "#7fdbff"), 0x7FDBFF);
        }
        if (country.isOpen()) {
            return parseColor(plugin.getConfig().getString("territories.dynmap.open-line-color", "#2ecc40"), 0x2ECC40);
        }
        return parseColor(plugin.getConfig().getString("territories.dynmap.closed-line-color", "#ff4136"), 0xFF4136);
    }

    private int getCountryFillColor(Country country) {
        if (country.getOwnerId() == null) {
            return parseColor(plugin.getConfig().getString("territories.dynmap.ownerless-fill-color", "#7fdbff"), 0x7FDBFF);
        }
        if (country.isOpen()) {
            return parseColor(plugin.getConfig().getString("territories.dynmap.open-fill-color", "#2ecc40"), 0x2ECC40);
        }
        return parseColor(plugin.getConfig().getString("territories.dynmap.closed-fill-color", "#ff4136"), 0xFF4136);
    }

    private void updateDynmapCenterMarker(Object markerSet, Country country, ProtectedRegion region) throws ReflectiveOperationException {
        if (!plugin.getConfig().getBoolean("territories.dynmap.show-center-marker", true)) {
            removeDynmapCenterMarker(markerSet, country);
            return;
        }

        double centerX = region.getPoints().stream().mapToDouble(BlockVector2::x).average().orElse(region.getMinimumPoint().x());
        double centerZ = region.getPoints().stream().mapToDouble(BlockVector2::z).average().orElse(region.getMinimumPoint().z());
        double centerY = region.getMaximumPoint().y() + plugin.getConfig().getDouble("territories.dynmap.marker-y-offset", 1.0D);

        Method findMarker = getDynmapMethod(DYNMAP_MARKER_SET_CLASS, "findMarker", String.class);
        Object marker = invoke(findMarker, markerSet, centerMarkerId(country));

        Object markerApi = getMarkerApi();
        if (markerApi == null) {
            return;
        }

        Method getMarkerIcon = getDynmapMethod(DYNMAP_MARKER_API_CLASS, "getMarkerIcon", String.class);
        Object markerIcon = invoke(getMarkerIcon, markerApi, plugin.getConfig().getString("territories.dynmap.marker-icon", "world"));

        if (marker == null) {
            Method createMarker = getDynmapMethod(DYNMAP_MARKER_SET_CLASS, "createMarker", String.class, String.class, boolean.class, String.class, double.class, double.class, double.class, Class.forName(DYNMAP_MARKER_ICON_CLASS), boolean.class);
            marker = invoke(createMarker, markerSet, centerMarkerId(country), country.getName(), false, country.getTerritoryWorld(), centerX, centerY, centerZ, markerIcon, true);
        } else {
            invoke(getDynmapMethod(DYNMAP_MARKER_CLASS, "setLabel", String.class), marker, country.getName());
            invoke(getDynmapMethod(DYNMAP_MARKER_CLASS, "setLocation", String.class, double.class, double.class, double.class), marker, country.getTerritoryWorld(), centerX, centerY, centerZ);
            if (markerIcon != null) {
                invoke(getDynmapMethod(DYNMAP_MARKER_CLASS, "setMarkerIcon", Class.forName(DYNMAP_MARKER_ICON_CLASS)), marker, markerIcon);
            }
        }

        if (marker != null) {
            invoke(getDynmapMethod(DYNMAP_MARKER_CLASS, "setDescription", String.class), marker, buildMarkerDescription(country));
        }
    }

    private void removeDynmapCenterMarker(Object markerSet, Country country) throws ReflectiveOperationException {
        Method findMarker = getDynmapMethod(DYNMAP_MARKER_SET_CLASS, "findMarker", String.class);
        Object marker = invoke(findMarker, markerSet, centerMarkerId(country));
        if (marker != null) {
            invoke(getDynmapMethod(DYNMAP_MARKER_CLASS, "deleteMarker"), marker);
        }
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        try {
            return type.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException exception) {
            return null;
        }
    }

    private Method getDynmapMethod(String className, String methodName, Class<?>... parameterTypes) throws ReflectiveOperationException {
        Class<?> type = Class.forName(className);
        return type.getMethod(methodName, parameterTypes);
    }

    private Object invoke(Method method, Object target, Object... args) throws ReflectiveOperationException {
        if (!method.canAccess(target)) {
            method.setAccessible(true);
        }
        return method.invoke(target, args);
    }
}
