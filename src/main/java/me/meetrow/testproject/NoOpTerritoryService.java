package me.meetrow.testproject;

import org.bukkit.Location;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NoOpTerritoryService implements TerritoryService {
    private final String reason;

    public NoOpTerritoryService(String reason) {
        this.reason = reason;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public TerritoryOperationResult bindCountry(Country country, Collection<Country> allCountries, String worldName, String regionId) {
        return TerritoryOperationResult.failure(reason);
    }

    @Override
    public TerritoryOperationResult clearCountry(Country country) {
        return TerritoryOperationResult.failure(reason);
    }

    @Override
    public TerritoryOperationResult syncCountry(Country country) {
        return TerritoryOperationResult.failure(reason);
    }

    @Override
    public void syncAll(Collection<Country> countries) {
    }

    @Override
    public String describeTerritory(Country country) {
        return country.hasTerritory()
                ? country.getTerritoryWorld() + " / " + country.getTerritoryRegionId()
                : "none";
    }

    @Override
    public List<String> getRegionIds(String worldName) {
        return Collections.emptyList();
    }

    @Override
    public Country getCountryAt(Location location, Collection<Country> countries) {
        return null;
    }

    @Override
    public List<Location> getBorderLocations(Country country) {
        return List.of();
    }

    @Override
    public TerritoryDebugInfo getDebugInfo(Country country) {
        return new TerritoryDebugInfo(
                false,
                false,
                false,
                country.hasTerritory(),
                false,
                false,
                country.getTerritoryWorld(),
                country.getTerritoryRegionId(),
                null,
                0,
                null,
                false
        );
    }

    @Override
    public int countFarmlandBlocks(Country country) {
        return 0;
    }

    @Override
    public int getTerritoryArea(Country country) {
        return 0;
    }
}
