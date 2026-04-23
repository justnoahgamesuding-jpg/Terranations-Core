# Updates

This file is the short, high-level timeline for the current repository snapshot.

## 2026-04-23

### Terra crafting moved toward a standalone workbench model

- A new playtest-only Terra crafting layer was added to separate progression from the vanilla crafting table.
- Seven specialist workbenches were introduced as placeable blocks with floating name labels and dedicated right-click crafting menus.
- Each workbench now supports general recipes for anyone plus specialist output bonuses and one specialist-only recipe for its linked job.
- A first placeholder batch of Terra ores, refined materials, blocks, tools, and armor was added so ItemsAdder visuals can be swapped in later.
- `/terra catalog` now opens an admin spawn GUI for the current Terra crafting categories and content entries.

## 2026-04-14

### UI and player guidance shifted away from the old pack-driven approach

- Terra's standalone resource-pack delivery flow was removed in favor of an ItemsAdder-only path.
- The old custom font override was removed from the bundled pack examples after it caused client load failures.
- New PlaceholderAPI values were added for country level, local or server time, and climate display so external scoreboards can carry more gameplay context.

### A dedicated player guide item was introduced

- Every player now receives a locked `Terra Guide` item in hotbar slot 9.
- The guide item is soulbound and cannot be moved, dropped, swapped, or stored.
- The guide now opens the first real player hub with stats, personal skills, contracts, jobs access, country access, and ore-sense toggles.
- Players now earn skill points from level-ups and playtime, and can spend them on permanent progression bonuses.

## 2026-04-06

### Staff and server operations expanded

- `/flyspeed` became a standalone command and `/vanish` was added as a direct admin shortcut.
- `/staff` grew into a broader moderation and utility entry point.
- Built-in lag reduction now covers scheduled item clears, item merging, and conservative mob stacking.
- Maintenance mode, join or leave messaging, and presence sounds were added to the core server utility set.

### Climate and stability were tuned for real worlds

- Climate now blends against biome data so heavily customized worlds behave more naturally.
- Stability now uses one global strictness model instead of split above-ground versus underground profiles.
- Floating support chains were tightened so only grounded support counts.

## 2026-04-01

### Countries gained stronger progression

- Country admin tools expanded.
- Country trader reputation overrides were added.
- Merchant management became a dedicated GUI workflow.
- The current codebase now also carries treasury, resources, upgrades, boosts, and roles as core country concepts.

### Stability shifted toward structure realism

- Stability checks became more focused on player-built spans, walls, roofs, docks, and columns.
- The hidden stability meter and temporary debug-vision bursts landed.

## 2026-03-30

### Climate became a full gameplay system

- Latitude, humidity, current influence, altitude, season, rainfall, and biome adaptation now drive local conditions.
- Rain now affects temperature and crop performance.
- Saplings and propagules joined crops in the climate model.

### Farming and climate tools expanded

- `/climate` became a full admin root instead of a narrow debug command.
- Climate previews moved to particles.
- `/climate crops` became the player-facing item and lore inspection surface.
- Crop and sapling climate lore refresh now runs on `/terra reload`.

### Stability became a real excavation system

- Material classes, support logic, collapse handling, warning feedback, rubble, and wetness stress all landed in this feature batch.

## 2026-03-29

### Communication shifted away from global-by-default chat

- Local chat became the default.
- Country chat moved to toggle mode.
- Global chat became opt-in and uses `!message`.

### Trader and merchant controls split out cleanly

- `/trader` and `/merchant` were separated from `/terra`.
- Country browsing moved into a GUI flow.

## 2026-03-27

### Terra-owned economy arrived

- Player balances stopped relying on an external money plugin for storage.
- `/balance` became the main balance command.
- Country-linked trader systems and fixed-node tooling expanded significantly.

### Playtests became session-based

- Timed playtests, shutdown flow, and reset behavior were added.

## 2026-03-26

### Core quality-of-life systems improved

- Break and place cooldowns were split.
- `/terra cooldowndebug` was added.
- `/spawn` became an admin country warp tool.
- `/terra setworldspawn` and country-home cooldown support were added.

## 2026-03-25

### Profession and admin tooling accelerated

- Starter kits, level-detail views, playtests, job caps, and expanded `/jobs admin` management landed.
- Blacksmith, furnace ownership, and profession restrictions were refined heavily.

## 2026-03-17

### Terra core systems landed

- `/terra`, `/jobs`, rewards, wilderness systems, and PlaceholderAPI support formed the main plugin base.

## 2026-03-16

### Countries landed

- The original country membership, territory, home, tag, and ownership flows entered the project.

## Usage

- Keep this file high level.
- Put feature-by-feature detail in [PATCH_NOTES.md](PATCH_NOTES.md).
