package org.llw.studio.scripting.js;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.Test;
import org.llw.studio.scene.Scene;
import org.llw.studio.scripting.js.bindings.ScriptHostApi;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathfHostTest {
    @Test
    void mathfDelegatesToHostMath() {
        Scene scene = new Scene();
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(ScriptHostApi.hostAccess())
                .build()) {
            new ScriptHostApi(context, null, scene, null, Path.of(".")).install();

            double clamped = context.eval("js", "Mathf.clamp(5, 0, 3)").asDouble();
            assertEquals(3.0, clamped, 0.001);

            double lerped = context.eval("js", "Mathf.lerp(0, 10, 0.25)").asDouble();
            assertEquals(2.5, lerped, 0.001);

            double rounded = context.eval("js", "Mathf.round(2.6)").asDouble();
            assertEquals(3.0, rounded, 0.001);

            double floored = context.eval("js", "Mathf.floor(2.9)").asDouble();
            assertEquals(2.0, floored, 0.001);

            double ceiled = context.eval("js", "Math.ceil(2.1)").asDouble();
            assertEquals(3.0, ceiled, 0.001);
        }
    }
}
