package com.yourname.smoney.commands;

import com.yourname.smoney.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MoneyCommand implements CommandExecutor {

    private final EconomyManager economy;

    public MoneyCommand(EconomyManager economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Hanya player!");
            return true;
        }

        UUID uuid = player.getUniqueId();

        // =====================
        // /money
        // =====================
        if (args.length == 0) {
            player.sendMessage("§aUang kamu: §e" + economy.getMoney(uuid));
            return true;
        }

        // =====================
        // /money pay <player> <amount>
        // =====================
        if (args.length == 3 && args[0].equalsIgnoreCase("pay")) {

            Player target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                player.sendMessage("§cPlayer tidak ditemukan!");
                return true;
            }

            double amount;

            try {
                amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cJumlah tidak valid!");
                return true;
            }

            if (amount <= 0) {
                player.sendMessage("§cJumlah harus lebih dari 0!");
                return true;
            }

            boolean success = economy.transfer(uuid, target.getUniqueId(), amount);

            if (!success) {
                player.sendMessage("§cUang tidak cukup!");
                return true;
            }

            player.sendMessage("§aKamu mengirim §e" + amount + " §ake " + target.getName());
            target.sendMessage("§aKamu menerima §e" + amount + " §adari " + player.getName());

            return true;
        }

        // =====================
        // 🔥 ADMIN COMMAND
        // =====================
        if (!player.hasPermission("smoney.admin")) {
            player.sendMessage("§cTidak punya permission!");
            return true;
        }

        if (args.length == 3) {

            Player target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                player.sendMessage("§cPlayer tidak ditemukan!");
                return true;
            }

            double amount;

            try {
                amount = Double.parseDouble(args[2]);
            } catch (Exception e) {
                player.sendMessage("§cJumlah tidak valid!");
                return true;
            }

            switch (args[0].toLowerCase()) {

                case "give":
                    economy.adminGiveMoney(target.getUniqueId(), amount);
                    player.sendMessage("§aBerhasil memberi uang!");
                    break;

                case "take":
                    economy.adminTakeMoney(target.getUniqueId(), amount);
                    player.sendMessage("§cBerhasil mengambil uang!");
                    break;

                case "set":
                    economy.adminSetMoney(target.getUniqueId(), amount);
                    player.sendMessage("§eUang player di-set!");
                    break;

                default:
                    player.sendMessage("§e/money give|take|set <player> <amount>");
            }

            return true;
        }

        player.sendMessage("§e/money | /money pay <player> <amount>");
        return true;
    }
}