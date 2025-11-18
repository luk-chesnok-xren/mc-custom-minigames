package org.ilmiandluk.customMinigame.game.handler;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.util.RayTraceResult;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.Game;
import org.ilmiandluk.customMinigame.game.controller.GameController;
import org.ilmiandluk.customMinigame.game.gui.ChestGUI;
import org.ilmiandluk.customMinigame.game.gui.PlayerListGUI;
import org.ilmiandluk.customMinigame.game.gui.ShopGUI;
import org.ilmiandluk.customMinigame.game.gui.SpectatorGUI;
import org.ilmiandluk.customMinigame.game.map.MapSegment;
import org.ilmiandluk.customMinigame.game.player.GamePlayer;
import org.ilmiandluk.customMinigame.game.player.HandledSegment;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;

import java.util.Set;

public class GameHandler  {
    private static final ConfigurationManager messageLoader = CustomMinigame.getInstance().getMessagesManager();
    private static final Set<Integer> PROTECTED_SLOTS = Set.of(0, 3, 4, 7,8);

    public void handleOnShopItem(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Game game = GameController.getGameWithPlayer(player);
        if(game != null) {
            if(event.hasItem() && event.getItem().getType() == Material.ENDER_CHEST) {
                ChestGUI chestGUI = new ShopGUI(game.getGamePlayer(player));
                chestGUI.openInventory(player);
            }
        }
    }
    public void handleOnAnyDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }
    public void handlePlayerChat(PlayerChatEvent event) {
        Game game = GameController.getGameWithPlayer(event.getPlayer());
        if(game.getHandledSegment(event.getPlayer()) != null) {
            HandledSegment handledSegment = game.getHandledSegment(event.getPlayer());
            if (handledSegment.count() == handledSegment.segment().
                    getFreePlayerSoldiers(game.getGamePlayer(event.getPlayer())).size()) {
                try {
                    int count = Integer.parseInt(event.getMessage().strip());
                    event.setCancelled(true);
                    if(game.setHandledSegmentUnitsCount(event.getPlayer(), count)){
                        event.getPlayer().sendMessage(messageLoader.getString("game.controlUnitsSetCount", count));
                        return;
                    }
                    event.getPlayer().sendMessage(messageLoader.getString("game.controlUnitsSetError"));
                } catch (NumberFormatException e) {
                }
            }
        }

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

    public void handleOnControlItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Game game = GameController.getGameWithPlayer(player);
        if(game != null) {
            if(event.hasItem() && event.getItem().getType() == Material.STICK) {
                Action action = event.getAction();
                RayTraceResult result = player.rayTraceBlocks(250.0);
                if(result != null && result.getHitBlock() != null) {
                    MapSegment segment = game.getClickedSegment(result.getHitBlock().getLocation());
                    if(segment != null) {
                        game.controlUnits(action, segment, player);
                    }
                }
            }
        }
    }
    public void handleAbilityItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Game game = GameController.getGameWithPlayer(player);
        if(game != null) {
            GamePlayer gamePlayer = game.getGamePlayer(player);
            if(event.hasItem() && event.getItem().getType() == Material.WOODEN_SWORD) {
                if(game.strengthAbility(gamePlayer)) {
                    event.getItem().setAmount(event.getItem().getAmount() - 1);
                    player.sendMessage(messageLoader.getString("game.abilitySuccessful"));
                }else
                    player.sendMessage(messageLoader.getString("game.abilityError"));
                event.setCancelled(true);
            }
            if(event.hasItem() && event.getItem().getType() == Material.FEATHER) {
                if(game.speedAbility(gamePlayer)) {
                    event.getItem().setAmount(event.getItem().getAmount() - 1);
                    player.sendMessage(messageLoader.getString("game.abilitySuccessful"));
                }else
                    player.sendMessage(messageLoader.getString("game.abilityError"));
                event.setCancelled(true);
            }
            if(event.hasItem() && event.getItem().getType() == Material.ENCHANTED_GOLDEN_APPLE) {
                RayTraceResult result = player.rayTraceBlocks(250.0);
                if(result != null && result.getHitBlock() != null) {
                    if(game.healAbility(gamePlayer, game.getClickedSegment(result.getHitBlock().getLocation()))) {
                        event.getItem().setAmount(event.getItem().getAmount() - 1);
                        player.sendMessage(messageLoader.getString("game.abilitySuccessful"));
                    }else
                        player.sendMessage(messageLoader.getString("game.abilityError"));
                    event.setCancelled(true);
                }
            }
            if(event.hasItem() && event.getItem().getType() == Material.RECOVERY_COMPASS) {
                RayTraceResult result = player.rayTraceBlocks(250.0);
                if(result != null && result.getHitBlock() != null) {
                    if(game.nuclearAbility(gamePlayer, game.getClickedSegment(result.getHitBlock().getLocation()))) {
                        event.getItem().setAmount(event.getItem().getAmount() - 1);
                        player.sendMessage(messageLoader.getString("game.abilitySuccessful"));
                    }else
                        player.sendMessage(messageLoader.getString("game.abilityError"));
                    event.setCancelled(true);
                }
            }
            if(event.hasItem() && event.getItem().getType() == Material.FIREWORK_ROCKET) {
                RayTraceResult result = player.rayTraceBlocks(250.0);
                if(result != null && result.getHitBlock() != null) {
                    if(game.rocketAbility(gamePlayer, game.getClickedSegment(result.getHitBlock().getLocation()))) {
                        event.getItem().setAmount(event.getItem().getAmount() - 1);
                        player.sendMessage(messageLoader.getString("game.abilitySuccessful"));
                    }else
                        player.sendMessage(messageLoader.getString("game.abilityError"));
                    event.setCancelled(true);
                }
            }
            if(event.hasItem() && event.getItem().getType() == Material.ENDER_PEARL) {
                if(game.teleportAbility(gamePlayer)) {
                    event.getItem().setAmount(event.getItem().getAmount() - 1);
                    player.sendMessage(messageLoader.getString("game.abilitySuccessful"));
                }else
                    player.sendMessage(messageLoader.getString("game.abilityError"));
                event.setCancelled(true);
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
    public void handleOnStatusItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Game game = GameController.getGameWithPlayer(player);
        if(game != null) {
            if(event.hasItem() && event.getItem().getType() == Material.PLAYER_HEAD) {
                ChestGUI chestGUI = new PlayerListGUI(game.getGamePlayer(player));
                chestGUI.openInventory(player);
            }
        }
    }
    public void handleOnSpectatorItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Game game = GameController.getGameWithPlayer(player);
        if(game != null) {
            if(event.hasItem() && event.getItem().getType() == Material.CLOCK) {
                ChestGUI chestGUI = new SpectatorGUI(game.getGamePlayer(player));
                chestGUI.openInventory(player);
            }
        }
    }
    public void handleOnLeaveItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Game game = GameController.getGameWithPlayer(player);
        if(game != null) {
            if(event.hasItem() && event.getItem().getType() == Material.IRON_DOOR) {
                GamePlayer gamePlayer = game.getGamePlayer(player);
                if(gamePlayer != null) {
                    game.playerLeave(gamePlayer);
                }
            }
        }
    }
    public void handleLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Game game = GameController.getGameWithPlayer(player);
        if(game != null) {
            GamePlayer gamePlayer = game.getGamePlayer(player);
            if(gamePlayer != null) {
                    game.playerLoose(gamePlayer, null);
                    game.playerLeave(gamePlayer);
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
