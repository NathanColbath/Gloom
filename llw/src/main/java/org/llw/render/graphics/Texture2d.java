package org.llw.render.graphics;

import java.nio.ByteBuffer;

import org.llw.render.core.IntSize;
import org.llw.util.log.Log;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;

/**
 * GPU-backed 2D RGBA texture. Default sampling is linear filtering with clamp-to-edge wrapping.
 *
 * <p>Factory methods create OpenGL texture objects. Call {@link #dispose()} when a texture
 * is no longer referenced to avoid leaking GPU memory. Image loaders flip vertically on
 * decode so pixel data matches Y-down screen space.
 */
public final class Texture2d {
    private static final Logger log = Log.get(Loggers.GRAPHICS);

    private final int id;
    private final IntSize size;
    private boolean disposed;

    private Texture2d(int id, IntSize size) {
        this.id = id;
        this.size = size;
    }

    /**
     * Allocates an uninitialized RGBA texture of {@code size}.
     *
     * @param size width and height in pixels
     * @return new empty texture with undefined pixel contents
     */
    public static Texture2d createEmpty(IntSize size) {
        int textureId = GL11.glGenTextures();
        bindTexture(textureId);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, size.width(), size.height(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        configureSampling();
        return new Texture2d(textureId, size);
    }

    /**
     * Uploads raw RGBA8 pixel data from a direct {@link ByteBuffer}.
     *
     * @param width       image width in pixels
     * @param height      image height in pixels
     * @param rgbaPixels  tightly packed RGBA bytes ({@code width * height * 4} bytes)
     * @return new texture containing the uploaded pixels
     */
    public static Texture2d fromRgbaPixels(int width, int height, ByteBuffer rgbaPixels) {
        int textureId = GL11.glGenTextures();
        bindTexture(textureId);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, rgbaPixels);
        configureSampling();
        return new Texture2d(textureId, new IntSize(width, height));
    }

    /**
     * Decodes an image from memory (PNG, JPEG, etc. via STB) and uploads it as a texture.
     *
     * <p>Images are flipped vertically on load for Y-down coordinate compatibility.
     *
     * @param imageBuffer encoded image bytes in a direct buffer
     * @return decoded texture
     * @throws IllegalStateException if decoding fails
     */
    public static Texture2d fromMemory(ByteBuffer imageBuffer) {
        STBImage.stbi_set_flip_vertically_on_load(true);
        int[] width = new int[1];
        int[] height = new int[1];
        int[] channels = new int[1];
        ByteBuffer pixels = STBImage.stbi_load_from_memory(imageBuffer, width, height, channels, 4);
        if (pixels == null) {
            String reason = STBImage.stbi_failure_reason();
            log.error("Failed to decode image: {}", reason);
            throw new IllegalStateException("Failed to decode image: " + reason);
        }

        try {
            int textureId = GL11.glGenTextures();
            bindTexture(textureId);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width[0], height[0], 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
            configureSampling();
            Texture2d texture = new Texture2d(textureId, new IntSize(width[0], height[0]));
            log.debug("Decoded texture {}x{} id={} channels={}", width[0], height[0], textureId, channels[0]);
            return texture;
        } finally {
            STBImage.stbi_image_free(pixels);
        }
    }

    /**
     * Decodes an image from a byte array and uploads it as a texture.
     *
     * @param bytes encoded image file contents
     * @return decoded texture
     * @throws IllegalStateException if decoding fails
     */
    public static Texture2d fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.put(bytes).flip();
        return fromMemory(buffer);
    }

    /**
     * Wraps an existing OpenGL texture name without taking ownership semantics beyond
     * {@link #dispose()}.
     *
     * @param textureId existing {@code GL_TEXTURE_2D} object id
     * @param size      known pixel dimensions
     * @return wrapper around the foreign texture id
     */
    public static Texture2d fromRaw(int textureId, IntSize size) {
        return new Texture2d(textureId, size);
    }

    /**
     * Returns a 1×1 opaque white texture, useful as a default sampler for untextured draws.
     *
     * @return solid white {@link Texture2d}
     */
    public static Texture2d whitePixel() {
        ByteBuffer pixel = ByteBuffer.allocateDirect(4);
        pixel.put((byte) 255).put((byte) 255).put((byte) 255).put((byte) 255).flip();
        int textureId = GL11.glGenTextures();
        bindTexture(textureId);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, 1, 1, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixel);
        configureSampling();
        return new Texture2d(textureId, new IntSize(1, 1));
    }

    /**
     * Returns the OpenGL texture object name.
     *
     * @return {@code GL_TEXTURE_2D} id
     */
    public int id() {
        return id;
    }

    /**
     * Returns the texture dimensions in pixels.
     *
     * @return immutable width and height
     */
    public IntSize size() {
        return size;
    }

    /**
     * Activates this texture on the given texture image unit.
     *
     * @param unit texture unit index (0 = {@code GL_TEXTURE0})
     */
    public void bind(int unit) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    /**
     * Updates min/mag filter and wrap modes on this texture object.
     *
     * @param filter filter used for both minification and magnification
     * @param wrap   wrap used for both S and T axes
     */
    public void applySampling(TextureFilter filter, TextureWrap wrap) {
        if (disposed) {
            return;
        }
        bindTexture(id);
        int glFilter = filter == null ? TextureFilter.LINEAR.glConstant() : filter.glConstant();
        int glWrap = wrap == null ? TextureWrap.CLAMP.glConstant() : wrap.glConstant();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, glFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, glFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, glWrap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, glWrap);
    }

    /**
     * Deletes the OpenGL texture object. Safe to call multiple times.
     *
     * <p>Do not bind or draw with this texture after disposal.
     */
    public void dispose() {
        if (!disposed) {
            GL11.glDeleteTextures(id);
            disposed = true;
        }
    }

    private static void bindTexture(int textureId) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
    }

    private static void configureSampling() {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, TextureFilter.LINEAR.glConstant());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, TextureFilter.LINEAR.glConstant());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, TextureWrap.CLAMP.glConstant());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, TextureWrap.CLAMP.glConstant());
    }
}
