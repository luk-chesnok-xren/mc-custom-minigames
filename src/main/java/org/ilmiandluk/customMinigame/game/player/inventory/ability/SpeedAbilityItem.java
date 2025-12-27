package org.ilmiandluk.customMinigame.game.player.inventory.ability;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ilmiandluk.customMinigame.game.player.inventory.GameItem;

import java.util.ArrayList;
import java.util.List;

public class SpeedAbilityItem implements GameItem {
    private final ItemStack speedAbilityItem = new ItemStack(Material.FEATHER);
    private final ItemMeta speedAbilityItemMeta = speedAbilityItem.getItemMeta();

    {
        speedAbilityItemMeta.setItemName("§eУскорение");
        List<String> lore = new ArrayList<>();
        lore.add("§7Нажмите ПКМ чтобы ускорить §eВСЕХ СВОИХ");
        lore.add("§7солдат на 120 секунд");
        speedAbilityItemMeta.setLore(lore);
        speedAbilityItem.setItemMeta(speedAbilityItemMeta);
    }

    @Override
    public ItemStack getItem() {
        return speedAbilityItem;
    }

    @Override
    public void giveItemToPlayer(Player player) {
        player.getInventory().addItem(speedAbilityItem);
    }
}
