# Troubleshooting

## ImGui layout broken or panels missing

Delete `.studio/imgui.ini` or use **View → Reset Layout**. Restart the editor.

## Black Scene or Game view

- Confirm the scene has a **Camera 2D** marked main (or expected camera setup).
- Check GPU drivers; llw needs working OpenGL.
- See engine [FAQ](/faq) for black window issues.

## Scripts do not compile

1. Open [Console](console.md) for esbuild errors.
2. Fix TypeScript syntax and `llw.core` imports.
3. **Assets → Refresh Scripts**.
4. Ensure `.llw/sdk/` exists (reopen project to regenerate).

## Scripts compile but nothing runs on Play

- Script component must reference the correct class name / GUID.
- Entity must be **active** in hierarchy.
- Check Console for Graal runtime exceptions.

## No keyboard or mouse in game

Click inside the **Game** view after Play. Input is disabled when Game view is not focused.

## GUID broken after copying project

Copy both `Assets/` and `.studio/metadata/assets/`. Scenes reference assets by GUID, not path alone.

## Tile palette missing

**Tile Palette** is not in the default dock. Open it from the panel list / Window menu and dock it beside Project.

## Physics behaves differently in play

Play uses Box2D with fixed timestep in `PhysicsSystem`. Collider sizes are in world units; check rigidbody type (static/kinematic/dynamic).

## Related

- [Getting started](getting-started.md)
- [Scripting](scripting.md)
