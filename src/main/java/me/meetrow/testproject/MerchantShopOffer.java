package me.meetrow.testproject;

import org.bukkit.Material;

public final class MerchantShopOffer {
    public enum Type {
        BUY,
        SELL
    }

    private final String key;
    private final Type type;
    private final Material material;
    private final int amount;
    private final double price;
    private final int stock;

    public MerchantShopOffer(String key, Type type, Material material, int amount, double price, int stock) {
        this.key = key;
        this.type = type;
        this.material = material;
        this.amount = amount;
        this.price = price;
        this.stock = stock;
    }

    public String getKey() {
        return key;
    }

    public Type getType() {
        return type;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }
}
