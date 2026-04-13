# BetterHud Quest HUD

This project now exposes quest data through PlaceholderAPI and does not need the old ItemsAdder HUD sync for the main quest tracker.

Use these Terra placeholders in BetterHud:

- `[terra_quest_title_plain]`
- `[terra_quest_objective_plain]`
- `[terra_quest_progress]`
- `[terra_quest_status]`
- `[terra_quest_progress_bar]`
- `[terra_quest_hint_plain]`
- `[terra_quest_percent]`
- `[terra_quest_current]`
- `[terra_quest_target]`
- `[terra_quest_steps]`
- `[terra_quest_max_steps]`
- `[terra_quest_active]`

## Files To Copy

Copy these repo files into your BetterHud plugin folder:

- `examples/betterhud/huds/terra_quest_hud.yml` -> `plugins/BetterHud/huds/terra_quest_hud.yml`
- `examples/betterhud/huds/terra_status_hud.yml` -> `plugins/BetterHud/huds/terra_status_hud.yml`
- `examples/betterhud/layouts/terra_quest_layout.yml` -> `plugins/BetterHud/layouts/terra_quest_layout.yml`
- `examples/betterhud/layouts/terra_status_layout.yml` -> `plugins/BetterHud/layouts/terra_status_layout.yml`
- `examples/betterhud/texts/terra_quest_font.yml` -> `plugins/BetterHud/texts/terra_quest_font.yml`
- `examples/betterhud/images/terra_quest_images.yml` -> `plugins/BetterHud/images/terra_quest_images.yml`
- `examples/betterhud/images/terra_status_images.yml` -> `plugins/BetterHud/images/terra_status_images.yml`
- `examples/betterhud/assets/quest/*` -> `plugins/BetterHud/assets/quest/*`
- `examples/betterhud/assets/status/*` -> `plugins/BetterHud/assets/status/*`

## BetterHud Config

In `plugins/BetterHud/config.yml`, add the Terra HUD to `default-hud`:

```yml
default-hud:
  - terra_status_hud
  - terra_quest_hud
```

If you want only the quest tracker, use:

```yml
default-hud:
  - terra_quest_hud
```

For self-hosting, do not leave `self-host-ip` as `'*'`. Use your real public IP or domain:

```yml
enable-self-host: true
self-host-ip: 185.206.149.16
self-host-port: 8071
```

## Reload

Run a full server restart after changing BetterHud files. If you only reload the plugin, the pack or HUD registration can stay stale.

## Position

The sample quest HUD is anchored to the top-left using:

- HUD `gui` position `0,0`
- pixel offset `18,18`

Positioning works like this:

- `gui.x` and `gui.y` are screen anchors in percent
- `0,0` = top-left
- `50,50` = center
- `100,100` = bottom-right
- `pixel.x` and `pixel.y` are fine offsets from that anchor

Examples:

- top-left tighter: `gui 0,0` and `pixel 8,8`
- top-left lower: `gui 0,0` and `pixel 18,32`
- center: `gui 50,50` and then use negative `pixel.x` and `pixel.y` to pull the panel back up/left

The sample styling uses layered dark text blocks to fake a smooth black panel behind the quest text. If your font pack does not render symbols like `✦` or `▸` cleanly, replace them with plain ASCII markers.

The sample status HUD is anchored to the left-middle of the screen and uses vertical numeric rows:

- `HP [health]/[max_health]`
- `HUNGER [food]/20`

It does not use vanilla heart or hunger icons.

## Notes

- Terra now registers native BetterHud placeholders, so use bracket syntax like `[terra_quest_title_plain]`.
- Terra already returns fallback text when no active quest exists, so the HUD will not be blank.
- `quests.itemsadder-sync.enabled` now defaults to `false` in Terra's `settings/quests.yml`.
- The status HUD uses BetterHud built-ins: `[health]`, `[max_health]`, and `[food]`.
