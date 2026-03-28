package com.yourname.smoney.market;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MarketGUI implements Listener {

    private final MarketManager manager;

    // page data
    private final Map<UUID, Integer> pageMap = new HashMap<>();
    private final Map<UUID, Map<Integer, String>> slotMap = new HashMap<>();

    public MarketGUI(MarketManager manager) {
        this.manager = manager;
    }

    // =====================
    // OPEN GUI (PAGE)
    // =====================
    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {

        Inventory inv = Bukkit.createInventory(null, 54, "§6Market Page " + (page + 1));

        List<MarketItem> items = new ArrayList<>(manager.getItems().values());

        int start = page * 45; // 45 slot item (sisanya tombol)
        int end = Math.min(start + 45, items.size());

        Map<Integer, String> map = new HashMap<>();

        int slot = 0;

        for (int i = start; i < end; i++) {

            MarketItem item = items.get(i);

            ItemStack stack = item.getItem().clone();
            ItemMeta meta = stack.getItemMeta();

            meta.setDisplayName("§e$" + item.getPrice());

            List<String> lore = new ArrayList<>();
            lore.add("§7Klik untuk beli");
            meta.setLore(lore);

            stack.setItemMeta(meta);

            inv.setItem(slot, stack);
            map.put(slot, item.getId());

            slot++;
        }

        // NAV BUTTON
        if (end < items.size()) {
            inv.setItem(53, createNav("§aNext Page"));
        }

        if (page > 0) {
            inv.setItem(45, createNav("§cPrevious Page"));
        }

        slotMap.put(player.getUniqueId(), map);
        pageMap.put(player.getUniqueId(), page);

        player.openInventory(inv);
    }

    // =====================
    // CLICK EVENT
    // =====================
    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!e.getView().getTitle().contains("§6Market Page")) return;

        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();

        int slot = e.getRawSlot();

        int page = pageMap.getOrDefault(player.getUniqueId(), 0);

        // NAVIGATION
        if (slot == 53) {
            open(player, page + 1);
            return;
        }

        if (slot == 45 && page > 0) {
            open(player, page - 1);
            return;
        }

        Map<Integer, String> map = slotMap.get(player.getUniqueId());

        if (map == null) return;

        String id = map.get(slot);

        if (id == null) return;

        manager.buy(player, id);
    }

    // =====================
    // CLEANUP
    // =====================
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        slotMap.remove(uuid);
        pageMap.remove(uuid);
    }

    // =====================
    // NAV ITEM
    // =====================
    private ItemStack createNav(String name) {
        ItemStack item = new ItemStack(org.bukkit.Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}