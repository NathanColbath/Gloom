# Camera

`org.llw.render.graphics.Camera2d` maps a world-space rectangle to a render target using an orthographic Y-down projection. Each `RenderTarget` owns its own camera (`getCamera()` / `setCamera()`).

## In this section

| Topic | Page |
|-------|------|
| `Camera2d` usage | [Camera](/render/camera) (this page) |
| On-screen target | [Graphics Context](/render/graphics-context) |
| Offscreen cameras | [Offscreen Rendering](/render/offscreen) |
| Input → world coords | [Input](/render/input) |
| Drawable placement | [Transformable](/render/transformable) |

## Core properties

| Property | Meaning |
|----------|---------|
| `center` | World-space center of the visible region |
| `size` | World width and height of the visible region |
| `viewport` | Normalized sub-rectangle on the target (default full window) |

Default construction: center `(0, 0)`, size `(1000, 1000)`, viewport `(0, 0, 1, 1)`.

```java
import org.llw.render.graphics.Camera2d;

Camera2d camera = gfx.getCamera();
camera.setCenter(640f, 360f);
camera.setSize(1280f, 720f);  // 1 world unit ≈ 1 pixel at this zoom
```

## Zoom and pan

```java
import org.llw.render.core.IntSize;

float panX = 0f, panY = 0f, zoom = 1f;
IntSize size = gfx.getSize();

camera.setCenter(size.width() / 2f + panX, size.height() / 2f + panY);
camera.setSize(size.width() * zoom, size.height() * zoom);
```

Smaller `size` = zoomed **in** (less world visible). Larger `size` = zoomed **out**.

Mouse-wheel zoom toward cursor:

```java
import org.llw.math.vector.Vector2f;
import org.llw.render.window.WindowEvent;

case WindowEvent.MouseScrolled s -> {
    Vector2f before = camera.screenToWorld(window.mousePosition(), gfx.getSize());
    zoom = Math.max(0.25f, zoom - s.yOffset() * 0.1f);
    camera.setSize(gfx.getSize().width() * zoom, gfx.getSize().height() * zoom);
    Vector2f after = camera.screenToWorld(window.mousePosition(), gfx.getSize());
    camera.setCenter(
            camera.getCenter().x + (before.x - after.x),
            camera.getCenter().y + (before.y - after.y));
}
```

## Screen ↔ world

```java
Vector2f world = camera.screenToWorld(window.mousePosition(), gfx.getSize());
Vector2f screen = camera.worldToScreen(worldPoint, gfx.getSize());
```

Use these whenever input or UI must align with zoomed/panned scene geometry.

## Viewport (split screen / picture-in-picture)

`setViewport(left, top, width, height)` uses **normalized fractions** of the target (0–1). Offsets are from the top-left; Y-down.

```java
// Left half of the window shows the game world
camera.setViewport(0f, 0f, 0.5f, 1f);

// Minimap in the top-right quarter
miniCam.setViewport(0.75f, 0f, 0.25f, 0.25f);
```

`screenToWorld` / `worldToScreen` account for the active viewport on that camera.

## Resize handling

When the window framebuffer changes, keep the visible world region coherent:

```java
case WindowEvent.Resized r -> {
    Camera2d cam = gfx.getCamera();
    cam.setCenter(r.width() / 2f, r.height() / 2f);
    cam.setSize(r.width(), r.height());  // 1:1 pixel mapping
}
```

For zoom-preserved resize, scale `size` proportionally instead of resetting to pixel dimensions.

## Matrices

| Method | Purpose |
|--------|---------|
| `getViewMatrix(IntSize)` | Ortho view from center/size |
| `getProjectionMatrix(IntSize)` | Viewport-scaled ortho |
| `getViewProjection(IntSize)` | Matrix uploaded during `flush()` |

Advanced custom drawing can reuse `getViewProjection` to match the batch renderer.

## Offscreen cameras

`OffscreenTarget` initializes its camera to the FBO midpoint and extent. Copy or configure independently:

```java
offscreen.getCamera().setCenter(256f, 256f);
offscreen.getCamera().setSize(512f, 512f);
```

## Common pitfalls

::: warning
Mouse events from `Window` are always in **full window pixels**. If the camera viewport does not cover the full target, convert with the same camera that drew that region.
:::

- World and screen both use **Y-down** — increasing Y moves downward.
- `getCenter()` / `getSize()` return **copies**; mutate via setters.
- After window resize, update center and/or size or world-aligned sprites will drift.

::: tip
Start with `setSize` equal to framebuffer pixels for 1:1 art placement, then introduce zoom by scaling `size` only.
:::

## See also

- [Coordinates & Frame Loop](/guide/coordinates)
- [Render Target](/render/render-target)
- [Math — Matrix3x2](/math/transforms)
