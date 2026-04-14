package me.meetrow.testproject;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
        if (!plugin.isForgedItem(itemStack)) {
            return;
        }

        String toolName = itemStack.getType().name();
        ForgedRarity rarity = plugin.getForgedItemRarity(itemStack);
        if (rarity == null) {
            return;
        }

        if (toolName.endsWith("_PICKAXE")) {
            handlePickaxeBreak(event, player, itemStack, rarity);
            return;
        }
        if (toolName.endsWith("_AXE")) {
            handleAxeBreak(event, player, rarity);
            return;
        }
        if (toolName.endsWith("_HOE")) {
            handleHoeBreak(event, player, rarity);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (plugin.isForgedItem(weapon) && weapon.getType().name().endsWith("_SWORD")) {
            ForgedRarity rarity = plugin.getForgedItemRarity(weapon);
            if (rarity != null) {
                applySwordPerk(player, rarity);
            }
        }

        if (!(event.getEntity() instanceof Player target)) {
            return;
        }
        applyArmorPerk(target);
    }

    private void handlePickaxeBreak(BlockBreakEvent event, Player player, ItemStack itemStack, ForgedRarity rarity) {
        if (!plugin.isOreMaterial(event.getBlock().getType())) {
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

    private void handleAxeBreak(BlockBreakEvent event, Player player, ForgedRarity rarity) {
        if (!isLogLike(event.getBlock())) {
            return;
        }
        if (rarity == ForgedRarity.LEGENDARY && ThreadLocalRandom.current().nextDouble() <= 0.15D) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 20, 1, true, false, true));
            player.sendActionBar(plugin.legacyComponent("&6Timber Rush"));
            return;
        }
        if (rarity == ForgedRarity.EPIC && ThreadLocalRandom.current().nextDouble() <= 0.12D) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 12, 0, true, false, true));
            player.sendActionBar(plugin.legacyComponent("&5Heavy Swing"));
        }
    }

    private void handleHoeBreak(BlockBreakEvent event, Player player, ForgedRarity rarity) {
        if (!plugin.isFarmerCrop(event.getBlock().getType())) {
            return;
        }
        if (rarity == ForgedRarity.LEGENDARY && ThreadLocalRandom.current().nextDouble() <= 0.15D) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 1, true, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 8, 0, true, false, true));
            player.sendActionBar(plugin.legacyComponent("&6Harvest Bloom"));
            return;
        }
        if (rarity == ForgedRarity.EPIC && ThreadLocalRandom.current().nextDouble() <= 0.12D) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 12, 0, true, false, true));
            player.sendActionBar(plugin.legacyComponent("&5Field Step"));
        }
    }

    private void applySwordPerk(Player player, ForgedRarity rarity) {
        if (rarity == ForgedRarity.LEGENDARY && ThreadLocalRandom.current().nextDouble() <= 0.12D) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 8, 0, true, false, true));
            player.sendActionBar(plugin.legacyComponent("&6Battle Rhythm"));
            return;
        }
        if (rarity == ForgedRarity.EPIC && ThreadLocalRandom.current().nextDouble() <= 0.10D) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 6, 0, true, false, true));
            player.sendActionBar(plugin.legacyComponent("&5Edge Flow"));
        }
    }

    private void applyArmorPerk(Player target) {
        ForgedRarity bestArmorRarity = getBestForgedArmorRarity(target);
        if (bestArmorRarity == null) {
            return;
        }
        if (bestArmorRarity == ForgedRarity.LEGENDARY && ThreadLocalRandom.current().nextDouble() <= 0.10D) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 8, 0, true, false, true));
            target.sendActionBar(plugin.legacyComponent("&6Bulwark"));
            return;
        }
        if (bestArmorRarity == ForgedRarity.EPIC && ThreadLocalRandom.current().nextDouble() <= 0.10D) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 6, 0, true, false, true));
            target.sendActionBar(plugin.legacyComponent("&5Guardstep"));
        }
    }

    private ForgedRarity getBestForgedArmorRarity(Player player) {
        ForgedRarity best = null;
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            if (!plugin.isForgedItem(armorPiece)) {
                continue;
            }
            ForgedRarity rarity = plugin.getForgedItemRarity(armorPiece);
            if (rarity == null) {
                continue;
            }
            if (best == null || rarity.ordinal() > best.ordinal()) {
                best = rarity;
            }
        }
        return best;
    }

    private boolean isLogLike(Block block) {
        if (block == null) {
            return false;
        }
        String name = block.getType().name();
        return name.endsWith("_LOG") || name.endsWith("_WOOD") || name.endsWith("_HYPHAE");
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
