package org.llw.studio.scripting.js;

import org.junit.jupiter.api.Test;
import org.llw.render.core.Color;
import org.llw.studio.scripting.js.bindings.GizmosBinding;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptGizmoDrawBufferTest {
    @Test
    void bufferTracksBudgetExceeded() {
        ScriptGizmoDrawBuffer buffer = new ScriptGizmoDrawBuffer();
        GizmosBinding gizmos = new GizmosBinding(buffer, 1f);
        gizmos.setColor(1, 1, 1, 1);
        for (int i = 0; i < ScriptGizmoDrawBuffer.MAX_PRIMITIVES + 5; i++) {
            gizmos.drawLine(0, 0, i, i);
        }
        assertTrue(buffer.budgetExceeded());
    }

    @Test
    void bufferAcceptsPrimitivesUnderBudget() {
        ScriptGizmoDrawBuffer buffer = new ScriptGizmoDrawBuffer();
        GizmosBinding gizmos = new GizmosBinding(buffer, 1f);
        gizmos.drawWireCircle(0, 0, 10);
        assertFalse(buffer.budgetExceeded());
    }
}
