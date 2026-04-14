package me.meetrow.testproject;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class BlockDelayListener implements Listener {
    private static final long COOLDOWN_MESSAGE_INTERVAL_MILLIS = 2000L;

    private final Testproject plugin;

    public BlockDelayListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (plugin.hasBlockDelayBypass(player.getUniqueId())) {
            return;
        }

        boolean bypassProfessionRestrictions = plugin.bypassesProfessionRestrictions(player.getUniqueId());

        boolean awardBreakRewards = plugin.shouldAwardBreakRewards(player, event.getBlock());
        Material material = event.getBlock().getType();
        Profession requiredProfession = plugin.getRequiredProfessionForBlock(material);
        Profession actionProfession = plugin.resolveProfessionForRequirement(player.getUniqueId(), requiredProfession);
        if (!bypassProfessionRestrictions && requiredProfession != null && !plugin.meetsProfessionRequirement(player.getUniqueId(), requiredProfession)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("profession.block-job-required", plugin.placeholders(
                    "block", plugin.formatMaterialName(material),
                    "profession", plugin.getProfessionPlainDisplayName(requiredProfession),
                    "level", String.valueOf(plugin.getRequiredProfessionLevelForBlock(requiredProfession, material))
            )));
            return;
        }

        if (plugin.isFarmerCrop(material) && !isHoe(player.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("general.tool-required", plugin.placeholders(
                    "block", plugin.formatMaterialName(material),
                    "tool", "a hoe"
            )));
            return;
        }
        RequiredTool requiredTool = getRequiredTool(material);
        if (requiredTool != null && !isRequiredTool(player.getInventory().getItemInMainHand(), requiredTool)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessage("general.tool-required", plugin.placeholders(
                    "block", plugin.formatMaterialName(material),
                    "tool", requiredTool.displayName
            )));
            return;
        }
        if (!bypassProfessionRestrictions && requiredProfession != null && plugin.meetsProfessionRequirement(player.getUniqueId(), requiredProfession)) {
            int level = plugin.getProfessionLevel(player.getUniqueId(), requiredProfession);
            if (!plugin.canProfessionBreak(player.getUniqueId(), requiredProfession, material, level)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessage("profession.block-level-locked", plugin.placeholders(
                        "block", plugin.formatMaterialName(material),
                        "level", String.valueOf(plugin.getRequiredProfessionLevelForBlock(requiredProfession, material)),
                        "profession", plugin.getProfessionPlainDisplayName(requiredProfession)
                )));
                return;
            }
        }

        if (actionProfession != null) {
            plugin.noteProfessionAction(player.getUniqueId(), actionProfession);
        }

        int cooldownSeconds = plugin.getSharedActionCooldownSeconds(player.getUniqueId());
        if (!handleAction(player, event, cooldownSeconds, true)) {
            return;
        }

        boolean fixedOreBlock = plugin.isFixedOreBlock(event.getBlock());
        if (!fixedOreBlock && plugin.getCountryAt(event.getBlock().getLocation()) == null) {
            plugin.scheduleWildernessBlockRestore(event.getBlock(), material);
        }

        plugin.awardStabilityMeterProgress(player, event.getBlock(), true);
        plugin.clearPlacedBlockOwner(event.getBlock());
        applyCropHoeDurabilityDamage(player, material);

        if (!awardBreakRewards) {
            return;
        }

        boolean fullyGrownCrop = plugin.isFullyGrownCrop(event.getBlock());
        BlockReward reward = plugin.getBlockReward(material);
        int xpAward = 0;

        if (requiredProfession != null && plugin.meetsProfessionRequirement(player.getUniqueId(), requiredProfession)) {
            int level = plugin.getProfessionLevel(player.getUniqueId(), requiredProfession);
            xpAward = plugin.getProfessionBlockXp(requiredProfession, material, level);
            if (requiredProfession == Profession.FARMER && plugin.isFarmerCrop(material) && !fullyGrownCrop) {
                xpAward = Math.min(xpAward, 1);
            }
            xpAward = plugin.rewardProfessionXp(player, requiredProfession, xpAward);
        } else if (actionProfession != null) {
            xpAward = reward.xp();
            xpAward = plugin.rewardProfessionXp(player, actionProfession, xpAward);
        }

        if (!plugin.areBlockRewardsEnabled()) {
            return;
        }

        boolean paidMoney = false;
        if (reward.money() > 0.0D && plugin.hasEconomy() && plugin.areBlockMoneyRewardsEnabled()) {
            double rewardMoney = reward.money()
                    * plugin.getCountryPassiveMoneyMultiplier(plugin.getPlayerCountry(player.getUniqueId()));
            plugin.depositBalance(player.getUniqueId(), rewardMoney);
            paidMoney = true;
            player.sendMessage(plugin.getMessage("rewards.break.money-and-xp", plugin.placeholders(
                    "xp", String.valueOf(xpAward),
                    "money", plugin.formatMoney(rewardMoney),
                    "block", plugin.formatMaterialName(material)
            )));
            return;
        }

        player.sendMessage(plugin.getMessage("rewards.break.xp-only", plugin.placeholders(
                "xp", String.valueOf(xpAward),
                "block", plugin.formatMaterialName(material)
        )));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFixedOreBreak(BlockBreakEvent event) {
        if (plugin.isFixedOreBlock(event.getBlock())) {
            plugin.handleFixedOreBreak(event.getBlock());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.hasBlockDelayBypass(event.getPlayer().getUniqueId())) {
            return;
        }

        if (!plugin.canPlacePlayerStructureBlock(event.getBlockPlaced(), event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessage("stability.place-blocked"));
            return;
        }

        int placeCooldownSeconds = plugin.getSharedActionCooldownSeconds(event.getPlayer().getUniqueId());
        if (!handleAction(event.getPlayer(), event, placeCooldownSeconds, false)) {
            return;
        }

        if (plugin.getCountryAt(event.getBlock().getLocation()) == null) {
            plugin.scheduleWildernessPlacedBlockRemoval(event.getBlock(), event.getBlockPlaced().getType());
        }

        plugin.markPlacedBlock(event.getBlockPlaced(), event.getPlayer().getUniqueId());
        plugin.awardStabilityMeterProgress(event.getPlayer(), event.getBlockPlaced(), false);
    }

    private boolean handleAction(Player player, org.bukkit.event.Cancellable event, int cooldownSeconds, boolean breakAction) {
        if (!plugin.isBlockDelayEnabled()) {
            return true;
        }

        long now = System.currentTimeMillis();
        long cooldownEnd = breakAction
                ? plugin.getBreakCooldownEnd(player.getUniqueId())
                : plugin.getPlaceCooldownEnd(player.getUniqueId());

        if (cooldownEnd > now && plugin.shouldEnforceBlockActionCooldown(player.getUniqueId(), breakAction)) {
            long millisLeft = cooldownEnd - now;
            long secondsLeft = (long) Math.ceil(millisLeft / 1000.0D);
            event.setCancelled(true);
            long lastMessageTime = breakAction
                    ? plugin.getLastBreakCooldownMessageTime(player.getUniqueId())
                    : plugin.getLastPlaceCooldownMessageTime(player.getUniqueId());
            if (now - lastMessageTime >= COOLDOWN_MESSAGE_INTERVAL_MILLIS) {
                player.sendMessage(plugin.getMessage(
                        breakAction ? "block-delay.break-cooldown" : "block-delay.place-cooldown",
                        plugin.placeholders("seconds", String.valueOf(secondsLeft))
                ));
                if (breakAction) {
                    plugin.setLastBreakCooldownMessageTime(player.getUniqueId(), now);
                } else {
                    plugin.setLastPlaceCooldownMessageTime(player.getUniqueId(), now);
                }
            }
            return false;
        }

        long nextCooldown = now + (Math.max(0, cooldownSeconds) * 1000L);
        if (breakAction) {
            plugin.setBreakCooldown(player.getUniqueId(), nextCooldown);
        } else {
            plugin.setPlaceCooldown(player.getUniqueId(), nextCooldown);
        }
        plugin.noteSuccessfulBlockAction(player.getUniqueId(), breakAction);
        return true;
    }

    private boolean isMinerTool(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        String materialName = itemStack.getType().name();
        return materialName.endsWith("_PICKAXE") || materialName.endsWith("_SHOVEL");
    }

    private RequiredTool getRequiredTool(Material material) {
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
        if (itemStack == null) {
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

    private void applyCropHoeDurabilityDamage(Player player, Material material) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!plugin.isFarmerCrop(material) || !isHoe(mainHand) || player.getGameMode() == org.bukkit.GameMode.CREATIVE) {
            return;
        }
        player.damageItemStack(EquipmentSlot.HAND, 1);
    }

    private enum RequiredTool {
        AXE("an axe"),
        PICKAXE("a pickaxe"),
        SHOVEL("a shovel");

        private final String displayName;

        RequiredTool(String displayName) {
            this.displayName = displayName;
        }
    }
}
