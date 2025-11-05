package org.ilmiandluk.customMinigame.game.player;

import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;

import java.util.Random;

public class PlayerResources {
    private final Random random = new Random();
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

    PlayerResources(){}
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
