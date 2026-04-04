package com.yourname.smoney.quest;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.yourname.smoney.economy.CurrencyUtil;

public class QuestGUI {

    private final QuestManager manager;

    public QuestGUI(QuestManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {

        manager.assignIfMissing(player);

        Inventory inv = Bukkit.createInventory(null, 27, "§8§lQUEST MENU");

        int[] dailySlots = {10,11,12,13,14};
        int[] weeklySlots = {15,16,17,18,19};
        int globalSlot = 22;

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        gm.setDisplayName(" ");
        glass.setItemMeta(gm);

        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        List<String> daily = manager.getDaily(player);
        List<String> weekly = manager.getWeekly(player);
        Quest global = manager.getGlobalQuest();

        int d = 0;
        int w = 0;

        // ================= DAILY =================
        for (String id : daily) {

            if (d >= dailySlots.length) break;

            Quest q = manager.getQuest(id, QuestType.DAILY);
            if (q == null) continue;

            int prog = manager.getProgress(player, id, QuestType.DAILY);
            boolean done = prog >= q.getTarget();

            ItemStack it = new ItemStack(Material.PAPER);
            ItemMeta m = it.getItemMeta();

            m.setDisplayName((done ? "§a✔ " : "§e✦ ") + q.getDescription());
            m.setLore(Arrays.asList(
                    "§7ID:" + id,
                    "§7" + q.getTargetType() + ": " + q.getTarget(),
                    "§7TYPE: DAILY",
                    "§7Progress: " + prog + "/" + q.getTarget(),
                    "§7Reward: " + CurrencyUtil.format(q.getReward()),
                    done ? "§aREADY TO CLAIM" : "§eIN PROGRESS"
            ));

            it.setItemMeta(m);
            inv.setItem(dailySlots[d++], it);
        }

        // ================= WEEKLY =================
        for (String id : weekly) {

            if (w >= weeklySlots.length) break;

            Quest q = manager.getQuest(id, QuestType.WEEKLY);
            if (q == null) continue;

            int prog = manager.getProgress(player, id, QuestType.WEEKLY);
            boolean done = prog >= q.getTarget();

            ItemStack it = new ItemStack(Material.BOOK);
            ItemMeta m = it.getItemMeta();

            m.setDisplayName((done ? "§a✔ " : "§b✦ ") + q.getDescription());
            m.setLore(Arrays.asList(
                    "§7ID:" + id,
                    "§7" + q.getTargetType() + ": " + q.getTarget(),
                    "§7TYPE: WEEKLY",
                    "§7Progress: " + prog + "/" + q.getTarget(),
                    "§7Reward: " + CurrencyUtil.format(q.getReward())
            ));

            it.setItemMeta(m);
            inv.setItem(weeklySlots[w++], it);
        }

        // ================= GLOBAL =================
        if (global != null) {
            int prog = manager.getProgress(player, global.getId(), QuestType.GLOBAL);
            boolean done = prog >= global.getTarget();

            ItemStack it = new ItemStack(Material.EMERALD);
            ItemMeta m = it.getItemMeta();

            m.setDisplayName((done ? "§a✔ " : "§6✦ ") + global.getDescription());
            m.setLore(Arrays.asList(
                    "§7ID:" + global.getId(),
                    "§7" + global.getTargetType() + ": " + global.getTarget(),
                    "§7TYPE: GLOBAL",
                    "§7Progress: " + prog + "/" + global.getTarget(),
                    "§7Reward: " + CurrencyUtil.format(global.getReward()),
                    done ? "§aREADY TO CLAIM" : "§eIN PROGRESS"
            ));

            it.setItemMeta(m);
            inv.setItem(globalSlot, it);
        }

        player.openInventory(inv);
    }
}