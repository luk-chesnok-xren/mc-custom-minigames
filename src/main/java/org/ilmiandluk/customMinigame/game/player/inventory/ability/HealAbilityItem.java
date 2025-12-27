package org.ilmiandluk.customMinigame.game.player.inventory.ability;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ilmiandluk.customMinigame.game.player.inventory.GameItem;

import java.util.ArrayList;
import java.util.List;

public class HealAbilityItem implements GameItem {
    private final ItemStack healAbilityItem = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
    private final ItemMeta healAbilityItemMeta = healAbilityItem.getItemMeta();

    {
        healAbilityItemMeta.setItemName("§cИсцеление на сегменте");
        List<String> lore = new ArrayList<>();
        lore.add("§7Нажмите ПКМ по сегменту");
        lore.add("§7чтобы исцелить §eСВОИХ §7солдат");
        healAbilityItemMeta.setLore(lore);
        healAbilityItem.setItemMeta(healAbilityItemMeta);
    }

    @Override
    public ItemStack getItem() {
        return healAbilityItem;
    }

    @Override
    public void giveItemToPlayer(Player player) {
        player.getInventory().addItem(healAbilityItem);
    }
}

