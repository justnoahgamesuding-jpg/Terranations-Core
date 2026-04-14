# Terra Quest HUD ItemsAdder Content

Use this folder as the single source of truth for the in-game ItemsAdder HUD.

Folder layout:

```text
configs/terra_quest_hud.yml
textures/font/hud/top_status/
resourcepack/assets/terrahud/textures/font/hud/top_status/
resourcepack/assets/minecraft/
```

What goes where:

- `textures/font/hud/top_status/` keeps the source panel images with the content config.
- `resourcepack/assets/terrahud/textures/font/hud/top_status/` is the runtime namespace path ItemsAdder actually reads for `terrahud:*` font images.
- `resourcepack/assets/minecraft/` contains vanilla overrides such as boss bar, hotbar, hearts, hunger, and XP bar.

Do not duplicate these files into:

- `resourcepack/minecraft/`
- `resourcepack/terrahud/`

Do keep the active top status panel PNGs mirrored in `resourcepack/assets/terrahud/textures/font/hud/top_status/`, otherwise ItemsAdder will log `Image not found for font_image 'terrahud:...'`.

Install by copying this folder to:

```text
plugins/ItemsAdder/contents/terra_quest_hud
```

Then run `/iazip`.
