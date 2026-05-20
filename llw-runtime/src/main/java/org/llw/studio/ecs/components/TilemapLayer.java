package org.llw.studio.ecs.components;

import org.llw.studio.tilemap.TilemapCellKey;

import java.util.HashMap;
import java.util.Map;

/**
 * One drawable layer in a tilemap.
 */
public final class TilemapLayer {
    public String name = "Layer";
    public boolean enabled = true;
    public int sortingOrder;
    public final Map<Long, TilemapCell> cells = new HashMap<>();

    public TilemapLayer copy() {
        TilemapLayer copy = new TilemapLayer();
        copy.name = name;
        copy.enabled = enabled;
        copy.sortingOrder = sortingOrder;
        for (Map.Entry<Long, TilemapCell> entry : cells.entrySet()) {
            copy.cells.put(entry.getKey(), entry.getValue().copy());
        }
        return copy;
    }

    public TilemapCell getCell(int cellX, int cellY) {
        return cells.get(TilemapCellKey.pack(cellX, cellY));
    }

    public void setCell(int cellX, int cellY, TilemapCell cell) {
        long key = TilemapCellKey.pack(cellX, cellY);
        if (cell == null || cell.spriteGuid == null || cell.spriteGuid.isBlank()) {
            cells.remove(key);
        } else {
            cells.put(key, cell);
        }
    }

    public void removeCell(int cellX, int cellY) {
        cells.remove(TilemapCellKey.pack(cellX, cellY));
    }
}
