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

import java.util.function.Consumer;

/**
 * Sprite batch for the lit 2D pipeline (adds per-vertex world position).
 */
public final class LitSpriteBatch {
    private static final Matrix3x2 IDENTITY_MVP = new Matrix3x2().identity();
    private static final int MAX_QUADS = 10_000;
    private static final int FLOATS_PER_VERTEX = 10;
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
    private final float[] scratchClip = new float[2];
    private final float[] scratchWorld = new float[2];
    private boolean active;

    public LitSpriteBatch() {
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
        GL15.glBufferData(
                GL15.GL_ARRAY_BUFFER,
                (long) MAX_QUADS * VERTICES_PER_QUAD * FLOATS_PER_VERTEX * Float.BYTES,
                GL15.GL_DYNAMIC_DRAW
        );
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);
        int stride = FLOATS_PER_VERTEX * Float.BYTES;
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, stride, 0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 2L * Float.BYTES);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, stride, 4L * Float.BYTES);
        GL20.glEnableVertexAttribArray(3);
        GL20.glVertexAttribPointer(3, 2, GL11.GL_FLOAT, false, stride, 8L * Float.BYTES);
        GL30.glBindVertexArray(0);
    }

    public void begin(ShaderProgram shader, BlendMode blendMode, Matrix3x2 viewProjection) {
        this.currentShader = shader;
        this.currentBlend = blendMode;
        this.viewProjection = viewProjection.copy();
        this.active = true;
    }

    public void drawQuad(
            Matrix3x2 model,
            float x0, float y0, float x1, float y1,
            float u0, float v0, float u1, float v1,
            Color color,
            Texture2d texture
    ) {
        if (!active) {
            throw new IllegalStateException("LitSpriteBatch.begin must be called before drawQuad");
        }
        if (quadCount >= MAX_QUADS) {
            throw new IllegalStateException("LitSpriteBatch capacity exceeded");
        }
        scratchMvp.set(viewProjection);
        if (model != null) {
            scratchMvp.multiply(model);
        }
        putVertex(scratchMvp, model, x0, y0, u0, v0, color);
        putVertex(scratchMvp, model, x1, y0, u1, v0, color);
        putVertex(scratchMvp, model, x1, y1, u1, v1, color);
        putVertex(scratchMvp, model, x0, y1, u0, v1, color);
        currentTexture = texture;
        quadCount++;
    }

    public void flush(GlStateTracker stateTracker, Consumer<ShaderProgram> uniformHook) {
        if (quadCount == 0 || currentShader == null) {
            reset();
            return;
        }
        stateTracker.useProgram(currentShader);
        stateTracker.applyBlendMode(currentBlend);
        if (uniformHook != null) {
            uniformHook.accept(currentShader);
        }
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
        GL30.glBindVertexArray(vao);
        GL11.glDrawElements(GL11.GL_TRIANGLES, quadCount * INDICES_PER_QUAD, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
        FrameDiagnostics.recordBatchQuads(quadCount);
        reset();
    }

    public boolean isActive() {
        return active;
    }

    public ShaderProgram currentShader() {
        return currentShader;
    }

    public BlendMode currentBlend() {
        return currentBlend;
    }

    public Texture2d currentTexture() {
        return currentTexture;
    }

    private static void transformPoint(Matrix3x2 matrix, float x, float y, float[] out) {
        float[] m = matrix.elements();
        out[0] = m[0] * x + m[4] * y + m[12];
        out[1] = m[1] * x + m[5] * y + m[13];
    }

    private void reset() {
        quadCount = 0;
        vertices.clear();
        active = false;
        currentTexture = null;
    }

    private void putVertex(
            Matrix3x2 mvp,
            Matrix3x2 model,
            float x,
            float y,
            float u,
            float v,
            Color color
    ) {
        transformPoint(mvp, x, y, scratchClip);
        if (model == null) {
            scratchWorld[0] = x;
            scratchWorld[1] = y;
        } else {
            transformPoint(model, x, y, scratchWorld);
        }
        vertices.put(scratchClip[0]).put(scratchClip[1]);
        vertices.put(u).put(v);
        vertices.put(color.r / 255f).put(color.g / 255f).put(color.b / 255f).put(color.a / 255f);
        vertices.put(scratchWorld[0]).put(scratchWorld[1]);
    }

}
