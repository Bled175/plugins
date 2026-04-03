package com.yourname.smoney.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class QuestGUIListener implements Listener {

    private final QuestManager manager;

    public QuestGUIListener(QuestManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if (!event.getView().getTitle().equals("§8§lQUEST MENU")) return;

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        String display = item.getItemMeta().getDisplayName();
        display = display.replaceAll("§.", "");

        // ambil ID dari lore (lebih aman)
        if (item.getItemMeta().getLore() == null) return;

        String idLine = item.getItemMeta().getLore().get(0);
        if (!idLine.startsWith("§7ID:")) return;

        String questId = idLine.replace("§7ID:", "").trim();

        // detect type
        QuestType type = null;

        for (String line : item.getItemMeta().getLore()) {
            if (line.contains("DAILY")) type = QuestType.DAILY;
            if (line.contains("WEEKLY")) type = QuestType.WEEKLY;
        }

        if (type == null) return;

        manager.claim(player, questId, type);

        new QuestGUI(manager).open(player);
    }
}