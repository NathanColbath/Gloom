package org.llw.studio.assets;

/**
 * Maps a neighbor pattern to an output sprite variant for rule tiles.
 */
public final class RuleTileRule {
    public final TileNeighborMask neighbors;
    public String spriteGuid;

    public RuleTileRule(TileNeighborMask neighbors, String spriteGuid) {
        this.neighbors = neighbors == null ? new TileNeighborMask() : neighbors;
        this.spriteGuid = spriteGuid == null ? "" : spriteGuid;
    }
}
