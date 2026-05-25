package org.llw.render.gl;

import java.nio.FloatBuffer;

import org.llw.render.core.Color;
import org.llw.math.matrix.Matrix3x2;
import org.llw.render.graphics.BlendMode;
import org.llw.render.graphics.ShaderProgram;
import org.llw.render.graphics.Texture2d;
import org.llw.util.log.FrameDiagnostics;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * GPU sprite batch that accumulates textured quads and draws them in a single indexed call.
 *
 * <p>Vertex layout per vertex: position (2), texcoord (2), color (4). The batch supports up to
 * {@value #MAX_QUADS} quads per flush. Call {@link #begin(ShaderProgram, BlendMode, Matrix3x2)}
 * before {@link #drawQuad} and {@link #flush(GlStateTracker)} to submit geometry.
 */
public final class SpriteBatch {
    private static final Matrix3x2 IDENTITY_MVP = new Matrix3x2().identity();
    private static final int MAX_QUADS = 10_000;
    private static final int FLOATS_PER_VERTEX = 8;
    private static final int VERTICES_PER_QUAD = 4;
    private static final int INDICES_PER_QUAD = 6;

    private final int vao;
    private final int vbo;
    private final int ebo;
    private final FloatBuffer vertices;
    private final int[] indices;
    private int quadCount;
    private Texture2d currentTexture;
    private ShaderProgram currentShader;
    private BlendMode currentBlend = BlendMode.ALPHA;
    private Matrix3x2 viewProjection = new Matrix3x2().identity();
    private final Matrix3x2 scratchMvp = new Matrix3x2();
    private float shaderTimeSeconds;
    private boolean active;
    private GlStateTracker flushStateTracker;

    /**
     * Allocates VAO, VBO, EBO, and precomputed index data for batched quad rendering.
     */
    public SpriteBatch() {
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        ebo = GL15.glGenBuffers();
        vertices = BufferUtils.createFloatBuffer(MAX_QUADS * VERTICES_PER_QUAD * FLOATS_PER_VERTEX);
        indices = new int[MAX_QUADS * INDICES_PER_QUAD];

        for (int i = 0; i < MAX_QUADS; i++) {
            int vertexOffset = i * VERTICES_PER_QUAD;
            int indexOffset = i * INDICES_PER_QUAD;
            indices[indexOffset] = vertexOffset;
            indices[indexOffset + 1] = vertexOffset + 1;
            indices[indexOffset + 2] = vertexOffset + 2;
            indices[indexOffset + 3] = vertexOffset + 2;
            indices[indexOffset + 4] = vertexOffset + 3;
            indices[indexOffset + 5] = vertexOffset;
        }

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) MAX_QUADS * VERTICES_PER_QUAD * FLOATS_PER_VERTEX * Float.BYTES, GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);

        int stride = FLOATS_PER_VERTEX * Float.BYTES;
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, stride, 0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 2L * Float.BYTES);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, stride, 4L * Float.BYTES);
        GL30.glBindVertexArray(0);
    }

    /**
     * Starts a new batch with the given shader, blend mode, and view-projection matrix.
     *
     * @param shader         program used when {@link #flush(GlStateTracker)} is called
     * @param blendMode      blending applied on flush
     * @param viewProjection matrix multiplied by each quad's model matrix before upload
     */
    public void begin(ShaderProgram shader, BlendMode blendMode, Matrix3x2 viewProjection) {
        this.currentShader = shader;
        this.currentBlend = blendMode;
        this.viewProjection = viewProjection.copy();
        this.active = true;
    }

    /**
     * Appends one axis-aligned textured quad to the batch in clip space after MVP transform.
     *
     * @param model    optional local model matrix
     * @param x0       local X of the first corner
     * @param y0       local Y of the first corner
     * @param x1       local X of the opposite corner
     * @param y1       local Y of the opposite corner
     * @param u0       normalized U at {@code (x0, y0)}
     * @param v0       normalized V at {@code (x0, y0)}
     * @param u1       normalized U at {@code (x1, y1)}
     * @param v1       normalized V at {@code (x1, y1)}
     * @param color    per-vertex tint
     * @param texture  texture sampled in the fragment shader; may be {@code null}
     * @throws IllegalStateException if {@link #begin} was not called or batch capacity is exceeded
     */
    public void drawQuad(
            Matrix3x2 model,
            float x0, float y0, float x1, float y1,
            float u0, float v0, float u1, float v1,
            Color color,
            Texture2d texture
    ) {
        if (!active) {
            throw new IllegalStateException("SpriteBatch.begin must be called before drawQuad");
        }
        if (quadCount >= MAX_QUADS) {
            if (flushStateTracker == null) {
                throw new IllegalStateException("SpriteBatch capacity exceeded");
            }
            flush(flushStateTracker);
            begin(currentShader, currentBlend, viewProjection);
        }

        scratchMvp.set(viewProjection);
        if (model != null) {
            scratchMvp.multiply(model);
        }

        putVertex(scratchMvp, x0, y0, u0, v0, color);
        putVertex(scratchMvp, x1, y0, u1, v0, color);
        putVertex(scratchMvp, x1, y1, u1, v1, color);
        putVertex(scratchMvp, x0, y1, u0, v1, color);
        currentTexture = texture;
        quadCount++;
    }

    /**
     * Uploads accumulated vertices and issues an indexed draw, then resets batch state.
     *
     * <p>No-op when the batch is empty or no shader was bound.
     *
     * @param stateTracker deduplicates GL program, texture, and blend state changes
     */
    /**
     * @param seconds value for {@code uTime} on custom shader programs
     */
    public void setShaderTimeSeconds(float seconds) {
        this.shaderTimeSeconds = seconds;
    }

    public void flush(GlStateTracker stateTracker) {
        if (quadCount == 0 || currentShader == null) {
            quadCount = 0;
            vertices.clear();
            active = false;
            currentTexture = null;
            return;
        }

        stateTracker.useProgram(currentShader);
        stateTracker.applyBlendMode(currentBlend);
        flushStateTracker = stateTracker;
        if (currentTexture != null) {
            stateTracker.bindTexture(0, currentTexture.id());
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        vertices.flip();
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertices);

        IDENTITY_MVP.upload(currentShader.mvpLocation());
        if (currentShader.textureLocation() >= 0) {
            GL20.glUniform1i(currentShader.textureLocation(), 0);
        }
        if (currentShader.useTextureLocation() >= 0) {
            GL20.glUniform1i(currentShader.useTextureLocation(), currentTexture == null ? 0 : 1);
        }
        currentShader.setUniform1f(currentShader.timeLocation(), shaderTimeSeconds);

        GL30.glBindVertexArray(vao);
        GL11.glDrawElements(GL11.GL_TRIANGLES, quadCount * INDICES_PER_QUAD, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);

        FrameDiagnostics.recordBatchQuads(quadCount);
        quadCount = 0;
        vertices.clear();
        active = false;
        currentTexture = null;
    }

    /**
     * Returns whether {@link #begin} has been called and the batch has not yet been flushed.
     *
     * @return {@code true} while accepting {@link #drawQuad} calls
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns the shader bound by the most recent {@link #begin}.
     *
     * @return active shader, or {@code null} if the batch is not active
     */
    public ShaderProgram currentShader() {
        return currentShader;
    }

    /**
     * Returns the blend mode bound by the most recent {@link #begin}.
     *
     * @return active blend mode
     */
    public BlendMode currentBlend() {
        return currentBlend;
    }

    /**
     * Returns the texture associated with the most recently appended quad.
     *
     * @return last quad's texture, or {@code null} if none
     */
    public Texture2d currentTexture() {
        return currentTexture;
    }

    /**
     * Deletes the VAO and buffer objects owned by this batch.
     */
    public void dispose() {
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL15.glDeleteBuffers(ebo);
    }

    private void putVertex(Matrix3x2 mvp, float x, float y, float u, float v, Color color) {
        float[] m = mvp.elements();
        float clipX = m[0] * x + m[4] * y + m[12];
        float clipY = m[1] * x + m[5] * y + m[13];
        float clipW = m[3] * x + m[7] * y + m[15];
        if (clipW != 0f && clipW != 1f) {
            clipX /= clipW;
            clipY /= clipW;
        }
        vertices.put(clipX).put(clipY).put(u).put(v)
                .put(color.rNorm()).put(color.gNorm()).put(color.bNorm()).put(color.aNorm());
    }
}
