package org.llw.studio.scripting.js.bindings;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.ecs.World;
import org.llw.studio.scene.Scene;

import java.nio.file.Path;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: per-runtime context shared by script bindings.
 */
public final class ScriptContext {
    private final Scene scene;
    private final AssetDatabase assets;
    private final Path projectRoot;
    private String scriptGuid = "";
    private String scriptName = "";
    private String entityName = "";
    private ScriptInstanceLookup scriptInstanceLookup;

    /**
     * @param scene       cloned play-mode scene
     * @param assets      project asset database
     * @param projectRoot project root directory
     */
    public ScriptContext(Scene scene, AssetDatabase assets, Path projectRoot) {
        this.scene = scene;
        this.assets = assets;
        this.projectRoot = projectRoot;
    }

    /**
     * @return play-mode scene
     */
    public Scene scene() {
        return scene;
    }

    /**
     * @return play-mode ECS world
     */
    public World world() {
        return scene.world();
    }

    /**
     * @return project asset database
     */
    public AssetDatabase assets() {
        return assets;
    }

    /**
     * @return project root directory
     */
    public Path projectRoot() {
        return projectRoot;
    }

    /**
     * @return current script asset GUID
     */
    public String scriptGuid() {
        return scriptGuid;
    }

    /**
     * @param scriptGuid script asset GUID
     */
    public void setScriptGuid(String scriptGuid) {
        this.scriptGuid = scriptGuid == null ? "" : scriptGuid;
    }

    /**
     * @return display name of the running script
     */
    public String scriptName() {
        return scriptName;
    }

    /**
     * @param scriptName display name for logging
     */
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName == null ? "" : scriptName;
    }

    /**
     * @return name of the entity running this script
     */
    public String entityName() {
        return entityName;
    }

    /**
     * @param entityName entity display name for logging
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName == null ? "" : entityName;
    }

    /**
     * @return console log prefix for the current script instance
     */
    public String logPrefix() {
        if (!entityName.isBlank() && !scriptName.isBlank()) {
            return "[" + entityName + "/" + scriptName + "] ";
        }
        if (!entityName.isBlank()) {
            return "[" + entityName + "] ";
        }
        return "";
    }

    /**
     * @param command deferred ECS or scene mutation
     */
    public void defer(Runnable command) {
        world().commandBuffer().enqueue(command);
    }

    /**
     * @return lookup for other script instances on entities
     */
    public ScriptInstanceLookup scriptInstanceLookup() {
        return scriptInstanceLookup;
    }

    /**
     * @param scriptInstanceLookup lookup implementation
     */
    public void setScriptInstanceLookup(ScriptInstanceLookup scriptInstanceLookup) {
        this.scriptInstanceLookup = scriptInstanceLookup;
    }
}
