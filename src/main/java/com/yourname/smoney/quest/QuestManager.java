package com.yourname.smoney.quest;

import com.yourname.smoney.data.DataManager;
import com.yourname.smoney.economy.CurrencyUtil;
import com.yourname.smoney.economy.EconomyManager;
import com.yourname.smoney.scoreboard.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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

    // ================= LOAD =================
    public void loadQuests() {
        quests.clear();

        loadSection("daily", QuestType.DAILY);
        loadSection("weekly", QuestType.WEEKLY);
        loadSection("global", QuestType.GLOBAL);

        plugin.getLogger().info("QUEST LOADED: " + quests.size());
    }

    // ================= GLOBAL CURRENT =================
    public Quest getGlobalQuest() {
        String current = data.getQuestConfig().getString("global.current", null);
        if (current != null && getQuest(current, QuestType.GLOBAL) != null) {
            return getQuest(current, QuestType.GLOBAL);
        }

        // pick random global if not set or invalid
        List<Quest> pool = new ArrayList<>();
        for (Quest q : quests.values()) if (q.getType() == QuestType.GLOBAL) pool.add(q);
        if (pool.isEmpty()) return null;

        Collections.shuffle(pool);
        Quest pick = pool.get(0);
        data.getQuestConfig().set("global.current", pick.getId());
        // persist quest config if backing file is writable via DataManager
        data.saveQuestConfig();
        return pick;
    }

    private void loadSection(String section, QuestType type) {
        if (data.getQuestConfig().getConfigurationSection(section) == null) return;

        for (String id : data.getQuestConfig().getConfigurationSection(section).getKeys(false)) {

            String path = section + "." + id;

            String targetType = data.getQuestConfig().getString(path + ".target");
            int target = data.getQuestConfig().getInt(path + ".amount");
            double reward = data.getQuestConfig().getDouble(path + ".reward");

            quests.put(type.name() + ":" + id,
                    new Quest(id, type, targetType, target, reward));
        }
    }

    // ================= GET =================
    public Quest getQuest(String id, QuestType type) {
        return quests.get(type.name() + ":" + id);
    }

    public Collection<Quest> getAllQuests() {
        return quests.values();
    }

    // ================= PROGRESS =================
    public int getProgress(Player player, String questId, QuestType type) {
        return data.getConfig().getInt(progressPath(player, questId, type), 0);
    }

    public void addProgress(Player player, String questId, QuestType type, int amount) {

        Quest quest = getQuest(questId, type);
        if (quest == null) return;

        String path = progressPath(player, questId, type);

        int current = data.getConfig().getInt(path, 0);

        if (current >= quest.getTarget()) return;

        data.getConfig().set(path, Math.min(current + amount, quest.getTarget()));
        data.save();
    }

    // ================= CLAIM =================
    public void claim(Player player, String questId, QuestType type) {

        Quest quest = getQuest(questId, type);
        if (quest == null) return;

        String progressPath = progressPath(player, questId, type);
        String claimedPath = claimedPath(player, questId, type);

        int progress = data.getConfig().getInt(progressPath, 0);

        if (progress < quest.getTarget()) {
            player.sendMessage("§cQuest belum selesai!");
            return;
        }

        // GLOBAL quests are exclusive: only one player can claim each global quest
        if (type == QuestType.GLOBAL) {
            String globalClaimedPath = "global.claimed." + questId;
            String claimer = data.getConfig().getString(globalClaimedPath, null);
            if (claimer != null && !claimer.isEmpty()) {
                player.sendMessage("§cQuest global sudah di-claim!");
                return;
            }

            data.getConfig().set(globalClaimedPath, player.getUniqueId().toString());
        } else {
            if (data.getConfig().getBoolean(claimedPath)) {
                player.sendMessage("§cSudah di-claim!");
                return;
            }

            data.getConfig().set(claimedPath, true);
        }

        economy.addMoney(player.getUniqueId(), quest.getReward());
        data.save();

        scoreboard.updateMoney(player);

        player.sendMessage("§aReward +" + CurrencyUtil.format(quest.getReward()));
    }

    // ================= ASSIGN IF MISSING =================
    public void assignIfMissing(Player player) {
        List<String> daily = getDaily(player);
        if (daily == null || daily.isEmpty()) assignDaily(player);

        List<String> weekly = getWeekly(player);
        if (weekly == null || weekly.isEmpty()) assignWeekly(player);
    }

    // ================= ASSIGN =================
    private void assign(Player player, QuestType type, int amount) {

        List<Quest> pool = new ArrayList<>();

        for (Quest q : quests.values()) {
            if (q.getType() == type) pool.add(q);
        }

        Collections.shuffle(pool);

        List<String> selected = new ArrayList<>();

        for (Quest q : pool) {
            if (selected.size() >= amount) break;
            selected.add(q.getId());
        }

        data.getConfig().set(basePath(player, type), selected);
        data.save();
    }

    public void assignDaily(Player player) {
        assign(player, QuestType.DAILY, 5);
    }

    public void assignWeekly(Player player) {
        assign(player, QuestType.WEEKLY, 7);
    }

    public List<String> getDaily(Player player) {
        return data.getConfig().getStringList(basePath(player, QuestType.DAILY));
    }

    public List<String> getWeekly(Player player) {
        return data.getConfig().getStringList(basePath(player, QuestType.WEEKLY));
    }

    // ================= RESET (FIXED PATH CONSISTENCY) =================
    public void resetDaily() {

        var section = data.getConfig().getConfigurationSection("players");
        if (section == null) return;

        for (String uuid : section.getKeys(false)) {

            data.getConfig().set("players." + uuid + ".daily", null);
            data.getConfig().set("players." + uuid + ".progress.daily", null);
            data.getConfig().set("players." + uuid + ".claimed.daily", null);
        }

        data.save();

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                assignDaily(p);
            }
        });
    }

    public void resetWeekly() {

        var section = data.getConfig().getConfigurationSection("players");
        if (section == null) return;

        for (String uuid : section.getKeys(false)) {

            data.getConfig().set("players." + uuid + ".weekly", null);
            data.getConfig().set("players." + uuid + ".progress.weekly", null);
            data.getConfig().set("players." + uuid + ".claimed.weekly", null);
        }

        data.save();

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                assignWeekly(p);
            }
        });
    }

    // ================= PATH =================
    private String basePath(Player p, QuestType type) {
        return "players." + p.getUniqueId() + "." + type.name().toLowerCase();
    }

    private String progressPath(Player p, String id, QuestType type) {
        return "players." + p.getUniqueId() + ".progress." + type.name().toLowerCase() + "." + id;
    }

    private String claimedPath(Player p, String id, QuestType type) {
        return "players." + p.getUniqueId() + ".claimed." + type.name().toLowerCase() + "." + id;
    }

    // ================= SCHEDULER =================
    private void scheduleResets() {

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            Calendar now = Calendar.getInstance();

            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);

            if (hour == 3 && minute == 0) {

                resetDaily();

                if (now.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                    resetWeekly();
                }
            }

        }, 20L, 1200L);
    }

    // ================= RESET PER PLAYER =================
    public void resetForPlayer(org.bukkit.entity.Player p) {
        String uuid = p.getUniqueId().toString();

        data.getConfig().set("players." + uuid + ".daily", null);
        data.getConfig().set("players." + uuid + ".weekly", null);
        data.getConfig().set("players." + uuid + ".progress.daily", null);
        data.getConfig().set("players." + uuid + ".progress.weekly", null);
        data.getConfig().set("players." + uuid + ".claimed.daily", null);
        data.getConfig().set("players." + uuid + ".claimed.weekly", null);

        data.save();
    }
}