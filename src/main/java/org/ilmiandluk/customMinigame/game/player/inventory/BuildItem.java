package org.ilmiandluk.customMinigame.game.player.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BuildItem implements GameItem{
    private static final ItemStack buildItem = new ItemStack(Material.BLAZE_ROD);
    private static final ItemMeta buildItemMeta = buildItem.getItemMeta();
    static{
        buildItemMeta.setItemName("§aПостроить здание");
        buildItem.setItemMeta(buildItemMeta);
    }
    @Override
    public ItemStack getItem() {
        return buildItem;
    }

    @Override
    public void giveItemToPlayer(Player player) {
        player.getInventory().setItem(4, buildItem);
    }
}
