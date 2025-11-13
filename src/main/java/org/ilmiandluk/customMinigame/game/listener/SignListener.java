package org.ilmiandluk.customMinigame.game.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.ilmiandluk.customMinigame.game.Sign;
import org.ilmiandluk.customMinigame.game.handler.SignHandler;
import org.ilmiandluk.customMinigame.game.repository.SignRepository;

public class SignListener implements Listener{
    private final SignHandler signHandler;
    public SignListener(SignHandler signHandler) {
        this.signHandler = signHandler;
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event){
        if(event.getBlock().getState() instanceof org.bukkit.block.Sign) {
            for (Sign sign : SignRepository.getAllSigns()) {
                if (event.getBlock().getLocation().equals(sign.getLocation())){
                    signHandler.handleOnSignBreak(event);
                }
            }
        }
    }

    @EventHandler
    public void interactWithSign(PlayerInteractEvent event){
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            assert event.getClickedBlock() != null;
            if (event.getClickedBlock().getState() instanceof org.bukkit.block.Sign){
                signHandler.handleInteractWithSign(event);
            }
        }
    }
}
