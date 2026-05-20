# Resources Overview

`org.llw.resources` centralizes asset registration, reference-counted loading, and optional **LLWP** pack bundling for textures, fonts, sounds, music paths, and raw byte slices.

## When to use `ResourceManager`

| Approach | Best for |
|----------|----------|
| Direct loaders (`Texture2d.fromClasspath`, `audio.loadSoundBuffer`) | One-off assets, tutorials, tiny demos |
| `ResourceManager` | Games with many assets, level unload, pack shipping, shared lifecycle |

`ResourceManager` delegates to `ResourceLoader`, `Texture2d`, `Font`, and `AudioContext` internally — it does not replace those APIs, it orchestrates them.

## Core concepts

- **Registration** — assign a string id to a classpath path, filesystem path, pack slice, or bulk directory scan.
- **Acquire / release** — `AssetRef<T>` bumps a per-asset ref count; at zero refs GPU/OpenAL objects are disposed automatically while the descriptor remains for re-acquire.
- **Pin / `loadAll()`** — internal pins keep assets warm without handing out `AssetRef` instances (eager warmup).
- **Music** — streaming only; `openMusic(id)` is caller-owned and not ref-counted.

## Package layout

```
org.llw.resources/
  ResourceManager, AssetRef, AssetDescriptor, AssetHandle, AssetType, AssetSource
  DirectoryScanOptions
  pack/  AssetPackFormat, ManifestJson, AssetPackWriter, AssetPackReader
```

## Quick start

```java
import org.llw.audio.AudioContext;
import org.llw.resources.AssetRef;
import org.llw.resources.ResourceManager;
import org.llw.render.graphics.Font;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.graphics.Texture2d;

GraphicsContext gfx = new GraphicsContext(window);
AudioContext audio = new AudioContext();
ResourceManager resources = new ResourceManager(gfx.backend(), audio);

resources
    .registerTexture("player", "assets/player.png")
    .registerFont("ui", "llw/render/fonts/Roboto-Regular.ttf", 24)
    .registerSound("click", "llw/audio/samples/click.wav")
    .registerMusic("theme", "assets/theme.ogg");

try (AssetRef<Texture2d> player = resources.acquireTexture("player")) {
    sprite.setTexture(player.get());
    gfx.draw(sprite);
}
```

## See also

- [Resource Manager API](/resources/resource-manager)
- [Asset Lifecycle](/resources/asset-lifecycle)
- [Asset Packs](/resources/asset-packs)
- [Directory Scan](/resources/directory-scan)
- [Cookbook: Resource Manager](/cookbook/resource-manager)
