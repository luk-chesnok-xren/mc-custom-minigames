package org.ilmiandluk.customMinigame.game.structures.builds;

import org.bukkit.entity.Player;
import org.ilmiandluk.customMinigame.game.player.GamePlayer;
import org.ilmiandluk.customMinigame.game.structures.BuildStructure;

public class MilitarySchool implements BuildStructure {
    private GamePlayer owner;
    public MilitarySchool(GamePlayer player){
        this.owner = player;
    }
    public Player getOwner() {
        return owner.getPlayer();
    }

    @Override
    public void changeOwner(GamePlayer newOwner) {
        this.owner = newOwner;
    }

}
