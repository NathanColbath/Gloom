package org.llw.render.gl;

/**
 * Built-in GLSL sources for the default sprite, shape, and text pipelines.
 */
final class DefaultShaders {
    private DefaultShaders() {}

    static final String SPRITE_VERTEX = """
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

    static final String SPRITE_FRAGMENT = """
            #version 330 core
            in vec2 vTexCoord;
            in vec4 vColor;

            uniform sampler2D uTexture;
            uniform int uUseTexture;

            out vec4 fragColor;

            void main() {
                if (uUseTexture == 1) {
                    fragColor = texture(uTexture, vTexCoord) * vColor;
                } else {
                    fragColor = vColor;
                }
            }
            """;

    static final String SHAPE_VERTEX = """
            #version 330 core
            layout(location = 0) in vec2 aPosition;
            layout(location = 1) in vec2 aTexCoord;
            layout(location = 2) in vec4 aColor;

            uniform mat4 uMvp;

            out vec4 vColor;

            void main() {
                vColor = aColor;
                gl_Position = uMvp * vec4(aPosition, 0.0, 1.0);
            }
            """;

    static final String SHAPE_FRAGMENT = """
            #version 330 core
            in vec4 vColor;
            out vec4 fragColor;

            void main() {
                fragColor = vColor;
            }
            """;

    static final String TEXT_VERTEX = """
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

    static final String TEXT_FRAGMENT = """
            #version 330 core
            in vec2 vTexCoord;
            in vec4 vColor;

            uniform sampler2D uTexture;

            out vec4 fragColor;

            void main() {
                float alpha = texture(uTexture, vTexCoord).r;
                if (alpha < 0.01) {
                    discard;
                }
                fragColor = vec4(vColor.rgb, vColor.a * alpha);
            }
            """;
}
