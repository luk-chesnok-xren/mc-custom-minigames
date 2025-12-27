package org.ilmiandluk.customMinigame.game.player.inventory.ability;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ilmiandluk.customMinigame.game.player.inventory.GameItem;

import java.util.ArrayList;
import java.util.List;

public class StrengthAbilityItem implements GameItem {
    private final ItemStack strengthAbilityItem = new ItemStack(Material.WOODEN_SWORD);
    private final ItemMeta strengthAbilityItemMeta = strengthAbilityItem.getItemMeta();

    {
        strengthAbilityItemMeta.setItemName("§eСила");
        List<String> lore = new ArrayList<>();
        lore.add("§7Нажмите ПКМ чтобы усилить §eВСЕХ СВОИХ");
        lore.add("§7солдат на 120 секунд");
        strengthAbilityItemMeta.setLore(lore);
        strengthAbilityItem.setItemMeta(strengthAbilityItemMeta);
    }

    @Override
    public ItemStack getItem() {
        return strengthAbilityItem;
    }

    @Override
    public void giveItemToPlayer(Player player) {
        player.getInventory().addItem(strengthAbilityItem);
    }
}
