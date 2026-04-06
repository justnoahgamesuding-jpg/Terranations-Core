package me.meetrow.testproject;

import org.bukkit.Location;

import java.util.Collection;
import java.util.List;

public interface TerritoryService {
    boolean isAvailable();

    TerritoryOperationResult bindCountry(Country country, Collection<Country> allCountries, String worldName, String regionId);

    TerritoryOperationResult clearCountry(Country country);

    TerritoryOperationResult syncCountry(Country country);

    void syncAll(Collection<Country> countries);

    String describeTerritory(Country country);

    List<String> getRegionIds(String worldName);

    Country getCountryAt(Location location, Collection<Country> countries);

    List<Location> getBorderLocations(Country country);

    TerritoryDebugInfo getDebugInfo(Country country);

    int countFarmlandBlocks(Country country);
}
