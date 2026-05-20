# Tutorial 4 — Sprites

## Goal

Draw a textured quad on screen with `Sprite`, position it in world space, crop the source image with `setTextureRect`, and multiply colors with `setTint`. You will connect the textures from tutorial 3 to the render path via `graphics.draw()`.

## Prerequisites

Complete [Tutorial 3 — Textures](/tutorials/03-textures). You should have a `Texture2d` (for example a checkerboard) and know to call `dispose()` when finished.

## Step 1 — Create a texture and sprite

A `Sprite` references a `Texture2d` and builds a quad from `(0, 0)` to the texture's pixel width and height in local space. The transform (position, rotation, scale, origin) comes from `AbstractTransformable`.

```java
import org.llw.render.graphics.TextureFactory;
import org.llw.render.graphics.Texture2d;
import org.llw.render.renderables.Sprite;

Texture2d checker = TextureFactory.checkerboard(256, 256, 32);
Sprite sprite = new Sprite(checker);
sprite.setPosition(200f, 150f);
```

Coordinates are in pixels with Y-down: `(200, 150)` places the sprite's local origin near the upper-left area of a 960×540 window.

## Step 2 — Draw each frame

After clearing the framebuffer, submit the sprite to the graphics context. `draw` queues the renderable; `present` flushes the batch and swaps buffers.

```java
import org.llw.render.core.Color;

graphics.clear(new Color(18, 20, 28));
graphics.draw(sprite);
graphics.present();
```

::: tip Draw order
Later draw calls appear on top of earlier ones within the same frame. Clear first, then draw background sprites, then foreground elements.
:::

## Step 3 — Crop with `setTextureRect`

By default a sprite samples the full texture (UV rectangle `0, 0, 1, 1`). Use `RectF` to select a normalized sub-region — values are fractions of the texture width and height, not pixels.

```java
import org.llw.math.geometry.RectF;

// Top-left quarter of the atlas
sprite.setTextureRect(new RectF(0f, 0f, 0.5f, 0.5f));
```

The on-screen size shrinks to match the cropped region: a 256×256 texture cropped to half width and height draws as a 128×128 quad (unless you scale the sprite).

::: details UV space
`left` and `top` are the normalized coordinates of the top-left corner of the sample region. `width` and `height` are the normalized size. `top` increases downward, consistent with LLW's Y-down convention.
:::

## Step 4 — Tint with `setTint`

`tint` multiplies the texture color per vertex. White (`Color.WHITE`) leaves the texture unchanged; semi-transparent tints fade the sprite; solid colors can recolor grayscale art.

```java
sprite.setTint(new Color(255, 200, 120, 220));
```

Use alpha below `255` for transparency. The default tint is `Color.WHITE`.

## Step 5 — Animate rotation (optional)

Sprites inherit `setRotation(float radians)` from `Transformable`. A slow spin makes it obvious that drawing happens every frame:

```java
float timeSeconds = /* accumulate delta time, or use a frame counter */;
sprite.setRotation(timeSeconds * 0.5f);
```

Set the origin if you want rotation around the texture center instead of the top-left corner:

```java
sprite.setOrigin(checker.size().width() / 2f, checker.size().height() / 2f);
sprite.setPosition(480f, 270f); // window center for 960×540
```

## Step 6 — Dispose resources

Dispose the texture when shutting down. The sprite itself does not own the texture — it only holds a reference.

```java
checker.dispose();
graphics.dispose();
```

::: warning Null texture
If you construct `new Sprite(null)`, `render` is a no-op. Always assign a live texture before drawing.
:::

## Complete example

Two sprites share one checkerboard atlas: one draws the full image with a warm tint; the other shows the top-left quarter, rotated about its center.

```java
import org.llw.math.geometry.RectF;
import org.llw.render.core.Clock;
import org.llw.render.core.Color;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.graphics.Texture2d;
import org.llw.render.graphics.TextureFactory;
import org.llw.render.renderables.Sprite;
import org.llw.render.window.Key;
import org.llw.render.window.Window;
import org.llw.render.window.WindowEvent;
import org.llw.render.window.WindowSettings;

public class Tutorial04Sprites {
    public static void main(String[] args) {
        Window window = new Window(
                new WindowSettings().title("Tutorial 04 — Sprites").size(960, 540));
        GraphicsContext graphics = new GraphicsContext(window);
        Clock clock = new Clock();

        Texture2d checker = TextureFactory.checkerboard(256, 256, 32);

        Sprite full = new Sprite(checker);
        full.setPosition(80f, 80f);
        full.setTint(new Color(255, 220, 180, 255));

        Sprite cropped = new Sprite(checker);
        cropped.setTextureRect(new RectF(0f, 0f, 0.5f, 0.5f));
        cropped.setOrigin(64f, 64f); // half of 128×128 cropped size
        cropped.setPosition(480f, 270f);
        cropped.setTint(new Color(140, 200, 255, 230));

        while (graphics.isActive()) {
            clock.tick();
            graphics.pollEvents();

            while (true) {
                var optionalEvent = window.pollEvent();
                if (optionalEvent.isEmpty()) {
                    break;
                }
                WindowEvent event = optionalEvent.get();
                if (event instanceof WindowEvent.Closed) {
                    graphics.window().requestClose();
                } else if (event instanceof WindowEvent.KeyPressed keyPressed
                        && keyPressed.key() == Key.ESCAPE) {
                    graphics.window().requestClose();
                }
            }

            cropped.setRotation(clock.elapsedSeconds() * 0.8f);

            graphics.clear(new Color(18, 20, 28));
            graphics.draw(full);
            graphics.draw(cropped);
            graphics.present();
        }

        checker.dispose();
        graphics.dispose();
    }
}
```

## What you learned

- `Sprite` draws a textured quad sized from the active texture and `textureRect`.
- `setPosition`, `setRotation`, `setOrigin`, and `setScale` control the model transform.
- `setTextureRect(RectF)` crops normalized UVs within the atlas.
- `setTint(Color)` multiplies texture colors, including alpha for transparency.
- `graphics.draw(sprite)` queues geometry; pair with `clear` and `present` each frame.
- Textures must still be `dispose()`d separately from sprites.

## Next

Continue to [Tutorial 5 — Shapes](/tutorials/05-shapes) to add vector rectangles, circles, and custom `VertexGeometry`.
