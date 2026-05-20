package org.llw.render.graphics;

import java.nio.ByteBuffer;

import org.llw.render.core.Color;
import org.llw.render.core.IntSize;

/**
 * Procedural {@link Texture2d} builders for debugging and placeholder art.
 *
 * <p>All methods allocate GPU textures; callers should {@link Texture2d#dispose()} results
 * when no longer needed.
 */
public final class TextureFactory {
    private TextureFactory() {}

    /**
     * Creates a two-tone checkerboard pattern.
     *
     * @param width    texture width in pixels
     * @param height   texture height in pixels
     * @param tileSize side length of each square in pixels
     * @return checkerboard {@link Texture2d}
     */
    public static Texture2d checkerboard(int width, int height, int tileSize) {
        ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean light = ((x / tileSize) + (y / tileSize)) % 2 == 0;
                byte value = light ? (byte) 230 : (byte) 60;
                pixels.put(value).put((byte) (light ? 90 : 30)).put((byte) (light ? 120 : 180)).put((byte) 255);
            }
        }
        pixels.flip();
        return Texture2d.fromRgbaPixels(width, height, pixels);
    }

    /**
     * Creates a solid-color texture filled with {@code color}.
     *
     * @param size  width and height in pixels
     * @param color uniform RGBA fill
     * @return solid {@link Texture2d}
     */
    public static Texture2d solid(IntSize size, Color color) {
        ByteBuffer pixels = ByteBuffer.allocateDirect(size.width() * size.height() * 4);
        for (int i = 0; i < size.width() * size.height(); i++) {
            pixels.put((byte) color.r).put((byte) color.g).put((byte) color.b).put((byte) color.a);
        }
        pixels.flip();
        return Texture2d.fromRgbaPixels(size.width(), size.height(), pixels);
    }
}
