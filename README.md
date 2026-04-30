# Terra Nations Core

Terra Nations Core is a Paper 1.21 plugin that combines profession-based progression, a guild-first territorial economy, legacy countries, climate-aware farming, structural stability, route traders, merchant waves, staff tooling, and server utility systems in one codebase.

## What It Includes

- Seven core professions: Miner, Lumberjack, Farmer, Builder, Blacksmith, Trader, and Soldier
- Guild management with treasury, roles, permissions, invite flows, progression, stockpiles, and guild-backed country claims
- Country management with territory links, homes, treasury, resources, roles, upgrades, boosts, trader access, and legacy tutorial compatibility
- Climate, seasons, rainfall, altitude, and biome-aware crop or sapling growth
- Stability and support-structure rules for excavation and large builds
- A standalone Terra workbench playtest system with placeable specialist benches, placeholder custom ores, materials, armor, tools, and blocks
- Traveling trader and wandering merchant systems
- Local, country, and opt-in global chat routing
- Terra-owned player balances and country treasury support
- A locked hotbar guide item that opens player stats, personal skills, contracts, and hub menus
- Staff tools, vanish, fly speed, lag reduction, maintenance mode, and utility commands

## Platform

- Java 21
- Maven
- Paper `1.21.1-R0.1-SNAPSHOT`

Optional integrations used by specific systems:

- Vault
- PlaceholderAPI
- LuckPerms
- WorldEdit
- WorldGuard
- Dynmap
- CoreProtect

## Build

```bash
mvn clean package
```

The shaded jar is produced in `target/`.

## Main Command Roots

- `/terra`
- `/jobs`
- `/guild`
- `/country`
- `/climate`
- `/trader`
- `/merchant`
- `/staff`
- `/balance`
- `/spawn`
- `/flyspeed`
- `/vanish`
- `/countrychat` (`/cc`)
- `/globalchat` (`/gc`)
- `/rollbackarea`
- `/undoarea`

## Current Playtest Crafting Layer

- `/terra catalog` opens an admin GUI for spawning Terra workbenches, placeholder ores, materials, blocks, armor, and tools.
- Vanilla crafting tables are blocked on interaction so players are pushed toward Terra workbenches instead.
- Placed Terra workbenches show a floating title above the block and open a dedicated crafting GUI on right-click.
- Each workbench has a linked specialist profession that gets extra output on eligible recipes and one specialist-only pattern.
- The current content set is intentionally placeholder-driven so ItemsAdder models, blocks, armor, and textures can be swapped in later without rebuilding the logic.

## Documentation

- Admin and system reference: [PLUGIN_DOCUMENTATION.md](PLUGIN_DOCUMENTATION.md)
- Player-facing guide: [BEGINNER_GUIDE.md](BEGINNER_GUIDE.md)
- High-level timeline: [UPDATES.md](UPDATES.md)
- Detailed feature ledger: [PATCH_NOTES.md](PATCH_NOTES.md)
- Future backlog: [GAME_FEATURE_IDEAS.md](GAME_FEATURE_IDEAS.md)

## Important Resource Files

- `src/main/resources/plugin.yml`
- `src/main/resources/settings/core.yml`
- `src/main/resources/settings/climate.yml`
- `src/main/resources/settings/stability.yml`
- `src/main/resources/settings/merchant.yml`
- `src/main/resources/settings/territories.yml`
- `src/main/resources/jobs/config.yml`
- `src/main/resources/jobs/*.yml`
- `src/main/resources/messages/messages.yml`
- `src/main/resources/data.yml`
- `src/main/resources/guilds/data.yml`
- `src/main/resources/countries/data.yml`
