package org.llw.studio.scripting.js.bindings;

import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.Animation2DComponent;
import org.llw.studio.ecs.components.AudioSourceComponent;
import org.llw.studio.ecs.components.BoxCollider2DComponent;
import org.llw.studio.ecs.components.Camera2DComponent;
import org.llw.studio.ecs.components.CircleCollider2DComponent;
import org.llw.studio.ecs.components.EdgeCollider2DComponent;
import org.llw.studio.ecs.components.Rigidbody2DComponent;
import org.llw.studio.ecs.components.SpriteRendererComponent;
import org.llw.studio.ecs.components.TilemapComponent;
import org.llw.studio.ecs.components.UIButtonComponent;
import org.llw.studio.ecs.components.UILabelComponent;
import org.llw.studio.ecs.components.UITextFieldComponent;
import org.llw.studio.ecs.components.UIToggleComponent;
import org.llw.studio.scripting.ScriptComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: factory and queries for component host bindings.
 */
public final class ComponentBindings {
    private static final Map<String, BiFunction<ScriptContext, EntityId, Object>> FACTORIES = new HashMap<>();

    static {
        register("SpriteRenderer", SpriteRendererBinding::new);
        register("Tilemap2D", Tilemap2DBinding::new);
        register("Animation2D", Animation2DBinding::new);
        register("AudioSource", AudioSourceBinding::new);
        register("Script", (ctx, entity) -> new ScriptComponentBinding(ctx, entity));
        register("Rigidbody2D", Rigidbody2DBinding::new);
        register("BoxCollider2D", BoxCollider2DBinding::new);
        register("CircleCollider2D", CircleCollider2DBinding::new);
        register("EdgeCollider2D", EdgeCollider2DBinding::new);
        register("UILabel", UILabelBinding::new);
        register("UIButton", UIButtonBinding::new);
        register("UIToggle", UIToggleBinding::new);
        register("UITextField", UITextFieldBinding::new);
    }

    private ComponentBindings() {
    }

    /**
     * @param type    component type name
     * @param factory binding factory
     */
    public static void register(String type, BiFunction<ScriptContext, EntityId, Object> factory) {
        FACTORIES.put(type, factory);
    }

    /**
     * @param context play-mode script context
     * @param hostApi host API (used for camera bindings)
     * @param entity  target entity
     * @param type    component type name
     * @return host binding, or {@code null} when unsupported
     */
    public static Object create(ScriptContext context, ScriptHostApi hostApi, EntityId entity, String type) {
        if ("Camera2D".equals(type)) {
            return new Camera2DBinding(context, hostApi, entity);
        }
        BiFunction<ScriptContext, EntityId, Object> factory = FACTORIES.get(type);
        if (factory == null) {
            return null;
        }
        return factory.apply(context, entity);
    }

    /**
     * @param type component type name
     * @return {@code true} when the type can be added from scripts
     */
    public static boolean supports(String type) {
        return "Camera2D".equals(type) || FACTORIES.containsKey(type)
                || "Rigidbody2D".equals(type) || "BoxCollider2D".equals(type)
                || "CircleCollider2D".equals(type) || "EdgeCollider2D".equals(type);
    }

    /**
     * @param world  play-mode world
     * @param entity target entity
     * @param type   component type name
     * @return {@code true} when the entity has the component
     */
    public static boolean hasComponent(World world, EntityId entity, String type) {
        return switch (type) {
            case "SpriteRenderer" -> world.getComponent(entity, SpriteRendererComponent.class) != null;
            case "Tilemap2D" -> world.getComponent(entity, TilemapComponent.class) != null;
            case "Animation2D" -> world.getComponent(entity, Animation2DComponent.class) != null;
            case "Camera2D" -> world.getComponent(entity, Camera2DComponent.class) != null;
            case "AudioSource" -> world.getComponent(entity, AudioSourceComponent.class) != null;
            case "Script" -> world.getComponent(entity, ScriptComponent.class) != null;
            case "Rigidbody2D" -> world.getComponent(entity, Rigidbody2DComponent.class) != null;
            case "BoxCollider2D" -> world.getComponent(entity, BoxCollider2DComponent.class) != null;
            case "CircleCollider2D" -> world.getComponent(entity, CircleCollider2DComponent.class) != null;
            case "EdgeCollider2D" -> world.getComponent(entity, EdgeCollider2DComponent.class) != null;
            case "UILabel" -> world.getComponent(entity, UILabelComponent.class) != null;
            case "UIButton" -> world.getComponent(entity, UIButtonComponent.class) != null;
            case "UIToggle" -> world.getComponent(entity, UIToggleComponent.class) != null;
            case "UITextField" -> world.getComponent(entity, UITextFieldComponent.class) != null;
            default -> false;
        };
    }

    /**
     * @param context play-mode script context
     * @param entity  target entity
     * @param type    component type name
     * @throws IllegalArgumentException when the type is unknown
     */
    public static void addDefaultComponent(ScriptContext context, EntityId entity, String type) {
        context.defer(() -> {
            World world = context.world();
            switch (type) {
                case "SpriteRenderer" -> {
                    if (world.getComponent(entity, SpriteRendererComponent.class) == null) {
                        world.addComponent(entity, SpriteRendererComponent.class, new SpriteRendererComponent());
                    }
                }
                case "Tilemap2D" -> {
                    if (world.getComponent(entity, TilemapComponent.class) == null) {
                        world.addComponent(entity, TilemapComponent.class, new TilemapComponent());
                    }
                }
                case "Animation2D" -> {
                    if (world.getComponent(entity, Animation2DComponent.class) == null) {
                        world.addComponent(entity, Animation2DComponent.class, new Animation2DComponent());
                    }
                }
                case "Camera2D" -> {
                    if (world.getComponent(entity, Camera2DComponent.class) == null) {
                        world.addComponent(entity, Camera2DComponent.class, new Camera2DComponent());
                    }
                }
                case "AudioSource" -> {
                    if (world.getComponent(entity, AudioSourceComponent.class) == null) {
                        world.addComponent(entity, AudioSourceComponent.class, new AudioSourceComponent());
                    }
                }
                case "Script" -> {
                    ScriptComponent scripts = world.getComponent(entity, ScriptComponent.class);
                    if (scripts == null) {
                        scripts = new ScriptComponent();
                        world.addComponent(entity, ScriptComponent.class, scripts);
                    }
                    scripts.addAttachment();
                }
                case "Rigidbody2D" -> {
                    if (world.getComponent(entity, Rigidbody2DComponent.class) == null) {
                        world.addComponent(entity, Rigidbody2DComponent.class, new Rigidbody2DComponent());
                    }
                }
                case "BoxCollider2D" -> {
                    if (world.getComponent(entity, BoxCollider2DComponent.class) == null) {
                        world.addComponent(entity, BoxCollider2DComponent.class, new BoxCollider2DComponent());
                    }
                }
                case "CircleCollider2D" -> {
                    if (world.getComponent(entity, CircleCollider2DComponent.class) == null) {
                        world.addComponent(entity, CircleCollider2DComponent.class, new CircleCollider2DComponent());
                    }
                }
                case "EdgeCollider2D" -> {
                    if (world.getComponent(entity, EdgeCollider2DComponent.class) == null) {
                        world.addComponent(entity, EdgeCollider2DComponent.class, new EdgeCollider2DComponent());
                    }
                }
                default -> throw new IllegalArgumentException("Unknown component type: " + type);
            }
        });
    }
}
