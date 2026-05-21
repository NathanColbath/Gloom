package org.llw.studio.editor.widgets;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import org.llw.render.graphics.Texture2d;
import org.llw.studio.assets.SpriteSliceSettings;
import org.llw.studio.assets.SpriteSlicer;
import org.llw.studio.editor.theme.EditorColors;

import java.util.List;

/**
 * Draws a texture preview with a live grid-slice overlay for the spritesheet editor.
 */
public final class SpritesheetGridPreview {
    private static final float MAX_DISPLAY_SIZE = 560f;

    private SpritesheetGridPreview() {
    }

    /**
     * @param texture      GPU texture preview (may be null)
     * @param imageWidth   atlas width in pixels
     * @param imageHeight  atlas height in pixels
     * @param baseName     file name used for generated slice names
     * @param settings     grid slice parameters
     * @return slice count and grid dimensions for UI labels
     */
    public static GridPreviewResult draw(
            Texture2d texture,
            int imageWidth,
            int imageHeight,
            String baseName,
            SpriteSliceSettings settings
    ) {
        if (texture == null || imageWidth <= 0 || imageHeight <= 0) {
            ImGui.textDisabled("Texture preview unavailable");
            return new GridPreviewResult(0, 0, 0);
        }
        float scale = Math.min(MAX_DISPLAY_SIZE / imageWidth, MAX_DISPLAY_SIZE / imageHeight);
        float displayW = imageWidth * scale;
        float displayH = imageHeight * scale;

        ImGui.image(texture.id(), displayW, displayH, 0f, 1f, 1f, 0f);
        ImVec2 min = ImGui.getItemRectMin();
        ImVec2 max = ImGui.getItemRectMax();

        List<SpriteSlicer.SpriteSliceDraft> cells = SpriteSlicer.sliceGrid(
                imageWidth, imageHeight, baseName, settings);
        int cols = countColumns(cells, settings);
        int rows = countRows(cells, settings);

        ImDrawList drawList = ImGui.getWindowDrawList();
        int borderColor = colorU32(EditorColors.BORDER_STRONG);
        int lineColor = colorU32(EditorColors.ACCENT);
        int fillColor = colorU32(0.26f, 0.59f, 0.98f, 0.12f);
        int hoverColor = colorU32(0.26f, 0.59f, 0.98f, 0.35f);

        drawList.addRect(min.x, min.y, max.x, max.y, borderColor, 0f, 0, 2f);

        int hovered = -1;
        ImVec2 mouse = ImGui.getMousePos();
        for (int i = 0; i < cells.size(); i++) {
            SpriteSlicer.SpriteSliceDraft cell = cells.get(i);
            float x0 = min.x + cell.x() * scale;
            float y0 = min.y + cell.y() * scale;
            float x1 = x0 + cell.width() * scale;
            float y1 = y0 + cell.height() * scale;
            boolean hover = mouse.x >= x0 && mouse.x <= x1 && mouse.y >= y0 && mouse.y <= y1
                    && ImGui.isWindowHovered();
            if (hover) {
                hovered = i;
                drawList.addRectFilled(x0, y0, x1, y1, hoverColor, 0f);
            } else {
                drawList.addRectFilled(x0, y0, x1, y1, fillColor, 0f);
            }
            drawList.addRect(x0, y0, x1, y1, lineColor, 0f, 0, 1f);
        }

        if (hovered >= 0) {
            SpriteSlicer.SpriteSliceDraft cell = cells.get(hovered);
            ImGui.setTooltip(cell.name() + "  " + cell.x() + "," + cell.y()
                    + "  " + cell.width() + "×" + cell.height());
        }

        if (cells.isEmpty()) {
            ImGui.textDisabled("No cells fit with current settings");
        } else {
            ImGui.text(cells.size() + " cells" + (cols > 0 && rows > 0 ? " (" + cols + "×" + rows + ")" : ""));
        }
        return new GridPreviewResult(cells.size(), cols, rows);
    }

    private static int countColumns(List<SpriteSlicer.SpriteSliceDraft> cells, SpriteSliceSettings settings) {
        if (cells.isEmpty()) {
            return 0;
        }
        int cellW = Math.max(1, settings.cellWidth);
        int stepX = cellW + settings.paddingX;
        if (stepX <= 0) {
            return cells.size();
        }
        long distinctX = cells.stream().mapToInt(SpriteSlicer.SpriteSliceDraft::x).distinct().count();
        return (int) distinctX;
    }

    private static int countRows(List<SpriteSlicer.SpriteSliceDraft> cells, SpriteSliceSettings settings) {
        if (cells.isEmpty()) {
            return 0;
        }
        long distinctY = cells.stream().mapToInt(SpriteSlicer.SpriteSliceDraft::y).distinct().count();
        return (int) distinctY;
    }

    private static int colorU32(float[] rgba) {
        return ImGui.colorConvertFloat4ToU32(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    private static int colorU32(float r, float g, float b, float a) {
        return ImGui.colorConvertFloat4ToU32(r, g, b, a);
    }

    /**
     * @param cellCount number of grid cells
     * @param columns   distinct column count (0 when unknown)
     * @param rows      distinct row count (0 when unknown)
     */
    public record GridPreviewResult(int cellCount, int columns, int rows) {
    }
}
