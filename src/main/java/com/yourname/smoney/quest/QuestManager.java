package com.yourname.smoney.quest;

import com.yourname.smoney.data.DataManager;
import com.yourname.smoney.economy.CurrencyUtil;
import com.yourname.smoney.economy.EconomyManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class QuestManager {

    private final Map<String, Quest> quests = new HashMap<>();

    private final DataManager data;
    private final EconomyManager economy;
    private final JavaPlugin plugin;

    public QuestManager(JavaPlugin plugin, DataManager data, EconomyManager economy) {
        this.plugin = plugin;
        this.data = data;
        this.economy = economy;
        
        // 🎯 Load all quests from quests.yml
        loadQuests();
        
        // ⏰ Start reset timers
        scheduleResets();
    }

    // ================= LOAD QUEST =================
public void loadQuests() {

    loadSection("daily", QuestType.DAILY);
    loadSection("weekly", QuestType.WEEKLY);
    loadSection("global", QuestType.GLOBAL);
}

private void loadSection(String section, QuestType type) {

    if (data.getQuestConfig().getConfigurationSection(section) == null) return;

    for (String id : data.getQuestConfig().getConfigurationSection(section).getKeys(false)) {

        String path = section + "." + id;

        int amount = data.getQuestConfig().getInt(path + ".amount");
        double reward = data.getQuestConfig().getDouble(path + ".reward");

        quests.put(id, new Quest(id, type, amount, reward));
    }
}

    public Quest getQuest(String id) {
        return quests.get(id);
    }

    // ================= ASSIGN DAILY (3-5 QUEST) =================
    public void assignDaily(Player player) {
        UUID uuid = player.getUniqueId();
        String path = "players." + uuid + ".daily";

        if (data.getConfig().contains(path)) return;

        List<Quest> list = new ArrayList<>();

        for (Quest q : quests.values()) {
            if (q.getType() == QuestType.DAILY) {
                list.add(q);
            }
        }

        Collections.shuffle(list);

        int count = Math.min(5, Math.max(3, list.size()));

        List<String> selected = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            selected.add(list.get(i).getId());
        }

        data.getConfig().set(path, selected);
        data.save();
    }

    // ================= ASSIGN WEEKLY =================
    public void assignWeekly(Player player) {
        UUID uuid = player.getUniqueId();
        String path = "players." + uuid + ".weekly";

        if (data.getConfig().contains(path)) return;

        List<Quest> list = new ArrayList<>();

        for (Quest q : quests.values()) {
            if (q.getType() == QuestType.WEEKLY) {
                list.add(q);
            }
        }

        Collections.shuffle(list);

        List<String> selected = new ArrayList<>();

        for (int i = 0; i < Math.min(7, list.size()); i++) {
            selected.add(list.get(i).getId());
        }

        data.getConfig().set(path, selected);
        data.save();
    }

    // ================= GET QUEST LIST =================
    public List<String> getDaily(Player player) {
        return data.getConfig().getStringList("players." + player.getUniqueId() + ".daily");
    }

    public List<String> getWeekly(Player player) {
        return data.getConfig().getStringList("players." + player.getUniqueId() + ".weekly");
    }

    // ================= PROGRESS =================
    public void addProgress(Player player, String questId, int amount) {

        Quest quest = quests.get(questId);
        if (quest == null) return;

        String path = "players." + player.getUniqueId() + ".progress." + questId;

        int current = data.getConfig().getInt(path, 0);
        int updated = current + amount;

        data.getConfig().set(path, updated);
        data.save();

        player.sendMessage("§eProgress " + questId + ": " + updated + "/" + quest.getTarget());
    }

    public int getProgress(Player player, String questId) {
        return data.getConfig().getInt(
                "players." + player.getUniqueId() + ".progress." + questId,
                0
        );
    }

    // ================= CLAIM =================
    public void claim(Player player, String questId) {

        Quest quest = quests.get(questId);
        if (quest == null) return;

        String progressPath = "players." + player.getUniqueId() + ".progress." + questId;
        int progress = data.getConfig().getInt(progressPath, 0);

        if (progress < quest.getTarget()) {
            player.sendMessage("§cQuest belum selesai!");
            return;
        }

        String claimedPath = "players." + player.getUniqueId() + ".claimed." + questId;

        if (data.getConfig().getBoolean(claimedPath)) {
            player.sendMessage("§cReward sudah diambil!");
            return;
        }

        economy.addMoney(player.getUniqueId(), quest.getReward());

        data.getConfig().set(claimedPath, true);
        data.save();

        player.sendMessage("§aReward di-claim! +" + CurrencyUtil.format(quest.getReward()));
    }

    // ================= RESET =================
    // ⏰ Schedule automatic resets every 24h (daily) and 7d (weekly)
    private void scheduleResets() {
        // Daily reset every 24 hours (20 ticks/second * 60 * 60 * 24)
        plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(
                plugin, this::resetDaily, 86400 * 20L, 86400 * 20L);

        // Weekly reset every 7 days
        plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(
                plugin, this::resetWeekly, 604800 * 20L, 604800 * 20L);
    }
    
    public void resetDaily() {
        if (data.getConfig().getConfigurationSection("players") == null) return;

        for (String uuid : data.getConfig().getConfigurationSection("players").getKeys(false)) {
            data.getConfig().set("players." + uuid + ".daily", null);
            data.getConfig().set("players." + uuid + ".progress", null);
            data.getConfig().set("players." + uuid + ".claimed", null);
        }

        data.getConfig().set("lastReset.daily", System.currentTimeMillis());
        data.save();
    }

    public void resetWeekly() {
        data.getConfig().set("lastReset.weekly", System.currentTimeMillis());
        data.save();
    }

    public void checkReset() {
        long now = System.currentTimeMillis();

        long lastDaily = data.getConfig().getLong("lastReset.daily", 0);
        long lastWeekly = data.getConfig().getLong("lastReset.weekly", 0);

        if (now - lastDaily >= 86400000) {
            resetDaily();
        }

        if (now - lastWeekly >= 604800000) {
            resetWeekly();
        }
    }
}