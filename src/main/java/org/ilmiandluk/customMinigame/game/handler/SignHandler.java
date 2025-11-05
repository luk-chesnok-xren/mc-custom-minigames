package org.ilmiandluk.customMinigame.game.handler;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.Lobby;
import org.ilmiandluk.customMinigame.game.controller.LobbyController;
import org.ilmiandluk.customMinigame.game.Sign;
import org.ilmiandluk.customMinigame.game.repository.MapRepository;
import org.ilmiandluk.customMinigame.game.repository.SignRepository;
import org.ilmiandluk.customMinigame.game.map.Map;
import org.ilmiandluk.customMinigame.game.enums.MapGameState;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;

public class SignHandler {
    private final ConfigurationManager messageManager
            = CustomMinigame.getInstance().getMessagesManager();

    public void handleOnSignBreak(BlockBreakEvent event) {
        if (!event.getPlayer().isOp()) {
            event.setCancelled(true);
            return;
        }
        event.getPlayer().sendMessage(messageManager.getString("admin.signWasRemoved"));
        SignRepository.removeSign(new Sign("any", 0, event.getBlock().getLocation()));
    }

    public void handleOnSignBreak(SignChangeEvent event){
        event.setCancelled(true);
    }

    public void handleInteractWithSign(PlayerInteractEvent event){
        for (Sign sign : SignRepository.getAllSigns()) {
            if (event.getClickedBlock().getLocation().equals(sign.getLocation())) {
                event.setCancelled(true);
                Lobby lobby = LobbyController.getLobby(MapRepository.getMap(sign.getMapName()));
                Map map = MapRepository.getMap(sign.getMapName());
                if (lobby != null && lobby.getPlayers().contains(event.getPlayer())) {
                    lobby.leave(event.getPlayer());
                    return;
                } else if (map.getMapGameState().equals(MapGameState.READY)) {
                    if (lobby == null) {
                        lobby = LobbyController.createLobby(MapRepository.getMap(sign.getMapName()));
                    }
                    assert lobby != null;
                    lobby.join(event.getPlayer());
                } else {
                    event.getPlayer().sendMessage(messageManager.getString("lobby.cantJoin"));
                }
            }
        }
    }
}
