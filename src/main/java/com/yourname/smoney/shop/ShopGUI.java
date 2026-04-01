package com.yourname.smoney.shop;

import com.yourname.smoney.economy.CurrencyUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ShopGUI {

    private final ShopManager manager;

    public ShopGUI(ShopManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {

        Inventory inv = Bukkit.createInventory(null, 54, "§8Shop Page 1");

        int slot = 0;

        for (Map.Entry<String, ShopItem> entry : manager.getItems().entrySet()) {

            String id = entry.getKey();
            ShopItem item = entry.getValue();

            ItemStack stack = item.getItem().clone();
            ItemMeta meta = stack.getItemMeta();

            List<String> lore = new ArrayList<>();
            lore.add("§7Harga: " + CurrencyUtil.format(item.getPrice()));

            if (item.isInfinite()) {
                lore.add("§aStock: Infinite");
            } else {
                lore.add("§cStock: " + item.getStock());
            }

            if (player.hasPermission("smoney.admin")) {
                lore.add("§eKlik kanan = edit");
                lore.add("§7Klik kiri = beli");
            } else {
                lore.add("§7Klik untuk beli");
            }

            meta.setLore(lore);
            stack.setItemMeta(meta);

            inv.setItem(slot++, stack);
        }

        player.openInventory(inv);
    }
}