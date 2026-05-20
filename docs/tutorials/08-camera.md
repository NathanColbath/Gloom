# Tutorial 8 — Camera & Views

## Goal

Pan and zoom a 2D scene with `Camera2d`, convert between screen pixels and world units with `screenToWorld` / `worldToScreen`, and pick world objects under the mouse cursor.

## Prerequisites

- Completed [Tutorial 7 — Transforms](/tutorials/07-transforms)
- Read [Coordinates & Frame Loop](/guide/coordinates) for Y-down conventions

::: details Why a camera?
Without a camera, one window pixel equals one world unit. Zooming out to show a large level or scrolling a map requires changing how world coordinates map to the screen — that is the camera's job.
:::

## Step 1 — Camera properties

`GraphicsContext` owns a `Camera2d` you retrieve once:

```java
import org.llw.render.graphics.Camera2d;
import org.llw.render.graphics.GraphicsContext;

GraphicsContext gfx = new GraphicsContext(window);
Camera2d camera = gfx.getCamera();
```

| Property | Meaning |
|----------|---------|
| `center` | World-space center of the visible rectangle |
| `size` | World width and height currently visible |
| `viewport` | Normalized sub-rectangle on the target (default: full window) |

At identity zoom, matching pixel size to world size is a good default:

```java
import org.llw.render.core.IntSize;

IntSize size = gfx.getSize();
camera.setCenter(size.width() / 2f, size.height() / 2f);
camera.setSize(size.width(), size.height());
```

## Step 2 — Pan with keyboard

Offset the center each frame using delta time:

```java
float panX = 0f, panY = 0f;

// inside the loop, after reading keys:
if (window.isKeyDown(Key.W)) panY -= 300f * dt;
if (window.isKeyDown(Key.S)) panY += 300f * dt;
if (window.isKeyDown(Key.A)) panX -= 300f * dt;
if (window.isKeyDown(Key.D)) panX += 300f * dt;

IntSize size = gfx.getSize();
camera.setCenter(size.width() / 2f + panX, size.height() / 2f + panY);
```

Always use `gfx.getSize()` after a window resize — initial `WindowSettings` dimensions can become stale.

## Step 3 — Zoom by changing visible size

**Smaller** `camera.setSize(...)` zooms **in** (less world visible). **Larger** size zooms **out**.

```java
float zoom = 1f; // 1 = default; 2 = see twice as much world

// on mouse scroll event:
zoom = Math.max(0.25f, Math.min(4f, zoom + scrollDelta * 0.1f));

camera.setSize(size.width() * zoom, size.height() * zoom);
```

::: tip Zoom toward cursor (optional enhancement)
Convert the cursor to world space before zooming, adjust `zoom`, then recompute `panX`/`panY` so the world point under the cursor stays fixed. See [Scroll Zoom Camera](/cookbook/scroll-zoom-camera) in the cookbook.
:::

## Step 4 — Screen ↔ world conversion

`Window.mousePosition()` returns **screen pixels**. After zoom or pan, those pixels no longer equal world coordinates.

```java
import org.llw.math.vector.Vector2f;

Vector2f world = camera.screenToWorld(window.mousePosition(), gfx.getSize());
Vector2f backToScreen = camera.worldToScreen(world, gfx.getSize());
```

Use `screenToWorld` whenever you place renderables at the cursor or run hit tests in world space. Use `worldToScreen` for screen-space UI that must align with a world object (health bars, labels).

## Step 5 — Mouse picking

Hit testing belongs in **world space** after conversion. For a clickable orb, keep its center and radius in world units and test with `Circle2f`:

```java
import org.llw.math.geometry.Circle2f;

Circle2f pickVolume = new Circle2f(orbX, orbY, 50f);
boolean hovered = pickVolume.contains(world.x, world.y);
```

On `MouseButtonPressed`, run the same test to detect clicks. Highlight the orb when `hovered` is true.

::: warning Picking ignores rotation
`Circle2f.contains` is axis-aligned in world space. Rotated rectangles need oriented tests or inverse-transform the point into local space. Start with circles and axis-aligned rects before tackling rotated sprites.
:::

## Step 6 — Resize handling

Reset center and size when the window changes so the scene stays framed:

```java
if (event instanceof WindowEvent.Resized r) {
    camera.setCenter(r.width() / 2f + panX, r.height() / 2f + panY);
    camera.setSize(r.width() * zoom, r.height() * zoom);
}
```

## Full class

```java
import org.llw.math.geometry.Circle2f;
import org.llw.math.vector.Vector2f;
import org.llw.render.core.Clock;
import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.render.graphics.Camera2d;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.renderables.Circle;
import org.llw.render.window.Key;
import org.llw.render.window.MouseButton;
import org.llw.render.window.Window;
import org.llw.render.window.WindowEvent;
import org.llw.render.window.WindowSettings;

public class CameraTutorial {
    public static void main(String[] args) {
        Window window = new Window(new WindowSettings().title("LLW Camera").size(960, 640));
        GraphicsContext gfx = new GraphicsContext(window);
        Camera2d camera = gfx.getCamera();
        Clock clock = new Clock();

        float panX = 0f, panY = 0f, zoom = 1f;
        float orbX = 480f, orbY = 320f;
        boolean orbSelected = false;

        Circle orb = new Circle();
        orb.setRadius(50f);
        orb.setOrigin(50f, 50f);
        orb.setFillColor(new Color(80, 160, 255, 230));

        Circle2f pick = new Circle2f(orbX, orbY, 50f);

        while (gfx.isActive()) {
            float dt = clock.tick();
            gfx.pollEvents();

            while (true) {
                var opt = window.pollEvent();
                if (opt.isEmpty()) break;
                WindowEvent e = opt.get();
                if (e instanceof WindowEvent.Closed) {
                    gfx.window().requestClose();
                } else if (e instanceof WindowEvent.Resized r) {
                    camera.setCenter(r.width() / 2f + panX, r.height() / 2f + panY);
                    camera.setSize(r.width() * zoom, r.height() * zoom);
                } else if (e instanceof WindowEvent.MouseScrolled s) {
                    zoom = Math.max(0.25f, Math.min(4f, zoom + s.yOffset() * 0.12f));
                } else if (e instanceof WindowEvent.MouseButtonPressed m
                        && m.button() == MouseButton.LEFT) {
                    Vector2f world = camera.screenToWorld(m.position(), gfx.getSize());
                    orbSelected = pick.contains(world.x, world.y);
                }
            }

            if (window.isKeyDown(Key.W)) panY -= 280f * dt;
            if (window.isKeyDown(Key.S)) panY += 280f * dt;
            if (window.isKeyDown(Key.A)) panX -= 280f * dt;
            if (window.isKeyDown(Key.D)) panX += 280f * dt;

            IntSize size = gfx.getSize();
            camera.setCenter(size.width() / 2f + panX, size.height() / 2f + panY);
            camera.setSize(size.width() * zoom, size.height() * zoom);

            Vector2f mouseWorld = camera.screenToWorld(window.mousePosition(), size);
            boolean hovered = pick.contains(mouseWorld.x, mouseWorld.y);

            orb.setPosition(orbX, orbY);
            orb.setFillColor(hovered || orbSelected
                    ? new Color(255, 200, 80, 240)
                    : new Color(80, 160, 255, 230));

            gfx.clear(new Color(16, 18, 26));
            gfx.draw(orb);
            gfx.present();
        }

        gfx.dispose();
    }
}
```

## What you learned

- `Camera2d` maps a world rectangle (`center` + `size`) onto the window.
- Panning adjusts `center`; zooming scales `size` relative to the framebuffer.
- `screenToWorld` and `worldToScreen` bridge input pixels and scene coordinates.
- Picking converts the mouse to world space, then tests geometry (e.g. `Circle2f.contains`).

**Next:** [Tutorial 9 — Audio](/tutorials/09-audio)
