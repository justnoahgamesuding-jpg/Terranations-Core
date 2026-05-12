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
    private final String contentId;
    private final int amount;
    private final double price;
    private final int stock;

    public MerchantShopOffer(String key, Type type, Material material, int amount, double price, int stock) {
        this(key, type, material, null, amount, price, stock);
    }

    public MerchantShopOffer(String key, Type type, Material material, String contentId, int amount, double price, int stock) {
        this.key = key;
        this.type = type;
        this.material = material;
        this.contentId = contentId;
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

    public String getContentId() {
        return contentId;
    }

    public boolean hasContentId() {
        return contentId != null && !contentId.isBlank();
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
