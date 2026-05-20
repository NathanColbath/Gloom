# Memory and performance

Studio targets interactive editing on typical dev machines. Heavy costs are GPU textures, script runtime, and physics during play.

## Generated and cache folders

Safe to delete or gitignore (regenerated on demand):

| Path | Contents |
|------|----------|
| `.studio/metadata/script-cache/` | esbuild output per script GUID |
| `.studio/metadata/script-schemas/` | Inspector metadata |
| `.studio/metadata/logs/` | Editor logs |

Reimport or **Refresh Scripts** rebuilds caches.

## Asset previews

`AssetPreviewCache` rasterizes texture thumbnails for the Project grid. Large projects increase memory proportional to unique previewed textures.

## Play mode

- **Scene clone** duplicates all serializable components for the play world.
- **GraalJS** runtime and script instances are disposed on Stop.
- **Box2D** world is destroyed on Stop.

Avoid leaving Play running while iterating on large scenes if memory is tight.

::: studio-screenshot{file="22-memory-profiler.png"}
Analysis or debug window with entity and GPU stats if available in your build.
:::

## Tips

- Prefer sprite slices over duplicating full textures.
- Batch script logging in `update()` — excessive `console.log` fills the Console and costs string allocation.

## Related

- [Project format](project-format.md)
- [Troubleshooting](troubleshooting.md)
