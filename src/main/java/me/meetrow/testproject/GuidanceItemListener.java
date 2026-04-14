package me.meetrow.testproject;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public final class GuidanceItemListener implements Listener {
    private static final int GUIDANCE_SLOT = 8;

    private final Testproject plugin;

    public GuidanceItemListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!plugin.isGuidanceItem(event.getItemDrop().getItemStack())) {
            return;
        }

        event.setCancelled(true);
        sendLockedMessage(event.getPlayer());
        plugin.ensurePlayerGuidanceItem(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (plugin.isGuidanceItem(current) || plugin.isGuidanceItem(cursor)) {
            event.setCancelled(true);
            sendLockedMessage(player);
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.ensurePlayerGuidanceItem(player));
            return;
        }

        if (event.getClick() == ClickType.NUMBER_KEY) {
            int hotbarButton = event.getHotbarButton();
            if (hotbarButton == GUIDANCE_SLOT || plugin.isGuidanceItem(player.getInventory().getItem(hotbarButton))) {
                event.setCancelled(true);
                sendLockedMessage(player);
                plugin.getServer().getScheduler().runTask(plugin, () -> plugin.ensurePlayerGuidanceItem(player));
                return;
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!plugin.isGuidanceItem(event.getOldCursor())) {
            return;
        }

        event.setCancelled(true);
        sendLockedMessage(player);
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.ensurePlayerGuidanceItem(player));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (!plugin.isGuidanceItem(event.getMainHandItem()) && !plugin.isGuidanceItem(event.getOffHandItem())) {
            return;
        }

        event.setCancelled(true);
        sendLockedMessage(event.getPlayer());
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.ensurePlayerGuidanceItem(event.getPlayer()));
    }

    private void sendLockedMessage(Player player) {
        player.sendActionBar(Component.text("The Terra Guide is locked to slot 9.", NamedTextColor.RED));
    }
}
