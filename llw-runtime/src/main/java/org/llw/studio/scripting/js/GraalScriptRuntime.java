package org.llw.studio.scripting.js;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.log.StudioLogSink;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.js.bindings.ScriptHostApi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * GraalVM JavaScript context for play mode, including host API installation.
 */
public final class GraalScriptRuntime implements AutoCloseable {
    private static final Engine SHARED_ENGINE = Engine.newBuilder("js")
            .option("engine.WarnInterpreterOnly", "false")
            .build();

    private final Context context;
    private final ScriptHostApi hostApi;

    /** Pre-initializes the shared Graal engine so the first play scene starts faster. */
    public static void warmupSharedEngine() {
        try (Context ctx = Context.newBuilder("js")
                .engine(SHARED_ENGINE)
                .allowHostAccess(ScriptHostApi.hostAccess())
                .allowHostClassLookup(className -> false)
                .build()) {
            ctx.eval("js", "0");
        }
    }

    /**
     * @param console     editor console for script logs
     * @param playScene   cloned play-mode scene
     * @param assets      project asset database
     * @param projectRoot project root for schema and asset resolution
     */
    public GraalScriptRuntime(StudioLogSink console, Scene playScene, AssetDatabase assets, Path projectRoot) {
        this.context = Context.newBuilder("js")
                .engine(SHARED_ENGINE)
                .allowHostAccess(ScriptHostApi.hostAccess())
                .allowHostClassLookup(className -> false)
                .build();
        this.hostApi = new ScriptHostApi(context, console, playScene, assets, projectRoot);
        hostApi.install();
    }

    /**
     * @param bundledPath path to a cached bundled script
     * @return factory value suitable for {@link JsScriptInstance#create}
     * @throws IOException when the bundle cannot be read or evaluated
     */
    public Value loadFactory(Path bundledPath) throws IOException {
        String source = ScriptBundler.sanitizeBundledSource(Files.readString(bundledPath));
        return loadFactorySource(source);
    }

    /**
     * @param source bundled JavaScript source text
     * @return factory value suitable for {@link JsScriptInstance#create}
     * @throws IOException when the bundle cannot be evaluated
     */
    public Value loadFactorySource(String source) throws IOException {
        try {
            Value result = context.eval("js", source);
            return resolveFactory(result, context.getBindings("js"));
        } catch (PolyglotException ex) {
            throw new IOException(ex.getMessage(), ex);
        }
    }

    /**
     * @param evalResult result of evaluating bundled source
     * @param bindings   JS global bindings
     * @return script factory export
     */
    static Value resolveFactory(Value evalResult, Value bindings) {
        if (evalResult != null && !evalResult.isNull() && (evalResult.canExecute() || evalResult.canInstantiate())) {
            return evalResult;
        }
        if (bindings == null) {
            return evalResult;
        }
        Value bundle = bindings.getMember("__LLWScriptBundle");
        if (bundle == null || bundle.isNull()) {
            return evalResult;
        }
        if (bundle.hasMember("default")) {
            Value defaultExport = bundle.getMember("default");
            if (defaultExport != null && !defaultExport.isNull()) {
                return defaultExport;
            }
        }
        return bundle;
    }

    /**
     * @return play-mode host API for this runtime
     */
    public ScriptHostApi hostApi() {
        return hostApi;
    }

    /**
     * @return underlying Graal context
     */
    public Context context() {
        return context;
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        context.close();
    }
}
