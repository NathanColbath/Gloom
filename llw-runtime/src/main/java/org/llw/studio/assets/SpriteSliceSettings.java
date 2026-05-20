package org.llw.studio.assets;

/**
 * Grid slice parameters stored in texture importer meta.
 */
public final class SpriteSliceSettings {
    public int cellWidth = 32;
    public int cellHeight = 32;
    public int offsetX;
    public int offsetY;
    public int paddingX;
    public int paddingY;
    /** Max columns to generate; {@code 0} fills the available width. */
    public int columnCount;
    /** Max rows to generate; {@code 0} fills the available height. */
    public int rowCount;
    /** When true, row 0 in names is the bottom row of the grid (common for packed sheets). */
    public boolean indexFromBottom;

    public SpriteSliceSettings copy() {
        SpriteSliceSettings copy = new SpriteSliceSettings();
        copy.cellWidth = cellWidth;
        copy.cellHeight = cellHeight;
        copy.offsetX = offsetX;
        copy.offsetY = offsetY;
        copy.paddingX = paddingX;
        copy.paddingY = paddingY;
        copy.columnCount = columnCount;
        copy.rowCount = rowCount;
        copy.indexFromBottom = indexFromBottom;
        return copy;
    }
}
