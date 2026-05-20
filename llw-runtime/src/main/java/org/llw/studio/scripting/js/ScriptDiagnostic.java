package org.llw.studio.scripting.js;

/**
 * Last bundle or compile message for a script asset.
 *
 * @param scriptGuid script asset GUID
 * @param error      {@code true} when the message represents a failure
 * @param message    human-readable detail
 */
public record ScriptDiagnostic(String scriptGuid, boolean error, String message) {
}
