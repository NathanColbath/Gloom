# Resource Lifecycle

LLW mixes Java objects with native OpenGL and OpenAL resources. Dispose in reverse order of creation.

## Ownership model

| Object | Native backing | Who disposes |
|--------|----------------|--------------|
| `GraphicsContext` | GLFW context, GL state | `gfx.dispose()` |
| `Window` | GLFW window | closed via `GraphicsContext` / manual `close()` |
| `Texture2d`, `Font` | GL textures | `texture.dispose()`, `font.dispose()` |
| `OffscreenTarget` | FBO + textures | `offscreen.dispose()` |
| `AudioContext` | AL device + source pool | `audio.dispose()` |
| `SoundBuffer` | AL buffer | `buffer.dispose()` after sounds stopped |
| `Sound` / `Music` | AL source (pooled) | `stop()`; music also on `AudioContext.dispose()` |

::: warning No GC for GPU/AL
Finalizers are not used. Leaked textures and buffers persist until process exit.
:::

## Recommended shutdown order

```java
// 1. Stop playback
bgm.stop();
for (Sound s : sounds) s.stop();

// 2. Release ResourceManager refs (if used)
levelTextureRef.release();
uiFontRef.release();
resources.dispose(); // force-clear any remaining pins/refs

// 3. Dispose buffers and GPU assets loaded outside the manager
clickBuffer.dispose();
checker.dispose();
offscreen.dispose();

// 4. Dispose subsystems
audio.dispose();
gfx.dispose();
```

When using `ResourceManager`, release all `AssetRef` instances and call `resources.dispose()` **before** `audio.dispose()` and `gfx.dispose()`. Do not manually `dispose()` objects still held by an active `AssetRef`.

## Loading strategy

**Boot:** load fonts, tile atlases, and common SFX once; keep references for the session.

**Level transition:** dispose level-specific textures and buffers; keep shared UI assets.

**Hot reload (dev only):** replace `Texture2d` only after disposing the old instance and ensuring no `Sprite` still references it.

## Classpath vs filesystem

| Source | API | Packaging |
|--------|-----|-----------|
| Classpath | `ResourceLoader`, `audio.loadSoundBuffer(String)` | JAR resources |
| Filesystem | `Path.of(...)`, `Texture2d.fromFile` | Dev mods, user content |

See [Resource Loading](/render/resource-loading) and [Audio Resources](/audio/resources).

::: tip Try-with-resources
`Window` and streams implement `AutoCloseable` where applicable. `GraphicsContext` and `AudioContext` use explicit `dispose()` — wrap them in your own `Game` class with a single `shutdown()` method.
:::

## See also

- [Frame Loop](/best-practices/frame-loop)
- [Error Handling](/best-practices/error-handling)
- [Project Setup](/guide/project-setup)
