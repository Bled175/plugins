package com.yourname.smoney;

import com.yourname.smoney.commands.MarketCommand;
import com.yourname.smoney.commands.MoneyCommand;
import com.yourname.smoney.commands.QuestCommand;
import com.yourname.smoney.commands.ShopCommand;
import com.yourname.smoney.data.DataManager;
import com.yourname.smoney.economy.EconomyManager;
import com.yourname.smoney.economy.TransactionLogger;
import com.yourname.smoney.listeners.DeathListener;
import com.yourname.smoney.listeners.MarketListener;
import com.yourname.smoney.listeners.PlayerJoinListener;
import com.yourname.smoney.listeners.QuestListener;
import com.yourname.smoney.listeners.QuestProgressListener;
import com.yourname.smoney.market.MarketManager;
import com.yourname.smoney.quest.QuestManager;
import com.yourname.smoney.scoreboard.ScoreboardManager;
import com.yourname.smoney.shop.ShopListener;
import com.yourname.smoney.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;
    private EconomyManager economyManager;
    private ShopManager shopManager;
    private QuestManager questManager;
    private MarketManager marketManager;
    private ScoreboardManager scoreboardManager;
    private TransactionLogger transactionLogger;

    @Override
    public void onEnable() {

        // =====================
        // 📁 FILE SETUP
        // =====================
        saveDefaultConfig();
        saveResource("data.yml", false);
        saveResource("quests.yml", false);

        // =====================
        // ⚙️ MANAGER INIT
        // =====================
        dataManager = new DataManager(this);
        economyManager = new EconomyManager(dataManager);
        transactionLogger = new TransactionLogger(this);

        shopManager = new ShopManager(dataManager, economyManager);

        // 🔥 FIX DI SINI (tambah this)
        questManager = new QuestManager(this, dataManager, economyManager);
        questManager.loadQuests();

        marketManager = new MarketManager(economyManager, this, transactionLogger);

        scoreboardManager = new ScoreboardManager(economyManager);

        // =====================
        // 🧾 COMMAND REGISTER
        // =====================
        getCommand("money").setExecutor(new MoneyCommand(economyManager));
        getCommand("shop").setExecutor(new ShopCommand(shopManager));
        getCommand("quest").setExecutor(new QuestCommand(questManager));
        getCommand("market").setExecutor(new MarketCommand(marketManager, economyManager));

        // =====================
        // 🎧 LISTENER REGISTER
        // =====================
        Bukkit.getPluginManager().registerEvents(
                new PlayerJoinListener(economyManager, questManager, scoreboardManager), this);

        Bukkit.getPluginManager().registerEvents(
                new ShopListener(shopManager), this);

        Bukkit.getPluginManager().registerEvents(
                new QuestListener(questManager), this);

        Bukkit.getPluginManager().registerEvents(
                new MarketListener(marketManager), this);

        Bukkit.getPluginManager().registerEvents(
                new QuestProgressListener(questManager), this);

        Bukkit.getPluginManager().registerEvents(
                new DeathListener(scoreboardManager), this);

        getLogger().info("§a[SMoney] Plugin berhasil dinyalakan!");
    }

    @Override
    public void onDisable() {
        getLogger().info("§c[SMoney] Plugin dimatikan!");
    }
}