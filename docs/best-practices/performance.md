# Performance

LLW batches draw calls internally, but game structure still dominates frame time.

## Rendering

**Batch by texture.** The draw queue sorts by layer and texture. Hundreds of sprites sharing one atlas cost far less than one sprite per unique texture.

**Minimize state changes.** Custom shaders and blend modes break batches. Group alpha-blended particles separately from opaque tiles.

**Do not allocate in the hot loop.**

```java
// Bad — new matrix every entity
for (Entity e : entities) {
    gfx.draw(e.sprite(new Matrix3x2()));
}

// Better — mutate a scratch transform
Matrix3x2 scratch = new Matrix3x2();
for (Entity e : entities) {
    e.applyTransform(scratch);
    e.sprite().setTransform(scratch); // if exposing transform API
    gfx.draw(e.sprite());
}
```

**Offscreen targets** cost a full extra pass. Use for minimaps and post-processing, not every sprite.

## CPU math

`Vector2f` and `Matrix3x2` are mutable — reuse instances in collision loops. `Intersection2` and geometry types are lightweight; prefer broad-phase (AABB grid) before SAT polygon tests.

## Audio

- Share `SoundBuffer` across many `Sound` voices.
- Stream long music; do not `loadSoundBuffer` multi-minute OGG files.
- The source pool caps at **32** simultaneous voices — design pools for typical peak, not worst-case unlimited fire.

## Measurement

```java
Clock clock = new Clock();
// log 1/clock.tick() for FPS
```

Profile before optimizing shaders. In the Gloom demo, draw queue flush is usually cheaper than uncached text layout or per-frame texture creation.

::: warning TextureFactory in production
`TextureFactory.checkerboard` generates pixels on the CPU — fine for demos. Ship real assets from disk or atlas files.
:::

::: tip Layer ordering
Use `DrawState` / layer constants to sort once. Changing layer per draw is cheap; changing texture is not.
:::

## See also

- [Draw Order Layers](/cookbook/draw-order-layers)
- [Resource Lifecycle](/best-practices/resource-lifecycle)
- [Frame Loop](/best-practices/frame-loop)
