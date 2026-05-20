# Tutorial 9 — Audio

## Goal

Initialize `AudioContext`, play short sound effects with `Sound` + `SoundBuffer`, stream background music with `Music`, and call `audio.update()` every frame so streaming stays healthy.

## Prerequisites

- Completed [Tutorial 8 — Camera & Views](/tutorials/08-camera) (or any tutorial with a running game loop)
- Working OpenAL natives (the Gloom Gradle run task configures LWJGL extraction automatically)

::: details Render vs audio lifetimes
Create `AudioContext` **after** GLFW/LWJGL can load native libraries — typically right after `GraphicsContext` in `main`. Audio and graphics shut down independently: `audio.dispose()` then `gfx.dispose()`.
:::

## Step 1 — Create the audio context

```java
import org.llw.audio.AudioContext;

AudioContext audio = new AudioContext();
```

`AudioContext` opens the default OpenAL device and a pool of sources. If initialization fails on a given machine, wrap construction in try/catch and continue without audio (the Gloom demo does this).

::: warning Headless / CI environments
Servers without a sound device may throw at `new AudioContext()`. Treat audio as optional unless your game hard-requires it.
:::

## Step 2 — Load a sound buffer and play effects

Short WAV clips are fully decoded into GPU-style AL buffers:

```java
import org.llw.audio.Sound;
import org.llw.audio.SoundBuffer;

SoundBuffer clickBuf = audio.loadSoundBuffer("llw/audio/samples/click.wav");

Sound click = audio.createSound();
click.setBuffer(clickBuf);
click.play();
```

`Sound` maps to SFML's `sf::Sound`: one buffer, many simultaneous plays by acquiring sources from the pool. Re-trigger on input:

```java
if (event instanceof WindowEvent.KeyPressed k && k.key() == Key.SPACE) {
    click.play();
}
```

Volume and pitch use a 0–100 style API shared with music:

```java
click.setVolume(85f);
click.setPitch(1.1f);
```

## Step 3 — Stream music with `Music`

OGG/Vorbis files stream from disk or classpath without loading the entire file into RAM:

```java
import org.llw.audio.Music;

Music ambient = audio.openMusic("llw/audio/samples/ambient.ogg");
ambient.setLooping(true);
ambient.setVolume(35f);
ambient.play();
```

Open one `Music` instance per simultaneous stream. Layering two tracks means two `openMusic` calls.

## Step 4 — Call `update()` every frame

Streaming music decodes the next chunk during `AudioContext.update()`:

```java
while (gfx.isActive()) {
    gfx.pollEvents();
    // handle input ...

    audio.update();   // required while Music is playing

    gfx.clear(background);
    gfx.present();
}

audio.dispose();
```

::: tip Where in the loop?
Call `audio.update()` once per frame after input handling and before or after rendering — order does not matter as long as it runs exactly once while music is active.
:::

Skipping `update()` causes music to stall or stutter even though `play()` succeeded.

## Step 5 — Loading from the filesystem

The same APIs accept `Path` for assets outside the JAR:

```java
import java.nio.file.Path;

SoundBuffer explosion = audio.loadSoundBuffer(Path.of("assets/sfx/explosion.wav"));
Music theme = audio.openMusic(Path.of("assets/bgm/theme.ogg"));
```

## Step 6 — Shutdown

`audio.dispose()` stops active music, releases sources, and closes the device. Dispose audio before exiting `main`:

```java
audio.dispose();
gfx.dispose();
```

## Full class

```java
import org.llw.audio.AudioContext;
import org.llw.audio.Music;
import org.llw.audio.Sound;
import org.llw.audio.SoundBuffer;
import org.llw.render.core.Color;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.window.Key;
import org.llw.render.window.Window;
import org.llw.render.window.WindowEvent;
import org.llw.render.window.WindowSettings;

public class AudioTutorial {
    public static void main(String[] args) {
        Window window = new Window(new WindowSettings().title("LLW Audio").size(800, 600));
        GraphicsContext gfx = new GraphicsContext(window);

        AudioContext audio;
        try {
            audio = new AudioContext();
        } catch (RuntimeException ex) {
            System.err.println("Audio unavailable: " + ex.getMessage());
            gfx.dispose();
            return;
        }

        SoundBuffer clickBuf = audio.loadSoundBuffer("llw/audio/samples/click.wav");
        Sound click = audio.createSound();
        click.setBuffer(clickBuf);
        click.setVolume(90f);

        Music ambient = audio.openMusic("llw/audio/samples/ambient.ogg");
        ambient.setLooping(true);
        ambient.setVolume(30f);
        ambient.play();

        while (gfx.isActive()) {
            gfx.pollEvents();

            while (true) {
                var opt = window.pollEvent();
                if (opt.isEmpty()) break;
                WindowEvent e = opt.get();
                if (e instanceof WindowEvent.Closed) {
                    gfx.window().requestClose();
                } else if (e instanceof WindowEvent.KeyPressed k) {
                    if (k.key() == Key.SPACE) {
                        click.play();
                    } else if (k.key() == Key.M) {
                        if (ambient.isPlaying()) ambient.pause();
                        else ambient.play();
                    }
                }
            }

            audio.update();

            gfx.clear(new Color(20, 22, 30));
            gfx.present();
        }

        audio.dispose();
        gfx.dispose();
    }
}
```

::: details Sound vs Music — when to use which
| Type | Best for | Loaded via |
|------|----------|------------|
| `Sound` + `SoundBuffer` | UI clicks, gunshots, short loops | `loadSoundBuffer` (full decode) |
| `Music` | Background tracks, long ambient beds | `openMusic` (streaming) |
:::

## What you learned

- `AudioContext` is the entry point for OpenAL in LLW.
- `SoundBuffer` holds decoded PCM; `Sound` plays it through a pooled source.
- `Music` streams OGG from classpath or disk and must be registered with the context.
- `audio.update()` must run once per frame while streaming music is active.

**Next:** [Tutorial 10 — Game Loop](/tutorials/10-game-loop)
