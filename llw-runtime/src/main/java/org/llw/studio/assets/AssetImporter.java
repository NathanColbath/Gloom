package org.llw.studio.assets;

import java.nio.file.Path;

/**
 * Pluggable importer that processes a newly discovered asset and updates its meta sidecar.
 */
public interface AssetImporter {
    /**
     * @return asset type handled by this importer
     */
    AssetType type();

    /**
     * Runs import logic for the asset at {@code assetPath} and may mutate {@code meta}.
     *
     * @param assetPath path to the asset file or folder
     * @param meta mutable meta data loaded from or written to the sidecar
     * @throws Exception if import fails
     */
    void importAsset(Path assetPath, MetaFile.MetaData meta) throws Exception;
}
