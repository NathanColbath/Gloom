# Text & Fonts

Text rendering uses FreeType (via LWJGL) to rasterize glyphs into a texture atlas. `org.llw.render.renderables.Text` draws strings with a loaded `org.llw.render.graphics.Font`.

## In this section

| Topic | Page |
|-------|------|
| Fonts & `Text` | [Text & Fonts](/render/text-and-fonts) (this page) |
| Drawable overview | [Renderables](/render/renderables) |
| Colors | [Color](/render/color) |
| Draw layers | [Draw State](/render/draw-state) |
| Classpath TTF paths | [Resource Loading](/render/resource-loading) |
| Sprite shader path | [Shaders](/render/shaders) |

## Loading a font

```java
import org.llw.render.graphics.Font;

Font font = Font.fromClasspath("llw/render/fonts/Roboto-Regular.ttf", 28);
```

The `pixelHeight` parameter sets the rasterized glyph size in pixels. `fromClasspath` loads bytes internally via `ResourceLoader`.

From a filesystem path:

```java
Font font = Font.fromFile(Path.of("assets/ui.ttf"), 28);
```

## System fonts (Windows)

`ResourceManager` auto-indexes installed Windows fonts when constructed. Acquire by **family name and pixel size**:

```java
import org.llw.render.graphics.FontStyle;
import org.llw.resources.AssetRef;
import org.llw.resources.ResourceManager;

ResourceManager resources = new ResourceManager(gfx.backend(), audio);

AssetRef<Font> ui = resources.acquireSystemFont("Segoe UI", 28);
Text label = new Text(ui.get());

AssetRef<Font> heading = resources.acquireSystemFont("Segoe UI", FontStyle.BOLD, 36);
```

Styled catalog keys use suffixes: `.bold`, `.italic`, `.bold-italic`. List available faces with `resources.systemFontFaces()`.

On unsupported platforms or missing faces, fall back to a bundled classpath font via `registerFont`.

## Drawing text

```java
import org.llw.render.core.Color;
import org.llw.render.renderables.Text;

Text text = new Text(font);
text.setContent("Score: 42");
text.setPosition(20f, 20f);
text.setFillColor(Color.WHITE);
gfx.draw(text);
```

Use a higher `DrawState` layer if text must draw above other geometry:

```java
import org.llw.render.graphics.DrawState;

gfx.draw(text, DrawState.DEFAULT.withLayer(5));
```

## Transform & style

`Text` extends `AbstractTransformable` — position, rotation, scale, and origin behave like [Transformable](/render/transformable) sprites.

```java
text.setOrigin(0f, 0f);           // top-left anchor (typical for HUD)
text.setFillColor(new Color(255, 220, 100));
text.setContent("Wave " + waveNumber);
```

## Cleanup

```java
font.dispose();
```

Disposing a font invalidates glyph data for dependent `Text` instances.

## Common pitfalls

::: warning
Font loading throws if the classpath path is wrong — validate paths in development and guard optional UI text in production builds.
:::

- Very long strings are not auto-wrapped; split lines manually or insert `\n` if your UI layer supports it.
- Disposing the font while `Text` objects still reference it produces missing glyphs.
- Text uses the sprite shader/batch path — custom shaders must remain compatible with textured quads.

::: tip
Load one `Font` per size/style at startup and reuse across many `Text` instances rather than reloading per label.
:::

## See also

- [Renderables](/render/renderables)
- [Graphics Context](/render/graphics-context)
- [Resource Loading](/render/resource-loading)
