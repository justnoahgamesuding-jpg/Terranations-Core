package me.meetrow.testproject;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExhaustionEvent;

public final class HungerRateListener implements Listener {
    private final Testproject plugin;

    public HungerRateListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExhaustion(EntityExhaustionEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        double climateMultiplier = plugin.getClimateHungerMultiplier(player.getLocation());
        float scaledExhaustion = (float) (event.getExhaustion()
                * plugin.getHungerSpeedMultiplier()
                * climateMultiplier
                * plugin.getProfessionExhaustionMultiplier(player.getUniqueId()));
        event.setExhaustion(Math.max(0.0F, scaledExhaustion));
    }
}
