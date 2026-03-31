package com.yourname.smoney.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final QuestManager manager;

    public PlayerJoinListener(QuestManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        manager.assignDaily(event.getPlayer());
        manager.assignWeekly(event.getPlayer());
    }
}