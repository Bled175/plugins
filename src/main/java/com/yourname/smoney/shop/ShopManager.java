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

    public ShopManager(DataManager data, EconomyManager economy) {
        this.data = data;
        this.economy = economy;
        load();
    }

    // =====================
    // LOAD SHOP FROM FILE
    // =====================
    public void load() {
        if (data.getConfig().getConfigurationSection("shop") == null) return;

        for (String key : data.getConfig().getConfigurationSection("shop").getKeys(false)) {

            ItemStack item = data.getConfig().getItemStack("shop." + key + ".item");
            double price = data.getConfig().getDouble("shop." + key + ".price");
            int stock = data.getConfig().getInt("shop." + key + ".stock");

            items.put(key, new ShopItem(item, price, stock));
        }
    }

    // =====================
    // SAVE / ADD ITEM ADMIN
    // =====================
    public void saveItem(String id, ShopItem item) {

        data.getConfig().set("shop." + id + ".item", item.getItem());
        data.getConfig().set("shop." + id + ".price", item.getPrice());
        data.getConfig().set("shop." + id + ".stock", item.getStock());
        data.save();

        items.put(id, item);
    }

    // =====================
    // BUY SYSTEM (CORE)
    // =====================
    public boolean buy(Player player, String id) {

        UUID uuid = player.getUniqueId();
        ShopItem item = items.get(id);

        if (item == null) {
            player.sendMessage("§cItem tidak ditemukan!");
            return false;
        }

        // cek stock
        if (!item.isInfinite() && item.isOutOfStock()) {
            player.sendMessage("§cStok habis!");
            return false;
        }

        double price = item.getPrice();

        // cek uang dari SMoney
        if (economy.getMoney(uuid) < price) {
            player.sendMessage("§cUang kamu tidak cukup!");
            return false;
        }

        // potong uang
        economy.removeMoney(uuid, price);

        // kasih item
        player.getInventory().addItem(item.getItem().clone());

        // kurangi stock
        item.reduceStock();

        player.sendMessage("§aKamu membeli item seharga §e" + price);

        return true;
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