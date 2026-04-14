package me.meetrow.testproject.scoreboard;

import me.clip.placeholderapi.PlaceholderAPI;
import me.meetrow.testproject.Country;
import me.meetrow.testproject.Profession;
import me.meetrow.testproject.Testproject;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class CustomScoreboard {
    private static final String CONFIG_ROOT = "custom-scoreboard";
    private static final String OBJECTIVE_NAME = "terrascore";
    private static final String TUTORIAL_OBJECTIVE_NAME = "terraguide";
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?i)&?#([0-9a-f]{6})");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final Testproject plugin;
    private final Map<UUID, Scoreboard> activeScoreboards = new ConcurrentHashMap<>();
    private final Map<UUID, Scoreboard> previousScoreboards = new ConcurrentHashMap<>();
    private BukkitTask task;

    public CustomScoreboard(Testproject plugin) {
        this.plugin = plugin;
    }

    public void restart() {
        stop();
        if (!isEnabled()) {
            return;
        }

        long updateTicks = Math.max(5L, plugin.getConfig().getLong(CONFIG_ROOT + ".update-ticks", 20L));
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateAll, 20L, updateTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            clearIfActive(player);
        }
        activeScoreboards.clear();
        previousScoreboards.clear();
    }

    private boolean isEnabled() {
        return plugin.getConfig().getBoolean(CONFIG_ROOT + ".enabled", true);
    }

    private void updateAll() {
        if (!isEnabled()) {
            stop();
            return;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayer(player);
        }

        activeScoreboards.keySet().removeIf(playerId -> plugin.getServer().getPlayer(playerId) == null);
        previousScoreboards.keySet().removeIf(playerId -> plugin.getServer().getPlayer(playerId) == null);
    }

    private void updatePlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Scoreboard current = player.getScoreboard();
        if (isSidebarObjective(current, TUTORIAL_OBJECTIVE_NAME)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        Scoreboard active = activeScoreboards.get(playerId);
        if (shouldRespectCurrentScoreboard(current, active)) {
            clearIfActive(player);
            return;
        }

        ScoreboardManager manager = plugin.getServer().getScoreboardManager();
        if (manager == null) {
            return;
        }

        if (active == null || current != active) {
            previousScoreboards.putIfAbsent(playerId, current);
        }

        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy", plugin.colorize(getTitle(player)));
        if (plugin.getConfig().getBoolean(CONFIG_ROOT + ".hide-numbers", true)) {
            objective.numberFormat(NumberFormat.blank());
        }
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = getLines(player);
        int score = Math.min(15, lines.size());
        int uniqueIndex = 0;
        for (String line : lines.subList(0, Math.min(15, lines.size()))) {
            objective.getScore(makeUniqueSidebarLine(plugin.colorize(line), uniqueIndex++)).setScore(score--);
        }

        activeScoreboards.put(playerId, scoreboard);
        player.setScoreboard(scoreboard);
    }

    private boolean shouldRespectCurrentScoreboard(Scoreboard current, Scoreboard active) {
        if (!plugin.getConfig().getBoolean(CONFIG_ROOT + ".respect-other-scoreboards", true)) {
            return false;
        }
        if (current == null || current == active) {
            return false;
        }
        Objective currentObjective = current.getObjective(DisplaySlot.SIDEBAR);
        return currentObjective != null && !OBJECTIVE_NAME.equals(currentObjective.getName());
    }

    private void clearIfActive(Player player) {
        if (player == null) {
            return;
        }

        UUID playerId = player.getUniqueId();
        Scoreboard active = activeScoreboards.remove(playerId);
        Scoreboard previous = previousScoreboards.remove(playerId);
        if (active == null || player.getScoreboard() != active) {
            return;
        }

        if (previous != null) {
            player.setScoreboard(previous);
            return;
        }

        ScoreboardManager manager = plugin.getServer().getScoreboardManager();
        if (manager != null) {
            player.setScoreboard(manager.getMainScoreboard());
        }
    }

    private boolean isSidebarObjective(Scoreboard scoreboard, String objectiveName) {
        if (scoreboard == null) {
            return false;
        }
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        return objective != null && objectiveName.equals(objective.getName());
    }

    private String getTitle(Player player) {
        String animatedTitle = getAnimatedTitle(player);
        if (animatedTitle != null) {
            return animatedTitle;
        }
        return resolvePlaceholders(player, expandHexColors(plugin.getConfig().getString(CONFIG_ROOT + ".title", "&6&lTerra")));
    }

    private String getAnimatedTitle(Player player) {
        String animationKey = plugin.getConfig().getString(CONFIG_ROOT + ".title-animation", "scoreboard_logo");
        if (animationKey == null || animationKey.isBlank()) {
            return null;
        }

        String path = CONFIG_ROOT + ".animations." + animationKey;
        List<String> frames = plugin.getConfig().getStringList(path + ".texts");
        int changeInterval = plugin.getConfig().getInt(path + ".change-interval", 0);
        if (frames.isEmpty()) {
            path = animationKey;
            frames = plugin.getConfig().getStringList(path + ".texts");
            changeInterval = plugin.getConfig().getInt(path + ".change-interval", 0);
        }
        if (frames.isEmpty()) {
            return null;
        }

        int safeInterval = Math.max(1, changeInterval > 0 ? changeInterval : 25);
        long elapsedTicks = Math.max(0L, System.currentTimeMillis() / 50L);
        int index = (int) ((elapsedTicks / safeInterval) % frames.size());
        return resolvePlaceholders(player, expandHexColors(frames.get(index)));
    }

    private List<String> getLines(Player player) {
        List<String> configuredLines = plugin.getConfig().getStringList(CONFIG_ROOT + ".lines");
        if (configuredLines.isEmpty()) {
            configuredLines = List.of(
                    "&7%date% &8| &7%server_time%",
                    "&f%player%",
                    "",
                    "&c⚐ &f%country% &7Lv.%country_level%",
                    "&a⛏ &f%job% &7Lv.%job_level%",
                    "&b✦ &f%job_xp%&7/&f%job_xp_required% XP",
                    "&6⛃ &f$%money%",
                    "",
                    "&9⌚ &f%playtime%",
                    "",
                    "&7ᴘʟᴀʏ.ᴛᴇʀʀᴀɴᴀᴛɪᴏɴѕ.ɴᴇᴛ"
            );
        }

        List<String> lines = new ArrayList<>();
        for (String line : configuredLines) {
            lines.add(resolvePlaceholders(player, expandHexColors(line)));
        }
        return lines;
    }

    private String resolvePlaceholders(Player player, String text) {
        if (text == null) {
            return "";
        }

        Country country = plugin.getPlayerCountry(player.getUniqueId());
        Profession profession = plugin.getProfession(player.getUniqueId());
        double maxHealth = getMaxHealth(player);
        double armor = getArmor(player);
        int jobXp = profession != null ? plugin.getProfessionXp(player.getUniqueId(), profession) : 0;
        int jobXpRequired = profession != null ? plugin.getProfessionXpRequired(player.getUniqueId(), profession) : 0;
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        String jobColor = expandHexColors(plugin.getConfig().getString(CONFIG_ROOT + ".job-name-color", "&f"));
        String resolved = text
                .replace("%player%", player.getName())
                .replace("%display_name%", player.getDisplayName())
                .replace("%world%", player.getWorld().getName())
                .replace("%country%", country != null ? country.getName() : "None")
                .replace("%country_tag%", country != null ? country.getTag() : "")
                .replace("%country_level%", country != null ? String.valueOf(country.getLevel()) : "0")
                .replace("%job_color%", jobColor)
                .replace("%job%", profession != null ? plugin.getProfessionPlainDisplayName(profession) : "No Job")
                .replace("%job_level%", profession != null ? String.valueOf(plugin.getProfessionLevel(player.getUniqueId(), profession)) : "0")
                .replace("%job_xp%", String.valueOf(jobXp))
                .replace("%job_xp_required%", formatRequiredXp(jobXpRequired))
                .replace("%job_xp_progress%", formatJobXpProgress(jobXp, jobXpRequired))
                .replace("%money%", plugin.formatMoney(plugin.getBalance(player.getUniqueId())))
                .replace("%health%", formatNumber(Math.max(0.0D, Math.min(player.getHealth(), maxHealth))))
                .replace("%max_health%", formatNumber(maxHealth))
                .replace("%armor%", formatNumber(armor))
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%ping%", String.valueOf(player.getPing()))
                .replace("%kills%", String.valueOf(player.getStatistic(Statistic.PLAYER_KILLS)))
                .replace("%deaths%", String.valueOf(player.getStatistic(Statistic.DEATHS)))
                .replace("%playtime%", formatPlaytime(player.getStatistic(Statistic.PLAY_ONE_MINUTE)))
                .replace("%date%", DATE_FORMAT.format(now))
                .replace("%server_time%", TIME_FORMAT.format(now));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            resolved = PlaceholderAPI.setPlaceholders(player, resolved);
        }
        return resolved;
    }

    private double getMaxHealth(Player player) {
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) == null) {
            return 20.0D;
        }
        return Math.max(1.0D, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
    }

    private double getArmor(Player player) {
        if (player.getAttribute(Attribute.GENERIC_ARMOR) == null) {
            return 0.0D;
        }
        return Math.max(0.0D, player.getAttribute(Attribute.GENERIC_ARMOR).getValue());
    }

    private String formatNumber(double value) {
        double safeValue = Math.max(0.0D, value);
        return safeValue == Math.rint(safeValue)
                ? String.valueOf((int) safeValue)
                : String.format(Locale.US, "%.1f", safeValue);
    }

    private String formatPlaytime(int ticks) {
        long totalMinutes = Math.max(0L, ticks / 20L / 60L);
        long hours = totalMinutes / 60L;
        long minutes = totalMinutes % 60L;
        if (hours <= 0L) {
            return minutes + "m";
        }
        if (minutes <= 0L) {
            return hours + "h";
        }
        return hours + "h " + minutes + "m";
    }

    private String formatRequiredXp(int requiredXp) {
        return requiredXp > 0 ? String.valueOf(requiredXp) : "MAX";
    }

    private String formatJobXpProgress(int currentXp, int requiredXp) {
        return Math.max(0, currentXp) + "/" + formatRequiredXp(requiredXp);
    }

    private String expandHexColors(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        Matcher matcher = HEX_COLOR_PATTERN.matcher(text);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (int index = 0; index < hex.length(); index++) {
                replacement.append('&').append(hex.charAt(index));
            }
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    private String makeUniqueSidebarLine(String line, int index) {
        ChatColor[] colors = ChatColor.values();
        return line + colors[index % colors.length];
    }
}
