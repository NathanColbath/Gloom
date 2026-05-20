package org.llw.studio.assets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.llw.resources.pack.AssetPackManifest;
import org.llw.resources.pack.AssetPackReader;
import org.llw.resources.ResourceManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds an {@link AssetDatabase} index from published pack content.
 */
public final class PackBootstrap {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PackBootstrap() {
    }

    /**
     * Loads all packs listed in {@code game.manifest.json} and indexes metadata for runtime lookup.
     *
     * @param contentDir  directory containing {@code game.manifest.json} and {@code *.pack} files
     * @param resources   shared resource manager
     * @return asset database backed by loaded packs
     * @throws IOException when manifest or packs cannot be read
     */
    public static AssetDatabase bootstrap(Path contentDir, ResourceManager resources) throws IOException {
        Path manifestPath = contentDir.resolve("game.manifest.json");
        var manifestTree = MAPPER.readTree(manifestPath.toFile());
        for (JsonPackName packName : readPackNames(manifestTree.path("packs"))) {
            if ("metadata.pack".equals(packName.value)) {
                continue;
            }
            Path packPath = contentDir.resolve(packName.value);
            if (Files.isRegularFile(packPath)) {
                resources.loadPackFile(packPath);
            }
        }
        AssetDatabase assets = new AssetDatabase(contentDir, resources);
        Path metadataPack = contentDir.resolve("metadata.pack");
        if (Files.isRegularFile(metadataPack)) {
            indexMetadata(contentDir, assets, metadataPack);
        } else {
            assets.refresh();
        }
        return assets;
    }

    private static void indexMetadata(Path contentDir, AssetDatabase assets, Path metadataPack) throws IOException {
        AssetPackReader reader = AssetPackReader.fromFile(metadataPack);
        for (Map.Entry<String, AssetPackManifest.Entry> entry : reader.manifest().entries().entrySet()) {
            byte[] bytes = reader.slice(entry.getValue());
            MetaFile.MetaData meta = MAPPER.readValue(bytes, MetaFile.MetaData.class);
            AssetType type = AssetType.valueOf(meta.type);
            Path virtualPath = contentDir.resolve("packed").resolve(entry.getKey());
            String displayName = publishedDisplayName(meta, entry.getKey());
            StudioAsset asset = new StudioAsset(
                    meta.guid,
                    virtualPath,
                    type,
                    displayName,
                    Instant.EPOCH,
                    spriteParentGuid(meta),
                    animationParentGuid(meta)
            );
            assets.indexPublishedAsset(asset, meta);
        }
        assets.finalizePublishedIndex();
    }

    private static String spriteParentGuid(MetaFile.MetaData meta) {
        if (!AssetType.SPRITE.name().equals(meta.type) || meta.importer == null) {
            return null;
        }
        return meta.importer.path("parentTextureGuid").asText(null);
    }

    /**
     * @return human-readable asset name stored during publish, or {@code guid} when absent
     */
    static String publishedDisplayName(MetaFile.MetaData meta, String guid) {
        if (meta != null && meta.importer != null) {
            String fromMeta = meta.importer.path("displayName").asText("");
            if (!fromMeta.isBlank()) {
                return fromMeta;
            }
        }
        return guid;
    }

    private static String animationParentGuid(MetaFile.MetaData meta) {
        if (!AssetType.ANIMATION_CLIP.name().equals(meta.type) || meta.importer == null) {
            return null;
        }
        return meta.importer.path("parentAnimationGuid").asText(null);
    }

    private static List<JsonPackName> readPackNames(com.fasterxml.jackson.databind.JsonNode packsNode) {
        List<JsonPackName> names = new ArrayList<>();
        if (packsNode == null || !packsNode.isArray()) {
            return names;
        }
        for (com.fasterxml.jackson.databind.JsonNode node : packsNode) {
            names.add(new JsonPackName(node.asText("")));
        }
        return names;
    }

    private record JsonPackName(String value) {
    }
}
