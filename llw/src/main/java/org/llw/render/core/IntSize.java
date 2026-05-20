package org.llw.render.core;

/**
 * Immutable pixel width and height pair.
 *
 * <p>Used to describe render-target and framebuffer dimensions in Y-down screen space.
 *
 * @param width horizontal size in pixels
 * @param height vertical size in pixels
 */
public record IntSize(int width, int height) {
    /**
     * Returns {@code width / height} as a float.
     *
     * <p>If {@code height} is zero, returns {@code 1f} to avoid division by zero.
     *
     * @return aspect ratio (width divided by height)
     */
    public float aspectRatio() {
        return height == 0 ? 1f : (float) width / height;
    }
}
