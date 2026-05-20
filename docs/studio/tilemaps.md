# Tilemaps

Tilemaps paint grid cells from a **tileset** texture onto layers. They are ideal for level geometry, backgrounds, and collision grids.

## Setup

1. Prepare a tileset texture (sliced or grid-aligned tiles).
2. Create a GameObject with **Tilemap** component.
3. Assign **tileset texture** GUID in Inspector.
4. Open [Tile palette](tile-palette.md) and select tiles.
5. Use **Paint** / **Erase** tools in [Scene view](scene-view.md).

::: studio-screenshot{file="38-tilemap-inspector.png"}
Tilemap component with tileset reference and layer settings.
:::

::: studio-screenshot{file="39-tile-paint-scene.png"}
Scene view with Paint tool active on a tilemap grid.
:::

## Layers

Tilemaps support multiple **layers** (stacked grids). Paint on the active layer from the palette or Inspector.

## Rule tiles (optional)

For autotiling corners and edges, configure [rule tiles](tilesets-and-rule-tiles.md) on the tileset asset.

## Scripts

```typescript
const map = entity.getComponent("Tilemap2D");
map?.setTile(layer, x, y, spriteGuid);
map?.refresh(layer, x, y, radius);
```

## Serialization

Tile data is stored in scene JSON per `TilemapComponent`. See `TilemapSerializationTest` for format expectations.

## Related

- [Tilesets and rule tiles](tilesets-and-rule-tiles.md)
- [Tile palette](tile-palette.md)
