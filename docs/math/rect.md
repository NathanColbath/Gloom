# Rect

`RectF` is an axis-aligned rectangle in **Y-down** space: top-left `(left, top)` plus `width` and `height`.

```java
import org.llw.math.geometry.RectF;

RectF panel = new RectF(10f, 20f, 100f, 50f);
// left=10, top=20, width=100, height=50
```

## Edges

```java
float r = panel.right();    // left + width
float b = panel.bottom();   // top + height
```

## Containment and overlap

```java
panel.contains(50f, 30f);
panel.intersects(other);

RectF merged = panel.union(other);      // smallest rect covering both
RectF clipped = panel.intersection(other); // null if disjoint
```

`contains` uses **inclusive** edges — points on the border count as inside.

## AABB conversion

```java
Aabb2f box = panel.asAabb();
RectF back = box.toRect();
```

## UI and hit testing

Screen-space UI maps naturally to `RectF`:

```java
RectF button = new RectF(x, y, w, h);
if (button.contains(mouseX, mouseY)) {
    // hover / click
}
```

When the camera zooms, convert mouse pixels to world space first — see [Coordinates](/best-practices/coordinates).

::: tip RectF vs Aabb2f
`RectF` stores top-left + size (SFML style). `Aabb2f` stores min/max corners (physics style). Convert with `rect.asAabb()` or `aabb.toRect()`.
:::

::: warning Negative size
Constructors do not validate positive width/height. Negative dimensions invert `right()` / `bottom()` semantics — always set positive extents.
:::

## See also

- [AABB](/math/aabb)
- [Geometry (hub)](/math/geometry)
- [Collision](/math/collision)
