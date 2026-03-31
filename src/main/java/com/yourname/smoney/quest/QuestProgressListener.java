package com.yourname.smoney.quest;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class QuestProgressListener implements Listener {

    private final QuestManager manager;

    public QuestProgressListener(QuestManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {

        if (event.getEntity().getKiller() == null) return;

        Player player = event.getEntity().getKiller();
        EntityType killed = event.getEntityType();

        for (String id : manager.getDaily(player)) {

            Quest quest = manager.getQuest(id);
            if (quest == null) continue;

            // 🔥 HARUS QUEST KILL
            if (quest.getType() != QuestType.KILL) continue;

            try {
                String target = quest.getTargetType();

                if (target.equalsIgnoreCase("ANY")) {
                    manager.addProgress(player, id, 1);
                    continue;
                }

                EntityType targetEntity = EntityType.valueOf(target.toUpperCase());

                if (killed == targetEntity) {
                    manager.addProgress(player, id, 1);
                }

            } catch (IllegalArgumentException e) {
                System.out.println("ENTITY INVALID: " + quest.getTargetType());
            }
        }
    }

    @EventHandler
    public void onMine(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Material broken = event.getBlock().getType();

        for (String id : manager.getDaily(player)) {

            Quest quest = manager.getQuest(id);
            if (quest == null) continue;

            // 🔥 HARUS QUEST MINE
            if (quest.getType() != QuestType.MINE) continue;

            try {
                Material target = Material.valueOf(quest.getTargetType().toUpperCase());

                if (broken == target) {
                    manager.addProgress(player, id, 1);
                }

            } catch (IllegalArgumentException e) {
                System.out.println("ERROR MATERIAL: " + quest.getTargetType());
            }
        }
    }
}