package org.ilmiandluk.customMinigame.game.map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.Game;
import org.ilmiandluk.customMinigame.game.GameController;
import org.ilmiandluk.customMinigame.game.structures.AbstractStructure;
import org.ilmiandluk.customMinigame.game.structures.environment.Forest;
import org.ilmiandluk.customMinigame.game.structures.environment.Hills;
import org.ilmiandluk.customMinigame.game.structures.environment.Plain;
import org.ilmiandluk.customMinigame.game.structures.builds.Base;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

// Map - связан с MapController. Только он может вызывать конструктор Map.
// Но многие методы должны вызываться извне, они помечены как public.

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

    Map(String mapName, Location mapLocation, int maxPlayers, int xSize, int zSize) {
        this.mapName = mapName;
        /*
        Костыль. При начале построения поля (вставке структур)
        происходит смещение по z.
        Смещение на 18 по z делает так, что карта
        начинает строится на месте, где она должна была.

         */
        this.mapLocation = mapLocation;
        this.maxPlayers = maxPlayers;
        this.segmentCount = xSize*zSize;
        this.xSize = xSize;
        this.zSize = zSize;
        segments = new MapSegment[xSize][zSize];
        this.players = new ArrayList<>();
        {
            for (int i = 0; i < 5; i++) {
                players.add(null);
            }
        }
    }
    // Я конченный, бегите
    public void segmentInitialize(){
        for(int x = 0; x < xSize; x++){
            for(int z = 0; z < zSize; z++){
                AbstractStructure structure = getRandomEnvStructure();
                Location loc = mapLocation.clone().add(x*20,0,z*20);
                segments[x][z] = new MapSegment(structure, loc, null);
                segmentBuilder.buildSegment(segments[x][z]);
            }
        }

        setUpBases(2);

        //логи чисто для меня
        //как видим оно нормально расставляет все в самом массиве segments
        //но на деле я не могу понять где у схемы находится origin
        // поэтому он не может вставить нормально в саму карту в майнкрафте
        for(int x = 0; x < xSize; x++){
            String a = "";
            for(int z = 0; z < zSize; z++){
                if(segments[x][z].getStructure() instanceof Base) a += "b";
                else a += ".";
            }

            Bukkit.getLogger().log(Level.INFO,  a);
        }
    }

    private void setUpBases(int bSize) {
        List<int[]> targetCandidates = new ArrayList<>();

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

            Location loc = mapLocation.clone().add(X*20,0,Z*20);
            for (int x = X; x < X+bSize; x++) {
                for (int z = Z; z < Z+bSize; z++) {
                    segments[x][z] = new MapSegment(new Base(), loc, players.get(placed));
                    Game game = GameController.getGameWithPlayer(players.get(placed));
                    if(game != null) game.addSegmentToPlayerFromMap(segments[x][z], players.get(placed));
                }
            }
            segmentBuilder.buildSegment(segments[X][Z]);
            placed++;
        }
    }

    private AbstractStructure getRandomEnvStructure() {
        int rand = random.nextInt(100);
        if (rand < 50) return new Plain();      // 50%
        else if (rand < 80) return new Forest(); // 30%
        else return new Hills();                 // 20%
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
        return mapLocation;
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
}
