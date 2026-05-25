package org.llw.studio.materials.assets;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.MetaFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Creates material assets on disk.
 */
public final class MaterialActions {
    private MaterialActions() {
    }

    /**
     * @param assets       asset database
     * @param parentFolder folder under Assets, or null for root
     * @param baseName     name without extension
     * @return new asset GUID
     */
    public static String createMaterial(AssetDatabase assets, Path parentFolder, String baseName) throws IOException {
        Path assetsRoot = assets.assetsRoot();
        Path folder = parentFolder == null ? assetsRoot : parentFolder;
        Files.createDirectories(folder);
        String safe = baseName == null || baseName.isBlank() ? "NewMaterial" : baseName.trim();
        if (!safe.endsWith(".material.json")) {
            safe = safe + ".material.json";
        }
        Path path = uniquePath(folder, safe);
        MaterialSerializer.save(path, MaterialSerializer.newDefaultLit());
        MetaFile.MetaData meta = MetaFile.read(assets.projectRoot(), assetsRoot, path);
        meta.type = AssetType.MATERIAL.name();
        MetaFile.write(assets.projectRoot(), assetsRoot, path, meta);
        assets.refresh();
        assets.bumpMaterialRevision(meta.guid);
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
            candidate = folder.resolve(stem + " (" + i + ")" + ext);
            i++;
        }
        return candidate;
    }
}
