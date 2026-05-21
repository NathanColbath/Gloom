package org.llw.studio.editor.assets;

/**
 * Maps editor asset icons to open-source Lucide glyphs via Iconify (no bundled image files).
 */
public final class OpenSourceIconCatalog {
    private OpenSourceIconCatalog() {
    }

    /**
     * @param kind browser icon slot
     * @return Lucide icon on Iconify
     */
    public static OpenSourceIconSpec spec(AssetIconKind kind) {
        return switch (kind) {
            case FOLDER -> new OpenSourceIconSpec("lucide", "folder");
            case TEXTURE -> new OpenSourceIconSpec("lucide", "image");
            case SPRITE -> new OpenSourceIconSpec("lucide", "images");
            case SCRIPT -> new OpenSourceIconSpec("lucide", "file-code");
            case SCENE -> new OpenSourceIconSpec("lucide", "layout-grid");
            case PREFAB -> new OpenSourceIconSpec("lucide", "package");
            case AUDIO -> new OpenSourceIconSpec("lucide", "volume-2");
            case FONT -> new OpenSourceIconSpec("lucide", "type");
            case ANIMATION -> new OpenSourceIconSpec("lucide", "circle-play");
            case ANIMATION_STATE -> new OpenSourceIconSpec("lucide", "layers");
            case ANIMATION_CLIP -> new OpenSourceIconSpec("lucide", "play");
            case PARTICLE_SYSTEM -> new OpenSourceIconSpec("lucide", "sparkles");
            case UNKNOWN -> new OpenSourceIconSpec("lucide", "help-circle");
            case CHEVRON_RIGHT -> new OpenSourceIconSpec("lucide", "chevron-right");
            case CHEVRON_DOWN -> new OpenSourceIconSpec("lucide", "chevron-down");
        };
    }
}
