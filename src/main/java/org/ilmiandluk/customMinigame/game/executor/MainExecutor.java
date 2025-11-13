package org.ilmiandluk.customMinigame.game.executor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.Game;
import org.ilmiandluk.customMinigame.game.controller.GameController;
import org.ilmiandluk.customMinigame.game.player.GamePlayer;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;
import org.jetbrains.annotations.NotNull;

public class MainExecutor implements CommandExecutor {
    private final ConfigurationManager messageManager = CustomMinigame.getInstance().getMessagesManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }
        Player player =  (Player) sender;
        if(args.length < 2){
            return sendHelpMessage(sender);
        }
        else if(args[0].equalsIgnoreCase("accept")) {
            if(GameController.getGameWithPlayer(player) == null){
                player.sendMessage(messageManager.getString("game.notInGame"));
                return true;
            }
            Game game = GameController.getGameWithPlayer(player);
            for(Player pl: game.getPlayers()){
                if(pl.getName().equals(args[1])){
                    GamePlayer ourGamePlayer = game.getGamePlayer(player);
                    GamePlayer targetGamePlayer =  game.getGamePlayer(pl);
                    if(ourGamePlayer.haveFriendRequestFrom(targetGamePlayer)){
                        ourGamePlayer.removeFriendInvite(targetGamePlayer);
                        targetGamePlayer.removeFriendInvite(ourGamePlayer);

                        targetGamePlayer.addFriend(ourGamePlayer);
                        ourGamePlayer.addFriend(targetGamePlayer);
                        ourGamePlayer.getFriendList().
                                forEach(p ->
                                        p.getPlayer().
                                                sendMessage(messageManager.getString("game.playerJoinAlly", ourGamePlayer)));
                        player.sendMessage(messageManager.getString("game.allyAccept"));
                        return true;
                    }
                    player.sendMessage(messageManager.getString("game.notRequestFromPlayer"));
                    return true;
                }
            }
            player.sendMessage(messageManager.getString("game.thisPlayerNotInGame"));
        }
        return false;
    }
    private boolean sendHelpMessage(@NotNull CommandSender sender){
        sender.sendMessage(messageManager.getString("game.helpMessage"));
        return true;
    }
}
