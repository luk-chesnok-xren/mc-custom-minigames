package org.ilmiandluk.customMinigame.game;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.map.Map;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    private final Map lobbyMap;
    private final int maxPlayers;

    /*
        Получим Instance плагина для отправки сообщений через него.
        Также получим messageManager, для загрузки сообщений из messages.yml
        Ну и configManager, чтобы получить minPlayersToStart и startTimer
    */
    private final CustomMinigame plugin
            = CustomMinigame.getInstance();
    private final ConfigurationManager messageManager
            = CustomMinigame.getInstance().getMessagesManager();
    private final ConfigurationManager configManager
            = CustomMinigame.getInstance().getConfigManager();

    // Будем запускать, если в лобби зайдет второй человек
    // и удалять, если кто-то вышел и количество игроков < 2
    private BukkitTask gameStartTimer;

    private final List<Player> players = new ArrayList<>();

    Lobby(Map lobbyMap) {
        this.lobbyMap = lobbyMap;
        this.maxPlayers = lobbyMap.getMaxPlayers();
    }
    private BukkitTask getGameStartTimer() {
        return new BukkitRunnable() {
            int maxSeconds = configManager.getInt("lobby.startTimer", 20);
            int seconds = maxSeconds;
            @Override
            public void run() {
                if(isCancelled()) return;
                if (seconds > 0) {
                    players.forEach(player -> {
                        player.sendActionBar(messageManager.getString("lobby.timeToStart", seconds));
                        player.setExp(1f - (float) (seconds-1)/maxSeconds);
                        player.playSound((Entity) player, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
                    });
                    seconds--;
                }
                else{
                    /*
                        Когда таймер заканчивается - игра должна начинаться.
                        Создаем новую игру через GameController.

                        Удаляем это лобби через LobbyController.

                    */
                    GameController.createGame(lobbyMap, players);
                    LobbyController.deleteLobby(lobbyMap);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void anotherLobbySolver(Player player){
        Lobby oldLobby = LobbyController.getLobbyWithPlayer(player);
        if(oldLobby != null && !oldLobby.equals(this)){
            oldLobby.leave(player);
        }
    }

    public boolean join(Player player) {
        if(LobbyController.getLobbyWithPlayer(player)!=null) anotherLobbySolver(player);

        if(!players.contains(player)){
            if(players.size() == maxPlayers) return false;
            players.add(player);
            players.forEach(p -> p.
                    sendMessage(messageManager.
                            getString("lobby.joinMessage", player, players.size(), maxPlayers)));
            if(players.size() == configManager.getInt("lobby.minPlayersToStart", 2)){
                players.forEach(p -> p.sendMessage(messageManager.getString("lobby.enoughToStart")));
                this.gameStartTimer = getGameStartTimer();
            }

            // Также, нужно обновить все таблички в мире, ссылающиеся на лобби
            // мы хотим изменить количество игроков на табличке, если что
            SignController.updateSignForMap(lobbyMap, players.size());
            return true;
        }

        return false;
    }

    public boolean leave(Player player) {
        if(LobbyController.getLobbyWithPlayer(player)!=null) anotherLobbySolver(player);

        if(players.contains(player)){
            players.remove(player);
            players.forEach(p -> p.
                    sendMessage(messageManager.
                            getString("lobby.leaveMessage", player, players.size(), maxPlayers)));

            if(players.size() < configManager.getInt("lobby.minPlayersToStart", 2)){
                if(gameStartTimer != null) gameStartTimer.cancel();
            }

            // Также, нужно обновить все таблички в мире, ссылающиеся на лобби
            SignController.updateSignForMap(lobbyMap, players.size());
            return true;
        }
        return false;
    }

    public List<Player> getPlayers() {
        return players;
    }
    public Map getLobbyMap() {
        return lobbyMap;
    }

}
