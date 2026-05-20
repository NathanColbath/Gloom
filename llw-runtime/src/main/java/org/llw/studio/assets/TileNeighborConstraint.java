package org.llw.studio.assets;

/**
 * Neighbor matching constraint for rule tiles (Unity Rule Tile style).
 */
public enum TileNeighborConstraint {
    /** Ignore this direction when matching. */
    DONT_CARE,
    /** Adjacent cell has a tile in the same rule-tile group. */
    SAME,
    /** Adjacent cell is empty or has a tile from a different rule-tile group. */
    NOT_SAME
}
