package me.meetrow.testproject;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class TutorialIntroListener implements Listener {
    private final Testproject plugin;

    public TutorialIntroListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        if (plugin.isTutorialIntroActive(sender.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        event.viewers().removeIf(viewer -> viewer instanceof Player player && plugin.isTutorialIntroActive(player.getUniqueId()));
    }
}
