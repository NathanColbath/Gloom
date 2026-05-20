package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.Value;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.scene.SceneObjectIds;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: converts JS values to entity and prefab bindings.
 */
public final class ScriptEntityCoercion {
    private ScriptEntityCoercion() {
    }

    /**
     * @param context play-mode script context
     * @param hostApi host API for binding creation
     * @param value   entity proxy, host binding, or object with {@code sceneId}
     * @return entity binding, or {@code null}
     */
    public static EntityBinding coerceEntityBinding(ScriptContext context, ScriptHostApi hostApi, Object value) {
        EntityBinding binding = coerceHostEntityBinding(value);
        if (binding != null) {
            return binding;
        }
        EntityId entityId = resolveEntityId(context, value);
        if (entityId.isNone()) {
            return null;
        }
        return hostApi.createEntityBinding(context, entityId);
    }

    /**
     * @param context play-mode script context
     * @param value   entity proxy, host binding, or object with {@code sceneId}
     * @return resolved entity id, or {@link EntityId#none()}
     */
    public static EntityId resolveEntityId(ScriptContext context, Object value) {
        EntityBinding binding = coerceHostEntityBinding(value);
        if (binding != null) {
            return binding.entityId();
        }
        Value polyglot = asPolyglotValue(value);
        if (polyglot != null && polyglot.hasMember("sceneId")) {
            Value sceneIdValue = polyglot.getMember("sceneId");
            if (sceneIdValue != null && sceneIdValue.fitsInInt()) {
                return SceneObjectIds.findBySceneId(context.world(), sceneIdValue.asInt());
            }
        }
        return EntityId.none();
    }

    /**
     * @param value prefab template binding, JS prefab proxy, or serialized {@code { prefab }} object
     * @return prefab template binding, or {@code null}
     */
    public static PrefabTemplateBinding coercePrefabTemplate(Object value) {
        if (value instanceof PrefabTemplateBinding binding) {
            return binding.prefabGuid().isBlank() ? null : binding;
        }
        Value polyglot = asPolyglotValue(value);
        if (polyglot == null || polyglot.isNull()) {
            return null;
        }
        if (polyglot.isHostObject()) {
            Object host = polyglot.asHostObject();
            if (host instanceof PrefabTemplateBinding binding) {
                return binding.prefabGuid().isBlank() ? null : binding;
            }
        }
        String prefabGuid = readPrefabGuid(polyglot);
        return prefabGuid == null || prefabGuid.isBlank() ? null : new PrefabTemplateBinding(prefabGuid);
    }

    /**
     * @param value prefab template binding or JS prefab proxy
     * @return prefab asset GUID, or {@code null}
     */
    public static String resolvePrefabGuid(Object value) {
        PrefabTemplateBinding binding = coercePrefabTemplate(value);
        if (binding == null) {
            return null;
        }
        String guid = binding.prefabGuid();
        return guid.isBlank() ? null : guid;
    }

    private static String readPrefabGuid(Value value) {
        if (value.hasMember("prefabGuid")) {
            Value member = value.getMember("prefabGuid");
            if (member != null && !member.isNull() && member.isString()) {
                return member.asString();
            }
        }
        if (value.hasMember("prefab")) {
            Value member = value.getMember("prefab");
            if (member != null && !member.isNull() && member.isString()) {
                return member.asString();
            }
        }
        return null;
    }

    private static Value asPolyglotValue(Object value) {
        if (value instanceof Value polyglot) {
            return polyglot;
        }
        return null;
    }

    private static EntityBinding coerceHostEntityBinding(Object value) {
        if (value instanceof EntityBinding binding) {
            return binding;
        }
        Value polyglot = asPolyglotValue(value);
        if (polyglot != null && polyglot.isHostObject()) {
            Object host = polyglot.asHostObject();
            if (host instanceof EntityBinding binding) {
                return binding;
            }
        }
        return null;
    }
}
