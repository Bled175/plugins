package com.yourname.smoney.market;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MarketMyGUI implements Listener {

    private final MarketManager manager;
    private final Map<UUID, Map<Integer, String>> slotMap = new HashMap<>();

    public MarketMyGUI(MarketManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {

        Inventory inv = Bukkit.createInventory(null, 54, "§6My Market Items");

        List<MarketItem> items = manager.getItemsBySeller(player.getUniqueId());

        Map<Integer, String> map = new HashMap<>();

        int slot = 0;

        for (MarketItem item : items) {

            ItemStack stack = item.getItem().clone();
            ItemMeta meta = stack.getItemMeta();

            meta.setDisplayName("§e$" + item.getPrice());

            List<String> lore = new ArrayList<>();
            lore.add("§7Klik untuk ambil kembali");
            lore.add("§8ID: " + item.getId());

            meta.setLore(lore);
            stack.setItemMeta(meta);

            inv.setItem(slot, stack);
            map.put(slot, item.getId());

            slot++;
        }

        slotMap.put(player.getUniqueId(), map);
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!e.getView().getTitle().equals("§6My Market Items")) return;

        // ❗ FIX PENTING
        if (e.getClickedInventory() == null) return;
        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();

        Map<Integer, String> map = slotMap.get(player.getUniqueId());
        if (map == null) return;

        String id = map.get(e.getSlot()); // 🔥 bukan rawSlot
        if (id == null) return;

        boolean success = manager.undo(player, id);

        if (success) {
            Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> open(player), 1L);
        }
    }
}