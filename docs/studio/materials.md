# Materials

Materials (`.material.json`) bind a shader program and default properties for sprites and other renderers.

## Shader sources

- `BUILTIN_UNLIT` — default sprite shader
- `BUILTIN_LIT` — normal-map 2D lighting shader
- `SHADER_GRAPH` — custom fragment graph compiled with the lit vertex pass when used on lit renderers

Assign materials on **Sprite Renderer → Material**. Legacy `shaderGraphGuid` fields still work but are deprecated.

## Editing materials

1. Select a `.material.json` asset in the **Project** panel.
2. Edit **Shader Source**, optional **Shader Graph** / **Normal Map**, and custom **Properties** in the **Inspector**.
3. Click **Save Material** to write the file and refresh the scene view.

From a sprite, use **Material → Edit** in the Inspector to jump to the assigned material asset.

## Properties

Custom properties are uploaded to shader uniforms by name when the program defines matching `float` or `vec4` uniforms (useful for shader graph materials).
