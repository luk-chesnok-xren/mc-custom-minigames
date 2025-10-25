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
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.player.inventory.BuildItem;
import org.ilmiandluk.customMinigame.game.player.inventory.ExploreItem;
import org.ilmiandluk.customMinigame.game.map.Map;
import org.ilmiandluk.customMinigame.game.map.MapGameState;
import org.ilmiandluk.customMinigame.game.map.MapSegment;
import org.ilmiandluk.customMinigame.game.map.SegmentBuilder;
import org.ilmiandluk.customMinigame.game.structures.AbstractStructure;
import org.ilmiandluk.customMinigame.game.structures.builds.Base;
import org.ilmiandluk.customMinigame.game.structures.builds.MilitarySchool;
import org.ilmiandluk.customMinigame.game.structures.builds.Mineshaft;
import org.ilmiandluk.customMinigame.game.structures.builds.Sawmill;
import org.ilmiandluk.customMinigame.game.structures.environment.Forest;
import org.ilmiandluk.customMinigame.game.structures.environment.Hills;
import org.ilmiandluk.customMinigame.game.structures.environment.Plain;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;
import org.ilmiandluk.customMinigame.util.PlayerInformationController;

import java.util.*;

public class Game {
    private final Map gameMap;
    private final List<Player> players;
    private SegmentBuilder segmentBuilder = CustomMinigame.getInstance().getSegmentBuilder();
    private final HashMap<Player, List<MapSegment>> playerOwnedSegments = new HashMap<>();
    private final HashMap<Player, GameWoolColors> playerColors = new HashMap<>();
    private final Location borderLocation1;
    private final Location borderLocation2;

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

    /*
        Ниже будем хранить все таски, чтобы затем отменять их, когда нужно.
     */


    public Game(Map gameMap, List<Player> players) {
        this.gameMap = gameMap;
        this.players = players;

        gameMap.setMapGameState(MapGameState.IN_GAME);
        gameMap.setPlayers(players);
        SignController.updateSignForMap(gameMap, players.size());

        // Определяем границы карты, чтобы игрок не выходил за ее пределы

        borderLocation1 = gameMap.getMapLocation().clone().add(-20, 0, -20);
        borderLocation2 = gameMap.getMapLocation().clone().add((gameMap.getxSize()+2)*20, 200, (gameMap.getzSize()+2)*20);
    }

    // Заполняет playerColors
    private void shuffleColors(){
        List<GameWoolColors> shuffled = new ArrayList<>(Arrays.asList(GameWoolColors.values()));
        Collections.shuffle(shuffled);
        for (int i = 0; i < players.size(); i++) {
            playerColors.put(players.get(i), shuffled.get(i));
        }
    }

    public void startMapPrepareTask() {
        shuffleColors();
        System.out.println(playerColors);
        gameMap.segmentInitialize();

        new BukkitRunnable() {
            @Override
            public void run() {
                // Показываем игрокам статус
                players.forEach(player -> player.sendTitle(
                        messageManager.getString("game.prepareMapTitle"),
                        messageManager.getString("game.prepareMapSubtitle"),
                        0, 40*20, 0));

                // Проверяем каждую секунду, завершены ли задачи
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(isCancelled()) return;
                        if (!SegmentBuilder.haveTasks()) {
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
        players.forEach(player -> {
            player.
                sendTitle(messageManager.getString("game.startTitle"),
                        messageManager.getString("game.startSubtitle"),
                        0,5*20,0);
                    PlayerInformationController.getOrSaveInformation(player);
                    setGameInformation(player);
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
        BlockType wool = switch (playerColors.get(player)) {
            case PINK_WOOL -> Objects.requireNonNull(BlockTypes.PINK_WOOL);
            case RED_WOOL -> Objects.requireNonNull(BlockTypes.RED_WOOL);
            case BLUE_WOOL -> Objects.requireNonNull(BlockTypes.BLUE_WOOL);
            case CYAN_WOOL -> Objects.requireNonNull(BlockTypes.CYAN_WOOL);
            case BROWN_WOOL -> Objects.requireNonNull(BlockTypes.BROWN_WOOL);
            case GREEN_WOOL -> Objects.requireNonNull(BlockTypes.GREEN_WOOL);
            case LIME_WOOL -> Objects.requireNonNull(BlockTypes.LIME_WOOL);
            case ORANGE_WOOL -> Objects.requireNonNull(BlockTypes.ORANGE_WOOL);
            case PURPLE_WOOL -> Objects.requireNonNull(BlockTypes.PURPLE_WOOL);
            case YELLOW_WOOL -> Objects.requireNonNull(BlockTypes.YELLOW_WOOL);
        };
        if(structure instanceof Base || structure instanceof MilitarySchool){
            pos2.add(20, 0, 20);
        }
        // Конвертируем точки в BlockVector3
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

        // Создаем регион
        CuboidRegion region = new CuboidRegion(weWorld, min, max);
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

    public void addSegmentToPlayerFromMap(MapSegment segment, Player player){
        if(!players.contains(player)) return;
        if(!playerOwnedSegments.containsKey(player)){
            playerOwnedSegments.put(player, new ArrayList<>());
        }
        playerOwnedSegments.get(player).add(segment);
    }

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
                switch (segment.getStructure().getClass().getSimpleName()){
                    case "Forest":
                        segment.setStructure(new Sawmill());
                        segmentBuilder.buildSegment(segment);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if(isCancelled()) return;
                                if (!SegmentBuilder.haveTasks()) {
                                    buildWoolBorders(segment, player);
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(plugin, 0L, 5L);
                        player.sendMessage(messageManager.getString("game.structureBuild"));
                        return;

                    case "Hills":
                        segment.setStructure(new Mineshaft());
                        segmentBuilder.buildSegment(segment);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if(isCancelled()) return;
                                if (!SegmentBuilder.haveTasks()) {
                                    buildWoolBorders(segment, player);
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(plugin, 0L, 5L);
                        player.sendMessage(messageManager.getString("game.structureBuild"));
                        return;
                    case "Plain":
                        if(X+1<xSize && Z+1<zSize
                                && allSegments[X+1][Z+1].getStructure().getClass().equals(Plain.class)
                                && allSegments[X+1][Z].getStructure().getClass().equals(Plain.class)
                                && allSegments[X][Z+1].getStructure().getClass().equals(Plain.class)
                                && allSegments[X+1][Z+1].getOwner() != null
                                && allSegments[X+1][Z].getOwner() != null
                                && allSegments[X][Z+1].getOwner() != null
                                && allSegments[X+1][Z+1].getOwner().equals(player)
                                && allSegments[X+1][Z].getOwner().equals(player)
                                && allSegments[X][Z+1].getOwner().equals(player)){
                            segment.setStructure(new MilitarySchool());
                            allSegments[X+1][Z+1].setStructure(new MilitarySchool());
                            allSegments[X+1][Z].setStructure(new MilitarySchool());
                            allSegments[X][Z+1].setStructure(new MilitarySchool());
                            segmentBuilder.buildSegment(segment);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if(isCancelled()) return;
                                    if (!SegmentBuilder.haveTasks()) {
                                        buildWoolBorders(segment, player);
                                        this.cancel();
                                    }
                                }
                            }.runTaskTimer(plugin, 0L, 5L);
                            player.sendMessage(messageManager.getString("game.structureBuild"));

                        } else player.sendMessage(messageManager.getString("game.structureMilitaryBuildError"));
                        return;

                        default:
                        player.sendMessage(messageManager.getString("game.structureBuildHelp"));
                }
            } else {
                player.sendMessage(messageManager.getString("game.structureBuildOnlyOnOwn"));
            }
        }
    }

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
            if(X+1<xSize && allSegments[X+1][Z].getOwner() != null && allSegments[X+1][Z].getOwner().equals(player)
            || X-1>=0 && allSegments[X-1][Z].getOwner() != null && allSegments[X-1][Z].getOwner().equals(player)
            || Z+1<zSize && allSegments[X][Z+1].getOwner() != null && allSegments[X][Z+1].getOwner().equals(player)
            || Z-1>=0 && allSegments[X][Z-1].getOwner() != null && allSegments[X][Z-1].getOwner().equals(player)){
                message = messageManager.getString("game.exploreSegment");
                addSegmentToPlayer(segment, player);
                player.sendMessage(message);
                return;
            }
            player.sendMessage(messageManager.getString("game.exploreTooFar"));
        }

    }

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
    public void tpIfBorderCross(Player player){
        Location playerPos = player.getLocation();
        Location pos1 = borderLocation1;
        Location pos2 = borderLocation2;
        if(playerPos.getX() < Math.min(pos1.getX(), pos2.getX()) || playerPos.getX() > Math.max(pos1.getX(), pos2.getX()) ||
                playerPos.getY() < Math.min(pos1.getY(), pos2.getY()) || playerPos.getY() > Math.max(pos1.getY(), pos2.getY()) ||
                playerPos.getZ() < Math.min(pos1.getZ(), pos2.getZ()) || playerPos.getZ() > Math.max(pos1.getZ(), pos2.getZ())) {
            double closestX = Math.max(Math.min(pos1.getX(), pos2.getX()), Math.min(Math.max(pos1.getX(), pos2.getX()), playerPos.getX()));
            double closestY = Math.max(Math.min(pos1.getY(), pos2.getY()), Math.min(Math.max(pos1.getY(), pos2.getY()), playerPos.getY()));
            double closestZ = Math.max(Math.min(pos1.getZ(), pos2.getZ()), Math.min(Math.max(pos1.getZ(), pos2.getZ()), playerPos.getZ()));

            // Телепортируем игрока на границу
            Location closestLoc = new Location(playerPos.getWorld(), closestX, closestY, closestZ, player.getYaw(), player.getPitch());
            player.teleport(closestLoc);
        }
    }
    public List<MapSegment> getSegments(Player player){
        return playerOwnedSegments.get(player);
    }
    public List<Player> getPlayers(){
        return players;
    }
}
