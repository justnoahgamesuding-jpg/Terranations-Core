package me.meetrow.testproject;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TerraCommand implements CommandExecutor, TabCompleter {
    private static final int BLOCKS_PER_PAGE = 20;
    private static final int HELP_ENTRIES_PER_PAGE = 8;
    private static final DateTimeFormatter BYPASS_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final List<Double> XP_BOOST_AMOUNTS = List.of(1.5D, 2.0D, 2.5D, 5.0D, 10.0D);
    private static final Pattern PLAYTEST_DURATION_PATTERN = Pattern.compile("(\\d+)([ymwdhs])", Pattern.CASE_INSENSITIVE);

    private final Testproject plugin;

    public TerraCommand(Testproject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("trader")) {
            if (!hasAdminAccess(sender)) {
                sender.sendMessage(plugin.getMessage("permissions.terra-admin"));
                return true;
            }
            return handleTraderCommand(sender, withRootSubcommand("trader", args));
        }

        if (command.getName().equalsIgnoreCase("merchant")) {
            if (!hasAdminAccess(sender)) {
                sender.sendMessage(plugin.getMessage("permissions.terra-admin"));
                return true;
            }
            return handleMerchantCommand(sender, withRootSubcommand("merchant", args));
        }

        if (command.getName().equalsIgnoreCase("climate")) {
            if (!hasAdminAccess(sender)) {
                sender.sendMessage(plugin.getMessage("permissions.terra-admin"));
                return true;
            }
            return handleClimateCommand(sender, withRootSubcommand("climate", args));
        }

        if (command.getName().equalsIgnoreCase("jobs")) {
            return handleJobsCommand(sender, args);
        }

        if (command.getName().equalsIgnoreCase("rollbackarea")) {
            if (!hasAdminAccess(sender)) {
                sender.sendMessage(plugin.getMessage("permissions.terra-admin"));
                return true;
            }
            return handleRollbackAreaCommand(sender, args);
        }

        if (command.getName().equalsIgnoreCase("undoarea")) {
            if (!hasAdminAccess(sender)) {
                sender.sendMessage(plugin.getMessage("permissions.terra-admin"));
                return true;
            }
            return handleUndoAreaCommand(sender, args);
        }

        if (command.getName().equalsIgnoreCase("flyspeed")) {
            if (!hasStaffAccess(sender)) {
                sender.sendMessage(plugin.getMessage("staff.no-permission"));
                return true;
            }
            return handleFlySpeedCommand(sender, withRootSubcommand("flyspeed", args));
        }

        if (command.getName().equalsIgnoreCase("vanish")) {
            if (!hasStaffAccess(sender)) {
                sender.sendMessage(plugin.getMessage("staff.no-permission"));
                return true;
            }
            return handleVanishCommand(sender, withRootSubcommand("vanish", args));
        }

        if (args.length == 0) {
            sendUsage(sender, 1);
            return true;
        }

        if (args[0].equalsIgnoreCase("help")) {
            return handleHelpCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("tutorial")) {
            return handleTutorialCommand(sender, args);
        }

        if (!hasAdminAccess(sender)) {
            sender.sendMessage(plugin.getMessage("permissions.terra-admin"));
            return true;
        }

        if (args[0].equalsIgnoreCase("blockdelay")) {
            return handleBlockDelayCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("blockvalue")) {
            return handleBlockValueCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("bypass")) {
            return handleBypassCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("craftbypass")) {
            return handleBypassCommand(sender, withRootSubcommand("bypass", Arrays.copyOfRange(args, 1, args.length)));
        }

        if (args[0].equalsIgnoreCase("bypasslist")) {
            return handleBypassListCommand(sender);
        }

        if (args[0].equalsIgnoreCase("wildernessregen")) {
            return handleWildernessRegenCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("fixedore")) {
            return handleFixedOreCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("fixedoretool")) {
            return handleFixedOreToolCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("rewards")) {
            return handleRewardsCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("hostilemobs")) {
            return handleHostileMobsCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("phantoms")) {
            return handlePhantomsCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("setxpboost")) {
            return handleSetXpBoostCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("cleardata")) {
            return handleClearDataCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("jobcap")) {
            return handleJobCapCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("jobeditor")) {
            return handleJobEditorCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("playtest")) {
            return handlePlaytestCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("trader")) {
            return handleTraderCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("merchant")) {
            return handleMerchantCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("climate")) {
            return handleClimateCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("flyspeed")) {
            return handleFlySpeedCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("vanish")) {
            return handleVanishCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("orevision")) {
            return handleOreVisionCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("setworldspawn")) {
            return handleSetWorldSpawnCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("cooldowndebug")) {
            return handleCooldownDebugCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("realtimeclock")) {
            return handleRealTimeClockCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("hungerspeed")) {
            return handleHungerSpeedCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("stability")) {
            return handleStabilityCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("items")) {
            return handleItemsCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("lag")) {
            return handleLagCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("maintenance")) {
            return handleMaintenanceCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("clearinventory")) {
            return handleClearInventoryCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("catalyst")) {
            return handleCatalystCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("admincatalyst")) {
            return handleAdminCatalystCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("quests")) {
            return handleQuestsCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("catalog")) {
            return handleCatalogCommand(sender);
        }

        if (args[0].equalsIgnoreCase("guieditor")) {
            return handleGuiEditorCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("questdebug")) {
            return handleQuestDebugCommand(sender, args);
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadTerra();
            sender.sendMessage(plugin.getMessage("terra.reload"));
            return true;
        }

        if (args[0].equalsIgnoreCase("hardrestart")) {
            sender.sendMessage(plugin.getMessage("terra.hardrestart.started"));
            plugin.initiateHardRestart();
            return true;
        }

        sendUsage(sender, 1);
        return true;
    }

    private boolean handleHelpCommand(CommandSender sender, String[] args) {
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(Component.text("Page must be a whole number.", NamedTextColor.RED));
                return true;
            }
        }

        sendUsage(sender, page);
        return true;
    }

    private boolean handleQuestsCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can open the quest admin GUI.", NamedTextColor.RED));
            return true;
        }
        plugin.openQuestAdminMenu(player);
        return true;
    }

    private boolean handleCatalogCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can open the Terra catalog.", NamedTextColor.RED));
            return true;
        }
        plugin.openTerraCraftCatalog(player);
        return true;
    }

    private boolean handleGuiEditorCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can open the Terra GUI editor.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 1 || args[1].equalsIgnoreCase("list")) {
            sender.sendMessage(Component.text("Editable Terra screens: " + String.join(", ", plugin.getTerraGuiEditorScreens()), NamedTextColor.YELLOW));
            return true;
        }
        if (args[1].equalsIgnoreCase("reset")) {
            if (args.length != 3) {
                sender.sendMessage(Component.text("Usage: /terra guieditor reset <screen>", NamedTextColor.RED));
                return true;
            }
            if (!plugin.resetTerraGuiEditorScreen(args[2])) {
                sender.sendMessage(Component.text("Unknown Terra GUI screen. Use /terra guieditor list.", NamedTextColor.RED));
                return true;
            }
            sender.sendMessage(Component.text("Reset the saved layout for " + args[2] + ".", NamedTextColor.GREEN));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(Component.text("Usage: /terra guieditor <screen|list|reset>", NamedTextColor.RED));
            return true;
        }
        if (!plugin.openAnyTerraGuiEditor(player, args[1])) {
            sender.sendMessage(Component.text("Unknown Terra GUI screen. Use /terra guieditor list.", NamedTextColor.RED));
            return true;
        }
        return true;
    }

    private boolean handleQuestDebugCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use quest debug.", NamedTextColor.RED));
            return true;
        }
        UUID playerId = player.getUniqueId();
        sender.sendMessage(Component.text("Quest debug:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Active: " + plugin.hasActiveTutorialQuest(playerId), NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Quest ID: " + plugin.getTutorialQuestId(playerId), NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Title: " + plugin.getTutorialQuestTitle(playerId), NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Progress: " + plugin.getTutorialQuestProgressText(playerId), NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Percent: " + plugin.getTutorialQuestPercent(playerId), NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Profession: " + plugin.getTutorialQuestProfessionKey(playerId), NamedTextColor.YELLOW));
        return true;
    }

    private boolean handleClearInventoryCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Component.text("Usage: /terra clearinventory <player>", NamedTextColor.RED));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getMessage("general.player-not-found"));
            return true;
        }
        plugin.clearOnlinePlayerInventory(target);
        sender.sendMessage(Component.text("Cleared " + target.getName() + "'s inventory and kept the Terra Guide.", NamedTextColor.GREEN));
        if (sender != target) {
            target.sendMessage(Component.text("Your inventory was cleared. Your Terra Guide was kept.", NamedTextColor.RED));
        }
        return true;
    }

    private boolean handleCatalystCommand(CommandSender sender, String[] args) {
        if (args.length < 3 || args.length > 4) {
            sender.sendMessage(Component.text("Usage: /terra catalyst <player> <forge_shard|tempered_flux|binding_thread|runic_prism|ancient_core> [amount]", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getMessage("general.player-not-found"));
            return true;
        }

        String catalystKey = args[2].toLowerCase(Locale.ROOT);
        if (!List.of("forge_shard", "tempered_flux", "binding_thread", "runic_prism", "ancient_core").contains(catalystKey)) {
            sender.sendMessage(Component.text("Unknown catalyst. Use forge_shard, tempered_flux, binding_thread, runic_prism, or ancient_core.", NamedTextColor.RED));
            return true;
        }

        int amount = 1;
        if (args.length == 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(Component.text("Amount must be a whole number.", NamedTextColor.RED));
                return true;
            }
            if (amount <= 0) {
                sender.sendMessage(Component.text("Amount must be above 0.", NamedTextColor.RED));
                return true;
            }
        }

        Map<Integer, ItemStack> leftovers = target.getInventory().addItem(plugin.createRareContractMaterial(catalystKey, amount));
        for (ItemStack leftover : leftovers.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), leftover);
        }

        String catalystName = plugin.formatRareContractMaterialName(catalystKey);
        sender.sendMessage(Component.text("Gave " + target.getName() + " " + amount + "x " + catalystName + ".", NamedTextColor.GREEN));
        if (sender != target) {
            target.sendMessage(Component.text("You received " + amount + "x " + catalystName + ".", NamedTextColor.GOLD));
        }
        return true;
    }

    private boolean handleAdminCatalystCommand(CommandSender sender, String[] args) {
        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(Component.text("Usage: /terra admincatalyst <Meetrow> [amount]", NamedTextColor.RED));
            return true;
        }

        if (!args[1].equalsIgnoreCase("Meetrow")) {
            sender.sendMessage(Component.text("Admin Catalyst can only be given to Meetrow.", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact("Meetrow");
        if (target == null) {
            sender.sendMessage(Component.text("Meetrow must be online to receive the admin catalyst.", NamedTextColor.RED));
            return true;
        }

        int amount = 1;
        if (args.length == 3) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(Component.text("Amount must be a whole number.", NamedTextColor.RED));
                return true;
            }
            if (amount <= 0) {
                sender.sendMessage(Component.text("Amount must be above 0.", NamedTextColor.RED));
                return true;
            }
        }

        ItemStack catalyst = plugin.createRareContractMaterial("admin_merge_catalyst", amount);
        Map<Integer, ItemStack> leftovers = target.getInventory().addItem(catalyst);
        if (!leftovers.isEmpty()) {
            sender.sendMessage(Component.text("Meetrow needs enough free inventory space for the admin catalyst.", NamedTextColor.RED));
            return true;
        }

        sender.sendMessage(Component.text("Gave Meetrow " + amount + "x Admin Catalyst.", NamedTextColor.GREEN));
        if (sender != target) {
            target.sendMessage(Component.text("You received " + amount + "x Admin Catalyst.", NamedTextColor.GOLD));
        }
        return true;
    }

    private boolean handleJobsCommand(CommandSender sender, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("admin")) {
            if (!hasAdminAccess(sender)) {
                sender.sendMessage(plugin.getMessage("permissions.terra-admin"));
                return true;
            }
            return handleJobsAdminCommand(sender, args);
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("country.only-players"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("open")) {
            plugin.openProfessionMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            Profession current = plugin.getProfession(player.getUniqueId());
            Profession primary = plugin.getPrimaryProfession(player.getUniqueId());
            Profession secondary = plugin.getSecondaryProfession(player.getUniqueId());
            if (primary == null) {
                player.sendMessage(plugin.getMessage("profession.none"));
                return true;
            }

            player.sendMessage(plugin.getMessage("profession.info.header"));
            player.sendMessage(plugin.getMessage("profession.info.current", plugin.placeholders(
                    "profession", current != null ? plugin.getProfessionPlainDisplayName(current) : "None"
            )));
            player.sendMessage(plugin.getMessage("profession.info.primary", plugin.placeholders(
                    "profession", plugin.getProfessionPlainDisplayName(primary),
                    "level", String.valueOf(plugin.getProfessionLevel(player.getUniqueId(), primary))
            )));
            player.sendMessage(plugin.getMessage("profession.info.secondary", plugin.placeholders(
                    "profession", secondary != null ? plugin.getProfessionPlainDisplayName(secondary) : "None",
                    "level", secondary != null ? String.valueOf(plugin.getProfessionLevel(player.getUniqueId(), secondary)) : "0"
            )));
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("switch")) {
            Profession profession = Profession.fromKey(args[1]);
            if (profession == null) {
                player.sendMessage(plugin.getMessage("profession.unknown"));
                return true;
            }
            if (!plugin.hasProfession(player.getUniqueId(), profession)) {
                player.sendMessage(plugin.getMessage("profession.not-owned"));
                return true;
            }
            if (profession == plugin.getProfession(player.getUniqueId())) {
                player.sendMessage(plugin.getMessage("profession.already-active", plugin.placeholders(
                        "profession", plugin.getProfessionPlainDisplayName(profession)
                )));
                return true;
            }
            if (!plugin.switchActiveProfession(player.getUniqueId(), profession)) {
                player.sendMessage(plugin.getMessage("profession.not-owned"));
                return true;
            }
            player.sendMessage(plugin.getMessage("profession.switched", plugin.placeholders(
                    "profession", plugin.getProfessionPlainDisplayName(profession)
            )));
            return true;
        }

        player.sendMessage(plugin.getMessage("terra.jobs.usage"));
        return true;
    }

    private boolean handleRollbackAreaCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("terra.rollbackarea.player-only"));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(plugin.getMessage("terra.rollbackarea.usage"));
            return true;
        }

        int radius;
        try {
            radius = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            player.sendMessage(plugin.getMessage("terra.rollbackarea.invalid-radius"));
            return true;
        }

        if (radius < 1) {
            player.sendMessage(plugin.getMessage("terra.rollbackarea.invalid-radius"));
            return true;
        }

        if (Bukkit.getPluginManager().getPlugin("CoreProtect") == null) {
            player.sendMessage(plugin.getMessage("terra.rollbackarea.coreprotect-missing"));
            return true;
        }

        Long duration = parsePlaytestDurationMillis(args[1]);
        if (duration == null) {
            player.sendMessage(plugin.getMessage("terra.rollbackarea.invalid-time"));
            return true;
        }

        String normalizedTime = args[1].trim().toLowerCase(Locale.ROOT);
        boolean dispatched = player.performCommand("co rollback t:" + normalizedTime + " r:" + radius);
        if (!dispatched) {
            player.sendMessage(plugin.getMessage("terra.rollbackarea.dispatch-failed"));
            return true;
        }

        player.sendMessage(plugin.getMessage("terra.rollbackarea.started", plugin.placeholders(
                "radius", String.valueOf(radius),
                "time", normalizedTime
        )));
        return true;
    }

    private boolean handleUndoAreaCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("terra.undoarea.player-only"));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(plugin.getMessage("terra.undoarea.usage"));
            return true;
        }

        int radius;
        try {
            radius = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            player.sendMessage(plugin.getMessage("terra.undoarea.invalid-radius"));
            return true;
        }

        if (radius < 1) {
            player.sendMessage(plugin.getMessage("terra.undoarea.invalid-radius"));
            return true;
        }

        if (Bukkit.getPluginManager().getPlugin("CoreProtect") == null) {
            player.sendMessage(plugin.getMessage("terra.undoarea.coreprotect-missing"));
            return true;
        }

        Long duration = parsePlaytestDurationMillis(args[1]);
        if (duration == null) {
            player.sendMessage(plugin.getMessage("terra.undoarea.invalid-time"));
            return true;
        }

        String normalizedTime = args[1].trim().toLowerCase(Locale.ROOT);
        boolean dispatched = player.performCommand("co restore t:" + normalizedTime + " r:" + radius);
        if (!dispatched) {
            player.sendMessage(plugin.getMessage("terra.undoarea.dispatch-failed"));
            return true;
        }

        player.sendMessage(plugin.getMessage("terra.undoarea.started", plugin.placeholders(
                "radius", String.valueOf(radius),
                "time", normalizedTime
        )));
        return true;
    }

    private boolean handleTutorialCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (args.length == 2 && args[1].equalsIgnoreCase("next")) {
            plugin.advanceTutorialIntro(player);
            return true;
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("redo")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            OfflinePlayer target = findPlayer(args[2]);
            if (target == null) {
                player.sendMessage(plugin.getMessage("general.player-not-found"));
                return true;
            }
            String targetName = target.getName() != null ? target.getName() : args[2];
            if (plugin.restartPlayerTutorial(target)) {
                player.sendMessage(plugin.colorize("&aRestarted the tutorial for &f" + targetName + "&a."));
                if (target.isOnline() && target.getPlayer() != null && target.getPlayer() != player) {
                    target.getPlayer().sendMessage(plugin.colorize("&eYour tutorial has been restarted by staff."));
                }
            } else {
                player.sendMessage(plugin.colorize("&cCould not restart that player's tutorial."));
            }
            return true;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("setlocation")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            if (args.length < 3) {
                player.sendMessage(plugin.colorize("&cUsage: /terra tutorial setlocation <key> [radius] [display name]"));
                return true;
            }
            String key = args[2];
            double radius = 8.0D;
            int displayNameStart = 3;
            if (args.length >= 4) {
                try {
                    radius = Double.parseDouble(args[3]);
                    displayNameStart = 4;
                } catch (NumberFormatException ignored) {
                }
            }
            String displayName = args.length > displayNameStart
                    ? String.join(" ", Arrays.copyOfRange(args, displayNameStart, args.length))
                    : key;
            if (plugin.setOnboardingLocationMarker(key, player.getLocation(), radius, displayName)) {
                player.sendMessage(plugin.colorize("&aSaved onboarding location marker &f" + key + "&a with radius &f" + String.format(Locale.US, "%.1f", radius) + "&a."));
            } else {
                player.sendMessage(plugin.colorize("&cCould not save that onboarding location marker."));
            }
            return true;
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("clearlocation")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            if (plugin.removeOnboardingLocationMarker(args[2])) {
                player.sendMessage(plugin.colorize("&aRemoved onboarding location marker &f" + args[2] + "&a."));
            } else {
                player.sendMessage(plugin.colorize("&cNo onboarding location marker exists for &f" + args[2] + "&c."));
            }
            return true;
        }
        if (args.length >= 3 && args[1].equalsIgnoreCase("marknpc")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            Entity target = player.getTargetEntity(8);
            if (target == null) {
                player.sendMessage(plugin.colorize("&cLook at an entity within 8 blocks first."));
                return true;
            }
            if (plugin.markOnboardingNpc(target, args[2])) {
                player.sendMessage(plugin.colorize("&aMarked that NPC with onboarding key &f" + args[2] + "&a."));
            } else {
                player.sendMessage(plugin.colorize("&cCould not mark that NPC."));
            }
            return true;
        }
        if (args.length >= 5 && args[1].equalsIgnoreCase("spawnnpc")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            String npcId = args[2];
            String questKey = args[3];
            String itemsAdderEntityId = args[4];
            String dialogueKey = args.length >= 6 && !args[5].equals("-") ? args[5] : null;
            int displayNameStart = args.length >= 6 ? 6 : 5;
            String displayName = args.length > displayNameStart
                    ? String.join(" ", Arrays.copyOfRange(args, displayNameStart, args.length))
                    : npcId;
            if (plugin.spawnOnboardingCustomNpc(npcId, questKey, dialogueKey, itemsAdderEntityId, player.getLocation(), displayName)) {
                player.sendMessage(plugin.colorize("&aSpawned onboarding NPC &f" + npcId + "&a using ItemsAdder entity &f" + itemsAdderEntityId + "&a."));
            } else {
                player.sendMessage(plugin.colorize("&cCould not spawn that ItemsAdder onboarding NPC. Check the entity id and that ItemsAdder is loaded."));
            }
            return true;
        }
        if (args.length >= 5 && args[1].equalsIgnoreCase("registernpc")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            Entity target = player.getTargetEntity(8);
            if (target == null) {
                player.sendMessage(plugin.colorize("&cLook at an entity within 8 blocks first."));
                return true;
            }
            String npcId = args[2];
            String questKey = args[3];
            String itemsAdderEntityId = args[4];
            String dialogueKey = args.length >= 6 && !args[5].equals("-") ? args[5] : null;
            int displayNameStart = args.length >= 6 ? 6 : 5;
            String displayName = args.length > displayNameStart
                    ? String.join(" ", Arrays.copyOfRange(args, displayNameStart, args.length))
                    : npcId;
            if (plugin.registerExistingOnboardingCustomNpc(target, npcId, questKey, dialogueKey, itemsAdderEntityId, displayName)) {
                player.sendMessage(plugin.colorize("&aRegistered that entity as onboarding NPC &f" + npcId + "&a."));
            } else {
                player.sendMessage(plugin.colorize("&cCould not register that entity as an onboarding NPC."));
            }
            return true;
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("removenpc")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            if (plugin.removeOnboardingCustomNpc(args[2])) {
                player.sendMessage(plugin.colorize("&aRemoved onboarding NPC &f" + args[2] + "&a."));
            } else {
                player.sendMessage(plugin.colorize("&cNo custom onboarding NPC exists for &f" + args[2] + "&c."));
            }
            return true;
        }
        if (args.length >= 4 && args[1].equalsIgnoreCase("renamenpc")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            String npcId = args[2];
            String displayName = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            if (plugin.renameOnboardingCustomNpc(npcId, displayName)) {
                player.sendMessage(plugin.colorize("&aRenamed onboarding NPC &f" + npcId + "&a to &f" + displayName + "&a."));
            } else {
                player.sendMessage(plugin.colorize("&cCould not rename that onboarding NPC."));
            }
            return true;
        }
        if (args.length == 2 && args[1].equalsIgnoreCase("clearnpc")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            Entity target = player.getTargetEntity(8);
            if (target == null) {
                player.sendMessage(plugin.colorize("&cLook at an entity within 8 blocks first."));
                return true;
            }
            if (plugin.clearOnboardingNpc(target)) {
                player.sendMessage(plugin.colorize("&aCleared that onboarding NPC marker."));
            } else {
                player.sendMessage(plugin.colorize("&cThat entity does not have an onboarding NPC marker."));
            }
            return true;
        }
        if (args.length == 2 && args[1].equalsIgnoreCase("locations")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            List<String> keys = plugin.getOnboardingLocationMarkerKeys();
            player.sendMessage(plugin.colorize("&6Onboarding location markers: &f" + (keys.isEmpty() ? "none" : String.join(", ", keys))));
            return true;
        }
        if (args.length == 2 && args[1].equalsIgnoreCase("npcs")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            List<String> ids = plugin.getOnboardingCustomNpcIds();
            player.sendMessage(plugin.colorize("&6Onboarding custom NPCs: &f" + (ids.isEmpty() ? "none" : String.join(", ", ids))));
            return true;
        }
        if (args.length >= 4 && args[1].equalsIgnoreCase("bindfancynpc")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            String fancyNpcId = args[2];
            String questKey = args[3];
            String dialogueKey = args.length >= 5 && !args[4].equals("-") ? args[4] : null;
            int displayNameStart = args.length >= 5 ? 5 : 4;
            String displayName = args.length > displayNameStart
                    ? String.join(" ", Arrays.copyOfRange(args, displayNameStart, args.length))
                    : fancyNpcId;
            if (plugin.bindOnboardingFancyNpc(fancyNpcId, questKey, dialogueKey, displayName)) {
                player.sendMessage(plugin.colorize("&aBound FancyNpcs NPC &f" + fancyNpcId + "&a to onboarding key &f" + questKey + "&a."));
            } else {
                player.sendMessage(plugin.colorize("&cCould not bind that FancyNpcs NPC. Check that the NPC exists and FancyNpcs is loaded."));
            }
            return true;
        }
        if (args.length >= 4 && args[1].equalsIgnoreCase("spawnfancynpc")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            String fancyNpcId = args[2];
            String questKey = args[3];
            String dialogueKey = args.length >= 5 && !args[4].equals("-") ? args[4] : null;
            int displayNameStart = args.length >= 5 ? 5 : 4;
            String displayName = args.length > displayNameStart
                    ? String.join(" ", Arrays.copyOfRange(args, displayNameStart, args.length))
                    : fancyNpcId;
            if (plugin.spawnAndBindOnboardingFancyNpc(player, fancyNpcId, questKey, dialogueKey, displayName, player.getLocation())) {
                player.sendMessage(plugin.colorize("&aSpawned and bound FancyNpcs NPC &f" + fancyNpcId + "&a to onboarding key &f" + questKey + "&a."));
            } else {
                player.sendMessage(plugin.colorize("&cCould not spawn that FancyNpcs onboarding NPC. Check that FancyNpcs is loaded and the NPC id is not already in use."));
            }
            return true;
        }
        if (args.length >= 4 && args[1].equalsIgnoreCase("skinfancynpc")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            String fancyNpcId = args[2];
            String skinName = args[3];
            if (plugin.setOnboardingFancyNpcSkin(fancyNpcId, skinName)) {
                player.sendMessage(plugin.colorize("&aUpdated FancyNpcs skin for &f" + fancyNpcId + "&a to &f" + skinName + "&a."));
            } else {
                player.sendMessage(plugin.colorize("&cCould not update that FancyNpcs skin. Check that the NPC exists, FancyNpcs is loaded, and the skin identifier is valid."));
            }
            return true;
        }
        if (args.length >= 4 && args[1].equalsIgnoreCase("renamefancynpc")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            String fancyNpcId = args[2];
            String displayName = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            if (plugin.renameOnboardingFancyNpc(fancyNpcId, displayName)) {
                player.sendMessage(plugin.colorize("&aRenamed FancyNpcs NPC &f" + fancyNpcId + "&a to &f" + displayName + "&a."));
            } else {
                player.sendMessage(plugin.colorize("&cCould not rename that FancyNpcs NPC."));
            }
            return true;
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("unbindfancynpc")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            if (plugin.unbindOnboardingFancyNpc(args[2])) {
                player.sendMessage(plugin.colorize("&aRemoved FancyNpcs onboarding binding for &f" + args[2] + "&a."));
            } else {
                player.sendMessage(plugin.colorize("&cNo FancyNpcs onboarding binding exists for &f" + args[2] + "&c."));
            }
            return true;
        }
        if (args.length == 2 && args[1].equalsIgnoreCase("fancynpcs")) {
            if (!player.hasPermission(Testproject.ADMIN_PERMISSION) && !player.isOp()) {
                player.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            List<String> available = plugin.getAvailableFancyNpcIds();
            List<String> bound = plugin.getBoundFancyNpcIds();
            player.sendMessage(plugin.colorize("&6FancyNpcs available: &f" + (available.isEmpty() ? "none" : String.join(", ", available))));
            player.sendMessage(plugin.colorize("&6FancyNpcs bound to onboarding: &f" + (bound.isEmpty() ? "none" : String.join(", ", bound))));
            return true;
        }
        return true;
    }

    private boolean handleJobsAdminCommand(CommandSender sender, String[] args) {
        if (args.length == 2) {
            OfflinePlayer target = findPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.getMessage("general.player-not-found"));
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessage("terra.jobs.admin-player-only"));
                return true;
            }
            plugin.openProfessionAdminMenu(player, target);
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(plugin.getMessage("terra.jobs.admin-usage"));
            return true;
        }

        String action = args[1].toLowerCase(Locale.ROOT);

        OfflinePlayer target = findPlayer(args[2]);
        if (target == null) {
            sender.sendMessage(plugin.getMessage("general.player-not-found"));
            return true;
        }

        if (action.equals("info")) {
            Profession current = plugin.getProfession(target.getUniqueId());
            Profession primary = plugin.getPrimaryProfession(target.getUniqueId());
            Profession secondary = plugin.getSecondaryProfession(target.getUniqueId());
            sender.sendMessage(plugin.getMessage("profession.admin.info-header", plugin.placeholders("player", target.getName() != null ? target.getName() : args[2])));
            sender.sendMessage(plugin.getMessage("profession.admin.info-current", plugin.placeholders(
                    "profession", current != null ? plugin.getProfessionPlainDisplayName(current) : "None"
            )));
            sender.sendMessage(plugin.getMessage("profession.admin.info-primary", plugin.placeholders(
                    "profession", primary != null ? plugin.getProfessionPlainDisplayName(primary) : "None",
                    "level", primary != null ? String.valueOf(plugin.getProfessionLevel(target.getUniqueId(), primary)) : "0"
            )));
            sender.sendMessage(plugin.getMessage("profession.admin.info-secondary", plugin.placeholders(
                    "profession", secondary != null ? plugin.getProfessionPlainDisplayName(secondary) : "None",
                    "level", secondary != null ? String.valueOf(plugin.getProfessionLevel(target.getUniqueId(), secondary)) : "0"
            )));
            return true;
        }

        if (action.equals("clearsecondary")) {
            plugin.adminClearSecondaryProfession(target.getUniqueId());
            sender.sendMessage(plugin.getMessage("profession.admin.cleared-secondary", plugin.placeholders(
                    "player", target.getName() != null ? target.getName() : args[2]
            )));
            return true;
        }

        if (action.equals("clearall")) {
            plugin.adminClearAllProfessions(target.getUniqueId());
            sender.sendMessage(plugin.getMessage("profession.admin.cleared-all", plugin.placeholders(
                    "player", target.getName() != null ? target.getName() : args[2]
            )));
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(plugin.getMessage("terra.jobs.admin-usage"));
            return true;
        }

        Profession profession = Profession.fromKey(args[3]);
        if (profession == null) {
            sender.sendMessage(plugin.getMessage("profession.unknown"));
            return true;
        }

        if (action.equals("setlevel") || action.equals("setxp") || action.equals("addskillpoints")) {
            if (args.length < 5) {
                sender.sendMessage(plugin.getMessage("terra.jobs.admin-usage"));
                return true;
            }

            int value;
            try {
                value = Integer.parseInt(args[4]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(plugin.getMessage(
                        action.equals("setlevel") ? "terra.jobs.invalid-level" : "terra.jobs.invalid-xp"));
                return true;
            }

            if (action.equals("setlevel")) {
                if (value < 1) {
                    sender.sendMessage(plugin.getMessage("terra.jobs.invalid-level-min"));
                    return true;
                }
                if (!plugin.adminSetProfessionLevel(target.getUniqueId(), profession, value)) {
                    sender.sendMessage(plugin.getMessage("profession.not-owned"));
                    return true;
                }
                sender.sendMessage(plugin.getMessage("profession.admin.set-level", plugin.placeholders(
                        "player", target.getName() != null ? target.getName() : args[2],
                        "profession", plugin.getProfessionPlainDisplayName(profession),
                        "level", String.valueOf(plugin.getProfessionLevel(target.getUniqueId(), profession))
                )));
                return true;
            }

            if (value < 0) {
                sender.sendMessage(plugin.getMessage("terra.jobs.invalid-xp-min"));
                return true;
            }
            if (action.equals("addskillpoints")) {
                if (!plugin.adminAddProfessionSkillPoints(target.getUniqueId(), profession, value)) {
                    sender.sendMessage(plugin.getMessage("profession.not-owned"));
                    return true;
                }
                sender.sendMessage(Component.text(
                        "Added " + value + " skill point(s) to " + plugin.safeOfflineName(target)
                                + " for " + plugin.getProfessionPlainDisplayName(profession)
                                + ". Total bonus: " + plugin.getProfessionSkillPointBonus(target.getUniqueId(), profession),
                        NamedTextColor.GREEN
                ));
                return true;
            }
            if (!plugin.adminSetProfessionXp(target.getUniqueId(), profession, value)) {
                sender.sendMessage(plugin.getMessage("profession.not-owned"));
                return true;
            }
            sender.sendMessage(plugin.getMessage("profession.admin.set-xp", plugin.placeholders(
                    "player", target.getName() != null ? target.getName() : args[2],
                    "profession", plugin.getProfessionPlainDisplayName(profession),
                    "xp", String.valueOf(plugin.getProfessionXp(target.getUniqueId(), profession))
            )));
            return true;
        }

        switch (action) {
            case "setprimary" -> {
                if (!plugin.adminSetPrimaryProfession(target.getUniqueId(), profession)) {
                    sender.sendMessage(plugin.getMessage("profession.job-full", plugin.placeholders(
                            "profession", plugin.getProfessionPlainDisplayName(profession),
                            "cap", String.valueOf(plugin.getProfessionPlayerCap(profession))
                    )));
                    return true;
                }
                plugin.switchActiveProfession(target.getUniqueId(), profession);
                sender.sendMessage(plugin.getMessage("profession.admin.set-primary", plugin.placeholders(
                        "player", target.getName() != null ? target.getName() : args[2],
                        "profession", plugin.getProfessionPlainDisplayName(profession)
                )));
                return true;
            }
            case "setsecondary" -> {
                if (plugin.getPrimaryProfession(target.getUniqueId()) == null) {
                    sender.sendMessage(plugin.getMessage("profession.admin.primary-required"));
                    return true;
                }
                if (!plugin.adminSetSecondaryProfession(target.getUniqueId(), profession)) {
                    sender.sendMessage(plugin.getMessage("profession.job-full", plugin.placeholders(
                            "profession", plugin.getProfessionPlainDisplayName(profession),
                            "cap", String.valueOf(plugin.getProfessionPlayerCap(profession))
                    )));
                    return true;
                }
                sender.sendMessage(plugin.getMessage("profession.admin.set-secondary", plugin.placeholders(
                        "player", target.getName() != null ? target.getName() : args[2],
                        "profession", plugin.getProfessionPlainDisplayName(profession)
                )));
                return true;
            }
            case "setactive" -> {
                if (!plugin.hasProfession(target.getUniqueId(), profession)) {
                    sender.sendMessage(plugin.getMessage("profession.not-owned"));
                    return true;
                }
                plugin.switchActiveProfession(target.getUniqueId(), profession);
                sender.sendMessage(plugin.getMessage("profession.admin.set-active", plugin.placeholders(
                        "player", target.getName() != null ? target.getName() : args[2],
                        "profession", plugin.getProfessionPlainDisplayName(profession)
                )));
                return true;
            }
            default -> {
                sender.sendMessage(plugin.getMessage("terra.jobs.admin-usage"));
                return true;
            }
        }
    }

    private boolean handleBlockDelayCommand(CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("enable")) {
                plugin.setBlockDelayEnabled(true);
                sender.sendMessage(plugin.getMessage("terra.blockdelay.enabled"));
                return true;
            }
            if (args[1].equalsIgnoreCase("disable")) {
                plugin.setBlockDelayEnabled(false);
                sender.sendMessage(plugin.getMessage("terra.blockdelay.disabled"));
                return true;
            }
        }

        if (args.length == 4 && args[1].equalsIgnoreCase("time") && args[2].equalsIgnoreCase("set")) {
            int seconds;
            try {
                seconds = Integer.parseInt(args[3]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(plugin.getMessage("terra.blockdelay.invalid-seconds"));
                return true;
            }

            if (seconds < 0) {
                sender.sendMessage(plugin.getMessage("terra.blockdelay.invalid-nonnegative"));
                return true;
            }

            plugin.setBlockDelaySeconds(seconds);
            sender.sendMessage(plugin.getMessage("terra.blockdelay.time-set", plugin.placeholders("seconds", String.valueOf(seconds))));
            return true;
        }

        for (String line : plugin.getMessageList("help.terra.blockdelay")) {
            sender.sendMessage(line);
        }
        return true;
    }

    private boolean handleBlockValueCommand(CommandSender sender, String[] args) {
        if (args.length == 2 && args[1].equalsIgnoreCase("list")) {
            sendBlockList(sender, 1);
            return true;
        }

        if (args.length == 3 && args[1].equalsIgnoreCase("list")) {
            int page;
            try {
                page = Integer.parseInt(args[2]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(plugin.getMessage("terra.blockvalue.invalid-page"));
                return true;
            }
            sendBlockList(sender, page);
            return true;
        }

        if (args.length == 2) {
            Material material = parseBlockMaterial(args[1]);
            if (material == null) {
                sender.sendMessage(plugin.getMessage("terra.blockvalue.unknown-block"));
                return true;
            }

            BlockReward reward = plugin.getBlockReward(material);
            sender.sendMessage(plugin.getMessage("terra.blockvalue.show", plugin.placeholders(
                    "block", formatMaterialName(material),
                    "xp", String.valueOf(reward.xp()),
                    "money", String.format("%.2f", reward.money())
            )));
            return true;
        }

        if (args.length == 5 && args[2].equalsIgnoreCase("set")) {
            Material material = parseBlockMaterial(args[1]);
            if (material == null) {
                sender.sendMessage(plugin.getMessage("terra.blockvalue.unknown-block"));
                return true;
            }

            BlockReward currentReward = plugin.getBlockReward(material);
            if (args[3].equalsIgnoreCase("xp")) {
                int xp;
                try {
                    xp = Integer.parseInt(args[4]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(plugin.getMessage("terra.blockvalue.invalid-xp"));
                    return true;
                }
                if (xp < 0) {
                    sender.sendMessage(plugin.getMessage("terra.blockdelay.invalid-nonnegative"));
                    return true;
                }
                plugin.setBlockReward(material, xp, currentReward.money());
                sender.sendMessage(plugin.getMessage("terra.blockvalue.set-xp",
                        plugin.placeholders("block", formatMaterialName(material), "xp", String.valueOf(xp))));
                return true;
            }

            if (args[3].equalsIgnoreCase("money")) {
                double money;
                try {
                    money = Double.parseDouble(args[4]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(plugin.getMessage("terra.blockvalue.invalid-money"));
                    return true;
                }
                if (money < 0.0D) {
                    sender.sendMessage(plugin.getMessage("terra.blockdelay.invalid-nonnegative"));
                    return true;
                }
                plugin.setBlockReward(material, currentReward.xp(), money);
                sender.sendMessage(plugin.getMessage("terra.blockvalue.set-money",
                        plugin.placeholders("block", formatMaterialName(material), "money", String.format("%.2f", money))));
                return true;
            }

            sender.sendMessage(plugin.getMessage("terra.blockvalue.use-xp-or-money"));
            return true;
        }

        for (String line : plugin.getMessageList("help.terra.blockvalue")) {
            sender.sendMessage(line);
        }
        return true;
    }

    private boolean handleBypassCommand(CommandSender sender, String[] args) {
        OfflinePlayer target;
        String targetName;
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessage("terra.bypass.usage"));
                return true;
            }
            target = player;
            targetName = player.getName();
        } else if (args.length == 2) {
            target = findPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.getMessage("general.player-not-found"));
                return true;
            }
            targetName = target.getName() != null ? target.getName() : args[1];
        } else {
            sender.sendMessage(plugin.getMessage("terra.bypass.usage"));
            return true;
        }

        boolean enable = !plugin.hasBlockDelayBypass(target.getUniqueId());
        plugin.setBlockDelayBypass(target, enable);
        sender.sendMessage(plugin.getMessage(enable ? "terra.bypass.enabled" : "terra.bypass.disabled",
                plugin.placeholders("player", targetName)));

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(plugin.getMessage(enable ? "terra.bypass.target-enabled" : "terra.bypass.target-disabled"));
        }
        return true;
    }

    private boolean handleBypassListCommand(CommandSender sender) {
        Map<UUID, BypassEntry> bypassEntries = plugin.getBypassEntries();
        if (bypassEntries.isEmpty()) {
            sender.sendMessage(plugin.getMessage("terra.bypass.list-empty"));
            return true;
        }

        sender.sendMessage(plugin.getMessage("terra.bypass.list-header"));
        for (BypassEntry entry : bypassEntries.values()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.playerId());
            String name = player.getName() != null ? player.getName() : entry.lastKnownName();
            String enabledAt = BYPASS_DATE_FORMAT.format(entry.enabledAt());
            String prefix = plugin.getHighestPrefix(player);
            sender.sendMessage(plugin.getMessage("terra.bypass.list-entry",
                    plugin.placeholders("player", name, "since", enabledAt, "prefix", prefix)));
        }
        return true;
    }

    private boolean handleWildernessRegenCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(plugin.getMessage("terra.wildernessregen.show", plugin.placeholders(
                    "break", String.valueOf(plugin.getWildernessRegenerationDelaySeconds()),
                    "build", String.valueOf(plugin.getWildernessBuildDecayDelaySeconds())
            )));
            return true;
        }

        if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
            long seconds;
            try {
                seconds = Long.parseLong(args[3]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(plugin.getMessage("terra.wildernessregen.invalid-seconds"));
                return true;
            }

            if (seconds < 1L) {
                sender.sendMessage(plugin.getMessage("terra.wildernessregen.invalid-min"));
                return true;
            }

            if (args[2].equalsIgnoreCase("break")) {
                plugin.setWildernessRegenerationDelaySeconds(seconds);
                sender.sendMessage(plugin.getMessage("terra.wildernessregen.set-break",
                        plugin.placeholders("seconds", String.valueOf(seconds))));
                return true;
            }

            if (args[2].equalsIgnoreCase("build")) {
                plugin.setWildernessBuildDecayDelaySeconds(seconds);
                sender.sendMessage(plugin.getMessage("terra.wildernessregen.set-build",
                        plugin.placeholders("seconds", String.valueOf(seconds))));
                return true;
            }

            sender.sendMessage(plugin.getMessage("terra.wildernessregen.use-break-or-build"));
            return true;
        }

        for (String line : plugin.getMessageList("help.terra.wildernessregen")) {
            sender.sendMessage(line);
        }
        return true;
    }

    private boolean handleFixedOreCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("country.only-players"));
            return true;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("delete")) {
            Block targetBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if (!plugin.deleteFixedOre(targetBlock)) {
                sender.sendMessage(plugin.getMessage("terra.fixedore.not-found"));
                return true;
            }
            sender.sendMessage(plugin.getMessage("terra.fixedore.deleted"));
            return true;
        }

        if (args.length == 4 && args[1].equalsIgnoreCase("fill")) {
            Material sourceType = Material.matchMaterial(args[2]);
            if (sourceType == null || !sourceType.isBlock()) {
                sender.sendMessage(plugin.getMessage("terra.fixedore.invalid-source"));
                return true;
            }

            Material oreType = Material.matchMaterial(args[3]);
            if (!plugin.isFixedOreMaterial(oreType)) {
                sender.sendMessage(plugin.getMessage("terra.fixedore.invalid-ore"));
                return true;
            }

            WorldEditPlugin worldEdit = getWorldEditPlugin();
            if (worldEdit == null) {
                sender.sendMessage(plugin.getMessage("terra.fixedore.worldedit-unavailable"));
                return true;
            }

            Region region;
            try {
                LocalSession session = worldEdit.getSession(player);
                region = session.getSelection(BukkitAdapter.adapt(player.getWorld()));
            } catch (IncompleteRegionException exception) {
                sender.sendMessage(plugin.getMessage("terra.fixedore.selection-required"));
                return true;
            }

            List<Block> matchingBlocks = new ArrayList<>();
            for (BlockVector3 point : region) {
                Block block = player.getWorld().getBlockAt(point.x(), point.y(), point.z());
                if (block.getType() == sourceType) {
                    matchingBlocks.add(block);
                }
            }

            int createdCount = plugin.createFixedOres(matchingBlocks, oreType);
            if (createdCount <= 0) {
                sender.sendMessage(plugin.getMessage("terra.fixedore.no-matching-blocks", plugin.placeholders(
                        "source", plugin.formatMaterialName(sourceType)
                )));
                return true;
            }

            sender.sendMessage(plugin.getMessage("terra.fixedore.created-selection", plugin.placeholders(
                    "count", String.valueOf(createdCount),
                    "source", plugin.formatMaterialName(sourceType),
                    "block", plugin.formatMaterialName(oreType)
            )));
            return true;
        }

        if (args.length != 3 || !args[1].equalsIgnoreCase("create")) {
            sender.sendMessage(plugin.getMessage("terra.fixedore.usage"));
            return true;
        }

        Material oreType = Material.matchMaterial(args[2]);
        if (!plugin.isFixedOreMaterial(oreType)) {
            sender.sendMessage(plugin.getMessage("terra.fixedore.invalid-ore"));
            return true;
        }

        Block targetBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        plugin.createFixedOre(targetBlock, oreType);
        sender.sendMessage(plugin.getMessage("terra.fixedore.created", plugin.placeholders(
                "block", plugin.formatMaterialName(oreType)
        )));
        return true;
    }

    private boolean handleFixedOreToolCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("terra.fixedore.tool-player-only"));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(plugin.getMessage("terra.fixedore.tool-usage"));
            return true;
        }

        ItemStack tool = plugin.createFixedOreToolItem();
        player.getInventory().addItem(tool);
        player.sendMessage(plugin.getMessage("terra.fixedore.tool-given"));
        return true;
    }

    private boolean handleRewardsCommand(CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("enable")) {
                plugin.setBlockRewardsEnabled(true);
                sender.sendMessage(plugin.getMessage("terra.rewards.enabled"));
                return true;
            }
            if (args[1].equalsIgnoreCase("disable")) {
                plugin.setBlockRewardsEnabled(false);
                sender.sendMessage(plugin.getMessage("terra.rewards.disabled"));
                return true;
            }
        }

        if (args.length == 3 && args[1].equalsIgnoreCase("money")) {
            if (args[2].equalsIgnoreCase("enable")) {
                plugin.setBlockMoneyRewardsEnabled(true);
                sender.sendMessage(plugin.getMessage("terra.rewards.money-enabled"));
                return true;
            }
            if (args[2].equalsIgnoreCase("disable")) {
                plugin.setBlockMoneyRewardsEnabled(false);
                sender.sendMessage(plugin.getMessage("terra.rewards.money-disabled"));
                return true;
            }
        }

        for (String line : plugin.getMessageList("help.terra.rewards")) {
            sender.sendMessage(line);
        }
        return true;
    }

    private boolean handleHostileMobsCommand(CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("enable")) {
                plugin.setHostileMobSpawnsEnabled(true);
                sender.sendMessage(plugin.getMessage("terra.hostilemobs.enabled"));
                return true;
            }
            if (args[1].equalsIgnoreCase("disable")) {
                plugin.setHostileMobSpawnsEnabled(false);
                sender.sendMessage(plugin.getMessage("terra.hostilemobs.disabled"));
                return true;
            }
        }

        for (String line : plugin.getMessageList("help.terra.hostilemobs")) {
            sender.sendMessage(line);
        }
        return true;
    }

    private boolean handlePhantomsCommand(CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("enable")) {
                plugin.setPhantomsEnabled(true);
                sender.sendMessage(plugin.getMessage("terra.phantoms.enabled"));
                return true;
            }
            if (args[1].equalsIgnoreCase("disable")) {
                plugin.setPhantomsEnabled(false);
                sender.sendMessage(plugin.getMessage("terra.phantoms.disabled"));
                return true;
            }
        }

        for (String line : plugin.getMessageList("help.terra.phantoms")) {
            sender.sendMessage(line);
        }
        return true;
    }

    private boolean handleSetXpBoostCommand(CommandSender sender, String[] args) {
        if (args.length == 2 && args[1].equalsIgnoreCase("off")) {
            plugin.stopGlobalXpBoost();
            sender.sendMessage(plugin.getMessage("terra.xpboost.disabled"));
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(plugin.getMessage("terra.xpboost.usage"));
            return true;
        }

        if (plugin.hasActiveGlobalXpBoost()) {
            sender.sendMessage(plugin.getMessage("terra.xpboost.already-active", plugin.placeholders(
                    "amount", plugin.formatXpBoostMultiplier(plugin.getGlobalXpBoostMultiplier()),
                    "time", plugin.formatDurationWords(plugin.getGlobalXpBoostRemainingMillis()),
                    "player", plugin.getGlobalXpBoostEnabledBy() != null ? plugin.getGlobalXpBoostEnabledBy() : "Unknown"
            )));
            return true;
        }

        Double multiplier = parseXpBoostAmount(args[1]);
        if (multiplier == null) {
            sender.sendMessage(plugin.getMessage("terra.xpboost.invalid-amount"));
            return true;
        }

        Long durationMillis = parseDurationMillis(args[2]);
        if (durationMillis == null || durationMillis <= 0L) {
            sender.sendMessage(plugin.getMessage("terra.xpboost.invalid-time"));
            return true;
        }

        plugin.setGlobalXpBoost(multiplier, durationMillis, sender.getName());
        sender.sendMessage(plugin.getMessage("terra.xpboost.enabled", plugin.placeholders(
                "amount", plugin.formatXpBoostMultiplier(multiplier),
                "time", plugin.formatDurationWords(durationMillis)
        )));
        Bukkit.broadcastMessage(plugin.getMessage("terra.xpboost.broadcast-enabled", plugin.placeholders(
                "player", sender.getName(),
                "amount", plugin.formatXpBoostMultiplier(multiplier),
                "time", plugin.formatDurationWords(durationMillis)
        )));
        return true;
    }

    private boolean handleClearDataCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(plugin.getMessage("terra.cleardata.usage"));
            return true;
        }

        OfflinePlayer target = findPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getMessage("general.player-not-found"));
            return true;
        }

        plugin.resetPlayerData(target);
        String targetName = target.getName() != null ? target.getName() : args[1];
        sender.sendMessage(plugin.getMessage("terra.cleardata.success", plugin.placeholders(
                "player", targetName
        )));
        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(plugin.getMessage("terra.cleardata.target"));
        }
        return true;
    }

    private boolean handleJobCapCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(plugin.getMessage("terra.jobcap.usage"));
            return true;
        }

        Profession profession = Profession.fromKey(args[1]);
        if (profession == null || !plugin.isProfessionEnabled(profession)) {
            sender.sendMessage(plugin.getMessage("profession.unknown"));
            return true;
        }

        int cap;
        try {
            cap = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(plugin.getMessage("terra.jobcap.invalid-cap"));
            return true;
        }

        if (cap < 0) {
            sender.sendMessage(plugin.getMessage("terra.jobcap.invalid-cap-min"));
            return true;
        }

        plugin.setProfessionPlayerCap(profession, cap);
        sender.sendMessage(plugin.getMessage("terra.jobcap.set", plugin.placeholders(
                "profession", plugin.getProfessionPlainDisplayName(profession),
                "cap", cap <= 0 ? "Unlimited" : String.valueOf(cap),
                "current", String.valueOf(plugin.getProfessionPlayerCount(profession))
        )));
        return true;
    }

    private boolean handlePlaytestCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            if (sender instanceof Player player) {
                plugin.openPlaytestMenu(player);
            } else {
                sender.sendMessage(plugin.getMessage("terra.playtest.usage"));
            }
            return true;
        }

        if (args[1].equalsIgnoreCase("manage")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessage("terra.playtest.player-only"));
                return true;
            }
            plugin.openPlaytestMenu(player);
            return true;
        }

        if (args[1].equalsIgnoreCase("status")) {
            if (plugin.isPlaytestPreparing()) {
                sender.sendMessage(plugin.getMessage("terra.playtest.status-preparing"));
                return true;
            }
            if (plugin.isPlaytestStopPending()) {
                sender.sendMessage(plugin.getMessage("terra.playtest.status-stopping"));
                return true;
            }
            if (plugin.isPlaytestRestoring()) {
                sender.sendMessage(plugin.getMessage("terra.playtest.status-restoring"));
                return true;
            }
            if (!plugin.isPlaytestActive()) {
                sender.sendMessage(plugin.getMessage("terra.playtest.status-inactive"));
                return true;
            }
            sender.sendMessage(plugin.getMessage("terra.playtest.status-active", plugin.placeholders(
                    "player", plugin.getPlaytestStartedBy() != null ? plugin.getPlaytestStartedBy() : "Console",
                    "time", plugin.formatPlaytestRemainingDuration(plugin.getPlaytestRemainingMillis())
            )));
            return true;
        }

        if (args[1].equalsIgnoreCase("stop")) {
            if (!plugin.stopPlaytestNow()) {
                sender.sendMessage(plugin.getMessage("terra.playtest.status-inactive"));
                return true;
            }
            sender.sendMessage(plugin.getMessage("terra.playtest.stop-requested"));
            return true;
        }

        if (args[1].equalsIgnoreCase("extend")) {
            if (args.length < 3) {
                sender.sendMessage(Component.text("Usage: /terra playtest extend <time>", NamedTextColor.RED));
                return true;
            }

            Long additionalMillis = parsePlaytestDurationMillis(args[2]);
            if (additionalMillis == null || additionalMillis <= 0L) {
                sender.sendMessage(plugin.getMessage("terra.playtest.invalid-time"));
                return true;
            }

            if (!plugin.extendPlaytest(additionalMillis)) {
                sender.sendMessage(plugin.getMessage("terra.playtest.status-inactive"));
                return true;
            }

            sender.sendMessage(Component.text(
                    "Extended the playtest by " + plugin.formatLongDurationWords(additionalMillis) + ".",
                    NamedTextColor.GREEN
            ));
            return true;
        }

        if (!args[1].equalsIgnoreCase("start")) {
            sender.sendMessage(plugin.getMessage("terra.playtest.usage"));
            return true;
        }

        long durationMillis = plugin.getConfiguredPlaytestDurationMillis();
        if (args.length >= 3) {
            Long parsedDurationMillis = parsePlaytestDurationMillis(args[2]);
            if (parsedDurationMillis == null || parsedDurationMillis <= 0L) {
                sender.sendMessage(plugin.getMessage("terra.playtest.invalid-time"));
                return true;
            }
            durationMillis = parsedDurationMillis;
            plugin.setConfiguredPlaytestDurationMillis(durationMillis);
        }

        if (!plugin.startPlaytest(durationMillis, sender.getName())) {
            sender.sendMessage(plugin.getMessage("terra.playtest.already-running"));
            return true;
        }

        sender.sendMessage(plugin.getMessage("terra.playtest.starting", plugin.placeholders(
                "time", plugin.formatLongDurationWords(durationMillis)
        )));
        return true;
    }

    private boolean handleJobEditorCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("country.only-players"));
            return true;
        }

        if (args.length == 1) {
            plugin.openJobConfigEditor(player);
            return true;
        }

        Profession profession = Profession.fromKey(args[1]);
        if (profession == null) {
            sender.sendMessage(plugin.getMessage("profession.unknown"));
            return true;
        }

        int level = 1;
        if (args.length >= 3) {
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(Component.text("Level must be a whole number.", NamedTextColor.RED));
                return true;
            }
        }

        plugin.openJobConfigEditor(player, profession, level);
        return true;
    }

    private boolean handleTraderCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessage("terra.trader.usage"));
            return true;
        }

        if (args[1].equalsIgnoreCase("status")) {
            DynamicTraderState state = plugin.getActiveTraderState();
            if (state != null) {
                sender.sendMessage(plugin.getMessage("terra.trader.status-active", plugin.placeholders(
                        "time", plugin.formatLongDurationWords(Math.max(1000L, state.getDespawnAtMillis() - System.currentTimeMillis()))
                )));
                return true;
            }

            long nextSpawn = plugin.getTraderNextSpawnMillis();
            if (nextSpawn > System.currentTimeMillis()) {
                sender.sendMessage(plugin.getMessage("terra.trader.status-waiting", plugin.placeholders(
                        "time", plugin.formatLongDurationWords(nextSpawn - System.currentTimeMillis())
                )));
                return true;
            }

            sender.sendMessage(plugin.getMessage("terra.trader.status-idle"));
            return true;
        }

        if (args[1].equalsIgnoreCase("time")) {
            if (args.length < 3) {
                sender.sendMessage(plugin.getMessage("terra.trader.time-usage"));
                return true;
            }

            if (args[2].equalsIgnoreCase("status")) {
                DynamicTraderState state = plugin.getActiveTraderState();
                String active = state != null
                        ? plugin.formatLongDurationWords(Math.max(1000L, state.getDespawnAtMillis() - System.currentTimeMillis()))
                        : "none";
                String next = plugin.getTraderNextSpawnMillis() > System.currentTimeMillis()
                        ? plugin.formatLongDurationWords(plugin.getTraderNextSpawnMillis() - System.currentTimeMillis())
                        : "ready";
                sender.sendMessage(plugin.getMessage("terra.trader.time-status", plugin.placeholders(
                        "active", active,
                        "next", next
                )));
                return true;
            }

            if (args.length != 4) {
                sender.sendMessage(plugin.getMessage("terra.trader.time-usage"));
                return true;
            }

            long minutes;
            try {
                minutes = Long.parseLong(args[3]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(plugin.getMessage("terra.trader.time-invalid"));
                return true;
            }

            if (minutes < 0L) {
                sender.sendMessage(plugin.getMessage("terra.trader.time-invalid"));
                return true;
            }

            if (args[2].equalsIgnoreCase("next")) {
                plugin.setTraderNextSpawnDelayMinutes(minutes);
                sender.sendMessage(plugin.getMessage("terra.trader.time-next-set", plugin.placeholders(
                        "time", minutes == 0L ? "now" : plugin.formatLongDurationWords(minutes * 60_000L)
                )));
                return true;
            }

            if (args[2].equalsIgnoreCase("active")) {
                if (!plugin.setActiveTraderRemainingMinutes(minutes)) {
                    sender.sendMessage(plugin.getMessage("terra.trader.no-active"));
                    return true;
                }
                sender.sendMessage(plugin.getMessage("terra.trader.time-active-set", plugin.placeholders(
                        "time", minutes == 0L ? "ended" : plugin.formatLongDurationWords(minutes * 60_000L)
                )));
                return true;
            }

            sender.sendMessage(plugin.getMessage("terra.trader.time-usage"));
            return true;
        }

        if (args[1].equalsIgnoreCase("spawn")) {
            Profession forcedSpecialty = null;
            if (args.length >= 3) {
                forcedSpecialty = Profession.fromKey(args[2]);
                if (forcedSpecialty == null) {
                    sender.sendMessage(plugin.getMessage("terra.trader.invalid-specialty"));
                    return true;
                }
            }
            if (!plugin.spawnDynamicTraderNow(forcedSpecialty)) {
                sender.sendMessage(plugin.getMessage("terra.trader.spawn-failed"));
                return true;
            }
            if (forcedSpecialty != null) {
                sender.sendMessage(plugin.getMessage("terra.trader.spawned-specialty", plugin.placeholders(
                        "profession", plugin.getProfessionPlainDisplayName(forcedSpecialty)
                )));
                return true;
            }
            sender.sendMessage(plugin.getMessage("terra.trader.spawned"));
            return true;
        }

        if (args[1].equalsIgnoreCase("remove")) {
            if (!plugin.despawnActiveTrader(false)) {
                sender.sendMessage(plugin.getMessage("terra.trader.no-active"));
                return true;
            }
            sender.sendMessage(plugin.getMessage("terra.trader.removed"));
            return true;
        }

        if (args[1].equalsIgnoreCase("open")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessage("country.only-players"));
                return true;
            }
            plugin.openTraderMenu(player);
            return true;
        }

        sender.sendMessage(plugin.getMessage("terra.trader.usage"));
        return true;
    }

    private boolean handleMerchantCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessage("terra.merchant.usage"));
            return true;
        }

        if (args[1].equalsIgnoreCase("status")) {
            MerchantShopState state = plugin.getAnyActiveMerchantState();
            if (state != null) {
                sender.sendMessage(plugin.getMessage("terra.merchant.status-active", plugin.placeholders(
                        "count", String.valueOf(plugin.getActiveMerchantCount()),
                        "time", plugin.formatLongDurationWords(Math.max(1000L, state.getDespawnAtMillis() - System.currentTimeMillis()))
                )));
                return true;
            }

            long nextSpawn = plugin.getMerchantNextSpawnMillis();
            if (nextSpawn > System.currentTimeMillis()) {
                sender.sendMessage(plugin.getMessage("terra.merchant.status-waiting", plugin.placeholders(
                        "time", plugin.formatLongDurationWords(nextSpawn - System.currentTimeMillis())
                )));
                return true;
            }

            sender.sendMessage(plugin.getMessage("terra.merchant.status-idle"));
            return true;
        }

        if (args[1].equalsIgnoreCase("time")) {
            if (args.length < 3) {
                sender.sendMessage(plugin.getMessage("terra.merchant.time-usage"));
                return true;
            }

            if (args[2].equalsIgnoreCase("status")) {
                MerchantShopState state = plugin.getAnyActiveMerchantState();
                String active = state != null
                        ? plugin.formatLongDurationWords(Math.max(1000L, state.getDespawnAtMillis() - System.currentTimeMillis()))
                        : "none";
                String next = plugin.getMerchantNextSpawnMillis() > System.currentTimeMillis()
                        ? plugin.formatLongDurationWords(plugin.getMerchantNextSpawnMillis() - System.currentTimeMillis())
                        : "ready";
                sender.sendMessage(plugin.getMessage("terra.merchant.time-status", plugin.placeholders(
                        "active", active,
                        "next", next
                )));
                return true;
            }

            if (args.length != 4) {
                sender.sendMessage(plugin.getMessage("terra.merchant.time-usage"));
                return true;
            }

            long minutes;
            try {
                minutes = Long.parseLong(args[3]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(plugin.getMessage("terra.merchant.time-invalid"));
                return true;
            }

            if (minutes < 0L) {
                sender.sendMessage(plugin.getMessage("terra.merchant.time-invalid"));
                return true;
            }

            if (args[2].equalsIgnoreCase("next")) {
                plugin.setMerchantNextSpawnDelayMinutes(minutes);
                sender.sendMessage(plugin.getMessage("terra.merchant.time-next-set", plugin.placeholders(
                        "time", minutes == 0L ? "now" : plugin.formatLongDurationWords(minutes * 60_000L)
                )));
                return true;
            }

            if (args[2].equalsIgnoreCase("active")) {
                if (!plugin.setActiveMerchantRemainingMinutes(minutes)) {
                    sender.sendMessage(plugin.getMessage("terra.merchant.no-active"));
                    return true;
                }
                sender.sendMessage(plugin.getMessage("terra.merchant.time-active-set", plugin.placeholders(
                        "time", minutes == 0L ? "ended" : plugin.formatLongDurationWords(minutes * 60_000L)
                )));
                return true;
            }

            sender.sendMessage(plugin.getMessage("terra.merchant.time-usage"));
            return true;
        }

        if (args[1].equalsIgnoreCase("spawn")) {
            if (!plugin.spawnMerchantNow()) {
                sender.sendMessage(plugin.getMessage("terra.merchant.spawn-failed"));
                return true;
            }
            sender.sendMessage(plugin.getMessage("terra.merchant.spawned", plugin.placeholders(
                    "count", String.valueOf(plugin.getActiveMerchantCount())
            )));
            return true;
        }

        if (args[1].equalsIgnoreCase("remove")) {
            if (!plugin.despawnMerchantNow()) {
                sender.sendMessage(plugin.getMessage("terra.merchant.no-active"));
                return true;
            }
            sender.sendMessage(plugin.getMessage("terra.merchant.removed"));
            return true;
        }

        if (args[1].equalsIgnoreCase("open")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessage("country.only-players"));
                return true;
            }
            plugin.openMerchantMenu(player);
            return true;
        }

        if (args[1].equalsIgnoreCase("manage")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessage("country.only-players"));
                return true;
            }
            plugin.openMerchantAdminMenu(player);
            return true;
        }

        sender.sendMessage(plugin.getMessage("terra.merchant.usage"));
        return true;
    }

    private boolean handleClimateCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendClimateUsage(sender);
            return true;
        }

        String subcommand = args[1].toLowerCase(Locale.ROOT);
        Player player = sender instanceof Player onlinePlayer ? onlinePlayer : null;

        if (subcommand.equals("check")) {
            if (player == null) {
                sender.sendMessage(plugin.getMessage("country.only-players"));
                return true;
            }
            ClimateSnapshot climate = plugin.getClimate(player.getLocation().getBlock().getLocation());
            ClimateSeason override = plugin.getClimateSeasonOverride();
            sender.sendMessage(Component.text(
                    "Climate: " + climate.climateName()
                            + " | Season: " + climate.season().getDisplayName()
                            + (override != null ? " (forced)" : "")
                            + " | Temp: " + plugin.formatTemperature(climate.temperatureCelsius())
                            + " | Growth: x" + String.format(Locale.US, "%.2f", climate.growthMultiplier())
                            + " | Rain: " + (climate.raining() ? "raining" : climate.recentlyRained() ? "recent" : "dry")
                            + " (" + signedTemperature(climate.rainTemperatureOffsetCelsius(), plugin) + ", x" + String.format(Locale.US, "%.2f", climate.rainGrowthBonusMultiplier()) + ")"
                            + " | Variation: " + signedTemperature(climate.patternTemperatureOffsetCelsius(), plugin)
                            + " | Humidity: " + percent(climate.humidity())
                            + " (" + signedTemperature(climate.humidityTemperatureOffsetCelsius(), plugin) + ")"
                            + " | Continentality: " + percent(climate.continentality())
                            + " (" + signedTemperature(climate.continentalTemperatureOffsetCelsius(), plugin) + ")"
                            + " | Current: " + signedPercent(climate.currentInfluence())
                            + " (" + signedTemperature(climate.currentTemperatureOffsetCelsius(), plugin) + ")"
                            + " | Altitude Temp: " + signedTemperature(climate.altitudeTemperatureOffsetCelsius(), plugin)
                            + " | Local: " + signedTemperature(climate.localTemperatureOffsetCelsius(), plugin)
                            + " | Water: " + signedTemperature(climate.submergedTemperatureOffsetCelsius(), plugin)
                            + " | Altitude Growth: x" + String.format(Locale.US, "%.2f", climate.altitudeGrowthMultiplier())
                            + (plugin.getClimateDebugRegion(player.getLocation()) != null ? " | Debug Area" : ""),
                    NamedTextColor.GREEN
            ));
            return true;
        }

        if (subcommand.equals("status")) {
            sendClimateStatus(sender, player);
            return true;
        }

        if (subcommand.equals("enable")) {
            if (args.length < 3) {
                sender.sendMessage(Component.text("Usage: /climate enable <on|off>", NamedTextColor.RED));
                return true;
            }
            Boolean enabled = parseClimateToggle(args[2]);
            if (enabled == null) {
                sender.sendMessage(Component.text("Use on or off.", NamedTextColor.RED));
                return true;
            }
            plugin.setClimateEnabled(enabled);
            sender.sendMessage(Component.text("Climate system " + (enabled ? "enabled" : "disabled") + ".", NamedTextColor.GREEN));
            return true;
        }

        if (subcommand.equals("unit")) {
            if (args.length < 3) {
                sender.sendMessage(Component.text("Usage: /climate unit <C|F>", NamedTextColor.RED));
                return true;
            }
            String unit = args[2].equalsIgnoreCase("F") ? "F" : args[2].equalsIgnoreCase("C") ? "C" : null;
            if (unit == null) {
                sender.sendMessage(Component.text("Use C or F.", NamedTextColor.RED));
                return true;
            }
            plugin.setClimateTemperatureUnit(unit);
            sender.sendMessage(Component.text("Climate temperature unit set to " + unit + ".", NamedTextColor.GREEN));
            return true;
        }

        if (subcommand.equals("seasons")) {
            if (args.length < 3) {
                sender.sendMessage(Component.text("Usage: /climate seasons <on|off>", NamedTextColor.RED));
                return true;
            }
            Boolean enabled = parseClimateToggle(args[2]);
            if (enabled == null) {
                sender.sendMessage(Component.text("Use on or off.", NamedTextColor.RED));
                return true;
            }
            plugin.setClimateSeasonsEnabled(enabled);
            sender.sendMessage(Component.text("Climate seasons " + (enabled ? "enabled" : "disabled") + ".", NamedTextColor.GREEN));
            return true;
        }

        if (subcommand.equals("season")) {
            if (args.length < 3) {
                sender.sendMessage(Component.text("Usage: /climate season <auto|spring|summer|autumn|winter>", NamedTextColor.RED));
                return true;
            }
            if (args[2].equalsIgnoreCase("auto")) {
                plugin.setClimateSeasonOverride(null);
                sender.sendMessage(Component.text("Climate season override cleared. Automatic season is active again.", NamedTextColor.GREEN));
                return true;
            }

            ClimateSeason forcedSeason;
            try {
                forcedSeason = ClimateSeason.valueOf(args[2].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                sender.sendMessage(Component.text("Use auto, spring, summer, autumn, or winter.", NamedTextColor.RED));
                return true;
            }

            plugin.setClimateSeasonOverride(forcedSeason);
            sender.sendMessage(Component.text("Climate season forced to " + forcedSeason.getDisplayName() + ".", NamedTextColor.GREEN));
            return true;
        }

        if (subcommand.equals("bossbar")) {
            if (args.length < 3) {
                sender.sendMessage(Component.text("Usage: /climate bossbar <on|off>", NamedTextColor.RED));
                return true;
            }
            Boolean enabled = parseClimateToggle(args[2]);
            if (enabled == null) {
                sender.sendMessage(Component.text("Use on or off.", NamedTextColor.RED));
                return true;
            }
            plugin.setClimateBossBarEnabled(enabled);
            sender.sendMessage(Component.text("Climate bossbar " + (enabled ? "enabled" : "disabled") + ".", NamedTextColor.GREEN));
            return true;
        }

        if (subcommand.equals("freeze")) {
            if (args.length < 3) {
                sender.sendMessage(Component.text("Usage: /climate freeze <on|off>", NamedTextColor.RED));
                return true;
            }
            Boolean enabled = parseClimateToggle(args[2]);
            if (enabled == null) {
                sender.sendMessage(Component.text("Use on or off.", NamedTextColor.RED));
                return true;
            }
            plugin.setClimateFreezeWaterEnabled(enabled);
            sender.sendMessage(Component.text("Climate water freezing " + (enabled ? "enabled" : "disabled") + ".", NamedTextColor.GREEN));
            return true;
        }

        if (subcommand.equals("display")) {
            if (player == null) {
                sender.sendMessage(plugin.getMessage("country.only-players"));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(Component.text("Usage: /climate display <on|off>", NamedTextColor.RED));
                return true;
            }
            Boolean enabled = parseClimateToggle(args[2]);
            if (enabled == null) {
                sender.sendMessage(Component.text("Use on or off.", NamedTextColor.RED));
                return true;
            }
            plugin.setClimateLiveDisplayEnabled(player.getUniqueId(), enabled);
            sender.sendMessage(Component.text("Live climate particle display " + (enabled ? "enabled" : "disabled") + ".", NamedTextColor.GREEN));
            return true;
        }

        if (subcommand.equals("altitude")) {
            if (args.length < 4 || !args[2].equalsIgnoreCase("optimal")) {
                sender.sendMessage(Component.text("Usage: /climate altitude optimal <y>", NamedTextColor.RED));
                return true;
            }
            double y;
            try {
                y = Double.parseDouble(args[3]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(Component.text("Optimal altitude must be a number.", NamedTextColor.RED));
                return true;
            }
            plugin.setClimateOptimalAltitudeY(y);
            sender.sendMessage(Component.text("Climate optimal altitude set to Y " + String.format(Locale.US, "%.1f", y) + ".", NamedTextColor.GREEN));
            return true;
        }

        if (subcommand.equals("playtest")) {
            if (args.length < 3) {
                sender.sendMessage(Component.text("Usage: /climate playtest <on|off|here|center <x> <z>|radius <blocks>|temps <centerC> <edgeC>>", NamedTextColor.RED));
                return true;
            }
            if (args[2].equalsIgnoreCase("on") || args[2].equalsIgnoreCase("off")) {
                boolean enabled = args[2].equalsIgnoreCase("on");
                plugin.setClimatePlaytestModeEnabled(enabled);
                sender.sendMessage(Component.text("Climate playtest mode " + (enabled ? "enabled" : "disabled") + ".", NamedTextColor.GREEN));
                if (enabled) {
                    sender.sendMessage(Component.text("Playtest mode keeps corner islands livable by using a warmer edge temperature and softer night/season shifts.", NamedTextColor.YELLOW));
                }
                return true;
            }
            if (args[2].equalsIgnoreCase("here")) {
                if (player == null) {
                    sender.sendMessage(plugin.getMessage("country.only-players"));
                    return true;
                }
                plugin.setClimatePlaytestCenter(player.getLocation().getX(), player.getLocation().getZ());
                sender.sendMessage(Component.text("Climate playtest center set to your current location.", NamedTextColor.GREEN));
                return true;
            }
            if (args[2].equalsIgnoreCase("center")) {
                if (args.length < 5) {
                    sender.sendMessage(Component.text("Usage: /climate playtest center <x> <z>", NamedTextColor.RED));
                    return true;
                }
                double x;
                double z;
                try {
                    x = Double.parseDouble(args[3]);
                    z = Double.parseDouble(args[4]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(Component.text("Center coordinates must be numbers.", NamedTextColor.RED));
                    return true;
                }
                plugin.setClimatePlaytestCenter(x, z);
                sender.sendMessage(Component.text("Climate playtest center set to X " + String.format(Locale.US, "%.1f", x)
                        + ", Z " + String.format(Locale.US, "%.1f", z) + ".", NamedTextColor.GREEN));
                return true;
            }
            if (args[2].equalsIgnoreCase("radius")) {
                if (args.length < 4) {
                    sender.sendMessage(Component.text("Usage: /climate playtest radius <blocks>", NamedTextColor.RED));
                    return true;
                }
                double radius;
                try {
                    radius = Double.parseDouble(args[3]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(Component.text("Radius must be a number.", NamedTextColor.RED));
                    return true;
                }
                plugin.setClimatePlaytestRadiusBlocks(radius);
                sender.sendMessage(Component.text("Climate playtest radius set to " + String.format(Locale.US, "%.1f", Math.max(1.0D, radius)) + " blocks.", NamedTextColor.GREEN));
                return true;
            }
            if (args[2].equalsIgnoreCase("temps")) {
                if (args.length < 5) {
                    sender.sendMessage(Component.text("Usage: /climate playtest temps <centerC> <edgeC>", NamedTextColor.RED));
                    return true;
                }
                double centerTemp;
                double edgeTemp;
                try {
                    centerTemp = Double.parseDouble(args[3]);
                    edgeTemp = Double.parseDouble(args[4]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(Component.text("Temperatures must be numbers in Celsius.", NamedTextColor.RED));
                    return true;
                }
                plugin.setClimatePlaytestTemperatures(centerTemp, edgeTemp);
                sender.sendMessage(Component.text("Climate playtest temperatures set. Center: "
                        + plugin.formatTemperature(centerTemp) + " | Edge: " + plugin.formatTemperature(edgeTemp) + ".", NamedTextColor.GREEN));
                return true;
            }
            sender.sendMessage(Component.text("Usage: /climate playtest <on|off|here|center <x> <z>|radius <blocks>|temps <centerC> <edgeC>>", NamedTextColor.RED));
            return true;
        }

        if (subcommand.equals("create")) {
            if (player == null) {
                sender.sendMessage(plugin.getMessage("country.only-players"));
                return true;
            }
            ClimateDebugRegion debugRegion;
            if (args.length >= 3 && args[2].equalsIgnoreCase("fullworld")) {
                org.bukkit.WorldBorder worldBorder = player.getWorld().getWorldBorder();
                Location center = worldBorder.getCenter();
                double halfSize = worldBorder.getSize() / 2.0D;
                int minX = (int) Math.floor(center.getX() - halfSize);
                int maxX = (int) Math.ceil(center.getX() + halfSize);
                int minZ = (int) Math.floor(center.getZ() - halfSize);
                int maxZ = (int) Math.ceil(center.getZ() + halfSize);
                debugRegion = plugin.registerClimateDebugRegion(
                        player.getWorld(),
                        minX,
                        maxX,
                        minZ,
                        maxZ,
                        player.getLocation().getBlockY()
                );
            } else {
                Region selection = getClimateSelectionRegion(player);
                if (selection == null) {
                    return true;
                }

                debugRegion = plugin.registerClimateDebugRegion(
                        player.getWorld(),
                        selection.getMinimumPoint().x(),
                        selection.getMaximumPoint().x(),
                        selection.getMinimumPoint().z(),
                        selection.getMaximumPoint().z(),
                        selection.getMinimumPoint().y()
                );
            }
            int changed = plugin.createClimateDebugPlatform(debugRegion);
            sender.sendMessage(Component.text("Registered climate particle preview across " + changed + " sample points.", NamedTextColor.GREEN));
            if (args.length >= 3 && args[2].equalsIgnoreCase("fullworld")) {
                sender.sendMessage(Component.text("Using the current world border as the full-world climate preview bounds.", NamedTextColor.YELLOW));
            } else {
                sender.sendMessage(Component.text("Top of the selection previews the north, bottom previews the south. You can also use /climate display on anywhere without creating a preview area.", NamedTextColor.YELLOW));
            }
            return true;
        }

        if (subcommand.equals("refresh")) {
            if (player == null) {
                sender.sendMessage(plugin.getMessage("country.only-players"));
                return true;
            }
            ClimateDebugRegion debugRegion = plugin.getClimateDebugRegion(player.getLocation());
            if (debugRegion == null) {
                sender.sendMessage(Component.text("Stand inside a climate debug area first.", NamedTextColor.RED));
                return true;
            }
            int changed = plugin.refreshClimateDebugPlatform(debugRegion);
            sender.sendMessage(Component.text("Refreshed the climate particle preview across " + changed + " sample points.", NamedTextColor.GREEN));
            return true;
        }

        if (subcommand.equals("clear")) {
            if (args.length >= 3 && args[2].equalsIgnoreCase("all")) {
                if (player == null) {
                    sender.sendMessage(plugin.getMessage("country.only-players"));
                    return true;
                }
                int changed = plugin.clearAllClimateDebugPlatforms(player.getWorld());
                if (changed <= 0) {
                    sender.sendMessage(Component.text("No climate debug areas were registered in this world.", NamedTextColor.RED));
                    return true;
                }
                sender.sendMessage(Component.text("Removed all climate debug areas in this world across " + changed + " blocks.", NamedTextColor.GREEN));
                return true;
            }
            if (player == null) {
                sender.sendMessage(plugin.getMessage("country.only-players"));
                return true;
            }
            ClimateDebugRegion region = plugin.getClimateDebugRegion(player.getLocation());
            if (region == null) {
                sender.sendMessage(Component.text("Stand inside a climate debug area first.", NamedTextColor.RED));
                return true;
            }
            int changed = plugin.clearClimateDebugPlatform(region);
            plugin.removeClimateDebugRegion(player.getLocation());
            sender.sendMessage(Component.text("Removed the current climate particle preview across " + changed + " sample points.", NamedTextColor.GREEN));
            return true;
        }

        if (subcommand.equals("crops")) {
            if (player == null) {
                sender.sendMessage(plugin.getMessage("country.only-players"));
                return true;
            }
            plugin.openClimateCropGuide(player);
            return true;
        }

        sendClimateUsage(sender);
        return true;
    }

    private void sendClimateStatus(CommandSender sender, Player player) {
        sender.sendMessage(Component.text(
                "Climate status | Enabled: " + onOff(plugin.isClimateEnabled())
                        + " | Unit: " + plugin.getClimateTemperatureUnit()
                        + " | Seasons: " + onOff(plugin.areClimateSeasonsEnabled())
                        + " | Season Mode: " + (plugin.getClimateSeasonOverride() != null ? plugin.getClimateSeasonOverride().getDisplayName() + " forced" : "auto")
                        + " | Bossbar: " + onOff(plugin.isClimateBossBarEnabled())
                        + " | Freeze: " + onOff(plugin.isClimateFreezeWaterEnabled())
                        + " | Live Display: " + (player != null ? onOff(plugin.isClimateLiveDisplayEnabled(player.getUniqueId())) : "player-only")
                        + " | Playtest: " + onOff(plugin.isClimatePlaytestModeEnabled()),
                NamedTextColor.GREEN
        ));
        sender.sendMessage(Component.text(
                "Climate/Altitude | Latitude Scale: " + String.format(Locale.US, "%.1f", plugin.getConfig().getDouble("climate.latitude-scale-blocks", 750.0D))
                        + " | Optimal Altitude: Y " + String.format(Locale.US, "%.1f", plugin.getClimateOptimalAltitudeY()),
                NamedTextColor.AQUA
        ));
        if (plugin.isClimatePlaytestModeEnabled()) {
            sender.sendMessage(Component.text(
                    "Playtest mode | Center: X " + String.format(Locale.US, "%.1f", plugin.getClimatePlaytestCenterX())
                            + ", Z " + String.format(Locale.US, "%.1f", plugin.getClimatePlaytestCenterZ())
                            + " | Radius: " + String.format(Locale.US, "%.1f", plugin.getClimatePlaytestRadiusBlocks())
                            + " | Center Temp: " + plugin.formatTemperature(plugin.getClimatePlaytestCenterTemperatureCelsius())
                            + " | Edge Temp: " + plugin.formatTemperature(plugin.getClimatePlaytestEdgeTemperatureCelsius()),
                    NamedTextColor.AQUA
            ));
        }
        if (player == null) {
            return;
        }
        ClimateDebugRegion debugRegion = plugin.getClimateDebugRegion(player.getLocation());
        if (debugRegion == null) {
            sender.sendMessage(Component.text("No climate debug area at your location.", NamedTextColor.YELLOW));
            return;
        }
        sender.sendMessage(Component.text(
                "Debug area | X: " + debugRegion.minX() + " to " + debugRegion.maxX()
                        + " | Z: " + debugRegion.minZ() + " to " + debugRegion.maxZ()
                        + " | Center: " + debugRegion.centerX() + ", " + debugRegion.centerZ(),
                NamedTextColor.AQUA
        ));
    }

    private void sendClimateUsage(CommandSender sender) {
        sender.sendMessage(Component.text(
                "Usage: /climate <check|status|enable|unit|seasons|season|bossbar|freeze|display|altitude|playtest|create [fullworld]|refresh|clear [all]|crops>",
                NamedTextColor.RED
        ));
    }

    private Boolean parseClimateToggle(String value) {
        if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("enable") || value.equalsIgnoreCase("enabled")) {
            return true;
        }
        if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("disable") || value.equalsIgnoreCase("disabled")) {
            return false;
        }
        return null;
    }

    private String onOff(boolean enabled) {
        return enabled ? "on" : "off";
    }

    private Region getWorldEditSelectionRegion(Player player) {
        WorldEditPlugin worldEdit = getWorldEditPlugin();
        if (worldEdit == null) {
            player.sendMessage(Component.text("WorldEdit is required for this command.", NamedTextColor.RED));
            return null;
        }

        try {
            LocalSession session = worldEdit.getSession(player);
            return session.getSelection(BukkitAdapter.adapt(player.getWorld()));
        } catch (IncompleteRegionException exception) {
            player.sendMessage(Component.text("Make a WorldEdit selection first.", NamedTextColor.RED));
            return null;
        }
    }

    private Region getClimateSelectionRegion(Player player) {
        return getWorldEditSelectionRegion(player);
    }

    private boolean handleFlySpeedCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("terra.flyspeed.player-only"));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(plugin.getMessage("terra.flyspeed.usage"));
            return true;
        }

        int speedLevel;
        try {
            speedLevel = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(plugin.getMessage("terra.flyspeed.invalid-speed"));
            return true;
        }

        if (speedLevel < 0 || speedLevel > 10) {
            sender.sendMessage(plugin.getMessage("terra.flyspeed.invalid-range"));
            return true;
        }

        float flySpeed = speedLevel / 10.0F;
        player.setFlySpeed(flySpeed);
        sender.sendMessage(plugin.getMessage("terra.flyspeed.set", plugin.placeholders(
                "speed", String.valueOf(speedLevel)
        )));
        return true;
    }

    private boolean handleVanishCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("staff.only-players"));
            return true;
        }
        if (args.length > 2) {
            sender.sendMessage(Component.text("Usage: /vanish [on|off|toggle]", NamedTextColor.RED));
            return true;
        }

        Boolean desired = null;
        if (args.length == 2) {
            desired = switch (args[1].toLowerCase(Locale.ROOT)) {
                case "on", "enable", "enabled", "true" -> true;
                case "off", "disable", "disabled", "false" -> false;
                case "toggle" -> null;
                default -> null;
            };
            if (desired == null && !args[1].equalsIgnoreCase("toggle")) {
                sender.sendMessage(Component.text("Usage: /vanish [on|off|toggle]", NamedTextColor.RED));
                return true;
            }
        }

        boolean vanished;
        if (desired == null) {
            vanished = plugin.toggleVanish(player);
        } else {
            plugin.setVanished(player, desired);
            vanished = desired;
        }
        sender.sendMessage(Component.text("Vanish " + (vanished ? "enabled." : "disabled."), NamedTextColor.GREEN));
        return true;
    }

    private boolean handleOreVisionCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("terra.orevision.player-only"));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(plugin.getMessage("terra.orevision.usage"));
            return true;
        }

        boolean enabled = plugin.toggleOreVision(player);
        sender.sendMessage(plugin.getMessage(enabled
                ? "terra.orevision.enabled"
                : "terra.orevision.disabled"));
        return true;
    }

    private boolean handleSetWorldSpawnCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("terra.setworldspawn.player-only"));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(plugin.getMessage("terra.setworldspawn.usage"));
            return true;
        }

        plugin.setWorldSpawn(player.getLocation());
        sender.sendMessage(plugin.getMessage("terra.setworldspawn.success", plugin.placeholders(
                "world", player.getWorld().getName(),
                "x", String.valueOf(player.getLocation().getBlockX()),
                "y", String.valueOf(player.getLocation().getBlockY()),
                "z", String.valueOf(player.getLocation().getBlockZ())
        )));
        return true;
    }

    private boolean handleCooldownDebugCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("terra.cooldowndebug.player-only"));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(plugin.getMessage("terra.cooldowndebug.usage"));
            return true;
        }

        boolean enabled = plugin.toggleCooldownDebug(player);
        sender.sendMessage(plugin.getMessage(enabled
                ? "terra.cooldowndebug.enabled"
                : "terra.cooldowndebug.disabled"));
        return true;
    }

    private boolean handleRealTimeClockCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Component.text("Usage: /terra realtimeclock <on|off|status|sync>", NamedTextColor.RED));
            return true;
        }

        if (args[1].equalsIgnoreCase("status")) {
            sender.sendMessage(Component.text(
                    "Real-time clock: " + (plugin.isRealTimeClockEnabled() ? "enabled" : "disabled")
                            + " | Timezone: " + plugin.getRealTimeClockTimezoneId()
                            + " | Worlds: " + String.join(", ", plugin.getRealTimeClockWorldNames())
                            + " | Tick: " + plugin.getCurrentRealTimeClockTicks(),
                    NamedTextColor.GREEN
            ));
            return true;
        }

        if (args[1].equalsIgnoreCase("on")) {
            plugin.setRealTimeClockEnabled(true);
            sender.sendMessage(Component.text(
                    "Real-time clock enabled. Configured worlds now follow " + plugin.getRealTimeClockTimezoneId() + ".",
                    NamedTextColor.GREEN
            ));
            return true;
        }

        if (args[1].equalsIgnoreCase("off")) {
            plugin.setRealTimeClockEnabled(false);
            sender.sendMessage(Component.text("Real-time clock disabled.", NamedTextColor.GREEN));
            return true;
        }

        if (args[1].equalsIgnoreCase("sync")) {
            plugin.syncRealTimeClockNow();
            sender.sendMessage(Component.text(
                    "Real-time clock synced now using timezone " + plugin.getRealTimeClockTimezoneId() + ".",
                    NamedTextColor.GREEN
            ));
            return true;
        }

        sender.sendMessage(Component.text("Usage: /terra realtimeclock <on|off|status|sync>", NamedTextColor.RED));
        return true;
    }

    private boolean handleLagCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /terra lag <status|clearitems|stacknow|itemclear|mobstack|itemmerge>", NamedTextColor.RED));
            return true;
        }

        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "status" -> {
                sender.sendMessage(Component.text("Lag tools status:", NamedTextColor.GOLD));
                sender.sendMessage(Component.text("Ground item clear: "
                        + (plugin.areGroundItemClearEnabled() ? "enabled" : "disabled")
                        + " every " + plugin.getGroundItemClearIntervalMinutes() + " minute(s)", NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("Ground items: " + plugin.countGroundItems(), NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("Mob stacking: "
                        + (plugin.isMobStackingEnabled() ? "enabled" : "disabled")
                        + ", stacked entities: " + plugin.countStackedMobEntities(), NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("Item merge: "
                        + (plugin.isItemMergeEnabled() ? "enabled" : "disabled")
                        + " radius " + formatDecimal(plugin.getItemMergeRadius()), NamedTextColor.YELLOW));
                return true;
            }
            case "clearitems" -> {
                int removed = plugin.clearGroundItems();
                sender.sendMessage(plugin.getMessage("lag.item-clear-complete", plugin.placeholders(
                        "count", String.valueOf(removed)
                )));
                return true;
            }
            case "stacknow" -> {
                int merged = plugin.runMobStackingSweep();
                sender.sendMessage(plugin.getMessage("lag.mob-stack-complete", plugin.placeholders(
                        "count", String.valueOf(merged)
                )));
                return true;
            }
            case "itemclear" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /terra lag itemclear <on|off|interval>", NamedTextColor.RED));
                    return true;
                }
                if (args[2].equalsIgnoreCase("on")) {
                    plugin.setGroundItemClearEnabled(true);
                    sender.sendMessage(Component.text("Ground item clear enabled.", NamedTextColor.GREEN));
                    return true;
                }
                if (args[2].equalsIgnoreCase("off")) {
                    plugin.setGroundItemClearEnabled(false);
                    sender.sendMessage(Component.text("Ground item clear disabled.", NamedTextColor.YELLOW));
                    return true;
                }
                if (args[2].equalsIgnoreCase("interval")) {
                    if (args.length < 4) {
                        sender.sendMessage(Component.text("Usage: /terra lag itemclear interval <minutes>", NamedTextColor.RED));
                        return true;
                    }
                    try {
                        int minutes = Integer.parseInt(args[3]);
                        plugin.setGroundItemClearIntervalMinutes(minutes);
                        sender.sendMessage(Component.text("Ground item clear interval set to " + Math.max(1, minutes) + " minute(s).", NamedTextColor.GREEN));
                    } catch (NumberFormatException exception) {
                        sender.sendMessage(Component.text("Minutes must be a whole number.", NamedTextColor.RED));
                    }
                    return true;
                }
            }
            case "mobstack" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /terra lag mobstack <on|off|radius|max>", NamedTextColor.RED));
                    return true;
                }
                if (args[2].equalsIgnoreCase("on")) {
                    plugin.setMobStackingEnabled(true);
                    sender.sendMessage(Component.text("Mob stacking enabled.", NamedTextColor.GREEN));
                    return true;
                }
                if (args[2].equalsIgnoreCase("off")) {
                    plugin.setMobStackingEnabled(false);
                    sender.sendMessage(Component.text("Mob stacking disabled.", NamedTextColor.YELLOW));
                    return true;
                }
                if (args[2].equalsIgnoreCase("radius")) {
                    if (args.length < 4) {
                        sender.sendMessage(Component.text("Usage: /terra lag mobstack radius <blocks>", NamedTextColor.RED));
                        return true;
                    }
                    try {
                        double radius = Double.parseDouble(args[3]);
                        plugin.setMobStackingRadius(radius);
                        sender.sendMessage(Component.text("Mob stacking radius set to " + formatDecimal(radius) + " blocks.", NamedTextColor.GREEN));
                    } catch (NumberFormatException exception) {
                        sender.sendMessage(Component.text("Radius must be a number.", NamedTextColor.RED));
                    }
                    return true;
                }
                if (args[2].equalsIgnoreCase("max")) {
                    if (args.length < 4) {
                        sender.sendMessage(Component.text("Usage: /terra lag mobstack max <amount>", NamedTextColor.RED));
                        return true;
                    }
                    try {
                        int maxSize = Integer.parseInt(args[3]);
                        plugin.setMobStackingMaxStackSize(maxSize);
                        sender.sendMessage(Component.text("Mob stack max set to " + Math.max(2, maxSize) + ".", NamedTextColor.GREEN));
                    } catch (NumberFormatException exception) {
                        sender.sendMessage(Component.text("Max stack size must be a whole number.", NamedTextColor.RED));
                    }
                    return true;
                }
            }
            case "itemmerge" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /terra lag itemmerge <on|off|radius>", NamedTextColor.RED));
                    return true;
                }
                if (args[2].equalsIgnoreCase("on")) {
                    plugin.setItemMergeEnabled(true);
                    sender.sendMessage(Component.text("Item merge enabled.", NamedTextColor.GREEN));
                    return true;
                }
                if (args[2].equalsIgnoreCase("off")) {
                    plugin.setItemMergeEnabled(false);
                    sender.sendMessage(Component.text("Item merge disabled.", NamedTextColor.YELLOW));
                    return true;
                }
                if (args[2].equalsIgnoreCase("radius")) {
                    if (args.length < 4) {
                        sender.sendMessage(Component.text("Usage: /terra lag itemmerge radius <blocks>", NamedTextColor.RED));
                        return true;
                    }
                    try {
                        double radius = Double.parseDouble(args[3]);
                        plugin.setItemMergeRadius(radius);
                        sender.sendMessage(Component.text("Item merge radius set to " + formatDecimal(radius) + " blocks.", NamedTextColor.GREEN));
                    } catch (NumberFormatException exception) {
                        sender.sendMessage(Component.text("Radius must be a number.", NamedTextColor.RED));
                    }
                    return true;
                }
            }
            default -> {
            }
        }

        sender.sendMessage(Component.text("Usage: /terra lag <status|clearitems|stacknow|itemclear|mobstack|itemmerge>", NamedTextColor.RED));
        return true;
    }

    private boolean handleMaintenanceCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /terra maintenance <on|off|status|add|remove|list>", NamedTextColor.RED));
            return true;
        }

        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "on", "enable", "enabled" -> {
                plugin.setMaintenanceModeEnabled(true);
                sender.sendMessage(plugin.getMessage("maintenance.enabled"));
                return true;
            }
            case "off", "disable", "disabled" -> {
                plugin.setMaintenanceModeEnabled(false);
                sender.sendMessage(plugin.getMessage("maintenance.disabled"));
                return true;
            }
            case "status" -> {
                sender.sendMessage(plugin.getMessage(plugin.isMaintenanceModeEnabled()
                        ? "maintenance.status-enabled"
                        : "maintenance.status-disabled"));
                return true;
            }
            case "list" -> {
                List<String> names = plugin.getMaintenanceAccessNames();
                if (names.isEmpty()) {
                    sender.sendMessage(plugin.getMessage("maintenance.list-empty"));
                } else {
                    sender.sendMessage(plugin.getMessage("maintenance.list", plugin.placeholders(
                            "players", String.join(", ", names)
                    )));
                }
                return true;
            }
            case "add", "remove" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /terra maintenance " + args[1].toLowerCase(Locale.ROOT) + " <player>", NamedTextColor.RED));
                    return true;
                }
                OfflinePlayer target = findPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage(plugin.getMessage("general.player-not-found"));
                    return true;
                }
                boolean changed = args[1].equalsIgnoreCase("add")
                        ? plugin.addMaintenanceAccess(target)
                        : plugin.removeMaintenanceAccess(target);
                String messageKey;
                if (args[1].equalsIgnoreCase("add")) {
                    messageKey = changed ? "maintenance.added" : "maintenance.already-added";
                } else {
                    messageKey = changed ? "maintenance.removed" : "maintenance.not-listed";
                }
                sender.sendMessage(plugin.getMessage(messageKey, plugin.placeholders(
                        "player", plugin.safeOfflineName(target)
                )));
                return true;
            }
            default -> {
                sender.sendMessage(Component.text("Usage: /terra maintenance <on|off|status|add|remove|list>", NamedTextColor.RED));
                return true;
            }
        }
    }

    private boolean handleHungerSpeedCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Component.text("Usage: /terra hungerspeed <multiplier|status>", NamedTextColor.RED));
            return true;
        }

        if (args[1].equalsIgnoreCase("status")) {
            sender.sendMessage(Component.text(
                    "Hunger speed multiplier: " + formatDecimal(plugin.getHungerSpeedMultiplier()) + "x",
                    NamedTextColor.GREEN
            ));
            return true;
        }

        double multiplier;
        try {
            multiplier = Double.parseDouble(args[1]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(Component.text("Hunger multiplier must be a number.", NamedTextColor.RED));
            return true;
        }

        if (multiplier < 0.0D || multiplier > 10.0D) {
            sender.sendMessage(Component.text("Hunger multiplier must be between 0 and 10.", NamedTextColor.RED));
            return true;
        }

        plugin.setHungerSpeedMultiplier(multiplier);
        sender.sendMessage(Component.text(
                "Hunger speed multiplier set to " + formatDecimal(plugin.getHungerSpeedMultiplier()) + "x.",
                NamedTextColor.GREEN
        ));
        return true;
    }

    private boolean handleStabilityCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /terra stability <status|enable|disable|scan|debug|meter|radius|delay|span|supportradius|debugradius|supports|strictness>", NamedTextColor.RED));
            return true;
        }

        if (args[1].equalsIgnoreCase("status")) {
            sender.sendMessage(Component.text(
                    "Stability: " + (plugin.isStabilityEnabled() ? "enabled" : "disabled")
                            + " | Scan Radius: " + plugin.getStabilityScanRadius()
                            + " | Delay: " + (plugin.getStabilityWarningDelayTicks() / 20.0D) + "s"
                            + " | Span loose/fragile: " + plugin.getStabilityLooseMaxSpan() + "/" + plugin.getStabilityFragileMaxSpan()
                            + " | Support Radius H/V: " + plugin.getStabilitySupportHorizontalRadius() + "/" + plugin.getStabilitySupportVerticalRadius()
                            + " | Debug Radius: " + formatDecimal(plugin.getStabilityDebugViewRadiusBlocks())
                            + " | Strictness: " + plugin.getStabilityStrictnessPercent() + "%"
                            + " | Pending: " + plugin.getPendingStabilityCollapseCount(),
                    NamedTextColor.GREEN
            ));
            return true;
        }

        if (args[1].equalsIgnoreCase("debug")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Only players can use stability debug.", NamedTextColor.RED));
                return true;
            }
            boolean enabled = plugin.toggleStabilityDebug(player);
            sender.sendMessage(Component.text(
                    enabled ? "Stability debug enabled." : "Stability debug disabled.",
                    NamedTextColor.GREEN
            ));
            return true;
        }

        if (args[1].equalsIgnoreCase("supports")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Only players can open the support materials GUI.", NamedTextColor.RED));
                return true;
            }
            plugin.openStabilitySupportGui(player);
            return true;
        }

        if (args[1].equalsIgnoreCase("meter")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Only players can manage the stability meter view.", NamedTextColor.RED));
                return true;
            }
            if (args.length == 2 || args[2].equalsIgnoreCase("status")) {
                sender.sendMessage(Component.text(
                        "Stability meter chat is " + (plugin.isStabilityMeterChatEnabled(player.getUniqueId()) ? "enabled" : "disabled")
                                + " | Progress: " + plugin.getStabilityMeterPercent(player.getUniqueId()) + "%",
                        NamedTextColor.GREEN
                ));
                return true;
            }
            if (args[2].equalsIgnoreCase("on")) {
                plugin.setStabilityMeterChatEnabled(player.getUniqueId(), true);
                sender.sendMessage(Component.text("Stability meter chat enabled.", NamedTextColor.GREEN));
                return true;
            }
            if (args[2].equalsIgnoreCase("off")) {
                plugin.setStabilityMeterChatEnabled(player.getUniqueId(), false);
                sender.sendMessage(Component.text("Stability meter chat disabled.", NamedTextColor.GREEN));
                return true;
            }
            sender.sendMessage(Component.text("Usage: /terra stability meter <on|off|status>", NamedTextColor.RED));
            return true;
        }

        if (args[1].equalsIgnoreCase("enable")) {
            plugin.setStabilityEnabled(true);
            sender.sendMessage(Component.text("Stability system enabled.", NamedTextColor.GREEN));
            return true;
        }

        if (args[1].equalsIgnoreCase("disable")) {
            plugin.setStabilityEnabled(false);
            sender.sendMessage(Component.text("Stability system disabled and pending cave-ins cleared.", NamedTextColor.GREEN));
            return true;
        }

        if (args[1].equalsIgnoreCase("scan")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Only players can run a local stability scan.", NamedTextColor.RED));
                return true;
            }
            boolean queued = plugin.scanStabilityAt(player.getLocation());
            sender.sendMessage(Component.text(
                    queued ? "Stability scan found an unstable area." : "No unstable area found in scan range.",
                    queued ? NamedTextColor.YELLOW : NamedTextColor.GREEN
            ));
            return true;
        }

        if (args[1].equalsIgnoreCase("radius")) {
            if (args.length != 3) {
                sender.sendMessage(Component.text("Usage: /terra stability radius <blocks>", NamedTextColor.RED));
                return true;
            }
            try {
                int radius = Integer.parseInt(args[2]);
                plugin.setStabilityScanRadius(radius);
                sender.sendMessage(Component.text("Stability scan radius set to " + plugin.getStabilityScanRadius() + " blocks.", NamedTextColor.GREEN));
            } catch (NumberFormatException exception) {
                sender.sendMessage(Component.text("Radius must be a whole number.", NamedTextColor.RED));
            }
            return true;
        }

        if (args[1].equalsIgnoreCase("delay")) {
            if (args.length != 3) {
                sender.sendMessage(Component.text("Usage: /terra stability delay <seconds>", NamedTextColor.RED));
                return true;
            }
            try {
                double seconds = Double.parseDouble(args[2]);
                plugin.setStabilityWarningDelayTicks((int) Math.round(Math.max(0.25D, seconds) * 20.0D));
                sender.sendMessage(Component.text("Stability warning delay set to " + formatDecimal(plugin.getStabilityWarningDelayTicks() / 20.0D) + "s.", NamedTextColor.GREEN));
            } catch (NumberFormatException exception) {
                sender.sendMessage(Component.text("Delay must be a number.", NamedTextColor.RED));
            }
            return true;
        }

        if (args[1].equalsIgnoreCase("span")) {
            if (args.length != 4) {
                sender.sendMessage(Component.text("Usage: /terra stability span <loose|fragile> <blocks>", NamedTextColor.RED));
                return true;
            }
            try {
                int span = Integer.parseInt(args[3]);
                if (args[2].equalsIgnoreCase("loose")) {
                    plugin.setStabilityLooseMaxSpan(span);
                    sender.sendMessage(Component.text("Loose-material max span set to " + plugin.getStabilityLooseMaxSpan() + " blocks.", NamedTextColor.GREEN));
                    return true;
                }
                if (args[2].equalsIgnoreCase("fragile")) {
                    plugin.setStabilityFragileMaxSpan(span);
                    sender.sendMessage(Component.text("Fragile-roof max span set to " + plugin.getStabilityFragileMaxSpan() + " blocks.", NamedTextColor.GREEN));
                    return true;
                }
                sender.sendMessage(Component.text("Use loose or fragile.", NamedTextColor.RED));
            } catch (NumberFormatException exception) {
                sender.sendMessage(Component.text("Span must be a whole number.", NamedTextColor.RED));
            }
            return true;
        }

        if (args[1].equalsIgnoreCase("supportradius")) {
            if (args.length != 4) {
                sender.sendMessage(Component.text("Usage: /terra stability supportradius <horizontal> <vertical>", NamedTextColor.RED));
                return true;
            }
            try {
                int horizontal = Integer.parseInt(args[2]);
                int vertical = Integer.parseInt(args[3]);
                plugin.setStabilitySupportHorizontalRadius(horizontal);
                plugin.setStabilitySupportVerticalRadius(vertical);
                sender.sendMessage(Component.text(
                        "Support radius set to H " + plugin.getStabilitySupportHorizontalRadius() + " / V " + plugin.getStabilitySupportVerticalRadius() + ".",
                        NamedTextColor.GREEN
                ));
            } catch (NumberFormatException exception) {
                sender.sendMessage(Component.text("Support radius values must be whole numbers.", NamedTextColor.RED));
            }
            return true;
        }

        if (args[1].equalsIgnoreCase("debugradius")) {
            if (args.length != 3) {
                sender.sendMessage(Component.text("Usage: /terra stability debugradius <blocks>", NamedTextColor.RED));
                return true;
            }
            try {
                double radius = Double.parseDouble(args[2]);
                plugin.setStabilityDebugViewRadiusBlocks(radius);
                sender.sendMessage(Component.text(
                        "Stability debug radius set to " + formatDecimal(plugin.getStabilityDebugViewRadiusBlocks()) + " blocks.",
                        NamedTextColor.GREEN
                ));
            } catch (NumberFormatException exception) {
                sender.sendMessage(Component.text("Debug radius must be a number.", NamedTextColor.RED));
            }
            return true;
        }

        if (args[1].equalsIgnoreCase("strictness")) {
            if (args.length != 3) {
                sender.sendMessage(Component.text("Usage: /terra stability strictness <percent>", NamedTextColor.RED));
                return true;
            }
            try {
                int percent = Integer.parseInt(args[2]);
                plugin.setStabilityStrictnessPercent(percent);
                sender.sendMessage(Component.text(
                        "Stability strictness set to " + plugin.getStabilityStrictnessPercent() + "%.",
                        NamedTextColor.GREEN
                ));
            } catch (NumberFormatException exception) {
                sender.sendMessage(Component.text("Strictness percent must be a whole number.", NamedTextColor.RED));
            }
            return true;
        }

        sender.sendMessage(Component.text("Usage: /terra stability <status|enable|disable|scan|debug|meter|radius|delay|span|supportradius|debugradius|supports|strictness>", NamedTextColor.RED));
        return true;
    }

    private boolean handleItemsCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessage("terra.items.usage"));
            return true;
        }

        String target = args[1].toLowerCase(Locale.ROOT);
        if (target.equals("list")) {
            int page = 1;
            if (args.length >= 3) {
                try {
                    page = Integer.parseInt(args[2]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(plugin.getMessage("terra.items.invalid-page"));
                    return true;
                }
            }

            List<String> materials = plugin.getRestrictableFunctionalMaterialNames();
            int totalPages = Math.max(1, (int) Math.ceil(materials.size() / (double) BLOCKS_PER_PAGE));
            if (page < 1 || page > totalPages) {
                sender.sendMessage(plugin.getMessage("terra.items.invalid-page-range", plugin.placeholders(
                        "pages", String.valueOf(totalPages)
                )));
                return true;
            }

            int start = (page - 1) * BLOCKS_PER_PAGE;
            int end = Math.min(start + BLOCKS_PER_PAGE, materials.size());
            sender.sendMessage(plugin.getMessage("terra.items.list-header", plugin.placeholders(
                    "page", String.valueOf(page),
                    "pages", String.valueOf(totalPages)
            )));
            for (int index = start; index < end; index++) {
                String materialName = materials.get(index);
                Material material = Material.matchMaterial(materialName.toUpperCase(Locale.ROOT));
                sender.sendMessage(plugin.getMessage("terra.items.list-entry", plugin.placeholders(
                        "material", materialName,
                        "status", material != null && plugin.isFunctionalMaterialEnabled(material) ? "enabled" : "disabled"
                )));
            }
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(plugin.getMessage("terra.items.usage"));
            return true;
        }

        if (args[2].equalsIgnoreCase("status")) {
            if (target.equals("enderpearls")) {
                sender.sendMessage(plugin.getMessage("terra.items.enderpearls-status", plugin.placeholders(
                        "status", plugin.areEnderPearlsEnabled() ? "enabled" : "disabled"
                )));
                return true;
            }
            if (target.equals("shulkerboxes")) {
                sender.sendMessage(plugin.getMessage("terra.items.shulkerboxes-status", plugin.placeholders(
                        "status", plugin.areShulkerBoxesEnabled() ? "enabled" : "disabled"
                )));
                return true;
            }
            Material material = plugin.matchRestrictableFunctionalMaterial(target);
            if (material != null) {
                sender.sendMessage(plugin.getMessage("terra.items.material-status", plugin.placeholders(
                        "material", material.name().toLowerCase(Locale.ROOT),
                        "status", plugin.isFunctionalMaterialEnabled(material) ? "enabled" : "disabled"
                )));
                return true;
            }
            sender.sendMessage(plugin.getMessage("terra.items.unknown-material"));
            return true;
        }

        Boolean enabled = parseClimateToggle(args[2]);
        if (enabled == null) {
            sender.sendMessage(plugin.getMessage("terra.items.usage"));
            return true;
        }

        if (target.equals("enderpearls")) {
            plugin.setEnderPearlsEnabled(enabled);
            sender.sendMessage(plugin.getMessage("terra.items.enderpearls-set", plugin.placeholders(
                    "status", enabled ? "enabled" : "disabled"
            )));
            return true;
        }

        if (target.equals("shulkerboxes")) {
            plugin.setShulkerBoxesEnabled(enabled);
            sender.sendMessage(plugin.getMessage("terra.items.shulkerboxes-set", plugin.placeholders(
                    "status", enabled ? "enabled" : "disabled"
            )));
            return true;
        }

        Material material = plugin.matchRestrictableFunctionalMaterial(target);
        if (material != null) {
            plugin.setFunctionalMaterialEnabled(material, enabled);
            sender.sendMessage(plugin.getMessage("terra.items.material-set", plugin.placeholders(
                    "material", material.name().toLowerCase(Locale.ROOT),
                    "status", enabled ? "enabled" : "disabled"
            )));
            return true;
        }

        sender.sendMessage(plugin.getMessage("terra.items.unknown-material"));
        return true;
    }

    private void sendUsage(CommandSender sender, int page) {
        List<HelpEntry> entries = getVisibleHelpEntries(sender);
        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) HELP_ENTRIES_PER_PAGE));
        if (page < 1 || page > totalPages) {
            sender.sendMessage(Component.text("Help page must be between 1 and " + totalPages + ".", NamedTextColor.RED));
            return;
        }

        int start = (page - 1) * HELP_ENTRIES_PER_PAGE;
        int end = Math.min(start + HELP_ENTRIES_PER_PAGE, entries.size());

        sender.sendMessage(Component.text(" "));
        sender.sendMessage(Component.text("Terra Help", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text("  Page " + page + "/" + totalPages, NamedTextColor.GRAY, TextDecoration.BOLD)));

        for (int i = start; i < end; i++) {
            HelpEntry entry = entries.get(i);
            sender.sendMessage(Component.text("• ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(entry.command, NamedTextColor.YELLOW))
                    .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(entry.description, NamedTextColor.GRAY)));
        }

        sender.sendMessage(buildHelpFooter(page, totalPages));
    }

    private Component buildHelpFooter(int page, int totalPages) {
        Component footer = Component.text("Use ", NamedTextColor.DARK_GRAY)
                .append(Component.text("/terra help <page>", NamedTextColor.YELLOW))
                .append(Component.text(" to open a page.", NamedTextColor.DARK_GRAY));

        if (totalPages <= 1) {
            return footer;
        }

        Component navigation = Component.text(" ", NamedTextColor.WHITE);
        if (page > 1) {
            navigation = navigation.append(buildHelpButton("Previous", page - 1, NamedTextColor.GREEN));
        } else {
            navigation = navigation.append(Component.text("[Previous]", NamedTextColor.DARK_GRAY));
        }

        navigation = navigation.append(Component.text("  ", NamedTextColor.WHITE));

        if (page < totalPages) {
            navigation = navigation.append(buildHelpButton("Next", page + 1, NamedTextColor.GOLD));
        } else {
            navigation = navigation.append(Component.text("[Next]", NamedTextColor.DARK_GRAY));
        }

        return footer.append(Component.newline()).append(navigation);
    }

    private Component buildHelpButton(String label, int targetPage, NamedTextColor color) {
        return Component.text("[" + label + "]", color, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/terra help " + targetPage))
                .hoverEvent(HoverEvent.showText(Component.text("Open /terra help " + targetPage, NamedTextColor.GRAY)));
    }

    private List<HelpEntry> getVisibleHelpEntries(CommandSender sender) {
        boolean admin = hasAdminAccess(sender);
        List<HelpEntry> entries = new ArrayList<>();
        entries.add(new HelpEntry("/terra help [page]", "Open this command list.", ignored -> true));
        entries.add(new HelpEntry("/terra tutorial", "Advance tutorial prompts and manage onboarding markers and NPCs.", ignored -> sender instanceof Player));

        if (!admin) {
            return entries;
        }

        entries.add(new HelpEntry("/terra reload", "Reload Terra configs.", ignored -> true));
        entries.add(new HelpEntry("/terra quests", "Open the quest admin GUI.", ignored -> sender instanceof Player));
        entries.add(new HelpEntry("/terra catalog", "Open the Terra crafting catalog GUI.", ignored -> sender instanceof Player));
        entries.add(new HelpEntry("/terra questdebug", "Show the active quest debug info.", ignored -> sender instanceof Player));
        entries.add(new HelpEntry("/terra hardrestart", "Queue the hard restart flow.", ignored -> true));
        entries.add(new HelpEntry("/terra blockdelay <enable|disable>", "Toggle block delay.", ignored -> true));
        entries.add(new HelpEntry("/terra blockdelay time set <seconds>", "Set the block delay time.", ignored -> true));
        entries.add(new HelpEntry("/terra blockvalue list [page]", "Browse configured block rewards.", ignored -> true));
        entries.add(new HelpEntry("/terra blockvalue <block>", "View reward values for a block.", ignored -> true));
        entries.add(new HelpEntry("/terra blockvalue <block> set <xp|money> <value>", "Edit block reward values.", ignored -> true));
        entries.add(new HelpEntry("/terra bypass [player]", "Toggle block-delay bypass for yourself or another player.", ignored -> true));
        entries.add(new HelpEntry("/terra craftbypass [player]", "Toggle crafting-requirement bypass using the same bypass state.", ignored -> true));
        entries.add(new HelpEntry("/terra bypasslist", "List players with bypass enabled.", ignored -> true));
        entries.add(new HelpEntry("/terra wildernessregen", "Show wilderness regeneration timers.", ignored -> true));
        entries.add(new HelpEntry("/terra wildernessregen <break|build> <seconds>", "Set wilderness regeneration timers.", ignored -> true));
        entries.add(new HelpEntry("/terra fixedore <create|fill|delete>", "Manage fixed ore nodes.", ignored -> sender instanceof Player));
        entries.add(new HelpEntry("/terra fixedoretool", "Get the fixed ore remover tool.", ignored -> sender instanceof Player));
        entries.add(new HelpEntry("/terra rewards <enable|disable|money>", "Control block reward systems.", ignored -> true));
        entries.add(new HelpEntry("/terra hostilemobs <enable|disable>", "Toggle hostile mob spawning.", ignored -> true));
        entries.add(new HelpEntry("/terra phantoms <enable|disable>", "Toggle phantom spawning.", ignored -> true));
        entries.add(new HelpEntry("/terra setxpboost <amount> <time|off>", "Manage the global XP boost.", ignored -> true));
        entries.add(new HelpEntry("/terra cleardata <player>", "Reset a player's Terra data.", ignored -> true));
        entries.add(new HelpEntry("/terra jobcap <job> <amount>", "Set a profession player cap.", ignored -> true));
        entries.add(new HelpEntry("/terra jobeditor [job] [level]", "Open the profession config editor GUI.", ignored -> sender instanceof Player));
        entries.add(new HelpEntry("/terra playtest [manage|start|stop|status]", "Open or control the playtest manager.", ignored -> true));
        entries.add(new HelpEntry("/terra trader <status|time|spawn|remove|open>", "Manage the traveling trader.", ignored -> true));
        entries.add(new HelpEntry("/terra merchant <status|time|spawn|remove|open|manage>", "Manage the merchant wave.", ignored -> true));
        entries.add(new HelpEntry("/terra items <list|material> <on|off|status>", "Toggle functional items and blocks.", ignored -> true));
        entries.add(new HelpEntry("/rollbackarea <radius> <time>", "Roll back an area around you with CoreProtect.", ignored -> sender instanceof Player));
        entries.add(new HelpEntry("/undoarea <radius> <time>", "Restore a rolled back area around you with CoreProtect.", ignored -> sender instanceof Player));
        entries.add(new HelpEntry("/flyspeed <0-10>", "Set your fly speed.", ignored -> sender instanceof Player));
        entries.add(new HelpEntry("/vanish [on|off|toggle]", "Toggle vanish mode.", ignored -> sender instanceof Player));
        entries.add(new HelpEntry("/terra orevision", "Toggle ore vision.", ignored -> sender instanceof Player));
        entries.add(new HelpEntry("/terra setworldspawn", "Set the global world spawn.", ignored -> sender instanceof Player));
        entries.add(new HelpEntry("/terra cooldowndebug", "Toggle cooldown debug bossbars.", ignored -> sender instanceof Player));
        entries.add(new HelpEntry("/terra lag <status|clearitems|stacknow|itemclear|mobstack|itemmerge>", "Manage lag-reduction tools.", ignored -> true));
        entries.add(new HelpEntry("/terra maintenance <on|off|status|add|remove|list>", "Manage maintenance mode access.", ignored -> true));
        entries.add(new HelpEntry("/terra clearinventory <player>", "Clear a player's inventory but keep the Terra Guide.", ignored -> true));
        entries.add(new HelpEntry("/terra catalyst <player> <type> [amount]", "Give forge catalysts for merge testing.", ignored -> true));
        entries.add(new HelpEntry("/terra admincatalyst Meetrow [amount]", "Give Meetrow the 100% merge admin catalyst.", ignored -> true));
        entries.add(new HelpEntry("/terra realtimeclock <on|off|status|sync>", "Control the real-time day/night clock.", ignored -> true));
        entries.add(new HelpEntry("/terra hungerspeed <multiplier|status>", "Set the global hunger drain speed.", ignored -> true));
        entries.add(new HelpEntry("/terra stability <status|enable|disable|scan|debug|meter|radius|delay|span|supportradius|debugradius|supports|strictness>", "Manage cave-in and support rules.", ignored -> true));
        return entries.stream()
                .filter(entry -> entry.isVisibleTo(sender))
                .toList();
    }

    private void sendBlockList(CommandSender sender, int page) {
        if (page < 1) {
            sender.sendMessage(plugin.getMessage("terra.blockvalue.invalid-page-min"));
            return;
        }

        List<Material> blocks = getAllBlocks();
        int totalPages = Math.max(1, (int) Math.ceil(blocks.size() / (double) BLOCKS_PER_PAGE));
        if (page > totalPages) {
            sender.sendMessage(plugin.getMessage("terra.blockvalue.only-pages", plugin.placeholders("pages", String.valueOf(totalPages))));
            return;
        }

        int start = (page - 1) * BLOCKS_PER_PAGE;
        int end = Math.min(start + BLOCKS_PER_PAGE, blocks.size());
        sender.sendMessage(plugin.getMessage("terra.blockvalue.page-header",
                plugin.placeholders("page", String.valueOf(page), "pages", String.valueOf(totalPages))));
        for (int i = start; i < end; i++) {
            Material material = blocks.get(i);
            BlockReward reward = plugin.getBlockReward(material);
            sender.sendMessage(plugin.getMessage("terra.blockvalue.page-entry", plugin.placeholders(
                    "block", material.name(),
                    "xp", String.valueOf(reward.xp()),
                    "money", String.format("%.2f", reward.money())
            )));
        }
    }

    private Material parseBlockMaterial(String input) {
        Material material = Material.matchMaterial(input);
        if (material == null || !material.isBlock() || material.isAir()) {
            return null;
        }
        return material;
    }

    private List<Material> getAllBlocks() {
        return List.of(Material.values()).stream()
                .filter(Material::isBlock)
                .filter(material -> !material.isAir())
                .sorted(Comparator.comparing(Material::name))
                .collect(Collectors.toList());
    }

    private String formatMaterialName(Material material) {
        String lower = material.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        String[] words = lower.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.toString();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("flyspeed")) {
            if (!hasStaffAccess(sender)) {
                return Collections.emptyList();
            }
            if (args.length == 1) {
                return partialMatches(args[0], List.of("0", "1", "3", "5", "7", "10"));
            }
            return Collections.emptyList();
        }

        if (command.getName().equalsIgnoreCase("vanish")) {
            if (!hasStaffAccess(sender)) {
                return Collections.emptyList();
            }
            if (args.length == 1) {
                return partialMatches(args[0], List.of("on", "off", "toggle"));
            }
            return Collections.emptyList();
        }

        if (command.getName().equalsIgnoreCase("trader")) {
            if (!hasAdminAccess(sender)) {
                return Collections.emptyList();
            }
            return completeTraderOrMerchantRoot("trader", args);
        }

        if (command.getName().equalsIgnoreCase("merchant")) {
            if (!hasAdminAccess(sender)) {
                return Collections.emptyList();
            }
            return completeTraderOrMerchantRoot("merchant", args);
        }

        if (command.getName().equalsIgnoreCase("climate")) {
            if (!hasAdminAccess(sender)) {
                return Collections.emptyList();
            }
            return completeClimateCommand(sender, args);
        }

        if (command.getName().equalsIgnoreCase("jobs")) {
            return tabCompleteJobsCommand(sender, args);
        }

        if (command.getName().equalsIgnoreCase("rollbackarea")) {
            if (!hasAdminAccess(sender)) {
                return Collections.emptyList();
            }
            if (args.length == 1) {
                return partialMatches(args[0], List.of("5", "10", "20", "50", "100"));
            }
            if (args.length == 2) {
                return partialMatches(args[1], List.of("10m", "30m", "1h", "6h", "1d", "1w"));
            }
            return Collections.emptyList();
        }

        if (command.getName().equalsIgnoreCase("undoarea")) {
            if (!hasAdminAccess(sender)) {
                return Collections.emptyList();
            }
            if (args.length == 1) {
                return partialMatches(args[0], List.of("5", "10", "20", "50", "100"));
            }
            if (args.length == 2) {
                return partialMatches(args[1], List.of("10m", "30m", "1h", "6h", "1d", "1w"));
            }
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return partialMatches(args[0], getVisibleHelpEntries(sender).stream()
                    .map(HelpEntry::rootSubcommand)
                    .distinct()
                    .toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("help")) {
            int totalPages = Math.max(1, (int) Math.ceil(getVisibleHelpEntries(sender).size() / (double) HELP_ENTRIES_PER_PAGE));
            List<String> pages = new ArrayList<>();
            for (int i = 1; i <= totalPages; i++) {
                pages.add(String.valueOf(i));
            }
            return partialMatches(args[1], pages);
        }

        if (args[0].equalsIgnoreCase("tutorial")) {
            return completeTutorialCommand(sender, args);
        }

        if (!hasAdminAccess(sender)) {
            return Collections.emptyList();
        }

        if (args[0].equalsIgnoreCase("blockdelay")) {
            if (args.length == 2) {
                return partialMatches(args[1], List.of("enable", "disable", "time"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("time")) {
                return partialMatches(args[2], Collections.singletonList("set"));
            }
        }

        if (args[0].equalsIgnoreCase("blockvalue")) {
            if (args.length == 2) {
                List<String> options = new ArrayList<>();
                options.add("list");
                for (Material material : getAllBlocks()) {
                    options.add(material.name().toLowerCase(Locale.ROOT));
                }
                return partialMatches(args[1], options);
            }
            if (args.length == 3 && !args[1].equalsIgnoreCase("list")) {
                return partialMatches(args[2], Collections.singletonList("set"));
            }
            if (args.length == 4 && !args[1].equalsIgnoreCase("list") && args[2].equalsIgnoreCase("set")) {
                return partialMatches(args[3], List.of("xp", "money"));
            }
        }

        if (args[0].equalsIgnoreCase("bypass") && args.length == 2) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return partialMatches(args[1], playerNames);
        }

        if (args[0].equalsIgnoreCase("craftbypass") && args.length == 2) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return partialMatches(args[1], playerNames);
        }

        if (args[0].equalsIgnoreCase("wildernessregen")) {
            if (args.length == 2) {
                return partialMatches(args[1], List.of("set"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("set")) {
                return partialMatches(args[2], List.of("break", "build"));
            }
        }

        if (args[0].equalsIgnoreCase("fixedore")) {
            if (args.length == 2) {
                return partialMatches(args[1], List.of("create", "fill", "delete"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("create")) {
                return partialMatches(args[2], plugin.getFixedOreMaterialNames());
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("fill")) {
                return partialMatches(args[2], getAllBlockMaterialNames());
            }
            if (args.length == 4 && args[1].equalsIgnoreCase("fill")) {
                return partialMatches(args[3], plugin.getFixedOreMaterialNames());
            }
        }

        if (args[0].equalsIgnoreCase("rewards") && args.length == 2) {
            return partialMatches(args[1], List.of("enable", "disable", "money"));
        }

        if (args[0].equalsIgnoreCase("rewards") && args.length == 3 && args[1].equalsIgnoreCase("money")) {
            return partialMatches(args[2], List.of("enable", "disable"));
        }

        if (args[0].equalsIgnoreCase("hostilemobs") && args.length == 2) {
            return partialMatches(args[1], List.of("enable", "disable"));
        }

        if (args[0].equalsIgnoreCase("phantoms") && args.length == 2) {
            return partialMatches(args[1], List.of("enable", "disable"));
        }

        if (args[0].equalsIgnoreCase("setxpboost")) {
            if (args.length == 2) {
                List<String> options = new ArrayList<>();
                options.add("off");
                for (double amount : XP_BOOST_AMOUNTS) {
                    options.add(plugin.formatXpBoostMultiplier(amount));
                }
                return partialMatches(args[1], options);
            }
            if (args.length == 3) {
                return partialMatches(args[2], List.of("15m", "30m", "1h", "2h"));
            }
        }

        if (args[0].equalsIgnoreCase("cleardata") && args.length == 2) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (offlinePlayer.getName() != null) {
                    playerNames.add(offlinePlayer.getName());
                }
            }
            return partialMatches(args[1], playerNames);
        }

        if (args[0].equalsIgnoreCase("jobcap")) {
            if (args.length == 2) {
                return partialMatches(args[1], getAllJobKeys());
            }
            if (args.length == 3) {
                return partialMatches(args[2], List.of("10", "20", "30", "0"));
            }
        }

        if (args[0].equalsIgnoreCase("jobeditor")) {
            if (args.length == 2) {
                return partialMatches(args[1], getAllJobKeys());
            }
            if (args.length == 3) {
                return partialMatches(args[2], List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
            }
        }

        if (args[0].equalsIgnoreCase("playtest")) {
            if (args.length == 2) {
                return partialMatches(args[1], List.of("manage", "start", "extend", "stop", "status"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("start")) {
                return partialMatches(args[2], List.of("30m", "1h", "2h", "1d", "1w"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("extend")) {
                return partialMatches(args[2], List.of("5m", "10m", "30m", "1h", "1d"));
            }
        }

        if (args[0].equalsIgnoreCase("climate")) {
            return completeClimateCommand(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        if (args[0].equalsIgnoreCase("realtimeclock")) {
            if (args.length == 2) {
                return partialMatches(args[1], List.of("on", "off", "status", "sync"));
            }
            return Collections.emptyList();
        }

        if (args[0].equalsIgnoreCase("hungerspeed")) {
            if (args.length == 2) {
                return partialMatches(args[1], List.of("status", "0", "0.5", "1", "1.5", "2", "3"));
            }
            return Collections.emptyList();
        }

        if (args[0].equalsIgnoreCase("stability")) {
            if (args.length == 2) {
                return partialMatches(args[1], List.of("status", "enable", "disable", "scan", "debug", "meter", "radius", "delay", "span", "supportradius", "debugradius", "supports", "strictness"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("meter")) {
                return partialMatches(args[2], List.of("on", "off", "status"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("radius")) {
                return partialMatches(args[2], List.of("6", "8", "10", "12"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("delay")) {
                return partialMatches(args[2], List.of("0.5", "1", "2", "3"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("span")) {
                return partialMatches(args[2], List.of("loose", "fragile"));
            }
            if (args.length == 4 && args[1].equalsIgnoreCase("span")) {
                return partialMatches(args[3], List.of("2", "3", "5", "6", "8"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("supportradius")) {
                return partialMatches(args[2], List.of("1", "2", "3", "4"));
            }
            if (args.length == 4 && args[1].equalsIgnoreCase("supportradius")) {
                return partialMatches(args[3], List.of("1", "2", "3"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("debugradius")) {
                return partialMatches(args[2], List.of("4", "6", "8", "10"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("strictness")) {
                return partialMatches(args[2], List.of("75", "100", "125", "150"));
            }
            return Collections.emptyList();
        }


        if (args[0].equalsIgnoreCase("items")) {
            if (args.length == 2) {
                List<String> options = new ArrayList<>(List.of("list", "enderpearls", "shulkerboxes"));
                options.addAll(plugin.getRestrictableFunctionalMaterialNames());
                return partialMatches(args[1], options);
            }
            if (args.length == 3) {
                if (args[1].equalsIgnoreCase("list")) {
                    return partialMatches(args[2], List.of("1", "2", "3", "4", "5"));
                }
                return partialMatches(args[2], List.of("on", "off", "status"));
            }
            return Collections.emptyList();
        }

        if (args[0].equalsIgnoreCase("lag")) {
            if (args.length == 2) {
                return partialMatches(args[1], List.of("status", "clearitems", "stacknow", "itemclear", "mobstack", "itemmerge"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("itemclear")) {
                return partialMatches(args[2], List.of("on", "off", "interval"));
            }
            if (args.length == 4 && args[1].equalsIgnoreCase("itemclear") && args[2].equalsIgnoreCase("interval")) {
                return partialMatches(args[3], List.of("3", "5", "10", "15"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("mobstack")) {
                return partialMatches(args[2], List.of("on", "off", "radius", "max"));
            }
            if (args.length == 4 && args[1].equalsIgnoreCase("mobstack") && args[2].equalsIgnoreCase("radius")) {
                return partialMatches(args[3], List.of("6", "8", "10", "12"));
            }
            if (args.length == 4 && args[1].equalsIgnoreCase("mobstack") && args[2].equalsIgnoreCase("max")) {
                return partialMatches(args[3], List.of("25", "50", "75", "100"));
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("itemmerge")) {
                return partialMatches(args[2], List.of("on", "off", "radius"));
            }
            if (args.length == 4 && args[1].equalsIgnoreCase("itemmerge") && args[2].equalsIgnoreCase("radius")) {
                return partialMatches(args[3], List.of("1.5", "2.5", "3.5", "5"));
            }
            return Collections.emptyList();
        }

        if (args[0].equalsIgnoreCase("maintenance")) {
            if (args.length == 2) {
                return partialMatches(args[1], List.of("on", "off", "status", "add", "remove", "list"));
            }
            if (args.length == 3 && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
                return partialMatches(args[2], getKnownPlayerNames());
            }
            return Collections.emptyList();
        }

        if (args[0].equalsIgnoreCase("catalog")) {
            return Collections.emptyList();
        }

        if (args[0].equalsIgnoreCase("guieditor")) {
            if (args.length == 2) {
                List<String> options = new ArrayList<>(plugin.getTerraGuiEditorScreens());
                options.addAll(List.of("list", "reset"));
                return partialMatches(args[1], options);
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
                return partialMatches(args[2], plugin.getTerraGuiEditorScreens());
            }
            return Collections.emptyList();
        }

        if (args[0].equalsIgnoreCase("clearinventory")) {
            if (args.length == 2) {
                return partialMatches(args[1], getKnownPlayerNames());
            }
            return Collections.emptyList();
        }

        if (args[0].equalsIgnoreCase("catalyst")) {
            if (args.length == 2) {
                return partialMatches(args[1], getKnownPlayerNames());
            }
            if (args.length == 3) {
                return partialMatches(args[2], List.of("forge_shard", "tempered_flux", "binding_thread", "runic_prism", "ancient_core"));
            }
            if (args.length == 4) {
                return partialMatches(args[3], List.of("1", "5", "10", "16", "32", "64"));
            }
            return Collections.emptyList();
        }

        if (args[0].equalsIgnoreCase("admincatalyst")) {
            if (args.length == 2) {
                return partialMatches(args[1], List.of("Meetrow"));
            }
            if (args.length == 3) {
                return partialMatches(args[2], List.of("1", "2", "5", "10", "16", "32", "64"));
            }
            return Collections.emptyList();
        }

        if (args[0].equalsIgnoreCase("trader") && args.length == 2) {
            return partialMatches(args[1], List.of("status", "time", "spawn", "remove", "open"));
        }

        if (args[0].equalsIgnoreCase("trader") && args.length == 3 && args[1].equalsIgnoreCase("spawn")) {
            return partialMatches(args[2], Arrays.stream(Profession.values())
                    .map(Profession::getKey)
                    .toList());
        }

        if (args[0].equalsIgnoreCase("trader") && args.length == 3 && args[1].equalsIgnoreCase("time")) {
            return partialMatches(args[2], List.of("status", "next", "active"));
        }

        if (args[0].equalsIgnoreCase("trader") && args.length == 4 && args[1].equalsIgnoreCase("time")) {
            return partialMatches(args[3], List.of("0", "10", "30", "60", "180"));
        }

        if (args[0].equalsIgnoreCase("merchant") && args.length == 2) {
            return partialMatches(args[1], List.of("status", "time", "spawn", "remove", "open", "manage"));
        }

        if (args[0].equalsIgnoreCase("merchant") && args.length == 3 && args[1].equalsIgnoreCase("time")) {
            return partialMatches(args[2], List.of("status", "next", "active"));
        }

        if (args[0].equalsIgnoreCase("merchant") && args.length == 4 && args[1].equalsIgnoreCase("time")) {
            return partialMatches(args[3], List.of("0", "2", "10", "30", "60"));
        }

        if (args[0].equalsIgnoreCase("flyspeed") && args.length == 2) {
            return partialMatches(args[1], List.of("0", "1", "3", "5", "7", "10"));
        }

        if (args[0].equalsIgnoreCase("vanish") && args.length == 2) {
            return partialMatches(args[1], List.of("on", "off", "toggle"));
        }

        if (args[0].equalsIgnoreCase("orevision")) {
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    private List<String> completeClimateCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return partialMatches(args[0], List.of(
                    "check", "status", "enable", "unit", "seasons", "season", "bossbar",
                    "freeze", "display", "altitude", "playtest", "create", "refresh", "clear", "crops"
            ));
        }
        if (args.length == 2 && List.of("enable", "seasons", "bossbar", "freeze", "display").contains(args[0].toLowerCase(Locale.ROOT))) {
            return partialMatches(args[1], List.of("on", "off"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("season")) {
            return partialMatches(args[1], List.of("auto", "spring", "summer", "autumn", "winter"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("altitude")) {
            return partialMatches(args[1], List.of("optimal"));
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("altitude") && args[1].equalsIgnoreCase("optimal")) {
            return partialMatches(args[2], List.of("63", "70", "80"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("clear")) {
            return partialMatches(args[1], List.of("all"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            return partialMatches(args[1], List.of("fullworld"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("playtest")) {
            return partialMatches(args[1], List.of("on", "off", "here", "center", "radius", "temps"));
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("playtest") && args[1].equalsIgnoreCase("radius")) {
            return partialMatches(args[2], List.of("64", "128", "256", "512"));
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("playtest") && args[1].equalsIgnoreCase("center") && sender instanceof Player player) {
            return partialMatches(args[2], List.of(String.valueOf(player.getLocation().getBlockX()), "0"));
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("playtest") && args[1].equalsIgnoreCase("center") && sender instanceof Player player) {
            return partialMatches(args[3], List.of(String.valueOf(player.getLocation().getBlockZ()), "0"));
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("playtest") && args[1].equalsIgnoreCase("temps")) {
            return partialMatches(args[2], List.of("28", "30", "32"));
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("playtest") && args[1].equalsIgnoreCase("temps")) {
            return partialMatches(args[3], List.of("12", "16", "18", "20"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("unit")) {
            return partialMatches(args[1], List.of("C", "F"));
        }
        return Collections.emptyList();
    }

    private String signedTemperature(double temperatureCelsius, Testproject plugin) {
        String formatted = plugin.formatTemperature(Math.abs(temperatureCelsius));
        return (temperatureCelsius >= 0.0D ? "+" : "-") + formatted;
    }

    private String formatDecimal(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001D) {
            return String.format(Locale.US, "%.0f", value);
        }
        return String.format(Locale.US, "%.2f", value)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }

    private String formatDuration(int totalSeconds) {
        int safeSeconds = Math.max(0, totalSeconds);
        int days = safeSeconds / 86_400;
        int hours = (safeSeconds % 86_400) / 3_600;
        int minutes = (safeSeconds % 3_600) / 60;
        int seconds = safeSeconds % 60;
        List<String> parts = new ArrayList<>();
        if (days > 0) {
            parts.add(days + "d");
        }
        if (hours > 0 || !parts.isEmpty()) {
            parts.add(hours + "h");
        }
        if (minutes > 0 || !parts.isEmpty()) {
            parts.add(minutes + "m");
        }
        parts.add(seconds + "s");
        return String.join(" ", parts);
    }

    private String prettyProfessionName(Profession profession) {
        return profession != null ? profession.getDisplayName() : "Unknown";
    }

    private String prettifyMaterialName(Material material) {
        if (material == null) {
            return "Unknown";
        }

        String[] parts = material.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    private String joinArgs(String[] args, int startInclusive, int endExclusive) {
        StringBuilder builder = new StringBuilder();
        for (int index = Math.max(0, startInclusive); index < Math.min(args.length, endExclusive); index++) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(args[index]);
        }
        return builder.toString().trim();
    }

    private String percent(double value) {
        return String.format(Locale.US, "%.0f%%", Math.max(0.0D, Math.min(1.0D, value)) * 100.0D);
    }

    private String signedPercent(double value) {
        return String.format(Locale.US, "%+.0f%%", Math.max(-1.0D, Math.min(1.0D, value)) * 100.0D);
    }

    private String[] withRootSubcommand(String root, String[] args) {
        String[] expanded = new String[args.length + 1];
        expanded[0] = root;
        System.arraycopy(args, 0, expanded, 1, args.length);
        return expanded;
    }

    private List<String> completeTraderOrMerchantRoot(String root, String[] args) {
        if (args.length == 1) {
            if (root.equalsIgnoreCase("trader") && "spawn".startsWith(args[0].toLowerCase(Locale.ROOT))) {
                return partialMatches(args[0], List.of("status", "time", "spawn", "remove", "open", "manage"));
            }
            return partialMatches(args[0], List.of("status", "time", "spawn", "remove", "open", "manage"));
        }
        if (root.equalsIgnoreCase("trader") && args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            return partialMatches(args[1], Arrays.stream(Profession.values())
                    .map(Profession::getKey)
                    .toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("time")) {
            return partialMatches(args[1], List.of("status", "next", "active"));
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("time")) {
            return partialMatches(args[2], root.equalsIgnoreCase("trader")
                    ? List.of("0", "10", "30", "60", "180")
                    : List.of("0", "2", "10", "30", "60"));
        }
        return Collections.emptyList();
    }

    private List<String> tabCompleteJobsCommand(CommandSender sender, String[] args) {
        if (!hasAdminAccess(sender)) {
            if (args.length == 1) {
                return partialMatches(args[0], List.of("open", "info", "switch"));
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("switch") && sender instanceof Player player) {
                return partialMatches(args[1], getOwnedJobKeys(player));
            }
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return partialMatches(args[0], List.of("open", "info", "switch", "admin"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("switch") && sender instanceof Player player) {
            return partialMatches(args[1], getOwnedJobKeys(player));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            List<String> options = new ArrayList<>(getKnownPlayerNames());
            options.addAll(List.of("info", "setprimary", "setsecondary", "setactive", "setlevel", "setxp", "addskillpoints", "clearsecondary", "clearall"));
            return partialMatches(args[1], options);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("admin")) {
            return partialMatches(args[2], getKnownPlayerNames());
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("admin")
                && List.of("setprimary", "setsecondary", "setactive", "setlevel", "setxp", "addskillpoints").contains(args[1].toLowerCase(Locale.ROOT))) {
            return partialMatches(args[3], getAllJobKeys());
        }
        return Collections.emptyList();
    }

    private List<String> getOwnedJobKeys(Player player) {
        List<String> ownedJobs = new ArrayList<>();
        for (Profession profession : plugin.getOwnedProfessions(player.getUniqueId())) {
            ownedJobs.add(profession.getKey());
        }
        return ownedJobs;
    }

    private List<String> completeTutorialCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }
        if (args.length == 2) {
            return partialMatches(args[1], List.of(
                    "next",
                    "redo",
                    "setlocation",
                    "clearlocation",
                    "locations",
                    "marknpc",
                    "clearnpc",
                    "spawnnpc",
                    "registernpc",
                    "removenpc",
                    "renamenpc",
                    "npcs",
                    "spawnfancynpc",
                    "skinfancynpc",
                    "renamefancynpc",
                    "bindfancynpc",
                    "unbindfancynpc",
                    "fancynpcs"
            ));
        }
        if (!hasAdminAccess(sender)) {
            return Collections.emptyList();
        }
        String action = args[1].toLowerCase(Locale.ROOT);

        if (args.length == 3) {
            if (action.equals("redo")) {
                return partialMatches(args[2], getKnownPlayerNames());
            }
            if (action.equals("setlocation")) {
                List<String> suggestions = new ArrayList<>(plugin.getOnboardingLocationMarkerKeys());
                suggestions.addAll(List.of("starter_hub", "builder_yard", "farm_plots", "forge_yard", "embassy_board", "trader_stop"));
                return partialMatches(args[2], dedupeSuggestions(suggestions));
            }
            if (action.equals("clearlocation")) {
                return partialMatches(args[2], plugin.getOnboardingLocationMarkerKeys());
            }
            if (action.equals("marknpc")) {
                return partialMatches(args[2], getSuggestedTutorialQuestKeys());
            }
            if (action.equals("removenpc") || action.equals("spawnnpc") || action.equals("registernpc") || action.equals("renamenpc")) {
                return partialMatches(args[2], getSuggestedTutorialNpcIds());
            }
            if (action.equals("bindfancynpc") || action.equals("unbindfancynpc") || action.equals("spawnfancynpc") || action.equals("skinfancynpc") || action.equals("renamefancynpc")) {
                return partialMatches(args[2], getSuggestedFancyNpcIds());
            }
        }
        if (args.length == 4) {
            if (action.equals("setlocation")) {
                return partialMatches(args[3], List.of("6", "8", "10", "12", "16", "24"));
            }
            if (action.equals("spawnnpc") || action.equals("registernpc")) {
                return partialMatches(args[3], getSuggestedTutorialQuestKeys());
            }
            if (action.equals("bindfancynpc")) {
                return partialMatches(args[3], getSuggestedTutorialQuestKeys());
            }
            if (action.equals("spawnfancynpc")) {
                return partialMatches(args[3], getSuggestedTutorialQuestKeys());
            }
            if (action.equals("skinfancynpc")) {
                return partialMatches(args[3], getKnownPlayerNames());
            }
        }
        if (args.length == 5 && (action.equals("spawnnpc") || action.equals("registernpc"))) {
            return partialMatches(args[4], List.of(
                    "terra:guide_npc",
                    "terra:trader_guide",
                    "terra:merchant_guide",
                    "terra:embassy_guide",
                    "yournamespace:npc_id"
            ));
        }
        if (args.length == 5 && action.equals("bindfancynpc")) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("-");
            suggestions.addAll(getSuggestedTutorialDialogueKeys());
            return partialMatches(args[4], dedupeSuggestions(suggestions));
        }
        if (args.length == 5 && action.equals("spawnfancynpc")) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("-");
            suggestions.addAll(getSuggestedTutorialDialogueKeys());
            return partialMatches(args[4], dedupeSuggestions(suggestions));
        }
        if (args.length == 6 && (action.equals("spawnnpc") || action.equals("registernpc"))) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("-");
            suggestions.addAll(getSuggestedTutorialDialogueKeys());
            return partialMatches(args[5], dedupeSuggestions(suggestions));
        }
        if (args.length >= 7 && (action.equals("spawnnpc") || action.equals("registernpc"))) {
            return partialMatches(args[args.length - 1], List.of(
                    "Starter Guide",
                    "Embassy Guide",
                    "Trader Guide",
                    "Merchant Guide",
                    "Builder Guide"
            ));
        }
        if (args.length >= 6 && action.equals("bindfancynpc")) {
            return partialMatches(args[args.length - 1], List.of(
                    "Starter Guide",
                    "Embassy Guide",
                    "Trader Guide",
                    "Merchant Guide",
                    "Country Recruiter"
            ));
        }
        if (args.length >= 6 && action.equals("spawnfancynpc")) {
            return partialMatches(args[args.length - 1], List.of(
                    "Starter Guide",
                    "Embassy Guide",
                    "Trader Guide",
                    "Merchant Guide",
                    "Country Recruiter"
            ));
        }
        if (args.length >= 4 && (action.equals("renamenpc") || action.equals("renamefancynpc"))) {
            return partialMatches(args[args.length - 1], List.of(
                    "Starter Guide",
                    "Embassy Guide",
                    "Trader Guide",
                    "Merchant Guide",
                    "Country Recruiter",
                    "Freeport Guide"
            ));
        }
        return Collections.emptyList();
    }

    private List<String> getSuggestedTutorialQuestKeys() {
        List<String> suggestions = new ArrayList<>(plugin.getTutorialQuestKeys());
        suggestions.addAll(List.of(
                "starter_hub",
                "trader_npc",
                "merchant_npc",
                "embassy_guide",
                "builder_guide",
                "farm_guide",
                "forge_guide",
                "country_recruiter"
        ));
        return dedupeSuggestions(suggestions);
    }

    private List<String> getSuggestedTutorialNpcIds() {
        List<String> suggestions = new ArrayList<>(plugin.getOnboardingCustomNpcIds());
        suggestions.addAll(List.of(
                "starter_guide",
                "embassy_guide",
                "builder_guide",
                "farm_guide",
                "forge_guide",
                "country_recruiter"
        ));
        return dedupeSuggestions(suggestions);
    }

    private List<String> getSuggestedFancyNpcIds() {
        List<String> suggestions = new ArrayList<>(plugin.getAvailableFancyNpcIds());
        suggestions.addAll(plugin.getBoundFancyNpcIds());
        return dedupeSuggestions(suggestions);
    }

    private List<String> getSuggestedTutorialDialogueKeys() {
        List<String> suggestions = new ArrayList<>(plugin.getOnboardingDialogueKeys());
        suggestions.addAll(List.of(
                "starter_hub",
                "trader_npc",
                "merchant_npc",
                "embassy_guide",
                "builder_guide",
                "farm_guide",
                "forge_guide",
                "country_recruiter",
                "default"
        ));
        return dedupeSuggestions(suggestions);
    }

    private List<String> dedupeSuggestions(List<String> suggestions) {
        List<String> deduped = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (suggestion != null && !suggestion.isBlank() && !deduped.contains(suggestion)) {
                deduped.add(suggestion);
            }
        }
        return deduped;
    }

    private static final class HelpEntry {
        private final String command;
        private final String description;
        private final Predicate<CommandSender> visibility;

        private HelpEntry(String command, String description, Predicate<CommandSender> visibility) {
            this.command = command;
            this.description = description;
            this.visibility = visibility;
        }

        private boolean isVisibleTo(CommandSender sender) {
            return visibility.test(sender);
        }

        private String rootSubcommand() {
            String trimmed = command.startsWith("/terra ") ? command.substring("/terra ".length()) : command;
            if (trimmed.startsWith("/")) {
                trimmed = trimmed.substring(1);
            }
            int spaceIndex = trimmed.indexOf(' ');
            return spaceIndex >= 0 ? trimmed.substring(0, spaceIndex) : trimmed;
        }
    }

    private List<String> getKnownPlayerNames() {
        List<String> playerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerNames.add(player.getName());
        }
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null && !playerNames.contains(offlinePlayer.getName())) {
                playerNames.add(offlinePlayer.getName());
            }
        }
        return playerNames;
    }

    private List<String> getAllJobKeys() {
        List<String> jobs = new ArrayList<>();
        for (Profession profession : plugin.getConfiguredProfessions()) {
            jobs.add(profession.getKey());
        }
        return jobs;
    }

    private List<String> partialMatches(String input, List<String> options) {
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.regionMatches(true, 0, input, 0, input.length())) {
                matches.add(option);
            }
        }
        return matches;
    }

    private String displayEmpty(String value) {
        return value == null || value.isBlank() ? "(empty)" : value;
    }

    private WorldEditPlugin getWorldEditPlugin() {
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") instanceof WorldEditPlugin worldEditPlugin) {
            return worldEditPlugin;
        }
        return null;
    }

    private List<String> getAllBlockMaterialNames() {
        List<String> materialNames = new ArrayList<>();
        for (Material material : Material.values()) {
            if (material.isBlock()) {
                materialNames.add(material.name().toLowerCase(Locale.ROOT));
            }
        }
        materialNames.sort(String::compareToIgnoreCase);
        return materialNames;
    }

    private boolean hasAdminAccess(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        return plugin.canUseAdminCommands(player);
    }

    private boolean hasStaffAccess(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        return plugin.canUseStaffMode(player);
    }

    private Double parseXpBoostAmount(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String normalized = input.toLowerCase(Locale.ROOT).replace("x", "");
        try {
            double value = Double.parseDouble(normalized);
            for (double amount : XP_BOOST_AMOUNTS) {
                if (Double.compare(amount, value) == 0) {
                    return amount;
                }
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private Long parseDurationMillis(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String normalized = input.trim().toLowerCase(Locale.ROOT);
        long multiplier = 1000L;
        if (normalized.endsWith("h")) {
            multiplier = 60L * 60L * 1000L;
            normalized = normalized.substring(0, normalized.length() - 1);
        } else if (normalized.endsWith("m")) {
            multiplier = 60L * 1000L;
            normalized = normalized.substring(0, normalized.length() - 1);
        } else if (normalized.endsWith("s")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        try {
            long value = Long.parseLong(normalized);
            return value > 0L ? value * multiplier : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Long parsePlaytestDurationMillis(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String normalized = input.trim().toLowerCase(Locale.ROOT);
        Matcher matcher = PLAYTEST_DURATION_PATTERN.matcher(normalized);
        long totalMillis = 0L;
        int matchedChars = 0;

        while (matcher.find()) {
            long value;
            try {
                value = Long.parseLong(matcher.group(1));
            } catch (NumberFormatException exception) {
                return null;
            }

            matchedChars += matcher.group(0).length();
            switch (matcher.group(2).toLowerCase(Locale.ROOT)) {
                case "y" -> totalMillis += value * 365L * 24L * 60L * 60L * 1000L;
                case "w" -> totalMillis += value * 7L * 24L * 60L * 60L * 1000L;
                case "d" -> totalMillis += value * 24L * 60L * 60L * 1000L;
                case "h" -> totalMillis += value * 60L * 60L * 1000L;
                case "m" -> totalMillis += value * 60L * 1000L;
                case "s" -> totalMillis += value * 1000L;
                default -> {
                    return null;
                }
            }
        }

        return matchedChars == normalized.length() && totalMillis > 0L ? totalMillis : null;
    }

    private OfflinePlayer findPlayer(String input) {
        Player onlinePlayer = Bukkit.getPlayerExact(input);
        if (onlinePlayer != null) {
            return onlinePlayer;
        }

        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(input)) {
                return offlinePlayer;
            }
        }
        return null;
    }
}
