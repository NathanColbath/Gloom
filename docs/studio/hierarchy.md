# Hierarchy panel

The Hierarchy lists every object in the active scene as an expandable tree. It is the primary way to select entities, toggle active state, reparent nodes, and open context actions.

## Where to find it

Docked in the **left column** by default (window title **Hierarchy**).

## Features

- **Search** — filters rows by object name
- **Active checkbox** — maps to `Active` component; inactive objects skip most play-mode logic
- **Selection** — click to select; **Ctrl+click** for multi-select
- **Expand/collapse** — tree rooted at scene objects (Scene Root is hidden from picking)
- **Drag-reparent** — drag a row onto another to change parent; uses internal `ENTITY_ID` payload
- **Context menu** — Create Child, Duplicate, Delete

::: studio-screenshot{file="04-hierarchy.png"}
Nested objects such as Player with children expanded.
:::

## Reparenting

1. Click and drag an object row.
2. Drop on the intended parent (highlighted row).
3. Undo via **Edit → Undo** if needed.

::: studio-screenshot{file="28-hierarchy-reparent.png"}
Drag operation moving a child under a new parent.
:::

## Prefab instances

Dragging a **prefab** asset from the Project panel into the Hierarchy creates an instance linked to that prefab asset. Editing instance overrides is done in the Inspector; see [Prefabs](prefabs.md).

## Tips

- Deleting the last selected object clears Inspector focus.
- Scene Root cannot be selected in Scene view picking; use Hierarchy for root-level operations.

## Related

- [Inspector](inspector.md)
- [Scenes and serialization](scenes-and-serialization.md)
