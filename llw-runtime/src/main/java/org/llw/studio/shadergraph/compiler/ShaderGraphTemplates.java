package org.llw.studio.shadergraph.compiler;

/**
 * Fixed GLSL templates for sprite-compatible shader graphs.
 */
public final class ShaderGraphTemplates {
    public static final String SPRITE_VERTEX = """
            #version 330 core
            layout(location = 0) in vec2 aPosition;
            layout(location = 1) in vec2 aTexCoord;
            layout(location = 2) in vec4 aColor;

            uniform mat4 uMvp;

            out vec2 vTexCoord;
            out vec4 vColor;

            void main() {
                vTexCoord = aTexCoord;
                vColor = aColor;
                gl_Position = uMvp * vec4(aPosition, 0.0, 1.0);
            }
            """;

    private static final String FRAGMENT_HEADER = """
            #version 330 core
            in vec2 vTexCoord;
            in vec4 vColor;

            uniform sampler2D uTexture;
            uniform int uUseTexture;
            uniform float uTime;

            out vec4 fragColor;

            """;

    private ShaderGraphTemplates() {
    }

    public static String wrapFragmentBody(String body) {
        return FRAGMENT_HEADER + "void main() {\n" + body + "}\n";
    }
}
