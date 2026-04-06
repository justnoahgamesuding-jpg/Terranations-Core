package me.meetrow.testproject;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MerchantShopListener implements Listener {
    private static final int MENU_SIZE = 54;
    private static final int ADMIN_MENU_SIZE = 27;
    private static final int ROTATION_MENU_SIZE = 27;
    private static final int OFFER_LIST_SIZE = 36;
    private static final int INFO_SLOT = 4;
    private static final int CLOSE_SLOT = 49;
    private static final int[] BUY_SLOTS = {19, 20, 21, 22, 23, 24, 25};
    private static final int[] SELL_SLOTS = {37, 38, 39, 40, 41};
    private static final int ADMIN_INFO_SLOT = 4;
    private static final int ADMIN_STATUS_SLOT = 11;
    private static final int ADMIN_SELLS_TO_PLAYERS_SLOT = 13;
    private static final int ADMIN_BUYS_FROM_PLAYERS_SLOT = 15;
    private static final int ADMIN_CLOSE_SLOT = 22;
    private static final int[] ADMIN_ROTATION_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int[] OFFER_LIST_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int OFFER_EDITOR_INFO_SLOT = 4;
    private static final int OFFER_EDITOR_MATERIAL_SLOT = 19;
    private static final int OFFER_EDITOR_AMOUNT_DOWN_SLOT = 20;
    private static final int OFFER_EDITOR_AMOUNT_UP_SLOT = 21;
    private static final int OFFER_EDITOR_PRICE_DOWN_SLOT = 23;
    private static final int OFFER_EDITOR_PRICE_UP_SLOT = 24;
    private static final int OFFER_EDITOR_STOCK_DOWN_SLOT = 29;
    private static final int OFFER_EDITOR_STOCK_UP_SLOT = 30;
    private static final int OFFER_EDITOR_BACK_SLOT = 49;

    private final Testproject plugin;

    public MerchantShopListener(Testproject plugin) {
        this.plugin = plugin;
    }

    public void openMerchantMenu(Player player, MerchantShopState state) {
        if (player == null || state == null) {
            return;
        }

        Inventory inventory = Bukkit.createInventory(
                new MerchantMenuHolder(player.getUniqueId(), state.getMerchantId()),
                MENU_SIZE,
                plugin.legacyComponent("&8Wandering Shop")
        );
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createInfoItem(state));
        List<MerchantShopOffer> buys = plugin.getMerchantBuyOffers();
        for (int i = 0; i < BUY_SLOTS.length && i < buys.size(); i++) {
            inventory.setItem(BUY_SLOTS[i], createBuyItem(buys.get(i)));
        }
        List<MerchantShopOffer> sells = plugin.getMerchantSellOffers();
        for (int i = 0; i < SELL_SLOTS.length && i < sells.size(); i++) {
            inventory.setItem(SELL_SLOTS[i], createSellItem(sells.get(i)));
        }
        inventory.setItem(CLOSE_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close the merchant menu.")));
        player.openInventory(inventory);
    }

    public void openMerchantAdminMenu(Player player) {
        if (player == null) {
            return;
        }
        Inventory inventory = Bukkit.createInventory(
                new MerchantAdminMenuHolder(player.getUniqueId()),
                ADMIN_MENU_SIZE,
                plugin.legacyComponent("&8Merchant Manager")
        );
        fillAdminMenuSlots(inventory);
        inventory.setItem(ADMIN_INFO_SLOT, createMerchantAdminInfoItem());
        inventory.setItem(ADMIN_STATUS_SLOT, createMerchantRuntimeItem());
        inventory.setItem(ADMIN_SELLS_TO_PLAYERS_SLOT, createSimpleItem(Material.EMERALD, "&aMerchant Sells To Players", List.of(
                "&7Edit each rotation of goods the",
                "&7merchant offers to players.",
                "&eClick to choose a rotation."
        )));
        inventory.setItem(ADMIN_BUYS_FROM_PLAYERS_SLOT, createSimpleItem(Material.CHEST, "&6Merchant Buys From Players", List.of(
                "&7Edit what players can sell to the",
                "&7merchant and the payout values.",
                "&eClick to open."
        )));
        inventory.setItem(ADMIN_CLOSE_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        player.openInventory(inventory);
    }

    private void openMerchantRotationMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(
                new MerchantBuyRotationListHolder(player.getUniqueId()),
                ROTATION_MENU_SIZE,
                plugin.legacyComponent("&8Merchant Rotations")
        );
        fillRotationSlots(inventory);
        inventory.setItem(INFO_SLOT, createSimpleItem(Material.EMERALD, "&aBuy Rotations", List.of(
                "&7Each rotation changes the goods the",
                "&7merchant sells during an active visit.",
                "&eClick a rotation to edit its offers."
        )));
        int rotations = plugin.getMerchantBuyRotationEditorCount();
        for (int i = 0; i < ADMIN_ROTATION_SLOTS.length && i < rotations; i++) {
            List<MerchantShopOffer> offers = plugin.getMerchantBuyOffersForRotation(i);
            inventory.setItem(ADMIN_ROTATION_SLOTS[i], createSimpleItem(Material.CLOCK, "&eRotation " + (i + 1), List.of(
                    "&7Offers: &f" + offers.size(),
                    "&eClick to edit"
            )));
        }
        inventory.setItem(ADMIN_CLOSE_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to merchant admin.")));
        player.openInventory(inventory);
    }

    private void openMerchantSellToPlayersOfferMenu(Player player, int rotationIndex) {
        Inventory inventory = Bukkit.createInventory(
                new MerchantBuyOfferListHolder(player.getUniqueId(), rotationIndex),
                OFFER_LIST_SIZE,
                plugin.legacyComponent("&8Sell Rotation " + (rotationIndex + 1))
        );
        fillOfferListSlots(inventory, true);
        inventory.setItem(INFO_SLOT, createSimpleItem(Material.EMERALD, "&aRotation " + (rotationIndex + 1), List.of(
                "&7Goods the merchant sells to players.",
                "&7Click an offer to edit item, amount,",
                "&7price, and shared stock."
        )));
        List<MerchantShopOffer> offers = plugin.getMerchantBuyOffersForRotation(rotationIndex);
        for (int i = 0; i < OFFER_LIST_SLOTS.length && i < offers.size(); i++) {
            MerchantShopOffer offer = offers.get(i);
            inventory.setItem(OFFER_LIST_SLOTS[i], createSimpleItem(offer.getMaterial(), "&a" + plugin.formatMaterialName(offer.getMaterial()), List.of(
                    "&7Amount: &f" + offer.getAmount(),
                    "&7Price: &f$" + plugin.formatMoney(offer.getPrice()),
                    "&7Stock: &f" + offer.getStock(),
                    "&eClick to edit"
            )));
        }
        inventory.setItem(ADMIN_CLOSE_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to rotations.")));
        player.openInventory(inventory);
    }

    private void openMerchantSellOfferMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(
                new MerchantSellOfferListHolder(player.getUniqueId()),
                OFFER_LIST_SIZE,
                plugin.legacyComponent("&8Merchant Buy List")
        );
        fillOfferListSlots(inventory, false);
        inventory.setItem(INFO_SLOT, createSimpleItem(Material.CHEST, "&6Merchant Buys From Players", List.of(
                "&7These are the goods players can sell",
                "&7to the merchant for money.",
                "&eClick an offer to edit it."
        )));
        List<MerchantShopOffer> offers = plugin.getMerchantSellOffers();
        for (int i = 0; i < OFFER_LIST_SLOTS.length && i < offers.size(); i++) {
            MerchantShopOffer offer = offers.get(i);
            inventory.setItem(OFFER_LIST_SLOTS[i], createSimpleItem(offer.getMaterial(), "&6" + plugin.formatMaterialName(offer.getMaterial()), List.of(
                    "&7Amount per trade: &f" + offer.getAmount(),
                    "&7Payout per item: &f$" + plugin.formatMoney(offer.getPrice()),
                    "&eClick to edit"
            )));
        }
        inventory.setItem(ADMIN_CLOSE_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to merchant admin.")));
        player.openInventory(inventory);
    }

    private void openMerchantOfferEditor(Player player, MerchantOfferEditorHolder holder) {
        MerchantShopOffer offer = holder.buy()
                ? plugin.getMerchantBuyOffer(holder.rotationIndex(), holder.offerIndex())
                : plugin.getMerchantSellOffer(holder.offerIndex());
        if (offer == null) {
            player.closeInventory();
            return;
        }
        Inventory inventory = Bukkit.createInventory(
                holder,
                MENU_SIZE,
                plugin.legacyComponent("&8Edit Merchant Offer")
        );
        fillEditorSlots(inventory, holder.buy());
        inventory.setItem(OFFER_EDITOR_INFO_SLOT, createSimpleItem(offer.getMaterial(), (holder.buy() ? "&a" : "&6") + plugin.formatMaterialName(offer.getMaterial()), List.of(
                "&7Amount: &f" + offer.getAmount(),
                "&7Price: &f$" + plugin.formatMoney(offer.getPrice()),
                holder.buy() ? "&7Stock: &f" + offer.getStock() : "&7Type: &fSell offer",
                "",
                "&7Use the item on your cursor or in your",
                "&7main hand, then click the material slot."
        )));
        inventory.setItem(22, createSimpleItem(offer.getMaterial(), "&fCurrent Good", List.of(
                "&7Currently edited material:",
                "&f" + plugin.formatMaterialName(offer.getMaterial())
        )));
        inventory.setItem(OFFER_EDITOR_MATERIAL_SLOT, createSimpleItem(Material.ITEM_FRAME, "&eSet Material From Hand", List.of(
                "&7Uses your cursor item first, then",
                "&7falls back to your main hand item.",
                "&eClick to apply."
        )));
        inventory.setItem(OFFER_EDITOR_AMOUNT_DOWN_SLOT, createSimpleItem(Material.RED_STAINED_GLASS_PANE, "&cAmount -1", List.of("&7Reduce the traded amount by 1.")));
        inventory.setItem(OFFER_EDITOR_AMOUNT_UP_SLOT, createSimpleItem(Material.LIME_STAINED_GLASS_PANE, "&aAmount +1", List.of("&7Increase the traded amount by 1.")));
        inventory.setItem(OFFER_EDITOR_PRICE_DOWN_SLOT, createSimpleItem(Material.RED_DYE, "&cPrice -1", List.of("&7Left click changes by 1.", "&7Shift click changes by 10.")));
        inventory.setItem(OFFER_EDITOR_PRICE_UP_SLOT, createSimpleItem(Material.LIME_DYE, "&aPrice +1", List.of("&7Left click changes by 1.", "&7Shift click changes by 10.")));
        if (holder.buy()) {
            inventory.setItem(OFFER_EDITOR_STOCK_DOWN_SLOT, createSimpleItem(Material.RED_TERRACOTTA, "&cStock -1", List.of("&7Reduce shared stock by 1.")));
            inventory.setItem(OFFER_EDITOR_STOCK_UP_SLOT, createSimpleItem(Material.LIME_TERRACOTTA, "&aStock +1", List.of("&7Increase shared stock by 1.")));
        }
        inventory.setItem(OFFER_EDITOR_BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the previous merchant menu.")));
        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMerchantInteract(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (plugin.cleanupStaleMerchantNpc(clicked)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessage("merchant.no-active"));
            return;
        }
        if (!plugin.isMerchantNpc(clicked)) {
            return;
        }
        event.setCancelled(true);
        openMerchantMenu(event.getPlayer(), plugin.getMerchantState(clicked));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMerchantDamage(EntityDamageEvent event) {
        if (plugin.cleanupStaleMerchantNpc(event.getEntity())) {
            event.setCancelled(true);
            return;
        }
        if (plugin.isMerchantNpc(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            plugin.cleanupStaleMerchantNpc(entity);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getView().getTopInventory().getHolder() instanceof MerchantMenuHolder holder)) {
            if (event.getView().getTopInventory().getHolder() instanceof MerchantInventoryHolder merchantHolder) {
                event.setCancelled(true);
                if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
                    return;
                }
                handleMerchantInventoryClick(player, merchantHolder, event);
            }
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }
        handleMerchantInventoryClick(player, holder, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof MerchantInventoryHolder) {
            event.setCancelled(true);
        }
    }

    private void handleMerchantInventoryClick(Player player, MerchantInventoryHolder holder, InventoryClickEvent event) {
        if (holder instanceof MerchantMenuHolder menuHolder) {
            handleMerchantShopClick(player, menuHolder, event.getSlot());
            return;
        }
        if (holder instanceof MerchantAdminMenuHolder) {
            handleMerchantAdminMenuClick(player, event);
            return;
        }
        if (holder instanceof MerchantBuyRotationListHolder) {
            if (event.getSlot() == ADMIN_CLOSE_SLOT) {
                openMerchantAdminMenu(player);
                return;
            }
            for (int i = 0; i < ADMIN_ROTATION_SLOTS.length; i++) {
                if (event.getSlot() == ADMIN_ROTATION_SLOTS[i]) {
                    openMerchantSellToPlayersOfferMenu(player, i);
                    return;
                }
            }
            return;
        }
        if (holder instanceof MerchantBuyOfferListHolder buyOfferListHolder) {
            if (event.getSlot() == ADMIN_CLOSE_SLOT) {
                openMerchantRotationMenu(player);
                return;
            }
            for (int i = 0; i < OFFER_LIST_SLOTS.length; i++) {
                if (event.getSlot() == OFFER_LIST_SLOTS[i]) {
                    openMerchantOfferEditor(player, new MerchantOfferEditorHolder(player.getUniqueId(), true, buyOfferListHolder.rotationIndex(), i));
                    return;
                }
            }
            return;
        }
        if (holder instanceof MerchantSellOfferListHolder) {
            if (event.getSlot() == ADMIN_CLOSE_SLOT) {
                openMerchantAdminMenu(player);
                return;
            }
            for (int i = 0; i < OFFER_LIST_SLOTS.length; i++) {
                if (event.getSlot() == OFFER_LIST_SLOTS[i]) {
                    openMerchantOfferEditor(player, new MerchantOfferEditorHolder(player.getUniqueId(), false, 0, i));
                    return;
                }
            }
            return;
        }
        if (holder instanceof MerchantOfferEditorHolder editorHolder) {
            handleMerchantOfferEditorClick(player, editorHolder, event);
        }
    }

    private void handleMerchantShopClick(Player player, MerchantMenuHolder holder, int slot) {
        MerchantShopState state = plugin.getMerchantStateByMerchantId(holder.merchantId());
        if (state == null) {
            player.closeInventory();
            player.sendMessage(plugin.getMessage("merchant.no-active"));
            return;
        }

        if (slot == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }

        for (int i = 0; i < BUY_SLOTS.length; i++) {
            if (slot == BUY_SLOTS[i]) {
                plugin.handleMerchantBuy(player, i);
                openMerchantMenu(player, state);
                return;
            }
        }
        for (int i = 0; i < SELL_SLOTS.length; i++) {
            if (slot == SELL_SLOTS[i]) {
                plugin.handleMerchantSell(player, i);
                openMerchantMenu(player, state);
                return;
            }
        }
    }

    private void handleMerchantAdminMenuClick(Player player, InventoryClickEvent event) {
        switch (event.getSlot()) {
            case ADMIN_CLOSE_SLOT -> player.closeInventory();
            case ADMIN_SELLS_TO_PLAYERS_SLOT -> openMerchantRotationMenu(player);
            case ADMIN_BUYS_FROM_PLAYERS_SLOT -> openMerchantSellOfferMenu(player);
            case ADMIN_STATUS_SLOT -> {
                if (event.isLeftClick()) {
                    plugin.spawnMerchantNow();
                } else if (event.isRightClick()) {
                    plugin.despawnMerchantNow();
                }
                openMerchantAdminMenu(player);
            }
            default -> {
            }
        }
    }

    private void handleMerchantOfferEditorClick(Player player, MerchantOfferEditorHolder holder, InventoryClickEvent event) {
        MerchantShopOffer offer = holder.buy()
                ? plugin.getMerchantBuyOffer(holder.rotationIndex(), holder.offerIndex())
                : plugin.getMerchantSellOffer(holder.offerIndex());
        if (offer == null) {
            player.closeInventory();
            return;
        }
        switch (event.getSlot()) {
            case OFFER_EDITOR_BACK_SLOT -> {
                if (holder.buy()) {
                    openMerchantSellToPlayersOfferMenu(player, holder.rotationIndex());
                } else {
                    openMerchantSellOfferMenu(player);
                }
            }
            case OFFER_EDITOR_MATERIAL_SLOT -> {
                ItemStack chosenItem = player.getInventory().getItemInMainHand();
                if (chosenItem == null || chosenItem.getType().isAir()) {
                    chosenItem = event.getCursor();
                }
                if (chosenItem == null || chosenItem.getType().isAir()) {
                    player.sendMessage(plugin.colorize("&cHold the new item in your main hand or on your cursor first."));
                    return;
                }
                boolean updated = holder.buy()
                        ? plugin.updateMerchantBuyOffer(holder.rotationIndex(), holder.offerIndex(), chosenItem.getType(), null, null, null)
                        : plugin.updateMerchantSellOffer(holder.offerIndex(), chosenItem.getType(), null, null);
                if (updated) {
                    player.sendMessage(plugin.colorize("&aUpdated merchant good to &f" + plugin.formatMaterialName(chosenItem.getType()) + "&a."));
                    openMerchantOfferEditor(player, holder);
                } else {
                    player.sendMessage(plugin.colorize("&cThat item cannot be used for this merchant offer."));
                }
            }
            case OFFER_EDITOR_AMOUNT_DOWN_SLOT -> {
                updateMerchantOfferAmount(holder, offer.getAmount() - 1);
                openMerchantOfferEditor(player, holder);
            }
            case OFFER_EDITOR_AMOUNT_UP_SLOT -> {
                updateMerchantOfferAmount(holder, offer.getAmount() + 1);
                openMerchantOfferEditor(player, holder);
            }
            case OFFER_EDITOR_PRICE_DOWN_SLOT -> {
                double delta = event.isShiftClick() ? 10.0D : 1.0D;
                updateMerchantOfferPrice(holder, Math.max(0.0D, offer.getPrice() - delta));
                openMerchantOfferEditor(player, holder);
            }
            case OFFER_EDITOR_PRICE_UP_SLOT -> {
                double delta = event.isShiftClick() ? 10.0D : 1.0D;
                updateMerchantOfferPrice(holder, offer.getPrice() + delta);
                openMerchantOfferEditor(player, holder);
            }
            case OFFER_EDITOR_STOCK_DOWN_SLOT -> {
                if (holder.buy()) {
                    plugin.updateMerchantBuyOffer(holder.rotationIndex(), holder.offerIndex(), null, null, null, offer.getStock() - 1);
                    openMerchantOfferEditor(player, holder);
                }
            }
            case OFFER_EDITOR_STOCK_UP_SLOT -> {
                if (holder.buy()) {
                    plugin.updateMerchantBuyOffer(holder.rotationIndex(), holder.offerIndex(), null, null, null, offer.getStock() + 1);
                    openMerchantOfferEditor(player, holder);
                }
            }
            default -> {
            }
        }
    }

    private void updateMerchantOfferAmount(MerchantOfferEditorHolder holder, int amount) {
        if (holder.buy()) {
            plugin.updateMerchantBuyOffer(holder.rotationIndex(), holder.offerIndex(), null, amount, null, null);
        } else {
            plugin.updateMerchantSellOffer(holder.offerIndex(), null, amount, null);
        }
    }

    private void updateMerchantOfferPrice(MerchantOfferEditorHolder holder, double price) {
        if (holder.buy()) {
            plugin.updateMerchantBuyOffer(holder.rotationIndex(), holder.offerIndex(), null, null, price, null);
        } else {
            plugin.updateMerchantSellOffer(holder.offerIndex(), null, null, price);
        }
    }

    private ItemStack createInfoItem(MerchantShopState state) {
        List<String> lore = new ArrayList<>();
        lore.add("&7This wandering merchant is active for");
        lore.add("&f" + plugin.formatLongDurationWords(Math.max(1000L, state.getDespawnAtMillis() - System.currentTimeMillis())) + "&7 more.");
        lore.add("&7Inventory switches every &f2 minutes&7.");
        lore.add("&7Stock is shared across all countries.");
        lore.add("&7Trade cooldown: &f30 seconds per player&7.");
        return createSimpleItem(Material.WANDERING_TRADER_SPAWN_EGG, "&6Wandering Merchant", lore);
    }

    private ItemStack createMerchantAdminInfoItem() {
        return createSimpleItem(Material.WRITABLE_BOOK, "&6Merchant Admin", List.of(
                "&7This menu only manages merchant goods",
                "&7and current-wave runtime controls.",
                "",
                "&7Use commands or the config files for",
                "&7spawn windows, rotation timing, and",
                "&7trade cooldown settings."
        ));
    }

    private ItemStack createMerchantRuntimeItem() {
        MerchantShopState state = plugin.getAnyActiveMerchantState();
        if (state != null) {
            return createSimpleItem(Material.EMERALD_BLOCK, "&aMerchant Active", List.of(
                    "&7Time left: &f" + plugin.formatLongDurationWords(Math.max(1000L, state.getDespawnAtMillis() - System.currentTimeMillis())),
                    "&7Current rotation goods are live.",
                    "&eLeft click to force a new spawn",
                    "&eRight click to remove active merchant"
            ));
        }
        long nextMillis = Math.max(0L, plugin.getMerchantNextSpawnMillis() - System.currentTimeMillis());
        return createSimpleItem(Material.REDSTONE_BLOCK, "&cMerchant Idle", List.of(
                "&7Next spawn: &f" + (nextMillis > 0L ? plugin.formatLongDurationWords(nextMillis) : "ready"),
                "&eLeft click to spawn now"
        ));
    }

    private ItemStack createBuyItem(MerchantShopOffer offer) {
        int remaining = plugin.getMerchantRemainingStock(offer);
        return createSimpleItem(
                offer.getMaterial(),
                "&aBuy &f" + offer.getAmount() + " " + plugin.formatMaterialName(offer.getMaterial()),
                List.of(
                        "&7Price: &f$" + plugin.formatMoney(offer.getPrice()),
                        "&7Shared stock left: &f" + remaining + "&7/&f" + offer.getStock(),
                        remaining <= 0 ? "&cSold out for this rotation." : "&8Click to buy."
                )
        );
    }

    private ItemStack createSellItem(MerchantShopOffer offer) {
        double payout = plugin.getMerchantSellPrice(offer);
        return createSimpleItem(
                offer.getMaterial(),
                "&6Sell &fup to " + offer.getAmount() + " " + plugin.formatMaterialName(offer.getMaterial()),
                List.of(
                        "&7Current payout per item: &f$" + plugin.formatMoney(payout),
                        "&7Daily supply affects this price.",
                        "&8Click to sell what you have."
                )
        );
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack filler = createSimpleItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());
        ItemStack border = createSimpleItem(Material.BLACK_STAINED_GLASS_PANE, "&8", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
        for (int slot = 0; slot < 9; slot++) {
            inventory.setItem(slot, border);
            inventory.setItem(45 + (slot % 9), border);
        }
        for (int slot : new int[]{9, 17, 18, 26, 27, 35}) {
            inventory.setItem(slot, border);
        }
        inventory.setItem(10, createSimpleItem(Material.EMERALD, "&aBuy Stock", List.of("&7Shared stock across all islands.")));
        inventory.setItem(28, createSimpleItem(Material.CHEST, "&6Sell Goods", List.of("&7Payout falls as more items are sold daily.")));
    }

    private void fillAdminMenuSlots(Inventory inventory) {
        ItemStack filler = createSimpleItem(Material.BROWN_STAINED_GLASS_PANE, "&7", List.of());
        ItemStack border = createSimpleItem(Material.BLACK_STAINED_GLASS_PANE, "&8", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
        for (int slot = 0; slot < 9; slot++) {
            inventory.setItem(slot, border);
        }
        for (int slot = inventory.getSize() - 9; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, border);
        }
    }

    private void fillRotationSlots(Inventory inventory) {
        ItemStack filler = createSimpleItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());
        ItemStack border = createSimpleItem(Material.BLACK_STAINED_GLASS_PANE, "&8", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
        for (int slot = 0; slot < 9; slot++) {
            inventory.setItem(slot, border);
        }
        for (int slot = inventory.getSize() - 9; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, border);
        }
    }

    private void fillOfferListSlots(Inventory inventory, boolean buyView) {
        ItemStack filler = createSimpleItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "&7", List.of());
        ItemStack border = createSimpleItem(Material.BLACK_STAINED_GLASS_PANE, "&8", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
        for (int slot = 0; slot < 9; slot++) {
            inventory.setItem(slot, border);
        }
        for (int slot = inventory.getSize() - 9; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, border);
        }
        inventory.setItem(28, createSimpleItem(buyView ? Material.EMERALD : Material.CHEST, buyView ? "&aMerchant Sells" : "&6Merchant Buys", List.of(
                buyView ? "&7Items the merchant sells to players." : "&7Items players can sell to the merchant."
        )));
    }

    private void fillEditorSlots(Inventory inventory, boolean buyView) {
        ItemStack filler = createSimpleItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());
        ItemStack border = createSimpleItem(Material.BLACK_STAINED_GLASS_PANE, "&8", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
        for (int slot = 0; slot < 9; slot++) {
            inventory.setItem(slot, border);
            inventory.setItem(45 + slot, border);
        }
        for (int slot : new int[]{9, 17, 18, 26, 27, 35, 36, 44}) {
            inventory.setItem(slot, border);
        }
        inventory.setItem(10, createSimpleItem(buyView ? Material.EMERALD : Material.CHEST, buyView ? "&aBuy Offer Editor" : "&6Sell Offer Editor", List.of(
                "&7Edit the traded good and values here."
        )));
        inventory.setItem(28, createSimpleItem(Material.PAPER, "&7Amount", List.of("&7Adjust the traded stack size.")));
        inventory.setItem(32, createSimpleItem(Material.GOLD_INGOT, "&7Price", List.of("&7Adjust the offer price.")));
        if (buyView) {
            inventory.setItem(37, createSimpleItem(Material.BARREL, "&7Stock", List.of("&7Adjust shared stock per rotation.")));
        }
    }

    private ItemStack createSimpleItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.legacyComponent(name));
            if (lore != null && !lore.isEmpty()) {
                meta.lore(lore.stream().map(plugin::legacyComponent).toList());
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private interface MerchantInventoryHolder extends InventoryHolder {
    }

    private record MerchantMenuHolder(UUID viewerId, UUID merchantId) implements MerchantInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record MerchantAdminMenuHolder(UUID viewerId) implements MerchantInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record MerchantBuyRotationListHolder(UUID viewerId) implements MerchantInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record MerchantBuyOfferListHolder(UUID viewerId, int rotationIndex) implements MerchantInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record MerchantSellOfferListHolder(UUID viewerId) implements MerchantInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record MerchantOfferEditorHolder(UUID viewerId, boolean buy, int rotationIndex, int offerIndex) implements MerchantInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
