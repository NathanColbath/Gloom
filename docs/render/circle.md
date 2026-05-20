# Circle

`org.llw.render.renderables.Circle` approximates a disk with a `TRIANGLE_FAN` and an optional outline ring with `TRIANGLE_STRIP`. The shape is centered at local origin `(0, 0)` with configurable radius and segment count.

## Key methods

| Method | Description |
|--------|-------------|
| `setRadius` / `getRadius` | Radius in local pixels (default 50) |
| `setPointCount` / `getPointCount` | Circumference segments (clamped to ≥ 3, default 48) |
| `setFillColor` / `getFillColor` | Disk interior color |
| `setOutlineColor` / `getOutlineColor` | Ring color |
| `setOutlineThickness` / `getOutlineThickness` | Ring width in local pixels |
| `setFilled` / `isFilled` | Draw filled disk (default `true`) |
| `setOutlined` / `isOutlined` | Draw outline ring (default `false`) |
| Transform API | Position moves the center; scale can produce ellipses |

## Examples

Simple orb:

```java
import org.llw.render.core.Color;
import org.llw.render.renderables.Circle;

Circle orb = new Circle();
orb.setRadius(70f);
orb.setPosition(900f, 360f);
orb.setFillColor(new Color(255, 120, 80, 200));
gfx.draw(orb);
```

Ring marker with low segment count (performance):

```java
Circle marker = new Circle();
marker.setRadius(24f);
marker.setPointCount(16);
marker.setFilled(false);
marker.setOutlined(true);
marker.setOutlineThickness(3f);
marker.setOutlineColor(Color.WHITE);
gfx.draw(marker);
```

Ellipse via non-uniform scale:

```java
Circle shadow = new Circle();
shadow.setRadius(50f);
shadow.setScale(1.5f, 0.6f);
shadow.setFillColor(new Color(0, 0, 0, 80));
```

## Pitfalls

::: warning
Low `pointCount` values show visible faceting. Values below 3 are clamped to 3 — a triangle fan, not a circle.
:::

- Outline inner radius is `max(0, radius - thickness)`; thick outlines on small radii fill the entire disk.
- `setPosition(x, y)` places the **center** at `(x, y)` because local geometry is centered on the origin.
- Non-uniform scale on the transform turns the circle into an ellipse; segment count does not adapt to stretched perimeter.

::: tip
Use ~32–64 segments for HUD elements; drop to 12–16 only for distant or tiny decorative circles.
:::

## See also

- [Rectangle](/render/rectangle)
- [Vertex Geometry](/render/vertex-geometry)
- [Transformable](/render/transformable)
