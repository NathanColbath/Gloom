package org.llw.studio.physics;

import org.jbox2d.common.Vec2;

/**
 * Converts between studio Y-down space and Box2D Y-up space.
 */
public final class PhysicsCoordinates {
    private PhysicsCoordinates() {
    }

    public static Vec2 studioToBox2d(float studioX, float studioY) {
        return new Vec2(studioX, -studioY);
    }

    public static float studioXFromBox2d(float box2dX) {
        return box2dX;
    }

    public static float studioYFromBox2d(float box2dY) {
        return -box2dY;
    }

    public static float studioToBox2dAngleRadians(float studioDegrees) {
        return (float) Math.toRadians(-studioDegrees);
    }

    public static float box2dToStudioAngleDegrees(float box2dRadians) {
        return (float) -Math.toDegrees(box2dRadians);
    }

    public static Vec2 studioVectorToBox2d(float studioX, float studioY) {
        return new Vec2(studioX, -studioY);
    }

    public static Vec2 box2dVectorToStudio(Vec2 box2d) {
        return new Vec2(box2d.x, -box2d.y);
    }
}
