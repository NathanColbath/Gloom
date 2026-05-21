package org.llw.studio.editor.particles;

import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import org.llw.studio.editor.panels.ParticlePanel;
import org.llw.studio.editor.widgets.fields.FloatField;
import org.llw.studio.curves.MinMaxCurve;
import org.llw.studio.editor.widgets.fields.ParticleSystemReferenceField;
import org.llw.studio.editor.widgets.fields.SpriteReferenceField;
import org.llw.studio.particles.model.ParticleSystemDocument;
import org.llw.studio.particles.render.ParticleSpriteResolve;
import org.llw.studio.assets.AssetDatabase;

/**
 * Module list and property rows for the particle editor panel.
 */
public final class ParticleModuleInspector {
    private static final String[] MODULES = {
            "emission", "lifetime", "shape", "velocity", "sizeOverLifetime", "colorOverLifetime",
            "rotationOverLifetime", "force", "renderer", "textureSheet", "subEmitters", "noise", "trails", "collision"
    };

    private ParticleModuleInspector() {
    }

    public static void drawModuleList(ParticleEditorState state) {
        for (String module : MODULES) {
            boolean selected = module.equals(state.selectedModule());
            if (ImGui.selectable(module, selected)) {
                state.setSelectedModule(module);
            }
        }
    }

    public static void drawProperties(ParticleEditorState state, AssetDatabase assets, ParticlePanel particlePanel) {
        ParticleSystemDocument doc = state.document();
        if (doc == null) {
            return;
        }
        ImInt maxParticles = new ImInt(doc.maxParticles);
        if (ImGui.inputInt("Max Particles", maxParticles)) {
            doc.maxParticles = Math.max(1, maxParticles.get());
        }
        doc.duration = Math.max(0.1f, FloatField.draw("Duration", doc.duration));
        ImBoolean looping = new ImBoolean(doc.looping);
        if (ImGui.checkbox("Looping", looping)) {
            doc.looping = looping.get();
        }
        if (ImGui.beginCombo("Simulation Space", doc.simulationSpace)) {
            if (ImGui.selectable("world", "world".equals(doc.simulationSpace))) {
                doc.simulationSpace = "world";
            }
            if (ImGui.selectable("local", "local".equals(doc.simulationSpace))) {
                doc.simulationSpace = "local";
            }
            ImGui.endCombo();
        }
        ImGui.separator();
        switch (state.selectedModule()) {
            case "emission" -> drawEmission(doc);
            case "lifetime" -> drawCurve("Lifetime", doc.modules.lifetime.curve);
            case "shape" -> drawShape(doc.modules.shape);
            case "velocity" -> {
                drawCurve("Speed", doc.modules.velocity.speed);
                drawCurve("Angle", doc.modules.velocity.angle);
            }
            case "sizeOverLifetime" -> {
                ImBoolean enabled = new ImBoolean(doc.modules.sizeOverLifetime.enabled);
                if (ImGui.checkbox("Enabled##size", enabled)) {
                    doc.modules.sizeOverLifetime.enabled = enabled.get();
                }
                drawCurve("Size", doc.modules.sizeOverLifetime.curve);
            }
            case "colorOverLifetime" -> {
                ImBoolean enabled = new ImBoolean(doc.modules.colorOverLifetime.enabled);
                if (ImGui.checkbox("Enabled##color", enabled)) {
                    doc.modules.colorOverLifetime.enabled = enabled.get();
                }
                drawGradient(doc);
            }
            case "rotationOverLifetime" -> {
                ImBoolean enabled = new ImBoolean(doc.modules.rotationOverLifetime.enabled);
                if (ImGui.checkbox("Enabled##rot", enabled)) {
                    doc.modules.rotationOverLifetime.enabled = enabled.get();
                }
                drawCurve("Rotation", doc.modules.rotationOverLifetime.curve);
            }
            case "force" -> drawForce(doc.modules.force);
            case "renderer" -> drawRenderer(doc, assets);
            case "textureSheet" -> drawTextureSheet(doc.modules.textureSheet);
            case "subEmitters" -> drawSubEmitters(doc, assets, particlePanel);
            case "noise" -> drawNoise(doc.modules.noise);
            case "trails" -> drawTrails(doc.modules.trails);
            case "collision" -> drawCollision(doc.modules.collision);
            default -> ImGui.textDisabled("Select a module.");
        }
    }

    private static void drawEmission(ParticleSystemDocument doc) {
        doc.modules.emission.rateOverTime = Math.max(0f, FloatField.draw("Rate Over Time", doc.modules.emission.rateOverTime));
        ImGui.text("Bursts: " + doc.modules.emission.bursts.size());
        if (ImGui.button("Add Burst")) {
            ParticleSystemDocument.Burst burst = new ParticleSystemDocument.Burst();
            burst.time = doc.duration * 0.5f;
            burst.count = 20;
            doc.modules.emission.bursts.add(burst);
        }
    }

    private static void drawShape(ParticleSystemDocument.ShapeModule shape) {
        if (ImGui.beginCombo("Shape", shape.type)) {
            for (String type : new String[]{"point", "circle", "box"}) {
                if (ImGui.selectable(type, type.equals(shape.type))) {
                    shape.type = type;
                }
            }
            ImGui.endCombo();
        }
        shape.radius = FloatField.draw("Radius", shape.radius);
        shape.width = FloatField.draw("Width", shape.width);
        shape.height = FloatField.draw("Height", shape.height);
    }

    private static void drawCurve(String label, MinMaxCurve curve) {
        if (curve == null) {
            return;
        }
        String mode = curve.mode == null ? MinMaxCurve.Mode.CONSTANT.name() : curve.mode.name();
        if (ImGui.beginCombo(label + " Mode", mode)) {
            for (MinMaxCurve.Mode value : MinMaxCurve.Mode.values()) {
                if (ImGui.selectable(value.name(), value == curve.mode)) {
                    curve.mode = value;
                }
            }
            ImGui.endCombo();
        }
        curve.constant = FloatField.draw(label + " Constant", curve.constant);
        curve.min = FloatField.draw(label + " Min", curve.min);
        curve.max = FloatField.draw(label + " Max", curve.max);
    }

    private static void drawGradient(ParticleSystemDocument doc) {
        var gradient = doc.modules.colorOverLifetime.gradient;
        if (gradient.keys.isEmpty()) {
            gradient.keys.add(new org.llw.studio.curves.Gradient.ColorKey(0f, 1f, 1f, 1f, 1f));
            gradient.keys.add(new org.llw.studio.curves.Gradient.ColorKey(1f, 1f, 1f, 1f, 0f));
        }
        for (int i = 0; i < gradient.keys.size(); i++) {
            var key = gradient.keys.get(i);
            key.time = FloatField.draw("Key " + i + " Time", key.time);
            key.a = FloatField.draw("Key " + i + " Alpha", key.a);
        }
    }

    private static void drawForce(ParticleSystemDocument.ForceModule force) {
        force.gravity = FloatField.draw("Gravity", force.gravity);
        force.drag = Math.max(0f, FloatField.draw("Drag", force.drag));
    }

    private static void drawRenderer(ParticleSystemDocument doc, AssetDatabase assets) {
        var renderer = doc.modules.renderer;
        renderer.spriteGuid = SpriteReferenceField.draw("Sprite", renderer.spriteGuid, assets);
        if (renderer.spriteGuid == null || renderer.spriteGuid.isBlank()) {
            if (ParticleSpriteResolve.resolve(assets, renderer) == null) {
                ImGui.textDisabled("Assign a sprite, or import a texture for preview.");
            } else {
                ImGui.textDisabled("Preview uses the first project sprite until you assign one.");
            }
        }
        if (ImGui.beginCombo("Blend Mode", renderer.blendMode)) {
            for (String mode : new String[]{"ALPHA", "ADDITIVE", "MULTIPLY", "NONE"}) {
                if (ImGui.selectable(mode, mode.equals(renderer.blendMode))) {
                    renderer.blendMode = mode;
                }
            }
            ImGui.endCombo();
        }
        ImInt sorting = new ImInt(renderer.sortingOrder);
        if (ImGui.inputInt("Sorting Order", sorting)) {
            renderer.sortingOrder = sorting.get();
        }
    }

    private static void drawTextureSheet(ParticleSystemDocument.TextureSheetModule sheet) {
        ImBoolean enabled = new ImBoolean(sheet.enabled);
        if (ImGui.checkbox("Enabled##sheet", enabled)) {
            sheet.enabled = enabled.get();
        }
        ImInt tilesX = new ImInt(sheet.tilesX);
        if (ImGui.inputInt("Tiles X", tilesX)) {
            sheet.tilesX = Math.max(1, tilesX.get());
        }
        ImInt tilesY = new ImInt(sheet.tilesY);
        if (ImGui.inputInt("Tiles Y", tilesY)) {
            sheet.tilesY = Math.max(1, tilesY.get());
        }
        sheet.frameRate = FloatField.draw("Frame Rate", sheet.frameRate);
    }

    private static void drawSubEmitters(ParticleSystemDocument doc, AssetDatabase assets, ParticlePanel particlePanel) {
        if (ImGui.button("Add Sub-Emitter")) {
            doc.modules.subEmitters.add(new ParticleSystemDocument.SubEmitterModule());
        }
        for (int i = 0; i < doc.modules.subEmitters.size(); i++) {
            ImGui.pushID(i);
            var sub = doc.modules.subEmitters.get(i);
            sub.systemGuid = ParticleSystemReferenceField.draw("System", sub.systemGuid, assets, particlePanel);
            if (ImGui.beginCombo("Trigger", sub.trigger)) {
                for (String trigger : new String[]{"onBirth", "onDeath", "onCollision"}) {
                    if (ImGui.selectable(trigger, trigger.equals(sub.trigger))) {
                        sub.trigger = trigger;
                    }
                }
                ImGui.endCombo();
            }
            sub.probability = FloatField.draw("Probability", sub.probability);
            ImGui.popID();
        }
    }

    private static void drawNoise(ParticleSystemDocument.NoiseModule noise) {
        ImBoolean enabled = new ImBoolean(noise.enabled);
        if (ImGui.checkbox("Enabled##noise", enabled)) {
            noise.enabled = enabled.get();
        }
        noise.strength = FloatField.draw("Strength", noise.strength);
    }

    private static void drawTrails(ParticleSystemDocument.TrailsModule trails) {
        ImBoolean enabled = new ImBoolean(trails.enabled);
        if (ImGui.checkbox("Enabled##trails", enabled)) {
            trails.enabled = enabled.get();
        }
        trails.lifetime = FloatField.draw("Lifetime", trails.lifetime);
    }

    private static void drawCollision(ParticleSystemDocument.CollisionModule collision) {
        ImBoolean enabled = new ImBoolean(collision.enabled);
        if (ImGui.checkbox("Enabled##collision", enabled)) {
            collision.enabled = enabled.get();
        }
        collision.bounce = FloatField.draw("Bounce", collision.bounce);
    }
}
