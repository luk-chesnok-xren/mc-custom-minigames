package org.ilmiandluk.customMinigame.util.controler;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.entity.Player;
import org.ilmiandluk.customMinigame.util.PlayerInformation;

import java.util.HashMap;

public class PlayerInformationController {
    private static HashMap<Player, PlayerInformation> informationHashMap = new HashMap<>();
    //private static File inventoryFile;

    @CanIgnoreReturnValue
    public static PlayerInformation getOrSaveInformation(Player player){
        return informationHashMap.get(player) == null
                ? informationHashMap.put(player, new PlayerInformation(player))
                : informationHashMap.get(player);
    }

    public static void restorePlayer(Player player){
        PlayerInformation information = informationHashMap.get(player);
        if(information == null){
            return;
        }
        player.setFlySpeed(0.5f);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setGameMode(information.getGameMode());
        player.getInventory().setContents(information.getPlayerInventory());
        player.teleport(information.getPlayerLocation());
        informationHashMap.remove(player);
    }

}