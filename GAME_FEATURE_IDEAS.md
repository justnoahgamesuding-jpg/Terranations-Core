# Game Feature Ideas And To-Do

This file is the active backlog for Terra Nations Core.

It should reflect what is still missing from the current game vision, not repeat features that already exist in the plugin.

## Current Vision

Terra is heading toward a slower, more deliberate multiplayer survival experience built around:

- job specialization instead of everyone doing everything
- countries that feel like real settlements, not just chat groups
- environmental pressure through climate, terrain, and geography
- supply chains between professions
- long-term nation progression instead of short-term loot rushing

The missing pieces now are mostly about cohesion, purpose, and long-term motivation.

## Highest Priority

### 1. First-Hour Onboarding

- Affects: new players, solo players, retention
- Why it matters: the core systems are strong now, but the first hour can still feel like being dropped into a complex world without a clear role.
- Goal: make the first session feel directed without removing freedom.
- Candidate work:
  - first-join guided sequence
  - profession explanation that is short and practical
  - one starter goal per profession
  - direct handoff into country play
  - early warnings about climate and structural stability
  - in-world or GUI checklist for the first hour

### 2. Stronger Country Purpose

- Affects: country leaders, group retention, mid-game progression
- Why it matters: countries now have homes, treasury, resources, upgrades, boosts, roles, territory, trader access, and farmland pressure, but they still need more shared reasons to exist.
- Goal: make countries feel like functioning communities with projects and identity.
- Candidate work:
  - country projects that consume treasury plus resources
  - country prosperity or development score
  - settlement stages or tiers
  - public country milestones
  - shared objectives that need multiple professions
  - stronger reasons to defend or maintain territory quality

### 3. Profession Loop Depth

- Affects: solo players, group economy, progression pacing
- Why it matters: the professions are already differentiated, but some loops still need more repeatable purpose beyond raw leveling.
- Goal: make each profession feel like it has meaningful work at every stage.
- Candidate work:
  - farmer supply contracts
  - builder infrastructure projects
  - blacksmith work orders
  - miner extraction objectives
  - lumberyard or forestry requests
  - profession-driven country tasks instead of generic grind

### 4. Better Progression Feedback

- Affects: all players
- Why it matters: Terra has many meaningful systems, but some progression moments still land too quietly.
- Goal: make advancement easier to understand and more satisfying.
- Candidate work:
  - stronger level-up feedback
  - clearer unlock summaries
  - second-job progress visibility
  - clearer blocked-action explanations
  - milestone reminders for countries and professions

## Near-Term Design Goals

### Country Identity And Presence

- Give each country more personality when entering or viewing it.
- Possible directions:
  - mottos
  - banners or insignia
  - stronger title presentation on entry
  - country visual identity on maps or GUIs
  - prestige presentation that players can actually feel

### Living Settlement Gameplay

- Push countries toward feeling inhabited and maintained rather than simply claimed.
- Possible directions:
  - upkeep systems tied to activity or projects
  - settlement bonuses for balanced profession populations
  - territory condition or fertility systems
  - localized bonuses around developed hubs

### Trader And Merchant Evolution

- The trader and merchant systems exist, but they can become more central to the world economy.
- Possible directions:
  - region-specific demand
  - merchant preferences based on country development
  - trade-route rivalry
  - seasonal trade shifts
  - supply shortages and surplus events

## World Simulation Ideas

### Weather And Season Consequences

- Expand climate from growth-only pressure into broader world behavior.
- Possible directions:
  - harsher winter farming windows
  - heat stress or cold stress on certain activities
  - weather-influenced profession output
  - seasonal resource demand

### Terrain And Infrastructure

- Stability already pushes players toward believable construction, but it could connect more strongly to settlement planning.
- Possible directions:
  - preferred support structures with stronger settlement aesthetics
  - quarry or mine infrastructure bonuses
  - road or transport advantages between major country locations
  - infrastructure projects that improve travel or logistics

## Long-Term Progression

### Endgame Goals

- Affects: max-level players, established countries
- Why it matters: once a player or country is stable, Terra needs larger ambitions than simple level gain.
- Candidate work:
  - mastery or prestige systems
  - elite profession branches
  - country wonders or landmark projects
  - late-game national bonuses tied to real investment

### Legendary Or Regional Resources

- Affects: exploration, economy, conflict, cooperation
- Why it matters: geography should matter more over time.
- Candidate work:
  - climate-locked crops or trees
  - region-specific materials
  - rare industrial or agricultural resources
  - incentives to trade rather than self-supply everything

## Admin And Operations Backlog

### Setup And Diagnostics

- clearer setup checklist for fresh servers
- territory sync diagnostics
- country health or progression diagnostics
- climate debugging summaries that are easier to read quickly
- easier playtest preset management

### Content Operations

- easier balancing workflow for jobs, rewards, and climate profiles
- safer live-edit admin tools
- quicker visibility into broken or underused progression paths

## Things That Are No Longer Backlog Items

These are now core implemented systems and should not come back into this file unless they are being replaced or significantly expanded:

- country upgrades
- country treasury and resources
- country roles
- trader spawn and trade-access controls
- local, country, and global chat routing
- climate crop guide
- climate-aware crop and sapling growth
- merchant admin GUI
- staff utility tooling
- maintenance mode
- lag-reduction systems
- stability meter and debug bursts

## Usage Notes

- Keep this file focused on future design and implementation gaps.
- Move shipped work into [PATCH_NOTES.md](PATCH_NOTES.md) or [UPDATES.md](UPDATES.md).
- If a feature is implemented enough that players can rely on it, remove it from this backlog.
