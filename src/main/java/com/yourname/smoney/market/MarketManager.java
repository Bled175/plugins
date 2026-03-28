package com.yourname.smoney.market;

import com.yourname.smoney.economy.EconomyManager;
import com.yourname.smoney.economy.TransactionLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MarketManager {

    private final Map<String, MarketItem> items = new HashMap<>();
    private final EconomyManager economy;
    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final TransactionLogger logger;
    
    // 🛡️ Anti-dupe: track ongoing transactions
    private final Set<String> transactionsInProgress = new HashSet<>();

    private final double TAX_PERCENT = 0.01;

    // ✅ CONSTRUCTOR FINAL (SATU SAJA)
    public MarketManager(EconomyManager economy, JavaPlugin plugin, TransactionLogger logger) {
        this.economy = economy;
        this.plugin = plugin;
        this.logger = logger;

        this.config = plugin.getConfig();

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

        // 🛡️ Anti-dupe: prevent double-click buying
        if (transactionsInProgress.contains(id)) {
            buyer.sendMessage("§cTransaksi sedang diproses...");
            return false;
        }

        MarketItem item = items.get(id);

        if (item == null) {
            buyer.sendMessage("§cItem tidak ditemukan!");
            return false;
        }

        // ✅ Lock transaction
        transactionsInProgress.add(id);

        try {
            double price = item.getPrice();
            UUID buyerId = buyer.getUniqueId();

            if (economy.getMoney(buyerId) < price) {
                buyer.sendMessage("§cUang kamu tidak cukup!");
                return false;
            }

            // 🔍 Validate item exists and is similar to what we're selling
            ItemStack itemToGive = item.getItem().clone();
            if (itemToGive == null || itemToGive.getAmount() == 0) {
                buyer.sendMessage("§cItem tidak valid!");
                return false;
            }

            double tax = price * TAX_PERCENT;
            double sellerMoney = price - tax;

            // 💸 transaksi
            economy.removeMoney(buyerId, price);
            economy.addMoney(item.getSeller(), sellerMoney);

            // 🧾 log transaksi
            logger.log(buyerId, item.getSeller(), itemToGive, price, tax);

            // 🎁 kasih item (dengan inventory safety)
            giveItemSafely(buyer, itemToGive);

            // 🧹 hapus dari market
            items.remove(id);
            config.set("items." + id, null);
            plugin.saveConfig();

            buyer.sendMessage("§aPembelian berhasil! §7Tax: §e" + tax);

            return true;

        } finally {
            // 🔓 Unlock transaction
            transactionsInProgress.remove(id);
        }
    }
    
    // =====================
    // SELL ITEM (NEW)
    // =====================
    public void sellItem(Player seller, ItemStack item, double price) {
        
        if (item == null || item.getAmount() == 0) {
            seller.sendMessage("§cItem tidak valid!");
            return;
        }
        
        // Generate unique ID for market item
        String id = "market-" + UUID.randomUUID().toString();
        
        // Create MarketItem
        MarketItem marketItem = new MarketItem(id, seller.getUniqueId(), item, price);
        
        // Add to market
        addItem(marketItem);
        
        seller.sendMessage("§a✓ Item dijual ke market!");
        seller.sendMessage("§7Harga: §e" + price);
        seller.sendMessage("§7Tax: §c" + (price * TAX_PERCENT));
    }
    
    // =====================
    // BUY FROM GUI (NEW)
    // =====================
    public boolean buyFromGUI(Player buyer, int slot) {
        
        // Get the map of slots to item IDs from current inventory
        // This is handled by MarketGUI, so we need the item ID
        // For now, this will be called from listener
        // The actual buying is done via the regular buy() method
        // This is a wrapper to integrate with GUI interactions
        
        return false; // Implementation handled by MarketGUI directly
    }
    
    // =====================
    // INVENTORY SAFETY (NEW)
    // =====================
    private void giveItemSafely(Player player, ItemStack item) {
        
        // Check if inventory has space
        if (player.getInventory().firstEmpty() != -1) {
            // Inventory has space, add normally
            player.getInventory().addItem(item);
        } else {
            // Inventory full, drop item on ground
            player.getWorld().dropItem(player.getLocation(), item);
            player.sendMessage("§7Inventory penuh! Item dijatuhkan di tanah.");
        }
    }

    // =====================
    // LOAD DATA
    // =====================
    public void loadItems() {

        if (config.getConfigurationSection("items") == null) return;

        for (String id : config.getConfigurationSection("items").getKeys(false)) {

            String path = "items." + id;

            try {
                UUID seller = UUID.fromString(config.getString(path + ".seller"));
                double price = config.getDouble(path + ".price");
                ItemStack item = config.getItemStack(path + ".item");

                if (item == null) continue;

                MarketItem marketItem = new MarketItem(id, seller, item, price);
                items.put(id, marketItem);

            } catch (Exception e) {
                plugin.getLogger().warning("Gagal load item market: " + id);
            }
        }
    }

    // =====================
    // GETTER
    // =====================
    public Map<String, MarketItem> getItems() {
        return items;
    }
}