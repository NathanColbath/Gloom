# Coordinates & Frame Loop

## In this section

| Page | Description |
|------|-------------|
| [Tutorial 8 — Camera](/tutorials/08-camera) | Hands-on zoom and picking |
| [Camera reference](/render/camera) | Full `Camera2d` API |
| [Best Practices — Coordinates](/best-practices/coordinates) | When to use world vs screen |
| [Cookbook — Scroll Zoom](/cookbook/scroll-zoom-camera) | Mouse wheel zoom |

## Y-down convention

LLW uses **Y-down** coordinates everywhere:

- Origin `(0, 0)` is the **top-left** of the window or render target.
- `x` increases to the right; `y` increases downward.
- This matches screen pixels and SFML-style APIs.

The math library (`org.llw.math`) documents the same convention in its `package-info`.

## Standard frame loop

```java
Clock clock = new Clock();

while (gfx.isActive()) {
    float dt = clock.tick();          // seconds since last frame
    gfx.pollEvents();                 // GLFW events → WindowEvent queue

    // handle input, update game state with dt ...

    gfx.clear(backgroundColor);
    gfx.draw(sprite);
    gfx.present();                    // flush draw queue + swap buffers
}
```

| Step | Why |
|------|-----|
| `pollEvents()` | Drains GLFW callbacks into `Window.pollEvent()` |
| `draw(...)` | Enqueues geometry; nothing hits the GPU yet |
| `present()` | Flushes the queue and swaps the front buffer |

Offscreen targets use `flush()` instead of `present()` — see [Offscreen Rendering](/render/offscreen).

## Screen ↔ world coordinates

The camera defines which world rectangle maps to the window. When you zoom by shrinking the visible world size, raw mouse pixels no longer equal world units.

```java
Camera2d camera = gfx.getCamera();
Vector2f world = camera.screenToWorld(window.mousePosition(), gfx.getSize());
sprite.setPosition(world.x, world.y);

Vector2f screen = camera.worldToScreen(sprite.getPosition(), gfx.getSize());
```

### Zoom example

```java
float zoom = 1f;
// on scroll: zoom = clamp(zoom + delta, 0.25f, 4f);

IntSize size = gfx.getSize();
camera.setCenter(size.width() / 2f + panX, size.height() / 2f + panY);
camera.setSize(size.width() * zoom, size.height() * zoom);
```

At `zoom = 2`, the camera shows twice as much world space; a mouse pixel maps to a world point closer to the center.

## Common pitfalls

- Forgetting `present()` — appears as a frozen or black window.
- Using `settings.width()` after a resize — prefer `gfx.getSize()` for live dimensions.
- Placing UI at raw mouse pixels while the camera is zoomed — always convert through the camera.

## See also

- [Camera](/render/camera)
- [Graphics Context](/render/graphics-context)
- [Full Demo](/examples/full-demo)
