package me.meetrow.testproject;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public final class ChatChannelCommand implements CommandExecutor, TabCompleter {
    private final Testproject plugin;

    public ChatChannelCommand(Testproject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("country.only-players"));
            return true;
        }

        boolean countryCommand = command.getName().equalsIgnoreCase("countrychat");
        Boolean enabled = parseToggle(args);
        if (enabled == null && args.length > 0) {
            player.sendMessage(plugin.getChatMessage(countryCommand ? "messages.country.toggle-usage" : "messages.global.toggle-usage"));
            return true;
        }

        if (countryCommand) {
            Country country = plugin.getPlayerCountry(player.getUniqueId());
            if (country == null) {
                player.sendMessage(plugin.getChatMessage("messages.country.not-in-country"));
                return true;
            }
            boolean next = enabled != null ? enabled : !plugin.isCountryChatEnabled(player.getUniqueId());
            plugin.setCountryChatEnabled(player.getUniqueId(), next);
            player.sendMessage(plugin.getChatMessage(next ? "messages.country.enabled" : "messages.country.disabled"));
            return true;
        }

        boolean next = enabled != null ? enabled : !plugin.isGlobalChatEnabled(player.getUniqueId());
        plugin.setGlobalChatEnabled(player.getUniqueId(), next);
        player.sendMessage(plugin.getChatMessage(next ? "messages.global.enabled" : "messages.global.disabled"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("on", "off");
        }
        return Collections.emptyList();
    }

    private Boolean parseToggle(String[] args) {
        if (args.length == 0) {
            return null;
        }
        if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("enable")) {
            return true;
        }
        if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("disable")) {
            return false;
        }
        return null;
    }
}
