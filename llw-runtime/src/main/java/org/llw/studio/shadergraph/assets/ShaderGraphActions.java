package org.llw.studio.shadergraph.assets;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.MetaFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Creates shader graph assets on disk.
 */
public final class ShaderGraphActions {
    private ShaderGraphActions() {
    }

    /**
     * @param assets       asset database
     * @param parentFolder folder under Assets, or null for root
     * @param baseName     name without extension
     * @return new asset GUID
     */
    public static String createShaderGraph(AssetDatabase assets, Path parentFolder, String baseName) throws IOException {
        Path assetsRoot = assets.assetsRoot();
        Path folder = parentFolder == null ? assetsRoot : parentFolder;
        Files.createDirectories(folder);
        String safe = baseName == null || baseName.isBlank() ? "NewShader" : baseName.trim();
        if (!safe.endsWith(".shadergraph.json")) {
            safe = safe + ".shadergraph.json";
        }
        Path path = uniquePath(folder, safe);
        ShaderGraphSerializer.save(path, ShaderGraphSerializer.newDefaultGraph());
        MetaFile.MetaData meta = MetaFile.read(assets.projectRoot(), assetsRoot, path);
        meta.type = AssetType.SHADER_GRAPH.name();
        MetaFile.write(assets.projectRoot(), assetsRoot, path, meta);
        assets.refresh();
        assets.bumpShaderGraphRevision(meta.guid);
        return meta.guid;
    }

    private static Path uniquePath(Path folder, String fileName) {
        Path candidate = folder.resolve(fileName);
        if (!Files.exists(candidate)) {
            return candidate;
        }
        String stem = fileName;
        String ext = "";
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            stem = fileName.substring(0, dot);
            ext = fileName.substring(dot);
        }
        int i = 1;
        while (Files.exists(candidate)) {
            candidate = folder.resolve(stem + i + ext);
            i++;
        }
        return candidate;
    }
}
