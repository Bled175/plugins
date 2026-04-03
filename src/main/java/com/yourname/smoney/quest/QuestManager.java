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

    public Collection<Quest> getAllQuests() {
        return quests.values();
    }

    // ================= LOAD =================
    public void loadQuests() {
        quests.clear();

        loadSection("daily", QuestType.DAILY);
        loadSection("weekly", QuestType.WEEKLY);
        loadSection("global", QuestType.GLOBAL);

        plugin.getLogger().info("TOTAL QUEST LOADED: " + quests.size());
    }

    private void loadSection(String section, QuestType type) {
        if (data.getQuestConfig().getConfigurationSection(section) == null) return;

        for (String id : data.getQuestConfig().getConfigurationSection(section).getKeys(false)) {

            String path = section + "." + id;

            String targetType = data.getQuestConfig().getString(path + ".target");
            int amount = data.getQuestConfig().getInt(path + ".amount");
            double reward = data.getQuestConfig().getDouble(path + ".reward");

            quests.put(id, new Quest(id, type, targetType, amount, reward));
        }
    }

    public Quest getQuest(String id) {
        return quests.get(id);
    }

    // ================= ASSIGN =================
    public void assignDaily(Player player) {
        assign(player, QuestType.DAILY, 3, 5);
    }

    public void assignWeekly(Player player) {
        assign(player, QuestType.WEEKLY, 5, 7);
    }

    private void assign(Player player, QuestType type, int min, int max) {

        UUID uuid = player.getUniqueId();
        String path = "players." + uuid + "." + type.name().toLowerCase();

        List<String> current = data.getConfig().getStringList(path);
        if (current != null && !current.isEmpty()) return;

        List<Quest> list = new ArrayList<>();

        for (Quest q : quests.values()) {
            if (q.getType() == type) list.add(q);
        }

        if (list.isEmpty()) return;

        Collections.shuffle(list);

        int maxAvailable = Math.min(max, list.size());
        int count = new Random().nextInt(maxAvailable - min + 1) + min;

        List<String> selected = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            selected.add(list.get(i).getId());
        }

        data.getConfig().set(path, selected);
        data.save();
    }

    // ================= GET =================
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

        String uuid = player.getUniqueId().toString();
        String type = quest.getType().name().toLowerCase();

        String path = "players." + uuid + ".progress." + type + "." + questId;

        int current = data.getConfig().getInt(path, 0);
        if (current >= quest.getTarget()) return;

        int updated = Math.min(current + amount, quest.getTarget());

        data.getConfig().set(path, updated);
        data.save();

        player.sendMessage("§eProgress " + questId + ": " + updated + "/" + quest.getTarget());
    }

    public int getProgress(Player player, String questId) {

        Quest quest = quests.get(questId);
        if (quest == null) return 0;

        String type = quest.getType().name().toLowerCase();

        return data.getConfig().getInt(
                "players." + player.getUniqueId() + ".progress." + type + "." + questId,
                0
        );
    }

    // ================= CLAIM =================
    public void claim(Player player, String questId) {

        Quest quest = quests.get(questId);
        if (quest == null) return;

        String uuid = player.getUniqueId().toString();
        String type = quest.getType().name().toLowerCase();

        String progressPath = "players." + uuid + ".progress." + type + "." + questId;
        String claimedPath = "players." + uuid + ".claimed." + type + "." + questId;

        int progress = data.getConfig().getInt(progressPath, 0);

        if (progress < quest.getTarget()) {
            player.sendMessage("§cQuest belum selesai!");
            return;
        }

        // ================= GLOBAL =================
        if (quest.getType() == QuestType.GLOBAL) {

            String globalPath = "global.claimed." + questId;

            if (data.getConfig().getBoolean(globalPath)) {
                player.sendMessage("§cQuest global sudah diselesaikan!");
                return;
            }

            data.getConfig().set(globalPath, true);
            data.save(); // ✅ penting

            Bukkit.broadcastMessage("§6[GLOBAL] §e" + player.getName() + " menyelesaikan quest " + questId + "!");
        }

        // ================= NON GLOBAL =================
        if (quest.getType() != QuestType.GLOBAL) {

            if (data.getConfig().getBoolean(claimedPath)) {
                player.sendMessage("§cReward sudah diambil!");
                return;
            }

            data.getConfig().set(claimedPath, true);
        }

        economy.addMoney(player.getUniqueId(), quest.getReward());
        data.save();

        scoreboard.updateMoney(player);

        player.sendMessage("§aReward di-claim! +" + CurrencyUtil.format(quest.getReward()));
    }

    // ================= RESET =================
    private void scheduleResets() {

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            Calendar now = Calendar.getInstance();

            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);
            int day = now.get(Calendar.DAY_OF_WEEK);

            if (hour == 3 && minute == 0) {

                long lastDaily = data.getConfig().getLong("lastReset.daily", 0);
                long lastWeekly = data.getConfig().getLong("lastReset.weekly", 0);

                long today = getToday();

                // DAILY
                if (lastDaily < today) {
                    resetDaily();
                    plugin.getLogger().info("Daily reset!");
                }

                // WEEKLY (Senin)
                if (day == Calendar.MONDAY && lastWeekly < today) {
                    resetWeekly();
                    plugin.getLogger().info("Weekly reset!");
                }
            }

        }, 0L, 1200L);
    }

    private long getToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public void resetDaily() {

        if (data.getConfig().getConfigurationSection("players") == null) return;

        for (String uuid : data.getConfig().getConfigurationSection("players").getKeys(false)) {
            data.getConfig().set("players." + uuid + ".daily", null);
            data.getConfig().set("players." + uuid + ".progress.daily", null);
            data.getConfig().set("players." + uuid + ".claimed.daily", null);
        }

        data.getConfig().set("lastReset.daily", System.currentTimeMillis());
        data.save();

        for (Player p : Bukkit.getOnlinePlayers()) {
            assignDaily(p);
        }
    }

    public void resetWeekly() {

        if (data.getConfig().getConfigurationSection("players") == null) return;

        for (String uuid : data.getConfig().getConfigurationSection("players").getKeys(false)) {
            data.getConfig().set("players." + uuid + ".weekly", null);
            data.getConfig().set("players." + uuid + ".progress.weekly", null);
            data.getConfig().set("players." + uuid + ".claimed.weekly", null);
        }

        data.getConfig().set("lastReset.weekly", System.currentTimeMillis());
        data.save();

        for (Player p : Bukkit.getOnlinePlayers()) {
            assignWeekly(p);
        }
    }
}