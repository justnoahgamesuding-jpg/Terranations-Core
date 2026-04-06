package me.meetrow.testproject;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class PhantomSpawnListener implements Listener {
    private final Testproject plugin;

    public PhantomSpawnListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (plugin.shouldBlockPhantom(event.getEntity())) {
            event.setCancelled(true);
        }
    }
}
