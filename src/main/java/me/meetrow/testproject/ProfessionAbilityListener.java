package me.meetrow.testproject;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class ProfessionAbilityListener implements Listener {
    private final Testproject plugin;

    public ProfessionAbilityListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) {
            return;
        }

        Material type = item.getType();
        if (type.name().endsWith("_PICKAXE")) {
            if (plugin.activateMinerOverdrive(player)) {
                event.setCancelled(true);
            }
            return;
        }

        if (type.name().endsWith("_HOE")) {
            if (plugin.activateFarmerGrowthBurst(player)) {
                event.setCancelled(true);
            }
            return;
        }

        if (type == Material.EMERALD) {
            if (plugin.activateTraderMarketScan(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        plugin.applyFarmerMealBuffs(event.getPlayer(), event.getItem());
    }
}
