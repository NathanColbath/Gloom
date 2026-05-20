# RenderTarget

`org.llw.render.graphics.RenderTarget` is the SFML-style drawing surface API. Implementations (`GraphicsContext`, `OffscreenTarget`) **queue** `draw()` calls and submit to the GPU only on `flush()`. On-screen presentation uses `GraphicsContext.present()`, which flushes then swaps buffers.

All drawing uses the target's `Camera2d` and Y-down pixel coordinates.

## Key methods

| Method | Description |
|--------|-------------|
| `clear(Color)` | Clear color buffer |
| `draw(Renderable)` | Enqueue with `DrawState.DEFAULT` |
| `draw(Renderable, DrawState)` | Enqueue with per-draw overrides |
| `setCamera(Camera2d)` | Copy center, size, viewport from another camera |
| `getCamera()` | Mutable camera owned by this target |
| `getSize()` | Pixel dimensions (`IntSize`) |
| `flush()` | Submit queued draws to GPU and empty queue |

## Implementations

| Class | Backing store | End-of-frame |
|-------|---------------|--------------|
| `GraphicsContext` | Window default framebuffer | `present()` → `flush()` + swap |
| `OffscreenTarget` | FBO color texture | `flush()` only |

## Examples

On-screen loop:

```java
import org.llw.render.core.Color;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.renderables.Sprite;

GraphicsContext gfx = new GraphicsContext(window);
Sprite hero = new Sprite(texture);

while (gfx.isActive()) {
    gfx.pollEvents();
    gfx.clear(new Color(18, 20, 28));
    gfx.draw(hero);
    gfx.present();
}
```

Off-screen pass then composite:

```java
import org.llw.render.core.IntSize;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.renderables.Rectangle;

OffscreenTarget rt = new OffscreenTarget(gfx.backend(), new IntSize(512, 512));

rt.clear(Color.TRANSPARENT);
rt.draw(sceneSprite);
rt.flush();

Sprite composite = new Sprite(rt.colorTexture());
gfx.draw(composite);
```

Shared camera setup:

```java
import org.llw.render.graphics.Camera2d;

Camera2d miniMapCam = new Camera2d();
miniMapCam.setCenter(256f, 256f);
miniMapCam.setSize(512f, 512f);
rt.setCamera(miniMapCam);
```

## Pitfalls

::: warning
Nothing renders until `flush()` (or `present()` on `GraphicsContext`). Calling `clear()` after draws but before flush wipes the framebuffer while the queue still holds pending geometry for the next flush.
:::

- Each target owns an independent camera — changing `gfx.getCamera()` does not affect an offscreen target unless you copy settings explicitly.
- `getSize()` on `GraphicsContext` tracks live window framebuffer size; offscreen size is fixed at construction.
- After `GraphicsContext.dispose()`, the target is inactive — do not draw.

::: tip
Treat `RenderTarget` as the abstraction in engine code so the same scene graph can render to screen or texture by swapping the active target.
:::

## See also

- [Graphics Context](/render/graphics-context)
- [Offscreen Rendering](/render/offscreen)
- [Draw State](/render/draw-state)
- [Camera](/render/camera)
