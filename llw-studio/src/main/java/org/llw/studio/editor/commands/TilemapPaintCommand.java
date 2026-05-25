package org.llw.studio.editor.commands;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.TilemapCell;
import org.llw.studio.ecs.components.TilemapComponent;
import org.llw.studio.ecs.components.TilemapLayer;
import org.llw.studio.scene.Scene;
import org.llw.studio.tilemap.TilemapCellKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Undoable batch of tile paint/erase operations on one layer.
 */
public final class TilemapPaintCommand implements EditorCommand {
    private final Scene scene;
    private final EntityId entity;
    private final int layerIndex;
    private final Map<Long, TilemapCell> before;
    private final Map<Long, TilemapCell> after;

    public TilemapPaintCommand(
            Scene scene,
            EntityId entity,
            int layerIndex,
            Map<Long, TilemapCell> before,
            Map<Long, TilemapCell> after
    ) {
        this.scene = scene;
        this.entity = entity;
        this.layerIndex = layerIndex;
        this.before = copyMap(before);
        this.after = copyMap(after); // Defensive copy so later scene edits cannot mutate stored snapshots.
    }

    @Override
    public void execute() {
        apply(after);
    }

    @Override
    public void undo() {
        apply(before);
    }

    private void apply(Map<Long, TilemapCell> cells) {
        TilemapComponent tilemap = scene.world().getComponent(entity, TilemapComponent.class);
        if (tilemap == null) {
            return;
        }
        TilemapLayer layer = tilemap.layerAt(layerIndex);
        // Full-layer replace keeps undo simple; paint controller coalesces drag strokes into one command.
        layer.cells.clear();
        for (Map.Entry<Long, TilemapCell> entry : cells.entrySet()) {
            TilemapCell cell = entry.getValue();
            if (cell == null || cell.spriteGuid == null || cell.spriteGuid.isBlank()) {
                continue;
            }
            layer.cells.put(entry.getKey(), cell.copy());
        }
    }

    public static Map<Long, TilemapCell> snapshotLayer(TilemapLayer layer) {
        Map<Long, TilemapCell> map = new HashMap<>();
        for (Map.Entry<Long, TilemapCell> entry : layer.cells.entrySet()) {
            map.put(entry.getKey(), entry.getValue().copy());
        }
        return map;
    }

    public static void restoreCells(TilemapLayer layer, Map<Long, TilemapCell> snapshot) {
        layer.cells.clear();
        for (Map.Entry<Long, TilemapCell> entry : snapshot.entrySet()) {
            TilemapCell cell = entry.getValue();
            if (cell != null && cell.spriteGuid != null && !cell.spriteGuid.isBlank()) {
                layer.cells.put(entry.getKey(), cell.copy());
            }
        }
    }

    public static long key(int cellX, int cellY) {
        return TilemapCellKey.pack(cellX, cellY);
    }

    private static Map<Long, TilemapCell> copyMap(Map<Long, TilemapCell> source) {
        Map<Long, TilemapCell> copy = new HashMap<>();
        for (Map.Entry<Long, TilemapCell> entry : source.entrySet()) {
            TilemapCell cell = entry.getValue();
            copy.put(entry.getKey(), cell == null ? null : cell.copy());
        }
        return copy;
    }
}
