# Project format

An LLW Studio **project** is a folder with a manifest, content tree, and generated metadata.

## Required layout

```
MyGame/
  MyGame.llwproj          # Manifest
  Assets/                 # Author content
  Scenes/                 # Scene JSON
  .studio/                # Editor + generated metadata
  .llw/sdk/               # TypeScript SDK (generated)
```

## Manifest (`*.llwproj`)

JSON fields include:

- Project display name
- **Startup scene** path (e.g. `Scenes/Main.scene.json`)

Written by **File → Save Project**.

## Metadata (`.studio/metadata/`)

| Subfolder | Purpose |
|-----------|---------|
| `assets/` | `*.meta` per asset: GUID, `AssetType`, importer settings |
| `script-cache/` | Bundled JS per script GUID |
| `script-schemas/` | Inspector field layout per script |
| `logs/` | Editor log files |

Legacy `Assets/**/*.meta` sidecars are migrated into `.studio/metadata/assets/` on open.

## GUID stability

Scenes, prefabs, and components reference assets by **GUID**, not file path. Renaming files updates path keys in metadata while preserving GUID if the meta file moves with the asset.

## TypeScript tooling

`ScriptProjectGenerator` writes:

- `package.json` with `llw.core` path mapping
- `tsconfig.json` for IDE checking
- Copied SDK from `llw-studio/src/main/resources/scripting-sdk/`

## Gitignore suggestions

```
.studio/metadata/script-cache/
.studio/metadata/logs/
.llw/sdk/          # optional; regenerates on open
.studio/imgui.ini  # personal layout
```

## Related

- [Getting started](getting-started.md)
- [Project and assets](project-and-assets.md)
- [Scenes and serialization](scenes-and-serialization.md)
