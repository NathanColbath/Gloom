package org.llw.studio.ui;

/**
 * How a {@link org.llw.studio.ecs.components.UICanvasComponent} positions its widget tree.
 */
public enum UiCanvasRenderMode {
    /** Viewport pixels (HUD); ignores world parent chain. */
    SCREEN_SPACE,
    /** Follows canvas world transform; child offsets are world units. */
    WORLD_SPACE;

    /**
     * @param id serialized id
     * @return mode, or {@link #SCREEN_SPACE} when unknown
     */
    public static UiCanvasRenderMode fromId(String id) {
        if (id == null) {
            return SCREEN_SPACE;
        }
        return switch (id) {
            case "worldSpace", "WORLD_SPACE" -> WORLD_SPACE;
            default -> SCREEN_SPACE;
        };
    }

    /** @return stable serialization id */
    public String id() {
        return switch (this) {
            case SCREEN_SPACE -> "screenSpace";
            case WORLD_SPACE -> "worldSpace";
        };
    }
}
