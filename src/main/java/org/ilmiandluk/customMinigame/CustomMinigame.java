package org.ilmiandluk.customMinigame;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class CustomMinigame extends JavaPlugin {

    @Override
    public void onEnable() {
        getDataFolder().mkdir();
        Bukkit.getLogger().log(Level.INFO, "[CustomMinigame] Plugin has been enabled!");

    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().log(Level.INFO, "[CustomMinigame] Plugin has been disabled!");
    }
}
