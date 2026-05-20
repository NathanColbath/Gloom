# Audio Context

`AudioContext` is the entry point for `org.llw.audio`. It owns the OpenAL device, a shared source pool, and helpers for loading buffers and opening music streams.

## Lifecycle

```java
import org.llw.audio.AudioContext;
import org.llw.audio.Sound;
import org.llw.audio.SoundBuffer;

AudioContext audio = new AudioContext();

SoundBuffer click = audio.loadSoundBuffer("llw/audio/samples/click.wav");
Sound sfx = audio.createSound();
sfx.setBuffer(click);
sfx.play();

while (running) {
    audio.update();   // required while Music is active
    // render / game logic ...
}

audio.dispose();
```

`AudioContext` must be constructed **after** LWJGL can load OpenAL natives. In practice, create it on the same thread as your GLFW window — typically right after `GraphicsContext` or at the start of `main`.

::: tip Frame pump
Call `update()` once per frame even if you only play short `Sound` effects. The call is cheap when no `Music` is streaming, and it keeps your loop uniform when you add background tracks later.
:::

## Loading helpers

| Method | Returns | Notes |
|--------|---------|-------|
| `loadSoundBuffer(String classpathPath)` | `SoundBuffer` | Decodes entire file into an AL buffer |
| `loadSoundBuffer(Path path)` | `SoundBuffer` | Filesystem variant |
| `createSound()` | `Sound` | Borrows from the internal source pool on `play()` |
| `openMusic(String classpathPath)` | `Music` | Registered for `update()` |
| `openMusic(Path path)` | `Music` | Filesystem variant |

## Source pool

`AudioContext` pre-allocates up to **32** OpenAL sources (`AlSourcePool`). Each `Sound` and `Music` instance acquires a source when playback starts and returns it on `stop()`.

::: warning Pool exhaustion
If all 32 sources are in use, `Sound.play()` silently does nothing (`source == 0`). For heavy SFX overlap, reuse buffers with multiple `Sound` instances and implement a voice-stealing pool — see [Sound Pool](/cookbook/sound-pool).
:::

## Disposal

`dispose()` stops active music, releases all sources, deletes OpenAL buffers owned by the backend, and closes the device. After disposal, every method except `update()` throws `IllegalStateException`.

::: details What AudioContext does not own
`SoundBuffer` instances you created are **not** automatically deleted. Stop every `Sound` that references a buffer, then call `SoundBuffer.dispose()` yourself if you unload assets mid-session.
:::

## See also

- [Sound Buffer](/audio/sound-buffer)
- [Playback](/audio/playback)
- [Resources](/audio/resources)
