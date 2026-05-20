package org.llw.studio.editor.panels;

import org.llw.studio.editor.StudioContext;

/**
 * Dockable ImGui editor window identified by {@link #id()} and {@link #title()}.
 *
 * <p>Implementation note: {@link #render(StudioContext)} is called once per frame from {@link org.llw.studio.editor.shell.EditorShell}
 * after the dock space and menu bar; each implementation must pair {@code ImGui.begin} with {@code ImGui.end}.
 */
public interface EditorPanel {
    /** @return stable panel id (used for docking and registry lookup) */
    String id();

    /** @return window title shown in the tab bar */
    String title();

    /**
     * Draws the panel for the current frame.
     *
     * @param context loaded project and active scene
     */
    void render(StudioContext context);
}
