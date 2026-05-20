package org.llw.studio.editor.assets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EditorIconDiskCacheTest {
    @Test
    void roundTripsSvgBytes(@TempDir Path dir) {
        EditorIconDiskCache cache = new EditorIconDiskCache(dir);
        OpenSourceIconSpec spec = new OpenSourceIconSpec("lucide", "folder");
        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes();
        cache.write(spec, 32, svg);
        assertArrayEquals(svg, cache.read(spec, 32));
    }
}
