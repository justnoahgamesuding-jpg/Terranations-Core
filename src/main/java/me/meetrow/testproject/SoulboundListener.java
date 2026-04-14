package me.meetrow.testproject;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class SoulboundListener implements Listener {
    private final Testproject plugin;

    public SoulboundListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack itemStack = event.getItemDrop().getItemStack();
        if (!plugin.isSoulboundItem(itemStack)) {
            return;
        }

        event.setCancelled(true);
        sendSoulboundMessage(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!isContainer(event.getView().getTopInventory().getType())) {
            return;
        }

        if (event.getClickedInventory() != null
                && event.getClickedInventory().equals(event.getView().getBottomInventory())
                && event.isShiftClick()
                && plugin.isSoulboundItem(event.getCurrentItem())) {
            event.setCancelled(true);
            sendSoulboundMessage(player);
            return;
        }

        if (event.getClickedInventory() != null
                && event.getClickedInventory().equals(event.getView().getTopInventory())
                && plugin.isSoulboundItem(event.getCursor())
                && event.getAction() != InventoryAction.NOTHING) {
            event.setCancelled(true);
            sendSoulboundMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!plugin.isSoulboundItem(event.getOldCursor())) {
            return;
        }
        if (!isContainer(event.getView().getTopInventory().getType())) {
            return;
        }

        int topSize = event.getView().getTopInventory().getSize();
        boolean hitsTop = event.getRawSlots().stream().anyMatch(slot -> slot < topSize);
        if (!hitsTop) {
            return;
        }

        event.setCancelled(true);
        sendSoulboundMessage(player);
    }

    private boolean isContainer(InventoryType type) {
        return type != null
                && type != InventoryType.CRAFTING
                && type != InventoryType.CREATIVE
                && type != InventoryType.PLAYER;
    }

    private void sendSoulboundMessage(Player player) {
        player.sendActionBar(Component.text("That item is soulbound.", NamedTextColor.RED));
    }
}
