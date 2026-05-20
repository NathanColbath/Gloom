# Tutorial 7 — Transforms

## Goal

Understand the `Transformable` interface used by sprites and shapes, practice rotation and scale with an **origin pivot**, and use `Transform2f` when you need a transform in game logic without a renderable.

## Prerequisites

- Completed [Tutorial 6 — Text](/tutorials/06-text)
- Shapes or sprites from [Tutorial 4 — Sprites](/tutorials/04-sprites) / [Tutorial 5 — Shapes](/tutorials/05-shapes)

::: details Coordinate reminder
LLW uses **Y-down** world space: `(0, 0)` is the top-left of the window, `x` increases right, `y` increases down. Rotation is in **radians**, counter-clockwise in this space.
:::

## Step 1 — The four transform fields

Every `Transformable` renderable exposes:

| Field | Role |
|-------|------|
| `position` | World-space placement of the local origin |
| `rotation` | Angle in radians about the origin pivot |
| `scale` | Non-uniform scale along X and Y |
| `origin` | Local pivot for rotation and scaling |

```java
import org.llw.render.renderables.Sprite;
import org.llw.render.graphics.TextureFactory;

Sprite card = new Sprite(TextureFactory.checkerboard(64, 64, 8));
card.setPosition(400f, 300f);
card.setRotation(0.3f);
card.setScale(1.5f, 1.5f);
```

By default `origin` is `(0, 0)` — the top-left corner of the sprite's local bounds.

## Step 2 — Set the origin before rotating

Without an origin offset, rotation spins around the top-left corner. For a card that should spin around its center, set the origin to half the texture size **before** applying rotation:

```java
card.setOrigin(32f, 32f);   // half of 64×64 checkerboard
card.setRotation((float) (System.nanoTime() / 1e9 * 0.8));
```

The same pattern applies to `Rectangle` and `Circle`. A `Circle` with radius `50` uses `setOrigin(50f, 50f)` so it rotates about its visual center (local space spans `0…2r`).

::: tip Order of operations
Internally, LLW composes: translate to position → translate origin → rotate → scale → translate back. You only set the four fields; `getTransform()` returns the combined `Matrix3x2`.
:::

## Step 3 — Animate with delta time

Combine transforms with a variable timestep (covered fully in [Tutorial 10](/tutorials/10-game-loop)):

```java
import org.llw.render.core.Clock;

Clock clock = new Clock();
float spinSpeed = 1.2f; // radians per second

while (gfx.isActive()) {
    float dt = clock.tick();
    gfx.pollEvents();

    card.setRotation(card.getRotation() + spinSpeed * dt);

    gfx.clear(new Color(18, 20, 28));
    gfx.draw(card);
    gfx.present();
}
```

## Step 4 — `Transform2f` for logic-only transforms

Renderables delegate to `org.llw.math.transform.Transform2f`. Use it directly when you simulate physics or attach child offsets without drawing:

```java
import org.llw.math.transform.Transform2f;
import org.llw.math.matrix.Matrix3x2;

Transform2f turret = new Transform2f();
turret.setPosition(640f, 360f);
turret.setOrigin(0f, 24f);
turret.setRotation(aimAngle);

Transform2f barrel = new Transform2f();
barrel.setPosition(0f, -40f); // local offset along turret up

Matrix3x2 worldBarrel = new Matrix3x2();
worldBarrel.combine(turret.toMatrix(), barrel.toMatrix());
// pass worldBarrel to DrawState.withTransform when drawing the barrel sprite
```

`toMatrix()` caches the result until you change a field (or call `markDirty()`).

## Step 5 — Parent transforms via `DrawState`

To draw a child renderable in a parent's space without mutating the child, multiply transforms at draw time:

```java
import org.llw.render.graphics.DrawState;

Matrix3x2 parent = turret.toMatrix();
gfx.draw(barrelSprite, DrawState.DEFAULT.withTransform(parent));
```

The child's own `setPosition` / `setRotation` are still applied — `DrawState` prepends an extra parent matrix. Use this for hierarchical UI or turrets on vehicles.

::: warning Scale and hit testing
Scaling a renderable visually enlarges it, but simple math tests (like `Circle2f.contains`) use the positions **you** store in game state. Keep logical positions in sync with visual transforms, or test in local space with the inverse matrix.
:::

## Full class

```java
import org.llw.render.core.Clock;
import org.llw.render.core.Color;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.graphics.Texture2d;
import org.llw.render.graphics.TextureFactory;
import org.llw.render.renderables.Circle;
import org.llw.render.renderables.Sprite;
import org.llw.render.window.Window;
import org.llw.render.window.WindowSettings;

public class TransformsTutorial {
    public static void main(String[] args) {
        Window window = new Window(new WindowSettings().title("LLW Transforms").size(900, 600));
        GraphicsContext gfx = new GraphicsContext(window);
        Clock clock = new Clock();

        Texture2d checker = TextureFactory.checkerboard(64, 64, 8);
        Sprite card = new Sprite(checker);
        card.setPosition(300f, 250f);
        card.setOrigin(32f, 32f);

        Circle orb = new Circle();
        orb.setRadius(48f);
        orb.setPosition(600f, 300f);
        orb.setOrigin(48f, 48f);
        orb.setFillColor(new Color(255, 140, 80, 220));

        float cardSpin = 0.8f;
        float orbPulse = 0f;

        while (gfx.isActive()) {
            float dt = clock.tick();
            gfx.pollEvents();

            card.setRotation(card.getRotation() + cardSpin * dt);

            orbPulse += dt;
            float s = 1f + 0.15f * (float) Math.sin(orbPulse * 3f);
            orb.setScale(s, s);

            gfx.clear(new Color(18, 20, 28));
            gfx.draw(card);
            gfx.draw(orb);
            gfx.present();
        }

        checker.dispose();
        gfx.dispose();
    }
}
```

## What you learned

- `Transformable` provides position, rotation, scale, and origin on sprites, shapes, and text.
- Setting `origin` to the visual center makes rotation and scaling feel natural.
- `Transform2f` mirrors renderable math for simulation code and produces `Matrix3x2`.
- `DrawState.withTransform` applies an extra parent matrix at draw time.

**Next:** [Tutorial 8 — Camera & Views](/tutorials/08-camera)
