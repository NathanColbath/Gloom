# Scene view

The Scene view shows the **edit scene** rendered into an llw `OffscreenTarget`, displayed inside ImGui with `ImGui.image`. Use it to navigate the level, select objects, transform them with gizmos, and paint tiles.

## Where to find it

Center dock, tabbed with **Game** (default). Window title **Scene**.

## Editor camera

| Input | Action |
|-------|--------|
| Middle-mouse drag | Pan |
| Scroll wheel | Zoom |
| **View → Frame Scene** | Fit all object bounds in view |

## Tool modes (toolbar)

| Tool | Behavior |
|------|----------|
| **Hand** | Pan; click to pick objects (no gizmo drag) |
| **Move** | Translate gizmo on X, Y, and center |
| **Rotate** | Rotation ring around selection bounds center |
| **Scale** | Axis and uniform corner handles |
| **Paint** | Paint tiles on selected tilemap (requires [Tile palette](tile-palette.md)) |
| **Erase** | Erase tiles from tilemap layers |

Gizmos are **hidden during Play mode**. Transform drags record one undo step via `TransformEditCommand`.

::: studio-screenshot{file="09-scene-view.png"}
Scene view with grid, sprite, and selection outline.
:::

::: studio-screenshot{file="10-scene-view-tools.png"}
Move tool active with translate gizmo visible.
:::

::: studio-screenshot{file="27-gizmo-translate.png"}
Translate gizmo mid-drag on a selected object.
:::

## Render layers (editor)

Draw order inside the Scene view FBO:

1. **Grid** (behind everything)
2. **Scene** — sprites and tilemaps by sorting order
3. **Selection outline** — blue AABB around selection
4. **Gizmo** — active tool handles

## Picking

Left-click in the viewport runs AABB picking (`ScenePicker`):

- Tests entities with sprite or tilemap bounds
- Smallest containing bounds wins
- Scene Root is ignored

**Pivot** — gizmos and `Transform2D.position` use the **center** of sprite bounds. Sprites render with texture origin at center.

## Tile painting

1. Select an object with a **Tilemap** component.
2. Open **Tile Palette** and pick a tileset / tile.
3. Activate **Paint** or **Erase** on the toolbar.
4. Click or drag in the Scene view over the tilemap grid.

See [Tilemaps](tilemaps.md).

## Related

- [Game view](game-view.md)
- [llw engine integration](llw-engine-integration.md)
- [Undo and commands](undo-and-commands.md)

::: studio-screenshot{file="26-llw-render-pipeline.png"}
Diagram: edit scene to OffscreenTarget to ImGui texture in Scene view.
:::
