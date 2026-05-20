package org.llw.studio.editor.inspector;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import org.llw.render.graphics.Texture2d;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetPreviewCache;
import org.llw.studio.assets.SpriteSliceSettings;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.assets.TextureImageSize;
import org.llw.studio.editor.widgets.SpritesheetGridPreview;
import org.llw.studio.editor.widgets.fields.FloatField;

/**
 * Modal spritesheet slice editor with live grid preview.
 */
public final class SpritesheetSliceModal {
    private static final String POPUP_ID = "Spritesheet Slice Editor";

    private boolean open;
    private StudioAsset asset;
    private final ImBoolean singleMode = new ImBoolean(true);
    private final SpriteSliceSettings settings = new SpriteSliceSettings();

    /**
     * Opens the modal for {@code textureAsset} using the given mode and slice settings.
     */
    public void open(StudioAsset textureAsset, boolean single, SpriteSliceSettings source) {
        asset = textureAsset;
        singleMode.set(single);
        if (source != null) {
            SpriteSliceSettings copy = source.copy();
            settings.cellWidth = copy.cellWidth;
            settings.cellHeight = copy.cellHeight;
            settings.offsetX = copy.offsetX;
            settings.offsetY = copy.offsetY;
            settings.paddingX = copy.paddingX;
            settings.paddingY = copy.paddingY;
        }
        open = true;
        ImGui.openPopup(POPUP_ID);
    }

    /**
     * @param assets   asset database
     * @param previews texture preview cache
     * @param editor   receives apply actions
     */
    public void render(AssetDatabase assets, AssetPreviewCache previews, SpritesheetInspectorEditor editor) {
        if (open) {
            ImGui.openPopup(POPUP_ID);
        }
        ImGui.setNextWindowSize(620f, 0f);
        if (!ImGui.beginPopupModal(POPUP_ID, ImGuiWindowFlags.AlwaysAutoResize)) {
            return;
        }
        open = false;

        if (asset == null) {
            ImGui.textDisabled("No texture selected");
            if (ImGui.button("Close", 120f, 0f)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
            return;
        }

        var pixelSize = TextureImageSize.read(asset.path());
        Texture2d texture = previews.preview(asset.guid());
        if (texture != null) {
            pixelSize = TextureImageSize.fromTexture(texture);
        }

        ImGui.text(asset.friendlyDisplayName());
        ImGui.separator();

        if (ImGui.radioButton("Single", singleMode.get())) {
            singleMode.set(true);
        }
        ImGui.sameLine();
        if (ImGui.radioButton("Multiple", !singleMode.get())) {
            singleMode.set(false);
        }

        boolean multiple = !singleMode.get();
        if (multiple) {
            settings.cellWidth = (int) FloatField.draw("Cell Width", settings.cellWidth);
            settings.cellHeight = (int) FloatField.draw("Cell Height", settings.cellHeight);
            settings.offsetX = (int) FloatField.draw("Offset X", settings.offsetX);
            settings.offsetY = (int) FloatField.draw("Offset Y", settings.offsetY);
            settings.paddingX = (int) FloatField.draw("Padding X", settings.paddingX);
            settings.paddingY = (int) FloatField.draw("Padding Y", settings.paddingY);
            settings.columnCount = (int) FloatField.draw("Columns (0 = auto)", settings.columnCount);
            settings.rowCount = (int) FloatField.draw("Rows (0 = auto)", settings.rowCount);
            if (ImGui.checkbox("Index from bottom row", settings.indexFromBottom)) {
                settings.indexFromBottom = !settings.indexFromBottom;
            }
            ImGui.separator();
            SpritesheetGridPreview.draw(
                    texture,
                    pixelSize.width(),
                    pixelSize.height(),
                    asset.displayName(),
                    settings
            );
        } else if (texture != null) {
            float max = 320f;
            float scale = Math.min(max / pixelSize.width(), max / pixelSize.height());
            ImGui.image(texture.id(), pixelSize.width() * scale, pixelSize.height() * scale, 0f, 1f, 1f, 0f);
            ImGui.text("Uses the full image as one sprite");
        }

        ImGui.separator();
        if (ImGui.button("Apply", 120f, 0f)) {
            if (multiple) {
                editor.applyGridSliceFromModal(asset, assets, previews, settings.copy());
            } else {
                editor.applySingleFromModal(asset, assets, previews);
            }
            ImGui.closeCurrentPopup();
        }
        ImGui.sameLine();
        if (ImGui.button("Cancel", 120f, 0f)) {
            ImGui.closeCurrentPopup();
        }
        ImGui.endPopup();
    }
}
