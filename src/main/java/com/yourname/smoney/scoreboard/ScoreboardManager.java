package com.yourname.smoney.scoreboard;

import com.yourname.smoney.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardManager {

    private final EconomyManager economy;

    public ScoreboardManager(EconomyManager economy) {
        this.economy = economy;
    }

    public void setScoreboard(Player player) {

        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();

        Objective obj = board.registerNewObjective("smoney", "dummy", "§cSMoney");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // 🍒 Ceri
        int money = (int) economy.getMoney(player.getUniqueId());
        Score moneyScore = obj.getScore("§c Cery: §f" + money);
        moneyScore.setScore(2);

        // ☠ Deaths
        int deaths = player.getStatistic(org.bukkit.Statistic.DEATHS);
        Score deathScore = obj.getScore("§7☠ Deaths: §f" + deaths);
        deathScore.setScore(1);

        player.setScoreboard(board);
    }
}