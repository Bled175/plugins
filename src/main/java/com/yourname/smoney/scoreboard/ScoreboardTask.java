package com.yourname.smoney.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ScoreboardTask implements Runnable {

    private final ScoreboardManager scoreboard;

    public ScoreboardTask(ScoreboardManager scoreboard) {
        this.scoreboard = scoreboard;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            scoreboard.setScoreboard(player);
        }
    }
}