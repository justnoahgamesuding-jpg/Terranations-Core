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
  bar-progress: 0.0
  panel-token: ":top_status_panel:"
  content-offset-token: ":offset_-248:"
  location-panel-token: ":top_status_location_panel:"
  job-panel-token: ":top_status_job_panel:"
  money-panel-token: ":top_status_money_panel:"
  location-offset-token: ":offset_-112:"
  job-offset-token: ":offset_-118:"
  money-offset-token: ":offset_-78:"
  format: "%location_panel%%location_offset%&f⌖ %location%:offset_14:%job_panel%%job_offset%&a⚒ %job% &7Lv.%level%:offset_14:%money_panel%%money_offset%&6☀ &f%money%"
  wilderness-label: "Wilderness"
  no-job-label: "No Job"
  max-location-chars: 12
  max-job-chars: 10
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
