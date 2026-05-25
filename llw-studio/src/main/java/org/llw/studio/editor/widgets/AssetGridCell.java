package org.llw.studio.editor.widgets;

import imgui.ImGui;
import org.llw.render.graphics.Texture2d;
import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetPreviewCache;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.assets.AssetIconKind;
import org.llw.studio.editor.assets.EditorIconRegistry;
import org.llw.studio.editor.theme.EditorColors;
import org.llw.studio.editor.theme.EditorStyle;

/**
 * Draws a single project browser grid cell (chevron, thumbnail/icon, label).
 */
public final class AssetGridCell {
    public static final float CELL_SIZE = 80f;
    private static final float ICON_SIZE = 48f;
    private static final float CHEVRON_SIZE = 12f;
    private static final float LABEL_HEIGHT = 14f;
    private static final float CELL_PADDING = 4f;

    private AssetGridCell() {
    }

    /**
     * @return {@code true} when the main cell body was clicked (not the expand chevron)
     */
    public static boolean render(
            StudioAsset asset,
            String displayLabel,
            AssetIconKind iconKind,
            boolean selected,
            boolean expandable,
            boolean expanded,
            Runnable onToggleExpand,
            AssetDatabase assets,
            AssetPreviewCache previews,
            EditorIconRegistry icons,
            AssetBrowserItemInteractions.Context interactions
    ) {
        float inner = CELL_SIZE - 8f;
        float startX = ImGui.getCursorScreenPosX();
        float startY = ImGui.getCursorScreenPosY();
        float bodyHeight = inner - LABEL_HEIGHT;

        // Thumbnail/icon and label drawn on draw list first; invisible button provides hit target on top.
        float iconX = startX + (inner - ICON_SIZE) * 0.5f;
        float iconY = startY + Math.max(0f, (bodyHeight - ICON_SIZE) * 0.5f);
        drawThumbnailOrIconDrawList(asset, iconKind, iconX, iconY, ICON_SIZE, assets, previews, icons);

        String label = displayLabel == null ? asset.friendlyDisplayName() : displayLabel;
        float labelY = startY + inner - LABEL_HEIGHT;
        drawLabelDrawList(startX, labelY, inner, label);

        ImGui.setCursorScreenPos(startX, startY);
        boolean clicked = ImGui.invisibleButton("cell##" + asset.guid(), inner, inner);
        boolean hovered = ImGui.isItemHovered();
        if (selected) {
            drawSelectionBox(startX, startY, inner);
        } else if (hovered) {
            drawHoverBox(startX, startY, inner);
        }
        AssetBrowserItemInteractions.attach(asset, interactions, false);

        if (asset.isFolder() && interactions != null) {
            // Register screen rect for deferred drop target (see AssetBrowserFolderDropState).
            AssetBrowserFolderDropState.register(asset.path(), startX, startY, startX + inner, startY + inner);
        }

        if (expandable) {
            ImGui.pushID("chev##" + asset.guid());
            renderChevronOverlay(icons, expanded, startX, startY, inner);
            // Chevron click toggles nest expand without selecting the asset.
            if (clicked && isChevronClick(startX, startY, inner)) {
                onToggleExpand.run();
                ImGui.popID();
                return false;
            }
            ImGui.popID();
        }

        return clicked;
    }

    private static void drawLabelDrawList(float x, float y, float width, String label) {
        var drawList = ImGui.getWindowDrawList();
        int color = colorU32(EditorColors.TEXT_PRIMARY);
        float maxWidth = width - 2f;
        String clipped = clipText(label, maxWidth);
        drawList.addText(x + 1f, y, color, clipped);
    }

    private static String clipText(String text, float maxWidth) {
        // Delegate to EditorStyle.middleTruncate for pixel‑aware middle‑ellipsis
        return EditorStyle.middleTruncate(text, maxWidth);
    }

    private static void drawSelectionBox(float startX, float startY, float size) {
        var drawList = ImGui.getWindowDrawList();
        int fill = colorU32(EditorColors.SELECTION_BG);
        int border = colorU32(EditorColors.ACCENT);
        drawList.addRectFilled(startX, startY, startX + size, startY + size, fill, 4f);
        drawList.addRect(startX, startY, startX + size, startY + size, border, 4f, 0, 2f);
    }

    private static void drawHoverBox(float startX, float startY, float size) {
        var drawList = ImGui.getWindowDrawList();
        int fill = colorU32(EditorColors.ASSET_BROWSER_HOVER);
        int border = colorU32(EditorColors.BORDER_STRONG);
        drawList.addRectFilled(startX, startY, startX + size, startY + size, fill, 4f);
        drawList.addRect(startX, startY, startX + size, startY + size, border, 4f, 0, 1f);
    }

    private static int colorU32(float[] rgba) {
        return ImGui.colorConvertFloat4ToU32(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    private static int colorU32(float r, float g, float b, float a) {
        return ImGui.colorConvertFloat4ToU32(r, g, b, a);
    }

    private static boolean isChevronClick(float cellX, float cellY, float inner) {
        float pad = 2f;
        float chevronX = cellX + inner - CHEVRON_SIZE - CELL_PADDING - pad;
        float chevronY = cellY + CELL_PADDING - pad;
        float chevronR = cellX + inner - CELL_PADDING + pad;
        float chevronB = cellY + CELL_PADDING + CHEVRON_SIZE + pad;
        return ImGui.isMouseHoveringRect(chevronX, chevronY, chevronR, chevronB);
    }

    /** Top-right expand affordance (drawn on the draw list; hit-tested via {@link #isChevronClick}). */
    private static void renderChevronOverlay(
            EditorIconRegistry icons,
            boolean expanded,
            float cellX,
            float cellY,
            float inner
    ) {
        float chevronX = cellX + inner - CHEVRON_SIZE - CELL_PADDING;
        float chevronY = cellY + CELL_PADDING;
        float pad = 2f;
        float backdropL = chevronX - pad;
        float backdropT = chevronY - pad;
        float backdropR = chevronX + CHEVRON_SIZE + pad;
        float backdropB = chevronY + CHEVRON_SIZE + pad;

        boolean chevronHovered = isChevronClick(cellX, cellY, inner);
        var drawList = ImGui.getWindowDrawList();
        int backdrop = chevronHovered
                ? colorU32(0.35f, 0.35f, 0.35f, 0.9f)
                : colorU32(0.12f, 0.12f, 0.12f, 0.75f);
        drawList.addRectFilled(backdropL, backdropT, backdropR, backdropB, backdrop, 3f);

        AssetIconKind kind = expanded ? AssetIconKind.CHEVRON_DOWN : AssetIconKind.CHEVRON_RIGHT;
        Texture2d chevronTex = icons.icon(kind);
        if (chevronTex != null) {
            drawList.addImage(chevronTex.id(), chevronX, chevronY, chevronX + CHEVRON_SIZE, chevronY + CHEVRON_SIZE, 0f, 1f, 1f, 0f);
        } else {
            drawList.addText(chevronX, chevronY, colorU32(EditorColors.TEXT_PRIMARY), expanded ? "v" : ">");
        }
    }

    private static void drawThumbnailOrIconDrawList(
            StudioAsset asset,
            AssetIconKind iconKind,
            float x,
            float y,
            float size,
            AssetDatabase assets,
            AssetPreviewCache previews,
            EditorIconRegistry icons
    ) {
        var drawList = ImGui.getWindowDrawList();
        if (asset.type() == AssetType.TEXTURE) {
            Texture2d texture = previews.preview(asset.guid());
            if (texture != null) {
                drawList.addImage(texture.id(), x, y, x + size, y + size, 0f, 1f, 1f, 0f);
                return;
            }
        } else if (asset.type() == AssetType.SPRITE) {
            if (SpriteSlicePreview.drawThumbDrawList(drawList, x, y, assets, previews, asset.guid(), size)) {
                return;
            }
        }
        AssetIconKind kind = iconKind != null ? iconKind : EditorIconRegistry.kindFor(asset);
        Texture2d icon = icons.icon(kind);
        if (icon != null) {
            drawList.addImage(icon.id(), x, y, x + size, y + size, 0f, 1f, 1f, 0f);
        }
    }
}
