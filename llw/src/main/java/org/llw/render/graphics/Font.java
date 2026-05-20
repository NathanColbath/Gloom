package org.llw.render.graphics;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.llw.render.core.IntSize;
import org.llw.render.resources.ResourceLoader;
import org.llw.util.log.Log;
import org.llw.util.log.LogHelper;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTruetype;

/**
 * Bitmap font backed by a single-channel glyph atlas {@link Texture2d}.
 *
 * <p>Glyphs cover ASCII printable characters (code points 32–127). Use {@link #glyph(char)}
 * for metrics and UVs when laying out text in Y-down screen space. Call {@link #dispose()}
 * to release the atlas texture.
 */
public final class Font {
    private static final Logger log = Log.get(Loggers.GRAPHICS);

    private static final int ATLAS_SIZE = 512;
    private static final int FIRST_CHAR = 32;
    private static final int CHAR_COUNT = 96;

    private final Texture2d atlas;
    private final Map<Character, Glyph> glyphs = new HashMap<>();
    private final float lineHeight;

    private Font(Texture2d atlas, Map<Character, Glyph> glyphs, float lineHeight) {
        this.atlas = atlas;
        this.glyphs.putAll(glyphs);
        this.lineHeight = lineHeight;
    }

    /**
     * Loads a TrueType font from the classpath, rasterizes glyphs at {@code pixelHeight},
     * and packs them into an atlas texture.
     *
     * @param classpathPath path to a {@code .ttf} resource
     * @param pixelHeight   target cap height in pixels
     * @return ready-to-use {@link Font}
     * @throws IllegalStateException if the font file cannot be initialized
     */
    public static Font fromClasspath(String classpathPath, int pixelHeight) {
        return fromBytes(ResourceLoader.loadBytes(classpathPath), pixelHeight);
    }

    /**
     * Loads a TrueType font from the filesystem.
     *
     * @param path        path to a {@code .ttf} or {@code .otf} file
     * @param pixelHeight target cap height in pixels
     * @return ready-to-use {@link Font}
     */
    public static Font fromFile(Path path, int pixelHeight) {
        try {
            return fromBytes(Files.readAllBytes(path), pixelHeight);
        } catch (IOException e) {
            throw LogHelper.logAndThrow(log, "Failed to read font file: " + path, e);
        }
    }

    /**
     * Builds a font from in-memory TrueType bytes.
     *
     * @param fontBytes   TTF/OTF file contents
     * @param pixelHeight target cap height in pixels
     * @return ready-to-use {@link Font}
     */
    public static Font fromBytes(byte[] fontBytes, int pixelHeight) {
        ByteBuffer fontBuffer = ByteBuffer.allocateDirect(fontBytes.length);
        fontBuffer.put(fontBytes).flip();

        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        if (!STBTruetype.stbtt_InitFont(fontInfo, fontBuffer)) {
            log.error("Failed to initialize font from memory pixelHeight={}", pixelHeight);
            throw new IllegalStateException("Failed to initialize font from memory");
        }

        int[] ascentBox = new int[1];
        int[] descentBox = new int[1];
        int[] lineGap = new int[1];
        STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascentBox, descentBox, lineGap);
        float scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, pixelHeight);
        float lineHeight = (ascentBox[0] - descentBox[0] + lineGap[0]) * scale;

        ByteBuffer bitmap = ByteBuffer.allocateDirect(ATLAS_SIZE * ATLAS_SIZE);
        STBTTPackedchar.Buffer packedChars = STBTTPackedchar.malloc(CHAR_COUNT);
        STBTTPackContext packContext = STBTTPackContext.malloc();
        STBTruetype.stbtt_PackBegin(packContext, bitmap, ATLAS_SIZE, ATLAS_SIZE, 0, 1, 0L);
        fontBuffer.rewind();
        STBTruetype.stbtt_PackFontRange(packContext, fontBuffer, 0, pixelHeight, FIRST_CHAR, packedChars);
        STBTruetype.stbtt_PackEnd(packContext);
        packContext.free();

        Texture2d atlas = uploadAtlas(bitmap, ATLAS_SIZE, ATLAS_SIZE);
        Map<Character, Glyph> glyphs = new HashMap<>();
        STBTTAlignedQuad quad = STBTTAlignedQuad.malloc();
        try {
            for (int codepoint = FIRST_CHAR; codepoint < FIRST_CHAR + CHAR_COUNT; codepoint++) {
                float[] penX = {0f};
                float[] penY = {0f};
                STBTruetype.stbtt_GetPackedQuad(
                        packedChars,
                        ATLAS_SIZE,
                        ATLAS_SIZE,
                        codepoint - FIRST_CHAR,
                        penX,
                        penY,
                        quad,
                        true
                );
                glyphs.put((char) codepoint, new Glyph(
                        quad.x0(),
                        quad.y0(),
                        quad.x1(),
                        quad.y1(),
                        quad.s0(),
                        quad.t0(),
                        quad.s1(),
                        quad.t1(),
                        penX[0]
                ));
            }
        } finally {
            quad.free();
            packedChars.free();
        }

        log.debug("Built font pixelHeight={} lineHeight={} atlas={}x{}", pixelHeight, lineHeight, ATLAS_SIZE, ATLAS_SIZE);
        return new Font(atlas, glyphs, lineHeight);
    }

    /**
     * Returns the grayscale glyph atlas used for text rendering.
     *
     * @return shared {@link Texture2d} for all glyphs in this font
     */
    public Texture2d atlas() {
        return atlas;
    }

    /**
     * Returns layout and UV metrics for {@code character}.
     *
     * <p>Unknown characters fall back to the {@code '?'} glyph.
     *
     * @param character code point to look up (ASCII printable range)
     * @return glyph quad and horizontal advance
     */
    public Glyph glyph(char character) {
        Glyph glyph = glyphs.get(character);
        if (glyph != null) {
            return glyph;
        }
        return glyphs.get('?');
    }

    /**
     * Returns the recommended vertical distance between baselines in pixels.
     *
     * @return line height for this font size
     */
    public float lineHeight() {
        return lineHeight;
    }

    /**
     * Releases the underlying atlas {@link Texture2d}.
     *
     * <p>Do not draw text with this font after disposal.
     */
    public void dispose() {
        atlas.dispose();
    }

    private static Texture2d uploadAtlas(ByteBuffer bitmap, int width, int height) {
        ByteBuffer rgba = ByteBuffer.allocateDirect(width * height * 4);
        for (int i = 0; i < width * height; i++) {
            byte coverage = bitmap.get(i);
            int offset = i * 4;
            rgba.put(offset, coverage);
            rgba.put(offset + 1, (byte) 0);
            rgba.put(offset + 2, (byte) 0);
            rgba.put(offset + 3, (byte) 255);
        }
        rgba.position(0);
        rgba.limit(width * height * 4);

        int textureId = org.lwjgl.opengl.GL11.glGenTextures();
        org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, textureId);
        org.lwjgl.opengl.GL11.glTexImage2D(
                org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                0,
                org.lwjgl.opengl.GL11.GL_RGBA8,
                width,
                height,
                0,
                org.lwjgl.opengl.GL11.GL_RGBA,
                org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE,
                rgba
        );
        org.lwjgl.opengl.GL11.glTexParameteri(
                org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER,
                org.lwjgl.opengl.GL11.GL_LINEAR
        );
        org.lwjgl.opengl.GL11.glTexParameteri(
                org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER,
                org.lwjgl.opengl.GL11.GL_LINEAR
        );
        org.lwjgl.opengl.GL11.glTexParameteri(
                org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_S,
                org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
        );
        org.lwjgl.opengl.GL11.glTexParameteri(
                org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_T,
                org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
        );
        return Texture2d.fromRaw(textureId, new IntSize(width, height));
    }

    /**
     * Per-character metrics from the packed atlas: screen quad, UV rectangle, and advance.
     *
     * @param x0     left edge of the glyph quad in local space (Y-down)
     * @param y0     top edge of the glyph quad in local space
     * @param x1     right edge of the glyph quad
     * @param y1     bottom edge of the glyph quad
     * @param s0     atlas U coordinate of the top-left texel
     * @param t0     atlas V coordinate of the top-left texel
     * @param s1     atlas U coordinate of the bottom-right texel
     * @param t1     atlas V coordinate of the bottom-right texel
     * @param advance horizontal distance to advance the pen after this glyph
     */
    public record Glyph(
            float x0,
            float y0,
            float x1,
            float y1,
            float s0,
            float t0,
            float s1,
            float t1,
            float advance
    ) {}
}
