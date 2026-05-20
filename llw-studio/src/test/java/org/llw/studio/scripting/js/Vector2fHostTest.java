package org.llw.studio.scripting.js;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.js.bindings.ScriptHostApi;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Vector2fHostTest {
    @Test
    void vector2fConstructorAndStatics() {
        Scene scene = new Scene();
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            new ScriptHostApi(context, null, scene, null, Path.of(".")).install();

            double length = context.eval("js", "new Vector2f(3, 4).length()").asDouble();
            assertEquals(5.0, length, 0.001);

            double dot = context.eval("js", """
                    (function () {
                      const a = new Vector2f(1, 0);
                      const b = new Vector2f(0, 1);
                      return Vector2f.dot(a, b);
                    })()
                    """).asDouble();
            assertEquals(0.0, dot, 0.001);

            Value legacy = context.eval("js", "Vec2.create(2, 3)");
            assertEquals(2.0, legacy.getMember("x").asDouble(), 0.001);
            assertEquals(3.0, legacy.getMember("y").asDouble(), 0.001);

            String text = context.eval("js", "String(new Vector2f(1, 2))").asString();
            assertTrue(text.contains("1") && text.contains("2"));
        }
    }
}
