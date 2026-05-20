package org.llw.resources.pack;

import org.llw.resources.AssetType;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManifestJsonTest {

  @Test
  void roundTrip() {
    Map<String, AssetPackManifest.Entry> entries = new LinkedHashMap<>();
    entries.put("player", new AssetPackManifest.Entry(AssetType.TEXTURE, 0, 100, "player.png", 0));
    entries.put("ui_font", new AssetPackManifest.Entry(AssetType.FONT, 100, 2048, "Roboto.ttf", 24));
    entries.put("click", new AssetPackManifest.Entry(AssetType.SOUND, 2148, 512, "click.wav", 0));

    String json = ManifestJson.write(entries);
    AssetPackManifest parsed = ManifestJson.parse(json);

    assertEquals(1, parsed.version());
    assertEquals(3, parsed.entries().size());
    assertEquals(AssetType.FONT, parsed.entries().get("ui_font").type());
    assertEquals(24, parsed.entries().get("ui_font").fontSize());
    assertEquals("click.wav", parsed.entries().get("click").hint());
    assertTrue(json.contains("\"version\":1"));
  }
}
