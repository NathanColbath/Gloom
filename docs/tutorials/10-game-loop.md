# Tutorial 10 — Game Loop

## Goal

Wire a complete **variable-timestep** game loop with `Clock`, separate update and draw phases, integrate rendering and optional audio, and see how the pieces from tutorials 1–9 fit into one runnable skeleton (~120 lines).

## Prerequisites

- Completed [Tutorial 9 — Audio](/tutorials/09-audio)
- Skimmed [Coordinates & Frame Loop](/guide/coordinates)

::: details Variable vs fixed timestep
This tutorial uses **variable** `dt` — each frame may last a different number of seconds. That is the simplest model and matches LLW's `Clock.tick()`. For deterministic physics, see [Fixed Timestep](/cookbook/fixed-timestep) in the cookbook.
:::

## Step 1 — Own the loop structure

Every LLW application follows the same skeleton:

```text
setup → while (active) { measure dt → poll events → update(dt) → render → present }
cleanup
```

`GraphicsContext.isActive()` is `false` after the window closes. Always pair `pollEvents()` with draining `window.pollEvent()` so input queues do not grow without bound.

## Step 2 — Measure delta time with `Clock`

```java
import org.llw.render.core.Clock;

Clock clock = new Clock();

while (gfx.isActive()) {
    float dt = clock.tick(); // seconds since last frame
    // movement: position += speed * dt;
}
```

`clock.elapsedSeconds()` returns total time since the last `restart()` — handy for animations that do not care about per-frame delta.

::: tip Cap extreme dt
When debugging with breakpoints, `dt` can jump to several seconds. Clamp it to avoid physics explosions:

```java
dt = Math.min(dt, 0.05f);
```
:::

## Step 3 — Update phase (simulation)

Keep simulation separate from drawing. Update game state first:

```java
playerX += (window.isKeyDown(Key.D) ? 1 : window.isKeyDown(Key.A) ? -1 : 0) * 320f * dt;
scoreLabel.setContent("Score: " + score);
camera.setCenter(gfx.getSize().width() / 2f, gfx.getSize().height() / 2f);
```

Process events in the same phase before or after movement — just stay consistent frame to frame.

## Step 4 — Render phase (presentation)

Drawing enqueues work; nothing hits the screen until `present()`:

```java
gfx.clear(new Color(18, 20, 28));
gfx.draw(player);
gfx.draw(scoreLabel, DrawState.DEFAULT.withLayer(5));
gfx.present();
```

Offscreen targets use `flush()` instead of `present()` — not needed in this skeleton.

## Step 5 — Pump audio

If you use streaming music, call `audio.update()` once per frame inside the loop (see [Tutorial 9](/tutorials/09-audio)).

## Step 6 — Dispose resources

Reverse order of creation: fonts and textures, audio, graphics.

## Full class

The listing below is a self-contained mini-game: move a circle with WASD, collect a drifting pickup, display score text on a higher layer, and optionally play a click sound.

```java
import org.llw.audio.AudioContext;
import org.llw.audio.Sound;
import org.llw.audio.SoundBuffer;
import org.llw.math.geometry.Circle2f;
import org.llw.math.vector.Vector2f;
import org.llw.render.core.Clock;
import org.llw.render.core.Color;
import org.llw.render.graphics.Camera2d;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.Font;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.renderables.Circle;
import org.llw.render.renderables.Text;
import org.llw.render.window.Key;
import org.llw.render.window.Window;
import org.llw.render.window.WindowEvent;
import org.llw.render.window.WindowSettings;

public class GameLoopTutorial {
    public static void main(String[] args) {
        Window window = new Window(new WindowSettings().title("LLW Game Loop").size(900, 600));
        GraphicsContext gfx = new GraphicsContext(window);
        Camera2d camera = gfx.getCamera();
        Clock clock = new Clock();

        AudioContext audio = tryAudio();
        Sound click = null;
        if (audio != null) {
            SoundBuffer buf = audio.loadSoundBuffer("llw/audio/samples/click.wav");
            click = audio.createSound();
            click.setBuffer(buf);
        }

        Font font = Font.fromClasspath("llw/render/fonts/Roboto-Regular.ttf", 24);
        Text hud = new Text(font);
        hud.setPosition(16f, 12f);
        hud.setFillColor(Color.WHITE);

        Circle player = new Circle();
        player.setRadius(28f);
        player.setOrigin(28f, 28f);
        player.setFillColor(new Color(100, 200, 255));

        Circle pickup = new Circle();
        pickup.setRadius(16f);
        pickup.setOrigin(16f, 16f);
        pickup.setFillColor(new Color(255, 200, 80));

        float playerX = 450f, playerY = 300f;
        float pickupX = 200f, pickupY = 150f;
        float pickupVx = 90f, pickupVy = 60f;
        int score = 0;

        while (gfx.isActive()) {
            float dt = Math.min(clock.tick(), 0.05f);
            gfx.pollEvents();

            while (true) {
                var opt = window.pollEvent();
                if (opt.isEmpty()) break;
                if (opt.get() instanceof WindowEvent.Closed) gfx.window().requestClose();
            }

            float moveX = (window.isKeyDown(Key.D) ? 1f : 0f) - (window.isKeyDown(Key.A) ? 1f : 0f);
            float moveY = (window.isKeyDown(Key.S) ? 1f : 0f) - (window.isKeyDown(Key.W) ? 1f : 0f);
            playerX += moveX * 280f * dt;
            playerY += moveY * 280f * dt;

            pickupX += pickupVx * dt;
            pickupY += pickupVy * dt;
            if (pickupX < 40f || pickupX > 860f) pickupVx = -pickupVx;
            if (pickupY < 40f || pickupY > 560f) pickupVy = -pickupVy;

            Circle2f playerHit = new Circle2f(playerX, playerY, 28f);
            if (playerHit.overlaps(new Circle2f(pickupX, pickupY, 16f))) {
                score++;
                pickupX = 100f + (float) (Math.random() * 700f);
                pickupY = 80f + (float) (Math.random() * 440f);
                if (click != null) click.play();
            }

            hud.setContent("Score: " + score);
            player.setPosition(playerX, playerY);
            pickup.setPosition(pickupX, pickupY);

            var size = gfx.getSize();
            camera.setCenter(size.width() / 2f, size.height() / 2f);
            camera.setSize(size.width(), size.height());

            if (audio != null) audio.update();

            gfx.clear(new Color(14, 16, 22));
            gfx.draw(pickup);
            gfx.draw(player);
            gfx.draw(hud, DrawState.DEFAULT.withLayer(5));
            gfx.present();
        }

        font.dispose();
        if (audio != null) audio.dispose();
        gfx.dispose();
    }

    private static AudioContext tryAudio() {
        try {
            return new AudioContext();
        } catch (RuntimeException ex) {
            System.err.println("Audio skipped: " + ex.getMessage());
            return null;
        }
    }
}
```

::: warning Do not block inside the loop
Sleeping the main thread or running heavy work synchronously drops frames and balloons `dt`. Offload long tasks or split work across frames.
:::

## Putting it all together

| Tutorial topic | Appears in the skeleton as |
|----------------|----------------------------|
| Window / events | `Window`, `pollEvents`, `WindowEvent.Closed` |
| Shapes | `Circle` player and pickup |
| Text + layers | `Text` HUD with `withLayer(5)` |
| Transforms | `setOrigin` on circles |
| Camera | `camera.setCenter` / `setSize` each frame |
| Collision | `Circle2f.overlaps` for pickup collection |
| Audio | optional `Sound` on collect, `audio.update()` |
| Clock | `clock.tick()` drives movement |

For a production-scale sample that also uses offscreen rendering, splines, and camera zoom, run the Gloom demo and read the walkthrough:

**[Full Demo](/examples/full-demo)** — `org.gloom.Launcher` via `./gradlew run`

## What you learned

- A game loop measures `dt`, processes input, updates state, renders, and presents.
- `Clock.tick()` provides variable delta time in seconds for frame-rate-independent motion.
- Update and draw are separate concerns even in a single-threaded loop.
- Fonts, audio, and graphics each have explicit disposal at shutdown.

**Next:** [Full Demo](/examples/full-demo) — see every LLW feature composed in one application.
