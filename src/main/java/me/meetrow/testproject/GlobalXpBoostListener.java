package me.meetrow.testproject;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class GlobalXpBoostListener implements Listener {
    private final Testproject plugin;

    public GlobalXpBoostListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.addPlayerToGlobalXpBoostBar(event.getPlayer());
    }
}
