package org.ilmiandluk.customMinigame.admin;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.map.Map;
import org.jetbrains.annotations.NotNull;

public class AdminExecutor implements CommandExecutor {

    private final CustomMinigame plugin;

    public AdminExecutor(CustomMinigame plugin) {
        this.plugin = plugin;
    }
    /*
        Здесь будет обработчики для команды /cmga
        Хочу сделать /cmga createmap <name> <size> <maxPlayers>
        которая будет создавать игровую карту на месте нахождения игрока sender.getLocation()

        Ну и дальше что-нибудь еще придумаем для админов.
        Скоро тут будут лежать обработчики для тестов (временно)
    */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }

        Player player = (Player) sender;
        Location loc = player.getLocation();

        Map map = new Map("Test", loc, 10, 10, 10);
        map.segmentInitialize();

        player.sendMessage("§eНачинаю построение схемы...");
        return true;
    }
}
