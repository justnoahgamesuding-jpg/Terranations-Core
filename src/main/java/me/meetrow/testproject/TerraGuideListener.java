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
    private static final int SKILL_SELECTOR_INFO_SLOT = 4;
    private static final int CONTRACT_ACTIVE_SLOT = 13;
    private static final int CONTRACT_REROLL_INFO_SLOT = 40;
    private static final int CONTRACT_ABANDON_SLOT = 41;
    private static final int[] CONTRACT_OFFER_SLOTS = {20, 22, 24, 31, 33};

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
            case SKILL_SELECTOR -> handleSkillSelectorClick(player, event.getSlot());
            case CONTRACTS -> handleContractsClick(player, event);
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
        inventory.setItem(14, createItem(Material.EXPERIENCE_BOTTLE, "&eJob Skills", List.of(
                "&7Open your specialization trees",
                "&7without going through the jobs menu."
        )));
        inventory.setItem(16, createItem(Material.WRITABLE_BOOK, "&6Contracts", List.of(
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
        inventory.setItem(4, createItem(Material.WRITABLE_BOOK, "&6Job Contracts", List.of(
                "&7Take one active contract at a time.",
                "&7Deliver the requested goods for",
                "&7money, job XP, and rare materials."
        )));
        JobContract activeContract = plugin.getActiveJobContract(playerId);
        if (activeContract == null) {
            inventory.setItem(CONTRACT_ACTIVE_SLOT, createItem(Material.BARRIER, "&7No Active Contract", List.of(
                    "&7Accept a contract from one of your",
                    "&7owned professions below."
            )));
        } else {
            int currentAmount = plugin.getJobContractInventoryAmount(player, activeContract);
            inventory.setItem(CONTRACT_ACTIVE_SLOT, createItem(activeContract.requestedMaterial(), "&aActive Contract", List.of(
                    "&7Job: &f" + plugin.getProfessionPlainDisplayName(activeContract.profession()),
                    "&7Deliver: &f" + activeContract.requiredAmount() + "x " + plugin.formatMaterialName(activeContract.requestedMaterial()),
                    "&7Progress: &f" + Math.min(currentAmount, activeContract.requiredAmount()) + "&7/&f" + activeContract.requiredAmount(),
                    "&7Reward: &f$" + plugin.formatMoney(activeContract.rewardMoney()) + " &8+ &f" + activeContract.rewardXp() + " XP",
                    "&7Rare Material: &f" + plugin.formatRareContractMaterialName(activeContract.rareMaterialKey()) + " x" + activeContract.rareMaterialAmount(),
                    "",
                    currentAmount >= activeContract.requiredAmount()
                            ? "&eClick to turn in this contract."
                            : "&7Gather the goods, then come back."
            )));
            inventory.setItem(CONTRACT_ABANDON_SLOT, createItem(Material.BARRIER, "&cAbandon Contract", List.of(
                    "&7Drop your current contract.",
                    "&7Useful if you want to wait for",
                    "&7the next rotation."
            )));
        }

        List<JobContract> offers = plugin.generateAvailableJobContracts(playerId);
        for (int i = 0; i < offers.size() && i < CONTRACT_OFFER_SLOTS.length; i++) {
            JobContract offer = offers.get(i);
            inventory.setItem(CONTRACT_OFFER_SLOTS[i], createItem(
                    offer.requestedMaterial(),
                    "&e" + plugin.getProfessionPlainDisplayName(offer.profession()) + " Contract",
                    List.of(
                            "&7Deliver: &f" + offer.requiredAmount() + "x " + plugin.formatMaterialName(offer.requestedMaterial()),
                            "&7Reward: &f$" + plugin.formatMoney(offer.rewardMoney()) + " &8+ &f" + offer.rewardXp() + " XP",
                            "&7Rare Material: &f" + plugin.formatRareContractMaterialName(offer.rareMaterialKey()) + " x" + offer.rareMaterialAmount(),
                            "",
                            activeContract == null ? "&eClick to accept." : "&7Finish or abandon your active contract first."
                    )));
        }
        inventory.setItem(CONTRACT_REROLL_INFO_SLOT, createItem(Material.CLOCK, "&bOffer Rotation", List.of(
                "&7Contract offers rotate automatically.",
                "&7Next refresh in: &f" + plugin.formatLongDurationWords(plugin.getJobContractRefreshMillisRemaining())
        )));

        inventory.setItem(BACK_SLOT, createItem(Material.ARROW, "&eBack", List.of("&7Return to the main guide menu.")));
        inventory.setItem(CLOSE_SLOT, createItem(Material.BARRIER, "&cClose", List.of("&7Close the Terra Guide.")));
        player.openInventory(inventory);
    }

    private void openSkillSelectorMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(new TerraGuideHolder(GuidePage.SKILL_SELECTOR), GUI_SIZE, plugin.legacyComponent("&8Job Skills"));
        fillEmpty(inventory);

        UUID playerId = player.getUniqueId();
        List<Profession> ownedProfessions = plugin.getOwnedProfessions(playerId);
        inventory.setItem(SKILL_SELECTOR_INFO_SLOT, createItem(Material.EXPERIENCE_BOTTLE, "&6Job Skills", List.of(
                "&7Choose one of your jobs to open",
                "&7its specialization tree."
        )));

        if (ownedProfessions.isEmpty()) {
            inventory.setItem(22, createItem(Material.BARRIER, "&cNo Jobs", List.of(
                    "&7You need a profession before",
                    "&7you can use job skills."
            )));
        } else {
            int[] slots = {20, 24, 29, 31};
            for (int i = 0; i < ownedProfessions.size() && i < slots.length; i++) {
                Profession profession = ownedProfessions.get(i);
                inventory.setItem(slots[i], createItem(plugin.getProfessionIcon(profession), plugin.getProfessionDisplayName(profession), List.of(
                        "&7Level: &f" + plugin.getProfessionLevel(playerId, profession),
                        "&7Available points: &f" + plugin.getAvailableProfessionSkillPoints(playerId, profession),
                        profession == plugin.getSecondaryProfession(playerId)
                                ? "&7Role: &fSecondary"
                                : "&7Role: &fMain",
                        "",
                        "&eClick to open skill tree."
                )));
            }
        }

        inventory.setItem(BACK_SLOT, createItem(Material.ARROW, "&eBack", List.of("&7Return to the main guide menu.")));
        inventory.setItem(CLOSE_SLOT, createItem(Material.BARRIER, "&cClose", List.of("&7Close the Terra Guide.")));
        player.openInventory(inventory);
    }

    private void handleMainClick(Player player, int slot) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        switch (slot) {
            case 10 -> openStatsMenu(player);
            case 12 -> plugin.openProfessionMenu(player);
            case 14 -> openSkillSelectorMenu(player);
            case 16 -> openContractsMenu(player);
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

    private void handleSkillSelectorClick(Player player, int slot) {
        if (handleBackClose(player, slot)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        Profession selected = switch (slot) {
            case 20, 24, 29, 31 -> {
                List<Profession> ownedProfessions = plugin.getOwnedProfessions(playerId);
                int index = switch (slot) {
                    case 20 -> 0;
                    case 24 -> 1;
                    case 29 -> 2;
                    case 31 -> 3;
                    default -> -1;
                };
                yield index >= 0 && index < ownedProfessions.size() ? ownedProfessions.get(index) : null;
            }
            default -> null;
        };
        if (selected != null) {
            plugin.openProfessionSkillTreeMenu(player, selected);
        }
    }

    private void handleContractsClick(Player player, InventoryClickEvent event) {
        int slot = event.getSlot();
        if (handleBackClose(player, slot)) {
            return;
        }
        UUID playerId = player.getUniqueId();
        JobContract activeContract = plugin.getActiveJobContract(playerId);
        if (slot == CONTRACT_ACTIVE_SLOT && activeContract != null) {
            if (plugin.completeActiveJobContract(player)) {
                openContractsMenu(player);
            } else {
                int currentAmount = plugin.getJobContractInventoryAmount(player, activeContract);
                player.sendMessage(plugin.colorize("&cYou still need &f"
                        + Math.max(0, activeContract.requiredAmount() - currentAmount) + "&c more "
                        + plugin.formatMaterialName(activeContract.requestedMaterial()) + "."));
            }
            return;
        }
        if (slot == CONTRACT_ABANDON_SLOT && activeContract != null) {
            plugin.abandonActiveJobContract(playerId);
            player.sendMessage(plugin.colorize("&cYou abandoned your active contract."));
            openContractsMenu(player);
            return;
        }
        for (int i = 0; i < CONTRACT_OFFER_SLOTS.length; i++) {
            if (slot != CONTRACT_OFFER_SLOTS[i]) {
                continue;
            }
            List<JobContract> offers = plugin.generateAvailableJobContracts(playerId);
            if (i >= offers.size()) {
                return;
            }
            if (activeContract != null) {
                player.sendMessage(plugin.colorize("&cFinish or abandon your active contract first."));
                return;
            }
            JobContract offer = offers.get(i);
            if (plugin.acceptGeneratedJobContract(playerId, offer.profession())) {
                player.sendMessage(plugin.colorize("&aAccepted a &f" + plugin.getProfessionPlainDisplayName(offer.profession())
                        + "&a contract. Deliver &f" + offer.requiredAmount() + "x "
                        + plugin.formatMaterialName(offer.requestedMaterial()) + "&a."));
                openContractsMenu(player);
            }
            return;
        }
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
        SKILL_SELECTOR,
        CONTRACTS
    }

    private record TerraGuideHolder(GuidePage page) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
