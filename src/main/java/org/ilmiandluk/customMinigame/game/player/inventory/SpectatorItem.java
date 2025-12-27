package org.ilmiandluk.customMinigame.game.player.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpectatorItem implements GameItem {
    private static final ItemStack spectatorItem = new ItemStack(Material.CLOCK);
    private static final ItemMeta spectatorItemMeta = spectatorItem.getItemMeta();

    static {
        spectatorItemMeta.setItemName("§aНаблюдать за игроками");
        spectatorItem.setItemMeta(spectatorItemMeta);
    }

    @Override
    public ItemStack getItem() {
        return spectatorItem;
    }

    @Override
    public void giveItemToPlayer(Player player) {
        player.getInventory().setItem(7, spectatorItem);
    }
}
