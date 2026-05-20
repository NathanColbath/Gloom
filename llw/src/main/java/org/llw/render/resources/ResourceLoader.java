package org.llw.render.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.llw.util.log.Log;
import org.llw.util.log.LogHelper;
import org.llw.util.log.Loggers;
import org.llw.util.log.Logger;

/**
 * Loads classpath resources as UTF-8 text or raw bytes.
 *
 * <p>This class cannot be instantiated.
 */
public final class ResourceLoader {
    private static final Logger log = Log.get(Loggers.RENDER_RESOURCES);

    private ResourceLoader() {}

    /**
     * Reads a classpath resource as a UTF-8 string.
     *
     * @param classpathPath path relative to the class loader root (for example
     *                      {@code llw/render/fonts/Roboto-Regular.ttf})
     * @return the full text content of the resource
     * @throws IllegalArgumentException if no resource exists at {@code classpathPath}
     * @throws IllegalStateException if the resource exists but cannot be read
     */
    public static String loadText(String classpathPath) {
        try (InputStream stream = ResourceLoader.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (stream == null) {
                throw new IllegalArgumentException("Resource not found: " + classpathPath);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw LogHelper.logAndThrow(log, "Failed to load resource: " + classpathPath, e);
        }
    }

    /**
     * Reads a classpath resource as a byte array.
     *
     * @param classpathPath path relative to the class loader root
     * @return a copy of the resource's raw bytes
     * @throws IllegalArgumentException if no resource exists at {@code classpathPath}
     * @throws IllegalStateException if the resource exists but cannot be read
     */
    public static byte[] loadBytes(String classpathPath) {
        try (InputStream stream = ResourceLoader.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (stream == null) {
                throw new IllegalArgumentException("Resource not found: " + classpathPath);
            }
            byte[] bytes = stream.readAllBytes();
            log.debug("Loaded classpath resource path={} bytes={}", classpathPath, bytes.length);
            return bytes;
        } catch (IOException e) {
            throw LogHelper.logAndThrow(log, "Failed to load resource: " + classpathPath, e);
        }
    }
}
