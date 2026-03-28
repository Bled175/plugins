package com.yourname.smoney.market;

import com.yourname.smoney.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AdminWalletGUI {

    private final EconomyManager economy;

    private final UUID adminWallet = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public AdminWalletGUI(EconomyManager economy) {
        this.economy = economy;
    }

    public void open(Player player) {

        Inventory inv = Bukkit.createInventory(null, 27, "§6Admin Wallet");

        double balance = economy.getMoney(adminWallet);

        ItemStack item = new ItemStack(org.bukkit.Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§eServer Tax Wallet");

        List<String> lore = new ArrayList<>();
        lore.add("§7Total Tax: §a" + balance);

        meta.setLore(lore);
        item.setItemMeta(meta);

        inv.setItem(13, item);

        player.openInventory(inv);
    }
}