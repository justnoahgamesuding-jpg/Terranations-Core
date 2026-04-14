package me.meetrow.testproject;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ProfessionActionListener implements Listener {
    private final Testproject plugin;

    public ProfessionActionListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (plugin.bypassesProfessionRestrictions(player.getUniqueId())) {
            return;
        }
        Material material = event.getBlockPlaced().getType();

        if (isClimateSapling(material) && plugin.prepareProfessionRequirement(player.getUniqueId(), Profession.LUMBERJACK)
                && plugin.meetsProfessionRequirement(player.getUniqueId(), Profession.LUMBERJACK)
                && plugin.rollInstantGrowProc(player.getUniqueId(), Profession.LUMBERJACK)) {
            plugin.triggerLumberjackInstantGrow(event.getBlockPlaced());
        }

        if (isFarmerPlantable(material) && plugin.meetsProfessionRequirement(player.getUniqueId(), Profession.FARMER)) {
            double xp = plugin.getFarmerPlantXp();
            int awardedXp = plugin.rewardFractionalProfessionXp(player, Profession.FARMER, xp);
            if (awardedXp > 0) {
                player.sendMessage(plugin.getMessage("rewards.farmer.plant-xp", plugin.placeholders(
                        "xp", String.valueOf(awardedXp),
                        "block", plugin.formatMaterialName(material)
                )));
            }
            if (plugin.rollInstantGrowProc(player.getUniqueId(), Profession.FARMER)) {
                plugin.triggerFarmerInstantGrow(event.getBlockPlaced());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFarmerBonemealUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }
        if (event.getItem() == null || event.getItem().getType() != Material.BONE_MEAL) {
            return;
        }

        Player player = event.getPlayer();
        if (plugin.bypassesProfessionRestrictions(player.getUniqueId())) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (!isFarmerBonemealTarget(clickedBlock)) {
            return;
        }

        if (!plugin.meetsProfessionRequirement(player.getUniqueId(), Profession.FARMER)) {
            return;
        }

        double xp = plugin.getFarmerBonemealXp();
        int awardedXp = plugin.rewardFractionalProfessionXp(player, Profession.FARMER, xp);
        if (awardedXp > 0) {
            player.sendMessage(plugin.getMessage("rewards.farmer.bonemeal-xp", plugin.placeholders(
                    "xp", String.valueOf(awardedXp),
                    "block", plugin.formatMaterialName(clickedBlock.getType())
            )));
        }
        if (plugin.rollInstantGrowProc(player.getUniqueId(), Profession.FARMER)) {
            plugin.triggerFarmerInstantGrow(clickedBlock);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Player attacker = getAttackingPlayer(event.getDamager());
        if (attacker == null) {
            return;
        }
        if (plugin.bypassesProfessionRestrictions(attacker.getUniqueId())) {
            return;
        }
        if (!(event.getEntity() instanceof org.bukkit.entity.LivingEntity)) {
            return;
        }
        if (!(event.getEntity() instanceof Player target)) {
            return;
        }
        if (!isSword(attacker.getInventory().getItemInMainHand())) {
            return;
        }

        Country attackerCountry = plugin.getPlayerCountry(attacker.getUniqueId());
        Country targetCountry = plugin.getPlayerCountry(target.getUniqueId());
        if (attackerCountry == null || targetCountry == null) {
            return;
        }
        if (attackerCountry != targetCountry && !attackerCountry.getName().equalsIgnoreCase(targetCountry.getName())) {
            return;
        }

        event.setCancelled(true);
        attacker.sendMessage(plugin.getMessage("country.friendly-fire-sword-blocked"));
    }

    private boolean isFarmerPlantable(Material material) {
        return plugin.isFarmerCrop(material)
                || material == Material.NETHER_WART
                || material == Material.SWEET_BERRY_BUSH
                || material == Material.SUGAR_CANE
                || material == Material.CACTUS
                || material == Material.COCOA;
    }

    private boolean isClimateSapling(Material material) {
        return material == Material.OAK_SAPLING
                || material == Material.SPRUCE_SAPLING
                || material == Material.BIRCH_SAPLING
                || material == Material.JUNGLE_SAPLING
                || material == Material.ACACIA_SAPLING
                || material == Material.DARK_OAK_SAPLING
                || material == Material.CHERRY_SAPLING
                || material == Material.MANGROVE_PROPAGULE;
    }

    private boolean isFarmerBonemealTarget(Block block) {
        if (block == null) {
            return false;
        }
        Material material = block.getType();
        if (!(plugin.isFarmerCrop(material)
                || material == Material.NETHER_WART
                || material == Material.SWEET_BERRY_BUSH
                || material == Material.COCOA)) {
            return false;
        }
        if (!(block.getBlockData() instanceof Ageable ageable)) {
            return false;
        }
        return ageable.getAge() < ageable.getMaximumAge();
    }

    private Player getAttackingPlayer(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }

    private boolean isSword(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        String materialName = itemStack.getType().name();
        return materialName.endsWith("_SWORD");
    }
}
