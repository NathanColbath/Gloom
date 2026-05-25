package org.llw.math.matrix;

import java.nio.FloatBuffer;

import org.llw.math.vector.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

/**
 * Mutable 2D affine transform stored as a 4×4 matrix suitable for OpenGL (column-major).
 *
 * <p>Coordinates follow a top-left origin: +X right, +Y down.
 */
public final class Matrix3x2 {
    private static final ThreadLocal<FloatBuffer> UPLOAD_BUFFER =
            ThreadLocal.withInitial(() -> BufferUtils.createFloatBuffer(16));

    private final float[] m = new float[16];
    private final float[] multiplyScratch = new float[16];

    /** Creates a new matrix initialized to the identity transform. */
    public Matrix3x2() {
        identity();
    }

    /**
     * Replaces this matrix with the identity transform.
     *
     * @return this matrix for chaining
     */
    public Matrix3x2 identity() {
        for (int i = 0; i < 16; i++) {
            m[i] = 0f;
        }
        m[0] = 1f;
        m[5] = 1f;
        m[10] = 1f;
        m[15] = 1f;
        return this;
    }

    /**
     * Copies all elements from {@code other} into this matrix.
     *
     * @param other source matrix
     * @return this matrix for chaining
     */
    public Matrix3x2 set(Matrix3x2 other) {
        System.arraycopy(other.m, 0, m, 0, 16);
        return this;
    }

    /**
     * Post-multiplies this matrix by a translation of {@code (x, y)}.
     *
     * @param x translation X
     * @param y translation Y
     * @return this matrix for chaining
     */
    public Matrix3x2 translate(float x, float y) {
        m[12] += m[0] * x + m[4] * y;
        m[13] += m[1] * x + m[5] * y;
        return this;
    }

    /**
     * Post-multiplies this matrix by a rotation of {@code radians}.
     *
     * @param radians rotation in radians
     * @return this matrix for chaining
     */
    public Matrix3x2 rotate(float radians) {
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float r0 = m[0], r1 = m[1], r4 = m[4], r5 = m[5];
        m[0] = r0 * cos + r4 * sin;
        m[1] = r1 * cos + r5 * sin;
        m[4] = r4 * cos - r0 * sin;
        m[5] = r5 * cos - r1 * sin;
        return this;
    }

    /**
     * Post-multiplies this matrix by a non-uniform scale {@code (sx, sy)}.
     *
     * @param sx scale along X
     * @param sy scale along Y
     * @return this matrix for chaining
     */
    public Matrix3x2 scale(float sx, float sy) {
        m[0] *= sx;
        m[1] *= sx;
        m[4] *= sy;
        m[5] *= sy;
        return this;
    }

    /**
     * Replaces this matrix with {@code this × other}.
     *
     * @param other right-hand matrix
     * @return this matrix for chaining
     */
    public Matrix3x2 multiply(Matrix3x2 other) {
        multiplyInto(other.m, multiplyScratch);
        System.arraycopy(multiplyScratch, 0, m, 0, 16);
        return this;
    }

    /**
     * Sets this matrix to {@code parent × local}.
     *
     * @param parent parent transform
     * @param local local transform
     * @return this matrix for chaining
     */
    public Matrix3x2 combine(Matrix3x2 parent, Matrix3x2 local) {
        multiplyInto(parent.m, local.m, m);
        return this;
    }

    /**
     * Builds an orthographic projection matrix for Y-down world coordinates.
     *
     * @param left left edge
     * @param right right edge
     * @param worldTop smaller Y (top edge)
     * @param worldBottom larger Y (bottom edge)
     * @return orthographic projection matrix
     */
    public static Matrix3x2 ortho(float left, float right, float worldTop, float worldBottom) {
        Matrix3x2 matrix = new Matrix3x2();
        matrix.m[0] = 2f / (right - left);
        matrix.m[5] = 2f / (worldTop - worldBottom);
        matrix.m[10] = -1f;
        matrix.m[12] = -(right + left) / (right - left);
        matrix.m[13] = -(worldTop + worldBottom) / (worldTop - worldBottom);
        matrix.m[15] = 1f;
        return matrix;
    }

    /**
     * Builds a transform from position, rotation, scale, and origin pivot.
     *
     * @param position world position
     * @param rotation rotation in radians
     * @param scale non-uniform scale
     * @param origin local pivot before rotation/scale
     * @return composite transform matrix
     */
    public static Matrix3x2 fromTransform(Vector2f position, float rotation, Vector2f scale, Vector2f origin) {
        Matrix3x2 matrix = new Matrix3x2();
        matrix.translate(position.x, position.y);
        matrix.translate(origin.x, origin.y);
        matrix.rotate(rotation);
        matrix.scale(scale.x, scale.y);
        matrix.translate(-origin.x, -origin.y);
        return matrix;
    }

    /** Uploads this matrix to an OpenGL {@code mat4} uniform. */
    public void upload(int location) {
        FloatBuffer buffer = UPLOAD_BUFFER.get();
        buffer.clear();
        buffer.put(m);
        buffer.flip();
        GL20.glUniformMatrix4fv(location, false, buffer);
    }

    /** Returns a deep copy of this matrix. */
    public Matrix3x2 copy() {
        Matrix3x2 copy = new Matrix3x2();
        copy.set(this);
        return copy;
    }

    /** Returns the backing 16-element column-major array. */
    public float[] elements() {
        return m;
    }

    private void multiplyInto(float[] lhs, float[] out) {
        multiplyInto(m, lhs, out);
    }

    private static void multiplyInto(float[] lhs, float[] rhs, float[] out) {
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                out[col * 4 + row] =
                        lhs[row] * rhs[col * 4] +
                        lhs[4 + row] * rhs[col * 4 + 1] +
                        lhs[8 + row] * rhs[col * 4 + 2] +
                        lhs[12 + row] * rhs[col * 4 + 3];
            }
        }
    }
}
