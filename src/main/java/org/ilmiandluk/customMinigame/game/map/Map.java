package org.ilmiandluk.customMinigame.game.map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.Game;
import org.ilmiandluk.customMinigame.game.controller.GameController;
import org.ilmiandluk.customMinigame.game.entity.Soldier;
import org.ilmiandluk.customMinigame.game.enums.MapGameState;
import org.ilmiandluk.customMinigame.game.enums.SoldierRelate;
import org.ilmiandluk.customMinigame.game.player.GamePlayer;
import org.ilmiandluk.customMinigame.game.structures.AbstractStructure;
import org.ilmiandluk.customMinigame.game.structures.environment.Forest;
import org.ilmiandluk.customMinigame.game.structures.environment.Hills;
import org.ilmiandluk.customMinigame.game.structures.environment.Plain;
import org.ilmiandluk.customMinigame.game.structures.builds.Base;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Map {
    private final static CustomMinigame customMinigame = CustomMinigame.getInstance();

    private MapGameState mapGameState = MapGameState.READY;
    private final String mapName;
    private final int maxPlayers;
    private final int segmentCount;
    private final SegmentBuilder segmentBuilder;
    private final int xSize;
    private final int zSize;
    private final BoundingBox boundingBox;
    private final Location mapLocation;
    private List<Player> players;
    private MapSegment[][] segments;
    private Random random = new Random();
    private List<int[]> targetCandidates;

    public Map(String mapName, Location mapLocation, int maxPlayers, int xSize, int zSize) {
        this.mapName = mapName;
        this.mapLocation = mapLocation;
        this.maxPlayers = maxPlayers;
        this.segmentCount = xSize*zSize;
        this.segmentBuilder = new SegmentBuilder(customMinigame);
        this.xSize = xSize;
        this.zSize = zSize;
        this.boundingBox = BoundingBox.of(mapLocation.clone().add(-20, -1, -20),
        mapLocation.clone().add((xSize+2)*20, 200, (zSize+2)*20));
        segments = new MapSegment[xSize][zSize];
        this.players = new ArrayList<>();

        // Это для тестов.
        {
            for (int i = 0; i < 5; i++) {
                players.add(null);
            }
        }
    }

    public void segmentInitialize(){
        setUpBases(2);
        buildBases(2);
        for(int x = 0; x < xSize; x++){
            for(int z = 0; z < zSize; z++){
                if(segments[x][z] != null){
                    if(segments[x][z].getStructure() instanceof Base) continue;
                }
                AbstractStructure structure = getRandomEnvStructure();
                Location loc = mapLocation.clone().add(x*20,0,z*20);
                segments[x][z] = new MapSegment(structure, loc, null, x, z);
                segmentBuilder.buildSegment(segments[x][z]);
            }
        }
    }
    private void buildBases(int bSize) {
        int step = targetCandidates.size() / players.size();
        int placed = 0;

        for (int i = 0; i < targetCandidates.size() && placed < players.size(); i += step) {
            int[] currentCandidate = targetCandidates.get(i);
            int X = currentCandidate[0];
            int Z = currentCandidate[1];

            Player player = players.get(placed);
            Game game = GameController.getGameWithPlayer(player);
            if (game == null) continue;
            segmentBuilder.buildSegment(segments[X][Z]);
            GamePlayer gamePlayer = game.getGamePlayer(player);
            for (int j = 0; j < gamePlayer.getPlayerResources().getSoldiersCount(); j++) {
                gamePlayer.getPlayerResources().createSoldier(segments[X][Z]);

            }
            placed++;
        }
    }
    private void setUpBases(int bSize) {
        targetCandidates = new ArrayList<>();

        //ОЧЕНЬ ИНТЕЛЛЕКТУАЛЬНО СОБИРАЕМ КООРДИНАТЫ ПО ПЕРИМЕТРУ

        for (int z = 0; z <= zSize - bSize; z++) targetCandidates.add(new int[]{0, z});
        for (int x = 1; x <= xSize - bSize; x++) targetCandidates.add(new int[]{x, xSize-bSize});
        for (int z = zSize - bSize - 1; z >= 0; z--) targetCandidates.add(new int[]{zSize-bSize, z});
        for (int x = xSize - bSize - 1; x > 0; x--) targetCandidates.add(new int[]{x, 0});

        int step = targetCandidates.size() / players.size();
        int placed  = 0;

        for (int i = 0; i < targetCandidates.size() && placed < players.size(); i += step) {
            int [] currentCandidate = targetCandidates.get(i);
            int X = currentCandidate[0]; int Z = currentCandidate[1];

            Player player = players.get(placed);
            Game game = GameController.getGameWithPlayer(player);
            if(game == null) continue;


            for (int x = X; x < X+bSize; x++) {
                for (int z = Z; z < Z+bSize; z++) {
                    Location loc = mapLocation.clone().add(x*20,0,z*20);
                    segments[x][z] = new MapSegment(new Base(), loc, player, x, z);
                    game.addSegmentToPlayerFromMap(segments[x][z], player);
                }
            }


            placed++;
        }
    }

    private AbstractStructure getRandomEnvStructure() {
        int rand = random.nextInt(100);
        if (rand < 70) return new Plain();      // 70%
        else if (rand < 85) return new Forest(); // 15%
        else return new Hills();                 // 15%
    }

    // Геттеры и сеттеры для MapController

    public MapGameState getMapGameState() {
        return mapGameState;
    }

    public void setMapGameState(MapGameState mapGameState) {
        this.mapGameState = mapGameState;
    }

    public String getMapName() {
        return mapName;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getxSize() {
        return xSize;
    }

    public int getzSize() {
        return zSize;
    }

    public Location getMapLocation() {
        return mapLocation.clone();
    }

    public List<Player> getPlayers() {
        return players;
    }
    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public MapSegment[][] getSegments() {
        return segments;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public SegmentBuilder getSegmentBuilder() {
        return segmentBuilder;
    }
}
