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

        Inventory inv = Bukkit.createInventory(null, 27, "§8Shop");

        int slot = 10;

        for (ShopItem item : manager.getItems().values()) {

            ItemStack stack = item.getItem().clone();
            ItemMeta meta = stack.getItemMeta();

            List<String> lore = new ArrayList<>();
            lore.add("§7Harga: " + CurrencyUtil.format(item.getPrice()));

            if (item.getStock() == -1) {
                lore.add("§aStock: Infinite");
            } else {
                lore.add("§cStock: " + item.getStock());
            }

            meta.setLore(lore);
            stack.setItemMeta(meta);

            inv.setItem(slot++, stack);
        }

        player.openInventory(inv);
    }
}