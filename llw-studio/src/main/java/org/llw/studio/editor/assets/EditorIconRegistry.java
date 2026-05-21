package org.llw.studio.editor.assets;

import imgui.ImGui;
import org.llw.render.graphics.Texture2d;
import org.llw.studio.assets.AssetType;
import org.llw.studio.assets.StudioAsset;
import org.llw.studio.editor.MainThreadQueue;
import org.llw.util.log.Log;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loads open-source Lucide icons from the Iconify API (with disk cache) and caches GPU textures.
 */
public final class EditorIconRegistry {
    private static final int ICON_SIZE = IconifyIconClient.defaultPixelSize();

    private final IconifyIconClient iconify = new IconifyIconClient();
    private final EditorIconDiskCache diskCache = EditorIconDiskCache.defaultCache();
    private final ExecutorService downloadExecutor = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "editor-icon-download");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<AssetIconKind, Texture2d> icons = new EnumMap<>(AssetIconKind.class);
    private final Map<AssetIconKind, Boolean> loading = new EnumMap<>(AssetIconKind.class);
    private MainThreadQueue mainThreadQueue;

    public void setMainThreadQueue(MainThreadQueue mainThreadQueue) {
        this.mainThreadQueue = mainThreadQueue;
    }

    /**
     * @param kind icon kind
     * @return OpenGL texture, or {@code null} while downloading
     */
    public Texture2d icon(AssetIconKind kind) {
        Texture2d cached = icons.get(kind);
        if (cached != null) {
            return cached;
        }
        requestDownload(kind);
        return null;
    }

    /**
     * @param asset studio asset
     * @return icon kind for browser display
     */
    public static AssetIconKind kindFor(StudioAsset asset) {
        if (asset == null) {
            return AssetIconKind.UNKNOWN;
        }
        if (asset.isFolder()) {
            return AssetIconKind.FOLDER;
        }
        return switch (asset.type()) {
            case TEXTURE -> AssetIconKind.TEXTURE;
            case SPRITE -> AssetIconKind.SPRITE;
            case SCRIPT -> AssetIconKind.SCRIPT;
            case SCENE -> AssetIconKind.SCENE;
            case PREFAB -> AssetIconKind.PREFAB;
            case AUDIO -> AssetIconKind.AUDIO;
            case FONT -> AssetIconKind.FONT;
            case ANIMATION -> AssetIconKind.ANIMATION;
            case ANIMATION_CLIP -> AssetIconKind.ANIMATION_CLIP;
            case SHADER_GRAPH -> AssetIconKind.SCRIPT;
            case PARTICLE_SYSTEM -> AssetIconKind.PARTICLE_SYSTEM;
            default -> AssetIconKind.UNKNOWN;
        };
    }

    /**
     * Draws the icon at the current cursor if loaded.
     *
     * @return {@code true} when drawn
     */
    public boolean draw(AssetIconKind kind, float size) {
        Texture2d texture = icon(kind);
        if (texture == null) {
            return false;
        }
        ImGui.image(texture.id(), size, size, 0f, 1f, 1f, 0f);
        return true;
    }

    /**
     * @return {@code true} when an image button was drawn and clicked this frame
     */
    public boolean imageButton(AssetIconKind kind, float size) {
        Texture2d texture = icon(kind);
        if (texture == null) {
            return false;
        }
        return ImGui.imageButton(texture.id(), size, size, 0f, 1f, 1f, 0f);
    }

    /** Downloads every icon from the API (or disk cache) on a background thread. */
    public void prefetchAll() {
        for (AssetIconKind kind : AssetIconKind.values()) {
            requestDownload(kind);
        }
    }

    /** Releases GPU textures and stops background downloads. */
    public void dispose() {
        downloadExecutor.shutdownNow();
        for (Texture2d texture : icons.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        icons.clear();
        loading.clear();
    }

    private void requestDownload(AssetIconKind kind) {
        if (icons.containsKey(kind) || Boolean.TRUE.equals(loading.get(kind))) {
            return;
        }
        loading.put(kind, true);
        downloadExecutor.submit(() -> downloadIcon(kind));
    }

    private void downloadIcon(AssetIconKind kind) {
        OpenSourceIconSpec spec = OpenSourceIconCatalog.spec(kind);
        byte[] svg = diskCache.read(spec, ICON_SIZE);
        if (svg == null) {
            try {
                svg = iconify.fetchSvg(spec, ICON_SIZE);
                diskCache.write(spec, ICON_SIZE, svg);
            } catch (IOException | InterruptedException ex) {
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                Log.get("EditorIconRegistry").warn(
                        "Icon download failed for {}: {}",
                        spec.iconifyPath(),
                        ex.getMessage()
                );
                uploadOnMainThread(kind, SvgIconRasterizer.flatFallback(ICON_SIZE, fallbackColor(kind)));
                return;
            }
        }
        byte[] svgBytes = svg;
        uploadOnMainThread(kind, null, svgBytes);
    }

    private void uploadOnMainThread(AssetIconKind kind, Texture2d ready) {
        Runnable upload = () -> {
            loading.remove(kind);
            icons.put(kind, ready);
        };
        if (mainThreadQueue != null) {
            mainThreadQueue.enqueue(upload);
        } else {
            upload.run();
        }
    }

    private void uploadOnMainThread(AssetIconKind kind, Texture2d ignored, byte[] svgBytes) {
        Runnable upload = () -> {
            loading.remove(kind);
            try {
                icons.put(kind, SvgIconRasterizer.rasterizeToTexture(svgBytes, ICON_SIZE));
            } catch (IOException ex) {
                Log.get("EditorIconRegistry").warn("Icon rasterize failed for {}: {}", kind, ex.getMessage());
                icons.put(kind, SvgIconRasterizer.flatFallback(ICON_SIZE, fallbackColor(kind)));
            }
        };
        if (mainThreadQueue != null) {
            mainThreadQueue.enqueue(upload);
        } else {
            upload.run();
        }
    }

    private static int fallbackColor(AssetIconKind kind) {
        return switch (kind) {
            case FOLDER -> 0x6B9BD1;
            case TEXTURE -> 0x7BC67E;
            case SPRITE -> 0x5FAF88;
            case SCRIPT -> 0xE8A0A0;
            case SCENE -> 0xE8C49A;
            case PREFAB -> 0xC49AE8;
            case AUDIO -> 0x8AB4E8;
            case FONT -> 0xE8D4A8;
            case ANIMATION -> 0xE8A878;
            case ANIMATION_STATE -> 0xA8B8E8;
            case ANIMATION_CLIP -> 0xE89898;
            case PARTICLE_SYSTEM -> 0xD8A8F0;
            case CHEVRON_RIGHT, CHEVRON_DOWN -> 0xB0B0B0;
            case UNKNOWN -> 0x909090;
        };
    }
}
