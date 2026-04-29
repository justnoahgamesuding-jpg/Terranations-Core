package me.meetrow.testproject;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

public class CountryGuiListener implements Listener {
    private static final DateTimeFormatter LAST_SEEN_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int GUI_SIZE = 54;
    private static final int INFO_SLOT = 4;
    private static final int OWNER_SLOT = 19;
    private static final int HOME_SLOT = 20;
    private static final int ECONOMY_SLOT = 21;
    private static final int PROGRESSION_SLOT = 22;
    private static final int MEMBERS_SLOT = 23;
    private static final int INVITE_HELP_SLOT = 24;
    private static final int COUNTRY_INFO_SLOT = 28;
    private static final int FARMLAND_SLOT = 29;
    private static final int LIST_SLOT = 30;
    private static final int TERRITORY_INFO_SLOT = 31;
    private static final int JOIN_STATUS_SLOT = 32;
    private static final int SET_HOME_SLOT = 33;
    private static final int TRADER_SPAWN_SLOT = 34;
    private static final int TRANSFER_SLOT = 35;
    private static final int CLOSE_SLOT = 49;
    private static final int LEAVE_SLOT = 50;
    private static final int MEMBER_MENU_INFO_SLOT = 4;
    private static final int MEMBER_MENU_BACK_SLOT = 49;
    private static final int ADMIN_INFO_SLOT = 4;
    private static final int ADMIN_OWNER_SLOT = 19;
    private static final int ADMIN_MEMBERS_SLOT = 20;
    private static final int ADMIN_STATUS_SLOT = 21;
    private static final int ADMIN_REPUTATION_SLOT = 22;
    private static final int ADMIN_INFO_COMMAND_SLOT = 23;
    private static final int ADMIN_TERRITORY_SLOT = 24;
    private static final int ADMIN_CLOSE_SLOT = 49;
    private static final int PROGRESSION_NAME_SLOT = 4;
    private static final int PROGRESSION_PAGE_INFO_SLOT = 11;
    private static final int PROGRESSION_PAGE_BADGE_SLOT = 12;
    private static final int PROGRESSION_LEVEL_SLOT = 4;
    private static final int PROGRESSION_BACK_SLOT = 45;
    private static final int PROGRESSION_TREASURY_SLOT = 49;
    private static final int PROGRESSION_NEXT_SLOT = 53;
    private static final int[] PROGRESSION_TRACK_SLOTS = {27, 28, 29, 30, 31, 32, 33, 34, 35};
    private static final int[] PROGRESSION_ACCENT_SLOTS = {
            0, 1, 2, 3, 5, 6, 7, 8, 9, 10, 13, 14, 15, 16, 17, 18,
            19, 20, 21, 22, 23, 24, 25, 26, 36, 37, 38, 39, 40, 41,
            42, 43, 44, 46, 47, 48, 50, 51, 52
    };
    private static final int DEPOSIT_INFO_SLOT = 4;
    private static final int DEPOSIT_AMOUNT_SLOT = 22;
    private static final int DEPOSIT_ADD_HALF_SLOT = 19;
    private static final int DEPOSIT_ADD_ONE_SLOT = 20;
    private static final int DEPOSIT_ADD_TEN_SLOT = 21;
    private static final int DEPOSIT_ADD_HUNDRED_SLOT = 23;
    private static final int DEPOSIT_CUSTOM_SLOT = 24;
    private static final int DEPOSIT_RESET_SLOT = 28;
    private static final int DEPOSIT_ALL_SLOT = 30;
    private static final int DEPOSIT_CONFIRM_SLOT = 31;
    private static final int DEPOSIT_BACK_SLOT = 49;
    private static final int LIST_INFO_SLOT = 4;
    private static final int LIST_FILTER_SLOT = 19;
    private static final int LIST_SORT_SLOT = 21;
    private static final int LIST_PREVIOUS_SLOT = 45;
    private static final int LIST_NEXT_SLOT = 53;
    private static final int LIST_BACK_SLOT = 49;
    private static final int[] LIST_COUNTRY_SLOTS = {
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int[] MEMBER_MENU_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int MINER_HEADER_SLOT = 0;
    private static final int[] MINER_MEMBER_SLOTS = {1, 2, 3, 5, 6, 7};
    private static final int LUMBERJACK_HEADER_SLOT = 9;
    private static final int[] LUMBERJACK_MEMBER_SLOTS = {10, 11, 12, 13, 14, 15, 16, 17};
    private static final int FARMER_HEADER_SLOT = 18;
    private static final int[] FARMER_MEMBER_SLOTS = {19, 20, 21, 22, 23, 24, 25, 26};
    private static final int BUILDER_HEADER_SLOT = 27;
    private static final int[] BUILDER_MEMBER_SLOTS = {28, 29, 30, 31, 32, 33, 34, 35};
    private static final int BLACKSMITH_HEADER_SLOT = 36;
    private static final int[] BLACKSMITH_MEMBER_SLOTS = {37, 38, 39, 40, 41, 42, 43, 44};
    private static final int CONFIRM_INFO_SLOT = 13;
    private static final int CONFIRM_ACCEPT_SLOT = 29;
    private static final int CONFIRM_CANCEL_SLOT = 33;

    private final Testproject plugin;
    private final Map<UUID, Double> selectedDepositAmounts = new ConcurrentHashMap<>();
    private final Set<UUID> awaitingDepositChatInput = ConcurrentHashMap.newKeySet();

    public CountryGuiListener(Testproject plugin) {
        this.plugin = plugin;
    }

    public void openCountryMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(
                new CountryMenuHolder(player.getUniqueId()),
                GUI_SIZE,
                plugin.legacyComponent("&8Country Dashboard")
        );

        fillEmptySlots(inventory);
        Country playerCountry = plugin.getPlayerCountry(player.getUniqueId());
        Country locationCountry = plugin.getPlayerTerritoryCountryAt(player.getLocation());
        boolean ownerView = canSeeOwnerControls(player, playerCountry);

        inventory.setItem(INFO_SLOT, createDashboardInfoItem(player, playerCountry, locationCountry));
        setCommandItemIfAvailable(inventory, HOME_SLOT, createCommandItem(
                Material.ENDER_PEARL,
                "&bCountry Home",
                List.of("&7Teleport to your country home.", "&7Runs: &f/country home"),
                playerCountry != null && canUse(player, Testproject.COUNTRY_HOME_PERMISSION)
        ));
        if (playerCountry != null && playerCountry.hasOwner()) {
            inventory.setItem(OWNER_SLOT, createOwnerItem(playerCountry));
        }
        setCommandItemIfAvailable(inventory, ECONOMY_SLOT, createEconomyItem(playerCountry));
        setCommandItemIfAvailable(inventory, PROGRESSION_SLOT, createCommandItem(
                Material.NETHER_STAR,
                "&6Country Upgrades",
                List.of("&7View country level progress, treasury,", "&7and unlocked bonuses.", "&7Runs: &f/country upgrade"),
                playerCountry != null
        ));
        setCommandItemIfAvailable(inventory, INVITE_HELP_SLOT, createCommandItem(
                Material.WRITABLE_BOOK,
                "&aInvite Player",
                List.of("&7Invite new players into your country.", "&7Suggested: &f/country invite <player>"),
                playerCountry != null && canUse(player, Testproject.COUNTRY_INVITE_PERMISSION)
        ));
        if (playerCountry != null) {
            inventory.setItem(MEMBERS_SLOT, createMembersItem(playerCountry));
        }
        setCommandItemIfAvailable(inventory, COUNTRY_INFO_SLOT, createCommandItem(
                Material.BOOK,
                "&eCountry Info",
                List.of("&7View detailed country information.", "&7Runs: &f/country info"),
                playerCountry != null && ownerView && canUse(player, Testproject.COUNTRY_INFO_PERMISSION)
        ));
        setCommandItemIfAvailable(inventory, FARMLAND_SLOT, createCommandItem(
                Material.WHEAT,
                "&aFarmland",
                List.of("&7View farmland usage for your country.", "&7Runs: &f/country farmland"),
                playerCountry != null && ownerView && canUse(player, Testproject.COUNTRY_FARMLAND_PERMISSION)
        ));
        setCommandItemIfAvailable(inventory, LIST_SLOT, createCommandItem(
                Material.MAP,
                "&eCountry Browser",
                List.of("&7Browse all countries on the server.", "&7Runs: &f/country list"),
                ownerView && canUse(player, Testproject.COUNTRY_LIST_PERMISSION)
        ));
        setCommandItemIfAvailable(inventory, TERRITORY_INFO_SLOT, createCommandItem(
                Material.GRASS_BLOCK,
                "&2Territory",
                List.of("&7Inspect the linked territory region.", "&7Runs: &f/country territory info <country>"),
                playerCountry != null && ownerView && canUse(player, Testproject.COUNTRY_TERRITORY_PERMISSION)
        ));
        setCommandItemIfAvailable(inventory, JOIN_STATUS_SLOT, createCommandItem(
                Material.LEVER,
                "&eJoin Status",
                List.of("&7Switch between open and invite-only.", "&7Runs: &f/country joinstatus <open|closed>"),
                playerCountry != null && ownerView && canUse(player, Testproject.COUNTRY_JOINSTATUS_PERMISSION)
        ));
        setCommandItemIfAvailable(inventory, SET_HOME_SLOT, createCommandItem(
                Material.LODESTONE,
                "&bSet Home",
                List.of("&7Set your country home from inside", "&7your own territory.", "&7Runs: &f/country sethome"),
                playerCountry != null && ownerView && canUse(player, Testproject.COUNTRY_SETHOME_PERMISSION)
        ));
        setCommandItemIfAvailable(inventory, LEAVE_SLOT, createCommandItem(
                Material.BARRIER,
                "&cLeave Country",
                List.of("&7Open a confirmation menu before", "&7leaving your current country."),
                playerCountry != null && canUse(player, Testproject.COUNTRY_LEAVE_PERMISSION)
        ));
        setCommandItemIfAvailable(inventory, TRANSFER_SLOT, createCommandItem(
                Material.TOTEM_OF_UNDYING,
                "&6Transfer Country",
                List.of("&7Choose a country member and confirm", "&7the ownership transfer request."),
                canOpenTransferMenu(player, playerCountry)
        ));
        setCommandItemIfAvailable(inventory, TRADER_SPAWN_SLOT, createCommandItem(
                Material.CHEST_MINECART,
                "&6Trader Spawn",
                List.of("&7Set your country trader spawn from", "&7inside your own territory.", "&7Runs: &f/country manage settraderspawn"),
                playerCountry != null && ownerView && canUse(player, Testproject.COUNTRY_SETHOME_PERMISSION)
        ));
        inventory.setItem(CLOSE_SLOT, createSimpleItem(Material.ARROW, "&eClose", List.of("&7Close this menu.")));

        player.openInventory(inventory);
    }

    public void openLeaveConfirmMenu(Player player) {
        openConfirmMenu(player, ConfirmAction.LEAVE, null);
    }

    public void openCountryListMenu(Player player) {
        openCountryListMenu(player, CountryListFilter.ANY, CountryListSort.ALPHABETICAL, 0);
    }

    public void openCountryProgressionMenu(Player player) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            return;
        }
        int safePage = Math.max(0, Math.min(plugin.getCountryLevel(country) - 1, plugin.getCountryMaxLevel() - 1));
        int pageLevel = safePage + 1;

        Inventory inventory = Bukkit.createInventory(
                new CountryProgressionMenuHolder(player.getUniqueId(), country.getName(), safePage),
                GUI_SIZE,
                plugin.legacyComponent("&8Country Upgrades")
        );

        decorateProgressionInventory(inventory);
        inventory.setItem(PROGRESSION_NAME_SLOT, createCountryLevelShowcaseItem(country));
        inventory.setItem(PROGRESSION_PAGE_INFO_SLOT, createCountryPageInfoItem(country, pageLevel));
        inventory.setItem(PROGRESSION_PAGE_BADGE_SLOT, createCountryPageBadgeItem(country, pageLevel));
        inventory.setItem(PROGRESSION_TREASURY_SLOT, createSimpleItem(Material.GOLD_INGOT, "&6Open Treasury", List.of(
                "&7Open the country treasury deposit menu.",
                "&7Current treasury: &f⛃" + plugin.formatMoney(plugin.getCountryBalance(country))
        )));
        if (safePage <= 0) {
            inventory.setItem(PROGRESSION_BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the country dashboard.")));
        } else {
            inventory.setItem(PROGRESSION_BACK_SLOT, createSimpleItem(Material.ARROW, "&ePrevious Page", List.of(
                    "&7Go to country level page &f" + safePage + "&7.",
                    "&7Current page: &f" + pageLevel + "&7/&f" + plugin.getCountryMaxLevel()
            )));
        }
        if (safePage >= plugin.getCountryMaxLevel() - 1) {
            inventory.setItem(PROGRESSION_NEXT_SLOT, createSimpleItem(Material.BARRIER, "&7Next Page", List.of(
                    "&7You are already on the last country level page."
            )));
        } else {
            inventory.setItem(PROGRESSION_NEXT_SLOT, createSimpleItem(Material.ARROW, "&eNext Page", List.of(
                    "&7Go to country level page &f" + (pageLevel + 1) + "&7.",
                    "&7Current page: &f" + pageLevel + "&7/&f" + plugin.getCountryMaxLevel()
            )));
        }
        renderProgressionTrack(inventory, player, country, pageLevel);

        player.openInventory(inventory);
    }

    public void openCountryAdminMenu(Player player, Country country) {
        if (player == null || country == null) {
            return;
        }

        Inventory inventory = Bukkit.createInventory(
                new CountryAdminMenuHolder(player.getUniqueId(), country.getName()),
                GUI_SIZE,
                plugin.legacyComponent("&8Admin Country Control")
        );

        fillEmptySlots(inventory);
        inventory.setItem(ADMIN_INFO_SLOT, createCountryAdminOverviewItem(country));
        inventory.setItem(ADMIN_OWNER_SLOT, createOwnerItem(country));
        inventory.setItem(ADMIN_MEMBERS_SLOT, createCountryAdminMembersItem(country));
        inventory.setItem(ADMIN_STATUS_SLOT, createCountryAdminJoinStatusItem(country));
        inventory.setItem(ADMIN_REPUTATION_SLOT, createCountryAdminTraderReputationItem(country));
        inventory.setItem(ADMIN_INFO_COMMAND_SLOT, createSimpleItem(Material.BOOK, "&eDetailed Info", List.of(
                "&7Runs: &f/country info " + country.getName(),
                "&eClick to view the full text info output."
        )));
        inventory.setItem(ADMIN_TERRITORY_SLOT, createSimpleItem(Material.GRASS_BLOCK, "&2Territory Info", List.of(
                "&7Runs: &f/country territory info " + country.getName(),
                "&eClick to inspect the linked territory."
        )));
        inventory.setItem(ADMIN_CLOSE_SLOT, createSimpleItem(Material.ARROW, "&eClose", List.of("&7Close this admin menu.")));
        player.openInventory(inventory);
    }

    private void openCountryProgressionMenu(Player player, int page) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            return;
        }
        int safePage = Math.max(0, Math.min(page, plugin.getCountryMaxLevel() - 1));
        int pageLevel = safePage + 1;

        Inventory inventory = Bukkit.createInventory(
                new CountryProgressionMenuHolder(player.getUniqueId(), country.getName(), safePage),
                GUI_SIZE,
                plugin.legacyComponent("&8Country Upgrades")
        );

        decorateProgressionInventory(inventory);
        inventory.setItem(PROGRESSION_NAME_SLOT, createCountryLevelShowcaseItem(country));
        inventory.setItem(PROGRESSION_PAGE_INFO_SLOT, createCountryPageInfoItem(country, pageLevel));
        inventory.setItem(PROGRESSION_PAGE_BADGE_SLOT, createCountryPageBadgeItem(country, pageLevel));
        inventory.setItem(PROGRESSION_TREASURY_SLOT, createSimpleItem(Material.GOLD_INGOT, "&6Open Treasury", List.of(
                "&7Open the country treasury deposit menu.",
                "&7Current treasury: &fâ›ƒ" + plugin.formatMoney(plugin.getCountryBalance(country))
        )));
        if (safePage <= 0) {
            inventory.setItem(PROGRESSION_BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the country dashboard.")));
        } else {
            inventory.setItem(PROGRESSION_BACK_SLOT, createSimpleItem(Material.ARROW, "&ePrevious Page", List.of(
                    "&7Go to country level page &f" + safePage + "&7.",
                    "&7Current page: &f" + pageLevel + "&7/&f" + plugin.getCountryMaxLevel()
            )));
        }
        if (safePage >= plugin.getCountryMaxLevel() - 1) {
            inventory.setItem(PROGRESSION_NEXT_SLOT, createSimpleItem(Material.BARRIER, "&7Next Page", List.of(
                    "&7You are already on the last country level page."
            )));
        } else {
            inventory.setItem(PROGRESSION_NEXT_SLOT, createSimpleItem(Material.ARROW, "&eNext Page", List.of(
                    "&7Go to country level page &f" + (pageLevel + 1) + "&7.",
                    "&7Current page: &f" + pageLevel + "&7/&f" + plugin.getCountryMaxLevel()
            )));
        }
        renderProgressionTrack(inventory, player, country, pageLevel);

        player.openInventory(inventory);
    }

    public void openCountryDepositMenu(Player player) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            return;
        }
        double selectedAmount = selectedDepositAmounts.getOrDefault(player.getUniqueId(), 0.0D);

        Inventory inventory = Bukkit.createInventory(
                new CountryDepositMenuHolder(player.getUniqueId(), country.getName()),
                GUI_SIZE,
                plugin.legacyComponent("&8Country Treasury")
        );

        fillEmptySlots(inventory);
        inventory.setItem(DEPOSIT_INFO_SLOT, createCountryDepositInfoItem(player, country));
        inventory.setItem(DEPOSIT_AMOUNT_SLOT, createDepositAmountItem(player, selectedAmount));
        inventory.setItem(DEPOSIT_ADD_HALF_SLOT, createDepositOptionItem("+0.50", 0.50D));
        inventory.setItem(DEPOSIT_ADD_ONE_SLOT, createDepositOptionItem("+1", 1.0D));
        inventory.setItem(DEPOSIT_ADD_TEN_SLOT, createDepositOptionItem("+10", 10.0D));
        inventory.setItem(DEPOSIT_ADD_HUNDRED_SLOT, createDepositOptionItem("+100", 100.0D));
        inventory.setItem(DEPOSIT_CUSTOM_SLOT, createSimpleItem(Material.WRITABLE_BOOK, "&bType Amount", List.of(
                "&7Click to enter an exact amount in chat.",
                "&7This will replace the current selected amount."
        )));
        inventory.setItem(DEPOSIT_RESET_SLOT, createSimpleItem(Material.BARRIER, "&cReset Amount", List.of(
                "&7Set the selected amount back to ⛃0.00."
        )));
        inventory.setItem(DEPOSIT_ALL_SLOT, createSimpleItem(Material.GOLD_BLOCK, "&eUse Full Balance", List.of(
                "&7Set the selected amount to your full balance.",
                "&7Current balance: &f⛃" + plugin.formatMoney(plugin.getBalance(player.getUniqueId()))
        )));
        inventory.setItem(DEPOSIT_CONFIRM_SLOT, createSimpleItem(Material.LIME_CONCRETE, "&aDeposit Selected", List.of(
                "&7Deposit the currently selected amount",
                "&7into your country treasury."
        )));
        inventory.setItem(DEPOSIT_BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the country dashboard.")));

        player.openInventory(inventory);
    }

    public void openAcceptTransferConfirmMenu(Player player) {
        openConfirmMenu(player, ConfirmAction.ACCEPT_TRANSFER, null);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof CountryInventoryHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        if (holder instanceof CountryMembersMenuHolder membersMenuHolder) {
            if (event.getSlot() == MEMBER_MENU_BACK_SLOT) {
                if (membersMenuHolder.adminView()) {
                    Country country = plugin.getCountry(membersMenuHolder.countryName());
                    if (country != null) {
                        openCountryAdminMenu(player, country);
                    } else {
                        player.closeInventory();
                    }
                } else {
                    openCountryMenu(player);
                }
            }
            return;
        }

        if (holder instanceof CountryAdminMenuHolder adminMenuHolder) {
            handleCountryAdminClick(player, adminMenuHolder, event);
            return;
        }

        if (holder instanceof CountryProgressionMenuHolder progressionMenuHolder) {
            handleCountryProgressionClick(player, progressionMenuHolder, event.getSlot());
            return;
        }

        if (holder instanceof CountryDepositMenuHolder depositMenuHolder) {
            handleCountryDepositClick(player, depositMenuHolder, event.getSlot());
            return;
        }

        if (holder instanceof CountryListMenuHolder listMenuHolder) {
            handleCountryListClick(player, listMenuHolder, event.getSlot());
            return;
        }

        if (holder instanceof CountryTransferMembersMenuHolder transferMembersMenuHolder) {
            if (event.getSlot() == MEMBER_MENU_BACK_SLOT) {
                openCountryMenu(player);
                return;
            }
            UUID targetId = getTransferTargetBySlot(transferMembersMenuHolder, event.getSlot());
            if (targetId != null) {
                openConfirmMenu(player, ConfirmAction.TRANSFER, targetId);
            }
            return;
        }

        if (holder instanceof CountryConfirmMenuHolder confirmMenuHolder) {
            if (event.getSlot() == CONFIRM_CANCEL_SLOT) {
                openCountryMenu(player);
                return;
            }
            if (event.getSlot() == CONFIRM_ACCEPT_SLOT) {
                executeConfirmedAction(player, confirmMenuHolder);
            }
            return;
        }

        if (holder instanceof CountryUpgradeConfirmMenuHolder upgradeConfirmMenuHolder) {
            handleUpgradeConfirmClick(player, upgradeConfirmMenuHolder, event.getSlot());
            return;
        }

        switch (event.getSlot()) {
            case COUNTRY_INFO_SLOT -> executeCountryCommand(player, "country info");
            case HOME_SLOT -> executeCountryCommand(player, "country home");
            case ECONOMY_SLOT -> openCountryDepositMenu(player);
            case FARMLAND_SLOT -> executeCountryCommand(player, "country farmland");
            case LIST_SLOT -> executeCountryCommand(player, "country list");
            case PROGRESSION_SLOT -> executeCountryCommand(player, "country upgrade");
            case JOIN_STATUS_SLOT -> {
                Country country = plugin.getPlayerCountry(player.getUniqueId());
                if (country != null) {
                    executeCountryCommand(player, "country joinstatus " + (country.isOpen() ? "closed" : "open"));
                }
            }
            case SET_HOME_SLOT -> executeCountryCommand(player, "country sethome");
            case INVITE_HELP_SLOT -> {
                player.closeInventory();
                player.sendMessage(plugin.getMessage("country.usage.invite"));
            }
            case MEMBERS_SLOT -> {
                Country country = plugin.getPlayerCountry(player.getUniqueId());
                if (country != null) {
                    openMembersMenu(player, country);
                }
            }
            case TERRITORY_INFO_SLOT -> {
                Country country = plugin.getPlayerCountry(player.getUniqueId());
                if (country != null) {
                    executeCountryCommand(player, "country territory info " + country.getName());
                }
            }
            case LEAVE_SLOT -> openConfirmMenu(player, ConfirmAction.LEAVE, null);
            case TRANSFER_SLOT -> {
                Country country = plugin.getPlayerCountry(player.getUniqueId());
                if (country != null && canOpenTransferMenu(player, country)) {
                    openTransferMembersMenu(player, country);
                }
            }
            case TRADER_SPAWN_SLOT -> executeCountryCommand(player, "country manage settraderspawn");
            case CLOSE_SLOT -> player.closeInventory();
            default -> {
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof CountryMenuHolder) {
            event.setCancelled(true);
        }
        if (event.getView().getTopInventory().getHolder() instanceof CountryMembersMenuHolder) {
            event.setCancelled(true);
        }
        if (event.getView().getTopInventory().getHolder() instanceof CountryProgressionMenuHolder) {
            event.setCancelled(true);
        }
        if (event.getView().getTopInventory().getHolder() instanceof CountryDepositMenuHolder) {
            event.setCancelled(true);
        }
        if (event.getView().getTopInventory().getHolder() instanceof CountryListMenuHolder) {
            event.setCancelled(true);
        }
        if (event.getView().getTopInventory().getHolder() instanceof CountryTransferMembersMenuHolder) {
            event.setCancelled(true);
        }
        if (event.getView().getTopInventory().getHolder() instanceof CountryConfirmMenuHolder) {
            event.setCancelled(true);
        }
    }

    private void executeCountryCommand(Player player, String command) {
        player.closeInventory();
        player.performCommand(command);
    }

    private void handleCountryAdminClick(Player player, CountryAdminMenuHolder holder, InventoryClickEvent event) {
        if (event.getSlot() == ADMIN_CLOSE_SLOT) {
            player.closeInventory();
            return;
        }

        Country country = plugin.getCountry(holder.countryName());
        if (country == null) {
            player.closeInventory();
            player.sendMessage(plugin.getMessage("country.not-found"));
            return;
        }

        switch (event.getSlot()) {
            case ADMIN_MEMBERS_SLOT -> openMembersMenu(player, country, true);
            case ADMIN_STATUS_SLOT -> {
                plugin.setCountryOpen(country, !country.isOpen());
                player.sendMessage(plugin.getMessage(country.isOpen() ? "country.joinstatus-open" : "country.joinstatus-closed", plugin.placeholders(
                        "country", country.getName()
                )));
                openCountryAdminMenu(player, country);
            }
            case ADMIN_REPUTATION_SLOT -> {
                double change = event.isShiftClick() ? 5.0D : 1.0D;
                if (event.isRightClick()) {
                    change = -change;
                }
                double next = Math.max(0.0D, plugin.getCountryTotalTraderReputation(country) + change);
                if (plugin.setCountryTotalTraderReputation(country, next)) {
                    player.sendMessage(plugin.getMessage("country.admin.set-trader-reputation-success", plugin.placeholders(
                            "country", country.getName(),
                            "reputation", plugin.formatTraderReputation(plugin.getCountryTotalTraderReputation(country))
                    )));
                } else {
                    player.sendMessage(plugin.getMessage("country.admin.set-trader-reputation-failed", plugin.placeholders(
                            "country", country.getName()
                    )));
                }
                openCountryAdminMenu(player, country);
            }
            case ADMIN_INFO_COMMAND_SLOT -> executeCountryCommand(player, "country info " + country.getName());
            case ADMIN_TERRITORY_SLOT -> executeCountryCommand(player, "country territory info " + country.getName());
            default -> {
            }
        }
    }

    private ItemStack createDashboardInfoItem(Player player, Country playerCountry, Country locationCountry) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Your country: &f" + (playerCountry != null ? playerCountry.getName() : "None"));
        lore.add("&7Territory here: &f" + (locationCountry != null ? locationCountry.getName() : "Wilderness"));
        if (playerCountry != null) {
            CountryRole role = plugin.getCountryRole(playerCountry, player.getUniqueId());
            lore.add("&7Your role: &f" + (role != null ? role.getDisplayName() : "Member"));
            lore.add("&7Country level: &f" + plugin.getCountryLevel(playerCountry));
            lore.add("&7Members: &f" + playerCountry.getMembers().size());
            lore.add("&7Home cooldown: &f" + plugin.formatDurationWords(plugin.getCountryHomeCooldownRemaining(player.getUniqueId())));
            lore.add("&7Treasury: &f⛃" + plugin.formatMoney(plugin.getCountryBalance(playerCountry)));
        } else {
            lore.add("&7You are not in a country right now.");
            lore.add("&7This menu focuses on your own country once");
            lore.add("&7you join or create one.");
        }
        return createSimpleItem(Material.NAME_TAG, "&eCountry Dashboard", lore);
    }

    private ItemStack createCountryAdminOverviewItem(Country country) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Country: &f" + country.getName());
        lore.add("&7Members: &f" + country.getMembers().size());
        lore.add("&7Level: &f" + plugin.getCountryLevel(country));
        lore.add("&7Join status: &f" + (country.isOpen() ? "Open" : "Invite only"));
        lore.add("&7Treasury: &f⛃" + plugin.formatMoney(plugin.getCountryBalance(country)));
        lore.add("&7Resources: &f" + plugin.getCountryResources(country));
        lore.add("&7Trader reputation: &f" + plugin.formatTraderReputation(plugin.getCountryTotalTraderReputation(country)));
        lore.add("&7Trader score: &f" + plugin.getCountryTradeReputationScore(country));
        return createSimpleItem(Material.COMPASS, "&6Admin Overview", lore);
    }

    private ItemStack createCountryAdminMembersItem(Country country) {
        return createSimpleItem(Material.BOOKSHELF, "&eMembers", List.of(
                "&7View the members of &f" + country.getName(),
                "&7sorted by profession and level.",
                "",
                "&7Members: &f" + country.getMembers().size(),
                "&eClick to open"
        ));
    }

    private ItemStack createCountryAdminJoinStatusItem(Country country) {
        return createSimpleItem(country.isOpen() ? Material.LIME_DYE : Material.RED_DYE, "&eJoin Status", List.of(
                "&7Current: &f" + (country.isOpen() ? "Open" : "Invite only"),
                "&eClick to toggle this country."
        ));
    }

    private ItemStack createCountryAdminTraderReputationItem(Country country) {
        return createSimpleItem(Material.EMERALD, "&6Trader Reputation", List.of(
                "&7Country total: &f" + plugin.formatTraderReputation(plugin.getCountryTotalTraderReputation(country)),
                "&7Trader score: &f" + plugin.getCountryTradeReputationScore(country),
                "",
                "&eLeft click: &a+1.0",
                "&eRight click: &c-1.0",
                "&eShift-left: &a+5.0",
                "&eShift-right: &c-5.0"
        ));
    }

    private ItemStack createOwnerItem(Country country) {
        if (country == null || !country.hasOwner()) {
            return createSimpleItem(Material.GRAY_DYE, "&6Country Owner", List.of("&7You are not currently in a country."));
        }

        return createPlayerItem(Bukkit.getOfflinePlayer(country.getOwnerId()), "&6Country Owner", List.of(
                "&7Owner: &f" + plugin.safeOfflineName(country.getOwnerId())
        ));
    }

    private ItemStack createMembersItem(Country country) {
        if (country == null) {
            return createSimpleItem(Material.GRAY_DYE, "&eMembers", List.of("&7Join or create a country to view members."));
        }

        return createSimpleItem(Material.BOOKSHELF, "&eMembers", List.of(
                "&7View all country members sorted by",
                "&7current job and then highest level.",
                "",
                "&7Members: &f" + country.getMembers().size(),
                "&eClick to open"
        ));
    }

    private ItemStack createEconomyItem(Country country) {
        if (country == null) {
            return null;
        }
        return createSimpleItem(Material.GOLD_INGOT, "&6Treasury", List.of(
                "&7Treasury: &f⛃" + plugin.formatMoney(plugin.getCountryBalance(country)),
                "&7Active boost: &f" + plugin.getCountryActiveBoostDisplay(country),
                "",
                "&eClick to open quick treasury deposit"
        ));
    }

    private ItemStack createCountryDepositInfoItem(Player player, Country country) {
        return createSimpleItem(Material.GOLD_INGOT, "&6" + country.getName() + " Treasury", List.of(
                "&7Your balance: &f⛃" + plugin.formatMoney(plugin.getBalance(player.getUniqueId())),
                "&7Country treasury: &f⛃" + plugin.formatMoney(plugin.getCountryBalance(country)),
                "",
                "&7Build an amount with the buttons below",
                "&7or type an exact amount in chat."
        ));
    }

    private ItemStack createDepositOptionItem(String title, double amount) {
        return createSimpleItem(Material.SUNFLOWER, "&e" + title, List.of(
                "&7Amount: &f⛃" + plugin.formatMoney(amount),
                "&eClick to add this to the selected amount"
        ));
    }

    private ItemStack createDepositAmountItem(Player player, double amount) {
        double balance = plugin.getBalance(player.getUniqueId());
        boolean affordable = amount > 0.0D && balance + 0.0001D >= amount;
        return createSimpleItem(affordable ? Material.EMERALD : Material.PAPER, "&6Selected Amount", List.of(
                "&7Selected: &f⛃" + plugin.formatMoney(amount),
                "&7Your balance: &f⛃" + plugin.formatMoney(balance),
                affordable ? "&aReady to deposit" : "&cSelected amount is above your balance"
        ));
    }

    private ItemStack createCountryProgressInfoItem(Player player, Country country) {
        CountryRole role = plugin.getCountryRole(country, player.getUniqueId());
        return createSimpleItem(Material.NETHER_STAR, "&6" + country.getName() + " Upgrade Tree", List.of(
                "&7Snake path: &fleft to right",
                "&7Current rank: &f" + (role != null ? role.getDisplayName() : "Guest"),
                "&7Progress score: &f" + plugin.getCountryProgressScore(country),
                "&7Unlocked nodes: &f" + country.getUnlockedUpgradeKeys().size() + "&7/&f" + getTotalProgressNodeCount(),
                "&7Mix: &futility, XP, and money upgrades",
                "",
                plugin.canUnlockCountryProgress(country, player.getUniqueId())
                        ? "&aYou can unlock eligible nodes here."
                        : "&7You can inspect progress here."
        ));
    }

    private ItemStack createCountryLevelShowcaseItem(Country country) {
        int level = plugin.getCountryLevel(country);
        int maxLevel = plugin.getCountryMaxLevel();
        int unlocks = country.getUnlockedUpgradeKeys().size();
        return createSimpleItem(Material.EXPERIENCE_BOTTLE, "&eCountry Level " + level + " &8/ " + maxLevel, List.of(
                "&7" + country.getName(),
                "&7This path is built for steady progress.",
                "&7Small country perks stack into strong late game power.",
                "",
                "&7Country score: &f" + plugin.getCountryProgressScore(country),
                "&7Unlocked nodes: &f" + unlocks + "&7/&f" + getTotalProgressNodeCount()
        ));
    }

    private ItemStack createCountryPageInfoItem(Country country, int pageLevel) {
        return createSimpleItem(Material.GOLD_NUGGET, "&6Country Level " + pageLevel, getCountryLevelRewardLines(country, pageLevel));
    }

    private ItemStack createCountryPageBadgeItem(Country country, int pageLevel) {
        int currentLevel = plugin.getCountryLevel(country);
        int targetLevel = Math.min(plugin.getCountryMaxLevel(), pageLevel + 1);
        boolean reached = currentLevel >= targetLevel;
        boolean currentPage = pageLevel == currentLevel;
        boolean canLevelUp = currentPage && plugin.canLevelUpCountry(country);
        boolean pageComplete = plugin.isCountryLevelPageComplete(country, pageLevel);
        Material material = reached ? getLevelBadgeMaterial(targetLevel) : canLevelUp ? Material.GOLD_INGOT : Material.WHITE_STAINED_GLASS_PANE;
        List<String> lore = new ArrayList<>();
        lore.add("&7Current country level: &f" + currentLevel);
        lore.add("&7Next level target: &f" + targetLevel);
        lore.add("");
        if (reached) {
            lore.add("&aThis country already reached level " + targetLevel + ".");
        } else {
            lore.add("&7Finish this page to level up to &f" + targetLevel + "&7.");
            lore.add("&7Page complete: " + (pageComplete ? "&aYes" : "&cNo"));
            lore.add("&7Score: &f" + plugin.getCountryProgressScore(country) + "&7/&f" + plugin.getCountryTargetScoreForLevel(targetLevel));
            lore.add("&7Treasury cost: &f⛃" + plugin.formatMoney(plugin.getCountryLevelUpBalanceCost(targetLevel)));
            lore.add("&7Material cost: &f" + plugin.getCountryLevelUpResourceCost(targetLevel));
            if (currentPage) {
                lore.add("");
                lore.add(canLevelUp ? "&eClick to level up the country." : "&7Complete the requirements first.");
            }
        }
        return createSimpleItem(material, reached ? "&eLevel " + targetLevel + " Reached" : "&fLevel " + targetLevel + " Locked", lore);
    }

    private List<String> getCountryLevelRewardLines(Country country, int pageLevel) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Rewards for country level &f" + pageLevel);
        lore.add("&7Unlocked page nodes: &f" + getUpgradesForCountryLevel(pageLevel).size());
        lore.add("");
        if (pageLevel == 2) {
            lore.add("&6Special: fixed ores disappear in your country.");
        }
        lore.add("&7Level perks:");
        lore.addAll(getCountryLevelRewardSummary(pageLevel));
        lore.add("");
        lore.add("&7Current country level: &f" + plugin.getCountryLevel(country));
        return lore;
    }

    private List<String> getCountryLevelRewardSummary(int pageLevel) {
        List<String> rewards = new ArrayList<>();
        List<CountryUpgrade> upgrades = getUpgradesForCountryLevel(pageLevel);
        if (upgrades.isEmpty()) {
            rewards.add("&7No milestone upgrades on this page.");
            return rewards;
        }
        for (CountryUpgrade upgrade : upgrades) {
            rewards.add("&f- " + upgrade.getDisplayName());
        }
        return rewards;
    }

    private Material getLevelBadgeMaterial(int level) {
        return switch (level) {
            case 2 -> Material.GOLD_INGOT;
            case 3 -> Material.GOLD_BLOCK;
            case 4 -> Material.RAW_GOLD_BLOCK;
            case 5 -> Material.NETHER_STAR;
            default -> Material.GOLD_NUGGET;
        };
    }

    private void renderProgressionTrack(Inventory inventory, Player player, Country country, int pageLevel) {
        List<ProgressNode> nodes = getProgressNodesForCountryLevel(pageLevel);
        int currentIndex = getCurrentTrackNodeIndex(country, nodes);

        for (int i = 0; i < PROGRESSION_TRACK_SLOTS.length; i++) {
            int slot = PROGRESSION_TRACK_SLOTS[i];
            if (i < nodes.size()) {
                inventory.setItem(slot, createCountryProgressNodeItem(player, country, nodes.get(i), i == currentIndex));
            } else {
                inventory.setItem(slot, createSimpleItem(
                        Material.GRAY_STAINED_GLASS_PANE,
                        "&7Upgrade Track",
                        List.of("&7Each track slot is part of the progression path.")
                ));
            }
        }
    }

    private ItemStack createCountryProgressNodeItem(Player player, Country country, ProgressNode node, boolean current) {
        boolean unlocked = plugin.isCountryProgressKeyUnlocked(country, node.key());
        boolean canUnlock = plugin.canUnlockCountryProgress(country, player.getUniqueId())
                && plugin.canUnlockCountryProgressKey(
                country,
                node.key(),
                node.prerequisiteKey(),
                node.requiredCountryLevel(),
                node.balanceCost(),
                node.resourceCost(),
                node.professionRequirements()
        );
        boolean hasAccess = plugin.canUnlockCountryProgress(country, player.getUniqueId());
        boolean prerequisiteUnlocked = node.prerequisiteKey() == null || plugin.isCountryProgressKeyUnlocked(country, node.prerequisiteKey());
        Material material;
        if (node.milestone()) {
            material = node.icon();
        } else {
            material = unlocked ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.displayName(plugin.legacyComponent((unlocked ? "&a" : current ? "&e" : "&c") + node.displayName()));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(plugin.legacyComponent("&7" + node.description()));
        lore.add(plugin.legacyComponent("&7Country level: &f" + node.requiredCountryLevel()));
        lore.add(plugin.legacyComponent("&7Treasury cost: &f⛃" + plugin.formatMoney(node.balanceCost())));
        if (node.prerequisiteName() != null) {
            lore.add(plugin.legacyComponent((prerequisiteUnlocked ? "&a" : "&c") + "Previous node: " + node.prerequisiteName()));
        }
        if (!node.professionRequirements().isEmpty()) {
            lore.add(plugin.legacyComponent("&7Job requirements:"));
            for (var entry : node.professionRequirements().entrySet()) {
                boolean met = plugin.hasCountryMemberAtProfessionLevel(country, entry.getKey(), entry.getValue());
                lore.add(plugin.legacyComponent((met ? "&a" : "&c") + plugin.getProfessionPlainDisplayName(entry.getKey()) + " level " + entry.getValue()));
            }
        }
        lore.add(plugin.legacyComponent(""));
        if (unlocked) {
            lore.add(plugin.legacyComponent("&aUnlocked and completed"));
        } else if (current && canUnlock) {
            lore.add(plugin.legacyComponent("&eCurrent upgrade stage"));
            lore.add(plugin.legacyComponent("&eClick to confirm this unlock"));
        } else if (current) {
            lore.add(plugin.legacyComponent("&eCurrent upgrade stage"));
            lore.add(plugin.legacyComponent(hasAccess ? "&cRequirements not met yet" : "&7View only"));
        } else {
            lore.add(plugin.legacyComponent("&cLocked future milestone"));
            lore.add(plugin.legacyComponent("&7Unlock the earlier milestones first."));
        }
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if (current && !unlocked) {
            meta.setEnchantmentGlintOverride(true);
        }
        item.setItemMeta(meta);
        return item;
    }

    private List<CountryUpgrade> getUpgradesForCountryLevel(int pageLevel) {
        List<CountryUpgrade> upgrades = new ArrayList<>();
        for (CountryUpgrade upgrade : CountryUpgrade.values()) {
            if (upgrade.getRequiredCountryLevel() == pageLevel) {
                upgrades.add(upgrade);
            }
        }
        return upgrades;
    }

    private int getTotalProgressNodeCount() {
        return plugin.getCountryMaxLevel() * PROGRESSION_TRACK_SLOTS.length;
    }

    private int getCurrentTrackNodeIndex(Country country, List<ProgressNode> nodes) {
        if (nodes.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < nodes.size(); i++) {
            if (!plugin.isCountryProgressKeyUnlocked(country, nodes.get(i).key())) {
                return i;
            }
        }
        return -1;
    }

    private List<ProgressNode> getProgressNodesForCountryLevel(int pageLevel) {
        List<ProgressNode> nodes = new ArrayList<>();
        List<CountryUpgrade> upgrades = getUpgradesForCountryLevel(pageLevel);
        String previousKey = null;
        String previousName = null;
        int bufferIndex = 1;

        nodes.add(createBufferNode(pageLevel, bufferIndex++, previousKey, previousName));
        previousKey = nodes.get(nodes.size() - 1).key();
        previousName = nodes.get(nodes.size() - 1).displayName();

        for (CountryUpgrade upgrade : upgrades) {
            ProgressNode milestone = new ProgressNode(
                    upgrade.getKey(),
                    upgrade.getDisplayName(),
                    upgrade.getIcon(),
                    upgrade.getRequiredCountryLevel(),
                    upgrade.getBalanceCost(),
                    upgrade.getResourceCost(),
                    upgrade.getProfessionRequirements(),
                    previousKey,
                    previousName,
                    upgrade.getEffectDescription(),
                    true
            );
            nodes.add(milestone);
            previousKey = milestone.key();
            previousName = milestone.displayName();

            if (nodes.size() < PROGRESSION_TRACK_SLOTS.length) {
                ProgressNode buffer = createBufferNode(pageLevel, bufferIndex++, previousKey, previousName);
                nodes.add(buffer);
                previousKey = buffer.key();
                previousName = buffer.displayName();
            }
        }

        while (nodes.size() < PROGRESSION_TRACK_SLOTS.length) {
            ProgressNode buffer = createBufferNode(pageLevel, bufferIndex++, previousKey, previousName);
            nodes.add(buffer);
            previousKey = buffer.key();
            previousName = buffer.displayName();
        }

        return nodes;
    }

    private ProgressNode createBufferNode(int pageLevel, int bufferIndex, String prerequisiteKey, String prerequisiteName) {
        return new ProgressNode(
                "path_level_" + pageLevel + "_" + bufferIndex,
                "Path Step " + bufferIndex,
                Material.GRAY_STAINED_GLASS_PANE,
                pageLevel,
                4.0D + (pageLevel * 2.0D) + bufferIndex,
                4 + (pageLevel * 2) + bufferIndex,
                Map.of(),
                prerequisiteKey,
                prerequisiteName,
                "A country progress step that advances the path toward the next milestone.",
                false
        );
    }

    private ItemStack createCountryProgressEconomyItem(Country country) {
        return createSimpleItem(Material.GOLD_BLOCK, "&6Treasury", List.of(
                "&7Treasury: &f⛃" + plugin.formatMoney(plugin.getCountryBalance(country)),
                "&7Active boost: &f" + plugin.getCountryActiveBoostDisplay(country),
                "&7Boost time left: &f" + plugin.formatDurationWords(plugin.getCountryActiveBoostRemainingMillis(country))
        ));
    }

    private ItemStack createCountryProgressAccessItem(Player player, Country country) {
        boolean canUnlock = plugin.canUnlockCountryProgress(country, player.getUniqueId());
        return createSimpleItem(canUnlock ? Material.LIME_DYE : Material.GRAY_DYE,
                canUnlock ? "&aUnlock Access" : "&7View Access",
                List.of(
                        canUnlock
                                ? "&7You can unlock the next available node."
                                : "&7You can inspect the full tree, but not unlock nodes.",
                        "&7Required rank: &fCo-Owner or Owner",
                        "",
                        "&7Path rule: unlock each node in order.",
                        "&7Lime panes are unlocked. Red panes are still locked."
                ));
    }

    private ItemStack createCountryUpgradeItem(Player player, Country country, CountryUpgrade upgrade) {
        boolean unlocked = plugin.isCountryUpgradeUnlocked(country, upgrade);
        boolean canUnlock = plugin.canUnlockCountryProgress(country, player.getUniqueId()) && plugin.canUnlockCountryUpgrade(country, upgrade);
        boolean hasAccess = plugin.canUnlockCountryProgress(country, player.getUniqueId());
        CountryUpgrade prerequisite = upgrade.getPrerequisite();
        boolean prerequisiteUnlocked = prerequisite == null || plugin.isCountryUpgradeUnlocked(country, prerequisite);
        List<String> lore = new ArrayList<>();
        lore.add("&7Theme: &f" + plugin.formatMaterialName(upgrade.getIcon()));
        lore.add("&7Effect: &f" + upgrade.getEffectDescription());
        lore.add("");
        lore.add("&7Country level: &f" + upgrade.getRequiredCountryLevel());
        lore.add("&7Treasury cost: &f⛃" + plugin.formatMoney(upgrade.getBalanceCost()));
        if (prerequisite != null) {
            lore.add((prerequisiteUnlocked ? "&a" : "&c") + "Previous node: " + prerequisite.getDisplayName());
        }
        if (!upgrade.getProfessionRequirements().isEmpty()) {
            lore.add("");
            lore.add("&7Job requirements:");
            for (var entry : upgrade.getProfessionRequirements().entrySet()) {
                boolean met = plugin.hasCountryMemberAtProfessionLevel(country, entry.getKey(), entry.getValue());
                lore.add((met ? "&a" : "&c") + plugin.getProfessionPlainDisplayName(entry.getKey()) + " level " + entry.getValue());
            }
        }
        lore.add("");
        if (unlocked) {
            lore.add("&aUnlocked");
        } else if (canUnlock) {
            lore.add("&eReady to unlock");
            lore.add("&eClick to unlock this node");
        } else if (hasAccess) {
            lore.add("&cRequirements not met yet");
        } else {
            lore.add("&7View only: co-owners and owners unlock nodes");
        }
        Material material = unlocked ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        String color = unlocked ? "&a" : canUnlock ? "&e" : prerequisiteUnlocked ? "&c" : "&4";
        return createSimpleItem(material, color + upgrade.getDisplayName(), lore);
    }

    private void openCountryListMenu(Player player, CountryListFilter filter, CountryListSort sort, int page) {
        List<Country> countries = getFilteredCountries(filter, sort);
        int totalPages = Math.max(1, (int) Math.ceil(countries.size() / (double) LIST_COUNTRY_SLOTS.length));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inventory = Bukkit.createInventory(
                new CountryListMenuHolder(player.getUniqueId(), filter, sort, safePage),
                GUI_SIZE,
                plugin.legacyComponent("&8Country List")
        );

        fillEmptySlots(inventory);
        inventory.setItem(LIST_INFO_SLOT, createCountryListInfoItem(filter, sort, countries.size(), safePage, totalPages));
        inventory.setItem(LIST_FILTER_SLOT, createCountryListFilterItem(filter));
        inventory.setItem(LIST_SORT_SLOT, createCountryListSortItem(sort));
        inventory.setItem(LIST_BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the country dashboard.")));
        inventory.setItem(LIST_PREVIOUS_SLOT, createSimpleItem(Material.SPECTRAL_ARROW, "&ePrevious Page", List.of(
                "&7Go to the previous page.",
                "&7Current: &f" + (safePage + 1) + "&7/&f" + totalPages
        )));
        inventory.setItem(LIST_NEXT_SLOT, createSimpleItem(Material.ARROW, "&eNext Page", List.of(
                "&7Go to the next page.",
                "&7Current: &f" + (safePage + 1) + "&7/&f" + totalPages
        )));

        if (countries.isEmpty()) {
            inventory.setItem(31, createSimpleItem(Material.BARRIER, "&cNo Countries Found", List.of(
                    "&7No countries match the current filter.",
                    "&7Try another filter or sorting mode."
            )));
        } else {
            int startIndex = safePage * LIST_COUNTRY_SLOTS.length;
            for (int i = 0; i < LIST_COUNTRY_SLOTS.length && startIndex + i < countries.size(); i++) {
                inventory.setItem(LIST_COUNTRY_SLOTS[i], createCountryListEntryItem(countries.get(startIndex + i)));
            }
        }

        player.openInventory(inventory);
    }

    private void handleCountryListClick(Player player, CountryListMenuHolder holder, int slot) {
        switch (slot) {
            case LIST_FILTER_SLOT -> openCountryListMenu(player, holder.filter().next(), holder.sort(), 0);
            case LIST_SORT_SLOT -> openCountryListMenu(player, holder.filter(), holder.sort().next(), 0);
            case LIST_PREVIOUS_SLOT -> openCountryListMenu(player, holder.filter(), holder.sort(), holder.page() - 1);
            case LIST_NEXT_SLOT -> openCountryListMenu(player, holder.filter(), holder.sort(), holder.page() + 1);
            case LIST_BACK_SLOT -> openCountryMenu(player);
            default -> {
                Country clicked = getCountryByListSlot(holder, slot);
                if (clicked != null) {
                    player.closeInventory();
                    player.performCommand("country info " + clicked.getName());
                }
            }
        }
    }

    private void handleCountryProgressionClick(Player player, CountryProgressionMenuHolder holder, int slot) {
        if (slot == PROGRESSION_BACK_SLOT) {
            if (holder.page() <= 0) {
                openCountryMenu(player);
            } else {
                openCountryProgressionMenu(player, holder.page() - 1);
            }
            return;
        }
        if (slot == PROGRESSION_TREASURY_SLOT) {
            openCountryDepositMenu(player);
            return;
        }
        if (slot == PROGRESSION_NEXT_SLOT) {
            openCountryProgressionMenu(player, holder.page() + 1);
            return;
        }

        Country country = plugin.getCountry(holder.countryName());
        if (country == null) {
            player.closeInventory();
            return;
        }

        if (slot == PROGRESSION_PAGE_BADGE_SLOT) {
            int pageLevel = holder.page() + 1;
            if (pageLevel == plugin.getCountryLevel(country) && plugin.levelUpCountry(country)) {
                player.sendMessage(plugin.getMessage("country.progress.unlocked", plugin.placeholders(
                        "country", country.getName(),
                        "upgrade", "Country Level " + plugin.getCountryLevel(country)
                )));
                openCountryProgressionMenu(player, holder.page());
            } else if (pageLevel == plugin.getCountryLevel(country)) {
                player.sendMessage(plugin.getMessage("country.progress.requirements-not-met"));
            }
            return;
        }

        ProgressNode node = getProgressNodeBySlot(holder.page(), slot);
        if (node == null) {
            return;
        }

        if (plugin.isCountryProgressKeyUnlocked(country, node.key())) {
            return;
        }
        if (!plugin.canUnlockCountryProgress(country, player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.progress.view-only"));
            return;
        }
        if (!plugin.canUnlockCountryProgressKey(
                country,
                node.key(),
                node.prerequisiteKey(),
                node.requiredCountryLevel(),
                node.balanceCost(),
                node.resourceCost(),
                node.professionRequirements()
        )) {
            player.sendMessage(plugin.getMessage("country.progress.requirements-not-met"));
            return;
        }
        openUpgradeConfirmMenu(player, country, holder.page(), node);
    }

    private void handleCountryDepositClick(Player player, CountryDepositMenuHolder holder, int slot) {
        if (slot == DEPOSIT_BACK_SLOT) {
            openCountryMenu(player);
            return;
        }

        Country country = plugin.getCountry(holder.countryName());
        if (country == null) {
            player.closeInventory();
            return;
        }

        switch (slot) {
            case DEPOSIT_ADD_HALF_SLOT -> adjustSelectedDepositAmount(player, 0.50D);
            case DEPOSIT_ADD_ONE_SLOT -> adjustSelectedDepositAmount(player, 1.0D);
            case DEPOSIT_ADD_TEN_SLOT -> adjustSelectedDepositAmount(player, 10.0D);
            case DEPOSIT_ADD_HUNDRED_SLOT -> adjustSelectedDepositAmount(player, 100.0D);
            case DEPOSIT_RESET_SLOT -> selectedDepositAmounts.put(player.getUniqueId(), 0.0D);
            case DEPOSIT_ALL_SLOT -> selectedDepositAmounts.put(player.getUniqueId(), plugin.getBalance(player.getUniqueId()));
            case DEPOSIT_CUSTOM_SLOT -> {
                awaitingDepositChatInput.add(player.getUniqueId());
                player.closeInventory();
                player.sendMessage(plugin.legacyComponent("&eType the exact amount to deposit in chat. Type &fcancel &eto stop."));
                return;
            }
            case DEPOSIT_CONFIRM_SLOT -> {
                double amount = selectedDepositAmounts.getOrDefault(player.getUniqueId(), 0.0D);
                if (amount <= 0.0D) {
                    player.sendMessage(plugin.legacyComponent("&cSelect an amount above ⛃0.00 first."));
                    return;
                }
                player.closeInventory();
                player.performCommand("country deposit " + formatAmountForCommand(amount));
                return;
            }
            default -> {
                return;
            }
        }
        openCountryDepositMenu(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!awaitingDepositChatInput.remove(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        String input = plugin.plainText(event.originalMessage()).trim();
        if (input.equalsIgnoreCase("cancel")) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(plugin.legacyComponent("&7Treasury deposit entry cancelled."));
                openCountryDepositMenu(player);
            });
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(input.replace(',', '.'));
        } catch (NumberFormatException exception) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(plugin.legacyComponent("&cThat is not a valid amount."));
                openCountryDepositMenu(player);
            });
            return;
        }

        double sanitized = Math.max(0.0D, Math.round(amount * 100.0D) / 100.0D);
        Bukkit.getScheduler().runTask(plugin, () -> {
            selectedDepositAmounts.put(player.getUniqueId(), sanitized);
            player.sendMessage(plugin.legacyComponent("&aSelected treasury deposit amount: &f⛃" + plugin.formatMoney(sanitized)));
            openCountryDepositMenu(player);
        });
    }

    private void adjustSelectedDepositAmount(Player player, double delta) {
        UUID playerId = player.getUniqueId();
        double current = selectedDepositAmounts.getOrDefault(playerId, 0.0D);
        double next = Math.max(0.0D, Math.round((current + delta) * 100.0D) / 100.0D);
        selectedDepositAmounts.put(playerId, next);
    }

    private String formatAmountForCommand(double amount) {
        return String.format(Locale.US, "%.2f", Math.max(0.0D, amount));
    }

    private ProgressNode getProgressNodeBySlot(int page, int slot) {
        List<ProgressNode> nodes = getProgressNodesForCountryLevel(page + 1);
        for (int i = 0; i < PROGRESSION_TRACK_SLOTS.length && i < nodes.size(); i++) {
            if (PROGRESSION_TRACK_SLOTS[i] == slot) {
                return nodes.get(i);
            }
        }
        return null;
    }

    private ProgressNode getProgressNodeByKey(int page, String key) {
        if (key == null) {
            return null;
        }
        for (ProgressNode node : getProgressNodesForCountryLevel(page + 1)) {
            if (node.key().equalsIgnoreCase(key)) {
                return node;
            }
        }
        return null;
    }

    private Country getCountryByListSlot(CountryListMenuHolder holder, int slot) {
        List<Country> countries = getFilteredCountries(holder.filter(), holder.sort());
        int startIndex = holder.page() * LIST_COUNTRY_SLOTS.length;
        for (int i = 0; i < LIST_COUNTRY_SLOTS.length; i++) {
            if (LIST_COUNTRY_SLOTS[i] != slot) {
                continue;
            }
            int index = startIndex + i;
            return index >= 0 && index < countries.size() ? countries.get(index) : null;
        }
        return null;
    }

    private List<Country> getFilteredCountries(CountryListFilter filter, CountryListSort sort) {
        List<Country> countries = new ArrayList<>(plugin.getVisibleCountries());
        countries.removeIf(country -> !filter.matches(country));
        countries.sort(sort.comparator(plugin));
        return countries;
    }

    private ItemStack createCountryListInfoItem(CountryListFilter filter, CountryListSort sort, int total, int page, int totalPages) {
        return createSimpleItem(Material.MAP, "&eCountry Browser", List.of(
                "&7Filter: &f" + filter.getDisplayName(),
                "&7Sort: &f" + sort.getDisplayName(),
                "&7Results: &f" + total,
                "&7Page: &f" + (page + 1) + "&7/&f" + totalPages,
                "",
                "&7Click a country to inspect it."
        ));
    }

    private ItemStack createCountryListFilterItem(CountryListFilter filter) {
        return createSimpleItem(Material.HOPPER, "&bFilter Countries", List.of(
                "&7Current filter: &f" + filter.getDisplayName(),
                "&7Click to cycle between all, open,",
                "&7and closed countries."
        ));
    }

    private ItemStack createCountryListSortItem(CountryListSort sort) {
        return createSimpleItem(Material.COMPARATOR, "&6Sort Countries", List.of(
                "&7Current sort: &f" + sort.getDisplayName(),
                "&7Click to cycle sorting mode."
        ));
    }

    private ItemStack createCountryListEntryItem(Country country) {
        Material statusMaterial = country.isOpen() ? Material.LIME_BANNER : Material.RED_BANNER;
        return createSimpleItem(statusMaterial, "&e" + country.getName(), List.of(
                "&7Status: &f" + (country.isOpen() ? "Open" : "Closed"),
                "&7Members: &f" + country.getMembers().size(),
                "&7Online members: &f" + plugin.getOnlineCountryMemberCount(country),
                "&7Country level: &f" + plugin.getCountryLevel(country),
                "&7Owner: &f" + (country.hasOwner() ? plugin.safeOfflineName(country.getOwnerId()) : "None"),
                "",
                "&eClick to view /country info"
        ));
    }

    private void openTransferMembersMenu(Player player, Country country) {
        List<UUID> candidates = new ArrayList<>();
        for (UUID memberId : country.getMembers()) {
            if (!memberId.equals(player.getUniqueId())) {
                candidates.add(memberId);
            }
        }

        Inventory inventory = Bukkit.createInventory(
                new CountryTransferMembersMenuHolder(player.getUniqueId(), country.getName(), candidates),
                GUI_SIZE,
                plugin.legacyComponent("&8Transfer Country")
        );

        fillEmptySlots(inventory);
        inventory.setItem(MEMBER_MENU_INFO_SLOT, createSimpleItem(Material.TOTEM_OF_UNDYING, "&6Select New Owner", List.of(
                "&7Pick a member to receive the transfer request.",
                "&7A second confirmation step comes next."
        )));

        candidates.sort(Comparator
                .comparingInt((UUID memberId) -> professionSortIndex(plugin.getProfession(memberId)))
                .thenComparing((UUID memberId) -> getHighestOwnedLevel(memberId), Comparator.reverseOrder())
                .thenComparing(plugin::safeOfflineName, String.CASE_INSENSITIVE_ORDER));

        for (int i = 0; i < MEMBER_MENU_SLOTS.length && i < candidates.size(); i++) {
            inventory.setItem(MEMBER_MENU_SLOTS[i], createMemberItem(Bukkit.getOfflinePlayer(candidates.get(i))));
        }

        inventory.setItem(MEMBER_MENU_BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the country dashboard.")));
        player.openInventory(inventory);
    }

    private UUID getTransferTargetBySlot(CountryTransferMembersMenuHolder holder, int slot) {
        for (int i = 0; i < MEMBER_MENU_SLOTS.length && i < holder.candidates().size(); i++) {
            if (MEMBER_MENU_SLOTS[i] == slot) {
                return holder.candidates().get(i);
            }
        }
        return null;
    }

    private void openConfirmMenu(Player player, ConfirmAction action, UUID targetId) {
        Inventory inventory = Bukkit.createInventory(
                new CountryConfirmMenuHolder(player.getUniqueId(), action, targetId),
                GUI_SIZE,
                plugin.legacyComponent("&8Confirm Action")
        );

        fillEmptySlots(inventory);
        inventory.setItem(CONFIRM_INFO_SLOT, createConfirmInfoItem(action, targetId));
        inventory.setItem(CONFIRM_ACCEPT_SLOT, createSimpleItem(Material.LIME_CONCRETE, "&aConfirm", List.of("&7Proceed with this action.")));
        inventory.setItem(CONFIRM_CANCEL_SLOT, createSimpleItem(Material.RED_CONCRETE, "&cCancel", List.of("&7Return to the country dashboard.")));
        player.openInventory(inventory);
    }

    private void openUpgradeConfirmMenu(Player player, Country country, int page, ProgressNode node) {
        Inventory inventory = Bukkit.createInventory(
                new CountryUpgradeConfirmMenuHolder(player.getUniqueId(), country.getName(), page, node.key()),
                GUI_SIZE,
                plugin.legacyComponent("&8Confirm Upgrade")
        );

        fillEmptySlots(inventory);
        inventory.setItem(CONFIRM_INFO_SLOT, createCountryUpgradeConfirmInfoItem(node));
        inventory.setItem(CONFIRM_ACCEPT_SLOT, createSimpleItem(Material.LIME_CONCRETE, "&aConfirm Unlock", List.of("&7Spend the treasury for this unlock.", "&7Unlock this step for the country.")));
        inventory.setItem(CONFIRM_CANCEL_SLOT, createSimpleItem(Material.RED_CONCRETE, "&cCancel", List.of("&7Return to the upgrade menu.")));
        player.openInventory(inventory);
    }

    private ItemStack createConfirmInfoItem(ConfirmAction action, UUID targetId) {
        return switch (action) {
            case LEAVE -> createSimpleItem(Material.BARRIER, "&cLeave Country", List.of(
                    "&7This is a protected action.",
                    "&7You must confirm before leaving your country."
            ));
            case ACCEPT_TRANSFER -> createSimpleItem(Material.EMERALD, "&aAccept Country Transfer", List.of(
                    "&7This is a protected action.",
                    "&7You must confirm before accepting ownership."
            ));
            case TRANSFER -> createSimpleItem(Material.TOTEM_OF_UNDYING, "&6Transfer Country", List.of(
                    "&7Send the transfer request to:",
                    "&f" + plugin.safeOfflineName(targetId),
                    "",
                    "&7This is a protected action.",
                    "&7The target player will still need to confirm."
            ));
        };
    }

    private ItemStack createCountryUpgradeConfirmInfoItem(ProgressNode node) {
        List<String> lore = new ArrayList<>();
        lore.add("&7" + node.description());
        lore.add("&7Country level: &f" + node.requiredCountryLevel());
        lore.add("&7Treasury cost: &f⛃" + plugin.formatMoney(node.balanceCost()));
        if (!node.professionRequirements().isEmpty()) {
            lore.add("");
            lore.add("&7Job requirements:");
            for (var entry : node.professionRequirements().entrySet()) {
                lore.add("&f" + plugin.getProfessionPlainDisplayName(entry.getKey()) + " level " + entry.getValue());
            }
        }
        lore.add("");
        lore.add("&7This unlock is permanent for the country.");
        return createSimpleItem(node.icon(), "&6Confirm " + node.displayName(), lore);
    }

    private void executeConfirmedAction(Player player, CountryConfirmMenuHolder holder) {
        switch (holder.action()) {
            case LEAVE -> executeCountryCommand(player, "country leave confirm");
            case ACCEPT_TRANSFER -> executeCountryCommand(player, "country accepttransfer confirm");
            case TRANSFER -> {
                if (holder.targetId() != null) {
                    executeCountryCommand(player, "country transfercountry " + plugin.safeOfflineName(holder.targetId()));
                }
            }
        }
    }

    private void handleUpgradeConfirmClick(Player player, CountryUpgradeConfirmMenuHolder holder, int slot) {
        if (slot == CONFIRM_CANCEL_SLOT) {
            openCountryProgressionMenu(player, holder.page());
            return;
        }
        if (slot != CONFIRM_ACCEPT_SLOT) {
            return;
        }

        Country country = plugin.getCountry(holder.countryName());
        if (country == null) {
            player.closeInventory();
            return;
        }

        ProgressNode node = getProgressNodeByKey(holder.page(), holder.nodeKey());
        if (node == null) {
            openCountryProgressionMenu(player, holder.page());
            return;
        }
        if (!plugin.canUnlockCountryProgress(country, player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.progress.view-only"));
            openCountryProgressionMenu(player, holder.page());
            return;
        }
        if (!plugin.unlockCountryProgressKey(
                country,
                node.key(),
                node.prerequisiteKey(),
                node.requiredCountryLevel(),
                node.balanceCost(),
                node.resourceCost(),
                node.professionRequirements()
        )) {
            player.sendMessage(plugin.getMessage("country.progress.requirements-not-met"));
            openCountryProgressionMenu(player, holder.page());
            return;
        }

        player.sendMessage(plugin.getMessage("country.progress.unlocked", plugin.placeholders(
                "country", country.getName(),
                "upgrade", node.displayName()
        )));
        openCountryProgressionMenu(player, holder.page());
    }

    private void openMembersMenu(Player player, Country country) {
        openMembersMenu(player, country, false);
    }

    private void openMembersMenu(Player player, Country country, boolean adminView) {
        Inventory inventory = Bukkit.createInventory(
                new CountryMembersMenuHolder(player.getUniqueId(), country.getName(), adminView),
                GUI_SIZE,
                plugin.legacyComponent("&8Country Members")
        );

        fillEmptySlots(inventory);
        inventory.setItem(MEMBER_MENU_INFO_SLOT, createSimpleItem(Material.BOOK, "&e" + country.getName(), List.of(
                "&7Members are grouped by active job lanes.",
                "&7Inside each section, higher levels come first.",
                "&7Country members: &f" + country.getMembers().size()
        )));
        inventory.setItem(MEMBER_MENU_BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the country dashboard.")));

        populateProfessionSection(inventory, country, Profession.MINER, MINER_HEADER_SLOT, MINER_MEMBER_SLOTS);
        populateProfessionSection(inventory, country, Profession.LUMBERJACK, LUMBERJACK_HEADER_SLOT, LUMBERJACK_MEMBER_SLOTS);
        populateProfessionSection(inventory, country, Profession.FARMER, FARMER_HEADER_SLOT, FARMER_MEMBER_SLOTS);
        populateProfessionSection(inventory, country, Profession.BUILDER, BUILDER_HEADER_SLOT, BUILDER_MEMBER_SLOTS);
        populateProfessionSection(inventory, country, Profession.BLACKSMITH, BLACKSMITH_HEADER_SLOT, BLACKSMITH_MEMBER_SLOTS);

        player.openInventory(inventory);
    }

    private void populateProfessionSection(Inventory inventory, Country country, Profession profession, int headerSlot, int[] memberSlots) {
        List<UUID> members = getSortedMembersForProfession(country, profession);
        int overflow = Math.max(0, members.size() - memberSlots.length);
        inventory.setItem(headerSlot, createProfessionHeaderItem(profession, members.size(), overflow));

        for (int i = 0; i < memberSlots.length && i < members.size(); i++) {
            inventory.setItem(memberSlots[i], createMemberItem(Bukkit.getOfflinePlayer(members.get(i))));
        }
    }

    private List<UUID> getSortedMembersForProfession(Country country, Profession profession) {
        List<UUID> members = new ArrayList<>();
        for (UUID memberId : country.getMembers()) {
            if (plugin.getProfession(memberId) == profession) {
                members.add(memberId);
            }
        }

        members.sort(Comparator
                .comparing((UUID memberId) -> getHighestOwnedLevel(memberId), Comparator.reverseOrder())
                .thenComparing(plugin::safeOfflineName, String.CASE_INSENSITIVE_ORDER));
        return members;
    }

    private ItemStack createProfessionHeaderItem(Profession profession, int memberCount, int overflow) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Current job group: &f" + plugin.getProfessionPlainDisplayName(profession));
        lore.add("&7Shown here: &f" + Math.min(memberCount, getProfessionCapacity(profession)));
        lore.add("&7Total in group: &f" + memberCount);
        lore.add("&7Sorted by: &fHighest level -> name");
        if (overflow > 0) {
            lore.add("");
            lore.add("&cOverflow: &f" + overflow + " more player" + (overflow == 1 ? "" : "s"));
        }
        return createSimpleItem(profession.getIcon(), "&e" + plugin.getProfessionPlainDisplayName(profession), lore);
    }

    private int getProfessionCapacity(Profession profession) {
        return switch (profession) {
            case MINER -> MINER_MEMBER_SLOTS.length;
            case LUMBERJACK -> LUMBERJACK_MEMBER_SLOTS.length;
            case FARMER -> FARMER_MEMBER_SLOTS.length;
            case BUILDER -> BUILDER_MEMBER_SLOTS.length;
            case BLACKSMITH -> BLACKSMITH_MEMBER_SLOTS.length;
            default -> 0;
        };
    }

    private ItemStack createMemberItem(org.bukkit.OfflinePlayer player) {
        UUID playerId = player.getUniqueId();
        Profession activeProfession = plugin.getProfession(playerId);
        String prefix = resolveMemberPrefix(player);
        Country country = plugin.getPlayerCountry(playerId);
        CountryRole role = plugin.getCountryRole(country, playerId);
        List<String> lore = new ArrayList<>();
        lore.add("&7Role: &f" + (role != null ? role.getDisplayName() : "Member"));
        lore.add("&7Job level: &f" + getDisplayedJobLevel(playerId, activeProfession));
        lore.add(getLastSeenLine(player));
        return createPlayerItem(
                player,
                prefix + plugin.safeOfflineName(player) + " &8• &6" + formatProfession(activeProfession),
                lore
        );
    }

    private int getDisplayedJobLevel(UUID playerId, Profession activeProfession) {
        if (activeProfession == null) {
            return 0;
        }
        return plugin.getProfessionLevel(playerId, activeProfession);
    }

    private String getLastSeenLine(org.bukkit.OfflinePlayer player) {
        if (player.isOnline()) {
            return "&7Status: &aOnline now";
        }

        long lastPlayed = player.getLastPlayed();
        if (lastPlayed <= 0L) {
            return "&7Status: &cOffline &7| &fLast seen unknown";
        }

        String formatted = LAST_SEEN_FORMATTER.format(
                Instant.ofEpochMilli(lastPlayed).atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
        return "&7Status: &cOffline &7| &fLast seen " + formatted;
    }

    private String formatPrefix(String prefix) {
        if (prefix == null) {
            return "";
        }
        if (prefix.equalsIgnoreCase("none") || prefix.equalsIgnoreCase("unknown")) {
            return "";
        }
        return prefix;
    }

    private String resolveMemberPrefix(org.bukkit.OfflinePlayer player) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return "";
        }

        String resolved = PlaceholderAPI.setPlaceholders(player, "%luckperms-prefix%");
        if (resolved == null || resolved.isBlank() || resolved.equals("%luckperms-prefix%")) {
            return "";
        }
        return resolved;
    }

    private ItemStack createPlayerItem(org.bukkit.OfflinePlayer player, String displayName, List<String> loreLines) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();
        itemMeta.setOwningPlayer(player);
        itemMeta.displayName(plugin.legacyComponent(displayName));
        itemMeta.lore(loreLines.stream().map(plugin::legacyComponent).toList());
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private ItemStack createCommandItem(Material material, String name, List<String> lore, boolean available) {
        return available ? createSimpleItem(material, name, lore) : null;
    }

    private void setCommandItemIfAvailable(Inventory inventory, int slot, ItemStack itemStack) {
        if (itemStack != null) {
            inventory.setItem(slot, itemStack);
        }
    }

    private ItemStack createSimpleItem(Material material, String displayName, List<String> loreLines) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(plugin.legacyComponent(displayName));
        itemMeta.lore(loreLines.stream().map(plugin::legacyComponent).toList());
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private void decorateProgressionInventory(Inventory inventory) {
        ItemStack accent = createSimpleItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());

        for (int slot : PROGRESSION_ACCENT_SLOTS) {
            inventory.setItem(slot, accent);
        }

        for (int slot : PROGRESSION_TRACK_SLOTS) {
            inventory.setItem(slot, null);
        }
        inventory.setItem(PROGRESSION_NAME_SLOT, null);
        inventory.setItem(PROGRESSION_PAGE_INFO_SLOT, null);
        inventory.setItem(PROGRESSION_PAGE_BADGE_SLOT, null);
        inventory.setItem(PROGRESSION_BACK_SLOT, null);
        inventory.setItem(PROGRESSION_TREASURY_SLOT, null);
        inventory.setItem(PROGRESSION_NEXT_SLOT, null);
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack filler = createSimpleItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
    }

    private boolean canUse(Player player, String permission) {
        return player.isOp() || player.hasPermission(Testproject.COUNTRY_ADMIN_PERMISSION) || player.hasPermission(permission);
    }

    private boolean canSeeOwnerControls(Player player, Country country) {
        return country != null && (player.isOp()
                || player.hasPermission(Testproject.COUNTRY_ADMIN_PERMISSION)
                || plugin.isCountryOwner(country, player.getUniqueId()));
    }

    private boolean canOpenTransferMenu(Player player, Country country) {
        return country != null
                && country.hasOwner()
                && country.getOwnerId().equals(player.getUniqueId())
                && (player.isOp()
                || player.hasPermission(Testproject.COUNTRY_ADMIN_PERMISSION)
                || player.hasPermission(Testproject.COUNTRY_TRANSFER_PERMISSION)
                || country.getOwnerId().equals(player.getUniqueId()));
    }

    private int professionSortIndex(Profession profession) {
        if (profession == null) {
            return Integer.MAX_VALUE;
        }
        List<Profession> configured = plugin.getConfiguredProfessions();
        int index = configured.indexOf(profession);
        return index >= 0 ? index : Integer.MAX_VALUE - 1;
    }

    private int getHighestOwnedLevel(UUID playerId) {
        int highest = 0;
        for (Profession profession : plugin.getOwnedProfessions(playerId)) {
            highest = Math.max(highest, plugin.getProfessionLevel(playerId, profession));
        }
        return highest;
    }

    private String formatProfession(Profession profession) {
        return profession != null ? plugin.getProfessionPlainDisplayName(profession) : "None";
    }

    private interface CountryInventoryHolder extends InventoryHolder {
    }

    private record CountryMenuHolder(UUID playerId) implements CountryInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record CountryMembersMenuHolder(UUID playerId, String countryName, boolean adminView) implements CountryInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record CountryAdminMenuHolder(UUID playerId, String countryName) implements CountryInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record CountryProgressionMenuHolder(UUID playerId, String countryName, int page) implements CountryInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record CountryDepositMenuHolder(UUID playerId, String countryName) implements CountryInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record CountryListMenuHolder(UUID playerId, CountryListFilter filter, CountryListSort sort, int page) implements CountryInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record CountryTransferMembersMenuHolder(UUID playerId, String countryName, List<UUID> candidates) implements CountryInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record CountryConfirmMenuHolder(UUID playerId, ConfirmAction action, UUID targetId) implements CountryInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record CountryUpgradeConfirmMenuHolder(UUID playerId, String countryName, int page, String nodeKey) implements CountryInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record ProgressNode(String key, String displayName, Material icon, int requiredCountryLevel,
                                double balanceCost, int resourceCost, Map<Profession, Integer> professionRequirements,
                                String prerequisiteKey, String prerequisiteName, String description, boolean milestone) {
    }

    private enum ConfirmAction {
        LEAVE,
        ACCEPT_TRANSFER,
        TRANSFER
    }

    private enum CountryListFilter {
        ANY("All Countries") {
            @Override
            boolean matches(Country country) {
                return true;
            }
        },
        OPEN("Open Countries") {
            @Override
            boolean matches(Country country) {
                return country != null && country.isOpen();
            }
        },
        CLOSED("Closed Countries") {
            @Override
            boolean matches(Country country) {
                return country != null && !country.isOpen();
            }
        };

        private final String displayName;

        CountryListFilter(String displayName) {
            this.displayName = displayName;
        }

        abstract boolean matches(Country country);

        String getDisplayName() {
            return displayName;
        }

        CountryListFilter next() {
            CountryListFilter[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private enum CountryListSort {
        ALPHABETICAL("Alphabetical") {
            @Override
            Comparator<Country> comparator(Testproject plugin) {
                return Comparator.comparing(Country::getName, String.CASE_INSENSITIVE_ORDER);
            }
        },
        MEMBER_COUNT("Member Count") {
            @Override
            Comparator<Country> comparator(Testproject plugin) {
                return Comparator
                        .comparingInt((Country country) -> country.getMembers().size()).reversed()
                        .thenComparing(Country::getName, String.CASE_INSENSITIVE_ORDER);
            }
        },
        ONLINE_PLAYERS("Online Players") {
            @Override
            Comparator<Country> comparator(Testproject plugin) {
                return Comparator
                        .comparingInt((Country country) -> plugin.getOnlineCountryMemberCount(country)).reversed()
                        .thenComparing(Country::getName, String.CASE_INSENSITIVE_ORDER);
            }
        },
        COUNTRY_LEVEL("Country Level") {
            @Override
            Comparator<Country> comparator(Testproject plugin) {
                return Comparator
                        .comparingInt((Country country) -> plugin.getCountryLevel(country)).reversed()
                        .thenComparing(Country::getName, String.CASE_INSENSITIVE_ORDER);
            }
        };

        private final String displayName;

        CountryListSort(String displayName) {
            this.displayName = displayName;
        }

        abstract Comparator<Country> comparator(Testproject plugin);

        String getDisplayName() {
            return displayName;
        }

        CountryListSort next() {
            CountryListSort[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }
}

