package org.llw.studio.particles.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.llw.render.graphics.BlendMode;
import org.llw.studio.curves.CurveEvaluator;
import org.llw.studio.particles.model.ParticleSystemDocument;
import org.llw.studio.physics.PhysicsRaycastHit;
import org.llw.studio.physics.PhysicsWorld;

/**
 * CPU particle simulation for one {@link EmitterState}.
 */
public final class ParticleSimulator {
    private static final int MAX_SUB_EMITTER_DEPTH = 4;
    public static final int MAX_SUB_SPAWNS_PER_FRAME = 64;
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    private final float[] colorScratch = new float[4];
    private final List<SubEmitterRequest> subEmitterQueue = new ArrayList<>();

    public List<SubEmitterRequest> subEmitterQueue() {
        return subEmitterQueue;
    }

    /**
     * Immediately spawns {@code count} particles (script/API burst).
     */
    public void spawnBurst(EmitterState state, int count) {
        if (state == null || count <= 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            spawnOne(state, 0f, 0f, 0f, 0f, state.subEmitterDepth);
        }
    }

    /**
     * @param state         emitter runtime state
     * @param deltaTime     frame delta seconds
     * @param emitting      when false, only integrate existing particles
     * @param physicsWorld  optional world collision; may be null
     */
    public void step(
            EmitterState state,
            float deltaTime,
            boolean emitting,
            PhysicsWorld physicsWorld
    ) {
        if (state == null || state.document == null || deltaTime <= 0f) {
            return;
        }
        ParticleSystemDocument doc = state.document;
        state.emitterTime += deltaTime;
        if (!doc.looping && state.emitterTime > doc.duration) {
            emitting = false;
        }

        if (emitting && state.playing) {
            spawnParticles(state, deltaTime);
        }
        integrateParticles(state, deltaTime, physicsWorld);
        state.pool.compact();
    }

    private void spawnParticles(EmitterState state, float deltaTime) {
        ParticleSystemDocument doc = state.document;
        var emission = doc.modules.emission;
        state.spawnAccumulator += emission.rateOverTime * deltaTime;
        while (state.spawnAccumulator >= 1f) {
            state.spawnAccumulator -= 1f;
            spawnOne(state, 0f, 0f, 0f, 0f, state.subEmitterDepth);
        }
        for (ParticleSystemDocument.Burst burst : emission.bursts) {
            if (Math.abs(state.emitterTime - burst.time) < deltaTime) {
                for (int i = 0; i < burst.count; i++) {
                    spawnOne(state, 0f, 0f, 0f, 0f, state.subEmitterDepth);
                }
            }
        }
    }

    /**
     * Spawns particles for sub-emitter requests queued during integration.
     */
    public void flushSubEmitters(
            EmitterState state,
            ParticleWorld world,
            org.llw.studio.assets.AssetDatabase assets,
            int frameBudget
    ) {
        int spawned = 0;
        for (int i = 0; i < subEmitterQueue.size() && spawned < frameBudget; i++) {
            SubEmitterRequest request = subEmitterQueue.get(i);
            EmitterState child = world == null ? null : world.resolveSubEmitter(request.systemGuid(), assets);
            if (child == null || child.document == null) {
                continue;
            }
            if (request.depth() >= MAX_SUB_EMITTER_DEPTH) {
                continue;
            }
            for (int n = 0; n < 3 && spawned < frameBudget; n++) {
                spawnOne(child, request.x(), request.y(), request.vx(), request.vy(), request.depth() + 1);
                spawned++;                                               
            }
        }
        subEmitterQueue.clear();
    }

    private void spawnOne(
            EmitterState state,
            float offsetX,
            float offsetY,
            float inheritVx,
            float inheritVy,
            
            int depth
    ) {
        Particle particle = state.pool.acquire();
        if (particle == null) {
            return;
        }
        ParticleSystemDocument doc = state.document;
        float[] shapeOffset = sampleShape(doc.modules.shape, state);
        float spawnX = state.worldX + shapeOffset[0] + offsetX;
        float spawnY = state.worldY + shapeOffset[1] + offsetY;
        particle.randomSeed = ThreadLocalRandom.current().nextInt();
        particle.maxLife = Math.max(0.05f, CurveEvaluator.evaluate(
                doc.modules.lifetime.curve,
                0f,
                particle.randomSeed
        ));
        particle.life = particle.maxLife;
        float speed = CurveEvaluator.randomTwoConstants(doc.modules.velocity.speed, particle.randomSeed);
        float angleDeg = CurveEvaluator.randomTwoConstants(doc.modules.velocity.angle, particle.randomSeed + 17);
        if (!state.localSpace) {
            angleDeg += state.worldRotation;
        }
        float angleRad = angleDeg * DEG_TO_RAD;
        particle.vx = (float) Math.cos(angleRad) * speed + inheritVx;
        particle.vy = (float) Math.sin(angleRad) * speed + inheritVy;
        particle.x = spawnX;
        particle.y = spawnY;
        float startSize = doc.modules.sizeOverLifetime.enabled
                ? CurveEvaluator.evaluate(doc.modules.sizeOverLifetime.curve, 0f, particle.randomSeed)
                : 32f;
        particle.size = startSize;
        CurveEvaluator.evaluateColor(doc.modules.colorOverLifetime.gradient, 0f, colorScratch);
        particle.r = colorScratch[0];
        particle.g = colorScratch[1];
        particle.b = colorScratch[2];
        particle.a = colorScratch[3];
        particle.rotation = doc.modules.rotationOverLifetime.enabled
                ? CurveEvaluator.evaluate(doc.modules.rotationOverLifetime.curve, 0f, particle.randomSeed)
                : 0f;
        particle.frameIndex = 0;
        particle.frameTime = 0f;
        particle.trailCount = 0;
        particle.trailDistance = 0f;
        particle.alive = true;
        state.subEmitterDepth = depth;
        queueSubEmitters(state, particle, "onBirth");
    }

    private void integrateParticles(EmitterState state, float deltaTime, PhysicsWorld physicsWorld) {
        ParticleSystemDocument doc = state.document;
        ParticlePool pool = state.pool;
        for (int i = 0; i < pool.capacity(); i++) {
            Particle particle = pool.particleAt(i);
            if (!particle.alive) {
                continue;
            }
            particle.life -= deltaTime;
            if (particle.life <= 0f) {
                queueSubEmitters(state, particle, "onDeath");
                particle.reset();
                continue;
            }
            float normalized = 1f - (particle.life / particle.maxLife);
            if (doc.modules.force.gravity != 0f) {
                particle.vy += doc.modules.force.gravity * deltaTime;
            }
            if (doc.modules.force.drag > 0f) {
                float drag = Math.max(0f, 1f - doc.modules.force.drag * deltaTime);
                particle.vx *= drag;
                particle.vy *= drag;
            }
            if (doc.modules.noise.enabled) {
                float nx = particle.x * doc.modules.noise.frequency
                        + state.emitterTime * doc.modules.noise.scrollSpeed;
                float ny = particle.y * doc.modules.noise.frequency;
                float n = ParticleNoise.sample(nx, ny) * doc.modules.noise.strength;
                particle.vx += n * deltaTime;
                particle.vy += ParticleNoise.sample(nx + 13.7f, ny + 4.2f) * doc.modules.noise.strength * deltaTime;
            }
            particle.x += particle.vx * deltaTime;
            particle.y += particle.vy * deltaTime;
            if (doc.modules.collision.enabled && physicsWorld != null) {
                applyCollision(state, particle, physicsWorld, normalized);
            }
            if (doc.modules.sizeOverLifetime.enabled) {
                particle.size = CurveEvaluator.evaluate(
                        doc.modules.sizeOverLifetime.curve,
                        normalized,
                        particle.randomSeed
                );
            }
            if (doc.modules.colorOverLifetime.enabled) {
                CurveEvaluator.evaluateColor(doc.modules.colorOverLifetime.gradient, normalized, colorScratch);
                particle.r = colorScratch[0];
                particle.g = colorScratch[1];
                particle.b = colorScratch[2];
                particle.a = colorScratch[3];
            }
            if (doc.modules.rotationOverLifetime.enabled) {
                particle.rotation = CurveEvaluator.evaluate(
                        doc.modules.rotationOverLifetime.curve,
                        normalized,
                        particle.randomSeed
                );
            }
            if (doc.modules.textureSheet.enabled && doc.modules.textureSheet.frameRate > 0f) {
                particle.frameTime += deltaTime;
                int frameCount = doc.modules.textureSheet.tilesX * doc.modules.textureSheet.tilesY;
                int frame = (int) (particle.frameTime * doc.modules.textureSheet.frameRate);
                if (doc.modules.textureSheet.cycle) {
                    particle.frameIndex = frame % Math.max(1, frameCount);
                } else {
                    particle.frameIndex = Math.min(frame, Math.max(0, frameCount - 1));
                }
            }
            appendTrail(state, particle);
        }
    }

    private void applyCollision(
            EmitterState state,
            Particle particle,
            PhysicsWorld physicsWorld,
            float normalized
    ) {
        float speed = (float) Math.hypot(particle.vx, particle.vy);
        if (speed < 0.001f) {
            return;
        }
        float dirX = particle.vx / speed;
        float dirY = particle.vy / speed;
        float radius = Math.max(1f, particle.size * 0.25f);
        PhysicsRaycastHit hit = physicsWorld.raycast(
                particle.x,
                particle.y,
                dirX,
                dirY,
                speed * 0.02f + radius,
                0xFFFF
        );
        if (hit == null) {
            return;
        }
        particle.vx = -particle.vx * state.document.modules.collision.bounce;
        particle.vy = -particle.vy * state.document.modules.collision.bounce;
        particle.life -= particle.maxLife * state.document.modules.collision.lifetimeLoss;
        queueSubEmitters(state, particle, "onCollision");
    }

    private void appendTrail(EmitterState state, Particle particle) {
        ParticleSystemDocument.TrailsModule trails = state.document.modules.trails;
        if (!trails.enabled) {
            return;
        }
        float dx = particle.x - (particle.trailCount > 0 ? particle.trailX[0] : particle.x);
        float dy = particle.y - (particle.trailCount > 0 ? particle.trailY[0] : particle.y);
        particle.trailDistance += (float) Math.hypot(dx, dy);
        if (particle.trailCount == 0 || particle.trailDistance >= trails.minVertexDistance) {
            shiftTrail(particle);
            particle.trailX[0] = particle.x;
            particle.trailY[0] = particle.y;
            particle.trailCount = Math.min(Particle.MAX_TRAIL_POINTS, particle.trailCount + 1);
            particle.trailDistance = 0f;
        }
    }

    private static void shiftTrail(Particle particle) {
        int count = Math.min(particle.trailCount, Particle.MAX_TRAIL_POINTS - 1);
        for (int i = count; i > 0; i--) {
            particle.trailX[i] = particle.trailX[i - 1];
            particle.trailY[i] = particle.trailY[i - 1];
        }
    }

    private void queueSubEmitters(EmitterState state, Particle particle, String trigger) {
        for (ParticleSystemDocument.SubEmitterModule sub : state.document.modules.subEmitters) {
            if (sub.systemGuid == null || sub.systemGuid.isBlank()) {
                continue;
            }
            if (!trigger.equalsIgnoreCase(sub.trigger)) {
                continue;
            }
            if (ThreadLocalRandom.current().nextFloat() > sub.probability) {
                continue;
            }
            subEmitterQueue.add(new SubEmitterRequest(
                    sub.systemGuid,
                    particle.x,
                    particle.y,
                    particle.vx * 0.5f,
                    particle.vy * 0.5f,
                    state.subEmitterDepth
            ));
        }
    }

    private static float[] sampleShape(ParticleSystemDocument.ShapeModule shape, EmitterState state) {
        float[] out = new float[2];
        String type = shape.type == null ? "point" : shape.type.toLowerCase();
        float scaleX = state.worldScaleX;
        float scaleY = state.worldScaleY;
        switch (type) {
            case "circle" -> {
                float angle = ThreadLocalRandom.current().nextFloat() * shape.arc * DEG_TO_RAD;
                float radius = shape.radius * ThreadLocalRandom.current().nextFloat();
                out[0] = (float) Math.cos(angle) * radius * scaleX;
                out[1] = (float) Math.sin(angle) * radius * scaleY;
            }
            case "box" -> {
                out[0] = (ThreadLocalRandom.current().nextFloat() - 0.5f) * shape.width * scaleX;
                out[1] = (ThreadLocalRandom.current().nextFloat() - 0.5f) * shape.height * scaleY;
            }
            default -> {
            }
        }
        return out;
    }

    public static BlendMode blendMode(String name) {
        if (name == null) {
            return BlendMode.ALPHA;
        }
        return switch (name.toUpperCase()) {
            case "ADDITIVE" -> BlendMode.ADDITIVE;
            case "MULTIPLY" -> BlendMode.MULTIPLY;
            case "NONE" -> BlendMode.NONE;
            default -> BlendMode.ALPHA;
        };
    }
}
