package org.llw.studio.scripting.setup;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.project.StudioProjectLayout;



/**

 * Creates new TypeScript script assets from the default {@code Script} template.

 */

public final class ScriptTemplateGenerator {

    private static final String DEFAULT_EXTENSION = ".ts";



    private ScriptTemplateGenerator() {

    }



    /**

     * @param projectRoot project root directory

     * @param assets      asset database to refresh after creation

     * @param baseName    desired class/file base name

     * @return GUID of the new script asset, or empty string when not indexed

     * @throws IOException when the file cannot be written

     */

    public static String createNewScript(Path projectRoot, AssetDatabase assets, String baseName) throws IOException {

        return createNewScript(projectRoot, assets, baseName, defaultScriptsDir(projectRoot));

    }



    /**

     * @param projectRoot project root directory

     * @param assets      asset database to refresh after creation

     * @param baseName    desired class/file base name

     * @param targetDir   directory under the project root; falls back to {@code Assets/Scripts}

     * @return GUID of the new script asset, or empty string when not indexed

     * @throws IOException when the file cannot be written

     */

    public static String createNewScript(Path projectRoot, AssetDatabase assets, String baseName, Path targetDir)

            throws IOException {

        Path scriptsDir = resolveTargetDir(projectRoot, targetDir);

        Files.createDirectories(scriptsDir);

        String safeName = sanitize(baseName);

        Path scriptPath = uniquePath(scriptsDir, safeName + DEFAULT_EXTENSION);

        String className = stripExtension(scriptPath.getFileName().toString());

        Files.writeString(scriptPath, template(className));

        Path assetsRoot = StudioProjectLayout.assetsRoot(projectRoot);
        MetaFile.read(projectRoot, assetsRoot, scriptPath);

        assets.refresh();

        return assets.getByPath(scriptPath) == null ? "" : assets.getByPath(scriptPath).guid();

    }



    private static Path defaultScriptsDir(Path projectRoot) {

        return projectRoot.resolve("Assets/Scripts");

    }



    private static Path resolveTargetDir(Path projectRoot, Path targetDir) {

        if (targetDir == null) {

            return defaultScriptsDir(projectRoot);

        }

        Path normalized = targetDir.toAbsolutePath().normalize();

        Path root = projectRoot.toAbsolutePath().normalize();

        if (!normalized.startsWith(root)) {

            return defaultScriptsDir(projectRoot);

        }

        return normalized;

    }



    private static Path uniquePath(Path dir, String fileName) {

        Path candidate = dir.resolve(fileName);

        if (!Files.exists(candidate)) {

            return candidate;

        }

        String extension = fileName.endsWith(DEFAULT_EXTENSION)

                ? DEFAULT_EXTENSION

                : fileName.substring(fileName.lastIndexOf('.'));

        String base = fileName.substring(0, fileName.length() - extension.length());

        int i = 1;

        while (Files.exists(dir.resolve(base + i + extension))) {

            i++;

        }

        return dir.resolve(base + i + extension);

    }



    private static String sanitize(String name) {

        String trimmed = name == null || name.isBlank() ? "NewScript" : name.trim();

        return trimmed.replaceAll("[^A-Za-z0-9_]", "");

    }



    private static String stripExtension(String name) {

        int dot = name.lastIndexOf('.');

        return dot >= 0 ? name.substring(0, dot) : name;

    }



    private static String template(String className) {

        return """

                import * as core from "llw.core";

                
                export default class %s extends core.Script {
                  // Inspector-visible (public instance field)
                  speed = 5;

                  // Runtime-only — not shown in the Inspector
                  private elapsed = 0;

                  start(): void {
                  }

                  update(): void {
                    this.elapsed += core.Time.deltaTime;
                  }

                }

                """.formatted(className);

    }

}

