# Renderables

Renderables are objects that emit GPU geometry when the draw queue is flushed. Each implements `Renderable.render(OpenGlBackend, DrawState)`.

## In this section

| Topic | Page |
|-------|------|
| Drawable overview | [Renderables](/render/renderables) (this page) |
| Textured quads | [Sprite](/render/sprite) |
| Axis-aligned rects | [Rectangle](/render/rectangle) |
| Circles & rings | [Circle](/render/circle) |
| Custom `Vertex[]` | [Vertex Geometry](/render/vertex-geometry) |
| Position / rotation / scale | [Transformable](/render/transformable) |
| Text rendering | [Text & Fonts](/render/text-and-fonts) |

## Built-in types

| Class | Description |
|-------|-------------|
| `Sprite` | Textured quad; UV rect and tint |
| `Rectangle` | Filled/outlined axis-aligned rectangle |
| `Circle` | Filled/outlined circle (triangle fan / strip) |
| `Text` | FreeType-rasterized string via `Font` |
| `VertexGeometry` | Arbitrary colored or textured vertices |

All transformable renderables extend `AbstractTransformable` and implement `Transformable`.

## Sprite

```java
import org.llw.render.graphics.TextureFactory;
import org.llw.render.renderables.Sprite;

Texture2d tex = TextureFactory.checkerboard(256, 256, 32);
Sprite sprite = new Sprite(tex);
sprite.setPosition(200f, 150f);
sprite.setRotation(0.5f);
sprite.setScale(2f, 2f);
sprite.setOrigin(tex.size().width() / 2f, tex.size().height() / 2f);
gfx.draw(sprite);
```

See [Sprite](/render/sprite).

## Rectangle & Circle

```java
import org.llw.render.core.Color;
import org.llw.render.renderables.Circle;
import org.llw.render.renderables.Rectangle;

Rectangle panel = new Rectangle();
panel.setSize(320f, 180f);
panel.setPosition(500f, 200f);
panel.setFillColor(new Color(40, 44, 52, 220));
panel.setOutlined(true);
panel.setOutlineColor(new Color(120, 180, 255));
panel.setOutlineThickness(4f);

Circle orb = new Circle();
orb.setRadius(70f);
orb.setPosition(900f, 360f);
orb.setFillColor(new Color(255, 120, 80, 200));
```

## Text

```java
import org.llw.render.graphics.Font;
import org.llw.render.renderables.Text;

Font font = Font.fromClasspath("llw/render/fonts/Roboto-Regular.ttf", 28);
Text label = new Text(font);
label.setContent("Hello LLW");
label.setPosition(100f, 50f);
label.setFillColor(Color.WHITE);
gfx.draw(label);
```

## Custom geometry

```java
import org.llw.render.core.Color;
import org.llw.render.graphics.PrimitiveType;
import org.llw.render.graphics.Vertex;
import org.llw.render.renderables.VertexGeometry;

Vertex[] tri = {
    new Vertex(100f, 100f, 0f, 0f, Color.GREEN),
    new Vertex(200f, 100f, 0f, 0f, new Color(0, 255, 255)),
    new Vertex(150f, 50f, 0f, 0f, new Color(255, 255, 0)),
};
VertexGeometry geo = new VertexGeometry();
geo.setVertices(tri);
geo.setPrimitiveType(PrimitiveType.TRIANGLES);
gfx.draw(geo);
```

## Common pitfalls

- `Text` needs a valid `Font`; dispose fonts when done.
- Rotation is in **radians** — see [Transformable](/render/transformable).
- Origin is applied before rotation/scale (SFML order).

## See also

- [Draw State](/render/draw-state)
- [Textures](/render/textures)
- [Math — Transforms](/math/transforms)
