# Sprite Sheet

## Problem

Your character art is a single PNG with many frames arranged in a grid. You need to draw one cell at a time without splitting the image into separate GPU textures.

## Solution

Load the atlas once, then set a **normalized UV rectangle** on `Sprite` via `setTextureRect`. UVs are fractions of the texture size in `[0, 1]` — left, top, width, height — matching how `Sprite.render` samples the quad.

```java
import org.llw.math.geometry.RectF;
import org.llw.render.graphics.Texture2d;
import org.llw.render.graphics.TextureFactory;
import org.llw.render.renderables.Sprite;

// 256×256 atlas — load PNG bytes or use a procedural stand-in while prototyping
Texture2d atlas = TextureFactory.checkerboard(256, 256, 32);
int cols = 4;
int rows = 4;
int cellW = atlas.size().width() / cols;
int cellH = atlas.size().height() / rows;

Sprite frame = new Sprite(atlas);
frame.setPosition(200f, 150f);

int column = 2;
int row = 1;

float u0 = (column * cellW) / (float) atlas.size().width();
float v0 = (row * cellH) / (float) atlas.size().height();
float uW = cellW / (float) atlas.size().width();
float vH = cellH / (float) atlas.size().height();

frame.setTextureRect(new RectF(u0, v0, uW, vH));

// Drawn size in world units follows the UV rect × texture pixel size
while (gfx.isActive()) {
    gfx.clear(background);
    gfx.draw(frame);
    gfx.present();
}
```

A frame at column 2, row 1 occupies UV `(0.5, 0.25, 0.25, 0.25)` in a uniform 4×4 grid.

::: details Variations

**Animation loop** — advance column from elapsed time:

```java
int frameIndex = (int) (clock.elapsedSeconds() * 8f) % (cols * rows);
int col = frameIndex % cols;
int row = frameIndex / cols;
applyCell(frame, atlas, col, row, cols, rows);
```

**Helper to avoid manual division**:

```java
static void applyCell(Sprite sprite, Texture2d tex, int col, int row, int cols, int rows) {
    float tw = tex.size().width();
    float th = tex.size().height();
    float cw = tw / cols;
    float ch = th / rows;
    sprite.setTextureRect(new RectF(col * cw / tw, row * ch / th, cw / tw, ch / th));
}
```

**Padding between cells** — shrink UV width/height slightly to avoid bleeding from neighboring texels:

```java
float padU = 0.5f / atlas.size().width();
float padV = 0.5f / atlas.size().height();
sprite.setTextureRect(new RectF(u0 + padU, v0 + padV, uW - 2 * padU, vH - 2 * padV));
```

**Non-uniform rows** — store per-frame `RectF` UVs in a table when the atlas is not a perfect grid.

**Reset to full texture**:

```java
sprite.setTextureRect(new RectF(0f, 0f, 1f, 1f));
```

:::

## Pitfalls

- **Pixel vs normalized coords** — `setTextureRect` expects UV fractions, not pixel addresses. Divide by texture width and height.
- **Y-down world, UV origin** — `top` is the upper edge in UV space (smaller V toward the top of the image); row 0 is the top row of the atlas.
- **Draw size follows UV rect** — halving UV width halves on-screen width unless you also scale the sprite transform.
- **Filtering bleed** — linear filtering at atlas seams causes color halos; add padding in the art or inset UVs.
- **Dispose the atlas** — one `Texture2d` backs all frames; dispose once when unloading the sheet.

## See also

- [Sprite](/render/sprite)
- [Textures](/render/textures)
- [Tinted Sprites](/cookbook/tinted-sprites)
- [Resource Loading](/render/resource-loading)
