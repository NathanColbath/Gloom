package org.llw.studio.ecs.components;

/**
 * One painted tile cell. {@link #spriteGuid} stores the logical tile (rule-tile root).
 */
public final class TilemapCell {
    public static final int FLAG_FLIP_X = 1;
    public static final int FLAG_FLIP_Y = 2;
    public static final int FLAG_ROTATE_90 = 4;

    public String spriteGuid = "";
    public byte flags;

    public TilemapCell copy() {
        TilemapCell copy = new TilemapCell();
        copy.spriteGuid = spriteGuid;
        copy.flags = flags;
        return copy;
    }
}
