package me.meetrow.testproject;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
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
        if (result == null || result.getType().isAir()) {
            return;
        }
        if (plugin.hasCraftingBypass(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(plugin.colorize("&cVanilla crafting is disabled. &7Use Terra workbenches for all recipes."));
    }
}
