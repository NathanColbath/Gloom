# Inspector panel

The Inspector edits the **selected GameObject** or, when no entity is selected, the **selected Project asset** (texture import, prefab root, tileset, etc.).

## Where to find it

Docked on the **right** by default (window title **Inspector**).

## GameObject inspector

1. **Header** — name, tag, active toggle
2. **Transform 2D** — position, rotation in **degrees**, scale (always present)
3. **Component foldouts** — one section per attached component
4. **Add Component** — searchable popup of addable types

Reference fields (sprites, clips, entities) accept **drag-and-drop** from the Project panel or Hierarchy.

::: studio-screenshot{file="05-inspector-transform.png"}
Transform foldout with position, rotation, and scale fields.
:::

::: studio-screenshot{file="06-inspector-sprite.png"}
Sprite Renderer with sprite GUID slot and tint.
:::

::: studio-screenshot{file="07-inspector-script.png"}
Script component with TypeScript class and serialized fields.
:::

::: studio-screenshot{file="08-inspector-add-component.png"}
Add Component popup filtered by name.
:::

::: studio-screenshot{file="15-drag-drop-asset.png"}
Dragging a sprite asset onto a reference field.
:::

## Asset inspector

Select an asset in **Project** (no scene selection) to see:

- Path, GUID, asset type
- Texture preview and **Reimport**
- **Slice Editor…** for spritesheets
- Tileset and rule-tile editors for tile assets

See [Project and assets](project-and-assets.md).

## Component catalog

Full list with field summaries: [Components reference](components-reference.md).

| Category | Components |
|----------|------------|
| Core | Transform 2D, Active |
| Rendering | Sprite Renderer, Tilemap, Camera 2D |
| Animation | Animation 2D |
| Audio | Audio Source |
| Scripting | Script |
| Physics | Rigidbody 2D, Box / Circle / Edge Collider 2D |
| UI | UI Canvas, UI Label, UI Button, UI Toggle, UI Text Field |

## Tips

- Rotation in the Inspector is **degrees**; scripts use degrees on `transform.rotation` as well.
- Multiple **Script** attachments are supported; the first is exposed to `entity.getComponent(ScriptClass)` in TypeScript.
- Use **private** fields in scripts for runtime-only state hidden from the Inspector ([Scripting](scripting.md)).

## Related

- [UI theme and widgets](ui-theme-and-widgets.md)
- [In-game UI](in-game-ui.md)
