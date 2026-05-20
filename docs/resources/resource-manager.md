# Resource Manager

`ResourceManager` is the public entry point for registering assets and acquiring reference-counted handles.

## Construction

```java
ResourceManager resources = new ResourceManager(gfx.backend(), audio);
```

Requires an initialized `OpenGlBackend` (from `GraphicsContext.backend()`) and `AudioContext`.

On construction, the manager automatically indexes installed **system fonts** on supported platforms (Windows today). No manual registration is required for OS fonts.

## Registration (fluent)

| Method | Registers |
|--------|-----------|
| `registerTexture(id, classpathPath)` | Classpath PNG/JPEG |
| `registerTextureFile(id, path)` | Filesystem image |
| `registerFont(id, classpathPath, pixelHeight)` | Classpath TTF/OTF font |
| `registerFontFile(id, path, pixelHeight)` | Filesystem TTF/OTF font |
| `registerSound(id, classpathPath)` | Decoded into `SoundBuffer` |
| `registerMusic(id, classpathPath)` | Streaming music path |
| `registerDirectory(idPrefix, dir, opts)` | Walk filesystem tree |
| `registerClasspathDirectory(idPrefix, dir, opts)` | Via `dir/_index.json` |
| `loadPackClasspath(packPath)` | LLWP pack on classpath |
| `loadPackFile(path)` | LLWP pack file |

All registration methods return `this` for chaining.

## Loading policy

```java
resources.setLazyLoading(true);   // default: load on first acquire
resources.loadAll();              // pin all non-music assets
resources.unloadAll();            // unpin manager pins
resources.isLoaded("player");     // GPU/AL resident?
resources.refCount("player");     // active refs (debug)
```

## Acquire / release

```java
try (AssetRef<Texture2d> tex = resources.acquireTexture("player")) {
    sprite.setTexture(tex.get());
}

AssetRef<SoundBuffer> sfx = resources.acquireSound("click");
sound.setBuffer(sfx.get());
sfx.release(); // or sfx.close()
```

- `acquireTexture`, `acquireFont`, `acquireSound`, `acquireRaw` — load if needed, increment ref count, return `AssetRef`.
- `acquireSystemFont(family, pixelHeight)` / `acquireSystemFont(family, style, pixelHeight)` — resolve an indexed OS font at the requested raster size (lazy registration).
- `systemFontFaces()` — face names from the auto-indexed catalog (e.g. `Segoe UI`, `Segoe UI.bold`).
- `hasSystemFontFace(family, style)` — whether a face exists before acquire.
- `AssetRef.close()` / `release()` — decrement ref count; idempotent second release is a no-op.
- Do **not** call `texture.dispose()` on acquired objects — only release the ref or dispose the manager.

## Pinning

```java
resources.pin("ui_atlas");    // internal ref++, no AssetRef returned
resources.unpin("ui_atlas");  // may unload at refCount 0
```

`loadAll()` is equivalent to pinning every registered non-music asset.

## Music

```java
Music theme = resources.openMusic("theme");
theme.setLooping(true);
theme.play();
// Stop before resources.dispose() — streams are caller-owned.
```

## Shutdown

```java
resources.dispose(); // force-unload all, clear registrations
```

Also implements `AutoCloseable` (`close()` → `dispose()`).

## See also

- [Asset Lifecycle](/resources/asset-lifecycle)
- [Asset Packs](/resources/asset-packs)
- [Resource Loading (low-level)](/render/resource-loading)
