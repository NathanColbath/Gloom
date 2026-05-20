package org.llw.studio.editor.panels;

import org.junit.jupiter.api.Test;
import org.llw.util.log.LogLevel;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsolePanelTest {

    @Test
    void appendCollapsesConsecutiveDuplicateMessages() {
        ConsolePanel panel = new ConsolePanel();
        panel.append(LogLevel.INFO, "tick");
        panel.append(LogLevel.INFO, "tick");
        panel.append(LogLevel.WARN, "warn");
        panel.append(LogLevel.INFO, "tick");

        List<ConsolePanel.ConsoleEntry> entries = new ArrayList<>();
        panel.linesForTest().forEach(entries::add);

        assertEquals(3, entries.size());
        assertEquals(2, entries.get(0).repeatCount());
        assertEquals(LogLevel.INFO, entries.get(0).level());
        assertEquals("tick", entries.get(0).message());
        assertEquals(1, entries.get(1).repeatCount());
        assertEquals(LogLevel.WARN, entries.get(1).level());
        assertEquals(1, entries.get(2).repeatCount());
    }

    @Test
    void appendDoesNotCollapseDifferentLevelsWithSameMessage() {
        ConsolePanel panel = new ConsolePanel();
        panel.append(LogLevel.INFO, "same");
        panel.append(LogLevel.ERROR, "same");

        List<ConsolePanel.ConsoleEntry> entries = new ArrayList<>();
        panel.linesForTest().forEach(entries::add);

        assertEquals(2, entries.size());
        assertEquals(1, entries.get(0).repeatCount());
        assertEquals(1, entries.get(1).repeatCount());
    }
}
