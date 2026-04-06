package me.meetrow.testproject;

import java.util.Locale;

public enum PlayerQuestType {
    SELECT_PROFESSION,
    EARN_PROFESSION_XP,
    REACH_PROFESSION_LEVEL,
    JOIN_COUNTRY,
    CONTRIBUTE_COUNTRY;

    public static PlayerQuestType fromKey(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String normalized = input.trim().replace(' ', '_').replace('-', '_').toUpperCase(Locale.ROOT);
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
