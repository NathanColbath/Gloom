package org.llw.studio.scripting.js;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory last diagnostic per script GUID after bundling or recompilation.
 */
public final class ScriptDiagnostics {
    private static final Map<String, ScriptDiagnostic> BY_GUID = new ConcurrentHashMap<>();

    private ScriptDiagnostics() {
    }

    /**
     * @param scriptGuid  script asset GUID
     * @param diagnostic  diagnostic to store; {@code null} removes the entry
     */
    public static void set(String scriptGuid, ScriptDiagnostic diagnostic) {
        if (scriptGuid == null || scriptGuid.isBlank()) {
            return;
        }
        if (diagnostic == null) {
            BY_GUID.remove(scriptGuid);
        } else {
            BY_GUID.put(scriptGuid, diagnostic);
        }
    }

    /**
     * @param scriptGuid script asset GUID
     * @return last diagnostic, or {@code null}
     */
    public static ScriptDiagnostic get(String scriptGuid) {
        return BY_GUID.get(scriptGuid);
    }

    /** Clears all stored diagnostics. */
    public static void clear() {
        BY_GUID.clear();
    }
}
