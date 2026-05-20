# Audio Overview

`org.llw.audio` provides SFML-style spatial audio on OpenAL via LWJGL.

## In this section

| Page | Topics |
|------|--------|
| [Audio Context](/audio/audio-context) | Device init, source pool, `update()`, disposal |
| [Sounds](/audio/sounds) | Short effects from `SoundBuffer` |
| [Sound Buffer](/audio/sound-buffer) | Loading, sharing, lifetime |
| [Music](/audio/music) | Streaming OGG/WAV, frame pump |
| [Playback](/audio/playback) | Volume, pitch, pan, seek, 3D |
| [Listener](/audio/listener) | Master gain, orientation |
| [Formats & Loading](/audio/formats) | WAV, OGG, classpath paths |
| [Resources](/audio/resources) | `AudioLoader`, `PcmData`, `AudioStream` |

## Key types

| Type | Purpose |
|------|---------|
| `AudioContext` | Entry point — init OpenAL, load assets, create sounds |
| `Sound` | Short effect played from a `SoundBuffer` |
| `Music` | Streaming OGG/Vorbis (and WAV) music |
| `SoundBuffer` | Decoded PCM loaded into an AL buffer |
| `AudioListener` | Static 3D listener + master volume |

## Quick example

```java
AudioContext audio = new AudioContext();
SoundBuffer click = audio.loadSoundBuffer("llw/audio/samples/click.wav");

Sound sfx = audio.createSound();
sfx.setBuffer(click);
sfx.play();

// each frame:
audio.update();
```

## Loading from disk

```java
SoundBuffer buf = audio.loadSoundBuffer(Path.of("assets/explosion.wav"));
Music music = audio.openMusic(Path.of("assets/theme.ogg"));
```

::: tip Construct after GLFW
Create `AudioContext` on the main thread after GLFW can load natives — typically alongside `GraphicsContext`.
:::

::: warning Always pump music
`Music` requires `audio.update()` every frame. Short `Sound` effects still benefit from a unified loop call.
:::

## Common pitfalls

- Call `audio.update()` every frame to pump streaming music.
- `AudioContext` must be created **after** LWJGL can load OpenAL natives.
- Dispose `AudioContext` on shutdown; dispose `SoundBuffer` instances you no longer need.

## See also

- [Audio Demo](/examples/audio-demo)
- [Sound Pool](/cookbook/sound-pool)
- [SFML Migration — Audio](/sfml-migration#audio)
