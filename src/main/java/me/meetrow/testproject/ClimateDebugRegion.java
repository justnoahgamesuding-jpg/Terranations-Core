package me.meetrow.testproject;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public record ClimateDebugRegion(
        UUID worldId,
        int minX,
        int maxX,
        int minZ,
        int maxZ,
        int baseY
) {
    public boolean contains(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        if (!location.getWorld().getUID().equals(worldId)) {
            return false;
        }
        int x = location.getBlockX();
        int z = location.getBlockZ();
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public int topY() {
        return baseY + 1;
    }

    public int centerZ() {
        return minZ + ((maxZ - minZ) / 2);
    }

    public int centerX() {
        return minX + ((maxX - minX) / 2);
    }

    public double centerDistanceFactor(Location location) {
        if (location == null) {
            return 0.0D;
        }
        double dx = location.getX() - centerX();
        double dz = location.getZ() - centerZ();
        double maxDx = Math.max(1.0D, Math.max(Math.abs(minX - centerX()), Math.abs(maxX - centerX())));
        double maxDz = Math.max(1.0D, Math.max(Math.abs(minZ - centerZ()), Math.abs(maxZ - centerZ())));
        double normalizedDistance = Math.sqrt((dx * dx) / (maxDx * maxDx) + (dz * dz) / (maxDz * maxDz));
        return Math.max(0.0D, Math.min(1.0D, normalizedDistance / Math.sqrt(2.0D)));
    }

    public double normalizedLatitude(Location location) {
        if (location == null) {
            return 0.5D;
        }
        double span = Math.max(1.0D, maxZ - minZ);
        double normalized = (location.getZ() - minZ) / span;
        return Math.max(0.0D, Math.min(1.0D, normalized));
    }

    public World world(org.bukkit.Server server) {
        return server.getWorld(worldId);
    }
}
