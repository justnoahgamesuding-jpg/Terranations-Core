package me.meetrow.testproject;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
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
        if (isDisabledNetherite(material)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessage("items.material-disabled", plugin.placeholders(
                    "material", formatMaterial(material)
            )));
            return;
        }
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
        if (isExplicitlyDisabledTechnology(item != null ? item.getType() : null)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessage("items.material-disabled", plugin.placeholders(
                    "material", formatMaterial(item.getType())
            )));
            return;
        }
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
        if (isExplicitlyDisabledTechnology(clickedType)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessage("items.material-disabled", plugin.placeholders(
                    "material", formatMaterial(clickedType)
            )));
            return;
        }
        if (!plugin.isRestrictableFunctionalMaterial(clickedType) || plugin.isFunctionalMaterialEnabled(clickedType)) {
            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(plugin.getMessage("items.material-disabled", plugin.placeholders(
                "material", formatMaterial(clickedType)
        )));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnterMinecart(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof org.bukkit.entity.Player player) || !(event.getVehicle() instanceof Minecart)) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(plugin.getMessage("items.material-disabled", plugin.placeholders(
                "material", "Minecart"
        )));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreakWithDisabledNetherite(BlockBreakEvent event) {
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        if (!isDisabledNetherite(tool != null ? tool.getType() : null)) {
            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(plugin.getMessage("items.material-disabled", plugin.placeholders(
                "material", formatMaterial(tool.getType())
        )));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageWithDisabledNetherite(EntityDamageByEntityEvent event) {
        Player player = extractPlayerDamager(event.getDamager());
        if (player == null) {
            return;
        }
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!isDisabledNetherite(weapon != null ? weapon.getType() : null)) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(plugin.getMessage("items.material-disabled", plugin.placeholders(
                "material", formatMaterial(weapon.getType())
        )));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        ItemStack result = event.getResult();
        if (result != null && isDisabledNetherite(result.getType())) {
            event.setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        boolean netheriteCurrent = isDisabledNetherite(current != null ? current.getType() : null);
        boolean netheriteCursor = isDisabledNetherite(cursor != null ? cursor.getType() : null);
        if (!netheriteCurrent && !netheriteCursor) {
            return;
        }
        boolean smithingResult = event.getView().getTopInventory().getType() == InventoryType.SMITHING && event.getRawSlot() == 3;
        boolean armorSlot = event.getSlotType() == InventoryType.SlotType.ARMOR;
        if (!smithingResult && !armorSlot) {
            return;
        }
        event.setCancelled(true);
        if (event.getWhoClicked() instanceof Player player) {
            Material material = netheriteCursor ? cursor.getType() : current.getType();
            player.sendMessage(plugin.getMessage("items.material-disabled", plugin.placeholders(
                    "material", formatMaterial(material)
            )));
        }
    }

    private boolean isExplicitlyDisabledTechnology(Material material) {
        if (material == null) {
            return false;
        }
        return isDisabledRedstone(material) || material.name().contains("MINECART") || isDisabledNetherite(material);
    }

    private boolean isDisabledRedstone(Material material) {
        String name = material.name();
        return name.contains("REDSTONE")
                || material == Material.REPEATER
                || material == Material.COMPARATOR
                || material == Material.OBSERVER
                || material == Material.PISTON
                || material == Material.STICKY_PISTON
                || material == Material.DISPENSER
                || material == Material.DROPPER
                || material == Material.LEVER
                || material == Material.DAYLIGHT_DETECTOR
                || material == Material.TRIPWIRE_HOOK
                || material == Material.TARGET
                || material == Material.SCULK_SENSOR
                || material == Material.CALIBRATED_SCULK_SENSOR
                || material == Material.REDSTONE_LAMP
                || material == Material.NOTE_BLOCK
                || material == Material.LIGHTNING_ROD;
    }

    private boolean isDisabledNetherite(Material material) {
        return material != null && (material.name().startsWith("NETHERITE_") || material == Material.ANCIENT_DEBRIS);
    }

    private Player extractPlayerDamager(Entity entity) {
        if (entity instanceof Player player) {
            return player;
        }
        if (entity instanceof AbstractArrow arrow && arrow.getShooter() instanceof Player player) {
            return player;
        }
        return null;
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
