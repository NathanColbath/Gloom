package org.llw.util.log;

/**
 * Writes session and capability banners at startup.
 */
public final class EnvironmentLog {
    private static final Logger LOG = Log.get("llw.util.log");

    private EnvironmentLog() {
    }

    public static void logSessionStart() {
        LOG.debug("=== LLW session start ===");
        LOG.debug("java.version={}", System.getProperty("java.version"));
        LOG.debug("os.name={} os.arch={}", System.getProperty("os.name"), System.getProperty("os.arch"));
        LOG.debug("user.dir={}", System.getProperty("user.dir"));
    }

    public static void logOpenGl(String vendor, String renderer, String version, String glslVersion, String glfwVersion) {
        LOG.debug("OpenGL vendor={}", vendor);
        LOG.debug("OpenGL renderer={}", renderer);
        LOG.debug("OpenGL version={}", version);
        LOG.debug("GLSL version={}", glslVersion);
        LOG.debug("GLFW version={}", glfwVersion);
    }

    public static void logOpenAl(String device, String version, String renderer) {
        LOG.debug("OpenAL device={}", device);
        LOG.debug("OpenAL version={}", version);
        LOG.debug("OpenAL renderer={}", renderer);
    }

    public static void logSystemFontCatalog(int faceCount) {
        LOG.debug("System font catalog indexed {} faces", faceCount);
    }
}
