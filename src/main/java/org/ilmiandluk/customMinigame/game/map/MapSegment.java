package org.ilmiandluk.customMinigame.game.map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.ilmiandluk.customMinigame.game.structures.AbstractStructure;

public class MapSegment {
    private AbstractStructure structure;
    private final Location location;
    private Player owner;

    public MapSegment(AbstractStructure structure, Location location, Player owner) {
        this.structure = structure;
        this.location = location;
        this.owner = owner;
    }

    public AbstractStructure getStructure() {
        return structure;
    }

    public Location getLocation() {
        return location.clone();
    }

    public Player getOwner() {
        return owner;
    }

    public void setStructure(AbstractStructure structure) {
        this.structure = structure;
    }

    public void setOwner(Player player) {
        this.owner = player;
    }
}
