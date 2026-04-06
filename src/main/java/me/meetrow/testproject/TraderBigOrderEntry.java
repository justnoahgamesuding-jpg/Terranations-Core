package me.meetrow.testproject;

import org.bukkit.Material;

public final class TraderBigOrderEntry {
    private final Profession profession;
    private final Material requestedMaterial;
    private final int requestedAmount;
    private int deliveredAmount;

    public TraderBigOrderEntry(Profession profession, Material requestedMaterial, int requestedAmount, int deliveredAmount) {
        this.profession = profession;
        this.requestedMaterial = requestedMaterial;
        this.requestedAmount = requestedAmount;
        this.deliveredAmount = Math.max(0, Math.min(requestedAmount, deliveredAmount));
    }

    public Profession getProfession() {
        return profession;
    }

    public Material getRequestedMaterial() {
        return requestedMaterial;
    }

    public int getRequestedAmount() {
        return requestedAmount;
    }

    public int getDeliveredAmount() {
        return deliveredAmount;
    }

    public int getRemainingAmount() {
        return Math.max(0, requestedAmount - deliveredAmount);
    }

    public boolean isComplete() {
        return deliveredAmount >= requestedAmount;
    }

    public int addDeliveredAmount(int amount) {
        int applied = Math.max(0, Math.min(getRemainingAmount(), amount));
        deliveredAmount += applied;
        return applied;
    }
}
