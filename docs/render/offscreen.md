# Offscreen Rendering

`org.llw.render.graphics.OffscreenTarget` implements `RenderTarget` against an OpenGL framebuffer. Draws go to an RGBA `Texture2d` color attachment instead of the window.

## In this section

| Topic | Page |
|-------|------|
| FBO workflow | [Offscreen Rendering](/render/offscreen) (this page) |
| `RenderTarget` API | [Render Target](/render/render-target) |
| Sampling results | [Textures](/render/textures) |
| Display as sprite | [Sprite](/render/sprite) |
| Shared GL backend | [Graphics Context](/render/graphics-context) |
| Independent camera | [Camera](/render/camera) |

## Setup

```java
import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.renderables.Rectangle;
import org.llw.render.renderables.Sprite;

OffscreenTarget offscreen = new OffscreenTarget(gfx.backend(), new IntSize(420, 260));

Rectangle panel = new Rectangle();
panel.setSize(200f, 120f);
panel.setFillColor(new Color(60, 80, 120, 230));

// render to texture:
offscreen.clear(new Color(25, 28, 35));
offscreen.draw(panel);
offscreen.flush();  // not present()

// display the result on screen:
Sprite quad = new Sprite(offscreen.colorTexture());
quad.setPosition(80f, 420f);
gfx.draw(quad);
gfx.present();
```

## Lifecycle

| Step | Call | Notes |
|------|------|-------|
| 1 | `clear(Color)` | Binds FBO, clears attachment, unbinds |
| 2 | `draw(...)` | Queues geometry (same as on-screen) |
| 3 | `flush()` | Binds FBO, submits queue, unbinds |
| 4 | `colorTexture()` | Sample or draw as `Sprite` texture |
| 5 | `dispose()` | Release FBO and attachment |

## Key differences from GraphicsContext

| On-screen (`GraphicsContext`) | Offscreen (`OffscreenTarget`) |
|-------------------------------|-------------------------------|
| `present()` flushes + swap | `flush()` only |
| Window-sized (`getSize()` live) | Fixed `IntSize` at construction |
| Owns `Window` | Requires initialized `OpenGlBackend` |
| Default camera from window settings | Default camera centered on FBO size |

## Multi-pass post-processing

```java
OffscreenTarget scene = new OffscreenTarget(gfx.backend(), new IntSize(1280, 720));
OffscreenTarget bloom = new OffscreenTarget(gfx.backend(), new IntSize(1280, 720));

scene.clear(Color.BLACK);
scene.draw(world);
scene.flush();

bloom.clear(Color.TRANSPARENT);
bloom.draw(glowSprites, DrawState.DEFAULT.withBlendMode(BlendMode.ADDITIVE));
bloom.flush();

gfx.clear(Color.BLACK);
gfx.draw(new Sprite(scene.colorTexture()));
gfx.draw(new Sprite(bloom.colorTexture()), DrawState.DEFAULT.withBlendMode(BlendMode.ADDITIVE));
gfx.present();
```

## Transparent RTs

Clear with `Color.TRANSPARENT` when the texture will be composited over the scene:

```java
offscreen.clear(new Color(0, 0, 0, 0));
```

Ensure subsequent on-screen draws use alpha blending (`DrawState.DEFAULT`).

## Camera on offscreen targets

The offscreen camera defaults to center `(width/2, height/2)` and size matching the FBO. Adjust for a different world view without affecting the main window:

```java
offscreen.getCamera().setCenter(playerX, playerY);
offscreen.getCamera().setSize(400f, 300f);
```

Or copy from another target: `offscreen.setCamera(gfx.getCamera())`.

## Cleanup

```java
offscreen.dispose();
```

Do not sample `colorTexture()` after disposal.

## Common pitfalls

::: warning
Forgetting `flush()` leaves the color attachment stale — `colorTexture()` still references the GPU object but pixel contents are from the last successful flush.
:::

- `OffscreenTarget` shares `OpenGlBackend` with `GraphicsContext` — create it **after** `new GraphicsContext(window)`.
- `clear()` on offscreen binds/unbinds internally; queued draws are not flushed by `clear()` alone.
- FBO size is fixed; resizing requires a new `OffscreenTarget`.
- Drawing the FBO texture while the same FBO is bound for writing causes feedback issues — `flush()` unbinds before you sample via a `Sprite` on the default framebuffer.

::: tip
Reuse one offscreen target per render pass per frame instead of allocating FBOs every tick.
:::

## See also

- [Render Target](/render/render-target)
- [Draw State](/render/draw-state)
- [Shaders](/render/shaders) — custom post-process programs
