package org.llw.studio.scripting;

import java.util.ArrayList;
import java.util.List;

/**
 * ECS component holding one or more {@link ScriptAttachment} instances on an entity.
 */
public final class ScriptComponent implements Cloneable {
    public final List<ScriptAttachment> attachments = new ArrayList<>();
    private int nextSlotId = 1;

    /**
     * @return a deep copy of this component including all attachments and slot ids
     */
    public ScriptComponent copy() {
        ScriptComponent copy = new ScriptComponent();
        int maxSlot = 0;
        for (ScriptAttachment attachment : attachments) {
            copy.attachments.add(attachment.copy());
            maxSlot = Math.max(maxSlot, attachment.slotId);
        }
        copy.nextSlotId = Math.max(nextSlotId, maxSlot + 1);
        return copy;
    }

    /** {@inheritDoc} */
    @Override
    public ScriptComponent clone() {
        return copy();
    }

    /**
     * Appends a new empty attachment with a unique {@link ScriptAttachment#slotId}.
     *
     * @return the new attachment
     */
    public ScriptAttachment addAttachment() {
        ScriptAttachment attachment = new ScriptAttachment();
        attachment.slotId = nextSlotId++;
        attachments.add(attachment);
        return attachment;
    }

    /**
     * @param slotId attachment slot to remove
     * @return {@code true} when an attachment was removed
     */
    public boolean removeAttachment(int slotId) {
        return attachments.removeIf(attachment -> attachment.slotId == slotId);
    }

    /**
     * @param slotId slot id
     * @return attachment with that slot, or {@code null}
     */
    public ScriptAttachment findBySlotId(int slotId) {
        for (ScriptAttachment attachment : attachments) {
            if (attachment.slotId == slotId) {
                return attachment;
            }
        }
        return null;
    }

    /**
     * @param guid script asset GUID
     * @return first attachment with that GUID, or {@code null}
     */
    public ScriptAttachment findByGuid(String guid) {
        if (guid == null || guid.isBlank()) {
            return null;
        }
        for (ScriptAttachment attachment : attachments) {
            if (guid.equals(attachment.scriptGuid)) {
                return attachment;
            }
        }
        return null;
    }

    /**
     * @param guid script asset GUID
     * @return {@code true} when any attachment already references {@code guid}
     */
    public boolean hasGuid(String guid) {
        return findByGuid(guid) != null;
    }

    /**
     * @return {@code true} when at least one attachment has a non-blank script GUID
     */
    public boolean hasAnyScriptReference() {
        for (ScriptAttachment attachment : attachments) {
            if (attachment.hasScriptReference()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ensures {@link #addAttachment()} will not reuse slot ids after deserialization.
     *
     * @param maxSlotId highest slot id present in {@link #attachments}
     */
    public void syncNextSlotId(int maxSlotId) {
        nextSlotId = Math.max(nextSlotId, maxSlotId + 1);
    }

    /**
     * @param guid script asset GUID to assign
     * @return {@code false} when {@code guid} is already used on this entity
     */
    public boolean tryAssignGuid(ScriptAttachment attachment, String guid) {
        if (guid == null || guid.isBlank()) {
            attachment.scriptGuid = "";
            return true;
        }
        ScriptAttachment existing = findByGuid(guid);
        if (existing != null && existing != attachment) {
            return false;
        }
        attachment.scriptGuid = guid;
        return true;
    }
}
