package me.meetrow.testproject;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class StabilityListener implements Listener {
    private final Testproject plugin;

    public StabilityListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        plugin.queueStabilityScansAround(event.getBlock().getLocation(), 1, 2);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        plugin.queueStabilityScansAround(event.getBlockPlaced().getLocation(), 1, 2);
        plugin.queueStabilityScansAround(event.getBlockAgainst().getLocation(), 1, 2);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        queueExplosionScan(event.blockList().stream().findFirst().orElse(event.getBlock()), event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        Location fallback = entity != null ? entity.getLocation() : event.getLocation();
        queueExplosionScan(event.blockList().stream().findFirst().orElse(null), fallback);
    }

    private void queueExplosionScan(Block affectedBlock, Location fallback) {
        if (affectedBlock != null) {
            plugin.queueStabilityScansAround(affectedBlock.getLocation(), 2, 2);
            return;
        }
        if (fallback != null) {
            plugin.queueStabilityScansAround(fallback, 2, 2);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        plugin.handleStabilityChunkUnload(event.getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        plugin.handleStabilityChunkLoad(event.getChunk());
    }
}
