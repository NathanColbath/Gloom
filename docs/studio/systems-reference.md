# ECS systems reference

Play mode registers systems on `SystemGroup.LOGIC`. Editor rendering uses separate draw passes (grid, sprites, gizmos) outside this list.

## Play mode logic systems

Order registered in `PlayModeRunner.activate` (then updated each frame in `scheduler.update`):

| Order | System | Responsibility |
|-------|--------|----------------|
| 1 | `PlayInputSystem` | GLFW → script `Input` when Game view focused |
| 2 | `UiInputSystem` | Hit-test UI widgets, focus text fields |
| 3 | `JsScriptSystem` | GraalJS `start` / `update` / `fixedUpdate`, collision/trigger events |
| 4 | `AnimationSystem` | Sample animation clips, update sprite frames |
| 5 | `TransformSystem` | Propagate local transforms to world space |
| 6 | `PhysicsSystem` | Box2D step, sync rigidbody transforms |
| 7 | `AudioSystem` | Play/stop `AudioSource` components |

Prepare phase (before activate) warms script factories on a worker thread; physics world builds on the main thread at activate.

## Script system

`JsScriptSystem` replaces the legacy Java `ScriptSystem`. It:

- Instantiates one script object per Script attachment
- Dispatches physics callbacks from `PhysicsContactBridge`
- Destroys instances on Stop

## Editor-only systems

Scene view rendering enqueues draw items with `EditorRenderLayers` (grid, scene sprites/tilemaps, selection, gizmos). These are not ECS systems in the play scheduler.

## Related

- [Play mode](play-mode.md)
- [Physics 2D](physics-2d.md)
- [Animation](animation.md)
- [In-game UI](in-game-ui.md)
