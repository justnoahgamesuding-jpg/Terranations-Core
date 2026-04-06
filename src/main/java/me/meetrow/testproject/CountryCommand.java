package me.meetrow.testproject;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CountryCommand implements CommandExecutor, TabCompleter {
    private final Testproject plugin;

    public CountryCommand(Testproject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("country.only-players"));
            return true;
        }

        if (args.length == 0) {
            if (!plugin.canUseAnyCountryCommand(player)) {
                return noPermission(player);
            }
            plugin.openCountryMenu(player);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "create" -> {
                if (!canCreateCountry(player)) return noPermission(player);
                return handleCreate(player, args);
            }
            case "setowner" -> {
                if (!hasPermission(player, Testproject.COUNTRY_SETOWNER_PERMISSION)) return noPermission(player);
                return handleSetOwner(player, args);
            }
            case "join" -> {
                if (!hasPermission(player, Testproject.COUNTRY_JOIN_PERMISSION)) return noPermission(player);
                return handleJoin(player, args);
            }
            case "home" -> {
                if (!hasPermission(player, Testproject.COUNTRY_HOME_PERMISSION)) return noPermission(player);
                return handleHome(player);
            }
            case "sethome" -> {
                if (!hasPermission(player, Testproject.COUNTRY_SETHOME_PERMISSION)) return noPermission(player);
                return handleSetHome(player);
            }
            case "borders" -> {
                if (!plugin.canUseAnyCountryCommand(player)) return noPermission(player);
                return handleBorders(player, args);
            }
            case "invite" -> {
                if (!hasPermission(player, Testproject.COUNTRY_INVITE_PERMISSION)) return noPermission(player);
                return handleInvite(player, args);
            }
            case "chat" -> {
                if (!plugin.canUseAnyCountryCommand(player)) return noPermission(player);
                return handleChatToggle(player, args);
            }
            case "acceptinvite" -> {
                if (!hasPermission(player, Testproject.COUNTRY_ACCEPT_INVITE_PERMISSION)) return noPermission(player);
                return handleAcceptInvite(player, args);
            }
            case "disband" -> {
                if (!hasPermission(player, Testproject.COUNTRY_DISBAND_PERMISSION)) return noPermission(player);
                return handleDisband(player, args);
            }
            case "joinstatus" -> {
                if (!hasPermission(player, Testproject.COUNTRY_JOINSTATUS_PERMISSION)) return noPermission(player);
                return handleJoinStatus(player, args);
            }
            case "leave" -> {
                if (!hasPermission(player, Testproject.COUNTRY_LEAVE_PERMISSION)) return noPermission(player);
                if (args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
                    return handleLeave(player, true);
                }
                return handleLeave(player);
            }
            case "kick" -> {
                if (!hasPermission(player, Testproject.COUNTRY_KICK_PERMISSION)) return noPermission(player);
                return handleKick(player, args);
            }
            case "info" -> {
                if (!hasPermission(player, Testproject.COUNTRY_INFO_PERMISSION)) return noPermission(player);
                return handleInfo(player, args);
            }
            case "farmland" -> {
                if (!hasPermission(player, Testproject.COUNTRY_FARMLAND_PERMISSION)) return noPermission(player);
                return handleFarmland(player, args);
            }
            case "list" -> {
                if (!hasPermission(player, Testproject.COUNTRY_LIST_PERMISSION)) return noPermission(player);
                return handleList(player);
            }
            case "upgrade" -> {
                if (!plugin.canUseAnyCountryCommand(player)) return noPermission(player);
                return handleUpgrade(player);
            }
            case "role" -> {
                if (!plugin.canUseAnyCountryCommand(player)) return noPermission(player);
                return handleRole(player, args);
            }
            case "balance" -> {
                if (!plugin.canUseAnyCountryCommand(player)) return noPermission(player);
                return handleBalance(player);
            }
            case "deposit" -> {
                if (!plugin.canUseAnyCountryCommand(player)) return noPermission(player);
                return handleDeposit(player, args);
            }
            case "contribute" -> {
                if (!plugin.canUseAnyCountryCommand(player)) return noPermission(player);
                return handleContribute(player, args);
            }
            case "boost" -> {
                if (!plugin.canUseAnyCountryCommand(player)) return noPermission(player);
                return handleBoost(player, args);
            }
            case "rename" -> {
                if (!hasPermission(player, Testproject.COUNTRY_RENAME_PERMISSION)) return noPermission(player);
                return handleRename(player, args);
            }
            case "transfercountry" -> {
                if (!canTransferOwnedCountry(player)) return noPermission(player);
                return handleTransferCountry(player, args);
            }
            case "accepttransfer" -> {
                if (!hasPermission(player, Testproject.COUNTRY_ACCEPTTRANSFER_PERMISSION)) return noPermission(player);
                return handleAcceptTransfer(player, args);
            }
            case "addtag" -> {
                if (!hasPermission(player, Testproject.COUNTRY_TAG_PERMISSION)) return noPermission(player);
                return handleAddTag(player, args);
            }
            case "addtagtocountry" -> {
                if (!hasPermission(player, Testproject.COUNTRY_TAG_PERMISSION)) return noPermission(player);
                return handleAddTagToCountry(player, args);
            }
            case "territory" -> {
                if (!hasPermission(player, Testproject.COUNTRY_TERRITORY_PERMISSION)) return noPermission(player);
                return handleTerritory(player, args);
            }
            case "manage" -> {
                return handleManage(player, args);
            }
            case "trade" -> {
                return handleTrade(player, args);
            }
            case "admin" -> {
                if (!hasPermission(player, Testproject.COUNTRY_ADMIN_PERMISSION)) return noPermission(player);
                return handleAdmin(player, args);
            }
            case "settraderreputation" -> {
                if (!hasPermission(player, Testproject.COUNTRY_ADMIN_PERMISSION)) return noPermission(player);
                return handleSetTraderReputation(player, args);
            }
            default -> {
                sendUsage(player);
                return true;
            }
        }
    }

    private boolean handleUpgrade(Player player) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }
        plugin.openCountryProgressionMenu(player);
        return true;
    }

    private boolean handleBorders(Player player, String[] args) {
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("on")) {
                plugin.setCountryBorderParticlesEnabled(player.getUniqueId(), true);
                player.sendMessage(plugin.getMessage("country.borders.enabled"));
                return true;
            }
            if (args[1].equalsIgnoreCase("off")) {
                plugin.setCountryBorderParticlesEnabled(player.getUniqueId(), false);
                player.sendMessage(plugin.getMessage("country.borders.disabled"));
                return true;
            }
        }

        Country country;
        if (args.length >= 2) {
            country = plugin.getCountry(joinName(args, 1));
            if (country == null) {
                player.sendMessage(plugin.getMessage("country.not-found"));
                return true;
            }
        } else {
            country = plugin.getPlayerCountry(player.getUniqueId());
            if (country == null) {
                country = plugin.getCountryAt(player.getLocation());
            }
            if (country == null) {
                player.sendMessage(plugin.getMessage("country.not-in-country"));
                return true;
            }
        }

        if (!country.hasTerritory()) {
            player.sendMessage(plugin.getMessage("country.borders.no-territory", plugin.placeholders("country", country.getName())));
            return true;
        }
        if (!plugin.showCountryBorders(player, country)) {
            player.sendMessage(plugin.getMessage("country.borders.unavailable", plugin.placeholders("country", country.getName())));
            return true;
        }
        player.sendMessage(plugin.getMessage("country.borders.shown", plugin.placeholders(
                "country", country.getName(),
                "status", plugin.isCountryBorderParticlesEnabled(player.getUniqueId()) ? "on" : "off"
        )));
        return true;
    }

    private boolean handleBalance(Player player) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }
        player.sendMessage(plugin.getMessage("country.economy.balance", plugin.placeholders(
                "country", country.getName(),
                "balance", plugin.formatMoney(plugin.getCountryBalance(country)),
                "resources", String.valueOf(plugin.getCountryResources(country)),
                "boost", plugin.getCountryActiveBoostDisplay(country),
                "boost_time", plugin.formatDurationWords(plugin.getCountryActiveBoostRemainingMillis(country))
        )));
        return true;
    }

    private boolean handleDeposit(Player player, String[] args) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }
        if (!plugin.hasEconomy()) {
            player.sendMessage(plugin.getMessage("country.economy.unavailable"));
            return true;
        }
        if (args.length != 2) {
            player.sendMessage(plugin.getMessage("country.usage.deposit"));
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException exception) {
            player.sendMessage(plugin.getMessage("country.usage.deposit"));
            return true;
        }
        if (amount <= 0.0D) {
            player.sendMessage(plugin.getMessage("country.economy.deposit-invalid"));
            return true;
        }
        if (plugin.getBalance(player.getUniqueId()) + 0.0001D < amount) {
            player.sendMessage(plugin.getMessage("country.economy.deposit-failed", plugin.placeholders(
                    "money", plugin.formatMoney(amount)
            )));
            return true;
        }
        plugin.withdrawBalance(player.getUniqueId(), amount);
        plugin.depositCountryBalance(country, amount);
        player.sendMessage(plugin.getMessage("country.economy.deposit-success", plugin.placeholders(
                "country", country.getName(),
                "money", plugin.formatMoney(amount),
                "balance", plugin.formatMoney(plugin.getCountryBalance(country))
        )));
        return true;
    }

    private boolean handleContribute(Player player, String[] args) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }
        int amount = -1;
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                player.sendMessage(plugin.getMessage("country.usage.contribute"));
                return true;
            }
        }
        if (amount == 0) {
            player.sendMessage(plugin.getMessage("country.usage.contribute"));
            return true;
        }
        if (!plugin.contributeCountryResourcesFromHand(player, country, amount)) {
            player.sendMessage(plugin.getMessage("country.economy.contribute-failed"));
            return true;
        }
        player.sendMessage(plugin.getMessage("country.economy.contribute-success", plugin.placeholders(
                "country", country.getName(),
                "resources", String.valueOf(plugin.getCountryResources(country))
        )));
        return true;
    }

    private boolean handleBoost(Player player, String[] args) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }
        if (args.length != 2) {
            player.sendMessage(plugin.getMessage("country.usage.boost"));
            return true;
        }
        if (!isAdminBypass(player) && !plugin.canManageCountry(country, player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.economy.boost-permission"));
            return true;
        }
        if (!plugin.activateCountryBoost(country, args[1])) {
            player.sendMessage(plugin.getMessage("country.economy.boost-failed"));
            return true;
        }
        player.sendMessage(plugin.getMessage("country.economy.boost-success", plugin.placeholders(
                "country", country.getName(),
                "boost", plugin.getCountryActiveBoostDisplay(country),
                "time", plugin.formatDurationWords(plugin.getCountryActiveBoostRemainingMillis(country)),
                "balance", plugin.formatMoney(plugin.getCountryBalance(country)),
                "resources", String.valueOf(plugin.getCountryResources(country))
        )));
        return true;
    }

    private boolean handleRole(Player player, String[] args) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }
        if (!isAdminBypass(player) && !plugin.isCountryOwner(country, player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.owner-only-role"));
            return true;
        }
        if (args.length != 3) {
            player.sendMessage(plugin.getMessage("country.usage.role"));
            return true;
        }
        OfflinePlayer target = findPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getMessage("general.player-not-found"));
            return true;
        }
        if (!country.getMembers().contains(target.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.transfer-target-not-member"));
            return true;
        }
        CountryRole role = CountryRole.fromKey(args[2]);
        if (role == null || role == CountryRole.OWNER) {
            player.sendMessage(plugin.getMessage("country.role.invalid"));
            return true;
        }
        if (!plugin.setCountryRole(country, target.getUniqueId(), role)) {
            player.sendMessage(plugin.getMessage("country.role.failed"));
            return true;
        }
        player.sendMessage(plugin.getMessage("country.role.updated", plugin.placeholders(
                "player", safeName(target),
                "role", role.getDisplayName(),
                "country", country.getName()
        )));
        if (target.isOnline() && target.getPlayer() != null && !target.getUniqueId().equals(player.getUniqueId())) {
            target.getPlayer().sendMessage(plugin.getMessage("country.role.updated-target", plugin.placeholders(
                    "role", role.getDisplayName(),
                    "country", country.getName()
            )));
        }
        return true;
    }

    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessage("country.usage.create"));
            return true;
        }
        boolean adminBypass = player.isOp() || player.hasPermission(Testproject.COUNTRY_ADMIN_PERMISSION);
        OfflinePlayer owner = adminBypass ? null : player;
        String countryName = joinName(args, 1);
        if (adminBypass && args.length >= 3) {
            OfflinePlayer targetOwner = findPlayer(args[args.length - 1]);
            if (targetOwner != null) {
                owner = targetOwner;
                countryName = joinName(args, 1, args.length - 1);
            }
        }

        if (countryName.isBlank()) {
            player.sendMessage(plugin.getMessage("country.usage.create"));
            return true;
        }
        if (!isValidCountryName(countryName)) {
            player.sendMessage(plugin.getMessage("country.invalid-name"));
            return true;
        }
        if (plugin.getCountry(countryName) != null) {
            player.sendMessage(plugin.getMessage("country.already-exists"));
            return true;
        }
        if (owner != null && plugin.getPlayerCountry(owner.getUniqueId()) != null) {
            player.sendMessage(plugin.getMessage(owner.getUniqueId().equals(player.getUniqueId()) ? "country.already-in-country" : "country.create-target-in-country"));
            return true;
        }

        plugin.createCountry(countryName, owner);
        player.sendMessage(plugin.getMessage("country.created", plugin.placeholders("country", countryName, "owner", owner != null ? safeName(owner) : "none")));
        plugin.playCountryTransferAcceptedSound(player);
        return true;
    }

    private boolean handleSetOwner(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.getMessage("country.usage.setowner"));
            return true;
        }

        OfflinePlayer newOwner = findPlayer(args[args.length - 1]);
        if (newOwner == null) {
            player.sendMessage(plugin.getMessage("general.player-not-found"));
            return true;
        }

        Country country = plugin.getCountry(joinName(args, 1, args.length - 1));
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-found"));
            return true;
        }
        if (!plugin.canSetCountryOwner(country, newOwner.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.owner-target-in-other-country"));
            return true;
        }

        plugin.setCountryOwner(country, newOwner);
        plugin.clearPendingCountryTransfer(newOwner.getUniqueId());
        player.sendMessage(plugin.getMessage("country.owner-set", plugin.placeholders("country", country.getName(), "player", safeName(newOwner))));
        plugin.playCountryTransferAcceptedSound(player);
        if (newOwner.isOnline() && newOwner.getPlayer() != null && !newOwner.getUniqueId().equals(player.getUniqueId())) {
            newOwner.getPlayer().sendMessage(plugin.getMessage("country.owner-set-target", plugin.placeholders("country", country.getName())));
            plugin.playCountryTransferAcceptedSound(newOwner.getPlayer());
        }
        return true;
    }

    private boolean handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessage("country.usage.join"));
            return true;
        }
        if (plugin.getPlayerCountry(player.getUniqueId()) != null) {
            player.sendMessage(plugin.getMessage("country.already-in-country"));
            return true;
        }

        Country country = plugin.getCountry(joinName(args, 1));
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-found"));
            return true;
        }
        if (!country.isOpen()) {
            player.sendMessage(plugin.getMessage("country.closed"));
            return true;
        }

        plugin.addPlayerToCountry(country, player);
        player.sendMessage(plugin.getMessage("country.joined", plugin.placeholders("country", country.getName())));
        return true;
    }

    private boolean handleHome(Player player) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        plugin.teleportToCountryHome(player, country, true, "country.home.teleported");
        return true;
    }

    private boolean handleInvite(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(plugin.getMessage("country.usage.invite"));
            return true;
        }

        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }
        if (!isAdminBypass(player) && !plugin.canInviteToCountry(country, player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.role.invite-required"));
            return true;
        }

        OfflinePlayer target = findPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getMessage("general.player-not-found"));
            return true;
        }
        if (plugin.getPlayerCountry(target.getUniqueId()) != null) {
            player.sendMessage(plugin.getMessage("country.target-in-country"));
            return true;
        }
        if (country.getInvitedPlayers().contains(target.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.already-invited"));
            return true;
        }

        plugin.inviteToCountry(country, target);
        player.sendMessage(plugin.getMessage("country.invited", plugin.placeholders("player", safeName(target), "country", country.getName())));
        plugin.playCountryInviteSentSound(player);
        if (target.isOnline() && target.getPlayer() != null) {
            sendInviteMessage(target.getPlayer(), country);
            plugin.playCountryInviteReceivedSound(target.getPlayer());
        }
        return true;
    }

    private boolean handleAcceptInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessage("country.usage.acceptinvite"));
            return true;
        }
        if (plugin.getPlayerCountry(player.getUniqueId()) != null) {
            player.sendMessage(plugin.getMessage("country.already-in-country"));
            return true;
        }

        Country country = plugin.getCountry(joinName(args, 1));
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-found"));
            return true;
        }
        if (!country.getInvitedPlayers().contains(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.no-invite"));
            return true;
        }

        plugin.acceptCountryInvite(country, player);
        player.sendMessage(plugin.getMessage("country.joined-invite", plugin.placeholders("country", country.getName())));
        return true;
    }

    private boolean handleDisband(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessage("country.usage.disband"));
            return true;
        }

        Country country = plugin.getCountry(joinName(args, 1));
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-found"));
            return true;
        }

        boolean adminBypass = player.isOp() || player.hasPermission(Testproject.COUNTRY_ADMIN_PERMISSION);
        if (!adminBypass && (!country.hasOwner() || !country.getOwnerId().equals(player.getUniqueId()))) {
            player.sendMessage(plugin.getMessage("country.owner-only-disband"));
            return true;
        }

        plugin.disbandCountry(country);
        player.sendMessage(plugin.getMessage("country.disbanded", plugin.placeholders("country", country.getName())));
        return true;
    }

    private boolean handleJoinStatus(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(plugin.getMessage("country.usage.joinstatus"));
            return true;
        }

        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }

        if (!isAdminBypass(player) && !plugin.canManageCountry(country, player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.owner-only-joinstatus"));
            return true;
        }

        if (args[1].equalsIgnoreCase("open")) {
            plugin.setCountryOpen(country, true);
            player.sendMessage(plugin.getMessage("country.joinstatus-open", plugin.placeholders("country", country.getName())));
            return true;
        }
        if (args[1].equalsIgnoreCase("closed")) {
            plugin.setCountryOpen(country, false);
            player.sendMessage(plugin.getMessage("country.joinstatus-closed", plugin.placeholders("country", country.getName())));
            return true;
        }

        player.sendMessage(plugin.getMessage("country.use-open-or-closed"));
        return true;
    }

    private boolean handleLeave(Player player) {
        return handleLeave(player, false);
    }

    private boolean handleLeave(Player player, boolean confirmed) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }

        if (!confirmed) {
            plugin.openCountryLeaveConfirmMenu(player);
            player.sendMessage(plugin.getMessage("country.leave-confirm-required"));
            return true;
        }

        boolean ownerLeaving = country.hasOwner() && country.getOwnerId().equals(player.getUniqueId());
        plugin.removePlayerFromCountry(country, player.getUniqueId());

        if (ownerLeaving && plugin.getCountry(country.getName()) == null) {
            player.sendMessage(plugin.getMessage("country.left-disbanded"));
            return true;
        }
        if (ownerLeaving) {
            Country updatedCountry = plugin.getCountry(country.getName());
            String newOwnerName = updatedCountry != null && updatedCountry.hasOwner()
                    ? safeName(Bukkit.getOfflinePlayer(updatedCountry.getOwnerId()))
                    : "none";
            player.sendMessage(plugin.getMessage("country.left-transfer", plugin.placeholders("country", country.getName(), "owner", newOwnerName)));
            return true;
        }

        player.sendMessage(plugin.getMessage("country.left", plugin.placeholders("country", country.getName())));
        return true;
    }

    private boolean handleKick(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(plugin.getMessage("country.usage.kick"));
            return true;
        }

        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }

        if (!isAdminBypass(player) && !plugin.canManageCountry(country, player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.owner-only-kick"));
            return true;
        }

        OfflinePlayer target = findPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getMessage("general.player-not-found"));
            return true;
        }
        if (!country.getMembers().contains(target.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.target-not-in-country"));
            return true;
        }
        if (country.hasOwner() && country.getOwnerId().equals(target.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.cannot-kick-owner"));
            return true;
        }
        if (!isAdminBypass(player) && plugin.isCountryCoOwner(country, target.getUniqueId()) && !plugin.isCountryOwner(country, player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.role.cannot-kick-coowner"));
            return true;
        }

        plugin.removePlayerFromCountry(country, target.getUniqueId());
        player.sendMessage(plugin.getMessage("country.kicked", plugin.placeholders("player", safeName(target), "country", country.getName())));
        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(plugin.getMessage("country.kicked-target", plugin.placeholders("country", country.getName())));
        }
        return true;
    }

    private boolean handleInfo(Player player, String[] args) {
        Country country = args.length >= 2 ? plugin.getCountry(joinName(args, 1)) : plugin.getCountryAt(player.getLocation());
        if (country == null) {
            player.sendMessage(plugin.getMessage(args.length >= 2 ? "country.not-found" : "country.info.here-none"));
            return true;
        }

        String ownerName = country.hasOwner() ? safeName(Bukkit.getOfflinePlayer(country.getOwnerId())) : "none";
        List<String> memberNames = new ArrayList<>();
        for (UUID memberId : country.getMembers()) {
            CountryRole role = plugin.getCountryRole(country, memberId);
            String roleSuffix = role != null && role != CountryRole.MEMBER ? " &8(" + role.getDisplayName() + ")" : "";
            memberNames.add(safeName(Bukkit.getOfflinePlayer(memberId)) + roleSuffix);
        }
        Collections.sort(memberNames, String.CASE_INSENSITIVE_ORDER);
        List<String> coOwnerNames = new ArrayList<>();
        for (UUID memberId : country.getCoOwners()) {
            coOwnerNames.add(safeName(Bukkit.getOfflinePlayer(memberId)));
        }
        Collections.sort(coOwnerNames, String.CASE_INSENSITIVE_ORDER);
        List<String> stewardNames = new ArrayList<>();
        for (UUID memberId : country.getStewards()) {
            stewardNames.add(safeName(Bukkit.getOfflinePlayer(memberId)));
        }
        Collections.sort(stewardNames, String.CASE_INSENSITIVE_ORDER);

        player.sendMessage(plugin.getMessage("country.info.header", plugin.placeholders("country", country.getName())));
        player.sendMessage(plugin.getMessage("country.info.owner", plugin.placeholders("owner", ownerName)));
        player.sendMessage(plugin.getMessage("country.info.coowners", plugin.placeholders("value", coOwnerNames.isEmpty() ? "none" : String.join(", ", coOwnerNames))));
        player.sendMessage(plugin.getMessage("country.info.stewards", plugin.placeholders("value", stewardNames.isEmpty() ? "none" : String.join(", ", stewardNames))));
        player.sendMessage(plugin.getMessage("country.info.joinstatus", plugin.placeholders("status", country.isOpen() ? "open" : "closed")));
        player.sendMessage(plugin.getMessage("country.info.tag", plugin.placeholders("tag", country.hasTag() ? country.getTag() : "none")));
        player.sendMessage(plugin.getMessage("country.info.level", plugin.placeholders(
                "level", String.valueOf(plugin.getCountryLevel(country)),
                "score", String.valueOf(plugin.getCountryProgressScore(country))
        )));
        player.sendMessage(plugin.getMessage("country.info.economy", plugin.placeholders(
                "balance", plugin.formatMoney(plugin.getCountryBalance(country)),
                "resources", String.valueOf(plugin.getCountryResources(country)),
                "boost", plugin.getCountryActiveBoostDisplay(country)
        )));
        player.sendMessage(plugin.getMessage("country.info.members", plugin.placeholders(
                "count", String.valueOf(memberNames.size()),
                "members", String.join(", ", memberNames)
        )));
        player.sendMessage(plugin.getMessage("country.info.territory", plugin.placeholders("territory", plugin.describeCountryTerritory(country))));
        player.sendMessage(plugin.getMessage("country.info.trader-spawn", plugin.placeholders(
                "value", country.hasTraderSpawn() ? "set" : "not set"
        )));
        player.sendMessage(plugin.getMessage("country.info.trade-access", plugin.placeholders(
                "countries", plugin.getAllowedTradeCountryNames(country).isEmpty() ? "none" : String.join(", ", plugin.getAllowedTradeCountryNames(country))
        )));
        String lastTraderText = country.getLastTraderSeenAtMillis() > 0L
                ? (country.getLastTraderName() != null ? country.getLastTraderName() : "Unknown")
                + " / "
                + (Profession.fromKey(country.getLastTraderSpecialty()) != null ? plugin.getProfessionPlainDisplayName(Profession.fromKey(country.getLastTraderSpecialty())) : "Unknown")
                + " / "
                + plugin.formatTraderLastSeen(country.getLastTraderSeenAtMillis())
                : "never";
        player.sendMessage(plugin.getMessage("country.info.last-trader", plugin.placeholders("value", lastTraderText)));
        return true;
    }

    private boolean handleList(Player player) {
        plugin.openCountryListMenu(player);
        return true;
    }

    private boolean handleChatToggle(Player player, String[] args) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getChatMessage("messages.country.not-in-country"));
            return true;
        }

        Boolean enabled = parseToggleArgument(args.length >= 2 ? args[1] : null);
        if (args.length >= 2 && enabled == null) {
            player.sendMessage(plugin.getChatMessage("messages.country.toggle-usage"));
            return true;
        }

        boolean next = enabled != null ? enabled : !plugin.isCountryChatEnabled(player.getUniqueId());
        plugin.setCountryChatEnabled(player.getUniqueId(), next);
        player.sendMessage(plugin.getChatMessage(next ? "messages.country.enabled" : "messages.country.disabled"));
        return true;
    }

    private boolean handleFarmland(Player player, String[] args) {
        Country country = args.length >= 2 ? plugin.getCountry(joinName(args, 1)) : plugin.getCountryAt(player.getLocation());
        if (country == null) {
            player.sendMessage(plugin.getMessage(args.length >= 2 ? "country.not-found" : "country.info.here-none"));
            return true;
        }

        if (!country.hasTerritory()) {
            player.sendMessage(plugin.getMessage("country.farmland.no-territory", plugin.placeholders("country", country.getName())));
            return true;
        }

        int farmland = plugin.countCountryFarmlandBlocks(country);
        int limit = plugin.getCountryFarmlandLimit(country);
        player.sendMessage(plugin.getMessage("country.farmland.count", plugin.placeholders(
                "country", country.getName(),
                "count", String.valueOf(farmland),
                "limit", String.valueOf(limit)
        )));
        return true;
    }

    private boolean handleRename(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessage("country.usage.rename"));
            return true;
        }

        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }

        if (!isAdminBypass(player) && !plugin.canManageCountry(country, player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.owner-only-rename"));
            return true;
        }

        String newName = joinName(args, 1);
        if (!isValidCountryName(newName)) {
            player.sendMessage(plugin.getMessage("country.invalid-name"));
            return true;
        }
        if (country.getName().equalsIgnoreCase(newName)) {
            player.sendMessage(plugin.getMessage("country.rename-same"));
            return true;
        }
        if (!plugin.renameCountry(country, newName)) {
            player.sendMessage(plugin.getMessage("country.already-exists"));
            return true;
        }

        player.sendMessage(plugin.getMessage("country.renamed", plugin.placeholders("country", newName)));
        return true;
    }

    private boolean handleTransferCountry(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(plugin.getMessage("country.usage.transfercountry"));
            return true;
        }

        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }
        if (!country.hasOwner() || !country.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.owner-only-transfer"));
            return true;
        }

        OfflinePlayer target = findPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getMessage("general.player-not-found"));
            return true;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.transfer-self"));
            return true;
        }
        if (!country.getMembers().contains(target.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.transfer-target-not-member"));
            return true;
        }

        plugin.setPendingCountryTransfer(country, player.getUniqueId(), target.getUniqueId());
        player.sendMessage(plugin.getMessage("country.transfer-sent", plugin.placeholders("country", country.getName(), "player", safeName(target))));
        plugin.playCountryTransferRequestSound(player);
        if (target.isOnline() && target.getPlayer() != null) {
            sendTransferMessage(target.getPlayer(), player, country);
            plugin.playCountryTransferRequestSound(target.getPlayer());
        }
        return true;
    }

    private boolean handleAcceptTransfer(Player player, String[] args) {
        if (args.length == 1) {
            CountryTransferRequest request = plugin.getPendingCountryTransfer(player.getUniqueId());
            if (request == null) {
                player.sendMessage(plugin.getMessage("country.transfer-none"));
                return true;
            }
            plugin.openCountryAcceptTransferConfirmMenu(player);
            player.sendMessage(plugin.getMessage("country.transfer-confirm-required"));
            return true;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
            return finishAcceptTransfer(player);
        }

        if (args.length != 2) {
            player.sendMessage(plugin.getMessage("country.usage.accepttransfer"));
            return true;
        }

        CountryTransferRequest request = plugin.getPendingCountryTransfer(player.getUniqueId());
        if (request == null) {
            player.sendMessage(plugin.getMessage("country.transfer-none"));
            return true;
        }

        if (request.currentOwnerId() == null) {
            plugin.clearPendingCountryTransfer(player.getUniqueId());
            player.sendMessage(plugin.getMessage("country.transfer-expired"));
            return true;
        }

        OfflinePlayer expectedOwner = Bukkit.getOfflinePlayer(request.currentOwnerId());
        if (expectedOwner.getName() == null || !expectedOwner.getName().equalsIgnoreCase(args[1])) {
            player.sendMessage(plugin.getMessage("country.transfer-none"));
            return true;
        }

        Country country = plugin.getCountry(request.countryKey());
        if (country == null || !country.hasOwner() || !country.getOwnerId().equals(request.currentOwnerId())) {
            plugin.clearPendingCountryTransfer(player.getUniqueId());
            player.sendMessage(plugin.getMessage("country.transfer-expired"));
            return true;
        }
        if (!country.getMembers().contains(player.getUniqueId())) {
            plugin.clearPendingCountryTransfer(player.getUniqueId());
            player.sendMessage(plugin.getMessage("country.transfer-expired"));
            return true;
        }

        plugin.openCountryAcceptTransferConfirmMenu(player);
        player.sendMessage(plugin.getMessage("country.transfer-confirm-required"));
        return true;
    }

    private boolean finishAcceptTransfer(Player player) {
        CountryTransferRequest request = plugin.getPendingCountryTransfer(player.getUniqueId());
        if (request == null) {
            player.sendMessage(plugin.getMessage("country.transfer-none"));
            return true;
        }

        if (request.currentOwnerId() == null) {
            plugin.clearPendingCountryTransfer(player.getUniqueId());
            player.sendMessage(plugin.getMessage("country.transfer-expired"));
            return true;
        }

        Country country = plugin.getCountry(request.countryKey());
        if (country == null || !country.hasOwner() || !country.getOwnerId().equals(request.currentOwnerId())) {
            plugin.clearPendingCountryTransfer(player.getUniqueId());
            player.sendMessage(plugin.getMessage("country.transfer-expired"));
            return true;
        }
        if (!country.getMembers().contains(player.getUniqueId())) {
            plugin.clearPendingCountryTransfer(player.getUniqueId());
            player.sendMessage(plugin.getMessage("country.transfer-expired"));
            return true;
        }

        plugin.setCountryOwner(country, player);
        plugin.clearPendingCountryTransfer(player.getUniqueId());
        player.sendMessage(plugin.getMessage("country.transfer-accepted", plugin.placeholders("country", country.getName())));
        plugin.playCountryTransferAcceptedSound(player);
        Player previousOwner = Bukkit.getPlayer(request.currentOwnerId());
        if (previousOwner != null) {
            previousOwner.sendMessage(plugin.getMessage("country.transfer-complete", plugin.placeholders("country", country.getName(), "player", safeName(player))));
            plugin.playCountryTransferAcceptedSound(previousOwner);
        }
        return true;
    }

    private boolean handleAddTag(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessage("country.usage.addtag"));
            return true;
        }

        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }

        String tag = joinName(args, 1);
        if (!isValidCountryTag(tag)) {
            player.sendMessage(plugin.getMessage("country.invalid-tag"));
            return true;
        }

        plugin.setCountryTag(country, tag);
        player.sendMessage(plugin.getMessage("country.tag-set", plugin.placeholders("country", country.getName(), "tag", tag)));
        return true;
    }

    private boolean handleAddTagToCountry(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.getMessage("country.usage.addtagtocountry"));
            return true;
        }

        String tag = args[args.length - 1];
        if (!isValidCountryTag(tag)) {
            player.sendMessage(plugin.getMessage("country.invalid-tag"));
            return true;
        }

        Country country = plugin.getCountry(joinName(args, 1, args.length - 1));
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-found"));
            return true;
        }

        plugin.setCountryTag(country, tag);
        player.sendMessage(plugin.getMessage("country.tag-set", plugin.placeholders("country", country.getName(), "tag", tag)));
        return true;
    }

    private boolean handleTerritory(Player player, String[] args) {
        if (args.length < 2) {
            sendTerritoryUsage(player);
            return true;
        }

        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "setregion" -> {
                if (args.length < 5) {
                    player.sendMessage(plugin.getMessage("country.territory.usage.setregion"));
                    return true;
                }

                String worldName = args[2];
                String regionId = args[3];
                Country country = plugin.getCountry(joinName(args, 4));
                if (country == null) {
                    player.sendMessage(plugin.getMessage("country.not-found"));
                    return true;
                }

                TerritoryOperationResult result = plugin.bindCountryTerritory(country, worldName, regionId);
                if (!result.success()) {
                    player.sendMessage(getTerritoryResultMessage(result.reason()));
                    return true;
                }

                player.sendMessage(plugin.getMessage("country.territory.bound", plugin.placeholders(
                        "country", country.getName(),
                        "world", country.getTerritoryWorld(),
                        "region", country.getTerritoryRegionId()
                )));
                return true;
            }
            case "clear" -> {
                if (args.length < 3) {
                    player.sendMessage(plugin.getMessage("country.territory.usage.clear"));
                    return true;
                }

                Country country = plugin.getCountry(joinName(args, 2));
                if (country == null) {
                    player.sendMessage(plugin.getMessage("country.not-found"));
                    return true;
                }

                TerritoryOperationResult result = plugin.clearCountryTerritory(country);
                if (!result.success()) {
                    player.sendMessage(getTerritoryResultMessage(result.reason()));
                    return true;
                }

                player.sendMessage(plugin.getMessage("country.territory.cleared", plugin.placeholders("country", country.getName())));
                return true;
            }
            case "sync" -> {
                if (args.length < 3) {
                    player.sendMessage(plugin.getMessage("country.territory.usage.sync"));
                    return true;
                }

                Country country = plugin.getCountry(joinName(args, 2));
                if (country == null) {
                    player.sendMessage(plugin.getMessage("country.not-found"));
                    return true;
                }

                TerritoryOperationResult result = plugin.syncCountryTerritory(country);
                if (!result.success()) {
                    player.sendMessage(getTerritoryResultMessage(result.reason()));
                    return true;
                }

                player.sendMessage(plugin.getMessage("country.territory.synced", plugin.placeholders(
                        "country", country.getName(),
                        "world", country.getTerritoryWorld(),
                        "region", country.getTerritoryRegionId()
                )));
                return true;
            }
            case "info" -> {
                if (args.length < 3) {
                    player.sendMessage(plugin.getMessage("country.territory.usage.info"));
                    return true;
                }

                Country country = plugin.getCountry(joinName(args, 2));
                if (country == null) {
                    player.sendMessage(plugin.getMessage("country.not-found"));
                    return true;
                }

                player.sendMessage(plugin.getMessage("country.territory.info", plugin.placeholders(
                        "country", country.getName(),
                        "territory", plugin.describeCountryTerritory(country)
                )));
                return true;
            }
            case "debug" -> {
                if (args.length < 3) {
                    player.sendMessage(plugin.getMessage("country.territory.usage.debug"));
                    return true;
                }

                Country country = plugin.getCountry(joinName(args, 2));
                if (country == null) {
                    player.sendMessage(plugin.getMessage("country.not-found"));
                    return true;
                }

                TerritoryDebugInfo debugInfo = plugin.getTerritoryDebugInfo(country);
                for (String line : debugInfo.toLines(plugin, country)) {
                    player.sendMessage(line);
                }
                return true;
            }
            default -> {
                sendTerritoryUsage(player);
                return true;
            }
        }
    }

    private boolean handleManage(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessage("country.usage.manage-sethome"));
            return true;
        }

        if (args[1].equalsIgnoreCase("sethome")) {
            if (!hasPermission(player, Testproject.COUNTRY_SETHOME_PERMISSION)) {
                return noPermission(player);
            }
            return handleSetHome(player);
        }

        if (args[1].equalsIgnoreCase("settraderspawn")) {
            if (!hasPermission(player, Testproject.COUNTRY_SETHOME_PERMISSION)) {
                return noPermission(player);
            }
            return handleSetTraderSpawn(player);
        }

        player.sendMessage(plugin.getMessage("country.usage.manage-sethome"));
        return true;
    }

    private boolean handleTrade(Player player, String[] args) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }
        if (!isAdminBypass(player) && !plugin.canManageCountry(country, player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.owner-only-sethome"));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(plugin.getMessage("country.trade.usage"));
            return true;
        }

        if (args[1].equalsIgnoreCase("list")) {
            player.sendMessage(plugin.getMessage("country.trade.list", plugin.placeholders(
                    "countries", plugin.getAllowedTradeCountryNames(country).isEmpty() ? "none" : String.join(", ", plugin.getAllowedTradeCountryNames(country))
            )));
            return true;
        }
        if (args.length < 3) {
            player.sendMessage(plugin.getMessage("country.trade.usage"));
            return true;
        }

        Country targetCountry = plugin.getCountry(joinName(args, 2));
        if (targetCountry == null) {
            player.sendMessage(plugin.getMessage("country.not-found"));
            return true;
        }

        if (args[1].equalsIgnoreCase("allow")) {
            if (!plugin.addAllowedTradeCountry(country, targetCountry)) {
                player.sendMessage(plugin.getMessage("country.trade.already-allowed", plugin.placeholders("country", targetCountry.getName())));
                return true;
            }
            player.sendMessage(plugin.getMessage("country.trade.allowed", plugin.placeholders("country", targetCountry.getName())));
            return true;
        }
        if (args[1].equalsIgnoreCase("remove")) {
            if (!plugin.removeAllowedTradeCountry(country, targetCountry)) {
                player.sendMessage(plugin.getMessage("country.trade.not-allowed", plugin.placeholders("country", targetCountry.getName())));
                return true;
            }
            player.sendMessage(plugin.getMessage("country.trade.removed", plugin.placeholders("country", targetCountry.getName())));
            return true;
        }

        player.sendMessage(plugin.getMessage("country.trade.usage"));
        return true;
    }

    private boolean handleSetHome(Player player) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }

        if (!isAdminBypass(player) && !plugin.canManageCountry(country, player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.owner-only-sethome"));
            return true;
        }

        Country territoryCountry = plugin.getCountryAt(player.getLocation());
        if (territoryCountry == null || !territoryCountry.getName().equalsIgnoreCase(country.getName())) {
            player.sendMessage(plugin.getMessage("country.home.must-be-in-territory"));
            return true;
        }

        plugin.setCountryHome(country, player.getLocation());
        player.sendMessage(plugin.getMessage("country.home.set", plugin.placeholders("country", country.getName())));
        return true;
    }

    private boolean handleSetTraderSpawn(Player player) {
        Country country = plugin.getPlayerCountry(player.getUniqueId());
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-in-country"));
            return true;
        }

        if (!isAdminBypass(player) && !plugin.canManageCountry(country, player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("country.owner-only-sethome"));
            return true;
        }

        Country territoryCountry = plugin.getCountryAt(player.getLocation());
        if (territoryCountry == null || !territoryCountry.getName().equalsIgnoreCase(country.getName())) {
            player.sendMessage(plugin.getMessage("country.home.must-be-in-territory"));
            return true;
        }

        plugin.setCountryTraderSpawn(country, player.getLocation());
        player.sendMessage(plugin.getMessage("country.trade.spawn-set", plugin.placeholders("country", country.getName())));
        return true;
    }

    private boolean handleAdmin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessage("country.admin.usage"));
            return true;
        }

        Country country = plugin.getCountry(joinName(args, 1));
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-found"));
            return true;
        }

        plugin.openCountryAdminMenu(player, country);
        return true;
    }

    private boolean handleSetTraderReputation(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.getMessage("country.admin.set-trader-reputation-usage"));
            return true;
        }

        double value;
        try {
            value = Double.parseDouble(args[args.length - 1]);
        } catch (NumberFormatException exception) {
            player.sendMessage(plugin.getMessage("country.admin.set-trader-reputation-usage"));
            return true;
        }

        Country country = plugin.getCountry(joinName(args, 1, args.length - 1));
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-found"));
            return true;
        }

        if (!plugin.setCountryTotalTraderReputation(country, value)) {
            player.sendMessage(plugin.getMessage("country.admin.set-trader-reputation-failed", plugin.placeholders(
                    "country", country.getName()
            )));
            return true;
        }

        player.sendMessage(plugin.getMessage("country.admin.set-trader-reputation-success", plugin.placeholders(
                "country", country.getName(),
                "reputation", plugin.formatTraderReputation(plugin.getCountryTotalTraderReputation(country))
        )));
        return true;
    }

    private void sendUsage(Player player) {
        for (String line : plugin.getMessageList("help.country")) {
            player.sendMessage(line);
        }
    }

    private void sendTerritoryUsage(Player player) {
        for (String line : plugin.getMessageList("help.country-territory")) {
            player.sendMessage(line);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        if (args.length > 1 && !canSuggestSubcommand(player, args[0])) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            if (canCreateCountry(player)) {
                options.add("create");
            }
            addIfPermitted(player, options, "setowner", Testproject.COUNTRY_SETOWNER_PERMISSION);
            addIfPermitted(player, options, "join", Testproject.COUNTRY_JOIN_PERMISSION);
        addIfPermitted(player, options, "home", Testproject.COUNTRY_HOME_PERMISSION);
        options.add("borders");
            addIfPermitted(player, options, "invite", Testproject.COUNTRY_INVITE_PERMISSION);
            if (plugin.canUseAnyCountryCommand(player)) {
                options.add("chat");
            }
            addIfPermitted(player, options, "acceptinvite", Testproject.COUNTRY_ACCEPT_INVITE_PERMISSION);
            addIfPermitted(player, options, "disband", Testproject.COUNTRY_DISBAND_PERMISSION);
            addIfPermitted(player, options, "joinstatus", Testproject.COUNTRY_JOINSTATUS_PERMISSION);
            addIfPermitted(player, options, "leave", Testproject.COUNTRY_LEAVE_PERMISSION);
            addIfPermitted(player, options, "kick", Testproject.COUNTRY_KICK_PERMISSION);
            addIfPermitted(player, options, "info", Testproject.COUNTRY_INFO_PERMISSION);
            addIfPermitted(player, options, "farmland", Testproject.COUNTRY_FARMLAND_PERMISSION);
            addIfPermitted(player, options, "list", Testproject.COUNTRY_LIST_PERMISSION);
            if (plugin.canUseAnyCountryCommand(player)) {
                options.add("upgrade");
                options.add("role");
                options.add("balance");
                options.add("deposit");
                options.add("contribute");
                options.add("boost");
            }
            addIfPermitted(player, options, "sethome", Testproject.COUNTRY_SETHOME_PERMISSION);
            addIfPermitted(player, options, "rename", Testproject.COUNTRY_RENAME_PERMISSION);
            if (canTransferOwnedCountry(player)) {
                options.add("transfercountry");
            }
            addIfPermitted(player, options, "accepttransfer", Testproject.COUNTRY_ACCEPTTRANSFER_PERMISSION);
            addIfPermitted(player, options, "addtag", Testproject.COUNTRY_TAG_PERMISSION);
            addIfPermitted(player, options, "addtagtocountry", Testproject.COUNTRY_TAG_PERMISSION);
            addIfPermitted(player, options, "territory", Testproject.COUNTRY_TERRITORY_PERMISSION);
            addIfPermitted(player, options, "manage", Testproject.COUNTRY_SETHOME_PERMISSION);
            addIfPermitted(player, options, "trade", Testproject.COUNTRY_SETHOME_PERMISSION);
            addIfPermitted(player, options, "admin", Testproject.COUNTRY_ADMIN_PERMISSION);
            addIfPermitted(player, options, "settraderreputation", Testproject.COUNTRY_ADMIN_PERMISSION);
            return partialMatches(args[0], options);
        }

        if ((args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("acceptinvite") || args[0].equalsIgnoreCase("disband") || args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("farmland")) && args.length >= 2) {
            return partialMatches(args[args.length - 1], plugin.getCountryNames());
        }

        if (args[0].equalsIgnoreCase("invite") && args.length == 2) {
            List<String> names = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                names.add(online.getName());
            }
            return partialMatches(args[1], names);
        }

        if (args[0].equalsIgnoreCase("setowner") && args.length >= 2) {
            if (args.length == 2) {
                return partialMatches(args[1], plugin.getCountryNames());
            }
            List<String> names = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                names.add(online.getName());
            }
            return partialMatches(args[args.length - 1], names);
        }

        if (args[0].equalsIgnoreCase("joinstatus") && args.length == 2) {
            return partialMatches(args[1], List.of("open", "closed"));
        }

        if (args[0].equalsIgnoreCase("kick") && args.length == 2) {
            Country country = plugin.getPlayerCountry(player.getUniqueId());
            if (country == null) {
                return Collections.emptyList();
            }

            List<String> memberNames = new ArrayList<>();
            for (UUID memberId : country.getMembers()) {
                if (!memberId.equals(player.getUniqueId())) {
                    memberNames.add(safeName(Bukkit.getOfflinePlayer(memberId)));
                }
            }
            return partialMatches(args[1], memberNames);
        }

        if (args[0].equalsIgnoreCase("transfercountry") && args.length == 2) {
            Country country = plugin.getPlayerCountry(player.getUniqueId());
            if (country == null) {
                return Collections.emptyList();
            }
            List<String> memberNames = new ArrayList<>();
            for (UUID memberId : country.getMembers()) {
                if (!memberId.equals(player.getUniqueId())) {
                    memberNames.add(safeName(Bukkit.getOfflinePlayer(memberId)));
                }
            }
            return partialMatches(args[1], memberNames);
        }

        if (args[0].equalsIgnoreCase("accepttransfer") && args.length == 2) {
            CountryTransferRequest request = plugin.getPendingCountryTransfer(player.getUniqueId());
            if (request == null) {
                return Collections.emptyList();
            }
            if (request.currentOwnerId() == null) {
                return Collections.emptyList();
            }
            return partialMatches(args[1], List.of("confirm", safeName(Bukkit.getOfflinePlayer(request.currentOwnerId()))));
        }

        if (args[0].equalsIgnoreCase("addtag")) {
            return Collections.emptyList();
        }

        if (args[0].equalsIgnoreCase("addtagtocountry") && args.length >= 2) {
            return partialMatches(args[args.length - 1], plugin.getCountryNames());
        }

        if (args[0].equalsIgnoreCase("role")) {
            Country country = plugin.getPlayerCountry(player.getUniqueId());
            if (country == null) {
                return Collections.emptyList();
            }
            if (args.length == 2) {
                List<String> memberNames = new ArrayList<>();
                for (UUID memberId : country.getMembers()) {
                    if (!memberId.equals(player.getUniqueId())) {
                        memberNames.add(safeName(Bukkit.getOfflinePlayer(memberId)));
                    }
                }
                return partialMatches(args[1], memberNames);
            }
            if (args.length == 3) {
                return partialMatches(args[2], List.of("member", "steward", "coowner"));
            }
        }

        if (args[0].equalsIgnoreCase("deposit") && args.length == 2) {
            return partialMatches(args[1], List.of("100", "250", "500"));
        }

        if (args[0].equalsIgnoreCase("contribute") && args.length == 2) {
            return partialMatches(args[1], List.of("16", "32", "64"));
        }

        if (args[0].equalsIgnoreCase("boost") && args.length == 2) {
            return partialMatches(args[1], List.of("all", "miner", "lumberjack", "farmer", "builder", "blacksmith"));
        }

        if (args[0].equalsIgnoreCase("sethome")) {
            return Collections.emptyList();
        }

        if (args[0].equalsIgnoreCase("leave") && args.length == 2) {
            return partialMatches(args[1], List.of("confirm"));
        }

        if (args[0].equalsIgnoreCase("borders")) {
            if (args.length >= 2) {
                List<String> suggestions = new ArrayList<>(List.of("on", "off"));
                suggestions.addAll(plugin.getCountryNames());
                return partialMatches(args[args.length - 1], suggestions);
            }
            return Collections.emptyList();
        }

        if (args[0].equalsIgnoreCase("territory")) {
            if (args.length == 2) {
                return partialMatches(args[1], List.of("setregion", "clear", "sync", "info", "debug"));
            }
            if (args[1].equalsIgnoreCase("setregion")) {
                if (args.length == 3) {
                    return partialMatches(args[2], plugin.getWorldNames());
                }
                if (args.length == 4) {
                    return partialMatches(args[3], plugin.getTerritoryRegionIds(args[2]));
                }
                if (args.length >= 5) {
                    return partialMatches(args[args.length - 1], plugin.getCountryNames());
                }
            }
            if ((args[1].equalsIgnoreCase("clear") || args[1].equalsIgnoreCase("sync") || args[1].equalsIgnoreCase("info") || args[1].equalsIgnoreCase("debug")) && args.length >= 3) {
                return partialMatches(args[args.length - 1], plugin.getCountryNames());
            }
        }

        if (args[0].equalsIgnoreCase("manage")) {
            if (args.length == 2) {
                return partialMatches(args[1], List.of("settraderspawn"));
            }
        }

        if (args[0].equalsIgnoreCase("admin") && args.length >= 2) {
            return partialMatches(args[args.length - 1], plugin.getCountryNames());
        }

        if (args[0].equalsIgnoreCase("settraderreputation")) {
            if (args.length == 2) {
                return partialMatches(args[1], plugin.getCountryNames());
            }
            if (args.length >= 3) {
                return partialMatches(args[args.length - 1], List.of("0", "5", "10", "25", "50"));
            }
        }

        if (args[0].equalsIgnoreCase("chat") && args.length == 2) {
            return partialMatches(args[1], List.of("on", "off"));
        }

        if (args[0].equalsIgnoreCase("trade")) {
            if (args.length == 2) {
                return partialMatches(args[1], List.of("allow", "remove", "list"));
            }
            if ((args[1].equalsIgnoreCase("allow") || args[1].equalsIgnoreCase("remove")) && args.length >= 3) {
                return partialMatches(args[args.length - 1], plugin.getCountryNames());
            }
        }

        return Collections.emptyList();
    }

    private List<String> partialMatches(String input, List<String> options) {
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.regionMatches(true, 0, input, 0, input.length())) {
                matches.add(option);
            }
        }
        return matches;
    }

    private OfflinePlayer findPlayer(String input) {
        Player onlinePlayer = Bukkit.getPlayerExact(input);
        if (onlinePlayer != null) {
            return onlinePlayer;
        }

        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(input)) {
                return offlinePlayer;
            }
        }
        return null;
    }

    private String safeName(OfflinePlayer player) {
        if (player == null) {
            return "none";
        }
        return player.getName() != null ? player.getName() : player.getUniqueId().toString();
    }

    private String joinName(String[] args, int startIndex) {
        return joinName(args, startIndex, args.length);
    }

    private String joinName(String[] args, int startIndex, int endExclusive) {
        StringBuilder builder = new StringBuilder();
        for (int i = startIndex; i < endExclusive; i++) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return builder.toString().trim();
    }

    private boolean isValidCountryName(String countryName) {
        return countryName.matches("[A-Za-z0-9 _-]{3,24}");
    }

    private boolean isValidCountryTag(String tag) {
        return !tag.isBlank() && tag.length() <= 32 && !tag.contains("\n") && !tag.contains("\r");
    }

    private boolean hasPermission(Player player, String permission) {
        return player.isOp() || player.hasPermission(Testproject.COUNTRY_ADMIN_PERMISSION) || player.hasPermission(permission);
    }

    private boolean canTransferOwnedCountry(Player player) {
        if (hasPermission(player, Testproject.COUNTRY_TRANSFER_PERMISSION)) {
            return true;
        }

        Country country = plugin.getPlayerCountry(player.getUniqueId());
        return country != null && country.hasOwner() && country.getOwnerId().equals(player.getUniqueId());
    }

    private void sendInviteMessage(Player player, Country country) {
        String command = "/country acceptinvite " + country.getName();
        Component base = LegacyComponentSerializer.legacyAmpersand().deserialize(
                plugin.getMessage("country.invite-received", plugin.placeholders("country", country.getName()))
        );
        Component clickableCommand = LegacyComponentSerializer.legacyAmpersand().deserialize(
                plugin.getMessage("country.invite-clickable", plugin.placeholders("command", command))
        ).clickEvent(ClickEvent.suggestCommand(command))
                .hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        plugin.getMessage("country.invite-hover", plugin.placeholders("command", command))
                )));
        player.sendMessage(base.append(Component.space()).append(clickableCommand));
    }

    private void sendTransferMessage(Player player, Player currentOwner, Country country) {
        String command = "/country accepttransfer";
        Component base = LegacyComponentSerializer.legacyAmpersand().deserialize(
                plugin.getMessage("country.transfer-received", plugin.placeholders("country", country.getName(), "player", currentOwner.getName()))
        );
        Component clickableCommand = LegacyComponentSerializer.legacyAmpersand().deserialize(
                plugin.getMessage("country.transfer-clickable", plugin.placeholders("command", command))
        ).clickEvent(ClickEvent.suggestCommand(command))
                .hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        plugin.getMessage("country.transfer-hover", plugin.placeholders("command", command))
                )));
        player.sendMessage(base.append(Component.space()).append(clickableCommand));
    }

    private boolean noPermission(Player player) {
        player.sendMessage(plugin.getMessage("permissions.country-command"));
        return true;
    }

    private void addIfPermitted(Player player, List<String> options, String command, String permission) {
        if (hasPermission(player, permission)) {
            options.add(command);
        }
    }

    private boolean canSuggestSubcommand(Player player, String subcommand) {
        return switch (subcommand.toLowerCase(Locale.ROOT)) {
            case "create" -> canCreateCountry(player);
            case "setowner" -> hasPermission(player, Testproject.COUNTRY_SETOWNER_PERMISSION);
            case "join" -> hasPermission(player, Testproject.COUNTRY_JOIN_PERMISSION);
            case "home" -> hasPermission(player, Testproject.COUNTRY_HOME_PERMISSION);
            case "sethome" -> hasPermission(player, Testproject.COUNTRY_SETHOME_PERMISSION);
            case "invite" -> hasPermission(player, Testproject.COUNTRY_INVITE_PERMISSION);
            case "chat", "upgrade", "role", "balance", "deposit", "contribute", "boost", "borders" -> plugin.canUseAnyCountryCommand(player);
            case "acceptinvite" -> hasPermission(player, Testproject.COUNTRY_ACCEPT_INVITE_PERMISSION);
            case "disband" -> hasPermission(player, Testproject.COUNTRY_DISBAND_PERMISSION);
            case "joinstatus" -> hasPermission(player, Testproject.COUNTRY_JOINSTATUS_PERMISSION);
            case "leave" -> hasPermission(player, Testproject.COUNTRY_LEAVE_PERMISSION);
            case "kick" -> hasPermission(player, Testproject.COUNTRY_KICK_PERMISSION);
            case "info" -> hasPermission(player, Testproject.COUNTRY_INFO_PERMISSION);
            case "farmland" -> hasPermission(player, Testproject.COUNTRY_FARMLAND_PERMISSION);
            case "list" -> hasPermission(player, Testproject.COUNTRY_LIST_PERMISSION);
            case "rename" -> hasPermission(player, Testproject.COUNTRY_RENAME_PERMISSION);
            case "transfercountry" -> canTransferOwnedCountry(player);
            case "accepttransfer" -> hasPermission(player, Testproject.COUNTRY_ACCEPTTRANSFER_PERMISSION);
            case "addtag", "addtagtocountry" -> hasPermission(player, Testproject.COUNTRY_TAG_PERMISSION);
            case "territory" -> hasPermission(player, Testproject.COUNTRY_TERRITORY_PERMISSION);
            case "manage", "trade" -> hasPermission(player, Testproject.COUNTRY_SETHOME_PERMISSION);
            case "admin", "settraderreputation" -> hasPermission(player, Testproject.COUNTRY_ADMIN_PERMISSION);
            default -> false;
        };
    }

    private boolean canCreateCountry(Player player) {
        return player != null && (player.isOp() || player.hasPermission(Testproject.COUNTRY_ADMIN_PERMISSION));
    }

    private boolean isAdminBypass(Player player) {
        return player != null && (player.isOp() || player.hasPermission(Testproject.COUNTRY_ADMIN_PERMISSION));
    }

    private Boolean parseToggleArgument(String input) {
        if (input == null) {
            return null;
        }
        if (input.equalsIgnoreCase("on") || input.equalsIgnoreCase("enable")) {
            return true;
        }
        if (input.equalsIgnoreCase("off") || input.equalsIgnoreCase("disable")) {
            return false;
        }
        return null;
    }

    private String getTerritoryResultMessage(String reason) {
        String path = "country.territory." + reason;
        if (plugin.hasMessage(path)) {
            return plugin.getMessage(path);
        }
        return plugin.getMessage("country.territory.unknown", plugin.placeholders("reason", reason));
    }
}
