package org.ilmiandluk.customMinigame.game.handler;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.util.RayTraceResult;
import org.ilmiandluk.customMinigame.game.Game;
import org.ilmiandluk.customMinigame.game.controller.GameController;

import java.util.Set;

public class GameHandler  {

    private static final Set<Integer> PROTECTED_SLOTS = Set.of(0, 4);

    public void handleOnAnyDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    public void handleOnPlayerHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    public void handleOnPlayerMovement(PlayerMoveEvent event) {
        Game game = GameController.getGameWithPlayer(event.getPlayer());
        if (game != null) {
            game.tpIfBorderCross(event.getPlayer());
        }
    }

    public void handleOnExploreItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Game game = GameController.getGameWithPlayer(player);
        if(game != null) {
            if(event.hasItem() && event.getItem().getType() == Material.STICK) {
                RayTraceResult result = player.rayTraceBlocks(250.0);
                if(result != null && result.getHitBlock() != null) {
                    game.exploreTerritory(player, result.getHitBlock().getLocation());
                }
            }
        }
    }

    public void handleOnBuildItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Game game = GameController.getGameWithPlayer(player);
        if(game != null) {
            if(event.hasItem() && event.getItem().getType() == Material.BLAZE_ROD) {
                RayTraceResult result = player.rayTraceBlocks(250.0);
                if(result != null && result.getHitBlock() != null) {
                    game.buildStructure(player, result.getHitBlock().getLocation());
                }
            }
        }
    }

    public void handleOnInventoryClick(InventoryClickEvent event) {
        if (isProtectedSlot(event.getSlot()) ||
                isProtectedSlot(event.getRawSlot()) ||
                (event.getHotbarButton() != -1 && isProtectedSlot(event.getHotbarButton()))) {
            event.setCancelled(true);
        }
    }

    public void handleOnInventoryDrag(InventoryDragEvent event) {
        for (int slot : event.getRawSlots()) {
            if (isProtectedSlot(slot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    public void handleOnHotkeySwap(InventoryClickEvent event) {
        if (event.getClick() == ClickType.NUMBER_KEY) {
            int hotbarSlot = event.getHotbarButton();
            if (isProtectedSlot(hotbarSlot)) {
                event.setCancelled(true);
            }
        }
    }

    public void handlePlayerDropEvent(PlayerDropItemEvent event) {
        int slot = event.getPlayer().getInventory().getHeldItemSlot();
        if (isProtectedSlot(slot)) {
            event.setCancelled(true);
        }
    }

    public void handlePlayerSwapHandsEvent(PlayerSwapHandItemsEvent event) {
        int slot = event.getPlayer().getInventory().getHeldItemSlot();
        if (isProtectedSlot(slot)) {
            event.setCancelled(true);
        }
    }

    public void handleOnCreativeInventoryAction(InventoryCreativeEvent event) {
        if (isProtectedSlot(event.getSlot())) {
                event.setCancelled(true);
        }
    }

    private boolean isProtectedSlot(int slot) {
        return PROTECTED_SLOTS.contains(slot);
    }
}
