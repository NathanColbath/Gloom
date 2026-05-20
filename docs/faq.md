# FAQ

Troubleshooting and frequently asked questions for LLW (`org.llw.*`) on LWJGL.

## Window & rendering

### Black or frozen window

**Symptoms:** Window opens but stays black; or only the first frame appears.

**Causes & fixes:**

1. Missing `gfx.present()` each frame — drawing only enqueues geometry; `present()` flushes and swaps buffers.
2. Missing `gfx.clear(color)` — cleared color may be black; still call `clear` before draw.
3. Exception during draw — check stderr; a thrown error mid-loop can skip `present()`.

### Nothing draws but no error

- Camera center/size may place content off-screen. Log sprite position and `camera.getCenter()`.
- Zero-alpha color on sprites/shapes — verify `Color` alpha channel.
- Drawing to offscreen target without blitting the result to the main target.

### Wrong size after resize

Use live dimensions:

```java
IntSize size = gfx.getSize();
```

`WindowSettings.width()` reflects **initial** size only. On `RESIZED` events, update camera:

```java
camera.setCenter(size.width() / 2f, size.height() / 2f);
camera.setSize(size.width(), size.height());
```

### Mouse click does not hit sprite

Raw `window.mousePosition()` is in **screen pixels**. With a zoomed/panned camera:

```java
Vector2f world = camera.screenToWorld(window.mousePosition(), gfx.getSize());
```

See [Coordinates](/best-practices/coordinates).

### Text does not appear

- Font failed to load — follow the null-check pattern in `Launcher`.
- Text color matches background.
- Position off-screen after camera transform.

---

## Natives & classpath

### `UnsatisfiedLinkError` / GLFW or OpenAL failed to load

1. Add LWJGL **natives** classifier for your OS (`natives-windows`, `natives-macos-arm64`, etc.).
2. Set `-Dorg.lwjgl.system.SharedLibraryExtractPath=build/lwjgl-natives` in IDE runs.
3. Run via `./gradlew run` to match project JVM args.

See [IDE & Natives](/best-practices/ide-and-natives).

### Resource not found on classpath

Paths are relative to the classpath root **without** a leading slash:

```java
// Correct
"llw/audio/samples/click.wav"

// Wrong
"/llw/audio/samples/click.wav"
```

Confirm the file lives under `src/main/resources/` in the module on the runtime classpath.

### Filesystem path works in Gradle but not IDE

IDE working directory may differ. Use Gradle run configuration with working directory = project root, or prefer classpath resources.

---

## Audio

### No sound at all

- OpenAL natives missing — same fix as GLFW natives.
- `AudioContext` created before GLFW init — create audio after window/GLFW is up.
- Master volume zero — `AudioListener.getGlobalVolume()`.
- `play()` on `Sound` without `setBuffer` — silent no-op.

### Music starts then stops / stutters

Call `audio.update()` **every frame** while `Music` is playing. Streaming refills happen in `update()`, not in `play()`.

### SFX cuts out under heavy fire

OpenAL source pool is capped at **32** voices. Implement a sound pool — [Sound Pool](/cookbook/sound-pool).

### WAV fails to load

Java Sound must decode the WAV. Exotic compression (some ADPCM, float oddities) may fail. Re-export as 16-bit PCM WAV.

### Unsupported format

MP3 and FLAC are not supported. Use OGG for music, WAV for SFX — [Formats](/audio/formats).

---

## Math & collision

### Angles feel inverted

LLW uses **Y-down**. Positive rotation is clockwise from +X. Use `org.llw.math.util.Angle` for degree/radian conversion.

### `getPosition()` changes do not move sprite

`getPosition()` on renderables returns a **copy**. Call `setPosition(x, y)`.

### SAT test wrong for rotated rects

`Sat2f` expects convex polygon vertices in world space. Axis-aligned `RectF` tests should use `Intersection2.intersects(rectA, rectB)` unless rotated.

---

## Build & modules

### Package not found `org.llw`

Add `implementation(project(":llw"))` or depend on the published JAR plus LWJGL stack.

### Migrating from `org.gloom.*`

| Old | New |
|-----|-----|
| `org.gloom.renderbackend` | `org.llw.render` |
| `org.gloom.audiobackend` | `org.llw.audio` |
| `org.gloom.math` | `org.llw.math` |

Full mapping: [SFML Migration](/sfml-migration) (SFML ↔ LLW) and [Project Setup](/guide/project-setup).

---

## Performance

### Low FPS with few sprites

- Unique texture per sprite prevents batching — use atlases.
- Creating `Texture2d` or `SoundBuffer` per frame — load once.
- Uncached text layout every frame — update string only when content changes.

See [Performance](/best-practices/performance).

---

## Still stuck?

1. Run the Gloom demo: `./gradlew run` — if it works, compare your `main` loop to `org.gloom.Launcher`.
2. Enable `-Dorg.lwjgl.util.Debug=true` for native loader logs.
3. Check [Getting Started](/guide/getting-started) and module Javadoc at `/api/javadoc`.
