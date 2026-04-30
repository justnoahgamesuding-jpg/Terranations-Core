package me.meetrow.testproject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class GuildCommand implements CommandExecutor, TabCompleter {
    private final Testproject plugin;

    public GuildCommand(Testproject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (!player.hasPermission(Testproject.GUILD_USE_PERMISSION) && !player.isOp()) {
            player.sendMessage(plugin.colorize("&cYou do not have permission to use guild commands."));
            return true;
        }
        if (args.length == 0) {
            plugin.openGuildMenu(player);
            return true;
        }
        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "create" -> handleCreate(player, args);
            case "info" -> handleInfo(player, args);
            case "invite" -> handleInvite(player, args, false);
            case "resendinvite", "resend" -> handleInvite(player, args, true);
            case "join", "accept" -> handleJoin(player, args);
            case "deny" -> handleDeny(player, args);
            case "deposit" -> handleDeposit(player, args);
            case "withdraw" -> handleWithdraw(player, args);
            case "claim" -> handleClaim(player, args);
            case "role" -> handleRole(player, args);
            case "permissions", "perms" -> handlePermissions(player, args);
            case "kick" -> handleKick(player, args);
            case "leave" -> handleLeave(player);
            case "transferleader", "transfer" -> handleTransferLeader(player, args);
            case "disband" -> handleDisband(player);
            case "description", "desc" -> handleDescription(player, args);
            case "motd" -> handleMotd(player, args);
            case "recruiting", "recruitment" -> handleRecruiting(player, args);
            case "stockpile" -> handleStockpile(player, args);
            case "logs", "log" -> handleLogs(player, args);
            case "list" -> handleList(player);
            default -> {
                sendOverview(player);
                yield true;
            }
        };
    }

    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.colorize("&cUsage: /guild create <name> <tag>"));
            return true;
        }
        String tag = args[args.length - 1];
        String name = joinArgs(args, 1, args.length - 1);
        String failure = plugin.createGuild(player, name, tag);
        if (failure != null) {
            player.sendMessage(plugin.colorize("&c" + failure));
            return true;
        }
        player.sendMessage(plugin.colorize("&aCreated guild &f" + name + " &a[" + tag.toUpperCase(Locale.ROOT) + "]"));
        return true;
    }

    private boolean handleInfo(Player player, String[] args) {
        Guild guild = args.length >= 2 ? plugin.getGuildByNameOrTag(joinArgs(args, 1, args.length)) : plugin.getPlayerGuild(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(plugin.colorize("&cGuild not found."));
            return true;
        }
        player.sendMessage(plugin.colorize("&6Guild: &f" + guild.getName() + " &8[" + guild.getTag() + "]"));
        player.sendMessage(plugin.colorize("&7Leader: &f" + plugin.safeOfflineName(guild.getLeaderId())));
        player.sendMessage(plugin.colorize("&7Level: &f" + plugin.getGuildLevel(guild) + " &8(score " + plugin.getGuildScore(guild) + ", xp " + guild.getGuildXp() + ")"));
        player.sendMessage(plugin.colorize("&7Members: &f" + guild.getMembers().size() + "&7/&f" + plugin.getGuildMemberCap(guild)));
        player.sendMessage(plugin.colorize("&7Treasury: &f⛃" + plugin.formatSignedMoney(guild.getBalance())));
        player.sendMessage(plugin.colorize("&7Recruiting: &f" + (guild.isRecruitingOpen() ? "Open" : "Closed")));
        player.sendMessage(plugin.colorize("&7Description: &f" + (guild.getDescription().isBlank() ? "None" : guild.getDescription())));
        if (!guild.getMotd().isBlank()) {
            player.sendMessage(plugin.colorize("&7MOTD: &f" + guild.getMotd()));
        }
        List<String> countries = plugin.getGuildClaimedCountryNames(guild);
        player.sendMessage(plugin.colorize("&7Countries: &f" + (countries.isEmpty() ? "none" : String.join(", ", countries))));
        if (guild.getCapitalCountryKey() != null) {
            Country capital = plugin.getCountryByKey(guild.getCapitalCountryKey());
            if (capital != null) {
                player.sendMessage(plugin.colorize("&7Capital: &f" + capital.getName()));
            }
        }
        return true;
    }

    private boolean handleInvite(Player player, String[] args, boolean resend) {
        if (args.length < 2) {
            player.sendMessage(plugin.colorize("&cUsage: /guild " + (resend ? "resendinvite" : "invite") + " <player>"));
            return true;
        }
        Guild guild = requireGuild(player);
        if (guild == null) {
            return true;
        }
        if (!plugin.playerHasGuildPermission(guild, player.getUniqueId(), GuildPermission.INVITE_PLAYERS)) {
            player.sendMessage(plugin.colorize("&cYou do not have permission to invite players."));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(plugin.colorize("&cPlayer not found."));
            return true;
        }
        String failure = plugin.invitePlayerToGuild(guild, player.getUniqueId(), target.getUniqueId());
        if (failure != null) {
            player.sendMessage(plugin.colorize("&c" + failure));
            return true;
        }
        player.sendMessage(plugin.colorize((resend ? "&aResent invite to &f" : "&aInvited &f") + target.getName() + "&a."));
        plugin.sendGuildInvitePrompt(target, guild, player.getName(), resend);
        return true;
    }

    private boolean handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.colorize("&cUsage: /guild join <guild>"));
            return true;
        }
        String failure = plugin.joinGuild(player, joinArgs(args, 1, args.length));
        if (failure != null) {
            player.sendMessage(plugin.colorize("&c" + failure));
            return true;
        }
        Guild guild = plugin.getPlayerGuild(player.getUniqueId());
        player.sendMessage(plugin.colorize("&aJoined guild &f" + (guild != null ? guild.getName() : joinArgs(args, 1, args.length)) + "&a."));
        if (guild != null && !guild.getMotd().isBlank()) {
            player.sendMessage(plugin.colorize("&7MOTD: &f" + guild.getMotd()));
        }
        return true;
    }

    private boolean handleDeny(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.colorize("&cUsage: /guild deny <guild>"));
            return true;
        }
        String failure = plugin.denyGuildInvite(player.getUniqueId(), joinArgs(args, 1, args.length));
        if (failure != null) {
            player.sendMessage(plugin.colorize("&c" + failure));
            return true;
        }
        player.sendMessage(plugin.colorize("&7Guild invite denied."));
        return true;
    }

    private boolean handleDeposit(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.colorize("&cUsage: /guild deposit <amount>"));
            return true;
        }
        Guild guild = requireGuild(player);
        if (guild == null) {
            return true;
        }
        if (!plugin.playerHasGuildPermission(guild, player.getUniqueId(), GuildPermission.DEPOSIT_FUNDS)) {
            player.sendMessage(plugin.colorize("&cYou do not have permission to deposit."));
            return true;
        }
        Double amount = parseAmount(args[1]);
        if (amount == null) {
            player.sendMessage(plugin.colorize("&cInvalid amount."));
            return true;
        }
        String failure = plugin.depositGuildBalance(player, guild, amount);
        if (failure != null) {
            player.sendMessage(plugin.colorize("&c" + failure));
            return true;
        }
        player.sendMessage(plugin.colorize("&aDeposited &f⛃" + plugin.formatMoney(amount) + "&a into &f" + guild.getName() + "&a."));
        return true;
    }

    private boolean handleWithdraw(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.colorize("&cUsage: /guild withdraw <amount>"));
            return true;
        }
        Guild guild = requireGuild(player);
        if (guild == null) {
            return true;
        }
        if (!plugin.playerHasGuildPermission(guild, player.getUniqueId(), GuildPermission.WITHDRAW_FUNDS)) {
            player.sendMessage(plugin.colorize("&cYou do not have permission to withdraw."));
            return true;
        }
        Double amount = parseAmount(args[1]);
        if (amount == null) {
            player.sendMessage(plugin.colorize("&cInvalid amount."));
            return true;
        }
        String failure = plugin.withdrawGuildBalance(player, guild, amount);
        if (failure != null) {
            player.sendMessage(plugin.colorize("&c" + failure));
            return true;
        }
        player.sendMessage(plugin.colorize("&aWithdrew &f⛃" + plugin.formatMoney(amount) + "&a from &f" + guild.getName() + "&a."));
        return true;
    }

    private boolean handleClaim(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.colorize("&cUsage: /guild claim <country>"));
            return true;
        }
        Guild guild = requireGuild(player);
        if (guild == null) {
            return true;
        }
        if (!plugin.playerHasGuildPermission(guild, player.getUniqueId(), GuildPermission.CLAIM_COUNTRIES)) {
            player.sendMessage(plugin.colorize("&cYou do not have permission to claim countries."));
            return true;
        }
        String countryName = joinArgs(args, 1, args.length);
        Country country = plugin.getVisibleCountry(countryName);
        if (country == null) {
            country = plugin.getCountry(countryName);
        }
        if (country == null) {
            player.sendMessage(plugin.colorize("&cCountry not found."));
            return true;
        }
        String failure = plugin.claimCountryForGuild(guild, country, player);
        if (failure != null) {
            player.sendMessage(plugin.colorize("&c" + failure));
            return true;
        }
        player.sendMessage(plugin.colorize("&aGuild &f" + guild.getName() + "&a claimed &f" + country.getName() + "&a."));
        return true;
    }

    private boolean handleRole(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.colorize("&cUsage: /guild role <player> <member|admiral|officer>"));
            return true;
        }
        Guild guild = requireGuild(player);
        if (guild == null) {
            return true;
        }
        if (!plugin.playerHasGuildPermission(guild, player.getUniqueId(), GuildPermission.MANAGE_ROLES)) {
            player.sendMessage(plugin.colorize("&cYou do not have permission to manage roles."));
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        GuildRole role = GuildRole.fromKey(args[2]);
        if (role == null || role == GuildRole.LEADER) {
            player.sendMessage(plugin.colorize("&cInvalid role."));
            return true;
        }
        String failure = plugin.setGuildMemberRole(guild, player.getUniqueId(), target.getUniqueId(), role);
        if (failure != null) {
            player.sendMessage(plugin.colorize("&c" + failure));
            return true;
        }
        player.sendMessage(plugin.colorize("&aUpdated &f" + plugin.safeOfflineName(target) + "&a to &f" + role.getDisplayName() + "&a."));
        return true;
    }

    private boolean handlePermissions(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(plugin.colorize("&cUsage: /guild permissions <role|player> <target> <permission> <allow|deny|default>"));
            return true;
        }
        Guild guild = requireGuild(player);
        if (guild == null) {
            return true;
        }
        String mode = args[1].toLowerCase(Locale.ROOT);
        GuildPermission permission = GuildPermission.fromKey(args[3]);
        GuildPermissionState state = GuildPermissionState.fromKey(args[4]);
        if (permission == null || state == null) {
            player.sendMessage(plugin.colorize("&cInvalid permission or state."));
            return true;
        }
        if (mode.equals("role")) {
            if (!plugin.playerHasGuildPermission(guild, player.getUniqueId(), GuildPermission.MANAGE_ROLE_PERMISSIONS)) {
                player.sendMessage(plugin.colorize("&cYou do not have permission to manage role permissions."));
                return true;
            }
            GuildRole role = GuildRole.fromKey(args[2]);
            if (role == null || role == GuildRole.LEADER) {
                player.sendMessage(plugin.colorize("&cInvalid role."));
                return true;
            }
            plugin.setGuildRolePermission(guild, role, permission, state);
            player.sendMessage(plugin.colorize("&aUpdated role permission."));
            return true;
        }
        if (!mode.equals("player")) {
            player.sendMessage(plugin.colorize("&cUsage: /guild permissions <role|player> <target> <permission> <allow|deny|default>"));
            return true;
        }
        if (!plugin.playerHasGuildPermission(guild, player.getUniqueId(), GuildPermission.MANAGE_PLAYER_PERMISSIONS)) {
            player.sendMessage(plugin.colorize("&cYou do not have permission to manage player permissions."));
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
        plugin.setGuildPlayerPermission(guild, target.getUniqueId(), permission, state);
        player.sendMessage(plugin.colorize("&aUpdated player permission."));
        return true;
    }

    private boolean handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.colorize("&cUsage: /guild kick <player>"));
            return true;
        }
        Guild guild = requireGuild(player);
        if (guild == null) {
            return true;
        }
        if (!plugin.playerHasGuildPermission(guild, player.getUniqueId(), GuildPermission.REMOVE_PLAYERS)) {
            player.sendMessage(plugin.colorize("&cYou do not have permission to remove players."));
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String failure = plugin.kickGuildMember(guild, player.getUniqueId(), target.getUniqueId());
        if (failure != null) {
            player.sendMessage(plugin.colorize("&c" + failure));
            return true;
        }
        player.sendMessage(plugin.colorize("&aRemoved &f" + plugin.safeOfflineName(target) + "&a from the guild."));
        return true;
    }

    private boolean handleLeave(Player player) {
        Guild guild = requireGuild(player);
        if (guild == null) {
            return true;
        }
        if (guild.getLeaderId() != null && guild.getLeaderId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.colorize("&cThe guild leader cannot leave. Use &f/guild transferleader <player> &cor &f/guild disband&c."));
            return true;
        }
        plugin.leaveGuild(player.getUniqueId());
        player.sendMessage(plugin.colorize("&aYou left &f" + guild.getName() + "&a."));
        return true;
    }

    private boolean handleTransferLeader(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.colorize("&cUsage: /guild transferleader <player>"));
            return true;
        }
        Guild guild = requireGuild(player);
        if (guild == null) {
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String failure = plugin.transferGuildLeadership(guild, player.getUniqueId(), target.getUniqueId());
        if (failure != null) {
            player.sendMessage(plugin.colorize("&c" + failure));
            return true;
        }
        player.sendMessage(plugin.colorize("&aTransferred guild leadership to &f" + plugin.safeOfflineName(target) + "&a."));
        return true;
    }

    private boolean handleDisband(Player player) {
        Guild guild = requireGuild(player);
        if (guild == null) {
            return true;
        }
        String guildName = guild.getName();
        String failure = plugin.disbandGuild(guild, player.getUniqueId());
        if (failure != null) {
            player.sendMessage(plugin.colorize("&c" + failure));
            return true;
        }
        player.sendMessage(plugin.colorize("&cDisbanded guild &f" + guildName + "&c."));
        return true;
    }

    private boolean handleDescription(Player player, String[] args) {
        Guild guild = requireGuild(player);
        if (guild == null) {
            return true;
        }
        if (!plugin.playerHasGuildPermission(guild, player.getUniqueId(), GuildPermission.MANAGE_GUILD_PROFILE)) {
            player.sendMessage(plugin.colorize("&cYou do not have permission to update the guild profile."));
            return true;
        }
        String description = args.length >= 2 ? joinArgs(args, 1, args.length) : "";
        plugin.setGuildDescription(guild, description);
        player.sendMessage(plugin.colorize("&aUpdated guild description."));
        return true;
    }

    private boolean handleMotd(Player player, String[] args) {
        Guild guild = requireGuild(player);
        if (guild == null) {
            return true;
        }
        if (!plugin.playerHasGuildPermission(guild, player.getUniqueId(), GuildPermission.MANAGE_GUILD_PROFILE)) {
            player.sendMessage(plugin.colorize("&cYou do not have permission to update the guild MOTD."));
            return true;
        }
        String motd = args.length >= 2 ? joinArgs(args, 1, args.length) : "";
        plugin.setGuildMotd(guild, motd);
        player.sendMessage(plugin.colorize("&aUpdated guild MOTD."));
        return true;
    }

    private boolean handleRecruiting(Player player, String[] args) {
        Guild guild = requireGuild(player);
        if (guild == null) {
            return true;
        }
        if (!plugin.playerHasGuildPermission(guild, player.getUniqueId(), GuildPermission.MANAGE_RECRUITMENT)) {
            player.sendMessage(plugin.colorize("&cYou do not have permission to change recruiting status."));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(plugin.colorize("&7Recruiting is currently &f" + (guild.isRecruitingOpen() ? "open" : "closed") + "&7."));
            return true;
        }
        boolean recruitingOpen = args[1].equalsIgnoreCase("open") || args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true");
        plugin.setGuildRecruitingOpen(guild, recruitingOpen);
        player.sendMessage(plugin.colorize("&aGuild recruiting is now &f" + (recruitingOpen ? "open" : "closed") + "&a."));
        return true;
    }

    private boolean handleStockpile(Player player, String[] args) {
        Guild guild = requireGuild(player);
        if (guild == null) {
            return true;
        }
        if (args.length == 1) {
            sendStockpileSummary(player, guild);
            return true;
        }
        if (!args[1].equalsIgnoreCase("deposit")) {
            player.sendMessage(plugin.colorize("&cUsage: /guild stockpile [deposit <amount>]"));
            return true;
        }
        if (!plugin.playerHasGuildPermission(guild, player.getUniqueId(), GuildPermission.MANAGE_STOCKPILE)) {
            player.sendMessage(plugin.colorize("&cYou do not have permission to manage the guild stockpile."));
            return true;
        }
        if (args.length < 3) {
            player.sendMessage(plugin.colorize("&cUsage: /guild stockpile deposit <amount>"));
            return true;
        }
        Integer amount = parseWholeAmount(args[2]);
        if (amount == null) {
            player.sendMessage(plugin.colorize("&cInvalid amount."));
            return true;
        }
        String failure = plugin.depositGuildStockpile(player, guild, amount);
        if (failure != null) {
            player.sendMessage(plugin.colorize("&c" + failure));
            return true;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        player.sendMessage(plugin.colorize("&aDeposited &f" + amount + "x " + plugin.formatMaterialName(held.getType()) + "&a into the guild stockpile."));
        return true;
    }

    private boolean handleLogs(Player player, String[] args) {
        Guild guild = requireGuild(player);
        if (guild == null) {
            return true;
        }
        List<String> logs = plugin.getGuildLogs(guild);
        if (logs.isEmpty()) {
            player.sendMessage(plugin.colorize("&7No guild logs yet."));
            return true;
        }
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Math.max(1, Integer.parseInt(args[1]));
            } catch (NumberFormatException ignored) {
            }
        }
        int pageSize = 8;
        int totalPages = Math.max(1, (int) Math.ceil(logs.size() / (double) pageSize));
        int safePage = Math.min(page, totalPages);
        int start = Math.max(0, logs.size() - (safePage * pageSize));
        int end = Math.min(logs.size(), start + pageSize);
        player.sendMessage(plugin.colorize("&6Guild Logs &7(" + safePage + "/" + totalPages + ")"));
        for (int index = start; index < end; index++) {
            player.sendMessage(plugin.colorize("&7" + logs.get(index)));
        }
        return true;
    }

    private boolean handleList(Player player) {
        List<Guild> guilds = plugin.getGuilds();
        guilds.sort(Comparator.comparing(Guild::getName, String.CASE_INSENSITIVE_ORDER));
        if (guilds.isEmpty()) {
            player.sendMessage(plugin.colorize("&7No guilds exist yet."));
            return true;
        }
        List<String> names = new ArrayList<>();
        for (Guild guild : guilds) {
            names.add(guild.getName() + " [" + guild.getTag() + "]" + (guild.isRecruitingOpen() ? " open" : " closed"));
        }
        player.sendMessage(plugin.colorize("&6Guilds: &f" + String.join("&7, &f", names)));
        return true;
    }

    private void sendStockpileSummary(Player player, Guild guild) {
        player.sendMessage(plugin.colorize("&6Guild Stockpile &7(" + guild.getName() + ")"));
        if (guild.getStockpile().isEmpty()) {
            player.sendMessage(plugin.colorize("&7No resources deposited yet."));
            return;
        }
        guild.getStockpile().entrySet().stream()
                .sorted((left, right) -> Integer.compare(right.getValue(), left.getValue()))
                .limit(8)
                .forEach(entry -> {
                    Material material = Material.matchMaterial(entry.getKey().toUpperCase(Locale.ROOT));
                    String display = material != null ? plugin.formatMaterialName(material) : entry.getKey();
                    player.sendMessage(plugin.colorize("&f" + entry.getValue() + "x &7" + display));
                });
    }

    private Guild requireGuild(Player player) {
        Guild guild = plugin.getPlayerGuild(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(plugin.colorize("&cYou are not in a guild."));
        }
        return guild;
    }

    private void sendOverview(Player player) {
        Guild guild = plugin.getPlayerGuild(player.getUniqueId());
        player.sendMessage(plugin.colorize("&6Guild Commands"));
        if (guild != null) {
            player.sendMessage(plugin.colorize("&7Current: &f" + guild.getName() + " &8[" + guild.getTag() + "]"));
        }
        player.sendMessage(plugin.colorize("&e/guild create <name> <tag>"));
        player.sendMessage(plugin.colorize("&e/guild info [guild]"));
        player.sendMessage(plugin.colorize("&e/guild invite <player>"));
        player.sendMessage(plugin.colorize("&e/guild resendinvite <player>"));
        player.sendMessage(plugin.colorize("&e/guild accept <guild>"));
        player.sendMessage(plugin.colorize("&e/guild deny <guild>"));
        player.sendMessage(plugin.colorize("&e/guild deposit <amount>"));
        player.sendMessage(plugin.colorize("&e/guild withdraw <amount>"));
        player.sendMessage(plugin.colorize("&e/guild claim <country>"));
        player.sendMessage(plugin.colorize("&e/guild role <player> <role>"));
        player.sendMessage(plugin.colorize("&e/guild permissions <role|player> <target> <permission> <allow|deny|default>"));
        player.sendMessage(plugin.colorize("&e/guild description <text>"));
        player.sendMessage(plugin.colorize("&e/guild motd <text>"));
        player.sendMessage(plugin.colorize("&e/guild recruiting <open|closed>"));
        player.sendMessage(plugin.colorize("&e/guild stockpile [deposit <amount>]"));
        player.sendMessage(plugin.colorize("&e/guild logs [page]"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.add("create");
            options.add("info");
            options.add("invite");
            options.add("resendinvite");
            options.add("resend");
            options.add("join");
            options.add("accept");
            options.add("deny");
            options.add("deposit");
            options.add("withdraw");
            options.add("claim");
            options.add("role");
            options.add("permissions");
            options.add("kick");
            options.add("leave");
            options.add("transferleader");
            options.add("disband");
            options.add("description");
            options.add("motd");
            options.add("recruiting");
            options.add("stockpile");
            options.add("logs");
            options.add("list");
            return filter(args[0], options);
        }
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (args.length == 2 && List.of("join", "accept", "deny", "info").contains(args[0].toLowerCase(Locale.ROOT))) {
            for (Guild guild : plugin.getGuilds()) {
                options.add(guild.getName());
            }
            return filter(args[1], options);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("claim")) {
            for (Country country : plugin.getVisibleCountries()) {
                options.add(country.getName());
            }
            return filter(args[1], options);
        }
        if (args.length == 2 && List.of("invite", "resendinvite", "resend", "kick", "role", "transferleader").contains(args[0].toLowerCase(Locale.ROOT))) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                options.add(online.getName());
            }
            return filter(args[1], options);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("permissions")) {
            return filter(args[1], List.of("role", "player"));
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("permissions") && args[1].equalsIgnoreCase("role")) {
            return filter(args[2], List.of("member", "admiral", "officer"));
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("permissions") && args[1].equalsIgnoreCase("player")) {
            for (UUID memberId : plugin.getGuildMemberIds(player.getUniqueId())) {
                options.add(plugin.safeOfflineName(Bukkit.getOfflinePlayer(memberId)));
            }
            return filter(args[2], options);
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("permissions")) {
            for (GuildPermission permission : GuildPermission.values()) {
                options.add(permission.name().toLowerCase(Locale.ROOT));
            }
            return filter(args[3], options);
        }
        if (args.length == 5 && args[0].equalsIgnoreCase("permissions")) {
            return filter(args[4], List.of("allow", "deny", "default"));
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("role")) {
            return filter(args[2], List.of("member", "admiral", "officer"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("recruiting")) {
            return filter(args[1], List.of("open", "closed"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("stockpile")) {
            return filter(args[1], List.of("deposit"));
        }
        return List.of();
    }

    private List<String> filter(String input, List<String> options) {
        String lowered = input == null ? "" : input.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowered))
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private String joinArgs(String[] args, int startInclusive, int endExclusive) {
        if (args == null || startInclusive >= endExclusive || startInclusive < 0) {
            return "";
        }
        return String.join(" ", java.util.Arrays.copyOfRange(args, startInclusive, endExclusive)).trim();
    }

    private Double parseAmount(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer parseWholeAmount(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

}
