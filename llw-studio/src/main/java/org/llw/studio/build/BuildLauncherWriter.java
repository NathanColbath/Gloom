package org.llw.studio.build;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Writes the fallback {@code .bat} launcher inside the packaged app folder.
 */
public final class BuildLauncherWriter {
    private BuildLauncherWriter() {
    }

    /**
     * @param outputRoot build output root
     * @param settings   build settings
     * @param iconIco    optional icon copied beside the launcher
     * @param log        build log
     * @return path to the {@code .bat} launcher
     * @throws IOException when the launcher cannot be written
     */
    public static Path writeBat(Path outputRoot, BuildSettings settings, Path iconIco, List<String> log)
            throws IOException {
        String product = settings.productName();
        Path appFolder = outputRoot.resolve(product);
        Files.createDirectories(appFolder);
        Path launchBat = appFolder.resolve(product + ".bat");
        String bat = """
                @echo off
                setlocal
                set APP_DIR=%~dp0
                set CONTENT_DIR=%APP_DIR%..\\content
                if defined LLW_CONTENT_DIR set CONTENT_DIR=%LLW_CONTENT_DIR%
                java -Dorg.lwjgl.system.SharedLibraryExtractPath="%APP_DIR%..\\runtime\\natives" ^
                  -jar "%APP_DIR%..\\runtime\\llw-player-all.jar" --content "%CONTENT_DIR%"
                endlocal
                """;
        Files.writeString(launchBat, bat);
        log.add("Wrote launcher script: " + launchBat);
        Path legacyBat = outputRoot.resolve(product + ".bat");
        Files.deleteIfExists(legacyBat);
        if (iconIco != null && Files.isRegularFile(iconIco)) {
            Path stagedIcon = appFolder.resolve(product + ".ico");
            Files.copy(iconIco, stagedIcon, StandardCopyOption.REPLACE_EXISTING);
            log.add("Copied application icon: " + stagedIcon);
        }
        return launchBat;
    }

    /**
     * Ensures {@code {product}/app/} contains the staged player JAR for jpackage launchers.
     *
     * @param outputRoot build output root
     * @param settings   build settings
     * @param log        build log
     * @return path to the JAR inside {@code app/}, or {@code null} when runtime staging is missing
     * @throws IOException when the app folder cannot be written
     */
    public static Path ensureAppRuntime(Path outputRoot, BuildSettings settings, List<String> log)
            throws IOException {
        Path playerJar = outputRoot.resolve("runtime/llw-player-all.jar");
        if (!Files.isRegularFile(playerJar)) {
            playerJar = outputRoot.resolve("runtime/llw-player.jar");
        }
        if (!Files.isRegularFile(playerJar)) {
            log.add("Warning: runtime JAR missing; app folder was not staged.");
            return null;
        }
        String product = settings.productName();
        Path appDir = outputRoot.resolve(product).resolve("app");
        Files.createDirectories(appDir.resolve("natives"));
        Path packagedJar = appDir.resolve(playerJar.getFileName().toString());
        Files.copy(playerJar, packagedJar, StandardCopyOption.REPLACE_EXISTING);
        log.add("Staged player runtime in app image: " + packagedJar);
        return packagedJar;
    }
}
