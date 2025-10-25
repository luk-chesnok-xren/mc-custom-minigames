package org.ilmiandluk.customMinigame.game.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ExploreItem implements GameItem {
    private static final ItemStack exploreItem = new ItemStack(Material.STICK);
    private static final ItemMeta exploreItemMeta = exploreItem.getItemMeta();
    static{
        exploreItemMeta.setItemName("§aИсследовать территорию");
        exploreItem.setItemMeta(exploreItemMeta);
    }

    @Override
    public ItemStack getItem(){
        return exploreItem;
    }
    @Override
    public void giveItemToPlayer(Player player){
        player.getInventory().setItem(0, exploreItem);
    }
}
