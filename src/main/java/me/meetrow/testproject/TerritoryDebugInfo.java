package me.meetrow.testproject;

import java.util.List;

public record TerritoryDebugInfo(
        boolean integrationAvailable,
        boolean dynmapInstalled,
        boolean dynmapEnabledInConfig,
        boolean territoryLinked,
        boolean worldFound,
        boolean regionFound,
        String worldName,
        String regionId,
        String regionType,
        int pointCount,
        String markerSetId,
        boolean markerSetPresent
) {
    public List<String> toLines(Testproject plugin, Country country) {
        return List.of(
                plugin.getMessage("country.territory.debug.country", plugin.placeholders("country", country.getName())),
                plugin.getMessage("country.territory.debug.integration", plugin.placeholders("value", String.valueOf(integrationAvailable))),
                plugin.getMessage("country.territory.debug.dynmap-installed", plugin.placeholders("value", String.valueOf(dynmapInstalled))),
                plugin.getMessage("country.territory.debug.dynmap-enabled", plugin.placeholders("value", String.valueOf(dynmapEnabledInConfig))),
                plugin.getMessage("country.territory.debug.linked", plugin.placeholders("value", String.valueOf(territoryLinked))),
                plugin.getMessage("country.territory.debug.world", plugin.placeholders("value", safe(worldName))),
                plugin.getMessage("country.territory.debug.world-found", plugin.placeholders("value", String.valueOf(worldFound))),
                plugin.getMessage("country.territory.debug.region", plugin.placeholders("value", safe(regionId))),
                plugin.getMessage("country.territory.debug.region-found", plugin.placeholders("value", String.valueOf(regionFound))),
                plugin.getMessage("country.territory.debug.region-type", plugin.placeholders("value", safe(regionType))),
                plugin.getMessage("country.territory.debug.point-count", plugin.placeholders("value", String.valueOf(pointCount))),
                plugin.getMessage("country.territory.debug.marker-set-id", plugin.placeholders("value", safe(markerSetId))),
                plugin.getMessage("country.territory.debug.marker-set-present", plugin.placeholders("value", String.valueOf(markerSetPresent)))
        );
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "none" : value;
    }
}
