# Vertex & VertexGeometry

`org.llw.render.graphics.Vertex` is a single 2D vertex (position, UV, color). `org.llw.render.renderables.VertexGeometry` submits arbitrary vertex arrays as immediate-mode primitives or batched textured quads.

## Vertex — key members

| Member / ctor | Description |
|---------------|-------------|
| `position` (`Vector2f`) | Local/world position, Y-down |
| `texCoord` (`Vector2f`) | Normalized UV; `v` increases downward |
| `color` (`Color`) | Per-vertex tint |
| `Vertex(float x, float y, float u, float v, Color)` | Scalar constructor |

## VertexGeometry — key methods

| Method | Description |
|--------|-------------|
| `getVertices()` / `setVertices(Vertex[])` | Geometry data (`null` → empty) |
| `getPrimitiveType()` / `setPrimitiveType(PrimitiveType)` | Topology for **untextured** draws (default `TRIANGLES`) |
| `getTexture()` / `setTexture(Texture2d)` | Enables textured quad path |
| Transform API | Standard `Transformable` |

## PrimitiveType values

| Enum | OpenGL topology |
|------|-----------------|
| `POINTS` | Isolated points |
| `LINES` | Line segments (pairs) |
| `LINE_STRIP` | Connected polyline |
| `TRIANGLES` | Independent triangles (groups of 3) |
| `TRIANGLE_FAN` | Fan sharing first vertex |
| `TRIANGLE_STRIP` | Strip sharing edges |

## Examples

Colored triangle:

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

Line strip debug path:

```java
Vertex[] path = new Vertex[count];
for (int i = 0; i < count; i++) {
    path[i] = new Vertex(points[i].x, points[i].y, 0f, 0f, Color.RED);
}
geo.setVertices(path);
geo.setPrimitiveType(PrimitiveType.LINE_STRIP);
gfx.draw(geo);
```

Textured custom quads (groups of 3 vertices → one quad each):

```java
import org.llw.render.graphics.Texture2d;

geo.setTexture(atlas);
geo.setVertices(new Vertex[] {
    new Vertex(0f, 0f,   0f, 0f, Color.WHITE),
    new Vertex(64f, 0f,  1f, 0f, Color.WHITE),
    new Vertex(64f, 64f, 1f, 1f, Color.WHITE),
});
gfx.draw(geo);
```

## Render behavior

- **No texture** (geometry and `DrawState` both null): `backend.drawVertices(model, vertices, primitiveType, …)`.
- **Texture set** on geometry or `DrawState`: every **three** vertices define a textured quad; calls `flushSprites()` after the batch.

Empty vertex arrays are skipped silently.

## Pitfalls

::: warning
In textured mode, vertices are interpreted as **triplets** forming quads (`i`, `i+1`, `i+2`). Leftover vertices fewer than three are ignored. This is not the same as `TRIANGLES` topology.
:::

- Mutating the array returned from `getVertices()` changes geometry in place — prefer `setVertices` with a new array if you cache references elsewhere.
- Textured path uses corners `a`, `b`, `c` to define axis-aligned UV bounds — not arbitrary triangle meshes with per-corner UV interpolation.
- `PrimitiveType` is ignored when a texture is active.

::: tip
Use untextured `VertexGeometry` for debug overlays (lines, fans); use `Sprite` for standard axis-aligned textured rects.
:::

## See also

- [Draw State](/render/draw-state)
- [Sprite](/render/sprite)
- [Rectangle](/render/rectangle) — built-in shape alternative
