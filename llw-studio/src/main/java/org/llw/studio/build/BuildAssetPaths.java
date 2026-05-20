package org.llw.studio.build;

import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.project.StudioProjectLayout;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Resolves asset GUIDs and {@link StudioAsset} views for build packaging without touching the GPU.
 */
final class BuildAssetPaths {
    private BuildAssetPaths() {
    }

    static String guidForPath(Path projectRoot, Path assetPath) throws IOException {
        Path assetsRoot = StudioProjectLayout.assetsRoot(projectRoot);
        MetaFile.MetaData meta = MetaFile.read(projectRoot, assetsRoot, assetPath);
        return meta.guid;
    }

    static StudioAsset sceneAsset(Path scenePath, String guid) {
        String name = scenePath.getFileName() == null ? guid : scenePath.getFileName().toString();
        return new StudioAsset(guid, scenePath, AssetType.SCENE, name, Instant.EPOCH);
    }
}
