# bgfx shaders

Built-in sprite and presentation shaders for `BgfxRenderBackend` are compiled with **shaderc** (see `lwjgl-shaderc` on the classpath).

Place platform-specific `.bin` outputs here when enabling the native bgfx sprite path (`BgfxSpriteRenderer.useBgfxPath`).

Until binaries are checked in, scene rendering uses the OpenGL delegate inside `BgfxRenderBackend`.
