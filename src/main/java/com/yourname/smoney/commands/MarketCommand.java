package com.yourname.smoney.commands;

import com.yourname.smoney.economy.EconomyManager;
import com.yourname.smoney.market.MarketGUI;
import com.yourname.smoney.market.MarketManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class MarketCommand implements CommandExecutor {

    private final MarketManager marketManager;
    private final EconomyManager economyManager;

    public MarketCommand(MarketManager marketManager, EconomyManager economyManager) {
        this.marketManager = marketManager;
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Hanya player!");
            return true;
        }

        // /market (open market GUI)
        if (args.length == 0) {
            new MarketGUI(marketManager).open(player);
            return true;
        }

        // /market sell <price> (sell item in hand)
        if (args.length == 2 && args[0].equalsIgnoreCase("sell")) {

            ItemStack inHand = player.getInventory().getItemInMainHand();

            if (inHand == null || inHand.getAmount() == 0) {
                player.sendMessage("§cKamu harus memegang item!");
                return true;
            }

            double price;

            try {
                price = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cHarga tidak valid!");
                return true;
            }

            if (price <= 0) {
                player.sendMessage("§cHarga harus lebih dari 0!");
                return true;
            }

            // Sell item
            marketManager.sellItem(player, inHand.clone(), price);
            inHand.setAmount(0);
            player.sendMessage("§aItem dijual! Harga: §e" + price);

            return true;
        }

        player.sendMessage("§eUsage: /market | /market sell <price>");
        return true;
    }
}
