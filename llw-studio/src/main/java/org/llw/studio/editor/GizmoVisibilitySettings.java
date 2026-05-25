package org.llw.studio.editor;

/**
 * Toggles for scene-view overlay drawing.
 */
public final class GizmoVisibilitySettings {
    private boolean showComponentGizmos = true;
    private boolean showScriptGizmos = true;

    /** @return whether built-in component gizmos are drawn */
    public boolean showComponentGizmos() {
        return showComponentGizmos;
    }

    /** @param showComponentGizmos updated component gizmo visibility */
    public void setShowComponentGizmos(boolean showComponentGizmos) {
        this.showComponentGizmos = showComponentGizmos;
    }

    /** @return whether script gizmo hooks are invoked and drawn */
    public boolean showScriptGizmos() {
        return showScriptGizmos;
    }

    /** @param showScriptGizmos updated script gizmo visibility */
    public void setShowScriptGizmos(boolean showScriptGizmos) {
        this.showScriptGizmos = showScriptGizmos;
    }
}
