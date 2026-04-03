package com.yourname.smoney.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class DataManager {

    private final JavaPlugin plugin;

    private File dataFile;
    private FileConfiguration dataConfig;

    private File questFile;
    private FileConfiguration questConfig;

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadDataFile();
        loadQuestFile();
    }

    private void loadDataFile() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");

        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            plugin.saveResource("data.yml", false);
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void loadQuestFile() {
        questFile = new File(plugin.getDataFolder(), "quests.yml");

        if (!questFile.exists()) {
            plugin.saveResource("quests.yml", false);
        }

        questConfig = YamlConfiguration.loadConfiguration(questFile);
    }

    public FileConfiguration getConfig() {
        return dataConfig;
    }

    public FileConfiguration getQuestConfig() {
        return questConfig;
    }

    public void save() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveQuestConfig() {
        try {
            questConfig.save(questFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}