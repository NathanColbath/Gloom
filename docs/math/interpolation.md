# Interpolation

`org.llw.math.interpolation` provides scalar blending utilities. Vector interpolation lives on `Vector2f.lerp`.

## Interpolator

```java
import org.llw.math.interpolation.Interpolator;

float value = Interpolator.linear(0f, 100f, 0.25f);   // 25
float smooth = Interpolator.smooth(0f, 100f, 0.25f);  // smoothstep-shaped
```

`smooth` applies `MathUtils.smoothstep` to `t` before lerping — handy for fades without picking an easing enum.

## Vector lerp

```java
Vector2f a = new Vector2f(0f, 0f);
Vector2f b = new Vector2f(100f, 50f);
Vector2f mid = Vector2f.lerp(a, b, 0.5f);
```

## MathUtils companions

```java
import org.llw.math.util.MathUtils;

float t = MathUtils.inverseLerp(0f, 100f, 40f);           // 0.4
float mapped = MathUtils.remap(speed, 0f, 10f, 0f, 1f);
float easedT = MathUtils.smoothstep(0.3f);
```

## Animation over time

```java
float duration = 0.4f;
elapsed += dt;
float t = MathUtils.clamp(elapsed / duration, 0f, 1f);
float alpha = Interpolator.linear(0f, 1f, Easing.EASE_OUT_QUAD.evaluate(t));
sprite.setOpacity(alpha);
```

Combine normalized time `t` with [Easing](/math/easing) curves for UI and camera motion — see [Tween UI](/cookbook/tween-ui).

::: tip Fixed timestep
Advance interpolation timers inside your fixed update when physics must be deterministic; render can lerp between previous and current state for smooth visuals.
:::

## See also

- [Easing](/math/easing)
- [Splines & Noise](/math/splines-noise)
- [Fixed Timestep](/cookbook/fixed-timestep)
