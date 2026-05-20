package org.llw.studio.scripting.js;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.log.StudioLogSink;
import org.llw.studio.assets.MetaFile;
import org.llw.studio.project.StudioProjectLayout;
import org.llw.util.log.LogLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Bundles script sources to disk without loading Graal factories. Used at edit time and before play.
 */
public final class ScriptCompileService {
    private ScriptCompileService() {
    }

    /**
     * @param projectRoot project root
     * @param assets      asset database for GUID resolution
     * @param guids       script asset GUIDs to bundle
     * @param console     optional log sink for failures
     * @return {@code true} when every GUID bundled successfully (or the set was empty)
     */
    public static boolean ensureBundled(
            Path projectRoot,
            AssetDatabase assets,
            Set<String> guids,
            StudioLogSink console
    ) {
        if (guids == null || guids.isEmpty() || assets == null) {
            return true;
        }
        List<ScriptBundler.ScriptSource> stale = new ArrayList<>();
        for (String guid : guids) {
            StudioAsset asset = assets.get(guid);
            if (asset == null || asset.type() != AssetType.SCRIPT || asset.isFolder()) {
                continue;
            }
            if (!Files.isRegularFile(asset.path())) {
                continue;
            }
            Path output = StudioProjectLayout.resolveScriptCachePath(projectRoot, guid);
            if (!ScriptBundler.isCacheFresh(asset.path(), output)) {
                stale.add(new ScriptBundler.ScriptSource(guid, asset.path()));
            }
        }
        if (stale.isEmpty()) {
            return true;
        }
        bundleSources(projectRoot, stale, console);
        return stale.stream().noneMatch(source -> {
            ScriptDiagnostic diagnostic = ScriptDiagnostics.get(source.guid());
            return diagnostic != null && diagnostic.error();
        });
    }

    /**
     * Bundles the given script GUIDs, running at most one TypeScript project check per batch.
     *
     * @param projectRoot project root
     * @param assets      asset database
     * @param guids       script GUIDs to bundle (missing assets are skipped)
     * @param console     optional log sink
     */
    public static void bundleGuids(
            Path projectRoot,
            AssetDatabase assets,
            Set<String> guids,
            StudioLogSink console
    ) {
        if (guids == null || guids.isEmpty() || assets == null) {
            return;
        }
        List<ScriptBundler.ScriptSource> sources = new ArrayList<>();
        for (String guid : guids) {
            StudioAsset asset = assets.get(guid);
            if (asset == null || asset.type() != AssetType.SCRIPT || asset.isFolder()) {
                continue;
            }
            if (!Files.isRegularFile(asset.path())) {
                continue;
            }
            sources.add(new ScriptBundler.ScriptSource(guid, asset.path()));
        }
        bundleSources(projectRoot, sources, console);
    }

    /**
     * @param projectRoot project root
     * @param scriptGuid  script asset GUID
     * @return path to the cached bundled {@code .js} file
     */
    public static Path bundledPath(Path projectRoot, String scriptGuid) {
        return StudioProjectLayout.scriptCachePath(projectRoot, scriptGuid);
    }

    /**
     * Bundles every {@code .ts} / {@code .js} file under {@code Assets/Scripts} using on-disk metadata GUIDs.
     *
     * @throws IOException when TypeScript checking fails for the project
     */
    public static void compileAllScriptsInProject(Path projectRoot, StudioLogSink console) throws IOException {
        Path assetsRoot = projectRoot.resolve("Assets");
        Path scriptsDir = assetsRoot.resolve("Scripts");
        if (!Files.isDirectory(scriptsDir)) {
            return;
        }
        List<ScriptBundler.ScriptSource> sources = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(scriptsDir)) {
            for (Path path : walk.filter(Files::isRegularFile).toList()) {
                String name = path.getFileName().toString().toLowerCase();
                if (!name.endsWith(".ts") && !name.endsWith(".js")) {
                    continue;
                }
                MetaFile.MetaData meta = MetaFile.read(projectRoot, assetsRoot, path);
                sources.add(new ScriptBundler.ScriptSource(meta.guid, path));
            }
        }
        bundleSources(projectRoot, sources, console);
    }

    private static void bundleSources(
            Path projectRoot,
            List<ScriptBundler.ScriptSource> sources,
            StudioLogSink console
    ) {
        if (sources.isEmpty()) {
            return;
        }
        boolean needsTypeCheck = false;
        for (ScriptBundler.ScriptSource source : sources) {
            if (isTypeScriptSource(source.path()) && !ScriptBundler.isCacheFresh(
                    source.path(),
                    StudioProjectLayout.resolveScriptCachePath(projectRoot, source.guid())
            )) {
                needsTypeCheck = true;
                break;
            }
        }
        if (needsTypeCheck) {
            try {
                ScriptBundler.runTypeCheck(projectRoot);
            } catch (IOException ex) {
                String message = ex.getMessage();
                for (ScriptBundler.ScriptSource source : sources) {
                    if (isTypeScriptSource(source.path())) {
                        ScriptDiagnostics.set(source.guid(), new ScriptDiagnostic(source.guid(), true, message));
                        log(console, LogLevel.ERROR, "Script compile failed for " + source.path().getFileName() + ": " + message);
                    }
                }
                return;
            }
        }
        for (ScriptBundler.ScriptSource source : sources) {
            if (!Files.isRegularFile(source.path())) {
                continue;
            }
            Path output = StudioProjectLayout.resolveScriptCachePath(projectRoot, source.guid());
            if (ScriptBundler.isCacheFresh(source.path(), output)) {
                continue;
            }
            try {
                ScriptBundler.bundle(projectRoot, source.guid(), source.path(), true);
            } catch (Exception ex) {
                String message = "Script compile failed for " + source.path().getFileName() + ": " + ex.getMessage();
                ScriptDiagnostics.set(source.guid(), new ScriptDiagnostic(source.guid(), true, message));
                log(console, LogLevel.ERROR, message);
            }
        }
    }

    private static boolean isTypeScriptSource(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".ts");
    }

    private static void log(StudioLogSink console, LogLevel level, String message) {
        if (console != null) {
            console.append(level, message);
        }
    }
}
