package me.meetrow.testproject;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public final class ClimateWeatherListener implements Listener {
    private final Testproject plugin;

    public ClimateWeatherListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (!event.toWeatherState()) {
            plugin.recordClimateRainEnded(event.getWorld());
        }
    }
}
