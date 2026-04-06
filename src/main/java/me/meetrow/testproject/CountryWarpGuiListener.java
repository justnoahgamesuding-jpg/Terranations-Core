package me.meetrow.testproject;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class CountryWarpGuiListener implements Listener {
    private static final int GUI_SIZE = 54;
    private static final int[] COUNTRY_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int INFO_SLOT = 4;
    private static final int GLOBAL_SPAWN_SLOT = 48;
    private static final int PREVIOUS_SLOT = 45;
    private static final int CLOSE_SLOT = 49;
    private static final int NEXT_SLOT = 53;

    private final Testproject plugin;

    public CountryWarpGuiListener(Testproject plugin) {
        this.plugin = plugin;
    }

    public void openWarpMenu(Player player, int page) {
        List<Country> countries = plugin.getCountries();
        countries.sort(Comparator.comparing(Country::getName, String.CASE_INSENSITIVE_ORDER));

        int maxPage = Math.max(0, (countries.size() - 1) / COUNTRY_SLOTS.length);
        int safePage = Math.max(0, Math.min(page, maxPage));

        Inventory inventory = Bukkit.createInventory(
                new CountryWarpMenuHolder(player.getUniqueId(), safePage),
                GUI_SIZE,
                plugin.legacyComponent("&8Country Warp Admin")
        );

        fillEmptySlots(inventory);
        inventory.setItem(INFO_SLOT, createSimpleItem(Material.RECOVERY_COMPASS, "&6Country Warp Admin", List.of(
                "&7Browse every stored country.",
                "&7Click a country to warp to its home.",
                "&7Use the global spawn shortcut below when needed.",
                "&7Countries without a home are shown as unavailable.",
                "",
                "&7Page: &f" + (safePage + 1) + "&7/&f" + (maxPage + 1),
                "&7Countries: &f" + countries.size()
        )));
        inventory.setItem(GLOBAL_SPAWN_SLOT, createGlobalSpawnItem(player));

        int startIndex = safePage * COUNTRY_SLOTS.length;
        for (int i = 0; i < COUNTRY_SLOTS.length; i++) {
            int countryIndex = startIndex + i;
            if (countryIndex >= countries.size()) {
                break;
            }
            Country country = countries.get(countryIndex);
            inventory.setItem(COUNTRY_SLOTS[i], createCountryItem(country));
        }

        if (safePage > 0) {
            inventory.setItem(PREVIOUS_SLOT, createSimpleItem(Material.ARROW, "&ePrevious Page", List.of("&7Open page &f" + safePage)));
        }
        inventory.setItem(CLOSE_SLOT, createSimpleItem(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
        if (safePage < maxPage) {
            inventory.setItem(NEXT_SLOT, createSimpleItem(Material.ARROW, "&eNext Page", List.of("&7Open page &f" + (safePage + 2))));
        }

        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof CountryWarpMenuHolder menuHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        if (!plugin.canUseCountryWarpAdmin(player)) {
            player.closeInventory();
            player.sendMessage(plugin.getMessage("spawn.no-permission"));
            return;
        }

        if (event.getSlot() == PREVIOUS_SLOT) {
            openWarpMenu(player, menuHolder.page() - 1);
            return;
        }
        if (event.getSlot() == NEXT_SLOT) {
            openWarpMenu(player, menuHolder.page() + 1);
            return;
        }
        if (event.getSlot() == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }
        if (event.getSlot() == GLOBAL_SPAWN_SLOT) {
            player.teleport(plugin.getPrimarySpawnLocation(player));
            player.sendMessage(plugin.getMessage("spawn.worldspawn-teleported"));
            player.closeInventory();
            return;
        }

        Country selectedCountry = getCountryBySlot(menuHolder.page(), event.getSlot());
        if (selectedCountry == null) {
            return;
        }

        if (!selectedCountry.hasHome()) {
            player.sendMessage(plugin.getMessage("spawn.country-no-home", plugin.placeholders("country", selectedCountry.getName())));
            return;
        }

        if (plugin.teleportToCountryHome(player, selectedCountry, false, "spawn.admin-teleported")) {
            player.closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof CountryWarpMenuHolder) {
            event.setCancelled(true);
        }
    }

    private Country getCountryBySlot(int page, int slot) {
        for (int i = 0; i < COUNTRY_SLOTS.length; i++) {
            if (COUNTRY_SLOTS[i] != slot) {
                continue;
            }

            int index = (page * COUNTRY_SLOTS.length) + i;
            List<Country> countries = plugin.getCountries();
            countries.sort(Comparator.comparing(Country::getName, String.CASE_INSENSITIVE_ORDER));
            return index >= 0 && index < countries.size() ? countries.get(index) : null;
        }
        return null;
    }

    private ItemStack createCountryItem(Country country) {
        boolean hasHome = country.hasHome() && plugin.getCountryHome(country) != null;
        List<String> lore = new ArrayList<>();
        lore.add("&7Owner: &f" + plugin.safeOfflineName(country.getOwnerId()));
        lore.add("&7Members: &f" + country.getMembers().size());
        lore.add("&7Join status: &f" + (country.isOpen() ? "Open" : "Closed"));
        lore.add("&7Territory: &f" + plugin.describeCountryTerritory(country));
        lore.add("&7Home: " + (hasHome ? "&aAvailable" : "&cNot set or world missing"));
        lore.add("");
        lore.add(hasHome ? "&eClick to warp" : "&cWarp unavailable");
        return createSimpleItem(hasHome ? Material.LODESTONE : Material.BARRIER, "&e" + country.getName(), lore);
    }

    private ItemStack createGlobalSpawnItem(Player player) {
        boolean configured = plugin.getWorldSpawnLocation() != null;
        org.bukkit.Location spawn = plugin.getPrimarySpawnLocation(player);
        List<String> lore = new ArrayList<>();
        lore.add("&7Type: &f" + (configured ? "Saved global spawn" : "Fallback world spawn"));
        lore.add("&7World: &f" + spawn.getWorld().getName());
        lore.add("&7Coords: &f" + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ());
        lore.add("");
        lore.add("&eClick to warp");
        return createSimpleItem(Material.COMPASS, "&bGlobal Spawn", lore);
    }

    private ItemStack createSimpleItem(Material material, String displayName, List<String> loreLines) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(plugin.legacyComponent(displayName));
        itemMeta.lore(loreLines.stream().map(plugin::legacyComponent).toList());
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack filler = createSimpleItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
    }

    private record CountryWarpMenuHolder(UUID playerId, int page) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
