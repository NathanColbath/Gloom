package org.llw.studio.editor.assets;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class IconifyIconClientTest {
    @Test
    void buildsIconifyUri() {
        URI uri = IconifyIconClient.iconifyUri(new OpenSourceIconSpec("lucide", "folder"), 32);
        assertEquals(IconifyIconClient.API_HOST, uri.getScheme() + "://" + uri.getAuthority());
        assertTrue(uri.getPath().endsWith("/lucide/folder.svg"));
        assertTrue(uri.getQuery().contains("width=32"));
        assertTrue(uri.getQuery().contains("height=32"));
    }
}
