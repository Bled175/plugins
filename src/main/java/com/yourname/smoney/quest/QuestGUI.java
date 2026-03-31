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

        manager.assignDaily(player);
        manager.assignWeekly(player);

        System.out.println("DAILY QUEST: " + manager.getDaily(player));

        Inventory inv = Bukkit.createInventory(null, 27, "§8Quest");

        int slot = 10;

        // ================= DAILY =================
        for (String id : manager.getDaily(player)) {

            Quest q = manager.getQuest(id);
            if (q == null) continue;

            int progress = manager.getProgress(player, id);

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            boolean done = progress >= q.getTarget();

            meta.setDisplayName((done ? "§a" : "§e") + "[Daily] " + id);
            meta.setLore(Arrays.asList(
                    "§7Progress: " + progress + "/" + q.getTarget(),
                    "§7Reward: " + CurrencyUtil.format(q.getReward())
            ));

            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        // ================= WEEKLY =================
        for (String id : manager.getWeekly(player)) {

            Quest q = manager.getQuest(id);
            if (q == null) continue;

            int progress = manager.getProgress(player, id);

            ItemStack item = new ItemStack(Material.BOOK);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            meta.setDisplayName("§b[Weekly] " + id);
            meta.setLore(Arrays.asList(
                    "§7Progress: " + progress + "/" + q.getTarget(),
                    "§7Reward: " + CurrencyUtil.format(q.getReward())
            ));

            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        // ================= GLOBAL =================
        for (Quest q : manager.getAllQuests()) {

            if (q.getType() != QuestType.GLOBAL) continue;

            ItemStack item = new ItemStack(Material.EMERALD);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            meta.setDisplayName("§a[Global] " + q.getId());
            meta.setLore(Arrays.asList(
                    "§7Target: " + q.getTarget(),
                    "§7Reward: " + CurrencyUtil.format(q.getReward())
            ));

            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        player.openInventory(inv);
    }
}