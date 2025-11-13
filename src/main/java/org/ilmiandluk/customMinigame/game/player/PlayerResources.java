package org.ilmiandluk.customMinigame.game.player;

import org.bukkit.Location;
import org.bukkit.scoreboard.Team;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.Game;
import org.ilmiandluk.customMinigame.game.entity.Soldier;
import org.ilmiandluk.customMinigame.game.enums.SoldierRelate;
import org.ilmiandluk.customMinigame.game.map.MapSegment;
import org.ilmiandluk.customMinigame.game.structures.builds.Base;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerResources {
    private final Random random = new Random();
    private final Game game;
    private final GamePlayer gamePlayer;
    private final ConfigurationManager messageLoader = CustomMinigame.getInstance().getMessagesManager();
    private final ConfigurationManager configLoader = CustomMinigame.getInstance().getConfigManager();
    private long woodCount = random.
            nextLong(configLoader.
                            getInt("game.startPlayerResources.wood.origin", 10),
                    configLoader.
                            getInt("game.startPlayerResources.wood.bound", 15));
    private long stoneCount = random.
            nextLong(configLoader.
                            getInt("game.startPlayerResources.stone.origin", 8),
                    configLoader.
                            getInt("game.startPlayerResources.stone.bound", 12));
    private long ironCount = random.
            nextLong(configLoader.
                            getInt("game.startPlayerResources.iron.origin", 4),
                    configLoader.
                            getInt("game.startPlayerResources.iron.bound", 6));
    private long preciousMetalsCount = random.
            nextLong(configLoader.
                            getInt("game.startPlayerResources.precious.origin", 0),
                    configLoader.
                            getInt("game.startPlayerResources.precious.bound", 2));

    private long peopleCount = random.
            nextLong(configLoader.
                            getInt("game.startPlayerResources.people.origin", 6),
                    configLoader.
                            getInt("game.startPlayerResources.people.bound", 8));
    private long soldiersCount = random.
            nextLong(configLoader.
                            getInt("game.startPlayerResources.soldiers.origin", 3),
                    configLoader.
                            getInt("game.startPlayerResources.soldiers.bound", 4));

    private long moneyCount = random.
            nextLong(configLoader.
                            getInt("game.startPlayerResources.money.origin", 100),
                    configLoader.
                            getInt("game.startPlayerResources.money.bound", 150));

    private final List<Soldier> soldierList = new ArrayList<>();

    PlayerResources(GamePlayer player, Game game){
        this.gamePlayer = player;
        this.game = game;
    }
    public void addMoney(long count){
        this.moneyCount += count;
    }
    public void addWood(long count){
        this.woodCount += count;
    }
    public void addIron(long count){
        this.ironCount+= count;
    }
    public void addStone(long count){
        this.stoneCount+= count;
    }
    public void addPreciousMetals(long count){
        this.preciousMetalsCount+= count;
    }
    public void addPeople(long count){
        this.peopleCount+= count;
    }
    public void addSoldiers(long count){
        this.soldiersCount+= count;
        if(game == null) return;
        if(count > 0){
            gamePlayer.getPlayer().
                    sendMessage(messageLoader.
                            getString("game.addSoldier", (int) count));
        }
        else if(count < 0){
            gamePlayer.getPlayer().
                    sendMessage(messageLoader.
                            getString("game.deadSoldier"));
        }
    }
    public void createSoldier(MapSegment segment){
        Location location = segment.getLocation();
        if(segment.getStructure() instanceof Base)
            location = location.clone().add(20, 2, 20);
        else
            location = location.clone().add(0, 2, 0);
        Soldier soldier = new Soldier(location,
                gamePlayer,
                game.getChunkController(),
                segment);
        segment.addSoldier(soldier.setRelate(SoldierRelate.OWNER));
        //Set glowing
        soldier.k(true);
        soldier.spawnMob();
        gamePlayer.getTeam().addEntry(soldier.getEntity().getUniqueId().toString());
        for(GamePlayer otherPlayer: game.getGamePlayersWithout(gamePlayer)) {
            Team otherTeam = otherPlayer.getPlayerTeam(gamePlayer.getPlayer());
            if (otherTeam != null) {
                if (soldier.getEntity() != null)
                    otherTeam.addEntry(soldier.getEntity().getUniqueId().toString());
            }
        }
        soldierList.add(soldier);
    }
    public List<Soldier> getSoldiers(){
        return soldierList;
    }
    public void removeSoldier(Soldier soldier){
        soldierList.remove(soldier);
        addSoldiers(-1);
    }
    public long getWoodCount() {
        return woodCount;
    }

    public void setWoodCount(long woodCount) {
        this.woodCount = woodCount;
    }

    public long getStoneCount() {
        return stoneCount;
    }

    public void setStoneCount(long stoneCount) {
        this.stoneCount = stoneCount;
    }

    public long getIronCount() {
        return ironCount;
    }

    public void setIronCount(long ironCount) {
        this.ironCount = ironCount;
    }

    public long getPreciousMetalsCount() {
        return preciousMetalsCount;
    }

    public void setPreciousMetalsCount(long preciousMetalsCount) {
        this.preciousMetalsCount = preciousMetalsCount;
    }

    public long getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(long peopleCount) {
        this.peopleCount = peopleCount;
    }

    public long getSoldiersCount() {
        return soldiersCount;
    }

    public void setSoldiersCount(long soldiersCount) {
        this.soldiersCount = soldiersCount;
    }

    public long getMoneyCount() {
        return moneyCount;
    }

    public void setMoneyCount(long moneyCount) {
        this.moneyCount = moneyCount;
    }
}
