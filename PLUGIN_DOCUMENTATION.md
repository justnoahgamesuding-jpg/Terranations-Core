# Terra Plugin Documentation

This file is the current admin and systems reference for the plugin snapshot in this repository.

## Overview

Terra combines these major systems:

- profession progression with enforced specialization
- country management with roles, treasury, upgrades, boosts, and territory
- climate-aware farming and sapling growth
- structural stability and support logic
- trader routes and merchant waves
- Terra-owned balance storage
- a locked player guide item in hotbar slot 9 for future gameplay navigation
- staff, maintenance, lag-reduction, and rollback utilities

## Command Surface

### `/terra`

Primary admin root. Most subcommands require `terra.admin`.

- `blockdelay`
  Enables, disables, or retimes the shared break/place cooldown system.
- `blockvalue`
  Lists or edits block XP and money rewards.
- `bypass` and `bypasslist`
  Manages block-delay bypass players.
- `wildernessregen`
  Shows or edits wilderness restoration and build-decay timings.
- `setxpboost`
  Starts or stops a global profession XP boost.
- `cleardata <player>`
  Resets Terra-owned player data and forces fresh profession setup.
- `jobcap <job> <amount>`
  Sets per-job population caps.
- `playtest`
  Starts, extends, stops, or inspects playtest sessions.
- `cooldowndebug`
  Toggles admin cooldown bossbars.
- `setworldspawn`
  Saves the plugin-owned global spawn point.
- `realtimeclock`
  Enables, disables, checks, or syncs real-time world time.
- `hungerspeed`
  Shows or changes the global hunger multiplier.
- `lag`
  Controls built-in item clearing, item merging, and mob stacking.
- `maintenance`
  Controls maintenance mode and the maintenance allowlist.
- `stability`
  Controls structural stability settings, debug tools, meter visibility, and support-material management.
- `items`
  Lists and toggles restricted functional materials, ender pearls, and shulker boxes.
- `rewards`
  Enables or disables block-break XP and money rewards.
- `hostilemobs`
  Toggles hostile mob spawning.
- `phantoms`
  Toggles phantom spawning.
- `orevision`
  Toggles admin ore-vision overlays.
- `fixedore` and `fixedoretool`
  Manages fixed ore nodes and the remover wand.
- `reload`
  Reloads Terra configs, cached state, integrations, and item metadata refresh passes.
- `hardrestart`
  Runs the plugin’s staged hard-restart workflow.

Notes:

- Break and place cooldowns are separate.
- `/terra reload` also refreshes climate-lore items and soulbound starter items in loaded inventories.
- The Terra Guide item is restored to slot 9 for online players and stays locked there.
- `lag clearitems` intentionally excludes Terra trader and merchant entities.
- The stability meter always runs in the background even when its chat display is off.

### `/jobs`

Player and admin profession root.

- `/jobs`, `/jobs open`
  Opens the profession GUI.
- `/jobs info`
  Shows current, primary, and secondary profession state.
- `/jobs switch <job>`
  Switches to an owned job.
- `/jobs admin ...`
  Lets admins inspect and edit another player’s profession data.

### `/country`

Country root for membership, territory, and progression.

Core membership:

- `create`
- `setowner`
- `join`
- `invite`
- `acceptinvite`
- `leave`
- `kick`
- `disband`
- `joinstatus`
- `transfercountry`
- `accepttransfer`
- `rename`
- `addtag`
- `addtagtocountry`
- `chat`

Country utility:

- `info`
- `list`
- `home`
- `sethome`
- `farmland`
- `borders`

Country economy and progression:

- `balance`
  Shows treasury balance, resources, and active country boost.
- `deposit <money>`
  Moves Terra money from the player into the country treasury.
- `contribute [amount]`
  Contributes resource items from the player’s hand into country resources.
- `upgrade`
  Opens the country progression and upgrade GUI.
- `boost <key>`
  Activates a country boost if the treasury and resources allow it.
- `role <player> <co-owner|steward|member>`
  Updates country member roles.

Country management and trade:

- `manage sethome`
- `manage settraderspawn`
- `trade allow <country>`
- `trade remove <country>`
- `trade list`

Territory and admin:

- `territory setregion <world> <region-id> <country>`
- `territory clear <country>`
- `territory sync <country>`
- `territory info <country>`
- `territory debug <country>`
- `admin <country>`
- `settraderreputation <country> <value>`

Notes:

- `/country` with no arguments opens the main country GUI.
- Country homes and trader spawns must be placed inside the country’s linked territory.
- Farmland limits depend on linked territory and current member count.

### `/climate`

Admin root for the climate system.

- `check`
- `status`
- `enable <on|off>`
- `unit <C|F>`
- `seasons <on|off>`
- `season <auto|spring|summer|autumn|winter>`
- `bossbar <on|off>`
- `freeze <on|off>`
- `display <on|off>`
- `altitude optimal <y>`
- `playtest on|off|here|center|radius|temps`
- `create`
- `create fullworld`
- `refresh`
- `clear`
- `clear all`
- `crops`

Notes:

- The climate model blends latitude, weather, altitude, and biome data.
- `/climate create` uses the current WorldEdit selection.
- `/climate create fullworld` uses the world border instead.

### `/trader`

Admin route-trader control root.

- `status`
- `time status`
- `time next <minutes>`
- `time active <minutes>`
- `spawn [profession]`
- `remove`
- `open`

### `/merchant`

Admin merchant-wave control root.

- `status`
- `time status`
- `time next <minutes>`
- `time active <minutes>`
- `spawn`
- `remove`
- `open`
- `manage`

### Standalone commands

- `/countrychat` and `/cc`
  Toggles country chat mode.
- `/globalchat` and `/gc`
  Toggles access to `!message` global chat.
- `/staff`
  Opens staff tools and supports mode, vanish, freeze, teleport, and bring actions.
- `/flyspeed <0-10>`
  Admin fly-speed shortcut.
- `/vanish [on|off|toggle]`
  Admin vanish shortcut.
- `/spawn`
  Admin country warp GUI and direct country warp command.
- `/balance`
  Shows Terra balance or performs admin balance edits.
- `/rollbackarea <radius> <time>`
  Sends a local CoreProtect rollback around the sender.
- `/undoarea <radius> <time>`
  Sends the matching CoreProtect restore around the sender.

## Main Systems

### Professions

The active professions are:

- Miner
- Lumberjack
- Farmer
- Builder
- Blacksmith

Key behavior:

- New players are forced through profession selection.
- Primary and secondary professions are tracked separately.
- Job caps are configurable.
- Starter kits and many progression rules come from `src/main/resources/jobs/`.
- Farmer and Lumberjack have instant-growth proc systems.
- Blacksmith and Farmer use furnace collaboration and output tagging.

### Terra Guide

- Every player receives a `Terra Guide` nether star in hotbar slot 9.
- The item is soulbound and additionally locked to that slot.
- It cannot be moved, dropped, hotbar-swapped, offhand-swapped, or placed into containers.
- Right-clicking it opens the main player hub.
- The current guide includes player stats, jobs access, personal skills, contracts, country access, and ore-sense toggles.

### Personal Skills And Work Orders

- Players earn skill points from profession level-ups and from playtime.
- Skill points can be spent on health, XP gain, cooldown reduction, trader bonuses, merchant cooldown reduction, double-drop bonuses, growth proc bonuses, and ore-sense unlocks.
- The guide also exposes a personal work-order system that rewards money and skill points for profession XP progress.

### Countries

Countries now include more than membership:

- owner, co-owner, steward, and member roles
- join state, invites, tags, home, and territory
- treasury balance and resource stockpile
- trader spawn and allowed trade-country list
- upgrades, level progression, and timed boosts

Important effects:

- Country upgrades can change farmland capacity, cooldowns, profession XP bonuses, and trader or money bonuses.
- Country state updates trigger territory sync and tag refresh logic.
- Country dashboards and admin GUIs are the primary management surface.

### Climate and Farming

Climate uses:

- latitude
- humidity
- continentality
- current influence
- altitude
- season
- day/night
- rainfall
- biome adaptation

Gameplay effects:

- crop and sapling growth is climate-aware
- recent rain gives a temporary growth bonus
- cold climates can freeze water
- crop, seed, sapling, and propagule items can carry climate lore

### Stability

The stability system is intended to push players toward supported mines and believable structures.

It includes:

- loose soil, packed soil, soft rock, fragile roof, and hard rock classes
- support radius and support-frame logic
- shaft, wall, roof, dock, span, and column checks
- rain and nearby-water stress
- collapse rubble and warning feedback
- a hidden player stability meter with temporary debug-vision bursts

### Chat

- Local chat is the default and stays within range.
- Country chat is toggle-based.
- Global chat is opt-in and uses `!message`.
- Chat sound feedback is configurable in `settings/core.yml`.

### Economy

The plugin owns its own money storage and country treasury logic.

- Player balances live in `data.yml`.
- Country treasury and progression state live in `countries/data.yml`.
- Block rewards, trader rewards, and some upgrades scale from config.

### Staff and Server Utilities

The plugin includes:

- staff mode and vanish
- teleport and freeze tools
- global spawn control
- maintenance mode
- lag reduction
- custom join or leave messages
- plugin-list protection
- CoreProtect rollback dispatch helpers

## PlaceholderAPI

Terra exposes PlaceholderAPI placeholders for scoreboards, menus, and chat integrations.

Common placeholders include:

- `%terra_balance%`
- `%terra_player_country%`
- `%terra_player_country_level%`
- `%terra_profession_display%`
- `%terra_current_job_level%`
- `%terra_current_job_xp%`
- `%terra_current_job_xp_required%`
- `%terra_server_time%`
- `%terra_server_date%`
- `%terra_server_datetime%`
- `%terra_climate_name%`
- `%terra_climate_temperature%`
- `%terra_climate_season%`

Notes:

- `%terra_current_job_xp%` is remaining XP to the next level, not current earned XP for the level.
- `%terra_server_time%`, `%terra_server_date%`, and `%terra_server_datetime%` follow the same timezone as the real-time world clock configured in `realtime-clock.timezone`.

## Integrations

Optional integrations used by Terra:

- Vault
- PlaceholderAPI
- LuckPerms
- WorldEdit
- WorldGuard
- Dynmap
- CoreProtect

## Recommended Setup Flow

1. Build the plugin and place it on a Paper 1.21 server with Java 21.
2. Review `src/main/resources/settings/core.yml`, `climate.yml`, `stability.yml`, `merchant.yml`, and `territories.yml`.
3. Review the job configs under `src/main/resources/jobs/`.
4. Set a safe global spawn with `/terra setworldspawn`.
5. If using countries with territory, create WorldGuard regions with `//sel poly` and `//expand vert`.
6. Link each region with `/country territory setregion ...` and run `/country territory sync ...`.
7. Set country homes and trader spawns from inside the linked territory.
8. Verify optional integrations you expect to use.

## Related Docs

- Repository overview: [README.md](README.md)
- Player-facing guide: [BEGINNER_GUIDE.md](BEGINNER_GUIDE.md)
- High-level timeline: [UPDATES.md](UPDATES.md)
- Detailed ledger: [PATCH_NOTES.md](PATCH_NOTES.md)
