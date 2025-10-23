package org.ilmiandluk.customMinigame.game;

import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.royawesome.jlibnoise.module.modifier.Abs;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.map.Map;
import org.ilmiandluk.customMinigame.game.map.MapGameState;
import org.ilmiandluk.customMinigame.game.map.MapSegment;
import org.ilmiandluk.customMinigame.game.map.SegmentBuilder;
import org.ilmiandluk.customMinigame.game.structures.AbstractStructure;
import org.ilmiandluk.customMinigame.game.structures.builds.Base;
import org.ilmiandluk.customMinigame.game.structures.builds.MilitarySchool;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;
import org.ilmiandluk.customMinigame.util.PlayerInformationController;

import java.util.*;

public class Game {
    private final Map gameMap;
    private final List<Player> players;
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

    /*
        Ниже будем хранить все таски, чтобы затем отменять их, когда нужно.
     */


    public Game(Map gameMap, List<Player> players) {
        this.gameMap = gameMap;
        this.players = players;

        gameMap.setMapGameState(MapGameState.IN_GAME);
        gameMap.setPlayers(players);
        SignController.updateSignForMap(gameMap, players.size());

        /*
            Вызовы методов вложены друг в друга.
            startMapPrepareTask - когда готов выполняет
                shuffleColors
                дальше инициализация сегментов внутри Map
                и присваивание нужных сегментов владельцам уже здесь через

                startGame

         */
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

        // Телепортируем на базу человечка
        updateAllBorders();
        player.teleport(playerOwnedSegments.get(player).getFirst().loc().clone().add(0, 40, 0));
    }
    private void buildWoolBorders(MapSegment mapSegment, Player player){
        World bukkitWorld = mapSegment.loc().getWorld();
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
        Location pos1 = mapSegment.loc().clone().add(0,-1,0);
        Location pos2 = mapSegment.loc().clone().add(19,-1,19);
        AbstractStructure structure = mapSegment.structure();
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
    synchronized public void addSegmentToPlayer(MapSegment segment, Player player){
        if(!players.contains(player)) return;
        if(!playerOwnedSegments.containsKey(player)){
            playerOwnedSegments.put(player, new ArrayList<>());
        }
        List<MapSegment> segments = playerOwnedSegments.get(player);
        segments.add(segment);
        if(segments.size() > 1){
            Class<? extends AbstractStructure> first = segments.get(segments.size()-2).structure().getClass();
            Class<? extends AbstractStructure> second = segments.get(segments.size()-1).structure().getClass();
            if(first != second){
                buildWoolBorders(segment, player);
            }
            return;
        }
            buildWoolBorders(segment, player);
    }
    public void updateAllBorders(){
        for(java.util.Map.Entry<Player, List<MapSegment>> entry : playerOwnedSegments.entrySet()){
            for (int i = 0; i < entry.getValue().size(); i++) {
                if(i > 0 && entry.getValue().get(i - 1).structure().getClass().equals(entry.getValue().get(i).structure().getClass())){
                    continue;
                }
                buildWoolBorders(entry.getValue().get(i), entry.getKey());
            }
        }
    }
    public List<MapSegment> getSegments(Player player){
        return playerOwnedSegments.get(player);
    }
    public List<Player> getPlayers(){
        return players;
    }
}
