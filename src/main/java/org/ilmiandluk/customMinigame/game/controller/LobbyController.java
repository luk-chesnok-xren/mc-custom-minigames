package org.ilmiandluk.customMinigame.game.controller;

import org.bukkit.entity.Player;
import org.ilmiandluk.customMinigame.game.Lobby;
import org.ilmiandluk.customMinigame.game.map.Map;

import java.util.HashMap;

public class LobbyController {
    private static final HashMap<Map, Lobby>
            lobbyControllers = new HashMap<Map, Lobby>();

    public static Lobby getLobby(Map map) {
        return lobbyControllers.get(map);
    }

    public static Lobby createLobby(Map map) {
        if (!lobbyControllers.containsKey(map)) {
            lobbyControllers.put(map, new Lobby(map));
            return lobbyControllers.get(map);
        }
        return null;
    }

    public static boolean deleteLobby(Map map) {
        if (lobbyControllers.containsKey(map)) {
            lobbyControllers.remove(map);
            return true;
        }
        return false;
    }

    public static Lobby getLobbyWithPlayer(Player player){
        for(Lobby lobby: lobbyControllers.values()){
            if(lobby.getPlayers().contains(player)) return lobby;
        }
        return null;
    }
}
