package org.llw.studio.assets;

import java.util.Locale;

/**
 * Direction the source art faces in the texture atlas before entity rotation is applied.
 * <p>
 * Studio gameplay treats transform rotation {@code 0} as facing right (+X). Use {@link #UP}
 * when sprites are drawn pointing up in the image so they appear facing right at rotation zero.
 */
public enum SpriteArtFacing {
    RIGHT,
    UP;

    /**
     * @param raw serialized value from texture meta ({@code "up"} or {@code "right"})
     * @return parsed facing, defaulting to {@link #RIGHT}
     */
    public static SpriteArtFacing fromId(String raw) {
        if (raw == null || raw.isBlank()) {
            return RIGHT;
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "up" -> UP;
            default -> RIGHT;
        };
    }

    /**
     * @return value stored in {@code importer.texture.artFacing}
     */
    public String id() {
        return switch (this) {
            case RIGHT -> "right";
            case UP -> "up";
        };
    }

    /**
     * Extra rotation applied at draw time so art orientation matches gameplay forward (+X).
     *
     * @return degrees added to {@link org.llw.studio.ecs.components.Transform2DComponent#rotation}
     */
    public float rotationOffsetDegrees() {
        return switch (this) {
            case RIGHT -> 0f;
            case UP -> 90f;
        };
    }

    /**
     * @param transformRotationDegrees entity world rotation in degrees
     * @return rotation passed to the renderer
     */
    public float applyToRotation(float transformRotationDegrees) {
        return transformRotationDegrees + rotationOffsetDegrees();
    }
}
