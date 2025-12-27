package org.ilmiandluk.customMinigame.game.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.handler.GUIHandler;
import org.ilmiandluk.customMinigame.game.player.GamePlayer;
import org.ilmiandluk.customMinigame.game.player.inventory.ability.*;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI implements ChestGUI{
    private final static ConfigurationManager messageLoader =
            CustomMinigame.getInstance().getMessagesManager();
    private static final ConfigurationManager configLoader =
            CustomMinigame.getInstance().getConfigManager();

    private static Integer healAbilityItemCost = configLoader.getInt("game.healAbilityItemCost", 200);
    private static Integer speedAbilityItemCost = configLoader.getInt("game.speedAbilityItemCost", 500);
    private static Integer strengthAbilityItemCost = configLoader.getInt("game.strengthAbilityItemCost", 1000);
    private static Integer teleportAbilityItemCost = configLoader.getInt("game.teleportAbilityItemCost", 5000);
    private static Integer rocketAbilityItemCost = configLoader.getInt("game.rocketAbilityItemCost", 5000);
    private static Integer nuclearAbilityItemCost = configLoader.getInt("game.nuclearAbilityItemCost", 30000);

    private final Inventory inventory;
    private final GamePlayer player;
    public ShopGUI(GamePlayer player){
        this.player = player;
        this.inventory = createInventory();
    }

    @Override
    public Inventory createInventory() {
        Inventory inv = Bukkit.createInventory(null, 27, messageLoader.getString("game.shopGUI.title"));
        initializeItems(inv);
        return inv;
    }

    @Override
    public void initializeItems(Inventory inv) {
        ItemStack healAbilityItem = new HealAbilityItem().getItem();
        ItemStack strengthAbilityItem = new StrengthAbilityItem().getItem();
        ItemStack speedAbilityItem = new SpeedAbilityItem().getItem();
        ItemStack teleportAbilityItem = new TeleportAbilityItem().getItem();
        ItemStack rocketAbilityItem = new RocketAbilityItem().getItem();
        ItemStack nuclearAbilityItem = new NuclearAbilityItem().getItem();

        addCostToItem(healAbilityItem, healAbilityItemCost);
        addCostToItem(speedAbilityItem, speedAbilityItemCost);
        addCostToItem(strengthAbilityItem, strengthAbilityItemCost);
        addCostToItem(teleportAbilityItem, teleportAbilityItemCost);
        addCostToItem(rocketAbilityItem, rocketAbilityItemCost);
        addCostToItem(nuclearAbilityItem, nuclearAbilityItemCost);

        inv.setItem(9, healAbilityItem);
        inv.setItem(10, strengthAbilityItem);
        inv.setItem(11, speedAbilityItem);
        inv.setItem(12, teleportAbilityItem);
        inv.setItem(13, rocketAbilityItem);
        inv.setItem(14, nuclearAbilityItem);
    }
    public void addCostToItem(ItemStack itemStack, int cost){
        String formatLore = "§7Стоимость §e%d§7 монет";
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>(itemStack.getItemMeta().getLore());
        lore.add(" ");
        lore.add(formatLore.formatted(cost));
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
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
        if(slot < 9 || slot > 14) return;

        switch (slot) {
            case 9:
                if(player.getPlayerResources().getMoneyCount() >= healAbilityItemCost){
                    new HealAbilityItem().giveItemToPlayer(pl);
                    player.getPlayerResources().addMoney(-healAbilityItemCost);
                    pl.sendMessage(messageLoader.getString("game.shopGUI.successful"));
                    return;
                }
            case 10:
                if(player.getPlayerResources().getMoneyCount() >= strengthAbilityItemCost){
                    new StrengthAbilityItem().giveItemToPlayer(pl);
                    player.getPlayerResources().addMoney(-strengthAbilityItemCost);
                    pl.sendMessage(messageLoader.getString("game.shopGUI.successful"));
                    return;
                }
            case 11:
                if(player.getPlayerResources().getMoneyCount() >= speedAbilityItemCost){
                    new SpeedAbilityItem().giveItemToPlayer(pl);
                    player.getPlayerResources().addMoney(-speedAbilityItemCost);
                    pl.sendMessage(messageLoader.getString("game.shopGUI.successful"));
                    return;
                }
            case 12:
                if(player.getPlayerResources().getMoneyCount() >= teleportAbilityItemCost){
                    new TeleportAbilityItem().giveItemToPlayer(pl);
                    player.getPlayerResources().addMoney(-teleportAbilityItemCost);
                    pl.sendMessage(messageLoader.getString("game.shopGUI.successful"));
                    return;
                }
            case 13:
                if(player.getPlayerResources().getMoneyCount() >= rocketAbilityItemCost){
                    new RocketAbilityItem().giveItemToPlayer(pl);
                    player.getPlayerResources().addMoney(-rocketAbilityItemCost);
                    pl.sendMessage(messageLoader.getString("game.shopGUI.successful"));
                    return;
                }
            case 14:
                if(player.getPlayerResources().getMoneyCount() >= nuclearAbilityItemCost){
                    new NuclearAbilityItem().giveItemToPlayer(pl);
                    player.getPlayerResources().addMoney(-nuclearAbilityItemCost);
                    pl.sendMessage(messageLoader.getString("game.shopGUI.successful"));
                    return;
                }
            default:
                pl.sendMessage(messageLoader.getString("game.shopGUI.error"));
        }
    }

    @Override
    public void openInventory(Player p) {
        updateItem();
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
        initializeItems(inventory);
    }
}
