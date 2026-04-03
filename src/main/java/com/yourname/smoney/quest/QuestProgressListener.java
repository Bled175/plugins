package com.yourname.smoney.listeners;

import com.yourname.smoney.quest.Quest;
import com.yourname.smoney.quest.QuestManager;
import com.yourname.smoney.quest.QuestType;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class QuestProgressListener implements Listener {

    private final QuestManager manager;

    public QuestProgressListener(QuestManager manager) {
        this.manager = manager;
    }

    // ================= KILL =================
    @EventHandler
    public void onKill(EntityDeathEvent e) {

        if (e.getEntity().getKiller() == null) return;

        Player player = e.getEntity().getKiller();
        EntityType killed = e.getEntityType();

        for (Quest quest : manager.getAllQuests()) {

            if (quest.getType() != QuestType.KILL) continue;

            String target = quest.getTargetType();

            if (target.equalsIgnoreCase("ANY")) {
                manager.addProgress(player, quest.getId(), QuestType.KILL, 1);
                continue;
            }

            try {
                if (EntityType.valueOf(target.toUpperCase()) == killed) {
                    manager.addProgress(player, quest.getId(), QuestType.KILL, 1);
                }
            } catch (Exception ex) {
                Bukkit.getLogger().warning("Invalid mob type: " + target);
            }
        }
    }

    // ================= MINE =================
    @EventHandler
    public void onMine(BlockBreakEvent e) {

        Player p = e.getPlayer();
        Material m = e.getBlock().getType();

        for (Quest q : manager.getAllQuests()) {

            if (q.getType() != QuestType.MINE) continue;

            if (m.name().equalsIgnoreCase(q.getTargetType())) {
                manager.addProgress(p, q.getId(), QuestType.MINE, 1);
            }
        }
    }

    // ================= WALK (FIXED ANTI SPAM) =================
    @EventHandler
    public void onWalk(PlayerMoveEvent e) {

        if (e.getFrom().distanceSquared(e.getTo()) < 0.01) return;

        Player p = e.getPlayer();

        for (Quest q : manager.getAllQuests()) {

            if (q.getType() != QuestType.WALK) continue;

            manager.addProgress(p, q.getId(), QuestType.WALK, 1);
        }
    }
}