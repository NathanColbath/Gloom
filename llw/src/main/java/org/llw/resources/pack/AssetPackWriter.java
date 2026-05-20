package org.llw.resources.pack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds LLWP {@code .pack} files from source files on disk.
 */
public final class AssetPackWriter {
    private AssetPackWriter() {}

    /**
     * Writes a pack file from ordered entries.
     *
     * @param output  destination {@code .pack} path
     * @param entries id → source file metadata (iteration order defines payload layout)
     * @throws IOException if a source file cannot be read
     */
    public static void write(Path output, Map<String, AssetPackManifest.PackEntry> entries) throws IOException {
        Map<String, AssetPackManifest.Entry> manifestEntries = new LinkedHashMap<>();
        int offset = 0;
        ByteBuffer payload = ByteBuffer.allocate(estimatePayloadSize(entries)).order(ByteOrder.LITTLE_ENDIAN);
        for (Map.Entry<String, AssetPackManifest.PackEntry> e : entries.entrySet()) {
            byte[] bytes = Files.readAllBytes(e.getValue().source());
            manifestEntries.put(
                    e.getKey(),
                    new AssetPackManifest.Entry(
                            e.getValue().type(),
                            offset,
                            bytes.length,
                            e.getValue().hint(),
                            e.getValue().fontSize()
                    )
            );
            payload.put(bytes);
            offset += bytes.length;
        }
        String json = ManifestJson.write(manifestEntries);
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        int totalSize = AssetPackFormat.HEADER_SIZE + jsonBytes.length + payload.position();
        ByteBuffer file = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN);
        AssetPackFormat.writeHeader(file, jsonBytes.length);
        file.put(jsonBytes);
        file.put(payload.array(), 0, payload.position());
        Files.write(output, file.array());
    }

    private static int estimatePayloadSize(Map<String, AssetPackManifest.PackEntry> entries) throws IOException {
        int size = 0;
        for (AssetPackManifest.PackEntry entry : entries.values()) {
            size += (int) Files.size(entry.source());
        }
        return Math.max(size, 1);
    }
}
