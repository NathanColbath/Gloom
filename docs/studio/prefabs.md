# Prefabs

Prefabs are reusable entity hierarchies saved as `*.prefab.json` under `Assets/`. Edit the prefab asset once, then place instances in scenes.

## Create a prefab

1. Build an object hierarchy in a scene (or empty prefab asset).
2. Use prefab authoring in the Inspector when a prefab asset is selected (`PrefabAssetEditor`).
3. Save the prefab JSON under `Assets/`.

::: studio-screenshot{file="33-prefab-inspector.png"}
Prefab asset open in Inspector with root object hierarchy.
:::

## Place in a scene

1. Drag the prefab from **Project** into **Hierarchy** or the **Scene** view.
2. Studio creates an instance linked to the prefab GUID.

::: studio-screenshot{file="34-prefab-drag-hierarchy.png"}
Dragging prefab asset into Hierarchy.
:::

## From scripts

```typescript
import * as core from "llw.core";

// By prefab asset path or GUID string
const bullet = core.Scene.createEntity("Bullet", "Assets/Prefabs/Bullet.prefab.json");
```

Also clone an existing entity:

```typescript
const copy = core.Scene.createEntity(templateEntity);
```

See [Scripting API — Scene](scripting-api/scene-and-entities.md).

## Runtime instantiation

`PrefabInstantiator` deserializes prefab JSON into the active play or edit world. Instance overrides (if any) layer on top of the base prefab data.

## Tips

- Keep prefab roots at sensible pivots; instances inherit transform on spawn.
- Script references on prefabs should use inspector fields or `findByName` after spawn.

## Related

- [Scripting cookbook](scripting-cookbook.md) — `Bullet.ts` sample
- [Scenes and serialization](scenes-and-serialization.md)
