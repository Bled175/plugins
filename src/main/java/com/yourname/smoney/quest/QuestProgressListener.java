package com.yourname.smoney.quest;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class QuestProgressListener implements Listener {

    private final QuestManager manager;
    private final Map<Player, Long> lastWalkSave = new HashMap<>();
    private static final long WALK_SAVE_INTERVAL = 20; // 1 second (20 ticks)

    public QuestProgressListener(QuestManager manager) {
        this.manager = manager;
    }

    // ================= KILL =================
    @EventHandler
    public void onKill(EntityDeathEvent e) {

        if (e.getEntity().getKiller() == null) return;

        Player player = e.getEntity().getKiller();
        EntityType killed = e.getEntityType();

        // Check player's assigned daily quests
        for (String questId : manager.getDaily(player)) {
            Quest quest = manager.getQuest(questId, QuestType.DAILY);
            if (quest == null || !quest.getActionType().equals("KILL")) continue;

            String target = quest.getTargetType();
            if (target.equalsIgnoreCase("ANY")) {
                manager.addProgress(player, questId, QuestType.DAILY, 1);
                continue;
            }

            if (isEntityMatch(killed, target)) {
                manager.addProgress(player, questId, QuestType.DAILY, 1);
            }
        }

        // Check player's assigned weekly quests
        for (String questId : manager.getWeekly(player)) {
            Quest quest = manager.getQuest(questId, QuestType.WEEKLY);
            if (quest == null || !quest.getActionType().equals("KILL")) continue;

            String target = quest.getTargetType();
            if (target.equalsIgnoreCase("ANY")) {
                manager.addProgress(player, questId, QuestType.WEEKLY, 1);
                continue;
            }

            if (isEntityMatch(killed, target)) {
                manager.addProgress(player, questId, QuestType.WEEKLY, 1);
            }
        }

        // Check global quest
        Quest globalQuest = manager.getGlobalQuest();
        if (globalQuest != null && globalQuest.getActionType().equals("KILL")) {
            String target = globalQuest.getTargetType();
            if (target.equalsIgnoreCase("ANY")) {
                manager.addProgress(player, globalQuest.getId(), QuestType.GLOBAL, 1);
            } else {
                if (isEntityMatch(killed, target)) {
                    manager.addProgress(player, globalQuest.getId(), QuestType.GLOBAL, 1);
                }
            }
        }
    }

    /**
     * Safely match entity type with quest target
     */
    private boolean isEntityMatch(EntityType killed, String target) {
        try {
            EntityType targetType = EntityType.valueOf(target.toUpperCase());
            return targetType.equals(killed);
        } catch (IllegalArgumentException ex) {
            Bukkit.getLogger().warning("[Quest] Invalid entity type in config: " + target);
            return false;
        }
    }

    // ================= MINE =================
    @EventHandler
    public void onMine(BlockBreakEvent e) {

        Player p = e.getPlayer();
        Material m = e.getBlock().getType();

        // Check daily quests
        for (String questId : manager.getDaily(p)) {
            Quest q = manager.getQuest(questId, QuestType.DAILY);
            if (q == null || !q.getActionType().equals("MINE")) continue;

            if (isMaterialMatches(m, q.getTargetType())) {
                manager.addProgress(p, questId, QuestType.DAILY, 1);
            }
        }

        // Check weekly quests
        for (String questId : manager.getWeekly(p)) {
            Quest q = manager.getQuest(questId, QuestType.WEEKLY);
            if (q == null || !q.getActionType().equals("MINE")) continue;

            if (isMaterialMatches(m, q.getTargetType())) {
                manager.addProgress(p, questId, QuestType.WEEKLY, 1);
            }
        }

        // Check global quest
        Quest globalQuest = manager.getGlobalQuest();
        if (globalQuest != null && globalQuest.getActionType().equals("MINE")) {
            if (isMaterialMatches(m, globalQuest.getTargetType())) {
                manager.addProgress(p, globalQuest.getId(), QuestType.GLOBAL, 1);
            }
        }
    }

    // ================= WALK (FIXED ANTI SPAM) =================
    @EventHandler
    public void onWalk(PlayerMoveEvent e) {

        if (e.getFrom().distanceSquared(e.getTo()) < 0.01) return;

        Player p = e.getPlayer();
        
        // Cooldown check: only save every 1 second
        long now = System.currentTimeMillis();
        long last = lastWalkSave.getOrDefault(p, 0L);
        if (now - last < 1000) return; // 1000ms = 1 second
        lastWalkSave.put(p, now);

        // Check daily quests
        for (String questId : manager.getDaily(p)) {
            Quest q = manager.getQuest(questId, QuestType.DAILY);
            if (q == null || !q.getActionType().equals("WALK")) continue;

            manager.addProgressAsync(p, questId, QuestType.DAILY, 1);
        }

        // Check weekly quests
        for (String questId : manager.getWeekly(p)) {
            Quest q = manager.getQuest(questId, QuestType.WEEKLY);
            if (q == null || !q.getActionType().equals("WALK")) continue;

            manager.addProgressAsync(p, questId, QuestType.WEEKLY, 1);
        }

        // Check global quest
        Quest globalQuest = manager.getGlobalQuest();
        if (globalQuest != null && globalQuest.getActionType().equals("WALK")) {
            manager.addProgressAsync(p, globalQuest.getId(), QuestType.GLOBAL, 1);
        }
    }

    /**
     * Match material dengan ore variants
     * Misal: DRIPSTONE_ORE akan match DRIPSTONE_ORE dan DEEPSLATE_DRIPSTONE_ORE
     */
    private boolean isMaterialMatches(Material mat, String targetType) {
        String matName = mat.name().toUpperCase();
        String targetName = targetType.toUpperCase();
        
        // Exact match
        if (matName.equals(targetName)) {
            return true;
        }
        
        // Match ore variants (untuk deepslate versions)
        // Jika target DRIPSTONE_ORE, maka cocok dengan DRIPSTONE_ORE dan DEEPSLATE_DRIPSTONE_ORE
        if (targetName.equals("DRIPSTONE_ORE")) {
            return matName.equals("DRIPSTONE_ORE") || matName.equals("DEEPSLATE_DRIPSTONE_ORE");
        }
        if (targetName.equals("COPPER_ORE")) {
            return matName.equals("COPPER_ORE") || matName.equals("DEEPSLATE_COPPER_ORE");
        }
        if (targetName.equals("IRON_ORE")) {
            return matName.equals("IRON_ORE") || matName.equals("DEEPSLATE_IRON_ORE");
        }
        if (targetName.equals("GOLD_ORE")) {
            return matName.equals("GOLD_ORE") || matName.equals("DEEPSLATE_GOLD_ORE");
        }
        if (targetName.equals("COAL_ORE")) {
            return matName.equals("COAL_ORE") || matName.equals("DEEPSLATE_COAL_ORE");
        }
        if (targetName.equals("EMERALD_ORE")) {
            return matName.equals("EMERALD_ORE") || matName.equals("DEEPSLATE_EMERALD_ORE");
        }
        if (targetName.equals("LAPIS_ORE")) {
            return matName.equals("LAPIS_ORE") || matName.equals("DEEPSLATE_LAPIS_ORE");
        }
        if (targetName.equals("REDSTONE_ORE")) {
            return matName.equals("REDSTONE_ORE") || matName.equals("DEEPSLATE_REDSTONE_ORE");
        }
        if (targetName.equals("DIAMOND_ORE")) {
            return matName.equals("DIAMOND_ORE") || matName.equals("DEEPSLATE_DIAMOND_ORE");
        }
        
        return false;
    }
}