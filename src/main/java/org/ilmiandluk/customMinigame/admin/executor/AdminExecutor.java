package org.ilmiandluk.customMinigame.admin.executor;

import net.minecraft.server.level.WorldServer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.Sign;
import org.ilmiandluk.customMinigame.game.controller.ChunkController;
import org.ilmiandluk.customMinigame.game.entity.Soldier;
import org.ilmiandluk.customMinigame.game.enums.GameWoolColors;
import org.ilmiandluk.customMinigame.game.player.GamePlayer;
import org.ilmiandluk.customMinigame.game.repository.MapRepository;
import org.ilmiandluk.customMinigame.game.repository.SignRepository;
import org.ilmiandluk.customMinigame.game.map.Map;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class AdminExecutor implements CommandExecutor {

    private final CustomMinigame plugin;
    private final ConfigurationManager messageManager;

    public AdminExecutor(CustomMinigame plugin) {
        this.plugin = plugin;
        messageManager = CustomMinigame.getInstance().getMessagesManager();
    }
    /*
        Здесь будет обработчики для команды /cmga
        Хочу сделать /cmga createmap <name> <maxPlayers> <xSize> <zSize>
        которая будет создавать игровую карту на месте нахождения игрока sender.getLocation()

        Ну и дальше что-нибудь еще придумаем для админов.
        Скоро тут будут лежать обработчики для тестов (временно)
    */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }
        if(args.length < 1){
            return sendHelpMessage(sender);
        }
        return switch (args[0].toLowerCase()) {
            case "createmap" -> handleCreateMap(((Player) sender).getPlayer(), args);
            case "createsign" -> handleCreateSign(((Player) sender).getPlayer(), args);
            case "generate" -> handleGenerateMap(((Player) sender).getPlayer(), args);
            case "test" -> handleTestSoldier(((Player) sender).getPlayer(), args);
            case "test1" -> handleTest1Soldier(((Player) sender).getPlayer(), args);
            default -> sendHelpMessage(sender);
        };
    }

    private boolean handleTestSoldier(Player player, String... args){
        GamePlayer gamePlayer = new GamePlayer(player, GameWoolColors.RED_WOOL);
        ChunkController chunkController = new ChunkController(((CraftWorld) player.getWorld()).getHandle());
        Soldier soldier1 = new Soldier(player.getLocation(), player.getLocation().add(100, 0, 0), gamePlayer, chunkController);
        Soldier soldier2 = new Soldier(player.getLocation(), player.getLocation().add(100, 0, 0), gamePlayer, chunkController);
        soldier1.spawnMob();
        soldier2.spawnMob();
        return true;
    }
    private boolean handleTest1Soldier(Player player, String... args){
        GamePlayer gamePlayer = new GamePlayer(player, GameWoolColors.RED_WOOL);
        ChunkController chunkController = new ChunkController(((CraftWorld) player.getWorld()).getHandle());
        Soldier soldier1 = new Soldier(player.getLocation(), player.getLocation().add(100, 0, 0), gamePlayer, chunkController);
        Soldier soldier2 = new Soldier(player.getLocation(), player.getLocation().add(100, 0, 0), null, chunkController);
        soldier1.spawnMob();
        soldier2.spawnMob();
        return true;
    }
    private boolean handleCreateSign(Player player, String... args){
        if (args.length < 2) {
            player.sendMessage(messageManager.getString("admin.createSignUsage"));
            return true;
        }
        if(MapRepository.getMap(args[1]) == null) {
            player.sendMessage(messageManager.getString("admin.mapNotFound"));
            return false;
        }
        Map map = MapRepository.getMap(args[1]);
        Set<Material> transparent = new HashSet<>();
        transparent.add(Material.AIR);

        // Use the getTargetBlock method with the filter and range
        Block targetBlock = player.getTargetBlock(transparent, 5);
        if (targetBlock.getState() instanceof org.bukkit.block.Sign) {
            org.bukkit.block.Sign sign = (org.bukkit.block.Sign) targetBlock.getState();
            sign.setLine(0, "§c[CustomMinigame]");
            sign.setLine(1, map.getMapName());
            sign.setLine(2, "Checking...");
            sign.setLine(3, "0/"+map.getMaxPlayers());
            sign.update();
            SignRepository.addSignToFile(new Sign(map.getMapName(), map.getMaxPlayers(), targetBlock.getLocation()));
            SignRepository.updateAllSigns();
        }
        else{
            player.sendMessage(messageManager.getString("admin.viewNotOnSign"));
            return false;
        }
        return true;
    }

    private boolean sendHelpMessage(CommandSender sender){
        for(String message: messageManager.getStringList("admin.help")){
            sender.sendMessage(message);
        }
        return false;
    }

    private boolean handleGenerateMap(@Nullable Player player, @NotNull String @NotNull [] args) {
        if (args.length < 2) {
            player.sendMessage(messageManager.getString("admin.generateMapUsage"));
            return true;
        }
        String mapName = args[1];
        Map thisMap = MapRepository.getMap(mapName);
        if (thisMap == null) {
            player.sendMessage(messageManager.getString("admin.mapNotFound"));
            return true;
        }
        thisMap.segmentInitialize();
        player.sendMessage(messageManager.getString("admin.mapWasGenerated"));
        return true;
    }

    private boolean handleCreateMap(@Nullable Player player, @NotNull String @NotNull [] args) {
        if (args.length < 5) {
            player.sendMessage(messageManager.getString("admin.createMapUsage"));
            return true;
        }
        String mapName = args[1];
        int maxPlayers;
        int xSize;
        int zSize;
        try{
            maxPlayers = Integer.parseInt(args[2]);
            xSize = Integer.parseInt(args[3]);
            zSize = Integer.parseInt(args[4]);
            MapRepository.createMap(mapName, player.getLocation(), maxPlayers, xSize, zSize);
            player.sendMessage(messageManager.getString("admin.mapWasCreated"));
        }catch (Exception e){
            player.sendMessage(messageManager.getString("admin.createMapUsage"));
            return true;
        }
        return false;
    }
}
