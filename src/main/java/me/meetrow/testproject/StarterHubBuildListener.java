package me.meetrow.testproject;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class StarterHubBuildListener implements Listener {
    private final Testproject plugin;

    public StarterHubBuildListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!shouldProtect(event.getPlayer(), event.getBlockPlaced())) {
            return;
        }
        if (plugin.isStarterHubAllowedCropBlock(event.getBlockPlaced().getType())) {
            return;
        }
        event.setCancelled(true);
        sendBlockedMessage(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!shouldProtect(event.getPlayer(), event.getBlock())) {
            return;
        }
        if (plugin.isStarterHubAllowedCropBlock(event.getBlock().getType())
                || plugin.isFixedOreBlock(event.getBlock())) {
            return;
        }
        event.setCancelled(true);
        sendBlockedMessage(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Block target = event.getBlock();
        if (!shouldProtect(event.getPlayer(), target)) {
            return;
        }
        event.setCancelled(true);
        sendBlockedMessage(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Block target = event.getBlock();
        if (!shouldProtect(event.getPlayer(), target)) {
            return;
        }
        event.setCancelled(true);
        sendBlockedMessage(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFarmlandTrample(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.FARMLAND) {
            return;
        }
        if (!shouldProtect(event.getPlayer(), clickedBlock)) {
            return;
        }
        event.setCancelled(true);
    }

    private boolean shouldProtect(Player player, Block block) {
        return player != null
                && block != null
                && plugin.isStarterHubBuildProtected(block.getLocation())
                && !plugin.canBypassStarterHubBuildProtection(player);
    }

    private void sendBlockedMessage(Player player) {
        player.sendMessage(plugin.getMessage("tutorial.starter-hub.build-blocked"));
    }
}
