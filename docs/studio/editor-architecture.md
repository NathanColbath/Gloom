# LLW Studio editor architecture

Module: `llw-studio` → depends on `llw-runtime` → `llw`.

## Layer model

| Layer | Package | Responsibility |
|-------|---------|----------------|
| Bootstrap | `org.llw.studio` | `StudioLauncher`, `StudioEditorRuntime`, GLFW loop |
| Shell | `editor.shell`, `editor.panels` | ImGui dock, `EditorPanel` implementations |
| Viewport orchestration | `editor.render` | Pipelines, `EditorRenderLayers`, `EditorWorldTransforms` |
| Editor draw passes | `editor.render.passes` | Grid, gizmos, selection (studio-only) |
| Editor UX | `editor.gizmo`, `editor.gizmos`, `editor.inspector`, … | Tools, drawers, widgets |
| Build / settings | `build`, `settings` | Player build, persisted prefs |
| Shippable render | `org.llw.studio.render` (runtime) | Sprites, tilemaps, UI, `PlaySceneRenderPasses` |

**Rule:** `editor.render.passes` may use `editor.*` and runtime `render`; runtime must not depend on studio.

## Scene view pass order (edit mode)

Source: `EditorSceneViewportPipeline`

1. `GridDrawPass`
2. `TilemapGridDrawPass` (if tile paint active)
3. `TilemapDrawPass`
4. `SceneRenderPasses.drawSprites` (lit or unlit)
5. `ParticleDrawPass`
6. `CameraGizmoDrawPass`, `PhysicsGizmoDrawPass`
7. `ComponentGizmoDrawPass` (if visible)
8. `ScriptGizmoDrawPass` (if visible)
9. `SelectionOutlinePass`
10. `GizmoDrawPass` (transform tool)

Play-in-scene: steps 1–5, then selection outline only; “Playing” label drawn by `SceneViewPanel`.

## Game view / player pass order

Source: `PlaySceneRenderPasses` (runtime), called from `PlaySceneViewportPipeline` (studio) and `PlayerGameLoop`.

1. `TilemapDrawPass`
2. `SceneRenderPasses.drawSprites`
3. `ParticleDrawPass` (if any)
4. `flush`
5. `UiDrawPass`
6. `flush`

## Draw layers

| Constant | Module | Value | Use |
|----------|--------|-------|-----|
| `EditorRenderLayers.GRID` | studio | -10000 | Editor grid |
| `RenderLayers.SCENE_BASE` | runtime | 0 | Sprites + sorting order |
| `EditorRenderLayers.CAMERA_GIZMO` | studio | 5000 | Camera frustum |
| `EditorRenderLayers.COMPONENT_GIZMO` | studio | 6000 | Component + physics overlays |
| `EditorRenderLayers.SCRIPT_GIZMO` | studio | 7000 | Script `onDrawGizmos` |
| `EditorRenderLayers.SELECTION` | studio | 10000 | Selection AABB |
| `EditorRenderLayers.GIZMO` | studio | 20000 | Move/rotate/scale handles |
| `UiDrawPass.UI_LAYER_BASE` | runtime | 100000+ | Screen-space UI |

## World transforms in the editor

`EditorWorldTransforms.ensureUpdated(Scene)` runs once per viewport frame (and on pick/gizmo input) instead of per draw pass.

## Separate preview pipelines

- **Shader graph:** `ShaderGraphPreviewService` — own FBO, not wired through scene pipelines
- **Particles:** `ParticlePreviewService`
- **Animation:** `AnimationPreviewViewport`

Backlog: shared viewport resize helper (see [codebase health report](codebase-health-report.md)).

## Health and tech debt

See [codebase-health-report.md](codebase-health-report.md).

## Code documentation

Comment tiers and package checklist: [code-documentation.md](code-documentation.md).

Inline (in-method) comment pass tracker: [inline-comment-pass.md](inline-comment-pass.md).
