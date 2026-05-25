package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.log.StudioLogSink;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.js.ScriptGizmoDrawBuffer;

import java.nio.file.Path;

/**
 * Minimal Graal host API for edit-mode script gizmo hooks.
 */
public final class GizmoHostApi {
    private final Context context;
    private final ScriptContext scriptContext;
    private final JsValueFactory valueFactory;
    private final GizmosBinding gizmosBinding;
    private final Value scriptBaseClass;

    /**
     * @param context        Graal JS context
     * @param console        editor console
     * @param editScene      scene being edited
     * @param assets         project assets
     * @param projectRoot    project root
     * @param buffer         gizmo draw buffer for the current frame
     * @param lineWorld      wire thickness in world units
     */
    public GizmoHostApi(
            Context context,
            StudioLogSink console,
            Scene editScene,
            AssetDatabase assets,
            Path projectRoot,
            ScriptGizmoDrawBuffer buffer,
            float lineWorld
    ) {
        this.context = context;
        this.valueFactory = new JsValueFactory(context);
        this.scriptContext = new ScriptContext(editScene, assets, projectRoot);
        this.gizmosBinding = new GizmosBinding(buffer, lineWorld);
        this.scriptBaseClass = context.eval("js", """
                class Script {
                  constructor(host) {
                    this.entity = host.entity;
                    this.transform = host.transform;
                    this.enabled = host.enabled;
                  }
                }
                Script
                """);
    }

    /** Installs {@code Gizmos} and core math helpers. */
    public void install() {
        context.getBindings("js").putMember("Gizmos", gizmosBinding);
        SdkGlobalsInstaller.install(
                context,
                new MathBinding.Vec2Factory(),
                new MathBinding.Mathf(),
                new ColorMathBinding.ColorFactory(),
                new Rect2Binding.Rect2Factory()
        );
    }

    /**
     * @param entity         owning entity
     * @param scriptEnabled  attachment enabled flag
     * @return host object for script construction
     */
    public Value createHost(EntityId entity, boolean scriptEnabled) {
        EntityBinding entityBinding = new EntityBinding(scriptContext, null, entity);
        Value entityProxy = createReadOnlyEntityProxy(entityBinding);
        Value transformBinding = valueFactory.createTransformProxy(new TransformBinding(scriptContext.world(), entity));
        return context.eval("js", """
                (function (entity, transform, enabled) {
                  return { entity: entity, transform: transform, enabled: enabled };
                })
                """).execute(entityProxy, transformBinding, scriptEnabled);
    }

    private Value createReadOnlyEntityProxy(EntityBinding binding) {
        Value javaBinding = Value.asValue(binding);
        return context.eval("js", """
                (function (binding) {
                  return {
                    get id() { return binding.getId(); },
                    get name() { return binding.getName(); },
                    get worldX() { return binding.getWorldX(); },
                    get worldY() { return binding.getWorldY(); },
                  };
                })
                """).execute(javaBinding);
    }

    public ScriptContext scriptContext() {
        return scriptContext;
    }

    public Value scriptBaseClass() {
        return scriptBaseClass;
    }

    /** @return gizmo draw binding updated each frame */
    public GizmosBinding gizmosBinding() {
        return gizmosBinding;
    }

    public static HostAccess hostAccess() {
        return ScriptHostApi.hostAccess();
    }
}
