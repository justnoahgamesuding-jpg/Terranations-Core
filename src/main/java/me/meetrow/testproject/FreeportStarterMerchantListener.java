package me.meetrow.testproject;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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

public final class FreeportStarterMerchantListener implements Listener {
    private static final int MENU_SIZE = 27;
    private static final int INFO_SLOT = 4;
    private static final int CLOSE_SLOT = 22;
    private static final int[] OFFER_SLOTS = {10, 11, 12, 14, 15, 16};

    private final Testproject plugin;

    public FreeportStarterMerchantListener(Testproject plugin) {
        this.plugin = plugin;
    }

    public void openMerchantMenu(Player player, String merchantKey) {
        if (player == null || merchantKey == null || !plugin.isFreeportStarterMerchantKey(merchantKey)) {
            return;
        }

        Inventory inventory = Bukkit.createInventory(
                new FreeportMerchantMenuHolder(player.getUniqueId(), merchantKey),
                MENU_SIZE,
                plugin.legacyComponent(plugin.getFreeportStarterMerchantTitle(merchantKey))
        );
        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createInfoItem(player, merchantKey));

        List<MerchantShopOffer> offers = plugin.getFreeportStarterMerchantOffers(merchantKey);
        for (int i = 0; i < OFFER_SLOTS.length && i < offers.size(); i++) {
            inventory.setItem(OFFER_SLOTS[i], createOfferItem(player, merchantKey, i, offers.get(i)));
        }

        inventory.setItem(CLOSE_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close this merchant menu.")));
        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onNpcInteract(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        String npcKey = plugin.getOnboardingNpcKey(clicked);
        if (!plugin.isFreeportStarterMerchantKey(npcKey)) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        plugin.beginOnboardingNpcInteraction(
                player,
                clicked,
                npcKey,
                () -> openMerchantMenu(player, npcKey)
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getView().getTopInventory().getHolder() instanceof FreeportMerchantMenuHolder holder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        if (event.getSlot() == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }

        for (int i = 0; i < OFFER_SLOTS.length; i++) {
            if (event.getSlot() == OFFER_SLOTS[i]) {
                if (event.isRightClick()) {
                    plugin.handleFreeportStarterMerchantBuy(player, holder.merchantKey(), i);
                } else {
                    plugin.handleFreeportStarterMerchantSell(player, holder.merchantKey(), i, false);
                }
                openMerchantMenu(player, holder.merchantKey());
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof FreeportMerchantMenuHolder) {
            event.setCancelled(true);
        }
    }

    private ItemStack createInfoItem(Player player, String merchantKey) {
        Material icon = plugin.getFreeportStarterMerchantIcon(merchantKey);
        String name = plugin.getFreeportStarterMerchantDisplayName(merchantKey);
        long cooldown = plugin.getFreeportStarterMerchantCooldownRemainingMillis(player.getUniqueId());
        List<String> lore = new ArrayList<>();
        lore.add("&7Shared Freeport starter market.");
        lore.add("&7Selling adds to island stock.");
        lore.add("&7Buying pulls from that stock.");
        lore.add("&8");
        lore.add("&eLeft-click &7sell one bundle.");
        lore.add("&eRight-click &7buy one bundle.");
        if (cooldown > 0L) {
            lore.add("&8");
            lore.add("&7Cooldown: &f" + plugin.formatLongDurationWords(cooldown));
        }
        return createSimpleItem(icon, "&6" + name, lore);
    }

    private ItemStack createOfferItem(Player player, String merchantKey, int offerIndex, MerchantShopOffer offer) {
        double payoutPerItem = offer != null ? plugin.getFreeportStarterMerchantSellPrice(merchantKey, offerIndex) : 0.0D;
        double buyPrice = offer != null ? plugin.getFreeportStarterMerchantBuyPrice(merchantKey, offerIndex) : 0.0D;
        int owned = offer != null ? countOwned(player, offer) : 0;
        int stock = offer != null ? plugin.getFreeportStarterMerchantRemainingStock(merchantKey, offerIndex) : 0;
        int quickSellAmount = offer != null ? Math.min(owned, offer.getAmount()) : 0;
        double quickSellValue = offer != null ? roundMoney(quickSellAmount * payoutPerItem) : 0.0D;
        String itemName = offer != null ? plugin.formatTradeRequestName(offer.getMaterial(), offer.getContentId()) : "Unknown";

        List<String> lore = new ArrayList<>();
        lore.add("&7Island stock: &f" + stock);
        lore.add("&7Owned: &f" + owned);
        lore.add("&7Pays per item: &f⛃" + plugin.formatMoney(payoutPerItem));
        lore.add("&7Buy bundle: &f" + (offer != null ? offer.getAmount() : 0));
        lore.add("&7Buy cost: &f⛃" + plugin.formatMoney(buyPrice));
        lore.add("&7Sell bundle: &f" + (offer != null ? offer.getAmount() : 0));
        lore.add("&7Sell value: &f⛃" + plugin.formatMoney(quickSellValue));
        lore.add("&8");
        lore.add("&eLeft-click &7sell one bundle");
        lore.add("&eRight-click &7buy one bundle");
        ItemStack item = plugin.createMerchantOfferItemStack(offer, 1);
        if (item == null || item.getType().isAir()) {
            item = new ItemStack(offer.getMaterial());
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.legacyComponent("&a" + itemName));
            List<net.kyori.adventure.text.Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                loreComponents.add(plugin.legacyComponent(line));
            }
            meta.lore(loreComponents);
            item.setItemMeta(meta);
        }
        return item;
    }

    private int countOwned(Player player, MerchantShopOffer offer) {
        if (player == null || offer == null || offer.getMaterial() == null) {
            return 0;
        }
        int total = 0;
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null || itemStack.getType() != offer.getMaterial()) {
                continue;
            }
            if (offer.getContentId() != null && !offer.getContentId().equalsIgnoreCase(plugin.getTerraCraftingContentId(itemStack))) {
                continue;
            }
            total += Math.max(0, itemStack.getAmount());
        }
        return total;
    }

    private double roundMoney(double amount) {
        return Math.round(Math.max(0.0D, amount) * 100.0D) / 100.0D;
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.legacyComponent("&7"));
            filler.setItemMeta(meta);
        }
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
    }

    private ItemStack createSimpleItem(Material material, String displayName, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.legacyComponent(displayName));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(plugin.legacyComponent(line));
            }
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private record FreeportMerchantMenuHolder(UUID playerId, String merchantKey) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
