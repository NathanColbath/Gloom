package org.llw.render.gl;

import java.nio.FloatBuffer;

import org.llw.math.matrix.Matrix3x2;
import org.llw.render.graphics.BlendMode;
import org.llw.render.graphics.PrimitiveType;
import org.llw.render.graphics.ShaderProgram;
import org.llw.render.graphics.Vertex;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Immediate-mode renderer for untextured 2D vertex arrays.
 *
 * <p>Vertices are uploaded to a dynamic VBO each draw. The MVP matrix and blend mode are applied
 * through the supplied {@link GlStateTracker}.
 */
public final class ShapeRenderer {
    private final int vao;
    private final int vbo;

    /**
     * Allocates a VAO and VBO configured for position, texcoord, and color attributes.
     */
    public ShapeRenderer() {
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        int stride = 8 * Float.BYTES;
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, stride, 0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 2L * Float.BYTES);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, stride, 4L * Float.BYTES);
        GL30.glBindVertexArray(0);
    }

    /**
     * Uploads vertices and issues a single draw call with the given primitive topology.
     *
     * @param stateTracker deduplicates GL program and blend state changes
     * @param shader       program whose MVP uniform receives {@code mvp}
     * @param blendMode    blending applied for this draw
     * @param mvp          model-view-projection matrix uploaded to the shader
     * @param vertices     vertex attributes to draw
     * @param primitiveType OpenGL primitive type mapped from {@link PrimitiveType}
     */
    public void draw(
            GlStateTracker stateTracker,
            ShaderProgram shader,
            BlendMode blendMode,
            Matrix3x2 mvp,
            Vertex[] vertices,
            PrimitiveType primitiveType
    ) {
        stateTracker.useProgram(shader);
        stateTracker.applyBlendMode(blendMode);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length * 8);
        for (Vertex vertex : vertices) {
            buffer.put(vertex.position.x).put(vertex.position.y)
                    .put(vertex.texCoord.x).put(vertex.texCoord.y)
                    .put(vertex.color.rNorm()).put(vertex.color.gNorm())
                    .put(vertex.color.bNorm()).put(vertex.color.aNorm());
        }
        buffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STREAM_DRAW);
        mvp.upload(shader.mvpLocation());
        if (shader.useTextureLocation() >= 0) {
            GL20.glUniform1i(shader.useTextureLocation(), 0);
        }

        GL30.glBindVertexArray(vao);
        GL11.glDrawArrays(toGlPrimitive(primitiveType), 0, vertices.length);
        GL30.glBindVertexArray(0);
    }

    /**
     * Deletes the VAO and VBO owned by this renderer.
     */
    public void dispose() {
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
    }

    private static int toGlPrimitive(PrimitiveType type) {
        return switch (type) {
            case POINTS -> GL11.GL_POINTS;
            case LINES -> GL11.GL_LINES;
            case LINE_STRIP -> GL11.GL_LINE_STRIP;
            case TRIANGLES -> GL11.GL_TRIANGLES;
            case TRIANGLE_FAN -> GL11.GL_TRIANGLE_FAN;
            case TRIANGLE_STRIP -> GL11.GL_TRIANGLE_STRIP;
        };
    }
}
