# Fixed Timestep

## Problem

Variable frame time (`clock.tick()`) makes physics and gameplay feel different at 30 FPS vs 144 FPS. Movement integrated as `speed * dt` is frame-rate dependent when collision resolution or AI logic assumes a constant step. You need simulation to advance in fixed slices while rendering can still run as fast as the display allows.

## Solution

Use `org.llw.render.core.Clock` to measure real elapsed time, accumulate it, and drain the accumulator in a `while` loop at a constant `FIXED_DT`. Run physics and game rules only inside that inner loop; use the leftover fraction for optional render interpolation.

```java
import org.llw.render.core.Clock;

private static final float FIXED_DT = 1f / 60f;
private float accumulator = 0f;

Clock clock = new Clock();

while (graphics.isActive()) {
    float frameDt = clock.tick();
    graphics.pollEvents();

    // Cap spikes (alt-tab, debugger) so the sim does not spiral
    float dt = Math.min(frameDt, 0.25f);
    accumulator += dt;

    while (accumulator >= FIXED_DT) {
        updateGame(FIXED_DT);   // physics, timers, AI — always the same step
        accumulator -= FIXED_DT;
    }

    float alpha = accumulator / FIXED_DT; // 0..1 blend toward next sim state
    render(alpha);

    if (audio != null) {
        audio.update();
    }
    graphics.present();
}
```

A minimal fixed-step update might integrate velocity and resolve collisions:

```java
void updateGame(float dt) {
    playerX += playerVx * dt;
    playerY += playerVy * dt;
    playerVy += gravity * dt;
    resolvePlatforms(dt);
}
```

`Clock.restart()` resets both elapsed and delta baselines — use it when pausing or loading a level, not every frame. Use `clock.elapsedSeconds()` for time-based animation that is allowed to vary with real time (UI pulses, cutscene timers).

::: details Variations

- **Single update pass:** If your game is simple, skip `alpha` and render the latest sim state directly. The accumulator still stabilizes physics.
- **Multiple fixed rates:** Run physics at `1/60` and slow systems (economy, spawners) every N steps with a counter inside `updateGame`.
- **Semi-fixed:** Call `updateGame(FIXED_DT)` in the inner loop but pass `frameDt` to input smoothing or camera follow outside it.
- **Pause:** Stop adding to `accumulator` when paused; optionally call `clock.restart()` on resume to discard stale delta.

:::

## Pitfalls

- **Spiral of death:** Without a max `frameDt` cap, a long hitch adds many inner iterations in one frame and freezes further. Clamp incoming delta (e.g. `0.25f` seconds).
- **Mixing fixed and variable integration:** Do not apply the same forces both inside and outside the fixed loop — pick one integration path for each system.
- **Zero fixed steps:** On very fast frames `alpha` stays small; that is correct. Do not force at least one physics step per frame unless you accept slow-motion on high refresh displays.
- **Audio:** `AudioContext.update()` belongs in the outer loop once per frame, not per physics sub-step.

## See also

- [Coordinates & Frame Loop](/guide/coordinates)
- [Frame Loop best practices](/best-practices/frame-loop)
- [Full Demo](/examples/full-demo)
