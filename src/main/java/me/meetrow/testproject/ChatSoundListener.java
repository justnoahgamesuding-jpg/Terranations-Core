package me.meetrow.testproject;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ChatSoundListener implements Listener {
    private final Testproject plugin;

    public ChatSoundListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        if (!plugin.areChatSoundsEnabled()) {
            return;
        }

        Player sender = event.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.equals(sender) || plugin.isTutorialIntroActive(player.getUniqueId())) {
                    continue;
                }
                plugin.playChatMessageSound(player);
            }
        });
    }
}
