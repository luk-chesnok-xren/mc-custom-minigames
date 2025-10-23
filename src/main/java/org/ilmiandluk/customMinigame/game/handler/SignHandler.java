package org.ilmiandluk.customMinigame.game.handler;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.Lobby;
import org.ilmiandluk.customMinigame.game.LobbyController;
import org.ilmiandluk.customMinigame.game.Sign;
import org.ilmiandluk.customMinigame.game.SignController;
import org.ilmiandluk.customMinigame.game.map.Map;
import org.ilmiandluk.customMinigame.game.map.MapController;
import org.ilmiandluk.customMinigame.game.map.MapGameState;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;

public class SignHandler implements Listener {
    private final ConfigurationManager messageManager
            = CustomMinigame.getInstance().getMessagesManager();

    @EventHandler
    public void onSignBreak(BlockBreakEvent event){
        if(event.getBlock().getState() instanceof org.bukkit.block.Sign) {
            for (Sign sign : SignController.getAllSigns()) {
                if (event.getBlock().getLocation().equals(sign.getLocation())){
                    if(!event.getPlayer().isOp()) {
                        event.setCancelled(true);
                        return;
                    }
                    event.getPlayer().sendMessage(messageManager.getString("admin.signWasRemoved"));
                    SignController.removeSign(new Sign("any", 0, event.getBlock().getLocation()));
                }
            }
        }
    }
    @EventHandler
    public void onSignBreak(SignChangeEvent event){
        if(event.getBlock().getState() instanceof org.bukkit.block.Sign) {
            for (Sign sign : SignController.getAllSigns()) {
                if (event.getBlock().getLocation().equals(sign.getLocation())){
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void interactWithSign(PlayerInteractEvent event){
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            assert event.getClickedBlock() != null;
            if (event.getClickedBlock().getState() instanceof org.bukkit.block.Sign){
                for (Sign sign : SignController.getAllSigns()) {
                    if (event.getClickedBlock().getLocation().equals(sign.getLocation())) {
                        event.setCancelled(true);
                        Lobby lobby = LobbyController.getLobby(MapController.getMap(sign.getMapName()));
                        Map map = MapController.getMap(sign.getMapName());
                        if (lobby != null && lobby.getPlayers().contains(event.getPlayer())) {
                            lobby.leave(event.getPlayer());
                            return;
                        } else if (map.getMapGameState().equals(MapGameState.READY)) {
                            if (lobby == null) {
                                lobby = LobbyController.createLobby(MapController.getMap(sign.getMapName()));
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
    }
}
