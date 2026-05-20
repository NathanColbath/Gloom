package org.llw.studio.assets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextureImageSizeTest {
  private static final Path PLAYER_SHEET = resolveStudioPlayerSheet();

  private static Path resolveStudioPlayerSheet() {
    Path[] candidates = {
        Paths.get("studio-project/Assets/PLayer 1.png"),
        Paths.get("llw-studio/studio-project/Assets/PLayer 1.png"),
    };
    for (Path candidate : candidates) {
      Path absolute = candidate.toAbsolutePath().normalize();
      if (Files.isRegularFile(absolute)) {
        return absolute;
      }
    }
    return candidates[0].toAbsolutePath().normalize();
  }

  @Test
  @EnabledIf("studioPlayerSheetExists")
  void readsStudioPlayerSheetDimensions() {
    var size = TextureImageSize.read(PLAYER_SHEET);
    assertTrue(size.width() >= 32);
    assertTrue(size.height() >= 32);
    assertEquals(384, size.width());
    assertEquals(256, size.height());
  }

  static boolean studioPlayerSheetExists() {
    return Files.isRegularFile(PLAYER_SHEET);
  }
}
