# Circle

`Circle2f` represents a disc by **center** `(x, y)` and **radius**.

```java
import org.llw.math.geometry.Circle2f;

Circle2f orb = new Circle2f(400f, 300f, 70f);
```

## Containment

```java
orb.contains(410f, 310f);
orb.overlaps(otherCircle);
orb.overlaps(rect);
```

Circle–rectangle overlap uses closest-point-on-AABB distance.

## Center and radius

```java
Vector2f c = orb.center();
float r = orb.radius();
orb.setRadius(80f);
```

## Render counterpart

`org.llw.render.renderables.Circle` is a **drawable** with fill/outline colors. Keep simulation state in `Circle2f` and sync each frame:

```java
Circle2f hitbox = new Circle2f(entityX, entityY, radius);
renderCircle.setPosition(hitbox.center().x, hitbox.center().y);
renderCircle.setRadius(hitbox.radius());
```

::: tip Radius vs diameter
LLW uses **radius** everywhere in math. Sprite sizes often use diameter — divide by two when building hit circles from texture dimensions.
:::

## See also

- [Geometry (hub)](/math/geometry)
- [Collision](/math/collision)
- [Render Circle](/render/circle)
