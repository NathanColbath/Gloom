# Clock

`org.llw.render.core.Clock` is a lightweight frame timer backed by `System.nanoTime()`. All durations are in **seconds** (`float`).

## Key methods

| Method | Description |
|--------|-------------|
| `tick()` | Delta since previous `tick()` or `restart()`; updates last-tick time |
| `restart()` | Same as `tick()` but also resets the elapsed-time origin |
| `elapsedSeconds()` | Total time since last `restart()` (or construction) |

## Frame loop integration

```java
import org.llw.render.core.Clock;
import org.llw.render.graphics.GraphicsContext;

Clock clock = new Clock();

while (gfx.isActive()) {
    float dt = clock.tick();
    update(gameState, dt);
    render(gfx);
    gfx.present();
}
```

Fixed-step simulation with leftover time:

```java
Clock clock = new Clock();
float accumulator = 0f;
final float fixedDt = 1f / 60f;

while (gfx.isActive()) {
    accumulator += clock.tick();
    while (accumulator >= fixedDt) {
        simulate(fixedDt);
        accumulator -= fixedDt;
    }
    float alpha = accumulator / fixedDt;
    render(gfx, alpha);
    gfx.present();
}
```

Measuring session time (HUD stopwatch):

```java
Clock session = new Clock();

void drawTimer(GraphicsContext gfx) {
    float seconds = session.elapsedSeconds();
    label.setContent(String.format("Time: %.1f", seconds));
}

void resetRound() {
    session.restart();  // returns delta; also resets elapsed origin
}
```

## Pitfalls

::: warning
The first `tick()` after construction measures time since the `Clock` was created, which may include initialization work and produce a large spike. Call `restart()` once before the main loop if you want the first frame delta discarded.
:::

- Deltas are **unclamped** — tabbing out or hitting a breakpoint can yield very large `dt`; clamp in gameplay code if needed (`Math.min(dt, 0.05f)`).
- `restart()` resets both the delta baseline **and** `elapsedSeconds()` — use `tick()` alone when you only need frame delta.
- `Clock` is independent of vsync; uncapped windows report variable `dt` each frame.

::: tip
Use one `Clock` per logical timer (frame delta, session elapsed, cooldowns) rather than sharing a single instance for unrelated measurements.
:::

## See also

- [Graphics Context](/render/graphics-context) — where `tick()` is usually called
- [Render Overview](/render/overview)
