package org.ilmiandluk.customMinigame.game.handler;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.ilmiandluk.customMinigame.game.gui.ChestGUI;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIHandler {
    private static final Map<UUID, ChestGUI> activeGUIs = new HashMap<>();

    public static void openGUI(Player player, ChestGUI gui) {
        activeGUIs.put(player.getUniqueId(), gui);
    }

    public static void closeGUI(Player player) {
        ChestGUI gui = activeGUIs.get(player.getUniqueId());
        if (gui != null) {
            activeGUIs.remove(player.getUniqueId());
        }
    }

    public static ChestGUI get(UUID player){
        if(activeGUIs.containsKey(player)) return activeGUIs.get(player);
        return null;
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ChestGUI gui = activeGUIs.get(player.getUniqueId());
        if (gui == null || !event.getInventory().equals(gui.getInventory())) return;

        gui.onInventoryClick(event);
        gui.handleClick(player, event.getSlot());
        event.setCancelled(true);
    }

    public void handleInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        ChestGUI gui = activeGUIs.get(player.getUniqueId());
        if (activeGUIs.containsKey(player.getUniqueId())) {
            gui.onInventoryDrag(event);
            event.setCancelled(true);
        }
    }

    public void handleInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        ChestGUI gui = activeGUIs.get(player.getUniqueId());
        if (activeGUIs.containsKey(player.getUniqueId())) {
            gui.onInventoryClose(event);
        }
    }
}
