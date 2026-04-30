package me.meetrow.testproject;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class BatSpawnListener implements Listener {
    private final Testproject plugin;

    public BatSpawnListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (plugin.shouldBlockBat(event.getEntity())) {
            event.setCancelled(true);
        }
    }
}
