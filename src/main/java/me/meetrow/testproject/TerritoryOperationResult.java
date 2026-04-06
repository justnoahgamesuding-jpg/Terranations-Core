package me.meetrow.testproject;

public record TerritoryOperationResult(boolean success, String reason, String worldName, String regionId) {
    public static TerritoryOperationResult success(String worldName, String regionId) {
        return new TerritoryOperationResult(true, "success", worldName, regionId);
    }

    public static TerritoryOperationResult failure(String reason) {
        return new TerritoryOperationResult(false, reason, null, null);
    }
}
