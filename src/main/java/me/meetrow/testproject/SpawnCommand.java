package me.meetrow.testproject;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpawnCommand implements CommandExecutor, TabCompleter {
    private final Testproject plugin;

    public SpawnCommand(Testproject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("spawn.only-players"));
            return true;
        }
        if (!plugin.canUseCountryWarpAdmin(player)) {
            player.sendMessage(plugin.getMessage("spawn.no-permission"));
            return true;
        }

        if (args.length == 0) {
            plugin.openCountryWarpMenu(player, 0);
            return true;
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        if (subcommand.equals("menu") || subcommand.equals("admin") || subcommand.equals("warp")) {
            int page = 0;
            if (args.length >= 2) {
                try {
                    page = Math.max(0, Integer.parseInt(args[1]) - 1);
                } catch (NumberFormatException exception) {
                    player.sendMessage(plugin.getMessage("spawn.invalid-page"));
                    return true;
                }
            }
            plugin.openCountryWarpMenu(player, page);
            return true;
        }

        Country country = plugin.getCountry(joinArgs(args));
        if (country == null) {
            player.sendMessage(plugin.getMessage("country.not-found"));
            return true;
        }
        if (!country.hasHome() || plugin.getCountryHome(country) == null) {
            player.sendMessage(plugin.getMessage("spawn.country-no-home", plugin.placeholders("country", country.getName())));
            return true;
        }

        plugin.teleportToCountryHome(player, country, false, "spawn.teleported");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (args.length == 1 && plugin.canUseCountryWarpAdmin(player)) {
            List<String> options = new ArrayList<>(List.of("menu", "admin", "warp"));
            for (Country country : plugin.getCountries()) {
                options.add(country.getName());
            }
            return partial(args[0], options);
        }
        return List.of();
    }

    private String joinArgs(String[] args) {
        return String.join(" ", args).trim();
    }

    private List<String> partial(String input, List<String> options) {
        String lowered = input.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(lowered)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
