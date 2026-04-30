package me.meetrow.testproject;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GuildGuiListener implements Listener {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int GUI_SIZE = 54;
    private static final int INFO_SLOT = 4;
    private static final int TREASURY_SLOT = 20;
    private static final int MEMBERS_SLOT = 22;
    private static final int COUNTRIES_SLOT = 24;
    private static final int WITHDRAW_SLOT = 29;
    private static final int CLAIM_SLOT = 31;
    private static final int PERMISSIONS_SLOT = 33;
    private static final int STOCKPILE_SLOT = 35;
    private static final int INVITES_SLOT = 37;
    private static final int LEAVE_SLOT = 49;
    private static final int MANAGEMENT_SLOT = 51;
    private static final int CLOSE_SLOT = 53;
    private static final int[] MEMBER_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int[] COUNTRY_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };
    private static final int DEPOSIT_AMOUNT_SLOT = 22;
    private static final int DEPOSIT_ADD_ONE_SLOT = 20;
    private static final int DEPOSIT_ADD_TEN_SLOT = 21;
    private static final int DEPOSIT_ADD_HUNDRED_SLOT = 23;
    private static final int DEPOSIT_ALL_SLOT = 24;
    private static final int DEPOSIT_CONFIRM_SLOT = 31;
    private static final int BACK_SLOT = 45;

    private final Testproject plugin;
    private final Map<UUID, Double> selectedDepositAmounts = new ConcurrentHashMap<>();

    public GuildGuiListener(Testproject plugin) {
        this.plugin = plugin;
    }

    public void openGuildMenu(Player player) {
        Guild guild = plugin.getPlayerGuild(player.getUniqueId());
        if (guild == null) {
            openGuildBrowserMenu(player, 0);
            return;
        }

        Inventory inventory = Bukkit.createInventory(
                new GuildDashboardHolder(player.getUniqueId()),
                GUI_SIZE,
                plugin.legacyComponent("&8Guild Dashboard")
        );
        fillEmpty(inventory);

        Country currentCountry = plugin.getPlayerTerritoryCountryAt(player.getLocation());
        inventory.setItem(INFO_SLOT, createGuildInfoItem(guild, player, currentCountry));
        inventory.setItem(TREASURY_SLOT, createTreasuryItem(guild, player));
        inventory.setItem(MEMBERS_SLOT, createMembersSummaryItem(guild));
        inventory.setItem(COUNTRIES_SLOT, createCountriesSummaryItem(guild));
        inventory.setItem(WITHDRAW_SLOT, createWithdrawItem(guild, player.getUniqueId()));
        inventory.setItem(CLAIM_SLOT, createClaimItem(guild, player, currentCountry));
        inventory.setItem(PERMISSIONS_SLOT, createPermissionsItem(guild, player.getUniqueId()));
        inventory.setItem(STOCKPILE_SLOT, createStockpileItem(guild));
        inventory.setItem(INVITES_SLOT, createInvitesItem(guild, player.getUniqueId()));
        inventory.setItem(LEAVE_SLOT, createLeaveItem(guild, player.getUniqueId()));
        inventory.setItem(MANAGEMENT_SLOT, createManagementItem(guild, player.getUniqueId()));
        inventory.setItem(CLOSE_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));

        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof GuildInventoryHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        if (holder instanceof GuildDashboardHolder) {
            handleDashboardClick(player, event.getSlot());
            return;
        }
        if (holder instanceof GuildDepositHolder) {
            handleDepositClick(player, event.getSlot());
            return;
        }
        if (holder instanceof GuildMembersHolder) {
            if (event.getSlot() == BACK_SLOT) {
                openGuildMenu(player);
            } else if (event.getSlot() == CLOSE_SLOT) {
                player.closeInventory();
            }
            return;
        }
        if (holder instanceof GuildCountriesHolder countriesHolder) {
            handleCountriesClick(player, countriesHolder, event.getSlot());
            return;
        }
        if (holder instanceof GuildInvitesHolder invitesHolder) {
            handleInvitesClick(player, invitesHolder, event.getSlot(), event.getClick());
            return;
        }
        if (holder instanceof GuildBrowserHolder browserHolder) {
            handleBrowserClick(player, browserHolder, event.getSlot());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof GuildInventoryHolder) {
            event.setCancelled(true);
        }
    }

    private void handleDashboardClick(Player player, int slot) {
        Guild guild = plugin.getPlayerGuild(player.getUniqueId());
        if (guild == null) {
            openGuildBrowserMenu(player, 0);
            return;
        }

        switch (slot) {
            case TREASURY_SLOT -> openDepositMenu(player, guild);
            case MEMBERS_SLOT -> openMembersMenu(player, guild);
            case COUNTRIES_SLOT -> openCountriesMenu(player, guild);
            case WITHDRAW_SLOT -> {
                player.closeInventory();
                player.sendMessage(plugin.colorize("&7Use &f/guild withdraw <amount>&7. Your current limit is &f⛃"
                        + plugin.formatMoney(plugin.getGuildWithdrawLimit(guild, player.getUniqueId())) + "&7."));
            }
            case CLAIM_SLOT -> handleClaimCurrentCountry(player, guild);
            case STOCKPILE_SLOT -> {
                player.closeInventory();
                player.sendMessage(plugin.colorize("&7Use &f/guild stockpile deposit <amount> &7while holding a material."));
            }
            case INVITES_SLOT -> openInvitesMenu(player, guild);
            case LEAVE_SLOT -> handleLeave(player, guild);
            case MANAGEMENT_SLOT -> sendManagementHelp(player, guild);
            case CLOSE_SLOT -> player.closeInventory();
            default -> {
            }
        }
    }

    private void handleDepositClick(Player player, int slot) {
        Guild guild = plugin.getPlayerGuild(player.getUniqueId());
        if (guild == null) {
            openGuildBrowserMenu(player, 0);
            return;
        }
        switch (slot) {
            case DEPOSIT_ADD_ONE_SLOT -> changeDeposit(player.getUniqueId(), 1.0D);
            case DEPOSIT_ADD_TEN_SLOT -> changeDeposit(player.getUniqueId(), 10.0D);
            case DEPOSIT_ADD_HUNDRED_SLOT -> changeDeposit(player.getUniqueId(), 100.0D);
            case DEPOSIT_ALL_SLOT -> selectedDepositAmounts.put(player.getUniqueId(), plugin.getBalance(player.getUniqueId()));
            case DEPOSIT_CONFIRM_SLOT -> {
                double amount = selectedDepositAmounts.getOrDefault(player.getUniqueId(), 0.0D);
                String failure = plugin.depositGuildBalance(player, guild, amount);
                if (failure != null) {
                    player.sendMessage(plugin.colorize("&c" + failure));
                } else {
                    player.sendMessage(plugin.colorize("&aDeposited &f⛃" + plugin.formatMoney(amount)
                            + "&a into &f" + guild.getName() + "&a."));
                    selectedDepositAmounts.put(player.getUniqueId(), 0.0D);
                }
            }
            case BACK_SLOT -> {
                openGuildMenu(player);
                return;
            }
            case CLOSE_SLOT -> {
                player.closeInventory();
                return;
            }
            default -> {
                return;
            }
        }
        openDepositMenu(player, guild);
    }

    private void handleCountriesClick(Player player, GuildCountriesHolder holder, int slot) {
        if (slot == BACK_SLOT) {
            openGuildMenu(player);
            return;
        }
        if (slot == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }
        Guild guild = plugin.getPlayerGuild(player.getUniqueId());
        if (guild == null) {
            openGuildBrowserMenu(player, 0);
            return;
        }
        int index = slotToIndex(slot, COUNTRY_SLOTS);
        if (index < 0) {
            return;
        }
        List<Country> countries = getGuildCountries(guild);
        if (index >= countries.size()) {
            return;
        }
        Country country = countries.get(index);
        player.closeInventory();
        player.performCommand("country info " + country.getName());
    }

    private void handleBrowserClick(Player player, GuildBrowserHolder holder, int slot) {
        if (slot == BACK_SLOT || slot == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }
        int index = slotToIndex(slot, COUNTRY_SLOTS);
        if (index < 0) {
            return;
        }
        List<Guild> guilds = getSortedGuilds();
        if (index >= guilds.size()) {
            return;
        }
        Guild guild = guilds.get(index);
        if (guild.getInvitedPlayers().contains(player.getUniqueId()) || guild.isRecruitingOpen()) {
            String failure = plugin.joinGuild(player, guild.getName());
            if (failure != null) {
                player.sendMessage(plugin.colorize("&c" + failure));
            } else {
                player.sendMessage(plugin.colorize("&aJoined guild &f" + guild.getName() + "&a."));
            }
            openGuildMenu(player);
            return;
        }
        player.closeInventory();
        player.performCommand("guild info " + guild.getName());
    }

    private void handleInvitesClick(Player player, GuildInvitesHolder holder, int slot, ClickType clickType) {
        if (slot == BACK_SLOT) {
            openGuildMenu(player);
            return;
        }
        if (slot == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }
        Guild guild = plugin.getPlayerGuild(player.getUniqueId());
        if (guild == null) {
            openGuildBrowserMenu(player, 0);
            return;
        }
        if (!plugin.playerHasGuildPermission(guild, player.getUniqueId(), GuildPermission.INVITE_PLAYERS)) {
            player.sendMessage(plugin.colorize("&cYou do not have permission to manage guild invites."));
            openGuildMenu(player);
            return;
        }
        int index = slotToIndex(slot, COUNTRY_SLOTS);
        if (index < 0 || index >= holder.invitees().size()) {
            return;
        }
        UUID inviteeId = holder.invitees().get(index);
        if (clickType.isRightClick()) {
            String failure = plugin.cancelGuildInvite(guild, player.getUniqueId(), inviteeId);
            if (failure != null) {
                player.sendMessage(plugin.colorize("&c" + failure));
            } else {
                player.sendMessage(plugin.colorize("&aCancelled invite for &f" + plugin.safeOfflineName(inviteeId) + "&a."));
            }
            openInvitesMenu(player, guild);
            return;
        }
        String failure = plugin.invitePlayerToGuild(guild, player.getUniqueId(), inviteeId);
        if (failure != null) {
            player.sendMessage(plugin.colorize("&c" + failure));
        } else {
            Player target = Bukkit.getPlayer(inviteeId);
            if (target != null && target.isOnline()) {
                plugin.sendGuildInvitePrompt(target, guild, player.getName(), true);
            }
            player.sendMessage(plugin.colorize("&aRefreshed invite for &f" + plugin.safeOfflineName(inviteeId) + "&a."));
        }
        openInvitesMenu(player, guild);
    }

    private void openDepositMenu(Player player, Guild guild) {
        Inventory inventory = Bukkit.createInventory(
                new GuildDepositHolder(player.getUniqueId()),
                GUI_SIZE,
                plugin.legacyComponent("&8Guild Treasury")
        );
        fillEmpty(inventory);
        double selected = selectedDepositAmounts.getOrDefault(player.getUniqueId(), 0.0D);
        inventory.setItem(INFO_SLOT, createSimpleItem(Material.GOLD_INGOT, "&6Guild Treasury", List.of(
                "&7Guild: &f" + guild.getName(),
                "&7Your balance: &f⛃" + plugin.formatMoney(plugin.getBalance(player.getUniqueId())),
                "&7Guild balance: &f⛃" + plugin.formatSignedMoney(guild.getBalance())
        )));
        inventory.setItem(DEPOSIT_AMOUNT_SLOT, createSimpleItem(Material.PAPER, "&eSelected Amount", List.of(
                "&7Selected: &f⛃" + plugin.formatMoney(selected),
                "&7Click the buttons around this slot",
                "&7to build your deposit amount."
        )));
        inventory.setItem(DEPOSIT_ADD_ONE_SLOT, createSimpleItem(Material.SUNFLOWER, "&e+1", List.of("&7Add ⛃1.00")));
        inventory.setItem(DEPOSIT_ADD_TEN_SLOT, createSimpleItem(Material.SUNFLOWER, "&e+10", List.of("&7Add ⛃10.00")));
        inventory.setItem(DEPOSIT_ADD_HUNDRED_SLOT, createSimpleItem(Material.SUNFLOWER, "&e+100", List.of("&7Add ⛃100.00")));
        inventory.setItem(DEPOSIT_ALL_SLOT, createSimpleItem(Material.EMERALD, "&aUse Full Balance", List.of(
                "&7Set the selected amount to",
                "&7your current personal balance."
        )));
        inventory.setItem(DEPOSIT_CONFIRM_SLOT, createSimpleItem(Material.GOLD_BLOCK, "&6Confirm Deposit", List.of(
                "&7Deposit the selected amount into",
                "&7the guild treasury."
        )));
        inventory.setItem(BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the guild dashboard.")));
        inventory.setItem(CLOSE_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        player.openInventory(inventory);
    }

    private void openMembersMenu(Player player, Guild guild) {
        Inventory inventory = Bukkit.createInventory(
                new GuildMembersHolder(player.getUniqueId()),
                GUI_SIZE,
                plugin.legacyComponent("&8Guild Members")
        );
        fillEmpty(inventory);
        inventory.setItem(INFO_SLOT, createSimpleItem(Material.BOOKSHELF, "&6Guild Members", List.of(
                "&7Guild: &f" + guild.getName(),
                "&7Members: &f" + guild.getMembers().size(),
                "&7Sorted by role, then name."
        )));
        List<UUID> members = guild.getMembersSortedByRole();
        for (int i = 0; i < MEMBER_SLOTS.length && i < members.size(); i++) {
            UUID memberId = members.get(i);
            inventory.setItem(MEMBER_SLOTS[i], createMemberItem(guild, Bukkit.getOfflinePlayer(memberId)));
        }
        inventory.setItem(BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the guild dashboard.")));
        inventory.setItem(CLOSE_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        player.openInventory(inventory);
    }

    private void openCountriesMenu(Player player, Guild guild) {
        Inventory inventory = Bukkit.createInventory(
                new GuildCountriesHolder(player.getUniqueId()),
                GUI_SIZE,
                plugin.legacyComponent("&8Guild Countries")
        );
        fillEmpty(inventory);
        List<Country> countries = getGuildCountries(guild);
        inventory.setItem(INFO_SLOT, createSimpleItem(Material.MAP, "&6Claimed Countries", List.of(
                "&7Guild: &f" + guild.getName(),
                "&7Claimed: &f" + countries.size() + "&7/&f" + plugin.getGuildCountryClaimCap(guild),
                "&7Level: &f" + plugin.getGuildLevel(guild)
        )));
        for (int i = 0; i < COUNTRY_SLOTS.length && i < countries.size(); i++) {
            inventory.setItem(COUNTRY_SLOTS[i], createCountryItem(countries.get(i)));
        }
        inventory.setItem(BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the guild dashboard.")));
        inventory.setItem(CLOSE_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        player.openInventory(inventory);
    }

    private void openInvitesMenu(Player player, Guild guild) {
        List<UUID> invitees = new ArrayList<>(guild.getInvitedPlayers());
        invitees.sort(Comparator.comparing(plugin::safeOfflineName, String.CASE_INSENSITIVE_ORDER));
        Inventory inventory = Bukkit.createInventory(
                new GuildInvitesHolder(player.getUniqueId(), List.copyOf(invitees)),
                GUI_SIZE,
                plugin.legacyComponent("&8Pending Invites")
        );
        fillEmpty(inventory);
        inventory.setItem(INFO_SLOT, createSimpleItem(Material.WRITABLE_BOOK, "&6Pending Invites", List.of(
                "&7Guild: &f" + guild.getName(),
                "&7Pending: &f" + invitees.size(),
                "&7Left-click: &fresend invite",
                "&7Right-click: &fcancel invite"
        )));
        for (int i = 0; i < COUNTRY_SLOTS.length && i < invitees.size(); i++) {
            inventory.setItem(COUNTRY_SLOTS[i], createInviteItem(guild, invitees.get(i)));
        }
        inventory.setItem(BACK_SLOT, createSimpleItem(Material.ARROW, "&eBack", List.of("&7Return to the guild dashboard.")));
        inventory.setItem(CLOSE_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        player.openInventory(inventory);
    }

    private void openGuildBrowserMenu(Player player, int page) {
        List<Guild> guilds = getSortedGuilds();
        int totalPages = Math.max(1, (int) Math.ceil(guilds.size() / (double) COUNTRY_SLOTS.length));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inventory = Bukkit.createInventory(
                new GuildBrowserHolder(player.getUniqueId(), safePage),
                GUI_SIZE,
                plugin.legacyComponent("&8Guild Browser")
        );
        fillEmpty(inventory);
        inventory.setItem(INFO_SLOT, createSimpleItem(Material.BLUE_BANNER, "&6Guild Browser", List.of(
                "&7Guild creation cost: &f⛃" + plugin.formatMoney(plugin.getConfig().getDouble("guilds.create-cost", 250.0D)),
                "&7To create a guild: &f/guild create <name> <tag>",
                "&7Tag rules: &f2-5 letters",
                "&7If you have an invite, click a guild to join it."
        )));
        int start = safePage * COUNTRY_SLOTS.length;
        for (int i = 0; i < COUNTRY_SLOTS.length && start + i < guilds.size(); i++) {
            inventory.setItem(COUNTRY_SLOTS[i], createGuildBrowserItem(guilds.get(start + i), player.getUniqueId()));
        }
        inventory.setItem(BACK_SLOT, createSimpleItem(Material.ARROW, "&eClose", List.of("&7Close this menu.")));
        inventory.setItem(CLOSE_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        player.openInventory(inventory);
    }

    private void handleClaimCurrentCountry(Player player, Guild guild) {
        if (!plugin.playerHasGuildPermission(guild, player.getUniqueId(), GuildPermission.CLAIM_COUNTRIES)) {
            player.sendMessage(plugin.colorize("&cYou do not have permission to claim countries."));
            return;
        }
        Country currentCountry = plugin.getVisibleCountryAt(player.getLocation());
        if (currentCountry == null) {
            player.sendMessage(plugin.colorize("&cYou are not standing inside a claimable country."));
            return;
        }
        String failure = plugin.claimCountryForGuild(guild, currentCountry, player.getUniqueId());
        if (failure != null) {
            player.sendMessage(plugin.colorize("&c" + failure));
        } else {
            player.sendMessage(plugin.colorize("&aGuild &f" + guild.getName() + "&a claimed &f" + currentCountry.getName() + "&a."));
        }
        openGuildMenu(player);
    }

    private void handleLeave(Player player, Guild guild) {
        if (guild.getLeaderId() != null && guild.getLeaderId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.colorize("&cYou are the guild leader. Use &f/guild transferleader <player> &cor &f/guild disband&c."));
            return;
        }
        plugin.leaveGuild(player.getUniqueId());
        player.sendMessage(plugin.colorize("&aYou left &f" + guild.getName() + "&a."));
        openGuildBrowserMenu(player, 0);
    }

    private void sendManagementHelp(Player player, Guild guild) {
        player.closeInventory();
        player.sendMessage(plugin.colorize("&6Guild Management"));
        player.sendMessage(plugin.colorize("&7Leader: &f/guild transferleader <player>"));
        player.sendMessage(plugin.colorize("&7Disband: &f/guild disband"));
        player.sendMessage(plugin.colorize("&7Roles: &f/guild role <player> <member|admiral|officer>"));
        player.sendMessage(plugin.colorize("&7Permissions: &f/guild permissions <role|player> <target> <permission> <allow|deny|default>"));
        player.sendMessage(plugin.colorize("&7Current leader: &f" + plugin.safeOfflineName(guild.getLeaderId())));
    }

    private void changeDeposit(UUID playerId, double amount) {
        double updated = selectedDepositAmounts.getOrDefault(playerId, 0.0D) + amount;
        selectedDepositAmounts.put(playerId, Math.round(Math.max(0.0D, updated) * 100.0D) / 100.0D);
    }

    private ItemStack createGuildInfoItem(Guild guild, Player player, Country currentCountry) {
        GuildRole role = guild.getRole(player.getUniqueId());
        return createSimpleItem(Material.NAME_TAG, "&6" + guild.getName() + " &8[" + guild.getTag() + "]", List.of(
                "&7Role: &f" + (role != null ? role.getDisplayName() : "None"),
                "&7Leader: &f" + plugin.safeOfflineName(guild.getLeaderId()),
                "&7Guild level: &f" + plugin.getGuildLevel(guild) + " &8(score " + plugin.getGuildScore(guild) + ")",
                "&7Treasury: &f⛃" + plugin.formatSignedMoney(guild.getBalance()),
                "&7Countries: &f" + guild.getClaimedCountryKeys().size() + "&7/&f" + plugin.getGuildCountryClaimCap(guild),
                "&7Current territory: &f" + (currentCountry != null ? currentCountry.getName() : "Wilderness")
        ));
    }

    private ItemStack createTreasuryItem(Guild guild, Player player) {
        return createSimpleItem(Material.GOLD_INGOT, "&6Treasury", List.of(
                "&7Guild balance: &f⛃" + plugin.formatSignedMoney(guild.getBalance()),
                "&7Your balance: &f⛃" + plugin.formatMoney(plugin.getBalance(player.getUniqueId())),
                "",
                "&eClick to open the deposit menu."
        ));
    }

    private ItemStack createMembersSummaryItem(Guild guild) {
        int officers = 0;
        int admirals = 0;
        for (UUID memberId : guild.getMembers()) {
            GuildRole role = guild.getRole(memberId);
            if (role == GuildRole.OFFICER) {
                officers++;
            } else if (role == GuildRole.ADMIRAL) {
                admirals++;
            }
        }
        return createSimpleItem(Material.BOOKSHELF, "&eMembers", List.of(
                "&7Members: &f" + guild.getMembers().size(),
                "&7Officers: &f" + officers,
                "&7Admirals: &f" + admirals,
                "",
                "&eClick to view the member list."
        ));
    }

    private ItemStack createCountriesSummaryItem(Guild guild) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Claimed: &f" + guild.getClaimedCountryKeys().size() + "&7/&f" + plugin.getGuildCountryClaimCap(guild));
        List<String> names = plugin.getGuildClaimedCountryNames(guild);
        if (names.isEmpty()) {
            lore.add("&7No countries claimed yet.");
        } else {
            for (int i = 0; i < Math.min(4, names.size()); i++) {
                lore.add("&f" + names.get(i));
            }
            if (names.size() > 4) {
                lore.add("&7+" + (names.size() - 4) + " more");
            }
        }
        lore.add("");
        lore.add("&eClick to inspect claimed countries.");
        return createSimpleItem(Material.MAP, "&bCountries", lore);
    }

    private ItemStack createClaimItem(Guild guild, Player player, Country currentCountry) {
        if (currentCountry == null) {
            return createSimpleItem(Material.GRAY_DYE, "&7Claim Current Country", List.of(
                    "&7Stand inside a visible country to claim it",
                    "&7for your guild."
            ));
        }
        Guild owningGuild = plugin.getOwningGuild(currentCountry);
        List<String> lore = new ArrayList<>();
        lore.add("&7Country: &f" + currentCountry.getName());
        lore.add("&7Claim cost: &f⛃" + plugin.formatMoney(plugin.getGuildCountryClaimCost(currentCountry)));
        lore.add("&7Weekly upkeep: &f⛃" + plugin.formatMoney(plugin.getWeeklyCountryUpkeepCost(currentCountry)));
        lore.add("&7Current owner: &f" + (owningGuild != null ? owningGuild.getName() : "Unclaimed"));
        lore.add("");
        lore.add(plugin.playerHasGuildPermission(guild, player.getUniqueId(), GuildPermission.CLAIM_COUNTRIES)
                ? "&eClick to claim this country."
                : "&cYou do not have claim permission.");
        return createSimpleItem(Material.GRASS_BLOCK, "&aClaim Current Country", lore);
    }

    private ItemStack createPermissionsItem(Guild guild, UUID playerId) {
        GuildRole role = guild.getRole(playerId);
        List<String> lore = new ArrayList<>();
        lore.add("&7Role: &f" + (role != null ? role.getDisplayName() : "None"));
        for (GuildPermission permission : GuildPermission.values()) {
            lore.add((plugin.playerHasGuildPermission(guild, playerId, permission) ? "&a" : "&c")
                    + permission.getDisplayName());
        }
        return createSimpleItem(Material.COMPARATOR, "&6Your Permissions", lore);
    }

    private ItemStack createWithdrawItem(Guild guild, UUID playerId) {
        return createSimpleItem(Material.GOLD_NUGGET, "&6Withdraw Limit", List.of(
                "&7Your limit: &fâ›ƒ" + plugin.formatMoney(plugin.getGuildWithdrawLimit(guild, playerId)),
                "&7Guild treasury: &fâ›ƒ" + plugin.formatSignedMoney(guild.getBalance()),
                "",
                "&7Use &f/guild withdraw <amount>"
        ));
    }

    private ItemStack createStockpileItem(Guild guild) {
        int totalUnits = guild.getStockpile().values().stream().mapToInt(Integer::intValue).sum();
        return createSimpleItem(Material.CHEST, "&6Guild Stockpile", List.of(
                "&7Stored units: &f" + totalUnits,
                "&7Unique materials: &f" + guild.getStockpile().size(),
                "",
                "&7Use &f/guild stockpile",
                "&7or &f/guild stockpile deposit <amount>"
        ));
    }

    private ItemStack createInvitesItem(Guild guild, UUID playerId) {
        boolean canManage = plugin.playerHasGuildPermission(guild, playerId, GuildPermission.INVITE_PLAYERS);
        int pending = guild.getInvitedPlayers().size();
        List<String> lore = new ArrayList<>();
        lore.add("&7Pending invites: &f" + pending);
        if (pending > 0) {
            int shown = 0;
            for (UUID inviteeId : guild.getInvitedPlayers()) {
                lore.add("&f" + plugin.safeOfflineName(inviteeId));
                shown++;
                if (shown >= 3) {
                    break;
                }
            }
            if (pending > 3) {
                lore.add("&7+" + (pending - 3) + " more");
            }
        } else {
            lore.add("&7No pending invites.");
        }
        lore.add("");
        lore.add(canManage ? "&eClick to manage invites." : "&7Invite management requires invite permission.");
        return createSimpleItem(canManage ? Material.WRITABLE_BOOK : Material.BOOK, "&6Pending Invites", lore);
    }

    private ItemStack createLeaveItem(Guild guild, UUID playerId) {
        boolean leader = guild.getLeaderId() != null && guild.getLeaderId().equals(playerId);
        return createSimpleItem(leader ? Material.TNT : Material.BARRIER, leader ? "&cLeader Exit Locked" : "&cLeave Guild", List.of(
                leader
                        ? "&7Use &f/guild transferleader <player> &7or &f/guild disband"
                        : "&eClick to leave your current guild."
        ));
    }

    private ItemStack createManagementItem(Guild guild, UUID playerId) {
        GuildRole role = guild.getRole(playerId);
        boolean privileged = role == GuildRole.LEADER || role == GuildRole.OFFICER;
        return createSimpleItem(privileged ? Material.TOTEM_OF_UNDYING : Material.BOOK, "&6Management", List.of(
                "&7Role tools and command shortcuts.",
                privileged ? "&eClick to view management actions." : "&7Most management tools require officer or leader."
        ));
    }

    private ItemStack createMemberItem(Guild guild, OfflinePlayer player) {
        UUID playerId = player.getUniqueId();
        GuildRole role = guild.getRole(playerId);
        Profession activeProfession = plugin.getProfession(playerId);
        List<String> lore = new ArrayList<>();
        lore.add("&7Role: &f" + (role != null ? role.getDisplayName() : "None"));
        lore.add("&7Best job level: &f" + getBestProfessionLevel(playerId));
        lore.add("&7Active job: &f" + (activeProfession != null ? plugin.getProfessionPlainDisplayName(activeProfession) : "None"));
        lore.add("&7Last seen: &f" + getLastSeen(player));
        return createPlayerItem(player, "&e" + plugin.safeOfflineName(player), lore);
    }

    private ItemStack createCountryItem(Country country) {
        Guild owningGuild = plugin.getOwningGuild(country);
        long nextUpkeepAt = country.getNextUpkeepAtMillis();
        String upkeepText = nextUpkeepAt > 0L
                ? TIME_FORMATTER.format(Instant.ofEpochMilli(nextUpkeepAt).atZone(ZoneId.systemDefault()).toLocalDateTime())
                : "Not scheduled";
        return createSimpleItem(Material.GREEN_BANNER, "&e" + country.getName(), List.of(
                "&7Country level: &f" + plugin.getCountryLevel(country),
                "&7Guild owner: &f" + (owningGuild != null ? owningGuild.getName() : "None"),
                "&7Weekly upkeep: &f⛃" + plugin.formatMoney(plugin.getWeeklyCountryUpkeepCost(country)),
                "&7Debt: &f⛃" + plugin.formatMoney(country.getUnpaidUpkeepDebt()),
                "&7Next upkeep: &f" + upkeepText,
                "",
                "&eClick to inspect this country."
        ));
    }

    private ItemStack createGuildBrowserItem(Guild guild, UUID viewerId) {
        boolean invited = guild.getInvitedPlayers().contains(viewerId);
        return createSimpleItem(invited ? Material.LIME_BANNER : Material.BLUE_BANNER,
                (invited ? "&a" : "&e") + guild.getName() + " &8[" + guild.getTag() + "]",
                List.of(
                        "&7Leader: &f" + plugin.safeOfflineName(guild.getLeaderId()),
                        "&7Level: &f" + plugin.getGuildLevel(guild),
                        "&7Members: &f" + guild.getMembers().size(),
                        "&7Claimed countries: &f" + guild.getClaimedCountryKeys().size(),
                        invited ? "&aYou have an invite. Click to join." : "&7Click to view guild info."
                ));
    }

    private ItemStack createInviteItem(Guild guild, UUID inviteeId) {
        OfflinePlayer invitee = Bukkit.getOfflinePlayer(inviteeId);
        UUID inviterId = guild.getInviteSender(inviteeId);
        long expiry = guild.getInviteExpiry(inviteeId);
        List<String> lore = new ArrayList<>();
        lore.add("&7Invited by: &f" + (inviterId != null ? plugin.safeOfflineName(inviterId) : "Unknown"));
        lore.add("&7Expires: &f" + formatInviteRemaining(expiry));
        lore.add("&7Last seen: &f" + getLastSeen(invitee));
        lore.add("");
        lore.add("&eLeft-click to resend the invite.");
        lore.add("&cRight-click to cancel the invite.");
        return createPlayerItem(invitee, "&e" + plugin.safeOfflineName(invitee), lore);
    }

    private ItemStack createPlayerItem(OfflinePlayer player, String displayName, List<String> loreLines) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();
        itemMeta.setOwningPlayer(player);
        itemMeta.displayName(plugin.legacyComponent(displayName));
        itemMeta.lore(loreLines.stream().map(plugin::legacyComponent).toList());
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private ItemStack createSimpleItem(Material material, String displayName, List<String> loreLines) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.displayName(plugin.legacyComponent(displayName));
            itemMeta.lore(loreLines.stream().map(plugin::legacyComponent).toList());
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    private void fillEmpty(Inventory inventory) {
        ItemStack filler = createSimpleItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
    }

    private int getBestProfessionLevel(UUID playerId) {
        int highest = 0;
        for (Profession profession : plugin.getConfiguredProfessions()) {
            highest = Math.max(highest, plugin.getProfessionLevel(playerId, profession));
        }
        return highest;
    }

    private String getLastSeen(OfflinePlayer player) {
        if (player.isOnline()) {
            return "Online";
        }
        long lastPlayed = player.getLastPlayed();
        if (lastPlayed <= 0L) {
            return "Unknown";
        }
        return TIME_FORMATTER.format(Instant.ofEpochMilli(lastPlayed).atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    private String formatInviteRemaining(long expiryMillis) {
        if (expiryMillis <= 0L) {
            return "Unknown";
        }
        long remainingMillis = expiryMillis - System.currentTimeMillis();
        if (remainingMillis <= 0L) {
            return "Expired";
        }
        long totalMinutes = Math.max(1L, remainingMillis / 60_000L);
        long days = totalMinutes / (60L * 24L);
        long hours = (totalMinutes / 60L) % 24L;
        long minutes = totalMinutes % 60L;
        if (days > 0L) {
            return days + "d " + hours + "h";
        }
        if (hours > 0L) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }

    private List<Guild> getSortedGuilds() {
        List<Guild> guilds = new ArrayList<>(plugin.getGuilds());
        guilds.sort(Comparator.comparing(Guild::getName, String.CASE_INSENSITIVE_ORDER));
        return guilds;
    }

    private List<Country> getGuildCountries(Guild guild) {
        List<Country> countries = new ArrayList<>();
        for (String countryKey : guild.getClaimedCountryKeys()) {
            Country country = plugin.getCountryByKey(countryKey);
            if (country != null) {
                countries.add(country);
            }
        }
        countries.sort(Comparator.comparing(Country::getName, String.CASE_INSENSITIVE_ORDER));
        return countries;
    }

    private int slotToIndex(int slot, int[] slots) {
        for (int index = 0; index < slots.length; index++) {
            if (slots[index] == slot) {
                return index;
            }
        }
        return -1;
    }

    private interface GuildInventoryHolder extends InventoryHolder {
    }

    private record GuildDashboardHolder(UUID playerId) implements GuildInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record GuildDepositHolder(UUID playerId) implements GuildInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record GuildMembersHolder(UUID playerId) implements GuildInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record GuildCountriesHolder(UUID playerId) implements GuildInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record GuildInvitesHolder(UUID playerId, List<UUID> invitees) implements GuildInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record GuildBrowserHolder(UUID playerId, int page) implements GuildInventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
