# Terra Quest HUD ItemsAdder Content

This folder is the canonical ItemsAdder content pack for Terra's HUD and hotbar presentation.

## Install

Copy this folder to:

```text
plugins/ItemsAdder/contents/terra_quest_hud
```

Then run:

```text
/iazip
```

## Required layout

```text
configs/terra_quest_hud.yml
textures/font/hud/top_status/
resourcepack/assets/terrahud/textures/font/hud/top_status/
resourcepack/assets/minecraft/
```

## What each path is for

- `textures/font/hud/top_status/`
  Source panel images stored next to the content config.
- `resourcepack/assets/terrahud/textures/font/hud/top_status/`
  Runtime namespace path for `terrahud:*` font images.
- `resourcepack/assets/minecraft/`
  Vanilla HUD overrides such as boss bar, hotbar, hearts, hunger, and XP bar.

## Important rules

- Do not duplicate these files into `resourcepack/minecraft/` or `resourcepack/terrahud/`.
- Keep the active top status panel PNGs mirrored into `resourcepack/assets/terrahud/textures/font/hud/top_status/`.
- If those mirrored files are missing, ItemsAdder will fail to resolve the font images.

## Related Terra config

The runtime toggle and layout settings for the top status HUD live in:

- `plugins/testproject/settings/core.yml`

Relevant section:

- `itemsadder-top-status`
