# Terra Plugin Documentation

This file is the current admin and systems reference for the plugin snapshot in this repository.

## Overview

Terra combines these major systems:

- a first-hour onboarding and tutorial framework with delayed profession lock-in
- profession progression with enforced specialization
- guild management with treasury, roles, permissions, recruitment, progression, stockpiles, and guild-backed territorial ownership
- country management with roles, treasury, upgrades, boosts, territory, and hidden system-country support
- climate-aware farming and sapling growth
- structural stability and support logic
- a standalone Terra workbench crafting playtest layer with placeable specialist benches and placeholder custom content
- trader routes and merchant waves
- Terra-owned balance storage
- a locked player guide item in hotbar slot 9 for future gameplay navigation
- onboarding NPC integrations for ItemsAdder and FancyNpcs
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
- `catalog`
  Opens the Terra crafting catalog GUI for spawning workbenches and placeholder Terra content items.
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
  Manages fixed ore nodes and the fixed-ore tool.
- `tutorial`
  Manages onboarding quest markers, tutorial NPC bindings, and starter-flow setup helpers.
- `reload`
  Reloads Terra configs, cached state, integrations, and item metadata refresh passes.
- `hardrestart`
  Runs the plugin’s staged hard-restart workflow.

Notes:

- Break and place cooldowns are separate.
- `/terra reload` also refreshes climate-lore items and soulbound starter items in loaded inventories.
- The Terra Guide item is restored to slot 9 for online players and stays locked there.
- `/terra catalog` is the current admin entry point for the standalone Terra workbench playtest items.
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

This is still present because parts of the tutorial and some older admin flows still depend on it. The long-term player-facing ownership model is now guild-first.

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
- Hidden system countries are excluded from normal player-facing lookup, browser, HUD, and info flows.
- Guild ownership can sit on top of the country layer. A country can be legacy-managed, guild-claimed, or both depending on the admin flow used.

### `/guild`

Guild root for the newer treasury, recruitment, and country-claim flow.

Core membership and recruitment:

- `menu`
  Opens the guild dashboard if the player is in a guild, otherwise opens the guild browser.
- `create <name> <tag>`
  Creates a guild. Tags are limited to letters and filtered against blocked language.
- `info [guild]`
  Shows guild summary, progression, treasury, recruiting state, and claimed countries.
- `invite <player>`
  Sends a timed guild invite.
- `resendinvite <player>` or `resend <player>`
  Refreshes an existing invite and resends the clickable chat prompt.
- `accept <guild>` or `join <guild>`
  Accepts an invite or joins an open-recruiting guild.
- `deny <guild>`
  Denies a pending guild invite.
- `leave`
  Leaves the current guild if the player is not the leader.
- `kick <player>`
  Removes a guild member if the actor has permission.

Treasury, stockpile, and claiming:

- `deposit <amount>`
  Deposits Terra balance into the guild treasury.
- `withdraw <amount>`
  Withdraws from the guild treasury if the actor has permission and is inside their role-based limit.
- `stockpile`
  Shows a stockpile summary.
- `stockpile deposit <amount>`
  Deposits held materials into the guild stockpile.
- `claim <country>`
  Claims a country for the guild if treasury, cooldown, member-count, and guild progression requirements are met.

Roles, permissions, and leadership:

- `role <player> <member|admiral|officer>`
  Sets the member role.
- `permissions <role|player> <target> <permission> <allow|deny|default>`
  Adjusts permission overrides per role or player.
- `transferleader <player>`
  Transfers guild leadership.
- `disband`
  Disbands the guild and releases any guild-owned countries.

Profile and visibility:

- `description <text>`
  Sets guild description.
- `motd <text>`
  Sets guild MOTD shown on join.
- `recruiting <open|closed>`
  Toggles open recruitment.
- `logs [page]`
  Shows recent guild audit entries.
- `list`
  Lists visible guilds.

Guild notes:

- Guild invites are timed and can be accepted or denied by clicking directly in chat.
- Officers and other members with `INVITE_PLAYERS` permission are notified when invites are accepted, denied, cancelled, or expired.
- The guild GUI exposes pending invites with resend and cancel actions.
- Guild claims use the existing country map and territory layer rather than replacing it.
- Weekly upkeep is based on country size. One unpaid week can push treasury negative; a second failed week releases the country.
- Guilds gain level from guild XP and score factors such as members, job levels, treasury strength, stockpile contribution, and controlled countries.

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
- Trader
- Soldier

Key behavior:

- New players are forced through profession selection.
- Primary and secondary professions are tracked separately.
- Job caps are configurable.
- Starter kits and many progression rules come from `src/main/resources/jobs/`.
- Farmer and Lumberjack have instant-growth proc systems.
- Blacksmith and Farmer use furnace collaboration and output tagging.

### Terra Workbench Crafting

- Vanilla crafting tables are blocked on interaction for the current playtest flow.
- Terra workbenches are placeable managed blocks that keep their Terra identity when broken and replaced.
- Each placed workbench spawns a floating title label above the block so the bench type is obvious in-world.
- The current playtest set includes seven benches:
  `Prospector Bench`, `Timber Bench`, `Field Kitchen`, `Mason Bench`, `Forge Bench`, `Trade Desk`, and `War Table`.
- Each workbench has:
  general recipes any player can use,
  a linked specialist profession that gets extra output on eligible recipes,
  and one specialist-only placeholder recipe.
- The current outputs are placeholder Terra ores, materials, blocks, tools, and armor meant to be swapped to ItemsAdder assets later.
- `/terra catalog` is the admin spawn GUI for the current Terra crafting categories.

### Terra Guide

- Every player receives a `Terra Guide` nether star in hotbar slot 9.
- The item is soulbound and additionally locked to that slot.
- It cannot be moved, dropped, hotbar-swapped, offhand-swapped, or placed into containers.
- Right-clicking it opens the main player hub.
- The current guide includes player stats, jobs access, personal skills, contracts, guild access, and ore-sense toggles.

### Tutorial And Onboarding

The first-hour onboarding flow is now a separate system from profession ownership.

Key behavior:

- New players can remain profession-unlocked while onboarding is active.
- The tutorial can require trial completion and playtime before the first real job choice unlocks.
- Tutorial progress is quest-driven and supports guide opens, location visits, block actions, NPC interactions, profession trials, country steps, and delayed profession lock-in.
- Tutorial visit-location markers can be saved from a WorldEdit cuboid selection or a direct point marker.
- New players can be routed to a starter hub spawn before they choose a primary profession.
- The starter hub can be bound to a real country entry while still being treated as a hidden admin/system country.
- Tutorial NPCs can be generic marked entities or persistent custom NPCs spawned through ItemsAdder.
- Tutorial NPCs can also be existing FancyNpcs NPCs, with Terra only binding tutorial logic to them.
- When an onboarding player interacts with a tutorial NPC, Terra can temporarily clear chat, lock movement and hotbar actions, suppress inventory and chat use, and keep the player camera focused on that NPC while dialogue lines display.
- Fancy NPC repeat dialogue can use a random post-introduction line pool instead of a single fixed repeat line.
- Delivery quests can now require handing a defined item amount to a target NPC.

Important limitation:

- The NPC focus system uses real camera-facing control, but Paper does not provide a general server-side FOV zoom for normal players. The effect is a strong cinematic focus lock, not a true optical zoom.

### Personal Skills And Work Orders

- Players earn skill points from profession level-ups and from playtime.
- Skill points can be spent on health, XP gain, cooldown reduction, trader bonuses, merchant cooldown reduction, double-drop bonuses, growth proc bonuses, and ore-sense unlocks.
- The guide also exposes a personal work-order system that rewards money and skill points for profession XP progress.

### Guilds

Guilds are now the main player-facing ownership and cooperation layer.

They include:

- leader, officer, admiral, and member roles
- role-based permission defaults plus per-player permission overrides
- treasury deposits, withdrawals, role-based withdraw limits, and audit logs
- timed invites, open recruitment, MOTD, description, and a guild browser
- stockpile storage for contributed materials
- guild XP, level, claim cap, member cap, and derived progression score
- capital-country tracking and guild-backed country claims

Important effects:

- Guilds claim the existing country objects rather than replacing the territory layer.
- Claiming can require treasury funds, member count, total job strength, and available claim slots.
- Weekly upkeep scales with country size and can place a guild treasury into temporary debt before control is lost.
- The guild GUI is now the main player-facing management surface for recruitment, treasury overview, countries, and pending invites.

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
- Countries still exist as the territory and progression object even when a guild owns them.
- Some older flows still use direct country membership because the tutorial and admin tooling were built around it first.

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

The plugin owns its own money storage, guild treasury logic, and country treasury logic.

- Player balances live in `data.yml`.
- Guild treasury, stockpile, invite, and progression state live in `guilds/data.yml`.
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

Economy and server:

- `%terra_balance%`
- `%terra_xp%`
- `%terra_server_time%`
- `%terra_local_time%`
- `%terra_server_date%`
- `%terra_local_date%`
- `%terra_server_datetime%`
- `%terra_local_datetime%`

Climate:

- `%terra_climate_name%`
- `%terra_climate_type%`
- `%terra_climate_temp%`
- `%terra_climate_temperature%`
- `%terra_climate_season%`
- `%terra_climate_raining%`
- `%terra_climate_freezing%`

Country:

- `%terra_player_country%`
- `%terra_player_country_level%`
- `%terra_player_countrytag%`
- `%terra_player_country_tag%`
- `%terra_current_country%`
- `%terra_current_country_tag%`
- `%terra_current_country_level%`
- `%terra_current_country_owner%`
- `%terra_current_country_owner_tag%`

Guild:

- `%terra_player_guild%`
- `%terra_player_guild_tag%`
- `%terra_player_guild_level%`
- `%terra_player_guild_balance%`
- `%terra_player_guild_role%`
- `%terra_player_guild_members%`
- `%terra_player_guild_member_count%`
- `%terra_player_guild_claims%`
- `%terra_player_guild_country_count%`
- `%terra_player_guild_capital%`
- `%terra_player_guild_capital_country%`
- `%terra_player_guild_recruiting%`
- `%terra_player_guild_recruitment%`
- `%terra_player_guild_score%`
- `%terra_player_guild_xp%`

Profession:

- `%terra_profession%`
- `%terra_current_job%`
- `%terra_profession_display%`
- `%terra_current_job_display%`
- `%terra_has_profession%`
- `%terra_profession_locked%`
- `%terra_current_job_level%`
- `%terra_current_job_xp%`
- `%terra_current_job_xp_required%`

Tutorial and onboarding:

- `%terra_quest_active%`
- `%terra_quest_has_active%`
- `%terra_quest_id%`
- `%terra_quest_active_id%`
- `%terra_quest_title%`
- `%terra_quest_active_title%`
- `%terra_quest_title_plain%`
- `%terra_quest_active_title_plain%`
- `%terra_quest_objective%`
- `%terra_quest_active_objective%`
- `%terra_quest_objective_plain%`
- `%terra_quest_active_objective_plain%`
- `%terra_quest_hint%`
- `%terra_quest_active_hint%`
- `%terra_quest_hint_plain%`
- `%terra_quest_active_hint_plain%`
- `%terra_quest_progress%`
- `%terra_quest_active_progress%`
- `%terra_quest_status%`
- `%terra_quest_active_status%`
- `%terra_quest_progress_bar%`
- `%terra_quest_active_progress_bar%`
- `%terra_quest_accent%`
- `%terra_quest_active_accent%`
- `%terra_quest_current%`
- `%terra_quest_active_current%`
- `%terra_quest_target%`
- `%terra_quest_active_target%`
- `%terra_quest_percent%`
- `%terra_quest_active_percent%`
- `%terra_quest_steps%`
- `%terra_quest_active_steps%`
- `%terra_quest_max_steps%`
- `%terra_quest_profession%`
- `%terra_quest_active_profession%`

Playtest:

- `%terra_playtest_active%`
- `%terra_playtest_remaining%`
- `%terra_playtest_remaining_short%`

Notes:

- `%terra_current_job_xp%` is remaining XP to the next level, not current earned XP for the level.
- `%terra_server_time%`, `%terra_server_date%`, and `%terra_server_datetime%` follow the same timezone as the real-time world clock configured in `realtime-clock.timezone`.
- `%terra_player_countrytag%` and `%terra_player_country_tag%` are aliases.
- `%terra_player_guild_members%` and `%terra_player_guild_member_count%` are aliases.
- `%terra_player_guild_claims%` and `%terra_player_guild_country_count%` are aliases.
- `%terra_player_guild_capital%` and `%terra_player_guild_capital_country%` are aliases.
- `%terra_player_guild_recruiting%` and `%terra_player_guild_recruitment%` are aliases.

## Integrations

Optional integrations used by Terra:

- Vault
- PlaceholderAPI
- LuckPerms
- WorldEdit
- WorldGuard
- Dynmap
- CoreProtect
- ItemsAdder
- FancyNpcs

## Recommended Setup Flow

1. Build the plugin and place it on a Paper 1.21 server with Java 21.
2. Review `src/main/resources/settings/core.yml`, `climate.yml`, `stability.yml`, `merchant.yml`, and `territories.yml`.
3. Review the job configs under `src/main/resources/jobs/`.
4. Set a safe global spawn with `/terra setworldspawn`.
5. If using countries with territory, create WorldGuard regions with `//sel poly` and `//expand vert`.
6. Link each region with `/country territory setregion ...` and run `/country territory sync ...`.
7. Set country homes and trader spawns from inside the linked territory.
8. Verify optional integrations you expect to use.

## Tutorial Setup

This is the recommended setup order if you want a clean first-join experience.

The intended architecture is:

- onboarding state is separate from profession ownership
- players can walk, explore, and try systems before choosing a locked profession
- the starter hub can be either a standalone spawn or a hidden system-country entry
- tutorial quests drive progression
- NPCs are presentation surfaces, while Terra owns the actual tutorial progress logic

If you place quests or NPC bindings before the physical onboarding space is stable, you will usually end up with mismatched keys, bad routing, and a confusing first-hour flow.

### 1. Enable and tune onboarding

Review `settings/core.yml`:

- `onboarding.enabled`
- `onboarding.required-trials`
- `onboarding.required-playtime-minutes`
- `onboarding.starter-hub.*`
- `onboarding.trial-thresholds.*`
- `onboarding.npc-focus.*`
- `onboarding.npc-dialogue.*`

Recommended starting values for a first pass:

- `required-trials: 3`
- `required-playtime-minutes: 20`
- keep `starter-hub.enabled: true`
- set `starter-hub.country-key` if the hub should use a hidden admin country home
- use `starter-hub.use-global-spawn: true` only when you are not binding the hub to a country

If you want a separate onboarding-only spawn without using a country, disable `use-global-spawn` and set:

- `starter-hub.world`
- `starter-hub.x`
- `starter-hub.y`
- `starter-hub.z`
- `starter-hub.yaw`
- `starter-hub.pitch`

#### Starter hub as a hidden admin country

If you want the starter island to exist inside the country/territory system:

1. Create the country normally as an admin.
   Recommended: create it without assigning a normal player owner.
2. Set its territory and its home location.
3. Set `onboarding.starter-hub.country-key` in `settings/core.yml` to that country name.

When that key is set, Terra treats that country as:

- the onboarding spawn source, using the country home first and trader spawn as fallback
- a hidden country for normal player-facing lookup, browser, HUD, and location display
- a system/admin country that is excluded from random trader and merchant hosting

Players cannot join it through normal country commands, cannot browse it in the country list, and cannot inspect it with normal `/country info` lookups. Admin tooling still resolves it directly.

Recommended use:

- use the country system for territory, home, trader spawn, and region ownership
- do not treat the hub like a player-managed nation
- use it as a neutral recruitment and onboarding space
- keep normal leadership and long-term progression in real player countries

### 2. Build the starter area first

Before wiring quests, build the actual spaces the tutorial refers to:

- arrival point
- central guide plaza
- profession trial spaces
- trader stop
- merchant stop
- embassy or country recruitment area
- any guided route between those spaces

Do not configure quest locations until the physical layout is stable.

### 3. Configure the starter quest flow

Review `settings/quests.yml` under `quests.starter.list`.

Each tutorial quest supports:

- `type`
- `key`
- `target`
- `title`
- `objective`
- `hint`
- `requires-completed`

Current onboarding-specific quest types:

- `open_guide`
- `visit_location`
- `break_block`
- `place_block`
- `interact_block`
- `interact_npc`
- `complete_trial`
- `playtime`
- `select_profession`
- `join_country`
- `contribute_country`

Examples:

- `visit_location` uses a marker key such as `starter_hub`
- `interact_npc` uses a quest key such as `trader_npc`
- `complete_trial` can use `key: any` to count any completed profession trial
- `playtime` uses `target` in minutes

Recommended first-hour sequence:

1. open the guide
2. visit the starter hub landmark
3. talk to a guide or trader NPC
4. complete at least three profession trials
5. spend the required playtime in-world
6. complete any country-facing onboarding step you want to teach
7. unlock and select the first real profession

### 4. Save named tutorial locations in-world

Use one of these flows depending on whether you want a point marker or a region marker:

- `/terra tutorial setlocation <key> [radius] [display name]`
- `/terra tutorial setlocationselection <key> [display name]`
- `/terra tutorial clearlocation <key>`
- `/terra tutorial locations`

Examples:

- `/terra tutorial setlocation starter_hub 12 Starter Hub`
- `/terra tutorial setlocationselection starter_hub Starter Hub`
- `/terra tutorial setlocation embassy_board 8 Embassy Board`
- `/terra tutorial setlocation forge_yard 10 Forge Yard`

`setlocationselection` uses the current WorldEdit selection. Location keys should match the `key` values used by any `visit_location` tutorial quest.

### 5. Add tutorial NPCs

There are three supported setup paths. Pick the one that matches who should own the NPC.

Use this rule:

- use a marked entity for quick tests or lightweight integrations
- use ItemsAdder when Terra should spawn, save, and respawn the NPC itself
- use FancyNpcs when you want better visual customization and you already manage the NPC through FancyNpcs

#### Option A: mark an existing entity

Use this if you already placed an NPC through another plugin or vanilla means.

- look at the entity
- run `/terra tutorial marknpc <quest-key>`

Example:

- `/terra tutorial marknpc trader_npc`

This is enough for simple onboarding interaction tracking.

#### Option B: create a persistent ItemsAdder tutorial NPC

Use this if you want Terra to own and respawn the onboarding NPC.

Command:

- `/terra tutorial spawnnpc <npc-id> <quest-key> <itemsadder-entity> <dialogue-key|-> [display name]`

Example:

- `/terra tutorial spawnnpc embassy_guide embassy_guide terra:embassy_guide embassy_guide Embassy Guide`

This stores:

- a stable Terra NPC id
- the quest key used by tutorial quests
- the ItemsAdder custom entity id
- an optional dialogue key
- the spawn location and facing

If the entity already exists and you want Terra to register it instead of spawning a new one:

- look at the entity
- run `/terra tutorial registernpc <npc-id> <quest-key> <itemsadder-entity> <dialogue-key|-> [display name]`

Management commands:

- `/terra tutorial npcs`
- `/terra tutorial removenpc <npc-id>`
- `/terra tutorial clearnpc`

Notes:

- Terra uses ItemsAdder as a soft dependency for this system.
- The custom NPC entity id must match the ItemsAdder entity you created in your content pack.
- Terra will try to respawn saved onboarding NPCs on reload and after ItemsAdder finishes loading its data.

#### Option C: bind an existing FancyNpcs NPC

Use this if you want FancyNpcs to handle the NPC visuals, skins, equipment, pose, visibility, and other presentation choices while Terra handles tutorial logic.

Commands:

- `/terra tutorial bindfancynpc <fancynpc-id> <quest-key> <dialogue-key|-> [display name]`
- `/terra tutorial unbindfancynpc <fancynpc-id>`
- `/terra tutorial fancynpcs`

Example:

- `/terra tutorial bindfancynpc embassy_guide embassy_guide embassy_guide Embassy Guide`

Behavior:

- Terra listens for FancyNpcs interaction events on bound NPC ids.
- The onboarding focus/camera-lock sequence still runs through Terra.
- Terra preserves the tutorial flow even when the NPC is only a packet-based FancyNpcs NPC.
- FancyNpcs keeps ownership of the NPC itself, so Terra does not respawn or delete it.

Recommended FancyNpcs workflow:

1. create and visually configure the NPC in FancyNpcs first
2. decide the Terra tutorial `quest-key` that should be completed by talking to it
3. decide whether it needs a dedicated `dialogue-key` in `core.yml`
4. bind the FancyNpcs id with `/terra tutorial bindfancynpc ...`
5. test the full interaction with a fresh onboarding player

Use FancyNpcs for:

- embassy recruiters
- arrival greeters
- profession trainers
- lore-heavy dialogue NPCs
- country representatives that need better visual identity

Use ItemsAdder instead when:

- Terra should recreate the NPC automatically
- you want the NPC location and facing fully controlled by Terra data
- you are already using an ItemsAdder custom entity as the tutorial asset

### 6. Match NPC keys to quest keys cleanly

This is where most setup mistakes happen.

Keep these identifiers straight:

- `npc-id`
  Terra-owned identifier for an ItemsAdder-spawned onboarding NPC
- `fancynpc-id`
  FancyNpcs-owned identifier for an existing FancyNpcs NPC
- `quest-key`
  The key used by tutorial quests such as `interact_npc`
- `dialogue-key`
  The key used to load dialogue lines from `core.yml`

Practical example:

- `quest-key: embassy_guide`
- `dialogue-key: embassy_guide`
- FancyNpcs id: `starter_embassy_guide`

That means:

- the quest listens for `embassy_guide`
- dialogue loads from `onboarding.npc-dialogue.embassy_guide`
- Terra binds the FancyNpcs NPC named `starter_embassy_guide` to that quest key

### 7. Add NPC dialogue

Dialogue lives in `settings/core.yml` under `onboarding.npc-dialogue`.

Each entry is a list of lines. The lookup order is:

1. custom NPC `dialogue-key`
2. custom NPC id
3. quest key
4. `default`

Example:

```yml
onboarding:
  npc-dialogue:
    embassy_guide:
      - "&fCountries are the long-term social loop."
      - "&fMeet people here before you commit to a path."
```

If onboarding focus is enabled, these lines drive the temporary cinematic interaction.

### 8. Understand the focus interaction mode

When an onboarding player interacts with a tutorial NPC and dialogue exists:

- the player inventory closes
- chat is visually pushed away by clearing lines
- chat messages and commands are blocked
- hotbar scrolling and swaps are blocked
- item drop and most interaction input is blocked
- movement is locked in place
- the camera is repeatedly aimed at the NPC
- dialogue lines display in sequence

This only applies during the onboarding focus session and is intended for new-player tutorial moments, not general late-game NPC usage.

FancyNpcs note:

- Terra can still focus the player camera on a FancyNpcs NPC even though the NPC is packet-based, because the focus session can lock onto a saved location when a live Bukkit entity is not available.

### 9. Test the full first-join flow

Use a fresh test account or clear a player’s Terra data before testing.

Verify:

- the player spawns at the intended onboarding location
- if `starter-hub.country-key` is set, the player spawns at that country home
- the intro finishes correctly
- the guide opens and the first quest advances
- named location markers complete
- tutorial NPC interactions complete
- FancyNpcs-bound NPCs complete the same way as Terra-owned NPCs
- profession trials count correctly
- the profession choice stays locked until trial and playtime requirements are met
- `/jobs` becomes available only when onboarding is ready

### 10. Use tab completion

The `/terra tutorial` command now suggests:

- subcommands
- saved location marker keys
- saved custom NPC ids
- available FancyNpcs ids
- configured tutorial quest keys
- configured onboarding dialogue keys
- common radius values
- ItemsAdder-style NPC id examples

That is intended to reduce setup mistakes when wiring the tutorial in-world.

## FancyNpcs Quick Setup

If you are using FancyNpcs for tutorial or onboarding NPCs, this is the shortest reliable setup:

1. Install and start FancyNpcs normally.
2. Create the NPC in FancyNpcs and place it where the onboarding route expects it.
3. Decide the Terra `quest-key` and optional `dialogue-key`.
4. Bind it with `/terra tutorial bindfancynpc <fancynpc-id> <quest-key> <dialogue-key|-> [display name]`.
5. Confirm it appears in `/terra tutorial fancynpcs`.
6. Test the click flow with a fresh player.

If the interaction should advance a tutorial step, the `quest-key` must match the `key` used by the `interact_npc` quest entry in `settings/quests.yml`.

## Fixed Ores

Fixed ores now persist more than just the block material.

Saved fixed-ore data includes:

- world and coordinates
- ore material
- full block-data string when available

That means directional or stateful valid fixed-ore blocks such as logs keep their block state more reliably across:

- creation
- placeholder swaps after breaking
- server reloads

Admin usage:

- `/terra fixedore create <material>`
- `/terra fixedore fill <material>`
- `/terra fixedore delete`
- `/terra fixedoretool`

The fixed-ore tool behavior is:

- left-click a valid block to mark it as a fixed ore
- right-click a fixed ore to remove it

## Related Docs

- Repository overview: [README.md](README.md)
- Player-facing guide: [BEGINNER_GUIDE.md](BEGINNER_GUIDE.md)
- High-level timeline: [UPDATES.md](UPDATES.md)
- Detailed ledger: [PATCH_NOTES.md](PATCH_NOTES.md)
