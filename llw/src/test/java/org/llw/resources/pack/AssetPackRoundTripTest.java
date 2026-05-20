package org.llw.resources.pack;

import org.llw.resources.AssetType;
import org.llw.resources.TestAssets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AssetPackRoundTripTest {

  @Test
  void writerReaderSliceMatchesSource(@TempDir Path temp) throws Exception {
    Path png = TestAssets.writePng(temp);
    byte[] wav = TestAssets.loadClasspath("llw/audio/samples/click.wav");

    Path wavFile = temp.resolve("click.wav");
    Files.write(wavFile, wav);

    Map<String, AssetPackManifest.PackEntry> entries = new LinkedHashMap<>();
    entries.put("pixel", new AssetPackManifest.PackEntry(AssetType.TEXTURE, png));
    entries.put("click", new AssetPackManifest.PackEntry(AssetType.SOUND, wavFile));

    Path packPath = temp.resolve("game.pack");
    AssetPackWriter.write(packPath, entries);

    AssetPackReader reader = AssetPackReader.fromFile(packPath);
    assertEquals(2, reader.manifest().entries().size());
    assertArrayEquals(TestAssets.PNG_1X1, reader.slice(reader.manifest().entries().get("pixel")));
    assertArrayEquals(wav, reader.slice(reader.manifest().entries().get("click")));
  }
}
