package com.yourname.smoney.market;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class MarketItem {

    private final String id;
    private final UUID seller;
    private final ItemStack item;
    private double price;

    public MarketItem(String id, UUID seller, ItemStack item, double price) {
        this.id = id;
        this.seller = seller;
        this.item = item.clone(); // 🔒 SAFE
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public UUID getSeller() {
        return seller;
    }

    public ItemStack getItem() {
        return item.clone(); // 🔒 WAJIB
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}