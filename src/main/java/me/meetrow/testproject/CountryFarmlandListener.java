package me.meetrow.testproject;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CountryFarmlandListener implements Listener {
    private final Testproject plugin;

    public CountryFarmlandListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTillsSoil(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !isHoe(event.getItem())) {
            return;
        }
        Material originalType = clickedBlock.getType();
        if (!canTurnIntoFarmland(originalType)) {
            return;
        }
        if (plugin.bypassesProfessionRestrictions(event.getPlayer().getUniqueId())) {
            return;
        }

        if (!plugin.prepareProfessionRequirement(event.getPlayer().getUniqueId(), Profession.FARMER)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessage("profession.action-job-required", plugin.placeholders(
                    "profession", plugin.getProfessionPlainDisplayName(Profession.FARMER),
                    "action", "use this block",
                    "level", "1"
            )));
            return;
        }

        int farmerLevel = plugin.getProfessionLevel(event.getPlayer().getUniqueId(), Profession.FARMER);

        int actionRequiredLevel = originalType == Material.COARSE_DIRT
                ? plugin.getFarmerCoarseDirtConversionRequiredLevel()
                : plugin.getFarmerFarmlandCreationRequiredLevel();
        if (farmerLevel < actionRequiredLevel) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessage("profession.action-job-required", plugin.placeholders(
                    "profession", plugin.getProfessionPlainDisplayName(Profession.FARMER),
                    "action", originalType == Material.COARSE_DIRT ? "convert coarse dirt" : "create farmland",
                    "level", String.valueOf(actionRequiredLevel)
            )));
            return;
        }

        if (originalType != Material.COARSE_DIRT) {
            Country country = plugin.getCountryAt(clickedBlock.getLocation());
            if (country != null && country.hasTerritory()) {
                int limit = plugin.getCountryFarmlandLimit(country);
                int current = plugin.countCountryFarmlandBlocks(country);
                if (current >= limit) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(plugin.getMessage("country.farmland.limit-reached", plugin.placeholders(
                            "country", country.getName(),
                            "count", String.valueOf(current),
                            "limit", String.valueOf(limit)
                    )));
                    return;
                }
            }
        }

        Block targetBlock = clickedBlock;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Material updatedType = targetBlock.getType();
            if (originalType == Material.COARSE_DIRT && updatedType == Material.DIRT) {
                int awardedXp = plugin.rewardProfessionXp(event.getPlayer(), Profession.FARMER, plugin.getFarmerCoarseDirtConversionXp());
                if (awardedXp > 0) {
                    event.getPlayer().sendMessage(plugin.getMessage("rewards.farmer.till-xp", plugin.placeholders(
                            "xp", String.valueOf(awardedXp),
                            "block", plugin.formatMaterialName(originalType)
                    )));
                }
                return;
            }
            if (updatedType == Material.FARMLAND) {
                int awardedXp = plugin.rewardProfessionXp(event.getPlayer(), Profession.FARMER, plugin.getFarmerFarmlandCreationXp());
                if (awardedXp > 0) {
                    event.getPlayer().sendMessage(plugin.getMessage("rewards.farmer.till-xp", plugin.placeholders(
                            "xp", String.valueOf(awardedXp),
                            "block", plugin.formatMaterialName(originalType)
                    )));
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerUseWaterBucket(PlayerBucketEmptyEvent event) {
        if (event.getBucket() != Material.WATER_BUCKET) {
            return;
        }
        if (plugin.bypassesProfessionRestrictions(event.getPlayer().getUniqueId())) {
            return;
        }

        if (!plugin.prepareProfessionRequirement(event.getPlayer().getUniqueId(), Profession.FARMER)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessage("profession.action-job-required", plugin.placeholders(
                    "profession", plugin.getProfessionPlainDisplayName(Profession.FARMER),
                    "action", "use this item",
                    "level", "1"
            )));
            return;
        }

        if (plugin.getProfessionLevel(event.getPlayer().getUniqueId(), Profession.FARMER) < plugin.getFarmerWaterBucketRequiredLevel()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessage("profession.farmer.water-bucket-locked", plugin.placeholders(
                    "level", String.valueOf(plugin.getFarmerWaterBucketRequiredLevel()),
                    "profession", plugin.getProfessionPlainDisplayName(Profession.FARMER)
            )));
        }
    }

    private boolean isHoe(ItemStack itemStack) {
        return itemStack != null && itemStack.getType().name().endsWith("_HOE");
    }

    private boolean canTurnIntoFarmland(Material material) {
        return material == Material.DIRT
                || material == Material.COARSE_DIRT
                || material == Material.GRASS_BLOCK
                || material == Material.DIRT_PATH
                || material == Material.ROOTED_DIRT;
    }
}
