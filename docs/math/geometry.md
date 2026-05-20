# Geometry

2D primitives in Y-down space under `org.llw.math.geometry`.

## In this section

| Page | Type |
|------|------|
| [Rect](/math/rect) | `RectF` — top-left + width/height |
| [Circle](/math/circle) | `Circle2f` — center + radius |
| [Line & Ray](/math/line-ray) | `Line2f`, `Ray2f` |
| [AABB](/math/aabb) | `Aabb2f` — min/max corners |
| [Collision](/math/collision) | Intersection helpers |

## Quick reference

### RectF

```java
RectF rect = new RectF(10f, 20f, 100f, 50f);
rect.contains(50f, 30f);
rect.intersects(other);
RectF merged = rect.union(other);
```

### Circle2f

```java
Circle2f circle = new Circle2f(64f, 64f, 32f);
circle.contains(70f, 70f);
circle.overlaps(rect);
```

### Line2f & Ray2f

```java
Line2f seg = new Line2f(0f, 0f, 100f, 0f);
Vector2f p = new Vector2f(mouseX, mouseY);
Vector2f closest = seg.closestPoint(p);
float dist = seg.distanceTo(p);

Ray2f ray = new Ray2f(ox, oy, dx, dy);
Vector2f at = ray.at(2f);
```

### Aabb2f

```java
Aabb2f box = Aabb2f.fromCenterExtents(50f, 50f, 25f, 25f);
box.overlaps(other);
box.merge(other);
RectF asRect = box.toRect();
```

## Render vs math types

| Math (`org.llw.math.geometry`) | Render (`org.llw.render.renderables`) |
|-------------------------------|---------------------------------------|
| `RectF`, `Circle2f` — logic | `Rectangle`, `Circle` — drawing |
| Lightweight, public fields | Colors, outlines, layers |

Keep hitboxes in math types; sync to renderables for display.

::: tip Public fields
`RectF` exposes `left`, `top`, `width`, `height` directly — fine for hot paths; use methods for derived edges and unions.
:::

## See also

- [Math Overview](/math/overview)
- [Click Triangle](/cookbook/click-triangle)
