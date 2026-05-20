# Tutorial 5 — Shapes

## Goal

Complement sprites with immediate-mode vector shapes: filled and outlined `Rectangle` and `Circle` renderables, plus arbitrary geometry through `VertexGeometry`, `Vertex`, and `PrimitiveType`. You will build a simple scene that mixes textures and colored primitives.

## Prerequisites

Complete [Tutorial 4 — Sprites](/tutorials/04-sprites). You should already draw a `Sprite` each frame with `graphics.draw()` and understand transform positioning.

## Step 1 — Rectangle panel

`Rectangle` lives in local space from `(0, 0)` to `(width, height)`. Enable fill and/or outline independently.

```java
import org.llw.render.core.Color;
import org.llw.render.renderables.Rectangle;

Rectangle panel = new Rectangle();
panel.setSize(320f, 180f);
panel.setPosition(120f, 100f);
panel.setFillColor(new Color(40, 44, 52, 220));
panel.setOutlined(true);
panel.setOutlineColor(new Color(120, 180, 255));
panel.setOutlineThickness(4f);
```

`setFilled(false)` would skip the interior while still drawing the border ring when `setOutlined(true)`.

## Step 2 — Circle orb

`Circle` is centered on its local origin `(0, 0)`. Radius is in pixels; non-uniform scale on the transform can stretch it into an ellipse.

```java
import org.llw.render.renderables.Circle;

Circle orb = new Circle();
orb.setRadius(70f);
orb.setPosition(720f, 300f);
orb.setFillColor(new Color(255, 120, 80, 200));
orb.setOutlined(true);
orb.setOutlineColor(Color.WHITE);
orb.setOutlineThickness(6f);
```

Increase `setPointCount(int)` if you need a smoother silhouette (default is 48 segments).

::: tip Layering
Draw opaque panels before translucent orbs so alpha blending reads correctly. Within one frame, later `draw` calls appear above earlier ones.
:::

## Step 3 — Custom mesh with `VertexGeometry`

For triangles, lines, or arbitrary polygons, build a `Vertex` array and choose a `PrimitiveType`. Each vertex carries position, optional UVs, and a per-vertex color.

```java
import org.llw.render.graphics.PrimitiveType;
import org.llw.render.graphics.Vertex;
import org.llw.render.renderables.VertexGeometry;

Vertex[] triangle = {
        new Vertex(600f, 80f, 0f, 0f, new Color(180, 255, 120)),
        new Vertex(750f, 80f, 0f, 0f, new Color(80, 200, 255)),
        new Vertex(675f, 20f, 0f, 0f, new Color(255, 200, 80))
};

VertexGeometry geometry = new VertexGeometry();
geometry.setVertices(triangle);
geometry.setPrimitiveType(PrimitiveType.TRIANGLES);
```

Available primitive types:

| `PrimitiveType` | Typical use |
|-----------------|-------------|
| `POINTS` | Point clouds |
| `LINES` | Independent segments (pairs of vertices) |
| `LINE_STRIP` | Connected polyline |
| `TRIANGLES` | Triangle list (3 vertices per triangle) |
| `TRIANGLE_FAN` | Fan sharing the first vertex (used internally by `Circle` fill) |
| `TRIANGLE_STRIP` | Strip sharing edges between triangles |

::: details Textured `VertexGeometry`
Assign `geometry.setTexture(texture)` to emit textured quads from groups of three vertices. Without a texture, vertices are submitted with `drawVertices` using `getPrimitiveType()`.
:::

## Step 4 — Combine sprites and shapes in one frame

Keep your sprite from tutorial 4 and add shape draw calls after the clear:

```java
import org.llw.render.graphics.TextureFactory;
import org.llw.render.renderables.Sprite;

Texture2d checker = TextureFactory.checkerboard(128, 128, 16);
Sprite icon = new Sprite(checker);
icon.setPosition(40f, 40f);

graphics.clear(new Color(16, 18, 24));
graphics.draw(icon);
graphics.draw(panel);
graphics.draw(orb);
graphics.draw(geometry);
graphics.present();
```

## Step 5 — Animate a shape

Because shapes implement the same `Transformable` interface as sprites, you can move them every frame. Slide the orb horizontally with a sine wave:

```java
float t = clock.elapsedSeconds();
orb.setPosition(720f + (float) Math.sin(t) * 40f, 300f);
```

## Step 6 — Cleanup

Dispose GPU textures you created. Shape renderables do not allocate separate GL objects for fill/outline geometry.

```java
checker.dispose();
graphics.dispose();
```

::: warning Empty geometry
`VertexGeometry` with zero vertices is a no-op at render time. Always set vertices before drawing.
:::

## Complete example

```java
import org.llw.render.core.Clock;
import org.llw.render.core.Color;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.graphics.PrimitiveType;
import org.llw.render.graphics.Texture2d;
import org.llw.render.graphics.TextureFactory;
import org.llw.render.graphics.Vertex;
import org.llw.render.renderables.Circle;
import org.llw.render.renderables.Rectangle;
import org.llw.render.renderables.Sprite;
import org.llw.render.renderables.VertexGeometry;
import org.llw.render.window.Key;
import org.llw.render.window.Window;
import org.llw.render.window.WindowEvent;
import org.llw.render.window.WindowSettings;

public class Tutorial05Shapes {
    public static void main(String[] args) {
        Window window = new Window(
                new WindowSettings().title("Tutorial 05 — Shapes").size(960, 540));
        GraphicsContext graphics = new GraphicsContext(window);
        Clock clock = new Clock();

        Texture2d checker = TextureFactory.checkerboard(128, 128, 16);
        Sprite icon = new Sprite(checker);
        icon.setPosition(40f, 40f);

        Rectangle panel = new Rectangle();
        panel.setSize(320f, 180f);
        panel.setPosition(120f, 100f);
        panel.setFillColor(new Color(40, 44, 52, 220));
        panel.setOutlined(true);
        panel.setOutlineColor(new Color(120, 180, 255));
        panel.setOutlineThickness(4f);

        Circle orb = new Circle();
        orb.setRadius(70f);
        orb.setFillColor(new Color(255, 120, 80, 200));
        orb.setOutlined(true);
        orb.setOutlineColor(Color.WHITE);
        orb.setOutlineThickness(6f);

        Vertex[] triangle = {
                new Vertex(600f, 80f, 0f, 0f, new Color(180, 255, 120)),
                new Vertex(750f, 80f, 0f, 0f, new Color(80, 200, 255)),
                new Vertex(675f, 20f, 0f, 0f, new Color(255, 200, 80))
        };
        VertexGeometry geometry = new VertexGeometry();
        geometry.setVertices(triangle);
        geometry.setPrimitiveType(PrimitiveType.TRIANGLES);

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

            float t = clock.elapsedSeconds();
            orb.setPosition(720f + (float) Math.sin(t) * 40f, 300f);

            graphics.clear(new Color(16, 18, 24));
            graphics.draw(icon);
            graphics.draw(panel);
            graphics.draw(orb);
            graphics.draw(geometry);
            graphics.present();
        }

        checker.dispose();
        graphics.dispose();
    }
}
```

You should see a checker icon, a outlined panel, a bouncing translucent orb, and a gradient triangle header.

## What you learned

- `Rectangle` draws axis-aligned fills and optional inner outline rings via `setSize`, `setFillColor`, and `setOutlined`.
- `Circle` approximates a disk with a triangle fan; outline uses a triangle strip ring.
- `Vertex` packs position, UV, and color; `VertexGeometry` submits arbitrary primitives.
- `PrimitiveType` selects how the GPU interprets vertex lists (`TRIANGLES`, `LINE_STRIP`, etc.).
- Sprites and shapes share the same `graphics.draw()` path and transform API.
- Dispose textures explicitly; shape geometry is generated each frame and does not need separate disposal.

## Next

Continue to [Tutorial 6 — Text](/tutorials/06-text) to render strings with `Font` and the `Text` renderable.
