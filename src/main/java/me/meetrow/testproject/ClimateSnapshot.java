package me.meetrow.testproject;

public record ClimateSnapshot(
        double temperatureCelsius,
        double growthMultiplier,
        ClimateSeason season,
        String climateName,
        boolean freezing,
        boolean raining,
        boolean recentlyRained,
        double altitudeGrowthMultiplier,
        double patternTemperatureOffsetCelsius,
        double altitudeTemperatureOffsetCelsius,
        double humidity,
        double continentality,
        double currentInfluence,
        double rainGrowthBonusMultiplier,
        double rainTemperatureOffsetCelsius,
        double humidityTemperatureOffsetCelsius,
        double continentalTemperatureOffsetCelsius,
        double currentTemperatureOffsetCelsius
) {
}
