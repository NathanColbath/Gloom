package org.llw.studio.build;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Resolves a project icon asset GUID to a Windows {@code .ico} file for packaging.
 */
public final class BuildIconResolver {
    private BuildIconResolver() {
    }

    /**
     * @param projectRoot project root
     * @param assets      asset database
     * @param iconGuid    texture, sprite, or image asset GUID
     * @param stagingDir  build staging directory
     * @return staged {@code .ico} path, or {@code null} when no icon is configured
     * @throws IOException when the asset is missing or cannot be converted
     */
    public static Path resolve(Path projectRoot, AssetDatabase assets, String iconGuid, Path stagingDir)
            throws IOException {
        if (iconGuid == null || iconGuid.isBlank()) {
            return null;
        }
        StudioAsset asset = assets.get(iconGuid);
        if (asset == null) {
            throw new IOException("Icon asset not found: " + iconGuid);
        }
        Path source = resolveSourcePath(assets, asset);
        if (!Files.isRegularFile(source)) {
            throw new IOException("Icon source file not found: " + source);
        }
        Path output = stagingDir.resolve("icons").resolve(safeFileName(asset) + ".ico");
        if (isIco(source)) {
            Files.createDirectories(output.getParent());
            Files.copy(source, output);
            return output;
        }
        if (!isRasterImage(source)) {
            throw new IOException("Icon asset must be a texture or image file (.png, .jpg, .ico): "
                    + asset.displayName());
        }
        BuildIconWriter.writeIco(source, output);
        return output;
    }

    private static Path resolveSourcePath(AssetDatabase assets, StudioAsset asset) {
        // Icon picker may reference a sprite slice; Windows .ico needs the parent raster texture file.
        if (asset.type() == AssetType.SPRITE && asset.parentTextureGuid() != null) {
            StudioAsset texture = assets.get(asset.parentTextureGuid());
            if (texture != null) {
                return texture.path();
            }
        }
        return asset.path();
    }

    private static boolean isIco(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".ico");
    }

    private static boolean isRasterImage(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".png")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".bmp")
                || name.endsWith(".gif")
                || name.endsWith(".webp");
    }

    private static String safeFileName(StudioAsset asset) {
        String name = asset.guid();
        if (name == null || name.isBlank()) {
            return "icon";
        }
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
