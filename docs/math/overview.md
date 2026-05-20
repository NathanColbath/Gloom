# Math Overview

`org.llw.math` is a **2D-only** game math library sharing LLW's Y-down convention.

## In this section

| Page | Topics |
|------|--------|
| [Vectors](/math/vectors) | `Vector2f` algebra |
| [Matrix](/math/matrix) | `Matrix3x2`, GL upload |
| [Transforms](/math/transforms) | `Transform2f`, ortho helpers |
| [Rect](/math/rect) | `RectF` top-left + size |
| [Circle](/math/circle) | `Circle2f` disc tests |
| [Line & Ray](/math/line-ray) | `Line2f`, `Ray2f` |
| [AABB](/math/aabb) | `Aabb2f` min/max box |
| [Geometry (hub)](/math/geometry) | All primitives at a glance |
| [Collision](/math/collision) | `Intersection2`, `Sat2f` |
| [Interpolation](/math/interpolation) | `Interpolator`, `MathUtils` |
| [Easing](/math/easing) | `Easing` enum curves |
| [Splines & Noise](/math/splines-noise) | Bezier, Catmull-Rom, Perlin |

## Subpackages

| Package | Types |
|---------|-------|
| `org.llw.math.vector` | `Vector2f` |
| `org.llw.math.matrix` | `Matrix3x2` |
| `org.llw.math.transform` | `Transform2f` |
| `org.llw.math.geometry` | `RectF`, `Aabb2f`, `Circle2f`, `Line2f`, `Ray2f` |
| `org.llw.math.collision` | `Intersection2`, `Sat2f` |
| `org.llw.math.interpolation` | `Interpolator`, `Easing` |
| `org.llw.math.spline` | `CubicBezier2f`, `CatmullRom2f` |
| `org.llw.math.noise` | `PerlinNoise` |
| `org.llw.math.util` | `MathUtils`, `Angle` |

3D types (`Vector3f` for rendering) are intentionally **not** here — OpenAL keeps its own `Vector3f` in `org.llw.audio.core`.

## Example

```java
import org.llw.math.vector.Vector2f;
import org.llw.math.collision.Intersection2;
import org.llw.math.geometry.Circle2f;
import org.llw.math.geometry.RectF;

Circle2f orb = new Circle2f(50f, 50f, 20f);
RectF panel = new RectF(60f, 60f, 40f, 40f);
boolean hit = Intersection2.intersects(orb, panel);
```

## Used by render

`Camera2d`, renderable transforms, and orthographic projection all use `org.llw.math` types internally.

::: tip Pure Java
The math module has no LWJGL dependency — safe for headless simulation, server logic, and unit tests.
:::

## See also

- [Render Overview](/render/overview)
- [Platformer AABB](/cookbook/platformer-aabb)
- [Full Demo](/examples/full-demo)
