package org.llw.render.backend;

import org.llw.math.matrix.Matrix3x2;
import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.render.graphics.BlendMode;
import org.llw.render.graphics.Font;
import org.llw.render.graphics.PrimitiveType;
import org.llw.render.graphics.ShaderProgram;
import org.llw.render.graphics.Texture2d;
import org.llw.render.graphics.Vertex;
import org.llw.render.gl.ShaderLibrary;
import org.llw.render.window.Window;

import java.util.function.Consumer;

/**
 * API-neutral 2D rendering facade (OpenGL or bgfx implementations).
 */
public interface RenderBackend {
    void initialize(Window window, BackendInitOptions options);

    ShaderLibrary shaderLibrary();

    RendererType rendererType();

    void setViewProjection(Matrix3x2 matrix);

    void setClearColor(Color color);

    void clear();

    void beginFrame(IntSize viewport);

    float shaderTimeSeconds();

    void endFrame();

    void drawTexturedQuad(
            Matrix3x2 model,
            float x0, float y0, float x1, float y1,
            float u0, float v0, float u1, float v1,
            Color color,
            Texture2d texture,
            ShaderProgram shader,
            BlendMode blendMode
    );

    void drawLitTexturedQuad(
            Matrix3x2 model,
            float x0, float y0, float x1, float y1,
            float u0, float v0, float u1, float v1,
            Color color,
            Texture2d texture,
            ShaderProgram shader,
            BlendMode blendMode
    );

    void drawLitTexturedQuad(
            Matrix3x2 model,
            float x0, float y0, float x1, float y1,
            float u0, float v0, float u1, float v1,
            Color color,
            Texture2d texture,
            ShaderProgram shader,
            BlendMode blendMode,
            long uniformBatchKey,
            Consumer<ShaderProgram> uniformHook
    );

    void flushSprites();

    void flushLitSprites(Consumer<ShaderProgram> uniformHook);

    void drawVertices(
            Matrix3x2 model,
            Vertex[] vertices,
            PrimitiveType primitiveType,
            ShaderProgram shader,
            BlendMode blendMode
    );

    void drawText(
            Matrix3x2 model,
            Font font,
            String text,
            float x,
            float y,
            Color color,
            ShaderProgram shader,
            BlendMode blendMode
    );

    Matrix3x2 combineMvp(Matrix3x2 model);

    void dispose();
}
