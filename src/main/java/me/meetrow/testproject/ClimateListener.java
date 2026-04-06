package me.meetrow.testproject;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.concurrent.ThreadLocalRandom;

public class ClimateListener implements Listener {
    private final Testproject plugin;

    public ClimateListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        if (!plugin.shouldClimateManageCrop(block.getType())) {
            return;
        }

        ClimateSnapshot climate = plugin.getClimate(block.getLocation());
        double multiplier = plugin.getClimateGrowthMultiplier(block.getType(), block.getLocation(), climate);
        if (multiplier < 1.0D && ThreadLocalRandom.current().nextDouble() > multiplier) {
            event.setCancelled(true);
            return;
        }

        if (multiplier > 1.0D) {
            int extraSteps = (int) Math.floor(multiplier - 1.0D);
            double partialChance = (multiplier - 1.0D) - extraSteps;
            if (ThreadLocalRandom.current().nextDouble() < partialChance) {
                extraSteps++;
            }
            if (extraSteps > 0) {
                plugin.scheduleClimateExtraGrowth(block, extraSteps);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (event.getLocation() == null || event.getLocation().getWorld() == null) {
            return;
        }

        Material sourceMaterial = event.getLocation().getBlock().getType();
        if (!plugin.shouldClimateManageCrop(sourceMaterial)) {
            return;
        }

        ClimateSnapshot climate = plugin.getClimate(event.getLocation());
        double multiplier = plugin.getClimateGrowthMultiplier(sourceMaterial, event.getLocation(), climate);
        if (multiplier < 1.0D && ThreadLocalRandom.current().nextDouble() > multiplier) {
            event.setCancelled(true);
        }
    }
}
