package org.ilmiandluk.customMinigame.game.gui;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.Game;
import org.ilmiandluk.customMinigame.game.controller.GameController;
import org.ilmiandluk.customMinigame.game.handler.GUIHandler;
import org.ilmiandluk.customMinigame.game.player.GamePlayer;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;

import java.util.ArrayList;
import java.util.List;

public class PlayerInteractionGUI implements ChestGUI{
    private final static ConfigurationManager messageLoader =
            CustomMinigame.getInstance().getMessagesManager();
    private final Inventory inventory;
    private final GamePlayer player;
    private final GamePlayer target;
    public PlayerInteractionGUI(GamePlayer player, GamePlayer target){
        this.player = player;
        this.target = target;
        this.inventory = createInventory();
    }

    @Override
    public Inventory createInventory() {
        Inventory inv = Bukkit.createInventory(null, 27, target.replacePlaceholders(messageLoader.getString("game.playerInteractionGUI.title")));
        initializeItems(inv);
        return inv;
    }
    @Override
    public void initializeItems(Inventory inv) {
        List<String> lore = new ArrayList();

        ItemStack warItemStack =  new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta warItemMeta = warItemStack.getItemMeta();

        if(target.getEnemyList().contains(player)){
            warItemMeta.setItemName(messageLoader.getString("game.playerInteractionGUI.warItemIfWar"));
            lore.add(messageLoader.getString("game.playerInteractionGUI.warItemIfWarLore"));
            warItemMeta.setLore(lore);
        }
        else if (target.getFriendList().contains(player)){
            warItemMeta.setItemName(messageLoader.getString("game.playerInteractionGUI.warItemIfFriend"));
            lore.add(messageLoader.getString("game.playerInteractionGUI.warItemIfFriendLore"));
            warItemMeta.setLore(lore);
        }
        else {
            warItemMeta.setItemName(messageLoader.getString("game.playerInteractionGUI.warItemDefault"));
            lore.add(messageLoader.getString("game.playerInteractionGUI.warItemDefaultLore"));
            warItemMeta.setLore(lore);
        }
        warItemStack.setItemMeta(warItemMeta);

        lore.clear();
        ItemStack friendItemStack =  new ItemStack(Material.DIAMOND);
        ItemMeta frienItemMeta = warItemStack.getItemMeta();

        if(target.getEnemyList().contains(player)){
            frienItemMeta.setItemName(messageLoader.getString("game.playerInteractionGUI.friendItemIfWar"));
            lore.add(messageLoader.getString("game.playerInteractionGUI.friendItemIfWarLore"));
            frienItemMeta.setLore(lore);
        }
        else if (target.getFriendList().contains(player)){
            frienItemMeta.setItemName(messageLoader.getString("game.playerInteractionGUI.friendItemIfFriend"));
            lore.add(messageLoader.getString("game.playerInteractionGUI.friendItemIfFriendLore"));
            frienItemMeta.setLore(lore);
        }
        else{
            frienItemMeta.setItemName(messageLoader.getString("game.playerInteractionGUI.friendItemDefault"));
            lore.add(messageLoader.getString("game.playerInteractionGUI.friendItemDefaultLore"));
            frienItemMeta.setLore(lore);
        }
        friendItemStack.setItemMeta(frienItemMeta);
        inv.setItem(12, warItemStack);
        inv.setItem(14, friendItemStack);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        closeInventory(player.getPlayer());
    }

    @Override
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(inventory)) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
    }

    @Override
    public void handleClick(Player pl, int slot) {
        if(slot != 12 && slot != 14) return;

        if(slot == 12) {
            if(!player.getEnemyList().contains(target)
            && !player.getFriendList().contains(target)) {
                closeInventory(pl);
                startWar();
            }
        }
        else {
            if(!player.getFriendList().contains(target)
            && !player.getEnemyList().contains(target)){
                sendInvite();
                closeInventory(pl);
            } else if(player.getFriendList().contains(target)) {
                closeInventory(pl);
                leaveAlly();
            } else{
                closeInventory(pl);
            }
        }
    }
    private void startWar(){
        Game game = GameController.getGameWithPlayer(player.getPlayer());
        if(game != null){
            for(Player pl : game.getPlayers()){
                pl.sendTitle(messageLoader.getString("game.startWarTitle", player), messageLoader.getString("game.startWarSubtitle", target), 0, 40, 0);
            }
        }
        player.addEnemy(target);
        target.addEnemy(player);
    }
    private void sendInvite(){
        TextComponent base = new TextComponent(
                messageLoader.getString("game.playerSendAllyTitle", player) + messageLoader.getString("game.playerSendAllySubtitle", player));

        TextComponent accept = new TextComponent(
                messageLoader.getString("game.acceptButton"));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cmg accept " + player.getPlayer().getName()));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(messageLoader.getString("game.acceptButtonHover")).create()));

        base.addExtra(accept);

        target.getPlayer().spigot().sendMessage(base);
        target.addFriendInvite(player);
        target.getPlayer().sendTitle(messageLoader.getString("game.playerSendAllyTitle", player), messageLoader.getString("game.playerSendAllySubtitle", player), 0, 40, 0);

        player.getPlayer().sendMessage(messageLoader.getString("game.sendAlly", target));
    }
    private void leaveAlly(){
        List<GamePlayer> friendsCopy = new ArrayList<>(player.getFriendList());
        for(GamePlayer gamePlayer: friendsCopy) {
            gamePlayer.getPlayer().sendMessage(messageLoader.getString("game.playerLeaveAlly", player));
            gamePlayer.getPlayer().sendTitle(
                    messageLoader.getString("game.playerLeaveAlly", player),
                    "", 0, 40, 0);
            gamePlayer.removeFriend(player);
            player.removeFriend(gamePlayer);
        }
        player.getPlayer().sendMessage(messageLoader.getString("game.playerLeaveAlly", player));
        player.getPlayer().sendTitle(
                messageLoader.getString("game.playerLeaveAlly", player),
                "", 0, 40, 0);
    }

    @Override
    public void openInventory(Player p) {
        p.openInventory(inventory);
        GUIHandler.openGUI(p, this);
    }

    @Override
    public void updateInventory() {
        closeInventory(player.getPlayer());
        openInventory(player.getPlayer());
    }

    @Override
    public void closeInventory(Player p) {
        GUIHandler.closeGUI(p);
        new PlayerListGUI(player).openInventory(p);
    }

    @Override
    public void updateItem() {

    }
}