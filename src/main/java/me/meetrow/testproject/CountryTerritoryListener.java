package me.meetrow.testproject;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CountryTerritoryListener implements Listener {
    private final Testproject plugin;

    public CountryTerritoryListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.updatePlayerCountryTag(event.getPlayer());
        plugin.setLastTerritoryCountry(event.getPlayer().getUniqueId(), plugin.getCountryAt(event.getPlayer().getLocation()));
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            plugin.playJoinPresenceSound(onlinePlayer);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.clearLastTerritoryCountry(event.getPlayer().getUniqueId());
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (!onlinePlayer.getUniqueId().equals(event.getPlayer().getUniqueId())) {
                plugin.playLeavePresenceSound(onlinePlayer);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.areTerritoryNotificationsEnabled() || event.getTo() == null) {
            return;
        }

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()
                && event.getFrom().getWorld() == event.getTo().getWorld()) {
            return;
        }

        Player player = event.getPlayer();
        Country previousCountry = plugin.getLastTerritoryCountry(player.getUniqueId());
        Country currentCountry = plugin.getCountryAt(event.getTo());

        String previousName = previousCountry != null ? previousCountry.getName() : null;
        String currentName = currentCountry != null ? currentCountry.getName() : null;

        if ((previousName == null && currentName == null)
                || (previousName != null && previousName.equalsIgnoreCase(currentName))) {
            return;
        }

        if (previousCountry != null) {
            player.sendMessage(plugin.getMessage("country.territory.leave", plugin.placeholders("country", previousCountry.getName())));
        }

        if (currentCountry != null) {
            player.sendMessage(plugin.getMessage("country.territory.enter", plugin.placeholders("country", currentCountry.getName())));
            plugin.sendTerritoryEnterTitle(player, currentCountry);
        } else if (previousCountry != null) {
            plugin.sendTerritoryLeaveTitle(player, previousCountry);
        }

        plugin.setLastTerritoryCountry(player.getUniqueId(), currentCountry);
    }
}
