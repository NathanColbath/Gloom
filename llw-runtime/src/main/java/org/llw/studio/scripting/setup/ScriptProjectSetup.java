package org.llw.studio.scripting.setup;

import org.llw.studio.log.StudioLogSink;
import org.llw.studio.scripting.js.ScriptCompileService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Installs npm dependencies and compiles scripts after a new project is scaffolded.
 */
public final class ScriptProjectSetup {
    @FunctionalInterface
    public interface Progress {
        void report(float fraction, String message);
    }

    private ScriptProjectSetup() {
    }

    /**
     * Runs {@code npm install} and bundles every script under {@code Assets/Scripts}.
     *
     * @param projectRoot project root (must already contain {@code package.json})
     * @param console     optional log sink for compile errors
     * @param progress    optional progress callback ({@code fraction} in {@code [0, 1]})
     * @throws IOException when install or compile fails
     */
    public static void prepareNewProject(
            Path projectRoot,
            StudioLogSink console,
            Progress progress
    ) throws IOException {
        report(progress, 0.05f, "Preparing script tooling...");
        ScriptProjectGenerator.ensureProject(projectRoot);
        report(progress, 0.15f, "Installing npm dependencies...");
        npmInstall(projectRoot);
        report(progress, 0.55f, "Compiling scripts...");
        ScriptCompileService.compileAllScriptsInProject(projectRoot, console);
        report(progress, 1f, "Done");
    }

    /**
     * @param projectRoot project root with {@code package.json}
     */
    public static void npmInstall(Path projectRoot) throws IOException {
        List<String> command = new ArrayList<>();
        command.add(isWindows() ? "npm.cmd" : "npm");
        command.add("install");
        command.add("--no-fund");
        command.add("--no-audit");
        runCommand(projectRoot, command, "npm install failed");
    }

    private static void report(Progress progress, float fraction, String message) {
        if (progress != null) {
            progress.report(fraction, message);
        }
    }

    private static void runCommand(Path projectRoot, List<String> command, String failurePrefix) throws IOException {
        try {
            Process process = new ProcessBuilder(command)
                    .directory(projectRoot.toFile())
                    .redirectErrorStream(true)
                    .start();
            String outputText;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                outputText = reader.lines().reduce("", (a, b) -> a.isEmpty() ? b : a + System.lineSeparator() + b);
            }
            if (!process.waitFor(300, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IOException(failurePrefix + ": timed out");
            }
            if (process.exitValue() != 0) {
                throw new IOException(failurePrefix + ":" + System.lineSeparator() + outputText);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(failurePrefix + ": interrupted", ex);
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
