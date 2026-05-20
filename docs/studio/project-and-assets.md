# Project panel and assets

Author-facing content lives under `Assets/`. Import metadata (GUID, type, importer settings) is stored under `.studio/metadata/assets/`, mirroring each asset path.

## Where to find it

**Project** panel — bottom dock by default. **Inspector** switches to asset mode when a Project row is selected and no scene object is selected.

## Asset types

| Type | Extensions / notes |
|------|-------------------|
| Folder | Virtual tree nodes |
| Texture | PNG, JPEG, WebP |
| Sprite | Virtual slice sub-asset of a texture |
| Font | TTF, OTF |
| Audio | WAV, Ogg |
| Scene | `*.scene.json` |
| Script | `.ts`, `.js` |
| Prefab | `*.prefab.json` |
| Animation | `*.animation.json` (state machine) |
| Animation clip | `*.anim.json` |

## Project panel

- **Tree** and **grid** modes; textures show thumbnails via `AssetPreviewCache`
- **Refresh** — rescan folders
- **Drag** — `ASSET_GUID` payload for Inspector fields and Hierarchy
- **Import** — drag files or folders from Windows Explorer onto Project to copy into the open folder
- **Double-click** `*.scene.json` — load scene
- **Context menu** — create folder, rename, delete, reveal in OS, open script externally

::: studio-screenshot{file="12-project-list.png"}
Assets tree expanded with folders and files.
:::

::: studio-screenshot{file="13-project-grid.png"}
Grid view with texture thumbnails.
:::

## Texture and sprites

1. Select a texture in Project.
2. In Inspector, open **Slice Editor…**
3. Set cell size, offset, padding; apply slices (re-apply replaces existing slices).
4. Drag **sprite** sub-assets onto **Sprite Renderer**.

Scene view draws the UV rectangle for the assigned slice. `textureGuid` on the component stays synced with the parent texture.

::: studio-screenshot{file="14-asset-inspector.png"}
Texture selected in Inspector with import settings and preview.
:::

::: studio-screenshot{file="31-slice-editor.png"}
Spritesheet slice modal with grid overlay.
:::

## Copying projects

Include both `Assets/` and `.studio/metadata/assets/` when zipping or sharing a project so GUID references in scenes stay valid.

## Related

- [Prefabs](prefabs.md)
- [Animation](animation.md)
- [Tilesets and rule tiles](tilesets-and-rule-tiles.md)
- [Project format](project-format.md)
