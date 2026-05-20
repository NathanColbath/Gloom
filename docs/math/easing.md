# Easing

`Easing` is an enum of normalized easing curves: input `t` in [0, 1], output in [0, 1].

```java
import org.llw.math.interpolation.Easing;

float t = 0.3f;
float curved = Easing.EASE_IN_OUT_CUBIC.evaluate(t);
```

`evaluate` clamps `t` before applying the curve.

## Available curves

| Enum | Character |
|------|-----------|
| `LINEAR` | `t` |
| `EASE_IN_QUAD` / `EASE_OUT_QUAD` / `EASE_IN_OUT_QUAD` | Quadratic |
| `EASE_IN_CUBIC` / `EASE_OUT_CUBIC` / `EASE_IN_OUT_CUBIC` | Cubic |

**Ease in** — slow start, fast finish (acceleration).  
**Ease out** — fast start, slow finish (deceleration).  
**Ease in-out** — slow at both ends.

## Applying to ranges

Easing shapes the **interpolation factor**, not the output range directly:

```java
float start = 100f, end = 400f;
float t = elapsed / duration;
float x = MathUtils.lerp(start, end, Easing.EASE_OUT_CUBIC.evaluate(t));
```

For vector paths, ease `t` then call `Vector2f.lerp`.

## Picking a curve

| Scenario | Suggested curve |
|----------|-----------------|
| UI panel slide-in | `EASE_OUT_CUBIC` |
| Camera punch / shake decay | `EASE_OUT_QUAD` |
| Menu fade | `EASE_IN_OUT_QUAD` |
| Mechanical motion | `LINEAR` |

::: tip Chaining
Run separate easings per axis if needed: `lerp(x0, x1, easeX(t))` and `lerp(y0, y1, easeY(t))` can produce arcs without splines.
:::

::: warning Overshoot
These enums do not overshoot (no back/elastic). Values stay in [0, 1]. For bounce, combine with splines or custom curves.
:::

## See also

- [Interpolation](/math/interpolation)
- [Splines & Noise](/math/splines-noise)
- [Tween UI](/cookbook/tween-ui)
