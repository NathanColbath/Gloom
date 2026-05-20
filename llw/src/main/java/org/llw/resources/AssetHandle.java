package org.llw.resources;

import org.llw.audio.SoundBuffer;
import org.llw.audio.resources.AudioLoader;
import org.llw.render.graphics.Font;
import org.llw.render.graphics.Texture2d;
import org.llw.render.resources.ResourceLoader;
import org.llw.util.log.Log;
import org.llw.util.log.LogHelper;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Mutable per-asset state: ref count, loaded object, and load/unload logic.
 */
final class AssetHandle {
    private static final Logger log = Log.get(Loggers.RESOURCES);

    private final AssetDescriptor descriptor;
    private int refCount;
    private Object object;

    AssetHandle(AssetDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    AssetDescriptor descriptor() {
        return descriptor;
    }

    int refCount() {
        return refCount;
    }

    boolean isLoaded() {
        return object != null;
    }

    @SuppressWarnings("unchecked")
    <T> T object() {
        return (T) object;
    }

    <T> T acquire(ResourceManager.LoadContext ctx) {
        boolean wasUnloaded = object == null;
        if (wasUnloaded) {
            object = load(ctx);
            log.debug("Lazy loaded asset id={} type={}", descriptor.id(), descriptor.type());
        }
        refCount++;
        return (T) object;
    }

    void release() {
        if (refCount <= 0) {
            return;
        }
        refCount--;
        if (refCount == 0) {
            unload();
        }
    }

    void forceUnload() {
        refCount = 0;
        unload();
    }

    private void unload() {
        if (object instanceof Texture2d texture) {
            texture.dispose();
        } else if (object instanceof Font font) {
            font.dispose();
        } else if (object instanceof SoundBuffer buffer) {
            buffer.dispose();
        }
        if (object != null) {
            log.debug("Unloaded asset id={} type={} class={}", descriptor.id(), descriptor.type(), object.getClass().getSimpleName());
        }
        object = null;
    }

    private Object load(ResourceManager.LoadContext ctx) {
        byte[] bytes = readBytes();
        return switch (descriptor.type()) {
            case TEXTURE -> Texture2d.fromBytes(bytes);
            case FONT -> Font.fromBytes(bytes, descriptor.fontSize());
            case SOUND -> ctx.audio().loadSoundBufferFromMemory(hintName(), bytes);
            case RAW -> bytes;
            case MUSIC -> throw new IllegalStateException("Music is opened via openMusic, not loaded into memory");
        };
    }

    private byte[] readBytes() {
        AssetSource source = descriptor.source();
        if (source instanceof AssetSource.Classpath classpath) {
            return ResourceLoader.loadBytes(classpath.path());
        }
        if (source instanceof AssetSource.File file) {
            try {
                return Files.readAllBytes(file.path());
            } catch (IOException e) {
                throw LogHelper.logAndThrow(log, "Failed to read asset file: " + file.path(), e);
            }
        }
        if (source instanceof AssetSource.PackSlice slice) {
            return slice.packReader().slice(slice.entry());
        }
        throw new IllegalStateException("Unknown source");
    }

    private String hintName() {
        AssetSource source = descriptor.source();
        if (source instanceof AssetSource.PackSlice slice) {
            return slice.entry().hint();
        }
        if (source instanceof AssetSource.Classpath classpath) {
            return classpath.path();
        }
        if (source instanceof AssetSource.File file) {
            return file.path().getFileName().toString();
        }
        return descriptor.id();
    }

    /** Opens a music stream from classpath/file/pack slice without loading fully into RAM. */
    org.llw.audio.resources.AudioStream openMusicStream() {
        AssetSource source = descriptor.source();
        if (source instanceof AssetSource.Classpath classpath) {
            return AudioLoader.openStreamFromClasspath(classpath.path());
        }
        if (source instanceof AssetSource.File file) {
            return AudioLoader.openStreamFromFile(file.path());
        }
        if (source instanceof AssetSource.PackSlice slice) {
            byte[] bytes = slice.packReader().slice(slice.entry());
            return AudioLoader.openStreamFromMemory(slice.entry().hint(), bytes);
        }
        throw new IllegalStateException("Unsupported music source");
    }
}
