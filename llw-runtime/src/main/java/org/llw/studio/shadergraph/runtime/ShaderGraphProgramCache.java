package org.llw.studio.shadergraph.runtime;

import org.llw.render.graphics.ShaderProgram;
import org.llw.render.gl.ShaderLibrary;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.shadergraph.assets.ShaderGraphSerializer;
import org.llw.studio.shadergraph.compiler.ShaderGraphCompileResult;
import org.llw.studio.shadergraph.compiler.ShaderGraphCompiler;
import org.llw.render.gl.DefaultShaders;
import org.llw.studio.shadergraph.compiler.ShaderGraphTemplates;
import org.llw.studio.shadergraph.model.ShaderGraphDocument;
import org.llw.util.log.Log;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Compiles and caches shader graph programs for runtime drawing.
 */
public final class ShaderGraphProgramCache {
    private static final Logger log = Log.get(Loggers.GL);
    private static final String NAME_PREFIX = "sg_";

    private final ShaderLibrary shaderLibrary;
    private final AssetDatabase assets;
    private final Map<String, Entry> cache = new HashMap<>();

    public ShaderGraphProgramCache(ShaderLibrary shaderLibrary, AssetDatabase assets) {
        this.shaderLibrary = shaderLibrary;
        this.assets = assets;
    }

    /**
     * @param assetGuid shader graph asset GUID
     * @return linked program, or null if compile failed or asset missing
     */
    public ShaderProgram program(String assetGuid) {
        if (assetGuid == null || assetGuid.isBlank()) {
            return null;
        }
        StudioAsset asset = assets.get(assetGuid);
        if (asset == null) {
            return null;
        }
        int revision = assets.shaderGraphRevision(assetGuid);
        Entry entry = cache.get(assetGuid);
        if (entry != null && entry.revision == revision && entry.program != null) {
            return entry.program;
        }
        ShaderGraphDocument document = assets.loadShaderGraph(asset.path());
        if (document == null) {
            return null;
        }
        ShaderGraphCompileResult compiled = ShaderGraphCompiler.compileFull(document);
        if (!compiled.success()) {
            log.warn("Shader graph compile failed guid={}: {}", assetGuid, compiled.errorMessage());
            cache.put(assetGuid, new Entry(revision, null));
            return null;
        }
        String name = NAME_PREFIX + assetGuid;
        ShaderProgram program = shaderLibrary.reloadFromSources(
                name,
                ShaderGraphTemplates.SPRITE_VERTEX,
                compiled.fragmentSource()
        );
        cache.put(assetGuid, new Entry(revision, program));
        return program;
    }

    public void invalidate(String assetGuid) {
        if (assetGuid != null) {
            cache.remove(assetGuid);
        }
    }

    public void invalidateAll() {
        cache.clear();
    }

    public void refreshFromDisk(Path path) {
        StudioAsset asset = assets.findByPath(path);
        if (asset != null) {
            invalidate(asset.guid());
        }
    }

    /**
     * Compiles a shader graph with the lit sprite vertex shader for material workflows.
     */
    public ShaderProgram programLit(String assetGuid) {
        if (assetGuid == null || assetGuid.isBlank()) {
            return null;
        }
        StudioAsset asset = assets.get(assetGuid);
        if (asset == null) {
            return null;
        }
        int revision = assets.shaderGraphRevision(assetGuid);
        String cacheKey = NAME_PREFIX + "lit_" + assetGuid;
        Entry entry = cache.get(cacheKey);
        if (entry != null && entry.revision == revision && entry.program != null) {
            return entry.program;
        }
        ShaderGraphDocument document = assets.loadShaderGraph(asset.path());
        if (document == null) {
            return null;
        }
        ShaderGraphCompileResult compiled = ShaderGraphCompiler.compileFull(document);
        if (!compiled.success()) {
            return null;
        }
        ShaderProgram program = shaderLibrary.reloadFromSources(
                cacheKey,
                DefaultShaders.LIT_SPRITE_VERTEX,
                compiled.fragmentSource()
        );
        cache.put(cacheKey, new Entry(revision, program));
        return program;
    }

    private record Entry(int revision, ShaderProgram program) {
    }
}
