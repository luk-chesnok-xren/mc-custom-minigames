package org.ilmiandluk.customMinigame.game.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

// Потом будем использовать для создания ОТЧЕТОВ, тех самых, легендарных
// ChestGUI - потому-что это GUI сундука, в котором будут лежать кастомные предметы
public interface ChestGUI {
    Inventory createInventory();
    Inventory getInventory();

    void onInventoryClose(InventoryCloseEvent event);
    void onInventoryDrag(InventoryDragEvent event);
    void onInventoryClick(InventoryClickEvent event);
    void handleClick(Player player, int slot);

    void openInventory(Player p);
    void updateInventory();
    void closeInventory(Player p);

    void initializeItems(Inventory inv);

    void updateItem();
}
