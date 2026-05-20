# Custom Renderable

## Problem

Built-in `Sprite`, `Rectangle`, and `Circle` are not enough — you need bespoke geometry, procedural meshes, or a composite drawable that emits several GPU commands in one enqueue.

## Solution

Implement `Renderable` and submit geometry in `render(OpenGlBackend backend, DrawState state)`. The render target calls this during `present()` / `flush()` with the active draw state (blend mode, optional shader, parent transform, layer).

```java
import org.llw.math.matrix.Matrix3x2;
import org.llw.render.core.Color;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.PrimitiveType;
import org.llw.render.graphics.Renderable;
import org.llw.render.graphics.Vertex;
import org.llw.render.gl.OpenGlBackend;

/** Filled triangle in local space with corners at (0,0), (w,0), (w/2,h). */
public final class TriangleRenderable implements Renderable {
    private float width = 80f;
    private float height = 70f;
    private Color color = new Color(180, 255, 120);
    private final Matrix3x2 model = new Matrix3x2().identity();
    private float x = 100f;
    private float y = 100f;

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void render(OpenGlBackend backend, DrawState state) {
        Vertex[] verts = {
                new Vertex(0f, 0f, 0f, 0f, color),
                new Vertex(width, 0f, 0f, 0f, color),
                new Vertex(width * 0.5f, height, 0f, 0f, color)
        };

        Matrix3x2 transform = model.copy().translate(x, y);
        if (state.transform() != null) {
            Matrix3x2 combined = state.transform().copy();
            combined.multiply(transform);
            transform = combined;
        }

        backend.drawVertices(transform, verts, PrimitiveType.TRIANGLES,
                state.shader(), state.blendMode());
    }
}
```

Use it like any other drawable:

```java
TriangleRenderable marker = new TriangleRenderable();
marker.setPosition(400f, 300f);

while (gfx.isActive()) {
    gfx.clear(background);
    gfx.draw(marker);
    gfx.draw(marker, DrawState.DEFAULT.withLayer(50));
    gfx.present();
}
```

For textured quads, follow `Sprite.render` and call `backend.drawTexturedQuad(...)`. For arbitrary batches, see `VertexGeometry` in the library source.

::: details Variations

**Extend `AbstractTransformable`** — reuse `getTransform()`, position, rotation, and scale instead of manual matrices:

```java
public final class QuadMarker extends AbstractTransformable implements Renderable {
    @Override
    public void render(OpenGlBackend backend, DrawState state) {
        Matrix3x2 model = getTransform();
        if (state.transform() != null) {
            model = state.transform().copy().multiply(model);
        }
        backend.drawVertices(model, vertices, PrimitiveType.TRIANGLE_FAN,
                state.shader(), state.blendMode());
    }
}
```

**Delegate to `VertexGeometry`** — store vertices externally and forward:

```java
private final VertexGeometry proxy = new VertexGeometry();

@Override
public void render(OpenGlBackend backend, DrawState state) {
    proxy.render(backend, state);
}
```

**No-op when empty** — return early when there is nothing to draw (see `Sprite` when texture is null).

**Composite renderable** — hold child `Renderable` instances and call each in `render`; layers still come from the single enqueued `DrawState`.

**Textured custom mesh** — set UVs on `Vertex` tex coords and use `VertexGeometry.setTexture`, or call `drawTexturedQuad` in a loop like `VertexGeometry`.

:::

## Pitfalls

- **Do not call OpenGL outside `render`** — enqueue with `gfx.draw` and let the flush pass invoke your code with a valid backend state.
- **Respect `DrawState.transform()`** — parent transforms from the enqueue call must multiply with your local model matrix (multiply parent × local).
- **Y-down coordinates** — vertex positions follow the same top-left, Y-down convention as built-in shapes.
- **Mutable fields** — the same instance is reused each frame; avoid allocating large vertex arrays inside `render` every call if performance matters.
- **Layers and blend** — `Renderable` does not choose its own layer; pass `DrawState` at `draw` time.

## See also

- [Renderables (hub)](/render/renderables)
- [Vertex Geometry](/render/vertex-geometry)
- [Draw Order Layers](/cookbook/draw-order-layers)
- [Shaders](/render/shaders)
