# 2D Lighting

## Components

- **Light 2D** — point, spot, or directional realtime lights
  - **Intensity** — typical range `0.5`–`3` for sprites without normal maps
  - **Range** — world units; falloff is smooth out to the gizmo radius (about 50% brightness at the edge)
  - **Inner/Outer Angle** (spot) — full cone width in degrees; matches the scene gizmo
- **Scene Lighting** — ambient color and baked lightmap settings
- **Static Lightmap Contributor** — include static sprites in lightmap bakes

## Workflow

1. Add **Scene Lighting** to a manager object and tune ambient.
2. Place **Light 2D** entities in the scene.
3. Use **Bake Lighting** on the Scene Lighting component to generate `SceneLighting.lightmap.png`.
4. Assign materials with `BUILTIN_LIT` or a lit shader graph on sprites.

Normal maps can be linked per texture in import settings or on the material asset.
