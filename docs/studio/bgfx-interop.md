# Studio bgfx / OpenGL interop

The LLW Studio editor UI uses **Dear ImGui** with `ImGuiImplGl3` (OpenGL 3.3). Scene and game views render into **OpenGL framebuffer objects** via `OffscreenTarget`.

## Renderer setting (primary)

1. Open **View → Settings** (closable **Settings** dock tab).
2. Under **Graphics**, choose a renderer from the combo box.
3. The choice is saved to `~/.llw-studio/settings.json` (field `renderer`).
4. **Restart LLW Studio** when the UI shows *Restart LLW Studio to apply renderer change.*

The same file is used by the published player when no `--renderer` or `GLOOM_RENDERER` override is set.

### Options

| Setting | Description |
|---------|-------------|
| OpenGL 3.3 | Default. Full shader graph and custom material support. |
| bgfx — OpenGL | Hybrid: scene on OpenGL FBOs, presentation via bgfx OpenGL. |
| bgfx — Vulkan | Hybrid: scene on OpenGL FBOs, presentation via bgfx Vulkan. |
| bgfx — Direct3D 11 | Hybrid: scene on OpenGL FBOs, presentation via bgfx D3D11 (Windows). |

## Developer overrides

Environment variables still win over the settings file for a single launch:

- Studio: `GLOOM_STUDIO_RENDERER` (`bgfx-opengl`, `bgfx-vulkan`, `bgfx-d3d11`)
- Player: `GLOOM_RENDERER` or `--renderer <id>`

## How hybrid bgfx works

`BgfxRenderBackend`:

1. Renders the scene with the embedded **OpenGL** backend (`openGlBackend()`).
2. For window presentation (player blit), uses `BgfxPresentPass`: OpenGL fullscreen blit of the offscreen texture, then `bgfx.frame()`.
3. Skips `glfwSwapBuffers` when bgfx initialized successfully (player).

Custom materials and shader graphs require GLSL; when a non-OpenGL renderer is active, `MaterialShaderTarget` falls back to built-in lit shaders for shader-graph materials.

## Player

```bash
java -jar llw-player-all.jar --content ./content --renderer bgfx-vulkan
```

Scene rendering stays on OpenGL FBOs; presentation uses bgfx when initialization succeeds.
