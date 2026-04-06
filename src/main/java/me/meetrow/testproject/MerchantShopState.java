package me.meetrow.testproject;

import java.util.UUID;

public final class MerchantShopState {
    private final UUID merchantId;
    private final UUID entityId;
    private final String hostCountryKey;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final long spawnedAtMillis;
    private final long despawnAtMillis;

    public MerchantShopState(
            UUID merchantId,
            UUID entityId,
            String hostCountryKey,
            String worldName,
            double x,
            double y,
            double z,
            float yaw,
            float pitch,
            long spawnedAtMillis,
            long despawnAtMillis
    ) {
        this.merchantId = merchantId;
        this.entityId = entityId;
        this.hostCountryKey = hostCountryKey;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.spawnedAtMillis = spawnedAtMillis;
        this.despawnAtMillis = despawnAtMillis;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public String getHostCountryKey() {
        return hostCountryKey;
    }

    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public long getSpawnedAtMillis() {
        return spawnedAtMillis;
    }

    public long getDespawnAtMillis() {
        return despawnAtMillis;
    }
}
