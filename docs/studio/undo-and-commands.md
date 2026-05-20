# Undo and commands

Editor mutations go through a command stack so **Edit → Undo** and **Edit → Redo** reverse structural and property changes.

## Where to find it

**Edit** menu in the menu bar. Menu items disable when the stack cannot undo or redo.

## Supported operations

Examples of undoable work:

- **Transform edits** — gizmo drags in Scene view (`TransformEditCommand`)
- Hierarchy reparent and create/delete (where wired to commands)
- Inspector field changes registered with the undo stack

Each gizmo drag typically records **one** undo step on mouse release.

::: studio-screenshot{file="29-undo-redo.png"}
Inspector after a move, with Edit → Undo available.
:::

## Play mode

The play scene clone is **not** part of the undo stack. Stopping play discards runtime-only state. Edit-scene undo history remains from before play.

## Tips

- Undo applies to the **edit scene** only.
- If undo seems inactive, confirm you are not in a modal dialog or play mode.

## Related

- [Scene view](scene-view.md)
- [Hierarchy](hierarchy.md)
