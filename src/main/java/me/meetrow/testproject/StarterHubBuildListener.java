package me.meetrow.testproject;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StarterHubBuildListener implements Listener {
    private static final long BLOCKED_MESSAGE_INTERVAL_MILLIS = 1500L;

    private final Testproject plugin;
    private final Map<UUID, Long> lastBlockedMessageAt = new ConcurrentHashMap<>();

    public StarterHubBuildListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!shouldProtect(event.getPlayer(), event.getBlockPlaced())) {
            return;
        }
        if (plugin.isStarterHubAllowedCropBlock(event.getBlockPlaced().getType())) {
            if (canOverridePlaceCancellation(event.getPlayer())) {
                event.setCancelled(false);
            }
            return;
        }
        event.setCancelled(true);
        sendBlockedMessage(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!shouldProtect(event.getPlayer(), event.getBlock())) {
            return;
        }
        if (plugin.isStarterHubAllowedCropBlock(event.getBlock().getType())
                || plugin.isFixedOreBlock(event.getBlock())) {
            if (canOverrideBreakCancellation(event.getPlayer(), event.getBlock())) {
                event.setCancelled(false);
            }
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProtectedBlockUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (!shouldProtect(event.getPlayer(), clickedBlock)) {
            return;
        }
        if (clickedBlock != null && canUseCropInteraction(clickedBlock)) {
            event.setCancelled(false);
            event.setUseInteractedBlock(Event.Result.ALLOW);
            return;
        }
        if (clickedBlock != null && plugin.isStarterHubAllowedTerraWorkbenchInteraction(clickedBlock)) {
            event.setCancelled(false);
            event.setUseInteractedBlock(Event.Result.ALLOW);
            return;
        }
        if (event.isCancelled() || event.useInteractedBlock() == Event.Result.DENY) {
            event.setCancelled(true);
            sendBlockedMessage(event.getPlayer());
        }
    }

    private boolean shouldProtect(Player player, Block block) {
        return player != null
                && block != null
                && plugin.isStarterHubBuildProtected(block.getLocation())
                && !plugin.canBypassStarterHubBuildProtection(player);
    }

    private void sendBlockedMessage(Player player) {
        if (player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        long last = lastBlockedMessageAt.getOrDefault(player.getUniqueId(), 0L);
        if ((now - last) < BLOCKED_MESSAGE_INTERVAL_MILLIS) {
            return;
        }
        lastBlockedMessageAt.put(player.getUniqueId(), now);
        player.sendMessage(plugin.getMessage("tutorial.starter-hub.build-blocked"));
    }

    private boolean canUseCropInteraction(Block clickedBlock) {
        if (clickedBlock == null) {
            return false;
        }
        Material type = clickedBlock.getType();
        return type == Material.FARMLAND || plugin.isStarterHubAllowedCropBlock(type);
    }

    private boolean canOverrideBreakCancellation(Player player, Block block) {
        if (player == null || block == null) {
            return false;
        }
        if (plugin.hasBlockDelayBypass(player.getUniqueId()) || !plugin.isBlockDelayEnabled()) {
            return true;
        }
        Material material = block.getType();
        if (plugin.isFarmerCrop(material) && !isHoe(player.getInventory().getItemInMainHand())) {
            return false;
        }
        RequiredTool requiredTool = getRequiredTool(material);
        if (requiredTool != null && !isRequiredTool(player.getInventory().getItemInMainHand(), requiredTool)) {
            return false;
        }
        long cooldownEnd = plugin.getBreakCooldownEnd(player.getUniqueId());
        return cooldownEnd <= System.currentTimeMillis()
                || !plugin.shouldEnforceBlockActionCooldown(player.getUniqueId(), true);
    }

    private boolean canOverridePlaceCancellation(Player player) {
        if (player == null) {
            return false;
        }
        if (plugin.hasBlockDelayBypass(player.getUniqueId()) || !plugin.isBlockDelayEnabled()) {
            return true;
        }
        long cooldownEnd = plugin.getPlaceCooldownEnd(player.getUniqueId());
        return cooldownEnd <= System.currentTimeMillis()
                || !plugin.shouldEnforceBlockActionCooldown(player.getUniqueId(), false);
    }

    private RequiredTool getRequiredTool(Material material) {
        if (material == null) {
            return null;
        }
        if (Tag.LOGS.isTagged(material) || material.name().endsWith("_WOOD") || material.name().endsWith("_HYPHAE")) {
            return RequiredTool.AXE;
        }
        if (Tag.MINEABLE_PICKAXE.isTagged(material)) {
            return RequiredTool.PICKAXE;
        }
        if (Tag.MINEABLE_SHOVEL.isTagged(material)) {
            return RequiredTool.SHOVEL;
        }
        return null;
    }

    private boolean isRequiredTool(ItemStack itemStack, RequiredTool requiredTool) {
        if (itemStack == null || requiredTool == null) {
            return false;
        }
        String materialName = itemStack.getType().name();
        return switch (requiredTool) {
            case AXE -> materialName.endsWith("_AXE");
            case PICKAXE -> materialName.endsWith("_PICKAXE");
            case SHOVEL -> materialName.endsWith("_SHOVEL");
        };
    }

    private boolean isHoe(ItemStack itemStack) {
        return itemStack != null && itemStack.getType().name().endsWith("_HOE");
    }

    private enum RequiredTool {
        AXE,
        PICKAXE,
        SHOVEL
    }
}
