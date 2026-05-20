package org.llw.studio.shadergraph.editor;

import org.llw.math.geometry.RectF;
import org.llw.math.matrix.Matrix3x2;
import org.llw.render.core.Color;
import org.llw.render.core.IntSize;
import org.llw.render.graphics.DrawState;
import org.llw.render.graphics.OffscreenTarget;
import org.llw.render.graphics.ShaderProgram;
import org.llw.render.graphics.Texture2d;
import org.llw.render.gl.OpenGlBackend;
import org.llw.render.renderables.Sprite;
import org.llw.studio.shadergraph.compiler.ShaderGraphCompileResult;
import org.llw.studio.shadergraph.compiler.ShaderGraphCompiler;
import org.llw.studio.shadergraph.compiler.ShaderGraphTemplates;
import org.llw.studio.shadergraph.model.ShaderGraphDocument;
import org.llw.studio.shadergraph.model.ShaderNodeType;

/**
 * Compiles and renders shader graph previews into an offscreen target.
 */
public final class ShaderGraphPreviewService {
    private ShaderProgram previewProgram;
    private int compiledRevision = -1;
    private String compiledNodeId = "";
    private String lastError = "";

    public String lastError() {
        return lastError;
    }

    public void ensureCompiled(ShaderGraphEditorState state) {
        if (state.revision() == compiledRevision && compiledNodeId.equals(state.previewRootNodeId())) {
            return;
        }
        disposeProgram();
        ShaderGraphDocument document = state.document();
        String root = state.previewRootNodeId();
        ShaderGraphCompileResult result;
        if (root == null || root.isBlank()) {
            result = ShaderGraphCompiler.compileFull(document);
        } else if (state.document().nodeById(root) != null
                && state.document().nodeById(root).type == ShaderNodeType.FragmentOutput) {
            result = ShaderGraphCompiler.compileFull(document);
        } else {
            result = ShaderGraphCompiler.compilePreview(document, root);
        }
        compiledRevision = state.revision();
        compiledNodeId = root == null ? "" : root;
        if (!result.success()) {
            lastError = result.errorMessage();
            state.setLastCompileError(lastError);
            return;
        }
        try {
            previewProgram = ShaderProgram.fromSources(
                    ShaderGraphTemplates.SPRITE_VERTEX,
                    result.fragmentSource()
            );
            lastError = "";
            state.setLastCompileError("");
        } catch (RuntimeException e) {
            lastError = e.getMessage();
            state.setLastCompileError(lastError);
        }
    }

    public void render(
            OpenGlBackend backend,
            OffscreenTarget target,
            Texture2d texture,
            int width,
            int height
    ) {
        if (previewProgram == null || texture == null) {
            return;
        }
        target.clear(new Color(40, 40, 44, 255));
        float quadW = width * 0.85f;
        float quadH = height * 0.85f;
        float x = (width - quadW) * 0.5f;
        float y = (height - quadH) * 0.5f;

        Sprite sprite = new Sprite(texture);
        sprite.setTextureRect(new RectF(0f, 0f, 1f, 1f));
        sprite.setTint(Color.WHITE);
        Matrix3x2 transform = new Matrix3x2().identity();
        transform.translate(x, y);
        transform.scale(quadW / texture.size().width(), quadH / texture.size().height());

        DrawState drawState = DrawState.DEFAULT.withShader(previewProgram).withTransform(transform);
        target.draw(sprite, drawState);
        target.flush();
    }

    public void disposeProgram() {
        if (previewProgram != null) {
            previewProgram.destroy();
            previewProgram = null;
        }
        compiledRevision = -1;
    }

    public void dispose() {
        disposeProgram();
    }
}
