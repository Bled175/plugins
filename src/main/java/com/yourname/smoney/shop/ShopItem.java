package com.yourname.smoney.shop;

import org.bukkit.inventory.ItemStack;

public class ShopItem {

    private final ItemStack item;
    private final double price;
    private int stock; // -1 = infinite

    public ShopItem(ItemStack item, double price, int stock) {
        this.item = item;
        this.price = price;
        this.stock = stock;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public void reduceStock() {
        if (stock > 0) stock--;
    }

    public boolean isInfinite() {
        return stock == -1;
    }

    public boolean isOutOfStock() {
        return stock == 0;
    }
}