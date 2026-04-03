package com.yourname.smoney.listeners;

import com.yourname.smoney.economy.CurrencyUtil;
import com.yourname.smoney.economy.EconomyManager;
import com.yourname.smoney.quest.QuestManager;
import com.yourname.smoney.scoreboard.ScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final EconomyManager economy;
    private final QuestManager questManager;
    private final ScoreboardManager scoreboard;

    public PlayerJoinListener(EconomyManager economy, QuestManager questManager, ScoreboardManager scoreboard) {
        this.economy = economy;
        this.questManager = questManager;
        this.scoreboard = scoreboard;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();

        // 💰 SET SALDO AWAL
        if (!economy.hasAccount(uuid)) {
            economy.setMoney(uuid, 100);
        }

        // 🎯 RELOAD QUEST DEFINITIONS & ASSIGN/REFRESH
        questManager.loadQuests();
        questManager.assignIfMissing(e.getPlayer());

        // 👋 MESSAGE
        e.getPlayer().sendMessage("§aSelamat datang!");
        e.getPlayer().sendMessage("§7Saldo: " + CurrencyUtil.format(economy.getMoney(uuid)));

        // 📊 SCOREBOARD
        scoreboard.setScoreboard(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // reset player's session quests on disconnect
        questManager.resetForPlayer(e.getPlayer());
    }
}