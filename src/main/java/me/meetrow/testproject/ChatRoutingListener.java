package me.meetrow.testproject;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ChatRoutingListener implements Listener {
    private static final double LOCAL_CHAT_RADIUS_BLOCKS = 50.0D;
    private final Testproject plugin;

    public ChatRoutingListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        String plainMessage = plugin.plainText(event.message()).trim();
        if (plainMessage.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        if (plugin.isCountryChatEnabled(sender.getUniqueId())) {
            plugin.sendCountryChat(sender, plainMessage);
            return;
        }

        if (plainMessage.startsWith("!")) {
            handleGlobalChat(sender, plainMessage.substring(1).trim());
            return;
        }

        plugin.sendLocalChat(sender, plainMessage, plugin.getChatLocalRadiusBlocks());
    }

    private void handleGlobalChat(Player sender, String message) {
        if (message.isEmpty()) {
            sender.sendMessage(plugin.getChatMessage("messages.global.prefix-empty"));
            return;
        }
        if (!plugin.isGlobalChatEnabled(sender.getUniqueId())) {
            sender.sendMessage(plugin.getChatMessage("messages.global.not-enabled"));
            return;
        }

        long remaining = plugin.getGlobalChatCooldownRemaining(sender.getUniqueId());
        if (remaining > 0L) {
            sender.sendMessage(plugin.getChatMessage("messages.global.cooldown", plugin.placeholders(
                    "time", plugin.formatLongDurationWords(remaining)
            )));
            return;
        }

        plugin.sendGlobalChat(sender, message);
    }
}
