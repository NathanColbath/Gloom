# Color

`org.llw.render.core.Color` is an immutable RGBA value with 8-bit channels in `[0, 255]`. Constructors clamp out-of-range components. Use normalized accessors when feeding shaders or floating-point math.

## Constants

| Constant | RGBA | Use |
|----------|------|-----|
| `BLACK` | 0, 0, 0, 255 | Opaque black |
| `WHITE` | 255, 255, 255, 255 | Opaque white |
| `RED` / `GREEN` / `BLUE` | primary channels | Debug tints |
| `TRANSPARENT` | 0, 0, 0, 0 | Fully transparent |

## Key methods

| Method | Description |
|--------|-------------|
| `Color(int r, int g, int b)` | Opaque RGB (alpha = 255) |
| `Color(int r, int g, int b, int a)` | Full RGBA |
| `withAlpha(int alpha)` | Same RGB, new alpha |
| `rNorm()`, `gNorm()`, `bNorm()`, `aNorm()` | Channels as `float` in `[0, 1]` |
| `toRgbaBits()` | Packed ARGB `int` (alpha in high byte) |
| `r`, `g`, `b`, `a` | Public final channel fields |

## Examples

Clear color and UI panel:

```java
import org.llw.render.core.Color;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.renderables.Rectangle;

gfx.clear(new Color(18, 20, 28));

Rectangle panel = new Rectangle();
panel.setFillColor(new Color(40, 44, 52, 220));  // translucent
panel.setOutlineColor(new Color(120, 180, 255));
```

Sprite tint and procedural texture:

```java
import org.llw.render.graphics.TextureFactory;
import org.llw.render.core.IntSize;

sprite.setTint(new Color(255, 200, 180).withAlpha(180));

Texture2d redTile = TextureFactory.solid(new IntSize(64, 64), Color.RED);
```

Normalized values for custom shader uniforms:

```java
float[] rgba = { color.rNorm(), color.gNorm(), color.bNorm(), color.aNorm() };
```

## Pitfalls

::: warning
`Color` channels are **bytes**, not floats. Passing `0.5f` to the constructor is invalid — use `new Color(128, 128, 128)` or scale normalized values to 255 first.
:::

- `toRgbaBits()` packs **ARGB**, not RGBA — match this layout if you interoperate with Java `BufferedImage` or other ARGB APIs.
- Tint on `Sprite` multiplies texture samples per vertex; white (`Color.WHITE`) leaves the texture unchanged.
- Alpha blending requires `BlendMode.ALPHA` (the `DrawState` default) and geometry drawn in sensible layer order.

::: tip
Use `withAlpha()` instead of allocating `new Color(r, g, b, a)` when you only need to adjust transparency on an existing color.
:::

## See also

- [Draw State](/render/draw-state) — `BlendMode` and layering
- [Sprite](/render/sprite) — per-draw tint
- [Rectangle](/render/rectangle) — fill and outline colors
