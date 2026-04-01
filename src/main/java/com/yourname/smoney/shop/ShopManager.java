package com.yourname.smoney.shop;

import com.yourname.smoney.data.DataManager;
import com.yourname.smoney.economy.EconomyManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopManager {

    private final Map<String, ShopItem> items = new HashMap<>();
    private final DataManager data;
    private final EconomyManager economy;

    // 🔥 ADMIN STATE
    private final Map<UUID, String> editingPrice = new HashMap<>();
    private final Map<UUID, String> lastOpened = new HashMap<>();

    public ShopManager(DataManager data, EconomyManager economy) {
        this.data = data;
        this.economy = economy;
        load();
    }

    // =====================
    // LOAD
    // =====================
    public void load() {

        items.clear(); // 🔥 pastikan tidak duplicate

        if (data.getConfig().getConfigurationSection("shop") == null) return;

        for (String key : data.getConfig().getConfigurationSection("shop").getKeys(false)) {

            ItemStack item = data.getConfig().getItemStack("shop." + key + ".item");
            double price = data.getConfig().getDouble("shop." + key + ".price");
            int stock = data.getConfig().getInt("shop." + key + ".stock");

            if (item == null) continue;

            items.put(key, new ShopItem(item.clone(), price, stock)); // 🔥 clone
        }
    }

    // =====================
    // SAVE ITEM
    // =====================
    public void saveItem(String id, ShopItem item) {

        data.getConfig().set("shop." + id + ".item", item.getItem());
        data.getConfig().set("shop." + id + ".price", item.getPrice());
        data.getConfig().set("shop." + id + ".stock", item.getStock());
        data.save();

        items.put(id, item);
    }

    // =====================
    // BUY
    // =====================
    public boolean buy(Player player, String id) {

        ShopItem item = items.get(id);

        if (item == null) {
            player.sendMessage("§cItem tidak ditemukan!");
            return false;
        }

        if (!item.isInfinite() && item.isOutOfStock()) {
            player.sendMessage("§cStok habis!");
            return false;
        }

        double price = item.getPrice();

        if (price <= 0) {
            player.sendMessage("§cHarga item error!");
            return false;
        }

        if (economy.getMoney(player.getUniqueId()) < price) {
            player.sendMessage("§cUang kamu tidak cukup!");
            return false;
        }

        // 💸 transaksi
        economy.removeMoney(player.getUniqueId(), price);

        // 🎁 kasih item (clone biar aman)
        player.getInventory().addItem(item.getItem());

        // 📦 kurangi stock
        if (!item.isInfinite()) {
            item.reduceStock();

            data.getConfig().set("shop." + id + ".stock", item.getStock());
            data.save();
        }

        player.sendMessage("§aKamu membeli item seharga §e" + price);
        return true;
    }

    // =====================
    // ADMIN: REMOVE
    // =====================
    public void removeItem(String id) {

        if (!items.containsKey(id)) return;

        items.remove(id);
        data.getConfig().set("shop." + id, null);
        data.save();
    }

    // =====================
    // ADMIN: EDIT PRICE MODE
    // =====================
    public void setEditing(Player player, String id) {
        editingPrice.put(player.getUniqueId(), id);
    }

    public boolean isEditing(Player player) {
        return editingPrice.containsKey(player.getUniqueId());
    }

    public void handleChat(Player player, String msg) {

        if (!player.hasPermission("smoney.admin")) return;

        UUID uuid = player.getUniqueId();

        if (!editingPrice.containsKey(uuid)) return;

        String id = editingPrice.remove(uuid);

        double price;

        try {
            price = Double.parseDouble(msg);
        } catch (Exception e) {
            player.sendMessage("§cHarga tidak valid!");
            return;
        }

        if (price <= 0) {
            player.sendMessage("§cHarga harus lebih dari 0!");
            return;
        }

        ShopItem item = items.get(id);
        if (item == null) {
            player.sendMessage("§cItem tidak ditemukan!");
            return;
        }

        item.setPrice(price);

        data.getConfig().set("shop." + id + ".price", price);
        data.save();

        player.sendMessage("§aHarga berhasil diubah!");
    }

    // =====================
    // ADMIN: TRACK ITEM
    // =====================
    public void setLastOpened(Player player, String id) {
        lastOpened.put(player.getUniqueId(), id);
    }

    public String getLastOpenedItem(Player player) {
        return lastOpened.get(player.getUniqueId());
    }

    // =====================
    // GETTERS
    // =====================
    public Map<String, ShopItem> getItems() {
        return items;
    }

    public ShopItem getItem(String id) {
        return items.get(id);
    }
}