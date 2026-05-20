package org.llw.studio.assets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpriteSlicerTest {
    @Test
    void gridSliceProducesSixteenCellsOn64x64Atlas() {
        SpriteSliceSettings settings = new SpriteSliceSettings();
        settings.cellWidth = 16;
        settings.cellHeight = 16;
        var slices = SpriteSlicer.sliceGrid(64, 64, "sheet.png", settings);
        assertEquals(16, slices.size());
        assertEquals("sheet_0", slices.get(0).name());
        assertEquals(0, slices.get(0).x());
        assertEquals(0, slices.get(0).y());
        assertEquals(16, slices.get(0).width());
        assertEquals(16, slices.get(0).height());
    }

    @Test
    void rowAndColumnLimitsCapGridSize() {
        SpriteSliceSettings settings = new SpriteSliceSettings();
        settings.cellWidth = 16;
        settings.cellHeight = 16;
        settings.columnCount = 2;
        settings.rowCount = 1;
        var slices = SpriteSlicer.sliceGrid(64, 64, "sheet.png", settings);
        assertEquals(2, slices.size());
    }

    @Test
    void indexFromBottomStartsAtLowestRow() {
        SpriteSliceSettings settings = new SpriteSliceSettings();
        settings.cellWidth = 16;
        settings.cellHeight = 16;
        settings.indexFromBottom = true;
        var slices = SpriteSlicer.sliceGrid(64, 64, "sheet.png", settings);
        assertEquals(16, slices.size());
        assertEquals(48, slices.get(0).y());
        assertEquals(0, slices.get(15).y());
    }
}
