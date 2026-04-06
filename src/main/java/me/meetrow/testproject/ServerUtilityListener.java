package me.meetrow.testproject;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.world.EntitiesLoadEvent;

public final class ServerUtilityListener implements Listener {
    private final Testproject plugin;

    public ServerUtilityListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.isJoinLeaveMessagesEnabled() && plugin.hasMessage("presence.join")) {
            event.joinMessage(plugin.legacyComponent(plugin.getPlayerMessage("presence.join", event.getPlayer(), plugin.placeholders(
                    "player", event.getPlayer().getName()
            ))));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.isJoinLeaveMessagesEnabled() && plugin.hasMessage("presence.leave")) {
            event.quitMessage(plugin.legacyComponent(plugin.getPlayerMessage("presence.leave", event.getPlayer(), plugin.placeholders(
                    "player", event.getPlayer().getName()
            ))));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isMaintenanceModeEnabled() || plugin.canBypassMaintenance(player) || plugin.canUseAdminCommands(player)) {
            return;
        }
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, plugin.getMessage("maintenance.kick"));
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        if (!plugin.isMaintenanceModeEnabled()) {
            return;
        }
        String line1 = plugin.getMessage("maintenance.motd-line1");
        String line2 = plugin.getMessage("maintenance.motd-line2");
        event.setMotd(line1 + "\n" + line2);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (!plugin.canParticipateInMobStacking(entity)) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.stackMobIfPossible(entity));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }
        if (plugin.getMobStackCount(livingEntity) <= 1) {
            return;
        }
        plugin.handleStackedMobDeath(livingEntity);
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, plugin::runMobStackingSweep);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemLoad(org.bukkit.event.entity.ItemSpawnEvent event) {
        Item item = event.getEntity();
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.tryMergeDroppedItem(item));
    }
}
