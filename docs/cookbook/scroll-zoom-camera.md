# Scroll Zoom Camera

## Problem

You want the mouse wheel to zoom the view in and out around the scene, with sensible limits so the player cannot zoom to zero or lose the playfield entirely.

## Solution

Track a `zoom` multiplier, clamp it on `WindowEvent.MouseScrolled`, and drive `Camera2d` size from the live window dimensions. The Gloom launcher demo uses this pattern:

```java
import org.llw.render.core.IntSize;
import org.llw.render.graphics.Camera2d;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.window.Key;
import org.llw.render.window.Window;
import org.llw.render.window.WindowEvent;

float panX = 0f;
float panY = 0f;
float zoom = 1f;

Camera2d camera = gfx.getCamera();
camera.setCenter(settings.width() / 2f, settings.height() / 2f);
camera.setSize(settings.width(), settings.height());

while (gfx.isActive()) {
    float dt = clock.tick();
    gfx.pollEvents();

    while (true) {
        var opt = window.pollEvent();
        if (opt.isEmpty()) break;

        WindowEvent event = opt.get();
        if (event instanceof WindowEvent.Resized resized) {
            // Keep 1:1 pixel mapping at zoom 1 after resize
            camera.setCenter(resized.width() / 2f, resized.height() / 2f);
            camera.setSize(resized.width(), resized.height());
            panX = 0f;
            panY = 0f;
            zoom = 1f;
        } else if (event instanceof WindowEvent.MouseScrolled scrolled) {
            zoom = Math.max(0.25f, Math.min(4f, zoom + scrolled.yOffset() * 0.1f));
        }
    }

    if (window.isKeyDown(Key.W)) panY -= 200f * dt;
    if (window.isKeyDown(Key.S)) panY += 200f * dt;
    if (window.isKeyDown(Key.A)) panX -= 200f * dt;
    if (window.isKeyDown(Key.D)) panX += 200f * dt;

    IntSize size = gfx.getSize();
    camera.setCenter(size.width() / 2f + panX, size.height() / 2f + panY);
    camera.setSize(size.width() * zoom, size.height() * zoom);

    gfx.clear(background);
    // draw world-space renderables ...
    gfx.present();
}
```

**How zoom works:** `camera.setSize` sets the world width and height visible on screen. Multiplying by `zoom > 1` increases the visible world area (zoom **out**). Values below `1` shrink the visible area (zoom **in**).

::: details Variations

**Zoom toward cursor** — before changing zoom, convert the cursor to world space; after updating zoom, adjust `panX` / `panY` so that world point stays under the cursor:

```java
Vector2f before = camera.screenToWorld(window.mousePosition(), size);
zoom = clamp(zoom + scrolled.yOffset() * 0.1f, 0.25f, 4f);
camera.setSize(size.width() * zoom, size.height() * zoom);
Vector2f after = camera.screenToWorld(window.mousePosition(), size);
panX += before.x - after.x;
panY += before.y - after.y;
```

**Logarithmic steps** — multiply instead of adding for even perceived steps:

```java
float factor = scrolled.yOffset() > 0 ? 0.9f : 1.1f;
zoom = clamp(zoom * factor, 0.25f, 4f);
```

**Discrete zoom levels** — snap to a ladder `{0.5, 1, 2, 4}` for a strategy map feel.

**Separate UI camera** — draw HUD in screen space by enqueueing with a high `DrawState` layer and positions in pixels, or use an orthographic camera with `setSize` equal to pixel size and zero pan.

:::

## Pitfalls

- **`settings.width()` after resize** — use `gfx.getSize()` when updating center and size each frame.
- **Forgetting to convert input** — after zoom changes, reposition labels and handle picking with `screenToWorld`, not raw mouse pixels.
- **Inverted scroll** — GLFW reports `yOffset` positive for scroll-up; flip the sign if it feels backwards on your platform.
- **Resize handler vs zoom** — resetting pan/zoom on resize is optional; if you preserve zoom, still re-center with live `getSize()`.
- **Offscreen targets** — each `OffscreenTarget` owns its own `Camera2d`; zooming the main view does not affect render-to-texture passes unless you update both.

## See also

- [Camera](/render/camera)
- [Coordinates & Frame Loop](/guide/coordinates)
- [Mouse Picking](/cookbook/mouse-picking)
- [Full Demo](/examples/full-demo)
