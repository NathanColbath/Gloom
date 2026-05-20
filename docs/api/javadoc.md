# Javadoc

LLW and LLW Studio generate standard Java API documentation from source comments in the `:llw` and `:llw-studio` Gradle modules.

## Generate locally

### LLW engine

```bash
./gradlew :llw:javadoc
```

Open:

```
llw/build/docs/javadoc/index.html
```

### LLW Studio

Builds `:llw:javadoc` first so Studio docs can link engine types.

```bash
./gradlew :llw-studio:javadoc
```

Open:

```
llw-studio/build/docs/javadoc/index.html
```

## Package index

| Package | Tutorial | Reference |
|---------|----------|-----------|
| `org.llw` | — | Root overview |
| `org.llw.render` | [Tutorials 1–8](/tutorials/01-window) | [Render overview](/render/overview) |
| `org.llw.render.window` | [02 Events](/tutorials/02-events) | [Window](/render/window), [Events](/render/events), [Input](/render/input) |
| `org.llw.render.graphics` | [03–08](/tutorials/03-textures) | [Graphics Context](/render/graphics-context), [Camera](/render/camera), [Textures](/render/textures) |
| `org.llw.render.renderables` | [04–06](/tutorials/04-sprites) | [Sprite](/render/sprite), [Rectangle](/render/rectangle), [Circle](/render/circle) |
| `org.llw.render.core` | [10 Game Loop](/tutorials/10-game-loop) | [Color](/render/color), [Clock](/render/clock) |
| `org.llw.render.gl` | [Shaders](/render/shaders) | Internal GL layer |
| `org.llw.render.resources` | [03 Textures](/tutorials/03-textures) | [Resource Loading](/render/resource-loading) |
| `org.llw.resources` | [Cookbook: Resource Manager](/cookbook/resource-manager) | [Resources overview](/resources/overview) |
| `org.llw.audio` | [09 Audio](/tutorials/09-audio) | [Audio overview](/audio/overview) |
| `org.llw.audio.core` | — | `Vector3f`, `PlaybackStatus`, `Time` |
| `org.llw.audio.resources` | [Formats](/audio/formats) | [Resources](/audio/resources) |
| `org.llw.math` | [08 Camera](/tutorials/08-camera) | [Math overview](/math/overview) |
| `org.llw.math.vector` | [07 Transforms](/tutorials/07-transforms) | [Vectors](/math/vectors) |
| `org.llw.math.matrix` | [08 Camera](/tutorials/08-camera) | [Matrix](/math/matrix) |
| `org.llw.math.transform` | [07 Transforms](/tutorials/07-transforms) | [Transforms](/math/transforms) |
| `org.llw.math.geometry` | [Cookbook](/cookbook/mouse-picking) | [Rect](/math/rect), [Circle](/math/circle), [Line & Ray](/math/line-ray) |
| `org.llw.math.collision` | [Cookbook](/cookbook/click-triangle) | [Collision](/math/collision) |
| `org.llw.math.interpolation` | [Tween UI](/cookbook/tween-ui) | [Interpolation](/math/interpolation), [Easing](/math/easing) |
| `org.llw.math.spline` | [10 Game Loop](/tutorials/10-game-loop) | [Splines & Noise](/math/splines-noise) |
| `org.llw.math.util` | — | `MathUtils`, `Angle` |
| `org.llw.math.noise` | [Procedural Shake](/cookbook/procedural-shake) | `PerlinNoise` in splines page |

## LLW Studio package index

| Package | Studio guide |
|---------|----------------|
| `org.llw.studio` | [Getting started](/studio/getting-started) |
| `org.llw.studio.ecs` | [ECS and GameObjects](/studio/ecs-and-gameobjects) |
| `org.llw.studio.ecs.components` | [ECS and GameObjects](/studio/ecs-and-gameobjects) |
| `org.llw.studio.scene` | [ECS and GameObjects](/studio/ecs-and-gameobjects) |
| `org.llw.studio.assets` | [Project and assets](/studio/project-and-assets) |
| `org.llw.studio.serialization` | [Scenes and serialization](/studio/scenes-and-serialization) |
| `org.llw.studio.systems` | [Systems reference](/studio/systems-reference) |
| `org.llw.studio.scripting` | [Scripting](/studio/scripting) |
| `org.llw.studio.scripting.js` | [Scripting](/studio/scripting) |
| `org.llw.studio.scripting.js.bindings` | [Scripting](/studio/scripting) |
| `org.llw.studio.scripting.setup` | [Scripting](/studio/scripting) |
| `org.llw.studio.render` | [Scene view](/studio/scene-view) |
| `org.llw.studio.editor` | [Editor shell](/studio/editor-shell) |
| `org.llw.studio.editor.panels` | [Editor shell](/studio/editor-shell) |
| `org.llw.studio.editor.commands` | [Undo and commands](/studio/undo-and-commands) |
| `org.llw.studio.editor.gizmo` | [Scene view](/studio/scene-view) |
| `org.llw.studio.editor.components` | [Inspector](/studio/inspector) |
| `org.llw.studio.editor.inspector.builtin` | [Inspector](/studio/inspector) |
| `org.llw.studio.editor.widgets` | [UI theme and widgets](/studio/ui-theme-and-widgets) |
| `org.llw.studio.playmode` | [Play mode](/studio/play-mode) |
| `org.llw.studio.project` | [Project and assets](/studio/project-and-assets) |
| `org.llw.studio.prefab` | [Scenes and serialization](/studio/scenes-and-serialization) |
| `org.llw.studio.memory` | [Memory and performance](/studio/memory-and-performance) |
| `org.llw.studio.log` | [Console](/studio/console) |

## Studio Javadoc conventions

When editing `llw-studio` sources:

- Add `package-info.java` when introducing a new package.
- Document every public type with a class-level summary (role, editor vs play context).
- Document public and protected members with `@param`, `@return`, and `@throws` where non-obvious.
- Use these terms consistently:
  - **Screen** — top-left origin pixels on a render target or ImGui panel.
  - **World** — Y-down world units aligned with LLW renderables and `EditorCamera`.
- Prefer `{@linkplain org.llw...}` for engine types; use `{@link}` for Studio types.
- Do not use `@implNote` (requires newer Javadoc); use `<p>Implementation note: …` in the comment body instead.

## Sources JAR

```bash
./gradlew :llw:sourcesJar
./gradlew :llw-studio:sourcesJar
```

Enables IDE navigation into LLW and Studio implementation.

## Hosted Javadoc (future)

Copy `llw/build/docs/javadoc/` and `llw-studio/build/docs/javadoc/` to `docs/public/javadoc/` and `docs/public/javadoc-studio/` before `vitepress build` to embed API reference in the static site.

## See also

- [Getting Started](/guide/getting-started)
- [SFML Migration](/sfml-migration)
- [FAQ](/faq)
