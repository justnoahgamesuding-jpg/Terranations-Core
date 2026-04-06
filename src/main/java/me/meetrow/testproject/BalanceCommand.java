package me.meetrow.testproject;

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

public class BalanceCommand implements CommandExecutor, TabCompleter {
    private final Testproject plugin;

    public BalanceCommand(Testproject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.hasEconomy()) {
            sender.sendMessage(plugin.getMessage("balance.disabled"));
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessage("balance.player-only"));
                return true;
            }
            sendBalance(sender, player, player);
            return true;
        }

        if (!hasAdminAccess(sender)) {
            sender.sendMessage(plugin.getMessage("balance.no-permission"));
            return true;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 1 && !isAdminAction(action)) {
            OfflinePlayer target = findPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(plugin.getMessage("general.player-not-found"));
                return true;
            }
            sendBalance(sender, target, target);
            return true;
        }

        switch (action) {
            case "show" -> {
                if (args.length != 2) {
                    sender.sendMessage(plugin.getMessage("balance.admin.usage"));
                    return true;
                }
                OfflinePlayer target = findPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getMessage("general.player-not-found"));
                    return true;
                }
                sendBalance(sender, target, target);
                return true;
            }
            case "set", "add", "take" -> {
                if (args.length != 3) {
                    sender.sendMessage(plugin.getMessage("balance.admin.usage"));
                    return true;
                }
                OfflinePlayer target = findPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getMessage("general.player-not-found"));
                    return true;
                }
                double amount;
                try {
                    amount = Double.parseDouble(args[2]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(plugin.getMessage("balance.invalid-amount"));
                    return true;
                }
                if (amount < 0.0D) {
                    sender.sendMessage(plugin.getMessage("balance.invalid-amount"));
                    return true;
                }

                double newBalance = switch (action) {
                    case "set" -> plugin.setBalance(target.getUniqueId(), amount);
                    case "add" -> plugin.depositBalance(target.getUniqueId(), amount);
                    default -> plugin.withdrawBalance(target.getUniqueId(), amount);
                };

                sender.sendMessage(plugin.getMessage("balance.admin." + action, plugin.placeholders(
                        "player", safeName(target, args[1]),
                        "amount", plugin.formatMoney(amount),
                        "balance", plugin.formatMoney(newBalance)
                )));
                if (target.isOnline() && target.getPlayer() != null) {
                    target.getPlayer().sendMessage(plugin.getMessage("balance.updated", plugin.placeholders(
                            "balance", plugin.formatMoney(newBalance)
                    )));
                }
                return true;
            }
            default -> {
                sender.sendMessage(plugin.getMessage("balance.admin.usage"));
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!plugin.hasEconomy()) {
            return Collections.emptyList();
        }
        if (!hasAdminAccess(sender)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("show", "set", "add", "take"));
            options.addAll(getKnownPlayerNames());
            return partialMatches(args[0], options);
        }
        if (args.length == 2 && isAdminAction(args[0])) {
            return partialMatches(args[1], getKnownPlayerNames());
        }
        if (args.length == 3 && List.of("set", "add", "take").contains(args[0].toLowerCase(Locale.ROOT))) {
            return partialMatches(args[2], List.of("0", "10", "100", "1000"));
        }
        return Collections.emptyList();
    }

    private void sendBalance(CommandSender viewer, OfflinePlayer target, OfflinePlayer nameSource) {
        viewer.sendMessage(plugin.getMessage("balance.show", plugin.placeholders(
                "player", safeName(nameSource, "Unknown"),
                "balance", plugin.formatMoney(plugin.getBalance(target))
        )));
    }

    private boolean hasAdminAccess(CommandSender sender) {
        return sender.hasPermission(Testproject.ADMIN_PERMISSION) || sender.isOp();
    }

    private boolean isAdminAction(String input) {
        String normalized = input.toLowerCase(Locale.ROOT);
        return normalized.equals("show") || normalized.equals("set") || normalized.equals("add") || normalized.equals("take");
    }

    private OfflinePlayer findPlayer(String input) {
        Player online = Bukkit.getPlayerExact(input);
        if (online != null) {
            return online;
        }
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(input)) {
                return offlinePlayer;
            }
        }
        return null;
    }

    private List<String> getKnownPlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null && !names.contains(offlinePlayer.getName())) {
                names.add(offlinePlayer.getName());
            }
        }
        return names;
    }

    private List<String> partialMatches(String input, List<String> options) {
        String normalized = input.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(normalized)) {
                matches.add(option);
            }
        }
        return matches;
    }

    private String safeName(OfflinePlayer player, String fallback) {
        return player.getName() != null ? player.getName() : fallback;
    }
}
