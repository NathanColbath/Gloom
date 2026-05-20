# ECS and GameObjects

LLW Studio uses a **hybrid** model: a Unity-like `GameObject` hierarchy for editing, backed by an ECS `World` of entities, components, and systems at runtime.

## Concepts

| Term | Meaning |
|------|---------|
| **World** | Stores entities, component stores, system scheduler |
| **Entity** | Integer ID + component bundles |
| **GameObject** | Editor/scene façade: name, parent, children, convenience accessors |
| **Component** | Data attached to entities (Transform, SpriteRenderer, …) |
| **System** | Logic that reads/writes components each frame |

## Hierarchy

Parent/child relationships use `HierarchyComponent`. **Active** state propagates: inactive parents disable children in play mode (`ActiveUtility`).

## Stable scene object IDs

Each serialized object has an integer **`id`** in scene JSON. Scripts and references use this for entity links across save/load and play-mode cloning (`SceneObjectIdComponent`).

## Coordinates

World space is **Y-down** (screen-style), matching [llw render coordinates](/guide/coordinates). `Transform2D.position` is the world-space center for sprites.

::: studio-screenshot{file="23-ecs-diagram.png"}
Diagram or debug view: World, component stores, and systems.
:::

## Edit vs play worlds

- **Edit scene** — live in the editor; serialized to disk.
- **Play scene** — clone at Play; mapped back to edit IDs for some bridges.

## Related

- [Scenes and serialization](scenes-and-serialization.md)
- [Systems reference](systems-reference.md)
- [Components reference](components-reference.md)
