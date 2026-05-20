# Sounds

`Sound` plays short, fully-loaded effects from a `SoundBuffer`. Maps to `sf::Sound`.

## In this section

- [Sound Buffer](/audio/sound-buffer) — load once, share across voices
- [Playback](/audio/playback) — volume, pitch, pan, spatialization
- [Audio Context](/audio/audio-context) — `createSound()` and source pool

## Load and play

```java
AudioContext audio = new AudioContext();
SoundBuffer buffer = audio.loadSoundBuffer("llw/audio/samples/click.wav");

Sound click = audio.createSound();
click.setBuffer(buffer);
click.setVolume(80f);
click.play();
```

## Multiple instances

Each `createSound()` returns an independent voice. Reuse one `SoundBuffer` for many simultaneous plays:

```java
for (int i = 0; i < 8; i++) {
    Sound voice = audio.createSound();
    voice.setBuffer(buffer);
    pool.add(voice);
}
```

See [Sound Pool](/cookbook/sound-pool) for voice selection when all sources are busy.

## Controls

| Method | Description |
|--------|-------------|
| `play()` | Start or resume; acquires AL source from pool |
| `pause()` | Pause at current offset |
| `stop()` | Stop, rewind, release source |
| `setVolume(float)` | 0–100 |
| `setPitch(float)` | 0.1–10 playback speed multiplier |
| `setPan(float)` | -1 (left) to 1 (right) |
| `setLooping(boolean)` | Repeat buffer |
| `setPlayingOffset(Time)` | Seek in seconds |
| `isPlaying()` | `status() == PLAYING` |

Inherited from `AbstractPlaybackSource` — full list in [Playback](/audio/playback).

::: warning No buffer, no sound
`play()` returns immediately if `getBuffer()` is null. Set a buffer before playing.
:::

## Common pitfalls

- Sounds must have a buffer set before `play()`.
- WAV loading uses Java Sound; ensure PCM is decodable — see [Formats](/audio/formats).
- Pool exhaustion: at most 32 concurrent AL sources across all sounds and music.

## See also

- [Audio Overview](/audio/overview)
- [Music](/audio/music)
