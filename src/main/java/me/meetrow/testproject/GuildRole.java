package me.meetrow.testproject;

import java.util.Locale;

public enum GuildRole {
    LEADER("Leader"),
    OFFICER("Officer"),
    ADMIRAL("Admiral"),
    MEMBER("Member");

    private final String displayName;

    GuildRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAtLeast(GuildRole other) {
        return other != null && this.ordinal() <= other.ordinal();
    }

    public static GuildRole fromKey(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String normalized = input.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
