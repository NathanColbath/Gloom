package org.llw.render.graphics;

import org.llw.render.core.IntSize;
import org.llw.math.matrix.Matrix3x2;
import org.llw.math.geometry.RectF;
import org.llw.math.vector.Vector2f;

/**
 * 2D orthographic camera for mapping world coordinates to a render target's Y-down
 * pixel space.
 *
 * <p>The camera defines a rectangular world region centered at {@link #getCenter()} with
 * extent {@link #getSize()}. An optional normalized {@link #getViewport()} sub-rectangle
 * maps that region onto a portion of the target (0–1 fractions of width/height).
 *
 * <p>World space uses the same Y-down convention as screen space: increasing {@code y}
 * moves downward. {@link #screenToWorld(Vector2f, IntSize)} and
 * {@link #worldToScreen(Vector2f, IntSize)} convert between pixel coordinates and
 * world units for the given target size.
 */
public final class Camera2d {
    private final Vector2f center = new Vector2f();
    private final Vector2f size = new Vector2f(1000f, 1000f);
    private final RectF viewport = new RectF(0f, 0f, 1f, 1f);
    private final Matrix3x2 viewMatrix = new Matrix3x2();
    private final Matrix3x2 inverseViewMatrix = new Matrix3x2();
    private boolean dirty = true;

    /**
     * Creates a camera with default center {@code (0, 0)}, size {@code (1000, 1000)},
     * and a full-target viewport.
     */
    public Camera2d() {
        updateMatrices();
    }

    /**
     * Returns a copy of the world-space center of the visible region.
     *
     * @return camera center in world units (Y-down)
     */
    public Vector2f getCenter() {
        return center.copy();
    }

    /**
     * Sets the world-space center of the visible region.
     *
     * @param x center X in world units
     * @param y center Y in world units (Y-down)
     */
    public void setCenter(float x, float y) {
        center.set(x, y);
        dirty = true;
    }

    /**
     * Sets the world-space center from a vector.
     *
     * @param center new center position
     */
    public void setCenter(Vector2f center) {
        setCenter(center.x, center.y);
    }

    /**
     * Returns a copy of the world-space width and height of the visible region.
     *
     * @return visible world extent along each axis
     */
    public Vector2f getSize() {
        return size.copy();
    }

    /**
     * Sets the world-space width and height of the visible region.
     *
     * @param width  horizontal extent in world units
     * @param height vertical extent in world units
     */
    public void setSize(float width, float height) {
        size.set(width, height);
        dirty = true;
    }

    /**
     * Sets the world-space size from a vector.
     *
     * @param size width and height in world units
     */
    public void setSize(Vector2f size) {
        setSize(size.x, size.y);
    }

    /**
     * Returns a copy of the normalized viewport rectangle applied to the render target.
     *
     * <p>Components are fractions of the target's pixel width/height: {@code left} and
     * {@code top} are offsets from the top-left corner; {@code width} and {@code height}
     * are the viewport size as fractions of the full target.
     *
     * @return viewport in normalized [0, 1] coordinates
     */
    public RectF getViewport() {
        return viewport.copy();
    }

    /**
     * Sets the normalized viewport sub-rectangle on the render target.
     *
     * @param left   horizontal offset as a fraction of target width
     * @param top    vertical offset as a fraction of target height (from top)
     * @param width  viewport width as a fraction of target width
     * @param height viewport height as a fraction of target height
     */
    public void setViewport(float left, float top, float width, float height) {
        viewport.left = left;
        viewport.top = top;
        viewport.width = width;
        viewport.height = height;
        dirty = true;
    }

    /**
     * Builds an orthographic view matrix for the current center and size.
     *
     * <p>The matrix maps world coordinates to clip space for the given target dimensions.
     * Recomputes cached state if the camera was modified since the last call.
     *
     * @param targetSize pixel dimensions of the render target
     * @return orthographic view matrix (Y-down)
     */
    public Matrix3x2 getViewMatrix(IntSize targetSize) {
        if (dirty) {
            updateMatrices();
        }
        float left = center.x - size.x / 2f;
        float right = center.x + size.x / 2f;
        float top = center.y - size.y / 2f;
        float bottom = center.y + size.y / 2f;
        return Matrix3x2.ortho(left, right, top, bottom);
    }

    /**
     * Builds an orthographic projection matrix that applies the normalized viewport to
     * {@code targetSize}.
     *
     * @param targetSize pixel dimensions of the render target
     * @return viewport-scaled orthographic projection (Y-down)
     */
    public Matrix3x2 getProjectionMatrix(IntSize targetSize) {
        float viewportWidth = targetSize.width() * viewport.width;
        float viewportHeight = targetSize.height() * viewport.height;
        float viewportLeft = targetSize.width() * viewport.left;
        float viewportTop = targetSize.height() * viewport.top;
        Matrix3x2 projection = Matrix3x2.ortho(
                viewportLeft,
                viewportLeft + viewportWidth,
                viewportTop + viewportHeight,
                viewportTop
        );
        return projection;
    }

    /**
     * Returns the combined view matrix used when flushing a {@link RenderTarget}.
     *
     * <p>Currently equivalent to {@link #getViewMatrix(IntSize)} for 2D orthographic
     * rendering. Recomputes cached state if the camera was modified.
     *
     * @param targetSize pixel dimensions of the render target
     * @return view-projection matrix passed to the GPU during {@link RenderTarget#flush()}
     */
    public Matrix3x2 getViewProjection(IntSize targetSize) {
        if (dirty) {
            updateMatrices();
        }
        float left = center.x - size.x / 2f;
        float right = center.x + size.x / 2f;
        float top = center.y - size.y / 2f;
        float bottom = center.y + size.y / 2f;
        return Matrix3x2.ortho(left, right, top, bottom);
    }

    /**
     * Converts a point from target pixel coordinates to world space.
     *
     * @param screen     position in Y-down pixel coordinates relative to the target
     * @param targetSize pixel dimensions of the render target
     * @return corresponding world-space position
     */
    public Vector2f screenToWorld(Vector2f screen, IntSize targetSize) {
        if (dirty) {
            updateMatrices();
        }
        float ndcX = screen.x;
        float ndcY = screen.y;
        Matrix3x2 vp = getViewProjection(targetSize);
        // Simplified inverse for 2D ortho: rebuild inverse from view params
        float worldX = center.x - size.x / 2f + (ndcX - targetSize.width() * viewport.left) / (targetSize.width() * viewport.width) * size.x;
        float worldY = center.y - size.y / 2f + (ndcY - targetSize.height() * viewport.top) / (targetSize.height() * viewport.height) * size.y;
        return new Vector2f(worldX, worldY);
    }

    /**
     * Converts a point from world space to target pixel coordinates.
     *
     * @param world      position in world units (Y-down)
     * @param targetSize pixel dimensions of the render target
     * @return corresponding Y-down pixel position on the target
     */
    public Vector2f worldToScreen(Vector2f world, IntSize targetSize) {
        float screenX = viewport.left * targetSize.width() + ((world.x - (center.x - size.x / 2f)) / size.x) * viewport.width * targetSize.width();
        float screenY = viewport.top * targetSize.height() + ((world.y - (center.y - size.y / 2f)) / size.y) * viewport.height * targetSize.height();
        return new Vector2f(screenX, screenY);
    }

    private void updateMatrices() {
        float left = center.x - size.x / 2f;
        float right = center.x + size.x / 2f;
        float top = center.y - size.y / 2f;
        float bottom = center.y + size.y / 2f;
        viewMatrix.set(Matrix3x2.ortho(left, right, top, bottom));
        dirty = false;
    }
}
