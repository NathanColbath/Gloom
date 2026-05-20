# Textures

`org.llw.render.graphics.Texture2d` wraps an OpenGL `GL_TEXTURE_2D` RGBA8 texture with linear filtering and clamp-to-edge wrapping. `TextureFactory` builds procedural placeholder textures for debugging.

## Texture2d — key methods

| Method | Description |
|--------|-------------|
| `createEmpty(IntSize)` | Uninitialized RGBA texture of given size |
| `fromRgbaPixels(int w, int h, ByteBuffer)` | Upload tightly packed RGBA8 bytes |
| `fromMemory(ByteBuffer)` / `fromBytes(byte[])` | Decode PNG/JPEG/etc. via STB (flipped vertically) |
| `fromRaw(int textureId, IntSize)` | Wrap an existing GL texture name |
| `whitePixel()` | Shared 1×1 opaque white texture |
| `id()` | OpenGL texture object name |
| `size()` | `IntSize` width and height in pixels |
| `bind(int unit)` | Activate on texture unit (`0` = `GL_TEXTURE0`) |
| `dispose()` | Delete GL object (idempotent) |

## TextureFactory — key methods

| Method | Description |
|--------|-------------|
| `checkerboard(int w, int h, int tileSize)` | Two-tone debug pattern |
| `solid(IntSize, Color)` | Uniform RGBA fill |

## Examples

Load from classpath bytes:

```java
import org.llw.render.graphics.Texture2d;
import org.llw.render.resources.ResourceLoader;
import org.llw.render.renderables.Sprite;

byte[] png = ResourceLoader.loadBytes("assets/sprites/player.png");
Texture2d playerTex = Texture2d.fromBytes(png);
Sprite player = new Sprite(playerTex);
```

Procedural placeholders:

```java
import org.llw.render.graphics.TextureFactory;
import org.llw.render.core.Color;
import org.llw.render.core.IntSize;

Texture2d grid = TextureFactory.checkerboard(256, 256, 32);
Texture2d sky = TextureFactory.solid(new IntSize(1, 1), new Color(135, 206, 235));
```

Raw pixel upload (direct buffer required):

```java
import java.nio.ByteBuffer;

ByteBuffer rgba = ByteBuffer.allocateDirect(4 * 4 * 4);
// ... fill width * height * 4 bytes ...
rgba.flip();
Texture2d icon = Texture2d.fromRgbaPixels(4, 4, rgba);
```

Cleanup:

```java
playerTex.dispose();
```

## Pitfalls

::: warning
Call `dispose()` when a texture is no longer referenced. Leaked `Texture2d` objects keep GPU memory until the GL context is destroyed.
:::

- STB decoding in `fromMemory` / `fromBytes` **flips images vertically** so pixel data matches Y-down screen space.
- `fromRgbaPixels` expects a **direct** `ByteBuffer` with exactly `width * height * 4` bytes.
- Do not bind or draw with a texture after `dispose()`.
- `Sprite.render` is a no-op when both the sprite texture and any `DrawState` texture override are null.

::: tip
`Texture2d.whitePixel()` is useful as a stand-in when you need a valid sampler for untextured batched paths that still expect a bound texture.
:::

## See also

- [Sprite](/render/sprite) — textured quads and UV rects
- [Resource Loading](/render/resource-loading)
- [Offscreen Rendering](/render/offscreen) — `OffscreenTarget.colorTexture()`
