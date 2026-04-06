package me.meetrow.testproject;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class StabilityGuiListener implements Listener {
    private static final int[] MATERIAL_SLOTS = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    };
    private static final int PREVIOUS_SLOT = 45;
    private static final int INFO_SLOT = 49;
    private static final int ADD_SLOT = 50;
    private static final int NEXT_SLOT = 53;

    private final Testproject plugin;

    public StabilityGuiListener(Testproject plugin) {
        this.plugin = plugin;
    }

    public void openSupportMaterialsMenu(Player player, int page) {
        List<Material> materials = plugin.getConfiguredStructuralSupportMaterials();
        int maxPage = Math.max(0, (materials.size() - 1) / MATERIAL_SLOTS.length);
        int safePage = Math.max(0, Math.min(page, maxPage));
        Inventory inventory = Bukkit.createInventory(
                new SupportMaterialsHolder(safePage),
                54,
                plugin.colorize("&8Stability Supports")
        );

        for (int slot = 45; slot < 54; slot++) {
            inventory.setItem(slot, createSimpleItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of()));
        }
        inventory.setItem(INFO_SLOT, createSimpleItem(
                Material.SCAFFOLDING,
                "&6Support Materials",
                List.of(
                        "&7Configured blocks: &f" + materials.size(),
                        "&7Left click a block to remove it.",
                        "&7Use the add button with an item in",
                        "&7your cursor or main hand."
                )
        ));
        inventory.setItem(ADD_SLOT, createSimpleItem(
                Material.ANVIL,
                "&aAdd Support Material",
                List.of(
                        "&7Cursor item is used first.",
                        "&7Falls back to main hand.",
                        "&7Adds that block as a support material."
                )
        ));
        inventory.setItem(PREVIOUS_SLOT, createSimpleItem(Material.ARROW, "&ePrevious Page", List.of("&7Go to the previous page.")));
        inventory.setItem(NEXT_SLOT, createSimpleItem(Material.ARROW, "&eNext Page", List.of("&7Go to the next page.")));

        int start = safePage * MATERIAL_SLOTS.length;
        for (int i = 0; i < MATERIAL_SLOTS.length && start + i < materials.size(); i++) {
            Material material = materials.get(start + i);
            inventory.setItem(MATERIAL_SLOTS[i], createSimpleItem(
                    material,
                    "&f" + plugin.formatMaterialName(material),
                    List.of(
                            "&7Material: &f" + material.name(),
                            "&cClick to remove from support list."
                    )
            ));
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder() instanceof SupportMaterialsHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() != topInventory) {
            return;
        }

        int slot = event.getRawSlot();
        if (slot == PREVIOUS_SLOT) {
            openSupportMaterialsMenu(player, holder.page - 1);
            return;
        }
        if (slot == NEXT_SLOT) {
            openSupportMaterialsMenu(player, holder.page + 1);
            return;
        }
        if (slot == ADD_SLOT) {
            ItemStack source = event.getCursor();
            if (source == null || source.getType().isAir()) {
                source = player.getInventory().getItemInMainHand();
            }
            if (source == null || source.getType().isAir() || !source.getType().isBlock()) {
                player.sendMessage(plugin.colorize("&cHold or pick up a block item first."));
                return;
            }
            if (plugin.addStructuralSupportMaterial(source.getType())) {
                player.sendMessage(plugin.colorize("&aAdded &f" + plugin.formatMaterialName(source.getType()) + "&a as a support material."));
            } else {
                player.sendMessage(plugin.colorize("&e" + plugin.formatMaterialName(source.getType()) + " is already a support material."));
            }
            openSupportMaterialsMenu(player, holder.page);
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir() || !clicked.getType().isBlock()) {
            return;
        }
        if (plugin.removeStructuralSupportMaterial(clicked.getType())) {
            player.sendMessage(plugin.colorize("&cRemoved &f" + plugin.formatMaterialName(clicked.getType()) + "&c from support materials."));
            openSupportMaterialsMenu(player, holder.page);
        }
    }

    private ItemStack createSimpleItem(Material material, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.colorize(name));
            if (loreLines != null && !loreLines.isEmpty()) {
                List<String> lore = new ArrayList<>();
                for (String line : loreLines) {
                    lore.add(plugin.colorize(line));
                }
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private record SupportMaterialsHolder(int page) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
