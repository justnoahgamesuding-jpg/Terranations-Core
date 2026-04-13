# GitHub Resource Pack Hosting

Terra can send a resource-pack URL to players on join using `plugins/testproject/settings/core.yml`.

## Recommended GitHub URL

If the pack zip is committed at the repo root, use the raw GitHub URL:

```yml
resource-pack:
  enabled: true
  url: "https://raw.githubusercontent.com/justnoahgamesuding-jpg/Terranations-Core/main/terranations_hud_pack.zip"
  sha1: "f968213d85d242aeb67ad723cedada0e83dfe369"
  delay-ticks: 40
  skip-when-betterhud-present: true
```

Update `sha1` whenever you rebuild the zip.

## Important

- The URL must be direct HTTPS to a `.zip`.
- The zip must contain `pack.mcmeta` and `assets/` at the top level.
- After changing the file in GitHub, Minecraft may cache the old pack if the URL stays the same. Updating `sha1` or changing the file name is the cleanest cache-bust.
- Restart the server after editing `core.yml`.
- Minecraft only keeps one server resource pack active. If BetterHud is installed, keep `skip-when-betterhud-present: true` and let BetterHud host the HUD pack so Terra does not override BetterHud's generated HUD assets.

## Current Pack

The ready-to-upload pack is:

- `terranations_hud_pack.zip`

If you commit and push it to GitHub, the raw URL above should work.
