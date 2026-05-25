package org.llw.studio.editor.gizmo;

import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.EditorCamera;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.scene.Scene;

/**
 * Shared inputs for component and script scene gizmos.
 */
public final class GizmoDrawContext {
    private final Scene scene;
    private final OffscreenTarget target;
    private final EditorCamera editorCamera;
    private final int viewWidth;
    private final int viewHeight;
    private final AssetDatabase assets;
    private final SelectionService selection;
    private final float lineWorld;
    private final DrawState drawState;

    /**
     * @param scene         scene being edited
     * @param target        offscreen render target
     * @param editorCamera  pan/zoom camera
     * @param viewWidth     viewport width in pixels
     * @param viewHeight    viewport height in pixels
     * @param assets        project assets
     * @param selection     entity selection
     * @param lineWorld     wire line thickness in world units
     * @param drawState     draw state including render layer
     */
    public GizmoDrawContext(
            Scene scene,
            OffscreenTarget target,
            EditorCamera editorCamera,
            int viewWidth,
            int viewHeight,
            AssetDatabase assets,
            SelectionService selection,
            float lineWorld,
            DrawState drawState
    ) {
        this.scene = scene;
        this.target = target;
        this.editorCamera = editorCamera;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.assets = assets;
        this.selection = selection;
        this.lineWorld = lineWorld;
        this.drawState = drawState;
    }

    public Scene scene() {
        return scene;
    }

    public OffscreenTarget target() {
        return target;
    }

    public EditorCamera editorCamera() {
        return editorCamera;
    }

    public int viewWidth() {
        return viewWidth;
    }

    public int viewHeight() {
        return viewHeight;
    }

    public AssetDatabase assets() {
        return assets;
    }

    public SelectionService selection() {
        return selection;
    }

    public float lineWorld() {
        return lineWorld;
    }

    public DrawState drawState() {
        return drawState;
    }
}
