package org.llw.studio.editor.scripting;

import org.llw.studio.assets.AssetDatabase;
import org.llw.studio.editor.MainThreadQueue;
import org.llw.studio.log.StudioLogSink;
import org.llw.studio.scripting.js.ScriptCompileService;
import org.llw.studio.scripting.js.ScriptRecompileService;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Watches {@code Assets/Scripts} and re-bundles changed sources when not in play mode.
 */
public final class ScriptFileWatcher implements AutoCloseable {
    private static final long DEBOUNCE_MS = 300L;

    private final Path projectRoot;
    private final AssetDatabase assets;
    private final Path scriptsRoot;
    private final StudioLogSink console;
    private final MainThreadQueue mainThreadQueue;
    private final Consumer<String> onRecompiled;
    private final BooleanSupplier isPlaying;
    private final Supplier<Set<String>> sceneScriptGuids;
    private final Set<Path> deferredPaths = ConcurrentHashMap.newKeySet();
    private final ExecutorService watchExecutor;
    private final ExecutorService compileExecutor;
    private final ScheduledExecutorService debounceExecutor;
    private final Map<Path, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();
    private volatile boolean running;

    /**
     * @param projectRoot     project root directory
     * @param console         editor console for compile messages
     * @param mainThreadQueue queue for UI-thread callbacks
     * @param isPlaying       when {@code true}, recompiles are deferred
     * @param onRecompiled    called on the main thread with script GUID after success
     */
    public ScriptFileWatcher(
            Path projectRoot,
            AssetDatabase assets,
            StudioLogSink console,
            MainThreadQueue mainThreadQueue,
            BooleanSupplier isPlaying,
            Consumer<String> onRecompiled,
            Supplier<Set<String>> sceneScriptGuids
    ) {
        this.projectRoot = projectRoot;
        this.assets = assets;
        this.scriptsRoot = projectRoot.resolve("Assets/Scripts");
        this.console = console;
        this.mainThreadQueue = mainThreadQueue;
        this.isPlaying = isPlaying;
        this.onRecompiled = onRecompiled;
        this.sceneScriptGuids = sceneScriptGuids == null ? Set::of : sceneScriptGuids;
        this.watchExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "script-file-watcher");
            thread.setDaemon(true);
            return thread;
        });
        this.compileExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "script-recompiler");
            thread.setDaemon(true);
            return thread;
        });
        this.debounceExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "script-watch-debounce");
            thread.setDaemon(true);
            return thread;
        });
    }

    /** Starts the background watch loop if not already running. */
    public void start() {
        if (running) {
            return;
        }
        running = true;
        watchExecutor.submit(this::watchLoop);
    }

    private void watchLoop() {
        try {
            Files.createDirectories(scriptsRoot);
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                registerTree(scriptsRoot, watchService);
                while (running) {
                    WatchKey key = watchService.take();
                    Path watchedDir = (Path) key.watchable();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                        Path child = watchedDir.resolve(pathEvent.context());
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(child)) {
                            registerTree(child, watchService);
                        }
                        if (isScriptSource(child)) {
                            scheduleRecompile(child.normalize());
                        }
                    }
                    if (!key.reset()) {
                        break;
                    }
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            postToMainThread(() -> {
                if (console != null) {
                    console.append(org.llw.util.log.LogLevel.ERROR,
                            "Script file watcher failed: " + ex.getMessage());
                }
            });
        }
    }

    private void registerTree(Path root, WatchService watchService) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            for (Path dir : paths.filter(Files::isDirectory).toList()) {
                dir.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
            }
        }
    }

    private static boolean isScriptSource(Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".js") || name.endsWith(".ts");
    }

    private void scheduleRecompile(Path scriptPath) {
        // Play mode keeps edit bundles stable; queue paths and flush on exit play.
        if (isPlaying.getAsBoolean()) {
            deferredPaths.add(scriptPath);
            return;
        }
        ScheduledFuture<?> previous = pending.remove(scriptPath);
        if (previous != null) {
            previous.cancel(false);
        }
        ScheduledFuture<?> future = debounceExecutor.schedule(
                () -> compileExecutor.submit(() -> recompile(scriptPath)),
                DEBOUNCE_MS,
                TimeUnit.MILLISECONDS
        );
        pending.put(scriptPath, future);
    }

    private void recompile(Path scriptPath) {
        if (isPlaying.getAsBoolean()) {
            deferredPaths.add(scriptPath);
            return;
        }
        ScriptRecompileService.Result result = ScriptRecompileService.recompile(projectRoot, scriptPath);
        if (result == null) {
            return;
        }
        if (result.success()) {
            warmSceneScripts();
        }
        // Console + hot-reload hook must run on GLFW thread (may touch play runner / UI).
        postToMainThread(() -> {
            if (console != null) {
                console.append(result.level(), result.message());
            }
            if (result.success() && result.guid() != null && onRecompiled != null) {
                onRecompiled.accept(result.guid());
            }
        });
    }

    /** Schedules any recompiles deferred while play mode was active. */
    private void warmSceneScripts() {
        Set<String> guids = sceneScriptGuids.get();
        if (guids.isEmpty()) {
            return;
        }
        compileExecutor.submit(() -> ScriptCompileService.bundleGuids(projectRoot, assets, guids, console));
    }

    public void flushDeferredRecompiles() {
        if (isPlaying.getAsBoolean()) {
            return;
        }
        for (Path path : Set.copyOf(deferredPaths)) {
            deferredPaths.remove(path);
            scheduleRecompile(path);
        }
    }

    private void postToMainThread(Runnable task) {
        mainThreadQueue.enqueue(task);
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        running = false;
        debounceExecutor.shutdownNow();
        compileExecutor.shutdownNow();
        watchExecutor.shutdownNow();
        pending.clear();
    }
}

