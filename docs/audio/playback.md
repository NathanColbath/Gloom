# Playback

`Sound` and `Music` share playback controls through `AbstractPlaybackSource`. Both map to SFML's `sf::SoundSource` concepts.

## Transport

| Method | `Sound` | `Music` |
|--------|---------|---------|
| `play()` | Starts or resumes from current offset | Opens stream if needed, then plays |
| `pause()` | Pauses AL source | Pauses stream refill |
| `stop()` | Stops and releases source | Stops stream and releases source |
| `status()` | `PlaybackStatus` enum | Same |
| `isPlaying()` | Convenience wrapper | Same |

```java
Sound jump = audio.createSound();
jump.setBuffer(jumpBuffer);
jump.play();

if (jump.status() == PlaybackStatus.PLAYING) {
    jump.setVolume(60f);
}

jump.pause();
jump.play();    // resumes
jump.stop();    // rewinds; source returned to pool
```

## Volume, pitch, and pan

```java
source.setVolume(80f);      // 0–100, linear gain
source.setPitch(1.2f);      // 0.1–10, clamped
source.setPan(-0.5f);       // -1 (left) to 1 (right)
```

`setVolume` on a source multiplies with `AudioListener.getGlobalVolume()` (master gain).

::: details How pan is implemented
LLW maps stereo pan to OpenAL's X position while Y/Z come from `setPosition`. For non-spatial 2D SFX, leave `setRelativeToListener(true)` (the default) and adjust `setPan` only.
:::

## Looping and seeking

```java
source.setLooping(true);

// Seek (requires AL11 — available through LWJGL OpenAL)
source.setPlayingOffset(new Time(12.5f));
Time pos = source.getPlayingOffset();
```

`Music` loop restarts the underlying `AudioStream` when the stream ends. `Sound` loops the attached buffer in hardware.

## Spatial audio

```java
source.setRelativeToListener(false);
source.setPosition(worldX, worldY, 0f);
source.setMinDistance(50f);
source.setMaxDistance(500f);
```

Move `AudioListener.setPosition` with the camera or player. See [Listener](/audio/listener).

::: warning 2D render vs 3D audio
Render math uses `org.llw.math.vector.Vector2f` (Y-down). Audio positions use `org.llw.audio.core.Vector3f` (OpenAL Y-up). Map explicitly: e.g. `setPosition(spriteX, -spriteY, 0f)` if you want screen pixels to match perceived left/right panning.
:::

## Music streaming requirement

```java
Music bgm = audio.openMusic("llw/audio/samples/ambient.ogg");
bgm.setLooping(true);
bgm.play();

while (running) {
    audio.update();   // pumps StreamPlayer buffer queue
}
```

Without `audio.update()`, music buffers drain and playback stalls even though `status()` may still report `PLAYING` briefly.

## See also

- [Sounds](/audio/sounds)
- [Music](/audio/music)
- [Listener](/audio/listener)
- [Crossfade Music](/cookbook/crossfade-music)
