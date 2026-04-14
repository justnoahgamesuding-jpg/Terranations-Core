# ItemsAdder Top Status HUD

This HUD is the top-center status strip for location text on a custom ItemsAdder panel.

## Files to copy

Copy this folder into your ItemsAdder plugin folder:

```text
examples/itemsadder/contents/terra_quest_hud
```

Target location:

```text
plugins/ItemsAdder/contents/terra_quest_hud
```

Canonical structure inside that folder:

```text
configs/terra_quest_hud.yml
textures/font/hud/top_status/
resourcepack/assets/minecraft/
```

Keep the HUD panel images only in `textures/font/hud/top_status/`.
Keep vanilla sprite and font overrides only in `resourcepack/assets/minecraft/`.
The old duplicate folders `resourcepack/minecraft`, `resourcepack/terrahud`, and `resourcepack/assets/terrahud` should not be used.

Then run:

```text
/iazip
```

Reload the ItemsAdder pack on the client after the new pack is generated.

## Terra config

The runtime settings are in:

```text
plugins/testproject/settings/core.yml
```

Current section:

```yml
itemsadder-top-status:
  enabled: true
  require-itemsadder: true
  update-ticks: 20

  tokens:
    panel: ":top_status_panel:"
    content-offset: ":offset_-248:"
    location-panel: ":top_status_location_panel:"
    location-offset: ":offset_-112:"

  layout:
    panel-width-pixels: 64
    location-panel-width-pixels: 96
    panel-gap-pixels: 8

  format: "auto"
  wilderness-label: "Wilderness"
  max-location-chars: 7
```

If the location text is not lined up on the panel, adjust the location offset token.
Use a more negative value to move the text left, for example `:offset_-108:`.
Use a less negative value to move the text right, for example `:offset_-92:`.

The HUD panel images are:

```text
examples/itemsadder/contents/terra_quest_hud/textures/font/hud/top_status/panel.png
examples/itemsadder/contents/terra_quest_hud/textures/font/hud/top_status/location_panel.png
examples/itemsadder/contents/terra_quest_hud/textures/font/hud/top_status/job_panel.png
examples/itemsadder/contents/terra_quest_hud/textures/font/hud/top_status/money_panel.png
```

The HUD uses the vanilla bossbar text position for top-screen placement. The ItemsAdder pack includes transparent white bossbar sprites so the vanilla white bossbar texture does not show behind the panel:

```text
examples/itemsadder/contents/terra_quest_hud/resourcepack/assets/minecraft/textures/gui/sprites/boss_bar/white_background.png
examples/itemsadder/contents/terra_quest_hud/resourcepack/assets/minecraft/textures/gui/sprites/boss_bar/white_progress.png
```
