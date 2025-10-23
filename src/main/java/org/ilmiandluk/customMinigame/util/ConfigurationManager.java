package org.ilmiandluk.customMinigame.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.ilmiandluk.customMinigame.game.structures.AbstractStructure;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ConfigurationManager {
    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;
    private final String fileName;
    private final File structuresFolder;

    public ConfigurationManager(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.configFile = new File(plugin.getDataFolder(), fileName);
        this.structuresFolder = new File(plugin.getDataFolder(), "structures");
        reloadConfig();
    }

    public void reloadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        setupStructuresFolder();
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }

        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }
    }
    private static String replacePlaceholders(String text, int currentPlayers, int maxPlayers){
        return text != null ? text.
                replaceAll("%current_players%", String.valueOf(currentPlayers)).
                replaceAll("%max_players%", String.valueOf(maxPlayers)): null;
    }
    private static String replacePlaceholders(String text, String player) {
        return text != null ? text.replaceAll("%player%", player): null;
    }
    private static String replacePlaceholders(String text, String player1, String player2) {
        return text != null ? text.
                replaceAll("%player1%", player1).
                replaceAll("%player2%", player2): null;
    }
    private String replacePlaceholders(String text){
        return text != null ? text.
                replaceAll("%prefix%", getConfig().getString("prefix")): null;
    }
    private String replacePlaceholders(String text, int num){
        return text != null ? text.
                replaceAll("%seconds%", String.valueOf(num)): null;
    }
    private String translateColors(String text) {
        return text != null ? ChatColor.translateAlternateColorCodes('&', text) : null;
    }
    public void setupStructuresFolder() {
        if (!structuresFolder.exists()) {
            structuresFolder.mkdirs();
            plugin.getLogger().info("Created structures folder");
        }
    }

    synchronized private void copyStructureIfMissing(String schemFileName) {
        File schemFile = new File(structuresFolder, schemFileName);

        if (!schemFile.exists()) {
            try {
                InputStream inputStream = plugin.getResource("structures/" + schemFileName);

                if (inputStream != null) {
                    Files.copy(inputStream, schemFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    plugin.getLogger().info("Copied default structure: " + schemFileName);
                } else {
                    plugin.getLogger().warning("Structure file not found in resources: structures/" + schemFileName);
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not copy structure file: " + schemFileName, e);
            }
        }
    }

    public File getStructureFile(String structureName) {
        return new File(structuresFolder, structureName + ".schem");
    }

    public boolean structureExists(String structureName) {
        return getStructureFile(structureName).exists();
    }

    public String getString(String path) {
        return translateColors(replacePlaceholders(getConfig().getString(path, path + " not found")));
    }

    public String getString(String path, String defaultValue) {
        return translateColors(replacePlaceholders(getConfig().getString(path, defaultValue)));
    }
    public String getString(String path, Player player){
        return replacePlaceholders(getString(path), player.getName());
    }
    public String getString(String path, Player player1, Player player2) {
        return replacePlaceholders(getString(path), player1.getName(), player2.getName());
    }
    public String getString(String path, int seconds){
        return replacePlaceholders(getString(path), seconds);
    }
    public String getString(String path, Player player, int currentPlayers, int maxPlayers) {
        return replacePlaceholders(getString(path, player), currentPlayers, maxPlayers);
    }

    public List<String> getStringList(String path) {
        return getConfig().getStringList(path).stream()
                .map(this::replacePlaceholders)
                .map(this::translateColors)
                .collect(Collectors.toList());
    }
    public int getInt(String path) {
        return getConfig().getInt(path);
    }

    public int getInt(String path, int defaultValue) {
        return getConfig().getInt(path, defaultValue);
    }

    public boolean getBoolean(String path) {
        return getConfig().getBoolean(path);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return getConfig().getBoolean(path, defaultValue);
    }
    // Немного полиморфизма и говнокода?
    public String getStructurePath(AbstractStructure structure) {
        String structure_name = structure.getClass().getSimpleName().toLowerCase();
        copyStructureIfMissing(structure_name+".schem");
        return getString("structure." + structure_name);
    }
}