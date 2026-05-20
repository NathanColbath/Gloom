package org.llw.studio.ui;

import org.llw.render.graphics.Font;
import org.llw.resources.AssetRef;
import org.llw.resources.ResourceManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lazily acquires system fonts for UI text at requested pixel heights.
 */
public final class UiFontCache {
    private static final String[] FONT_FAMILIES = {"Segoe UI", "Arial", "Consolas"};

    private final ResourceManager resources;
    private final Map<Integer, AssetRef<Font>> fonts = new HashMap<>();

    public UiFontCache(ResourceManager resources) {
        this.resources = resources;
    }

    /**
     * @param pixelHeight rasterized cap height in pixels
     * @return cached font, or {@code null} when no system font is available
     */
    public Font font(int pixelHeight) {
        int size = Math.max(8, pixelHeight);
        AssetRef<Font> cached = fonts.get(size);
        if (cached != null) {
            return cached.get();
        }
        AssetRef<Font> loaded = loadFontRef(size);
        if (loaded != null) {
            fonts.put(size, loaded);
            return loaded.get();
        }
        return null;
    }

    private AssetRef<Font> loadFontRef(int pixelHeight) {
        if (resources == null) {
            return null;
        }
        for (String family : FONT_FAMILIES) {
            try {
                if (resources.hasSystemFontFace(family, org.llw.render.graphics.FontStyle.PLAIN)) {
                    return resources.acquireSystemFont(family, pixelHeight);
                }
            } catch (Exception ignored) {
                // try next family
            }
        }
        List<String> faces = resources.systemFontFaces();
        if (faces.isEmpty()) {
            return null;
        }
        try {
            return resources.acquireSystemFont(faces.get(0), pixelHeight);
        } catch (Exception ex) {
            return null;
        }
    }

    /** Releases acquired font references held by this cache. */
    public void dispose() {
        for (AssetRef<Font> ref : fonts.values()) {
            ref.close();
        }
        fonts.clear();
    }
}
