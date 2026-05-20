# Gloom / LLW Documentation Enhancement Plan

**Date:** 2026-05-20
**Scope:** Full audit of ~110+ Markdown files in `docs/` across VitePress site
**Status:** Proposed

---

## Executive Summary

The current documentation is **well-structured** with good cross-referencing, architecture diagrams, a cookbook, tutorials, and an SFML migration guide. However, several areas are **thin** or **missing entirely** — notably the engine architecture explanation, the editor internals, SDK documentation, and several smaller pages that lack depth. This plan addresses those gaps in priority order.

---

## Phase 1 — Core Engine Architecture & Internals (HIGH PRIORITY)

### 1.1 New page: `docs/architecture/engine-overview.md`

**What's missing:** There is no single page that explains how the LLW engine actually works under the hood. The current `index.md` has a brief mermaid diagram, but no depth.

**What to write:**
- **The GL lifecycle** — Window creation → OpenGL 3.3 core profile → context ownership → the frame pipeline (pollEvents → clear → draw → present → swap buffers)
- **Draw queue architecture** — How `gfx.draw()` enqueues work; the sorting mechanism (layer → shader → texture → blendMode → submissionOrder); how `present()` triggers the flush
- **OpenGL backend (`org.llw.render.gl`)** — What `OpenGlBackend`, `SpriteBatch`, `ShapeRenderer`, `TextRenderer`, and `DrawQueue` each do; how they share state via `GlStateTracker`; the `FramebufferObject` wrapper
- **Shader system** — `ShaderLibrary` default programs, how `ShaderProgram` wraps GL handles, what the default sprite and shape GLSL do, how uniforms are set
- **Camera / projection** — How `Camera2d` produces the orthographic Y-down view/projection matrices, how `getViewProjection()` feeds into the batch renderer
- **Audio architecture** — `AudioContext` → OpenAL device/context init; the source pool; how `Music` streaming works via `audio.update()`; pitch/pan via AL source properties
- **Math as pure Java** — Why `org.llw.math` has no LWJGL dependency; how it feeds into render transforms; the matrix convention (column-major for GL upload)
- **Resource loading** — `ResourceLoader` classpath I/O; how `Texture2d.fromClasspath` works; the `ResourceManager` orchestration layer
- **Threading model** — Why everything runs on the main thread; GLFW callbacks; implications for background work

Add:
- Full mermaid sequence diagram of a single frame
- Link to source code files for each subsystem
- "When to go deeper" callout boxes

### 1.2 New page: `docs/architecture/render-pipeline.md`

**What's missing:** The render overview page is a table of contents. No detailed render pipeline with GL-level detail.

**What to write:**
- Step-by-step: `GraphicsContext.draw(Sprite)` → `DrawQueue.enqueue()` → sort → `flush()` → `SpriteBatch.render()` → `glDrawElements()`
- How vertex data flows: `Vertex` struct → VBO upload → batch → draw call
- How texture binds work: `Texture2d` → GL texture handle → bind per batch
- How `DrawState` overrides affect the pipeline (transform, shader, blend mode, texture)
- The `OffscreenTarget` pipeline: FBO → color attachment → read as GL texture → use in subsequent draw
- Sprite batch flush triggers (texture change, shader change, blend mode change, max batch vertices)

### 1.3 Enhanced: `docs/render/overview.md`

**Current:** A table of contents with a minimal draw loop and a brief draw-queue mention (80 lines).

**Add:**
- Full package tree with class names
- "How the pipeline works" callout section with architecture diagram
- Performance characteristics (what causes batch breaks)
- Integration with offscreen targets for multi-pass
- Common GL-specific pitfalls (context loss, texture limits, etc.)

---

## Phase 2 — Studio / Editor Deep Dive (HIGH PRIORITY)

### 2.1 Enhanced: `docs/studio/llw-engine-integration.md`

**Current:** 55 lines — architecture overview but no depth.

**Rewrite to include:**
- **Startup sequence:** `StudioLauncher.main()` → GLFW → ImGui init → `EditorShell` → project load → scene deserialize → frame loop
- **Frame loop in detail:** pollEvents → render Scene view FBO → render Game view FBO → build ImGui UI (menus, panels, docking) → render ImGui → swap buffers
- **Render pipeline for views:** How `SceneRenderer` and `GameRenderer` render the world into `OffscreenTarget`s; how those FBO color attachments are passed to `ImGui.image()`
- **The tab system:** How Scene/Game views share a viewport; how focus tracking works for input routing
- **Tool system:** How scene tools (Hand, Move, Rotate, Scale, Paint, Erase) are selected, activated, and communicate with the scene. How gizmo rendering integrates with the scene pass (overlay layer).
- **Asset import pipeline:** How dropping a PNG into the Project panel triggers `AssetImporter` → GUID generation → metadata write → preview generation → thumbnail cache
- **Component system:** How `ComponentCatalog` works; how new component types register; how `ComponentSerializerRegistry` discovers serializers; how `ComponentInspectorDrawer` renders each component in the inspector
- **Undo/redo architecture:** The command stack; how `TransformEditCommand`, `AddComponentCommand`, etc. implement `EditorCommand`; how the stack serializes to support undo across saves

### 2.2 Enhanced: `docs/studio/play-mode.md`

**Current:** Good (80 lines) but could be deeper on:
- The `PlayModeRunner` lifecycle in more detail (clone → warmup → activate → tick → deactivate)
- How the deep clone works: `SceneCloner` traverses entities, components, references
- How play bridges are wired: `PlayInputBridge`, `PlayPhysicsBridge`, `PlayAnimationBridge`, `PlayUiInputBridge`, `PlayClock` — what each does internally
- How `GraalScriptRuntime` creates GraalJS context, loads bundled JS, instantiates script classes, and maps lifecycle callbacks
- The play-mode sandbox: why edit scene is frozen, how active/inactive propagation differs in play
- Physics world construction: how `PhysicsWorldBuilder` processes colliders and rigidbodies
- Error recovery: what happens when esbuild bundling fails, when GraalJS throws, when Box2D explodes

### 2.3 Enhanced: `docs/studio/ecs-and-gameobjects.md`

**Current:** 40 lines — brief concept table.

**Add:**
- **The World + Entity model:** How `World` stores entities, component stores, and system scheduling; how `Entity` is an integer ID + component bundle accessor
- **The GameObject façade:** How `GameObject` wraps entity ID with name, parent/child tree, convenience accessors; how the hierarchy tree is rebuilt from `HierarchyComponent` on scene load
- **The hybrid model in detail:** When do you use raw entity access vs GameObject? Where is the boundary between edit-mode GameObject and play-mode ECS?
- **System scheduling:** How `SystemGroup.LOGIC` orders are enforced; how systems declare dependencies; how `TransformSystem` depends on `JsScriptSystem` output
- **World matrix propagation:** The `TransformSystem` algorithm (DAG traversal from root entities, combine local transforms into world-space matrices)
- **Active state propagation:** How `ActiveUtility` walks the hierarchy to determine entity active state
- **SceneObjectIdComponent:** How stable IDs survive save/load and play-mode cloning; how reference mapping works

### 2.4 Enhanced: `docs/studio/scenes-and-serialization.md`

**Current:** 50 lines — brief JSON structure.

**Add:**
- **Full JSON schema** for scene files with examples of every component type's serialized form
- **Serializer architecture:** How `SceneSerializer` → `SceneObjectSerializer` → `ComponentSerializerRegistry` → per-type `ComponentSerializer<T>` works
- **Version migration:** How `version` field triggers migration steps; what a migration looks like
- **Prefab instance serialization:** How overrides are stored (field diffs vs base prefab data)
- **GUID resolution:** How asset GUIDs in scene JSON are resolved to runtime handles
- **The round-trip guarantee:** What serialization tests verify

---

## Phase 3 — Missing Sections & New Pages (MEDIUM PRIORITY)

### 3.1 New: `docs/contributing/index.md`

- How to build from source
- Running tests: `./gradlew test`, `:llw:test`, `:llw-runtime:test`
- Code style conventions (check for checkstyle/spotless config)
- Project conventions (branch naming, commits, PR workflow — pull from README)
- How to add a new component type (registration, serializer, inspector drawer)
- How to add a new renderable type
- How to add a new scripting API namespace
- Documentation contribution guide (VitePress setup, screenshot tooling)

### 3.2 New: `docs/performance/profiling.md`

- Using `Clock` for frame timing
- Batch counting: how to measure draw-call count
- Texture atlas best practices with real examples
- Offscreen target cost analysis
- JVM profiling tools (JMC, async-profiler) integration tips
- Pre-allocating objects in hot paths
- When to use fixed timestep vs variable timestep for physics

### 3.3 New: `docs/distribution/index.md`

- Building the fat JAR: `./gradlew :llw-player:fatJar`
- Using jpackage for native desktop bundles
- The `BuildPlayer` command from Studio
- What's included in a build (essential JARs, content packs, launcher scripts)
- Platform-specific notes (Windows `.exe`, macOS `.app`, Linux AppImage)
- Distribution checklist (JRE bundling, content pack signing)

### 3.4 New: `docs/architecture/data-formats.md`

- **LLWP asset pack format:** Header, manifest, chunk layout, compression
- **`.llwproj` manifest format:** Full JSON schema
- **`.scene.json` format** (moved from 2.4 above)
- **`.animation.json` and `.anim.json` formats**
- **`.prefab.json` format**
- **`.shadergraph.json` format**
- **Metadata format** (`.studio/metadata/assets/*.meta`)

### 3.5 New: `docs/sdk/overview.md`

**What's missing:** The TypeScript SDK is mentioned but never documented as a standalone artifact.

- SDK directory structure (`.llw/sdk/`)
- `llw.core` module exports: all types, functions, classes
- How the SDK is generated from `llw-studio/src/main/resources/scripting-sdk/`
- How to update the SDK when adding new script API namespaces
- TypeScript configuration (`tsconfig.json`) for external IDE IntelliSense
- Mapping between TypeScript types and Java internal types

---

## Phase 4 — Thin Page Expansion (MEDIUM PRIORITY)

### 4.1 Keyboard Shortcuts → Full Reference

**Current:** 49 lines, mostly noting what's *not* implemented.

**Expand to:**
- Document all working shortcuts with a proper table
- Show modifier conventions (Ctrl on Windows/Linux, Cmd on macOS)
- Add a "planned" section for menu-only items
- Screenshot of the shortcut if implemented via a preferences panel

### 4.2 Undo and Commands → Developer Reference

**Current:** 35 lines.

**Expand:**
- List every `EditorCommand` subclass with what it does
- Explain the command stack model (push → execute → push to undo stack)
- How composite commands work (gizmo drag records one composite)
- How undo interacts with serialization (does undo mark the scene dirty?)
- How to add a new undoable operation

### 4.3 Console Panel → Functional Reference

**Current:** 39 lines.

**Expand:**
- Log formatting and filtering
- How to write log messages from Java code (for engine contributors)
- The Console sink architecture (how editor modules, GraalJS, and esbuild route to the same panel)
- Clear, copy, and search features (if implemented)
- Integration with error recovery workflows

### 4.4 Game View → Full Documentation

**Current:** 38 lines.

**Expand:**
- Render target resolution / aspect ratio (configurable?)
- FPS counter / stats overlay
- Multi-monitor / multi-viewport support (if any)
- How the Game view rendering differs from Scene view (no gizmos, no grid, different camera)
- Input focus mechanics in detail (what happens when you click outside Game)

### 4.5 Shader Graph → Authoring Guide

**Current:** 28 lines.

**Expand:**
- Node type reference with screenshots: all available node types
- Input/output type compatibility table
- Practical tutorial: "Create a pulsing glow effect"
- How shader graphs compile to GLSL (the code generation path)
- Debugging shader graphs (console errors, GLSL validation)
- Performance characteristics of graph nodes
- How to share shader graphs between sprites

### 4.6 In-Game UI → Widget Reference

**Current:** 60 lines.

**Expand:**
- UI layout system (anchors, offsets, or pixel-only?)
- UI Canvas sorting and rendering order with multiple canvases
- Widget event lifecycle (hover → press → release → click)
- Text field input handling (IME, focus management, validation)
- Styling and theming (colors, fonts per widget)
- How to create custom UI widgets (if supported)
- UI performance guidelines

### 4.7 Physics 2D → Comprehensive Reference

**Current:** 62 lines.

**Expand:**
- Full Box2D integration details: world stepping, velocity/position iterations
- Collision matrix (what collides with what, layer-based filtering)
- Joints: distance, revolute, prismatic, weld — which are supported?
- `RaycastHit2D` return value in detail: point, normal, distance, entity
- `overlapCircle` / `overlapAABB` shape queries
- Physics materials (bounce, friction) — if supported
- Debug visualization during play
- Known Box2D limitations (max polygons per shape, etc.)

### 4.8 Tilemaps & Tilesets → Complete Reference

**Current:** 44 lines + 36 lines.

**Expand:**
- Tilemap layer properties (parallax, collision, z-offset)
- Runtime tilemap manipulation performance considerations
- Rule tile priority and conflict resolution algorithm
- Large tilemap optimization (chunking, frustum culling?)
- Autotiling edge cases (diagonal corners, single tile islands)
- Tile collider shape types (full cell, reduced, per-subtile)
- Animated tiles (if supported)

### 4.9 Animation → Full Authoring Guide

**Current:** 58 lines.

**Expand:**
- Animation clip format: keyframe types (sprite, position, rotation, scale, color)
- Animation set as a state machine: state transitions, conditions (if implemented)
- Blend trees and crossfade (if supported)
- Event frames in animation clips
- Script-driven animation override patterns
- Performance: how many animated entities can run simultaneously

---

## Phase 5 — Scripting API Expansion (MEDIUM PRIORITY)

### 5.1 Enhanced: All Scripting API Pages

**Current state:** Most scripting API pages are reference lists with minimal examples.

**For each page (`overview.md`, `scene-and-entities.md`, `components.md`, `physics.md`, `animation.md`, `ui.md`, `input-and-keys.md`) add:**

- **More code examples** — 2-3 practical examples per page, not just type signatures
- **Error handling patterns** — what happens when `getComponent` returns null, when GUID is invalid
- **Performance notes** — avoid per-frame string lookups, cache component references
- **TypeScript→Java mapping** — how each TypeScript API call maps to the underlying Java bridge
- **Common pitfalls** specific to scripting (e.g., mutation of shared objects, closure memory leaks)

### 5.2 Enhanced: Scripting Cookbook

**Current:** 99 lines with a few good recipes.

**Add recipes:**
- Entity pooling (reusing bullet/prefab instances)
- Coroutine-style timers with `frameCount` / `time`
- Screen shake using transform offset
- Health bar UI widget from sprite or label
- Camera lerp smoothing
- Scene loading/unloading transitions
- Achievement system with Console logging
- Save/load game state (serializing script field values)
- Audio manager (pooled sounds with volume categories)
- Input action mapping (rebindable keys)

### 5.3 New: `docs/studio/scripting-api/events.md`

A dedicated page for the event system:
- `start()`, `update()`, `fixedUpdate()` execution guarantees
- `onEnable()` / `onDisable()` behavior at play start/stop
- `onDestroy()` cleanup patterns
- Collision event ordering (enter → stay → exit)
- Trigger vs collision differences
- Custom events / message system (if implemented)

---

## Phase 6 — Tutorial Enhancements (LOWER PRIORITY)

### 6.1 Tutorial cross-references and "under the hood" boxes

Each tutorial should gain:
- "How it works" expandable sections connecting the code to the engine internals
- Source file links for curious readers
- "What's next" pathway to specific cookbook recipes
- Warning boxes for common mistakes discovered in user testing

### 6.2 New tutorial chapters (optional)

Potential additions:
- **Tutorial 11 — Scenes & Resource Manager** — proper asset lifecycle
- **Tutorial 12 — Offscreen Effects** — minimap, post-processing
- **Tutorial 13 — Working with Shaders** — custom GLSL, shader parameters
- **Tutorial 14 — Game States** — implementing a scene stack

---

## Phase 7 — Structural Improvements (LOWER PRIORITY)

### 7.1 Sidebar reorganization

Current sidebar has sections in this order: Tutorials → Guide → Render → Audio → Math → Resources → Cookbook → Best Practices → Studio → More → Examples → API.

Suggested reordering:
1. **Getting Started** (Guide → Project Setup → FAQ)
2. **Tutorials** (10 chapters)
3. **Engine** (Architecture → Render → Audio → Math → Resources)
4. **Studio** (all studio sections)
5. **Scripting** (all scripting API + cookbook)
6. **Cookbook** (standalone recipes)
7. **Best Practices**
8. **Building & Distribution**
9. **Contributing**
10. **Reference** (SFML Migration, Data Formats, Javadoc)

### 7.2 Centralize troubleshooting

Merge `faq.md` and `studio/troubleshooting.md` into a single `troubleshooting/index.md` with categorized sections. Add a "Common errors by symptom" quick-reference table at the top.

### 7.3 Add a "Getting Help" page

How to:
- Check the Console
- Enable LWJGL debug logging (`-Dorg.lwjgl.util.Debug=true`)
- Run the Gloom demo as a reference
- File a GitHub issue (templates, what to include)
- Check Javadoc

### 7.4 Screenshot audit

- Identify pages that reference studio screenshots that don't exist yet (the `studio-screenshot` macro is used in many places with paths like `{file=\"26-llw-render-pipeline.png\"}`)
- Ensure all referenced screenshots exist in `docs/studio/images/`
- Add alt-text to all screenshots for accessibility

---

## Priority Summary

| Phase | Focus | Effort | Impact | Status |
|-------|-------|--------|--------|--------|
| **1** | Engine architecture + render pipeline internals | High | Highest — fills the biggest gap | ✅ **DONE** |
| **2** | Studio/editor deep dive (integration, play mode, ECS, serialization) | High | Critical for Studio users | ⏳ Planned |
| **3** | New pages (contributing, perf, distribution, data formats, SDK) | Medium | Fills missing sections | ⏳ Planned |
| **4** | Thin page expansion (14 pages from 28-60 lines to full reference) | Medium | Round out existing content | ⏳ Planned |
| **5** | Scripting API expansion + cookbook recipes | Medium | Directly helps game developers | ⏳ Planned |
| **6** | Tutorial enhancements + new chapters | Lower | Nice-to-have | ⏳ Planned |
| **7** | Structural improvements | Lower | Polish + maintainability | ⏳ Planned |

---

## Estimated Effort

| Phase | Estimated pages to write/rewrite | Estimated word count |
|-------|----------------------------------|---------------------|
| 1 | 3 new, 1 enhanced | ~8,000–12,000 words |
| 2 | 4 enhanced pages (2-4× expansion each) | ~6,000–10,000 words |
| 3 | 5 new pages | ~6,000–8,000 words |
| 4 | 9 thin pages expanded | ~4,000–6,000 words |
| 5 | 7 scripting API pages + cookbook | ~5,000–7,000 words |
| 6 | Tutorial enhancements + 4 new | ~6,000–10,000 words |
| 7 | Sidebar, troubleshooting, screenshots | ~2,000–4,000 words |

**Total estimated scope:** ~37,000–57,000 words across ~30 pages
