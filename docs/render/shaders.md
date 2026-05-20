# Shaders

LLW ships embedded default GLSL for sprites, shapes, and text. Programs compile at startup through `ShaderLibrary.loadDefaults()` — no external `.glsl` files are required for built-in rendering.

## In this section

| Topic | Page |
|-------|------|
| Shader programs | [Shaders](/render/shaders) (this page) |
| Per-draw override | [Draw State](/render/draw-state) |
| `Vertex` layout | [Vertex Geometry](/render/vertex-geometry) |
| Classpath GLSL | [Resource Loading](/render/resource-loading) |
| GL internals | [Render Overview](/render/overview) |

## Default programs

`DefaultShaders` registers programs consumed by the backend:

| Program | Used by |
|---------|---------|
| Sprite shader | `Sprite`, `Text`, textured `VertexGeometry` |
| Shape shader | `Rectangle`, `Circle`, untextured `VertexGeometry` |

`DrawState.shader()` overrides the backend default for a single queued draw.

## Custom shaders (advanced)

```java
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.ShaderProgram;

ShaderProgram custom = ShaderProgram.fromSources(vertexGlsl, fragmentGlsl);
// or from classpath:
ShaderProgram custom = ShaderProgram.fromClasspath("shaders/mine.vert", "shaders/mine.frag");

gfx.draw(sprite, DrawState.DEFAULT.withShader(custom));
```

Load sources with [Resource Loading](/render/resource-loading):

```java
String vert = ResourceLoader.loadText("shaders/pulse.vert");
String frag = ResourceLoader.loadText("shaders/pulse.frag");
ShaderProgram pulse = ShaderProgram.fromSources(vert, frag);
```

## Blend mode interaction

Shader output still passes through the draw's `BlendMode`:

```java
gfx.draw(glow, DrawState.DEFAULT
        .withShader(glowShader)
        .withBlendMode(BlendMode.ADDITIVE));
```

## Internals

The `org.llw.render.gl` package contains `OpenGlBackend`, `SpriteBatch`, `ShapeRenderer`, `TextRenderer`, and `DrawQueue`. Most games interact only with `GraphicsContext` and renderables.

## Common pitfalls

::: warning
Shader sources must be **GLSL 330 core** compatible with the OpenGL 3.3 context created by `Window`.
:::

- Custom shaders must respect the same `Vertex` attribute layout (position, UV, color) or provide a fully custom geometry path.
- Shader overrides participate in queue sorting — batching splits when program ids differ.
- `ShaderProgram` lifecycle is tied to the GL context — dispose programs when tearing down the backend.

::: tip
Prototype effects on a single `Sprite` with `DrawState.DEFAULT.withShader(...)` before wiring global backend changes.
:::

## See also

- [Draw State](/render/draw-state)
- [Graphics Context](/render/graphics-context)
- [Offscreen Rendering](/render/offscreen) — multi-pass effects
