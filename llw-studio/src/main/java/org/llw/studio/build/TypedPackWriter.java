package org.llw.studio.build;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.llw.resources.pack.AssetPackManifest;
import org.llw.resources.pack.AssetPackWriter;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.project.StudioProjectLayout;
import org.llw.studio.scripting.ScriptSchemaRegistry;
import org.llw.studio.scripting.js.ScriptCompileService;
import org.llw.studio.shadergraph.assets.ShaderGraphSerializer;
import org.llw.studio.shadergraph.compiler.ShaderGraphCompileResult;
import org.llw.studio.shadergraph.compiler.ShaderGraphCompiler;
import org.llw.studio.shadergraph.model.ShaderGraphDocument;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes typed LLWP pack files from a scanned {@link BuildAssetSet}.
 */
public final class TypedPackWriter {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int DEFAULT_FONT_SIZE = 24;

    private TypedPackWriter() {
    }

    /**
     * @param projectRoot project root
     * @param assets      asset database
     * @param assetSet    scanned assets
     * @param contentDir  output {@code content/} directory
     * @param stagingDir  temporary files for meta and compiled shaders
     * @throws IOException when a pack cannot be written
     */
    public static void writeAll(
            Path projectRoot,
            AssetDatabase assets,
            BuildAssetSet assetSet,
            Path contentDir,
            Path stagingDir
    ) throws IOException {
        Files.createDirectories(contentDir);
        Files.createDirectories(stagingDir);
        Path assetsRoot = StudioProjectLayout.assetsRoot(projectRoot);

        for (BuildPackCategory category : BuildPackCategory.values()) {
            List<StudioAsset> categoryAssets = assetSet.assets(category);
            if (categoryAssets.isEmpty()) {
                continue;
            }
            Map<String, AssetPackManifest.PackEntry> entries = new LinkedHashMap<>();
            for (StudioAsset asset : categoryAssets) {
                switch (category) {
                    case METADATA -> addMetadataEntry(projectRoot, assetsRoot, stagingDir, asset, entries);
                    case SCRIPTS -> addScriptEntry(projectRoot, asset, entries);
                    case SHADERS -> addShaderEntry(asset, stagingDir, entries);
                    default -> addFileEntry(category, asset, entries);
                }
            }
            if (!entries.isEmpty()) {
                AssetPackWriter.write(contentDir.resolve(category.fileName()), entries);
            }
        }
    }

    private static void addFileEntry(
            BuildPackCategory category,
            StudioAsset asset,
            Map<String, AssetPackManifest.PackEntry> entries
    ) throws IOException {
        org.llw.resources.AssetType engineType = engineType(category, asset);
        if (engineType == org.llw.resources.AssetType.FONT) {
            entries.put(asset.guid(), new AssetPackManifest.PackEntry(
                    engineType,
                    asset.path(),
                    DEFAULT_FONT_SIZE
            ));
        } else {
            entries.put(asset.guid(), new AssetPackManifest.PackEntry(engineType, asset.path()));
        }
    }

    private static void addScriptEntry(
            Path projectRoot,
            StudioAsset asset,
            Map<String, AssetPackManifest.PackEntry> entries
    ) {
        Path bundled = ScriptCompileService.bundledPath(projectRoot, asset.guid());
        if (!Files.isRegularFile(bundled)) {
            bundled = StudioProjectLayout.resolveScriptCachePath(projectRoot, asset.guid());
        }
        if (Files.isRegularFile(bundled)) {
            entries.put(asset.guid(), new AssetPackManifest.PackEntry(
                    org.llw.resources.AssetType.RAW,
                    bundled,
                    asset.guid() + ".js",
                    0
            ));
        }
        Path schemaPath = StudioProjectLayout.resolveScriptSchemaPath(projectRoot, asset.guid());
        if (Files.isRegularFile(schemaPath)) {
            entries.put(ScriptSchemaRegistry.packedSchemaId(asset.guid()), new AssetPackManifest.PackEntry(
                    org.llw.resources.AssetType.RAW,
                    schemaPath,
                    asset.guid() + ".schema.json",
                    0
            ));
        }
    }

    private static void addShaderEntry(
            StudioAsset asset,
            Path stagingDir,
            Map<String, AssetPackManifest.PackEntry> entries
    ) throws IOException {
        ShaderGraphDocument document = ShaderGraphSerializer.load(asset.path());
        if (document == null) {
            return;
        }
        ShaderGraphCompileResult compiled = ShaderGraphCompiler.compileFull(document);
        if (!compiled.success()) {
            return;
        }
        Path staged = stagingDir.resolve("shaders").resolve(asset.guid() + ".frag.glsl");
        Files.createDirectories(staged.getParent());
        Files.writeString(staged, compiled.fragmentSource(), StandardCharsets.UTF_8);
        entries.put(asset.guid(), new AssetPackManifest.PackEntry(
                org.llw.resources.AssetType.RAW,
                staged,
                asset.guid() + ".frag.glsl",
                0
        ));
    }

    private static void addMetadataEntry(
            Path projectRoot,
            Path assetsRoot,
            Path stagingDir,
            StudioAsset asset,
            Map<String, AssetPackManifest.PackEntry> entries
    ) throws IOException {
        MetaFile.MetaData meta;
        if (asset.type() == AssetType.SPRITE) {
            meta = new MetaFile.MetaData();
            meta.guid = asset.guid();
            meta.type = AssetType.SPRITE.name();
            meta.importer = MAPPER.createObjectNode();
            meta.importer.put("parentTextureGuid", asset.parentTextureGuid());
            meta.importer.put("displayName", asset.displayName());
        } else if (asset.isFolder()) {
            meta = MetaFile.read(projectRoot, assetsRoot, asset.path());
        } else {
            meta = MetaFile.read(projectRoot, assetsRoot, asset.path());
        }
        if (meta.importer == null) {
            meta.importer = MAPPER.createObjectNode();
        }
        if (asset.displayName() != null && !asset.displayName().isBlank()) {
            meta.importer.put("displayName", asset.displayName());
        }
        Path staged = stagingDir.resolve("metadata").resolve(asset.guid() + ".meta.json");
        Files.createDirectories(staged.getParent());
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(staged.toFile(), meta);
        entries.put(asset.guid(), new AssetPackManifest.PackEntry(
                org.llw.resources.AssetType.RAW,
                staged,
                asset.guid() + ".meta.json",
                0
        ));
    }

    private static org.llw.resources.AssetType engineType(BuildPackCategory category, StudioAsset asset) {
        return switch (category) {
            case TEXTURES -> org.llw.resources.AssetType.TEXTURE;
            case FONTS -> org.llw.resources.AssetType.FONT;
            case AUDIO -> org.llw.resources.AssetType.SOUND;
            case SCENES, PREFABS, ANIMATIONS, PARTICLES -> org.llw.resources.AssetType.RAW;
            default -> org.llw.resources.AssetType.RAW;
        };
    }
}
