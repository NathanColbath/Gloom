package org.llw.studio.project;

import java.nio.file.Path;

/**
 * Immutable summary of an opened LLW Studio project.
 *
 * @param root absolute project root directory
 * @param name display name from the project manifest
 * @param startupSceneRelative project-relative path to the scene loaded on play
 */
public record ProjectDescriptor(Path root, String name, String startupSceneRelative) {
    /**
     * @return absolute path to the startup scene file
     */
    public Path startupScenePath() {
        return root.resolve(startupSceneRelative);
    }
}
