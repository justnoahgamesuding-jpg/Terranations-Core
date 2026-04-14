# ItemsAdder Top Status HUD

This HUD is the top-center status strip for location, current job level, and money.

## Files to copy

Copy this folder into your ItemsAdder plugin folder:

```text
examples/itemsadder/contents/terra_quest_hud
```

Target location:

```text
plugins/ItemsAdder/contents/terra_quest_hud
```

Then run:

```text
/iazip
```

Reload the ItemsAdder pack on the client after the new pack is generated.

## Terra config

The runtime settings are in:

```text
plugins/testproject/settings/core.yml
```

Default section:

```yml
itemsadder-top-status:
  enabled: true
  require-itemsadder: true
  update-ticks: 20
  panel-token: ":top_status_panel:"
  content-offset-token: ":offset_-248:"
  location-panel-token: ":top_status_location_panel:"
  job-panel-token: ":top_status_job_panel:"
  money-panel-token: ":top_status_money_panel:"
  location-offset-token: ":offset_-112:"
  job-offset-token: ":offset_-118:"
  money-offset-token: ":offset_-78:"
  panel-width-pixels: 64
  location-panel-width-pixels: 96
  job-panel-width-pixels: 96
  money-panel-width-pixels: 96
  panel-gap-pixels: 8
  location-job-gap-pixels: 8
  job-money-gap-pixels: 18
  job-text-inset-pixels: 6
  format: "auto"
  wilderness-label: "Wilderness"
  no-job-label: "No Job"
  max-location-chars: 7
  max-job-chars: 6
```

Auto mode uses these symbols:

```text
➣ location
⚒ job
⛁ money
```

If the text is not lined up on a black panel, adjust that panel's offset token.
Use a more negative value to move the text left, for example `:offset_-108:`.
Use a less negative value to move the text right, for example `:offset_-92:`.

The panel images are:

```text
examples/itemsadder/contents/terra_quest_hud/textures/font/hud/top_status/location_panel.png
examples/itemsadder/contents/terra_quest_hud/textures/font/hud/top_status/job_panel.png
examples/itemsadder/contents/terra_quest_hud/textures/font/hud/top_status/money_panel.png
```

The HUD uses the vanilla bossbar text position for top-screen placement. The ItemsAdder pack includes transparent white bossbar sprites so the vanilla white bossbar texture does not show behind the panels:

```text
examples/itemsadder/contents/terra_quest_hud/resourcepack/assets/minecraft/textures/gui/sprites/boss_bar/white_background.png
examples/itemsadder/contents/terra_quest_hud/resourcepack/assets/minecraft/textures/gui/sprites/boss_bar/white_progress.png
```
