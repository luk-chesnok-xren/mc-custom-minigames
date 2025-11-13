package org.ilmiandluk.customMinigame.game;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.minecraft.util.datafix.fixes.ScoreboardDisplayNameFix;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.controller.ChunkController;
import org.ilmiandluk.customMinigame.game.controller.GameController;
import org.ilmiandluk.customMinigame.game.entity.Soldier;
import org.ilmiandluk.customMinigame.game.enums.SoldierRelate;
import org.ilmiandluk.customMinigame.game.enums.SoldierState;
import org.ilmiandluk.customMinigame.game.map.BoundingBox;
import org.ilmiandluk.customMinigame.game.player.GamePlayer;
import org.ilmiandluk.customMinigame.game.player.HandledSegment;
import org.ilmiandluk.customMinigame.game.player.inventory.StatusItem;
import org.ilmiandluk.customMinigame.game.repository.SignRepository;
import org.ilmiandluk.customMinigame.game.enums.GameWoolColors;
import org.ilmiandluk.customMinigame.game.player.inventory.BuildItem;
import org.ilmiandluk.customMinigame.game.player.inventory.ControlItem;
import org.ilmiandluk.customMinigame.game.map.Map;
import org.ilmiandluk.customMinigame.game.enums.MapGameState;
import org.ilmiandluk.customMinigame.game.map.MapSegment;
import org.ilmiandluk.customMinigame.game.map.SegmentBuilder;
import org.ilmiandluk.customMinigame.game.structures.AbstractStructure;
import org.ilmiandluk.customMinigame.game.structures.BuildStructure;
import org.ilmiandluk.customMinigame.game.structures.builds.Base;
import org.ilmiandluk.customMinigame.game.structures.builds.MilitarySchool;
import org.ilmiandluk.customMinigame.game.structures.builds.Mineshaft;
import org.ilmiandluk.customMinigame.game.structures.builds.Sawmill;
import org.ilmiandluk.customMinigame.game.structures.environment.Plain;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;
import org.ilmiandluk.customMinigame.util.controler.PlayerInformationController;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Game {
    private final Map gameMap;
    private final List<GamePlayer> gamePlayers;
    private final BoundingBox boundingBox;
    private final List<Player> players;
    private final SegmentBuilder segmentBuilder;
    private final HashMap<Player, List<MapSegment>> playerOwnedSegments = new HashMap<>();
    private final HashMap<Player, GameWoolColors> playerColors = new HashMap<>();
    private static final HashMap<Player, HandledSegment> handledSegment = new HashMap<>();
    private final ChunkController chunkController;

    /*
        Получим Instance плагина для отправки сообщений через него.
        Также получим messageManager, для загрузки сообщений из messages.yml
        Ну и configManager, чтобы получить minPlayersToStart и startTimer
    */
    private final CustomMinigame plugin
            = CustomMinigame.getInstance();
    private final ConfigurationManager messageManager
            = CustomMinigame.getInstance().getMessagesManager();
    private final ConfigurationManager configManager
            = CustomMinigame.getInstance().getConfigManager();


    public Game(Map gameMap, List<Player> players) {
        this.gameMap = gameMap;
        this.players = players;
        this.boundingBox = gameMap.getBoundingBox();
        this.segmentBuilder = gameMap.getSegmentBuilder();
        this.chunkController = new ChunkController(((CraftWorld) gameMap.getMapLocation().getWorld()).getHandle());
        shuffleColors(players);

        this.gamePlayers = players.stream().
                map(player -> new GamePlayer(player, playerColors.get(player), this)).
                collect(Collectors.toCollection(ArrayList::new));

        gameMap.setMapGameState(MapGameState.IN_GAME);
        gameMap.setPlayers(players);
        SignRepository.updateSignForMap(gameMap, players.size());
        }

    private void shuffleColors(List<Player> players) {
        List<GameWoolColors> shuffled = new ArrayList<>(Arrays.asList(GameWoolColors.values()));
        Collections.shuffle(shuffled);
        for (int i = 0; i < players.size(); i++) {
            playerColors.put(players.get(i), shuffled.get(i));
        }
    }
    public Map getMap(){
        return gameMap;
    }
    /**
     * Вызов из GameController'а.
     * <p>
     * Вызывается единожды. Вызывает постройку сегментов, ждет окончания и начинает игру.
     */
    public void startMapPrepareTask() {
        gamePlayers.forEach(player -> getGamePlayersWithout(player).
                forEach(player::createPlayerTeam));

        gameMap.segmentInitialize();

        new BukkitRunnable() {
            @Override
            public void run() {
                // Показываем игрокам статус
                gamePlayers.forEach(player -> {
                    player.getPlayer().sendTitle(
                            messageManager.getString("game.prepareMapTitle"),
                            messageManager.getString("game.prepareMapSubtitle"),
                            0, 40*20, 0);
                    PlayerInformationController.getOrSaveInformation(player.getPlayer());
                    player.setGameInformation();
                    player.getPlayer().teleport(gameMap.getMapLocation().add(20, 50, 20));
                });

                // Проверяем каждую секунду, завершены ли задачи
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(isCancelled()) return;
                        if (!segmentBuilder.haveTasks()) {
                            createBarrierWall();
                            startGame();
                            this.cancel();
                        }
                        // Иначе продолжаем ждать (проверим снова через 5 секунд)
                    }
                }.runTaskTimer(plugin, 0L, 5*20L); // Проверка каждые 5 секунд
            }
        }.runTaskLater(plugin, 20L);
    }

    private void startGame(){
        gamePlayers.forEach(player -> {
            player.getPlayer().
                sendTitle(messageManager.getString("game.startTitle"),
                        messageManager.getString("game.startSubtitle"),
                        0,5*20,0);
                    player.getPlayer().teleport(playerOwnedSegments.get(player.getPlayer()).getFirst().getLocation().clone().add(0, 40, 0));
        }
        );
        updateAllBorders();
    }
    private void endGame(){
        List<GamePlayer> endPlayers = gamePlayers.stream().filter(player -> !player.isSpectator()).toList();
        boolean everyoneIsAlly = endPlayers.stream().allMatch(p ->
                endPlayers.stream()
                        .filter(other -> other != p)
                        .allMatch(other -> p.getFriendList().contains(other))
        );
        if(everyoneIsAlly || endPlayers.size() == 1) {
            gameMap.setMapGameState(MapGameState.READY);
            new ArrayList<>(gamePlayers).forEach(player -> {
                playerLeave(player);
                new ArrayList<>(player.getPlayerResources().getSoldiers()).
                        forEach(soldier -> soldier.x(0));
            });
            playersWin(endPlayers);
            GameController.deleteGame(this.gameMap);
            SignRepository.updateSignForMap(gameMap, 0);
            chunkController.cleanup();
            chunkController.stopWorking();
        }
    }
    public void playersWin(List<GamePlayer> gamePlayers){
        if(gamePlayers.size() == 1){
            gamePlayers.getLast().cancelTasks();
            gamePlayers.getLast().getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            gamePlayers.getLast().getPlayer().sendTitle(messageManager.getString("game.youAreWinTitle"),
                    messageManager.getString("game.youAreWinSubtitle"), 0, 4*20, 0);
        } else {
            for(GamePlayer gamePlayer : gamePlayers){
                gamePlayer.cancelTasks();
                gamePlayer.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                gamePlayer.getPlayer().sendTitle(messageManager.getString("game.youTeamWinTitle"),
                        messageManager.getString("game.youTeamWinSubtitle"), 0, 4*20, 0);
            }
        }
    }
    public void playerLoose(GamePlayer looser, @Nullable GamePlayer winner){
        looser.getPlayer().sendTitle(messageManager.getString("game.youAreLooseTitle"),
                messageManager.getString("game.youAreLooseSubtitle"), 0, 4*20, 0);
        if(winner != null) {
            winner.winAnotherPlayer(looser);
            playerOwnedSegments.get(winner.getPlayer()).addAll(playerOwnedSegments.get(looser.getPlayer()));
            playerOwnedSegments.remove(looser.getPlayer());
            Arrays.stream(getMap().getSegments()).forEach(s -> Arrays.stream(s).filter(segment -> segment.getOwner() == looser.getPlayer()).forEach(segment -> segment.setOwner(winner.getPlayer())));
        }
        else{
            Arrays.stream(getMap().getSegments()).forEach(s -> Arrays.stream(s).filter(segment -> segment.getOwner() == looser.getPlayer()).forEach(segment -> segment.setOwner(null)));
        }

        looser.setSpectator();
        updateAllBorders();

        // is it end of game?
        endGame();
    }
    public void playerLeave(GamePlayer leaver){
        leaver.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        gamePlayers.remove(leaver);
        players.remove(leaver.getPlayer());
        leaver.restoreInventory();
    }
    // Abilities
    public boolean healAbility(GamePlayer healer, MapSegment segment){
        if(segment == null) return false;
        List<Soldier> soldiers = segment.getSoldiers().stream().
                filter(soldier -> soldier.getGamePlayer() == healer).toList();
        if(soldiers.isEmpty()){
            return false;
        }
        for(Soldier soldier: soldiers){
            soldier.doHeal();
        }
        return true;
    }
    public boolean speedAbility(GamePlayer healer){
        for(Soldier soldier: healer.getPlayerResources().getSoldiers()){
            soldier.doSpeed();
        }
        return true;
    }
    public boolean strengthAbility(GamePlayer healer){
        for(Soldier soldier: healer.getPlayerResources().getSoldiers()){
            soldier.doStrength();
        }
        return true;
    }
    public boolean teleportAbility(GamePlayer healer){
        List<Soldier> soldiers = healer.getPlayerResources().getSoldiers();
        if(soldiers.isEmpty()){
            return false;
        }
        for(Soldier soldier: soldiers){
            soldier.doTeleport();
        }
        return true;
    }
    public boolean rocketAbility(GamePlayer healer, MapSegment segment){
        if(segment == null) return false;
        List<Soldier> soldiers = segment.getSoldiers().stream().
                filter(soldier -> soldier.getSoldierState() != SoldierState.Walking).toList();
        if(soldiers.isEmpty()){
            return false;
        }
        for(Soldier soldier: soldiers){
            soldier.doRocket();
        }
        return true;
    }
    public boolean nuclearAbility(GamePlayer healer, MapSegment segment){
        if(segment == null) return false;
        int[][] offsets = {{1,0}, {-1,0}, {0,1}, {0,-1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        MapSegment[][] allSegments = gameMap.getSegments();
        int xSize = gameMap.getxSize();
        int zSize = gameMap.getzSize();
        int X = segment.getX();
        int Z = segment.getZ();
        for (int[] o : offsets) {
            int nx = X + o[0];
            int nz = Z + o[1];
            if (nx < 0 || nz < 0 || nx >= xSize || nz >= zSize) continue;
            MapSegment s = allSegments[nx][nz];
            List<Soldier> soldiers = s.getSoldiers().stream().
                    filter(soldier -> soldier.getSoldierState() != SoldierState.Walking).toList();
            for (Soldier soldier : soldiers) {
                soldier.doNuclear();
            }
        }
        return true;
    }

    private void build(Location pos1, Location pos2, BlockType block, com.sk89q.worldedit.world.World weWorld){
        CuboidRegion region = getBlockVector3s(pos1, pos2, weWorld);
        try (EditSession editSession = WorldEdit.getInstance()
                .newEditSessionBuilder()
                .world(weWorld)
                .fastMode(true)
                .build()) {

            BlockState blockState = block.getDefaultState();
            editSession.makeWalls(region, blockState);

            editSession.flushSession();

        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }
    /// Дебильное решение, чтобы никто не выпадал за карту
    private void createBarrierWall(){
        World bukkitWorld = gameMap.getMapLocation().getWorld();
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
        Location pos1 = gameMap.getMapLocation().clone().add(-1,-1,-1);
        Location pos2 = gameMap.getMapLocation().clone().add(20*gameMap.getxSize(),4,20*gameMap.getzSize());
        BlockType barrier = BlockTypes.BARRIER;
        if(barrier != null)
            build(pos1, pos2, barrier, weWorld);
    }
    private void buildWoolBorders(MapSegment mapSegment, Player player){
        World bukkitWorld = mapSegment.getLocation().getWorld();
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
        int xSize = 1, zSize = 1;
        if(mapSegment.getStructure() instanceof MilitarySchool
        || mapSegment.getStructure() instanceof Base) {
            xSize = zSize = 2;
        }
        Location pos1 = mapSegment.getLocation().clone().add(0,-1,0);
        Location pos2 = mapSegment.getLocation().clone().add(19+20*(xSize-1),-1,19+20*(zSize-1));
        BlockType wool = playerColors.get(player).getBlockType();
        build(pos1, pos2, wool, weWorld);
    }
    /**
     * Добавляет сегмент игроку.
     * Отстраивает границы сегмента нужным цветом.
     * @param segment сегмент, который нужно добавить игроку
     * @param player владелец сегмента
     */
    synchronized public void addSegmentToPlayer(MapSegment segment, Player player){
        if(!players.contains(player)) return;
        if(!playerOwnedSegments.containsKey(player)){
            playerOwnedSegments.put(player, new ArrayList<>());
        }
        List<MapSegment> segments = playerOwnedSegments.get(player);
        segments.add(segment);
        segment.setOwner(player);
        getGamePlayer(player).addStructure(segment.getStructure());
        buildWoolBorders(segment, player);
    }
    /**
     * Стоит вызывать только из Map, при инициализации сегментов.
     */
    public void addSegmentToPlayerFromMap(MapSegment segment, Player player){
        if(!players.contains(player)) return;
        if(!playerOwnedSegments.containsKey(player)){
            playerOwnedSegments.put(player, new ArrayList<>());
        }
        playerOwnedSegments.get(player).add(segment);
    }

    public void removeSegmentFromPlayer(MapSegment segment, Player player){
        if(playerOwnedSegments.containsKey(player)){
            playerOwnedSegments.get(player).remove(segment);
            getGamePlayer(player).removeStructure(segment.getStructure());
        }
    }
    public void removeSegmentFromPlayerFromMap(MapSegment segment, Player player){
        if(playerOwnedSegments.containsKey(player)){
            playerOwnedSegments.get(player).remove(segment);
        }
    }

    /**
     * Вызывается при нажатии игрока ПКМ специальным предметом (Построить здание).
     * <p>
     * Пытается построить здание, определяемое типом сегмента, на который нажал игрок.
     * Если тип сегмента Forest - пытаемся строить Sawmill;
     * Если тип сегмента Hills - пытаемся строить Mineshaft;
     * Если тип сегмента Plains - проверяет, чтобы рядом было еще 3 Plains, принадлежащие игроку
     * - пытается построить MilitarySchool (структуру 2x2).
     * @param player игрок, использующий инструмент
     * @param location блок, в сторону которого игрок смотрел при нажатии
     */
    public void buildStructure(Player player, Location location) {
        MapSegment segment = getClickedSegment(location);
        if (segment == null) return;
        if (segment.getOwner() == player) {
            BuildStructure structure;
            GamePlayer gamePlayer = getGamePlayer(player);
            switch (segment.getStructure().getClass().getSimpleName()) {
                case "Forest":
                    structure = new Sawmill();
                    if (gamePlayer.canBuild(structure)) {
                        gamePlayer.build(structure);
                        buildSingleSegmentStructure(segment, structure, player);
                    }
                    return;

                case "Hills":
                    structure = new Mineshaft();
                    if (gamePlayer.canBuild(structure)) {
                        gamePlayer.build(structure);
                        buildSingleSegmentStructure(segment, structure, player);
                    }
                    return;
                case "Plain":
                    if (canBuildMilitarySchool(segment.getX(), segment.getZ(), player)) {
                        structure = new MilitarySchool();
                        if (gamePlayer.canBuild(structure)) {
                            gamePlayer.build(structure);
                            buildMilitarySchool(segment.getX(), segment.getZ(), player, gamePlayer);
                        }
                    } else player.sendMessage(messageManager.getString("game.structureMilitaryBuildError"));
                    return;
                default:
                    player.sendMessage(messageManager.getString("game.structureBuildHelp"));
            }
        } else {
            player.sendMessage(messageManager.getString("game.structureBuildOnlyOnOwn"));
        }
    }
    @Nullable
    public MapSegment getClickedSegment(Location location){
        Location mapLocation = gameMap.getMapLocation();
        MapSegment[][] allSegments = gameMap.getSegments();
        int xSize = gameMap.getxSize();
        int zSize = gameMap.getzSize();
        int X = (int) ((location.getBlockX()-mapLocation.getX())/20);
        int Z = (int) ((location.getBlockZ()-mapLocation.getZ())/20);
        if(X < xSize && X >= 0 && Z < zSize && Z >= 0) {
            MapSegment segment = allSegments[X][Z];
            return segment;
        }
        return null;
    }
    public void controlUnits(Action action, MapSegment segment, Player player){
        if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            List<Soldier> freeSoldiers = segment.getFreePlayerSoldiers(getGamePlayer(player));
            if(freeSoldiers.isEmpty()) {
                player.sendMessage(messageManager.getString("game.controlUnitsFreeError"));
                return;
            }
            handledSegment.put(player, new HandledSegment(segment, freeSoldiers.size()));
            player.sendMessage(messageManager.getString("game.controlUnitsLeft", freeSoldiers.size()));
        }
        if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if(handledSegment.get(player) == null) {
                player.sendMessage(messageManager.getString("game.controlUnitsRightError"));
                return;
            }
            if(handledSegment.get(player).count() == 0){
                player.sendMessage(messageManager.getString("game.controlUnitsRightCountError"));
                return;
            }
            MapSegment soldierSegment = handledSegment.get(player).segment();
            if(!hasAdjacentFriendlySegment(segment.getX(), segment.getZ(), player)){
                player.sendMessage(messageManager.getString("game.controlUnitsRightTooFar"));
                return;
            }
                if(segment.getOwner() == null){
                    if(handledSegment.get(player).count() <
                            configManager.getInt("game.soldiersToExplore",2)){
                        player.sendMessage(messageManager.getString("game.cantExplore", configManager.getInt("game.soldiersToExplore",2)));
                        return;
                    }
                    // Мгновенно присваиваем территорию (во избежание любых конфликтов)
                    addSegmentToPlayer(segment, player);
                    player.sendMessage(messageManager.getString("game.soldiersGone"));
                    // Берем выделенных солдат, отправляем их из одного сегмента в другой
                    List<Soldier> handledSoldiers =
                            soldierSegment.getFreePlayerSoldiers(getGamePlayer(player))
                                    .stream().limit(handledSegment.get(player).count())
                                    .toList();
                    for(Soldier soldier : handledSoldiers) {
                        soldierSegment.removeSoldier(soldier);
                        // STATE меняется на Walking внутри moveToLocation
                        // контроль полностью передан Pathfinder'у
                        soldier.moveToLocation(segment.getLocation());
                        // Привязываем солдата к сегменту
                        // Показываем, что солдат становиться OWNER на сегменте
                        segment.addSoldier(soldier.setRelate(SoldierRelate.OWNER));
                    }
                    handledSegment.remove(player);
                }
                else{
                    if(handledSegment.get(player).segment() == segment){
                        player.sendMessage(messageManager.getString("game.segmentsMustNotMatch"));
                        return;
                    }
                    if(segment.getOwner() == player){
                        List<Soldier> handledSoldiers =
                                soldierSegment.getFreePlayerSoldiers(getGamePlayer(player))
                                        .stream().limit(handledSegment.get(player).count())
                                        .toList();
                        for(Soldier soldier : handledSoldiers) {
                            soldierSegment.removeSoldier(soldier);
                            soldier.moveToLocation(segment.getLocation());
                            segment.addSoldier(soldier.setRelate(SoldierRelate.OWNER));
                        }
                        player.sendMessage(messageManager.getString("game.dislocateSoldiers"));
                    }
                    else if(getGamePlayer(player).getFriendList().stream().
                            anyMatch(friend -> segment.getOwner() == friend.getPlayer())){
                        List<Soldier> handledSoldiers =
                                soldierSegment.getFreePlayerSoldiers(getGamePlayer(player))
                                        .stream().limit(handledSegment.get(player).count())
                                        .toList();
                        for(Soldier soldier : handledSoldiers) {
                            soldierSegment.removeSoldier(soldier);
                            soldier.moveToLocation(segment.getLocation());
                            segment.addSoldier(soldier.setRelate(SoldierRelate.FRIEND));
                        }
                        player.sendMessage(messageManager.getString("game.dislocateToFriend"));
                    }
                    else if(getGamePlayer(player).getEnemyList().stream().
                            anyMatch(enemy -> segment.getOwner() == enemy.getPlayer())){
                        List<Soldier> handledSoldiers =
                                soldierSegment.getFreePlayerSoldiers(getGamePlayer(player))
                                        .stream().limit(handledSegment.get(player).count())
                                        .toList();
                        for(Soldier soldier : handledSoldiers) {
                            soldierSegment.removeSoldier(soldier);
                            soldier.moveToLocation(segment.getLocation());
                            segment.addSoldier(soldier.setRelate(SoldierRelate.ENEMY));
                        }
                        player.sendMessage(messageManager.getString("game.dislocateToEnemy"));
                    }
                    else{
                        player.sendMessage(messageManager.getString("game.cantDislocate"));
                    }
                }

        }
    }


    /**
     * Костыль.
     * <p>
     * Обновляет цвета границ всех сегментов на карте.
     * Применяется редко, но метко.
     * <p>
     * Допустим, если какой-то игрок завоевал территорию другого игрока
     * и теперь нужно перерисовать все границы, чтобы они отображались верно.
     */
    public void updateAllBorders(){
        for(java.util.Map.Entry<Player, List<MapSegment>> entry : playerOwnedSegments.entrySet()){
            for (int i = 0; i < entry.getValue().size(); i++) {
                if(i==0){
                    buildWoolBorders(entry.getValue().get(i), entry.getKey());
                    continue;
                }
                Class<? extends AbstractStructure> first = entry.getValue().get(i - 1).getStructure().getClass();
                Class<? extends AbstractStructure> second = entry.getValue().get(i).getStructure().getClass();

                if(first.equals(second)
                        && (first.equals(Base.class) || first.equals(MilitarySchool.class))){
                    continue;
                }
                buildWoolBorders(entry.getValue().get(i), entry.getKey());
            }
        }
    }
    /**
     * Проверяет, достиг ли игрок границы карты.
     * Если да - не дает ему пройти через границу.
     */
    public void tpIfBorderCross(Player player){
        Location pos = player.getLocation();

        if (!boundingBox.contains(pos)) {
            player.teleport(boundingBox.clamp(pos));
        }
    }

    /*
        Вспомогательные методы.
     */
    /**
     * Проверяет, есть ли рядом сегменты, принадлежащие игроку или союзникам.
     * На 1 сегмент в каждую сторону света от основного сегмента.
     * @param X X исследуемого сегмента в массиве
     * @param Z Z исследуемого сегмента в массиве
     * @param player игрок, сегменты которого мы ищем
     * @return
     * true - если рядом есть сегмент игрока
     * false - если рядом нет сегмента игрока
     */
    private boolean hasAdjacentFriendlySegment(int X, int Z, Player player) {
        int[][] offsets = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        MapSegment[][] allSegments = gameMap.getSegments();
        int xSize = gameMap.getxSize();
        int zSize = gameMap.getzSize();
        List<GamePlayer> friendList = getGamePlayer(player).getFriendList();
        for (int[] o : offsets) {
            int nx = X + o[0];
            int nz = Z + o[1];
            if (nx < 0 || nz < 0 || nx >= xSize || nz >= zSize) continue;

            MapSegment neighbor = allSegments[nx][nz];

            if (neighbor.getOwner() != null &&
                    (neighbor.getOwner().equals(player) ||
                            friendList.stream().anyMatch(player1 ->
                                    neighbor.
                                    getOwner().
                                    equals(player1.getPlayer())))) {
                return true;
            }
        }
        return false;
    }


    private void buildSingleSegmentStructure(MapSegment segment, AbstractStructure structure, Player player) {
        segment.setStructure(structure);
        segmentBuilder.buildSegment(segment);
        startBorderCheckTask(segment, player);
        player.sendMessage(messageManager.getString("game.structureBuild"));
    }

    private void buildMilitarySchool(int X, int Z, Player player, GamePlayer gamePlayer) {
        AbstractStructure school = new MilitarySchool();

        MapSegment[][] s = gameMap.getSegments();
        s[X][Z].setStructure(school);
        s[X+1][Z].setStructure(school);
        s[X][Z+1].setStructure(school);
        s[X+1][Z+1].setStructure(school);

        gamePlayer.getPlayerStructures().addSchoolSegment(s[X][Z]);
        segmentBuilder.buildSegment(s[X][Z]);
        startBorderCheckTask(s[X][Z], player);
        player.sendMessage(messageManager.getString("game.structureBuild"));
    }

    private boolean canBuildMilitarySchool(int X, int Z, Player player) {
        if (X + 1 >= gameMap.getxSize() || Z + 1 >= gameMap.getzSize()) return false;

        MapSegment[][] s = gameMap.getSegments();
        return Stream.of(
                s[X][Z], s[X+1][Z], s[X][Z+1], s[X+1][Z+1]
        ).allMatch(seg ->
                seg.getStructure().getClass().equals(Plain.class) &&
                        player.equals(seg.getOwner())
        );
    }

    /**
     * Костыль.
     * <p>
     * Ждет когда все сегменты будут достроены, затем обновляет цвет границы сегмента.
     * <p>
     * Перестраивает белую шерсть на блоки того цвета, который соответствует игроку.
     */
    private void startBorderCheckTask(MapSegment segment, Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isCancelled()) return;
                if (!segmentBuilder.haveTasks()) {
                    buildWoolBorders(segment, player);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
    private static @NotNull CuboidRegion getBlockVector3s(Location pos1, Location pos2, com.sk89q.worldedit.world.World weWorld) {
        BlockVector3 min = BlockVector3.at(
                Math.min(pos1.getBlockX(), pos2.getBlockX()),
                Math.min(pos1.getBlockY(), pos2.getBlockY()),
                Math.min(pos1.getBlockZ(), pos2.getBlockZ())
        );

        BlockVector3 max = BlockVector3.at(
                Math.max(pos1.getBlockX(), pos2.getBlockX()),
                Math.max(pos1.getBlockY(), pos2.getBlockY()),
                Math.max(pos1.getBlockZ(), pos2.getBlockZ())
        );

        return new CuboidRegion(weWorld, min, max);
    }
    public List<MapSegment> getSegments(Player player){
        return playerOwnedSegments.get(player);
    }
    public List<GamePlayer> getGamePlayers(){
        return gamePlayers;
    }
    public List<GamePlayer> getGamePlayersWithout(GamePlayer player){
        List<GamePlayer> gamePlayersWithout = new ArrayList<>();
        for(GamePlayer gamePlayer : gamePlayers){
            if(!gamePlayer.equals(player) && !gamePlayer.isSpectator()) gamePlayersWithout.add(gamePlayer);
        }
        return gamePlayersWithout;
    }
    public GamePlayer getGamePlayer(Player player){
        for(GamePlayer gamePlayer : gamePlayers){
            if(gamePlayer.getPlayer().equals(player)){
                return gamePlayer;
            }
        }
        return null;
    }
    @Nullable
    public ChunkController getChunkController(){
        return chunkController;
    }
    @Nullable
    public HandledSegment getHandledSegment(Player player){
        return handledSegment.get(player);
    }
    public boolean setHandledSegmentUnitsCount(Player player, int count){
        if(handledSegment.get(player) != null){
            MapSegment mapSegment = handledSegment.get(player).segment();
            if(mapSegment.getFreePlayerSoldiers(getGamePlayer(player)).size() >= count){
                handledSegment.put(player, new HandledSegment(mapSegment, count));
                return true;
            }
        }
        return false;
    }
    public List<Player> getPlayers(){
        return players;
    }
}
