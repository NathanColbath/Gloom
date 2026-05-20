# Sound Pool

## Problem

Explosions, footsteps, and UI ticks often overlap. Loading the same WAV into memory repeatedly wastes RAM, but a single `Sound` instance cannot play twice at once — starting a new play restarts or blocks the same OpenAL source.

## Solution

Load **one** `SoundBuffer` and bind it to **many** `Sound` instances from `AudioContext.createSound()`. Each `Sound` borrows an independent OpenAL source from the shared pool when `play()` is called. Pick a free voice from your pool, or skip the play if all voices are busy.

```java
import org.llw.audio.AudioContext;
import org.llw.audio.Sound;
import org.llw.audio.SoundBuffer;

AudioContext audio = new AudioContext();
SoundBuffer hitBuffer = audio.loadSoundBuffer("llw/audio/samples/click.wav");

final class SoundPool {
  private final List<Sound> voices = new ArrayList<>();

  SoundPool(AudioContext audio, SoundBuffer buffer, int voiceCount) {
    for (int i = 0; i < voiceCount; i++) {
      Sound sound = audio.createSound();
      sound.setBuffer(buffer);
      voices.add(sound);
    }
  }

  void play(float volume) {
    for (Sound voice : voices) {
      if (!voice.isPlaying()) {
        voice.setVolume(volume);
        voice.play();
        return;
      }
    }
    // All voices busy — steal oldest or drop; dropping is safest
  }
}

SoundPool hits = new SoundPool(audio, hitBuffer, 8);
```

Call `play` from gameplay or input:

```java
if (window.isKeyDown(Key.SPACE)) {
    hits.play(80f);
}
```

Remember the frame-loop contract:

```java
while (running) {
    audio.update(); // required while Music streams are active
    // ...
}
```

`Sound` playback does not need `update()`, but sharing an `AudioContext` with music still does.

**Lifecycle:** Keep the `SoundBuffer` alive until every `Sound` that references it has `stop()`ped. Dispose the buffer only after all voices are stopped.

::: details Variations

- **Round-robin steal:** If every voice is playing, restart `voices.get(nextIndex++)` for machine-gun effects where dropping shots is unacceptable.
- **Per-effect pools:** One buffer + pool for `hit`, another for `footstep`, each with different voice counts.
- **Pitch variation:** Before `play()`, call `setPitch(0.9f + random * 0.2f)` so reused samples feel less repetitive.
- **Lazy voices:** Create sounds on first use up to a max; avoids allocating 32 voices for rare effects.

:::

## Pitfalls

- **Pool size vs OpenAL limit:** `AlSourcePool` caps at **32** total sources shared by every `Sound` and `Music` in the context. Size each pool so the sum across effects and music leaves headroom.
- **`play()` with no buffer:** `Sound.play()` returns immediately if `setBuffer` was never called.
- **Disposing the buffer early:** `SoundBuffer.dispose()` deletes OpenAL data while voices may still reference it — stop all sounds first.
- **Same buffer, many files:** Each unique sample still needs its own `SoundBuffer`; the pool pattern is per-buffer, not global.

## See also

- [Sounds](/audio/sounds)
- [Audio Overview](/audio/overview)
- [Crossfade Music](/cookbook/crossfade-music)
