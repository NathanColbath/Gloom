# Rectangle

`org.llw.render.renderables.Rectangle` draws an axis-aligned rectangle in local space from `(0, 0)` to `(width, height)`. Supports optional filled interior and outlined border (inner ring).

## Key methods

| Method | Description |
|--------|-------------|
| `setSize(float width, float height)` | Local dimensions (default 100×100) |
| `getWidth()` / `getHeight()` | Current size |
| `setFillColor` / `getFillColor` | Interior color when filled |
| `setOutlineColor` / `getOutlineColor` | Border color when outlined |
| `setOutlineThickness` / `getOutlineThickness` | Border width in local pixels (default 1) |
| `setFilled` / `isFilled` | Draw filled quad (default `true`) |
| `setOutlined` / `isOutlined` | Draw inner border ring (default `false`) |
| Transform API | `setPosition`, `setRotation`, `setScale`, `setOrigin` |

## Examples

UI panel:

```java
import org.llw.render.core.Color;
import org.llw.render.renderables.Rectangle;

Rectangle panel = new Rectangle();
panel.setSize(320f, 180f);
panel.setPosition(500f, 200f);
panel.setFillColor(new Color(40, 44, 52, 220));
panel.setOutlined(true);
panel.setOutlineColor(new Color(120, 180, 255));
panel.setOutlineThickness(4f);
gfx.draw(panel);
```

Outline only:

```java
Rectangle frame = new Rectangle();
frame.setSize(200f, 100f);
frame.setFilled(false);
frame.setOutlined(true);
frame.setOutlineThickness(2f);
frame.setOutlineColor(Color.WHITE);
gfx.draw(frame);
```

Higher draw layer (draw above sprites):

```java
import org.llw.render.graphics.DrawState;

gfx.draw(panel, DrawState.DEFAULT.withLayer(10));
```

## Pitfalls

::: warning
Outline is an **inner** ring inset by `outlineThickness` from each edge — it does not expand outside the rectangle bounds. Very thick outlines on small rects can collapse visually.
:::

- Fill uses `PrimitiveType.TRIANGLE_FAN`; outline uses a second fan tracing the inner hole.
- Non-uniform `setScale` stretches the rectangle; rotation pivots around `origin`.
- Rectangle does not use textures — color comes only from fill/outline `Color` values and the shape shader.

::: tip
Combine `setFilled(true)` and `setOutlined(true)` for solid panels with crisp borders in one `draw()` call.
:::

## See also

- [Circle](/render/circle)
- [Color](/render/color)
- [Transformable](/render/transformable)
