package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.ecs.components.ActiveComponent;
import org.llw.studio.ecs.components.HierarchyComponent;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.ecs.components.WorldTransformComponent;
import org.llw.studio.scene.GameObject;
import org.llw.studio.scene.SceneObjectIds;

import java.util.ArrayList;
import java.util.List;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: entity hierarchy and component access for scripts.
 */
public final class EntityBinding {
    private final ScriptContext context;
    private final ScriptHostApi hostApi;
    private final EntityId entity;

    /**
     * @param context play-mode script context
     * @param hostApi host API for child binding creation
     * @param entity  target entity
     */
    public EntityBinding(ScriptContext context, ScriptHostApi hostApi, EntityId entity) {
        this.context = context;
        this.hostApi = hostApi;
        this.entity = entity;
    }

    /**
     * @return underlying ECS entity id
     */
    public EntityId entityId() {
        return entity;
    }

    /**
     * @return string form of the entity id
     */
    @HostAccess.Export
    public String getId() {
        return entity.toString();
    }

    /** @return entity display name */
    @HostAccess.Export
    public String getName() {
        NameComponent name = context.world().getComponent(entity, NameComponent.class);
        return name == null ? "" : name.name();
    }

    /**
     * @param value new display name; blank values are ignored
     */
    @HostAccess.Export
    public void setName(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        context.defer(() -> {
            GameObject object = context.scene().find(entity);
            if (object != null) {
                object.setName(value);
            }
        });
    }

    /** @return entity gameplay tag */
    @HostAccess.Export
    public String getTag() {
        NameComponent identity = context.world().getComponent(entity, NameComponent.class);
        return identity == null ? "" : identity.tag();
    }

    /**
     * @param value new gameplay tag; {@code null} is ignored
     */
    @HostAccess.Export
    public void setTag(String value) {
        if (value == null) {
            return;
        }
        context.defer(() -> {
            GameObject object = context.scene().find(entity);
            if (object != null) {
                object.setTag(value);
            }
        });
    }

    /**
     * @param value tag to compare
     * @return {@code true} when this entity's tag equals {@code value}
     */
    @HostAccess.Export
    public boolean compareTag(String value) {
        return value != null && value.equals(getTag());
    }

    /** @return {@code true} when the entity is self-active */
    @HostAccess.Export
    public boolean isActive() {
        ActiveComponent active = context.world().getComponent(entity, ActiveComponent.class);
        return active == null || active.selfActive;
    }

    /**
     * @param value self-active flag
     */
    @HostAccess.Export
    public void setActive(boolean value) {
        ActiveComponent active = context.world().getComponent(entity, ActiveComponent.class);
        if (active != null) {
            active.selfActive = value;
        }
    }

    /**
     * @return parent entity binding, or {@code null}
     */
    @HostAccess.Export
    public EntityBinding getParent() {
        GameObject object = context.scene().find(entity);
        if (object == null) {
            return null;
        }
        GameObject parent = object.parent();
        return parent == null ? null : hostApi.createEntityBinding(context, parent.entity());
    }

    /**
     * @return direct child entity bindings
     */
    @HostAccess.Export
    public List<EntityBinding> getChildren() {
        List<EntityBinding> result = new ArrayList<>();
        GameObject object = context.scene().find(entity);
        if (object == null) {
            return result;
        }
        for (GameObject child : object.children()) {
            result.add(hostApi.createEntityBinding(context, child.entity()));
        }
        return result;
    }

    /**
     * @param parent              new parent binding, or {@code null} for root
     * @param worldPositionStays  when {@code true}, preserves world transform
     */
    @HostAccess.Export
    public void setParent(EntityBinding parent, boolean worldPositionStays) {
        context.defer(() -> {
            GameObject self = context.scene().find(entity);
            if (self == null) {
                return;
            }
            GameObject parentObject = parent == null ? null : context.scene().find(parent.entity);
            self.setParent(parentObject, worldPositionStays);
        });
    }

    /**
     * @param type component type name
     * @return {@code true} when the component exists
     */
    @HostAccess.Export
    public boolean hasComponent(String type) {
        return ComponentBindings.hasComponent(context.world(), entity, type);
    }

    /**
     * @param type component type name
     * @return wrapped component binding, or {@code null}
     */
    @HostAccess.Export
    public Object getComponent(String type) {
        if (!ComponentBindings.hasComponent(context.world(), entity, type)) {
            return null;
        }
        Object binding = ComponentBindings.create(context, hostApi, entity, type);
        return hostApi.wrapComponent(type, binding);
    }

    /**
     * @param scriptClassName script class name from the bundle
     * @return live script instance value, or {@code null}
     */
    @HostAccess.Export
    public Object getScriptComponent(String scriptClassName) {
        ScriptInstanceLookup lookup = context.scriptInstanceLookup();
        if (lookup == null || scriptClassName == null || scriptClassName.isBlank()) {
            return null;
        }
        return lookup.find(entity, scriptClassName);
    }

    /**
     * @param type component type name
     * @throws IllegalArgumentException when the type is not supported
     */
    @HostAccess.Export
    public void addComponent(String type) {
        if (!ComponentBindings.supports(type)) {
            throw new IllegalArgumentException("Unknown component type: " + type);
        }
        ComponentBindings.addDefaultComponent(context, entity, type);
    }

    /**
     * @param type component type name
     * @throws IllegalArgumentException when deferred removal fails for an unknown type
     */
    @HostAccess.Export
    public void removeComponent(String type) {
        context.defer(() -> removeComponentNow(type));
    }

    /** Destroys this entity at the end of the frame. */
    @HostAccess.Export
    public void destroy() {
        context.defer(() -> {
            org.llw.studio.physics.PlayPhysicsBridge.unregisterEntity(entity);
            context.world().destroyEntity(entity);
        });
    }

    /**
     * @return stable scene object id for serialization
     */
    @HostAccess.Export
    public int getSceneId() {
        return SceneObjectIds.get(context.world(), entity);
    }

    /** @return world-space X */
    @HostAccess.Export
    public double getWorldX() {
        WorldTransformComponent worldTransform = context.world().getComponent(entity, WorldTransformComponent.class);
        return worldTransform == null ? 0d : worldTransform.worldX;
    }

    /** @return world-space Y */
    @HostAccess.Export
    public double getWorldY() {
        WorldTransformComponent worldTransform = context.world().getComponent(entity, WorldTransformComponent.class);
        return worldTransform == null ? 0d : worldTransform.worldY;
    }

    private void removeComponentNow(String type) {
        World world = context.world();
        switch (type) {
            case "SpriteRenderer" -> world.removeComponent(entity, org.llw.studio.ecs.components.SpriteRendererComponent.class);
            case "Camera2D" -> world.removeComponent(entity, org.llw.studio.ecs.components.Camera2DComponent.class);
            case "Animation2D" -> world.removeComponent(entity, org.llw.studio.ecs.components.Animation2DComponent.class);
            case "ParticleEmitter" -> world.removeComponent(entity, org.llw.studio.ecs.components.ParticleEmitterComponent.class);
            case "AudioSource" -> world.removeComponent(entity, org.llw.studio.ecs.components.AudioSourceComponent.class);
            case "Script" -> world.removeComponent(entity, org.llw.studio.scripting.ScriptComponent.class);
            case "Rigidbody2D" -> world.removeComponent(entity, org.llw.studio.ecs.components.Rigidbody2DComponent.class);
            case "BoxCollider2D" -> world.removeComponent(entity, org.llw.studio.ecs.components.BoxCollider2DComponent.class);
            case "CircleCollider2D" -> world.removeComponent(entity, org.llw.studio.ecs.components.CircleCollider2DComponent.class);
            case "EdgeCollider2D" -> world.removeComponent(entity, org.llw.studio.ecs.components.EdgeCollider2DComponent.class);
            default -> throw new IllegalArgumentException("Unknown component type: " + type);
        }
    }
}
