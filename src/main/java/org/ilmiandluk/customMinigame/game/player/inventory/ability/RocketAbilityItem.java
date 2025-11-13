package org.ilmiandluk.customMinigame.game.player.inventory.ability;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ilmiandluk.customMinigame.game.player.inventory.GameItem;

import java.util.ArrayList;
import java.util.List;

public class RocketAbilityItem implements GameItem {
    private final ItemStack rocketAbilityItem = new ItemStack(Material.FIREWORK_ROCKET);
    private final ItemMeta rocketAbilityItemMeta = rocketAbilityItem.getItemMeta();

    {
        rocketAbilityItemMeta.setItemName("§cОбычная ракета");
        List<String> lore = new ArrayList<>();
        lore.add("§7Нажмите ПКМ по сегменту");
        lore.add("§7чтобы убить §cВСЕХ §7солдат");
        rocketAbilityItemMeta.setLore(lore);
        rocketAbilityItem.setItemMeta(rocketAbilityItemMeta);
    }

    @Override
    public ItemStack getItem() {
        return rocketAbilityItem;
    }

    @Override
    public void giveItemToPlayer(Player player) {
        player.getInventory().addItem(rocketAbilityItem);
    }
}
