package org.llw.studio.scripting.js;

import org.llw.studio.ecs.EntityId;

/**
 * Identifies one live Graal script instance for an entity attachment slot.
 */
public record ScriptInstanceKey(EntityId entity, int slotId) {
}
