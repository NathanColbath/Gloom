package org.llw.studio.scripting;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Compiles legacy Java sources under a scripts directory into a {@link ScriptAssembly}.
 */
public final class ScriptCompiler {
    /**
     * @param scriptsDir directory tree containing {@code .java} sources
     * @param outputJar  unused jar path; parent directories are used for class output
     * @return compiled assembly, or {@link ScriptAssembly#empty()} on failure or no sources
     */
    public ScriptAssembly compile(Path scriptsDir, Path outputJar) {
        if (!Files.isDirectory(scriptsDir)) {
            return ScriptAssembly.empty();
        }
        try {
            Files.createDirectories(outputJar.getParent());
            Path classesDir = outputJar.getParent().resolve("classes");
            Files.createDirectories(classesDir);
            List<Path> sources = new ArrayList<>();
            try (Stream<Path> stream = Files.walk(scriptsDir)) {
                stream.filter(path -> path.toString().endsWith(".java")).forEach(sources::add);
            }
            if (sources.isEmpty()) {
                return ScriptAssembly.empty();
            }
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                return ScriptAssembly.empty();
            }
            try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
                Iterable<? extends javax.tools.JavaFileObject> units =
                        fileManager.getJavaFileObjectsFromPaths(sources);
                List<String> options = List.of("-d", classesDir.toString());
                boolean ok = compiler.getTask(null, fileManager, null, options, null, units).call();
                if (!ok) {
                    return ScriptAssembly.empty();
                }
            }
            URLClassLoader loader = new URLClassLoader(new URL[]{classesDir.toUri().toURL()}, ScriptBehaviour.class.getClassLoader());
            return new ScriptAssembly(loader);
        } catch (IOException ex) {
            return ScriptAssembly.empty();
        }
    }
}
