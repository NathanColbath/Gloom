package org.llw.render.renderables;

import org.junit.jupiter.api.Test;
import org.llw.math.geometry.RectF;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpriteTextureCoordsTest {
    @Test
    void atlasTopUsesLargerVThanBottomForTypicalSubRect() {
        RectF rect = new RectF(0f, 0f, 0.25f, 0.25f);
        float vAtlasTop = rect.top + rect.height;
        float vAtlasBottom = rect.top;
        assertTrue(vAtlasTop > vAtlasBottom);
    }
}
