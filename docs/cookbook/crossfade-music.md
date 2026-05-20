# Crossfade Music

## Problem

Switching tracks with `stop()` then `play()` causes an audible gap. Level transitions and menu-to-game handoffs need one track to fade out while another fades in over the same interval.

## Solution

Open two `Music` streams from `AudioContext`, start the outgoing track at full volume and the incoming track silent, then lerp volumes each frame with `Interpolator.linear`. Call `AudioContext.update()` every frame so both streams keep refilling their buffers.

```java
import org.llw.audio.AudioContext;
import org.llw.audio.Music;
import org.llw.math.interpolation.Interpolator;

AudioContext audio = new AudioContext();
Music current = audio.openMusic("llw/audio/samples/ambient.ogg");
Music next = audio.openMusic("llw/audio/samples/ambient.ogg"); // second track path

current.setLooping(true);
current.setVolume(100f);
current.play();

float crossfadeDuration = 2f;
float crossfadeT = 0f;
boolean crossfading = false;

void startCrossfade(Music incoming) {
    next = incoming;
    next.setLooping(true);
    next.setVolume(0f);
    next.play();
    crossfadeT = 0f;
    crossfading = true;
}

void updateAudio(float dt) {
    audio.update();

    if (!crossfading) {
        return;
    }

    crossfadeT += dt / crossfadeDuration;
    float t = Math.min(crossfadeT, 1f);

    current.setVolume(Interpolator.linear(100f, 0f, t));
    next.setVolume(Interpolator.linear(0f, 100f, t));

    if (t >= 1f) {
        current.stop();
        current = next;
        crossfading = false;
    }
}
```

Trigger from gameplay:

```java
Music bossTheme = audio.openMusic("assets/boss.ogg");
startCrossfade(bossTheme);
```

Volume is on a **0–100** scale (`AbstractPlaybackSource.setVolume`). Both tracks can play simultaneously during the fade because each `Music` instance owns its own streaming pipeline.

::: details Variations

- **Ease the fade:** Replace raw `t` with `Easing.EASE_IN_OUT_CUBIC.evaluate(t)` before lerping volumes for a softer midpoint.
- **Overlap offset:** Start `next.play()` 0.5 s before the fade begins so streaming buffers are primed — reduces the chance of a quiet first frame.
- **One-shot stinger:** Do not set `setLooping(true)` on short jingles; stop `current` after fade and let `next` finish naturally.
- **Global gain:** Multiply both lerped values by `AudioListener.getGlobalVolume()` if you expose a master slider.

:::

## Pitfalls

- **Skipping `audio.update()`:** Streaming stalls or stutters — both tracks need per-frame `update()` while either is playing.
- **Reusing `current` after `stop()`:** After crossfade, reassign `current = next` so the active reference matches what is audible.
- **Opening too many `Music` objects:** Each open track uses an OpenAL source from the same 32-source pool as `Sound` effects.
- **Identical files for testing:** Use two distinct OGG paths in production; duplicate opens of one file are only useful to verify the fade curve.

## See also

- [Music](/audio/music)
- [Sound Pool](/cookbook/sound-pool)
- [Easing](/math/easing)
