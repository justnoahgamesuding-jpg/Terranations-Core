# Terra Nations Core

Terra Nations Core is a Paper `1.21.1` plugin that combines professions, guild-backed country ownership, climate, stability, custom crafting, traders, onboarding, and staff utilities in one codebase.

This repository is documented for two audiences:

- server admins who need to install, configure, and operate it
- server developers who need to build it, inspect its layout, and extend it safely

## What matters

- Main admin reference: [PLUGIN_DOCUMENTATION.md](PLUGIN_DOCUMENTATION.md)
- ItemsAdder HUD content: [examples/itemsadder/contents/terra_quest_hud/README.md](examples/itemsadder/contents/terra_quest_hud/README.md)
- Standalone vanilla resource pack example: [examples/resourcepacks/terranations_hud_pack/README.md](examples/resourcepacks/terranations_hud_pack/README.md)

## Platform

- Java `21`
- Maven
- Paper `1.21.1-R0.1-SNAPSHOT`

Optional runtime integrations used by specific systems:

- Vault
- PlaceholderAPI
- ItemsAdder
- FancyNpcs
- LuckPerms
- WorldEdit
- WorldGuard
- Dynmap
- CoreProtect
- SimpleScore

## Build

```bash
mvn clean package
```

The plugin jar is produced in `target/`.

## Install

1. Build the jar with Maven.
2. Place the jar in `plugins/`.
3. Start the server once to generate runtime data.
4. Review the settings files under `plugins/testproject/`.
5. If you use the HUD pack, install the ItemsAdder example content and run `/iazip`.
6. If you use territory sync, also install and configure WorldGuard. Dynmap is optional.

## Command roots

Admin-facing roots:

- `/terra`
- `/trader`
- `/merchant`
- `/climate`
- `/staff`
- `/flyspeed`
- `/vanish`
- `/rollbackarea`
- `/undoarea`

Mixed player and admin roots:

- `/jobs`
- `/guild`
- `/country`
- `/balance`
- `/spawn`
- `/countrychat` (`/cc`)
- `/globalchat` (`/gc`)

Use `/terra help` for the current admin command surface.

## Runtime layout

Bundled config and message files live in `src/main/resources/`.

The most important ones are:

- `plugin.yml`
- `settings/core.yml`
- `settings/guilds.yml`
- `settings/climate.yml`
- `settings/stability.yml`
- `settings/territories.yml`
- `settings/merchant.yml`
- `settings/freeport-merchants.yml`
- `settings/onboarding.yml`
- `settings/quests.yml`
- `jobs/config.yml`
- `jobs/*.yml`
- `messages/messages.yml`
- `messages/territories.yml`
- `scoreboard/config.yml`
- `chat/config.yml`

Generated runtime storage files include:

- `data.yml`
- `countries/data.yml`
- `guilds/data.yml`

## Current model

- `guild` is the main player-facing ownership and progression layer
- `country` is still the territory and settlement object underneath it
- climate, stability, merchants, and crafting are active gameplay systems, not placeholders
- Freeport starter merchants now use shared island stock with separate buy and sell pricing
- some onboarding and legacy admin flows still reference countries directly

## Notes for maintainers

- `src/main/java/me/meetrow/testproject/Testproject.java` is the central plugin class and owns most runtime state.
- Command handling is split mainly across `TerraCommand`, `GuildCommand`, and `CountryCommand`.
- The repository currently includes example pack content and generated zip artifacts for manual pack work.

For the operational details, config map, and admin workflows, use [PLUGIN_DOCUMENTATION.md](PLUGIN_DOCUMENTATION.md).
