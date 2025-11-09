package org.ilmiandluk.customMinigame.game.controller;

import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.ChunkCoordIntPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChunkController {
    private final WorldServer world;
    private final List<Long> chunks;
    private final HashMap<Long, Integer> unitsInChunk;

    public ChunkController(WorldServer world) {
        this.world = world;
        this.chunks = new ArrayList<>();
        this.unitsInChunk = new HashMap<>();
    }

    public void loadChunk(int chunkX, int chunkZ) {
        // asLong(int cx, int cz)
        long chunkLong = ChunkCoordIntPair.c(chunkX, chunkZ);
        if(!chunks.contains(chunkLong)) {
            chunks.add(chunkLong);
            unitsInChunk.put(chunkLong, 1);
            // setChunkForced(int cx, cz, boolean)
            world.a(chunkX, chunkZ, true);
            System.out.println("Chunk " + chunkX + " " + chunkZ + " has been loaded");
        }
        else {
            unitsInChunk.put(chunkLong, unitsInChunk.getOrDefault(chunkLong, 0) + 1);
        }
    }
    public void unloadChunk(int chunkX, int chunkZ)  {
        long chunkLong = ChunkCoordIntPair.c(chunkX, chunkZ);
        if(chunks.contains(chunkLong)) {
            if(unitsInChunk.getOrDefault(chunkLong, 0) <= 1) {
                chunks.remove(chunkLong);
                unitsInChunk.remove(chunkLong);
                // setChunkForced(int cx, cz, boolean)
                world.a(chunkX, chunkZ, false);
                System.out.println("Chunk " + chunkX + " " + chunkZ + " has been unloaded");
            }

        }
    }
}
