# Mouse Picking

## Problem

You need to know which world-space object the player clicked. `Window.mousePosition()` returns **screen pixels** (top-left origin, Y-down). After zoom or pan, those pixels no longer equal world units. You must convert the cursor to world coordinates, then test against each object's bounds.

## Solution

Use `Camera2d.screenToWorld` with the live render-target size, then run containment tests with `Intersection2`.

```java
import org.llw.math.collision.Intersection2;
import org.llw.math.geometry.Circle2f;
import org.llw.math.geometry.RectF;
import org.llw.math.vector.Vector2f;
import org.llw.render.core.IntSize;
import org.llw.render.graphics.Camera2d;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.graphics.Texture2d;
import org.llw.render.renderables.Circle;
import org.llw.render.renderables.Sprite;
import org.llw.render.window.MouseButton;
import org.llw.render.window.Window;
import org.llw.render.window.WindowEvent;

Camera2d camera = gfx.getCamera();
IntSize size = gfx.getSize();

while (gfx.isActive()) {
    gfx.pollEvents();

    while (true) {
        var opt = window.pollEvent();
        if (opt.isEmpty()) break;

        WindowEvent event = opt.get();
        if (event instanceof WindowEvent.MouseButtonPressed pressed
                && pressed.button() == MouseButton.LEFT) {
            Vector2f world = camera.screenToWorld(pressed.position(), size);

            if (hitSprite(sprite, world)) {
                System.out.println("Clicked sprite");
            }
            if (hitCircle(orb, world)) {
                System.out.println("Clicked orb");
            }
        }
    }

    // ... update, draw, present ...
}

/** Axis-aligned bounds for an untransformed sprite at its position. */
static boolean hitSprite(Sprite sprite, Vector2f world) {
    Texture2d tex = sprite.getTexture();
    if (tex == null) return false;

    RectF uv = sprite.getTextureRect();
    float w = uv.width * tex.size().width();
    float h = uv.height * tex.size().height();
    Vector2f pos = sprite.getPosition();

    RectF bounds = new RectF(pos.x, pos.y, w, h);
    return Intersection2.contains(bounds, world.x, world.y);
}

static boolean hitCircle(Circle circle, Vector2f world) {
    Vector2f c = circle.getPosition();
    Circle2f shape = new Circle2f(c.x, c.y, circle.getRadius());
    return Intersection2.contains(shape, world.x, world.y);
}
```

Walk candidates **back-to-front** (highest draw layer or last enqueued first) so the topmost object wins when regions overlap.

::: details Variations

**Poll every frame instead of click events** — useful for hover highlights:

```java
Vector2f world = camera.screenToWorld(window.mousePosition(), gfx.getSize());
boolean hovering = hitSprite(sprite, world);
sprite.setTint(hovering ? new Color(255, 255, 200) : Color.WHITE);
```

**Triangles** — use barycentric containment when shapes are not axis-aligned boxes:

```java
Vector2f a = new Vector2f(100f, 50f);
Vector2f b = new Vector2f(200f, 50f);
Vector2f c = new Vector2f(150f, 10f);
boolean inside = Intersection2.pointInTriangle(world, a, b, c);
```

**Rectangle overlap** — for selection marquees or broad-phase culling:

```java
RectF selection = new RectF(startX, startY, width, height);
boolean overlaps = Intersection2.intersects(selection, objectBounds);
```

**Offscreen UI panels** — convert with the **main** camera and target size, then subtract the panel sprite's world offset before testing geometry drawn inside the offscreen pass (or maintain parallel world coordinates for minimap entities).

:::

## Pitfalls

- **Stale target size** — always pass `gfx.getSize()`, not cached `WindowSettings` dimensions, after a resize.
- **Camera updated after input** — convert the mouse using the same camera state used for rendering that frame; if you pan/zoom in the same loop, apply camera updates before `screenToWorld`.
- **Rotation and scale** — the recipe above uses axis-aligned rectangles in world space. Rotated sprites need an oriented bounds test (transform the point into local space, or use a convex hull + `Sat2f`).
- **Origin offset** — `Sprite` draws from local `(0, 0)`; if you set a non-zero origin via `setOrigin`, include it when building hit bounds.
- **Empty picks** — `screenToWorld` outside the camera viewport still returns a world point; clamp or ignore clicks outside the visible region if needed.

## See also

- [Camera](/render/camera)
- [Coordinates & Frame Loop](/guide/coordinates)
- [Collision](/math/collision)
- [Draw Order Layers](/cookbook/draw-order-layers)
- [Click Triangle](/cookbook/click-triangle)
