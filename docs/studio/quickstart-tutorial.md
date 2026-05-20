# Quickstart tutorial

This walkthrough uses the bundled **studio-project** sample. You will open the project, inspect the player, tweak a script field, and press Play.

## 1. Open the sample project

```bash
./gradlew :llw-studio:run --args="llw-studio/studio-project"
```

Or use **File → Open Project** and select `llw-studio/studio-project`.

::: studio-screenshot{file="01-full-editor.png"}
Editor open on studio-project with Main scene loaded.
:::

## 2. Explore the Hierarchy

In the **Hierarchy** panel, expand objects in `Main` scene. Select **Player**.

::: studio-screenshot{file="04-hierarchy.png"}
Hierarchy showing Player and child objects.
:::

## 3. Inspect components

With Player selected, the **Inspector** shows:

- **Transform 2D** — position, rotation (degrees), scale
- **Sprite Renderer** — sprite slice reference
- **Script** — `PlayerController` with exposed fields (`moveSpeed`, `acceleration`, …)

Change **acceleration** to `120` and leave the Inspector.

::: studio-screenshot{file="07-inspector-script.png"}
Script component with serialized TypeScript fields.
:::

## 4. Play

1. Click inside the **Game** view tab so it receives input.
2. Press **Play** on the toolbar.
3. Hold **W** to accelerate, **A** / **D** to rotate.

::: studio-screenshot{file="11-game-view.png"}
Game view while playing with scene output visible.
:::

::: studio-screenshot{file="21-play-mode-banner.png"}
Editor during play (toolbar Stop, muted edit scene).
:::

## 5. Read the Console

Open the **Console** panel. `PlayerController` and other scripts can call `Logger.log` / `console.log`; compile errors appear here after **Assets → Refresh Scripts**.

::: studio-screenshot{file="16-console.png"}
Console with log lines from play mode.
:::

## 6. Edit the script (optional)

1. In Project, open `Assets/Scripts/PlayerController.ts` in your IDE.
2. Scripts use `import * as core from "llw.core"` and `export default class … extends core.Script`.
3. Save, then **Assets → Refresh Scripts** if not playing.
4. Play again to test.

See [Scripting](scripting.md) and [Scripting cookbook](scripting-cookbook.md) for API patterns.

## What you learned

| Step | Panel / action |
|------|----------------|
| Open project | File menu or CLI args |
| Select entity | Hierarchy |
| Tune gameplay | Inspector script fields |
| Run game | Play + Game view focus |
| Debug | Console |

Continue with [Scene view](scene-view.md), [Prefabs](prefabs.md), or [Tilemaps](tilemaps.md).
