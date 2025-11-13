package org.ilmiandluk.customMinigame;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.ilmiandluk.customMinigame.admin.executor.AdminExecutor;
import org.ilmiandluk.customMinigame.game.executor.MainExecutor;
import org.ilmiandluk.customMinigame.game.handler.GUIHandler;
import org.ilmiandluk.customMinigame.game.listener.GUIListener;
import org.ilmiandluk.customMinigame.game.listener.GameListener;
import org.ilmiandluk.customMinigame.game.listener.SignListener;
import org.ilmiandluk.customMinigame.game.repository.MapRepository;
import org.ilmiandluk.customMinigame.game.repository.SignRepository;
import org.ilmiandluk.customMinigame.game.handler.GameHandler;
import org.ilmiandluk.customMinigame.game.handler.SignHandler;
import org.ilmiandluk.customMinigame.game.map.SegmentBuilder;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;

import java.util.logging.Level;

public final class CustomMinigame extends JavaPlugin {
    private static CustomMinigame instance;
    public static CustomMinigame getInstance() {
        return instance;
    }
    public static void setInstance(CustomMinigame instance) {
        CustomMinigame.instance = instance;
    }

    private ConfigurationManager configManager;
    private ConfigurationManager messagesManager;

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

    public ConfigurationManager getMessagesManager() {
        return messagesManager;
    }


    @Override
    public void onEnable() {
        this.configManager = new ConfigurationManager(this, "config.yml");
        this.messagesManager = new ConfigurationManager(this, "messages.yml");

        setInstance(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                MapRepository.loadMapsFromFile();
                SignRepository.updateAllSigns();
            }
        }.runTaskLater(this, 20);


        getServer().getPluginManager().registerEvents(new GameListener(new GameHandler()), this);
        getServer().getPluginManager().registerEvents(new SignListener(new SignHandler()), this);
        getServer().getPluginManager().registerEvents(new GUIListener(new GUIHandler()), this);
        getCommand("cmga").setExecutor(new AdminExecutor(this));
        getCommand("cmg").setExecutor(new MainExecutor());
        Bukkit.getLogger().log(Level.INFO, "[CustomMinigame] Plugin has been enabled!");

    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().log(Level.INFO, "[CustomMinigame] Plugin has been disabled!");
    }
}
