# Game view

The Game view shows the **play scene** while play mode is active — the cloned world where scripts and physics run. When stopped, it shows a placeholder prompting you to press Play.

## Where to find it

Center dock, tabbed with **Scene** (default). Window title **Game**.

## Play mode output

During play:

- Renders the same camera stack as a standalone game (main camera, sorting, tilemaps, UI canvas)
- May show an FPS overlay depending on build settings
- Receives **keyboard and mouse** only when the Game view is **focused** (click inside the panel)

::: studio-screenshot{file="11-game-view.png"}
Game view running with play scene visible.
:::

## Input focus

`PlayInputBridge` routes GLFW input to scripts only when the Game view has focus. If movement does nothing after Play, click inside Game view and try again.

## Edit vs play

| State | Game view shows |
|-------|-----------------|
| Stopped | Placeholder message |
| Playing | Live play clone |
| Scene view (edit) | Unchanged edit scene in the Scene tab |

Changes made in play mode are **not** written back to the scene file on Stop.

## Related

- [Play mode](play-mode.md)
- [Scripting API — Input](scripting-api/input-and-keys.md)
