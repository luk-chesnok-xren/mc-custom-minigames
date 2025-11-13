package org.ilmiandluk.customMinigame.game.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.ilmiandluk.customMinigame.game.handler.GUIHandler;

public class GUIListener implements Listener {
    private final GUIHandler guiHandler;

    public GUIListener(GUIHandler guiHandler) {
        this.guiHandler = guiHandler;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        guiHandler.handleInventoryClick(event);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        guiHandler.handleInventoryDrag(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        guiHandler.handleInventoryClose(event);
    }

}