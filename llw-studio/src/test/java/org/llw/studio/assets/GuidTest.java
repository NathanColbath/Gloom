package org.llw.studio.assets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GuidTest {
    @Test
    void newGuidIsValid() {
        String guid = Guid.newGuid();
        assertTrue(Guid.isValid(guid));
    }

    @Test
    void rejectsBlank() {
        assertFalse(Guid.isValid(""));
        assertFalse(Guid.isValid(null));
    }
}
