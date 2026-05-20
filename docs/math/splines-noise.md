# Splines & Noise

Procedural paths and noise in `org.llw.math.spline` and `org.llw.math.noise`.

## In this section

- [Interpolation](/math/interpolation) — scalar `lerp`, `smoothstep`
- [Easing](/math/easing) — normalized easing curves
- [Vectors](/math/vectors) — `Vector2f.lerp` along straight segments

## Easing

```java
import org.llw.math.interpolation.Easing;

float t = 0.3f;
float v = Easing.EASE_IN_OUT_CUBIC.evaluate(t);

float x = MathUtils.lerp(0f, 100f, Easing.EASE_OUT_QUAD.evaluate(t));
```

Available: `LINEAR`, `EASE_IN/OUT/IN_OUT` quad and cubic variants. See [Easing](/math/easing) for selection guidance.

## Cubic Bezier

```java
import org.llw.math.spline.CubicBezier2f;

CubicBezier2f curve = new CubicBezier2f(p0, p1, p2, p3);
Vector2f pos = curve.position(0.5f);
Vector2f tangent = curve.tangent(0.5f);
```

Control points `p1` and `p2` pull the curve; endpoints are `p0` and `p3`. Use tangent for facing rotation:

```java
float angle = (float) Math.atan2(tangent.y, tangent.x);
```

## Catmull-Rom

```java
import org.llw.math.spline.CatmullRom2f;

CatmullRom2f path = new CatmullRom2f(p0, p1, p2, p3); // minimum 4 points
Vector2f pos = path.position(t);  // t in [0, 1] along full spline
```

Passes through interior control points; useful for camera rails and entity patrol paths. The Gloom demo animates the orb along a Catmull-Rom spline.

::: details Endpoint behavior
Catmull-Rom needs phantom points beyond endpoints for curvature at starts/ends — duplicate end points or add ghost controls when paths must clamp at terminals.
:::

## Perlin noise

```java
import org.llw.math.noise.PerlinNoise;

PerlinNoise noise = new PerlinNoise(42); // seed
float n1d = noise.noise1D(x);
float n2d = noise.noise2D(x, y);
```

Same seed produces deterministic output — useful for procedural terrain, wind sway, and [Procedural Shake](/cookbook/procedural-shake).

::: tip Frequency
Scale input coordinates: `noise2D(x * 0.01f, y * 0.01f)` for low-frequency hills; larger multipliers add detail.
:::

## See also

- [Full Demo](/examples/full-demo)
- [Math Overview](/math/overview)
- [Tween UI](/cookbook/tween-ui)
