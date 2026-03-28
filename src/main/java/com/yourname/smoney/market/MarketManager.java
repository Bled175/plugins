package com.yourname.smoney.market;

import com.yourname.smoney.economy.EconomyManager;
import com.yourname.smoney.economy.TransactionLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MarketManager {

    private final Map<String, MarketItem> items = new HashMap<>();
    private final EconomyManager economy;
    private final FileConfiguration config;

    private final JavaPlugin plugin;

    private final TransactionLogger logger;
    private final double TAX_PERCENT = 0.01;

    public MarketManager(EconomyManager economy, JavaPlugin plugin) {
        this.economy = economy;
        this.plugin = plugin;

        this.config = plugin.getConfig();

        this.logger = new TransactionLogger(plugin);

        loadItems();
    }

    // =====================
    // ADD ITEM
    // =====================
    public void addItem(MarketItem item) {

        items.put(item.getId(), item);

        config.set("items." + item.getId() + ".seller", item.getSeller().toString());
        config.set("items." + item.getId() + ".price", item.getPrice());
        config.set("items." + item.getId() + ".item", item.getItem());

        plugin.saveConfig();
    }

    // =====================
    // BUY SYSTEM
    // =====================
    public boolean buy(Player buyer, String id) {

        MarketItem item = items.get(id);

        if (item == null) {
            buyer.sendMessage("§cItem tidak ditemukan!");
            return false;
        }

        double price = item.getPrice();
        UUID buyerId = buyer.getUniqueId();

        if (economy.getMoney(buyerId) < price) {
            buyer.sendMessage("§cUang kamu tidak cukup!");
            return false;
        }

        double tax = price * TAX_PERCENT;
        double sellerMoney = price - tax;

        economy.removeMoney(buyerId, price);
        economy.addMoney(item.getSeller(), sellerMoney);

        // admin tax wallet (optional)
        // economy.addMoney(adminUUID, tax);

        buyer.getInventory().addItem(item.getItem().clone());

        // remove from memory + config
        items.remove(id);
        config.set("items." + id, null);
        plugin.saveConfig();

        buyer.sendMessage("§aPembelian berhasil! §7Tax: §e" + tax);

        return true;
    }

    // =====================
    // LOAD ON START
    // =====================
    public void loadItems() {

        if (config.getConfigurationSection("items") == null) return;

        for (String id : config.getConfigurationSection("items").getKeys(false)) {

            String path = "items." + id;

            UUID seller = UUID.fromString(config.getString(path + ".seller"));
            double price = config.getDouble(path + ".price");
            ItemStack item = config.getItemStack(path + ".item");

            if (item == null) continue;

            MarketItem marketItem = new MarketItem(id, seller, item, price);

            items.put(id, marketItem);
        }
    }

    // =====================
    // GET ITEMS
    // =====================
    public Map<String, MarketItem> getItems() {
        return items;
    }
}