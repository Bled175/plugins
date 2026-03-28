package com.yourname.smoney.economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TransactionLogger {

    private final JavaPlugin plugin;
    private final FileConfiguration config;

    public TransactionLogger(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void log(UUID buyer, UUID seller, ItemStack item, double price, double tax) {

        String id = generateId();

        String path = "transactions." + id;

        config.set(path + ".buyer", buyer.toString());
        config.set(path + ".seller", seller.toString());
        config.set(path + ".item", item);
        config.set(path + ".price", price);
        config.set(path + ".tax", tax);
        config.set(path + ".time", getTime());

        plugin.saveConfig();
    }

    private String generateId() {
        return "tx-" + UUID.randomUUID().toString();
    }

    private String getTime() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}