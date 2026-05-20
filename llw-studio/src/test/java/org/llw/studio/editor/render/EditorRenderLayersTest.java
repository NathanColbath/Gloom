package org.llw.studio.editor.render;

import org.junit.jupiter.api.Test;
import org.llw.render.graphics.DrawState;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorRenderLayersTest {
    @Test
    void gridSortsBeforeSceneAndGizmo() {
        long gridKey = DrawState.DEFAULT.withLayer(EditorRenderLayers.GRID).sortKey(0);
        long sceneKey = DrawState.DEFAULT.withLayer(EditorRenderLayers.SCENE_BASE).sortKey(0);
        long selectionKey = DrawState.DEFAULT.withLayer(EditorRenderLayers.SELECTION).sortKey(0);
        long gizmoKey = DrawState.DEFAULT.withLayer(EditorRenderLayers.GIZMO).sortKey(0);
        assertTrue(gridKey < sceneKey);
        assertTrue(sceneKey < selectionKey);
        assertTrue(selectionKey < gizmoKey);
    }
}
