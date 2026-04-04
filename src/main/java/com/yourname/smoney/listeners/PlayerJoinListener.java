package com.yourname.smoney.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.yourname.smoney.economy.CurrencyUtil;
import com.yourname.smoney.economy.EconomyManager;
import com.yourname.smoney.quest.QuestManager;
import com.yourname.smoney.scoreboard.ScoreboardManager;

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

        // 🎯 ASSIGN/REFRESH QUESTS
        questManager.assignIfMissing(e.getPlayer());

        // 👋 MESSAGE
        e.getPlayer().sendMessage("§aSelamat datang!");
        e.getPlayer().sendMessage("§7Saldo: " + CurrencyUtil.format(economy.getMoney(uuid)));

        // 📊 SCOREBOARD
        scoreboard.setScoreboard(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // Jangan reset quest data saat quit - data harus persist untuk session berikutnya
        // questManager.resetForPlayer(e.getPlayer());
    }
}