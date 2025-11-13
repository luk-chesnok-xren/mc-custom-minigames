package org.ilmiandluk.customMinigame.game.player.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LeaveItem implements GameItem {
    private static final ItemStack leaveItem = new ItemStack(Material.IRON_DOOR);
    private static final ItemMeta leaveItemMeta = leaveItem.getItemMeta();

    static {
        leaveItemMeta.setItemName("§cВыйти из игры");
        leaveItem.setItemMeta(leaveItemMeta);
    }

    @Override
    public ItemStack getItem() {
        return leaveItem;
    }

    @Override
    public void giveItemToPlayer(Player player) {
        player.getInventory().setItem(8, leaveItem);
    }
}
