# llw-studio inline comment pass (Tier C+)

Last updated: 2026-05-25 — **pass complete** for P0–P3 scope in `llw-studio/src/main/java`.

Track **in-method** `//` comments only. Class Javadoc: [code-documentation.md](code-documentation.md). Rules: [Tier C+](code-documentation.md#tier-c--inline-comments).

## Progress

| Batch | Status |
|-------|--------|
| I0 P0 | Done (12 files) |
| I5 Render | Done |
| I6 Gizmos | Done |
| I3+I4 Core/Shell/Panels/ImGui | Done (~22 files) |
| I2+I7+I9 Build/Inspector/Assets/… | Done (~30 files) |
| I8+I10+I1 Widgets/ShaderGraph/Settings | Done (9 files + skips) |

## I0 — P0 critical path

| File | Inline |
|------|--------|
| `editor/panels/SceneViewPanel.java` | [x] |
| `editor/panels/GameViewPanel.java` | [x] |
| `editor/render/EditorSceneViewportPipeline.java` | [x] |
| `editor/render/EditorWorldTransforms.java` | [x] |
| `editor/SceneViewInput.java` | [x] |
| `editor/shell/EditorShell.java` | [x] |
| `StudioEditorRuntime.java` | [x] |
| `StudioLauncher.java` | [x] |
| `editor/EditorCamera.java` | [x] |
| `editor/ScenePicker.java` | [x] |
| `build/ProjectBuildService.java` | [x] |
| `editor/scripting/ScriptFileWatcher.java` | [x] |

## I5 — Render passes

| File | Inline |
|------|--------|
| `editor/render/PlaySceneViewportPipeline.java` | [x] |
| `editor/render/passes/GridDrawPass.java` | [x] |
| `editor/render/passes/TilemapGridDrawPass.java` | [x] |
| `editor/render/passes/CameraGizmoDrawPass.java` | [x] (class doc) |
| `editor/render/passes/PhysicsGizmoDrawPass.java` | [x] |
| `editor/render/passes/SelectionOutlinePass.java` | [x] |
| `editor/render/passes/GizmoDrawPass.java` | [x] (thin delegate) |
| `editor/render/passes/ComponentGizmoDrawPass.java` | [x] |
| `editor/render/passes/ScriptGizmoDrawPass.java` | [x] |
| `editor/render/passes/GizmoDrawHelper.java` | [x] (helpers) |

## I6 — Gizmos

| File | Inline |
|------|--------|
| `editor/gizmo/GizmoController.java` | [x] |
| `editor/gizmo/GizmoHandleController.java` | [x] |
| `editor/gizmo/TransformMath.java` | [x] |
| `editor/gizmos/builtin/Light2DGizmo.java` | [x] |
| `editor/gizmos/builtin/ParticleEmitterGizmo.java` | [x] (subagent) |
| `editor/gizmos/builtin/SceneLightingGizmo.java` | [x] |

## P3 — Skipped (trivial)

- `editor/widgets/fields/*` — linear ImGui property rows
- Most `editor/inspector/builtin/*Drawer` except Script, Tilemap, Material, SceneLighting
- `StudioSettings`, `ConsoleLogSink`, simple toolbar/header widgets

See subagent logs in conversation for per-file notes on I3–I10 batches.
