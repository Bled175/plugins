package com.yourname.smoney;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig(); // config.yml

        saveResource("data.yml", false);
        saveResource("quests.yml", false);

        getLogger().info("Semua file berhasil dibuat!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin smoney dimatikan!");
    }
}