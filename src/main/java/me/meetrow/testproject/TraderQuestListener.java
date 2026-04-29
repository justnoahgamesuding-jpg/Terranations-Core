package me.meetrow.testproject;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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

public class TraderQuestListener implements Listener {
    private static final int MENU_SIZE = 45;
    private static final int INFO_SLOT = 4;
    private static final int SPECIALTY_SLOT = 13;
    private static final int BIG_ORDER_SLOT = 31;
    private static final int CLOSE_SLOT = 40;
    private static final Profession[] CONTRACT_PROFESSIONS = {
            Profession.MINER,
            Profession.LUMBERJACK,
            Profession.FARMER,
            Profession.BUILDER,
            Profession.BLACKSMITH
    };
    private static final int[] CONTRACT_SLOTS = {19, 20, 21, 22, 23};

    private final Testproject plugin;

    public TraderQuestListener(Testproject plugin) {
        this.plugin = plugin;
    }

    public void openTraderMenu(Player player) {
        openTraderMenu(player, plugin.getActiveTraderState());
    }

    public void openTraderMenu(Player player, DynamicTraderState traderState) {
        if (player == null || traderState == null) {
            if (player != null) {
                player.sendMessage(plugin.getMessage("terra.trader.no-active"));
            }
            return;
        }
        if (!plugin.canUseTrader(player.getUniqueId(), traderState)) {
            player.sendMessage(plugin.getMessage("terra.trader.country-not-allowed"));
            return;
        }

        Inventory inventory = Bukkit.createInventory(
                new TraderMenuHolder(player.getUniqueId(), traderState.getTraderId()),
                MENU_SIZE,
                plugin.legacyComponent("&8" + traderState.getTraderName())
        );
        fillEmptySlots(inventory);
        inventory.setItem(10, createSimpleItem(Material.WRITABLE_BOOK, "&6Personal Contracts", List.of(
                "&7Accept one job-locked contract",
                "&7from this trader at a time."
        )));
        inventory.setItem(16, createSimpleItem(Material.CHEST_MINECART, "&6Shared Country Order", List.of(
                "&7Your country can contribute items",
                "&7to this route together."
        )));
        inventory.setItem(INFO_SLOT, createInfoItem(player, traderState));
        inventory.setItem(SPECIALTY_SLOT, createSpecialtyItem(traderState));
        for (int i = 0; i < CONTRACT_PROFESSIONS.length; i++) {
            inventory.setItem(CONTRACT_SLOTS[i], createContractItem(player, traderState, CONTRACT_PROFESSIONS[i]));
        }
        inventory.setItem(BIG_ORDER_SLOT, createBigOrderItem(player, traderState));
        inventory.setItem(CLOSE_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close the trader menu.")));
        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTraderInteract(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (!plugin.isTraderNpc(clicked)) {
            return;
        }

        event.setCancelled(true);
        plugin.beginOnboardingNpcInteraction(
                event.getPlayer(),
                clicked,
                "trader_npc",
                () -> openTraderMenu(event.getPlayer(), plugin.getTraderState(clicked))
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTraderDamage(EntityDamageEvent event) {
        if (plugin.isTraderNpc(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getView().getTopInventory().getHolder() instanceof TraderMenuHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        TraderMenuHolder holder = (TraderMenuHolder) event.getView().getTopInventory().getHolder();
        DynamicTraderState traderState = plugin.getTraderStateByTraderId(holder.traderId());
        if (traderState == null) {
            player.closeInventory();
            player.sendMessage(plugin.getMessage("terra.trader.no-active"));
            return;
        }
        if (!plugin.canUseTrader(player.getUniqueId(), traderState)) {
            player.closeInventory();
            player.sendMessage(plugin.getMessage("terra.trader.country-not-allowed"));
            return;
        }

        if (event.getSlot() == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }

        for (int i = 0; i < CONTRACT_SLOTS.length; i++) {
            if (event.getSlot() == CONTRACT_SLOTS[i]) {
                handleContractClick(player, traderState, CONTRACT_PROFESSIONS[i]);
                return;
            }
        }
        if (event.getSlot() == BIG_ORDER_SLOT) {
            plugin.contributeToTraderBigOrder(player, traderState);
            openTraderMenu(player, traderState);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof TraderMenuHolder) {
            event.setCancelled(true);
        }
    }

    private ItemStack createInfoItem(Player player, DynamicTraderState traderState) {
        long remainingMillis = Math.max(1000L, traderState.getDespawnAtMillis() - System.currentTimeMillis());
        List<String> lore = new ArrayList<>();
        Country hostCountry = plugin.getTraderHostCountry(traderState);
        lore.add("&7The trader is visiting the server for");
        lore.add("&f" + plugin.formatLongDurationWords(remainingMillis) + "&7 more.");
        if (hostCountry != null) {
            lore.add("&7Hosting country: &f" + hostCountry.getName());
        }
        lore.add("&7Trader: &f" + traderState.getTraderName());
        lore.add("&7Specialty: &f" + plugin.getProfessionPlainDisplayName(traderState.getSpecialtyProfession()));
        lore.add("&7Your route reputation: &e" + plugin.formatTraderReputation(plugin.getTraderReputation(player.getUniqueId())));
        Country playerCountry = plugin.getPlayerCountry(player.getUniqueId());
        if (playerCountry != null && playerCountry.getLastTraderSeenAtMillis() > 0L) {
            lore.add("&7Last seen in your country: &f" + plugin.formatTraderLastSeen(playerCountry.getLastTraderSeenAtMillis()));
            if (playerCountry.getLastTraderName() != null) {
                Profession lastSpecialty = Profession.fromKey(playerCountry.getLastTraderSpecialty());
                lore.add("&7Last trader: &f" + playerCountry.getLastTraderName()
                        + " &8(" + (lastSpecialty != null ? plugin.getProfessionPlainDisplayName(lastSpecialty) : "Unknown") + "&8)");
            }
        }
        return createSimpleItem(Material.EMERALD, "&6" + traderState.getTraderName(), lore);
    }

    private ItemStack createSpecialtyItem(DynamicTraderState traderState) {
        return createSimpleItem(
                traderState.getSpecialtyProfession().getIcon(),
                "&6Specialty Route",
                List.of(
                        "&7Trader: &f" + traderState.getTraderName(),
                        "&7Focus: &f" + plugin.getProfessionPlainDisplayName(traderState.getSpecialtyProfession()),
                        "&7Matching this job pays better",
                        "&7money, xp, and reputation."
                )
        );
    }

    private ItemStack createContractItem(Player player, DynamicTraderState traderState, Profession profession) {
        String professionName = plugin.getProfessionDisplayName(profession);
        TraderPlayerQuest traderQuest = plugin.getTraderQuest(player.getUniqueId(), traderState);
        List<String> lore = new ArrayList<>();

        if (!plugin.hasProfession(player.getUniqueId(), profession)) {
            lore.add("&7Required job: " + professionName);
            lore.add("&cYou do not have this job.");
            return createSimpleItem(Material.GRAY_DYE, professionName, lore);
        }

        if (traderQuest != null && traderQuest.getProfession() == profession) {
            lore.add("&7Requested: &f" + traderQuest.getRequestedAmount() + " " + plugin.formatMaterialName(traderQuest.getRequestedMaterial()));
            lore.add("&7Reward: &f⛃" + plugin.formatMoney(traderQuest.getRewardMoney()) + "&7, &f" + traderQuest.getRewardXp() + " xp&7, &e+" + plugin.formatTraderReputation(traderQuest.getRewardReputation()));
            lore.add("&aReady for delivery.");
            lore.add("&8Click to deliver the items.");
            return createSimpleItem(traderQuest.getRequestedMaterial(), "&aDeliver " + plugin.getProfessionPlainDisplayName(profession) + " Contract", lore);
        }

        if (traderQuest != null) {
            lore.add("&7You already have an active");
            lore.add("&7" + plugin.getProfessionDisplayName(traderQuest.getProfession()) + " &7contract with this trader.");
            return createSimpleItem(Material.BARRIER, "&cBusy", lore);
        }

        long cooldownRemaining = plugin.getTraderQuestCooldownRemainingMillis(player.getUniqueId(), traderState);
        if (cooldownRemaining > 0L) {
            lore.add("&eThis trader is packing the last route.");
            lore.add("&7Come back in &f" + plugin.formatLongDurationWords(cooldownRemaining) + "&7.");
            lore.add("&8This cooldown only applies to this trader.");
            return createSimpleItem(Material.CLOCK, professionName, lore);
        }

        TraderQuestOffer offer = plugin.previewTraderQuest(player.getUniqueId(), profession, traderState);
        if (offer == null) {
            lore.add("&cNo contract is available right now.");
            return createSimpleItem(Material.BARRIER, "&cUnavailable", lore);
        }

        lore.add("&7Requested: &f" + offer.getRequestedAmount() + " " + plugin.formatMaterialName(offer.getRequestedMaterial()));
        lore.add("&7Reward: &f⛃" + plugin.formatMoney(offer.getRewardMoney()) + "&7, &f" + offer.getRewardXp() + " xp&7, &e+" + plugin.formatTraderReputation(offer.getRewardReputation()));
        lore.add("&7Difficulty tier: &f" + offer.getDifficultyTier());
        if (profession == traderState.getSpecialtyProfession()) {
            lore.add("&6Specialty bonus applied.");
        }
        lore.add("&8Click to accept this contract.");
        return createSimpleItem(offer.getRequestedMaterial(), professionName, lore);
    }

    private ItemStack createBigOrderItem(Player player, DynamicTraderState traderState) {
        List<String> lore = new ArrayList<>();
        TraderBigOrder bigOrder = plugin.getTraderBigOrder(player.getUniqueId(), traderState);
        if (bigOrder == null) {
            lore.add("&7Join a country with access to this");
            lore.add("&7trade route to contribute.");
            return createSimpleItem(Material.BOOK, "&6Shared Country Order", lore);
        }

        lore.add("&7Tier: &f" + bigOrder.getDifficultyTier());
        lore.add("&7Progress: &f" + bigOrder.getTotalDeliveredAmount() + "&7/&f" + bigOrder.getTotalRequestedAmount());
        lore.add("&7Reward: &f⛃" + plugin.formatMoney(bigOrder.getRewardMoney()) + "&7, &f" + bigOrder.getRewardXp() + " xp&7, &e+" + plugin.formatTraderReputation(bigOrder.getRewardReputation()));
        for (TraderBigOrderEntry entry : bigOrder.getEntries()) {
            lore.add("&8- &f" + entry.getDeliveredAmount() + "&7/&f" + entry.getRequestedAmount() + " " + plugin.formatMaterialName(entry.getRequestedMaterial()));
        }
        lore.add("&8Click to contribute matching items.");
        return createSimpleItem(Material.CHEST_MINECART, "&6Shared Country Order", lore);
    }

    private void handleContractClick(Player player, DynamicTraderState traderState, Profession profession) {
        if (!plugin.hasProfession(player.getUniqueId(), profession)) {
            player.sendMessage(plugin.getMessage("terra.trader.profession-locked", plugin.placeholders(
                    "profession", plugin.getProfessionPlainDisplayName(profession)
            )));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 0.8F, 0.9F);
            return;
        }

        TraderPlayerQuest traderQuest = plugin.getTraderQuest(player.getUniqueId(), traderState);
        if (traderQuest != null && traderQuest.getProfession() == profession) {
            if (plugin.completeTraderQuest(player, traderState)) {
                player.sendMessage(plugin.getMessage("terra.trader.quest-complete", plugin.placeholders(
                        "money", plugin.formatMoney(traderQuest.getRewardMoney()),
                        "xp", String.valueOf(traderQuest.getRewardXp()),
                        "reputation", plugin.formatTraderReputation(traderQuest.getRewardReputation()),
                        "time", plugin.formatLongDurationWords(plugin.getTraderDeliveryCooldownMillis())
                )));
            } else {
                player.sendMessage(plugin.getMessage("terra.trader.missing-items", plugin.placeholders(
                        "amount", String.valueOf(traderQuest.getRequestedAmount()),
                        "item", plugin.formatMaterialName(traderQuest.getRequestedMaterial())
                )));
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 0.8F, 0.9F);
            }
            openTraderMenu(player, traderState);
            return;
        }

        if (traderQuest != null) {
            player.sendMessage(plugin.getMessage("terra.trader.quest-other-active", plugin.placeholders(
                    "profession", plugin.getProfessionPlainDisplayName(traderQuest.getProfession())
            )));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 0.8F, 0.9F);
            return;
        }

        long cooldownRemaining = plugin.getTraderQuestCooldownRemainingMillis(player.getUniqueId(), traderState);
        if (cooldownRemaining > 0L) {
            player.sendMessage(plugin.getMessage("terra.trader.quest-waiting", plugin.placeholders(
                    "time", plugin.formatLongDurationWords(cooldownRemaining)
            )));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 0.8F, 0.9F);
            return;
        }

        TraderPlayerQuest accepted = plugin.acceptTraderQuest(player.getUniqueId(), profession, traderState);
        if (accepted == null) {
            player.sendMessage(plugin.getMessage("terra.trader.quest-accept-failed"));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 0.8F, 0.9F);
            return;
        }

        plugin.playTraderQuestAcceptEffect(player);
        player.sendMessage(plugin.getMessage("terra.trader.quest-accepted", plugin.placeholders(
                "profession", plugin.getProfessionPlainDisplayName(accepted.getProfession()),
                "amount", String.valueOf(accepted.getRequestedAmount()),
                "item", plugin.formatMaterialName(accepted.getRequestedMaterial()),
                "time", "now",
                "expire", plugin.formatLongDurationWords(Math.max(0L, accepted.getExpiresAtMillis() - System.currentTimeMillis()))
        )));
        openTraderMenu(player, traderState);
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack filler = createSimpleItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());
        ItemStack border = createSimpleItem(Material.BLACK_STAINED_GLASS_PANE, "&8", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
        for (int slot = 0; slot < 9; slot++) {
            inventory.setItem(slot, border);
            inventory.setItem(36 + slot, border);
        }
        for (int slot : new int[]{9, 17, 18, 26, 27, 35}) {
            inventory.setItem(slot, border);
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

    private record TraderMenuHolder(UUID viewerId, UUID traderId) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
