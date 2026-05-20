package org.llw.resources;

import org.llw.audio.AudioContext;
import org.llw.render.graphics.GraphicsContext;
import org.llw.render.window.Window;
import org.llw.render.window.WindowSettings;
import org.llw.util.log.Log;
import org.llw.util.log.LogConfig;
import org.llw.util.log.LogLevel;
import org.junit.jupiter.api.Assumptions;

import java.nio.file.Files;

/**
 * Hidden GLFW window + GL + OpenAL for integration tests.
 */
final class ResourceTestHarness implements AutoCloseable {
  final GraphicsContext graphics;
  final AudioContext audio;
  final ResourceManager resources;

  ResourceTestHarness() {
    try {
      Log.initForTests(LogConfig.builder()
              .logDir(Files.createTempDirectory("llw-test-logs"))
              .minLevel(LogLevel.WARN)
              .consoleEnabled(false)
              .build());
    } catch (java.io.IOException e) {
      throw new RuntimeException("Failed to create test log directory", e);
    }
    Window window = new Window(new WindowSettings().title("ResourceTest").size(64, 64));
    graphics = new GraphicsContext(window);
    try {
      audio = new AudioContext();
    } catch (RuntimeException ex) {
      graphics.dispose();
      throw ex;
    }
    resources = new ResourceManager(graphics.backend(), audio);
  }

  static ResourceTestHarness assumeAvailable() {
    try {
      return new ResourceTestHarness();
    } catch (RuntimeException ex) {
      Assumptions.abort("Graphics/audio unavailable: " + ex.getMessage());
      return null;
    }
  }

  @Override
  public void close() {
    resources.dispose();
    audio.dispose();
    graphics.dispose();
    Log.shutdown();
  }
}
