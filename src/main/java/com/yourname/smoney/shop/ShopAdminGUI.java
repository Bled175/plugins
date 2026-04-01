package com.yourname.smoney.shop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ShopAdminGUI {

    private final ShopManager manager;
    private final String id;

    public ShopAdminGUI(ShopManager manager, String id) {
        this.manager = manager;
        this.id = id;
    }

    public void open(Player player) {

        manager.setLastOpened(player, id);

        Inventory inv = Bukkit.createInventory(null, 27, "§cManage Item");

        inv.setItem(11, createItem(Material.GOLD_INGOT, "§eEdit Harga", "§7Klik untuk edit"));
        inv.setItem(15, createItem(Material.BARRIER, "§cHapus Item", "§7Klik untuk hapus"));

        player.openInventory(inv);
    }

    public void handleClick(Player player, int slot) {

        if (slot == 11) {
            manager.setEditing(player, id);
            player.closeInventory();
            player.sendMessage("§eMasukkan harga baru di chat:");
            return;
        }

        if (slot == 15) {
            manager.removeItem(id);
            player.closeInventory();
            player.sendMessage("§cItem dihapus!");
        }
    }

    private ItemStack createItem(Material mat, String name, String loreText) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(loreText));
        item.setItemMeta(meta);
        return item;
    }
}