package org.llw.studio.editor.components;



import org.llw.studio.assets.AssetDatabase;

import org.llw.studio.editor.EditorSession;

import org.llw.studio.editor.SelectionService;

import org.llw.studio.editor.StudioContext;

import org.llw.studio.editor.commands.UndoStack;

import org.llw.studio.scene.GameObject;

import org.llw.studio.scene.Scene;

import org.llw.studio.systems.JsScriptSystem;



/**

 * Shared services and dirty flag for component drawers and prefab asset editing.

 */

public final class InspectorContext {

    private final AssetDatabase assets;

    private final UndoStack undoStack;

    private final SelectionService selection;

    private final EditorSession editorSession;

    private StudioContext studioContext;

    private Scene inspectionScene;

    private boolean dirty;



    /**

     * @param assets         project asset database

     * @param undoStack      edit-scene undo

     * @param selection      entity selection

     * @param editorSession  session (play script system, etc.)

     */

    public InspectorContext(AssetDatabase assets, UndoStack undoStack, SelectionService selection, EditorSession editorSession) {

        this.assets = assets;

        this.undoStack = undoStack;

        this.selection = selection;

        this.editorSession = editorSession;

    }



    /** @param studioContext loaded project context for object lookup */

    public void setStudioContext(StudioContext studioContext) {

        this.studioContext = studioContext;

    }



    /**

     * Overrides the scene drawers read/write (e.g. prefab scratch scene).

     *

     * @param inspectionScene scene to inspect, or null to use {@link StudioContext#activeScene()}

     */

    public void setInspectionScene(Scene inspectionScene) {

        this.inspectionScene = inspectionScene;

    }



    /**

     * @return explicit inspection scene, or active scene from context, or null

     */

    public Scene inspectionScene() {

        if (inspectionScene != null) {

            return inspectionScene;

        }

        if (studioContext == null) {

            return null;

        }

        return studioContext.activeScene();

    }



    /** @return studio context set by panels each frame */

    public StudioContext studioContext() {

        return studioContext;

    }



    /** @return asset database */

    public AssetDatabase assets() {

        return assets;

    }



    /** @return undo stack for inspector edits */

    public UndoStack undoStack() {

        return undoStack;

    }



    /** @return entity selection */

    public SelectionService selection() {

        return selection;

    }



    /** @return editor session */

    public EditorSession editorSession() {

        return editorSession;

    }



    /** @return script system while in play mode, or null */

    public JsScriptSystem playScriptSystem() {

        return editorSession.playScriptSystem();

    }



    /** Marks the inspected scene or asset as modified (call after property edits). */

    public void markDirty() {

        dirty = true;

    }



    /**

     * @return true once if {@link #markDirty()} was called since the last consume

     */

    public boolean consumeDirty() {

        boolean value = dirty;

        dirty = false;

        return value;

    }



    /**

     * @param context studio context for the active scene

     * @return selected game object in the active scene, or null

     */

    public GameObject object(StudioContext context) {

        if (context == null) {

            return null;

        }

        return context.activeScene().find(selection.selected());

    }

}

