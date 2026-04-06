package me.meetrow.testproject;

public enum CountryRole {
    OWNER("Owner"),
    CO_OWNER("Co-Owner"),
    STEWARD("Steward"),
    MEMBER("Member");

    private final String displayName;

    CountryRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CountryRole fromKey(String input) {
        if (input == null) {
            return null;
        }
        return switch (input.toLowerCase()) {
            case "owner" -> OWNER;
            case "coowner", "co-owner", "co_owner" -> CO_OWNER;
            case "steward" -> STEWARD;
            case "member" -> MEMBER;
            default -> null;
        };
    }
}
