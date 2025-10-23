package org.ilmiandluk.customMinigame;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.ilmiandluk.customMinigame.admin.AdminExecutor;
import org.ilmiandluk.customMinigame.game.SignController;
//import org.ilmiandluk.customMinigame.game.handler.GameHandler;
//import org.ilmiandluk.customMinigame.game.handler.SignHandler;
import org.ilmiandluk.customMinigame.game.handler.GameHandler;
import org.ilmiandluk.customMinigame.game.handler.SignHandler;
import org.ilmiandluk.customMinigame.game.map.MapController;
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
    private SegmentBuilder segmentBuilder;

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

    public ConfigurationManager getMessagesManager() {
        return messagesManager;
    }
    public SegmentBuilder getSegmentBuilder() {
        return segmentBuilder;
    }

    @Override
    public void onEnable() {
        this.configManager = new ConfigurationManager(this, "config.yml");
        this.messagesManager = new ConfigurationManager(this, "messages.yml");
        this.segmentBuilder = new SegmentBuilder(this);

        setInstance(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                MapController.loadMapsFromFile();
                SignController.updateAllSigns();
            }
        }.runTaskLater(this, 20);


        getServer().getPluginManager().registerEvents(new GameHandler(), this);
        getServer().getPluginManager().registerEvents(new SignHandler(), this);
        getCommand("cmga").setExecutor(new AdminExecutor(this));
        Bukkit.getLogger().log(Level.INFO, "[CustomMinigame] Plugin has been enabled!");

    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().log(Level.INFO, "[CustomMinigame] Plugin has been disabled!");
    }
}
