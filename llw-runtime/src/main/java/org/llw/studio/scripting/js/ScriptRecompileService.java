package org.llw.studio.scripting.js;

import org.llw.studio.assets.MetaFile;
import org.llw.studio.project.StudioProjectLayout;
import org.llw.util.log.LogLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Re-bundles a single script source file when it changes on disk.
 */
public final class ScriptRecompileService {
    /**
     * @param guid    script asset GUID from meta, or {@code null} on failure before meta read
     * @param success {@code true} when bundling succeeded
     * @param message log line for the editor console
     * @param level   log severity
     */
    public record Result(String guid, boolean success, String message, LogLevel level) {
    }

    private ScriptRecompileService() {
    }

    /**
     * @param projectRoot project root directory
     * @param scriptPath  path to a {@code .js} or {@code .ts} source file
     * @return recompile result, or {@code null} when the path is not a script file
     */
    public static Result recompile(Path projectRoot, Path scriptPath) {
        if (scriptPath == null || !Files.isRegularFile(scriptPath)) {
            return null;
        }
        String name = scriptPath.getFileName().toString().toLowerCase();
        if (!name.endsWith(".js") && !name.endsWith(".ts")) {
            return null;
        }
        try {
            Path assetsRoot = StudioProjectLayout.assetsRoot(projectRoot);
            MetaFile.MetaData meta = MetaFile.read(projectRoot, assetsRoot, scriptPath);
            ScriptBundler.bundle(projectRoot, meta.guid, scriptPath);
            return new Result(meta.guid, true, "Recompiled script: " + scriptPath.getFileName(), LogLevel.INFO);
        } catch (IOException ex) {
            String guid = readGuidQuietly(projectRoot, scriptPath);
            if (guid != null) {
                ScriptDiagnostics.set(guid, new ScriptDiagnostic(guid, true, ex.getMessage()));
            }
            return new Result(guid, false,
                    "Script compile failed for " + scriptPath.getFileName() + ": " + ex.getMessage(),
                    LogLevel.ERROR);
        }
    }

    private static String readGuidQuietly(Path projectRoot, Path scriptPath) {
        try {
            Path assetsRoot = StudioProjectLayout.assetsRoot(projectRoot);
            return MetaFile.read(projectRoot, assetsRoot, scriptPath).guid;
        } catch (IOException ignored) {
            return null;
        }
    }
}
