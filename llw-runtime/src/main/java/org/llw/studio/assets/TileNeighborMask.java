package org.llw.studio.assets;

/**
 * Eight-direction neighbor constraints for one rule-tile rule.
 */
public final class TileNeighborMask {
    public TileNeighborConstraint north = TileNeighborConstraint.DONT_CARE;
    public TileNeighborConstraint northEast = TileNeighborConstraint.DONT_CARE;
    public TileNeighborConstraint east = TileNeighborConstraint.DONT_CARE;
    public TileNeighborConstraint southEast = TileNeighborConstraint.DONT_CARE;
    public TileNeighborConstraint south = TileNeighborConstraint.DONT_CARE;
    public TileNeighborConstraint southWest = TileNeighborConstraint.DONT_CARE;
    public TileNeighborConstraint west = TileNeighborConstraint.DONT_CARE;
    public TileNeighborConstraint northWest = TileNeighborConstraint.DONT_CARE;

    public TileNeighborMask copy() {
        TileNeighborMask copy = new TileNeighborMask();
        copy.north = north;
        copy.northEast = northEast;
        copy.east = east;
        copy.southEast = southEast;
        copy.south = south;
        copy.southWest = southWest;
        copy.west = west;
        copy.northWest = northWest;
        return copy;
    }
}
