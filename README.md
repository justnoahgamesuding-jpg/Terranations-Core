# Terra Nations Core

Terra Nations Core is a Paper 1.21 plugin that combines profession-based progression, countries, climate-aware farming, structural stability, route traders, merchant waves, staff tooling, and server utility systems in one codebase.

## What It Includes

- Five core professions: Miner, Lumberjack, Farmer, Builder, and Blacksmith
- Country management with territory links, homes, treasury, resources, roles, upgrades, boosts, and trader access
- Climate, seasons, rainfall, altitude, and biome-aware crop or sapling growth
- Stability and support-structure rules for excavation and large builds
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
- `src/main/resources/countries/data.yml`
