package org.llw.studio.tilemap;

import org.llw.studio.ecs.components.TilemapComponent;

/**
 * Converts between world space and tilemap cell coordinates (top-left anchor, Y-down).
 */
public final class TilemapMath {
    private TilemapMath() {
    }

    public static int worldToCellX(float worldX, float originX, float cellWidth, float scaleX) {
        float cellWorld = Math.max(cellWidth * Math.abs(scaleX), 1e-4f);
        return (int) Math.floor((worldX - originX) / cellWorld);
    }

    public static int worldToCellY(float worldY, float originY, float cellHeight, float scaleY) {
        float cellWorld = Math.max(cellHeight * Math.abs(scaleY), 1e-4f);
        return (int) Math.floor((worldY - originY) / cellWorld);
    }

    public static float cellToWorldX(int cellX, float originX, TilemapComponent tilemap, float scaleX) {
        return originX + cellX * tilemap.cellWidth * scaleX;
    }

    public static float cellToWorldY(int cellY, float originY, TilemapComponent tilemap, float scaleY) {
        return originY + cellY * tilemap.cellHeight * scaleY;
    }
}
