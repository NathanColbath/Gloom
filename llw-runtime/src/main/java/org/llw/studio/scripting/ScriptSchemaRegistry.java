package org.llw.studio.scripting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.llw.resources.ResourceManager;
import org.llw.studio.project.StudioProjectLayout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and caches {@link ScriptSchema} JSON files produced when scripts are bundled.
 */
public final class ScriptSchemaRegistry {
    /** Resource id suffix for script schemas packed into {@code scripts.pack}. */
    public static final String PACKED_SCHEMA_SUFFIX = "__schema";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, ScriptSchema> CACHE = new ConcurrentHashMap<>();

    private ScriptSchemaRegistry() {
    }

    /**
     * @param projectRoot project root directory
     * @param scriptGuid  script asset GUID
     * @return cached schema, or {@link ScriptSchema#empty()} when missing or invalid
     */
    public static ScriptSchema get(Path projectRoot, String scriptGuid) {
        return get(projectRoot, scriptGuid, null);
    }

    /**
     * @param projectRoot project root directory
     * @param scriptGuid  script asset GUID
     * @param resources   optional pack-backed resource manager for published players
     * @return cached schema, or {@link ScriptSchema#empty()} when missing or invalid
     */
    public static ScriptSchema get(Path projectRoot, String scriptGuid, ResourceManager resources) {
        if (scriptGuid == null || scriptGuid.isBlank()) {
            return ScriptSchema.empty();
        }
        return CACHE.computeIfAbsent(scriptGuid, guid -> load(projectRoot, guid, resources));
    }

    /**
     * @param scriptGuid script asset GUID to evict from the cache
     */
    public static void invalidate(String scriptGuid) {
        if (scriptGuid != null) {
            CACHE.remove(scriptGuid);
        }
    }

    /** Clears the in-memory schema cache. */
    public static void clear() {
        CACHE.clear();
    }

    /**
     * @param scriptGuid script asset GUID
     * @return resource id used when packing schemas into {@code scripts.pack}
     */
    public static String packedSchemaId(String scriptGuid) {
        return scriptGuid + PACKED_SCHEMA_SUFFIX;
    }

    private static ScriptSchema load(Path projectRoot, String scriptGuid, ResourceManager resources) {
        Path schemaPath = StudioProjectLayout.resolveScriptSchemaPath(projectRoot, scriptGuid);
        if (Files.isRegularFile(schemaPath)) {
            try {
                return ScriptSchema.fromJson(MAPPER.readTree(schemaPath.toFile()));
            } catch (IOException ex) {
                return ScriptSchema.empty();
            }
        }
        if (resources != null) {
            String packedId = packedSchemaId(scriptGuid);
            if (resources.isRegistered(packedId)) {
                try (var ref = resources.acquireRaw(packedId)) {
                    return ScriptSchema.fromJson(MAPPER.readTree(ref.get()));
                } catch (Exception ex) {
                    return ScriptSchema.empty();
                }
            }
        }
        return ScriptSchema.empty();
    }
}
