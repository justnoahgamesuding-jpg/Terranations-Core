# Patch Notes

This file keeps the more detailed feature ledger for the current code snapshot. Dates are still best-effort, but the entries below match the current repository more closely than the older documentation did.

| Date | Feature | Notes |
| --- | --- | --- |
| 2026-04-30 | guild-first territory layer | Added a guild system with treasury, stockpile, roles, permissions, progression, upkeep-backed country claims, guild UI, and legacy country compatibility. |
| 2026-04-30 | guild invite UX | Added timed guild invites with clickable accept or deny chat actions, resend support, officer notifications, inviter tracking, and GUI-based pending invite management. |
| 2026-04-30 | onboarding and quest admin improvements | Added WorldEdit selection support for tutorial locations, a more interaction-driven quest editor flow, Fancy NPC random repeat dialogue, and NPC delivery quests. |
| 2026-04-30 | NPC custom-item reward persistence | Fancy NPC reward inventories now preserve full custom item stacks, including Terra item metadata, through save, load, and reward delivery. |
| 2026-04-30 | bat spawn toggle | Added config and commands to enable or disable bat spawning and remove existing bats when the toggle is turned off. |
| 2026-04-23 | Terra workbench playtest system | Added a standalone Terra crafting subsystem with placeable specialist workbenches, floating bench labels, saved placed-block identities, and dedicated crafting GUIs. |
| 2026-04-23 | `/terra catalog` | Added an admin GUI for spawning Terra workbench, ore, material, block, armor, and tool playtest items. |
| 2026-04-23 | placeholder Terra content set | Added a first placeholder batch of Terra ores, refined materials, blocks, armor, and tools intended to be reskinned through ItemsAdder later. |
| 2026-04-23 | crafting table redirect | Vanilla crafting tables are now blocked on interaction so the new Terra workbench layer can drive the crafting playtest flow. |
| 2026-04-14 | Terra Guide main menu | The locked guide item now opens player stats, jobs access, personal skills, contracts, country access, and ore-sense controls. |
| 2026-04-14 | personal skills | Players now earn skill points from level-ups and playtime and can spend them on permanent gameplay bonuses. |
| 2026-04-14 | personal work orders | The guide now surfaces personal profession work orders that reward money and skill points. |
| 2026-04-14 | Terra Guide slot item | Players now receive a locked soulbound `Terra Guide` nether star in hotbar slot 9. |
| 2026-04-14 | guide slot lock rules | The guide item cannot be moved, dropped, offhand-swapped, hotbar-swapped, or placed into containers. |
| 2026-04-14 | country level placeholders | `%terra_player_country_level%` and `%terra_current_country_level%` were added for external UI use. |
| 2026-04-14 | server time placeholders | `%terra_server_time%`, `%terra_server_date%`, and `%terra_server_datetime%` were added and aligned with the real-time clock timezone. |
| 2026-04-14 | climate placeholders | `%terra_climate_name%`, `%terra_climate_temperature%`, `%terra_climate_season%`, `%terra_climate_raining%`, and `%terra_climate_freezing%` were added. |
| 2026-04-14 | ItemsAdder-only pack flow | Terra's standalone resource-pack delivery settings were removed in favor of an ItemsAdder-only workflow. |
| 2026-04-14 | ItemsAdder pack cleanup | Broken font overrides and unused HUD panel assets were removed from the repository pack examples. |
| 2026-04-14 | quest stat sync removal | The old `terra_quest_steps` ItemsAdder stat sync path was disabled. |
| 2026-04-06 | `/flyspeed` standalone command | Fly speed is now exposed as its own admin command instead of only living under `/terra`. |
| 2026-04-06 | `/vanish` standalone command | Direct vanish control was added for admins. |
| 2026-04-06 | expanded `/staff` tooling | Staff mode grew into a broader moderation and utility surface. |
| 2026-04-06 | lag reduction suite | Timed item clearing, item merging, and mob stacking were added to the core plugin. |
| 2026-04-06 | maintenance mode | Maintenance join blocking, MOTD override, and allowlist support were added. |
| 2026-04-06 | join and leave presentation | Custom join or quit broadcasts and presence sounds were added. |
| 2026-04-06 | biome-aware climate adaptation | Climate now blends with biome temperature and humidity. |
| 2026-04-06 | global stability strictness | Stability no longer splits above-ground and underground strictness. |
| 2026-04-06 | grounded support validation | Floating support chains stopped counting as valid support. |
| 2026-04-01 | country admin GUI | `/country admin <country>` became part of the admin toolset. |
| 2026-04-01 | trader reputation override | `/country settraderreputation <country> <value>` was added. |
| 2026-04-01 | merchant admin GUI | `/merchant manage` became the main merchant editing workflow. |
| 2026-04-01 | stability meter | Hidden meter progression and temporary debug bursts were added. |
| 2026-04-01 | structure-focused stability pass | Stability tuning shifted harder toward large player-built structures. |
| 2026-03-30 | advanced climate simulation | Climate moved from a simple model to a layered world simulation. |
| 2026-03-30 | climate admin root expansion | `/climate` gained season, particle, altitude, playtest, and crop-guide controls. |
| 2026-03-30 | climate crop guide | `/climate crops` began exposing lore-bearing crop and sapling items in a GUI. |
| 2026-03-30 | climate item refresh | `/terra reload` now refreshes missing climate and starter-item metadata in loaded inventories. |
| 2026-03-30 | rain-aware farming | Rain now cools climate and grants a temporary growth bonus. |
| 2026-03-30 | farmer and lumberjack instant growth procs | Profession levels can trigger crop or tree instant-growth effects. |
| 2026-03-30 | stability system v1 and v2 | Material classes, support frames, collapse effects, rubble, wetness stress, and debug overlays landed across this window. |
| 2026-03-30 | cooldown HUD | Normal players gained a visible cooldown readout above the hotbar. |
| 2026-03-30 | playtest persistence | Playtest and XP-boost state began surviving reloads and restarts. |
| 2026-03-29 | local, country, and global chat routing | Local chat became default, country chat became toggle-based, and global chat became opt-in with `!message`. |
| 2026-03-29 | `/trader` and `/merchant` roots | Trader and merchant controls moved out of `/terra`. |
| 2026-03-29 | country browser GUI | `/country list` became a GUI browser. |
| 2026-03-27 | Terra-owned player balance system | Player balances moved into plugin-owned storage with `/balance`. |
| 2026-03-27 | route trader system | Country-hosted route traders, contracts, reputation, and specialty jobs were added. |
| 2026-03-27 | country trader controls | `/country manage settraderspawn` and `/country trade ...` arrived. |
| 2026-03-27 | fixed ore tooling | Fixed ore fill, regeneration variants, and the remover wand were expanded. |
| 2026-03-27 | ore vision | `/terra orevision` added admin ore overlays. |
| 2026-03-27 | playtest shutdown flow | Active playtests gained countdown-based stop and reset behavior. |
| 2026-03-26 | split cooldown tracks | Break and place cooldowns were separated. |
| 2026-03-26 | cooldown debug bossbars | `/terra cooldowndebug` was added. |
| 2026-03-26 | admin country warp tool | `/spawn` became the admin country warp command and GUI. |
| 2026-03-26 | country home territory validation | Setting home now requires standing inside linked country territory. |
| 2026-03-26 | persistent Terra spawn | `/terra setworldspawn` added a plugin-owned spawn location. |
| 2026-03-25 | starter kits and progression polish | Starter kits, GUI detail views, and profession quality-of-life upgrades expanded. |
| 2026-03-25 | blacksmith and furnace flow | Forge restrictions, smelting collaboration, and furnace ownership were refined. |
| 2026-03-25 | timed playtests | Timed playtests and admin controls were added. |
| 2026-03-17 | Terra core systems | `/terra`, `/jobs`, rewards, wilderness systems, and PlaceholderAPI support formed the main plugin base. |
| 2026-03-16 | country foundation | Country membership, territory, home, tag, and ownership flow were introduced. |

## Usage

- Keep this file focused on features that shipped.
- Keep the shorter narrative summary in [UPDATES.md](UPDATES.md).
