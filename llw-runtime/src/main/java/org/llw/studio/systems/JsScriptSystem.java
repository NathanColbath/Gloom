package org.llw.studio.systems;

import com.fasterxml.jackson.databind.JsonNode;
import org.graalvm.polyglot.Value;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.ecs.EcsSystem;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.ActiveComponent;
import org.llw.studio.ecs.components.AudioSourceComponent;
import org.llw.studio.log.StudioLogSink;
import org.llw.studio.scene.Scene;
import org.llw.studio.scene.SceneObjectIds;
import org.llw.studio.scripting.ScriptAttachment;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.scripting.ScriptFieldSchema;
import org.llw.studio.scripting.js.ScriptFieldApplicator;
import org.llw.studio.scripting.ScriptSchemaRegistry;
import org.llw.studio.scripting.js.GraalScriptRuntime;
import org.llw.studio.scripting.js.JsScriptInstance;
import org.llw.studio.scripting.js.ScriptBundler;
import org.llw.studio.scripting.js.PlayAudioBridge;
import org.llw.studio.physics.PhysicsContactEvent;
import org.llw.studio.scripting.js.ScriptCompileService;
import org.llw.studio.scripting.js.ScriptInstanceKey;
import org.llw.studio.scripting.js.ScriptPhysicsMessageInvoker;
import org.llw.studio.scripting.js.ScriptDiagnostic;
import org.llw.studio.scripting.js.ScriptDiagnostics;
import org.llw.util.log.LogLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Play-mode GraalJS script host: loads bundled script factories, creates per-attachment
 * {@link JsScriptInstance}s, drives {@code start}/{@code update}/{@code destroy}, and bridges
 * inspector field reads/writes to live script objects.
 */
public final class JsScriptSystem implements EcsSystem {
    private final GraalScriptRuntime runtime;
    private final AssetDatabase assets;
    private final Path projectRoot;
    private final StudioLogSink console;
    private final Map<String, Value> factories = new HashMap<>();
    private final Map<String, String> scriptNames = new HashMap<>();
    private final Set<String> failedGuids = new HashSet<>();
    private final Map<ScriptInstanceKey, JsScriptInstance> instances = new HashMap<>();
    private final Set<EntityId> playOnStartHandled = new HashSet<>();
    private World world;

    public JsScriptSystem(GraalScriptRuntime runtime, AssetDatabase assets, Path projectRoot, StudioLogSink console) {
        this.runtime = runtime;
        this.assets = assets;
        this.projectRoot = projectRoot;
        this.console = console;
        runtime.hostApi().scriptContext().setScriptInstanceLookup(this::findScriptInstance);
    }

    public void reloadScript(String scriptGuid) {
        ScriptSchemaRegistry.invalidate(scriptGuid);
        factories.remove(scriptGuid);
        scriptNames.remove(scriptGuid);
        failedGuids.remove(scriptGuid);
        if (world == null) {
            return;
        }
        List<ScriptInstanceKey> toRemove = new ArrayList<>();
        for (Map.Entry<ScriptInstanceKey, JsScriptInstance> entry : instances.entrySet()) {
            ScriptAttachment attachment = findAttachment(entry.getKey().entity(), entry.getKey().slotId());
            if (attachment != null && scriptGuid.equals(attachment.scriptGuid)) {
                toRemove.add(entry.getKey());
            }
        }
        for (ScriptInstanceKey key : toRemove) {
            JsScriptInstance instance = instances.remove(key);
            if (instance != null) {
                try {
                    instance.destroy();
                } catch (RuntimeException ignored) {
                }
            }
        }
    }

    @Override
    public void onUpdate(World world, float deltaTime) {
        this.world = world;
        runtime.hostApi().refreshFrameBindings();
        handlePlayOnStart(world);
        runLifecycle(world, true);
        world.commandBuffer().flush(world);
    }

    public void destroyAll() {
        for (JsScriptInstance instance : instances.values()) {
            try {
                instance.destroy();
            } catch (RuntimeException ignored) {
            }
        }
        instances.clear();
        failedGuids.clear();
        playOnStartHandled.clear();
        PlayAudioBridge.reset();
    }

    public void dispatchPhysicsEvent(PhysicsContactEvent event) {
        if (world == null || event == null || event.self().isNone()) {
            return;
        }
        ScriptComponent container = world.getComponent(event.self(), ScriptComponent.class);
        if (container == null) {
            return;
        }
        for (ScriptAttachment attachment : container.attachments) {
            if (!attachment.enabled || !attachment.hasScriptReference()) {
                continue;
            }
            if (failedGuids.contains(attachment.scriptGuid)) {
                continue;
            }
            JsScriptInstance instance = getOrCreateInstance(world, event.self(), attachment);
            if (instance == null) {
                continue;
            }
            try {
                ScriptPhysicsMessageInvoker.invoke(
                        runtime.hostApi(),
                        instance.instance(),
                        instance.logPrefix(),
                        event
                );
            } catch (RuntimeException ex) {
                reportRuntimeError(attachment.scriptGuid, ex);
            }
        }
    }

    public void warmupFactories(Collection<String> scriptGuids) {
        if (scriptGuids == null) {
            return;
        }
        for (String guid : scriptGuids) {
            if (guid == null || guid.isBlank()) {
                continue;
            }
            try {
                factories.computeIfAbsent(guid, this::loadFactory);
            } catch (RuntimeException ex) {
                reportRuntimeError(guid, ex);
            }
        }
    }

    public void warmupInstances(Scene playScene) {
        if (playScene == null) {
            return;
        }
        World playWorld = playScene.world();
        this.world = playWorld;
        runtime.hostApi().refreshFrameBindings();
        handlePlayOnStart(playWorld);
        runLifecycle(playWorld, false);
    }

    private void runLifecycle(World world, boolean runUpdate) {
        var scripts = world.store(ScriptComponent.class);
        for (int i = 0; i < scripts.size(); i++) {
            EntityId entity = scripts.entityAt(i);
            ScriptComponent container = scripts.componentAt(i);
            if (!isEntityActive(world, entity)) {
                continue;
            }
            for (ScriptAttachment attachment : container.attachments) {
                if (!attachment.enabled || !attachment.hasScriptReference()) {
                    continue;
                }
                if (failedGuids.contains(attachment.scriptGuid)) {
                    continue;
                }
                JsScriptInstance instance = getOrCreateInstance(world, entity, attachment);
                if (instance == null) {
                    continue;
                }
                try {
                    instance.start();
                } catch (RuntimeException ex) {
                    reportRuntimeError(attachment.scriptGuid, ex);
                    instances.remove(key(entity, attachment.slotId));
                    continue;
                }
                if (runUpdate) {
                    try {
                        instance.update();
                    } catch (RuntimeException ex) {
                        reportRuntimeError(attachment.scriptGuid, ex);
                    }
                }
            }
        }
    }

    private JsScriptInstance getOrCreateInstance(World world, EntityId entity, ScriptAttachment attachment) {
        ScriptInstanceKey instanceKey = key(entity, attachment.slotId);
        JsScriptInstance instance = instances.get(instanceKey);
        if (instance != null) {
            return instance;
        }
        instance = createInstance(world, entity, attachment);
        if (instance != null) {
            instances.put(instanceKey, instance);
        }
        return instance;
    }

    private JsScriptInstance createInstance(World world, EntityId entity, ScriptAttachment attachment) {
        try {
            Value factory = factories.computeIfAbsent(attachment.scriptGuid, this::loadFactory);
            String scriptName = scriptNames.getOrDefault(attachment.scriptGuid, attachment.scriptGuid);
            return JsScriptInstance.create(
                    runtime.context(),
                    runtime.hostApi(),
                    entity,
                    attachment,
                    scriptName,
                    factory
            );
        } catch (RuntimeException ex) {
            failedGuids.add(attachment.scriptGuid);
            reportRuntimeError(attachment.scriptGuid, ex);
            return null;
        }
    }

    private Value loadFactory(String scriptGuid) {
        StudioAsset asset = assets.get(scriptGuid);
        if (asset == null || asset.type() != AssetType.SCRIPT) {
            throw new IllegalStateException("Missing script asset: " + scriptGuid);
        }
        scriptNames.put(scriptGuid, asset.displayName());
        try {
            Path bundled = ScriptCompileService.bundledPath(projectRoot, scriptGuid);
            if (Files.isRegularFile(bundled)) {
                return runtime.loadFactory(bundled);
            }
            if (assets.resourceManager().isRegistered(scriptGuid)) {
                try (var ref = assets.resourceManager().acquireRaw(scriptGuid)) {
                    String source = ScriptBundler.sanitizeBundledSource(
                            new String(ref.get(), java.nio.charset.StandardCharsets.UTF_8)
                    );
                    return runtime.loadFactorySource(source);
                }
            }
            throw new IOException(
                    "Script not compiled: " + asset.displayName() + ". Use Refresh Scripts or wait for background compile."
            );
        } catch (IOException ex) {
            ScriptDiagnostics.set(scriptGuid, new ScriptDiagnostic(scriptGuid, true, ex.getMessage()));
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    private void handlePlayOnStart(World world) {
        var audioSources = world.store(AudioSourceComponent.class);
        for (int i = 0; i < audioSources.size(); i++) {
            EntityId entity = audioSources.entityAt(i);
            if (playOnStartHandled.contains(entity)) {
                continue;
            }
            AudioSourceComponent source = audioSources.componentAt(i);
            if (source.playOnStart) {
                org.llw.studio.scripting.js.PlayAudioBridge.play(entity, source);
            }
            playOnStartHandled.add(entity);
        }
    }

    private void reportRuntimeError(String scriptGuid, RuntimeException ex) {
        String message = "Script runtime error (" + scriptGuid + "): " + ex.getMessage();
        ScriptDiagnostics.set(scriptGuid, new ScriptDiagnostic(scriptGuid, true, message));
        if (console != null) {
            console.append(LogLevel.ERROR, message);
        }
    }

    private static boolean isEntityActive(World world, EntityId entity) {
        ActiveComponent active = world.getComponent(entity, ActiveComponent.class);
        return active == null || active.selfActive;
    }

    private Value findScriptInstance(EntityId entity, String scriptClassName) {
        if (world == null || entity == null || entity.isNone() || scriptClassName == null || scriptClassName.isBlank()) {
            return null;
        }
        for (Map.Entry<ScriptInstanceKey, JsScriptInstance> entry : instances.entrySet()) {
            if (!entry.getKey().entity().equals(entity)) {
                continue;
            }
            if (JsScriptInstance.namesMatch(entry.getValue().scriptName(), scriptClassName)) {
                return entry.getValue().instance();
            }
        }
        ScriptComponent container = world.getComponent(entity, ScriptComponent.class);
        if (container == null) {
            return null;
        }
        for (ScriptAttachment attachment : container.attachments) {
            if (!attachment.enabled || !attachment.hasScriptReference()) {
                continue;
            }
            String scriptName = scriptNames.getOrDefault(attachment.scriptGuid, attachment.scriptGuid);
            if (!JsScriptInstance.namesMatch(scriptName, scriptClassName)) {
                continue;
            }
            JsScriptInstance instance = getOrCreateInstance(world, entity, attachment);
            if (instance != null) {
                return instance.instance();
            }
        }
        return null;
    }

    public double readNumberField(EntityId entity, int slotId, String fieldName, ScriptFieldSchema field) {
        return coerceDouble(readFieldValue(entity, slotId, fieldName, field));
    }

    public boolean readBooleanField(EntityId entity, int slotId, String fieldName, ScriptFieldSchema field) {
        return coerceBoolean(readFieldValue(entity, slotId, fieldName, field));
    }

    public String readStringField(EntityId entity, int slotId, String fieldName, ScriptFieldSchema field) {
        return coerceString(readFieldValue(entity, slotId, fieldName, field));
    }

    public int readEntityFieldSceneId(EntityId entity, int slotId, String fieldName, ScriptFieldSchema field) {
        Value value = readFieldValue(entity, slotId, fieldName, field);
        if (value != null && !value.isNull()) {
            if (value.isHostObject()) {
                Object host = value.asHostObject();
                if (host instanceof org.llw.studio.scripting.js.bindings.EntityBinding binding) {
                    return SceneObjectIds.get(world, binding.entityId());
                }
            }
            if (value.hasMember("sceneId")) {
                Value sceneIdValue = value.getMember("sceneId");
                if (sceneIdValue != null && !sceneIdValue.isNull()) {
                    return sceneIdValue.asInt();
                }
            }
        }
        return storedEntityFieldSceneId(entity, slotId, fieldName, field);
    }

    private int storedEntityFieldSceneId(EntityId entity, int slotId, String fieldName, ScriptFieldSchema field) {
        ScriptAttachment attachment = findAttachment(entity, slotId);
        if (attachment == null) {
            return -1;
        }
        JsonNode stored = attachment.fields.get(fieldName);
        if (stored == null || stored.isNull()) {
            stored = field == null ? null : field.copyDefault();
        }
        return ScriptFieldApplicator.entitySceneId(stored);
    }

    public void writeNumberField(EntityId entity, int slotId, String fieldName, double value) {
        Value instance = requireInstance(entity, slotId);
        if (instance != null) {
            instance.putMember(fieldName, value);
        }
    }

    public void writeBooleanField(EntityId entity, int slotId, String fieldName, boolean value) {
        Value instance = requireInstance(entity, slotId);
        if (instance != null) {
            instance.putMember(fieldName, value);
        }
    }

    public void writeStringField(EntityId entity, int slotId, String fieldName, String value) {
        Value instance = requireInstance(entity, slotId);
        if (instance != null) {
            instance.putMember(fieldName, value == null ? "" : value);
        }
    }

    public void writeVector2Field(EntityId entity, int slotId, String fieldName, float x, float y) {
        Value instance = requireInstance(entity, slotId);
        if (instance != null) {
            instance.putMember(fieldName, runtime.hostApi().createVec2(x, y));
        }
        ScriptAttachment attachment = findAttachment(entity, slotId);
        if (attachment != null) {
            attachment.setVector2Field(fieldName, x, y);
        }
    }

    public void writeEntityField(EntityId entity, int slotId, String fieldName, int sceneId, Scene scene) {
        Value instance = requireInstance(entity, slotId);
        if (instance == null) {
            return;
        }
        ScriptAttachment attachment = findAttachment(entity, slotId);
        if (attachment != null) {
            if (sceneId < 0) {
                attachment.clearEntityField(fieldName);
            } else {
                attachment.setEntityField(fieldName, sceneId);
            }
        }
        if (sceneId < 0) {
            instance.putMember(fieldName, (Object) null);
            return;
        }
        EntityId target = SceneObjectIds.findBySceneId(scene.world(), sceneId);
        if (target.isNone()) {
            instance.putMember(fieldName, (Object) null);
            return;
        }
        instance.putMember(
                fieldName,
                runtime.hostApi().wrapEntity(runtime.hostApi().createEntityBinding(runtime.hostApi().scriptContext(), target))
        );
    }

    public void writePrefabField(EntityId entity, int slotId, String fieldName, String prefabGuid) {
        Value instance = requireInstance(entity, slotId);
        if (instance == null) {
            return;
        }
        ScriptAttachment attachment = findAttachment(entity, slotId);
        if (attachment != null) {
            attachment.setPrefabField(fieldName, prefabGuid);
        }
        if (prefabGuid == null || prefabGuid.isBlank()) {
            instance.putMember(fieldName, (Object) null);
            return;
        }
        instance.putMember(fieldName, runtime.hostApi().wrapPrefabTemplate(prefabGuid));
    }

    public Value readFieldValue(EntityId entity, int slotId, String fieldName, ScriptFieldSchema field) {
        Value instance = requireInstance(entity, slotId);
        if (instance == null) {
            return null;
        }
        if (instance.hasMember(fieldName)) {
            Value value = instance.getMember(fieldName);
            if (value != null && !value.isNull()) {
                return value;
            }
        }
        ScriptAttachment attachment = findAttachment(entity, slotId);
        if (attachment == null) {
            return null;
        }
        JsonNode stored = attachment.fields.getOrDefault(fieldName, field == null ? null : field.copyDefault());
        if (stored == null || stored.isNull()) {
            return null;
        }
        if (stored.isNumber()) {
            return Value.asValue(stored.asDouble());
        }
        if (stored.isBoolean()) {
            return Value.asValue(stored.asBoolean());
        }
        if (stored.isTextual()) {
            return Value.asValue(stored.asText());
        }
        if (stored.isObject()) {
            int sceneId = ScriptFieldApplicator.entitySceneId(stored);
            if (sceneId >= 0) {
                EntityId target = SceneObjectIds.findBySceneId(world, sceneId);
                if (!target.isNone()) {
                    return runtime.hostApi().wrapEntity(
                            runtime.hostApi().createEntityBinding(runtime.hostApi().scriptContext(), target)
                    );
                }
            }
        }
        return null;
    }

    private static double coerceDouble(Value value) {
        if (value == null || value.isNull()) {
            return 0d;
        }
        if (value.fitsInDouble()) {
            return value.asDouble();
        }
        if (value.isString()) {
            try {
                return Double.parseDouble(value.asString());
            } catch (NumberFormatException ignored) {
                return 0d;
            }
        }
        return 0d;
    }

    private static boolean coerceBoolean(Value value) {
        if (value == null || value.isNull()) {
            return false;
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        if (value.fitsInDouble()) {
            return value.asDouble() != 0d;
        }
        if (value.isString()) {
            return Boolean.parseBoolean(value.asString());
        }
        return false;
    }

    private static String coerceString(Value value) {
        if (value == null || value.isNull()) {
            return "";
        }
        if (value.isString()) {
            return value.asString();
        }
        if (value.fitsInDouble()) {
            return String.valueOf(value.asDouble());
        }
        if (value.isBoolean()) {
            return Boolean.toString(value.asBoolean());
        }
        return value.toString();
    }

    private Value requireInstance(EntityId entity, int slotId) {
        if (world == null || entity == null || entity.isNone()) {
            return null;
        }
        JsScriptInstance instance = instances.get(key(entity, slotId));
        return instance == null ? null : instance.instance();
    }

    private ScriptAttachment findAttachment(EntityId entity, int slotId) {
        ScriptComponent container = world == null ? null : world.getComponent(entity, ScriptComponent.class);
        return container == null ? null : container.findBySlotId(slotId);
    }

    private static ScriptInstanceKey key(EntityId entity, int slotId) {
        return new ScriptInstanceKey(entity, slotId);
    }
}
