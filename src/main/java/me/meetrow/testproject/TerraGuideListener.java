package me.meetrow.testproject;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TerraGuideListener implements Listener {
    private static final int GUI_SIZE = 54;
    private static final int CLOSE_SLOT = 49;
    private static final int BACK_SLOT = 45;

    private final Testproject plugin;

    public TerraGuideListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK
                && event.getAction() != Action.LEFT_CLICK_AIR
                && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (!plugin.isGuidanceItem(event.getItem())) {
            return;
        }

        event.setCancelled(true);
        openMainMenu(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof TerraGuideHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        switch (((TerraGuideHolder) holder).page()) {
            case MAIN -> handleMainClick(player, event.getSlot());
            case STATS -> handleBackClose(player, event.getSlot());
            case CONTRACTS -> handleContractsClick(player, event.getSlot());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof TerraGuideHolder) {
            event.setCancelled(true);
        }
    }

    public void openMainMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(new TerraGuideHolder(GuidePage.MAIN), GUI_SIZE, plugin.legacyComponent("&8Terra Guide"));
        fillEmpty(inventory);

        Country country = plugin.getPlayerCountry(player.getUniqueId());
        boolean canManageCountry = plugin.canManageCountry(country, player.getUniqueId()) || player.hasPermission(Testproject.COUNTRY_ADMIN_PERMISSION) || player.isOp();

        inventory.setItem(4, createItem(Material.NETHER_STAR, "&6Terra Guide", List.of(
                "&7Your hub for progression, guidance,",
                "&7countries, jobs, and personal stats."
        )));
        inventory.setItem(10, createItem(Material.PLAYER_HEAD, "&bPlayer Stats", List.of(
                "&7View your current progression summary."
        )));
        inventory.setItem(12, createItem(Material.IRON_PICKAXE, "&aJobs", List.of(
                "&7Open your job selection and",
                "&7profession progression menu."
        )));
        inventory.setItem(14, createItem(Material.WRITABLE_BOOK, "&6Contracts", List.of(
                "&7Track your active quest and",
                "&7future personal work systems."
        )));
        inventory.setItem(28, createItem(Material.BLUE_BANNER, country != null ? "&9Country Dashboard" : "&9Country Browser", List.of(
                country != null ? "&7Open your player country dashboard." : "&7Browse and join server countries."
        )));
        if (canManageCountry && country != null) {
            inventory.setItem(30, createItem(Material.LODESTONE, "&cCountry Owner Panel", List.of(
                    "&7Open the management view for",
                    "&7country leaders and admins."
            )));
        }
        inventory.setItem(CLOSE_SLOT, createItem(Material.BARRIER, "&cClose", List.of("&7Close the Terra Guide.")));

        player.openInventory(inventory);
    }

    private void openStatsMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(new TerraGuideHolder(GuidePage.STATS), GUI_SIZE, plugin.legacyComponent("&8Guide Stats"));
        fillEmpty(inventory);

        UUID playerId = player.getUniqueId();
        Profession activeProfession = plugin.getProfession(playerId);
        Country country = plugin.getPlayerCountry(playerId);
        List<Profession> ownedProfessions = plugin.getOwnedProfessions(playerId);
        List<String> professionLore = new ArrayList<>();
        if (ownedProfessions.isEmpty()) {
            professionLore.add("&7No professions unlocked.");
        } else {
            for (Profession profession : ownedProfessions) {
                professionLore.add("&7" + plugin.getProfessionPlainDisplayName(profession) + ": &fLv." + plugin.getProfessionLevel(playerId, profession));
            }
        }

        inventory.setItem(4, createItem(Material.PLAYER_HEAD, "&b" + player.getName(), List.of(
                "&7Country: &f" + (country != null ? country.getName() : "None"),
                "&7Balance: &f$" + plugin.formatMoney(plugin.getBalance(player)),
                "&7Trader reputation: &f" + plugin.formatTraderReputation(plugin.getTraderReputation(playerId))
        )));
        inventory.setItem(20, createItem(activeProfession != null ? activeProfession.getIcon() : Material.BARRIER, "&aActive Profession", List.of(
                "&7Current: &f" + (activeProfession != null ? plugin.getProfessionPlainDisplayName(activeProfession) : "None"),
                activeProfession != null ? "&7Level: &f" + plugin.getProfessionLevel(playerId, activeProfession) : "&7Level: &f0",
                activeProfession != null ? "&7XP left: &f" + Math.max(0, plugin.getProfessionXpRequired(playerId, activeProfession) - plugin.getProfessionXp(playerId, activeProfession)) : "&7XP left: &f0"
        )));
        inventory.setItem(24, createItem(Material.BOOK, "&eOwned Professions", professionLore));
        inventory.setItem(31, createItem(Material.CLOCK, "&6Server Time", List.of(
                "&7Time: &f" + plugin.getRealTimeClockTimezoneId(),
                "&7Now: &f" + new TerraPlaceholderExpansion(plugin).onRequest(player, "server_datetime")
        )));
        inventory.setItem(BACK_SLOT, createItem(Material.ARROW, "&eBack", List.of("&7Return to the main guide menu.")));
        inventory.setItem(CLOSE_SLOT, createItem(Material.BARRIER, "&cClose", List.of("&7Close the Terra Guide.")));
        player.openInventory(inventory);
    }

    private void openContractsMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(new TerraGuideHolder(GuidePage.CONTRACTS), GUI_SIZE, plugin.legacyComponent("&8Work Contracts"));
        fillEmpty(inventory);

        UUID playerId = player.getUniqueId();
        inventory.setItem(4, createItem(Material.WRITABLE_BOOK, "&6Active Quest", List.of(
                "&7Title: &f" + plugin.getTutorialQuestTitlePlain(playerId),
                "&7Objective: &f" + plugin.getTutorialQuestObjectivePlain(playerId),
                plugin.getTutorialQuestHintPlain(playerId).isBlank() ? "&7Hint: &fNone" : "&7Hint: &f" + plugin.getTutorialQuestHintPlain(playerId),
                "&7Progress: &f" + plugin.getTutorialQuestProgressText(playerId)
        )));
        inventory.setItem(20, createItem(Material.CLOCK, "&eContracts Rework Pending", List.of(
                "&7The old skill tree contract loop",
                "&7has been removed.",
                "",
                "&7This page will be rebuilt around",
                "&7job-based contracts next."
        )));

        inventory.setItem(BACK_SLOT, createItem(Material.ARROW, "&eBack", List.of("&7Return to the main guide menu.")));
        inventory.setItem(CLOSE_SLOT, createItem(Material.BARRIER, "&cClose", List.of("&7Close the Terra Guide.")));
        player.openInventory(inventory);
    }

    private void handleMainClick(Player player, int slot) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        switch (slot) {
            case 10 -> openStatsMenu(player);
            case 12 -> plugin.openProfessionMenu(player);
            case 14 -> openContractsMenu(player);
            case 28 -> {
                if (country != null) {
                    plugin.openCountryMenu(player);
                } else {
                    plugin.openCountryListMenu(player);
                }
            }
            case 30 -> {
                if (country != null && (plugin.canManageCountry(country, player.getUniqueId()) || player.hasPermission(Testproject.COUNTRY_ADMIN_PERMISSION) || player.isOp())) {
                    plugin.openCountryAdminMenu(player, country);
                }
            }
            case CLOSE_SLOT -> player.closeInventory();
            default -> {
            }
        }
    }

    private void handleContractsClick(Player player, int slot) {
        handleBackClose(player, slot);
    }

    private boolean handleBackClose(Player player, int slot) {
        if (slot == BACK_SLOT) {
            openMainMenu(player);
            return true;
        }
        if (slot == CLOSE_SLOT) {
            player.closeInventory();
            return true;
        }
        return false;
    }

    private void fillEmpty(Inventory inventory) {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
    }

    private ItemStack createItem(Material material, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.legacyComponent(name));
            meta.lore(loreLines.stream().map(plugin::legacyComponent).toList());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private enum GuidePage {
        MAIN,
        STATS,
        CONTRACTS
    }

    private record TerraGuideHolder(GuidePage page) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
