package com.yourname.smoney;

import com.yourname.smoney.commands.MarketCommand;
import com.yourname.smoney.commands.MoneyCommand;
import com.yourname.smoney.commands.QuestCommand;
import com.yourname.smoney.commands.ShopCommand;
import com.yourname.smoney.data.DataManager;
import com.yourname.smoney.economy.EconomyManager;
import com.yourname.smoney.economy.TransactionLogger;
import com.yourname.smoney.listeners.DeathListener;
import com.yourname.smoney.listeners.PlayerJoinListener;
import com.yourname.smoney.listeners.QuestListener;
import com.yourname.smoney.listeners.QuestProgressListener;
import com.yourname.smoney.market.MarketGUI;
import com.yourname.smoney.market.MarketManager;
import com.yourname.smoney.market.MarketMyGUI;
import com.yourname.smoney.quest.QuestGUIListener;
import com.yourname.smoney.quest.QuestManager;
import com.yourname.smoney.scoreboard.ScoreboardManager;
import com.yourname.smoney.shop.ShopListener;
import com.yourname.smoney.shop.ShopManager;
import com.yourname.smoney.shop.ShopGUI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private DataManager dataManager;
    private EconomyManager economyManager;
    private ShopManager shopManager;
    private QuestManager questManager;
    private MarketManager marketManager;
    private MarketGUI marketGUI;
    private MarketMyGUI marketMyGUI;
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
        new ShopGUI(shopManager);

        scoreboardManager = new ScoreboardManager(economyManager);
        economyManager.setScoreboard(scoreboardManager);

        questManager = new QuestManager(this, dataManager, economyManager, scoreboardManager);
        questManager.loadQuests();

        marketManager = new MarketManager(economyManager, this, transactionLogger);

        // ✅ INIT GUI SEKALI
        marketGUI = new MarketGUI(marketManager);
        marketMyGUI = new MarketMyGUI(marketManager);

        // =====================
        // 🧾 COMMAND REGISTER
        // =====================
        getCommand("money").setExecutor(new MoneyCommand(economyManager));
        getCommand("shop").setExecutor(new ShopCommand(shopManager));
        getCommand("quest").setExecutor(new QuestCommand(questManager));

        // ✅ FIX PENTING
        getCommand("market").setExecutor(
                new MarketCommand(marketManager, marketGUI, marketMyGUI)
        );

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
                new QuestGUIListener(questManager), this);

        // ✅ REGISTER YANG SUDAH DI-INSTANTIATE
        Bukkit.getPluginManager().registerEvents(marketGUI, this);
        Bukkit.getPluginManager().registerEvents(marketMyGUI, this);

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