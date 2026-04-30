package me.meetrow.testproject;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class OnboardingProgressListener implements Listener {
    private final Testproject plugin;

    public OnboardingProgressListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        plugin.recordOnboardingBlockBreak(event.getPlayer(), event.getBlock().getType());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        plugin.recordOnboardingBlockPlace(event.getPlayer(), event.getBlockPlaced().getType());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        plugin.recordOnboardingBlockInteract(player, block.getType());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        String key = plugin.getOnboardingNpcKey(event.getRightClicked());
        if (key != null) {
            plugin.handleNpcItemDelivery(player, key);
            plugin.recordOnboardingNpcInteraction(player, key);
            return;
        }
        if (plugin.isTraderNpc(event.getRightClicked())) {
            plugin.handleNpcItemDelivery(player, "trader_npc");
            plugin.recordOnboardingNpcInteraction(player, "trader_npc");
            return;
        }
        if (plugin.isMerchantNpc(event.getRightClicked())) {
            plugin.handleNpcItemDelivery(player, "merchant_npc");
            plugin.recordOnboardingNpcInteraction(player, "merchant_npc");
        }
    }
}
