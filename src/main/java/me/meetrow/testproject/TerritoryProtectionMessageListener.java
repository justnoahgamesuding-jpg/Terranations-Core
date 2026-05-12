package me.meetrow.testproject;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TerritoryProtectionMessageListener implements Listener {
    private static final long MESSAGE_INTERVAL_MILLIS = 1500L;

    private final Testproject plugin;
    private final Map<UUID, Long> lastMessageAt = new ConcurrentHashMap<>();

    public TerritoryProtectionMessageListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            return;
        }
        sendTerritoryBlockedMessage(event.getPlayer(), event.getBlock(), "country.territory.blocked-break", ProtectionMessageSource.BUILD);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled()) {
            return;
        }
        ProtectionMessageSource source = event.getBlockPlaced().getType() == Material.FARMLAND
                ? ProtectionMessageSource.FARMLAND
                : ProtectionMessageSource.BUILD;
        sendTerritoryBlockedMessage(event.getPlayer(), event.getBlockPlaced(), "country.territory.blocked-place", source);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!event.isCancelled()) {
            return;
        }
        sendTerritoryBlockedMessage(event.getPlayer(), event.getBlock(), "country.territory.blocked-use", ProtectionMessageSource.INTERACTABLE);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!event.isCancelled()) {
            return;
        }
        sendTerritoryBlockedMessage(event.getPlayer(), event.getBlock(), "country.territory.blocked-use", ProtectionMessageSource.INTERACTABLE);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProtectedInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (!event.isCancelled() && event.useInteractedBlock() != org.bukkit.event.Event.Result.DENY) {
            return;
        }
        sendTerritoryBlockedMessage(event.getPlayer(), event.getClickedBlock(), "country.territory.blocked-use",
                classifyInteractSource(event.getClickedBlock()));
    }

    private void sendTerritoryBlockedMessage(Player player, Block block, String messagePath, ProtectionMessageSource source) {
        if (player == null || block == null) {
            return;
        }
        Country country = plugin.getCountryAt(block.getLocation());
        if (country == null || !country.hasTerritory() || plugin.isConfiguredStarterHubCountry(country)) {
            return;
        }
        long now = System.currentTimeMillis();
        long last = lastMessageAt.getOrDefault(player.getUniqueId(), 0L);
        if ((now - last) < MESSAGE_INTERVAL_MILLIS) {
            return;
        }
        lastMessageAt.put(player.getUniqueId(), now);
        String resolvedMessagePath = resolveMessagePath(messagePath, source, country, player.getUniqueId());
        Guild owningGuild = plugin.getOwningGuild(country);
        player.sendMessage(plugin.getMessage(resolvedMessagePath, plugin.placeholders(
                "country", country.getName(),
                "guild", owningGuild != null ? owningGuild.getName() : "None"
        )));
    }

    private String resolveMessagePath(String basePath, ProtectionMessageSource source, Country country, UUID playerId) {
        String relationshipKey = resolveRelationshipKey(country, playerId);
        String sourceKey = plugin.isSystemCountry(country) ? ProtectionMessageSource.SYSTEM_COUNTRY.key() : source.key();
        String specificPath = basePath + "." + sourceKey + "." + relationshipKey;
        if (plugin.hasMessage(specificPath)) {
            return specificPath;
        }
        String defaultSourcePath = basePath + ".default." + relationshipKey;
        if (plugin.hasMessage(defaultSourcePath)) {
            return defaultSourcePath;
        }
        return basePath + "." + relationshipKey;
    }

    private String resolveRelationshipKey(Country country, UUID playerId) {
        if (plugin.isCountryOwner(country, playerId)) {
            return "owner";
        }
        if (plugin.isCountryCoOwner(country, playerId)) {
            return "co-owner";
        }
        if (plugin.isCountrySteward(country, playerId)) {
            return "steward";
        }
        if (country.getMembers().contains(playerId)) {
            return "member";
        }
        Guild owningGuild = plugin.getOwningGuild(country);
        if (owningGuild != null && owningGuild.getMembers().contains(playerId)) {
            return "guild-member";
        }
        Country playerCountry = plugin.getPlayerCountry(playerId);
        if (playerCountry != null) {
            return "foreign-country";
        }
        return "outsider";
    }

    private ProtectionMessageSource classifyInteractSource(Block block) {
        if (block == null) {
            return ProtectionMessageSource.DEFAULT;
        }
        if (block.getType() == Material.FARMLAND) {
            return ProtectionMessageSource.FARMLAND;
        }
        if (block.getState() instanceof org.bukkit.inventory.InventoryHolder) {
            return ProtectionMessageSource.CONTAINER;
        }
        if (block.getType().isInteractable()) {
            return ProtectionMessageSource.INTERACTABLE;
        }
        return ProtectionMessageSource.DEFAULT;
    }

    private enum ProtectionMessageSource {
        DEFAULT("default"),
        BUILD("build"),
        CONTAINER("container"),
        FARMLAND("farmland"),
        INTERACTABLE("interactable"),
        SYSTEM_COUNTRY("system-country");

        private final String key;

        ProtectionMessageSource(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }
}
