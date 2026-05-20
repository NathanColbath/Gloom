# Line & Ray

Segments and rays live in `org.llw.math.geometry` as `Line2f` and `Ray2f`.

## Line2f — finite segment

```java
import org.llw.math.geometry.Line2f;
import org.llw.math.geometry.Ray2f;
import org.llw.math.vector.Vector2f;

Line2f edge = new Line2f(0f, 0f, 100f, 0f);
Vector2f mouse = new Vector2f(mouseX, mouseY);

Vector2f closest = edge.closestPoint(mouse);
float dist = edge.distanceTo(mouse);
```

`closestPoint` clamps to the segment — unlike an infinite line, points beyond the endpoints project to the nearest endpoint.

## Ray2f — infinite half-line

```java
import org.llw.math.geometry.Ray2f;

Ray2f ray = new Ray2f(originX, originY, dirX, dirY);
Vector2f hit = ray.at(2.5f);   // origin + direction * t
```

Direction is **not** auto-normalized. For unit-speed casting, normalize the direction vector first:

```java
Vector2f dir = new Vector2f(1f, 1f);
dir.normalize();
Ray2f ray = new Ray2f(ox, oy, dir.x, dir.y);
```

## Ray intersection

`Intersection2` provides ray tests against other primitives:

```java
// Ray–ray: writes param t into outTA if rays intersect
boolean hit = Intersection2.rayRay(rayA, rayB, outTA);

// Closest point on segment (line utility)
Vector2f onSeg = Intersection2.closestPointOnSegment(p, segA, segB);
```

Use rays for mouse picking along a view direction, line-of-sight checks, and projectile paths.

::: tip Y-down angles
When converting a rotation angle to a ray direction, remember +Y points **down**. A 90° clockwise rotation from +X yields `(0, 1)`, not `(0, -1)`.
:::

::: warning Zero direction
A zero-length ray direction makes `at(t)` stationary. Guard `normalize()` when length is below epsilon.
:::

## See also

- [Geometry (hub)](/math/geometry)
- [Collision](/math/collision)
- [Mouse Picking](/cookbook/mouse-picking)
