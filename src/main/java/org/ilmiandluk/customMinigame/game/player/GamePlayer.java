package org.ilmiandluk.customMinigame.game.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.enums.GameWoolColors;
import org.ilmiandluk.customMinigame.game.enums.Resources;
import org.ilmiandluk.customMinigame.game.structures.AbstractStructure;
import org.ilmiandluk.customMinigame.game.structures.BuildStructure;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;
import org.ilmiandluk.customMinigame.util.controler.PlayerInformationController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Хранит в себе PlayerResource, PlayerStructures и Scoreboard для отрисовки статистики игрок на экране.
 * <p>Содержит логику "PayDay" для каждой структуры.</p>
 * <p>Также содержит проверки canExplore и canBuild, проверяющие достаточно ли ресурсов у игрока.</p>
 * <p>И explore(), build() методы, которые манипулируют ресурсами игрока (забирают нужные).</p>
 */
public class GamePlayer {
    private final Scoreboard gameScoreboard;
    private final Objective objective;
    private final Player player;
    private final GameWoolColors color;
    private final Random random = new Random();
    private final ConfigurationManager messageLoader = CustomMinigame.getInstance().getMessagesManager();
    private final ConfigurationManager configLoader = CustomMinigame.getInstance().getConfigManager();

    private BukkitTask scoreboardUpdateTask;
    private BukkitTask payDayTask;

    private final PlayerResources playerResources = new PlayerResources();
    private final PlayerStructures playerStructures = new PlayerStructures();

    public GamePlayer(Player player, GameWoolColors color) {
        this.player = player;
        this.color = color;
        this.gameScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = gameScoreboard.registerNewObjective("CustomMinigame_Stats_" + player.getName(), "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective.setDisplayName(messageLoader.getString("game.playerScoreboardTitle"));
        this.scoreboardUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(isCancelled()) return;
                updateScoreboard();
            }
        }.runTaskTimer(CustomMinigame.getInstance(), 0, 20);
        this.payDayTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(isCancelled()) return;
                sawmillHandler();
                mineshaftHandler();
                baseHandler();
                militarySchoolHandler();
            }
        }.runTaskTimer(CustomMinigame.getInstance(), 0, 20*60);
    }

    public void restoreInventory(){
        PlayerInformationController.restorePlayer(player);
    }
    public void addStructure(AbstractStructure structure){
        playerStructures.addStructure(structure);
    }
    public void removeStructure(AbstractStructure structure){
        playerStructures.removeStructure(structure);
    }
    public void baseHandler(){
        playerResources.addMoney((long) (playerResources.getPeopleCount() *
                        configLoader.getDouble("game.moneyChargeCoefficient", 1)));
        playerResources.addPeople((long) (playerResources.getPeopleCount() *
                        configLoader.getDouble("game.populationGrowthCoefficient", 0.1) +
                        configLoader.getDouble("game.populationGrowthGuaranteed", 1)));
    }
    public void militarySchoolHandler(){
        int count = playerStructures.getMilitarySchoolCount();
        int perPayday = configLoader.getInt("game.soldiersPerPayday", 1);
        long result = Math.min(playerResources.getPeopleCount(), (long) count*perPayday);
        playerResources.addPeople(-result);
        playerResources.addSoldiers(result);
    }
    public boolean canExplore(){
        int count = configLoader.getInt("game.soldiersToExplore", 2);
        return playerResources.getSoldiersCount() >= count;
    }
    public boolean canBuild(BuildStructure structure){
        Map<Resources, Integer> resourceGap = new HashMap<>();
        return switch (structure.getClass().getSimpleName()) {
            case "MilitarySchool" -> {
                for (String key : configLoader.
                        getConfigurationSection("game.militarySchoolBuildResources").
                        getKeys(false)) {
                    Resources resource = Resources.of(key);
                    if (resource != null) {
                        int count = configLoader.
                                getInt("game.militarySchoolBuildResources." + key, 1);
                        if (resource.getResourceCount(playerResources) < count) resourceGap.put(resource, count);
                    }
                }
                if(!resourceGap.isEmpty()){
                    player.sendMessage(messageLoader.getString("game.notEnoughResourceToBuild", resourceGap));
                    yield false;
                }
                yield true;
            }
            case "Mineshaft" -> {
                for (String key : configLoader.
                        getConfigurationSection("game.mineshaftBuildResources").
                        getKeys(false)) {
                    Resources resource = Resources.of(key);
                    if (resource != null) {
                        int count = configLoader.
                                getInt("game.mineshaftBuildResources." + key, 1);
                        if (resource.getResourceCount(playerResources) < count) resourceGap.put(resource, count);
                    }
                }
                if(!resourceGap.isEmpty()){
                    player.sendMessage(messageLoader.getString("game.notEnoughResourceToBuild", resourceGap));
                    yield false;
                }
                yield true;
            }
            case "Sawmill" -> {
                for (String key : configLoader.
                        getConfigurationSection("game.sawmillBuildResources").
                        getKeys(false)) {
                    Resources resource = Resources.of(key);
                    if (resource != null) {
                        int count = configLoader.
                                getInt("game.sawmillBuildResources." + key, 1);
                        if (resource.getResourceCount(playerResources) < count) resourceGap.put(resource, count);
                    }
                }
                if(!resourceGap.isEmpty()){
                    player.sendMessage(messageLoader.getString("game.notEnoughResourceToBuild", resourceGap));
                    yield false;
                }
                yield true;
            }
            default -> false;
        };
    }
    public void build(BuildStructure structure){
        switch (structure.getClass().getSimpleName()) {
            case "MilitarySchool" -> {
                addStructure(structure);
                for (String key : configLoader.
                        getConfigurationSection("game.militarySchoolBuildResources").
                        getKeys(false)) {
                    Resources resource = Resources.of(key);
                    if (resource != null) {
                        int count = configLoader.
                                getInt("game.militarySchoolBuildResources." + key, 1);
                        resource.popResource(playerResources, count);

                    }
                }
            }
            case "Mineshaft" -> {
                addStructure(structure);
                for (String key : configLoader.
                        getConfigurationSection("game.mineshaftBuildResources").
                        getKeys(false)) {
                    Resources resource = Resources.of(key);
                    if (resource != null) {
                        int count = configLoader.
                                getInt("game.mineshaftBuildResources." + key, 1);
                        resource.popResource(playerResources, count);
                    }
                }
            }
            case "Sawmill" -> {
                addStructure(structure);
                for (String key : configLoader.
                        getConfigurationSection("game.sawmillBuildResources").
                        getKeys(false)) {
                    Resources resource = Resources.of(key);
                    if (resource != null) {
                        int count = configLoader.
                                getInt("game.sawmillBuildResources." + key, 1);
                        resource.popResource(playerResources, count);
                    }
                }
            }
        }
    }
    public void explore(){
        int count = configLoader.getInt("game.soldiersToExplore", 2);
        playerResources.addSoldiers(-count);
        getPlayer().sendMessage(messageLoader.getString("game.soldiersGone", count));
        new BukkitRunnable() {
            @Override
            public void run() {
                playerResources.addSoldiers(count);
                getPlayer().sendMessage(messageLoader.getString("game.soldiersBack", count));
            }
        }.runTaskLater(CustomMinigame.getInstance(), 20L*configLoader.getInt("game.exploreTime", 60));
    }
    public void mineshaftHandler(){
        int count = playerStructures.getMineshaftCount();
        for(String key: configLoader.getConfigurationSection("game.mineshaftProvides").getKeys(false)){
            Resources resource = Resources.of(key);
            if(resource != null){
                resource.addResource(playerResources, count,
                        configLoader.getInt("game.mineshaftProvides."+key+".origin", 1),
                        configLoader.getInt("game.mineshaftProvides."+key+".bound", 2));
            }
        }
    }
    public void sawmillHandler(){
        int count = playerStructures.getSawmillCount();
        for(String key: configLoader.getConfigurationSection("game.sawmillProvides").getKeys(false)){
            Resources resource = Resources.of(key);
            if(resource != null){
                resource.addResource(playerResources, count,
                        configLoader.getInt("game.sawmillProvides."+key+".origin", 1),
                        configLoader.getInt("game.sawmillProvides."+key+".bound", 2));
            }
        }
    }
    public Player getPlayer() {
        return player;
    }
    public void updateScoreboard(){
        for (String entry : gameScoreboard.getEntries()) {
            gameScoreboard.resetScores(entry);
        }
        List<String> messages = messageLoader.getStringList("game.playerScoreboard");
        String message;
        for (int i = 0; i < messages.size(); i++) {
            message = replacePlaceholders(messages.get(i));
            objective.getScore(message).setScore(13-i);
        }

        player.setScoreboard(gameScoreboard);
    }
    private String replacePlaceholders(String s){
        s = s.replace("%player%", player.getName())
                .replace("%color%", color.getColorString())
                .replace("%wood%", playerResources.getWoodCount()+"")
                .replace("%stone%", playerResources.getStoneCount()+"")
                .replace("%iron%", playerResources.getIronCount()+"")
                .replace("%precious%", playerResources.getPreciousMetalsCount()+"")
                .replace("%people%", playerResources.getPeopleCount()+"")
                .replace("%money%", playerResources.getMoneyCount()+"")
                .replace("%soldiers%", playerResources.getSoldiersCount()+"");
        return s;
    }
}
