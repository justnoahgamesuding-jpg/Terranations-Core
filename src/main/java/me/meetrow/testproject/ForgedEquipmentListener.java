package me.meetrow.testproject;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class ForgedEquipmentListener implements Listener {
    private final Testproject plugin;

    public ForgedEquipmentListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        ItemStack itemStack = event.getItem();
        if (!plugin.isForgedItem(itemStack)) {
            return;
        }

        ForgedRarity rarity = plugin.getForgedItemRarity(itemStack);
        int itemLevel = plugin.getForgedDisplayLevel(itemStack);
        int protectionPercent = plugin.getForgedDurabilityProtectionPercent(rarity, itemLevel);
        if (ThreadLocalRandom.current().nextInt(100) < protectionPercent) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (!plugin.isForgedItem(itemStack) || !itemStack.getType().name().endsWith("_PICKAXE")) {
            return;
        }
        if (!plugin.isOreMaterial(event.getBlock().getType())) {
            return;
        }

        ForgedRarity rarity = plugin.getForgedItemRarity(itemStack);
        if (rarity == null) {
            return;
        }

        if (rarity == ForgedRarity.LEGENDARY && !plugin.hasLegendaryPickaxeBoost(player.getUniqueId())) {
            if (ThreadLocalRandom.current().nextDouble() <= 0.12D) {
                plugin.activateLegendaryPickaxeBoost(player);
            }
        }

        if (!plugin.hasLegendaryPickaxeBoost(player.getUniqueId())) {
            return;
        }

        ItemStack boostedTool = itemStack.clone();
        boostedTool.addUnsafeEnchantment(Enchantment.FORTUNE, 3);
        Collection<ItemStack> normalDrops = event.getBlock().getDrops(itemStack, player);
        Collection<ItemStack> boostedDrops = event.getBlock().getDrops(boostedTool, player);
        dropDifference(player, normalDrops, boostedDrops);
    }

    private void dropDifference(Player player, Collection<ItemStack> normalDrops, Collection<ItemStack> boostedDrops) {
        if (boostedDrops == null || boostedDrops.isEmpty()) {
            return;
        }
        for (ItemStack boostedDrop : boostedDrops) {
            if (boostedDrop == null || boostedDrop.getType().isAir()) {
                continue;
            }
            int baseAmount = getMatchingAmount(normalDrops, boostedDrop.getType());
            int extraAmount = Math.max(0, boostedDrop.getAmount() - baseAmount);
            if (extraAmount <= 0) {
                continue;
            }
            ItemStack extraDrop = boostedDrop.clone();
            extraDrop.setAmount(extraAmount);
            player.getWorld().dropItemNaturally(player.getLocation(), extraDrop);
        }
    }

    private int getMatchingAmount(Collection<ItemStack> drops, Material material) {
        if (drops == null || material == null) {
            return 0;
        }
        int amount = 0;
        for (ItemStack drop : drops) {
            if (drop != null && drop.getType() == material) {
                amount += drop.getAmount();
            }
        }
        return amount;
    }
}
