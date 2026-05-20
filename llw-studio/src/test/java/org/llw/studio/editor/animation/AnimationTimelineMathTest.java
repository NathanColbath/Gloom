package org.llw.studio.editor.animation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnimationTimelineMathTest {
    @Test
    void timeAndXWithScroll() {
        assertEquals(50f, AnimationTimelineMath.timeToX(0.5f, 100f, 0f), 0.001f);
        assertEquals(0f, AnimationTimelineMath.timeToX(0.5f, 100f, 50f), 0.001f);
        assertEquals(0.5f, AnimationTimelineMath.xToTime(50f, 100f, 0f, 2f), 0.001f);
        assertEquals(1f, AnimationTimelineMath.xToTime(50f, 100f, 50f, 2f), 0.001f);
    }
}
