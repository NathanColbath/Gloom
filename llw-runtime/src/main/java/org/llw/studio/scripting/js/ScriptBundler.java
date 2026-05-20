package org.llw.studio.scripting.js;

import org.llw.studio.log.StudioLogSink;
import org.llw.studio.project.StudioProjectLayout;
import org.llw.studio.scripting.ScriptSchemaRegistry;
import org.llw.util.log.LogLevel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Bundles project script sources to cached JavaScript for Graal evaluation (esbuild or fallback).
 */
public final class ScriptBundler {
    private static final Pattern IMPORT_LINE = Pattern.compile(
            "^\\s*import\\s+.*?from\\s+[\"'](?:@llw/studio|llw\\.core)[\"'];?\\s*$");
    private static final Pattern EXPORT_DEFAULT = Pattern.compile("^\\s*export\\s+default\\s+");
    private static final AtomicInteger typeCheckInvocationCount = new AtomicInteger();

    private ScriptBundler() {
    }

    /**
     * @param projectRoot project root directory
     * @param scriptGuid  script asset GUID
     * @param sourcePath  {@code .js} or {@code .ts} source file
     * @return path to the cached bundle under {@code .studio/metadata/script-cache}
     * @throws IOException when type-checking or bundling fails
     */
    public static Path bundle(Path projectRoot, String scriptGuid, Path sourcePath) throws IOException {
        return bundle(projectRoot, scriptGuid, sourcePath, false);
    }

    /**
     * @param skipTypeCheck when {@code true}, skips per-file TypeScript checking (caller runs {@link #runTypeCheck} once per batch)
     */
    public static Path bundle(Path projectRoot, String scriptGuid, Path sourcePath, boolean skipTypeCheck) throws IOException {
        Path output = StudioProjectLayout.scriptCachePath(projectRoot, scriptGuid);
        Files.createDirectories(output.getParent());
        if (isCacheFresh(sourcePath, output)) {
            return output;
        }
        String sourceName = sourcePath.getFileName().toString().toLowerCase();
        if (sourceName.endsWith(".ts") && !skipTypeCheck) {
            typeCheck(projectRoot);
        }
        if (tryEsbuild(projectRoot, sourcePath, output)) {
            extractSchema(projectRoot, scriptGuid, sourcePath);
            ScriptDiagnostics.set(scriptGuid, new ScriptDiagnostic(scriptGuid, false, "Bundled with esbuild"));
            return output;
        }
        if (sourceName.endsWith(".ts")) {
            throw new IOException("TypeScript scripts require esbuild. Run `npm install` in the project root.");
        }
        String bundled = simpleBundle(Files.readString(sourcePath));
        Files.writeString(output, bundled);
        if (sourceName.endsWith(".ts")) {
            extractSchema(projectRoot, scriptGuid, sourcePath);
        }
        ScriptDiagnostics.set(scriptGuid, new ScriptDiagnostic(scriptGuid, false, "Bundled with built-in fallback"));
        return output;
    }

    /**
     * @param projectRoot project root directory
     * @param sources     scripts to bundle
     * @param console     optional sink for per-script errors
     */
    public static void bundleAll(Path projectRoot, List<ScriptSource> sources, StudioLogSink console) {
        ScriptDiagnostics.clear();
        boolean needsTypeCheck = false;
        for (ScriptSource source : sources) {
            Path output = StudioProjectLayout.resolveScriptCachePath(projectRoot, source.guid());
            String sourceName = source.path().getFileName().toString().toLowerCase();
            if (sourceName.endsWith(".ts") && !isCacheFresh(source.path(), output)) {
                needsTypeCheck = true;
                break;
            }
        }
        if (needsTypeCheck) {
            try {
                runTypeCheck(projectRoot);
            } catch (IOException ex) {
                String message = ex.getMessage();
                for (ScriptSource source : sources) {
                    String sourceName = source.path().getFileName().toString().toLowerCase();
                    if (sourceName.endsWith(".ts")) {
                        ScriptDiagnostics.set(source.guid(), new ScriptDiagnostic(source.guid(), true, message));
                        if (console != null) {
                            console.append(LogLevel.ERROR,
                                    "Script compile failed for " + source.path().getFileName() + ": " + message);
                        }
                    }
                }
                return;
            }
        }
        for (ScriptSource source : sources) {
            try {
                bundle(projectRoot, source.guid(), source.path(), true);
            } catch (Exception ex) {
                String message = "Script compile failed for " + source.path().getFileName() + ": " + ex.getMessage();
                ScriptDiagnostics.set(source.guid(), new ScriptDiagnostic(source.guid(), true, message));
                if (console != null) {
                    console.append(LogLevel.ERROR, message);
                }
            }
        }
    }

    public static void runTypeCheck(Path projectRoot) throws IOException {
        typeCheck(projectRoot);
    }

    static int typeCheckInvocationCount() {
        return typeCheckInvocationCount.get();
    }

    static void resetTypeCheckInvocationCount() {
        typeCheckInvocationCount.set(0);
    }

    public static boolean isCacheFresh(Path sourcePath, Path output) {
        if (!Files.isRegularFile(output)) {
            return false;
        }
        try {
            return Files.getLastModifiedTime(sourcePath).compareTo(Files.getLastModifiedTime(output)) <= 0;
        } catch (IOException ex) {
            return false;
        }
    }

    private static void typeCheck(Path projectRoot) throws IOException {
        typeCheckInvocationCount.incrementAndGet();
        Path tsconfig = projectRoot.resolve("tsconfig.json");
        if (!Files.isRegularFile(tsconfig)) {
            return;
        }
        List<String> command = toolCommand(projectRoot, "tsc", "--noEmit", "-p", tsconfig.toAbsolutePath().toString());
        runCommand(projectRoot, command, "TypeScript type check failed");
    }

    private static boolean tryEsbuild(Path projectRoot, Path sourcePath, Path output) {
        List<String> command = new ArrayList<>(toolCommand(projectRoot, "esbuild"));
        command.add(sourcePath.toAbsolutePath().toString());
        command.add("--bundle");
        command.add("--format=iife");
        command.add("--global-name=__LLWScriptBundle");
        command.add("--platform=browser");
        command.add("--target=es2020");
        Path coreEntry = projectRoot.resolve(".llw/sdk/core.js");
        Path shim = projectRoot.resolve(".llw/sdk/runtime-shim.js");
        Path sdkEntry = Files.exists(coreEntry) ? coreEntry : shim;
        if (Files.exists(sdkEntry)) {
            String aliasTarget = sdkEntry.toAbsolutePath().toString();
            command.add("--alias:llw.core=" + aliasTarget);
            command.add("--alias:@llw/studio=" + aliasTarget);
        }
        command.add("--outfile=" + output.toAbsolutePath());
        try {
            runCommand(projectRoot, command, "esbuild failed");
            String bundled = Files.readString(output);
            Files.writeString(output, wrapEsbuildBundle(bundled));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static List<String> toolCommand(Path projectRoot, String... args) {
        List<String> command = new ArrayList<>();
        Path local = projectRoot.resolve(isWindows() ? "node_modules/.bin/" + args[0] + ".cmd" : "node_modules/.bin/" + args[0]);
        if (Files.exists(local)) {
            command.add(local.toAbsolutePath().toString());
        } else {
            command.add(isWindows() ? "npx.cmd" : "npx");
            command.add(args[0]);
        }
        for (int i = 1; i < args.length; i++) {
            command.add(args[i]);
        }
        return command;
    }

    private static void extractSchema(Path projectRoot, String scriptGuid, Path sourcePath) {
        try {
            Path schemaOutput = StudioProjectLayout.scriptSchemaPath(projectRoot, scriptGuid);
            Files.createDirectories(schemaOutput.getParent());
            Path extractor = ensureSchemaExtractor(projectRoot);
            List<String> command = new ArrayList<>();
            command.add(nodeCommand(projectRoot));
            command.add(extractor.toAbsolutePath().toString());
            command.add(sourcePath.toAbsolutePath().toString());
            command.add(schemaOutput.toAbsolutePath().toString());
            runCommand(projectRoot, command, "Schema extraction failed");
            ScriptSchemaRegistry.invalidate(scriptGuid);
        } catch (IOException ex) {
            ScriptDiagnostics.set(scriptGuid, new ScriptDiagnostic(scriptGuid, true, ex.getMessage()));
        }
    }

    private static Path ensureSchemaExtractor(Path projectRoot) throws IOException {
        Path target = StudioProjectLayout.schemaExtractorScript(projectRoot);
        if (Files.isRegularFile(target)) {
            return target;
        }
        Files.createDirectories(target.getParent());
        try (var in = ScriptBundler.class.getClassLoader().getResourceAsStream("scripting-sdk/extract-schema.mjs")) {
            if (in == null) {
                throw new IOException("Missing extract-schema.mjs resource");
            }
            Files.copy(in, target);
        }
        return target;
    }

    private static String nodeCommand(Path projectRoot) {
        Path local = projectRoot.resolve(isWindows() ? "node_modules/.bin/node.cmd" : "node_modules/.bin/node");
        if (Files.exists(local)) {
            return local.toAbsolutePath().toString();
        }
        return "node";
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static void runCommand(Path projectRoot, List<String> command, String failurePrefix) throws IOException {
        try {
            Process process = new ProcessBuilder(command)
                    .directory(projectRoot.toFile())
                    .redirectErrorStream(true)
                    .start();
            String outputText;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                outputText = reader.lines().reduce("", (a, b) -> a.isEmpty() ? b : a + System.lineSeparator() + b);
            }
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
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

    public static String sanitizeBundledSource(String source) {
        String marker = "if (typeof __LLWScriptBundle !== 'undefined' && __LLWScriptBundle.default)";
        int idx = source.lastIndexOf(marker);
        if (idx >= 0) {
            return source.substring(0, idx).stripTrailing();
        }
        return source;
    }

    private static String wrapEsbuildBundle(String bundled) {
        return bundled;
    }

    static String simpleBundle(String source) {
        StringBuilder body = new StringBuilder();
        boolean foundExport = false;
        for (String line : source.split("\\R")) {
            if (IMPORT_LINE.matcher(line).matches()) {
                continue;
            }
            if (EXPORT_DEFAULT.matcher(line).find()) {
                body.append(line.replaceFirst("export\\s+default\\s+", "return ")).append(System.lineSeparator());
                foundExport = true;
            } else {
                body.append(line).append(System.lineSeparator());
            }
        }
        if (!foundExport) {
            throw new IllegalStateException("Script must contain `export default class ...`");
        }
        return """
                (function(LLW) {
                  const Script = LLW.Script;
                  const Time = globalThis.Time;
                  const Input = globalThis.Input;
                  const Logger = globalThis.Logger;
                  const Math = globalThis.Math;
                  const Mathf = globalThis.Mathf;
                  const Vec2 = globalThis.Vec2;
                  const Vector2f = globalThis.Vector2f;
                  const Color = globalThis.Color;
                  const Rect2 = globalThis.Rect2;
                  const Camera = globalThis.Camera;
                  const Scene = globalThis.Scene;
                  const Assets = globalThis.Assets;
                  %s
                })
                """.formatted(body.toString().trim());
    }

    /**
     * @param guid script asset GUID
     * @param path source file path
     */
    public record ScriptSource(String guid, Path path) {
    }
}
