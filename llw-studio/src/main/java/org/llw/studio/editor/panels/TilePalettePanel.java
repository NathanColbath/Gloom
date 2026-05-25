package org.llw.studio.editor.panels;

import imgui.ImGui;
import org.llw.render.graphics.Texture2d;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetPreviewCache;
import org.llw.studio.assets.SpriteDefinition;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.EditorSession;
import org.llw.studio.editor.SelectionService;
import org.llw.studio.editor.StudioContext;
import org.llw.studio.editor.theme.EditorStyle;
import org.llw.studio.editor.widgets.SpriteSlicePreview;
import org.llw.studio.ecs.components.TilemapComponent;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.Scene;

import java.util.List;

/**
 * Tile palette for the active tilemap's tileset.
 */
public final class TilePalettePanel implements EditorPanel {
    private static final float THUMB_SIZE = 48f;
    private static final int COLUMNS = 6;

    private final AssetDatabase assets;
    private final AssetPreviewCache previews;
    private final SelectionService selection;
    private final EditorSession session;
    private final PanelVisibility visibility;

    public TilePalettePanel(
            AssetDatabase assets,
            AssetPreviewCache previews,
            SelectionService selection,
            EditorSession session,
            PanelVisibility visibility
    ) {
        this.assets = assets;
        this.previews = previews;
        this.selection = selection;
        this.session = session;
        this.visibility = visibility;
    }

    @Override
    public String id() {
        return "tile_palette";
    }

    @Override
    public String title() {
        return "Tile Palette";
    }

    @Override
    public void render(StudioContext context) {
        if (!visibility.isOpen(id())) {
            return;
        }
        boolean draw = visibility.begin(id(), title());
        try {
            if (!draw) {
                return;
            }
            Scene scene = context.activeScene();
            GameObject selected = scene.find(selection.selected());
            if (selected == null) {
                EditorStyle.pushMutedText();
                ImGui.text("Select a GameObject with a Tilemap.");
                EditorStyle.popMutedText();
                return;
            }
            TilemapComponent tilemap = selected.getComponent(TilemapComponent.class);
            if (tilemap == null || tilemap.tilesetTextureGuid.isBlank()) {
                EditorStyle.pushMutedText();
                ImGui.text("Assign a tileset texture on the Tilemap component.");
                EditorStyle.popMutedText();
                return;
            }

            session.tilemapEdit().activeTilemapEntity = selected.entity();
            List<StudioAsset> sprites = assets.spriteChildren(tilemap.tilesetTextureGuid);
            if (sprites.isEmpty()) {
                EditorStyle.pushMutedText();
                ImGui.text("Slice the tileset texture to create tiles.");
                EditorStyle.popMutedText();
                return;
            }

            Texture2d texture = previews.preview(tilemap.tilesetTextureGuid);
            if (texture == null) {
                texture = assets.texture(tilemap.tilesetTextureGuid);
            }

            int column = 0;
            for (StudioAsset spriteAsset : sprites) {
                if (column > 0) {
                    ImGui.sameLine();
                }
                SpriteDefinition slice = assets.sprite(spriteAsset.guid());
                if (slice == null || texture == null) {
                    continue;
                }
                boolean active = spriteAsset.guid().equals(session.tilemapEdit().activeSpriteGuid);
                if (active) {
                    ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, 0.3f, 0.5f, 0.8f, 1f);
                }
                ImGui.pushID(spriteAsset.guid());
                if (ImGui.button("##tile", THUMB_SIZE, THUMB_SIZE)) {
                    session.tilemapEdit().activeSpriteGuid = spriteAsset.guid();
                }
                if (ImGui.isItemHovered()) {
                    ImGui.setTooltip(slice.name());
                    ImGui.beginTooltip();
                    SpriteSlicePreview.draw(texture, slice, 96f);
                    ImGui.endTooltip();
                }
                ImGui.popID();
                if (active) {
                    ImGui.popStyleColor();
                }
                // Manual wrap: ImGui has no grid layout; advance column when next tile would overflow.
                float cursorX = ImGui.getItemRectMaxX();
                float nextX = cursorX + THUMB_SIZE + ImGui.getStyle().getItemSpacingX();
                if (nextX + THUMB_SIZE > ImGui.getWindowPosX() + ImGui.getWindowContentRegionMaxX()) {
                    column = 0;
                } else {
                    column++;
                }
            }
        } finally {
            visibility.end();
        }
    }
}
