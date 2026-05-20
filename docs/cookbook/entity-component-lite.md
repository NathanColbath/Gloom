# Entity Lite (Transform2f)

## Problem

You want dozens of game objects with position, rotation, and scale without adopting a full entity-component-system framework. Renderables already embed transforms, but simulation code (AI, collision, networking) should not depend on `Sprite` or `Rectangle` types.

## Solution

Store a shared `org.llw.math.transform.Transform2f` per logical entity and sync it to drawables each frame. `Transform2f` mirrors the math used by `AbstractTransformable` renderables and produces a `Matrix3x2` when needed.

```java
import org.llw.math.transform.Transform2f;
import org.llw.math.vector.Vector2f;
import org.llw.render.renderables.Sprite;

final class Entity {
    final Transform2f transform = new Transform2f();
    Sprite sprite;

    Entity(Sprite sprite) {
        this.sprite = sprite;
    }

    void syncToSprite() {
        Vector2f pos = transform.getPosition();
        sprite.setPosition(pos.x, pos.y);
        sprite.setRotation(transform.getRotation());
        Vector2f scale = transform.getScale();
        sprite.setScale(scale.x, scale.y);
        Vector2f origin = transform.getOrigin();
        sprite.setOrigin(origin.x, origin.y);
    }
}
```

Create entities, update transforms in your game loop, then push state to renderables before drawing:

```java
List<Entity> entities = new ArrayList<>();

Entity player = new Entity(playerSprite);
player.transform.setPosition(400f, 300f);
player.transform.setOrigin(16f, 16f);
entities.add(player);

while (graphics.isActive()) {
    float dt = clock.tick();
    graphics.pollEvents();

    // Simulation writes to transform only
    Vector2f pos = player.transform.getPosition();
    player.transform.setPosition(pos.x + 120f * dt, pos.y);

    for (Entity e : entities) {
        e.syncToSprite();
    }

    graphics.clear(background);
    for (Entity e : entities) {
        graphics.draw(e.sprite);
    }
    graphics.present();
}
```

For collision or custom drawing without a renderable, read the matrix directly:

```java
import org.llw.math.matrix.Matrix3x2;

Matrix3x2 world = entity.transform.toMatrix();
```

`markDirty()` is only needed if you mutate internal state without going through setters (you normally do not).

::: details Variations

- **Component bags:** Add plain fields (`float health`, `Aabb2f bounds`) beside `Transform2f` on the same class — still "lite" without reflection or registries.
- **Parenting:** Multiply child `toMatrix()` by parent `toMatrix()` with `Matrix3x2` operations when you need hierarchies; keep one `Transform2f` per node.
- **Pooling:** Reuse `Entity` instances from an `ArrayList`; call `transform.setPosition(0, 0)` and reset scale on spawn instead of allocating each time.
- **No sprite:** Entities that only exist for logic can omit `Sprite` and use `transform` with `Intersection2` / `Aabb2f` tests.

:::

## Pitfalls

- **`getPosition()` copies:** `Transform2f.getPosition()` returns a new `Vector2f`. Read once per frame or chain `setPosition` with literals — do not assume mutating the returned vector updates the transform.
- **Forgetting `syncToSprite()`:** Simulation changes are invisible until you copy transform fields onto the renderable.
- **Double ownership:** Avoid calling `sprite.setPosition` in gameplay code *and* `syncToSprite()` — pick transform as the single source of truth.
- **Origin vs position:** Rotation and scale pivot around `setOrigin`; align origin with sprite art center for natural spins.

## See also

- [Transforms](/math/transforms)
- [Renderables](/render/renderables)
- [Platformer AABB](/cookbook/platformer-aabb)
