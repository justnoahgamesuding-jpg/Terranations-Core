package me.meetrow.testproject;

public final class PersonalWorkOrder {
    private final Profession profession;
    private final int targetXp;
    private int progressXp;
    private final double rewardMoney;
    private final int rewardSkillPoints;
    private final long generatedAtMillis;

    public PersonalWorkOrder(Profession profession, int targetXp, int progressXp, double rewardMoney, int rewardSkillPoints, long generatedAtMillis) {
        this.profession = profession;
        this.targetXp = Math.max(1, targetXp);
        this.progressXp = Math.max(0, progressXp);
        this.rewardMoney = Math.max(0.0D, rewardMoney);
        this.rewardSkillPoints = Math.max(0, rewardSkillPoints);
        this.generatedAtMillis = Math.max(0L, generatedAtMillis);
    }

    public Profession getProfession() {
        return profession;
    }

    public int getTargetXp() {
        return targetXp;
    }

    public int getProgressXp() {
        return progressXp;
    }

    public void addProgress(int amount) {
        progressXp = Math.min(targetXp, progressXp + Math.max(0, amount));
    }

    public double getRewardMoney() {
        return rewardMoney;
    }

    public int getRewardSkillPoints() {
        return rewardSkillPoints;
    }

    public long getGeneratedAtMillis() {
        return generatedAtMillis;
    }

    public boolean isComplete() {
        return progressXp >= targetXp;
    }

    public int getRemainingXp() {
        return Math.max(0, targetXp - progressXp);
    }
}
