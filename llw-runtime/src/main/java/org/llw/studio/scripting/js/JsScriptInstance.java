package org.llw.studio.scripting.js;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.scripting.ScriptAttachment;
import org.llw.studio.scripting.js.bindings.LoggerBinding;
import org.llw.studio.scripting.js.bindings.ScriptContext;
import org.llw.studio.scripting.ScriptSchema;
import org.llw.studio.scripting.ScriptSchemaRegistry;
import org.llw.studio.scripting.js.bindings.ScriptHostApi;
import org.llw.studio.scripting.js.ScriptFieldApplicator;

/**
 * Live Graal script instance with lifecycle hooks ({@code start}, {@code update}, {@code onDestroy}).
 */
public final class JsScriptInstance {
    private final Value instance;
    private final ScriptContext context;
    private final String scriptName;
    private boolean started;

    private JsScriptInstance(Value instance, ScriptContext context, String scriptName) {
        this.instance = instance;
        this.context = context;
        this.scriptName = scriptName;
    }

    /**
     * @param graalContext Graal JS context
     * @param hostApi      play-mode host API
     * @param entity       owning entity
     * @param attachment   serialized script attachment
     * @param scriptName   display name for logging
     * @param factory      bundled script factory export
     * @return running script instance with fields applied
     * @throws IllegalStateException when the factory is not constructible
     */
    public static JsScriptInstance create(
            Context graalContext,
            ScriptHostApi hostApi,
            EntityId entity,
            ScriptAttachment attachment,
            String scriptName,
            Value factory
    ) {
        ScriptContext context = hostApi.scriptContext();
        context.setScriptGuid(attachment.scriptGuid);
        context.setScriptName(scriptName);
        context.setEntityName(readEntityName(context, entity));
        Value host = hostApi.createHost(context, entity, attachment.enabled);
        Value llw = graalContext.getBindings("js").getMember("LLW");
        Value scriptClass = resolveScriptClass(factory, llw);
        if (!scriptClass.canInstantiate()) {
            throw new IllegalStateException("Script factory did not return a constructible class");
        }
        Value created = scriptClass.newInstance(host);
        ScriptSchema schema = ScriptSchemaRegistry.get(
                hostApi.scriptContext().projectRoot(),
                attachment.scriptGuid,
                hostApi.scriptContext().assets().resourceManager()
        );
        ScriptFieldApplicator.applySerializedFields(
                created,
                hostApi,
                context.world(),
                attachment,
                schema
        );
        return new JsScriptInstance(created, context, scriptName);
    }

    /**
     * @return underlying Graal script object
     */
    public Value instance() {
        return instance;
    }

    /**
     * @return script display name used for logging
     */
    public String scriptName() {
        return scriptName;
    }

    public String logPrefix() {
        return context.logPrefix();
    }

    /**
     * @param scriptName asset or class file name
     * @param className  script class name from the bundle
     * @return {@code true} when the names refer to the same script
     */
    public static boolean namesMatch(String scriptName, String className) {
        if (scriptName == null || className == null || className.isBlank()) {
            return false;
        }
        String normalized = scriptName;
        int dot = normalized.lastIndexOf('.');
        if (dot >= 0) {
            normalized = normalized.substring(0, dot);
        }
        return className.equals(normalized);
    }

    private static String readEntityName(ScriptContext context, EntityId entity) {
        var name = context.world().getComponent(entity, org.llw.studio.ecs.components.NameComponent.class);
        return name == null ? "" : name.name();
    }

    private static Value resolveScriptClass(Value factory, Value llw) {
        if (factory.canExecute()) {
            try {
                Value result = factory.execute(llw);
                if (result != null && !result.isNull() && result.canInstantiate()) {
                    return result;
                }
            } catch (PolyglotException ignored) {
                // Bare script classes are also executable but must be constructed with `new`.
            }
        }
        if (factory.canInstantiate()) {
            return factory;
        }
        throw new IllegalStateException("Script factory must be a bundle function or script class");
    }

    /** Invokes {@code start} once when not already started. */
    public void start() {
        if (started) {
            return;
        }
        invokeIfPresent("start");
        started = true;
    }

    /** Invokes {@code update} when present. */
    public void update() {
        invokeIfPresent("update");
    }

    /** Invokes {@code onDestroy} when present. */
    public void destroy() {
        invokeIfPresent("onDestroy");
    }

    private void invokeIfPresent(String member) {
        if (instance == null || !instance.hasMember(member)) {
            return;
        }
        Value callable = instance.getMember(member);
        if (!callable.canExecute()) {
            return;
        }
        LoggerBinding.setPrefix(context.logPrefix());
        try {
            instance.invokeMember(member);
        } catch (PolyglotException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        } finally {
            LoggerBinding.clearPrefix();
        }
    }
}
