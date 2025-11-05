package org.ilmiandluk.customMinigame.game.structures;

import org.ilmiandluk.customMinigame.game.player.GamePlayer;

public interface BuildStructure extends AbstractStructure{
    void changeOwner(GamePlayer newOwner);
}
