package org.llw.studio.editor.inspector;

import imgui.ImGui;
import imgui.type.ImInt;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetPreviewCache;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.assets.SpriteDefinition;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.assets.TileCollisionType;
import org.llw.studio.assets.TileDefinition;
import org.llw.studio.assets.TilesetData;
import org.llw.studio.assets.TilesetDefinition;
import org.llw.studio.editor.widgets.PropertyRow;
import org.llw.studio.editor.widgets.SpriteSlicePreview;
import org.llw.render.graphics.Texture2d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Inspector UI for texture tilesets (spritesheet slice + per-tile metadata).
 */
public final class TilesetInspectorEditor {
    private String loadedTextureGuid = "";
    private TilesetDefinition editableTileset;
    private final List<TileDefinition> editableTiles = new ArrayList<>();
    private final ImInt editTileIndex = new ImInt(0);
    private final SpritesheetInspectorEditor spritesheetEditor = new SpritesheetInspectorEditor();
    private final RuleTileEditorModal ruleTileModal = new RuleTileEditorModal();

    public void render(StudioAsset asset, AssetDatabase assets, AssetPreviewCache previews) {
        if (asset == null || asset.type() != org.llw.studio.assets.AssetType.TEXTURE) {
            return;
        }
        spritesheetEditor.render(asset, assets, previews);

        ImGui.separator();
        ImGui.text("Tileset");
        ensureLoaded(asset, assets);

        if (editableTileset != null) {
            boolean changed = false;
            ImInt cellW = new ImInt(editableTileset.cellWidth);
            ImInt cellH = new ImInt(editableTileset.cellHeight);
            if (ImGui.inputInt("Cell Width", cellW)) {
                editableTileset.cellWidth = Math.max(1, cellW.get());
                changed = true;
            }
            if (ImGui.inputInt("Cell Height", cellH)) {
                editableTileset.cellHeight = Math.max(1, cellH.get());
                changed = true;
            }
            if (!editableTiles.isEmpty()) {
                String[] names = editableTiles.stream()
                        .map(t -> tileLabel(t, assets))
                        .toArray(String[]::new);
                if (editTileIndex.get() >= names.length) {
                    editTileIndex.set(0);
                }
                ImGui.combo("Tile", editTileIndex, names);
                int idx = Math.min(editTileIndex.get(), editableTiles.size() - 1);
                TileDefinition tile = editableTiles.get(idx);
                drawTileEditor(tile, asset, assets, previews);
                TileDefinition applied = ruleTileModal.render();
                if (applied != null) {
                    editableTiles.set(idx, applied);
                    changed = true;
                }
            }
            if (changed && ImGui.button("Save Tileset")) {
                saveTileset(asset, assets);
            }
        }
    }

    private static String tileLabel(TileDefinition tile, AssetDatabase assets) {
        SpriteDefinition sprite = assets.sprite(tile.spriteGuid);
        return sprite != null ? sprite.name() : tile.spriteGuid;
    }

    private void drawTileEditor(
            TileDefinition tile,
            StudioAsset asset,
            AssetDatabase assets,
            AssetPreviewCache previews
    ) {
        SpriteDefinition sprite = assets.sprite(tile.spriteGuid);
        Texture2d texture = assets.texture(asset.guid());
        if (sprite != null && texture != null) {
            SpriteSlicePreview.draw(texture, sprite, 96f);
        }
        boolean solid = tile.collision == TileCollisionType.SOLID;
        if (ImGui.checkbox("Collision (solid)", solid)) {
            tile.collision = solid ? TileCollisionType.SOLID : TileCollisionType.NONE;
        }
        boolean hasRule = tile.ruleTile != null && tile.ruleTile.isActive();
        ImGui.textDisabled(hasRule ? "Rule tile configured" : "Plain tile");
        if (ImGui.button("Edit Rule Tile...")) {
            ruleTileModal.open(tile, editableTileset, assets);
        }
    }

    private void ensureLoaded(StudioAsset asset, AssetDatabase assets) {
        if (asset.guid().equals(loadedTextureGuid)) {
            return;
        }
        loadedTextureGuid = asset.guid();
        editableTiles.clear();
        TilesetDefinition tileset = assets.tileset(asset.guid());
        if (tileset != null) {
            editableTileset = copyTileset(tileset);
            for (TileDefinition tile : tileset.tiles) {
                editableTiles.add(tile.copy());
            }
        } else {
            editableTileset = new TilesetDefinition(asset.guid());
            List<SpriteDefinition> sprites = new ArrayList<>();
            for (var child : assets.spriteChildren(asset.guid())) {
                SpriteDefinition s = assets.sprite(child.guid());
                if (s != null) {
                    sprites.add(s);
                }
            }
            editableTileset = TilesetDefinition.fromSprites(asset.guid(), sprites, 32, 32);
            editableTiles.addAll(editableTileset.tiles);
        }
        editTileIndex.set(0);
    }

    private static TilesetDefinition copyTileset(TilesetDefinition source) {
        TilesetDefinition copy = new TilesetDefinition(source.textureGuid);
        copy.cellWidth = source.cellWidth;
        copy.cellHeight = source.cellHeight;
        return copy;
    }

    private void saveTileset(StudioAsset asset, AssetDatabase assets) {
        try {
            MetaFile.MetaData meta = MetaFile.read(assets.projectRoot(), assets.assetsRoot(), asset.path());
            TilesetDefinition def = new TilesetDefinition(asset.guid());
            def.cellWidth = editableTileset.cellWidth;
            def.cellHeight = editableTileset.cellHeight;
            for (TileDefinition tile : editableTiles) {
                def.tiles.add(tile.copy());
            }
            TilesetData.write(meta.importer, def);
            assets.saveTileset(asset.path(), meta);
            loadedTextureGuid = "";
            ensureLoaded(asset, assets);
        } catch (IOException ignored) {
        }
    }

    /** Called after spritesheet slice so tile list stays in sync. */
    public void reload(StudioAsset asset, AssetDatabase assets) {
        loadedTextureGuid = "";
        ensureLoaded(asset, assets);
    }
}
