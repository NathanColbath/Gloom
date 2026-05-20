# Tilesets and rule tiles

A **tileset** ties a texture to tile definitions (sprite rects, collision flags). **Rule tiles** automatically pick the correct sprite from neighbor patterns when painting.

## Tileset inspector

1. Select or create tileset metadata linked to a texture (via tileset importer / `TilesetData`).
2. In **Inspector**, define tile cells, sprites, and collider shapes per tile.

::: studio-screenshot{file="40-tileset-inspector.png"}
Tileset inspector with grid of tile definitions.
:::

## Rule tile editor

1. Open **Rule Tile Editor** from the tileset Inspector (modal).
2. Define neighbor masks (N, E, S, W, corners) → output sprite.
3. Save; painting uses `RuleTileResolver` to pick sprites.

::: studio-screenshot{file="41-rule-tile-modal.png"}
Rule tile editor modal with neighbor pattern grid.
:::

## Painting with rules

When a rule tile is active in the [Tile palette](tile-palette.md), painting on a tilemap runs the resolver for affected cells and neighbors (`refresh` with radius).

## Tips

- Test rule priority when multiple rules match; ordering matters in the rule list.
- Use simple tiles for prototyping before investing in full autotile sets.

## Related

- [Tilemaps](tilemaps.md)
- [Tile palette](tile-palette.md)
