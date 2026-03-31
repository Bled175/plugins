package com.yourname.smoney.quest;

import com.yourname.smoney.data.DataManager;
import com.yourname.smoney.economy.CurrencyUtil;
import com.yourname.smoney.economy.EconomyManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.yourname.smoney.scoreboard.ScoreboardManager;

import java.util.*;

public class QuestManager {

    private final Map<String, Quest> quests = new HashMap<>();

    private final DataManager data;
    private final EconomyManager economy;
    private final JavaPlugin plugin;
    private final ScoreboardManager scoreboard;

    public QuestManager(JavaPlugin plugin, DataManager data, EconomyManager economy, ScoreboardManager scoreboard) {
        this.plugin = plugin;
        this.data = data;
        this.economy = economy;
        this.scoreboard = scoreboard;

        loadQuests();
        scheduleResets();
    }
    public Collection<Quest> getAllQuests() {
        return quests.values();
    }
    // ================= LOAD QUEST =================
    public void loadQuests() {

        quests.clear(); // 🔥 penting biar gak numpuk

        loadSection("daily", QuestType.DAILY);
        loadSection("weekly", QuestType.WEEKLY);
        loadSection("global", QuestType.GLOBAL);

        System.out.println("TOTAL QUEST LOADED: " + quests.size());
    }

    private void loadSection(String section, QuestType type) {

        if (data.getQuestConfig().getConfigurationSection(section) == null) {
            System.out.println("❌ SECTION NULL: " + section);
            return;
        }

        for (String id : data.getQuestConfig().getConfigurationSection(section).getKeys(false)) {

            String path = section + "." + id;

            String targetType = data.getQuestConfig().getString(path + ".target");
            int amount = data.getQuestConfig().getInt(path + ".amount");
            double reward = data.getQuestConfig().getDouble(path + ".reward");

            quests.put(id, new Quest(id, type, targetType, amount, reward));

            System.out.println("LOAD QUEST: " + id);

        }
}

    public Quest getQuest(String id) {
        return quests.get(id);
    }

    // ================= ASSIGN DAILY (3-5 QUEST) =================
    public void assignDaily(Player player) {
        UUID uuid = player.getUniqueId();
        String path = "players." + uuid + ".daily";

        List<String> current = data.getConfig().getStringList(path);

        // ✅ FIX: cek kosong, bukan cuma contains
        if (current != null && !current.isEmpty()) return;

        List<Quest> list = new ArrayList<>();

        for (Quest q : quests.values()) {
            if (q.getType() == QuestType.DAILY) {
                list.add(q);
            }
        }

        // 🔥 DEBUG
        System.out.println("AVAILABLE DAILY QUEST: " + list.size());

        if (list.isEmpty()) {
            System.out.println("❌ NO DAILY QUEST LOADED!");
            return;
        }

        Collections.shuffle(list);

        List<String> selected = new ArrayList<>();

        int max = Math.min(5, list.size());

        for (int i = 0; i < max; i++) {
            selected.add(list.get(i).getId());
        }

        data.getConfig().set(path, selected);
        data.save();
    }

    // ================= ASSIGN WEEKLY =================
    public void assignWeekly(Player player) {
        UUID uuid = player.getUniqueId();
        String path = "players." + uuid + ".weekly";

        List<String> current = data.getConfig().getStringList(path);

        if (current != null && !current.isEmpty()) return;

        List<Quest> list = new ArrayList<>();

        for (Quest q : quests.values()) {
            if (q.getType() == QuestType.WEEKLY) {
                list.add(q);
            }
        }

        if (list.isEmpty()) {
            System.out.println("❌ NO WEEKLY QUEST LOADED!");
            return;
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

        // 🔥 STOP kalau sudah selesai (biar gak lebih dari target & gak spam)
        if (current >= quest.getTarget()) return;

        // 🔥 LIMIT PROGRESS (gak bisa lebih dari target)
        int updated = Math.min(current + amount, quest.getTarget());

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

        String uuid = player.getUniqueId().toString();

        String progressPath = "players." + uuid + ".progress." + questId;
        String claimedPath = "players." + uuid + ".claimed." + questId;

        int progress = data.getConfig().getInt(progressPath, 0);

        // ❌ BELUM SELESAI
        if (progress < quest.getTarget()) {
            player.sendMessage("§cQuest belum selesai!");
            return;
        }

        // ❌ SUDAH CLAIM
        if (data.getConfig().getBoolean(claimedPath)) {
            player.sendMessage("§cReward sudah diambil!");
            return;
        }

        // 💰 TAMBAH UANG
        economy.addMoney(player.getUniqueId(), quest.getReward());

        // ✅ SET CLAIMED
        data.getConfig().set(claimedPath, true);
        data.save();

        // 🔥 UPDATE SCOREBOARD (INI YANG BENER)
        scoreboard.updateMoney(player);

        player.sendMessage("§aReward di-claim! +" + CurrencyUtil.format(quest.getReward()));
    }

    // ================= RESET =================
    // ⏰ Schedule automatic resets every 24h (daily) and 7d (weekly)
    private void scheduleResets() {
        plugin.getServer().getScheduler().runTaskTimer(
                plugin, this::resetDaily, 86400 * 20L, 86400 * 20L
        );

        plugin.getServer().getScheduler().runTaskTimer(
                plugin, this::resetWeekly, 604800 * 20L, 604800 * 20L
        );
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