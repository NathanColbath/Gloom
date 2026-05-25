package org.llw.studio.build;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.log.StudioLogSink;
import org.llw.studio.project.ProjectDescriptor;
import org.llw.studio.scripting.js.ScriptCompileService;
import org.llw.studio.scripting.js.ScriptDiagnostics;
import org.llw.studio.scripting.js.ScriptDiagnostic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates scanning, script compilation, pack writing, and player staging.
 */
public final class ProjectBuildService {
    private ProjectBuildService() {
    }

    /**
     * @param project     open project descriptor
     * @param assets      asset database for the project
     * @param settings    build settings
     * @param console     optional log sink for script errors
     * @param progress    optional progress callback
     * @return build result with output paths
     */
    public static BuildResult build(
            ProjectDescriptor project,
            AssetDatabase assets,
            BuildSettings settings,
            StudioLogSink console,
            BuildProgress progress
    ) {
        List<String> log = new ArrayList<>();
        Path outputRoot = resolveOutputRoot(project, settings);
        try {
            report(progress, 0.02f, "Validating project...");
            validate(project, settings, outputRoot);

            // Stages: validate → scan scene refs → compile scripts → write packs → stage player → package.
            report(progress, 0.08f, "Scanning scenes for referenced assets...");
            BuildAssetSet assetSet = BuildAssetScanner.scan(project.root(), assets);
            log.addAll(assetSet.scanLog());

            report(progress, 0.18f, "Compiling scripts...");
            ScriptCompileService.bundleGuids(
                    project.root(),
                    assets,
                    assetSet.scriptGuids(),
                    console
            );
            if (hasScriptErrors(assetSet.scriptGuids())) {
                return BuildResult.failure(outputRoot, log, "Script compilation failed. Fix errors in the Console.");
            }

            Path contentDir = outputRoot.resolve("content");
            Path stagingDir = outputRoot.resolve(".build-staging");
            deleteRecursively(stagingDir); // Fresh staging each build; holds compiled shaders/meta sidecars.
            Files.createDirectories(stagingDir);

            report(progress, 0.35f, "Writing asset packs...");
            TypedPackWriter.writeAll(project.root(), assets, assetSet, contentDir, stagingDir);

            String startupSceneGuid = resolveStartupSceneGuid(project, assets);
            GameManifest manifest = createManifest(settings, startupSceneGuid, assetSet);
            Files.writeString(contentDir.resolve("game.manifest.json"), manifest.toJson(), StandardCharsets.UTF_8);
            log.add("Wrote game.manifest.json");

            report(progress, 0.62f, "Staging player runtime...");
            PlayerStaging.stage(project.root(), outputRoot, settings, contentDir, log);

            Path iconIco = null;
            try {
                iconIco = BuildIconResolver.resolve(project.root(), assets, settings.iconAssetGuid(), stagingDir);
                if (iconIco != null) {
                    log.add("Prepared application icon: " + iconIco);
                }
            } catch (IOException ex) {
                log.add("Warning: icon preparation failed: " + ex.getMessage());
            }

            report(progress, 0.72f, "Packaging Windows executable...");
            Path jpackageExe = JpackageRunner.packageWindows(outputRoot, settings, iconIco, log);
            if (jpackageExe != null) {
                log.add("Created executable: " + jpackageExe);
            } else {
                log.add("jpackage unavailable; folder launcher will be used.");
            }

            report(progress, 0.86f, "Writing launcher...");
            BuildLauncherWriter.writeBat(outputRoot, settings, iconIco, log);
            BuildLauncherWriter.ensureAppRuntime(outputRoot, settings, log);

            Path exe = resolvePackagedExe(outputRoot, settings);
            Path shortcut = exe == null
                    ? null
                    : BuildShortcutWriter.createShortcut(outputRoot, settings, exe, iconIco, log);
            Path executable = shortcut != null ? shortcut : (exe != null ? exe : outputRoot);
            if (exe == null) {
                log.add("Executable not found; shortcut was not created.");
            }

            deleteRecursively(stagingDir); // Sidecar files are copied into packs; remove temp tree from output.
            report(progress, 1f, "Build complete");
            return BuildResult.success(outputRoot, contentDir, executable, log);
        } catch (Exception ex) {
            log.add("Build failed: " + ex.getMessage());
            return BuildResult.failure(outputRoot, log, ex.getMessage());
        }
    }

    private static Path resolvePackagedExe(Path outputRoot, BuildSettings settings) {
        String product = settings.productName();
        Path exe = outputRoot.resolve(product).resolve(product + ".exe");
        return Files.isRegularFile(exe) ? exe : null;
    }

    private static void validate(ProjectDescriptor project, BuildSettings settings, Path outputRoot) throws IOException {
        if (project == null || project.root() == null) {
            throw new IOException("No project is open.");
        }
        if (settings.productName().isBlank()) {
            throw new IOException("Product name is required.");
        }
        Files.createDirectories(outputRoot.getParent() == null ? outputRoot : outputRoot.getParent());
        Path startupScene = project.startupScenePath();
        if (!Files.isRegularFile(startupScene)) {
            throw new IOException("Startup scene not found: " + project.startupSceneRelative());
        }
    }

    private static Path resolveOutputRoot(ProjectDescriptor project, BuildSettings settings) {
        String product = sanitizeFileName(settings.productName());
        if (settings.outputDirectory() != null && !settings.outputDirectory().isBlank()) {
            return Path.of(settings.outputDirectory()).resolve(product).normalize();
        }
        return project.root().resolve("Builds").resolve(product).normalize();
    }

    private static String resolveStartupSceneGuid(ProjectDescriptor project, AssetDatabase assets) throws IOException {
        Path startupScene = project.startupScenePath();
        StudioAsset startupAsset = assets.getByPath(startupScene);
        if (startupAsset != null) {
            return startupAsset.guid();
        }
        return BuildAssetPaths.guidForPath(project.root(), startupScene);
    }

    private static GameManifest createManifest(BuildSettings settings, String startupSceneGuid, BuildAssetSet assetSet) {
        List<String> packs = new ArrayList<>();
        for (BuildPackCategory category : BuildPackCategory.values()) {
            if (!assetSet.assets(category).isEmpty()) {
                packs.add(category.fileName());
            }
        }
        GameManifest.WindowSettings window = new GameManifest.WindowSettings(
                settings.windowTitle().isBlank() ? settings.productName() : settings.windowTitle(),
                settings.windowWidth(),
                settings.windowHeight(),
                settings.vsync()
        );
        return new GameManifest(
                1,
                settings.productName(),
                settings.version(),
                startupSceneGuid,
                packs.toArray(String[]::new),
                window
        );
    }

    private static boolean hasScriptErrors(java.util.Set<String> scriptGuids) {
        for (String guid : scriptGuids) {
            ScriptDiagnostic diagnostic = ScriptDiagnostics.get(guid);
            if (diagnostic != null && diagnostic.error()) {
                return true;
            }
        }
        return false;
    }

    private static String sanitizeFileName(String value) {
        return value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    private static void report(BuildProgress progress, float fraction, String message) {
        if (progress != null) {
            progress.report(fraction, message);
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (var walk = Files.walk(path)) {
            walk.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        }
    }
}
