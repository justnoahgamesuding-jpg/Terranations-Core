package me.meetrow.testproject;

public class ProfessionProgress {
    private int level;
    private int xp;

    public ProfessionProgress(int level, int xp) {
        this.level = Math.max(1, level);
        this.xp = Math.max(0, xp);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = Math.max(0, xp);
    }
}
