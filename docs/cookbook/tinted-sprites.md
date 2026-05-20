# Tinted Sprites

## Problem

You want to recolor sprites at runtime — team colors, damage flashes, night tints, or fade-out effects — without authoring duplicate textures.

## Solution

`Sprite.setTint` multiplies the texture sample per vertex. White `(255, 255, 255)` leaves the bitmap unchanged; other RGB values tint; alpha scales opacity.

```java
import org.llw.math.util.MathUtils;
import org.llw.render.core.Color;
import org.llw.render.renderables.Sprite;

Sprite ally = new Sprite(texture);
ally.setPosition(300f, 200f);

// Static team tint
ally.setTint(new Color(120, 200, 255)); // cool blue highlight

// Damage flash toward red
ally.setTint(new Color(255, 80, 80));

// Semi-transparent ghost
ally.setTint(new Color(255, 255, 255, 128));
```

**Fade out over time** — lerp alpha each frame (colors are immutable; build a new `Color`):

```java
float fadeT = 0f; // 0 = visible, 1 = gone

while (gfx.isActive()) {
    float dt = clock.tick();
    fadeT = Math.min(1f, fadeT + dt * 0.5f);

    int alpha = (int) MathUtils.lerp(255f, 0f, fadeT);
    ally.setTint(new Color(255, 255, 255, alpha));

    if (fadeT >= 1f) {
        // stop drawing or respawn
    }

    gfx.clear(background);
    gfx.draw(ally);
    gfx.present();
}
```

**Fade between two tints** — lerp each channel for smooth heat-to-cool transitions:

```java
Color hot  = new Color(255, 200, 80);
Color cold = new Color(80, 160, 255);
float t = 0.35f;

int r = (int) MathUtils.lerp(hot.r, cold.r, t);
int g = (int) MathUtils.lerp(hot.g, cold.g, t);
int b = (int) MathUtils.lerp(hot.b, cold.b, t);
ally.setTint(new Color(r, g, b));
```

::: details Variations

**Pulsing highlight** — drive alpha with a sine wave:

```java
float pulse = (float) (0.5 + 0.5 * Math.sin(clock.elapsedSeconds() * 4));
int a = (int) (128 + 127 * pulse);
ally.setTint(new Color(255, 255, 100, a));
```

**Multiply vs replace** — darkening uses low RGB without changing alpha:

```java
ally.setTint(new Color(64, 64, 64)); // night mode dim
```

**Per-draw override** — `DrawState` does not carry tint; use separate sprite instances or swap tint before enqueueing.

**White flash frame** — full white with high alpha reads as a brief hit flash on any texture.

**Restore default**:

```java
ally.setTint(Color.WHITE);
```

:::

## Pitfalls

- **Tint is multiplicative** — black tint `(0, 0, 0)` silences the texture entirely; there is no additive glow without custom shaders.
- **`Color` is immutable** — `withAlpha` returns a new color; calling it without assigning to `setTint` has no effect.
- **Premultiplied expectations** — the default alpha blend expects straight alpha; very low alpha on bright tints can look washed out.
- **Forgotten reset** — reuse pooled sprites only after resetting tint to `Color.WHITE`.
- **Text and shapes** — `Rectangle` and `Text` use fill colors, not `setTint`; the tint API is specific to `Sprite`.

## See also

- [Sprite](/render/sprite)
- [Color](/render/color)
- [Interpolation](/math/interpolation)
- [Sprite Sheet](/cookbook/sprite-sheet)
