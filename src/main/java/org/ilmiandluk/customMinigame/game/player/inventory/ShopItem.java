package org.ilmiandluk.customMinigame.game.player.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
public class ShopItem implements GameItem {
    private static final ItemStack shopItem = new ItemStack(Material.ENDER_CHEST);
    private static final ItemMeta shopItemMeta = shopItem.getItemMeta();


    static {
        shopItemMeta.setItemName("§6Открыть магазин товаров");
        shopItem.setItemMeta(shopItemMeta);
    }

    @Override
    public ItemStack getItem() {
        return shopItem;
    }

    @Override
    public void giveItemToPlayer(Player player) {
        player.getInventory().setItem(3, shopItem);
    }
}
