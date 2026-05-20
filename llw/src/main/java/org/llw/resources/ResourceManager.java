package org.llw.resources;

import org.llw.audio.AudioContext;
import org.llw.audio.Music;
import org.llw.audio.SoundBuffer;
import org.llw.render.graphics.Font;
import org.llw.render.graphics.FontStyle;
import org.llw.render.graphics.Texture2d;
import org.llw.render.graphics.system.SystemFonts;
import org.llw.render.gl.OpenGlBackend;
import org.llw.render.resources.ResourceLoader;
import org.llw.resources.pack.AssetPackManifest;
import org.llw.resources.pack.AssetPackReader;
import org.llw.util.log.EnvironmentLog;
import org.llw.util.log.Log;
import org.llw.util.log.LogHelper;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Central registry for game assets with reference-counted load/unload and optional pack bundling.
 */
public final class ResourceManager implements AutoCloseable {
    private static final Logger log = Log.get(Loggers.RESOURCES);

    private final OpenGlBackend gl;
    private final AudioContext audio;
    private final Map<String, AssetHandle> handles = new LinkedHashMap<>();
    private Map<String, Path> systemFontCatalog;
    private final Object systemFontCatalogLock = new Object();
    private final Set<String> pinned = new HashSet<>();
    private final LoadContext loadContext;
    private boolean lazyLoading = true;
    private boolean disposed;

    /**
     * Creates a resource manager bound to the render and audio backends.
     *
     * @param gl    OpenGL backend (textures/fonts)
     * @param audio initialized audio context (sounds/music)
     */
    public ResourceManager(OpenGlBackend gl, AudioContext audio) {
        this.gl = gl;
        this.audio = audio;
        this.loadContext = new LoadContext(audio);
        log.info("ResourceManager created");
    }

    ResourceManager(OpenGlBackend gl, AudioContext audio, Map<String, Path> systemFontCatalog) {
        this.gl = gl;
        this.audio = audio;
        this.loadContext = new LoadContext(audio);
        this.systemFontCatalog = Map.copyOf(systemFontCatalog);
        log.info("ResourceManager created");
        EnvironmentLog.logSystemFontCatalog(this.systemFontCatalog.size());
    }

    /** @return fluent registration of a classpath texture */
    public ResourceManager registerTexture(String id, String classpathPath) {
        register(new AssetDescriptor(id, AssetType.TEXTURE, new AssetSource.Classpath(classpathPath), 0));
        return this;
    }

    /** @return whether an asset id is already registered */
    public boolean isRegistered(String id) {
        return handles.containsKey(id);
    }

    /** @return fluent registration of a filesystem texture */
    public ResourceManager registerTextureFile(String id, Path path) {
        register(new AssetDescriptor(id, AssetType.TEXTURE, new AssetSource.File(path), 0));
        return this;
    }

    /** @return fluent registration of a classpath font */
    public ResourceManager registerFont(String id, String classpathPath, int pixelHeight) {
        register(new AssetDescriptor(id, AssetType.FONT, new AssetSource.Classpath(classpathPath), pixelHeight));
        return this;
    }

    /** @return fluent registration of a filesystem font */
    public ResourceManager registerFontFile(String id, Path path, int pixelHeight) {
        register(new AssetDescriptor(id, AssetType.FONT, new AssetSource.File(path), pixelHeight));
        return this;
    }

    /** @return fluent registration of a classpath sound */
    public ResourceManager registerSound(String id, String classpathPath) {
        register(new AssetDescriptor(id, AssetType.SOUND, new AssetSource.Classpath(classpathPath), 0));
        return this;
    }

    /** @return fluent registration of a filesystem sound (.wav / .ogg) */
    public ResourceManager registerSoundFile(String id, Path path) {
        register(new AssetDescriptor(id, AssetType.SOUND, new AssetSource.File(path), 0));
        return this;
    }

    /** @return shared OpenAL context used for sound and music */
    public AudioContext audioContext() {
        ensureActive();
        return audio;
    }

    /** @return fluent registration of a streaming music path */
    public ResourceManager registerMusic(String id, String classpathPath) {
        register(new AssetDescriptor(id, AssetType.MUSIC, new AssetSource.Classpath(classpathPath), 0));
        return this;
    }

    /**
     * Registers assets listed in {@code classpathDir/_index.json}.
     *
     * @param idPrefix     prefix for entry ids
     * @param classpathDir directory containing {@code _index.json}
     * @param opts         scan options
     * @return this manager
     */
    public ResourceManager registerClasspathDirectory(String idPrefix, String classpathDir, DirectoryScanOptions opts) {
        String indexPath = classpathDir.endsWith("/") ? classpathDir + "_index.json" : classpathDir + "/_index.json";
        String json = ResourceLoader.loadText(indexPath);
        registerFromIndexJson(idPrefix, json, path -> new AssetSource.Classpath(
                classpathDir.endsWith("/") ? classpathDir + path : classpathDir + "/" + path
        ), opts);
        return this;
    }

    /**
     * Walks a filesystem directory and registers supported files.
     *
     * @param idPrefix id prefix
     * @param dir      root directory
     * @param opts     scan options
     * @return this manager
     */
    public ResourceManager registerDirectory(String idPrefix, Path dir, DirectoryScanOptions opts) {
        int before = handles.size();
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String relative = dir.relativize(file).toString().replace('\\', '/');
                    opts.inferType(file.getFileName().toString()).ifPresent(type -> {
                        String id = idPrefix + stripExtension(relative).replace('/', '.');
                        int fontSize = type == AssetType.FONT ? opts.defaultFontPixelHeight() : 0;
                        register(new AssetDescriptor(id, type, new AssetSource.File(file), fontSize));
                    });
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw LogHelper.logAndThrow(log, "Failed to scan directory: " + dir, e);
        }
        int registered = handles.size() - before;
        log.info("Registered directory root={} prefix={} assets={}", dir, idPrefix, registered);
        return this;
    }

    /** Loads a pack from the classpath and registers all manifest entries. */
    public ResourceManager loadPackClasspath(String packClasspathPath) {
        return loadPack(AssetPackReader.fromClasspath(packClasspathPath));
    }

    /** Loads a pack file and registers all manifest entries. */
    public ResourceManager loadPackFile(Path packPath) {
        try {
            return loadPack(AssetPackReader.fromFile(packPath));
        } catch (IOException e) {
            throw LogHelper.logAndThrow(log, "Failed to load pack: " + packPath, e);
        }
    }

    /** When true (default), assets load on first {@code acquire*}. */
    public void setLazyLoading(boolean lazy) {
        this.lazyLoading = lazy;
        log.debug("Lazy loading set to {}", lazy);
    }

    /** Pins every registered non-music asset (eager warmup). */
    public void loadAll() {
        for (String id : handles.keySet()) {
            AssetHandle handle = handles.get(id);
            if (handle.descriptor().type() != AssetType.MUSIC) {
                pin(id);
            }
        }
    }

    /** Unpins all manager pins and unloads assets with zero external refs. */
    public void unloadAll() {
        for (String id : new HashSet<>(pinned)) {
            unpin(id);
        }
    }

    /** @return whether the GPU/AL object is currently resident */
    public boolean isLoaded(String id) {
        return requireHandle(id).isLoaded();
    }

    /** @return active reference count for an asset */
    public int refCount(String id) {
        return requireHandle(id).refCount();
    }

    /** Acquires a reference-counted texture. */
    public AssetRef<Texture2d> acquireTexture(String id) {
        return acquire(id, AssetType.TEXTURE);
    }

    /** Acquires a reference-counted font. */
    public AssetRef<Font> acquireFont(String id) {
        return acquire(id, AssetType.FONT);
    }

    /**
     * Acquires a plain system font at the given pixel height.
     *
     * @param family      installed font family name
     * @param pixelHeight rasterized glyph height in pixels
     * @return reference-counted font handle
     */
    public AssetRef<Font> acquireSystemFont(String family, int pixelHeight) {
        return acquireSystemFont(family, FontStyle.PLAIN, pixelHeight);
    }

    /**
     * Acquires a styled system font at the given pixel height.
     *
     * @param family      installed font family name
     * @param style       logical style
     * @param pixelHeight rasterized glyph height in pixels
     * @return reference-counted font handle
     */
    public AssetRef<Font> acquireSystemFont(String family, FontStyle style, int pixelHeight) {
        if (pixelHeight <= 0) {
            throw new IllegalArgumentException("pixelHeight must be positive");
        }
        String faceName = SystemFonts.faceName(family, style);
        Path path = systemFontCatalog().get(faceName);
        if (path == null) {
            throw new IllegalArgumentException("Unknown system font face: " + faceName);
        }
        String assetId = SystemFonts.assetId(family, style, pixelHeight);
        if (!handles.containsKey(assetId)) {
            log.debug("Lazy register system font assetId={} face={} pixelHeight={} path={}", assetId, faceName, pixelHeight, path);
            registerFontFile(assetId, path, pixelHeight);
        }
        return acquireFont(assetId);
    }

    /**
     * @return face names discovered in the system font catalog
     */
    public List<String> systemFontFaces() {
        return List.copyOf(systemFontCatalog().keySet());
    }

    /**
     * @param family font family name
     * @param style  logical style
     * @return whether the face exists in the system font catalog
     */
    public boolean hasSystemFontFace(String family, FontStyle style) {
        return systemFontCatalog().containsKey(SystemFonts.faceName(family, style));
    }

    private Map<String, Path> systemFontCatalog() {
        Map<String, Path> catalog = systemFontCatalog;
        if (catalog != null) {
            return catalog;
        }
        synchronized (systemFontCatalogLock) {
            if (systemFontCatalog == null) {
                systemFontCatalog = SystemFonts.scanCatalog();
                EnvironmentLog.logSystemFontCatalog(systemFontCatalog.size());
            }
            return systemFontCatalog;
        }
    }

    /** Acquires a reference-counted sound buffer. */
    public AssetRef<SoundBuffer> acquireSound(String id) {
        return acquire(id, AssetType.SOUND);
    }

    /** Acquires a reference-counted raw byte slice. */
    public AssetRef<byte[]> acquireRaw(String id) {
        return acquire(id, AssetType.RAW);
    }

    /**
     * Opens streaming music for a registered music asset (not ref-counted).
     *
     * @param id registered music id
     * @return new music instance tracked by {@link AudioContext#update()}
     */
    public Music openMusic(String id) {
        ensureActive();
        AssetHandle handle = requireHandle(id);
        if (handle.descriptor().type() != AssetType.MUSIC) {
            throw new IllegalArgumentException("Asset is not music: " + id);
        }
        log.debug("Opening music asset id={}", id);
        return audio.openMusicSupplier(handle::openMusicStream);
    }

    /** Increments internal pin ref count without returning an {@link AssetRef}. */
    public void pin(String id) {
        AssetHandle handle = requireHandle(id);
        if (handle.descriptor().type() == AssetType.MUSIC) {
            return;
        }
        if (pinned.add(id)) {
            handle.acquire(loadContext);
        }
    }

    /** Decrements internal pin ref count. */
    public void unpin(String id) {
        if (pinned.remove(id)) {
            requireHandle(id).release();
        }
    }

    void releaseRef(String id) {
        AssetHandle handle = requireHandle(id);
        handle.release();
        log.debug("Released asset id={} refCount={}", id, handle.refCount());
    }

    @Override
    public void close() {
        dispose();
    }

    /** Force-unloads all assets and clears registrations. */
    public void dispose() {
        if (disposed) {
            return;
        }
        pinned.clear();
        for (AssetHandle handle : handles.values()) {
            handle.forceUnload();
        }
        handles.clear();
        disposed = true;
        log.info("ResourceManager disposed");
    }

    private ResourceManager loadPack(AssetPackReader reader) {
        for (Map.Entry<String, AssetPackManifest.Entry> entry : reader.manifest().entries().entrySet()) {
            AssetPackManifest.Entry e = entry.getValue();
            int fontSize = e.type() == AssetType.FONT ? e.fontSize() : 0;
            log.debug("Pack entry id={} type={} offset={} length={} hint={} fontSize={}",
                    entry.getKey(), e.type(), e.offset(), e.length(), e.hint(), fontSize);
            register(new AssetDescriptor(
                    entry.getKey(),
                    e.type(),
                    new AssetSource.PackSlice(reader, e),
                    fontSize
            ));
        }
        log.info("Loaded asset pack entries={}", reader.manifest().entries().size());
        return this;
    }

    private void register(AssetDescriptor descriptor) {
        ensureActive();
        if (handles.containsKey(descriptor.id())) {
            throw new IllegalArgumentException("Duplicate asset id: " + descriptor.id());
        }
        handles.put(descriptor.id(), new AssetHandle(descriptor));
        log.debug("Registered asset id={} type={} source={}", descriptor.id(), descriptor.type(), describeSource(descriptor.source()));
        if (!lazyLoading && descriptor.type() != AssetType.MUSIC) {
            pin(descriptor.id());
        }
    }

    private <T> AssetRef<T> acquire(String id, AssetType expected) {
        ensureActive();
        AssetHandle handle = requireHandle(id);
        if (handle.descriptor().type() != expected) {
            throw new IllegalArgumentException("Asset " + id + " is " + handle.descriptor().type() + ", expected " + expected);
        }
        T value = handle.acquire(loadContext);
        log.debug("Acquired asset id={} type={} refCount={} loaded={}", id, expected, handle.refCount(), handle.isLoaded());
        return new AssetRef<>(this, id, value);
    }

    private AssetHandle requireHandle(String id) {
        ensureActive();
        AssetHandle handle = handles.get(id);
        if (handle == null) {
            throw new IllegalArgumentException("Unknown asset id: " + id);
        }
        return handle;
    }

    private void registerFromIndexJson(
            String idPrefix,
            String json,
            java.util.function.Function<String, AssetSource> sourceFactory,
            DirectoryScanOptions opts
    ) {
        Map<String, String> paths = parseIndexJson(json);
        for (Map.Entry<String, String> e : paths.entrySet()) {
            opts.inferType(e.getValue()).ifPresent(type -> {
                String id = idPrefix + stripExtension(e.getKey()).replace('/', '.');
                int fontSize = type == AssetType.FONT ? opts.defaultFontPixelHeight() : 0;
                register(new AssetDescriptor(id, type, sourceFactory.apply(e.getValue()), fontSize));
            });
        }
    }

    private static Map<String, String> parseIndexJson(String json) {
        Map<String, String> paths = new LinkedHashMap<>();
        int filesIdx = json.indexOf("\"files\":{");
        if (filesIdx < 0) {
            return paths;
        }
        int brace = json.indexOf('{', filesIdx + 8);
        int i = brace + 1;
        while (i < json.length()) {
            i = skipWs(json, i);
            if (i < json.length() && json.charAt(i) == '}') {
                break;
            }
            if (json.charAt(i) != '"') {
                break;
            }
            int keyEnd = json.indexOf('"', i + 1);
            String key = json.substring(i + 1, keyEnd);
            int colon = json.indexOf('"', keyEnd + 1);
            int valEnd = json.indexOf('"', colon + 1);
            String value = json.substring(colon + 1, valEnd);
            paths.put(key, value);
            i = valEnd + 1;
            i = skipWs(json, i);
            if (i < json.length() && json.charAt(i) == ',') {
                i++;
            }
        }
        return paths;
    }

    private static int skipWs(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    private static String stripExtension(String path) {
        int slash = path.lastIndexOf('/');
        int dot = path.lastIndexOf('.');
        if (dot > slash) {
            return path.substring(0, dot);
        }
        return path;
    }

    private static String describeSource(AssetSource source) {
        if (source instanceof AssetSource.Classpath classpath) {
            return "classpath:" + classpath.path();
        }
        if (source instanceof AssetSource.File file) {
            return "file:" + LogHelper.describePath(file.path());
        }
        if (source instanceof AssetSource.PackSlice slice) {
            return "pack:" + slice.entry().hint();
        }
        return source.toString();
    }

    private void ensureActive() {
        if (disposed) {
            throw new IllegalStateException("ResourceManager has been disposed");
        }
    }

    static final class LoadContext {
        private final AudioContext audio;

        LoadContext(AudioContext audio) {
            this.audio = audio;
        }

        AudioContext audio() {
            return audio;
        }
    }
}
