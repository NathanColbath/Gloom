package org.llw.studio.build;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

/**
 * Invokes {@code jpackage} to produce a Windows app image when the tool is available.
 */
public final class JpackageRunner {
    private static final String POLYGLOT_SERVICE =
            "META-INF/services/org.graalvm.polyglot.impl.AbstractPolyglotImpl";
    private static final String COMMUNITY_POLYGLOT_IMPL = "com.oracle.truffle.polyglot.PolyglotImpl";

    private JpackageRunner() {
    }

    /**
     * @param outputRoot staged build directory containing {@code runtime/llw-player.jar}
     * @param settings   build settings
     * @param iconIco    optional staged {@code .ico} for the app image
     * @param log        build log
     * @return path to {@code .exe} when successful, otherwise {@code null}
     */
    public static Path packageWindows(Path outputRoot, BuildSettings settings, Path iconIco, List<String> log) {
        if (!isWindows()) {
            log.add("Skipping jpackage: supported on Windows builds only in v1.");
            return null;
        }
        Path playerJar = outputRoot.resolve("runtime/llw-player-all.jar");
        if (!Files.isRegularFile(playerJar)) {
            playerJar = outputRoot.resolve("runtime/llw-player.jar");
        }
        if (!Files.isRegularFile(playerJar)) {
            log.add("Skipping jpackage: player JAR missing.");
            return null;
        }
        try {
            validatePlayerJar(playerJar, log);
        } catch (IOException ex) {
            log.add("Skipping jpackage: " + ex.getMessage());
            return null;
        }
        String jpackage = System.getProperty("java.home") + "/bin/jpackage.exe";
        if (!Files.isRegularFile(Path.of(jpackage))) {
            jpackage = System.getProperty("java.home") + "/bin/jpackage";
        }
        if (!Files.isRegularFile(Path.of(jpackage))) {
            log.add("Skipping jpackage: tool not found in current JDK.");
            return null;
        }
        String product = settings.productName();
        deleteQuietly(outputRoot.resolve(product)); // jpackage refuses to overwrite a partial app-image folder.
        List<String> command = new java.util.ArrayList<>(List.of(
                jpackage,
                "--type", "app-image",
                "--name", product,
                "--input", outputRoot.resolve("runtime").toString(),
                "--main-jar", playerJar.getFileName().toString(),
                "--main-class", "org.llw.player.PlayerLauncher",
                "--dest", outputRoot.toString(),
                "--app-version", settings.version(),
                "--java-options", "-Dorg.lwjgl.system.SharedLibraryExtractPath=$APPDIR\\natives"
        ));
        if (iconIco != null && Files.isRegularFile(iconIco)) {
            command.add("--icon");
            command.add(iconIco.toAbsolutePath().toString());
            log.add("Using application icon: " + iconIco);
        }
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes());
            if (!output.isBlank()) {
                log.add(output.trim());
            }
            boolean finished = process.waitFor(15, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                log.add("jpackage timed out.");
                return null;
            }
            if (process.exitValue() != 0) {
                log.add("jpackage failed with exit code " + process.exitValue());
                return null;
            }
            Path exe = outputRoot.resolve(product).resolve(product + ".exe");
            if (Files.isRegularFile(exe)) {
                return exe;
            }
            try (var stream = Files.list(outputRoot)) {
                return stream
                        .filter(path -> path.getFileName().toString().endsWith(".exe"))
                        .findFirst()
                        .orElse(null);
            }
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.add("jpackage error: " + ex.getMessage());
            return null;
        }
    }

    private static void validatePlayerJar(Path playerJar, List<String> log) throws IOException {
        try (JarFile jar = new JarFile(playerJar.toFile())) {
            var entry = jar.getEntry(POLYGLOT_SERVICE);
            if (entry == null) {
                throw new IOException("player JAR is missing Graal polyglot service metadata");
            }
            String providers = new String(jar.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
            boolean hasCommunity = providers.lines()
                    .map(String::trim)
                    .anyMatch(COMMUNITY_POLYGLOT_IMPL::equals);
            if (!hasCommunity) {
                // Packaged exe needs community PolyglotImpl; enterprise-only JARs fail at script startup.
                throw new IOException(
                        "player JAR does not include the community Graal polyglot provider; "
                                + "rebuild the engine with gradlew :llw-player:fatJar"
                );
            }
            log.add("Validated Graal polyglot provider in player runtime.");
        }
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }

    private static void deleteQuietly(Path path) {
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
        } catch (IOException ignored) {
        }
    }
}
