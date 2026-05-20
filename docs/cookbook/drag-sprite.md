# Drag a Sprite

## Problem

You want the player to click a sprite and move it with the mouse. Raw screen pixels drift from the sprite once the camera zooms or pans, and assigning the cursor position directly makes the sprite **jump** because its top-left corner snaps to the pointer.

## Solution

On press, record a **world-space grab offset** between the sprite and the cursor. While dragging, convert the current mouse position to world space and subtract that offset.

```java
import org.llw.math.collision.Intersection2;
import org.llw.math.geometry.RectF;
import org.llw.math.vector.Vector2f;
import org.llw.render.core.IntSize;
import org.llw.render.graphics.Camera2d;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.renderables.Sprite;
import org.llw.render.window.MouseButton;
import org.llw.render.window.Window;
import org.llw.render.window.WindowEvent;

Sprite draggable = new Sprite(texture);
draggable.setPosition(400f, 300f);

boolean dragging = false;
float grabOffsetX = 0f;
float grabOffsetY = 0f;

Camera2d camera = gfx.getCamera();

while (gfx.isActive()) {
    float dt = clock.tick();
    gfx.pollEvents();

    IntSize size = gfx.getSize();

    while (true) {
        var opt = window.pollEvent();
        if (opt.isEmpty()) break;

        WindowEvent event = opt.get();
        if (event instanceof WindowEvent.MouseButtonPressed pressed
                && pressed.button() == MouseButton.LEFT) {
            Vector2f world = camera.screenToWorld(pressed.position(), size);
            Vector2f pos = draggable.getPosition();

            RectF bounds = spriteBounds(draggable); // same helper as mouse-picking recipe
            if (Intersection2.contains(bounds, world.x, world.y)) {
                dragging = true;
                grabOffsetX = world.x - pos.x;
                grabOffsetY = world.y - pos.y;
            }
        } else if (event instanceof WindowEvent.MouseButtonReleased released
                && released.button() == MouseButton.LEFT) {
            dragging = false;
        }
    }

    // Apply pan/zoom before positioning the dragged sprite this frame
    camera.setCenter(size.width() / 2f + panX, size.height() / 2f + panY);
    camera.setSize(size.width() * zoom, size.height() * zoom);

    if (dragging) {
        Vector2f world = camera.screenToWorld(window.mousePosition(), size);
        draggable.setPosition(world.x - grabOffsetX, world.y - grabOffsetY);
    }

    gfx.clear(background);
    gfx.draw(draggable);
    gfx.present();
}
```

The grab offset preserves the point on the sprite where the user clicked, so motion feels anchored.

::: details Variations

**Drag only while the button is held** — keep `dragging` true from `MouseButtonPressed` until `MouseButtonReleased`; only update position inside that window.

**Clamp to a world rectangle** — after computing the new position:

```java
float x = Math.max(worldMinX, Math.min(worldMaxX, world.x - grabOffsetX));
float y = Math.max(worldMinY, Math.min(worldMaxY, world.y - grabOffsetY));
draggable.setPosition(x, y);
```

**Drag with keyboard pan active** — update `panX` / `panY` from WASD *before* `screenToWorld` so the sprite stays under the cursor while the view moves.

**Multi-select** — store a list of `(sprite, offset)` pairs on press and update each entry while dragging.

**Right-click to cancel** — clear `dragging` on `MouseButton.RIGHT` without moving the sprite.

:::

## Pitfalls

- **Camera order** — call `screenToWorld` with the camera already updated for the current frame; dragging before pan/zoom updates causes one-frame jitter.
- **Missing grab offset** — `setPosition(mouseWorld)` places the sprite's local origin at the cursor, not the clicked pixel.
- **Hit test too small** — transparent margins in the texture still count unless you shrink the pick rect or use per-pixel alpha tests (not built into LLW).
- **Events vs polling** — `MouseMoved` events work, but polling `window.mousePosition()` each frame while `dragging` is simpler and matches the latest camera.
- **Resize** — re-read `gfx.getSize()` every frame; do not cache initial window dimensions.

## See also

- [Mouse Picking](/cookbook/mouse-picking)
- [Scroll Zoom Camera](/cookbook/scroll-zoom-camera)
- [Camera](/render/camera)
- [Events](/render/events)
- [Sprite](/render/sprite)
