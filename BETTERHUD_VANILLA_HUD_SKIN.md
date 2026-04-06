# BetterHud Vanilla HUD Skin

This pack changes the vanilla HUD sprites to a cleaner MMORPG-style look closer to the reference you sent.

Files are here:

- `examples/betterhud/resourcepack/assets/minecraft/textures/gui/sprites/hud/hotbar.png`
- `examples/betterhud/resourcepack/assets/minecraft/textures/gui/sprites/hud/hotbar_selection.png`
- `examples/betterhud/resourcepack/assets/minecraft/textures/gui/sprites/hud/hotbar_offhand_left.png`
- `examples/betterhud/resourcepack/assets/minecraft/textures/gui/sprites/hud/hotbar_offhand_right.png`
- `examples/betterhud/resourcepack/assets/minecraft/textures/gui/sprites/hud/experience_bar_background.png`
- `examples/betterhud/resourcepack/assets/minecraft/textures/gui/sprites/hud/experience_bar_progress.png`
- `examples/betterhud/resourcepack/assets/minecraft/textures/gui/sprites/hud/food_*.png`
- `examples/betterhud/resourcepack/assets/minecraft/textures/gui/sprites/hud/heart/*.png`

## Copy Path

Copy the `examples/betterhud/resourcepack/assets/minecraft` folder into your BetterHud pack source so it ends up under:

- `plugins/BetterHud/assets/minecraft/textures/gui/sprites/hud/...`

If you keep your BetterHud assets in another merged pack folder, copy the same `assets/minecraft/...` tree there instead.

## Important Setting

If you want the skinned vanilla hotbar to show, BetterHud must not hide it:

```yml
remove-default-hotbar: false
```

If this stays `true`, your new hotbar sprites can exist and still not be visible.

## Reload

After copying the assets:

1. Restart the server
2. Re-apply the BetterHud resource pack if needed

## Notes

- This is a starter skin, not a final polished MMO pack.
- The quest card position is still controlled separately by `terra_quest_hud.yml`.
