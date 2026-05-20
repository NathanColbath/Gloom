package org.llw.studio.editor.inspector;

import imgui.ImGui;
import imgui.type.ImInt;
import org.llw.render.graphics.TextureFilter;
import org.llw.render.graphics.TextureWrap;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetPreviewCache;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.assets.SpriteArtFacing;
import org.llw.studio.assets.TextureImportSettings;
import org.llw.studio.assets.TextureSpriteData;
import org.llw.studio.editor.widgets.PropertyRow;

import java.io.IOException;

/**
 * Inspector controls for texture filter and wrap import settings.
 */
public final class TextureImportInspectorEditor {
    private static final String[] FILTER_LABELS = {"Point (pixel art)", "Linear (smooth)"};
    private static final String[] WRAP_LABELS = {"Clamp", "Repeat"};
    private static final String[] ART_FACING_LABELS = {
            "Right (game forward)",
            "Up (rotate +90° to face right)"
    };

    private String loadedTextureGuid = "";
    private final TextureImportSettings settings = new TextureImportSettings();
    private final ImInt filterIndex = new ImInt(0);
    private final ImInt wrapIndex = new ImInt(0);
    private final ImInt artFacingIndex = new ImInt(0);

    /**
     * @param asset    selected texture asset
     * @param assets   asset database
     * @param previews preview cache (invalidated after apply)
     */
    public void render(StudioAsset asset, AssetDatabase assets, AssetPreviewCache previews) {
        if (asset == null) {
            return;
        }
        ensureLoaded(asset, assets);

        ImGui.separator();
        ImGui.text("Rendering");
        int previousFilter = filterIndex.get();
        int previousWrap = wrapIndex.get();
        ImGui.combo("Filter", filterIndex, FILTER_LABELS);
        ImGui.combo("Wrap", wrapIndex, WRAP_LABELS);
        settings.filter = filterIndex.get() == 0 ? TextureFilter.POINT : TextureFilter.LINEAR;
        settings.wrap = wrapIndex.get() == 0 ? TextureWrap.CLAMP : TextureWrap.REPEAT;

        if (filterIndex.get() != previousFilter || wrapIndex.get() != previousWrap) {
            persistImportSettings(asset, assets, previews);
        }

        ImGui.separator();
        ImGui.text("Sprite");
        int previousFacing = artFacingIndex.get();
        ImGui.combo("Art facing", artFacingIndex, ART_FACING_LABELS);
        settings.artFacing = artFacingIndex.get() == 0 ? SpriteArtFacing.RIGHT : SpriteArtFacing.UP;
        if (artFacingIndex.get() != previousFacing) {
            persistImportSettings(asset, assets, previews);
        }
        ImGui.textDisabled("Transform rotation 0 faces right (+X). "
                + "Choose Up when the texture art points upward.");

        PropertyRow.readOnlyValue("Min / Mag", settings.filter == TextureFilter.POINT ? "Nearest" : "Linear");
        PropertyRow.readOnlyValue("Wrap S / T", settings.wrap == TextureWrap.REPEAT ? "Repeat" : "Clamp to edge");
    }

    /**
     * @param parentTextureGuid atlas texture GUID for a sprite sub-asset
     * @param assets            asset database
     */
    public void renderInherited(String parentTextureGuid, AssetDatabase assets) {
        StudioAsset parent = assets.get(parentTextureGuid);
        if (parent == null) {
            return;
        }
        TextureImportSettings inherited = assets.readTextureImportSettings(parent.path());
        ImGui.separator();
        ImGui.text("Rendering (from atlas)");
        PropertyRow.readOnlyValue("Filter", inherited.filter == TextureFilter.POINT ? "Point" : "Linear");
        PropertyRow.readOnlyValue("Wrap", inherited.wrap == TextureWrap.REPEAT ? "Repeat" : "Clamp");
        SpriteArtFacing facing = inherited.artFacing == null ? SpriteArtFacing.RIGHT : inherited.artFacing;
        PropertyRow.readOnlyValue("Art facing", facing == SpriteArtFacing.UP ? "Up" : "Right");
    }

    private void ensureLoaded(StudioAsset asset, AssetDatabase assets) {
        if (asset.guid().equals(loadedTextureGuid)) {
            return;
        }
        loadedTextureGuid = asset.guid();
        TextureImportSettings loaded = assets.readTextureImportSettings(asset.path());
        settings.filter = loaded.filter;
        settings.wrap = loaded.wrap;
        filterIndex.set(loaded.filter == TextureFilter.POINT ? 0 : 1);
        wrapIndex.set(loaded.wrap == TextureWrap.REPEAT ? 1 : 0);
        SpriteArtFacing facing = loaded.artFacing == null ? SpriteArtFacing.RIGHT : loaded.artFacing;
        artFacingIndex.set(facing == SpriteArtFacing.UP ? 1 : 0);
        settings.artFacing = facing;
        try {
            MetaFile.MetaData meta = MetaFile.read(assets.projectRoot(), assets.assetsRoot(), asset.path());
            TextureSpriteData.ensureImportDefaults(meta.importer);
            MetaFile.write(assets.projectRoot(), assets.assetsRoot(), asset.path(), meta);
        } catch (IOException ignored) {
        }
    }

    private void persistImportSettings(StudioAsset asset, AssetDatabase assets, AssetPreviewCache previews) {
        try {
            assets.saveTextureImportSettings(asset.path(), settings.copy());
            previews.invalidate(asset.guid());
        } catch (IOException ignored) {
        }
    }
}
