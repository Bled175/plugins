package com.yourname.smoney.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ShopListener implements Listener {

    private final ShopManager manager;

    public ShopListener(ShopManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        String title = e.getView().getTitle();

        // =====================
        // SHOP GUI
        // =====================
        if (title.contains("§8Shop Page")) {

            e.setCancelled(true);

            if (!(e.getWhoClicked() instanceof Player)) return;
            if (e.getCurrentItem() == null) return;

            Player player = (Player) e.getWhoClicked();

            for (String id : manager.getItems().keySet()) {

                ShopItem item = manager.getItem(id);
                if (item == null) continue;

                if (item.getItem().getType() == e.getCurrentItem().getType()) {

                    if (player.hasPermission("smoney.admin") && e.isRightClick()) {
                        new ShopAdminGUI(manager, id).open(player);
                        return;
                    }

                    manager.buy(player, id);
                    return;
                }
            }
        }

        // =====================
        // ADMIN GUI
        // =====================
        if (title.contains("§cManage Item")) {

            e.setCancelled(true);

            Player player = (Player) e.getWhoClicked();

            String id = manager.getLastOpenedItem(player);
            if (id == null) return;

            new ShopAdminGUI(manager, id).handleClick(player, e.getRawSlot());
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        manager.handleChat(e.getPlayer(), e.getMessage());
    }
}