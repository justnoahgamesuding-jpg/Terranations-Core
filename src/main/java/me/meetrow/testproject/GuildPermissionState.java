package me.meetrow.testproject;

public enum GuildPermissionState {
    ALLOW,
    DENY,
    DEFAULT;

    public static GuildPermissionState fromKey(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        return switch (input.trim().toLowerCase()) {
            case "allow", "on", "true", "yes" -> ALLOW;
            case "deny", "off", "false", "no" -> DENY;
            case "default", "clear", "reset", "inherit" -> DEFAULT;
            default -> null;
        };
    }
}
