package org.ilmiandluk.customMinigame.game.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.Game;
import org.ilmiandluk.customMinigame.game.controller.GameController;
import org.ilmiandluk.customMinigame.game.handler.GUIHandler;
import org.ilmiandluk.customMinigame.game.player.GamePlayer;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;

import java.util.List;

public class PlayerListGUI implements ChestGUI{
    private final static ConfigurationManager messageLoader =
            CustomMinigame.getInstance().getMessagesManager();
    private final Inventory inventory;
    private final GamePlayer player;
    public PlayerListGUI(GamePlayer player){
        this.player = player;
        this.inventory = createInventory();
    }

    @Override
    public Inventory createInventory() {
        Inventory inv = Bukkit.createInventory(null, 27, messageLoader.getString("game.playerListGUI.title"));
        initializeItems(inv);
        return inv;
    }

    @Override
    public void initializeItems(Inventory inv) {
        Game game = GameController.getGameWithPlayer(player.getPlayer());
        if(game != null){
            List<GamePlayer> gamePlayers = game.getGamePlayersWithout(player);
            for(int i = 0; i < gamePlayers.size(); i++) {
                GamePlayer gamePlayer = gamePlayers.get(i);
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwningPlayer(gamePlayer.getPlayer());
                meta.setDisplayName(gamePlayer.replacePlaceholders(
                        messageLoader.getString("game.playerListGUI.headName")
                ));

                List<String> lore = gamePlayer.replacePlaceholders(
                        messageLoader.getStringList("game.playerListGUI.headLore"),
                        player
                );
                meta.setLore(lore);
                head.setItemMeta(meta);

                inv.setItem(i+9, head);
            }
        }
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
    public void handleClick(Player player, int slot) {
        List<GamePlayer> gamePlayers = GameController.getGameWithPlayer(player).getGamePlayersWithout(this.player);
        if(slot < 9 || slot > 17 || slot >= gamePlayers.size()+9) return;

        GamePlayer targetPlayer = GameController.getGameWithPlayer(player).getGamePlayersWithout(this.player).get(slot-9);
        new PlayerInteractionGUI(this.player, targetPlayer).openInventory(player);
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
    }



    @Override
    public void updateItem() {

    }
}