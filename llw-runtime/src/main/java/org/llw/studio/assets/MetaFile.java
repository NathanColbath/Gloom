package org.llw.studio.assets;



import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.llw.studio.project.StudioProjectLayout;



import java.io.IOException;

import java.nio.file.Files;

import java.nio.file.Path;



/**

 * Reads and writes asset metadata (GUID, type, importer) under {@code .studio/metadata/assets/}.

 */

public final class MetaFile {

    private static final ObjectMapper MAPPER = new ObjectMapper();



    private MetaFile() {

    }



    /**

     * Mutable representation of asset metadata JSON fields.

     */

    public static final class MetaData {

        /** Stable asset identifier. */

        public String guid;

        /** {@link AssetType} name stored in JSON. */

        public String type;

        /** Importer-specific settings object. */

        public ObjectNode importer;

    }



    /**

     * Loads meta for {@code assetPath}, creating or upgrading when missing or incomplete.

     * Reads from the metadata tree first, then legacy sidecars next to the asset.

     *

     * @param projectRoot project root directory

     * @param assetsRoot  project's {@code Assets} directory

     * @param assetPath   path to the asset file or folder

     * @return parsed (and possibly persisted) meta data

     * @throws IOException if meta cannot be read or written

     */

    public static MetaData read(Path projectRoot, Path assetsRoot, Path assetPath) throws IOException {

        Path readFrom = resolveReadPath(projectRoot, assetsRoot, assetPath);

        if (!Files.exists(readFrom)) {

            MetaData data = new MetaData();

            data.guid = Guid.newGuid();

            data.type = inferType(assetPath).name();

            data.importer = MAPPER.createObjectNode();

            write(projectRoot, assetsRoot, assetPath, data);

            return data;

        }

        MetaData data = MAPPER.readValue(readFrom.toFile(), MetaData.class);

        boolean upgraded = false;

        if (data.guid == null || data.guid.isBlank()) {

            data.guid = Guid.newGuid();

            upgraded = true;

        }

        AssetType inferred = inferType(assetPath);

        if (data.type == null || data.type.isBlank() || AssetType.UNKNOWN.name().equals(data.type)) {

            if (inferred != AssetType.UNKNOWN || data.type == null || data.type.isBlank()) {

                data.type = inferred.name();

                upgraded = true;

            }

        }

        if (data.importer == null) {

            data.importer = MAPPER.createObjectNode();

            upgraded = true;

        }

        Path centralized = metaPath(projectRoot, assetsRoot, assetPath);

        if (upgraded || !readFrom.equals(centralized)) {

            write(projectRoot, assetsRoot, assetPath, data);

            if (!readFrom.equals(centralized)) {

                Files.deleteIfExists(readFrom);

            }

        }

        return data;

    }



    /**

     * Writes meta to {@code .studio/metadata/assets/} (never to legacy sidecars).

     *

     * @param projectRoot project root directory

     * @param assetsRoot  project's {@code Assets} directory

     * @param assetPath   path to the asset file or folder

     * @param data        meta fields to persist

     * @throws IOException if meta cannot be written

     */

    public static void write(Path projectRoot, Path assetsRoot, Path assetPath, MetaData data) throws IOException {

        Path meta = metaPath(projectRoot, assetsRoot, assetPath);

        Files.createDirectories(meta.getParent());

        ObjectNode node = MAPPER.createObjectNode();

        node.put("guid", data.guid);

        node.put("type", data.type);

        node.set("importer", data.importer == null ? MAPPER.createObjectNode() : data.importer);

        Files.writeString(meta, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node));

    }



    /**

     * @param projectRoot project root directory

     * @param assetsRoot  project's {@code Assets} directory

     * @param assetPath   path to the asset file or folder

     * @return path to metadata file under {@code .studio/metadata/assets/}

     */

    public static Path metaPath(Path projectRoot, Path assetsRoot, Path assetPath) {

        return StudioProjectLayout.assetsMetaPath(projectRoot, assetsRoot, assetPath);

    }



    /**

     * Deletes centralized and legacy meta files for an asset.

     *

     * @param projectRoot project root directory

     * @param assetsRoot  project's {@code Assets} directory

     * @param assetPath   path to the asset file or folder

     * @throws IOException if deletion fails

     */

    public static void deleteMeta(Path projectRoot, Path assetsRoot, Path assetPath) throws IOException {

        Files.deleteIfExists(metaPath(projectRoot, assetsRoot, assetPath));

        Files.deleteIfExists(StudioProjectLayout.legacyAssetsMetaPath(assetPath));

    }



    private static Path resolveReadPath(Path projectRoot, Path assetsRoot, Path assetPath) {

        Path centralized = metaPath(projectRoot, assetsRoot, assetPath);

        if (Files.exists(centralized)) {

            return centralized;

        }

        Path legacy = StudioProjectLayout.legacyAssetsMetaPath(assetPath);

        if (Files.exists(legacy)) {

            return legacy;

        }

        return centralized;

    }



    /**

     * @param path asset file or folder path

     * @return inferred {@link AssetType} from extension and whether the path is a directory

     */

    public static AssetType inferType(Path path) {

        if (Files.isDirectory(path)) {

            return AssetType.FOLDER;

        }

        String name = path.getFileName().toString().toLowerCase();

        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".webp")) {

            return AssetType.TEXTURE;

        }

        if (name.endsWith(".ttf") || name.endsWith(".otf")) {

            return AssetType.FONT;

        }

        if (name.endsWith(".wav") || name.endsWith(".ogg")) {

            return AssetType.AUDIO;

        }

        if (name.endsWith(".scene.json")) {

            return AssetType.SCENE;

        }

        if (name.endsWith(".prefab.json")) {

            return AssetType.PREFAB;

        }

        if (name.endsWith(".animation.json")) {

            return AssetType.ANIMATION;

        }

        if (name.endsWith(".anim.json")) {

            return AssetType.ANIMATION_CLIP;

        }

        if (name.endsWith(".shadergraph.json")) {

            return AssetType.SHADER_GRAPH;

        }

        if (name.endsWith(".particle.json")) {

            return AssetType.PARTICLE_SYSTEM;

        }

        if (name.endsWith(".java") || name.endsWith(".js") || name.endsWith(".ts")) {

            return AssetType.SCRIPT;

        }

        return AssetType.UNKNOWN;

    }

}


