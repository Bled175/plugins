package com.yourname.smoney.commands;

import com.yourname.smoney.market.MarketGUI;
import com.yourname.smoney.market.MarketManager;
import com.yourname.smoney.market.MarketMyGUI;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MarketCommand implements CommandExecutor {

    private final MarketManager marketManager;
    private final MarketGUI marketGUI;
    private final MarketMyGUI marketMyGUI;

    // ✅ FIX: inject GUI, jangan bikin baru
    public MarketCommand(MarketManager marketManager, MarketGUI marketGUI, MarketMyGUI marketMyGUI) {
        this.marketManager = marketManager;
        this.marketGUI = marketGUI;
        this.marketMyGUI = marketMyGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Hanya player!");
            return true;
        }

        // =====================
        // /market
        // =====================
        if (args.length == 0) {
            sendHelp(player);
            marketGUI.open(player); // ✅ FIX
            return true;
        }

        // =====================
        // /market help
        // =====================
        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sendHelp(player);
            return true;
        }

        // =====================
        // /market my
        // =====================
        if (args.length == 1 && args[0].equalsIgnoreCase("my")) {
            marketMyGUI.open(player); // ✅ FIX
            return true;
        }

        // =====================
        // /market sell <harga>
        // =====================
        if (args.length == 2 && args[0].equalsIgnoreCase("sell")) {

            ItemStack item = player.getInventory().getItemInMainHand();

            if (item == null || item.getType().isAir()) {
                player.sendMessage("§cKamu harus memegang item!");
                return true;
            }

            double price;

            try {
                price = Double.parseDouble(args[1]);
            } catch (Exception e) {
                player.sendMessage("§cHarga tidak valid!");
                return true;
            }

            marketManager.sellItem(player, item, price);

            player.sendMessage("§aItem berhasil dijual!");
            return true;
        }

        // =====================
        // /market undo <id>
        // =====================
        if (args.length == 2 && args[0].equalsIgnoreCase("undo")) {
            marketManager.undo(player, args[1]);
            return true;
        }

        // =====================
        // DEFAULT
        // =====================
        sendHelp(player);
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6=== Market Help ===");
        player.sendMessage("§e/market §7- Buka market");
        player.sendMessage("§e/market my §7- Lihat item kamu");
        player.sendMessage("§e/market sell <harga> §7- Jual item di tangan");
        player.sendMessage("§e/market undo <id> §7- Ambil kembali item");
    }
}