package me.meetrow.testproject;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public final class OnboardingNpcInteractionListener implements Listener {
    private final Testproject plugin;

    public OnboardingNpcInteractionListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (plugin.isTraderNpc(clicked) || plugin.isMerchantNpc(clicked)) {
            return;
        }
        if (plugin.getOnboardingNpcKey(clicked) == null) {
            return;
        }
        event.setCancelled(true);
        plugin.handleOnboardingNpcInteraction(event.getPlayer(), clicked);
    }
}
