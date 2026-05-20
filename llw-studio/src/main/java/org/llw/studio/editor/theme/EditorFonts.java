package org.llw.studio.editor.theme;

import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.ImGuiIO;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loads the default UI font and merges Font Awesome icons for {@link EditorIcons}.
 */
public final class EditorFonts {
  private static final float FONT_SIZE = 14f;
  private static final float ICON_MIN_ADVANCE = 14f;

  private EditorFonts() {}

  public static void load() {
    ImGuiIO io = ImGui.getIO();
    io.getFonts().addFontDefault();

    byte[] iconFont = loadResource("/fonts/fa-solid-900.ttf");
    ImFontConfig config = new ImFontConfig();
    config.setMergeMode(true);
    config.setPixelSnapH(true);
    config.setGlyphMinAdvanceX(ICON_MIN_ADVANCE);
    config.setGlyphMaxAdvanceX(ICON_MIN_ADVANCE);
    io.getFonts().addFontFromMemoryTTF(iconFont, FONT_SIZE, config, EditorIcons.ICON_RANGE);
    io.getFonts().build();
    config.destroy();
  }

  private static byte[] loadResource(String path) {
    try (InputStream in = EditorFonts.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalStateException("Missing font resource: " + path);
      }
      return in.readAllBytes();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load font resource: " + path, e);
    }
  }
}
