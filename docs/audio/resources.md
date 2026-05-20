# Audio Resources

The `org.llw.audio.resources` package handles file I/O and PCM layout. Most games interact only with `AudioContext`, but these types explain what happens under the hood.

## Package map

| Type | Responsibility |
|------|----------------|
| `AudioLoader` | Decode WAV/OGG from classpath, file, or memory |
| `PcmData` | Immutable decoded buffer + metadata |
| `AudioStream` | Incremental decode for streaming playback |

## AudioLoader

```java
import org.llw.audio.resources.AudioLoader;
import org.llw.audio.resources.PcmData;

PcmData pcm = AudioLoader.loadFromClasspath("llw/audio/samples/click.wav");
float duration = pcm.durationSeconds();
int channels = pcm.channels();
int rate = pcm.sampleRate();
ByteBuffer samples = pcm.preparedSamples();  // native byte order, rewound
```

Streaming entry points return an `AudioStream` handle:

```java
AudioStream stream = AudioLoader.openStreamFromFile(Path.of("theme.ogg"));
// consumed internally by Music / StreamPlayer
stream.close();
```

::: details preparedSamples()
`PcmData` stores samples in a direct `ByteBuffer`. `preparedSamples()` duplicates, sets native byte order, and resets position — safe to pass to OpenAL without mutating the record's internal state.
:::

## PcmData layout

- **Format:** 16-bit signed, little-endian, interleaved channels
- **Duration:** `frameCount / sampleRate` where `frameCount = bytes / (2 * channels)`

`SoundBuffer` uploads this layout with `OpenAlBackend.createBuffer`.

## AudioStream

`AudioStream` implements `AutoCloseable` and decodes chunks on demand:

- **WAV:** reads through `AudioInputStream`, converting to target PCM on the fly
- **OGG:** uses `STBVorbis` decode calls per refill

`StreamPlayer` (package-private) pulls from `AudioStream` each frame when `AudioContext.update()` runs.

::: warning Do not share one stream
Each `Music` instance owns its stream factory. Opening the same file twice creates independent streams — required for crossfading two music tracks.
:::

## Custom pipelines

If you generate PCM procedurally, you can still use LLW's upload path:

1. Build a direct `ByteBuffer` of 16-bit LE interleaved samples.
2. Wrap with `new PcmData(buffer, channels, sampleRate)`.
3. Use `SoundBuffer` static factories with a backend from a live `AudioContext` (advanced).

For most procedural audio, pre-render to a WAV on disk or in memory and use `loadFromMemory`.

## See also

- [Formats & Loading](/audio/formats)
- [Sound Buffer](/audio/sound-buffer)
- [Audio Context](/audio/audio-context)
