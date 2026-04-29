package me.meetrow.testproject;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.fancynpcs.api.events.NpcInteractEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.function.Consumer;

public final class OnboardingFancyNpcListener implements Listener {
    private final Testproject plugin;

    public OnboardingFancyNpcListener(Testproject plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onNpcInteract(NpcInteractEvent event) {
        Npc npc = event.getNpc();
        if (npc == null) {
            return;
        }
        NpcData data = npc.getData();
        if (data == null || data.getId() == null || data.getId().isBlank()) {
            return;
        }
        Testproject.OnboardingFancyNpcBinding binding = plugin.getOnboardingFancyNpcBinding(data.getId());
        if (binding == null) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        event.setCancelled(true);
        Location focusLocation = data.getLocation();
        Consumer<Player> onClick = event.getOnClick();
        Runnable completionAction = onClick == null ? null : () -> onClick.accept(player);
        String displayName = binding.displayName();
        if ((displayName == null || displayName.isBlank()) && data.getDisplayName() != null && !data.getDisplayName().isBlank()) {
            displayName = data.getDisplayName();
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = data.getName() != null && !data.getName().isBlank() ? data.getName() : data.getId();
        }

        plugin.beginOnboardingFancyNpcInteraction(
                player,
                data.getId(),
                binding.questKey(),
                binding.dialogueKey(),
                displayName,
                focusLocation,
                completionAction
        );
    }
}
