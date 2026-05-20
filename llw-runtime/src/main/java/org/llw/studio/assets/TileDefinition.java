package org.llw.studio.assets;

/**
 * Metadata for one tile in a tileset (references a sprite slice by GUID).
 */
public final class TileDefinition {
    public String spriteGuid = "";
    public TileCollisionType collision = TileCollisionType.NONE;
    public RuleTileDefinition ruleTile;

    public TileDefinition() {
    }

    public TileDefinition(String spriteGuid) {
        this.spriteGuid = spriteGuid == null ? "" : spriteGuid;
    }

    public TileDefinition copy() {
        TileDefinition copy = new TileDefinition(spriteGuid);
        copy.collision = collision;
        copy.ruleTile = ruleTile == null ? null : ruleTile.copy();
        return copy;
    }

    /** @return rule-tile group id used for neighbor matching */
    public String ruleTileGroupId() {
        if (ruleTile == null || !ruleTile.isActive()) {
            return "";
        }
        return ruleTile.defaultSpriteGuid.isBlank() ? spriteGuid : ruleTile.defaultSpriteGuid;
    }
}
