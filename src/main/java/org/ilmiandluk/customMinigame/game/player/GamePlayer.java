package org.ilmiandluk.customMinigame.game.player;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.Game;
import org.ilmiandluk.customMinigame.game.enums.GameWoolColors;
import org.ilmiandluk.customMinigame.game.enums.Resources;
import org.ilmiandluk.customMinigame.game.map.MapSegment;
import org.ilmiandluk.customMinigame.game.player.inventory.*;
import org.ilmiandluk.customMinigame.game.structures.AbstractStructure;
import org.ilmiandluk.customMinigame.game.structures.BuildStructure;
import org.ilmiandluk.customMinigame.game.structures.builds.Base;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;
import org.ilmiandluk.customMinigame.util.controler.PlayerInformationController;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Хранит в себе PlayerResource, PlayerStructures и Scoreboard для отрисовки статистики игрока на экране.
 * <p>Содержит логику "PayDay" для каждой структуры.</p>
 * <p>Также содержит проверки canExplore и canBuild, проверяющие достаточно ли ресурсов у игрока.</p>
 * <p>И explore(), build() методы, которые манипулируют ресурсами игрока (забирают нужные).</p>
 */
public class GamePlayer {
    private final Scoreboard gameScoreboard;
    private final Objective objective;
    private final Player player;
    private final GameWoolColors color;
    private final Game game;
    private final Team team;
    private final Random random = new Random();
    private final ConfigurationManager messageLoader = CustomMinigame.getInstance().getMessagesManager();
    private final ConfigurationManager configLoader = CustomMinigame.getInstance().getConfigManager();

    private BukkitTask scoreboardUpdateTask;
    private BukkitTask payDayTask;

    private PlayerResources playerResources;
    private final PlayerStructures playerStructures = new PlayerStructures();

    private final List<GamePlayer> friendList = new ArrayList<>();
    private final List<GamePlayer> enemyList = new ArrayList<>();
    private final List<MapSegment> mapSegments = new ArrayList<>();

    private final Set<GamePlayer> friendInvites = new HashSet<>();

    private boolean isSpectator = false;
    private String currency = messageLoader.getString("game.currencyNotDefine");
    private String capital = messageLoader.getString("game.capitalNotDefine");

    public GamePlayer(Player player, GameWoolColors color, Game game) {
        this.player = player;
        this.color = color;
        this.game = game;
        this.playerResources = new PlayerResources(this, game);
        this.gameScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = gameScoreboard.registerNewObjective("CustomMinigame_Stats_" + player.getName(), "dummy");
        this.team = gameScoreboard.registerNewTeam(player.getName());
        team.setColor(color.getChatColor());
        team.addEntry(player.getName());
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
        }.runTaskTimer(CustomMinigame.getInstance(), 20L*configLoader.getInt("game.payDayTime"), 20L*configLoader.getInt("game.payDayTime"));
    }
    public PlayerStructures getPlayerStructures(){
        return playerStructures;
    }
    public Team getTeam(){
        return team;
    }
    public List<MapSegment> getMapSegments(){
        return mapSegments;
    }
    public void addMapSegment(MapSegment mapSegment){
        mapSegments.add(mapSegment);
    }
    public void removeMapSegment(MapSegment mapSegment){
        mapSegments.remove(mapSegment);
    }
    public List<MapSegment> getNumberedBase(int number){
        int count = 0;
        List<MapSegment> baseSegments = new ArrayList<>();
        for(MapSegment mapSegment : mapSegments){
            if(mapSegment.getStructure() instanceof Base){
                if(count < 4*number && count >= 4 * (number-1)){
                    baseSegments.add(mapSegment);
                }
                count++;
            }
        }
        return baseSegments;
    }
    public void createPlayerTeam(GamePlayer player){
        Team team = gameScoreboard.registerNewTeam(player.getPlayer().getName());
        team.setColor(player.color.getChatColor());
        team.addEntry(player.getPlayer().getName());
    }
    @Nullable
    public Team getPlayerTeam(Player player){
        return gameScoreboard.getTeam(player.getName());
    }
    @Nullable
    public MapSegment getNearestMilitarySchoolSegment(Location location){
        if (playerStructures.getSchoolMapSegments().isEmpty()) {
            return null;
        }
        MapSegment nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (MapSegment segment : playerStructures.getSchoolMapSegments()) {
            Location segLoc = segment.getLocation();
            if (segLoc == null) continue;

            if (!segLoc.getWorld().equals(location.getWorld())) continue;

            double distance = segLoc.distanceSquared(location);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = segment;
            }
        }

        return nearest;
    }
    public void cancelTasks(){
        this.scoreboardUpdateTask.cancel();
        this.payDayTask.cancel();
    }
    public Game getGame(){
        return game;
    }

    public void restoreInventory(){
        PlayerInformationController.restorePlayer(player);
    }
    public void setSpectator(){
        isSpectator = true;
        player.getInventory().clear();
        player.setInvisible(true);
        new LeaveItem().giveItemToPlayer(player);
        new SpectatorItem().giveItemToPlayer(player);

        scoreboardUpdateTask.cancel();
        payDayTask.cancel();

        for (String entry : gameScoreboard.getEntries()) {
            gameScoreboard.resetScores(entry);
        }
        this.objective.setDisplayName(messageLoader.getString("game.spectatorScoreboardTitle"));
        List<String> messages = messageLoader.getStringList("game.spectatorScoreboard");
        String message;
        for (int i = 0; i < messages.size(); i++) {
            message = replacePlaceholders(messages.get(i));
            objective.getScore(message).setScore(16-i);
        }

        player.setScoreboard(gameScoreboard);
    }
    public boolean isSpectator(){
        return isSpectator;
    }
    public void setGameInformation(){
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFlySpeed(1f);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setExp(0);
        player.setLevel(0);

        // Выдаём нужные предметы игроку
        player.getInventory().clear();
        new ControlItem().giveItemToPlayer(player);
        new BuildItem().giveItemToPlayer(player);
        new StatusItem().giveItemToPlayer(player);
        new SpectatorItem().giveItemToPlayer(player);
        new ShopItem().giveItemToPlayer(player);
    }
    public void winAnotherPlayer(GamePlayer anotherPlayer){
        playerResources.addMoney(anotherPlayer.getPlayerResources().getMoneyCount());
        playerResources.addWood(anotherPlayer.getPlayerResources().getWoodCount());
        playerResources.addIron(anotherPlayer.getPlayerResources().getIronCount());
        playerResources.addStone(anotherPlayer.getPlayerResources().getStoneCount());
        playerResources.addPeople(anotherPlayer.getPlayerResources().getPeopleCount());
        playerResources.addPreciousMetals(anotherPlayer.getPlayerResources().getPreciousMetalsCount());

        List<MapSegment> copy = new ArrayList<>(anotherPlayer.getPlayerStructures().getSchoolMapSegments());
        for (MapSegment segment : copy) {
            playerStructures.addSchoolSegment(segment);
            playerStructures.addStructure(segment.getStructure());
        }
        playerStructures.setSawmillCount(anotherPlayer.getPlayerStructures().getSawmillCount()+playerStructures.getSawmillCount());
        playerStructures.setMineshaftCount(anotherPlayer.getPlayerStructures().getMineshaftCount()+playerStructures.getMineshaftCount());


    }
    public void addStructure(AbstractStructure structure){
        playerStructures.addStructure(structure);
    }
    public void removeStructure(AbstractStructure structure){
        playerStructures.removeStructure(structure);
    }

    public void baseHandler(){
        playerResources.addMoney((long) (playerResources.getPeopleCount() * playerStructures.getBaseCount() *
                        configLoader.getDouble("game.moneyChargeCoefficient", 1)));
        playerResources.addPeople((long) (playerResources.getPeopleCount() * playerStructures.getBaseCount() *
                        configLoader.getDouble("game.populationGrowthCoefficient", 0.1) +
                        configLoader.getDouble("game.populationGrowthGuaranteed", 1)));
    }
    public void militarySchoolHandler(){
        int count = playerStructures.getMilitarySchoolCount();
        int perPayday = configLoader.getInt("game.soldiersPerPayday", 1);
        long result = Math.min(playerResources.getPeopleCount(), (long) count*perPayday);
        if(game == null) return;

        playerResources.addPeople(-result);
        playerResources.addSoldiers(result);
        for(MapSegment segment : playerStructures.getSchoolMapSegments()){
            for (int i = 0; i < perPayday; i++) {
                getPlayerResources().createSoldier(segment);
            }
        }
    }
    public PlayerResources getPlayerResources() {
        return playerResources;
    }
    public void addFriendInvite(GamePlayer gamePlayer){
        this.friendInvites.add(gamePlayer);
    }
    public boolean haveFriendRequestFrom(GamePlayer gamePlayer){
        return friendInvites.contains(gamePlayer);
    }
    public void removeFriendInvite(GamePlayer gamePlayer){
        friendInvites.remove(gamePlayer);
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
    public GameWoolColors getColor(){
        return color;
    }
    public List<GamePlayer> getFriendList(){
        return friendList;
    }
    public List<GamePlayer> getEnemyList(){
        return enemyList;
    }
    public void addEnemy(GamePlayer player){
        if(!enemyList.contains(player)){
            enemyList.add(player);
        }
    }
    public void addFriend(GamePlayer player){
        if(!friendList.contains(player)){
            friendList.add(player);
            // Друг моего друга - мой друг, верно?
            for(GamePlayer p : player.getFriendList()){
                if(p != player && p!= this){
                    friendList.add(p);
                    p.addFriend(player);
                    p.getPlayer().sendMessage(messageLoader.getString("game.playerJoinAlly", this));
                }
            }
        }
    }
    public void removeFriend(GamePlayer player){
        friendList.remove(player);
    }
    public void removeEnemy(GamePlayer player){
        enemyList.remove(player);
    }
    public String getCurrency(){
        return currency;
    }
    public String getCapital(){
        return capital;
    }
    public void setCurrency(String currency){
        this.currency = currency;
    }
    public void setCapital(String capital){
        this.capital = capital;
    }
    public void updateScoreboard(){
        for (String entry : gameScoreboard.getEntries()) {
            gameScoreboard.resetScores(entry);
        }
        List<String> messages = messageLoader.getStringList("game.playerScoreboard");
        String message;
        for (int i = 0; i < messages.size(); i++) {
            message = replacePlaceholders(messages.get(i));
            objective.getScore(message).setScore(16-i);
        }

        player.setScoreboard(gameScoreboard);
    }
    public String replacePlaceholders(String s){
        s = s.replace("%player%", player.getName())
                .replace("%color%", color.getColorString())
                .replace("%wood%", playerResources.getWoodCount()+"")
                .replace("%stone%", playerResources.getStoneCount()+"")
                .replace("%iron%", playerResources.getIronCount()+"")
                .replace("%precious%", playerResources.getPreciousMetalsCount()+"")
                .replace("%people%", playerResources.getPeopleCount()+"")
                .replace("%money%", playerResources.getMoneyCount()+"")
                .replace("%soldiers%", playerResources.getSoldiersCount()+"")
                .replace("%capital%", getCapital())
                .replace("%currency%", getCurrency());
        return s;
    }
    public List<String> replacePlaceholders(List<String> listS, GamePlayer gamePlayer){
        List<String> result = new ArrayList<>();
        String status = messageLoader.getString("game.statusNeutral");
        if(gamePlayer.getFriendList().contains(this)){
            status = messageLoader.getString("game.statusFriend");
        }
        else if(gamePlayer.getEnemyList().contains(this)){
            status = messageLoader.getString("game.statusEnemy");
        }
        for(String s: listS) {
            result.add(s.replace("%player%", player.getName())
                    .replace("%color%", color.getColorString())
                    .replace("%wood%", playerResources.getWoodCount() + "")
                    .replace("%stone%", playerResources.getStoneCount() + "")
                    .replace("%iron%", playerResources.getIronCount() + "")
                    .replace("%precious%", playerResources.getPreciousMetalsCount() + "")
                    .replace("%people%", playerResources.getPeopleCount() + "")
                    .replace("%money%", playerResources.getMoneyCount() + "")
                    .replace("%soldiers%", playerResources.getSoldiersCount() + "")
                    .replace("%capital%", getCapital())
                    .replace("%currency%", getCurrency())
                    .replace("%status%", status));
        }
        return result;
    }
}
