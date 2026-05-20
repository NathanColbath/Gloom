package org.llw.studio.tilemap;

/**
 * Packs tile cell coordinates into a long key for sparse maps.
 */
public final class TilemapCellKey {
    private TilemapCellKey() {
    }

    public static long pack(int cellX, int cellY) {
        return ((long) cellX << 32) | (cellY & 0xFFFFFFFFL);
    }

    public static int unpackX(long key) {
        return (int) (key >> 32);
    }

    public static int unpackY(long key) {
        return (int) key;
    }
}
