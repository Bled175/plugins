package com.yourname.smoney.listeners;

import com.yourname.smoney.market.MarketGUI;
import com.yourname.smoney.market.MarketManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Handles clicks in the Market GUI for buying items
 */
public class MarketListener implements Listener {

    private final MarketManager marketManager;

    public MarketListener(MarketManager marketManager) {
        this.marketManager = marketManager;
    }

    @EventHandler
    public void onMarketClick(InventoryClickEvent e) {

        // Only listen to Market GUI
        if (!e.getView().getTitle().contains("Market Page")) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null) return;
        if (e.getCurrentItem().getAmount() == 0) return;

        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();

        // 🛍 Try to buy item
        marketManager.buyFromGUI(player, slot);
    }
}
