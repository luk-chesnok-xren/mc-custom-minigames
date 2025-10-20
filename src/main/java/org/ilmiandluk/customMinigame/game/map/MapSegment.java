package org.ilmiandluk.customMinigame.game.map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.ilmiandluk.customMinigame.game.structures.AbstractStructure;

record MapSegment (
    AbstractStructure structure,
    Location loc,
    Player owner
){}
