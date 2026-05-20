package org.llw.studio.scripting.js.bindings;

import org.graalvm.polyglot.HostAccess;
import org.llw.studio.ecs.EntityId;
import org.llw.studio.ecs.World;
import org.llw.studio.scripting.ScriptAttachment;
import org.llw.studio.scripting.ScriptComponent;

/**
 * Play-mode {@link org.llw.studio.ecs.World} API: {@code Script} component host binding (first attachment).
 */
public final class ScriptComponentBinding {
    private final World world;
    private final EntityId entity;

    public ScriptComponentBinding(ScriptContext context, EntityId entity) {
        this.world = context.world();
        this.entity = entity;
    }

    @HostAccess.Export
    public String getScriptGuid() {
        ScriptAttachment attachment = firstAttachment();
        return attachment == null ? "" : attachment.scriptGuid;
    }

    @HostAccess.Export
    public void setScriptGuid(String guid) {
        ScriptAttachment attachment = firstAttachment();
        if (attachment != null) {
            attachment.scriptGuid = guid == null ? "" : guid;
        }
    }

    @HostAccess.Export
    public boolean getEnabled() {
        ScriptAttachment attachment = firstAttachment();
        return attachment == null || attachment.enabled;
    }

    @HostAccess.Export
    public void setEnabled(boolean value) {
        ScriptAttachment attachment = firstAttachment();
        if (attachment != null) {
            attachment.enabled = value;
        }
    }

    private ScriptAttachment firstAttachment() {
        ScriptComponent container = world.getComponent(entity, ScriptComponent.class);
        if (container == null || container.attachments.isEmpty()) {
            return null;
        }
        return container.attachments.get(0);
    }
}
