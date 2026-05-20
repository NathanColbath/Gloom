# AABB

`Aabb2f` is an axis-aligned bounding box stored as **min** and **max** corners `(minX, minY)` and `(maxX, maxY)`.

```java
import org.llw.math.geometry.Aabb2f;

Aabb2f box = Aabb2f.fromCenterExtents(50f, 50f, 25f, 25f);
// center (50,50), half-extents (25,25)
```

## Overlap and merge

```java
box.overlaps(other);

Aabb2f combined = new Aabb2f(box.minX, box.minY, box.maxX, box.maxY);
combined.merge(other);   // expands in place to include other
```

## Conversion

```java
RectF rect = box.toRect();
Aabb2f fromRect = rect.asAabb();
```

## Physics and tile grids

AABBs excel in platformers and broad-phase grids:

```java
Aabb2f player = Aabb2f.fromRect(spriteBounds);
for (Aabb2f tile : tiles) {
    if (player.overlaps(tile)) {
        resolveCollision(player, tile);
    }
}
```

See [Platformer AABB](/cookbook/platformer-aabb) for a full resolution pattern.

::: tip Fatten for continuous collision
When moving fast, expand the AABB by `velocity * dt` before tile queries to avoid tunneling between frames.
:::

::: details Empty boxes
If `minX > maxX` or `minY > maxY`, the box is degenerate. `overlaps` returns false against valid boxes. Normalize inputs when building from drag rectangles where x0 > x1.
:::

## See also

- [Rect](/math/rect)
- [Collision](/math/collision)
- [Geometry (hub)](/math/geometry)
