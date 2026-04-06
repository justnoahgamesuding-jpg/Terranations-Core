package me.meetrow.testproject;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class StaffModeListener implements Listener {
    private final Testproject plugin;

    public StaffModeListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.clearStaffStatesOnQuit(event.getPlayer());
    }
}
