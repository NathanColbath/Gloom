package org.llw.studio.ecs.components;

import java.util.ArrayList;
import java.util.List;

/**
 * Grid tilemap referencing a texture tileset. Cells are stored per layer.
 */
public final class TilemapComponent implements Cloneable {
    public String tilesetTextureGuid = "";
    public float cellWidth = 32f;
    public float cellHeight = 32f;
    public final List<TilemapLayer> layers = new ArrayList<>();

    public TilemapComponent() {
        ensureDefaultLayer();
    }

    public void ensureDefaultLayer() {
        if (layers.isEmpty()) {
            layers.add(new TilemapLayer());
        }
    }

    public TilemapLayer layerAt(int index) {
        ensureDefaultLayer();
        if (index < 0 || index >= layers.size()) {
            return layers.get(0);
        }
        return layers.get(index);
    }

    public TilemapComponent copy() {
        TilemapComponent copy = new TilemapComponent();
        copy.tilesetTextureGuid = tilesetTextureGuid;
        copy.cellWidth = cellWidth;
        copy.cellHeight = cellHeight;
        for (TilemapLayer layer : layers) {
            copy.layers.add(layer.copy());
        }
        return copy;
    }

    @Override
    public TilemapComponent clone() {
        return copy();
    }
}
