package me.meetrow.testproject;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class RestrictedItemListener implements Listener {
    private final Testproject plugin;

    public RestrictedItemListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerUseEnderPearl(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.ENDER_PEARL || plugin.isFunctionalMaterialEnabled(Material.ENDER_PEARL)) {
            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(plugin.getMessage("items.material-disabled", plugin.placeholders(
                "material", formatMaterial(Material.ENDER_PEARL)
        )));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlaceRestrictedFunctionalBlock(BlockPlaceEvent event) {
        Material material = event.getBlockPlaced().getType();
        if (!plugin.isRestrictableFunctionalMaterial(material) || plugin.isFunctionalMaterialEnabled(material)) {
            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(plugin.getMessage("items.material-disabled", plugin.placeholders(
                "material", formatMaterial(material)
        )));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUseOrOpenRestrictedFunctionalMaterial(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        ItemStack item = event.getItem();
        if (item != null && !item.getType().isBlock()
                && plugin.isRestrictableFunctionalMaterial(item.getType())
                && !plugin.isFunctionalMaterialEnabled(item.getType())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessage("items.material-disabled", plugin.placeholders(
                    "material", formatMaterial(item.getType())
            )));
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }
        Material clickedType = clicked.getType();
        if (!plugin.isRestrictableFunctionalMaterial(clickedType) || plugin.isFunctionalMaterialEnabled(clickedType)) {
            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(plugin.getMessage("items.material-disabled", plugin.placeholders(
                "material", formatMaterial(clickedType)
        )));
    }

    private String formatMaterial(Material material) {
        String lower = material.name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        String[] parts = lower.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }
}
