package org.llw.studio.build;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Creates a Windows {@code .lnk} shortcut in the build output root.
 */
public final class BuildShortcutWriter {
    private BuildShortcutWriter() {
    }

    /**
     * @param outputRoot build output root
     * @param settings   build settings
     * @param target     preferred launch target (usually the {@code .exe})
     * @param iconIco    optional icon for the shortcut
     * @param log        build log
     * @return shortcut path when created, otherwise {@code null}
     */
    public static Path createShortcut(
            Path outputRoot,
            BuildSettings settings,
            Path target,
            Path iconIco,
            List<String> log
    ) {
        if (!isWindows() || target == null || !Files.isRegularFile(target)) {
            return null;
        }
        if (!target.getFileName().toString().toLowerCase().endsWith(".exe")) {
            return null;
        }
        String product = settings.productName();
        Path shortcut = outputRoot.resolve(product + ".lnk");
        Path workingDir = target.getParent();
        Path icon = workingDir.resolve(product + ".ico");
        if (!Files.isRegularFile(icon)) {
            icon = iconIco;
        }
        String psScript = buildPowerShellScript(shortcut, target, workingDir, icon, product);
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy", "Bypass",
                    "-Command",
                    psScript
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes());
            if (!output.isBlank()) {
                log.add(output.trim());
            }
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.add("Shortcut creation timed out.");
                return null;
            }
            if (process.exitValue() != 0) {
                log.add("Shortcut creation failed with exit code " + process.exitValue());
                return null;
            }
            if (Files.isRegularFile(shortcut)) {
                log.add("Created shortcut: " + shortcut);
                return shortcut;
            }
            log.add("Shortcut creation did not produce a file.");
            return null;
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.add("Shortcut creation error: " + ex.getMessage());
            return null;
        }
    }

    private static String buildPowerShellScript(
            Path shortcut,
            Path target,
            Path workingDir,
            Path icon,
            String description
    ) {
        List<String> lines = new ArrayList<>();
        lines.add("$shell = New-Object -ComObject WScript.Shell");
        lines.add("$link = $shell.CreateShortcut('" + escapePs(shortcut.toAbsolutePath()) + "')");
        lines.add("$link.TargetPath = '" + escapePs(target.toAbsolutePath()) + "'");
        lines.add("$link.WorkingDirectory = '" + escapePs(workingDir.toAbsolutePath()) + "'");
        lines.add("$link.Description = '" + escapePsString(description) + "'");
        if (icon != null && Files.isRegularFile(icon)) {
            lines.add("$link.IconLocation = '" + escapePs(icon.toAbsolutePath()) + ",0'");
        }
        lines.add("$link.Save()");
        return String.join("; ", lines);
    }

    private static String escapePs(Path path) {
        return escapePsString(path.toString());
    }

    private static String escapePsString(String value) {
        return value.replace("'", "''");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
