package org.llw.studio.editor.shell;

/**
 * File, edit, asset, and view commands invoked from the main menu and toolbar.
 */
public interface EditorMenuActions {
    /** Opens the new-project dialog. */
    void newProject();

    /** Opens the native folder chooser to load a project. */
    void openProject();

    /** Saves the current edit scene to disk. */
    void saveScene();

    /** Saves project metadata. */
    void saveProject();

    /** Opens build settings for player export. */
    void buildSettings();

    /** Packages a standalone player build. */
    void buildPlayer();

    /** @return {@code true} while a player build is running */
    boolean isBuildingPlayer();

    /** Exits the application. */
    void exit();

    /** Undoes the last edit-scene command. */
    void undo();

    /** Redoes the last undone command. */
    void redo();

    /** @return whether undo is available */
    boolean canUndo();

    /** @return whether redo is available */
    boolean canRedo();

    /** Opens create-script dialog in the project assets root. */
    void createScript();

    /** Creates a new animation clip in the project assets root. */
    void createAnimationClip();

    /** Creates a parent animation set in the project assets root. */
    void createAnimation();

    /**
     * Creates a parent animation set under a folder.
     *
     * @param folder parent folder under assets, or null for root
     */
    void createAnimationInFolder(java.nio.file.Path folder);

    /**
     * Creates a new animation clip under a folder.
     *
     * @param folder parent folder under assets, or null for root
     */
    void createAnimationClipInFolder(java.nio.file.Path folder);

    /**
     * Opens create-script dialog targeting a folder.
     *
     * @param folder parent folder under assets, or null for root
     */
    void createScriptInFolder(java.nio.file.Path folder);

    /** Regenerates script project files and refreshes play-mode assemblies. */
    void refreshScripts();

    /** Frames the scene view camera to scene content. */
    void frameScene();

    /** Toggles the Animation panel visibility. */
    void toggleAnimationPanel();

    /** Toggles the Tile Palette panel visibility. */
    void toggleTilePalettePanel();

    /** Toggles the Shader Graph panel visibility. */
    void toggleShaderGraphPanel();

    /** Creates a shader graph asset in the project assets root. */
    void createShaderGraph();

    /**
     * Creates a shader graph under a folder.
     *
     * @param folder parent folder under assets, or null for root
     */
    void createShaderGraphInFolder(java.nio.file.Path folder);

    /** Toggles the Particles panel visibility. */
    void toggleParticlePanel();

    /** Creates a particle system asset in the project assets root. */
    void createParticleSystem();

    /**
     * Creates a particle system under a folder.
     *
     * @param folder parent folder under assets, or null for root
     */
    void createParticleSystemInFolder(java.nio.file.Path folder);

    /** @return whether the Animation panel tab is open */
    boolean isAnimationPanelOpen();

    /** @return whether the Tile Palette panel tab is open */
    boolean isTilePalettePanelOpen();

    /** @return whether the Shader Graph panel tab is open */
    boolean isShaderGraphPanelOpen();

    /** @return whether the Particles panel tab is open */
    boolean isParticlePanelOpen();

    /** Deletes ImGui ini and reapplies {@link DockLayout}. */
    void resetLayout();

    /** @return current project display name, or empty if none loaded */
    String projectName();
}
