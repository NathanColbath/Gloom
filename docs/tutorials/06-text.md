# Tutorial 6 — Text

## Goal

Load a TrueType font from the classpath, draw a `Text` renderable on screen, and use `DrawState.withLayer` so labels always appear above filled shapes.

## Prerequisites

- Completed [Tutorial 5 — Shapes](/tutorials/05-shapes) (window, clear/draw/present loop, `Rectangle` / `Circle`)
- Familiarity with [Color](/render/color) and the [Graphics Context](/render/graphics-context)

::: details What you already know from earlier tutorials
You can open a window, poll events, clear the framebuffer, draw vector shapes, and call `present()` each frame. Text builds on that same loop — fonts are just another GPU resource like textures.
:::

## Step 1 — Load a font from the classpath

LLW rasterizes glyphs with FreeType (via LWJGL) into a texture atlas. The bundled Roboto font ships inside the `llw` module:

```java
import org.llw.render.graphics.Font;

Font font = Font.fromClasspath("llw/render/fonts/Roboto-Regular.ttf", 28);
```

The second argument is the **pixel height** of rasterized glyphs. Larger values produce sharper text at the cost of atlas memory.

::: tip Choosing a size
`24`–`32` works well for HUD labels at 1080p. Use one font size per `Font` instance; scaling a `Text` object with `setScale` works but can look soft because glyphs are bitmaps.
:::

## Step 2 — Create and configure `Text`

`Text` extends `AbstractTransformable`, so it supports position, rotation, scale, and origin like sprites and shapes:

```java
import org.llw.render.core.Color;
import org.llw.render.renderables.Text;

Text scoreLabel = new Text(font);
scoreLabel.setContent("Score: 0");
scoreLabel.setPosition(24f, 24f);
scoreLabel.setFillColor(Color.WHITE);
```

Content is drawn starting at local `(0, 0)`. Newline characters advance to the next line using the font's line height. LLW does not auto-wrap long strings — split lines yourself if needed.

## Step 3 — Draw shapes, then text on a higher layer

The render queue sorts draws by **layer** (lower first). Shapes and sprites submitted without a custom `DrawState` use layer `0`. If you draw a panel and then text at the same layer, submission order decides who wins — fragile when you refactor.

Draw the background panel first, then pass an explicit layer for text:

```java
import org.llw.render.graphics.DrawState;
import org.llw.render.renderables.Rectangle;

Rectangle panel = new Rectangle();
panel.setSize(220f, 56f);
panel.setPosition(12f, 12f);
panel.setFillColor(new Color(30, 34, 44, 230));

gfx.clear(new Color(18, 20, 28));
gfx.draw(panel);
gfx.draw(scoreLabel, DrawState.DEFAULT.withLayer(5));
gfx.present();
```

::: warning Layer only affects sort order
`withLayer` does not clip or mask text to the panel. It only guarantees the label is flushed after lower-layer geometry. For a text box effect, draw the panel on layer `0` and the label on layer `5` as shown.
:::

## Step 4 — Update text at runtime

Treat `Text` like any other renderable: mutate it during the update phase, then redraw each frame.

```java
int score = 0;

while (gfx.isActive()) {
    gfx.pollEvents();
    // ... game logic may change score ...
    scoreLabel.setContent("Score: " + score);

    gfx.clear(new Color(18, 20, 28));
    gfx.draw(panel);
    gfx.draw(scoreLabel, DrawState.DEFAULT.withLayer(5));
    gfx.present();
}
```

## Step 5 — Dispose the font

`Font` owns the glyph atlas texture. Dispose it after the render loop:

```java
font.dispose();
gfx.dispose();
```

Disposing a font invalidates any `Text` that still references it — dispose fonts only at shutdown.

## Full class

```java
import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.Font;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.renderables.Rectangle;
import org.llw.render.renderables.Text;
import org.llw.render.window.Window;
import org.llw.render.window.WindowSettings;

public class TextTutorial {
    public static void main(String[] args) {
        Window window = new Window(new WindowSettings().title("LLW Text").size(800, 600));
        GraphicsContext gfx = new GraphicsContext(window);

        Font font = Font.fromClasspath("llw/render/fonts/Roboto-Regular.ttf", 28);

        Rectangle panel = new Rectangle();
        panel.setSize(220f, 56f);
        panel.setPosition(12f, 12f);
        panel.setFillColor(new Color(30, 34, 44, 230));

        Text scoreLabel = new Text(font);
        scoreLabel.setContent("Score: 0");
        scoreLabel.setPosition(24f, 24f);
        scoreLabel.setFillColor(Color.WHITE);

        int score = 0;
        float timer = 0f;

        while (gfx.isActive()) {
            gfx.pollEvents();
            timer += 1f / 60f;
            if (timer >= 1f) {
                timer = 0f;
                score++;
                scoreLabel.setContent("Score: " + score);
            }

            gfx.clear(new Color(18, 20, 28));
            gfx.draw(panel);
            gfx.draw(scoreLabel, DrawState.DEFAULT.withLayer(5));
            gfx.present();
        }

        font.dispose();
        gfx.dispose();
    }
}
```

## What you learned

- `Font.fromClasspath(path, pixelHeight)` builds a bitmap atlas from a `.ttf` on the classpath.
- `Text` displays a string with `setContent`, `setPosition`, and `setFillColor`.
- `gfx.draw(renderable, DrawState.DEFAULT.withLayer(n))` controls draw order without changing the renderable itself.
- Fonts must be `dispose()`d; keep font lifetime aligned with shutdown.

**Next:** [Tutorial 7 — Transforms](/tutorials/07-transforms)
