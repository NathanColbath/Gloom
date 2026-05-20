# Directory Scan

Bulk-register assets from a filesystem tree or a classpath directory manifest.

## Filesystem: `registerDirectory`

Walks a `Path` recursively. File ids are `idPrefix` + relative path without extension, with `/` replaced by `.`:

```
assets/tiles/grass.png  →  idPrefix "game."  →  "game.tiles.grass"
```

### `DirectoryScanOptions`

| Field | Default | Purpose |
|-------|---------|---------|
| `defaultFontPixelHeight` | `24` | Raster size for `.ttf` / `.otf` |
| `musicExtensions` | `{".ogg"}` | Extensions treated as `MUSIC` not `SOUND` |
| `ignored` | `{"_index.json"}` | Skipped filenames |

### Extension rules

| Extension | `AssetType` |
|-----------|-------------|
| `.png`, `.jpg`, `.jpeg` | `TEXTURE` |
| `.ttf`, `.otf` | `FONT` |
| `.wav` | `SOUND` |
| `.ogg` (in `musicExtensions`) | `MUSIC` |
| other `.ogg` | `SOUND` |

```java
import org.llw.resources.DirectoryScanOptions;

resources.registerDirectory("game.", Path.of("assets"), DirectoryScanOptions.DEFAULT);
```

## Classpath: `registerClasspathDirectory`

JARs cannot list directory entries reliably. Use a companion manifest:

`assets/_index.json`:

```json
{
  "files": {
    "tiles/grass.png": "tiles/grass.png",
    "fonts/ui.ttf": "fonts/ui.ttf",
    "sfx/click.wav": "sfx/click.wav"
  }
}
```

```java
resources.registerClasspathDirectory("game.", "assets", DirectoryScanOptions.DEFAULT);
```

Paths in `_index.json` are relative to `assets/` on the classpath.

## See also

- [Resource Manager](/resources/resource-manager)
- [Asset Packs](/resources/asset-packs)
