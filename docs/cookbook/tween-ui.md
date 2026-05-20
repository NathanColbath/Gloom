# Tween UI

## Problem

Menus and HUD elements should slide, fade, or scale smoothly into place instead of popping. Linear motion feels mechanical; designers expect ease-in, ease-out, and smooth acceleration curves.

## Solution

Drive a normalized timer `t` from 0 to 1 over a duration, evaluate an `org.llw.math.interpolation.Easing` curve, then map the eased value into your property range with `org.llw.math.interpolation.Interpolator`.

```java
import org.llw.math.interpolation.Easing;
import org.llw.math.interpolation.Interpolator;
import org.llw.render.renderables.Text;

final class UiTween {
    float elapsed;
    final float duration;
    final float startX, endX;
    final float startY, endY;
    final Easing easing;
    final Text label;
    boolean finished;

    UiTween(Text label, float fromX, float fromY, float toX, float toY,
            float durationSeconds, Easing easing) {
        this.label = label;
        this.startX = fromX;
        this.startY = fromY;
        this.endX = toX;
        this.endY = toY;
        this.duration = durationSeconds;
        this.easing = easing;
    }

    void update(float dt) {
        if (finished) {
            return;
        }
        elapsed += dt;
        float t = Math.min(elapsed / duration, 1f);
        float eased = easing.evaluate(t);

        float x = Interpolator.linear(startX, endX, eased);
        float y = Interpolator.linear(startY, endY, eased);
        label.setPosition(x, y);

        if (t >= 1f) {
            finished = true;
        }
    }
}
```

Use in the frame loop:

```java
UiTween slideIn = new UiTween(
        titleLabel,
        -200f, 80f,   // off-screen left
        480f, 80f,    // resting position
        0.45f,
        Easing.EASE_OUT_CUBIC
);

while (graphics.isActive()) {
    float dt = clock.tick();
    slideIn.update(dt);
    graphics.draw(titleLabel);
    graphics.present();
}
```

**Fade** via tint alpha on a `Sprite` or `Text` color channel:

```java
float alpha = Interpolator.linear(0f, 255f, Easing.EASE_IN_QUAD.evaluate(t));
Color c = label.getFillColor();
label.setFillColor(new Color(c.r, c.g, c.b, (int) alpha));
```

**Smooth without enum:** `Interpolator.smooth(a, b, t)` applies `MathUtils.smoothstep` to `t` for a quick Hermite ease.

Available easing curves: `LINEAR`, `EASE_IN_QUAD`, `EASE_OUT_QUAD`, `EASE_IN_OUT_QUAD`, `EASE_IN_CUBIC`, `EASE_OUT_CUBIC`, `EASE_IN_OUT_CUBIC`. All clamp `t` to [0, 1] inside `evaluate`.

::: details Variations

- **Chained tweens:** Start the next `UiTween` when `finished` becomes true, or queue segments for multi-step intro sequences.
- **Parallel properties:** One timer can lerp position, scale, and rotation by evaluating the same `eased` value against different start/end pairs.
- **Ping-pong:** Use `t = 1f - Math.abs((elapsed / duration) % 2f - 1f)` for menu elements that breathe in and out.
- **Fixed UI timestep:** Update tweens with variable `dt` from `Clock.tick()`; UI animation is usually fine at display rate unlike physics.

:::

## Pitfalls

- **`Easing.apply` is not public:** Call `easing.evaluate(t)` — the enum's `apply` method is internal.
- **Forgetting final snap:** When `t >= 1f`, assign exact `endX`/`endY` if floating-point drift matters for pixel-aligned text.
- **Mixing spaces:** Tween in screen-space UI with a 1:1 camera; world-space HUD needs the same camera as gameplay or positions will slide under zoom.
- **Per-frame allocation:** Reuse one `UiTween` instance and call `elapsed = 0; finished = false` to replay instead of constructing each open.

## See also

- [Easing](/math/easing)
- [Splines & Noise](/math/splines-noise)
- [Scene Stack](/cookbook/scene-stack)
