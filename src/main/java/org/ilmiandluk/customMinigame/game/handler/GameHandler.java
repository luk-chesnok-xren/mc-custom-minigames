package org.ilmiandluk.customMinigame.game.handler;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
import org.ilmiandluk.customMinigame.game.GameController;

import java.util.Set;

public class GameHandler implements Listener {

    private static final Set<Integer> PROTECTED_SLOTS = Set.of(0, 4);


    @EventHandler
    public void onAnyDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        if (GameController.getGameWithPlayer(((Player) event.getEntity()).getPlayer()) != null) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            if (GameController.getGameWithPlayer(((Player) event.getEntity()).getPlayer()) != null) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onPlayerMovement(PlayerMoveEvent event) {
        Game game = GameController.getGameWithPlayer(event.getPlayer());
        if (game != null) {
            game.tpIfBorderCross(event.getPlayer());
        }
    }
    @EventHandler
    public void onExploreItem(PlayerInteractEvent event) {
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
    @EventHandler
    public void onBuildItem(PlayerInteractEvent event) {
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (GameController.getGameWithPlayer(player) != null) {
            // Проверяем все возможные слоты
            if (isProtectedSlot(event.getSlot()) ||
                    isProtectedSlot(event.getRawSlot()) ||
                    (event.getHotbarButton() != -1 && isProtectedSlot(event.getHotbarButton()))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (GameController.getGameWithPlayer(((Player) event.getWhoClicked()).getPlayer()) != null) {
            // Проверяем все затронутые слоты
            for (int slot : event.getRawSlots()) {
                if (isProtectedSlot(slot)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onHotkeySwap(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (GameController.getGameWithPlayer(player) != null) {
            // Обрабатываем горячие клавиши (нажатие цифр)
            if (event.getClick() == ClickType.NUMBER_KEY) {
                int hotbarSlot = event.getHotbarButton();
                if (isProtectedSlot(hotbarSlot)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void playerDropEvent(PlayerDropItemEvent event) {
        if (GameController.getGameWithPlayer(event.getPlayer()) != null) {
            int slot = event.getPlayer().getInventory().getHeldItemSlot();
            if (isProtectedSlot(slot)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void playerSwapHandsEvent(PlayerSwapHandItemsEvent event) {
        if (GameController.getGameWithPlayer(event.getPlayer()) != null) {
            int slot = event.getPlayer().getInventory().getHeldItemSlot();
            if (isProtectedSlot(slot)) {
                event.setCancelled(true);
            }
        }
    }

    // (на всякий случай)
    @EventHandler
    public void onCreativeInventoryAction(InventoryCreativeEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (GameController.getGameWithPlayer(((Player) event.getWhoClicked()).getPlayer()) != null) {
            if (isProtectedSlot(event.getSlot())) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isProtectedSlot(int slot) {
        return PROTECTED_SLOTS.contains(slot);
    }
}
