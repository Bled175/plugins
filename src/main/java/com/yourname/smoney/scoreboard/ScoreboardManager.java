package com.yourname.smoney.scoreboard;

import com.yourname.smoney.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

/**
 * Manages player scoreboards showing money and stats
 */
public class ScoreboardManager {

    private final EconomyManager economy;

    public ScoreboardManager(EconomyManager economy) {
        this.economy = economy;
    }

    public void setScoreboard(Player player) {

        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();

        Objective obj = board.registerNewObjective("smoney", "dummy", "§6SMoney");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // 💰 Money
        int money = (int) economy.getMoney(player.getUniqueId());
        Score moneyScore = obj.getScore("§fMoney: §a" + money);
        moneyScore.setScore(3);

        // ☠ Deaths
        int deaths = player.getStatistic(org.bukkit.Statistic.DEATHS);
        Score deathScore = obj.getScore("§fDeaths: §c" + deaths);
        deathScore.setScore(2);
        
        // Player name
        Score nameScore = obj.getScore("§7Player: §f" + player.getName());
        nameScore.setScore(1);

        player.setScoreboard(board);

    }
    
    // Update scoreboard when money changes
    public void updateMoney(Player player) {
        setScoreboard(player);
    }

    // 🔥 TAMBAH INI
    public void update(Player player) {
        setScoreboard(player);
    }
}