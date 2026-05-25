package org.llw.studio.scripting.js;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.ecs.ComponentStore;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.log.StudioLogSink;
import org.llw.studio.project.StudioProjectLayout;
import org.llw.studio.scene.ActiveUtility;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.ScriptAttachment;
import org.llw.studio.scripting.ScriptComponent;
import org.llw.studio.scripting.ScriptSchema;
import org.llw.studio.scripting.ScriptSchemaRegistry;
import org.llw.studio.scripting.js.bindings.GizmoHostApi;
import org.llw.studio.scripting.js.bindings.LoggerBinding;
import org.llw.studio.scripting.js.bindings.ScriptContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Edit-mode Graal runtime that invokes script gizmo lifecycle hooks.
 */
public final class EditorScriptGizmoRuntime implements AutoCloseable {
    private static final Engine SHARED_ENGINE = Engine.newBuilder("js")
            .option("engine.WarnInterpreterOnly", "false")
            .build();

    private final StudioLogSink console;
    private final AssetDatabase assets;
    private final Path projectRoot;
    private final ScriptGizmoDrawBuffer buffer = new ScriptGizmoDrawBuffer();
    private final Map<String, Value> factories = new HashMap<>();
    private final Map<InstanceKey, Value> instances = new HashMap<>();

    private Context context;
    private GizmoHostApi hostApi;
    private Scene editScene;

    /**
     * @param console     editor console
     * @param editScene   scene being edited
     * @param assets      project assets
     * @param projectRoot project root directory
     */
    public EditorScriptGizmoRuntime(
            StudioLogSink console,
            Scene editScene,
            AssetDatabase assets,
            Path projectRoot
    ) {
        this.console = console;
        this.editScene = editScene;
        this.assets = assets;
        this.projectRoot = projectRoot;
        openContext();
    }

    /**
     * @param editScene updated edit scene (clears instance cache when reference changes)
     */
    public void setEditScene(Scene editScene) {
        if (this.editScene != editScene) {
            this.editScene = editScene;
            instances.clear();
        }
    }

    /**
     * @param lineWorld wire thickness in world units for the current frame
     */
    public void prepareFrame(float lineWorld) {
        hostApi.gizmosBinding().setBuffer(buffer);
        hostApi.gizmosBinding().setLineWorld(lineWorld);
    }

    /** Drops cached script factories and instances (after recompile). */
    public void invalidateScripts() {
        factories.clear();
        instances.clear();
    }

    /**
     * @param selectedOnly when true, only {@code onDrawGizmosSelected} on selected entities
     * @param selection    selected entities for selected-only pass
     */
    public void invokeGizmos(boolean selectedOnly, Set<EntityId> selection) {
        if (editScene == null || context == null || hostApi == null) {
            return;
        }
        buffer.clear();
        long start = System.nanoTime();
        ComponentStore<ScriptComponent> scripts = editScene.world().store(ScriptComponent.class);
        for (int i = 0; i < scripts.size(); i++) {
            EntityId entity = scripts.entityAt(i);
            if (!ActiveUtility.isEffectivelyActive(editScene.world(), entity)) {
                continue;
            }
            if (selectedOnly && (selection == null || !selection.contains(entity))) {
                continue;
            }
            ScriptComponent component = scripts.componentAt(i);
            for (ScriptAttachment attachment : component.attachments) {
                if (!attachment.enabled || attachment.scriptGuid == null || attachment.scriptGuid.isBlank()) {
                    continue;
                }
                ScriptSchema schema = ScriptSchemaRegistry.get(projectRoot, attachment.scriptGuid, assets.resourceManager());
                boolean drawAll = !selectedOnly && schema.hasDrawGizmos;
                boolean drawSelected = selectedOnly && schema.hasDrawGizmosSelected;
                if (!drawAll && !drawSelected) {
                    continue;
                }
                String hook = drawSelected ? "onDrawGizmosSelected" : "onDrawGizmos";
                invokeHook(entity, attachment, hook);
                if (System.nanoTime() - start > 2_000_000L) {
                    break;
                }
            }
        }
    }

    public ScriptGizmoDrawBuffer buffer() {
        return buffer;
    }

    @Override
    public void close() {
        instances.clear();
        factories.clear();
        if (context != null) {
            context.close();
            context = null;
        }
        hostApi = null;
    }

    private void openContext() {
        if (context != null) {
            context.close();
        }
        context = Context.newBuilder("js")
                .engine(SHARED_ENGINE)
                .allowHostAccess(GizmoHostApi.hostAccess())
                .allowHostClassLookup(className -> false)
                .build();
        hostApi = new GizmoHostApi(context, console, editScene, assets, projectRoot, buffer, 1f);
        hostApi.install();
    }

    private void invokeHook(EntityId entity, ScriptAttachment attachment, String hook) {
        try {
            Value instance = instanceFor(entity, attachment);
            if (instance == null || !instance.hasMember(hook)) {
                return;
            }
            Value callable = instance.getMember(hook);
            if (!callable.canExecute()) {
                return;
            }
            ScriptContext ctx = hostApi.scriptContext();
            ctx.setScriptGuid(attachment.scriptGuid);
            ctx.setEntityName(readEntityName(entity));
            LoggerBinding.setPrefix(ctx.logPrefix());
            try {
                instance.invokeMember(hook);
            } finally {
                LoggerBinding.clearPrefix();
            }
        } catch (PolyglotException ex) {
            console.append(org.llw.util.log.LogLevel.WARN, "Script gizmo: " + ex.getMessage());
        }
    }

    private Value instanceFor(EntityId entity, ScriptAttachment attachment) {
        InstanceKey key = new InstanceKey(entity, attachment.slotId, attachment.scriptGuid);
        Value cached = instances.get(key);
        if (cached != null) {
            return cached;
        }
        Value factory = factories.computeIfAbsent(attachment.scriptGuid, this::loadFactoryUnchecked);
        if (factory == null) {
            return null;
        }
        hostApi.scriptContext().setScriptGuid(attachment.scriptGuid);
        Value host = hostApi.createHost(entity, attachment.enabled);
        Value scriptClass = resolveScriptClass(factory);
        if (!scriptClass.canInstantiate()) {
            return null;
        }
        Value created = scriptClass.newInstance(host);
        instances.put(key, created);
        return created;
    }

    private Value loadFactoryUnchecked(String guid) {
        try {
            Path bundled = StudioProjectLayout.scriptCachePath(projectRoot, guid);
            if (!Files.exists(bundled)) {
                return null;
            }
            String source = ScriptBundler.sanitizeBundledSource(Files.readString(bundled));
            Value result = context.eval("js", source);
            return resolveFactory(result, context.getBindings("js"));
        } catch (IOException | PolyglotException ex) {
            return null;
        }
    }

    private static Value resolveFactory(Value evalResult, Value bindings) {
        if (evalResult != null && evalResult.canExecute()) {
            try {
                Value llw = bindings.getMember("LLW");
                Value produced = evalResult.execute(llw);
                if (produced != null && !produced.isNull() && produced.canInstantiate()) {
                    return produced;
                }
            } catch (PolyglotException ignored) {
            }
        }
        if (evalResult != null && evalResult.canInstantiate()) {
            return evalResult;
        }
        return null;
    }

    private static Value resolveScriptClass(Value factory) {
        if (factory.canExecute()) {
            try {
                Value result = factory.execute();
                if (result != null && !result.isNull() && result.canInstantiate()) {
                    return result;
                }
            } catch (PolyglotException ignored) {
            }
        }
        if (factory.canInstantiate()) {
            return factory;
        }
        throw new IllegalStateException("Script factory is not constructible");
    }

    private String readEntityName(EntityId entity) {
        var name = editScene.world().getComponent(entity, org.llw.studio.ecs.components.NameComponent.class);
        return name == null ? "" : name.name();
    }

    private record InstanceKey(EntityId entity, int slotId, String scriptGuid) {
    }
}
