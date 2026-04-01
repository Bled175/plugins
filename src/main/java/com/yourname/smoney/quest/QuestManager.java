package com.yourname.smoney.quest;

import com.yourname.smoney.data.DataManager;
import com.yourname.smoney.economy.CurrencyUtil;
import com.yourname.smoney.economy.EconomyManager;
import com.yourname.smoney.scoreboard.ScoreboardManager;
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

    // ================= LOAD QUEST =================
    public void loadQuests() {

        quests.clear();

        loadSection("daily", QuestType.DAILY);
        loadSection("weekly", QuestType.WEEKLY);
        loadSection("global", QuestType.GLOBAL);

        plugin.getLogger().info("TOTAL QUEST LOADED: " + quests.size());
    }

    private void loadSection(String section, QuestType type) {

        if (data.getQuestConfig().getConfigurationSection(section) == null) {
            plugin.getLogger().warning("SECTION NULL: " + section);
            return;
        }

        for (String id : data.getQuestConfig().getConfigurationSection(section).getKeys(false)) {

            String path = section + "." + id;

            String targetType = data.getQuestConfig().getString(path + ".target");
            int amount = data.getQuestConfig().getInt(path + ".amount");
            double reward = data.getQuestConfig().getDouble(path + ".reward");

            quests.put(id, new Quest(id, type, targetType, amount, reward));

            plugin.getLogger().info("LOAD QUEST: " + id);
        }
    }

    public Quest getQuest(String id) {
        return quests.get(id);
    }

    // ================= ASSIGN DAILY =================
    public void assignDaily(Player player) {
        UUID uuid = player.getUniqueId();
        String path = "players." + uuid + ".daily";

        List<String> current = data.getConfig().getStringList(path);
        if (current != null && !current.isEmpty()) return;

        List<Quest> list = new ArrayList<>();

        for (Quest q : quests.values()) {
            if (q.getType() == QuestType.DAILY) {
                list.add(q);
            }
        }

        if (list.isEmpty()) {
            plugin.getLogger().warning("NO DAILY QUEST LOADED!");
            return;
        }

        Collections.shuffle(list);

        int min = Math.min(3, list.size());
        int max = Math.min(5, list.size());
        int count = new Random().nextInt(max - min + 1) + min;

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

        List<String> current = data.getConfig().getStringList(path);
        if (current != null && !current.isEmpty()) return;

        List<Quest> list = new ArrayList<>();

        for (Quest q : quests.values()) {
            if (q.getType() == QuestType.WEEKLY) {
                list.add(q);
            }
        }

        if (list.isEmpty()) {
            plugin.getLogger().warning("NO WEEKLY QUEST LOADED!");
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

    // ================= GET QUEST =================
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

        if (current >= quest.getTarget()) return;

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

        if (progress < quest.getTarget()) {
            player.sendMessage("§cQuest belum selesai!");
            return;
        }

        if (data.getConfig().getBoolean(claimedPath)) {
            player.sendMessage("§cReward sudah diambil!");
            return;
        }

        economy.addMoney(player.getUniqueId(), quest.getReward());

        data.getConfig().set(claimedPath, true);
        data.save();

        scoreboard.updateMoney(player);

        player.sendMessage("§aReward di-claim! +" + CurrencyUtil.format(quest.getReward()));
    }

    // ================= RESET SYSTEM =================
    private void scheduleResets() {

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            Calendar now = Calendar.getInstance();

            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);
            int day = now.get(Calendar.DAY_OF_WEEK);

            if (hour == 3 && minute == 0) {

                long lastDaily = data.getConfig().getLong("lastReset.daily", 0);
                long lastWeekly = data.getConfig().getLong("lastReset.weekly", 0);

                long today = getTodayTimestamp();

                // DAILY
                if (lastDaily < today) {
                    resetDaily();
                    plugin.getLogger().info("Daily quest reset!");
                }

                // WEEKLY (Senin)
                if (day == Calendar.MONDAY && lastWeekly < today) {
                    resetWeekly();
                    plugin.getLogger().info("Weekly quest reset!");
                }
            }

        }, 0L, 1200L); // cek tiap 1 menit
    }

    private long getTodayTimestamp() {

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
            data.getConfig().set("players." + uuid + ".progress", null);
            data.getConfig().set("players." + uuid + ".claimed", null);
        }

        data.getConfig().set("lastReset.daily", System.currentTimeMillis());
        data.save();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            assignDaily(player);
        }
    }

    public void resetWeekly() {

        if (data.getConfig().getConfigurationSection("players") == null) return;

        for (String uuid : data.getConfig().getConfigurationSection("players").getKeys(false)) {
            data.getConfig().set("players." + uuid + ".weekly", null);
        }

        data.getConfig().set("lastReset.weekly", System.currentTimeMillis());
        data.save();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            assignWeekly(player);
        }
    }
}