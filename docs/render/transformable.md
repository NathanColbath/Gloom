# Transformable

`org.llw.render.graphics.Transformable` defines SFML-style 2D transforms: position, rotation, scale, and origin pivot in **Y-down** world space. `getTransform()` composes them into a `Matrix3x2` used during drawing.

Built-in renderables extend `AbstractTransformable`, which delegates to `org.llw.math.transform.Transform2f`.

## Key methods

| Method | Description |
|--------|-------------|
| `getPosition()` / `setPosition(float x, float y)` | World position (top-left reference unless origin shifts pivot) |
| `setPosition(Vector2f)` | Vector overload |
| `getRotation()` / `setRotation(float radians)` | Rotation about origin, CCW in Y-down space |
| `getScale()` / `setScale(float x, float y)` | Non-uniform scale factors |
| `setScale(Vector2f)` | Vector overload |
| `getOrigin()` / `setOrigin(float x, float y)` | Local pivot for rotation and scale |
| `setOrigin(Vector2f)` | Vector overload |
| `getTransform()` | Composed `Matrix3x2` model matrix |

## Transform order

Composition matches SFML: **translate → rotate about origin → scale about origin**. Set `origin` to the point that should stay fixed under rotation (sprite center, rectangle corner, etc.).

## Examples

Centered spinning sprite:

```java
import org.llw.render.renderables.Sprite;
import org.llw.render.graphics.Texture2d;

Texture2d tex = /* ... */;
Sprite sprite = new Sprite(tex);
var size = tex.size();
sprite.setOrigin(size.width() / 2f, size.height() / 2f);
sprite.setPosition(640f, 360f);

float t = 0f;
while (gfx.isActive()) {
    t += clock.tick();
    sprite.setRotation(t);
    gfx.clear(Color.BLACK);
    gfx.draw(sprite);
    gfx.present();
}
```

Rectangle with top-left anchor (default):

```java
import org.llw.render.renderables.Rectangle;

Rectangle box = new Rectangle();
box.setSize(120f, 40f);
box.setPosition(50f, 50f);  // top-left at (50, 50)
```

Grouped motion via `DrawState` instead of mutating each child:

```java
import org.llw.render.graphics.DrawState;
import org.llw.math.matrix.Matrix3x2;

Matrix3x2 arm = new Matrix3x2().translate(200f, 300f).rotate(angle);
gfx.draw(upperArm, DrawState.DEFAULT.withTransform(arm));
gfx.draw(lowerArm, DrawState.DEFAULT.withTransform(arm).combineTransform(elbowLocal));
```

## Pitfalls

::: warning
Rotation is in **radians**, not degrees. Use `Math.toRadians(degrees)` or multiply by `π/180`.
:::

- `getPosition()` returns a live view from the underlying transform — mutating the returned `Vector2f` may or may not mark the matrix dirty depending on math layer usage; prefer `setPosition`.
- Negative scale flips geometry; combine with careful origin placement to avoid jumping.
- `DrawState.withTransform()` multiplies **on the left** of the renderable's local matrix — parent group transforms belong in `DrawState`, local offsets on the renderable.

::: tip
For UI anchored to screen corners, set position each frame from `gfx.getSize()` rather than relying on camera pan when you want screen-fixed HUD.
:::

## See also

- [Math — Transforms](/math/transforms)
- [Sprite](/render/sprite)
- [Draw State](/render/draw-state)
