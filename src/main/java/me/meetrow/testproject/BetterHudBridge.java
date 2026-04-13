package me.meetrow.testproject;

import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.util.UUID;
import java.util.function.Function;

public final class BetterHudBridge {
    private final Testproject plugin;
    private boolean registered;

    public BetterHudBridge(Testproject plugin) {
        this.plugin = plugin;
    }

    public void registerQuestPlaceholders() {
        if (registered) {
            return;
        }

        try {
            Class<?> betterHudApiClass = Class.forName("kr.toxicity.hud.api.BetterHudAPI");
            Object betterHud = betterHudApiClass.getMethod("inst").invoke(null);
            if (betterHud == null) {
                return;
            }

            Method getPlaceholderManagerMethod = betterHud.getClass().getMethod("getPlaceholderManager");
            Object placeholderManager = getPlaceholderManagerMethod.invoke(betterHud);
            if (placeholderManager == null) {
                return;
            }

            Class<?> placeholderContainerClass = Class.forName("kr.toxicity.hud.api.placeholder.PlaceholderContainer");
            Method addPlaceholderMethod = placeholderContainerClass.getMethod(
                    "addPlaceholder",
                    String.class,
                    Class.forName("kr.toxicity.hud.api.placeholder.HudPlaceholder")
            );

            Object stringContainer = placeholderManager.getClass().getMethod("getStringContainer").invoke(placeholderManager);
            Object numberContainer = placeholderManager.getClass().getMethod("getNumberContainer").invoke(placeholderManager);
            Object booleanContainer = placeholderManager.getClass().getMethod("getBooleanContainer").invoke(placeholderManager);

            registerStringPlaceholder(addPlaceholderMethod, stringContainer, "terra_quest_id",
                    playerId -> plugin.getTutorialQuestId(playerId));
            registerStringPlaceholder(addPlaceholderMethod, stringContainer, "terra_quest_title",
                    playerId -> plugin.getTutorialQuestTitle(playerId));
            registerStringPlaceholder(addPlaceholderMethod, stringContainer, "terra_quest_title_plain",
                    playerId -> plugin.getTutorialQuestTitlePlain(playerId));
            registerStringPlaceholder(addPlaceholderMethod, stringContainer, "terra_quest_objective",
                    playerId -> plugin.getTutorialQuestObjective(playerId));
            registerStringPlaceholder(addPlaceholderMethod, stringContainer, "terra_quest_objective_plain",
                    playerId -> plugin.getTutorialQuestObjectivePlain(playerId));
            registerStringPlaceholder(addPlaceholderMethod, stringContainer, "terra_quest_hint",
                    playerId -> plugin.getTutorialQuestHint(playerId));
            registerStringPlaceholder(addPlaceholderMethod, stringContainer, "terra_quest_hint_plain",
                    playerId -> plugin.getTutorialQuestHintPlain(playerId));
            registerStringPlaceholder(addPlaceholderMethod, stringContainer, "terra_quest_progress",
                    playerId -> plugin.getTutorialQuestProgressText(playerId));
            registerStringPlaceholder(addPlaceholderMethod, stringContainer, "terra_quest_status",
                    playerId -> plugin.getTutorialQuestStatusText(playerId));
            registerStringPlaceholder(addPlaceholderMethod, stringContainer, "terra_quest_progress_bar",
                    playerId -> plugin.getTutorialQuestProgressBarText(playerId));
            registerStringPlaceholder(addPlaceholderMethod, stringContainer, "terra_quest_accent",
                    playerId -> plugin.getTutorialQuestAccentColor(playerId));
            registerStringPlaceholder(addPlaceholderMethod, stringContainer, "terra_quest_profession",
                    playerId -> plugin.getTutorialQuestProfessionKey(playerId));

            registerNumberPlaceholder(addPlaceholderMethod, numberContainer, "terra_quest_current",
                    playerId -> plugin.getTutorialQuestCurrentValue(playerId));
            registerNumberPlaceholder(addPlaceholderMethod, numberContainer, "terra_quest_target",
                    playerId -> plugin.getTutorialQuestTargetValue(playerId));
            registerNumberPlaceholder(addPlaceholderMethod, numberContainer, "terra_quest_percent",
                    playerId -> plugin.getTutorialQuestPercent(playerId));
            registerNumberPlaceholder(addPlaceholderMethod, numberContainer, "terra_quest_steps",
                    playerId -> plugin.getTutorialQuestSteps(playerId));
            registerNumberPlaceholder(addPlaceholderMethod, numberContainer, "terra_quest_max_steps",
                    playerId -> plugin.getTutorialQuestMaxSteps());
            registerNumberPlaceholder(addPlaceholderMethod, numberContainer, "terra_health",
                    playerId -> plugin.getPlayerHealthRounded(playerId));
            registerNumberPlaceholder(addPlaceholderMethod, numberContainer, "terra_max_health",
                    playerId -> plugin.getPlayerMaxHealthRounded(playerId));
            registerNumberPlaceholder(addPlaceholderMethod, numberContainer, "terra_food",
                    playerId -> plugin.getPlayerFoodLevel(playerId));
            registerNumberPlaceholder(addPlaceholderMethod, numberContainer, "terra_level",
                    playerId -> plugin.getPlayerExperienceLevel(playerId));

            registerBooleanPlaceholder(addPlaceholderMethod, booleanContainer, "terra_quest_active",
                    playerId -> plugin.hasActiveTutorialQuest(playerId));

            registered = true;
            plugin.getLogger().info("Registered BetterHud Terra quest placeholders.");
        } catch (ClassNotFoundException ignored) {
            // BetterHud is not installed.
        } catch (Throwable throwable) {
            plugin.getLogger().warning("Failed to register BetterHud Terra quest placeholders: " + throwable.getMessage());
        }
    }

    public void reloadBetterHud() {
        try {
            Class<?> betterHudApiClass = Class.forName("kr.toxicity.hud.api.BetterHudAPI");
            Object betterHud = betterHudApiClass.getMethod("inst").invoke(null);
            if (betterHud == null) {
                return;
            }

            Class<?> reloadFlagTypeClass = Class.forName("kr.toxicity.hud.api.plugin.ReloadFlagType");
            Object emptyFlags = Array.newInstance(reloadFlagTypeClass, 0);
            Method reloadMethod = betterHud.getClass().getMethod("reload", emptyFlags.getClass());
            reloadMethod.invoke(betterHud, emptyFlags);
            plugin.getLogger().info("Reloaded BetterHud after Terra placeholder registration.");
        } catch (ClassNotFoundException ignored) {
            // BetterHud is not installed.
        } catch (Throwable throwable) {
            plugin.getLogger().warning("Failed to reload BetterHud after Terra placeholder registration: " + throwable.getMessage());
        }
    }

    private void registerStringPlaceholder(Method addPlaceholderMethod, Object container, String key,
                                           Function<UUID, String> resolver) throws Exception {
        addPlaceholderMethod.invoke(container, key, createHudPlaceholder(playerId -> {
            String value = resolver.apply(playerId);
            return value != null ? value : "";
        }));
    }

    private void registerNumberPlaceholder(Method addPlaceholderMethod, Object container, String key,
                                           Function<UUID, Number> resolver) throws Exception {
        addPlaceholderMethod.invoke(container, key, createHudPlaceholder(playerId -> {
            Number value = resolver.apply(playerId);
            return value != null ? value : 0;
        }));
    }

    private void registerBooleanPlaceholder(Method addPlaceholderMethod, Object container, String key,
                                            Function<UUID, Boolean> resolver) throws Exception {
        addPlaceholderMethod.invoke(container, key, createHudPlaceholder(playerId -> {
            Boolean value = resolver.apply(playerId);
            return value != null && value;
        }));
    }

    private Object createHudPlaceholder(Function<UUID, Object> resolver) throws Exception {
        Class<?> hudPlayerClass = Class.forName("kr.toxicity.hud.api.player.HudPlayer");
        Class<?> placeholderFunctionClass = Class.forName("kr.toxicity.hud.api.placeholder.HudPlaceholder$PlaceholderFunction");
        Class<?> hudPlaceholderClass = Class.forName("kr.toxicity.hud.api.placeholder.HudPlaceholder");

        Method uuidMethod = hudPlayerClass.getMethod("uuid");
        Method placeholderFunctionOfMethod = placeholderFunctionClass.getMethod("of", Function.class);
        Method hudPlaceholderOfMethod = hudPlaceholderClass.getMethod("of", placeholderFunctionClass);

        Function<Object, Object> hudResolver = hudPlayer -> {
            try {
                UUID playerId = (UUID) uuidMethod.invoke(hudPlayer);
                return resolver.apply(playerId);
            } catch (Throwable throwable) {
                return "";
            }
        };

        Object placeholderFunction = placeholderFunctionOfMethod.invoke(null, hudResolver);
        return hudPlaceholderOfMethod.invoke(null, placeholderFunction);
    }
}
