package org.ilmiandluk.customMinigame.game.player.inventory.ability;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ilmiandluk.customMinigame.game.player.inventory.GameItem;

import java.util.ArrayList;
import java.util.List;

public class NuclearAbilityItem implements GameItem {
    private final ItemStack nuclearAbilityItem = new ItemStack(Material.RECOVERY_COMPASS);
    private final ItemMeta nuclearAbilityItemMeta = nuclearAbilityItem.getItemMeta();

    {
        nuclearAbilityItemMeta.setItemName("§4ЯДЕРНАЯ РАКЕТА");
        List<String> lore = new ArrayList<>();
        lore.add("§7Нажмите ПКМ по сегменту");
        lore.add("§7чтобы убить §cВСЕХ §7солдат в радиусе 3x3");
        nuclearAbilityItemMeta.setLore(lore);
        nuclearAbilityItem.setItemMeta(nuclearAbilityItemMeta);
    }

    @Override
    public ItemStack getItem() {
        return nuclearAbilityItem;
    }
    @Override
    public void giveItemToPlayer(Player player) {
        player.getInventory().addItem(nuclearAbilityItem);
    }
}


