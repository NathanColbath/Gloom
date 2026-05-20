# Collision

Static helpers in `org.llw.math.collision` for 2D intersection and containment.

## In this section

- [Geometry (hub)](/math/geometry) — primitive definitions
- [Rect](/math/rect) / [Circle](/math/circle) / [AABB](/math/aabb) — type-specific overlap
- [Line & Ray](/math/line-ray) — segment queries

## Intersection2

Central API for boolean tests and a few constructive results:

```java
import org.llw.math.collision.Intersection2;

Intersection2.contains(rect, x, y);
Intersection2.contains(circle, x, y);
Intersection2.intersects(rectA, rectB);
Intersection2.intersects(circleA, circleB);
Intersection2.intersects(circle, rect);
Intersection2.intersects(aabbA, aabbB);

// Barycentric triangle test (Y-down):
Intersection2.pointInTriangle(p, a, b, c);

// Closest point on segment:
Vector2f closest = Intersection2.closestPointOnSegment(p, a, b);

// Ray–ray: writes param t into outTA if rays intersect
boolean hit = Intersection2.rayRay(rayA, rayB, outTA);
```

Most `intersects` overloads delegate to methods on geometry types — use either style consistently in your codebase.

## Sat2f — convex polygons

Separating Axis Theorem for arbitrary **convex** polygons (rotated rectangles as four vertices):

```java
import org.llw.math.collision.Sat2f;

Vector2f[] square = { v0, v1, v2, v3 };
Vector2f[] other = { ... };
boolean overlap = Sat2f.intersects(square, other);
```

Vertices must be ordered (CW or CCW) in world space.

::: warning Concave shapes
`Sat2f` is for convex polygons only. Decompose concave shapes or use AABB broad phase + per-tile tests.
:::

::: tip Broad phase first
Use `Aabb2f.overlaps` or a grid before `Sat2f` — SAT is heavier than AABB tests.
:::

## See also

- [Platformer AABB](/cookbook/platformer-aabb)
- [Mouse Picking](/cookbook/mouse-picking)
- [Vectors](/math/vectors)
