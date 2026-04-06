package me.meetrow.testproject;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CooldownDebugListener implements Listener {
    private final Testproject plugin;

    public CooldownDebugListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.refreshCooldownDebugBars(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.handleCooldownDebugQuit(event.getPlayer());
    }
}
