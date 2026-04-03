package com.yourname.smoney.listeners;

import com.yourname.smoney.quest.Quest;
import com.yourname.smoney.quest.QuestManager;
import com.yourname.smoney.quest.QuestType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class QuestListener implements Listener {

    private final QuestManager questManager;

    public QuestListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        // match the GUI title used by QuestGUI
        if (!"§8§lQUEST MENU".equals(e.getView().getTitle())) return;

        e.setCancelled(true);

        ItemStack current = e.getCurrentItem();
        if (current == null) return;

        ItemMeta meta = current.getItemMeta();
        if (meta == null) return;

        Player player = (Player) e.getWhoClicked();

        List<String> lore = meta.getLore();
        if (lore == null || lore.size() < 2) return;

        // lore format expected: ["§7ID:<id>", "§7TYPE: <TYPE>", ...]
        String idLine = lore.get(0);
        String typeLine = lore.get(1);

        if (!idLine.contains("ID:") || !typeLine.contains("TYPE:")) return;

        String questId = idLine.substring(idLine.indexOf("ID:") + 3).trim();
        String typeStr = typeLine.substring(typeLine.indexOf("TYPE:") + 5).trim();

        QuestType type;
        try {
            type = QuestType.valueOf(typeStr.toUpperCase());
        } catch (Exception ex) {
            return;
        }

        questManager.claim(player, questId, type);
    }
}