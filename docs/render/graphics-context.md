# Graphics Context

`GraphicsContext` is the on-screen `RenderTarget` tied to a `Window`. It initializes `OpenGlBackend`, configures a default `Camera2d` to the window's initial size, and owns the draw queue.

## In this section

| Topic | Page |
|-------|------|
| On-screen `RenderTarget` | [Graphics Context](/render/graphics-context) (this page) |
| Target interface | [Render Target](/render/render-target) |
| `DrawState` & layers | [Draw State](/render/draw-state) |
| `Color` | [Color](/render/color) |
| `Clock` | [Clock](/render/clock) |
| `Texture2d` / `TextureFactory` | [Textures](/render/textures) |
| Classpath I/O | [Resource Loading](/render/resource-loading) |
| Camera | [Camera](/render/camera) |
| Offscreen FBO | [Offscreen Rendering](/render/offscreen) |

## Lifecycle

```java
import org.llw.render.core.Color;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.window.Window;

Window window = new Window(settings);
GraphicsContext gfx = new GraphicsContext(window);

// each frame:
gfx.pollEvents();
gfx.clear(color);
gfx.draw(renderable);
gfx.present();

// shutdown:
gfx.dispose();
```

## Key methods

| Method | Description |
|--------|-------------|
| `pollEvents()` | Poll GLFW and forward to the window |
| `clear(Color)` | Set GL clear color and clear the framebuffer |
| `draw(Renderable)` | Enqueue a drawable |
| `draw(Renderable, DrawState)` | Enqueue with blend mode, layer, shader override |
| `present()` | Flush queue and swap buffers |
| `getCamera()` | Mutable `Camera2d` for this target |
| `getSize()` | Live window framebuffer size |
| `window()` | Underlying `Window` |
| `backend()` | `OpenGlBackend` for advanced / offscreen use |
| `isActive()` | `false` after dispose |

## DrawState

```java
import org.llw.render.graphics.BlendMode;
import org.llw.render.graphics.DrawState;

DrawState state = DrawState.DEFAULT
        .withLayer(10)
        .withBlendMode(BlendMode.ALPHA);

gfx.draw(text, state);  // draws above layer 0 sprites
```

Layers sort ascending; equal layers preserve submission order. Full detail: [Draw State](/render/draw-state).

## Common pitfalls

- `draw()` does not render immediately — always end the frame with `present()`.
- After resize, call `getSize()` rather than cached `WindowSettings` dimensions.
- `dispose()` destroys the window — do not use `gfx` afterward.

## See also

- [Render Target](/render/render-target)
- [Renderables](/render/renderables)
- [Offscreen Rendering](/render/offscreen)
