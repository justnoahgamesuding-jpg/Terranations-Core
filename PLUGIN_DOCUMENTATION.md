# Terra Server Setup Guide

This document is written for server owners, admins, and builders who need to set Terra up properly and understand how the plugin behaves in play.

The goal here is simple:

- explain what each major system does
- explain which files control it
- explain how to set it up in a sensible order
- keep the writing easy to read without turning it into a shallow summary

## What Terra is

Terra is not a small utility plugin. It is a full survival framework built around progression, specialization, territory, economy, and server-controlled world rules.

The main parts of the plugin are:

- professions and job progression
- guilds
- countries and territory
- climate-based farming
- stability and cave-in rules
- Terra workbench crafting
- traders and merchants
- onboarding and tutorial content
- Terra-owned player balances
- chat routing
- staff and server utility tools

The current live model is `guild first`.

That means players will mainly feel the server through guilds, but countries still matter because they hold the actual settlement and territory layer underneath guild claims.

## Before you install it

### Required platform

- Java `21`
- Paper `1.21.1-R0.1-SNAPSHOT`
- Maven if you are building from source

### Optional integrations

Terra can run without every integration, but some systems are much better when the matching plugin is installed.

- `WorldGuard`
  Needed for proper country territory linking.
- `WorldEdit`
  Useful for setup flows that use selections.
- `Dynmap`
  Optional map display for country regions.
- `ItemsAdder`
  Used for the HUD/resource pack path this project now prefers.
- `FancyNpcs`
  Used by the onboarding and dialogue systems.
- `PlaceholderAPI`
  Used for placeholders in HUD or external displays.
- `CoreProtect`
  Needed for `/rollbackarea` and `/undoarea`.
- `LuckPerms`
  Useful for server permission structure.
- `Vault`
  Present as a soft dependency, though Terra owns its own balance storage.
- `SimpleScore`
  Optional scoreboard support.

## Install order

If you want the cleanest setup, do it in this order instead of configuring everything at once.

1. Install Terra and start the server once.
2. Stop the server and review every file under `plugins/testproject/`.
3. Configure the core gameplay file first.
4. Configure guilds and countries next.
5. Configure climate and stability before opening survival.
6. Configure onboarding and quests before inviting fresh players.
7. Configure merchants and traders after your economy pace feels right.
8. Add ItemsAdder HUD content if you want the visual layer.
9. Link territories only after your WorldGuard regions are ready.
10. Back up your Terra data before testing resets, claims, or admin cleanup commands.

## Important files

These are the files most server admins will actually need.

### Main settings

- `settings/core.yml`
- `settings/guilds.yml`
- `settings/climate.yml`
- `settings/stability.yml`
- `settings/territories.yml`
- `settings/merchant.yml`
- `settings/freeport-merchants.yml`
- `settings/onboarding.yml`
- `settings/quests.yml`

### Supporting settings

- `jobs/config.yml`
- `jobs/*.yml`
- `messages/messages.yml`
- `messages/territories.yml`
- `chat/config.yml`
- `scoreboard/config.yml`

### Runtime data

- `data.yml`
- `countries/data.yml`
- `guilds/data.yml`

If you are going to test destructive admin commands, change progression rules heavily, or wipe player state, back these files up first.

## First server setup

This is the safest first-pass setup for a live Terra server.

### 1. Review `settings/core.yml`

This file controls the broad server rules.

The most important sections are:

- `block-delay`
  Shared break/place cooldown behavior.
- `rewards`
  Whether block actions give rewards.
- `economy`
  Global money scaling.
- `hunger`
  Hunger rate and climate-related hunger pressure.
- `items`
  Ender pearl, shulker box, and per-material restrictions.
- `hostile-mobs`, `phantoms`, `bats`
  Core mob toggles.
- `wilderness-regeneration`
  Wilderness cleanup and decay timing.
- `realtime-clock`
  Real-world time sync settings.
- `server-list-motd`
  Animated server list MOTD frames.
- `itemsadder-top-status`
  Top-center HUD display settings.
- `lag-reduction`
  Ground item clearing, item merging, and mob stacking.

Practical advice:

- keep `economy.reward-scale` conservative at launch
- keep lag reduction enabled unless you have a strong reason not to
- decide early whether you want phantom spawning on or off
- review `items.materials` if you want to hard-block specific utility blocks

### 2. Review `settings/guilds.yml`

This file controls the main player ownership layer.

Key sections:

- `creation`
  Guild creation cost and tag rules.
- `invites`
  Invite duration and recruiter notifications.
- `treasury`
  Withdraw limits by role.
- `claims`
  Country claim cost, reclaim cooldown, and minimum guild strength.
- `upkeep`
  Inactivity and upkeep reduction behavior.
- `progression`
  Guild XP, score, member cap, claim cap, and level thresholds.

Practical advice:

- set `create-cost` high enough that guild creation feels deliberate
- keep `minimum-claim-members` and `minimum-claim-total-levels` high enough to stop instant land grabs
- test `country-claim-base-cost` against your real earning pace, not your guess
- be careful with officer and admiral withdrawal limits because treasury abuse is one of the easiest ways to destabilize settlement gameplay

### 3. Review `settings/territories.yml`

This file matters if countries are going to be linked to real land.

It controls:

- whether territory integration is enabled
- chat notifications on entry
- title notifications on entry
- Dynmap marker style
- passive country border particles

Practical advice:

- do not enable territory-heavy gameplay before your WorldGuard regions are named and tested
- keep title notifications readable and not too noisy
- if using Dynmap, confirm the color choices make open/closed/ownerless land easy to understand

### 4. Review `settings/climate.yml`

This is one of the most important balance files in the plugin because it changes where farming works well.

It controls:

- the master climate toggle
- temperature unit
- equator behavior
- seasonal override
- large-scale climate pattern
- biome adaptation
- altitude penalties
- rain effects
- crop particle effects
- freeze-water behavior
- climate debug displays

What this system does in practice:

- different places on the map will feel better or worse for different crops
- altitude matters
- rain helps in some conditions
- seasons can shift how forgiving farming feels
- biome blending helps custom world generation feel less wrong

Practical advice:

- leave `biome-adaptation.enabled` on unless you have a very controlled custom world reason to disable it
- do not overtune temperature values before you playtest multiple regions
- if your server uses large mountains, read the `altitude` section carefully
- if players should survive more easily early on, tune hunger and climate together, not separately

### 5. Review `settings/stability.yml`

This file controls whether players can dig and build like vanilla or whether the world demands support and structure.

It controls:

- the master stability toggle
- scan radius and warning delay
- support detection rules
- span allowances by material type
- wetness stress
- load and weight tracing
- global strictness
- separate mining leniency for natural terrain
- rubble chance
- stability meter behavior

What this system does in practice:

- unsupported mines can cave in
- long spans and weak roofs are punished
- player-built structures are judged more strictly than natural terrain
- rain and nearby water can make areas riskier

Practical advice:

- if your community is not used to this kind of survival pressure, keep `strictness.percent` near the default at first
- do not make underground tunneling too punishing until you test natural cave tolerance
- when players complain about collapses, check whether the issue is real balance or just missing support education

### 6. Review `settings/onboarding.yml` and `settings/quests.yml`

These files control the first-hour experience.

`onboarding.yml` covers:

- whether onboarding is enabled
- starter hub behavior
- profession trial requirements
- objective HUD
- NPC focus mode
- NPC dialogue text

What onboarding does:

- it delays full profession commitment
- it pushes players through guided trials
- it gives the server a controlled first impression instead of dumping players into raw survival

Practical advice:

- if Terra is central to your server identity, do not skip onboarding setup
- use a real starter hub location instead of leaving it vague
- make sure the dialogue and NPC positions match what players actually see in the world
- keep trial thresholds high enough to teach the loop, but not so high that players feel trapped

### 7. Review `settings/merchant.yml` and `settings/freeport-merchants.yml`

These files control the merchant economy.

`merchant.yml` controls:

- wandering merchant wave timing
- active duration
- rotation frequency
- trade cooldown
- merchant spawn radius
- buy rotations
- sell offers

`freeport-merchants.yml` controls:

- fixed starter merchants
- their display names
- icons
- exact offers
- cooldowns

What the merchant systems do:

- `merchant-shop` is a rotating wave market
- `freeport-merchants` are stable starter sellers/buyers for early progression

Practical advice:

- fixed starter merchants should help new players move, not let them skip progression
- keep early prices modest and predictable
- use the wave merchant for variety, not as the entire economy
- do not set trade cooldowns so low that players can spam the market faster than gathering loops can support

## Major gameplay systems

This section explains the plugin's systems in plain server-owner terms.

### Professions

Terra is built around specialization. Players are not supposed to feel equally good at every loop from the start.

The current core professions are:

- Miner
- Lumberjack
- Farmer
- Builder
- Blacksmith
- Trader
- Soldier

Profession files live under `jobs/`.

What professions change:

- leveling pace
- access and efficiency
- progression identity
- how useful a player is inside a guild economy

Admin guidance:

- avoid making every profession level equally fast
- make sure Trader and Builder are still worth choosing, not just Miner and Farmer
- if players are power-leveling one job too easily, fix the reward or progression file for that job rather than inflating every other system around it

### Guilds

Guilds are the main social and progression layer players should care about.

Guilds handle:

- membership
- treasury
- stockpiles
- role structure
- permission overrides
- recruiting state
- progression level
- claiming countries
- upkeep on claimed land

What makes guilds important:

- they are the main bridge between personal progression and shared territory
- they decide who can claim and hold land
- they turn collected resources into long-term settlement power

Admin guidance:

- train staff to think in guild terms first
- use countries as the territorial object, but explain the world to players through guilds
- watch treasury values and withdrawal permissions closely during the first weeks of a server

### Countries

Countries are still important even though guilds are the main player-facing layer.

Countries still hold:

- territory links
- homes
- some progression and boosts
- trader and settlement-related setup
- legacy administrative flows

Think of countries as the map object and guilds as the ownership and social layer sitting on top of them.

Admin guidance:

- keep country naming and region linking tidy
- document which countries are player-facing and which are system-only
- do not let hidden/system countries leak into public presentation if they are only there for backend logic

### Climate

Climate is not cosmetic. It changes farming and world feel.

Players should learn that:

- location matters
- altitude matters
- seasons matter
- not every farming area is equally good

Admin guidance:

- test warm, cold, low, and high regions before launch
- make sure staff can answer "why won't this crop grow well here?"
- if players feel farming is random, the usual problem is feedback or setup, not the feature itself

### Stability

Stability exists to stop the world from playing like normal no-consequence vanilla mining and building.

Players should learn that:

- support matters
- large unsupported spans are risky
- poor excavation can cause cave-ins
- bad terrain choices can punish greedy mining

Admin guidance:

- explain this system early in onboarding or server rules
- staff should know the difference between intended collapse behavior and actual bugs
- if a server is casual, reduce strictness instead of disabling the system entirely

### Terra workbench crafting

Terra includes its own crafting direction instead of relying only on vanilla crafting tables.

The current setup includes:

- specialist benches
- custom crafting flows
- admin catalog access
- progression-linked crafting structure

What this means for your server:

- crafting can be used to push profession identity
- stations matter
- the plugin can control access and recipe flow more tightly than vanilla

Admin guidance:

- if using this system heavily, teach players which bench is for what
- make sure starter areas actually expose the benches players are expected to learn
- do not leave bench content half-configured if you want the system to feel intentional

### Traders and merchants

Terra has more than one economy-facing NPC loop.

Broadly:

- traders are route/economic progression systems
- merchants are buy/sell shop systems
- Freeport merchants are early-game fixed support merchants

Admin guidance:

- use Freeport merchants to stabilize the start
- use merchant waves to create movement and short-term opportunities
- do not let buy prices trivialize core gathering loops

### Onboarding and tutorial

This system matters more than most servers think.

A plugin as large as Terra becomes much harder to understand if new players spawn in with no guidance.

What good onboarding should do:

- introduce the survival tone
- explain the Terra Guide and starter flow
- teach that profession choice matters
- teach that guilds matter
- prepare players for climate and stability rules

Admin guidance:

- keep onboarding enabled unless your server has a deliberate alternative
- match NPC placement, signs, and routes to the actual dialogue text
- test the full onboarding flow with a fresh account before every major release

### Chat routing

Terra supports local, country, and global chat behavior.

This matters because it changes how a survival server feels socially.

Admin guidance:

- decide whether you want a quieter local-first world or a louder server-wide chat culture
- make sure staff understand the available chat channels before launch

### Staff utilities

Terra also includes server utility and moderation support.

This includes:

- staff tools
- vanish
- fly speed
- maintenance support
- lag reduction controls
- rollback helpers
- world spawn and realtime clock tools

Admin guidance:

- only give `terra.admin` and `terra.staff` to trusted roles
- treat rollback commands as serious recovery tools, not everyday toys
- test maintenance mode before using it during an actual live incident

## Commands you will use the most

This is not every argument. It is the command surface most admins need to remember.

### `/terra`

Main admin root.

Important uses:

- `reload`
- `hardrestart`
- `maintenance`
- `lag`
- `setworldspawn`
- `realtimeclock`
- `blockdelay`
- `wildernessregen`
- `hungerspeed`
- `hostilemobs`
- `phantoms`
- `bats`
- `items`
- `rewards`
- `jobcap`
- `setxpboost`
- `cleardata`
- `stability`
- `fixedore`
- `fixedoretool`
- `tutorial`
- `quests`
- `catalog`

### `/jobs`

Use this for:

- checking job state
- opening the profession GUI
- switching active jobs
- admin profession edits

### `/guild`

Use this for:

- invite flow
- treasury deposits and withdrawals
- stockpile management
- claims
- guild role and permission control

### `/country`

Use this for:

- settlement setup
- homes
- territory linking
- borders
- progression and boosts
- country admin cleanup

### `/climate`

Use this for:

- climate checks
- season testing
- debug display
- crop/climate testing

### `/merchant` and `/trader`

Use these for:

- status checks
- spawn timing
- manual runtime control
- merchant management access

### `/staff`, `/vanish`, `/flyspeed`

Use these for staff operation and moderation support.

## Setting up land and settlements properly

If your server uses countries seriously, do not improvise the territory side.

Recommended process:

1. Build or choose the settlement area first.
2. Create the WorldGuard region cleanly.
3. Confirm the region name and boundaries.
4. Link the region through the country territory command flow.
5. Test entry notifications.
6. Test country borders.
7. Test country home placement.
8. If using Dynmap, confirm map markers.
9. Only then allow claims and live player use.

This order saves a lot of confusion later.

## Recommended values by server size

These are not the only workable values, but they are solid starting points if you want a balanced setup without spending days tuning from scratch.

Treat them as launch presets, not permanent law.

### Small server preset

This is a good fit for a smaller community where:

- players know each other
- early settlement progress should happen reasonably fast
- you want fewer hard progression walls
- you still want guilds and land control to matter

Recommended values:

#### `settings/guilds.yml`

```yml
guilds:
  creation:
    create-cost: 150.0

  invites:
    duration-hours: 48

  treasury:
    officer-base-withdraw-limit: 200.0
    officer-per-level-withdraw-limit: 50.0
    admiral-base-withdraw-limit: 50.0
    admiral-per-level-withdraw-limit: 15.0

  claims:
    country-claim-base-cost: 300.0
    reclaim-cooldown-days: 2
    minimum-claim-members: 2
    minimum-claim-total-levels: 18

  upkeep:
    leader-inactivity-days: 21
```

#### `settings/core.yml`

```yml
economy:
  reward-scale: 0.22
  price-scale: 0.20

hunger:
  speed-multiplier: 0.95

hostile-mobs:
  spawning-enabled: true

phantoms:
  enabled: false

wilderness-regeneration:
  enabled: true
  seconds: 900
  build-decay-seconds: 600
```

#### `settings/stability.yml`

```yml
stability:
  strictness:
    percent: 90
```

Why this works:

- guild creation is reachable early
- first claims happen without needing a large active population
- stability still matters, but is a little more forgiving
- hunger and world friction stay present without exhausting newer players

### Medium server preset

This is a better fit for a more active public server where:

- several guilds may compete at once
- claims need to feel earned
- economy abuse matters more
- staff need stronger progression gates

Recommended values:

#### `settings/guilds.yml`

```yml
guilds:
  creation:
    create-cost: 250.0

  invites:
    duration-hours: 48

  treasury:
    officer-base-withdraw-limit: 300.0
    officer-per-level-withdraw-limit: 75.0
    admiral-base-withdraw-limit: 75.0
    admiral-per-level-withdraw-limit: 25.0

  claims:
    country-claim-base-cost: 500.0
    reclaim-cooldown-days: 3
    minimum-claim-members: 3
    minimum-claim-total-levels: 30

  upkeep:
    leader-inactivity-days: 30
```

#### `settings/core.yml`

```yml
economy:
  reward-scale: 0.20
  price-scale: 0.20

hunger:
  speed-multiplier: 1.0

hostile-mobs:
  spawning-enabled: true

phantoms:
  enabled: false

wilderness-regeneration:
  enabled: true
  seconds: 600
  build-decay-seconds: 300
```

#### `settings/stability.yml`

```yml
stability:
  strictness:
    percent: 100
```

Why this works:

- guild creation still happens at a healthy pace
- claims take real coordination
- treasury misuse is harder
- land ownership feels competitive instead of automatic
- the world keeps its intended survival pressure

### When to move away from these presets

Change them if:

- claims are happening in the first day and you do not want that
- no guild can realistically reach a claim
- players are hoarding money faster than land or upkeep can drain it
- cave-ins are driving people away instead of teaching better building
- new players are getting stuck before they even understand the main loop

## Step-by-step territory claim example

This is a practical example of how to go from an empty world area to a live claimed settlement that players can actually use.

### Stage 1: choose the settlement area

Start by choosing the land before you touch commands.

Pick an area that:

- has enough flat or workable space for a settlement
- has a clear center
- has reasonable access to starter resources
- is not overlapping another planned country

At this stage, decide:

- the settlement name
- whether it is meant for public players, a starter settlement, or a guild target
- where the country home should eventually be
- where traders or merchant-related spawns should eventually sit

### Stage 2: prepare the physical space

Before creating regions, make sure the area is actually ready.

You should already know:

- the rough outer border
- the main roads or entry points
- the area players will think of as the center
- where important utility points belong

Do not create a territory first and figure out the build later. That usually leads to ugly borders and confusing home placement.

### Stage 3: create the WorldGuard region

Use WorldEdit to select the land, then create the region with WorldGuard.

The important part is not the exact WorldGuard syntax. The important part is that the region:

- has a clean name
- matches the intended playable area
- does not accidentally include a huge amount of wilderness
- does not cut buildings in half

Good region naming matters. Use consistent names that staff can understand later.

Example pattern:

- `oakvale`
- `freeport`
- `ironreach`

### Stage 4: create the country

Now create the Terra country object that will represent the settlement.

Use:

```text
/country create Oakvale
```

After that:

- confirm the country exists
- confirm the owner is correct
- decide whether it is a normal public-facing country or part of a more controlled admin flow

If you are building a staff-prepared country, make sure the ownership and name are finalized before inviting or moving real players into it.

### Stage 5: link the territory to the country

Once the WorldGuard region and country both exist, connect them.

Use the territory command flow:

```text
/country territory setregion <world> <region-id> <country>
```

Example:

```text
/country territory setregion world oakvale Oakvale
```

Then verify it:

```text
/country territory info Oakvale
```

At this point, confirm:

- the region linked successfully
- the country now recognizes its territory
- staff can inspect the territory cleanly

### Stage 6: test entry and border behavior

Before anyone lives there, walk in and out of the territory yourself.

Test:

- chat notifications
- title notifications
- border particles
- Dynmap markers if Dynmap is enabled

If something looks wrong here, fix it now. Do not wait until players start using the settlement.

### Stage 7: set the country home

Stand inside the linked territory and set the settlement home.

Use:

```text
/country sethome
```

Then test:

```text
/country home
```

This matters because country home behavior is territory-aware. If the home is not valid, players will feel the setup is broken immediately.

### Stage 8: prepare trader and merchant points if needed

If the country is supposed to participate in trader or merchant loops, set those points while you are still in setup mode.

Depending on the intended design, use the available country management and merchant admin flows to place:

- trader spawn points
- merchant access points
- admin-managed settlement utility positions

Do this before the settlement becomes active so the economy layer feels intentional from day one.

### Stage 9: create or prepare the guild that will claim it

If the settlement is meant to be claimed through the live guild system, now prepare the guild side.

That means making sure the future claimant guild:

- exists
- has enough members
- has enough total job levels
- has enough treasury value
- meets your claim rules

If you are testing as staff, you can simulate this by creating a test guild and funding it properly instead of bypassing everything blindly.

### Stage 10: claim the country through the guild flow

Once the guild is ready, claim the country.

Use:

```text
/guild claim Oakvale
```

After the claim:

- check guild info
- check country info
- confirm the settlement is now tied to the intended guild
- confirm upkeep expectations are understood

### Stage 11: live player validation

Before calling the settlement finished, test it from a real player perspective.

A normal player should be able to:

- enter the settlement and get the right notifications
- use `/country info`
- use `/country home` if appropriate
- understand who owns the place
- interact with the guild/country structure without confusing messages

If any of that feels messy, the settlement is not really finished yet.

### Stage 12: monitor the first week

The first week after a new settlement goes live is where most setup mistakes show up.

Watch for:

- home complaints
- border confusion
- guild claim confusion
- upkeep misunderstandings
- trader/merchant placement problems
- climate or stability interactions that make the settlement less usable than intended

The right fix is usually not "add more commands." It is usually better setup, clearer messaging, or better placement.

## Recommended pre-launch test pass

Before opening the server, test these with a fresh account and an admin account.

### Fresh player test

- spawn flow
- onboarding
- profession trial progress
- first guild interaction
- early merchant use
- climate feedback on crops
- any starter crafting path you expect players to use

### Admin test

- `/terra reload`
- guild creation
- guild claim attempt
- country territory link
- merchant and trader runtime
- stability behavior in a test mine
- maintenance mode
- rollback commands if CoreProtect is installed

## Common mistakes

- leaving onboarding enabled but not actually finishing the onboarding area
- enabling territory play before regions are ready
- setting guild claim costs without testing the real earning pace
- turning climate or stability on without warning players that the server is no longer vanilla-like
- using merchants to bypass progression instead of support it
- forgetting to back up Terra data before admin cleanup or reset work

## Final advice

Terra works best when the server owner treats it as a connected survival framework, not as a pile of separate features.

If you configure guilds without thinking about countries, or climate without thinking about farming, or merchants without thinking about progression, the server will feel messy even if each individual file looks correct.

Set it up in layers, test it in the same order players will meet it, and keep the first hour of gameplay extremely deliberate.
