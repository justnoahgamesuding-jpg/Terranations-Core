package me.meetrow.testproject;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CommandProtectionListener implements Listener {
    private static final Set<String> BLOCKED_PLUGIN_COMMANDS = Set.of(
            "pl",
            "plugins",
            "bukkit:pl",
            "bukkit:plugins",
            "minecraft:plugins"
    );

    private final Testproject plugin;

    public CommandProtectionListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (plugin.canViewPlugins(player)) {
            return;
        }

        String message = event.getMessage();
        if (message.length() <= 1 || message.charAt(0) != '/') {
            return;
        }

        String commandLabel = message.substring(1).split(" ", 2)[0].toLowerCase(Locale.ROOT);
        if (!BLOCKED_PLUGIN_COMMANDS.contains(commandLabel)) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(plugin.getMessage("permissions.plugin-list"));
    }

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        Set<String> commands = new HashSet<>(event.getCommands());
        for (String command : commands) {
            String normalized = command.toLowerCase(Locale.ROOT);
            if (!plugin.canViewPlugins(event.getPlayer()) && (normalized.equals("pl") || normalized.equals("plugins"))) {
                event.getCommands().remove(command);
            }
            if (!plugin.canUseAdminCommands(event.getPlayer()) && normalized.equals("terra")) {
                event.getCommands().remove(command);
            }
            if (!plugin.canUseStaffMode(event.getPlayer()) && normalized.equals("staff")) {
                event.getCommands().remove(command);
            }
            if (!plugin.canUseStaffMode(event.getPlayer()) && (normalized.equals("flyspeed") || normalized.equals("vanish"))) {
                event.getCommands().remove(command);
            }
            if (!plugin.canUseAnyCountryCommand(event.getPlayer()) && normalized.equals("country")) {
                event.getCommands().remove(command);
            }
        }
    }
}
