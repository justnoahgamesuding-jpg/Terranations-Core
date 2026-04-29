package me.meetrow.testproject;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class FixedOreToolListener implements Listener {
    private final Testproject plugin;

    public FixedOreToolListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerUseFixedOreTool(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!plugin.isFixedOreTool(item)) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        event.setCancelled(true);
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (!plugin.isFixedOreMaterial(block.getType())) {
                player.sendMessage(plugin.colorize("&cThat block cannot be made into a fixed ore node."));
                return;
            }
            plugin.createFixedOre(block, block.getType());
            player.sendMessage(plugin.colorize("&aMarked &f" + plugin.formatMaterialName(block.getType()) + "&a as a fixed ore node."));
            return;
        }

        if (!plugin.deleteFixedOre(block)) {
            player.sendMessage(plugin.getMessage("terra.fixedore.tool-not-found"));
            return;
        }

        player.sendMessage(plugin.getMessage("terra.fixedore.tool-deleted", plugin.placeholders(
                "block", plugin.formatMaterialName(block.getType())
        )));
    }
}
