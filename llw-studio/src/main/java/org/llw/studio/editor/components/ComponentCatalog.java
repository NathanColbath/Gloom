package org.llw.studio.editor.components;



import org.llw.studio.editor.inspector.builtin.ActiveDrawer;
import org.llw.studio.editor.inspector.builtin.Animation2DDrawer;
import org.llw.studio.editor.inspector.builtin.ParticleEmitterDrawer;

import org.llw.studio.editor.inspector.builtin.AudioSourceDrawer;

import org.llw.studio.editor.inspector.builtin.Camera2DDrawer;

import org.llw.studio.editor.inspector.builtin.BoxCollider2DDrawer;
import org.llw.studio.editor.inspector.builtin.CircleCollider2DDrawer;
import org.llw.studio.editor.inspector.builtin.EdgeCollider2DDrawer;
import org.llw.studio.editor.inspector.builtin.Rigidbody2DDrawer;
import org.llw.studio.editor.inspector.builtin.ScriptDrawer;

import org.llw.studio.editor.inspector.builtin.SpriteRendererDrawer;
import org.llw.studio.editor.inspector.builtin.TilemapDrawer;
import org.llw.studio.editor.inspector.builtin.UICanvasDrawer;
import org.llw.studio.editor.inspector.builtin.UIButtonDrawer;
import org.llw.studio.editor.inspector.builtin.UILabelDrawer;
import org.llw.studio.editor.inspector.builtin.UITextFieldDrawer;
import org.llw.studio.editor.inspector.builtin.UIToggleDrawer;

import org.llw.studio.editor.inspector.builtin.TransformDrawer;

import org.llw.studio.ecs.components.ActiveComponent;
import org.llw.studio.ecs.components.Animation2DComponent;
import org.llw.studio.ecs.components.ParticleEmitterComponent;

import org.llw.studio.ecs.components.Camera2DComponent;

import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.ecs.components.TilemapComponent;
import org.llw.studio.ecs.components.UIButtonComponent;
import org.llw.studio.ecs.components.UICanvasComponent;
import org.llw.studio.ecs.components.UILabelComponent;
import org.llw.studio.ecs.components.UITextFieldComponent;
import org.llw.studio.ecs.components.UIToggleComponent;

import org.llw.studio.ecs.components.Transform2DComponent;

import org.llw.studio.ecs.components.AudioSourceComponent;
import org.llw.studio.ecs.components.BoxCollider2DComponent;
import org.llw.studio.ecs.components.CircleCollider2DComponent;
import org.llw.studio.ecs.components.EdgeCollider2DComponent;
import org.llw.studio.ecs.components.Rigidbody2DComponent;

import org.llw.studio.scripting.ScriptComponent;



import java.util.ArrayList;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;



/**

 * Registry of built-in component types, drawers, and add-component menu metadata.

 */

public final class ComponentCatalog {

    private final Map<Class<?>, ComponentTypeInfo> types = new LinkedHashMap<>();



    /** Registers default studio component types and inspector drawers. */

    public ComponentCatalog() {

        register(new ComponentTypeInfo(

                Transform2DComponent.class, "Transform 2D", "Core", false, false,

                Transform2DComponent::new, new TransformDrawer()));

        register(new ComponentTypeInfo(

                ActiveComponent.class, "Active", "Core", false, true,

                ActiveComponent::new, new ActiveDrawer()));

        register(new ComponentTypeInfo(

                SpriteRendererComponent.class, "Sprite Renderer", "Rendering", true, false,

                SpriteRendererComponent::new, new SpriteRendererDrawer()));

        register(new ComponentTypeInfo(

                TilemapComponent.class, "Tilemap", "Rendering", true, false,

                TilemapComponent::new, new TilemapDrawer()));

        register(new ComponentTypeInfo(

                Animation2DComponent.class, "Animation 2D", "Animation", true, false,

                Animation2DComponent::new, new Animation2DDrawer()));

        register(new ComponentTypeInfo(

                ParticleEmitterComponent.class, "Particle Emitter", "Effects", true, false,

                ParticleEmitterComponent::new, new ParticleEmitterDrawer()));

        register(new ComponentTypeInfo(

                Camera2DComponent.class, "Camera 2D", "Rendering", true, false,

                Camera2DComponent::new, new Camera2DDrawer()));

        register(new ComponentTypeInfo(

                AudioSourceComponent.class, "Audio Source", "Audio", true, false,

                AudioSourceComponent::new, new AudioSourceDrawer()));

        register(new ComponentTypeInfo(

                ScriptComponent.class, "Script", "Scripting", true, false,

                () -> {
                    ScriptComponent scripts = new ScriptComponent();
                    scripts.addAttachment();
                    return scripts;
                }, new ScriptDrawer()));

        register(new ComponentTypeInfo(

                Rigidbody2DComponent.class, "Rigidbody 2D", "Physics", true, false,

                Rigidbody2DComponent::new, new Rigidbody2DDrawer()));

        register(new ComponentTypeInfo(

                BoxCollider2DComponent.class, "Box Collider 2D", "Physics", true, false,

                BoxCollider2DComponent::new, new BoxCollider2DDrawer()));

        register(new ComponentTypeInfo(

                CircleCollider2DComponent.class, "Circle Collider 2D", "Physics", true, false,

                CircleCollider2DComponent::new, new CircleCollider2DDrawer()));

        register(new ComponentTypeInfo(

                EdgeCollider2DComponent.class, "Edge Collider 2D", "Physics", true, false,

                EdgeCollider2DComponent::new, new EdgeCollider2DDrawer()));

        register(new ComponentTypeInfo(
                UICanvasComponent.class, "UI Canvas", "UI", true, false,
                UICanvasComponent::new, new UICanvasDrawer()));
        register(new ComponentTypeInfo(
                UILabelComponent.class, "UI Label", "UI", true, false,
                UILabelComponent::new, new UILabelDrawer()));
        register(new ComponentTypeInfo(
                UIButtonComponent.class, "UI Button", "UI", true, false,
                UIButtonComponent::new, new UIButtonDrawer()));
        register(new ComponentTypeInfo(
                UIToggleComponent.class, "UI Toggle", "UI", true, false,
                UIToggleComponent::new, new UIToggleDrawer()));
        register(new ComponentTypeInfo(
                UITextFieldComponent.class, "UI Text Field", "UI", true, false,
                UITextFieldComponent::new, new UITextFieldDrawer()));

    }



    /**

     * @param info type metadata and drawer to register

     */

    public void register(ComponentTypeInfo info) {

        types.put(info.type(), info);

    }



    /**

     * @param type component class

     * @return metadata, or null if unknown

     */

    public ComponentTypeInfo get(Class<?> type) {

        return types.get(type);

    }



    /** @return types that may be added from the inspector menu */

    public List<ComponentTypeInfo> addable() {

        List<ComponentTypeInfo> result = new ArrayList<>();

        for (ComponentTypeInfo info : types.values()) {

            if (info.addable()) {

                result.add(info);

            }

        }

        return result;

    }



    /** @return all registered types in registration order */

    public Iterable<ComponentTypeInfo> all() {

        return types.values();

    }

}

