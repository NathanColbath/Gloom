package org.llw.studio.project;

import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.setup.ScriptProjectGenerator;
import org.llw.studio.serialization.ProjectSerializer;
import org.llw.studio.serialization.SceneSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Creates a new on-disk project layout with default assets, script stub, scene, and manifest.
 */
public final class ProjectScaffolder {
    private ProjectScaffolder() {
    }

    /**
     * Creates folder structure, starter script, main scene, and {@code *.llwproj} under {@code parentDirectory}.
     *
     * @param parentDirectory folder that will contain the new project directory
     * @param projectName human-readable name; sanitized for use as the directory and manifest base name
     * @return descriptor for the created project
     * @throws IOException if the name is blank, the path conflicts, or files cannot be written
     */
    public static ProjectDescriptor create(Path parentDirectory, String projectName) throws IOException {
        if (projectName == null || projectName.isBlank()) {
            throw new IOException("Project name is required");
        }
        String sanitized = sanitizeName(projectName.trim());
        Path projectRoot = parentDirectory.resolve(sanitized).toAbsolutePath().normalize();
        if (Files.exists(projectRoot) && !Files.isDirectory(projectRoot)) {
            throw new IOException("Path already exists and is not a directory: " + projectRoot);
        }
        Files.createDirectories(projectRoot.resolve("Assets/Scripts"));
        Files.createDirectories(projectRoot.resolve("Scenes"));
        StudioProjectLayout.ensureMetadataDirs(projectRoot);
        Path scriptPath = projectRoot.resolve("Assets/Scripts/PlayerController.ts");
        if (!Files.exists(scriptPath)) {
            Files.writeString(scriptPath, """
                    import * as core from "llw.core";

                    export default class PlayerController extends core.Script {
                      speed = 5;

                      update(): void {
                        const position = this.transform.position;
                        if (core.Input.getKey(core.Keys.VK_A) || core.Input.getKey(core.Keys.VK_LEFT)) {
                          position.x -= this.speed;
                        }
                        if (core.Input.getKey(core.Keys.VK_D) || core.Input.getKey(core.Keys.VK_RIGHT)) {
                          position.x += this.speed;
                        }
                        if (core.Input.getKey(core.Keys.VK_W) || core.Input.getKey(core.Keys.VK_UP)) {
                          position.y -= this.speed;
                        }
                        if (core.Input.getKey(core.Keys.VK_S) || core.Input.getKey(core.Keys.VK_DOWN)) {
                          position.y += this.speed;
                        }
                        if (core.Input.getKeyDown(core.Keys.VK_SPACE)) {
                          core.Logger.log("Jump");
                        }
                      }
                    }
                    """);
        }
        ScriptProjectGenerator.ensureProject(projectRoot);
        Path scenePath = projectRoot.resolve("Scenes/Main.scene.json");
        if (!Files.exists(scenePath)) {
            SceneSerializer.save(new Scene(), scenePath);
        }
        String startupScene = "Scenes/Main.scene.json";
        ProjectSerializer.save(projectRoot, sanitized, startupScene);
        return new ProjectDescriptor(projectRoot, sanitized, startupScene);
    }

    private static String sanitizeName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
