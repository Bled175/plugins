package com.yourname.smoney.listeners;

import com.yourname.smoney.quest.QuestManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Tracks quest progress for kills and mining
 */
public class QuestProgressListener implements Listener {

    private final QuestManager questManager;

    public QuestProgressListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    // ==================
    // MOB KILL TRACKING
    // ==================
    @EventHandler
    public void onMobKill(EntityDeathEvent e) {

        LivingEntity entity = e.getEntity();

        // hanya monster (zombie, skeleton, dll)
        if (!(entity instanceof Monster)) return;

        Player killer = entity.getKiller();
        if (killer == null) return;

        String mobName = entity.getType().name().toLowerCase();

        // contoh: kill_zombie
        String specificQuest = "kill_" + mobName;

        // progress
        questManager.addProgress(killer, "kill_monster", 1); // generic
        questManager.addProgress(killer, specificQuest, 1);  // spesifik
    }

    // ==================
    // BLOCK BREAK TRACKING
    // ==================
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        Player player = e.getPlayer();
        String blockType = e.getBlock().getType().name().toLowerCase();

        // contoh: mine_diamond_ore
        String specificQuest = "mine_" + blockType;

        questManager.addProgress(player, specificQuest, 1); // spesifik
        questManager.addProgress(player, "mining", 1);      // generic
    }
}