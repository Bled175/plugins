package com.yourname.smoney.quest;

import com.yourname.smoney.economy.CurrencyUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class QuestGUI {

    private final QuestManager manager;

    public QuestGUI(QuestManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {

    // 🔥 FORCE ASSIGN (INI KUNCI)
    manager.assignDaily(player);
    manager.assignWeekly(player);

    Inventory inv = Bukkit.createInventory(null, 27, "§8Quest");

        int slot = 10;

        for (String id : manager.getDaily(player)) {

            Quest q = manager.getQuest(id);
            if (q == null) continue;
            int progress = manager.getProgress(player, id);

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();

            boolean done = progress >= q.getTarget();

            meta.setDisplayName((done ? "§a" : "§e") + id);

            meta.setLore(Arrays.asList(
                    "§7Progress: " + progress + "/" + q.getTarget(),
                    "§7Reward: " + CurrencyUtil.format(q.getReward()),
                    done ? "§aKlik untuk claim!" : "§cBelum selesai"
            ));

            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        player.openInventory(inv);
    }
}