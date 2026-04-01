package com.yourname.smoney.economy;

import com.yourname.smoney.data.DataManager;
import com.yourname.smoney.scoreboard.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EconomyManager {

    private final DataManager data;
    private ScoreboardManager scoreboard;

    public EconomyManager(DataManager data) {
        this.data = data;
    }

    // 🔗 CONNECT SCOREBOARD
    public void setScoreboard(ScoreboardManager scoreboard) {
        this.scoreboard = scoreboard;
    }

    private String path(UUID uuid) {
        return "players." + uuid.toString() + ".money";
    }

    // =====================
    // GET MONEY
    // =====================
    public double getMoney(UUID uuid) {
        return data.getConfig().getDouble(path(uuid), 0);
    }

    // =====================
    // SET MONEY (CORE)
    // =====================
    public void setMoney(UUID uuid, double amount) {
        data.getConfig().set(path(uuid), amount);
        data.save();

        // 🔥 AUTO UPDATE SCOREBOARD
        if (scoreboard != null) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                scoreboard.updateMoney(player);
            }
        }
    }

    // =====================
    // ADD MONEY
    // =====================
    public void addMoney(UUID uuid, double amount) {
        setMoney(uuid, getMoney(uuid) + amount);
    }

    // =====================
    // REMOVE MONEY (BISA MINUS)
    // =====================
    public boolean removeMoney(UUID uuid, double amount) {
        setMoney(uuid, getMoney(uuid) - amount);
        return true;
    }

    // =====================
    // TRANSFER PLAYER → PLAYER
    // =====================
    public boolean transfer(UUID from, UUID to, double amount) {

        if (amount <= 0) return false;

        double senderBalance = getMoney(from);

        if (senderBalance < amount) return false;

        setMoney(from, senderBalance - amount);
        setMoney(to, getMoney(to) + amount);

        return true;
    }

    // =====================
    // ADMIN POWER
    // =====================
    public void adminGiveMoney(UUID uuid, double amount) {
        setMoney(uuid, getMoney(uuid) + amount);
    }

    public void adminTakeMoney(UUID uuid, double amount) {
        setMoney(uuid, getMoney(uuid) - amount); // bisa minus
    }

    public void adminSetMoney(UUID uuid, double amount) {
        setMoney(uuid, amount);
    }

    // =====================
    // CHECK ACCOUNT
    // =====================
    public boolean hasAccount(UUID uuid) {
        return data.getConfig().contains(path(uuid));
    }
}