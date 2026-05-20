package org.llw.studio.assets;

import org.llw.render.graphics.TextureFilter;
import org.llw.render.graphics.TextureWrap;

/**
 * Texture import sampling options stored in asset meta ({@code importer.texture}).
 */
public final class TextureImportSettings {
    public TextureFilter filter = TextureFilter.LINEAR;
    public TextureWrap wrap = TextureWrap.CLAMP;
    /** How the unrotated source art is oriented in the texture file. */
    public SpriteArtFacing artFacing = SpriteArtFacing.RIGHT;

    public TextureImportSettings copy() {
        TextureImportSettings copy = new TextureImportSettings();
        copy.filter = filter;
        copy.wrap = wrap;
        copy.artFacing = artFacing;
        return copy;
    }
}
