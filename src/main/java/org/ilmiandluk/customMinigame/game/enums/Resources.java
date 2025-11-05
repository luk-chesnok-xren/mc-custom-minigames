package org.ilmiandluk.customMinigame.game.enums;

import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.player.PlayerResources;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;

import javax.annotation.Nullable;
import java.util.Random;

public enum Resources {
    WOOD{
        @Override
        public void addResource(PlayerResources playerResources, long count, int origin, int bound) {
            playerResources.addWood(count*random.nextLong(origin, bound));
        }
        @Override
        public void popResource(PlayerResources playerResources, long count){
            playerResources.addWood(count*(-1));
        }
        @Override
        public long getResourceCount(PlayerResources playerResources){
            return playerResources.getWoodCount();
        }
        @Override
        public String getConfigName(){
            return messageLoader.getString("game.resourcesName.wood");
        }
    },
    STONE{
        @Override
        public void addResource(PlayerResources playerResources, long count, int origin, int bound) {
            playerResources.addStone(count*random.nextLong(origin, bound));
        }
        @Override
        public void popResource(PlayerResources playerResources, long count){
            playerResources.addStone(count*(-1));
        }
        @Override
        public long getResourceCount(PlayerResources playerResources){
            return playerResources.getStoneCount();
        }
        @Override
        public String getConfigName(){
            return messageLoader.getString("game.resourcesName.stone");
        }
    },
    IRON{
        @Override
        public void addResource(PlayerResources playerResources, long count, int origin, int bound) {
            playerResources.addIron(count*random.nextLong(origin, bound));
        }
        @Override
        public void popResource(PlayerResources playerResources, long count){
            playerResources.addIron(count*(-1));
        }
        @Override
        public long getResourceCount(PlayerResources playerResources){
            return playerResources.getIronCount();
        }
        @Override
        public String getConfigName(){
            return messageLoader.getString("game.resourcesName.iron");
        }
    },
    PRECIOUS{
        @Override
        public void addResource(PlayerResources playerResources, long count, int origin, int bound) {
            playerResources.addPreciousMetals(count*random.nextLong(origin, bound));
        }
        @Override
        public void popResource(PlayerResources playerResources, long count){
            playerResources.addPreciousMetals(count*(-1));
        }
        @Override
        public long getResourceCount(PlayerResources playerResources){
            return playerResources.getPreciousMetalsCount();
        }
        @Override
        public String getConfigName(){
            return messageLoader.getString("game.resourcesName.precious");
        }
    },
    PEOPLE{
        @Override
        public void addResource(PlayerResources playerResources, long count, int origin, int bound) {
            playerResources.addPeople(count*random.nextLong(origin, bound));
        }
        @Override
        public void popResource(PlayerResources playerResources, long count){
            playerResources.addPeople(count*(-1));
        }
        @Override
        public long getResourceCount(PlayerResources playerResources){
            return playerResources.getPeopleCount();
        }
        @Override
        public String getConfigName(){
            return messageLoader.getString("game.resourcesName.people");
        }
    },
    MONEY{
        @Override
        public void addResource(PlayerResources playerResources, long count, int origin, int bound) {
            playerResources.addMoney(count*random.nextLong(origin, bound));
        }
        @Override
        public void popResource(PlayerResources playerResources, long count){
            playerResources.addMoney(count*(-1));
        }
        @Override
        public long getResourceCount(PlayerResources playerResources){
            return playerResources.getMoneyCount();
        }
        @Override
        public String getConfigName(){
            return messageLoader.getString("game.resourcesName.money");
        }
    },
    SOLDIERS{
        @Override
        public void addResource(PlayerResources playerResources, long count, int origin, int bound) {
            playerResources.addSoldiers(count*random.nextLong(origin, bound));
        }
        @Override
        public void popResource(PlayerResources playerResources, long count){
            playerResources.addSoldiers(count*(-1));
        }
        @Override
        public long getResourceCount(PlayerResources playerResources){
            return playerResources.getSoldiersCount();
        }
        @Override
        public String getConfigName(){
            return messageLoader.getString("game.resourcesName.soldiers");
        }
    };
    private static final Random random = new Random();
    private static final ConfigurationManager messageLoader = CustomMinigame.getInstance().getMessagesManager();

    @Nullable
    public static Resources of(String resource){
        if (resource == null) return null;
        try {
            return Resources.valueOf(resource.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
    public void addResource(PlayerResources playerResources, long count, int origin, int bound){
    }
    public void popResource(PlayerResources playerResources, long count){}
    public long getResourceCount(PlayerResources playerResources){
        return 0;
    }
    public String getConfigName(){
        return "";
    }
}
