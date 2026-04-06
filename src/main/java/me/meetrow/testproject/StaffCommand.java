package me.meetrow.testproject;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class StaffCommand implements CommandExecutor, TabCompleter {
    private final Testproject plugin;

    public StaffCommand(Testproject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("staff.only-players"));
            return true;
        }
        if (!plugin.canUseStaffMode(player)) {
            player.sendMessage(plugin.getMessage("staff.no-permission"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("menu")) {
            plugin.openStaffMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("mode")) {
            Boolean desired = parseToggle(args, 1);
            boolean enabled;
            if (desired == null) {
                enabled = plugin.toggleStaffMode(player);
            } else if (desired) {
                if (!plugin.isInStaffMode(player.getUniqueId())) {
                    plugin.enableStaffMode(player);
                }
                enabled = true;
            } else {
                if (plugin.isInStaffMode(player.getUniqueId())) {
                    plugin.disableStaffMode(player);
                }
                enabled = false;
            }
            player.sendMessage(plugin.getMessage(enabled ? "staff.enabled" : "staff.disabled"));
            return true;
        }

        if (args[0].equalsIgnoreCase("vanish")) {
            Boolean desired = parseToggle(args, 1);
            boolean vanished;
            if (desired == null) {
                vanished = plugin.toggleVanish(player);
            } else {
                plugin.setVanished(player, desired);
                vanished = desired;
            }
            player.sendMessage(Component.text("Vanish " + (vanished ? "enabled." : "disabled."), NamedTextColor.GREEN));
            return true;
        }

        if (args[0].equalsIgnoreCase("freeze")) {
            if (args.length < 2) {
                player.sendMessage(Component.text("Usage: /staff freeze <player> [on|off|toggle]", NamedTextColor.RED));
                return true;
            }
            Player target = findOnlinePlayer(args[1]);
            if (target == null) {
                player.sendMessage(plugin.getMessage("general.player-not-found"));
                return true;
            }
            Boolean desired = parseToggle(args, 2);
            boolean frozen;
            if (desired == null) {
                frozen = plugin.toggleFrozen(target.getUniqueId());
            } else {
                plugin.setFrozen(target.getUniqueId(), desired);
                frozen = desired;
            }
            player.sendMessage(Component.text((frozen ? "Froze " : "Unfroze ") + target.getName() + ".", NamedTextColor.GREEN));
            target.sendMessage(Component.text(frozen ? "You have been frozen by staff." : "You are no longer frozen.", frozen ? NamedTextColor.RED : NamedTextColor.GREEN));
            return true;
        }

        if (args[0].equalsIgnoreCase("heal")) {
            Player target = args.length >= 2 ? findOnlinePlayer(args[1]) : player;
            if (target == null) {
                player.sendMessage(plugin.getMessage("general.player-not-found"));
                return true;
            }
            plugin.healAndFeedPlayer(target);
            if (target.equals(player)) {
                player.sendMessage(Component.text("You have been healed and fed.", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Healed and fed " + target.getName() + ".", NamedTextColor.GREEN));
                target.sendMessage(Component.text("You have been healed and fed by staff.", NamedTextColor.GREEN));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("tp")) {
            if (args.length < 2) {
                player.sendMessage(Component.text("Usage: /staff tp <player>", NamedTextColor.RED));
                return true;
            }
            Player target = findOnlinePlayer(args[1]);
            if (target == null) {
                player.sendMessage(plugin.getMessage("general.player-not-found"));
                return true;
            }
            plugin.teleportPlayerToPlayer(player, target);
            player.sendMessage(Component.text("Teleported to " + target.getName() + ".", NamedTextColor.GREEN));
            return true;
        }

        if (args[0].equalsIgnoreCase("bring")) {
            if (args.length < 2) {
                player.sendMessage(Component.text("Usage: /staff bring <player>", NamedTextColor.RED));
                return true;
            }
            Player target = findOnlinePlayer(args[1]);
            if (target == null) {
                player.sendMessage(plugin.getMessage("general.player-not-found"));
                return true;
            }
            plugin.summonPlayer(target, player);
            player.sendMessage(Component.text("Brought " + target.getName() + " to you.", NamedTextColor.GREEN));
            target.sendMessage(Component.text("You were summoned by staff.", NamedTextColor.YELLOW));
            return true;
        }

        player.sendMessage(Component.text("Usage: /staff <menu|mode|vanish|freeze|heal|tp|bring>", NamedTextColor.RED));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player) || !plugin.canUseStaffMode(player)) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return partialMatches(args[0], List.of("menu", "mode", "vanish", "freeze", "heal", "tp", "bring"));
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("mode") || args[0].equalsIgnoreCase("vanish"))) {
            return partialMatches(args[1], List.of("on", "off", "toggle"));
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("freeze")
                || args[0].equalsIgnoreCase("heal")
                || args[0].equalsIgnoreCase("tp")
                || args[0].equalsIgnoreCase("bring"))) {
            return partialMatches(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("freeze")) {
            return partialMatches(args[2], List.of("on", "off", "toggle"));
        }
        return Collections.emptyList();
    }

    private Boolean parseToggle(String[] args, int index) {
        if (args.length <= index) {
            return null;
        }
        return switch (args[index].toLowerCase(Locale.ROOT)) {
            case "on", "enable", "enabled", "true" -> true;
            case "off", "disable", "disabled", "false" -> false;
            case "toggle" -> null;
            default -> null;
        };
    }

    private Player findOnlinePlayer(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        Player exact = Bukkit.getPlayerExact(input);
        if (exact != null) {
            return exact;
        }
        String lowered = input.toLowerCase(Locale.ROOT);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().toLowerCase(Locale.ROOT).startsWith(lowered)) {
                return online;
            }
        }
        return null;
    }

    private List<String> partialMatches(String token, List<String> values) {
        String lowered = token == null ? "" : token.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowered))
                .toList();
    }
}
