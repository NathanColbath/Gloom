# Draw Order Layers

## Problem

Objects enqueued with `gfx.draw(...)` appear in submission order when they share the same sort key. Background tiles, gameplay sprites, UI panels, and text can end up interleaved incorrectly when you add or reorder draws across systems.

## Solution

Pass a `DrawState` with an explicit layer. Lower layers draw first; higher layers draw on top. Within the same layer, submission order is preserved.

```java
import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.renderables.Rectangle;
import org.llw.render.renderables.Sprite;
import org.llw.render.renderables.Text;

DrawState background = DrawState.DEFAULT.withLayer(0);
DrawState gameplay   = DrawState.DEFAULT.withLayer(10);
DrawState ui         = DrawState.DEFAULT.withLayer(100);
DrawState overlay    = DrawState.DEFAULT.withLayer(200);

Rectangle floor = new Rectangle();
floor.setSize(2000f, 2000f);
floor.setFillColor(new Color(30, 32, 40));

Sprite hero = new Sprite(heroTexture);
hero.setPosition(320f, 240f);

Rectangle panel = new Rectangle();
panel.setSize(280f, 120f);
panel.setPosition(20f, 20f);
panel.setFillColor(new Color(0, 0, 0, 180));

Text label = new Text(font);
label.setContent("Score: 42");
label.setPosition(36f, 32f);

while (gfx.isActive()) {
    gfx.clear(new Color(18, 20, 28));

    gfx.draw(floor, background);
    gfx.draw(hero, gameplay);
    gfx.draw(panel, ui);
    gfx.draw(label, ui);   // same layer as panel — drawn after panel

    gfx.present();
}
```

`DrawState` is immutable — chain `withLayer`, `withBlendMode`, or `withShader` without mutating `DEFAULT`.

::: details Variations

**Per-object highlight** — temporarily boost one sprite without resorting the whole scene:

```java
gfx.draw(selectedEnemy, DrawState.DEFAULT.withLayer(15)); // above other gameplay (10)
```

**Combine layer + blend** — fade a fullscreen flash on top:

```java
DrawState flash = DrawState.DEFAULT
        .withLayer(500)
        .withBlendMode(BlendMode.ADD);
gfx.draw(fullscreenQuad, flash);
```

**Offscreen pass layers** — layers sort **within** a single `RenderTarget` flush. The offscreen texture is one sprite on the main target; internal layering does not interact with main-target layers.

**Negative layers** — integers are allowed; only relative order matters. Some teams use `0` world, `1000` UI, `2000` debug.

**Stable text over panels** — enqueue the panel then the text at the same UI layer so labels always appear above their background rect.

:::

## Pitfalls

- **`draw()` is deferred** — layers apply at `present()` / `flush()` sort time, not at the call site.
- **Same layer ties break on submission order** — two UI widgets at layer `100` overlap based on enqueue order, not Y position.
- **Huge layer gaps** — spacing by 10 or 100 leaves room to insert mid-layer draws later.
- **DrawState.DEFAULT is shared** — always call `withLayer` to get a new instance; do not try to mutate the default record.
- **Transforms are separate** — layers do not replace depth sorting for overlapping sprites at the same layer and position.

## See also

- [Graphics Context](/render/graphics-context)
- [Render Target](/render/render-target)
- [Text & Fonts](/render/text-and-fonts)
- [Mouse Picking](/cookbook/mouse-picking)
