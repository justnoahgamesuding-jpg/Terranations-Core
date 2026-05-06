# Terra Operations And Development Reference

This document is the working reference for server admins and plugin developers. It is focused on installation, configuration, runtime behavior, and the command surface that actually matters in production.

## 1. System summary

Terra currently ships these main systems:

- profession progression with specialization pressure
- guilds as the main ownership, treasury, stockpile, and progression layer
- countries as the territory and settlement layer underneath guild claims
- climate-aware crops and saplings
- structural stability and excavation support rules
- Terra workbench and specialist bench crafting
- route traders, merchant shops, and fixed Freeport starter merchants
- onboarding, quest assignment, and NPC-driven setup flows
- local, country, and global chat routing
- Terra-owned player balances
- PlaceholderAPI, ItemsAdder, FancyNpcs, WorldGuard, Dynmap, and staff utility integrations

## 2. Build and runtime requirements

- Java `21`
- Maven
- Paper `1.21.1-R0.1-SNAPSHOT`

Optional but important integrations:

- `Vault`: economy bridge support where applicable
- `PlaceholderAPI`: Terra placeholders
- `ItemsAdder`: HUD pack and custom item presentation
- `FancyNpcs`: onboarding and dialogue integrations
- `LuckPerms`: permission-aware behavior
- `WorldEdit`: admin setup flows that use selections
- `WorldGuard`: country territory linking
- `Dynmap`: optional territory map markers
- `CoreProtect`: `/rollbackarea` and `/undoarea`
- `SimpleScore`: optional scoreboard/dialog support

## 3. Startup checklist

1. Build with `mvn clean package`.
2. Place the jar in the server `plugins/` directory.
3. Start the server once to generate plugin data.
4. Review all settings under `plugins/testproject/`.
5. Review message files before going live.
6. Install ItemsAdder content if you use the HUD.
7. Install WorldGuard before enabling territory-linked country workflows.
8. Back up `data.yml`, `countries/data.yml`, and `guilds/data.yml` before large admin edits or testing resets.

## 4. Runtime file map

### Core configuration

- `settings/core.yml`
  Global gameplay toggles, economy scale, hunger, world systems, MOTD, HUD, join/leave presentation, lag reduction, and maintenance behavior.
- `settings/guilds.yml`
  Guild create cost, invite timing, withdrawal limits, claim rules, upkeep, XP, score, and level thresholds.
- `settings/climate.yml`
  Climate model tuning, seasons, rainfall, crop behavior, and display settings.
- `settings/stability.yml`
  Stability strictness, material classes, support logic, and collapse behavior.
- `settings/territories.yml`
  Territory sync, entry notifications, Dynmap integration, and border particle settings.
- `settings/merchant.yml`
  Wandering merchant and route merchant runtime settings.
- `settings/freeport-merchants.yml`
  Fixed starter merchant inventories and cooldowns.
- `settings/onboarding.yml`
  Onboarding, tutorial, marker, and related setup values.
- `settings/quests.yml`
  General and tutorial quest definitions.

### Supporting configuration

- `jobs/config.yml`
  Shared profession settings.
- `jobs/*.yml`
  Profession-specific progression and tuning.
- `messages/messages.yml`
  Main chat, status, admin, and gameplay messages.
- `messages/territories.yml`
  Territory protection and territory-specific messaging.
- `chat/config.yml`
  Chat presentation settings.
- `scoreboard/config.yml`
  Scoreboard and HUD support config.

### Runtime storage

- `data.yml`
  Main plugin data store.
- `countries/data.yml`
  Country state.
- `guilds/data.yml`
  Guild state.

## 5. Ownership model

The current production model is:

- players mainly interact through `guild`
- guilds claim and sustain `countries`
- countries still hold territory, homes, boosts, and related settlement state

That means:

- country tools still matter for setup, legacy flows, and some admin actions
- guild rules are the main live progression and treasury rules
- documentation and staff training should be guild-first

## 6. Command map

This is the practical command surface. It is not a full syntax dump; it is the set of roots admins and maintainers need to know.

### `/terra`

Primary admin root. Most subcommands require `terra.admin`.

Main groups:

- server control: `reload`, `hardrestart`, `maintenance`, `lag`, `setworldspawn`, `realtimeclock`
- world rules: `blockdelay`, `wildernessregen`, `hungerspeed`, `hostilemobs`, `phantoms`, `bats`, `items`, `rewards`
- profession/admin tuning: `blockvalue`, `jobcap`, `jobeditor`, `setxpboost`, `cleardata`
- climate and stability support: `cooldowndebug`, `orevision`, `stability`
- mining and resource tools: `fixedore`, `fixedoretool`, `bypass`, `bypasslist`
- onboarding and quests: `tutorial`, `quests`, `questdebug`
- crafting playtest/admin content: `catalog`, `guieditor`

Operational notes:

- `/terra reload` reloads configs, cached state, and item metadata refresh passes.
- `/terra hardrestart` is a staged plugin-owned restart workflow, not a generic server restart wrapper.
- `/terra catalog` is the current admin entry point for Terra crafting content and specialist benches.
- `/terra tutorial` and `/terra quests` are part of the onboarding/admin setup surface.

### `/jobs`

Profession root.

Key usage:

- `/jobs`
- `/jobs open`
- `/jobs info`
- `/jobs switch <job>`
- `/jobs admin ...`

Use this when validating profession state, switching active owned jobs, or fixing a player profile.

### `/guild`

Primary player-facing social and ownership root.

Main groups:

- membership: `create`, `invite`, `resendinvite`, `accept`, `deny`, `leave`, `kick`
- treasury and stockpile: `deposit`, `withdraw`, `stockpile`
- ownership: `claim`
- governance: `role`, `permissions`, `transferleader`, `disband`
- visibility and administration: `info`, `description`, `motd`, `recruiting`, `logs`, `list`

Operational notes:

- guild invites are timed and can be accepted from chat
- claims sit on top of the existing country layer
- upkeep failure can release countries after extended nonpayment

### `/country`

Legacy and administrative country root. Still required for territory, settlement, and some compatibility flows.

Main groups:

- membership and state: `create`, `join`, `invite`, `acceptinvite`, `leave`, `kick`, `disband`, `joinstatus`
- presentation and movement: `info`, `list`, `home`, `sethome`, `borders`, `chat`
- economy and progression: `balance`, `deposit`, `contribute`, `upgrade`, `boost`, `role`
- ownership/admin: `setowner`, `transfercountry`, `accepttransfer`, `rename`, `addtag`, `addtagtocountry`
- territory and trader admin: `territory`, `manage`, `trade`, `admin`, `settraderreputation`

Operational notes:

- country homes and trader spawns are territory-aware
- `/country` with no arguments opens the country GUI
- hidden system countries are intentionally excluded from normal player browsing

### `/climate`

Admin root for climate inspection and tuning support.

Key usage includes:

- checking local climate data
- enabling or freezing climate simulation
- season and display controls
- crop debug and preview flows

Use this together with `settings/climate.yml` when tuning farming behavior.

### `/trader` and `/merchant`

Admin roots for trader route and merchant runtime systems.

Use them for:

- status checks
- spawn timing
- manual spawn or removal
- merchant management GUI access

### `/staff`, `/flyspeed`, `/vanish`

Staff and moderation tools.

Use these for:

- staff mode
- player utility actions
- direct vanish control
- direct fly speed control

### `/balance`

Terra-owned player balance command. Use this when validating or correcting plugin-owned economy state.

### `/spawn`

Admin country warp and related UI surface.

### `/rollbackarea` and `/undoarea`

CoreProtect-backed rollback helpers. These should be considered destructive admin tooling and used with normal rollback discipline.

## 7. Permissions that matter

Declared root permissions in `plugin.yml`:

- `terra.admin`
- `terra.plugins.view`
- `terra.country.use`
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
- `terra.guild.use`
- `terra.country.territory`
- `terra.country.tag`
- `terra.staff`

Guild internal permissions are separate and configured through guild roles and `/guild permissions ...`.

## 8. Recommended admin workflows

### First deployment

1. Configure `settings/core.yml`.
2. Configure guild claim and upkeep values in `settings/guilds.yml`.
3. Configure climate and stability before opening the server.
4. If using territories, verify WorldGuard region names and Dynmap behavior.
5. If using ItemsAdder HUD assets, install the example content and run `/iazip`.
6. Start a controlled test world and validate onboarding, guild creation, claims, and merchant behavior.

### Territory setup

1. Create or verify the WorldGuard region.
2. Use the `/country territory ...` admin flow to link the region.
3. Confirm entry messaging and border particles.
4. If Dynmap is enabled, confirm markers render correctly.

### Onboarding and NPC setup

1. Configure `settings/onboarding.yml` and `settings/quests.yml`.
2. Use `/terra tutorial ...` to set markers and onboarding helpers.
3. Validate FancyNpcs and ItemsAdder dependent flows on a live test account.

### Guild economy tuning

1. Set guild create cost, claim cost, and upkeep first.
2. Tune withdrawal limits by role.
3. Validate progression thresholds against your intended server pace.
4. Test a claim, an upkeep cycle, and an unpaid upkeep path before launch.

## 9. ItemsAdder and pack notes

Terra no longer uses a standalone plugin-managed resource pack URL.

Use ItemsAdder content from:

- `examples/itemsadder/contents/terra_quest_hud`

Reference:

- [examples/itemsadder/contents/terra_quest_hud/README.md](examples/itemsadder/contents/terra_quest_hud/README.md)

The standalone pack example remains here for manual pack work:

- `examples/resourcepacks/terranations_hud_pack`

Reference:

- [examples/resourcepacks/terranations_hud_pack/README.md](examples/resourcepacks/terranations_hud_pack/README.md)

## 10. Developer notes

- `Testproject.java` is the central runtime owner and currently contains most state management.
- `TerraCommand.java`, `GuildCommand.java`, and `CountryCommand.java` are the main command entry points.
- Most config-backed systems are wired directly from `src/main/resources/settings/`.
- This is a stateful plugin with multiple runtime maps and generated YAML data stores, so documentation and code changes should be validated together.

## 11. Change discipline

When updating this plugin:

- verify command docs against `plugin.yml` and the command handler classes
- verify config docs against the actual resource file names
- treat guild and country docs as a coupled system
- document operator-facing changes before adding more backlog or design notes
