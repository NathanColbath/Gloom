# LLW Studio code documentation standards

## Tiers

| Tier | Applies to | Required |
|------|------------|----------|
| **A** | Public types; public methods on services, panels, pipelines | Class Javadoc + `@param`/`@return` when non-obvious |
| **B** | Every package | `package-info.java` (2–5 sentences, `@see`) |
| **C** | Complex logic (>15 lines, play/edit branches, coordinate math) | Inline `//` explaining **why** |
| **C+** | Same triggers as C; in-method only (see below) | Inline comments per [inline-comment-pass.md](inline-comment-pass.md) |
| **D** | Thin widgets | One-sentence class Javadoc |
| **E** | Tests | Class Javadoc describing behavior under test |

## Anti-patterns

- Restating method names in Javadoc
- Commenting every theme color constant
- `@author` / `@since` tags

## Package checklist (main sources)

| Batch | Package | Done |
|-------|---------|------|
| C1 | `org.llw.studio`, `log`, `settings` | [x] |
| C2 | `build` | [x] |
| C3 | `editor` (root) | [x] |
| C4 | `editor.shell`, `editor.panels`, `editor.imgui` | [x] |
| C5 | `editor.render`, `editor.render.passes` | [x] |
| C6 | `editor.gizmo`, `editor.gizmos`, `editor.gizmos.builtin` | [x] |
| C7 | `editor.commands`, `editor.components`, `editor.inspector`, `editor.inspector.builtin` | [x] |
| C8 | `editor.widgets`, `editor.widgets.fields` | [x] |
| C9 | `editor.assets`, `editor.launcher`, `editor.prefab`, `editor.animation`, `editor.particles`, `editor.tilemap`, `editor.lighting`, `editor.ui`, `editor.theme` | [x] |
| C10 | `shadergraph.editor`, `editor.scripting` | [x] |

## Test checklist

| Batch | Scope | Done |
|-------|-------|------|
| T1 | `editor/**` tests | [x] |
| T2 | `build`, `project`, `serialization`, `prefab` | [x] |
| T3 | `scripting/**`, `shadergraph/**`, `materials` | [x] |
| T4 | Other runtime-tested packages | [x] |

Maintenance pass applied class-level Javadoc to types that lacked documentation; packages gained `package-info.java` where missing.

## Tier C+ — Inline comments

**Scope:** `llw-studio/src/main/java` only. **Manual review** — do not use automated Javadoc scripts.

### When to comment

- Method body **>15 lines** or multiple branches/loops
- **Play vs edit**, coordinate spaces (screen/world/ImGui item rect), frame ordering (flush before UI, input after `ImGui.image`)
- Undo boundaries, ECS iteration with early-continue, async/main-thread handoff
- Non-obvious domain rules (tile paint, build stages, script debounce)

### When to skip

- One-line ImGui field widgets; obvious `undo.push` without merge logic
- Restating the next line

### Style

- 1–2 lines above a block (**why**, not what)
- Section labels in long methods: `// --- Picking (edit mode) ---`
- Reference [editor-architecture.md](editor-architecture.md) for pass order when helpful

### Inline batches (I0–I10)

| Batch | Packages |
|-------|----------|
| I0 | P0 critical path (see inline-comment-pass.md) |
| I1 | `org.llw.studio`, `log`, `settings` |
| I2 | `build` |
| I3 | `editor` root |
| I4 | `editor.shell`, `editor.panels`, `editor.imgui` |
| I5 | `editor.render`, `editor.render.passes` |
| I6 | `editor.gizmo`, `editor.gizmos` |
| I7 | `editor.commands`, `editor.components`, `editor.inspector` |
| I8 | `editor.widgets`, `editor.widgets.fields` |
| I9 | `editor.assets`, `editor.launcher`, `editor.prefab`, `editor.animation`, … |
| I10 | `editor.scripting`, `shadergraph.editor` |

Track file-level status in [inline-comment-pass.md](inline-comment-pass.md). **Inline pass I0–I10:** complete (2026-05-25); class/package batches C1–C10 unchanged.
