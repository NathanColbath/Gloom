# Music

`Music` streams audio without loading the entire file into memory. Maps to `sf::Music`.

## In this section

- [Playback](/audio/playback) — shared transport and gain controls
- [Formats & Loading](/audio/formats) — OGG vs WAV streaming
- [Audio Context](/audio/audio-context) — `openMusic()` and `update()`

## Play looping music

```java
AudioContext audio = new AudioContext();
Music ambient = audio.openMusic("llw/audio/samples/ambient.ogg");
ambient.setLooping(true);
ambient.setVolume(35f);
ambient.play();

while (running) {
    audio.update();  // required each frame
    // ...
}
```

## From filesystem

```java
Music track = audio.openMusic(Path.of("assets/bgm.ogg"));
```

## Layering tracks

Open multiple `Music` instances for ambience + combat layers:

```java
Music base = audio.openMusic("music/base.ogg");
Music combat = audio.openMusic("music/combat.ogg");
combat.setVolume(0f);
// crossfade via setVolume — see Crossfade Music cookbook
```

Each stream consumes one OpenAL source while playing.

## Controls

Same playback API as `Sound`: `play`, `pause`, `stop`, `setVolume`, `setPitch`, `setLooping`, seek offsets.

::: warning update() is mandatory
Streaming refills happen in `AudioContext.update()`. Without it, playback stalls after the first buffer queue drains.
:::

::: details Loop implementation
When a stream ends with looping enabled, `Music` reopens the `AudioStream` factory. Ensure factory paths remain valid for the session.
:::

## Common pitfalls

- **Must** call `AudioContext.update()` while music is playing.
- Only one stream per `Music` instance; open multiple `Music` objects for layering.
- Do not use `Music` for rapid one-shot SFX — use `Sound` + `SoundBuffer`.

## See also

- [Audio Overview](/audio/overview)
- [Sounds](/audio/sounds)
- [Crossfade Music](/cookbook/crossfade-music)
