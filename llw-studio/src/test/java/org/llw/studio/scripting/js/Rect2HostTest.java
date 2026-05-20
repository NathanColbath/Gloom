package org.llw.studio.scripting.js;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.Test;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.js.bindings.ScriptHostApi;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Rect2HostTest {
    @Test
    void rect2ContainsAndIntersects() {
        Scene scene = new Scene();
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            new ScriptHostApi(context, null, scene, null, Path.of(".")).install();

            boolean inside = context.eval("js", """
                    (function () {
                      const r = new Rect2(0, 0, 10, 10);
                      return r.contains(5, 5);
                    })()
                    """).asBoolean();
            assertTrue(inside);

            boolean outside = context.eval("js", """
                    (function () {
                      const a = new Rect2(0, 0, 10, 10);
                      const b = new Rect2(20, 20, 5, 5);
                      return a.intersects(b);
                    })()
                    """).asBoolean();
            assertFalse(outside);

            boolean overlaps = context.eval("js", """
                    (function () {
                      const a = new Rect2(0, 0, 10, 10);
                      const b = new Rect2(5, 5, 10, 10);
                      return a.intersects(b);
                    })()
                    """).asBoolean();
            assertTrue(overlaps);
        }
    }
}
