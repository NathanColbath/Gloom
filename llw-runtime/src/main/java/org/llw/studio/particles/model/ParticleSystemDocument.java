package org.llw.studio.particles.model;

import org.llw.studio.curves.Gradient;
import org.llw.studio.curves.MinMaxCurve;

import java.util.ArrayList;
import java.util.List;

/**
 * Authoring document for a {@code .particle.json} particle system asset.
 */
public final class ParticleSystemDocument {
    public static final int CURRENT_VERSION = 1;

    public int version = CURRENT_VERSION;
    public int maxParticles = 256;
    public float duration = 5f;
    public boolean looping = true;
    /** {@code world} or {@code local}. */
    public String simulationSpace = "world";
    public final ParticleModules modules = new ParticleModules();

    public ParticleSystemDocument copy() {
        ParticleSystemDocument copy = new ParticleSystemDocument();
        copy.version = version;
        copy.maxParticles = maxParticles;
        copy.duration = duration;
        copy.looping = looping;
        copy.simulationSpace = simulationSpace;
        copy.modules.copyFrom(modules);
        return copy;
    }

    public static final class ParticleModules {
        public EmissionModule emission = new EmissionModule();
        public LifetimeModule lifetime = new LifetimeModule();
        public ShapeModule shape = new ShapeModule();
        public VelocityModule velocity = new VelocityModule();
        public SizeOverLifetimeModule sizeOverLifetime = new SizeOverLifetimeModule();
        public ColorOverLifetimeModule colorOverLifetime = new ColorOverLifetimeModule();
        public RotationOverLifetimeModule rotationOverLifetime = new RotationOverLifetimeModule();
        public ForceModule force = new ForceModule();
        public RendererModule renderer = new RendererModule();
        public TextureSheetModule textureSheet = new TextureSheetModule();
        public final List<SubEmitterModule> subEmitters = new ArrayList<>();
        public NoiseModule noise = new NoiseModule();
        public TrailsModule trails = new TrailsModule();
        public CollisionModule collision = new CollisionModule();

        public void copyFrom(ParticleModules other) {
            emission = other.emission.copy();
            lifetime = other.lifetime.copy();
            shape = other.shape.copy();
            velocity = other.velocity.copy();
            sizeOverLifetime = other.sizeOverLifetime.copy();
            colorOverLifetime = other.colorOverLifetime.copy();
            rotationOverLifetime = other.rotationOverLifetime.copy();
            force = other.force.copy();
            renderer = other.renderer.copy();
            textureSheet = other.textureSheet.copy();
            subEmitters.clear();
            for (SubEmitterModule sub : other.subEmitters) {
                subEmitters.add(sub.copy());
            }
            noise = other.noise.copy();
            trails = other.trails.copy();
            collision = other.collision.copy();
        }
    }

    public static final class EmissionModule {
        public float rateOverTime = 20f;
        public final List<Burst> bursts = new ArrayList<>();

        public EmissionModule copy() {
            EmissionModule copy = new EmissionModule();
            copy.rateOverTime = rateOverTime;
            for (Burst burst : bursts) {
                copy.bursts.add(burst.copy());
            }
            return copy;
        }
    }

    public static final class Burst {
        public float time;
        public int count = 10;

        public Burst copy() {
            Burst copy = new Burst();
            copy.time = time;
            copy.count = count;
            return copy;
        }
    }

    public static final class LifetimeModule {
        public MinMaxCurve curve = defaultLifetime();

        public LifetimeModule copy() {
            LifetimeModule copy = new LifetimeModule();
            copy.curve = curve == null ? defaultLifetime() : curve.copy();
            return copy;
        }

        private static MinMaxCurve defaultLifetime() {
            MinMaxCurve curve = new MinMaxCurve();
            curve.mode = MinMaxCurve.Mode.CONSTANT;
            curve.constant = 1.2f;
            return curve;
        }
    }

    public static final class ShapeModule {
        /** {@code point}, {@code circle}, {@code box}. */
        public String type = "circle";
        public float radius = 0.5f;
        public float width = 1f;
        public float height = 1f;
        public float arc = 360f;

        public ShapeModule copy() {
            ShapeModule copy = new ShapeModule();
            copy.type = type;
            copy.radius = radius;
            copy.width = width;
            copy.height = height;
            copy.arc = arc;
            return copy;
        }
    }

    public static final class VelocityModule {
        public MinMaxCurve speed = defaultSpeed();
        public MinMaxCurve angle = defaultAngle();

        public VelocityModule copy() {
            VelocityModule copy = new VelocityModule();
            copy.speed = speed == null ? defaultSpeed() : speed.copy();
            copy.angle = angle == null ? defaultAngle() : angle.copy();
            return copy;
        }

        private static MinMaxCurve defaultSpeed() {
            MinMaxCurve curve = new MinMaxCurve();
            curve.mode = MinMaxCurve.Mode.TWO_CONSTANTS;
            curve.min = 40f;
            curve.max = 120f;
            return curve;
        }

        private static MinMaxCurve defaultAngle() {
            MinMaxCurve curve = new MinMaxCurve();
            curve.mode = MinMaxCurve.Mode.TWO_CONSTANTS;
            curve.min = 250f;
            curve.max = 290f;
            return curve;
        }
    }

    public static final class SizeOverLifetimeModule {
        public boolean enabled = true;
        public MinMaxCurve curve = defaultSize();

        public SizeOverLifetimeModule copy() {
            SizeOverLifetimeModule copy = new SizeOverLifetimeModule();
            copy.enabled = enabled;
            copy.curve = curve == null ? defaultSize() : curve.copy();
            return copy;
        }

        private static MinMaxCurve defaultSize() {
            MinMaxCurve curve = new MinMaxCurve();
            curve.mode = MinMaxCurve.Mode.CONSTANT;
            curve.constant = 32f;
            return curve;
        }
    }

    public static final class ColorOverLifetimeModule {
        public boolean enabled = true;
        public Gradient gradient = defaultGradient();

        public ColorOverLifetimeModule copy() {
            ColorOverLifetimeModule copy = new ColorOverLifetimeModule();
            copy.enabled = enabled;
            copy.gradient = gradient == null ? defaultGradient() : gradient.copy();
            return copy;
        }

        private static Gradient defaultGradient() {
            Gradient gradient = new Gradient();
            gradient.keys.add(new Gradient.ColorKey(0f, 1f, 1f, 1f, 1f));
            gradient.keys.add(new Gradient.ColorKey(1f, 1f, 1f, 1f, 0f));
            return gradient;
        }
    }

    public static final class RotationOverLifetimeModule {
        public boolean enabled;
        public MinMaxCurve curve = new MinMaxCurve();

        public RotationOverLifetimeModule copy() {
            RotationOverLifetimeModule copy = new RotationOverLifetimeModule();
            copy.enabled = enabled;
            copy.curve = curve == null ? new MinMaxCurve() : curve.copy();
            return copy;
        }
    }

    public static final class ForceModule {
        public float gravity;
        public float drag = 0.1f;

        public ForceModule copy() {
            ForceModule copy = new ForceModule();
            copy.gravity = gravity;
            copy.drag = drag;
            return copy;
        }
    }

    public static final class RendererModule {
        public String spriteGuid = "";
        public String blendMode = "ADDITIVE";
        public int sortingOrder;
        public String shaderGraphGuid = "";

        public RendererModule copy() {
            RendererModule copy = new RendererModule();
            copy.spriteGuid = spriteGuid;
            copy.blendMode = blendMode;
            copy.sortingOrder = sortingOrder;
            copy.shaderGraphGuid = shaderGraphGuid;
            return copy;
        }
    }

    public static final class TextureSheetModule {
        public boolean enabled;
        public int tilesX = 1;
        public int tilesY = 1;
        public float frameRate = 12f;
        public boolean cycle = true;

        public TextureSheetModule copy() {
            TextureSheetModule copy = new TextureSheetModule();
            copy.enabled = enabled;
            copy.tilesX = Math.max(1, tilesX);
            copy.tilesY = Math.max(1, tilesY);
            copy.frameRate = frameRate;
            copy.cycle = cycle;
            return copy;
        }
    }

    public static final class SubEmitterModule {
        public String systemGuid = "";
        /** {@code onBirth}, {@code onDeath}, {@code onCollision}. */
        public String trigger = "onDeath";
        public float probability = 1f;

        public SubEmitterModule copy() {
            SubEmitterModule copy = new SubEmitterModule();
            copy.systemGuid = systemGuid;
            copy.trigger = trigger;
            copy.probability = probability;
            return copy;
        }
    }

    public static final class NoiseModule {
        public boolean enabled;
        public float strength = 10f;
        public float frequency = 0.5f;
        public float scrollSpeed = 1f;

        public NoiseModule copy() {
            NoiseModule copy = new NoiseModule();
            copy.enabled = enabled;
            copy.strength = strength;
            copy.frequency = frequency;
            copy.scrollSpeed = scrollSpeed;
            return copy;
        }
    }

    public static final class TrailsModule {
        public boolean enabled;
        public float lifetime = 0.3f;
        public float width = 4f;
        public float minVertexDistance = 8f;

        public TrailsModule copy() {
            TrailsModule copy = new TrailsModule();
            copy.enabled = enabled;
            copy.lifetime = lifetime;
            copy.width = width;
            copy.minVertexDistance = minVertexDistance;
            return copy;
        }
    }

    public static final class CollisionModule {
        public boolean enabled;
        public String mode = "world";
        public float bounce = 0.3f;
        public float lifetimeLoss = 0.1f;

        public CollisionModule copy() {
            CollisionModule copy = new CollisionModule();
            copy.enabled = enabled;
            copy.mode = mode;
            copy.bounce = bounce;
            copy.lifetimeLoss = lifetimeLoss;
            return copy;
        }
    }
}
