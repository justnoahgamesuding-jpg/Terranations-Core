package me.meetrow.testproject;

import java.util.Locale;

public enum GuildPermission {
    INVITE_PLAYERS("Invite Players"),
    REMOVE_PLAYERS("Remove Players"),
    CLAIM_COUNTRIES("Claim Countries"),
    DEPOSIT_FUNDS("Deposit Funds"),
    WITHDRAW_FUNDS("Withdraw Funds"),
    MANAGE_ROLES("Manage Roles"),
    PROMOTE_MEMBERS("Promote Members"),
    DEMOTE_MEMBERS("Demote Members"),
    MANAGE_ROLE_PERMISSIONS("Manage Role Permissions"),
    MANAGE_PLAYER_PERMISSIONS("Manage Player Permissions"),
    MANAGE_COUNTRY_SETTINGS("Manage Country Settings"),
    OVERRIDE_UPKEEP("Override Upkeep"),
    MANAGE_RECRUITMENT("Manage Recruitment"),
    MANAGE_GUILD_PROFILE("Manage Guild Profile"),
    MANAGE_STOCKPILE("Manage Stockpile");

    private final String displayName;

    GuildPermission(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static GuildPermission fromKey(String input) {
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
