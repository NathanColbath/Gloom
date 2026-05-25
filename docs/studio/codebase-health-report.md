# llw-studio codebase health report

Last updated: 2026-05-25

## Summary

| Severity | Open | Resolved |
|----------|------|----------|
| High | 0 | 3 |
| Medium | 2 | 4 |
| Low | 3 | 1 |

**Quick wins (S effort):** R03, D01, D02, P02 — addressed in maintenance pass.

## Health and tech debt

See [Editor architecture](editor-architecture.md) for layer model and pass order. This report tracks redundancy and standards gaps in `llw-studio` only.

---

## 1. Redundancy

| ID | Sev | Effort | Finding | Status |
|----|-----|--------|---------|--------|
| R01 | High | M | Play-scene draw duplicated in `GameViewPanel` and `PlayerGameLoop` | **Resolved** — `PlaySceneRenderPasses` in llw-runtime |
| R02 | High | M | `TransformSystem.onUpdate` from 9+ studio call sites per frame/interaction | **Resolved** — `EditorWorldTransforms.ensureUpdated` + pipeline calls once per viewport frame |
| R03 | Medium | S | Unused `UiDrawPass` import in `SceneViewPanel` | **Resolved** — removed during pipeline extraction |
| R04 | Medium | L | ~21 similar `ComponentDrawer` types under `editor.inspector.builtin` | **Open** — acceptable; document pattern only |
| R05 | Medium | M | Multiple offscreen preview paths (scene/game, particles, shader graph, animation) | **Open** — backlog: shared viewport helper |

---

## 2. Standards and layering violations

| ID | Sev | Effort | Finding | Status |
|----|-----|--------|---------|--------|
| S01 | High | M | `org.llw.studio.render` split across llw-studio and llw-runtime | **Resolved** — studio passes → `editor.render.passes` |
| S02 | Medium | S | `editor.gizmo` vs `editor.gizmos` naming | **Resolved** — package-info clarifies roles |
| S03 | Medium | S | `ScriptFileWatcher` under `scripting.js` | **Resolved** — `editor.scripting` |
| S04 | Medium | M | `StudioEditorRuntime` ~440 lines, central wiring | **Open** — backlog: composition root split |
| S05 | Low | S | Loose types in `editor` package root | **Open** — backlog: optional `editor.core` |
| S06 | Low | S | Runtime render tests in llw-studio module | **Open** — accepted convention or move to runtime later |

---

## 3. Documentation drift

| ID | Sev | Effort | Finding | Status |
|----|-----|--------|---------|--------|
| D01 | Medium | S | `llw-engine-integration.md` §3 render order stale | **Resolved** — see `editor-architecture.md` |
| D02 | Low | S | Runtime `render/package-info` mentioned editor gizmos | **Resolved** — runtime package-info updated |

---

## 4. Performance smells

| ID | Sev | Effort | Finding | Status |
|----|-----|--------|---------|--------|
| P01 | High | S | Per-pass `TransformSystem` in draw hot path | **Resolved** — centralized world transforms |
| P02 | Medium | S | `PhysicsGizmoDrawPass` used `CAMERA_GIZMO` layer | **Resolved** — uses `COMPONENT_GIZMO` |
| P03 | Low | S | FBO recreate on resize | **Resolved** — panels guard `w != lastWidth`; audited in architecture doc |

---

## 5. Low-hanging fixes (completed)

1. R03 — dead import removed  
2. D01 — architecture doc is source of truth for pass order  
3. P02 — physics gizmo layer constant  
4. D02 — runtime package-info  

---

## 6. Backlog (out of studio-only scope)

- **R05** — `EditorViewportTarget` helper for FBO + ImGui.image  
- **S04** — Extract panel factory from `StudioEditorRuntime`  
- **S05** — Reorganize `editor` root types into subpackages  
- **S06** — Move `SpriteResolveTest` / `SpritePlacementTest` to llw-runtime  
- Runtime: dedupe `LitSceneDrawPass` / `SceneDrawPass` ECS walks  
- Clone this report for `llw-runtime` and `llw-player`  

---

## Appendix: FBO resize audit

| Panel / service | Guards size change |
|-----------------|-------------------|
| `SceneViewPanel` | Yes |
| `GameViewPanel` | Yes |
| `UiEditorPanel` | Yes (verify in code) |
| `ShaderGraphPreviewService` | Yes |
| `ParticlePreviewService` | Yes |
| `AnimationPreviewViewport` | Yes |
