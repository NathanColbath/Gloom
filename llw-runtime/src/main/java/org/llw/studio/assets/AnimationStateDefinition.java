package org.llw.studio.assets;

/**
 * Named state on an {@link AnimationSetDefinition} referencing a clip GUID.
 */
public record AnimationStateDefinition(String name, String clipGuid) {
    public AnimationStateDefinition {
        if (name == null) {
            name = "";
        }
        if (clipGuid == null) {
            clipGuid = "";
        }
    }
}
