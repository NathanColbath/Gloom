# Scenes and serialization

Scenes are **JSON** files under `Scenes/`, typically `*.scene.json`. The project manifest (`*.llwproj`) stores which scene loads at startup.

## Scene file structure

```json
{
  "version": 2,
  "name": "Main",
  "objects": [
    {
      "id": 1,
      "name": "Player",
      "active": true,
      "transform": { "x": 0, "y": 0, "rotation": 0, "scaleX": 1, "scaleY": 1 },
      "components": { ... }
    }
  ]
}
```

- **`version`** — serializer format; Studio migrates older versions on load.
- **`id`** — stable integer for references and script entity links.
- **`components`** — map of component type name to serialized fields.

`SceneSerializer` and `SceneObjectSerializer` handle round-trip; component types register with `ComponentSerializerRegistry`.

## Loading and saving

- **Double-click** scene asset in Project to load.
- **File → Save Scene** — writes active scene path.
- **File → Save Project** — updates `*.llwproj` (startup scene path, project name).

## Script field serialization

Public script fields on attached Script components are stored per entity. Private/runtime fields are not saved. See [Scripting](scripting.md).

## Prefabs in scenes

Prefab instances store a link to the prefab asset GUID plus optional overrides. See [Prefabs](prefabs.md).

## Tilemaps and UI

`TilemapComponent` and UI components serialize GUIDs, layer data, and layout fields into the same `components` object. Tests such as `TilemapSerializationTest` and `UiSerializationTest` define expected JSON shapes.

## Related

- [Project format](project-format.md)
- [ECS and GameObjects](ecs-and-gameobjects.md)
