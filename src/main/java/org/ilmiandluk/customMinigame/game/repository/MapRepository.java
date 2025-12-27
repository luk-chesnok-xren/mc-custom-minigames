package org.ilmiandluk.customMinigame.game.repository;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.map.Map;

import java.io.File;
import java.util.LinkedHashMap;

public class MapRepository {
    private static FileConfiguration mapsConfig;
    private final static LinkedHashMap<String, Map> mapList = new LinkedHashMap<>();

    public static Map getMap(String name){
        return mapList.get(name);
    }
    public static Map createMap(String name, Location location, int maxPlayers, int xSize, int zSize){
        Map newMap = new Map(name, location, maxPlayers, xSize, zSize);
        addMapToFile(newMap);
        return newMap;
    }

    /**
     Загружает все карты из файла maps.yml в mapList (десериализация объектов).
     Возможен многократный вызов (Чтобы подтянуть изменения из файла,
     внесённые вручную в maps.yml).
     */
    public static void loadMapsFromFile(){
        File file = getMapsYMLFile();
        mapsConfig = YamlConfiguration.loadConfiguration(file);

        if (mapsConfig.contains("maps")) {
            for (String mapName : mapsConfig.getConfigurationSection("maps").getKeys(false)) {
                int maxPlayers = mapsConfig.getInt("maps." + mapName + ".maxPlayers");
                int xSize = mapsConfig.getInt("maps." + mapName + ".xSize");
                int zSize = mapsConfig.getInt("maps." + mapName + ".zSize");

                Location mapLocation = getLocationFromPath("maps." + mapName + ".mapLocation");
                mapList.put(mapName, new Map(mapName, mapLocation, maxPlayers, xSize, zSize));

            }
        }
    }

    /**
     Сохраняет новую карту в файл maps.yml и добавляет в mapList (сериализация объектов).
     */
    public static void addMapToFile(Map map){
        File file = getMapsYMLFile();
        mapsConfig = YamlConfiguration.loadConfiguration(file);

        String path = "maps." +  map.getMapName();
        mapsConfig.set(path + ".xSize", map.getxSize());
        mapsConfig.set(path + ".zSize", map.getzSize());
        mapsConfig.set(path + ".maxPlayers", map.getMaxPlayers());
        setLocationToPath(String.format("%s.mapLocation", path), map.getMapLocation());
        try {
            mapsConfig.save(file);
        }catch (Exception ex){
            CustomMinigame.getInstance().getLogger().severe("Failed to create maps.yml file!");
            ex.printStackTrace();
        }
        mapList.put(map.getMapName(), map);
    }

    /**
     Методы для работы с Location
     (Десериализация и сериализация из конфига)
     */

    private static void setLocationToPath(String path, Location loc){
        mapsConfig.set(path + ".world", loc.getWorld().getName());
        mapsConfig.set(path + ".x", loc.getX());
        mapsConfig.set(path + ".y", loc.getY());
        mapsConfig.set(path + ".z", loc.getZ());
        mapsConfig.set(path + ".yaw", loc.getYaw());
        mapsConfig.set(path + ".pitch", loc.getPitch());
    }
    private static Location getLocationFromPath(String path){
        Location loc = new Location(
                Bukkit.getWorld(mapsConfig.getString(path + ".world")),
                mapsConfig.getDouble(path + ".x"),
                mapsConfig.getDouble(path + ".y"),
                mapsConfig.getDouble(path + ".z"),
                (float) mapsConfig.getDouble(path + ".yaw"),
                (float) mapsConfig.getDouble(path + ".pitch")
        );
        return loc;
    }
    /**
     Возвращает File для maps.yml. Пытается создать, если его не существует.
     */
    private static File getMapsYMLFile(){
        File file = new File(CustomMinigame.getInstance().getDataFolder(), "maps.yml");
        if (!file.exists()) {
            CustomMinigame.getInstance().getDataFolder().mkdirs();
            try {
                file.createNewFile();
            }catch (Exception ex){
                CustomMinigame.getInstance().getLogger().severe("Failed to create maps.yml file!");
                ex.printStackTrace();
            }
        }
        return file;
    }
}
