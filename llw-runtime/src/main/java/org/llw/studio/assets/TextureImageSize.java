package org.llw.studio.assets;

import org.llw.render.core.IntSize;
import org.llw.render.graphics.Texture2d;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads raster dimensions using the same STB decoder as {@link Texture2d} (with ImageIO fallback).
 */
public final class TextureImageSize {
    private TextureImageSize() {
    }

    /**
     * @param path image file on disk
     * @return width and height in pixels; {@code 1×1} when the file cannot be read
     */
    public static IntSize read(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            return new IntSize(1, 1);
        }
        try {
            byte[] bytes = Files.readAllBytes(path);
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes).flip();
            int[] width = new int[1];
            int[] height = new int[1];
            int[] channels = new int[1];
            if (STBImage.stbi_info_from_memory(buffer, width, height, channels)) {
                return new IntSize(Math.max(1, width[0]), Math.max(1, height[0]));
            }
        } catch (IOException ignored) {
        }
        try {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image != null) {
                return new IntSize(Math.max(1, image.getWidth()), Math.max(1, image.getHeight()));
            }
        } catch (IOException ignored) {
        }
        return new IntSize(1, 1);
    }

    /**
     * @param texture loaded GPU texture
     * @return pixel size from the texture object
     */
    public static IntSize fromTexture(Texture2d texture) {
        if (texture == null) {
            return new IntSize(1, 1);
        }
        return texture.size();
    }
}
