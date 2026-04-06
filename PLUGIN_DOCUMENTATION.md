# Terra Plugin Documentation

This file documents the current command surface, player/admin systems, and integration points implemented by this plugin.

## Overview

The plugin adds multiple command roots:

- `/terra` for admin controls and general server controls
- `/jobs` for the profession/jobs system
- `/trader` for admin traveling-trader controls
- `/merchant` for admin merchant-wave controls
- `/climate` for climate debugging, visualization, and tuning
- `/country` for country ownership, membership, territory, tags, farmland, and homes
- `/cc` for toggling country-chat mode
- `/gc` for toggling global-chat access
- `/staff` for staff tools, moderation utilities, and the staff menu
- `/flyspeed` for direct admin fly speed control
- `/vanish` for direct admin invisibility control
- `/spawn` for admin country warp and global spawn support
- `/balance` for Terra-owned player balance viewing/management

It also adds multiple passive systems:

- profession selection and progression
- profession-based action restrictions
- starter kits and soulbound starter-kit protection
- block delay and bypass handling
- separate break and place cooldown tracking
- player-facing cooldown HUD above the hotbar
- wilderness regeneration and build decay
- country territory integration with WorldGuard and Dynmap
- country tags shown on player names
- country territory enter/leave notifications
- local, country, and global chat routing
- farmland limits inside country territory
- climate, seasons, rainfall, altitude, and temperature-driven farming
- biome-aware climate adaptation for custom biome datapacks and worlds
- sapling climate profiles and climate-aware tree growth
- furnace locking and smelting collaboration
- terrain stability, cave-ins, and support structures
- lag reduction utilities including timed item clears, item merge, and conservative mob stacking
- maintenance mode with MOTD override and join allowlist
- custom join and leave messages
- vanish, staff mode, freeze tools, and staff-player management actions
- plugin list protection
- PlaceholderAPI placeholders
- optional real-time clock world syncing

## Commands

### `/terra`

Primary admin root. Its subcommands require `terra.admin`.

| Command | Permission | What it does |
| --- | --- | --- |
| `/terra blockdelay enable` | `terra.admin` | Enables the block/action cooldown system. |
| `/terra blockdelay disable` | `terra.admin` | Disables the block/action cooldown system. |
| `/terra blockdelay time set <seconds>` | `terra.admin` | Sets the global block delay cooldown in seconds. |
| `/terra cooldowndebug` | `terra.admin` | Toggles live bossbars for the sender showing break and place cooldown states separately. |
| `/terra blockvalue list [page]` | `terra.admin` | Lists stored block XP/money rewards. |
| `/terra blockvalue <block>` | `terra.admin` | Shows the configured reward for one block. |
| `/terra blockvalue <block> set xp <amount>` | `terra.admin` | Sets the XP reward for a block. |
| `/terra blockvalue <block> set money <amount>` | `terra.admin` | Sets the money reward for a block. |
| `/terra bypass <player>` | `terra.admin` | Toggles block-delay bypass for a player. |
| `/terra bypasslist` | `terra.admin` | Lists all active bypass players, time enabled, and LuckPerms prefix if available. |
| `/terra wildernessregen` | `terra.admin` | Shows current wilderness break/build timers. |
| `/terra wildernessregen set break <seconds>` | `terra.admin` | Sets how long broken wilderness blocks take to regenerate. |
| `/terra wildernessregen set build <seconds>` | `terra.admin` | Sets how long placed wilderness blocks remain before removal. |
| `/terra setxpboost <amount> <time>` | `terra.admin` | Starts a global profession XP boost for all players using one of the fixed multipliers. |
| `/terra setxpboost off` | `terra.admin` | Ends the active global profession XP boost. |
| `/terra cleardata <player>` | `terra.admin` | Resets a player's stored plugin data, clears fresh-start state for online players, teleports them to spawn, and forces job selection again. |
| `/terra jobcap <job> <amount>` | `terra.admin` | Sets the maximum number of players who can hold a specific job. `0` removes the cap for that job. |
| `/terra playtest start <time>` | `terra.admin` | Runs a 30-second countdown, resets profession selection for testing, and starts a timed playtest session. |
| `/terra playtest extend <time>` | `terra.admin` | Adds more time to a pending playtest countdown or an active playtest. |
| `/terra playtest status` | `terra.admin` | Shows whether a playtest countdown is active, a playtest is running, or the system is inactive. |
| `/terra playtest stop` | `terra.admin` | Starts the playtest shutdown flow and ends the active playtest early. |
| `/terra setworldspawn` | `terra.admin` | Saves a persistent global spawn location from the sender's current position for plugin-controlled teleports and resets. |
| `/terra realtimeclock <on\|off\|status\|sync>` | `terra.admin` | Controls the real-time day/night system based on the configured timezone. |
| `/terra hungerspeed <multiplier\|status>` | `terra.admin` | Sets or shows the global hunger drain multiplier. |
| `/terra lag <status\|clearitems\|stacknow\|itemclear\|mobstack\|itemmerge>` | `terra.admin` | Manages the built-in lag-reduction systems, including item clears, item merge, and mob stacking. |
| `/terra maintenance <on\|off\|status\|add\|remove\|list>` | `terra.admin` | Controls maintenance mode and the player allowlist that can still join while it is active. |
| `/terra stability <status\|enable\|disable\|scan\|debug\|meter\|radius\|delay\|span\|supportradius\|debugradius\|supports\|strictness>` | `terra.admin` | Manages the terrain stability, structure rules, support-material GUI, and stability-meter tools. |
| `/terra rewards enable` | `terra.admin` | Enables block break rewards. |
| `/terra rewards disable` | `terra.admin` | Disables block break rewards. |
| `/terra rewards money enable` | `terra.admin` | Enables money rewards from block breaking. |
| `/terra rewards money disable` | `terra.admin` | Disables money rewards from block breaking. |
| `/terra hostilemobs enable` | `terra.admin` | Allows hostile mob spawning again. |
| `/terra hostilemobs disable` | `terra.admin` | Cancels hostile monster spawns. |
| `/terra phantoms enable` | `terra.admin` | Allows phantom spawning again. |
| `/terra phantoms disable` | `terra.admin` | Cancels phantom spawns. |
| `/terra reload` | `terra.admin` | Reloads config, custom YAML files, integrations, territory sync, item metadata refresh, and profession GUI state. |
| `/terra hardrestart` | `terra.admin` | Calls the server restart flow, then after boot runs `/reload`, `/reload confirm`, and `/terra reload`. |

#### `/terra` behavior notes

- Break and place cooldowns are independent.
- Normal players see live break and place cooldown timers above the hotbar while one or both cooldowns are active.
- `/terra setworldspawn` controls where plugin-driven spawn teleports go, including reset flows.
- Active playtests and active global XP boosts are persisted across `/reload` and full restarts.
- `/terra reload` now also refreshes unlored or unnamed descriptive climate items and soulbound starter-kit items in online inventories, ender chests, and loaded container inventories.
- `/terra realtimeclock` uses the configured timezone and is intended to keep real day as day and real night as night in the configured worlds.
- `/terra stability meter <on|off|status>` controls only the player's chat readout for the stability meter. The hidden meter itself always progresses in the background.
- `/terra lag clearitems` intentionally skips the custom trader and merchant entities.
- Maintenance mode changes the server-list MOTD before login and only allows listed players to join.

### `/trader`

Admin traveling-trader root. Subcommands require `terra.admin`.

| Command | Permission | What it does |
| --- | --- | --- |
| `/trader status` | `terra.admin` | Shows whether a traveling trader is active and how long remains. |
| `/trader time status` | `terra.admin` | Shows active and next-spawn timing info. |
| `/trader time next <minutes>` | `terra.admin` | Sets the next trader spawn delay. |
| `/trader time active <minutes>` | `terra.admin` | Sets remaining active time for the current trader. |
| `/trader spawn [profession]` | `terra.admin` | Spawns the traveling trader immediately, optionally forcing a specialty profession. |
| `/trader remove` | `terra.admin` | Removes the active traveling trader. |
| `/trader open` | `terra.admin` | Opens the trader menu for the sender. |

### `/merchant`

Admin wandering-merchant root. Subcommands require `terra.admin`.

| Command | Permission | What it does |
| --- | --- | --- |
| `/merchant status` | `terra.admin` | Shows whether a merchant wave is active and how long remains. |
| `/merchant time status` | `terra.admin` | Shows active and next-spawn timing info. |
| `/merchant time next <minutes>` | `terra.admin` | Sets the next merchant-wave spawn delay. |
| `/merchant time active <minutes>` | `terra.admin` | Sets remaining active time for the current merchant wave. |
| `/merchant spawn` | `terra.admin` | Spawns the merchant wave immediately for one selected host country. |
| `/merchant remove` | `terra.admin` | Removes the active merchant wave. |
| `/merchant open` | `terra.admin` | Opens the merchant menu for the sender. |
| `/merchant manage` | `terra.admin` | Opens the merchant admin GUI for editing timing, buy rotations, sell goods, prices, and amounts. |

### `/climate`

Admin climate root. Subcommands require `terra.admin`.

| Command | Permission | What it does |
| --- | --- | --- |
| `/climate check` | `terra.admin` | Shows the local climate name, season, temperature, growth multiplier, rain state, variation, humidity, continentality, current effect, altitude effect, and whether the player is inside a debug area. |
| `/climate status` | `terra.admin` | Shows current climate system settings, unit, season mode, bossbar state, freeze state, live display state, altitude target, playtest settings, and local debug-area info if applicable. |
| `/climate enable <on\|off>` | `terra.admin` | Enables or disables the climate system. |
| `/climate unit <C\|F>` | `terra.admin` | Changes the display unit between Celsius and Fahrenheit. |
| `/climate seasons <on\|off>` | `terra.admin` | Enables or disables automatic seasonal changes. |
| `/climate season <auto\|spring\|summer\|autumn\|winter>` | `terra.admin` | Forces a season or returns the system to automatic season calculation. |
| `/climate bossbar <on\|off>` | `terra.admin` | Enables or disables the climate bossbar. |
| `/climate freeze <on\|off>` | `terra.admin` | Enables or disables climate-based water freezing. |
| `/climate display <on\|off>` | `terra.admin` | Toggles a live local particle visualization of the generated climate around the player. |
| `/climate altitude optimal <y>` | `terra.admin` | Sets the optimal farming altitude. |
| `/climate playtest on` | `terra.admin` | Enables the dedicated playtest climate mode. |
| `/climate playtest off` | `terra.admin` | Disables the dedicated playtest climate mode. |
| `/climate playtest here` | `terra.admin` | Uses the sender's current location as the playtest climate center. |
| `/climate playtest center <x> <z>` | `terra.admin` | Sets the playtest climate center explicitly. |
| `/climate playtest radius <blocks>` | `terra.admin` | Sets the playtest climate radius. |
| `/climate playtest temps <centerC> <edgeC>` | `terra.admin` | Sets the center and edge temperatures for playtest climate mode. |
| `/climate create` | `terra.admin` | Registers a climate particle preview using the player's current WorldEdit selection. |
| `/climate create fullworld` | `terra.admin` | Registers a full-world climate particle preview bounded by the current world border. |
| `/climate refresh` | `terra.admin` | Refreshes the current climate preview area. |
| `/climate clear` | `terra.admin` | Clears the current climate preview area the player is standing in. |
| `/climate clear all` | `terra.admin` | Clears all climate preview areas in the current world. |
| `/climate crops` | `terra.admin` | Opens the climate crop guide GUI with crop, seed, sapling, and propagule sample items. |

#### `/climate` behavior notes

- The climate model is no longer the older center/equator debug model. The current system is latitude-driven with additional regional and weather factors layered on top.
- Temperature is influenced by latitude, humidity, continentality, current influence, altitude, season, day/night, and rain.
- The final result is blended with the world biome's own temperature and humidity so Terralith-, Tectonic-, or other custom-biome worlds fit the climate system better.
- Rain lowers temperature and gives a temporary growth bonus after recent rainfall.
- Going higher makes it colder. Going lower makes it warmer. Moving too far away from the optimal altitude also lowers crop growth.
- Saplings and propagules have climate profiles just like crops.
- `/climate create` requires WorldEdit and an active WorldEdit selection in the player's current world.
- `/climate create fullworld` uses the active world border rather than WorldEdit.
- `/climate display on` is the fastest way to inspect the already-generated climate without creating preview regions.
- `/climate crops` is intended as the inspection GUI for the lore players see on crop and sapling items.

### `/cc`

Country-chat toggle root for players who already have country access.

| Command | Permission | What it does |
| --- | --- | --- |
| `/cc` | any player with country access | Toggles country-chat mode on or off. |
| `/cc on` | any player with country access | Forces country-chat mode on. |
| `/cc off` | any player with country access | Forces country-chat mode off. |

### `/gc`

Global-chat toggle root for players.

| Command | Permission | What it does |
| --- | --- | --- |
| `/gc` | normal player access | Toggles whether the player can use `!message` for global chat. |
| `/gc on` | normal player access | Enables `!message` global chat for the player. |
| `/gc off` | normal player access | Disables `!message` global chat for the player. |

### `/jobs`

Player-facing jobs root. Normal players can use the menu, info, and switch actions. The admin subcommand requires `terra.admin`.

| Command | Permission | What it does |
| --- | --- | --- |
| `/jobs` | none beyond normal access | Opens the profession selection/details GUI. |
| `/jobs open` | none beyond normal access | Same as `/jobs`. |
| `/jobs info` | none beyond normal access | Shows current, primary, and secondary profession info. |
| `/jobs switch <job>` | none beyond normal access | Switches the active job to one the player already owns. |
| `/jobs admin <player>` | `terra.admin` | Opens the admin job management GUI for the target player. |
| `/jobs admin info <player>` | `terra.admin` | Shows another player's profession state. |
| `/jobs admin setprimary <player> <job>` | `terra.admin` | Sets the player's primary job and makes it active. |
| `/jobs admin setsecondary <player> <job>` | `terra.admin` | Sets the player's second job. Primary must already exist. |
| `/jobs admin setactive <player> <job>` | `terra.admin` | Switches a player's active job to one they already own. |
| `/jobs admin setlevel <player> <job> <level>` | `terra.admin` | Sets a player's level in a job they own. |
| `/jobs admin setxp <player> <job> <xp>` | `terra.admin` | Sets a player's stored XP in a job they own. |
| `/jobs admin clearsecondary <player>` | `terra.admin` | Removes the player's secondary job. |
| `/jobs admin clearall <player>` | `terra.admin` | Removes all profession data for the player. |

### `/country`

Player-facing country system with per-command permissions. Ops and players with `terra.country.admin` bypass most ownership restrictions.

| Command | Permission | What it does |
| --- | --- | --- |
| `/country create <country name>` | op or `terra.country.admin` | Creates a country. This is admin-only and no longer appears in the normal player GUI. |
| `/country create <country name> [owner]` | op or `terra.country.admin` | Admins can optionally use the last argument as a target owner. |
| `/country setowner <country name> <player>` | `terra.country.setowner` | Sets a country's owner directly. |
| `/country join <country name>` | `terra.country.join` | Joins an open country. |
| `/country home` | `terra.country.home` | Teleports the player to their country home if one is set and cooldown has expired. |
| `/country invite <player>` | `terra.country.invite` | Invites a player to the sender's country. |
| `/country acceptinvite <country name>` | `terra.country.acceptinvite` | Accepts a pending invite. |
| `/country disband <country name>` | `terra.country.disband` | Disbands a country. Non-admins must own it. |
| `/country joinstatus <open\|closed>` | `terra.country.joinstatus` | Changes whether the sender's country is open or invite-only. |
| `/country leave` | `terra.country.leave` | Leaves the current country. |
| `/country kick <player>` | `terra.country.kick` | Removes a member from the sender's country. |
| `/country chat [on\|off]` | any player with country access | Toggles country-chat mode for normal messages. |
| `/country info [country name]` | `terra.country.info` | Shows owner, join status, tag, members, and territory. Without a name, checks the current location. |
| `/country farmland [country name]` | `terra.country.farmland` | Shows farmland blocks used versus farmland limit. Without a name, checks the current location. |
| `/country list` | `terra.country.list` | Opens the country-browser GUI with filters and sorting. |
| `/country rename <new country name>` | `terra.country.rename` | Renames the sender's country. |
| `/country transfercountry <player>` | `terra.country.transfer` | Sends an ownership transfer request to another member. |
| `/country accepttransfer <player>` | `terra.country.accepttransfer` | Accepts a pending ownership transfer. |
| `/country addtag <tag>` | `terra.country.tag` | Sets the sender's country tag. |
| `/country addtagtocountry <country name> <tag>` | `terra.country.tag` | Sets a tag on a named country directly. |
| `/country manage sethome` | `terra.country.sethome` | Sets country home at the sender's current position. Sender must stand inside that country's territory. |
| `/country territory setregion <world> <region-id> <country name>` | `terra.country.territory` | Links a country to a WorldGuard region. |
| `/country territory clear <country name>` | `terra.country.territory` | Removes a country's linked region. |
| `/country territory sync <country name>` | `terra.country.territory` | Resyncs the linked territory using the territory service. |
| `/country territory info <country name>` | `terra.country.territory` | Shows the stored region/world binding. |
| `/country territory debug <country name>` | `terra.country.territory` | Dumps integration/debug state for WorldGuard and Dynmap support. |
| `/country admin <country name>` | `terra.country.admin` or op/admin bypass | Opens the admin country management GUI for the selected country. |
| `/country settraderreputation <country name> <value>` | `terra.country.admin` or op/admin bypass | Sets the total trader reputation value for a country. |

Running `/country` with no arguments opens a country dashboard GUI for players with country access.

### `/spawn`

Admin country-support warp root.

| Command | Permission | What it does |
| --- | --- | --- |
| `/spawn` | `terra.country.warpadmin` or op/admin bypass | Opens the admin warp GUI. |
| `/spawn menu` | `terra.country.warpadmin` or op/admin bypass | Opens the admin warp GUI explicitly. |
| `/spawn admin` | `terra.country.warpadmin` or op/admin bypass | Alias for the admin warp GUI. |
| `/spawn warp` | `terra.country.warpadmin` or op/admin bypass | Alias for the admin warp GUI. |
| `/spawn <country name>` | `terra.country.warpadmin` or op/admin bypass | Warps directly to a country's saved home if it exists. |

### `/staff`

| Command | Permission | What it does |
| --- | --- | --- |
| `/staff` | `terra.staff` | Opens the staff menu and toggles staff-mode access to moderation/admin tools. |
| `/staff mode` | `terra.staff` | Toggles staff mode directly. |
| `/staff vanish` | `terra.staff` | Toggles vanish directly. |
| `/staff freeze <player>` | `terra.staff` | Toggles movement freezing for a target player. |
| `/staff tp <player>` | `terra.staff` | Teleports to a target player. |
| `/staff bring <player>` | `terra.staff` | Teleports a target player to the sender. |

### `/flyspeed`

| Command | Permission | What it does |
| --- | --- | --- |
| `/flyspeed <0-10>` | `terra.admin` | Sets the sender's fly speed on a simple 0-10 scale. |

### `/vanish`

| Command | Permission | What it does |
| --- | --- | --- |
| `/vanish [on\|off\|toggle]` | `terra.admin` | Hides or reveals the sender to normal players. |

## Permissions

### Admin and utility permissions

- `terra.admin`
- `terra.plugins.view`
- `terra.staff`

### Country permissions

- `terra.country.admin`
- `terra.country.create`
- `terra.country.join`
- `terra.country.invite`
- `terra.country.acceptinvite`
- `terra.country.home`
- `terra.country.disband`
- `terra.country.joinstatus`
- `terra.country.leave`
- `terra.country.kick`
- `terra.country.info`
- `terra.country.farmland`
- `terra.country.list`
- `terra.country.rename`
- `terra.country.setowner`
- `terra.country.transfer`
- `terra.country.accepttransfer`
- `terra.country.sethome`
- `terra.country.warpadmin`
- `terra.country.territory`
- `terra.country.tag`

## Server Functions And Features

### 1. Profession system

The plugin defines these professions:

- Miner
- Lumberjack
- Farmer
- Builder
- Blacksmith

Core behavior:

- Players without a profession are forced into the profession selection GUI on join.
- Until they choose a profession, they cannot move freely, chat, take damage, or drop items.
- A player has a primary profession and can unlock a secondary profession later.
- Each profession can have a configurable player cap.
- During a playtest, players can immediately unlock a second job and use owned playtest jobs more freely.
- Admins can manage jobs through `/jobs admin <player>`.
- Profession progression, unlocks, GUI icons, XP curves, starter kits, and many rewards are driven by `jobs/config.yml` and the job resource files.

### 2. Profession action restrictions and profession perks

- Miner is required for profession-gated mining and gets mining cooldown reduction by level.
- Lumberjack handles wood-focused progression and now has a level-scaled chance to trigger instant tree growth when placing saplings or propagules.
- Farmer is required for most farming workflows and now gains:
  - tilling XP
  - planting XP
  - bonemeal XP
  - food crafting XP based on full crafted item count
  - a level-scaled chance to instantly grow crops
- Builder is required for placing configured builder blocks and gains XP from those placements.
- Blacksmith is required for crafting restricted gear and for smelting ore workflows.
- Starter-kit items are soulbound and cannot be dropped or moved into containers.

### 3. Climate, seasons, weather, and growth

- The climate model calculates a local temperature and growth state from:
  - latitude
  - humidity
  - continentality
  - current influence
  - altitude
  - season
  - day/night
  - rain
- local biome temperature and humidity
- Temperature can be displayed in Celsius or Fahrenheit.
- Rain lowers local temperature.
- Recent rain improves crop growth for a temporary period.
- Water can freeze in very cold climates when that option is enabled.
- Crops and saplings have optimal climate profiles with preferred seasons and temperature ranges.
- Crops and saplings grow faster in the right environment and slower in the wrong one.
- Crops in strong optimal conditions emit periodic happy particles.
- A bossbar and particle display tools exist for admin debugging.

### 4. Climate item detail and crop guide

- Crop, seed, sapling, and propagule items can carry climate lore describing their preferred environment.
- `/climate crops` opens a guide GUI containing those item variants.
- After `/terra reload`, unlored or unnamed descriptive items are refreshed in:
  - online player inventories
  - ender chests
  - loaded container inventories

### 5. Block delay and bypass

- When enabled, most block break and place actions apply a cooldown.
- Break and place cooldowns are tracked separately.
- Bypass players skip the cooldown entirely.
- Normal players see their active cooldowns above the hotbar.

### 6. Break rewards

- Breaking blocks can award profession XP and optionally money.
- Reward values are read from `economy/block-values.yml`.
- Miner, Farmer, and Lumberjack can have configurable double-drop chances.

### 7. Wilderness regeneration and build decay

- If a block is broken outside any country territory, the plugin can restore it after a configured delay.
- If a block is placed outside any country territory, the plugin can remove it after a configured delay.
- Placed block ownership is tracked to distinguish natural interactions from placed blocks.

### 8. Stability, support structures, and cave-ins

- The plugin adds a structural stability system intended to discourage unrealistic building and unsafe excavation.
- Material groups include:
  - loose
  - packed soil
  - soft rock
  - fragile roof material
  - hard rock
- Player-built structures are judged more strictly than natural terrain, so the system now focuses on walls, roofs, spans, columns, docks, towers, and similar builds rather than punishing ordinary mining as aggressively.
- Unsupported roofs, shaft walls, floating loose masses, thin masonry walls, and overloaded vertical stacks can collapse.
- Support blocks include logs, wood, planks, stairs, slabs, fences, fence gates, walls, iron bars, and chains.
- The system recognizes support radius, support frames, vertical posts, braces, and buttress-like support patterns.
- Collapse handling includes:
  - local scans
  - dust/instability feedback
  - sideways collapse motion
  - rubble leftovers
  - wetness/rain stress
  - chunk-safe rescan handling
  - a debug particle overlay through `/terra stability debug`
- The same stability profile now applies everywhere. Above-ground and underground checks are no longer split into separate rule sets.
- `/terra stability strictness <percent>` is the single global strictness slider for the whole system.
- Short grounded loose stacks can remain, but unsupported floating loose platforms should collapse.
- Qualifying non-crop, non-functional break and place actions build a hidden stability meter.
- Each full meter charge grants a short burst of Terra stability debug vision so players can inspect stress hotspots while building.
- `/terra stability meter` lets a player show or hide the meter's chat status display without disabling the underlying progression.

### 9. Merchant and country admin GUIs

- `/merchant manage` opens the merchant admin editor for:
  - spawn timing
  - active duration
  - rotation timing
  - trade cooldown
  - buy rotations
  - sell offers
- Merchant buy/sell entries can now be edited directly for traded material, amount, price, and stock values.
- `/country admin <country>` opens an admin-only country overview GUI.
- The country admin GUI can inspect members, join state, and trader reputation quickly.
- `/country settraderreputation` provides a direct command-based override for country-wide trader reputation totals.

### 10. Staff tools, vanish, and moderation utilities

- `/staff` opens a GUI-oriented staff menu for common server-management actions.
- Staff tools include staff mode, vanish, ore vision, cooldown debug, player teleport/bring, freeze, heal/feed, inventory clearing, gamemode changes, Terra player-data reset, world-spawn setup, and quick access to other admin utilities.
- `/flyspeed` and `/vanish` also exist as direct standalone commands.

### 11. Lag reduction, maintenance, and server utility systems

- The plugin can automatically clear dropped ground items on a repeating timer with warning messages before each clear.
- Dropped items can merge more aggressively to reduce entity count.
- Nearby mobs of the same valid type can be stacked together, with the stack size shown above the entity.
- Trader and merchant NPCs are excluded from the item-clear and mob-stacking cleanup rules.
- Maintenance mode changes the server-list MOTD, blocks normal joins, and supports an allowlist for approved staff/testers.
- Join and leave messages are configurable through the bundled messages/config files.

### 12. Country system

Countries store:

- name
- owner
- members
- open or closed join state
- invitations
- optional tag
- optional territory binding
- optional country home

Important behavior:

- owner changes, join status changes, renames, and membership changes trigger territory sync and tag refresh
- leaving as owner hands the country to another member if possible
- invites and transfers are stored as pending state until accepted or invalidated
- the country list is a GUI browser with filtering and sorting

### 13. Chat routing

- Normal chat is local by default and only reaches players within 50 blocks in the same world.
- Players can toggle country chat with `/cc` or `/country chat`.
- Players can toggle global chat access with `/gc`.
- Global chat uses `!message` and has a 5 minute cooldown per player.

### 14. Country territory integration

When enabled and available:

- a country can bind to a WorldGuard region
- the plugin can resolve what country a player is standing in
- territory membership can be synced through the territory service
- Dynmap markers can be updated from country territory data
- enter/leave chat messages and titles can be shown

#### Recommended WorldGuard setup flow

1. Define the region with WorldGuard.
   Example: `//wand`, `//sel poly`, click the outline, `//expand vert`, `/rg define mycountry_region`
2. Create the country.
   Example: `/country create My Country`
3. Link the region.
   Example: `/country territory setregion world mycountry_region My Country`
4. Sync the territory data.
   Example: `/country territory sync My Country`
5. While standing inside the linked region, set the country home.
   Example: `/country manage sethome`

### 15. Farmland limits

- Countries with linked territory can have farmland counted inside that region.
- The hard limit is `member count * 16`.
- Farmer players are prevented from tilling more farmland once the cap is reached.
- `/country farmland` exposes the current count and max.

### 16. Spawn handling

- The plugin can save a persistent global spawn with `/terra setworldspawn`.
- Plugin-driven teleports that return a player to spawn use that saved location first.
- If no custom spawn is saved, the plugin falls back to the normal default world spawn.

### 17. Furnace locking and profession collaboration

- The first valid player to insert items into a furnace, blast furnace, or smoker claims a temporary lock.
- Other players are blocked while the lock is active.
- Miner/Farmer source tagging allows collaboration rewards when Blacksmith or Farmer later processes the output.

### 18. PlaceholderAPI placeholders

If PlaceholderAPI is installed, the plugin registers `%terra_*%` placeholders including:

- `%terra_balance%`
- `%terra_xp%`
- `%terra_player_country%`
- `%terra_player_countrytag%`
- `%terra_current_country%`
- `%terra_profession%`
- `%terra_current_job%`
- `%terra_profession_display%`
- `%terra_current_job_display%`
- `%terra_has_profession%`
- `%terra_profession_locked%`
- `%terra_current_job_level%`
- `%terra_current_job_xp%`
- `%terra_current_job_xp_required%`
- `%terra_playtest_active%`
- `%terra_playtest_remaining%`
- `%terra_playtest_remaining_short%`

### 19. Real-time clock

- If enabled in `settings/core.yml`, the plugin can set world time to match a real timezone.
- The system is controlled live through `/terra realtimeclock`.

## Admin Workflows

### Common moderator tasks

1. Help a player in or near a country:
   Use `/spawn` or `/spawn <country name>`.
2. Reset a stuck or broken player:
   Use `/terra cleardata <player>`.
3. Test cooldown complaints:
   Use `/terra cooldowndebug`.
4. Start or extend a playtest:
   Use `/terra playtest start <time>` and `/terra playtest extend <time>`.
5. Inspect climate behavior:
   Use `/climate check`, `/climate display on`, or `/climate create fullworld`.
6. Inspect cave-in behavior:
   Use `/terra stability debug`, `/terra stability meter status`, and `/terra stability scan`.

### Recommended initial setup

1. Set the plugin's global spawn with `/terra setworldspawn` in a safe area.
2. Review the main settings files under `src/main/resources/settings/` for climate, real-time clock, lag reduction, maintenance, wilderness regeneration, stability, hostile mobs, phantoms, and territory integration.
3. Review each enabled profession file in `src/main/resources/jobs/`.
4. Configure country territories through WorldGuard before expecting farmland caps or territory notifications to work.
5. Verify Vault, PlaceholderAPI, LuckPerms, WorldEdit, WorldGuard, Dynmap, and CoreProtect integrations if your server expects those features.

## Important Config Files

- `src/main/resources/settings/core.yml`
- `src/main/resources/settings/climate.yml`
- `src/main/resources/settings/stability.yml`
- `src/main/resources/settings/merchant.yml`
- `src/main/resources/settings/territories.yml`
- `src/main/resources/economy/block-values.yml`
- `src/main/resources/jobs/config.yml`
- `src/main/resources/jobs/*.yml`
- `src/main/resources/messages/messages.yml`
- `src/main/resources/data.yml`
- `src/main/resources/countries/data.yml`

## Related Docs

- Player-facing starter guide: [BEGINNER_GUIDE.md](/C:/Users/noahu/customplugins/testproject/BEGINNER_GUIDE.md)
- Detailed patch history: [PATCH_NOTES.md](/C:/Users/noahu/customplugins/testproject/PATCH_NOTES.md)
- High-level timeline: [UPDATES.md](/C:/Users/noahu/customplugins/testproject/UPDATES.md)
