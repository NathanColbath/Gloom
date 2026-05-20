# Getting started

## Run the editor

From the monorepo root:

```bash
./gradlew :llw-studio:run
```

Pass an optional project folder:

```bash
./gradlew :llw-studio:run --args="path/to/your/project"
```

On Windows PowerShell, use the same `--args` form with forward slashes or quoted paths.

## First launch

1. **New Project** — choose name and location; Studio creates the folder structure below.
2. **Open Project** — pick an existing folder containing `*.llwproj`.
3. Open the bundled sample: `llw-studio/studio-project`.

::: studio-screenshot{file="19-new-project-dialog.png"}
New Project dialog with name and location fields.
:::

## Folder layout

| Path | Purpose |
|------|---------|
| `Assets/` | Textures, scripts, audio, prefabs, animations (content only; no `.meta` beside files) |
| `Scenes/` | JSON scene files (`*.scene.json`) |
| `*.llwproj` | Project manifest (startup scene, name) |
| `.studio/` | Editor settings, `imgui.ini`, tool scripts |
| `.studio/metadata/assets/` | Per-asset import metadata (GUID, type, importer) |
| `.studio/metadata/script-schemas/` | Inspector field schemas (generated) |
| `.studio/metadata/script-cache/` | Bundled script output (generated; safe to gitignore) |
| `.studio/metadata/logs/` | Editor logs (generated) |
| `.llw/sdk/` | TypeScript SDK copied for IntelliSense (`llw.core`, etc.) |

When copying a project outside Studio, include both `Assets/` and `.studio/metadata/assets/` so GUIDs stay stable.

::: studio-screenshot{file="25-project-folder.png"}
Project folder on disk showing Assets, Scenes, .studio, and .llwproj.
:::

## Save and play

1. **File → Save Scene** — writes the active scene JSON.
2. **File → Save Project** — updates the manifest.
3. Click **Play** on the toolbar — bundles scripts, clones the scene, runs play-mode systems.
4. Click **Stop** — discards the play world.

Focus the **Game** view (click inside it) so keyboard and mouse input reach your scripts.

## External script editor

Scripts are `.ts` files under `Assets/Scripts/`. Double-click a script in the Project panel or use the OS default editor. Studio copies the SDK to `.llw/sdk/`; point your IDE `tsconfig` at that project (see [Scripting](scripting.md)).

## Next steps

- [Quickstart tutorial](quickstart-tutorial.md) — walk through the sample project
- [Editor shell](editor-shell.md) — menus, toolbar, docking
- [Scripting](scripting.md) — TypeScript gameplay code
