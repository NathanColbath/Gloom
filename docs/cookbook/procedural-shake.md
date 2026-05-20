# Procedural Shake

## Problem

Hand-authored camera shake keyframes are tedious and repetitive. You want impact feedback — explosions, damage, landing — that feels organic but stays cheap to evaluate every frame.

## Solution

Offset `Camera2d.setCenter` with samples from `org.llw.math.noise.PerlinNoise` over time. Noise is continuous and deterministic for a given seed, so shake feels random yet reproducible. Fade amplitude with a decay envelope so the effect ends cleanly.

```java
import org.llw.math.noise.PerlinNoise;
import org.llw.render.core.IntSize;
import org.llw.render.graphics.Camera2d;

final class CameraShake {
    private final PerlinNoise noise = new PerlinNoise(0xC0FFEE);
    private float time;
    private float duration;
    private float magnitude;
  private float baseCenterX, baseCenterY;

    void trigger(float durationSeconds, float magnitudePixels) {
        this.duration = durationSeconds;
        this.magnitude = magnitudePixels;
        this.time = 0f;
    }

    void apply(Camera2d camera, IntSize size, float dt) {
        float cx = baseCenterX != 0f ? baseCenterX : size.width() / 2f;
        float cy = baseCenterY != 0f ? baseCenterY : size.height() / 2f;

        if (time >= duration) {
            camera.setCenter(cx, cy);
            return;
        }

        time += dt;
        float t = time / duration;
        float falloff = 1f - t; // linear decay; square for snappier stop

        float sampleX = noise.noise1D(time * 24f);
        float sampleY = noise.noise1D(time * 24f + 100f); // decorrelated channel

        float offsetX = sampleX * magnitude * falloff;
        float offsetY = sampleY * magnitude * falloff;

        camera.setCenter(cx + offsetX, cy + offsetY);
    }

    void setBaseCenter(float x, float y) {
        baseCenterX = x;
        baseCenterY = y;
    }

    boolean isActive() {
        return time < duration;
    }
}
```

Integrate with gameplay pan:

```java
CameraShake shake = new CameraShake();
Camera2d camera = graphics.getCamera();
float panX = 0f, panY = 0f;

while (graphics.isActive()) {
    float dt = clock.tick();
    IntSize size = graphics.getSize();

  float cx = size.width() / 2f + panX;
  float cy = size.height() / 2f + panY;
    shake.setBaseCenter(cx, cy);

    if (window.isKeyDown(Key.SPACE)) {
        shake.trigger(0.35f, 18f);
    }

    if (shake.isActive()) {
        shake.apply(camera, size, dt);
    } else {
        camera.setCenter(cx, cy);
    }

    graphics.clear(background);
    graphics.draw(scene);
    graphics.present();
}
```

`noise2D(time, 0f)` works equally well; `noise1D` is enough for horizontal/vertical offset pairs. Output is roughly in **[-1, 1]**, so scale by `magnitude` to convert to world/pixel units.

::: details Variations

- **2D noise field:** `noise.noise2D(time * 20f, 0f)` and `noise.noise2D(0f, time * 20f)` for slightly different motion than dual 1D slices.
- **Rotation shake:** Add `sprite.setRotation(baseRot + sampleX * 0.05f * falloff)` on a container layer instead of moving the camera.
- **Impulse stacking:** On repeated `trigger`, take `Math.max` of remaining duration or add magnitudes with a cap to avoid unbounded offset.
- **Seeded variety:** Use `new PerlinNoise(levelSeed)` so the same event type shakes differently per level but consistently across replays.

:::

## Pitfalls

- **Fighting the camera:** Apply shake **after** computing follow/pan base center; overwrite center once per frame, do not accumulate offset into `panX`/`panY`.
- **Never resetting:** When duration elapses, restore exact base center or the view can drift one frame at a stale shaken position.
- **Huge magnitude:** Perlin output near 1 multiplied by large pixels throws the view off-screen — keep magnitudes modest (8–24 for 720p).
- **Fixed timestep mismatch:** Shake is visual; updating with variable `dt` is normal. Using fixed physics `dt` for shake is optional, not required.

## See also

- [Splines & Noise](/math/splines-noise)
- [Camera](/render/camera)
- [Scene Stack](/cookbook/scene-stack)
- [Fixed Timestep](/cookbook/fixed-timestep)
