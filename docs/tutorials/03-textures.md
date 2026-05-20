# Tutorial 3 — Textures

## Goal

Create GPU-backed 2D images with `Texture2d`, generate placeholder art with `TextureFactory`, and release texture memory with `dispose` when your application shuts down. Textures are the bitmap source that sprites and other renderables sample in later tutorials.

## Prerequisites

Complete [Tutorial 2 — Events & Input](/tutorials/02-events). You should be comfortable with the poll/clear/present loop and closing the window on Escape.

## Step 1 — Window and context (unchanged)

The event loop from tutorial 2 stays the same. Textures are created once before the loop, not every frame.

```java
Window window = new Window(
        new WindowSettings().title("Tutorial 03 — Textures").size(960, 540));
GraphicsContext graphics = new GraphicsContext(window);
```

## Step 2 — Procedural textures with `TextureFactory`

`TextureFactory` builds CPU-side pixel buffers and uploads them to the GPU. It is ideal for debugging visuals when you do not yet have art assets.

```java
import org.llw.render.graphics.Texture2d;
import org.llw.render.graphics.TextureFactory;

Texture2d checker = TextureFactory.checkerboard(256, 256, 32);
```

`checkerboard(width, height, tileSize)` fills the image with alternating colored squares. The factory also offers `solid(IntSize, Color)` for flat fills.

::: tip GPU memory
Every `TextureFactory` method allocates a new OpenGL texture. Keep references only as long as you need them, and call `dispose()` when done.
:::

## Step 3 — Other ways to create `Texture2d`

The `Texture2d` class exposes several static factories for different data sources:

```java
import org.llw.render.core.IntSize;
import org.llw.render.core.Color;

// Empty RGBA buffer (undefined pixels — useful as a render target later)
Texture2d empty = Texture2d.createEmpty(new IntSize(128, 128));

// 1×1 white pixel — handy as a default sampler
Texture2d white = Texture2d.whitePixel();

// Solid color without going through TextureFactory
Texture2d redBlock = TextureFactory.solid(new IntSize(64, 64), Color.RED);
```

To load PNG or JPEG bytes from disk or the classpath, use `Texture2d.fromBytes(byte[])` or `Texture2d.fromMemory(ByteBuffer)`. Images are flipped vertically on decode so pixel rows match LLW's Y-down screen space.

::: details Loading from the classpath
```java
byte[] pngBytes = /* read bytes from resources */;
Texture2d logo = Texture2d.fromBytes(pngBytes);
```
See [Resource Loading](/render/resource-loading) for helpers that integrate with `ResourceLoader`.
:::

## Step 4 — Inspect texture metadata

After creation you can query the OpenGL name and pixel dimensions. You will use `size()` when placing sprites in tutorial 4.

```java
int glId = checker.id();
IntSize pixelSize = checker.size();
System.out.println("Texture " + glId + " is " + pixelSize.width() + "×" + pixelSize.height());
```

Do not call `bind(int unit)` unless you are writing custom GL code — renderables bind textures for you during `graphics.draw()`.

## Step 5 — Dispose textures on shutdown

`Texture2d.dispose()` deletes the GL texture object. It is safe to call multiple times. Dispose textures before or after `graphics.dispose()`, but never draw with a disposed texture.

```java
checker.dispose();
empty.dispose();
white.dispose();
redBlock.dispose();
graphics.dispose();
```

::: warning Leaks add up
A single forgotten texture is small, but reloading levels without disposing old atlases will exhaust GPU memory in a long-running game. Pair every creation path with a matching disposal path.
:::

## Step 6 — Keep the event loop

This tutorial does not draw the texture yet — that is tutorial 4. For now, retain the clear loop so the program still runs interactively:

```java
import org.llw.render.core.Color;
import org.llw.render.window.Key;
import org.llw.render.window.WindowEvent;

while (graphics.isActive()) {
    graphics.pollEvents();
    while (true) {
        var e = window.pollEvent();
        if (e.isEmpty()) break;
        WindowEvent event = e.get();
        if (event instanceof WindowEvent.Closed
                || (event instanceof WindowEvent.KeyPressed kp && kp.key() == Key.ESCAPE)) {
            graphics.window().requestClose();
        }
    }
    graphics.clear(new Color(22, 24, 30));
    graphics.present();
}
```

## Complete example

```java
import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.graphics.Texture2d;
import org.llw.render.graphics.TextureFactory;
import org.llw.render.window.Key;
import org.llw.render.window.Window;
import org.llw.render.window.WindowEvent;
import org.llw.render.window.WindowSettings;

public class Tutorial03Textures {
    public static void main(String[] args) {
        Window window = new Window(
                new WindowSettings().title("Tutorial 03 — Textures").size(960, 540));
        GraphicsContext graphics = new GraphicsContext(window);

        Texture2d checker = TextureFactory.checkerboard(256, 256, 32);
        Texture2d accent = TextureFactory.solid(new IntSize(64, 64), new Color(255, 140, 60));
        Texture2d white = Texture2d.whitePixel();

        System.out.println("Checker: " + checker.size().width() + "×" + checker.size().height()
                + " (GL id " + checker.id() + ")");

        while (graphics.isActive()) {
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

            graphics.clear(new Color(22, 24, 30));
            graphics.present();
        }

        checker.dispose();
        accent.dispose();
        white.dispose();
        graphics.dispose();
    }
}
```

Run the program and confirm the console prints texture dimensions. In the next tutorial you will finally draw `checker` on screen.

## What you learned

- `Texture2d` wraps an OpenGL `GL_TEXTURE_2D` with linear filtering and clamp-to-edge wrapping.
- `TextureFactory` provides quick procedural `checkerboard` and `solid` textures for prototyping.
- Static loaders (`fromBytes`, `fromMemory`, `createEmpty`, `whitePixel`) cover other asset pipelines.
- `size()` and `id()` expose metadata; `dispose()` frees GPU memory.
- Textures are created outside the frame loop and released at shutdown.

::: tip ResourceManager
For games with many assets and level unload, see [Resources Overview](/resources/overview) — `ResourceManager` provides reference-counted `AssetRef<Texture2d>` handles that auto-dispose GPU memory when refs reach zero.
:::

## Next

Continue to [Tutorial 4 — Sprites](/tutorials/04-sprites) to draw a textured quad with `Sprite`, `setTextureRect`, and tinting.
