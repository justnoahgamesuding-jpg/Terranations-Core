# BetterHud Quest HUD

This project now exposes quest data through PlaceholderAPI and does not need the old ItemsAdder HUD sync for the main quest tracker.

Use these Terra placeholders in BetterHud:

- `%terra_quest_title_plain%`
- `%terra_quest_objective_plain%`
- `%terra_quest_progress%`
- `%terra_quest_hint_plain%`
- `%terra_quest_percent%`
- `%terra_quest_current%`
- `%terra_quest_target%`
- `%terra_quest_steps%`
- `%terra_quest_max_steps%`

## Files To Copy

Copy these repo files into your BetterHud plugin folder:

- `examples/betterhud/huds/terra_quest_hud.yml` -> `plugins/BetterHud/huds/terra_quest_hud.yml`
- `examples/betterhud/layouts/terra_quest_layout.yml` -> `plugins/BetterHud/layouts/terra_quest_layout.yml`
- `examples/betterhud/texts/terra_quest_font.yml` -> `plugins/BetterHud/texts/terra_quest_font.yml`

## BetterHud Config

In `plugins/BetterHud/config.yml`, add the Terra HUD to `default-hud`:

```yml
default-hud:
  - test_hud
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

The sample HUD is anchored to the top-left of the screen using:

- HUD `gui` position `0,0`
- pixel offset `12,12`

Adjust these in `terra_quest_hud.yml` if you want it tighter or lower.

## Notes

- BetterHud can render PlaceholderAPI placeholders directly. Use `%terra_quest_title_plain%`, not bracket syntax.
- Terra already returns fallback text when no active quest exists, so the HUD will not be blank.
- `quests.itemsadder-sync.enabled` now defaults to `false` in Terra's `settings/quests.yml`.
