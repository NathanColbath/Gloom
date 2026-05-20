# Tile palette

The Tile Palette panel lists tiles from the active **tileset** so you can pick what to paint in the Scene view.

## Where to find it

Window title **Tile Palette**. It is **not** part of the default dock layout — open it from the panel menu and dock it near Project or Scene.

::: studio-screenshot{file="42-tile-palette-panel.png"}
Tile Palette panel with tileset dropdown and tile grid.
:::

## How to use

1. Select a GameObject with a **Tilemap** component in the Hierarchy.
2. Open **Tile Palette** and choose the tileset matching the tilemap’s texture.
3. Click a tile thumbnail to set the active brush.
4. Activate **Paint** or **Erase** on the toolbar.
5. Click or drag in the [Scene view](scene-view.md).

## Tips

- If painting does nothing, confirm the tilemap’s tileset GUID matches the palette selection.
- Rule tiles show as special entries when configured on the tileset.

## Related

- [Tilemaps](tilemaps.md)
- [Scene view](scene-view.md)
