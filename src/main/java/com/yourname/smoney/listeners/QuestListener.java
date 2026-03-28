package com.yourname.smoney.listeners;

import com.yourname.smoney.quest.QuestManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class QuestListener implements Listener {

    private final QuestManager questManager;

    public QuestListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (e.getView().getTitle().equals("§8Quest")) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null) return;
            if (e.getCurrentItem().getItemMeta() == null) return;

            Player player = (Player) e.getWhoClicked();

            String name = e.getCurrentItem().getItemMeta().getDisplayName()
                    .replace("§a", "")
                    .replace("§e", "");

            questManager.claim(player, name);
        }
    }
}