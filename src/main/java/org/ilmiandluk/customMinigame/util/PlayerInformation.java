package org.ilmiandluk.customMinigame.util;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
    Этот класс нужен просто для того, чтобы хранить информацию о
    содержимом инвентаря игрока, его локации и режиме игры
    до того, как он попал в игру.

    Чтобы после окончания игры вернуть его на прежнюю позицию,
    вернуть прежние вещи (и удалить предметы плагина).

    А также изменить режим игры на тот, который был установлен ранее.
    Допустим, вернуть Режим креатива, хотя в игре был установлен
    Режим приключения и т.п.
 */
public class PlayerInformation implements ConfigurationSerializable {
    private ItemStack[] playerInventory;
    private Player player;
    private Location playerLocation;
    private GameMode gameMode;
    public PlayerInformation(Player player){
        this.playerInventory = player.getInventory().getContents();
        this.playerLocation = player.getLocation();
        this.gameMode = player.getGameMode();
        this.player = player;
    }

    public ItemStack[] getPlayerInventory(){
        return playerInventory;
    }
    public Location getPlayerLocation(){
        return playerLocation;
    }
    public GameMode getGameMode(){
        return gameMode;
    }
    // Может пригодиться, если я захочу потом инвентари сохранять в файл?
    @Override
    public Map<String, Object> serialize() {
        return Map.of("playerInventory", playerInventory,
                "playerLocation", playerLocation,
                "gameMode", gameMode.getValue());
    }
}

