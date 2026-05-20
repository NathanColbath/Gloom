# Formats & Loading

LLW decodes audio through `org.llw.audio.resources.AudioLoader` into **16-bit signed PCM** suitable for OpenAL upload.

## Supported containers

| Extension | Decoder | Typical use |
|-----------|---------|-------------|
| `.wav` | `javax.sound.sampled` (Java Sound) | SFX, UI clicks |
| `.ogg` | STB Vorbis (`STBVorbis`) | Music, long ambience |

Any WAV format Java Sound can transcode to 16-bit PCM works — including 8/16/24-bit PCM and common float WAVs. The loader normalizes to 16-bit signed little-endian interleaved samples.

::: tip File extension matters
`AudioLoader` picks the decoder from the **filename extension** (classpath path or `Path.getFileName()`). Use `.wav` / `.ogg` suffixes even when loading from memory: `loadFromMemory("explosion.wav", bytes)`.
:::

## SoundBuffer vs streaming

| Path | API | Memory |
|------|-----|--------|
| Full decode | `audio.loadSoundBuffer(...)` | Entire file in RAM + AL buffer |
| Stream | `audio.openMusic(...)` | Small rolling buffer via `AudioStream` |

OGG can be loaded either way. Short OGG stingers belong in `SoundBuffer`; soundtracks belong in `Music`.

```java
// Whole file in memory — fine for a 2-second sting
SoundBuffer sting = audio.loadSoundBuffer("assets/sting.ogg");

// Stream a 5-minute track
Music theme = audio.openMusic("assets/theme.ogg");
```

## Classpath layout

Bundled samples ship under `llw/src/main/resources/llw/audio/samples/`:

```
llw/audio/samples/click.wav
llw/audio/samples/ambient.ogg
```

Reference them without a leading slash:

```java
audio.loadSoundBuffer("llw/audio/samples/click.wav");
```

## Error handling

Missing resources throw `IllegalArgumentException` with the path. Corrupt or unsupported data throws `IllegalArgumentException` (WAV) or `IllegalStateException` wrapping `IOException` (filesystem).

::: warning No MP3
MP3 is not supported. Convert assets to OGG (music) or WAV (SFX) during your content pipeline.
:::

## Low-level types

| Type | Role |
|------|------|
| `PcmData` | Decoded samples + channel count + sample rate |
| `AudioStream` | Incremental decode for `Music` / `StreamPlayer` |
| `AudioLoader` | Static load/open helpers |

Application code normally goes through `AudioContext` rather than calling `AudioLoader` directly. See [Resources](/audio/resources) for when to use the lower layer.

## See also

- [Sound Buffer](/audio/sound-buffer)
- [Music](/audio/music)
- [Resources](/audio/resources)
