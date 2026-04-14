package me.meetrow.testproject.server;

import me.meetrow.testproject.Testproject;
import org.bukkit.event.server.ServerListPingEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AnimatedServerMotd {
    private static final String CONFIG_ROOT = "server-list-motd";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final Testproject plugin;

    public AnimatedServerMotd(Testproject plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean(CONFIG_ROOT + ".enabled", true);
    }

    public String getMotd(ServerListPingEvent event) {
        List<String> frame = getCurrentFrame();
        if (frame.isEmpty()) {
            return "";
        }

        List<String> resolved = new ArrayList<>();
        for (String line : frame) {
            resolved.add(plugin.colorize(resolvePlaceholders(line, event)));
        }
        return String.join("\n", resolved);
    }

    private List<String> getCurrentFrame() {
        List<Map<?, ?>> configuredFrames = plugin.getConfig().getMapList(CONFIG_ROOT + ".frames");
        if (configuredFrames.isEmpty()) {
            return plugin.getConfig().getStringList(CONFIG_ROOT + ".lines");
        }

        int safeInterval = Math.max(1, plugin.getConfig().getInt(CONFIG_ROOT + ".change-interval", 25));
        long elapsedTicks = Math.max(0L, System.currentTimeMillis() / 50L);
        int index = (int) ((elapsedTicks / safeInterval) % configuredFrames.size());
        Map<?, ?> frame = configuredFrames.get(index);

        List<String> lines = new ArrayList<>();
        Object configuredLines = frame.get("lines");
        if (configuredLines instanceof List<?> rawLines) {
            for (Object rawLine : rawLines) {
                lines.add(String.valueOf(rawLine));
            }
        } else {
            Object line1 = frame.get("line1");
            Object line2 = frame.get("line2");
            if (line1 != null) {
                lines.add(String.valueOf(line1));
            }
            if (line2 != null) {
                lines.add(String.valueOf(line2));
            }
        }
        return lines;
    }

    private String resolvePlaceholders(String text, ServerListPingEvent event) {
        if (text == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        return text
                .replace("%online%", String.valueOf(event.getNumPlayers()))
                .replace("%max_players%", String.valueOf(event.getMaxPlayers()))
                .replace("%date%", DATE_FORMAT.format(now))
                .replace("%server_time%", TIME_FORMAT.format(now));
    }
}
