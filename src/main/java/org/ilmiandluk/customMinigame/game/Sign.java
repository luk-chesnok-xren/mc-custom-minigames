package org.ilmiandluk.customMinigame.game;

import org.bukkit.Location;

public class Sign {
    private String mapName;
    private int maxPlayers;
    private Location location;
    private int currentPlayers = 0;

    public Sign(String mapName, int maxPlayers, Location location){
        this.mapName = mapName;
        this.maxPlayers = maxPlayers;
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
    public String getMapName(){
        return mapName;
    }
    public int getMaxPlayers(){
        return maxPlayers;
    }
    public int getCurrentPlayers(){
        return currentPlayers;
    }
    public void setCurrentPlayers(int currentPlayers){
        this.currentPlayers = currentPlayers;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            Sign other = (Sign) obj;
            return other.getLocation().equals(this.getLocation());
        }
    }
}
