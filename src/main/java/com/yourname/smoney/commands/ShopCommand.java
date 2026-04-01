package com.yourname.smoney.commands;

import com.yourname.smoney.shop.ShopGUI;
import com.yourname.smoney.shop.ShopItem;
import com.yourname.smoney.shop.ShopManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ShopCommand implements CommandExecutor {

    private final ShopManager shopManager;

    public ShopCommand(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        // =====================
        // /shop
        // =====================
        if (args.length == 0) {
            new ShopGUI(shopManager).open(player);
            return true;
        }

        // =====================
        // /shop add <price> <stock>
        // =====================
        if (args.length == 3 && args[0].equalsIgnoreCase("add")) {

            if (!player.hasPermission("smoney.admin")) {
                player.sendMessage("§cKamu tidak punya izin!");
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();

            if (item == null || item.getType().isAir()) {
                player.sendMessage("§cPegang item dulu!");
                return true;
            }

            double price;
            int stock;

            // =====================
            // PARSE PRICE
            // =====================
            try {
                price = Double.parseDouble(args[1]);
            } catch (Exception e) {
                player.sendMessage("§cHarga tidak valid!");
                return true;
            }

            if (price <= 0) {
                player.sendMessage("§cHarga harus lebih dari 0!");
                return true;
            }

            // =====================
            // PARSE STOCK
            // =====================
            if (args[2].equalsIgnoreCase("inf")) {
                stock = -1;
            } else {
                try {
                    stock = Integer.parseInt(args[2]);
                } catch (Exception e) {
                    player.sendMessage("§cStock harus angka atau 'inf'!");
                    return true;
                }

                // 🔥 VALIDASI FINAL
                if (stock <= 0) {
                    player.sendMessage("§cStock harus > 0 atau gunakan 'inf'!");
                    return true;
                }
            }

            // =====================
            // SAVE ITEM
            // =====================
            String id = UUID.randomUUID().toString();

            shopManager.saveItem(id, new ShopItem(
                    item.clone(),
                    price,
                    stock
            ));

            player.sendMessage("§aItem berhasil ditambahkan!");
            player.sendMessage("§7Harga: §e" + price);
            player.sendMessage("§7Stock: §e" + (stock == -1 ? "∞" : stock));

            return true;
        }

        // =====================
        // HELP
        // =====================
        player.sendMessage("§eGunakan:");
        player.sendMessage("§7/shop");
        player.sendMessage("§7/shop add <price> <stock|inf>");

        return true;
    }
}