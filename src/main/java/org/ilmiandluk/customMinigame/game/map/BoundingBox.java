package org.ilmiandluk.customMinigame.game.map;

import org.bukkit.Location;

public record BoundingBox(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {

    public static BoundingBox of(Location a, Location b) {
        return new BoundingBox(
                Math.min(a.getX(), b.getX()), Math.max(a.getX(), b.getX()),
                Math.min(a.getY(), b.getY()), Math.max(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ()), Math.max(a.getZ(), b.getZ())
        );
    }

    public Location clamp(Location loc) {
        return new Location(
                loc.getWorld(),
                clamp(loc.getX(), minX, maxX),
                clamp(loc.getY(), minY, maxY),
                clamp(loc.getZ(), minZ, maxZ),
                loc.getYaw(), loc.getPitch()
        );
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public boolean contains(Location loc) {
        return loc.getX() >= minX && loc.getX() <= maxX &&
                loc.getY() >= minY && loc.getY() <= maxY &&
                loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }
}

