# Patch Notes

Dates below are based on the current source files in this workspace. Treat them as best-available feature dates for this local snapshot.

| Date | Command or Feature | Notes |
| --- | --- | --- |
| 2026-04-06 | `/flyspeed <0-10>` | Promoted fly speed control to its own standalone admin command while keeping the same simple self-targeted 0-10 scale. |
| 2026-04-06 | `/vanish [on\|off\|toggle]` | Added a direct vanish command so admins can hide or reveal themselves without going through the wider Terra command root. |
| 2026-04-06 | `/staff` staff menu expansion | Expanded staff mode into a broader GUI/admin utility system with vanish, freeze, player management, quick teleports, world spawn, ore vision, and other common moderation tools. |
| 2026-04-06 | built-in lag reduction tools | Added timed ground-item clearing, stronger dropped-item merging, and conservative mob stacking with visible stack counts to reduce entity load without relying on a separate cleanup plugin. |
| 2026-04-06 | trader and merchant cleanup exclusions | Excluded the custom trader and merchant NPCs from ground-item cleanup and mob-stacking routines so server cleanup does not break those systems. |
| 2026-04-06 | `/terra maintenance <on\|off\|status\|add\|remove\|list>` | Added maintenance mode with a server-list MOTD override, blocked normal joins, and an allowlist for approved players who can still connect during maintenance. |
| 2026-04-06 | custom join and leave messages | Added configurable join and quit broadcast messages through the bundled message configuration. |
| 2026-04-06 | biome-aware climate adaptation | Updated climate calculation to blend with each biome's own temperature and humidity so custom biome datapacks and heavily customized worlds fit the climate system better. |
| 2026-04-06 | global stability strictness | Removed the above-ground versus underground split from stability strictness and replaced it with one global `stability.strictness.percent` setting used everywhere. |
| 2026-04-06 | grounded support validation | Tightened support validation so floating player-built support chains no longer count as valid support unless they are actually grounded. |
| 2026-04-06 | simulator removal | Removed the abandoned simulator/world-simulation system, related commands, listeners, GUI handlers, and stored work-area data so it no longer ships in the plugin. |
| 2026-04-01 | structure-focused stability tuning | Rebalanced the stability system so strict collapse logic now focuses more on player-built structures than natural terrain, making realistic walls, roofs, posts, and supports matter more while reducing mining frustration. |
| 2026-04-01 | stability meter and temporary debug vision | Added a hidden per-player stability meter that builds from qualifying non-crop, non-functional block breaks and placements; each full charge grants a short burst of Terra stability debug vision. |
| 2026-04-01 | `/terra stability meter <on\|off\|status>` | Added a player toggle for showing or hiding the stability meter's chat display without disabling the underlying meter progression. |
| 2026-04-01 | `/merchant manage` merchant admin GUI | Added and iterated on an admin GUI for merchant timing and trade editing, including buy/sell material, amount, price, and stock controls. |
| 2026-04-01 | stale merchant cleanup | Fixed expired custom wandering merchants that could survive chunk unloads and later behave like normal vanilla wandering traders. |
| 2026-04-01 | `/country admin <country name>` | Added an admin country management GUI for viewing and adjusting country state. |
| 2026-04-01 | `/country settraderreputation <country name> <value>` | Added a direct admin command for setting the effective trader reputation value of a country. |
| 2026-04-01 | playtest remaining-time formatting | Updated playtest remaining-time formatting so long durations show days and hours first, then hours/minutes, then minutes/seconds as the session gets shorter. |
| 2026-03-30 | advanced climate simulation | Replaced the early simple climate model with a more layered system using latitude, humidity, continentality, current influence, altitude, season, day/night, and rainfall effects to produce more realistic temperatures and crop conditions. |
| 2026-03-30 | `/climate season <auto\|spring\|summer\|autumn\|winter>` | Added manual season override controls so admins can force a specific season or return to automatic seasonal calculation. |
| 2026-03-30 | `/climate display <on\|off>` | Added live local particle rendering so admins can see the generated climate around them without first making a preview area. |
| 2026-03-30 | `/climate create fullworld` | Added a full-world climate preview mode that uses the active world border as the bounds, matching Chunky/world-border style map generation. |
| 2026-03-30 | particle-based climate previews | Replaced the older block-painted climate preview with smoother particle-based preview rendering. |
| 2026-03-30 | `/climate crops` climate guide GUI | Added a crop guide GUI that exposes the actual crop, seed, sapling, and propagule item variants with their climate detail lore. |
| 2026-03-30 | sapling climate profiles | Added optimal season and temperature profiles for saplings and propagules, and tied tree growth behavior into climate checks. |
| 2026-03-30 | crop and sapling lore refresh after `/terra reload` | Added a reload-time pass that refreshes unlored or unnamed descriptive crop, sapling, and starter-kit items in online inventories, ender chests, and loaded containers. |
| 2026-03-30 | rain-aware farming | Rain now lowers local temperature, and recent rain grants a temporary crop growth bonus. |
| 2026-03-30 | crop climate particle feedback | Crops growing in strong optimal climate conditions now emit a periodic bonemeal-style happy particle effect. |
| 2026-03-30 | farmer instant-grow profession proc | Farmer level now grants a scaling chance for planting or bonemeal use to instantly finish crop growth. |
| 2026-03-30 | lumberjack instant-grow profession proc | Lumberjack level now grants a scaling chance for sapling placement to immediately force tree growth attempts. |
| 2026-03-30 | soulbound starter-kit restrictions expanded | Starter-kit soulbound items were already non-droppable; they now also cannot be transferred into chests or other containers. |
| 2026-03-30 | farmer tilling XP message | Turning soil into farmland now sends a farmer XP message through the dedicated tilling reward path. |
| 2026-03-30 | farmer crafting XP aggregation | Crafting food items now grants farmer XP based on the full number of items crafted, including bulk shift-crafting, with one aggregated payout/message. |
| 2026-03-30 | `/terra realtimeclock <on\|off\|status\|sync>` | Added direct admin control for the real-time day/night clock and later refined it so configured worlds track real local time more reliably. |
| 2026-03-30 | `/terra hungerspeed <multiplier\|status>` | Added an admin command to scale global hunger drain speed. |
| 2026-03-30 | stability system v1 | Added local terrain stability checks with support-aware cave-ins for unsupported loose materials and fragile spans. |
| 2026-03-30 | stability system v2 | Expanded stability into a broader excavation system with material classes, stress scoring, support-frame recognition, warning phases, sideways collapse motion, wetness stress, rubble, shaft-wall cave-ins, chunk-safe handling, and a debug overlay. |
| 2026-03-30 | grounded loose-stack tolerance | Short grounded loose stacks such as a 2-high dirt column can remain, while unsupported floating loose structures still collapse. |
| 2026-03-30 | merchant and trader single-host fix | Trader and merchant waves were corrected so they no longer repeatedly spawn across multiple countries in the same cycle. |
| 2026-03-30 | normal-player cooldown HUD above hotbar | Added a persistent player-facing action-bar cooldown display for break and place timers so normal players can see active cooldowns above the hotbar. |
| 2026-03-30 | `/terra playtest extend <time>` | Added an admin playtest extension command that increases the remaining duration for either a pending playtest countdown or an already active playtest session. |
| 2026-03-30 | playtest PlaceholderAPI time placeholders | Added playtest status and remaining-time placeholders so external sidebar/scoreboard plugins can render playtest timing without relying on the action bar. |
| 2026-03-29 | merchant country spawn announcement | When a wandering merchant wave spawns, online members of the host country now receive a private arrival message for their own country merchant. |
| 2026-03-29 | `/trader` and `/merchant` root commands | Moved trader and merchant admin controls from `/terra trader ...` and `/terra merchant ...` to dedicated `/trader ...` and `/merchant ...` roots while keeping the same admin-only behavior. |
| 2026-03-29 | local, country, and global chat routing | Normal chat is now local within 50 blocks, `/cc` and `/country chat` toggle country-chat mode for normal messages, and `!message` uses opt-in global chat with a 5 minute cooldown. |
| 2026-03-29 | `/country list` browser GUI | Replaced the plain text country list with a GUI browser that can filter open/closed countries and sort by alphabetical order, online players, member count, or country level. |
| 2026-03-27 | custom money system | Replaced Essentials/Vault-backed player balances with Terra-owned stored balances in `data.yml`, updated reward payouts and placeholders to use it, and added `/balance` with admin balance management subcommands. |
| 2026-03-27 | WorldEdit selection fixed ore fill | Added `/terra fixedore fill <source> <ore>` to convert matching blocks inside the player's WorldEdit selection into tracked fixed ore nodes. |
| 2026-03-27 | fixed node stripped wood placeholders | Fixed wood-based fixed nodes now use their matching stripped wood variant during regeneration instead of bedrock, while ore and stone nodes still use bedrock. |
| 2026-03-27 | dynamic trader quest system | Added timed traveling traders with personal job-locked contracts, persistent trader reputation, per-player accepted contracts, delivery-ready timers, and trader menu interaction with reward effects. |
| 2026-03-27 | slower trader progression pacing | Trader route reputation now uses fractional gains like `+0.1`, quest delivery has a default 30-minute prep delay before turn-in opens, and menu states now show waiting versus ready-to-deliver contract stages. |
| 2026-03-27 | country-based route traders | Traders are now country-hosted, each active trader has a generated name and specialty job, matching specialty jobs earn boosted trader rewards, and only the host country plus allowed trade-list countries can use that trader. |
| 2026-03-27 | country trader management commands | Added `/country manage settraderspawn` plus `/country trade allow <country>`, `/country trade remove <country>`, and `/country trade list` to manage route trader spawn locations and cross-country trade access. |
| 2026-03-27 | country trader info and dashboard controls | Country info now includes trader spawn status, allowed trade countries, and the last trader seen in that country, and the country dashboard now has a trader spawn setup button. |
| 2026-03-27 | `/terra flyspeed <0-10>` | Added the original admin self-command for personal fly speed on a simple `0-10` scale; this was later promoted to the standalone `/flyspeed` command. |
| 2026-03-27 | `/terra orevision` | Added an admin toggle that highlights nearby ores through terrain using per-admin glowing block-display overlays colored by ore type. |
| 2026-03-27 | `/terra fixedoretool` admin wand | Added a tagged admin remover wand that deletes fixed ore nodes by right-clicking them. |
| 2026-03-27 | playtest stop countdown | `/terra playtest stop` now starts a 10-second shutdown countdown for active playtests before the session fully ends. |
| 2026-03-27 | playtest end full player reset | When an active playtest ends or is stopped, online players are teleported to spawn, their inventory and Minecraft XP are wiped, plugin-owned player data is reset, country state is cleared, and forced primary-job selection starts again with the second slot locked behind normal progression. |
| 2026-03-27 | WorldGuard country setup docs clarified | Updated the country territory setup guide to explicitly require `//sel poly` for shaped country borders and `//expand vert` so linked regions span from top to bottom before `/rg define` and `/country territory setregion`. |
| 2026-03-26 | split break and place cooldowns | Separated block breaking and block placing cooldown tracks so one action no longer blocks the other, while both still keep the same configured cooldown length. |
| 2026-03-26 | `/terra cooldowndebug` bossbars | Added an admin debug toggle that shows live bossbars for break and place cooldown states independently. |
| 2026-03-26 | playtest and XP boost reload persistence | Playtest countdowns, active playtests, and active global XP boosts now continue correctly across `/reload` and full server restarts. |
| 2026-03-26 | safer forced profession selection | Players who still need to choose a job are now moved to a safe spawn position before the profession menu is forced open, preventing fluid and collision lockups at login. |
| 2026-03-26 | farmer planting and bonemeal XP feedback | Planting crops now shows farmer XP gain messages, and using bonemeal on supported growable farmer crops also grants XP with its own message. |
| 2026-03-26 | crop harvesting hoe durability | Hoes now lose durability when used to break crops successfully. |
| 2026-03-26 | miner progression cooldown text | Miner level progression now shows the configured break-cooldown reduction in the unlock list for later levels. |
| 2026-03-26 | double-drop player feedback | Double-drop triggers now show an action-bar confirmation and play a pickup sound for the triggering player. |
| 2026-03-26 | furnace lock owner name resolution | Furnace lock warnings now resolve and display the assigned player's username from stored lock data instead of falling back to a generic "another player" message. |
| 2026-03-26 | admin `/spawn` country warp tool | Reworked `/spawn` into an admin country-support tool using `terra.country.warpadmin`, with a paged warp GUI by default, direct `/spawn <country name>` warps, and country-name tab completion. |
| 2026-03-26 | `/country manage sethome` territory validation | Added explicit country-home placement through `/country manage sethome`, restricted to eligible admins/owners standing inside their own linked territory before the home can be saved. |
| 2026-03-26 | `/country home` cooldown enforcement | Country-home teleports now honor a configurable `country-home.cooldown-seconds` timer, block early reuse with a remaining-seconds message, and continue using the stored country home location when valid. |
| 2026-03-26 | `/terra setworldspawn` | Added an admin command to save a persistent global spawn location from the player's current position, and updated plugin-owned spawn teleports to use that saved location first. |
| 2026-03-26 | Trader and Soldier removed from jobs | Removed Trader and Soldier from the profession enum and bundled job resources so they no longer ship as selectable or configurable jobs. |
