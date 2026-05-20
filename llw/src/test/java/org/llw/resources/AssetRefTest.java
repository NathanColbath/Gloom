package org.llw.resources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetRefTest {

  @Test
  void tryWithResourcesReleases(@TempDir Path temp) throws Exception {
    try (ResourceTestHarness harness = ResourceTestHarness.assumeAvailable()) {
      harness.resources.registerTextureFile("tex", TestAssets.writePng(temp));

      try (var ref = harness.resources.acquireTexture("tex")) {
        assertTrue(harness.resources.isLoaded("tex"));
        assertEquals(1, harness.resources.refCount("tex"));
        ref.get();
      }
      assertFalse(harness.resources.isLoaded("tex"));
      assertEquals(0, harness.resources.refCount("tex"));
    }
  }

  @Test
  void doubleReleaseIsNoOp(@TempDir Path temp) throws Exception {
    try (ResourceTestHarness harness = ResourceTestHarness.assumeAvailable()) {
      harness.resources.registerTextureFile("tex", TestAssets.writePng(temp));
      var ref = harness.resources.acquireTexture("tex");
      ref.release();
      ref.release();
      assertEquals(0, harness.resources.refCount("tex"));
    }
  }

  @Test
  void getAfterReleaseThrows(@TempDir Path temp) throws Exception {
    try (ResourceTestHarness harness = ResourceTestHarness.assumeAvailable()) {
      harness.resources.registerTextureFile("tex", TestAssets.writePng(temp));
      var ref = harness.resources.acquireTexture("tex");
      ref.release();
      assertThrows(IllegalStateException.class, ref::get);
    }
  }
}
