package me.meetrow.testproject;

import java.time.Instant;
import java.util.UUID;

public record BypassEntry(UUID playerId, String lastKnownName, Instant enabledAt) {
}
