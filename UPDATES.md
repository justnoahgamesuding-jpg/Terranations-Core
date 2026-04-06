# Updates

This file is the high-level change timeline. Use it to see the major feature batches without reading the full patch ledger.

## 2026-04-06

### Server utility and staff tooling expansion

- `/flyspeed` was promoted to a standalone command, `/vanish` was added, and `/staff` grew into a broader staff menu with moderation and admin utility actions.
- Built-in lag reduction was added with timed ground-item clears, item merging, and conservative mob stacking that shows stack counts above entities.
- Traders and merchants are excluded from the cleanup systems so they are not removed or stacked by the lag tools.
- Maintenance mode was added with a pre-login server-list MOTD, join blocking, and a maintenance-access allowlist.
- Custom join and leave messages were added for easier server-specific presentation.

### Climate and stability rules were simplified for real worlds

- Climate now blends in each biome's own temperature and humidity so datapack/custom-biome worlds such as Terralith fit the farming system better.
- Stability no longer uses separate above-ground and underground profiles; the same rule set now applies everywhere with one global strictness slider.
- Floating supports were tightened so player-built support chains only count when they are actually grounded rather than hanging from other unsupported blocks.

## 2026-03-30

### Climate simulation became a full gameplay system

- Climate now uses latitude, humidity, continentality, current influence, altitude, season, day/night, and rain instead of the earlier simple equator-only model.
- The temperature display can still use Celsius or Fahrenheit, but the actual climate calculation is now much more detailed and more suitable for both playtests and full-world mapping.
- Rain now cools the local temperature, and crops gain a temporary growth bonus after recent rain.
- Saplings and propagules now also use climate profiles, not just farm crops.

### Climate tools, display, and crop guide expanded

- `/climate` grew into a full admin climate control root with status, unit, season control, bossbar, water freezing, live particle display, altitude tuning, playtest climate controls, preview creation, preview cleanup, and crop-guide access.
- Climate previews now use particles instead of wool/concrete platforms.
- `/climate create fullworld` now registers a full-world preview bounded by the active world border, which fits Chunky-style bordered worlds.
- `/climate display on` lets an admin see the already-generated climate directly around them without creating a preview area first.
- `/climate crops` now opens a guide GUI that exposes the actual crop, seed, sapling, and propagule variants with their climate lore.

### Farming progression and item detail improved

- Crop drops and saplings now carry optimal climate lore.
- After `/terra reload`, unlored or unnamed climate-profile items and starter-kit tools are refreshed in player inventories, ender chests, and loaded containers.
- Farmer XP feedback was corrected for planting and bonemeal, tilling now sends its own XP message, and food crafting now awards aggregated XP based on the full crafted stack size.
- Farmer and Lumberjack levels now grant a scaling chance to trigger instant growth on crops or saplings.
- Crops in very good climate conditions now emit a periodic happy-growth particle effect.

### Stability and excavation rules became a real system

- A first-pass cave-in and structural stability system was added and then expanded.
- Loose, packed-soil, soft-rock, fragile-roof, and hard-rock classes now behave differently.
- Unsupported roofs, floating loose masses, freestanding loose walls, and unsupported shaft walls can now collapse.
- Wood/log/fence support structures now matter, and the system recognizes support frames in addition to simple support radius.
- Collapse handling now includes warning phases, sideways spill motion, rubble, wetness stress, chunk-safe handling, and a player debug overlay.

### Admin tooling updates

- `/terra realtimeclock` now controls real local-time day/night syncing more directly.
- `/terra hungerspeed` was added for global hunger drain tuning.
- Trader and merchant spawning were corrected so they no longer fan out across multiple countries at once.

## 2026-04-01

### Stability became more building-focused

- The stability system was rebalanced so strict collapse checks now target player-built structures much more than ordinary natural terrain.
- Heavy walls, stacked masonry, unsupported roofs, docks, bridges, and overhangs now push players toward realistic support patterns such as posts, frames, braces, and buttresses.
- Early progression is more forgiving because the system now respects the fact that low-level players operate on much slower break and place cooldowns.

### Stability meter and debug bursts were added

- Qualifying break and place actions now build a hidden stability meter in the background.
- Each full charge grants a short temporary burst of Terra stability debug vision, giving players occasional insight into unstable blocks while building.
- Players can use `/terra stability meter on` if they want to see the meter status in chat, or leave it off and still gain the hidden progression.

### Merchant and country admin tools expanded

- `/merchant manage` was added and refined into a dedicated admin GUI for controlling merchant timing and trade entries.
- `/country admin <country>` was added as an admin country management GUI.
- `/country settraderreputation <country> <value>` was added for direct trader-reputation control.

### Playtest and merchant cleanup polish

- Playtest remaining time now formats more cleanly at long durations by shifting between `days + hours`, `hours + minutes`, and `minutes + seconds`.
- Expired custom wandering merchants now clean up more reliably instead of surviving as normal vanilla wandering traders after chunk unload/load cycles.

## 2026-03-29

### Chat routing and communication friction

- Normal player chat now stays local within 50 blocks by default instead of automatically reaching the whole server.
- Country chat became a toggle mode through `/cc` or `/country chat`, so players can switch their normal messages into country-only communication when needed.
- Global chat became opt-in through `/gc`, and global messages are now sent by prefixing the message with `!` while respecting a 5 minute cooldown.

### Country browsing and trader command cleanup

- `/country list` now opens a country browser GUI with filters for open or closed countries and sorting by alphabetical order, online players, member count, or country level.
- Trader and merchant admin controls were moved out of `/terra` into dedicated `/trader` and `/merchant` command roots.
- Wandering merchant spawns now send a private announcement to online members of the host country.

## 2026-03-27

### Economy, admin tools, and fixed-node expansion

- Terra now uses its own stored balance system with `/balance` management commands instead of depending on Essentials/Vault-backed player money.
- Fixed ore tooling expanded with WorldEdit selection filling, a dedicated fixed-ore remover wand, and wood-node regeneration that temporarily swaps to stripped wood instead of bedrock.
- New admin utility commands were added for fly speed control and ore vision overlays through terrain.

### Trader routes and country-owned traders

- The trader system was added and then expanded into a slower long-term route system with job-locked contracts, delivery prep timers, fractional reputation gains, and better trader menu feedback.
- Active traders are now country-hosted, have names and job specialties, and can grant better rewards to matching jobs.
- Countries now manage trader spawn points, allowed trade-country access lists, and trader route visibility through country info and dashboard controls.

### Playtest shutdown and cleanup

- Active playtests now stop through a 10-second shutdown countdown instead of ending immediately.
- When an active playtest ends or is stopped, players are sent to spawn, inventory and Minecraft XP are cleared, plugin-owned player data is wiped, country state is cleared, and fresh primary-job selection is forced again.

### Documentation and setup clarity

- WorldGuard country setup instructions now explicitly require `//sel poly` for the border outline and `//expand vert` before region definition.

## 2026-03-26

### Cooldown quality-of-life and debug improvements

- Break and place cooldowns were split into separate tracks.
- `/terra cooldowndebug` was added with live bossbars for both cooldown types.
- Miner progression text now clearly shows cooldown reduction unlocks.

### Country travel and territory setup improvements

- `/spawn` was reworked into an admin country warp tool with GUI support and direct country-name warping.
- `/country manage sethome` now validates that the player is standing inside their own linked territory.
- `/country home` now respects a configurable cooldown.
- `/terra setworldspawn` was added for persistent plugin-owned spawn teleports.

### Feedback and onboarding polish

- Forced profession selection became safer by moving players to a safer spawn position before the menu opens.
- Double-drop feedback now uses action bar plus sound.
- Furnace lock messages now resolve the stored owner name properly.
- Farmer planting and bonemeal actions now provide XP feedback.
- Crop harvesting now consumes hoe durability properly.

### Persistence and cleanup

- Active playtests and active global XP boosts now survive `/reload` and full restarts.
- Trader and Soldier were removed from the profession set and bundled job resources.

## 2026-03-25

### Playtest and admin tooling expansion

- Timed playtests were added with countdowns, dual-job testing, and periodic reminder broadcasts.
- World snapshot/restore logic was removed from playtests so they run as session-based testing only.
- `/terra cleardata <player>` was added as a full fresh-start reset tool.
- `/terra jobcap <job> <amount>` added per-job player caps.
- `/jobs admin` gained broader GUI-based profession management and development-mode controls.

### Profession system growth

- Profession starter kits were added.
- Primary-job respawn kits were added.
- Profession level detail GUI was added.
- Double-drop chances were added for Miner, Farmer, and Lumberjack.
- Restriction message formatting was standardized.
- Miner and Farmer progression text was cleaned up.

### Blacksmith, smelting, and item flow

- The Blacksmith forge GUI was added.
- The Blacksmith anvil GUI was cleaned up.
- Forge-linked normal crafting restrictions were added.
- Miner ore smelting rules and forge-only furnace crafting were added.
- Furnace ownership and smelting feedback were refined several times.

### Country system and GUI improvements

- `/country` became a dashboard GUI entry point.
- Country creation became admin-only.
- Country member views were cleaned up, including grouped job display and better member info.
- Country transfer visibility improved for owners.
- Trader and Soldier were removed from country member job lanes.

### Economy and reward flow

- Global XP boosts were added with boss bar support and lockout rules.
- Block drops now go directly to inventory where possible.
- Farmer planting XP was added.

## 2026-03-17

### Core Terra systems landed

- `/terra` admin command surface was established.
- `/jobs` profession menu, switching, and admin tooling were established.
- Profession progression, action restrictions, crafting restrictions, and dual-job flow were added.
- Block delay, bypasses, wilderness regeneration, rewards, hostile mob toggles, phantom toggles, reload, and hard restart support were added.
- Furnace collaboration, PlaceholderAPI support, farmland cap enforcement, and real-time clock syncing were added.
- Staff mode was added.

## 2026-03-16

### Country foundation landed

- `/country` command root and the core membership flow were added.
- Country creation, joining, leaving, inviting, disbanding, ownership transfer, renaming, tagging, and listing were added.
- Country home support was added.
- Country territory binding, sync, info, and debug support were added.
- Territory enter/leave notifications were added.
- Plugin-list protection and staff mode command support were present from the initial base.

## How To Use This File

- Add a new dated section when a major feature batch lands.
- Keep this file high level.
- Put detailed command-by-command or fix-by-fix history in [PATCH_NOTES.md](/C:/Users/noahu/customplugins/testproject/PATCH_NOTES.md).
