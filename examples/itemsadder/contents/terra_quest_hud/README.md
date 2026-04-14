# Terra Quest HUD ItemsAdder Content

Use this folder as the single source of truth for the in-game ItemsAdder HUD.

Folder layout:

```text
configs/terra_quest_hud.yml
textures/font/hud/top_status/
resourcepack/assets/minecraft/
```

What goes where:

- `textures/font/hud/top_status/` contains the `terrahud` panel images used by `font_images`.
- `resourcepack/assets/minecraft/` contains vanilla overrides such as boss bar, hotbar, hearts, hunger, XP bar, and custom font files.

Do not duplicate these files into:

- `resourcepack/minecraft/`
- `resourcepack/terrahud/`
- `resourcepack/assets/terrahud/`

Install by copying this folder to:

```text
plugins/ItemsAdder/contents/terra_quest_hud
```

Then run `/iazip`.
