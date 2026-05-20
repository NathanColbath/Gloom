package org.llw.studio.assets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates sprite rectangles from a uniform grid over a texture.
 */
public final class SpriteSlicer {
    private SpriteSlicer() {
    }

    /**
     * @param imageWidth   atlas width in pixels
     * @param imageHeight  atlas height in pixels
     * @param baseName     file name used as prefix for generated slice names
     * @param settings     grid cell size, offset, and padding
     * @return slice definitions without GUIDs (caller assigns)
     */
    public static List<SpriteSliceDraft> sliceGrid(
            int imageWidth,
            int imageHeight,
            String baseName,
            SpriteSliceSettings settings
    ) {
        List<SpriteSliceDraft> slices = new ArrayList<>();
        if (imageWidth <= 0 || imageHeight <= 0 || settings == null) {
            return slices;
        }
        int cellW = Math.max(1, settings.cellWidth);
        int cellH = Math.max(1, settings.cellHeight);
        int stepX = cellW + settings.paddingX;
        int stepY = cellH + settings.paddingY;
        String prefix = sanitizeBaseName(baseName);

        List<Integer> rowYs = new ArrayList<>();
        for (int y = settings.offsetY; y + cellH <= imageHeight; y += stepY) {
            rowYs.add(y);
        }
        if (settings.indexFromBottom) {
            Collections.reverse(rowYs);
        }

        int maxRows = settings.rowCount > 0 ? settings.rowCount : Integer.MAX_VALUE;
        int maxCols = settings.columnCount > 0 ? settings.columnCount : Integer.MAX_VALUE;
        int index = 0;
        int row = 0;
        for (int y : rowYs) {
            if (row >= maxRows) {
                break;
            }
            int col = 0;
            for (int x = settings.offsetX; x + cellW <= imageWidth; x += stepX) {
                if (col >= maxCols) {
                    break;
                }
                slices.add(new SpriteSliceDraft(
                        prefix + "_" + index,
                        x,
                        y,
                        cellW,
                        cellH,
                        0.5f,
                        0.5f
                ));
                index++;
                col++;
            }
            row++;
        }
        return slices;
    }

    private static String sanitizeBaseName(String baseName) {
        if (baseName == null || baseName.isBlank()) {
            return "sprite";
        }
        int dot = baseName.lastIndexOf('.');
        String stem = dot > 0 ? baseName.substring(0, dot) : baseName;
        return stem.isBlank() ? "sprite" : stem;
    }

    /**
     * Intermediate slice before GUID assignment.
     */
    public record SpriteSliceDraft(
            String name,
            int x,
            int y,
            int width,
            int height,
            float pivotX,
            float pivotY
    ) {
    }
}
