package org.llw.studio.build;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Copies the llw-player runtime artifacts next to built content.
 */
public final class PlayerStaging {
    private PlayerStaging() {
    }

    /**
     * @param projectRoot project root (used to locate engine checkout when developing)
     * @param outputRoot  build output directory
     * @param settings    build settings
     * @param contentDir  content directory with packs
     * @param log         build log
     * @return path to the staged player runtime JAR
     * @throws IOException when staging fails
     */
    public static Path stage(
            Path projectRoot,
            Path outputRoot,
            BuildSettings settings,
            Path contentDir,
            List<String> log
    ) throws IOException {
        Path playerJar = resolvePlayerJar(projectRoot, log);
        Path runtimeDir = outputRoot.resolve("runtime");
        Files.createDirectories(runtimeDir);
        Path stagedJar = runtimeDir.resolve("llw-player-all.jar");
        Files.copy(playerJar, stagedJar, StandardCopyOption.REPLACE_EXISTING);
        log.add("Staged player runtime: " + stagedJar);
        return stagedJar;
    }

    private static Path resolvePlayerJar(Path projectRoot, List<String> log) throws IOException {
        List<String> attempts = new ArrayList<>();
        // Dev checkout fatJar first; shipped studio falls back to classpath:/player/llw-player.jar.
        Path engineRoot = locateEngineRoot(projectRoot);
        if (engineRoot != null) {
            attempts.add(engineRoot.resolve("llw-player/build/libs/llw-player-all.jar").toString());
            Path fatJar = engineRoot.resolve("llw-player/build/libs/llw-player-all.jar");
            if (Files.isRegularFile(fatJar)) {
                log.add("Using player runtime from engine checkout: " + fatJar);
                return fatJar;
            }
            Path versionedFatJar = findFirstJar(engineRoot.resolve("llw-player/build/libs"), "*-all.jar");
            if (versionedFatJar != null) {
                log.add("Using player runtime from engine checkout: " + versionedFatJar);
                return versionedFatJar;
            }
            Path devJar = engineRoot.resolve("llw-player/build/libs/llw-player.jar");
            attempts.add(devJar.toString());
            if (Files.isRegularFile(devJar)) {
                log.add("Using player runtime from engine checkout: " + devJar);
                return devJar;
            }
        } else {
            attempts.add("engine checkout (settings.gradle.kts + llw-player/)");
        }

        Path fromResources = extractEmbeddedPlayerJar();
        attempts.add("classpath:/player/llw-player.jar");
        if (fromResources != null) {
            log.add("Using bundled player runtime: " + fromResources);
            return fromResources;
        }

        throw new IOException(
                "Player runtime JAR not found. Searched: "
                        + String.join("; ", attempts)
                        + ". Build the engine once with: gradlew :llw-player:fatJar"
        );
    }

    static Path locateEngineRoot(Path projectRoot) {
        Path override = readEngineRootOverride();
        if (override != null && isEngineRoot(override)) {
            return override.normalize();
        }
        Set<Path> starts = new LinkedHashSet<>();
        if (projectRoot != null) {
            starts.add(projectRoot);
        }
        starts.add(Path.of(System.getProperty("user.dir", ".")));
        Path codeSource = codeSourceRoot();
        if (codeSource != null) {
            starts.add(codeSource);
        }
        for (Path start : starts) {
            // Walk parents to find monorepo root (settings.gradle.kts + llw-player module).
            Path found = walkUpForEngineRoot(start);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static Path readEngineRootOverride() {
        String configured = System.getProperty("llw.engine.root");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("LLW_ENGINE_ROOT");
        }
        if (configured == null || configured.isBlank()) {
            return null;
        }
        return Path.of(configured);
    }

    private static Path codeSourceRoot() {
        try {
            var location = PlayerStaging.class.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return null;
            }
            Path path = Path.of(location.toURI());
            if (Files.isRegularFile(path)) {
                return path.getParent();
            }
            return path;
        } catch (URISyntaxException | SecurityException ignored) {
            return null;
        }
    }

    private static Path walkUpForEngineRoot(Path start) {
        Path current = start.toAbsolutePath().normalize();
        while (current != null) {
            if (isEngineRoot(current)) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    private static boolean isEngineRoot(Path dir) {
        return Files.isRegularFile(dir.resolve("settings.gradle.kts"))
                && Files.isRegularFile(dir.resolve("llw-player/build.gradle.kts"));
    }

    private static Path findFirstJar(Path directory, String glob) throws IOException {
        if (!Files.isDirectory(directory)) {
            return null;
        }
        try (Stream<Path> files = Files.list(directory)) {
            Optional<Path> match = files
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        if (glob.startsWith("*")) {
                            return name.endsWith(glob.substring(1));
                        }
                        return name.equals(glob);
                    })
                    .sorted()
                    .findFirst();
            return match.orElse(null);
        }
    }

    private static Path extractEmbeddedPlayerJar() throws IOException {
        String resource = "/player/llw-player.jar";
        try (InputStream stream = PlayerStaging.class.getResourceAsStream(resource)) {
            if (stream == null) {
                return null;
            }
            Path temp = Files.createTempFile("llw-player-", ".jar");
            Files.copy(stream, temp, StandardCopyOption.REPLACE_EXISTING);
            temp.toFile().deleteOnExit();
            return temp;
        }
    }

    static String readMainClass(Path jarPath) throws IOException {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                return null;
            }
            return manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
        }
    }
}
