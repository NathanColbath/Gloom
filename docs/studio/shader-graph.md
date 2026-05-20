# Shader Graph

The Shader Graph panel edits fragment-only shader assets (`*.shadergraph.json`) that compile to GLSL 330 for the default sprite vertex pass.

## Opening the panel

- **View → Shader Graph** toggles the dockable panel.
- Double-click a shader graph asset in the Project browser.
- On a **Sprite Renderer**, use the **Shader** field **Edit** button.

## Authoring

- **Right-click** the canvas to add nodes.
- **Connect:** click an **output** pin, then click a compatible **input** pin.
- **Reconnect:** click an **input** pin that already has a wire (picks up the link); click another input to move it.
- **Disconnect:** right-click an **input** pin (removes incoming wire) or an **output** pin (removes all wires from that output).
- **Delete** removes the selected node, or the selected wire (click the wire first).
- **Escape** cancels an in-progress connection drag.
- **Middle-mouse drag** pans; **scroll** zooms.
- Select a node to preview its subgraph in the preview pane.

## Runtime

Assign a shader graph on **Sprite Renderer → Shader**. Scene and Game views compile the graph at draw time via `ShaderGraphProgramCache`.

## Asset format

Graphs are stored as JSON under `Assets/` with nodes, links, and an optional `previewTextureGuid` for the editor preview.
