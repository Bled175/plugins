package com.yourname.smoney.economy;

import com.yourname.smoney.data.DataManager;

import java.util.UUID;

public class EconomyManager {

    private final DataManager data;

    public EconomyManager(DataManager data) {
        this.data = data;
    }

    private String path(UUID uuid) {
        return "players." + uuid.toString() + ".money";
    }

    // GET MONEY (bisa minus juga)
    public double getMoney(UUID uuid) {
        return data.getConfig().getDouble(path(uuid), 0);
    }

    // SET MONEY (ADMIN POWER - bisa minus)
    public void setMoney(UUID uuid, double amount) {
        data.getConfig().set(path(uuid), amount);
        data.save();
    }

    // ADD MONEY
    public void addMoney(UUID uuid, double amount) {
        setMoney(uuid, getMoney(uuid) + amount);
    }

    // REMOVE MONEY (admin use)
    public boolean removeMoney(UUID uuid, double amount) {
        double current = getMoney(uuid);
        setMoney(uuid, current - amount);
        return true;
    }

    // 💸 TRANSFER (RULE BARU)
    public boolean transfer(UUID from, UUID to, double amount) {

        if (amount <= 0) return false;

        double senderBalance = getMoney(from);

        // ❌ tidak boleh transfer kalau saldo <= 0
        if (senderBalance <= 0) return false;

        // ❌ tidak cukup uang
        if (senderBalance < amount) return false;

        setMoney(from, senderBalance - amount);
        setMoney(to, getMoney(to) + amount);

        return true;
    }

    // ADMIN FORCE SET
    public void adminSetMoney(UUID uuid, double amount) {
        setMoney(uuid, amount);
    }

    // CHECK ACCOUNT
    public boolean hasAccount(UUID uuid) {
        return data.getConfig().contains(path(uuid));
    }
}