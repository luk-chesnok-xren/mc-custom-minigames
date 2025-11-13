package org.ilmiandluk.customMinigame.game.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.ilmiandluk.customMinigame.game.controller.GameController;
import org.ilmiandluk.customMinigame.game.handler.GameHandler;

public class GameListener implements Listener {
    private final GameHandler gameHandler;
    public GameListener(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    @EventHandler
    public void onPlayerDamageAnyone(EntityDamageEvent event) {
        if(event.getDamageSource().getCausingEntity() instanceof Player player) {
            if (GameController.getGameWithPlayer(player) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onAnyDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        if (GameController.getGameWithPlayer(((Player) event.getEntity()).getPlayer()) != null) {
            gameHandler.handleOnAnyDamage(event);
        }
    }
    @EventHandler
    public void onPlayerHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            if (GameController.getGameWithPlayer(((Player) event.getEntity()).getPlayer()) != null) {
                gameHandler.handleOnPlayerHunger(event);
            }
        }
    }
    @EventHandler
    public void onPlayerMovement(PlayerMoveEvent event) {
        gameHandler.handleOnPlayerMovement(event);
    }
    @EventHandler
    public void onControlItem(PlayerInteractEvent event) {
        gameHandler.handleOnControlItem(event);
    }
    @EventHandler
    public void onBuildItem(PlayerInteractEvent event) {
        gameHandler.handleOnBuildItem(event);
    }
    @EventHandler
    public void onSpectatorItem(PlayerInteractEvent event) {
        gameHandler.handleOnSpectatorItem(event);
    }
    @EventHandler
    public void onStatusItem(PlayerInteractEvent event) {
        gameHandler.handleOnStatusItem(event);
    }
    @EventHandler
    public void onLeaveItem(PlayerInteractEvent event) {
        gameHandler.handleOnLeaveItem(event);
    }
    @EventHandler
    public void onShopItem(PlayerInteractEvent event){
        gameHandler.handleOnShopItem(event);
    }
    @EventHandler
    public void onAbilityItem(PlayerInteractEvent event){
        gameHandler.handleAbilityItem(event);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (GameController.getGameWithPlayer(event.getPlayer()) != null) {
            gameHandler.handleLeave(event);
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (GameController.getGameWithPlayer(event.getPlayer()) != null) {
            gameHandler.handlePlayerChat(event);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (GameController.getGameWithPlayer(player) != null) {
            gameHandler.handleOnInventoryClick(event);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (GameController.getGameWithPlayer(((Player) event.getWhoClicked()).getPlayer()) != null) {
            gameHandler.handleOnInventoryDrag(event);
        }
    }

    @EventHandler
    public void onHotkeySwap(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (GameController.getGameWithPlayer(player) != null) {
            gameHandler.handleOnHotkeySwap(event);
        }
    }

    @EventHandler
    public void playerDropEvent(PlayerDropItemEvent event) {
        if (GameController.getGameWithPlayer(event.getPlayer()) != null) {
            gameHandler.handlePlayerDropEvent(event);
        }
    }

    @EventHandler
    public void playerSwapHandsEvent(PlayerSwapHandItemsEvent event) {
        if (GameController.getGameWithPlayer(event.getPlayer()) != null) {
            gameHandler.handlePlayerSwapHandsEvent(event);
        }
    }

    // (на всякий случай)
    @EventHandler
    public void onCreativeInventoryAction(InventoryCreativeEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (GameController.getGameWithPlayer(((Player) event.getWhoClicked()).getPlayer()) != null) {
            gameHandler.handleOnCreativeInventoryAction(event);
        }
    }
}
