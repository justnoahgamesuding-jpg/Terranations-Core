package me.meetrow.testproject;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ClimateGuiListener implements Listener {
    private final Testproject plugin;

    public ClimateGuiListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        InventoryHolder holder = topInventory.getHolder();
        if (!(holder instanceof ClimateCropGuideHolder)) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() != topInventory || event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
            return;
        }

        ItemMeta meta = event.getCurrentItem().getItemMeta();
        String profileKey = meta != null
                ? meta.getPersistentDataContainer().get(plugin.getClimateCropLoreKey(), PersistentDataType.STRING)
                : null;
        CropClimateProfile profile = plugin.getClimateCropProfile(profileKey);
        if (profile == null) {
            profile = plugin.getClimateCropProfile(event.getCurrentItem().getType());
        }
        if (profile == null) {
            return;
        }

        ItemStack sample = event.getCurrentItem().clone();
        sample = plugin.refreshDescriptiveItemDetails(sample);
        player.getInventory().addItem(sample);
        player.sendMessage(plugin.colorize("&aAdded a climate guide sample for &f" + plugin.formatMaterialName(sample.getType()) + "&a."));
    }

    public record ClimateCropGuideHolder() implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
