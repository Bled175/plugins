package com.yourname.smoney.market;

import com.yourname.smoney.economy.EconomyManager;
import com.yourname.smoney.economy.TransactionLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MarketManager {

    private final Map<String, MarketItem> items = new LinkedHashMap<>();
    private final Set<String> transactionsInProgress = new HashSet<>();

    private final EconomyManager economy;
    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final TransactionLogger logger;

    private final double TAX_PERCENT = 0.01;

    public MarketManager(EconomyManager economy, JavaPlugin plugin, TransactionLogger logger) {
        this.economy = economy;
        this.plugin = plugin;
        this.logger = logger;
        this.config = plugin.getConfig();

        loadItems();
    }

    // =====================
    // SELL
    // =====================
    public void sellItem(Player seller, ItemStack item, double price) {

        if (item == null || item.getType().isAir()) {
            seller.sendMessage("§cItem tidak valid!");
            return;
        }

        if (price <= 0) {
            seller.sendMessage("§cHarga harus lebih dari 0!");
            return;
        }

        // 🔥 FIX: clean item
        ItemStack clone = item.clone();
        clone.setAmount(1);

        String id = "market-" + UUID.randomUUID();

        MarketItem marketItem = new MarketItem(id, seller.getUniqueId(), clone, price);

        addItem(marketItem);

        seller.sendMessage("§a✓ Item masuk market!");
        seller.sendMessage("§7ID: §e" + id);
        seller.sendMessage("§7Harga: §e" + price);
    }

    // =====================
    // BUY
    // =====================
    public boolean buy(Player buyer, String id) {

        if (transactionsInProgress.contains(id)) {
            buyer.sendMessage("§cTransaksi sedang diproses...");
            return false;
        }

        MarketItem item = items.get(id);

        if (item == null) {
            buyer.sendMessage("§cItem tidak ditemukan!");
            return false;
        }

        if (item.getSeller().equals(buyer.getUniqueId())) {
            buyer.sendMessage("§cTidak bisa beli item sendiri!");
            return false;
        }

        transactionsInProgress.add(id);

        try {
            double price = item.getPrice();

            double money = economy.getMoney(buyer.getUniqueId());
            if (money < price) {
                buyer.sendMessage("§cUang tidak cukup!");
                return false;
            }

            // 🔥 FIX: NO DOUBLE CLONE
            ItemStack itemToGive = item.getItem();

            if (itemToGive == null || itemToGive.getType().isAir()) {
                buyer.sendMessage("§cItem corrupt!");
                return false;
            }

            double tax = price * TAX_PERCENT;
            double sellerMoney = price - tax;

            economy.removeMoney(buyer.getUniqueId(), price);
            economy.addMoney(item.getSeller(), sellerMoney);

            giveItemSafely(buyer, itemToGive.clone());

            logger.log(buyer.getUniqueId(), item.getSeller(), itemToGive, price, tax);

            items.remove(id);
            config.set("items." + id, null);
            plugin.saveConfig();

            buyer.sendMessage("§aPembelian berhasil!");
            return true;

        } finally {
            transactionsInProgress.remove(id);
        }
    }

    // =====================
    // UNDO
    // =====================
    public boolean undo(Player player, String id) {

        MarketItem item = items.get(id);

        if (item == null) {
            player.sendMessage("§cItem tidak ditemukan!");
            return false;
        }

        if (!item.getSeller().equals(player.getUniqueId())) {
            player.sendMessage("§cIni bukan item kamu!");
            return false;
        }

        giveItemSafely(player, item.getItem().clone());

        items.remove(id);
        config.set("items." + id, null);
        plugin.saveConfig();

        player.sendMessage("§aItem berhasil diambil!");
        return true;
    }

    // =====================
    // ADD ITEM
    // =====================
    public void addItem(MarketItem item) {

        items.put(item.getId(), item);

        // 🔥 FIX: SAFE SAVE
        ItemStack safeItem = item.getItem().clone();
        safeItem.setAmount(1);

        config.set("items." + item.getId() + ".seller", item.getSeller().toString());
        config.set("items." + item.getId() + ".price", item.getPrice());
        config.set("items." + item.getId() + ".item", safeItem);

        plugin.saveConfig();
    }

    // =====================
    // LOAD
    // =====================
    public void loadItems() {

        if (config.getConfigurationSection("items") == null) return;

        for (String id : config.getConfigurationSection("items").getKeys(false)) {

            String path = "items." + id;

            try {
                UUID seller = UUID.fromString(config.getString(path + ".seller"));
                double price = config.getDouble(path + ".price");
                ItemStack item = config.getItemStack(path + ".item");

                if (item == null || item.getType().isAir()) continue;

                items.put(id, new MarketItem(id, seller, item, price));

            } catch (Exception e) {
                plugin.getLogger().warning("Gagal load item: " + id);
            }
        }
    }

    // =====================
    // UTIL
    // =====================
    private void giveItemSafely(Player player, ItemStack item) {

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
        } else {
            player.getWorld().dropItem(player.getLocation(), item);
            player.sendMessage("§7Inventory penuh, item dijatuhkan.");
        }
    }

    public Map<String, MarketItem> getItems() {
        return items;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}