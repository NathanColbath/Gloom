package org.llw.studio.editor.assets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenSourceIconCatalogTest {
    @Test
    void everyKindMapsToLucideIcon() {
        for (AssetIconKind kind : AssetIconKind.values()) {
            OpenSourceIconSpec spec = OpenSourceIconCatalog.spec(kind);
            assertEquals("lucide", spec.prefix());
            assertFalse(spec.name().isBlank());
        }
    }
}
