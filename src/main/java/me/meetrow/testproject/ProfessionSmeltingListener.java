package me.meetrow.testproject;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ProfessionSmeltingListener implements Listener {
    private final Testproject plugin;

    public ProfessionSmeltingListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        Profession requiredProfession = plugin.getRequiredProfessionForBlock(event.getBlockState().getType());
        Profession profession = requiredProfession != null && plugin.meetsProfessionRequirement(player.getUniqueId(), requiredProfession)
                ? requiredProfession
                : null;
        List<ItemStack> collectedDrops = new ArrayList<>();
        boolean doubled = false;

        for (Item itemEntity : event.getItems()) {
            ItemStack itemStack = itemEntity.getItemStack();
            if (profession == Profession.MINER && plugin.isSmeltableOre(itemStack.getType()) && isOreBlock(event.getBlockState().getType())) {
                tagItem(itemStack, player.getUniqueId(), Profession.MINER);
                itemEntity.setItemStack(itemStack);
            } else if (profession == Profession.FARMER && plugin.isCookableFood(itemStack.getType())) {
                tagItem(itemStack, player.getUniqueId(), Profession.FARMER);
                itemEntity.setItemStack(itemStack);
            }
            plugin.applyClimateCropLore(itemStack);
            itemEntity.setItemStack(itemStack);

            collectedDrops.add(itemEntity.getItemStack().clone());
            if (profession != null
                    && profession == requiredProfession
                    && supportsDoubleDrop(profession)
                    && ThreadLocalRandom.current().nextDouble() < plugin.getProfessionDoubleDropChance(player.getUniqueId(), profession)) {
                collectedDrops.add(itemEntity.getItemStack().clone());
                doubled = true;
            }
            itemEntity.remove();
        }

        for (ItemStack drop : collectedDrops) {
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(drop);
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }

        if (doubled) {
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.notifyDoubleDrop(player, profession));
        }
    }

    private boolean supportsDoubleDrop(Profession profession) {
        return profession == Profession.MINER
                || profession == Profession.FARMER
                || profession == Profession.LUMBERJACK;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null || plugin.bypassesProfessionRestrictions(killer.getUniqueId())) {
            return;
        }
        if (!plugin.meetsProfessionRequirement(killer.getUniqueId(), Profession.FARMER)) {
            return;
        }

        for (ItemStack drop : event.getDrops()) {
            if (plugin.isCookableFood(drop.getType())) {
                tagItem(drop, killer.getUniqueId(), Profession.FARMER);
            }
            plugin.applyClimateCropLore(drop);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFurnaceOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (plugin.bypassesProfessionRestrictions(player.getUniqueId())) {
            return;
        }

        Location location = getCookingBlockLocation(event.getInventory().getType(), event.getInventory().getLocation());
        if (location == null) {
            return;
        }

        clearLockIfStationEmpty(location, event.getInventory());

        Testproject.FurnaceAccessResult access = plugin.getFurnaceAccess(location, player.getUniqueId());
        if (access == Testproject.FurnaceAccessResult.LOCKED_OTHER) {
            event.setCancelled(true);
            sendLockedMessage(player, location);
            return;
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFurnaceClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (plugin.bypassesProfessionRestrictions(player.getUniqueId())) {
            return;
        }

        InventoryType inventoryType = event.getView().getTopInventory().getType();
        Location location = getCookingBlockLocation(inventoryType, event.getView().getTopInventory().getLocation());
        if (location == null) {
            return;
        }

        clearLockIfStationEmpty(location, event.getView().getTopInventory());

        Testproject.FurnaceAccessResult access = plugin.getFurnaceAccess(location, player.getUniqueId());
        if (access == Testproject.FurnaceAccessResult.LOCKED_OTHER) {
            event.setCancelled(true);
            sendLockedMessage(player, location);
            return;
        }
        if (access == Testproject.FurnaceAccessResult.EXPIRED) {
            plugin.clearFurnaceSession(location);
            return;
        }

        if (event.getRawSlot() == 2) {
            handleExtraction(player, event, location);
            return;
        }

        ItemStack moving = null;
        if (event.getRawSlot() == 0) {
            moving = event.getCursor();
        } else if (event.getClick().isShiftClick() && event.getClickedInventory() != null
                && event.getClickedInventory().getType() == InventoryType.PLAYER) {
            moving = event.getCurrentItem();
        }

        if (moving != null) {
            enforceInputRestrictions(player, moving, inventoryType, event);
            if (!event.isCancelled() && isInputInsertion(event, moving)) {
                plugin.noteFurnaceProcessor(location, player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFurnaceDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (plugin.bypassesProfessionRestrictions(player.getUniqueId())) {
            return;
        }

        InventoryType inventoryType = event.getView().getTopInventory().getType();
        Location location = getCookingBlockLocation(inventoryType, event.getView().getTopInventory().getLocation());
        if (location == null) {
            return;
        }

        clearLockIfStationEmpty(location, event.getView().getTopInventory());

        Testproject.FurnaceAccessResult access = plugin.getFurnaceAccess(location, player.getUniqueId());
        if (access == Testproject.FurnaceAccessResult.LOCKED_OTHER) {
            event.setCancelled(true);
            sendLockedMessage(player, location);
            return;
        }

        if (event.getRawSlots().contains(0)) {
            enforceInputRestrictions(player, event.getOldCursor(), inventoryType, event);
            if (!event.isCancelled() && event.getNewItems().containsKey(0)) {
                plugin.noteFurnaceProcessor(location, player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        if (event.getResult() == null) {
            return;
        }

        plugin.activateFurnaceOutputLock(event.getBlock().getLocation());

        Profession sourceProfession = getTaggedSourceProfession(event.getSource());
        UUID sourcePlayerId = getTaggedSourceOwner(event.getSource());
        if (sourceProfession == null || sourcePlayerId == null) {
            return;
        }

        if (sourceProfession == Profession.MINER && plugin.getBlacksmithSmeltXp(event.getResult().getType()) > 0) {
            plugin.addFurnaceSmeltContribution(event.getBlock().getLocation(), Profession.MINER, sourcePlayerId, event.getResult().getAmount());
            return;
        }

        if (sourceProfession == Profession.FARMER && plugin.getFarmerCookingXp(event.getResult().getType()) > 0) {
            plugin.addFurnaceSmeltContribution(event.getBlock().getLocation(), Profession.FARMER, sourcePlayerId, event.getResult().getAmount());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFurnaceStartSmelt(FurnaceStartSmeltEvent event) {
        if (!plugin.isSmeltableOre(event.getSource().getType())) {
            return;
        }

        UUID processorId = plugin.getFurnaceAssignedOwnerId(event.getBlock().getLocation());
        if (processorId == null) {
            return;
        }

        if (!plugin.meetsProfessionRequirement(processorId, Profession.BLACKSMITH)
                && plugin.meetsProfessionRequirement(processorId, Profession.MINER)) {
            event.setTotalCookTime(event.getTotalCookTime() * 3);
        }
    }

    private void handleExtraction(Player player, InventoryClickEvent event, Location location) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType().isAir()) {
            return;
        }

        int amount = getExtractedAmount(event, currentItem);
        if (amount <= 0) {
            return;
        }

        if (plugin.meetsProfessionRequirement(player.getUniqueId(), Profession.BLACKSMITH)) {
            int blacksmithXp = plugin.getBlacksmithSmeltXp(currentItem.getType()) * amount;
            blacksmithXp = plugin.rewardProfessionXp(player, Profession.BLACKSMITH, blacksmithXp);
            if (blacksmithXp > 0) {
                player.sendMessage(plugin.getMessage("rewards.smelting.xp", plugin.placeholders(
                        "xp", String.valueOf(blacksmithXp),
                        "item", plugin.formatMaterialName(currentItem.getType())
                )));
            }

            int minerXpPerItem = plugin.getMinerSmeltCollaborationXp(currentItem.getType());
            if (minerXpPerItem > 0) {
                for (Map.Entry<UUID, Integer> entry : plugin.consumeFurnaceSmeltContributions(location, Profession.MINER, amount).entrySet()) {
                    int sharedXp = minerXpPerItem * entry.getValue();
                    sharedXp = plugin.rewardProfessionXp(entry.getKey(), Profession.MINER, sharedXp);
                    notifySharedSmeltingXp(entry.getKey(), sharedXp, player, currentItem.getType());
                }
            }
            clearLockIfExtractionLeavesStationEmpty(location, event.getView().getTopInventory(), currentItem, amount);
            return;
        }

        if (plugin.meetsProfessionRequirement(player.getUniqueId(), Profession.FARMER)) {
            int farmerXp = plugin.getFarmerCookingXp(currentItem.getType()) * amount;
            farmerXp = plugin.rewardProfessionXp(player, Profession.FARMER, farmerXp);
            if (farmerXp > 0) {
                player.sendMessage(plugin.getMessage("rewards.smelting.xp", plugin.placeholders(
                        "xp", String.valueOf(farmerXp),
                        "item", plugin.formatMaterialName(currentItem.getType())
                )));
            }

            int sourceXpPerItem = plugin.getFarmerCookCollaborationXp(currentItem.getType());
            if (sourceXpPerItem > 0) {
                for (Map.Entry<UUID, Integer> entry : plugin.consumeFurnaceSmeltContributions(location, Profession.FARMER, amount).entrySet()) {
                    int sharedXp = sourceXpPerItem * entry.getValue();
                    sharedXp = plugin.rewardProfessionXp(entry.getKey(), Profession.FARMER, sharedXp);
                    notifySharedSmeltingXp(entry.getKey(), sharedXp, player, currentItem.getType());
                }
            }
            clearLockIfExtractionLeavesStationEmpty(location, event.getView().getTopInventory(), currentItem, amount);
        }
    }

    private int getExtractedAmount(InventoryClickEvent event, ItemStack currentItem) {
        int available = currentItem.getAmount();
        if (available <= 0) {
            return 0;
        }

        if (event.getClick().isShiftClick()) {
            return available;
        }

        if (event.isRightClick()) {
            return 1;
        }

        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType().isAir()) {
            return available;
        }
        if (cursor.getType() != currentItem.getType()) {
            return 0;
        }
        if (!cursor.isSimilar(currentItem)) {
            return 0;
        }
        return Math.min(available, currentItem.getMaxStackSize() - cursor.getAmount());
    }

    private void enforceInputRestrictions(Player player, ItemStack moving, InventoryType inventoryType, org.bukkit.event.Cancellable event) {
        if (moving == null || moving.getType().isAir()) {
            return;
        }
    }

    private void tagItem(ItemStack itemStack, UUID playerId, Profession profession) {
        if (itemStack == null || playerId == null || profession == null) {
            return;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return;
        }
        itemMeta.getPersistentDataContainer().set(plugin.getItemSourceOwnerKey(), PersistentDataType.STRING, playerId.toString());
        itemMeta.getPersistentDataContainer().set(plugin.getItemSourceProfessionKey(), PersistentDataType.STRING, profession.name());
        itemStack.setItemMeta(itemMeta);
    }

    private UUID getTaggedSourceOwner(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return null;
        }
        String value = itemMeta.getPersistentDataContainer().get(plugin.getItemSourceOwnerKey(), PersistentDataType.STRING);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private Profession getTaggedSourceProfession(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return null;
        }
        return Profession.fromKey(itemMeta.getPersistentDataContainer().get(plugin.getItemSourceProfessionKey(), PersistentDataType.STRING));
    }

    private Location getCookingBlockLocation(InventoryType inventoryType, Location location) {
        if (location == null) {
            return null;
        }
        if (inventoryType != InventoryType.FURNACE
                && inventoryType != InventoryType.BLAST_FURNACE
                && inventoryType != InventoryType.SMOKER) {
            return null;
        }
        return location;
    }

    private boolean isOreBlock(Material material) {
        return switch (material) {
            case COAL_ORE, DEEPSLATE_COAL_ORE,
                 COPPER_ORE, DEEPSLATE_COPPER_ORE,
                 IRON_ORE, DEEPSLATE_IRON_ORE,
                 GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE,
                 REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE,
                 LAPIS_ORE, DEEPSLATE_LAPIS_ORE,
                 EMERALD_ORE, DEEPSLATE_EMERALD_ORE,
                 DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE,
                 ANCIENT_DEBRIS -> true;
            default -> false;
        };
    }

    private boolean isInputInsertion(InventoryClickEvent event, ItemStack moving) {
        if (moving == null || moving.getType().isAir()) {
            return false;
        }
        if (event.getRawSlot() == 0 && event.getClickedInventory() != null
                && event.getClickedInventory().getType() != InventoryType.PLAYER) {
            return true;
        }
        return event.getClick().isShiftClick()
                && event.getClickedInventory() != null
                && event.getClickedInventory().getType() == InventoryType.PLAYER;
    }

    private void clearLockIfStationEmpty(Location location, Inventory inventory) {
        if (inventory == null) {
            return;
        }
        ItemStack input = inventory.getItem(0);
        ItemStack output = inventory.getItem(2);
        if (isEmpty(input) && isEmpty(output)) {
            plugin.clearFurnaceOutputLock(location);
        }
    }

    private void clearLockIfExtractionLeavesStationEmpty(Location location, Inventory inventory, ItemStack currentItem, int amountTaken) {
        if (inventory == null) {
            plugin.clearFurnaceOutputLock(location);
            return;
        }

        ItemStack input = inventory.getItem(0);
        boolean inputEmpty = isEmpty(input);
        boolean outputWillBeEmpty = isEmpty(currentItem) || amountTaken >= currentItem.getAmount();
        if (inputEmpty && outputWillBeEmpty) {
            plugin.clearFurnaceOutputLock(location);
        }
    }

    private void sendLockedMessage(Player player, Location location) {
        String ownerName = plugin.getFurnaceLockOwnerName(location);
        player.sendMessage(plugin.getMessage("profession.furnace.locked", plugin.placeholders(
                "player", ownerName != null ? ownerName : "another player"
        )));
    }

    private boolean isEmpty(ItemStack itemStack) {
        return itemStack == null || itemStack.getType().isAir() || itemStack.getAmount() <= 0;
    }

    private void notifySharedSmeltingXp(UUID playerId, int xp, Player processor, Material itemType) {
        if (xp <= 0) {
            return;
        }

        Player target = plugin.getServer().getPlayer(playerId);
        if (target == null) {
            return;
        }

        target.sendMessage(plugin.getMessage("rewards.smelting.shared-xp", plugin.placeholders(
                "xp", String.valueOf(xp),
                "player", processor.getName(),
                "item", plugin.formatMaterialName(itemType)
        )));
    }
}
