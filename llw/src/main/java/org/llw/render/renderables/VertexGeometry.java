package org.llw.render.renderables;

import org.llw.math.matrix.Matrix3x2;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.PrimitiveType;
import org.llw.render.graphics.Renderable;
import org.llw.render.graphics.Texture2d;
import org.llw.render.graphics.Vertex;
import org.llw.render.gl.OpenGlBackend;

/**
 * Custom {@link Renderable} backed by an arbitrary {@link Vertex} array and primitive type.
 *
 * <p>When a texture is set on this geometry or overridden in {@link DrawState}, vertices are
 * interpreted as groups of three corners and emitted as textured quads (with a final sprite
 * flush). Otherwise vertices are drawn immediately with {@link #getPrimitiveType()}.
 */
public final class VertexGeometry extends AbstractTransformable implements Renderable {
    private Vertex[] vertices = new Vertex[0];
    private PrimitiveType primitiveType = PrimitiveType.TRIANGLES;
    private Texture2d texture;

    /**
     * Returns the vertex array used when rendered.
     *
     * <p>Callers must not mutate the returned array unless they intend to change geometry in place.
     *
     * @return current vertices; never {@code null} but may be empty
     */
    public Vertex[] getVertices() {
        return vertices;
    }

    /**
     * Replaces the vertex array used when rendered.
     *
     * @param vertices new vertices; {@code null} is stored as an empty array
     */
    public void setVertices(Vertex[] vertices) {
        this.vertices = vertices == null ? new Vertex[0] : vertices;
    }

    /**
     * Returns the OpenGL primitive type used for untextured draws.
     *
     * @return primitive type passed to the backend when no texture is active
     */
    public PrimitiveType getPrimitiveType() {
        return primitiveType;
    }

    /**
     * Sets the OpenGL primitive type used for untextured draws.
     *
     * @param primitiveType primitive type for immediate vertex submission
     */
    public void setPrimitiveType(PrimitiveType primitiveType) {
        this.primitiveType = primitiveType;
    }

    /**
     * Returns the texture used for textured quad emission when {@link DrawState#texture()} is not set.
     *
     * @return optional texture, or {@code null} for untextured vertex draws
     */
    public Texture2d getTexture() {
        return texture;
    }

    /**
     * Assigns the texture used for textured quad emission when {@link DrawState#texture()} is not set.
     *
     * @param texture optional texture; {@code null} selects untextured vertex draws
     */
    public void setTexture(Texture2d texture) {
        this.texture = texture;
    }

    /**
     * Submits vertices to the backend, combining this geometry's transform with any transform
     * in {@code state}.
     *
     * <p>Does nothing if the vertex array is empty.
     *
     * @param backend OpenGL backend for sprite batching or immediate vertex draws
     * @param state per-draw blend mode, shader, optional texture override, and parent transform
     */
    @Override
    public void render(OpenGlBackend backend, DrawState state) {
        if (vertices.length == 0) {
            return;
        }
        Matrix3x2 model = combineModel(state);
        if (texture != null || state.texture() != null) {
            Texture2d activeTexture = state.texture() != null ? state.texture() : texture;
            for (int i = 0; i + 2 < vertices.length; i += 3) {
                Vertex a = vertices[i];
                Vertex b = vertices[i + 1];
                Vertex c = vertices[i + 2];
                backend.drawTexturedQuad(
                        model,
                        a.position.x, a.position.y, c.position.x, c.position.y,
                        a.texCoord.x, a.texCoord.y, c.texCoord.x, c.texCoord.y,
                        a.color,
                        activeTexture,
                        state.shader(),
                        state.blendMode()
                );
            }
            backend.flushSprites();
        } else {
            backend.drawVertices(model, vertices, primitiveType, state.shader(), state.blendMode());
        }
    }

    private Matrix3x2 combineModel(DrawState state) {
        Matrix3x2 model = getTransform();
        if (state.transform() != null) {
            Matrix3x2 combined = state.transform().copy();
            combined.multiply(model);
            return combined;
        }
        return model;
    }
}
