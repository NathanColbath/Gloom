package org.llw.resources;

import org.llw.resources.pack.AssetPackManifest;
import org.llw.resources.pack.AssetPackWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceManagerPackTest {

  @Test
  void loadPackAcquiresTextureAndSound(@TempDir Path temp) throws Exception {
    try (ResourceTestHarness harness = ResourceTestHarness.assumeAvailable()) {
      Path png = TestAssets.writePng(temp);
      byte[] wav = TestAssets.loadClasspath("llw/audio/samples/click.wav");
      Path wavFile = temp.resolve("click.wav");
      java.nio.file.Files.write(wavFile, wav);

      Map<String, AssetPackManifest.PackEntry> entries = new LinkedHashMap<>();
      entries.put("pixel", new AssetPackManifest.PackEntry(AssetType.TEXTURE, png));
      entries.put("click", new AssetPackManifest.PackEntry(AssetType.SOUND, wavFile));
      Path pack = temp.resolve("game.pack");
      AssetPackWriter.write(pack, entries);

      ResourceManager rm = harness.resources;
      rm.loadPackFile(pack);

      try (var tex = rm.acquireTexture("pixel")) {
        assertNotNull(tex.get());
        assertTrue(tex.get().size().width() >= 1);
      }
      try (var sound = rm.acquireSound("click")) {
        assertNotNull(sound.get());
      }
    }
  }
}
