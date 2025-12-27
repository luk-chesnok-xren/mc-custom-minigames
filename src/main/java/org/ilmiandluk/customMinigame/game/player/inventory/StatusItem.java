package org.ilmiandluk.customMinigame.game.player.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatusItem implements GameItem{
    private static final ItemStack statusItem = new ItemStack(Material.PLAYER_HEAD);
    private static final ItemMeta statusItemMeta = statusItem.getItemMeta();
    static{
        statusItemMeta.setItemName("§eОтчет об игроках");
        statusItem.setItemMeta(statusItemMeta);
    }

    @Override
    public ItemStack getItem(){
        return statusItem;
    }
    @Override
    public void giveItemToPlayer(Player player){
        player.getInventory().setItem(8, statusItem);
    }
}
