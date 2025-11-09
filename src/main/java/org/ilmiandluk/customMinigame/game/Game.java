package org.ilmiandluk.customMinigame.game;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.map.BoundingBox;
import org.ilmiandluk.customMinigame.game.player.GamePlayer;
import org.ilmiandluk.customMinigame.game.repository.SignRepository;
import org.ilmiandluk.customMinigame.game.enums.GameWoolColors;
import org.ilmiandluk.customMinigame.game.player.inventory.BuildItem;
import org.ilmiandluk.customMinigame.game.player.inventory.ExploreItem;
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
import org.ilmiandluk.customMinigame.game.structures.environment.Forest;
import org.ilmiandluk.customMinigame.game.structures.environment.Plain;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;
import org.ilmiandluk.customMinigame.util.controler.PlayerInformationController;
import org.jetbrains.annotations.NotNull;

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
        shuffleColors(players);

        this.gamePlayers = players.stream().
                map(player -> new GamePlayer(player, playerColors.get(player))).
                collect(Collectors.toCollection(ArrayList::new));

        gameMap.setMapGameState(MapGameState.IN_GAME);
        gameMap.setPlayers(players);
        SignRepository.updateSignForMap(gameMap, players.size());
        }

    private void shuffleColors(List<Player> players){
        List<GameWoolColors> shuffled = new ArrayList<>(Arrays.asList(GameWoolColors.values()));
        Collections.shuffle(shuffled);
        for (int i = 0; i < players.size(); i++) {
            playerColors.put(players.get(i), shuffled.get(i));
        }
    }

    /**
     * Вызов из GameController'а.
     * <p>
     * Вызывается единожды. Вызывает постройку сегментов, ждет окончания и начинает игру.
     */
    public void startMapPrepareTask() {
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
                });

                // Проверяем каждую секунду, завершены ли задачи
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(isCancelled()) return;
                        if (!segmentBuilder.haveTasks()) {
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
                    PlayerInformationController.getOrSaveInformation(player.getPlayer());
                    setGameInformation(player.getPlayer());
        }
        );
        updateAllBorders();
    }
    private void setGameInformation(Player player){
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
        new ExploreItem().giveItemToPlayer(player);
        new BuildItem().giveItemToPlayer(player);
        // Телепортируем на базу человечка
        player.teleport(playerOwnedSegments.get(player).getFirst().getLocation().clone().add(0, 40, 0));
    }
    private void buildWoolBorders(MapSegment mapSegment, Player player){
        World bukkitWorld = mapSegment.getLocation().getWorld();
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
        Location pos1 = mapSegment.getLocation().clone().add(0,-1,0);
        Location pos2 = mapSegment.getLocation().clone().add(19,-1,19);
        AbstractStructure structure = mapSegment.getStructure();
        BlockType wool = playerColors.get(player).getBlockType();
        if(structure instanceof Base || structure instanceof MilitarySchool){
            pos2.add(20, 0, 20);
        }
        // Конвертируем точки в BlockVector3
        CuboidRegion region = getBlockVector3s(pos1, pos2, weWorld);
        try (EditSession editSession = WorldEdit.getInstance()
                .newEditSessionBuilder()
                .world(weWorld)
                .fastMode(true)
                .build()) {

            // Заполняем шерстью

            BlockState woolState = wool.getDefaultState();
            editSession.makeWalls(region, woolState);

            // Применяем изменения
            editSession.flushSession();

        } catch (WorldEditException e) {
            e.printStackTrace();
        }
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
        getGamePlayer(player).addStructure(segment.getStructure());
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
        Location mapLocation = gameMap.getMapLocation();
        MapSegment[][] allSegments = gameMap.getSegments();
        int xSize = gameMap.getxSize();
        int zSize = gameMap.getzSize();
        int X = (int) ((location.getBlockX() - mapLocation.getX()) / 20);
        int Z = (int) ((location.getBlockZ() - mapLocation.getZ()) / 20);
        if (X < xSize && X >= 0 && Z < zSize && Z >= 0) {
            MapSegment segment = allSegments[X][Z];
            if (segment == null) return;
            if (segment.getOwner() == player) {
                BuildStructure structure;
                GamePlayer gamePlayer = getGamePlayer(player);
                switch (segment.getStructure().getClass().getSimpleName()){
                    case "Forest":
                        structure = new Sawmill(gamePlayer);
                        if(gamePlayer.canBuild(structure)) {
                            gamePlayer.build(structure);
                            buildSingleSegmentStructure(segment, structure, player);
                        }
                        return;

                    case "Hills":
                        structure = new Mineshaft(gamePlayer);
                        if(gamePlayer.canBuild(structure)) {
                            gamePlayer.build(structure);
                            buildSingleSegmentStructure(segment, structure, player);
                        }
                        return;
                    case "Plain":
                        if(canBuildMilitarySchool(X, Z, player)){
                            structure = new MilitarySchool(gamePlayer);
                            if(gamePlayer.canBuild(structure)) {
                                gamePlayer.build(structure);
                                buildMilitarySchool(X, Z, player, gamePlayer);
                            }
                        }
                        else player.sendMessage(messageManager.getString("game.structureMilitaryBuildError"));
                        return;
                    default:
                        player.sendMessage(messageManager.getString("game.structureBuildHelp"));
                }
            } else {
                player.sendMessage(messageManager.getString("game.structureBuildOnlyOnOwn"));
            }
        }
    }

    /**
     * Вызывается при нажатии игрока ПКМ специальным предметом (Исследовать территорию).
     * <p>
     * Пытается отправить "солдат" на исследование территории.
     * Необходимое количество определяется в config.yml
     * @param player игрок, использующий инструмент
     * @param location блок, в сторону которого игрок смотрел при нажатии
     */
    public void exploreTerritory(Player player, Location location){
        Location mapLocation = gameMap.getMapLocation();
        MapSegment[][] allSegments = gameMap.getSegments();
        int xSize = gameMap.getxSize();
        int zSize = gameMap.getzSize();
        int X = (int) ((location.getBlockX()-mapLocation.getX())/20);
        int Z = (int) ((location.getBlockZ()-mapLocation.getZ())/20);
        if(X < xSize && X >= 0 && Z < zSize && Z >= 0){
            MapSegment segment = allSegments[X][Z];
            String message;
            if(segment == null) return;
            if(segment.getOwner() != null) {
                if(segment.getOwner().equals(player)) {
                    message = messageManager.getString("game.segmentAlreadyYours");
                }
                else{
                    message = messageManager.getString("game.segmentWasOwned",  segment.getOwner());
                }
                player.sendMessage(message);
                return;
            }
            GamePlayer gamePlayer = getGamePlayer(player);
            if(!gamePlayer.canExplore()) {
                player.sendMessage(messageManager.getString("game.cantExplore", configManager.getInt("game.soldiersToExplore")));
                return;
            }

            if(hasAdjacentOwnedSegment(X, Z, player)){
                gamePlayer.explore();
                message = messageManager.getString("game.exploreSegment");
                addSegmentToPlayer(segment, player);

                player.sendMessage(message);
                return;
            }
            player.sendMessage(messageManager.getString("game.exploreTooFar"));
        }

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
        buildWoolBorders(segment, player);
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
     * Проверяет, есть ли рядом сегменты, принадлежащие игроку.
     * На 1 сегмент в каждую сторону света от основного сегмента.
     * @param X X исследуемого сегмента в массиве
     * @param Z Z исследуемого сегмента в массиве
     * @param player игрок, сегменты которого мы ищем
     * @return
     * true - если рядом есть сегмент игрока
     * false - если рядом нет сегмента игрока
     */
    private boolean hasAdjacentOwnedSegment(int X, int Z, Player player) {
        int[][] offsets = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        MapSegment[][] allSegments = gameMap.getSegments();
        int xSize = gameMap.getxSize();
        int zSize = gameMap.getzSize();
        for (int[] o : offsets) {
            int nx = X + o[0];
            int nz = Z + o[1];
            if (nx < 0 || nz < 0 || nx >= xSize || nz >= zSize) continue;

            MapSegment neighbor = allSegments[nx][nz];
            if (neighbor.getOwner() != null && neighbor.getOwner().equals(player)) {
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
        AbstractStructure school = new MilitarySchool(gamePlayer);

        MapSegment[][] s = gameMap.getSegments();
        s[X][Z].setStructure(school);
        s[X+1][Z].setStructure(school);
        s[X][Z+1].setStructure(school);
        s[X+1][Z+1].setStructure(school);

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
    public GamePlayer getGamePlayer(Player player){
        for(GamePlayer gamePlayer : gamePlayers){
            if(gamePlayer.getPlayer().equals(player)){
                return gamePlayer;
            }
        }
        return null;
    }
    public List<Player> getPlayers(){
        return players;
    }
}
