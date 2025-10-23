package org.ilmiandluk.customMinigame.game;

import org.bukkit.entity.Player;
import org.ilmiandluk.customMinigame.game.map.Map;

import java.util.HashMap;
import java.util.List;

public class GameController {
    private static final HashMap<Map, Game>
            allGames = new HashMap<Map, Game>();

    public static Game getGame(Map map) {
        return allGames.get(map);
    }

    public static boolean createGame(Map map, List<Player> players) {
        if (!allGames.containsKey(map)) {
            allGames.put(map, new Game(map, players));

            allGames.get(map).startMapPrepareTask();
            return true;
        }
        return false;
    }

    public static boolean deleteGame(Map map) {
        if (allGames.containsKey(map)) {
            allGames.remove(map);
            return true;
        }
        return false;
    }

    public static Game getGameWithPlayer(Player player){
        for(Game game: allGames.values()){
            if(game.getPlayers().contains(player)) return game;
        }
        return null;
    }
}
