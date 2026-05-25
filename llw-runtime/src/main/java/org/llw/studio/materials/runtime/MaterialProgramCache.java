package org.llw.studio.materials.runtime;

import org.llw.render.backend.MaterialShaderTarget;
import org.llw.render.gl.ShaderLibrary;
import org.llw.render.graphics.ShaderProgram;
import org.llw.render.graphics.Texture2d;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.materials.model.MaterialDocument;
import org.llw.studio.materials.model.MaterialProperty;
import org.llw.studio.materials.model.MaterialPropertyType;
import org.llw.studio.materials.model.MaterialShaderSource;
import org.llw.studio.shadergraph.runtime.ShaderGraphProgramCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves material assets to {@link ResolvedMaterial} instances.
 */
public final class MaterialProgramCache {
    private final AssetDatabase assets;
    private final ShaderLibrary shaderLibrary;
    private final ShaderGraphProgramCache shaderGraphs;
    private final Map<String, ResolvedMaterial> cache = new HashMap<>();
    private final Map<String, Integer> revisionByGuid = new HashMap<>();

    public MaterialProgramCache(
            AssetDatabase assets,
            ShaderLibrary shaderLibrary,
            ShaderGraphProgramCache shaderGraphs
    ) {
        this.assets = assets;
        this.shaderLibrary = shaderLibrary;
        this.shaderGraphs = shaderGraphs;
    }

    public ResolvedMaterial resolve(String materialGuid) {
        if (materialGuid == null || materialGuid.isBlank() || assets == null) {
            return null;
        }
        int revision = assets.materialRevision(materialGuid);
        Integer cachedRevision = revisionByGuid.get(materialGuid);
        if (cachedRevision != null && cachedRevision == revision) {
            ResolvedMaterial hit = cache.get(materialGuid);
            if (hit != null) {
                return hit;
            }
        }
        StudioAsset asset = assets.get(materialGuid);
        MaterialDocument document = assets.loadMaterial(materialGuid);
        if (document == null) {
            return null;
        }
        MaterialShaderSource source = parseSource(document.shaderSource);
        ShaderProgram program = programFor(document, source);
        Texture2d normalMap = resolveNormalMap(document);
        boolean useNormal = normalMap != null;
        ResolvedMaterial resolved = new ResolvedMaterial(source, program, normalMap, useNormal);
        for (MaterialProperty property : document.properties) {
            MaterialPropertyType type = parseType(property.type);
            if (type == MaterialPropertyType.FLOAT) {
                resolved.floats.put(property.name, property.floatValue);
            } else if (type == MaterialPropertyType.COLOR) {
                resolved.colors.put(property.name, new float[]{property.r, property.g, property.b, property.a});
            }
        }
        cache.put(materialGuid, resolved);
        revisionByGuid.put(materialGuid, revision);
        return resolved;
    }

    public void invalidate(String guid) {
        if (guid != null) {
            cache.remove(guid);
            revisionByGuid.remove(guid);
        }
    }

    public void invalidateAll() {
        cache.clear();
        revisionByGuid.clear();
    }

    private ShaderProgram programFor(MaterialDocument document, MaterialShaderSource source) {
        return switch (source) {
            case BUILTIN_UNLIT -> shaderLibrary.spriteShader();
            case BUILTIN_LIT -> shaderLibrary.litSpriteShader();
            case SHADER_GRAPH -> {
                if (!MaterialShaderTarget.supportsShaderGraphs()) {
                    yield shaderLibrary.litSpriteShader();
                }
                if (shaderGraphs == null
                        || document.shaderGraphGuid == null
                        || document.shaderGraphGuid.isBlank()) {
                    yield shaderLibrary.litSpriteShader();
                }
                ShaderProgram graph = shaderGraphs.programLit(document.shaderGraphGuid);
                if (graph == null) {
                    graph = shaderGraphs.program(document.shaderGraphGuid);
                }
                yield graph != null ? graph : shaderLibrary.litSpriteShader();
            }
        };
    }

    private Texture2d resolveNormalMap(MaterialDocument document) {
        String guid = document.normalMapTextureGuid;
        if (guid == null || guid.isBlank()) {
            return null;
        }
        return assets.texture(guid);
    }

    private static MaterialShaderSource parseSource(String raw) {
        if (raw == null || raw.isBlank()) {
            return MaterialShaderSource.BUILTIN_LIT;
        }
        try {
            return MaterialShaderSource.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            return MaterialShaderSource.BUILTIN_LIT;
        }
    }

    private static MaterialPropertyType parseType(String raw) {
        if (raw == null || raw.isBlank()) {
            return MaterialPropertyType.FLOAT;
        }
        try {
            return MaterialPropertyType.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            return MaterialPropertyType.FLOAT;
        }
    }
}
