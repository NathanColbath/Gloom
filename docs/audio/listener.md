# Listener

`AudioListener` controls the OpenAL listener for 3D positional audio and **master gain**. Maps to `sf::Listener`.

Unlike SFML's class instance, LLW exposes **static** methods on `AudioListener`.

## In this section

- [Playback](/audio/playback) — per-source volume and `setRelativeToListener`
- [Audio Context](/audio/audio-context) — initializes global volume to 100

## Master volume

```java
AudioListener.setGlobalVolume(50f);   // 0–100, affects all sources
float master = AudioListener.getGlobalVolume();
```

Per-source `setVolume` multiplies with master gain.

## Default listener

`AudioContext` configures OpenAL with listener at the origin, direction `(0, 0, -1)`, up `(0, 1, 0)` — OpenAL's default forward/down convention.

## Position and orientation

```java
AudioListener.setPosition(0f, 0f, 0f);
AudioListener.setDirection(0f, 0f, -1f);
AudioListener.setUpVector(0f, 1f, 0f);

Vector3f pos = AudioListener.getPosition();   // mutable cached vector
```

Positions use `org.llw.audio.core.Vector3f` — **not** `org.llw.math.vector.Vector2f`.

## 2D games

Many 2D games leave the listener fixed and play non-spatial audio:

```java
sound.setRelativeToListener(true);   // default
sound.setPan(0f);
sound.setVolume(80f);
```

Enable world-space audio when needed:

```java
sound.setRelativeToListener(false);
sound.setPosition(entityX, -entityY, 0f);  // map Y-down render to AL space
```

::: tip Pan without full 3D
For stereo balance without placing sources in 3D, keep relative mode and use `setPan` only.
:::

## See also

- [Audio Overview](/audio/overview)
- [Coordinates](/best-practices/coordinates)
