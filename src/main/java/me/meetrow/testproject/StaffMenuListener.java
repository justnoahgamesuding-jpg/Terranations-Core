package me.meetrow.testproject;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class StaffMenuListener implements Listener {
    private static final int MAIN_SIZE = 45;
    private static final int PLAYER_BROWSER_SIZE = 54;
    private static final int PLAYER_ACTION_SIZE = 45;
    private static final int[] PLAYER_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int[] FLY_SPEED_PRESETS = {1, 3, 5, 7, 10};

    private final Testproject plugin;

    public StaffMenuListener(Testproject plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        if (player == null) {
            return;
        }

        Inventory inventory = Bukkit.createInventory(
                new MainMenuHolder(player.getUniqueId()),
                MAIN_SIZE,
                plugin.legacyComponent("&8Staff Menu")
        );

        fillEmptySlots(inventory);
        inventory.setItem(4, createStatusItem(player));
        inventory.setItem(10, createToggleItem(Material.REDSTONE_TORCH, "&6Staff Mode",
                plugin.isInStaffMode(player.getUniqueId()),
                "Quick access creative moderation mode."));
        inventory.setItem(11, createToggleItem(Material.ENDER_EYE, "&bVanish",
                plugin.isVanished(player.getUniqueId()),
                "Hide yourself from regular players."));
        inventory.setItem(12, createToggleItem(Material.DIAMOND_ORE, "&3Ore Vision",
                plugin.isOreVisionEnabled(player.getUniqueId()),
                "Show nearby ores through blocks."));
        inventory.setItem(13, createToggleItem(Material.CLOCK, "&eCooldown Debug",
                plugin.isCooldownDebugEnabled(player.getUniqueId()),
                "Show break and place cooldown bars."));
        inventory.setItem(14, createActionItem(Material.PLAYER_HEAD, "&aPlayer Manager",
                List.of("&7Browse online players.", "&eClick to open.")));
        inventory.setItem(15, createActionItem(Material.LODESTONE, "&6Country Warps",
                List.of("&7Open the country warp admin GUI.", "&eClick to open.")));
        inventory.setItem(16, createActionItem(Material.COMPASS, "&dPlaytest Manager",
                List.of("&7Open the playtest control GUI.", "&eClick to open.")));
        inventory.setItem(19, createActionItem(Material.RESPAWN_ANCHOR, "&aSet World Spawn",
                buildSpawnLore(player)));
        inventory.setItem(20, createToggleItem(Material.IRON_SWORD, "&cHostile Mobs",
                plugin.areHostileMobSpawnsEnabled(),
                "Globally allow hostile mob spawning."));
        inventory.setItem(21, createToggleItem(Material.PHANTOM_MEMBRANE, "&5Phantoms",
                plugin.arePhantomsEnabled(),
                "Allow or block phantom spawns."));
        inventory.setItem(22, createActionItem(Material.GOLDEN_APPLE, "&aHeal Self",
                List.of("&7Restore health, hunger, and saturation.", "&eClick to use.")));
        inventory.setItem(23, createFlySpeedItem(player));
        inventory.setItem(24, createActionItem(Material.ANVIL, "&cReload Terra",
                List.of("&7Reload Terra config and runtime state.", "&cAdmin only.")));
        inventory.setItem(40, createActionItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        player.openInventory(inventory);
    }

    public void openPlayerBrowser(Player viewer, int page) {
        if (viewer == null) {
            return;
        }

        List<Player> players = getVisibleOnlinePlayers();
        int maxPage = Math.max(0, (players.size() - 1) / PLAYER_SLOTS.length);
        int normalizedPage = Math.max(0, Math.min(page, maxPage));

        Inventory inventory = Bukkit.createInventory(
                new PlayerBrowserMenuHolder(viewer.getUniqueId(), normalizedPage),
                PLAYER_BROWSER_SIZE,
                plugin.legacyComponent("&8Staff Players")
        );

        fillEmptySlots(inventory);
        int startIndex = normalizedPage * PLAYER_SLOTS.length;
        for (int i = 0; i < PLAYER_SLOTS.length; i++) {
            int playerIndex = startIndex + i;
            if (playerIndex >= players.size()) {
                break;
            }
            inventory.setItem(PLAYER_SLOTS[i], createPlayerEntry(players.get(playerIndex)));
        }

        inventory.setItem(45, createActionItem(Material.ARROW, "&ePrevious Page",
                List.of("&7Go to the previous page.")));
        inventory.setItem(49, createActionItem(Material.COMPASS, "&7Back",
                List.of("&7Return to the main staff menu.")));
        inventory.setItem(53, createActionItem(Material.ARROW, "&eNext Page",
                List.of("&7Go to the next page.")));
        viewer.openInventory(inventory);
    }

    public void openPlayerMenu(Player viewer, UUID targetId) {
        if (viewer == null || targetId == null) {
            return;
        }

        Player target = Bukkit.getPlayer(targetId);
        if (target == null) {
            viewer.sendMessage(plugin.getMessage("general.player-not-found"));
            openPlayerBrowser(viewer, 0);
            return;
        }

        Inventory inventory = Bukkit.createInventory(
                new PlayerActionMenuHolder(viewer.getUniqueId(), targetId),
                PLAYER_ACTION_SIZE,
                plugin.legacyComponent("&8Manage " + target.getName())
        );

        fillEmptySlots(inventory);
        inventory.setItem(4, createPlayerSummary(target));
        inventory.setItem(10, createActionItem(Material.ENDER_PEARL, "&bTeleport To",
                List.of("&7Teleport to this player.", "&eClick to use.")));
        inventory.setItem(11, createActionItem(Material.LEAD, "&6Bring Player",
                List.of("&7Summon this player to you.", "&eClick to use.")));
        inventory.setItem(12, createToggleItem(Material.PACKED_ICE, "&3Freeze",
                plugin.isFrozen(targetId),
                "Prevent this player from moving and acting."));
        inventory.setItem(13, createActionItem(Material.GOLDEN_APPLE, "&aHeal / Feed",
                List.of("&7Restore this player's health and hunger.", "&eClick to use.")));
        inventory.setItem(14, createActionItem(Material.LAVA_BUCKET, "&cClear Inventory",
                List.of("&7Remove all inventory and armor contents.", "&eClick to use.")));
        inventory.setItem(15, createActionItem(Material.TNT, "&4Reset Terra Data",
                List.of("&7Clear this player's Terra plugin data.", "&cAdmin only.")));
        inventory.setItem(28, createGameModeItem(GameMode.SURVIVAL, target.getGameMode() == GameMode.SURVIVAL));
        inventory.setItem(29, createGameModeItem(GameMode.CREATIVE, target.getGameMode() == GameMode.CREATIVE));
        inventory.setItem(30, createGameModeItem(GameMode.ADVENTURE, target.getGameMode() == GameMode.ADVENTURE));
        inventory.setItem(31, createGameModeItem(GameMode.SPECTATOR, target.getGameMode() == GameMode.SPECTATOR));
        inventory.setItem(36, createActionItem(Material.ARROW, "&7Back",
                List.of("&7Return to the player list.")));
        inventory.setItem(44, createActionItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        viewer.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof MainMenuHolder)
                && !(holder instanceof PlayerBrowserMenuHolder)
                && !(holder instanceof PlayerActionMenuHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }
        if (!plugin.canUseStaffMode(player)) {
            player.closeInventory();
            return;
        }

        if (holder instanceof MainMenuHolder mainMenuHolder) {
            if (!player.getUniqueId().equals(mainMenuHolder.viewerId())) {
                player.closeInventory();
                return;
            }
            handleMainMenuClick(player, event.getRawSlot(), event.isLeftClick());
            return;
        }

        if (holder instanceof PlayerBrowserMenuHolder browserMenuHolder) {
            if (!player.getUniqueId().equals(browserMenuHolder.viewerId())) {
                player.closeInventory();
                return;
            }
            handlePlayerBrowserClick(player, browserMenuHolder, event.getRawSlot());
            return;
        }

        PlayerActionMenuHolder playerActionMenuHolder = (PlayerActionMenuHolder) holder;
        if (!player.getUniqueId().equals(playerActionMenuHolder.viewerId())) {
            player.closeInventory();
            return;
        }
        handlePlayerActionClick(player, playerActionMenuHolder, event.getRawSlot());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof MainMenuHolder
                || event.getView().getTopInventory().getHolder() instanceof PlayerBrowserMenuHolder
                || event.getView().getTopInventory().getHolder() instanceof PlayerActionMenuHolder) {
            event.setCancelled(true);
        }
    }

    private void handleMainMenuClick(Player player, int slot, boolean leftClick) {
        switch (slot) {
            case 10 -> {
                boolean enabled = plugin.toggleStaffMode(player);
                player.sendMessage(plugin.getMessage(enabled ? "staff.enabled" : "staff.disabled"));
                openMainMenu(player);
            }
            case 11 -> {
                boolean vanished = plugin.toggleVanish(player);
                player.sendMessage(Component.text("Vanish " + (vanished ? "enabled." : "disabled."), NamedTextColor.GREEN));
                openMainMenu(player);
            }
            case 12 -> {
                boolean enabled = plugin.toggleOreVision(player);
                player.sendMessage(plugin.getMessage(enabled ? "terra.orevision.enabled" : "terra.orevision.disabled"));
                openMainMenu(player);
            }
            case 13 -> {
                boolean enabled = plugin.toggleCooldownDebug(player);
                player.sendMessage(plugin.getMessage(enabled ? "terra.cooldowndebug.enabled" : "terra.cooldowndebug.disabled"));
                openMainMenu(player);
            }
            case 14 -> openPlayerBrowser(player, 0);
            case 15 -> {
                if (!plugin.canUseCountryWarpAdmin(player)) {
                    sendNoAccess(player);
                    return;
                }
                plugin.openCountryWarpMenu(player, 0);
            }
            case 16 -> {
                if (!plugin.canUseAdminCommands(player)) {
                    sendNoAccess(player);
                    return;
                }
                plugin.openPlaytestMenu(player);
            }
            case 19 -> {
                if (!plugin.canUseAdminCommands(player)) {
                    sendNoAccess(player);
                    return;
                }
                plugin.setWorldSpawn(player.getLocation());
                player.sendMessage(plugin.getMessage("terra.setworldspawn.success", plugin.placeholders(
                        "world", player.getWorld().getName(),
                        "x", String.valueOf(player.getLocation().getBlockX()),
                        "y", String.valueOf(player.getLocation().getBlockY()),
                        "z", String.valueOf(player.getLocation().getBlockZ())
                )));
                openMainMenu(player);
            }
            case 20 -> {
                if (!plugin.canUseAdminCommands(player)) {
                    sendNoAccess(player);
                    return;
                }
                plugin.setHostileMobSpawnsEnabled(!plugin.areHostileMobSpawnsEnabled());
                player.sendMessage(Component.text("Hostile mob spawning "
                        + (plugin.areHostileMobSpawnsEnabled() ? "enabled." : "disabled."), NamedTextColor.GREEN));
                openMainMenu(player);
            }
            case 21 -> {
                if (!plugin.canUseAdminCommands(player)) {
                    sendNoAccess(player);
                    return;
                }
                plugin.setPhantomsEnabled(!plugin.arePhantomsEnabled());
                player.sendMessage(Component.text("Phantoms "
                        + (plugin.arePhantomsEnabled() ? "enabled." : "disabled."), NamedTextColor.GREEN));
                openMainMenu(player);
            }
            case 22 -> {
                plugin.healAndFeedPlayer(player);
                player.sendMessage(Component.text("You have been healed and fed.", NamedTextColor.GREEN));
                openMainMenu(player);
            }
            case 23 -> {
                int nextSpeed = cycleFlySpeed(player.getFlySpeed(), leftClick);
                player.setFlySpeed(nextSpeed / 10.0F);
                player.sendMessage(plugin.getMessage("terra.flyspeed.set", plugin.placeholders(
                        "speed", String.valueOf(nextSpeed)
                )));
                openMainMenu(player);
            }
            case 24 -> {
                if (!plugin.canUseAdminCommands(player)) {
                    sendNoAccess(player);
                    return;
                }
                player.closeInventory();
                plugin.reloadTerra();
                player.sendMessage(plugin.getMessage("terra.reload"));
            }
            case 40 -> player.closeInventory();
            default -> {
            }
        }
    }

    private void handlePlayerBrowserClick(Player player, PlayerBrowserMenuHolder holder, int slot) {
        if (slot == 45) {
            openPlayerBrowser(player, holder.page() - 1);
            return;
        }
        if (slot == 49) {
            openMainMenu(player);
            return;
        }
        if (slot == 53) {
            openPlayerBrowser(player, holder.page() + 1);
            return;
        }

        ItemStack item = player.getOpenInventory().getTopInventory().getItem(slot);
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        OfflinePlayer target = extractTarget(item);
        if (target == null) {
            return;
        }
        openPlayerMenu(player, target.getUniqueId());
    }

    private void handlePlayerActionClick(Player viewer, PlayerActionMenuHolder holder, int slot) {
        Player target = Bukkit.getPlayer(holder.targetId());
        if (target == null) {
            viewer.sendMessage(plugin.getMessage("general.player-not-found"));
            openPlayerBrowser(viewer, 0);
            return;
        }

        switch (slot) {
            case 10 -> {
                plugin.teleportPlayerToPlayer(viewer, target);
                viewer.sendMessage(Component.text("Teleported to " + target.getName() + ".", NamedTextColor.GREEN));
            }
            case 11 -> {
                plugin.summonPlayer(target, viewer);
                viewer.sendMessage(Component.text("Brought " + target.getName() + " to you.", NamedTextColor.GREEN));
                target.sendMessage(Component.text("You were summoned by staff.", NamedTextColor.YELLOW));
            }
            case 12 -> {
                boolean frozen = plugin.toggleFrozen(target.getUniqueId());
                viewer.sendMessage(Component.text((frozen ? "Froze " : "Unfroze ") + target.getName() + ".", NamedTextColor.GREEN));
                target.sendMessage(Component.text(
                        frozen ? "You have been frozen by staff." : "You are no longer frozen.",
                        frozen ? NamedTextColor.RED : NamedTextColor.GREEN
                ));
                openPlayerMenu(viewer, target.getUniqueId());
            }
            case 13 -> {
                plugin.healAndFeedPlayer(target);
                viewer.sendMessage(Component.text("Healed and fed " + target.getName() + ".", NamedTextColor.GREEN));
                target.sendMessage(Component.text("You have been healed and fed by staff.", NamedTextColor.GREEN));
            }
            case 14 -> {
                plugin.clearOnlinePlayerInventory(target);
                viewer.sendMessage(Component.text("Cleared " + target.getName() + "'s inventory.", NamedTextColor.GREEN));
                target.sendMessage(Component.text("Your inventory was cleared by staff.", NamedTextColor.RED));
            }
            case 15 -> {
                if (!plugin.canUseAdminCommands(viewer)) {
                    sendNoAccess(viewer);
                    return;
                }
                viewer.closeInventory();
                plugin.resetPlayerData(target);
                viewer.sendMessage(Component.text("Reset Terra data for " + target.getName() + ".", NamedTextColor.GREEN));
                target.sendMessage(Component.text("Your Terra plugin data was reset by staff.", NamedTextColor.RED));
            }
            case 28 -> setTargetGameMode(viewer, target, GameMode.SURVIVAL);
            case 29 -> setTargetGameMode(viewer, target, GameMode.CREATIVE);
            case 30 -> setTargetGameMode(viewer, target, GameMode.ADVENTURE);
            case 31 -> setTargetGameMode(viewer, target, GameMode.SPECTATOR);
            case 36 -> openPlayerBrowser(viewer, 0);
            case 44 -> viewer.closeInventory();
            default -> {
            }
        }
    }

    private void setTargetGameMode(Player viewer, Player target, GameMode gameMode) {
        target.setGameMode(gameMode);
        viewer.sendMessage(Component.text("Set " + target.getName() + " to " + formatGameMode(gameMode) + ".", NamedTextColor.GREEN));
        target.sendMessage(Component.text("Your game mode was set to " + formatGameMode(gameMode) + " by staff.", NamedTextColor.YELLOW));
        openPlayerMenu(viewer, target.getUniqueId());
    }

    private ItemStack createStatusItem(Player viewer) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Rank access: &f" + (plugin.canUseAdminCommands(viewer) ? "Admin" : "Staff"));
        lore.add("&7Staff mode: " + yesNo(plugin.isInStaffMode(viewer.getUniqueId())));
        lore.add("&7Vanished: " + yesNo(plugin.isVanished(viewer.getUniqueId())));
        lore.add("&7Frozen players online: &f" + countFrozenPlayers());
        Location spawn = plugin.getWorldSpawnLocation();
        lore.add("&7Spawn: &f" + (spawn != null
                ? spawn.getWorld().getName() + " " + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ()
                : "Default world spawn"));
        return createItem(Material.NETHER_STAR, "&6Staff Overview", lore);
    }

    private ItemStack createFlySpeedItem(Player player) {
        int speed = Math.round(player.getFlySpeed() * 10.0F);
        return createActionItem(Material.FEATHER, "&bFly Speed",
                List.of(
                        "&7Current speed: &f" + speed,
                        "",
                        "&eLeft click: next preset",
                        "&eRight click: previous preset"
                ));
    }

    private List<String> buildSpawnLore(Player player) {
        Location spawn = plugin.getWorldSpawnLocation();
        List<String> lore = new ArrayList<>();
        lore.add("&7Set the global spawn to your location.");
        lore.add("&cAdmin only.");
        lore.add("");
        if (spawn != null) {
            lore.add("&7Current: &f" + spawn.getWorld().getName());
            lore.add("&7Coords: &f" + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ());
        } else {
            lore.add("&7Current: &fDefault world spawn");
        }
        lore.add("");
        lore.add("&eClick to set here");
        lore.add("&7Your position: &f" + player.getLocation().getBlockX() + ", "
                + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ());
        return lore;
    }

    private ItemStack createPlayerEntry(Player target) {
        String countryName = plugin.getPlayerCountry(target.getUniqueId()) != null
                ? plugin.getPlayerCountry(target.getUniqueId()).getName()
                : "None";
        List<String> lore = new ArrayList<>();
        lore.add("&7World: &f" + target.getWorld().getName());
        lore.add("&7Game mode: &f" + formatGameMode(target.getGameMode()));
        lore.add("&7Country: &f" + countryName);
        lore.add("&7Vanished: &f" + yesNo(plugin.isVanished(target.getUniqueId())));
        lore.add("&7Frozen: &f" + yesNo(plugin.isFrozen(target.getUniqueId())));
        lore.add("");
        lore.add("&eClick to manage");

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(target);
            meta = skullMeta;
        }
        if (meta == null) {
            return item;
        }
        meta.displayName(plugin.legacyComponent("&a" + target.getName()));
        meta.lore(lore.stream().map(plugin::legacyComponent).toList());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPlayerSummary(Player target) {
        String countryName = plugin.getPlayerCountry(target.getUniqueId()) != null
                ? plugin.getPlayerCountry(target.getUniqueId()).getName()
                : "None";
        List<String> lore = List.of(
                "&7UUID: &f" + target.getUniqueId(),
                "&7World: &f" + target.getWorld().getName(),
                "&7Coords: &f" + target.getLocation().getBlockX() + ", "
                        + target.getLocation().getBlockY() + ", " + target.getLocation().getBlockZ(),
                "&7Country: &f" + countryName,
                "&7Game mode: &f" + formatGameMode(target.getGameMode()),
                "&7Vanished: &f" + yesNo(plugin.isVanished(target.getUniqueId())),
                "&7Frozen: &f" + yesNo(plugin.isFrozen(target.getUniqueId()))
        );
        return createPlayerEntryWithLore(target, "&6" + target.getName(), lore);
    }

    private ItemStack createGameModeItem(GameMode gameMode, boolean active) {
        Material material = switch (gameMode) {
            case SURVIVAL -> Material.IRON_PICKAXE;
            case CREATIVE -> Material.GRASS_BLOCK;
            case ADVENTURE -> Material.MAP;
            case SPECTATOR -> Material.ENDER_EYE;
        };
        return createItem(material,
                (active ? "&a" : "&e") + formatGameMode(gameMode),
                List.of(
                        "&7Set the target player to " + formatGameMode(gameMode) + ".",
                        active ? "&aCurrent mode" : "&eClick to apply"
                ));
    }

    private ItemStack createToggleItem(Material material, String title, boolean enabled, String description) {
        return createItem(material, title,
                List.of(
                        "&7" + description,
                        "",
                        "&7Current: " + (enabled ? "&aEnabled" : "&cDisabled"),
                        "&eClick to toggle"
                ));
    }

    private ItemStack createActionItem(Material material, String title, List<String> lore) {
        return createItem(material, title, lore);
    }

    private ItemStack createPlayerEntryWithLore(Player target, String displayName, List<String> loreLines) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(target);
            meta = skullMeta;
        }
        if (meta == null) {
            return item;
        }
        meta.displayName(plugin.legacyComponent(displayName));
        meta.lore(loreLines.stream().map(plugin::legacyComponent).toList());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material material, String displayName, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.displayName(plugin.legacyComponent(displayName));
        meta.lore(loreLines.stream().map(plugin::legacyComponent).toList());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.legacyComponent("&7"));
            filler.setItemMeta(meta);
        }

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
    }

    private OfflinePlayer extractTarget(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof SkullMeta skullMeta)) {
            return null;
        }
        return skullMeta.getOwningPlayer();
    }

    private List<Player> getVisibleOnlinePlayers() {
        List<Player> players = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            players.add(online);
        }
        players.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
        return players;
    }

    private int cycleFlySpeed(float currentSpeed, boolean forward) {
        int current = Math.round(currentSpeed * 10.0F);
        int index = 0;
        for (int i = 0; i < FLY_SPEED_PRESETS.length; i++) {
            if (FLY_SPEED_PRESETS[i] == current) {
                index = i;
                break;
            }
        }
        index = forward ? (index + 1) % FLY_SPEED_PRESETS.length : (index - 1 + FLY_SPEED_PRESETS.length) % FLY_SPEED_PRESETS.length;
        return FLY_SPEED_PRESETS[index];
    }

    private int countFrozenPlayers() {
        int count = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (plugin.isFrozen(online.getUniqueId())) {
                count++;
            }
        }
        return count;
    }

    private String yesNo(boolean value) {
        return value ? "&aYes" : "&cNo";
    }

    private String formatGameMode(GameMode gameMode) {
        return switch (gameMode) {
            case SURVIVAL -> "Survival";
            case CREATIVE -> "Creative";
            case ADVENTURE -> "Adventure";
            case SPECTATOR -> "Spectator";
        };
    }

    private void sendNoAccess(Player player) {
        player.sendMessage(Component.text("You do not have access to that staff action.", NamedTextColor.RED));
    }

    private record MainMenuHolder(UUID viewerId) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record PlayerBrowserMenuHolder(UUID viewerId, int page) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record PlayerActionMenuHolder(UUID viewerId, UUID targetId) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
