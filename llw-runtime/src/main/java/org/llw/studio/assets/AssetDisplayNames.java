package org.llw.studio.assets;

/**
 * Human-readable labels for assets shown in the studio UI.
 */
public final class AssetDisplayNames {
    private AssetDisplayNames() {
    }

    /**
     * @param asset asset to label, or {@code null}
     * @return friendly display name, or an empty string when {@code asset} is {@code null}
     */
    public static String friendly(StudioAsset asset) {
        if (asset == null) {
            return "";
        }
        return switch (asset.type()) {
            case PREFAB -> prefabLabel(asset.displayName());
            case ANIMATION -> animationLabel(asset.displayName());
            case ANIMATION_CLIP -> clipLabel(asset.displayName());
            default -> asset.displayName();
        };
    }

    /**
     * @param fileName on-disk animation set file name
     * @return label without {@code .animation.json}
     */
    public static String animationLabel(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "Animation";
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".animation.json")) {
            return fileName.substring(0, fileName.length() - ".animation.json".length());
        }
        return fileName;
    }

    /**
     * @param fileName on-disk clip file name
     * @return label without {@code .anim.json}
     */
    public static String clipLabel(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "Clip";
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".anim.json")) {
            return fileName.substring(0, fileName.length() - ".anim.json".length());
        }
        return fileName;
    }

    /**
     * Normalizes prefab file names for display (strips redundant {@code .json} suffix when present).
     *
     * @param fileName on-disk prefab file name
     * @return label suitable for the asset browser
     */
    public static String prefabLabel(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "GameObject.prefab";
        }
        if (fileName.endsWith(".prefab.json")) {
            return fileName.substring(0, fileName.length() - ".json".length());
        }
        if (fileName.endsWith(".prefab")) {
            return fileName;
        }
        return fileName + ".prefab";
    }
}
