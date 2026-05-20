package org.llw.studio.assets;

import org.junit.jupiter.api.Test;
import org.llw.math.geometry.RectF;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpriteDefinitionUvTest {
    @Test
    void topRowSliceUsesHigherVAtScreenTop() {
        SpriteDefinition top = new SpriteDefinition(
                "g", "top", "tex", 0, 0, 32, 32, 0.5f, 0.5f, 256, 256);
        RectF uv = top.uvRect(256, 256);
        float vAtlasTop = uv.top + uv.height;
        float vAtlasBottom = uv.top;
        assertTrue(vAtlasTop > vAtlasBottom);
        assertTrue(vAtlasTop > 0.85f);
    }

    @Test
    void bottomRowSliceIsBelowTopRowInV() {
        SpriteDefinition top = new SpriteDefinition(
                "a", "top", "tex", 0, 0, 32, 32, 0.5f, 0.5f, 256, 256);
        SpriteDefinition bottom = new SpriteDefinition(
                "b", "bottom", "tex", 0, 224, 32, 32, 0.5f, 0.5f, 256, 256);
        float topV = top.uvRect(256, 256).top + top.uvRect(256, 256).height;
        float bottomV = bottom.uvRect(256, 256).top + bottom.uvRect(256, 256).height;
        assertTrue(topV > bottomV);
    }

    @Test
    void topLeftPixelMapsToExpectedNormalizedU() {
        SpriteDefinition slice = new SpriteDefinition(
                "g", "s", "tex", 0, 0, 32, 32, 0.5f, 0.5f, 256, 256);
        RectF uv = slice.uvRect(256, 256);
        assertEquals(0.5f / 256f, uv.left, 0.001f);
    }
}
