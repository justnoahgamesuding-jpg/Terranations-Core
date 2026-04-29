package me.meetrow.testproject;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class PlayerQuestDefinition {
    private final String id;
    private final int order;
    private final boolean enabled;
    private final String title;
    private final String objective;
    private final String hint;
    private final int target;
    private final String key;
    private final Profession profession;
    private final PlayerQuestType type;
    private final Set<String> requiresCompleted;

    public PlayerQuestDefinition(
            String id,
            int order,
            boolean enabled,
            String title,
            String objective,
            String hint,
            int target,
            String key,
            Profession profession,
            PlayerQuestType type,
            Set<String> requiresCompleted
    ) {
        this.id = id;
        this.order = order;
        this.enabled = enabled;
        this.title = title;
        this.objective = objective;
        this.hint = hint;
        this.target = target;
        this.key = key;
        this.profession = profession;
        this.type = type;
        this.requiresCompleted = Collections.unmodifiableSet(new LinkedHashSet<>(requiresCompleted));
    }

    public String getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getTitle() {
        return title;
    }

    public String getObjective() {
        return objective;
    }

    public String getHint() {
        return hint;
    }

    public int getTarget() {
        return target;
    }

    public String getKey() {
        return key;
    }

    public Profession getProfession() {
        return profession;
    }

    public PlayerQuestType getType() {
        return type;
    }

    public Set<String> getRequiresCompleted() {
        return requiresCompleted;
    }
}
