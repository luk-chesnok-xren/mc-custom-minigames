package org.ilmiandluk.customMinigame.game.player.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface GameItem {
    ItemStack getItem();
    void giveItemToPlayer(Player player);
}
