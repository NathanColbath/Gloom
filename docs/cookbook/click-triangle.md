# Click Triangle

## Problem

Buttons and hotspots are not always axis-aligned rectangles. A triangular map region, UI wedge, or mesh pick target needs reliable hit testing when the player clicks — especially after camera zoom and pan, because mouse events arrive in **screen pixels**, not world units.

## Solution

Convert the mouse position to world space with `Camera2d.screenToWorld`, then test containment with `Intersection2.pointInTriangle`. The test uses barycentric weights and includes edges.

```java
import org.llw.math.collision.Intersection2;
import org.llw.math.vector.Vector2f;
import org.llw.render.core.IntSize;
import org.llw.render.graphics.Camera2d;
import org.llw.render.window.MouseButton;
import org.llw.render.window.Window;
import org.llw.render.window.WindowEvent;

// Triangle vertices in world space (Y-down)
Vector2f a = new Vector2f(1050f, 120f);
Vector2f b = new Vector2f(1180f, 120f);
Vector2f c = new Vector2f(1115f, 30f);

Camera2d camera = graphics.getCamera();
IntSize size = graphics.getSize();

while (graphics.isActive()) {
    graphics.pollEvents();

    while (true) {
        var opt = window.pollEvent();
        if (opt.isEmpty()) {
            break;
        }
        WindowEvent event = opt.get();
        if (event instanceof WindowEvent.MouseButtonPressed pressed
                && pressed.button() == MouseButton.LEFT) {
            Vector2f world = camera.screenToWorld(pressed.position(), size);
            if (Intersection2.pointInTriangle(world, a, b, c)) {
                onTriangleClicked();
            }
        }
    }

    // draw triangle geometry for feedback ...
    graphics.present();
}
```

For a moving or rotated triangle, transform the click into the triangle's local space first (apply the inverse of the entity `Transform2f.toMatrix()`), or recompute `a`, `b`, `c` each frame from transformed corners.

Continuous hover checks use the same test with `window.mousePosition()` each frame:

```java
Vector2f mouseWorld = camera.screenToWorld(window.mousePosition(), graphics.getSize());
boolean hovered = Intersection2.pointInTriangle(mouseWorld, a, b, c);
```

::: details Variations

- **VertexGeometry picking:** If you already draw a `VertexGeometry` triangle, keep vertex positions in a field array and reuse them for the same test.
- **Convex polygons:** Decompose into triangles and test each, or use `Sat2f.intersects` for general convex polygons.
- **Screen-space UI:** When the camera is 1:1 with pixels (menu scene), world and screen coordinates match — conversion is still safe after resize.
- **Touch-style drag:** Combine with [Drag Sprite](/cookbook/drag-sprite) by starting drag only when the initial press hits the triangle.

:::

## Pitfalls

- **Raw mouse pixels:** `window.mousePosition()` without `screenToWorld` breaks as soon as `camera.setSize` implements zoom — see [Camera](/render/camera).
- **Degenerate triangle:** Collinear vertices yield zero area; `pointInTriangle` returns `false` for all points.
- **Winding order:** The barycentric implementation accepts any winding; inconsistent vertex order still works but keep order stable when animating corners.
- **Drawing vs hit mesh:** Align pick vertices with drawn geometry; an outline drawn with stroke width still uses the same three corner points for the math test.

## See also

- [Collision](/math/collision)
- [Mouse Picking](/cookbook/mouse-picking)
- [Scene Stack](/cookbook/scene-stack)
