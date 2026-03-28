package com.yourname.smoney.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Listener {

    private final ShopManager shopManager;

    public ShopListener(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!e.getView().getTitle().equals("§8Shop")) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null) return;

        Player player = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();

        // 🔥 PAKAI ID SYSTEM (WAJIB)
        for (String id : shopManager.getItems().keySet()) {

            ShopItem shopItem = shopManager.getItem(id);
            ItemStack stack = shopItem.getItem();

            // compare item type (basic)
            if (clicked.getType() == stack.getType()) {

                // 🔥 SEMUA LOGIC DI SINI
                shopManager.buy(player, id);
                return;
            }
        }
    }
}