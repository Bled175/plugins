package com.yourname.smoney.listeners;

import com.yourname.smoney.scoreboard.ScoreboardManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Updates scoreboard when player dies
 */
public class DeathListener implements Listener {

    private final ScoreboardManager scoreboardManager;

    public DeathListener(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        
        // Update scoreboard to show new death count
        scoreboardManager.setScoreboard(player);
    }
}
