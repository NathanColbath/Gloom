package org.llw.resources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceManagerTest {

  @Test
  void acquireReleaseAutoUnloads(@TempDir Path temp) throws Exception {
    try (ResourceTestHarness harness = ResourceTestHarness.assumeAvailable()) {
      ResourceManager rm = harness.resources;
      rm.registerTextureFile("tex", TestAssets.writePng(temp));

      assertFalse(rm.isLoaded("tex"));
      var ref = rm.acquireTexture("tex");
      assertTrue(rm.isLoaded("tex"));
      ref.release();
      assertFalse(rm.isLoaded("tex"));
    }
  }

  @Test
  void pinLoadAllKeepsWarm(@TempDir Path temp) throws Exception {
    try (ResourceTestHarness harness = ResourceTestHarness.assumeAvailable()) {
      ResourceManager rm = harness.resources;
      rm.registerTextureFile("tex", TestAssets.writePng(temp));
      rm.loadAll();
      assertTrue(rm.isLoaded("tex"));
      assertTrue(rm.refCount("tex") >= 1);
      rm.unloadAll();
      assertFalse(rm.isLoaded("tex"));
    }
  }

  @Test
  void refCountTracksMultipleAcquires(@TempDir Path temp) throws Exception {
    try (ResourceTestHarness harness = ResourceTestHarness.assumeAvailable()) {
      ResourceManager rm = harness.resources;
      rm.registerTextureFile("tex", TestAssets.writePng(temp));
      var a = rm.acquireTexture("tex");
      var b = rm.acquireTexture("tex");
      assertEquals(2, rm.refCount("tex"));
      a.release();
      assertTrue(rm.isLoaded("tex"));
      b.release();
      assertFalse(rm.isLoaded("tex"));
    }
  }

  @Test
  void disposeForceUnloads(@TempDir Path temp) throws Exception {
    try (ResourceTestHarness harness = ResourceTestHarness.assumeAvailable()) {
      ResourceManager rm = harness.resources;
      rm.registerTextureFile("tex", TestAssets.writePng(temp));
      rm.acquireTexture("tex");
      assertTrue(rm.isLoaded("tex"));
      rm.dispose();
    }
  }
}
