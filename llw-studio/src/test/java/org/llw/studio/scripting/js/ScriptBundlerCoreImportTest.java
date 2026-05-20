package org.llw.studio.scripting.js;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptBundlerCoreImportTest {
    @Test
    void simpleBundleStripsLlwCoreImports() {
        String source = """
                import * as core from "llw.core";

                export default class Example extends core.Script {
                  update() {}
                }
                """;
        String bundled = ScriptBundler.simpleBundle(source);
        assertFalse(bundled.contains("import"));
        assertTrue(bundled.contains("const Script = LLW.Script"));
        assertTrue(bundled.contains("const Vector2f = globalThis.Vector2f"));
    }
}
