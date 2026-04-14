# Resource Pack Delivery

This project no longer uses Terra Core's standalone `resource-pack` config section.

Use ItemsAdder for pack generation and delivery instead:

1. Copy `examples/itemsadder/contents/terra_quest_hud` into `plugins/ItemsAdder/contents/terra_quest_hud`
2. Run `/iazip`
3. Let ItemsAdder host and send the generated pack

The zip files in the repository root are optional build artifacts for manual testing or external hosting, but Terra Core itself no longer reads a `resource-pack.url` or `resource-pack.sha1` from `core.yml`.
