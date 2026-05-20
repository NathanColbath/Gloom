package org.llw.studio.editor.inspector;

import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetPreviewCache;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.assets.SpriteDefinition;
import org.llw.studio.assets.SpriteSliceSettings;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.assets.TextureImageSize;
import org.llw.studio.assets.TextureSpriteData;
import org.llw.studio.assets.TextureSpriteImporter;
import org.llw.studio.editor.widgets.PropertyRow;
import org.llw.studio.editor.widgets.SpriteSlicePreview;
import org.llw.render.graphics.Texture2d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Inspector UI for texture spritesheet slicing.
 */
public final class SpritesheetInspectorEditor {
    private String loadedTextureGuid = "";
    private final ImBoolean singleMode = new ImBoolean(true);
    private final SpriteSliceSettings sliceSettings = new SpriteSliceSettings();
    private final List<SpriteDefinition> editableSprites = new ArrayList<>();
    private final ImInt editIndex = new ImInt(0);
    private final SpritesheetSliceModal sliceModal = new SpritesheetSliceModal();

    /**
     * @param asset    selected texture asset
     * @param assets   asset database
     * @param previews preview cache to invalidate after apply
     */
    public void render(StudioAsset asset, AssetDatabase assets, AssetPreviewCache previews) {
        if (asset == null || asset.type() != org.llw.studio.assets.AssetType.TEXTURE) {
            return;
        }
        ensureLoaded(asset, assets);

        ImGui.separator();
        ImGui.text("Sprite Editor");
        String modeLabel = singleMode.get() ? "Single sprite" : "Multiple (" + editableSprites.size() + " slices)";
        ImGui.textDisabled(modeLabel);
        if (ImGui.button("Slice Editor...")) {
            sliceModal.open(asset, singleMode.get(), sliceSettings);
        }

        sliceModal.render(assets, previews, this);

        if (!editableSprites.isEmpty()) {
            ImGui.separator();
            ImGui.text("Slices (" + editableSprites.size() + ")");
            String[] names = editableSprites.stream().map(SpriteDefinition::name).toArray(String[]::new);
            ImGui.combo("Slice", editIndex, names);
            int idx = Math.min(editIndex.get(), editableSprites.size() - 1);
            SpriteDefinition sprite = editableSprites.get(idx);
            PropertyRow.begin("Name");
            ImGui.text(sprite.name());
            PropertyRow.end();
            PropertyRow.readOnlyValue("Rect", sprite.x() + ", " + sprite.y() + "  " + sprite.width() + "x" + sprite.height());
            Texture2d texture = assets.texture(asset.guid());
            SpriteSlicePreview.draw(texture, sprite, 96f);
        }
    }

    void applySingleFromModal(StudioAsset asset, AssetDatabase assets, AssetPreviewCache previews) {
        singleMode.set(true);
        applySingle(asset, assets, previews);
    }

    void applyGridSliceFromModal(
            StudioAsset asset,
            AssetDatabase assets,
            AssetPreviewCache previews,
            SpriteSliceSettings settings
    ) {
        singleMode.set(false);
        sliceSettings.cellWidth = settings.cellWidth;
        sliceSettings.cellHeight = settings.cellHeight;
        sliceSettings.offsetX = settings.offsetX;
        sliceSettings.offsetY = settings.offsetY;
        sliceSettings.paddingX = settings.paddingX;
        sliceSettings.paddingY = settings.paddingY;
        sliceSettings.columnCount = settings.columnCount;
        sliceSettings.rowCount = settings.rowCount;
        sliceSettings.indexFromBottom = settings.indexFromBottom;
        applyGridSlice(asset, assets, previews);
    }

    private void ensureLoaded(StudioAsset asset, AssetDatabase assets) {
        if (asset.guid().equals(loadedTextureGuid)) {
            return;
        }
        loadedTextureGuid = asset.guid();
        editableSprites.clear();
        try {
            MetaFile.MetaData meta = MetaFile.read(assets.projectRoot(), assets.assetsRoot(), asset.path());
            singleMode.set(!"multiple".equals(TextureSpriteData.spriteMode(meta.importer)));
            SpriteSliceSettings loaded = TextureSpriteData.readSliceSettings(meta.importer);
            sliceSettings.cellWidth = loaded.cellWidth;
            sliceSettings.cellHeight = loaded.cellHeight;
            sliceSettings.offsetX = loaded.offsetX;
            sliceSettings.offsetY = loaded.offsetY;
            sliceSettings.paddingX = loaded.paddingX;
            sliceSettings.paddingY = loaded.paddingY;
            sliceSettings.columnCount = loaded.columnCount;
            sliceSettings.rowCount = loaded.rowCount;
            sliceSettings.indexFromBottom = loaded.indexFromBottom;
            var size = TextureImageSize.read(asset.path());
            editableSprites.addAll(TextureSpriteData.parseSprites(
                    meta.importer, asset.guid(), size.width(), size.height()));
        } catch (IOException ignored) {
            singleMode.set(true);
        }
    }

    private void applySingle(StudioAsset asset, AssetDatabase assets, AssetPreviewCache previews) {
        try {
            MetaFile.MetaData meta = MetaFile.read(assets.projectRoot(), assets.assetsRoot(), asset.path());
            var size = TextureImageSize.read(asset.path());
            Map<String, String> previous = TextureSpriteData.guidKeysByRect(meta.importer);
            SpriteDefinition full = TextureSpriteImporter.fullImageSprite(
                    asset.guid(), asset.displayName(), size.width(), size.height());
            String key = full.rectKey();
            full = new SpriteDefinition(
                    previous.getOrDefault(key, full.guid()),
                    full.name(),
                    full.textureGuid(),
                    full.x(),
                    full.y(),
                    full.width(),
                    full.height(),
                    full.pivotX(),
                    full.pivotY(),
                    full.atlasWidth(),
                    full.atlasHeight()
            );
            TextureSpriteData.setSpriteMode(meta.importer, "single");
            TextureSpriteData.replaceSprites(meta.importer, List.of(full));
            assets.saveTextureSprites(asset.path(), meta);
            previews.invalidate(asset.guid());
            reloadAfterApply(asset, assets);
        } catch (IOException ignored) {
        }
    }

    private void applyGridSlice(StudioAsset asset, AssetDatabase assets, AssetPreviewCache previews) {
        try {
            MetaFile.MetaData meta = MetaFile.read(assets.projectRoot(), assets.assetsRoot(), asset.path());
            TextureSpriteImporter.applyGridSlice(asset.path(), meta, sliceSettings);
            assets.saveTextureSprites(asset.path(), meta);
            previews.invalidate(asset.guid());
            reloadAfterApply(asset, assets);
        } catch (IOException ignored) {
        }
    }

    private void reloadAfterApply(StudioAsset asset, AssetDatabase assets) {
        loadedTextureGuid = "";
        editableSprites.clear();
        editIndex.set(0);
        ensureLoaded(asset, assets);
    }
}
