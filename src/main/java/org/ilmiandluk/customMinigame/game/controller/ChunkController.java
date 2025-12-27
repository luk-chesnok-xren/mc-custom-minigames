package org.ilmiandluk.customMinigame.game.controller;

import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.ChunkCoordIntPair;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.ilmiandluk.customMinigame.CustomMinigame;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChunkController {
    private final WorldServer world;
    private final Map<Long, Integer> chunkRefCounts = new HashMap<>();
    private final BukkitTask task;

    // Размер "буфера" — сколько чанков вокруг активного нужно держать загруженным
    private static final int CHUNK_RADIUS = 1;

    public ChunkController(WorldServer world) {
        this.world = world;
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if(isCancelled()) return;
                cleanup();
            }
        }.runTaskTimer(CustomMinigame.getInstance(), 0, 100);
    }
    public void stopWorking(){
        this.task.cancel();
    }

    public void cleanup() {
        Iterator<Map.Entry<Long, Integer>> iterator = chunkRefCounts.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue() <= 0) {
                long chunkLong = entry.getKey();
                int chunkX = (int) (chunkLong >> 32);
                int chunkZ = (int) (chunkLong & 0xFFFFFFFFL);
                world.a(chunkX, chunkZ, false);
                iterator.remove();
            }
        }
    }

    public synchronized void ensureChunkLoaded(int chunkX, int chunkZ) {
        for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
            for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                long key = ChunkCoordIntPair.c(chunkX + dx, chunkZ + dz);
                chunkRefCounts.put(key, chunkRefCounts.getOrDefault(key, 0) + 1);
                world.a(chunkX + dx, chunkZ + dz, true);
            }
        }
    }

    public synchronized void releaseChunk(int chunkX, int chunkZ) {
        for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
            for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                long key = ChunkCoordIntPair.c(chunkX + dx, chunkZ + dz);
                int count = chunkRefCounts.getOrDefault(key, 0) - 1;
                if (count <= 0) {
                    chunkRefCounts.remove(key);
                    world.a(chunkX + dx, chunkZ + dz, false);
                } else {
                    chunkRefCounts.put(key, count);
                }
            }
        }
    }
}
