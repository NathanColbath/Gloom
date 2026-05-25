package org.llw.studio.playmode;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.ActiveComponent;
import org.llw.studio.ecs.components.HierarchyComponent;
import org.llw.studio.ecs.components.Light2DComponent;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.ecs.components.SceneLightingComponent;
import org.llw.studio.ecs.components.StaticLightmapContributor;
import org.llw.studio.ecs.components.Animation2DComponent;
import org.llw.studio.ecs.components.ParticleEmitterComponent;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.ecs.components.Transform2DComponent;
import org.llw.studio.log.StudioLogSink;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.ecs.components.SceneObjectIdComponent;
import org.llw.studio.scripting.js.GraalScriptRuntime;
import org.llw.studio.scripting.js.PlayClock;
import org.llw.resources.ResourceManager;
import org.llw.studio.scripting.js.PlayAudioBridge;
import org.llw.studio.scripting.js.PlayInputBridge;
import org.llw.studio.scripting.js.ScriptBundler;
import org.llw.studio.scripting.js.ScriptCompileService;
import org.llw.studio.scripting.js.ScriptSceneIndex;
import org.llw.studio.physics.PhysicsContactBridge;
import org.llw.studio.physics.PhysicsWorld;
import org.llw.studio.physics.PlayPhysicsBridge;
import org.llw.studio.scripting.js.PlayAnimationBridge;
import org.llw.studio.scripting.js.PlayParticleBridge;
import org.llw.studio.systems.AudioSystem;
import org.llw.studio.particles.runtime.ParticleWorld;
import org.llw.studio.particles.systems.ParticleSimulationSystem;
import org.llw.studio.systems.AnimationSystem;
import org.llw.studio.systems.JsScriptSystem;
import org.llw.studio.systems.PhysicsSystem;
import org.llw.studio.systems.PlayInputSystem;
import org.llw.studio.systems.UiInputSystem;
import org.llw.studio.ui.PlayUiInputBridge;
import org.llw.studio.systems.TransformSystem;
import org.llw.studio.ecs.components.BoxCollider2DComponent;
import org.llw.studio.ecs.components.CircleCollider2DComponent;
import org.llw.studio.ecs.components.EdgeCollider2DComponent;
import org.llw.studio.ecs.components.Rigidbody2DComponent;
import org.llw.studio.ecs.components.UIButtonComponent;
import org.llw.studio.ecs.components.UICanvasComponent;
import org.llw.studio.ecs.components.UILabelComponent;
import org.llw.studio.ecs.components.UITextFieldComponent;
import org.llw.studio.ecs.components.UIToggleComponent;
import org.llw.studio.ecs.components.TilemapComponent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Clones the edit scene into an isolated play world, wires GraalJS systems, and ticks logic
 * while the editor is in play mode.
 */
public final class PlayModeRunner {
    private GraalScriptRuntime runtime;
    private JsScriptSystem scriptSystem;
    private PhysicsWorld physicsWorld;
    private PhysicsSystem physicsSystem;
    private AnimationSystem animationSystem;
    private ParticleWorld particleWorld;
    private ParticleSimulationSystem particleSimulationSystem;
    private PhysicsContactBridge physicsContactBridge;
    private long windowHandle;
    private final Map<Integer, EntityId> playEntityBySceneId = new HashMap<>();

    /**
     * @param windowHandle native GLFW window handle for input routing
     */
    public void setWindowHandle(long windowHandle) {
        this.windowHandle = windowHandle;
    }

    /**
     * Clones the edit scene and prepares scripts on a worker thread (no Box2D yet).
     *
     * @param editScene    authoritative edit-mode scene to clone
     * @param projectRoot  project root for script bundling
     * @param assets       project asset database
     * @param console      log sink for script errors
     * @return prepared play scene; call {@link #activate(PlayPrepareResult, AssetDatabase, ResourceManager)} on the main thread
     */
    public PlayPrepareResult prepareScene(
            Scene editScene,
            Path projectRoot,
            AssetDatabase assets,
            StudioLogSink console
    ) {
        playEntityBySceneId.clear();
        Set<String> sceneScriptGuids = ScriptSceneIndex.collectGuids(editScene);
        ScriptCompileService.ensureBundled(projectRoot, assets, sceneScriptGuids, console);
        Scene playScene = cloneScene(editScene);
        GraalScriptRuntime preparedRuntime = new GraalScriptRuntime(console, playScene, assets, projectRoot);
        JsScriptSystem preparedScriptSystem = new JsScriptSystem(preparedRuntime, assets, projectRoot, console);
        preparedScriptSystem.warmupFactories(sceneScriptGuids);
        preparedScriptSystem.warmupInstances(playScene);
        return new PlayPrepareResult(playScene, preparedRuntime, preparedScriptSystem);
    }

    /**
     * Builds Box2D, registers play systems, and enters play mode. Must run on the main thread.
     *
     * @param prepared   result from {@link #prepareScene}
     * @param assets     project assets for clip lookup; may be {@code null} in tests
     * @param resources  engine resources (OpenAL); may be {@code null} in tests
     * @return isolated play scene
     */
    public Scene activate(PlayPrepareResult prepared, AssetDatabase assets, ResourceManager resources) {
        runtime = prepared.runtime;
        scriptSystem = prepared.scriptSystem;
        Scene playScene = prepared.playScene;
        physicsWorld = new PhysicsWorld();
        physicsContactBridge = new PhysicsContactBridge();
        physicsContactBridge.setDispatcher(scriptSystem::dispatchPhysicsEvent);
        physicsSystem = new PhysicsSystem(physicsWorld, physicsContactBridge);
        physicsSystem.resetFixedStep();
        physicsWorld.buildFromScene(playScene.world());
        PlayPhysicsBridge.setActive(physicsWorld);
        PlayPhysicsBridge.setHostApi(runtime.hostApi());
        playScene.world().scheduler().add(org.llw.studio.ecs.SystemGroup.LOGIC, new PlayInputSystem());
        playScene.world().scheduler().add(org.llw.studio.ecs.SystemGroup.LOGIC, new TransformSystem());
        playScene.world().scheduler().add(org.llw.studio.ecs.SystemGroup.LOGIC, new UiInputSystem(playScene));
        playScene.world().scheduler().add(org.llw.studio.ecs.SystemGroup.LOGIC, scriptSystem);
        animationSystem = assets == null ? null : new AnimationSystem(assets);
        if (animationSystem != null) {
            playScene.world().scheduler().add(org.llw.studio.ecs.SystemGroup.LOGIC, animationSystem);
            PlayAnimationBridge.setActive(animationSystem);
        }
        particleWorld = new ParticleWorld();
        if (assets != null) {
            particleSimulationSystem = new ParticleSimulationSystem(assets, particleWorld);
            particleSimulationSystem.setPhysicsWorld(physicsWorld);
            playScene.world().scheduler().add(org.llw.studio.ecs.SystemGroup.LOGIC, particleSimulationSystem);
            PlayParticleBridge.setActive(particleSimulationSystem);
        }
        playScene.world().scheduler().add(org.llw.studio.ecs.SystemGroup.LOGIC, physicsSystem);
        PlayClock.reset();
        PlayInputBridge.configure(windowHandle, true);
        PlayAudioBridge.reset();
        if (assets != null && resources != null) {
            PlayAudioBridge.configure(resources.audioContext(), assets);
            playScene.world().scheduler().add(org.llw.studio.ecs.SystemGroup.LOGIC, new AudioSystem());
        }
        return playScene;
    }

    /** Prepared play clone and script runtime before physics activation. */
    public record PlayPrepareResult(Scene playScene, GraalScriptRuntime runtime, JsScriptSystem scriptSystem) {
    }

    /**
     * Advances play-mode logic for one frame.
     *
     * @param playScene         scene returned from {@link #start}
     * @param deltaTime         elapsed seconds
     * @param gameViewFocused   when {@code false}, play input is suppressed
     */
    public void update(Scene playScene, float deltaTime, boolean gameViewFocused) {
        PlayClock.beginFrame(deltaTime);
        PlayInputBridge.configure(windowHandle, gameViewFocused);
        playScene.world().scheduler().update(playScene.world(), deltaTime);
        PlayAudioBridge.update();
    }

    /** Destroys script instances, closes the Graal runtime, and resets bridges. */
    public void stop() {
        if (scriptSystem != null) {
            scriptSystem.destroyAll();
        }
        if (runtime != null) {
            runtime.close();
        }
        if (physicsWorld != null) {
            physicsWorld.destroy();
        }
        physicsWorld = null;
        physicsSystem = null;
        physicsContactBridge = null;
        animationSystem = null;
        particleWorld = null;
        particleSimulationSystem = null;
        scriptSystem = null;
        runtime = null;
        PlayPhysicsBridge.clear();
        PlayAnimationBridge.clear();
        PlayParticleBridge.clear();
        PlayClock.reset();
        PlayInputBridge.configure(windowHandle, false);
        PlayAudioBridge.reset();
        PlayUiInputBridge.reset();
        playEntityBySceneId.clear();
    }

    /**
     * Maps a stable edit-scene object id to the corresponding play-scene entity.
     *
     * @param sceneId scene object id from the edit scene
     * @return play entity, or {@link EntityId#none()} when unknown
     */
    public EntityId playEntityForSceneId(int sceneId) {
        return playEntityBySceneId.getOrDefault(sceneId, EntityId.none());
    }

    /** @return immutable copy of the scene-id to play-entity map */
    public Map<Integer, EntityId> playEntityBySceneId() {
        return Map.copyOf(playEntityBySceneId);
    }

    /**
     * Hot-reloads a script asset in the active play session.
     *
     * @param scriptGuid script asset GUID
     */
    public void reloadScript(String scriptGuid) {
        if (scriptSystem != null) {
            scriptSystem.reloadScript(scriptGuid);
        }
    }

    /** @return active script system, or {@code null} when not in play mode */
    public JsScriptSystem scriptSystem() {
        return scriptSystem;
    }

    /** @return particle simulation state during play mode, or {@code null} when inactive */
    public ParticleWorld particleWorld() {
        return particleWorld;
    }

    /** @return particle logic system during play mode */
    public ParticleSimulationSystem particleSimulationSystem() {
        return particleSimulationSystem;
    }

    /**
     * Rebundles every script asset on disk (editor idle path before entering play mode).
     *
     * @param projectRoot project root
     * @param assets      script assets to bundle
     * @param console     optional sink for bundle diagnostics
     */
    public static void refreshScripts(Path projectRoot, AssetDatabase assets, StudioLogSink console) {
        List<ScriptBundler.ScriptSource> sources = new ArrayList<>();
        for (StudioAsset asset : assets.allAssets()) {
            if (asset.type() == AssetType.SCRIPT && !asset.isFolder()) {
                sources.add(new ScriptBundler.ScriptSource(asset.guid(), asset.path()));
            }
        }
        ScriptBundler.bundleAll(projectRoot, sources, console);
    }

    private Scene cloneScene(Scene source) {
        Scene clone = new Scene();
        clone.setName(source.name());
        Map<EntityId, GameObject> cloneBySource = new HashMap<>();
        var names = source.world().store(NameComponent.class);
        for (int i = 0; i < names.size(); i++) {
            EntityId id = names.entityAt(i);
            NameComponent name = names.componentAt(i);
            if ("Scene Root".equals(name.name())) {
                continue;
            }
            GameObject object = clone.createGameObject(name.name());
            object.setTag(name.tag());
            cloneBySource.put(id, object);
            Transform2DComponent srcTransform = source.world().getComponent(id, Transform2DComponent.class);
            if (srcTransform != null) {
                object.transform().x = srcTransform.x;
                object.transform().y = srcTransform.y;
                object.transform().rotation = srcTransform.rotation;
                object.transform().scaleX = srcTransform.scaleX;
                object.transform().scaleY = srcTransform.scaleY;
            }
            ActiveComponent srcActive = source.world().getComponent(id, ActiveComponent.class);
            if (srcActive != null) {
                object.getComponent(ActiveComponent.class).selfActive = srcActive.selfActive;
            }
            SpriteRendererComponent srcSprite = source.world().getComponent(id, SpriteRendererComponent.class);
            if (srcSprite != null) {
                object.addComponent(SpriteRendererComponent.class, srcSprite.copy());
            }
            Animation2DComponent srcAnim = source.world().getComponent(id, Animation2DComponent.class);
            if (srcAnim != null) {
                object.addComponent(Animation2DComponent.class, srcAnim.copy());
            }
            ParticleEmitterComponent srcParticles = source.world().getComponent(id, ParticleEmitterComponent.class);
            if (srcParticles != null) {
                object.addComponent(ParticleEmitterComponent.class, srcParticles.copy());
            }
            ScriptComponent srcScript = source.world().getComponent(id, ScriptComponent.class);
            if (srcScript != null) {
                object.addComponent(ScriptComponent.class, srcScript.copy());
            }
            Light2DComponent srcLight = source.world().getComponent(id, Light2DComponent.class);
            if (srcLight != null) {
                object.addComponent(Light2DComponent.class, srcLight.copy());
            }
            SceneLightingComponent srcSceneLighting = source.world().getComponent(id, SceneLightingComponent.class);
            if (srcSceneLighting != null) {
                object.addComponent(SceneLightingComponent.class, srcSceneLighting.copy());
            }
            StaticLightmapContributor srcLightmapContributor =
                    source.world().getComponent(id, StaticLightmapContributor.class);
            if (srcLightmapContributor != null) {
                object.addComponent(StaticLightmapContributor.class, srcLightmapContributor.copy());
            }
            var srcCamera = source.world().getComponent(id, org.llw.studio.ecs.components.Camera2DComponent.class);
            if (srcCamera != null) {
                object.addComponent(org.llw.studio.ecs.components.Camera2DComponent.class, srcCamera.copy());
            }
            var srcAudio = source.world().getComponent(id, org.llw.studio.ecs.components.AudioSourceComponent.class);
            if (srcAudio != null) {
                object.addComponent(org.llw.studio.ecs.components.AudioSourceComponent.class, srcAudio.copy());
            }
            Rigidbody2DComponent srcRb = source.world().getComponent(id, Rigidbody2DComponent.class);
            if (srcRb != null) {
                object.addComponent(Rigidbody2DComponent.class, srcRb.copy());
            }
            BoxCollider2DComponent srcBox = source.world().getComponent(id, BoxCollider2DComponent.class);
            if (srcBox != null) {
                object.addComponent(BoxCollider2DComponent.class, srcBox.copy());
            }
            CircleCollider2DComponent srcCircle = source.world().getComponent(id, CircleCollider2DComponent.class);
            if (srcCircle != null) {
                object.addComponent(CircleCollider2DComponent.class, srcCircle.copy());
            }
            EdgeCollider2DComponent srcEdge = source.world().getComponent(id, EdgeCollider2DComponent.class);
            if (srcEdge != null) {
                object.addComponent(EdgeCollider2DComponent.class, srcEdge.copy());
            }
            TilemapComponent srcTilemap = source.world().getComponent(id, TilemapComponent.class);
            if (srcTilemap != null) {
                object.addComponent(TilemapComponent.class, srcTilemap.copy());
            }
            UICanvasComponent srcCanvas = source.world().getComponent(id, UICanvasComponent.class);
            if (srcCanvas != null) {
                object.addComponent(UICanvasComponent.class, srcCanvas.copy());
            }
            UILabelComponent srcLabel = source.world().getComponent(id, UILabelComponent.class);
            if (srcLabel != null) {
                object.addComponent(UILabelComponent.class, srcLabel.copy());
            }
            UIButtonComponent srcButton = source.world().getComponent(id, UIButtonComponent.class);
            if (srcButton != null) {
                object.addComponent(UIButtonComponent.class, srcButton.copy());
            }
            UIToggleComponent srcToggle = source.world().getComponent(id, UIToggleComponent.class);
            if (srcToggle != null) {
                object.addComponent(UIToggleComponent.class, srcToggle.copy());
            }
            UITextFieldComponent srcField = source.world().getComponent(id, UITextFieldComponent.class);
            if (srcField != null) {
                object.addComponent(UITextFieldComponent.class, srcField.copy());
            }
            SceneObjectIdComponent srcSceneId = source.world().getComponent(id, SceneObjectIdComponent.class);
            if (srcSceneId != null) {
                object.addComponent(SceneObjectIdComponent.class, srcSceneId.copy());
                playEntityBySceneId.put(srcSceneId.sceneId, object.entity());
            }
        }
        for (int i = 0; i < names.size(); i++) {
            EntityId id = names.entityAt(i);
            NameComponent name = names.componentAt(i);
            if ("Scene Root".equals(name.name())) {
                continue;
            }
            HierarchyComponent hierarchy = source.world().getComponent(id, HierarchyComponent.class);
            if (hierarchy == null || hierarchy.parentIndex < 0) {
                continue;
            }
            EntityId parentId = new EntityId(hierarchy.parentIndex, hierarchy.parentGeneration);
            GameObject child = cloneBySource.get(id);
            GameObject parent = cloneBySource.get(parentId);
            if (child != null && parent != null) {
                child.setParent(parent, false);
            }
        }
        return clone;
    }
}
