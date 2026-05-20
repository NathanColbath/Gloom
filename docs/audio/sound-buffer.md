# Sound Buffer

`SoundBuffer` holds decoded PCM uploaded to a single OpenAL buffer. It corresponds to `sf::SoundBuffer` in SFML.

## Loading

```java
AudioContext audio = new AudioContext();

// Classpath (bundled under src/main/resources/)
SoundBuffer click = audio.loadSoundBuffer("llw/audio/samples/click.wav");

// Filesystem
SoundBuffer boom = audio.loadSoundBuffer(Path.of("assets/explosion.wav"));

```

Prefer `audio.loadSoundBuffer(...)` for all application loading. Lower-level `SoundBuffer.fromMemory` exists for tests and custom loaders that already hold encoded bytes.

## Metadata

```java
float seconds = click.getDuration().asSeconds();
int hz = click.getSampleRate();
int ch = click.getChannelCount();   // 1 = mono, 2 = stereo
```

Use duration for UI timing or animation sync. Sample rate and channel count reflect the decoded PCM, not the original file container.

## Sharing buffers

One `SoundBuffer` can feed many `Sound` instances:

```java
SoundBuffer buffer = audio.loadSoundBuffer("llw/audio/samples/click.wav");

Sound a = audio.createSound();
a.setBuffer(buffer);

Sound b = audio.createSound();
b.setBuffer(buffer);

a.play();
b.play();   // both play simultaneously
```

::: tip Memory vs voices
Buffers are the expensive part (full PCM in GPU/driver memory). Sources are cheap — create as many `Sound` objects as you need for polyphony.
:::

## Lifetime rules

A buffer must **outlive** every `Sound` that references it.

1. `stop()` all sounds using the buffer.
2. Call `buffer.dispose()` to delete the OpenAL buffer.
3. Do not call `setBuffer` with a disposed buffer.

::: warning Premature dispose
Disposing a buffer while a source still references it produces OpenAL errors or silent playback failure. Always stop sounds first.
:::

## When to use buffers vs music

| Use `SoundBuffer` | Use `Music` |
|-------------------|-------------|
| SFX under ~10 seconds | Long BGM / ambience |
| Needs random access / instant replay | Sequential streaming |
| Many overlapping one-shots | One stream per file |

See [Formats](/audio/formats) for supported encodings.

## See also

- [Sounds](/audio/sounds)
- [Audio Context](/audio/audio-context)
- [Resources](/audio/resources)
