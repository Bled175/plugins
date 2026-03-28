package com.yourname.smoney.commands;

import com.yourname.smoney.shop.ShopGUI;
import com.yourname.smoney.shop.ShopManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    private final ShopManager shopManager;

    public ShopCommand(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        new ShopGUI(shopManager).open(player);

        return true;
    }
}