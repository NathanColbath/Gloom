package org.llw.render.gl;

/**
 * Built-in GLSL sources for the default sprite, shape, and text pipelines.
 */
public final class DefaultShaders {
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

    public static final String LIT_SPRITE_VERTEX = """
            #version 330 core
            layout(location = 0) in vec2 aPosition;
            layout(location = 1) in vec2 aTexCoord;
            layout(location = 2) in vec4 aColor;
            layout(location = 3) in vec2 aWorldPos;

            uniform mat4 uMvp;

            out vec2 vTexCoord;
            out vec4 vColor;
            out vec2 vWorldPos;
            out float vRotation;

            uniform float uSpriteRotation;

            void main() {
                vTexCoord = aTexCoord;
                vColor = aColor;
                vWorldPos = aWorldPos;
                vRotation = uSpriteRotation;
                gl_Position = uMvp * vec4(aPosition, 0.0, 1.0);
            }
            """;

    public static final String LIT_SPRITE_FRAGMENT = """
            #version 330 core
            in vec2 vTexCoord;
            in vec4 vColor;
            in vec2 vWorldPos;
            in float vRotation;

            uniform sampler2D uTexture;
            uniform int uUseTexture;
            uniform sampler2D uNormalMap;
            uniform int uUseNormalMap;
            uniform sampler2D uLightMap;
            uniform int uUseLightMap;
            uniform vec4 uLightMapTransform;
            uniform vec4 uAmbient;
            uniform float uLightData[64];

            out vec4 fragColor;

            vec3 rotateNormal(vec3 n, float rad) {
                float c = cos(rad);
                float s = sin(rad);
                return vec3(n.x * c - n.y * s, n.x * s + n.y * c, n.z);
            }

            float distanceAttenuation(float dist, float range) {
                float t = dist / max(range, 0.001);
                return 1.0 / (1.0 + t * t);
            }

            float spotConeAttenuation(vec2 worldPos, vec2 lightPos, vec2 dir, float innerCos, float outerCos) {
                vec2 toFrag = worldPos - lightPos;
                float len2 = dot(toFrag, toFrag);
                if (len2 < 1e-6) {
                    return 1.0;
                }
                float cosTheta = dot(toFrag * inversesqrt(len2), normalize(dir));
                return smoothstep(outerCos, innerCos, cosTheta);
            }

            float diffuseTerm(vec3 normal, vec3 lightDir, bool useNormalDiffuse) {
                if (!useNormalDiffuse) {
                    return 1.0;
                }
                return max(dot(normal, lightDir), 0.0);
            }

            vec3 evalLights(vec3 normal, vec2 worldPos, bool useNormalDiffuse) {
                vec3 lit = vec3(0.0);
                int directionalCount = int(uLightData[0]);
                int pointCount = int(uLightData[1]);
                int spotCount = int(uLightData[2]);
                int idx = 4;
                for (int i = 0; i < directionalCount; i++) {
                    vec3 dir = normalize(vec3(uLightData[idx], uLightData[idx + 1], 0.0));
                    vec3 color = vec3(uLightData[idx + 2], uLightData[idx + 3], uLightData[idx + 4]);
                    float intensity = uLightData[idx + 5];
                    idx += 6;
                    float ndl = diffuseTerm(normal, -dir, useNormalDiffuse);
                    lit += color * intensity * ndl;
                }
                for (int i = 0; i < pointCount; i++) {
                    vec2 lpos = vec2(uLightData[idx], uLightData[idx + 1]);
                    vec3 color = vec3(uLightData[idx + 2], uLightData[idx + 3], uLightData[idx + 4]);
                    float intensity = uLightData[idx + 5];
                    float range = max(uLightData[idx + 6], 0.001);
                    idx += 7;
                    vec2 toLight = lpos - worldPos;
                    float dist = length(toLight);
                    float atten = distanceAttenuation(dist, range);
                    vec3 ldir = dist > 1e-4 ? normalize(vec3(toLight, 0.0)) : vec3(0.0, 0.0, 1.0);
                    float ndl = diffuseTerm(normal, ldir, useNormalDiffuse);
                    lit += color * intensity * ndl * atten;
                }
                for (int i = 0; i < spotCount; i++) {
                    vec2 lpos = vec2(uLightData[idx], uLightData[idx + 1]);
                    vec3 color = vec3(uLightData[idx + 2], uLightData[idx + 3], uLightData[idx + 4]);
                    float intensity = uLightData[idx + 5];
                    float range = max(uLightData[idx + 6], 0.001);
                    vec2 dir = vec2(uLightData[idx + 7], uLightData[idx + 8]);
                    float innerCos = uLightData[idx + 9];
                    float outerCos = uLightData[idx + 10];
                    idx += 11;
                    vec2 toLight = lpos - worldPos;
                    float dist = length(toLight);
                    float atten = distanceAttenuation(dist, range)
                            * spotConeAttenuation(worldPos, lpos, dir, innerCos, outerCos);
                    vec3 ldir = dist > 1e-4 ? normalize(vec3(toLight, 0.0)) : vec3(0.0, 0.0, 1.0);
                    float ndl = diffuseTerm(normal, ldir, useNormalDiffuse);
                    lit += color * intensity * ndl * atten;
                }
                return lit;
            }

            void main() {
                vec4 albedo = uUseTexture == 1 ? texture(uTexture, vTexCoord) * vColor : vColor;
                if (albedo.a < 0.01) {
                    discard;
                }
                vec3 normal = vec3(0.0, 0.0, 1.0);
                bool useNormalDiffuse = false;
                if (uUseNormalMap == 1) {
                    normal = texture(uNormalMap, vTexCoord).rgb * 2.0 - 1.0;
                    normal = rotateNormal(normal, vRotation);
                    useNormalDiffuse = true;
                }
                vec3 diffuse = evalLights(normal, vWorldPos, useNormalDiffuse);
                vec3 ambient = uAmbient.rgb * uAmbient.a;
                if (uUseLightMap == 1) {
                    vec2 lmUv = (vWorldPos - uLightMapTransform.xy) / max(uLightMapTransform.zw, vec2(0.001));
                    vec3 baked = texture(uLightMap, lmUv).rgb;
                    ambient += baked;
                }
                vec3 color = albedo.rgb * (ambient + diffuse);
                fragColor = vec4(color, albedo.a);
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
