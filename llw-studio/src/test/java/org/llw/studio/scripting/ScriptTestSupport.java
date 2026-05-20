package org.llw.studio.scripting;

/**
 * Test helpers for {@link ScriptComponent} containers.
 */
public final class ScriptTestSupport {
    private ScriptTestSupport() {
    }

    /**
     * @param scriptGuid script asset GUID for the sole attachment
     * @return container with one attachment
     */
    public static ScriptComponent single(String scriptGuid) {
        ScriptComponent container = new ScriptComponent();
        ScriptAttachment attachment = container.addAttachment();
        attachment.scriptGuid = scriptGuid == null ? "" : scriptGuid;
        attachment.enabled = true;
        return container;
    }

    /**
     * @param container script component container
     * @return first attachment, or {@code null} when empty
     */
    public static ScriptAttachment first(ScriptComponent container) {
        if (container == null || container.attachments.isEmpty()) {
            return null;
        }
        return container.attachments.get(0);
    }
}
