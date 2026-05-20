# Editor shell and layout

LLW Studio uses a single GLFW window. Each frame: poll events, render Scene/Game offscreen targets, build ImGui UI, swap buffers.

## Menu bar

| Menu | Items |
|------|--------|
| **File** | New Project, Open Project, Save Scene, Save Project, Exit |
| **Edit** | Undo, Redo (disabled when stack empty) |
| **Assets** | Create Script, Create Animation, Create Animation Clip, Refresh Scripts |
| **View** | Frame Scene, Reset Layout |

The active **project name** appears on the right side of the menu bar.

::: studio-screenshot{file="02-menu-bar.png"}
Menu bar with File, Edit, Assets, and View menus visible.
:::

## Toolbar

Docked above Scene/Game (default layout):

- **Play / Stop** — enter and exit [play mode](play-mode.md)
- **Scene tools** — Hand, Move, Rotate, Scale, Tile Paint, Tile Erase (see [Scene view](scene-view.md))

::: studio-screenshot{file="03-toolbar-play.png"}
Toolbar with Play button and scene tool toggles.
:::

## Docking

ImGui docking is enabled. Drag panel title bars to split or tab windows. Layout persists in `.studio/imgui.ini`.

- **View → Reset Layout** — deletes `imgui.ini` and reapplies the default split from `DockLayout`.
- If panels vanish or overlap oddly, reset layout before other troubleshooting.

::: studio-screenshot{file="20-docking-layout.png"}
Dragging a panel; blue docking preview region.
:::

## Default dock map

| Window | Default region |
|--------|----------------|
| Hierarchy | Left column (~20% width) |
| Toolbar | Thin strip above Scene/Game |
| Scene / Game | Center (tabbed) |
| Inspector | Right column |
| Animation | Bottom, above Project |
| Project / Console | Bottom (split) |

## Play mode chrome

While playing, the edit scene is frozen; gizmos hide in Scene view. Stop returns to edit mode without saving play-time changes to disk (unless you changed edit data before play).

::: studio-screenshot{file="21-play-mode-banner.png"}
Editor state during play mode.
:::

## Related

- [Hierarchy](hierarchy.md)
- [Keyboard shortcuts](keyboard-shortcuts.md)
- [Troubleshooting](troubleshooting.md) — broken `imgui.ini`
