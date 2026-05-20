# Offscreen Minimap

## Problem

You want a small overview map in the corner that shows entity positions without squeezing the whole world into the main camera view. Rendering the full scene twice at different scales is easiest with a dedicated offscreen pass.

## Solution

Create an `OffscreenTarget`, render a simplified world into it each frame, `flush()` to the FBO, then draw `colorTexture()` as a regular `Sprite` on the main target.

```java
import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.render.graphics.Camera2d;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.renderables.Circle;
import org.llw.render.renderables.Rectangle;
import org.llw.render.renderables.Sprite;

IntSize minimapSize = new IntSize(200, 200);
OffscreenTarget minimap = new OffscreenTarget(gfx.backend(), minimapSize);
Sprite minimapSprite = new Sprite(minimap.colorTexture());
minimapSprite.setPosition(16f, 16f); // HUD corner in screen/world pixels

// Icons drawn inside the minimap pass
Rectangle mapBackground = new Rectangle();
mapBackground.setSize(180f, 180f);
mapBackground.setPosition(10f, 10f);
mapBackground.setFillColor(new Color(20, 24, 32, 220));

Circle playerBlip = new Circle();
playerBlip.setRadius(6f);
playerBlip.setFillColor(new Color(80, 220, 120));

Circle enemyBlip = new Circle();
enemyBlip.setRadius(5f);
enemyBlip.setFillColor(new Color(255, 90, 70));

// World bounds your main game uses (example)
float worldW = 4000f;
float worldH = 3000f;

while (gfx.isActive()) {
    // ... simulate playerX, playerY, enemyX, enemyY in world space ...

    // --- Minimap pass (independent camera) ---
    Camera2d miniCam = minimap.getCamera();
    miniCam.setCenter(minimapSize.width() / 2f, minimapSize.height() / 2f);
    miniCam.setSize(minimapSize.width(), minimapSize.height());

    // Map world positions into minimap pixel space
    float blipX = (playerX / worldW) * minimapSize.width();
  float blipY = (playerY / worldH) * minimapSize.height();
    playerBlip.setPosition(blipX, blipY);

    enemyBlip.setPosition(
            (enemyX / worldW) * minimapSize.width(),
            (enemyY / worldH) * minimapSize.height());

    minimap.clear(new Color(12, 14, 20));
    minimap.draw(mapBackground);
    minimap.draw(playerBlip);
    minimap.draw(enemyBlip);
    minimap.flush(); // commits to colorTexture()

    // --- Main pass ---
    gfx.clear(new Color(18, 20, 28));
    // draw main game ...
    gfx.draw(minimapSprite, DrawState.DEFAULT.withLayer(100));
    gfx.present();
}

minimap.dispose();
```

The minimap camera defaults to the FBO size at construction; you can also zoom it to show a subsection of the world by adjusting `miniCam.setSize`.

::: details Variations

**Follow player** — center the minimap camera on the player blip instead of normalizing to full world bounds:

```java
miniCam.setCenter(playerBlip.getPosition().x, playerBlip.getPosition().y);
miniCam.setSize(120f, 120f); // local zoomed patch
```

**Border frame** — draw an outlined `Rectangle` on the main target around `minimapSprite` at a higher layer.

**Rotate with player heading** — set rotation on blip icons or rotate the offscreen camera (advanced; keep icon positions in unrotated map space for clarity).

**Update every N frames** — call `minimap.flush()` every second frame for large worlds; the sprite still displays the last committed texture.

**Second full-scene minimap** — draw the same renderables into `minimap` with a scaled camera rather than abstract blips; costs more GPU time.

:::

## Pitfalls

- **`flush()` not `present()`** — offscreen targets commit with `flush()`; skipping it leaves a stale or cleared texture.
- **Shared backend** — construct with `gfx.backend()` after `GraphicsContext` initialization.
- **Texture handle lifetime** — `minimapSprite` holds `colorTexture()` from the FBO; dispose the `OffscreenTarget` only after removing draws that reference it.
- **Coordinate spaces** — main HUD position uses the main camera; blip positions use the minimap camera. Do not mix without conversion.
- **Alpha on the panel** — clear color and panel fill alpha affect whether the minimap occludes the scene behind it.

## See also

- [Offscreen Rendering](/render/offscreen)
- [Camera](/render/camera)
- [Draw Order Layers](/cookbook/draw-order-layers)
- [Full Demo](/examples/full-demo)
