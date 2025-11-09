package org.ilmiandluk.customMinigame.game.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class PlayerListGUI implements ChestGUI{
    @Override
    public Inventory createInventory() {
        return null;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {

    }

    @Override
    public void onInventoryDrag(InventoryDragEvent event) {

    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {

    }

    @Override
    public void handleClick(Player player, int slot) {

    }

    @Override
    public void openInventory(Player p) {

    }

    @Override
    public void updateInventory() {

    }

    @Override
    public void closeInventory(Player p) {

    }

    @Override
    public void initializeItems(Inventory inv) {

    }

    @Override
    public void updateItem() {

    }
}