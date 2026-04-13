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
  format: "%panel%%offset%&7LOCATION &f%location% &8| &7JOB &f%job% &7Lv.%level% &8| &7MONEY &f$%money%"
  wilderness-label: "Wilderness"
  no-job-label: "No Job"
  max-location-chars: 18
  max-job-chars: 14
```

If the text is not lined up on the black panel, adjust `content-offset-token`.
Use a more negative value to move the text left, for example `:offset_-260:`.
Use a less negative value to move the text right, for example `:offset_-236:`.

The panel image is:

```text
examples/itemsadder/contents/terra_quest_hud/textures/font/hud/top_status/panel.png
```
