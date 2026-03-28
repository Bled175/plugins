package com.yourname.smoney.shop;

import com.yourname.smoney.economy.CurrencyUtil;
import com.yourname.smoney.economy.EconomyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Listener {

    private final ShopManager shopManager;
    private final EconomyManager economy;

    public ShopListener(ShopManager shopManager, EconomyManager economy) {
        this.shopManager = shopManager;
        this.economy = economy;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (e.getView().getTitle().equals("§8Shop")) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null) return;

            Player player = (Player) e.getWhoClicked();
            ItemStack clicked = e.getCurrentItem();

            for (ShopItem shopItem : shopManager.getItems().values()) {
                ItemStack stack = shopItem.getItem();

                if (clicked.getType() == stack.getType()) {

                    double money = economy.getMoney(player.getUniqueId());

                    if (money < shopItem.getPrice()) {
                        player.sendMessage("§cUang tidak cukup!");
                        return;
                    }

                    economy.removeMoney(player.getUniqueId(), shopItem.getPrice());

                    player.getInventory().addItem(stack.clone());

                    String name = (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName())
                            ? stack.getItemMeta().getDisplayName()
                            : stack.getType().toString();

                    player.sendMessage("§aBerhasil membeli §e" + name);
                    player.sendMessage("§7Sisa saldo: " + CurrencyUtil.format(economy.getMoney(player.getUniqueId())));

                    return;
                }
            }
        }
    }
}