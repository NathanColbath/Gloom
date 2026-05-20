# Frame Loop

A correct LLW frame processes input, updates simulation, renders, and pumps audio.

## Canonical loop

```java
import org.llw.render.core.Clock;
import org.llw.render.graphics.GraphicsContext;

Clock clock = new Clock();
AudioContext audio = new AudioContext();

while (gfx.isActive()) {
    float dt = clock.tick();

    gfx.pollEvents();
    while (window.pollEvent(event)) {
        handle(event);
    }

    update(dt);
    audio.update();

    gfx.clear(background);
  drawScene(gfx);
    gfx.present();
}

audio.dispose();
gfx.dispose();
```

| Step | Purpose |
|------|---------|
| `clock.tick()` | Delta time in seconds (variable) |
| `pollEvents()` | GLFW → `WindowEvent` queue |
| `update(dt)` | Game logic |
| `audio.update()` | Refill streaming music |
| `draw` + `present()` | Flush GPU queue and swap buffers |

::: warning Missing present
Calling `draw` without `present()` shows a black or frozen window — nothing reaches the screen.
:::

## Fixed timestep (optional)

For deterministic physics, accumulate time and step at a fixed rate:

```java
float accumulator = 0f;
final float fixedDt = 1f / 60f;

while (gfx.isActive()) {
    accumulator += clock.tick();
    gfx.pollEvents();

    while (accumulator >= fixedDt) {
        physics.step(fixedDt);
        accumulator -= fixedDt;
    }

    float alpha = accumulator / fixedDt;
    render(physics.state(), alpha);

    audio.update();
    gfx.present();
}
```

Full pattern: [Fixed Timestep](/cookbook/fixed-timestep).

## Offscreen passes

When rendering to `OffscreenTarget`, call `flush()` on that target instead of `present()`:

```java
offscreen.begin();
gfx.draw(minimapContent);
offscreen.flush();

gfx.clear(background);
gfx.draw(offscreenSprite);
gfx.present();
```

## Threading

GLFW and OpenAL expect calls from the **main thread**. Do not create `GraphicsContext` or `AudioContext` on worker threads.

::: details VSync
`WindowSettings.vsync(true)` ties `present()` to the display refresh. Disable for benchmarking; enable for smooth visuals and lower CPU use.
:::

## See also

- [Coordinates & Frame Loop](/guide/coordinates)
- [Performance](/best-practices/performance)
- [Tutorial — Game Loop](/tutorials/10-game-loop)
