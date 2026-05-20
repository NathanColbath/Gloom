package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.Value;
import org.llw.studio.ecs.EntityId;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: resolves live script instances by class name.
 */
@FunctionalInterface
public interface ScriptInstanceLookup {
    /**
     * @param entity          target entity
     * @param scriptClassName script class name from the bundle
     * @return live script instance, or {@code null}
     */
    Value find(EntityId entity, String scriptClassName);
}
