package org.llw.studio.physics;

import org.jbox2d.common.Vec2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhysicsCoordinatesTest {
    @Test
    void studioAndBox2dPositionsRoundTrip() {
        Vec2 box2d = PhysicsCoordinates.studioToBox2d(10f, 20f);
        assertEquals(10f, box2d.x, 0.001f);
        assertEquals(-20f, box2d.y, 0.001f);
        assertEquals(10f, PhysicsCoordinates.studioXFromBox2d(box2d.x), 0.001f);
        assertEquals(20f, PhysicsCoordinates.studioYFromBox2d(box2d.y), 0.001f);
    }

    @Test
    void anglesFlipSignBetweenSpaces() {
        float studio = 45f;
        float box2d = PhysicsCoordinates.studioToBox2dAngleRadians(studio);
        assertEquals(studio, PhysicsCoordinates.box2dToStudioAngleDegrees(box2d), 0.001f);
    }
}
