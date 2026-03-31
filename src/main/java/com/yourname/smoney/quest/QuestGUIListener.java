package com.yourname.smoney.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class QuestGUIListener implements Listener {

    private final QuestManager manager;

    public QuestGUIListener(QuestManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        // 🔥 CEK GUI TITLE
        if (!event.getView().getTitle().equals("§8Quest")) return;

        event.setCancelled(true); // biar item gak bisa diambil

        if (event.getCurrentItem() == null) return;
        if (!event.getCurrentItem().hasItemMeta()) return;
        if (!event.getCurrentItem().getItemMeta().hasDisplayName()) return;

        String questId = event.getCurrentItem().getItemMeta().getDisplayName();

        // 🔥 HAPUS WARNA (semua '§x') dan tag seperti [Daily] / [Weekly] / [Global]
        questId = questId.replaceAll("§.", "");
        questId = questId.replaceAll("^\\[[^\\]]+\\]\\s*", "");

        // 🔥 CLAIM QUEST
        manager.claim(player, questId);

        // 🔥 REFRESH GUI
        new QuestGUI(manager).open(player);
    }
}