# Asset Packs (LLWP)

`asset.pack` files bundle many assets into one binary with a JSON manifest and concatenated payload. Build packs at dev time with `AssetPackWriter`; load at runtime with `ResourceManager.loadPackFile` or `loadPackClasspath`.

## File layout (v1)

```
Offset  Size     Content
0       4        Magic ASCII 'LLWP'
4       4        uint32 version (= 1)
8       4        uint32 jsonLength
12      N        UTF-8 JSON manifest
12+N    ...      Concatenated raw file bytes
```

Payload offsets in the manifest are relative to the start of the concatenated data (byte `12 + jsonLength`).

## Manifest example

```json
{
  "version": 1,
  "entries": {
    "player": { "type": "texture", "offset": 0, "length": 4821, "hint": "player.png" },
    "ui_font": { "type": "font", "offset": 4821, "length": 120044, "hint": "Roboto.ttf", "fontSize": 24 },
    "click": { "type": "sound", "offset": 124865, "length": 8822, "hint": "click.wav" }
  }
}
```

- `hint` — original filename; extension matters for audio decode.
- `fontSize` — required when `type` is `font`.

## Writing a pack

```java
import org.llw.resources.AssetType;
import org.llw.resources.pack.AssetPackManifest.PackEntry;
import org.llw.resources.pack.AssetPackWriter;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

Map<String, PackEntry> entries = new LinkedHashMap<>();
entries.put("player", new PackEntry(AssetType.TEXTURE, Path.of("assets/player.png")));
entries.put("ui_font", new PackEntry(AssetType.FONT, Path.of("assets/Roboto.ttf"), 24));
entries.put("click", new PackEntry(AssetType.SOUND, Path.of("assets/click.wav")));

AssetPackWriter.write(Path.of("build/game.pack"), entries);
```

## Loading a pack

```java
resources.loadPackFile(Path.of("build/game.pack"));
// or
resources.loadPackClasspath("packs/game.pack");

try (var tex = resources.acquireTexture("player")) {
    sprite.setTexture(tex.get());
}
```

`AssetPackReader` validates magic/version and slices payload bytes without temp files.

## Out of scope (v1)

- Encryption or compression (raw concatenation only)
- Background loading threads (OpenGL remains main-thread)

## See also

- [Resource Manager](/resources/resource-manager)
- [Directory Scan](/resources/directory-scan)
