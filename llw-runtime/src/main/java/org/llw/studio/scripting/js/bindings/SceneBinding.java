package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.llw.studio.physics.PlayPhysicsBridge;
import org.llw.studio.prefab.PrefabInstantiator;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.components.NameComponent;
import org.llw.studio.scene.GameObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: entity lookup and spawning exposed as {@code Scene}.
 */
public final class SceneBinding {
    private final ScriptContext context;
    private final ScriptHostApi hostApi;

    /**
     * @param context play-mode script context
     * @param hostApi host API for entity wrapping
     */
    public SceneBinding(ScriptContext context, ScriptHostApi hostApi) {
        this.context = context;
        this.hostApi = hostApi;
    }

    /**
     * @param name entity name
     * @return first matching entity proxy, or {@code null}
     */
    @HostAccess.Export
    public Object findByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        var store = context.world().store(NameComponent.class);
        for (int i = 0; i < store.size(); i++) {
            NameComponent component = store.componentAt(i);
            if (name.equals(component.name())) {
                return hostApi.wrapEntity(hostApi.createEntityBinding(context, store.entityAt(i)));
            }
        }
        return null;
    }

    /**
     * @param name entity name
     * @return all matching entity proxies (may be empty)
     */
    @HostAccess.Export
    public List<Object> findAllByName(String name) {
        List<Object> matches = new ArrayList<>();
        if (name == null || name.isBlank()) {
            return matches;
        }
        var store = context.world().store(NameComponent.class);
        for (int i = 0; i < store.size(); i++) {
            NameComponent component = store.componentAt(i);
            if (name.equals(component.name())) {
                matches.add(hostApi.wrapEntity(hostApi.createEntityBinding(context, store.entityAt(i))));
            }
        }
        return matches;
    }

    /**
     * @param tag entity tag
     * @return first matching entity proxy, or {@code null}
     */
    @HostAccess.Export
    public Object findByTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return null;
        }
        var store = context.world().store(NameComponent.class);
        for (int i = 0; i < store.size(); i++) {
            NameComponent component = store.componentAt(i);
            if (tag.equals(component.tag())) {
                return hostApi.wrapEntity(hostApi.createEntityBinding(context, store.entityAt(i)));
            }
        }
        return null;
    }

    /**
     * @param tag entity tag
     * @return all matching entity proxies (may be empty)
     */
    @HostAccess.Export
    public List<Object> findAllByTag(String tag) {
        List<Object> matches = new ArrayList<>();
        if (tag == null || tag.isBlank()) {
            return matches;
        }
        var store = context.world().store(NameComponent.class);
        for (int i = 0; i < store.size(); i++) {
            NameComponent component = store.componentAt(i);
            if (tag.equals(component.tag())) {
                matches.add(hostApi.wrapEntity(hostApi.createEntityBinding(context, store.entityAt(i))));
            }
        }
        return matches;
    }

    /**
     * @return new empty entity proxy named {@code GameObject}
     */
    @HostAccess.Export
    public Object createEntity() {
        return createEmpty(null);
    }

    /**
     * @param arg0 optional name, prefab GUID, entity template, or prefab template
     * @return created entity proxy, or {@code null} on failure
     */
    @HostAccess.Export
    public Object createEntity(Value arg0) {
        return createEntityWithArgs(arg0, null);
    }

    /**
     * @param arg0 first optional name, prefab GUID, entity template, or prefab template
     * @param arg1 second optional argument with the same semantics
     * @return created entity proxy, or {@code null} on failure
     */
    @HostAccess.Export
    public Object createEntity(Value arg0, Value arg1) {
        return createEntityWithArgs(arg0, arg1);
    }

    /**
     * @param entity entity proxy to destroy
     */
    @HostAccess.Export
    public void destroyEntity(Value entity) {
        EntityId id = ScriptEntityCoercion.resolveEntityId(context, entity);
        if (id.isNone()) {
            return;
        }
        context.defer(() -> {
            PlayPhysicsBridge.unregisterEntity(id);
            context.world().destroyEntity(id);
        });
    }

    private Object createEntityWithArgs(Object arg0, Object arg1) {
        if (isGuestNull(arg0) && isGuestNull(arg1)) {
            return null;
        }
        PrefabTemplateBinding prefabTemplate = ScriptEntityCoercion.coercePrefabTemplate(arg0);
        if (prefabTemplate == null) {
            prefabTemplate = ScriptEntityCoercion.coercePrefabTemplate(arg1);
        }

        String name = null;
        String prefab = prefabTemplate == null ? null : prefabTemplate.prefabGuid();
        if (prefab == null) {
            prefab = ScriptEntityCoercion.resolvePrefabGuid(arg0);
        }
        if (prefab == null) {
            prefab = ScriptEntityCoercion.resolvePrefabGuid(arg1);
        }
        if (guestString(arg0) != null) {
            name = guestString(arg0);
        }
        String arg1Text = guestString(arg1);
        if (arg1Text != null) {
            if (prefab == null) {
                prefab = arg1Text;
            } else if (name == null) {
                name = arg1Text;
            }
        }

        if (prefab != null && !prefab.isBlank()) {
            try {
                String rootName = name == null || name.isBlank() ? null : name;
                GameObject root = PrefabInstantiator.instantiate(
                        context.scene(),
                        context.assets(),
                        context.projectRoot(),
                        prefab,
                        null,
                        0f,
                        0f,
                        false,
                        rootName
                );
                if (root != null) {
                    PlayPhysicsBridge.registerSpawnedObject(context.world(), root);
                }
                return root == null ? null : hostApi.wrapEntity(hostApi.createEntityBinding(context, root.entity()));
            } catch (Exception ex) {
                return null;
            }
        }

        EntityBinding entityTemplate = ScriptEntityCoercion.coerceEntityBinding(context, hostApi, arg0);
        if (entityTemplate == null) {
            entityTemplate = ScriptEntityCoercion.coerceEntityBinding(context, hostApi, arg1);
        }

        if (entityTemplate != null) {
            GameObject root = PrefabInstantiator.cloneSubtree(
                    context.scene(),
                    entityTemplate.entityId(),
                    null,
                    0f,
                    0f,
                    false,
                    name
            );
            if (root != null) {
                PlayPhysicsBridge.registerSpawnedObject(context.world(), root);
            }
            return root == null ? null : hostApi.wrapEntity(hostApi.createEntityBinding(context, root.entity()));
        }

        return createEmpty(name);
    }

    private Object createEmpty(String name) {
        String entityName = name == null || name.isBlank() ? "GameObject" : name;
        GameObject object = context.scene().createGameObject(entityName);
        return hostApi.wrapEntity(hostApi.createEntityBinding(context, object.entity()));
    }

    private static boolean isGuestNull(Object value) {
        return value == null || (value instanceof Value guest && guest.isNull());
    }

    private static String guestString(Object value) {
        if (value instanceof String text) {
            return text.isBlank() ? null : text;
        }
        if (value instanceof Value guest && guest.isString()) {
            String text = guest.asString();
            return text == null || text.isBlank() ? null : text;
        }
        return null;
    }
}
