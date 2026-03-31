package com.yourname.smoney.commands;

import com.yourname.smoney.quest.QuestGUI;
import com.yourname.smoney.quest.QuestManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestCommand implements CommandExecutor {

    private final QuestManager questManager;

    public QuestCommand(QuestManager questManager) {
        this.questManager = questManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        System.out.println("COMMAND QUEST KE TRIGGER");

        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        System.out.println("BUKA GUI");

        new QuestGUI(questManager).open(player);

        return true;
    }
}