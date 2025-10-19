package org.ilmiandluk.customMinigame.game.map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.structures.AbstractStructure;
import org.ilmiandluk.customMinigame.game.structures.environment.Forest;
import org.ilmiandluk.customMinigame.game.structures.environment.Hills;
import org.ilmiandluk.customMinigame.game.structures.environment.Plain;

import java.util.List;
import java.util.Random;

public class Map {
    private MapGameState mapGameState = MapGameState.READY;
    private SegmentBuilder segmentBuilder = CustomMinigame.getInstance().getSegmentBuilder();
    private final String mapName;
    private final int maxPlayers;
    private final int segmentCount;
    private final int xSize;
    private final int zSize;
    private final Location mapLocation;
    private List<Player> players;
    private MapSegment[][] segments;
    private Random random = new Random();

    public Map(String mapName, Location mapLocation, int maxPlayers, int xSize, int zSize) {
        this.mapName = mapName;
        this.mapLocation = mapLocation;
        this.maxPlayers = maxPlayers;
        this.segmentCount = xSize*zSize;
        this.xSize = xSize;
        this.zSize = zSize;
        segments = new MapSegment[xSize][zSize];
    }
    // Я конченный, бегите
    public void segmentInitialize(){
        for(int x = 0; x < xSize; x++){
            for(int z = 0; z < zSize; z++){
                AbstractStructure structure = getRandomEnvStructure();
                Location loc = mapLocation.clone().add(x*20+1,0,z*20+1);
                segments[x][z] = new MapSegment(structure, loc, null);
                segmentBuilder.buildSegment(segments[x][z]);
            }
        }
    }
    private AbstractStructure getRandomEnvStructure() {
        int rand = random.nextInt(100);
        if (rand < 50) return new Plain();      // 50%
        else if (rand < 80) return new Forest(); // 30%
        else return new Hills();                 // 20%
    }
}
