# Sprites & Shapes

Draw a textured sprite, colored panel, and circle.

```java
import org.llw.render.core.Color;
import org.llw.render.graphics.*;
import org.llw.render.renderables.Circle;
import org.llw.render.renderables.Rectangle;
import org.llw.render.renderables.Sprite;
import org.llw.render.window.Window;
import org.llw.render.window.WindowSettings;

Window window = new Window(new WindowSettings().size(1280, 720));
GraphicsContext gfx = new GraphicsContext(window);

Texture2d checker = TextureFactory.checkerboard(256, 256, 32);
Sprite sprite = new Sprite(checker);
sprite.setPosition(200f, 150f);

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

while (gfx.isActive()) {
    gfx.pollEvents();
    sprite.setRotation(sprite.getRotation() + 0.01f);

    gfx.clear(new Color(18, 20, 28));
    gfx.draw(sprite);
    gfx.draw(panel);
    gfx.draw(orb);
    gfx.present();
}

checker.dispose();
gfx.dispose();
```

## Offscreen variant

Render a sub-scene to a texture and blit it as a sprite — see [Offscreen Rendering](/render/offscreen).

## See also

- [Renderables](/render/renderables)
- [Full Demo](/examples/full-demo)
