package org.llw.studio.particles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.llw.studio.curves.Gradient;
import org.llw.studio.curves.MinMaxCurve;
import org.llw.studio.particles.model.ParticleSystemDocument;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

/**
 * JSON persistence for {@link ParticleSystemDocument} ({@code .particle.json}).
 */
public final class ParticleSystemSerializer {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ParticleSystemSerializer() {
    }

    public static void save(Path path, ParticleSystemDocument document) throws IOException {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("version", document.version);
        root.put("maxParticles", document.maxParticles);
        root.put("duration", document.duration);
        root.put("looping", document.looping);
        root.put("simulationSpace", document.simulationSpace == null ? "world" : document.simulationSpace);
        root.set("modules", writeModules(document.modules));
        Files.createDirectories(path.getParent());
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), root);
    }

    public static ParticleSystemDocument load(Path path) throws IOException {
        JsonNode root = MAPPER.readTree(path.toFile());
        ParticleSystemDocument document = new ParticleSystemDocument();
        document.version = root.path("version").asInt(ParticleSystemDocument.CURRENT_VERSION);
        document.maxParticles = root.path("maxParticles").asInt(256);
        document.duration = (float) root.path("duration").asDouble(5.0);
        document.looping = root.path("looping").asBoolean(true);
        document.simulationSpace = root.path("simulationSpace").asText("world");
        readModules(document.modules, root.path("modules"));
        return document;
    }

    public static ParticleSystemDocument newDefault() {
        ParticleSystemDocument document = new ParticleSystemDocument();
        document.modules.renderer.blendMode = "ALPHA";
        return document;
    }

    private static ObjectNode writeModules(ParticleSystemDocument.ParticleModules modules) {
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode emission = root.putObject("emission");
        emission.put("rateOverTime", modules.emission.rateOverTime);
        ArrayNode bursts = emission.putArray("bursts");
        for (ParticleSystemDocument.Burst burst : modules.emission.bursts) {
            ObjectNode b = bursts.addObject();
            b.put("time", burst.time);
            b.put("count", burst.count);
        }
        root.putObject("lifetime").set("curve", writeCurve(modules.lifetime.curve));
        ObjectNode shape = root.putObject("shape");
        shape.put("type", modules.shape.type);
        shape.put("radius", modules.shape.radius);
        shape.put("width", modules.shape.width);
        shape.put("height", modules.shape.height);
        shape.put("arc", modules.shape.arc);
        ObjectNode velocity = root.putObject("velocity");
        velocity.set("speed", writeCurve(modules.velocity.speed));
        velocity.set("angle", writeCurve(modules.velocity.angle));
        ObjectNode size = root.putObject("sizeOverLifetime");
        size.put("enabled", modules.sizeOverLifetime.enabled);
        size.set("curve", writeCurve(modules.sizeOverLifetime.curve));
        ObjectNode color = root.putObject("colorOverLifetime");
        color.put("enabled", modules.colorOverLifetime.enabled);
        color.set("gradient", writeGradient(modules.colorOverLifetime.gradient));
        ObjectNode rotation = root.putObject("rotationOverLifetime");
        rotation.put("enabled", modules.rotationOverLifetime.enabled);
        rotation.set("curve", writeCurve(modules.rotationOverLifetime.curve));
        ObjectNode force = root.putObject("force");
        force.put("gravity", modules.force.gravity);
        force.put("drag", modules.force.drag);
        ObjectNode renderer = root.putObject("renderer");
        renderer.put("spriteGuid", modules.renderer.spriteGuid == null ? "" : modules.renderer.spriteGuid);
        renderer.put("blendMode", modules.renderer.blendMode);
        renderer.put("sortingOrder", modules.renderer.sortingOrder);
        renderer.put("shaderGraphGuid", modules.renderer.shaderGraphGuid == null ? "" : modules.renderer.shaderGraphGuid);
        ObjectNode sheet = root.putObject("textureSheet");
        sheet.put("enabled", modules.textureSheet.enabled);
        sheet.put("tilesX", modules.textureSheet.tilesX);
        sheet.put("tilesY", modules.textureSheet.tilesY);
        sheet.put("frameRate", modules.textureSheet.frameRate);
        sheet.put("cycle", modules.textureSheet.cycle);
        ArrayNode subs = root.putArray("subEmitters");
        for (ParticleSystemDocument.SubEmitterModule sub : modules.subEmitters) {
            ObjectNode s = subs.addObject();
            s.put("systemGuid", sub.systemGuid);
            s.put("trigger", sub.trigger);
            s.put("probability", sub.probability);
        }
        ObjectNode noise = root.putObject("noise");
        noise.put("enabled", modules.noise.enabled);
        noise.put("strength", modules.noise.strength);
        noise.put("frequency", modules.noise.frequency);
        noise.put("scrollSpeed", modules.noise.scrollSpeed);
        ObjectNode trails = root.putObject("trails");
        trails.put("enabled", modules.trails.enabled);
        trails.put("lifetime", modules.trails.lifetime);
        trails.put("width", modules.trails.width);
        trails.put("minVertexDistance", modules.trails.minVertexDistance);
        ObjectNode collision = root.putObject("collision");
        collision.put("enabled", modules.collision.enabled);
        collision.put("mode", modules.collision.mode);
        collision.put("bounce", modules.collision.bounce);
        collision.put("lifetimeLoss", modules.collision.lifetimeLoss);
        return root;
    }

    private static void readModules(ParticleSystemDocument.ParticleModules modules, JsonNode root) {
        if (!root.isObject()) {
            return;
        }
        JsonNode emission = root.path("emission");
        modules.emission.rateOverTime = (float) emission.path("rateOverTime").asDouble(20.0);
        modules.emission.bursts.clear();
        for (JsonNode burstJson : emission.path("bursts")) {
            ParticleSystemDocument.Burst burst = new ParticleSystemDocument.Burst();
            burst.time = (float) burstJson.path("time").asDouble();
            burst.count = burstJson.path("count").asInt(10);
            modules.emission.bursts.add(burst);
        }
        modules.lifetime.curve = readCurve(root.path("lifetime").path("curve"));
        JsonNode shape = root.path("shape");
        modules.shape.type = shape.path("type").asText("circle");
        modules.shape.radius = (float) shape.path("radius").asDouble(0.5);
        modules.shape.width = (float) shape.path("width").asDouble(1.0);
        modules.shape.height = (float) shape.path("height").asDouble(1.0);
        modules.shape.arc = (float) shape.path("arc").asDouble(360.0);
        JsonNode velocity = root.path("velocity");
        modules.velocity.speed = readCurve(velocity.path("speed"));
        modules.velocity.angle = readCurve(velocity.path("angle"));
        JsonNode size = root.path("sizeOverLifetime");
        modules.sizeOverLifetime.enabled = size.path("enabled").asBoolean(true);
        modules.sizeOverLifetime.curve = readCurve(size.path("curve"));
        JsonNode color = root.path("colorOverLifetime");
        modules.colorOverLifetime.enabled = color.path("enabled").asBoolean(true);
        modules.colorOverLifetime.gradient = readGradient(color.path("gradient"));
        JsonNode rotation = root.path("rotationOverLifetime");
        modules.rotationOverLifetime.enabled = rotation.path("enabled").asBoolean(false);
        modules.rotationOverLifetime.curve = readCurve(rotation.path("curve"));
        JsonNode force = root.path("force");
        modules.force.gravity = (float) force.path("gravity").asDouble();
        modules.force.drag = (float) force.path("drag").asDouble(0.1);
        JsonNode renderer = root.path("renderer");
        modules.renderer.spriteGuid = renderer.path("spriteGuid").asText("");
        modules.renderer.blendMode = renderer.path("blendMode").asText("ADDITIVE");
        modules.renderer.sortingOrder = renderer.path("sortingOrder").asInt();
        modules.renderer.shaderGraphGuid = renderer.path("shaderGraphGuid").asText("");
        JsonNode sheet = root.path("textureSheet");
        modules.textureSheet.enabled = sheet.path("enabled").asBoolean(false);
        modules.textureSheet.tilesX = Math.max(1, sheet.path("tilesX").asInt(1));
        modules.textureSheet.tilesY = Math.max(1, sheet.path("tilesY").asInt(1));
        modules.textureSheet.frameRate = (float) sheet.path("frameRate").asDouble(12.0);
        modules.textureSheet.cycle = sheet.path("cycle").asBoolean(true);
        modules.subEmitters.clear();
        for (JsonNode subJson : root.path("subEmitters")) {
            ParticleSystemDocument.SubEmitterModule sub = new ParticleSystemDocument.SubEmitterModule();
            sub.systemGuid = subJson.path("systemGuid").asText("");
            sub.trigger = subJson.path("trigger").asText("onDeath");
            sub.probability = (float) subJson.path("probability").asDouble(1.0);
            modules.subEmitters.add(sub);
        }
        JsonNode noise = root.path("noise");
        modules.noise.enabled = noise.path("enabled").asBoolean(false);
        modules.noise.strength = (float) noise.path("strength").asDouble(10.0);
        modules.noise.frequency = (float) noise.path("frequency").asDouble(0.5);
        modules.noise.scrollSpeed = (float) noise.path("scrollSpeed").asDouble(1.0);
        JsonNode trails = root.path("trails");
        modules.trails.enabled = trails.path("enabled").asBoolean(false);
        modules.trails.lifetime = (float) trails.path("lifetime").asDouble(0.3);
        modules.trails.width = (float) trails.path("width").asDouble(4.0);
        modules.trails.minVertexDistance = (float) trails.path("minVertexDistance").asDouble(8.0);
        JsonNode collision = root.path("collision");
        modules.collision.enabled = collision.path("enabled").asBoolean(false);
        modules.collision.mode = collision.path("mode").asText("world");
        modules.collision.bounce = (float) collision.path("bounce").asDouble(0.3);
        modules.collision.lifetimeLoss = (float) collision.path("lifetimeLoss").asDouble(0.1);
    }

    private static ObjectNode writeCurve(MinMaxCurve curve) {
        ObjectNode node = MAPPER.createObjectNode();
        if (curve == null) {
            node.put("mode", MinMaxCurve.Mode.CONSTANT.name());
            node.put("constant", 1f);
            return node;
        }
        node.put("mode", curve.mode == null ? MinMaxCurve.Mode.CONSTANT.name() : curve.mode.name());
        node.put("constant", curve.constant);
        node.put("min", curve.min);
        node.put("max", curve.max);
        ArrayNode keys = node.putArray("keyframes");
        for (MinMaxCurve.Keyframe keyframe : curve.keyframes) {
            ObjectNode k = keys.addObject();
            k.put("time", keyframe.time);
            k.put("value", keyframe.value);
        }
        return node;
    }

    private static MinMaxCurve readCurve(JsonNode node) {
        MinMaxCurve curve = new MinMaxCurve();
        if (!node.isObject()) {
            return curve;
        }
        String mode = node.path("mode").asText("CONSTANT");
        try {
            curve.mode = MinMaxCurve.Mode.valueOf(mode);
        } catch (IllegalArgumentException ex) {
            curve.mode = MinMaxCurve.Mode.CONSTANT;
        }
        curve.constant = (float) node.path("constant").asDouble(1.0);
        curve.min = (float) node.path("min").asDouble();
        curve.max = (float) node.path("max").asDouble(1.0);
        curve.keyframes.clear();
        for (JsonNode keyJson : node.path("keyframes")) {
            MinMaxCurve.Keyframe keyframe = new MinMaxCurve.Keyframe();
            keyframe.time = (float) keyJson.path("time").asDouble();
            keyframe.value = (float) keyJson.path("value").asDouble();
            curve.keyframes.add(keyframe);
        }
        return curve;
    }

    private static ObjectNode writeGradient(Gradient gradient) {
        ObjectNode node = MAPPER.createObjectNode();
        ArrayNode keys = node.putArray("keys");
        if (gradient != null) {
            for (Gradient.ColorKey key : gradient.keys) {
                ObjectNode k = keys.addObject();
                k.put("time", key.time);
                k.put("r", key.r);
                k.put("g", key.g);
                k.put("b", key.b);
                k.put("a", key.a);
            }
        }
        return node;
    }

    private static Gradient readGradient(JsonNode node) {
        Gradient gradient = new Gradient();
        if (!node.isObject()) {
            return gradient;
        }
        for (JsonNode keyJson : node.path("keys")) {
            Gradient.ColorKey key = new Gradient.ColorKey();
            key.time = (float) keyJson.path("time").asDouble();
            key.r = (float) keyJson.path("r").asDouble(1.0);
            key.g = (float) keyJson.path("g").asDouble(1.0);
            key.b = (float) keyJson.path("b").asDouble(1.0);
            key.a = (float) keyJson.path("a").asDouble(1.0);
            gradient.keys.add(key);
        }
        return gradient;
    }
}
