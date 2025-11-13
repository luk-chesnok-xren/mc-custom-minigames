package org.ilmiandluk.customMinigame.game.player.inventory.ability;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ilmiandluk.customMinigame.game.player.inventory.GameItem;

import java.util.ArrayList;
import java.util.List;

public class TeleportAbilityItem implements GameItem {
    private final ItemStack teleportAbilityItem = new ItemStack(Material.ENDER_PEARL);
    private final ItemMeta teleportAbilityItemMeta = teleportAbilityItem.getItemMeta();

    {
        teleportAbilityItemMeta.setItemName("§eТелепортировать на точки");
        List<String> lore = new ArrayList<>();
        lore.add("§7Нажмите ПКМ чтобы телепортировать §eВСЕХ СВОИХ");
        lore.add("§7солдат на нужные сегменты (если они куда-то идут)");
        teleportAbilityItemMeta.setLore(lore);
        teleportAbilityItem.setItemMeta(teleportAbilityItemMeta);
    }

    @Override
    public ItemStack getItem() {
        return teleportAbilityItem;
    }

    @Override
    public void giveItemToPlayer(Player player) {
        player.getInventory().addItem(teleportAbilityItem);
    }
}
