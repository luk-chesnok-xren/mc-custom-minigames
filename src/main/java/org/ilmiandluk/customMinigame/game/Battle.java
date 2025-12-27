package org.ilmiandluk.customMinigame.game;

import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.map.MapSegment;
import org.ilmiandluk.customMinigame.game.player.GamePlayer;
import org.ilmiandluk.customMinigame.game.structures.builds.Base;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;

import javax.annotation.Nullable;

public class Battle {
    private static final ConfigurationManager messageLoader = CustomMinigame.getInstance().getMessagesManager();
    private static final ConfigurationManager configLoader = CustomMinigame.getInstance().getConfigManager();

    private final GamePlayer owner;
    private final GamePlayer enemy;
    private final GamePlayer friend;
    private final MapSegment mapSegment;

    public Battle(MapSegment segment, @Nullable GamePlayer owner, @Nullable GamePlayer friend, GamePlayer enemy){
        this.mapSegment = segment;
        this.owner = owner;
        this.friend = friend;
        this.enemy = enemy;
    }

    public void ownerOrFriendWin(){
        if(owner != null)
            owner.getPlayer().sendMessage(messageLoader.getString("game.winOnSegment"));
        if(friend != null)
            friend.getPlayer().sendMessage(messageLoader.getString("game.winOnSegment"));

        enemy.getPlayer().sendMessage(messageLoader.getString("game.loseOnSegment"));
    }
    public void enemyWin(){
        if(owner != null)
            owner.getPlayer().sendMessage(messageLoader.getString("game.loseOnSegment"));
        if(friend != null)
            friend.getPlayer().sendMessage(messageLoader.getString("game.loseOnSegment"));

        enemy.getPlayer().sendMessage(messageLoader.getString("game.winOnSegment"));
        if(mapSegment.getStructure() instanceof Base) {
            enemy.getPlayer().sendMessage(messageLoader.getString("game.catchSegment", configLoader.getInt("game.timeToCatchBase")));
        } else {
            enemy.getPlayer().sendMessage(messageLoader.getString("game.catchSegment", configLoader.getInt("game.timeToCatchSegment")));
        }
        mapSegment.catchSegment();
    }
}
