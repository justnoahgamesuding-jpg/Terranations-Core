package me.meetrow.testproject;

import org.bukkit.Material;

import java.util.Set;

public record CropClimateProfile(
        String key,
        String displayName,
        Material displayMaterial,
        ClimateSeason bestSeason,
        double optimalMinCelsius,
        double optimalMaxCelsius,
        Set<Material> relatedMaterials
) {
    public boolean matches(Material material) {
        return material != null && relatedMaterials.contains(material);
    }
}
