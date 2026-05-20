# Sprite

`org.llw.render.renderables.Sprite` draws a textured axis-aligned quad. It extends `AbstractTransformable` and implements `Renderable`. Local geometry spans `(0, 0)` to `(uvWidth × texWidth, uvHeight × texHeight)` in pixels.

## Key methods

| Method | Description |
|--------|-------------|
| `Sprite(Texture2d texture)` | Creates sprite; `null` texture makes `render` a no-op |
| `getTexture()` / `setTexture(Texture2d)` | Source texture when no `DrawState` override |
| `getTextureRect()` / `setTextureRect(RectF)` | Normalized UV rectangle `[0, 1]` as left, top, width, height |
| `getTint()` / `setTint(Color)` | Per-vertex color multiplier (default `Color.WHITE`) |
| `setPosition`, `setRotation`, `setScale`, `setOrigin` | From `Transformable` |

## Examples

Basic sprite:

```java
import org.llw.render.graphics.TextureFactory;
import org.llw.render.renderables.Sprite;
import org.llw.math.geometry.RectF;

Texture2d tex = TextureFactory.checkerboard(128, 128, 16);
Sprite sprite = new Sprite(tex);
sprite.setPosition(100f, 80f);
sprite.setScale(2f, 2f);
gfx.draw(sprite);
```

Atlas sub-rectangle (normalized UVs):

```java
// Sample the top-left quarter of the atlas
sprite.setTextureRect(new RectF(0f, 0f, 0.5f, 0.5f));
```

Centered rotation:

```java
IntSize size = tex.size();
sprite.setOrigin(size.width() / 2f, size.height() / 2f);
sprite.setPosition(640f, 360f);
sprite.setRotation((float) Math.toRadians(45));
```

Tint and `DrawState` texture override:

```java
import org.llw.render.graphics.DrawState;

sprite.setTint(new Color(255, 220, 180).withAlpha(200));

// Draw many sprites with one shared atlas bound via DrawState
gfx.draw(sprite, DrawState.DEFAULT.withTexture(sharedAtlas));
```

When `DrawState.texture()` is set, that texture **and its dimensions** drive quad size instead of the sprite's own texture.

## Pitfalls

::: warning
If `texture` is `null`, `render` returns immediately without drawing — even if `DrawState` carries a texture override. Always assign a non-null texture to the sprite, or use `DrawState.withTexture()` only when the sprite already has a texture set.
:::

Actually wait - let me re-read Sprite.render:

```java
if (texture == null) {
    return;
}
...
Texture2d activeTexture = state.texture() != null ? state.texture() : texture;
```

So if texture is null, it returns early BEFORE checking state.texture(). That's a pitfall - DrawState override won't help if sprite texture is null.

- UV rect uses **normalized** coordinates; pixel rects must be divided by texture size first.
- `setTextureRect` copies fields into an internal `RectF` — mutating the passed rect afterward does not affect the sprite.
- Parent transforms from `DrawState.withTransform()` multiply on the left of the sprite's local transform.

::: tip
Set origin to the visual pivot (center of character, base of tree) before applying rotation or non-uniform scale.
:::

## See also

- [Textures](/render/textures)
- [Draw State](/render/draw-state)
- [Transformable](/render/transformable)
