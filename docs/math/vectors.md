# Vectors

`Vector2f` in `org.llw.math.vector` is a mutable 2D vector used throughout LLW render and math.

## In this section

- [Transforms](/math/transforms) — position as part of `Transform2f`
- [Geometry](/math/geometry) — points, closest points, ray origins
- [Interpolation](/math/interpolation) — `Vector2f.lerp`

## Basics

```java
Vector2f a = new Vector2f(3f, 4f);
Vector2f b = new Vector2f(1f, 0f);

float len = a.length();
a.normalize();
float d = a.dot(b);
Vector2f perp = a.perp();           // (-y, x) — CCW perpendicular
Vector2f mid = Vector2f.lerp(a, b, 0.5f);
float dist = a.distance(b);
float lenSq = a.lengthSquared();   // for comparisons without sqrt
```

## Arithmetic

```java
a.add(1f, 2f).subtract(b).scale(2f);
Vector2f copy = a.copy();
```

Prefer in-place `add` / `subtract` / `scale` in collision loops; use static factories when immutability clarifies intent.

## Projection

```java
Vector2f along = a.project(b);
Vector2f sideways = a.reject(b);
float angle = a.angleBetween(b);     // radians, Y-down aware
```

## Approximate equality

```java
boolean close = a.isApproxEqual(b, 1e-4f);
// default epsilon: MathUtils.EPSILON
```

## Copy semantics

```java
Vector2f copy = new Vector2f(a);
copy.set(b);
```

Renderables return **copies** from `getPosition()` — always mutate through `setPosition`.

::: warning Y-down angles
`angleBetween` and rotation integration assume +Y points **down**. A vector pointing to screen-bottom has positive Y.
:::

::: tip distanceSquared
Compare squared distances with `lengthSquared()` or manual `dx*dx+dy*dy` — no square root.
:::

## See also

- [Math Overview](/math/overview)
- [Matrix](/math/matrix)
