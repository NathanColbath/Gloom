package org.llw.studio.particles;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.llw.studio.curves.MinMaxCurve;
import org.llw.studio.particles.model.ParticleSystemDocument;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParticleSystemSerializerTest {
    @Test
    void roundTrip_preservesCoreFields(@TempDir Path tempDir) throws Exception {
        ParticleSystemDocument original = ParticleSystemSerializer.newDefault();
        original.maxParticles = 128;
        original.modules.emission.rateOverTime = 42f;
        original.modules.lifetime.curve.mode = MinMaxCurve.Mode.TWO_CONSTANTS;
        original.modules.lifetime.curve.min = 0.5f;
        original.modules.lifetime.curve.max = 2f;
        original.modules.renderer.spriteGuid = "sprite-guid";
        original.modules.noise.enabled = true;

        Path path = tempDir.resolve("fx.particle.json");
        ParticleSystemSerializer.save(path, original);
        ParticleSystemDocument loaded = ParticleSystemSerializer.load(path);

        assertEquals(128, loaded.maxParticles);
        assertEquals(42f, loaded.modules.emission.rateOverTime, 0.001f);
        assertEquals(MinMaxCurve.Mode.TWO_CONSTANTS, loaded.modules.lifetime.curve.mode);
        assertEquals("sprite-guid", loaded.modules.renderer.spriteGuid);
        assertTrue(loaded.modules.noise.enabled);
    }
}
