# DrawState

`org.llw.render.graphics.DrawState` is an immutable record describing per-draw overrides applied when a `Renderable` is flushed from a `RenderTarget` queue. Derive variants with `with*` methods; start from `DrawState.DEFAULT`.

## Fields & defaults

| Field | `DEFAULT` value | Purpose |
|-------|-----------------|---------|
| `blendMode` | `BlendMode.ALPHA` | Fragment blending equation |
| `texture` | `null` | Override sprite/geometry texture |
| `shader` | `null` | Override program (`null` → backend default) |
| `transform` | identity `Matrix3x2` | Parent transform multiplied with renderable |
| `layer` | `0` | Sort key — lower layers draw first |

## Key methods

| Method | Description |
|--------|-------------|
| `withBlendMode(BlendMode)` | Copy with new blend mode |
| `withTexture(Texture2d)` | Copy with texture override |
| `withShader(ShaderProgram)` | Copy with shader override |
| `withTransform(Matrix3x2)` | Copy with transform override |
| `withLayer(int)` | Copy with draw layer |
| `combineTransform(Matrix3x2 local)` | Parent × local (local applied first) |
| `sortKey(int submissionOrder)` | Internal stable sort key for the queue |

## BlendMode values

| Mode | Effect |
|------|--------|
| `ALPHA` | Standard src-alpha over dst |
| `ADDITIVE` | Add source color to destination |
| `MULTIPLY` | Multiply source and destination |
| `NONE` | Replace destination (no blending) |

## Examples

Layered HUD over world:

```java
import org.llw.render.graphics.BlendMode;
import org.llw.render.graphics.DrawState;

gfx.draw(worldSprite);  // layer 0 (default)

gfx.draw(hudPanel, DrawState.DEFAULT.withLayer(10));
gfx.draw(hudLabel, DrawState.DEFAULT.withLayer(11));
```

Additive glow:

```java
gfx.draw(glowSprite, DrawState.DEFAULT
        .withBlendMode(BlendMode.ADDITIVE)
        .withLayer(5));
```

Parent transform for grouped objects:

```java
import org.llw.math.matrix.Matrix3x2;

Matrix3x2 group = new Matrix3x2().translate(400f, 300f).rotate(0.2f);
DrawState groupState = DrawState.DEFAULT.withTransform(group);

gfx.draw(childA, groupState);
gfx.draw(childB, groupState.combineTransform(childB.getTransform()));
```

Custom shader pass:

```java
gfx.draw(sprite, DrawState.DEFAULT.withShader(waveShader));
```

## Sort order

The draw queue sorts by `sortKey(submissionOrder)`:

1. `layer` (ascending)
2. `submissionOrder` (stable FIFO within layer)
3. `shader` program id
4. `texture` id
5. `blendMode` ordinal

::: tip
Keep UI on higher layers than gameplay (e.g. world `0`, panels `10`, text `11`) so submission order among sprites does not bury HUD elements.
:::

## Pitfalls

::: warning
`draw(Renderable)` does not hit the GPU until `present()` or `flush()`. `DrawState` only affects the queued flush, not immediate GL state.
:::

- `DrawState` records are immutable — each `with*` allocates a new instance.
- Renderables combine `state.transform()` with their own `getTransform()` as `parent × local`.
- Texture override on `DrawState` does not help `Sprite` instances whose own `texture` field is `null` (early exit in `Sprite.render`).

## See also

- [Render Target](/render/render-target)
- [Shaders](/render/shaders)
- [Sprite](/render/sprite)
