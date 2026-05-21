package org.llw.studio.particles.assets;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.particles.ParticleSystemSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Creates particle system assets on disk.
 */
public final class ParticleSystemActions {
    private ParticleSystemActions() {
    }

    /**
     * @param assets       asset database
     * @param parentFolder folder under Assets, or null for root
     * @param baseName     name without extension
     * @return new asset GUID
     */
    public static String createParticleSystem(AssetDatabase assets, Path parentFolder, String baseName) throws IOException {
        Path assetsRoot = assets.assetsRoot();
        Path folder = parentFolder == null ? assetsRoot : parentFolder;
        Files.createDirectories(folder);
        String safe = baseName == null || baseName.isBlank() ? "NewParticles" : baseName.trim();
        if (!safe.endsWith(".particle.json")) {
            safe = safe + ".particle.json";
        }
        Path path = uniquePath(folder, safe);
        ParticleSystemSerializer.save(path, ParticleSystemSerializer.newDefault());
        MetaFile.MetaData meta = MetaFile.read(assets.projectRoot(), assetsRoot, path);
        meta.type = AssetType.PARTICLE_SYSTEM.name();
        MetaFile.write(assets.projectRoot(), assetsRoot, path, meta);
        assets.refresh();
        assets.bumpParticleRevision(meta.guid);
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
        int index = 1;
        while (Files.exists(candidate)) {
            candidate = folder.resolve(stem + " (" + index + ")" + ext);
            index++;
        }
        return candidate;
    }
}
