package org.llw.studio.scripting.setup;

import org.llw.studio.project.StudioProjectLayout;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Ensures TypeScript SDK files, {@code package.json}, and {@code tsconfig.json} exist in a project.
 */
public final class ScriptProjectGenerator {
    private static final String SDK_VERSION = "18";
    private static final String[] SDK_FILES = {
            "index.d.ts",
            "core.d.ts",
            "core.js",
            "keys.d.ts",
            "keys.js",
            "animation.d.ts",
            "components.d.ts",
            "physics2d.d.ts",
            "math.d.ts",
            "color.d.ts",
            "rect.d.ts",
            "scene.d.ts",
            "assets.d.ts",
            "camera.d.ts",
            "runtime-shim.js",
            "ui.d.ts",
            "package.json",
    };

    private ScriptProjectGenerator() {
    }

    /**
     * @param projectRoot project root directory
     * @throws IllegalStateException when SDK or config files cannot be written
     */
    public static void ensureProject(Path projectRoot) {
        try {
            Files.createDirectories(projectRoot.resolve("Assets/Scripts"));
            StudioProjectLayout.ensureMetadataDirs(projectRoot);
            Path sdkDir = projectRoot.resolve(".llw/sdk");
            Path versionFile = sdkDir.resolve("version.txt");
            boolean sdkCurrent = Files.exists(versionFile) && SDK_VERSION.equals(Files.readString(versionFile).trim());
            if (!sdkCurrent) {
                Files.createDirectories(sdkDir);
                for (String file : SDK_FILES) {
                    copyResource("scripting-sdk/" + file, sdkDir.resolve(file));
                }
                Files.writeString(versionFile, SDK_VERSION);
            }
            Files.deleteIfExists(projectRoot.resolve("jsconfig.json"));
            writeProjectFile(projectRoot.resolve("package.json"), """
                    {
                      "name": "llw-game-project",
                      "private": true,
                      "type": "module",
                      "devDependencies": {
                        "llw.core": "file:.llw/sdk",
                        "esbuild": "^0.25.0",
                        "typescript": "^5.8.0"
                      }
                    }
                    """);
            writeProjectFile(projectRoot.resolve("tsconfig.json"), """
                    {
                      "compilerOptions": {
                        "target": "ES2020",
                        "module": "ESNext",
                        "moduleResolution": "bundler",
                        "strict": true,
                        "noEmit": true,
                        "skipLibCheck": true,
                        "baseUrl": ".",
                        "paths": {
                          "llw.core": [".llw/sdk/core.d.ts"],
                          "@llw/studio": [".llw/sdk/core.d.ts"]
                        }
                      },
                      "include": ["Assets/Scripts/**/*.ts", ".llw/sdk/**/*.d.ts"]
                    }
                    """);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate scripting project files", ex);
        }
    }

    private static void writeProjectFile(Path path, String content) throws IOException {
        Files.writeString(path, content);
    }

    private static void copyResource(String resource, Path target) throws IOException {
        try (InputStream in = ScriptProjectGenerator.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Missing resource: " + resource);
            }
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
