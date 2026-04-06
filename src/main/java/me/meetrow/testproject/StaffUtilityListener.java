package me.meetrow.testproject;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public final class StaffUtilityListener implements Listener {
    private final Testproject plugin;

    public StaffUtilityListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.refreshVanishedViewFor(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isFrozen(player.getUniqueId()) || event.getTo() == null) {
            return;
        }
        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            return;
        }
        double distanceSquared = event.getFrom().distanceSquared(event.getTo());
        if (distanceSquared > 16.0D) {
            return;
        }
        if (event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (plugin.isFrozen(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(Component.text("You are frozen by staff.", NamedTextColor.RED));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.isFrozen(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(Component.text("You are frozen by staff.", NamedTextColor.RED));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.isFrozen(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(Component.text("You are frozen by staff.", NamedTextColor.RED));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (plugin.isFrozen(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(Component.text("You are frozen by staff.", NamedTextColor.RED));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickupArrow(PlayerPickupArrowEvent event) {
        if (plugin.isFrozen(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && plugin.isFrozen(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendActionBar(Component.text("You are frozen by staff.", NamedTextColor.RED));
            return;
        }
        if (event.getEntity() instanceof Player player && plugin.isFrozen(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isFrozen(player.getUniqueId())) {
            return;
        }
        String command = event.getMessage().toLowerCase();
        if (command.startsWith("/msg ")
                || command.startsWith("/tell ")
                || command.startsWith("/w ")
                || command.startsWith("/r ")
                || command.startsWith("/helpop ")) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(Component.text("You are frozen by staff and cannot use commands right now.", NamedTextColor.RED));
    }
}
