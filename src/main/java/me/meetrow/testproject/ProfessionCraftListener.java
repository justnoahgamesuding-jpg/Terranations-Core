package me.meetrow.testproject;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class ProfessionCraftListener implements Listener {
    private final Testproject plugin;

    public ProfessionCraftListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack result = event.getRecipe() != null ? event.getRecipe().getResult() : null;
        Material resultType = result != null ? result.getType() : null;
        if (resultType == null) {
            return;
        }
        event.setCurrentItem(plugin.applyUsageRequirementLore(event.getCurrentItem()));
        if (plugin.bypassesProfessionRestrictions(player.getUniqueId())) {
            return;
        }

        if (plugin.isBlacksmithForgeRecipe(resultType) || plugin.isForgeManagedEquipment(resultType)) {
            event.setCancelled(true);
            player.sendMessage(plugin.colorize("&cForge " + plugin.formatMaterialName(resultType) + " at an anvil instead of the crafting table."));
            return;
        }

        if (plugin.isFarmerCraftFood(resultType)) {
            if (!plugin.tryConsumeSharedActionCooldown(player, plugin.meetsProfessionRequirement(player.getUniqueId(), Profession.FARMER) ? Profession.FARMER : null)) {
                event.setCancelled(true);
                return;
            }
            if (plugin.meetsProfessionRequirement(player.getUniqueId(), Profession.FARMER)) {
                int craftedAmount = getCraftedAmount(event, result);
                int awardedXp = plugin.rewardProfessionXp(player, Profession.FARMER, plugin.getFarmerCraftXp(resultType) * craftedAmount);
                if (awardedXp > 0) {
                    player.sendMessage(plugin.getMessage("rewards.farmer.craft-xp", plugin.placeholders(
                            "xp", String.valueOf(awardedXp),
                            "amount", String.valueOf(craftedAmount),
                            "item", plugin.formatMaterialName(resultType)
                    )));
                }
            }
            return;
        }

        if (!plugin.isBlacksmithCraft(resultType) || plugin.isForgeManagedEquipment(resultType)) {
            return;
        }

        if (!plugin.tryConsumeSharedActionCooldown(player, plugin.meetsProfessionRequirement(player.getUniqueId(), Profession.BLACKSMITH) ? Profession.BLACKSMITH : null)) {
            event.setCancelled(true);
            return;
        }

        if (plugin.meetsProfessionRequirement(player.getUniqueId(), Profession.BLACKSMITH)) {
            plugin.rewardProfessionXp(player, Profession.BLACKSMITH, plugin.getBlacksmithCraftXp(resultType));
        }
    }

    private int getCraftedAmount(CraftItemEvent event, ItemStack result) {
        if (result == null || result.getType().isAir()) {
            return 0;
        }

        int resultAmount = Math.max(1, result.getAmount());
        if (!event.isShiftClick()) {
            return resultAmount;
        }

        if (!(event.getInventory() instanceof CraftingInventory craftingInventory)) {
            return resultAmount;
        }

        int operationsByIngredients = Integer.MAX_VALUE;
        for (ItemStack ingredient : craftingInventory.getMatrix()) {
            if (ingredient == null || ingredient.getType().isAir()) {
                continue;
            }
            operationsByIngredients = Math.min(operationsByIngredients, ingredient.getAmount());
        }
        if (operationsByIngredients == Integer.MAX_VALUE) {
            operationsByIngredients = 1;
        }

        int maxStack = result.getMaxStackSize();
        int spaceForResult = 0;
        for (ItemStack storageItem : event.getWhoClicked().getInventory().getStorageContents()) {
            if (storageItem == null || storageItem.getType().isAir()) {
                spaceForResult += maxStack;
                continue;
            }
            if (storageItem.isSimilar(result)) {
                spaceForResult += Math.max(0, maxStack - storageItem.getAmount());
            }
        }

        int operationsBySpace = Math.max(1, spaceForResult / resultAmount);
        int operations = Math.max(1, Math.min(operationsByIngredients, operationsBySpace));
        return operations * resultAmount;
    }
}
