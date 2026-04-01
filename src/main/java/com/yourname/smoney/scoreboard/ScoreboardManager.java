package com.yourname.smoney.scoreboard;

import com.yourname.smoney.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardManager {

    private final EconomyManager economy;

    // 🔥 SIMPAN BOARD PER PLAYER
    private final Map<Player, Scoreboard> boards = new HashMap<>();

    public ScoreboardManager(EconomyManager economy) {
        this.economy = economy;
    }

    // =====================
    // CREATE SCOREBOARD (1x saja)
    // =====================
    public void setScoreboard(Player player) {

        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();

        Objective obj = board.registerNewObjective("smoney", "dummy", "§6SMoney");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // dummy line biar urutan aman
        obj.getScore("§7").setScore(4);

        // money
        obj.getScore(getMoneyLine(player)).setScore(3);

        // deaths
        int deaths = player.getStatistic(org.bukkit.Statistic.DEATHS);
        obj.getScore("§fDeaths: §c" + deaths).setScore(2);

        // name
        obj.getScore("§7Player: §f" + player.getName()).setScore(1);

        player.setScoreboard(board);

        boards.put(player, board);
    }

    // =====================
    // UPDATE MONEY (NO RESET)
    // =====================
    public void updateMoney(Player player) {

        Scoreboard board = boards.get(player);
        if (board == null) {
            setScoreboard(player);
            return;
        }

        Objective obj = board.getObjective("smoney");
        if (obj == null) return;

        // 🔥 HAPUS LINE LAMA
        board.getEntries().forEach(entry -> {
            if (entry.contains("Money")) {
                board.resetScores(entry);
            }
        });

        // 🔥 TAMBAH LINE BARU
        obj.getScore(getMoneyLine(player)).setScore(3);
    }

    private String getMoneyLine(Player player) {
        int money = (int) economy.getMoney(player.getUniqueId());
        return "§fMoney: §a" + money;
    }

    // =====================
    // FULL UPDATE (OPTIONAL)
    // =====================
    public void update(Player player) {
        setScoreboard(player);
    }
}